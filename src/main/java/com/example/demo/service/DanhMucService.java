package com.example.demo.service;

import com.example.demo.dto.DanhMucForm;
import com.example.demo.entity.DanhMuc;
import com.example.demo.repository.DanhMucRepository;
import com.example.demo.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class DanhMucService {

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public String taoSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    public Page<DanhMuc> layDanhSach(String tuKhoa, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (tuKhoa != null && !tuKhoa.isEmpty()) {
            return danhMucRepository.timKiemDanhSach(tuKhoa, pageable);
        }
        return danhMucRepository.findByDaXoaFalse(pageable);
    }
    
    public List<DanhMuc> layTatCaKhongPhanTrang() {
        return danhMucRepository.findByDaXoaFalse();
    }

    @Transactional
    public DanhMuc themMoi(DanhMucForm form) {
        DanhMuc d = new DanhMuc();
        d.setTenDanhMuc(form.getTenDanhMuc());
        
        // Tự động sinh Slug duy nhất
        String slug = taoSlug(form.getTenDanhMuc());
        int count = 1;
        while (danhMucRepository.findByDuongDanSlugAndDaXoaFalse(slug).isPresent()) {
            slug = taoSlug(form.getTenDanhMuc()) + "-" + count;
            count++;
        }
        d.setDuongDanSlug(slug);
        d.setLinkIcon(form.getLinkIcon());
        d.setThuTuHienThi(form.getThuTuHienThi() != null ? form.getThuTuHienThi() : 0);
        d.setDangHoatDong(form.getDangHoatDong() != null ? form.getDangHoatDong() : true);

        if (form.getMaDanhMucCha() != null) {
            DanhMuc cha = danhMucRepository.findById(form.getMaDanhMucCha())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha"));
            
            // Validate đệ quy: Độ sâu tối đa 3 cấp
            if (cha.getCapDo() >= 3) {
                throw new RuntimeException("Độ sâu của cây danh mục tối đa là 3 cấp. Không thể thêm danh mục con cho danh mục cấp 3.");
            }
            d.setDanhMucCha(cha);
            d.setCapDo(cha.getCapDo() + 1);
        } else {
            d.setCapDo(1);
        }

        return danhMucRepository.save(d);
    }

    @Transactional
    public DanhMuc capNhat(Long id, DanhMucForm form) {
        DanhMuc d = danhMucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        
        d.setTenDanhMuc(form.getTenDanhMuc());
        d.setLinkIcon(form.getLinkIcon());
        d.setThuTuHienThi(form.getThuTuHienThi() != null ? form.getThuTuHienThi() : 0);
        d.setDangHoatDong(form.getDangHoatDong() != null ? form.getDangHoatDong() : true);

        if (form.getMaDanhMucCha() != null && !form.getMaDanhMucCha().equals(d.getMaDanhMuc())) {
            DanhMuc cha = danhMucRepository.findById(form.getMaDanhMucCha())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha"));
            
            if (cha.getCapDo() >= 3) {
                throw new RuntimeException("Độ sâu tối đa là 3 cấp.");
            }
            d.setDanhMucCha(cha);
            d.setCapDo(cha.getCapDo() + 1);
        } else if (form.getMaDanhMucCha() == null) {
            d.setDanhMucCha(null);
            d.setCapDo(1);
        }

        return danhMucRepository.save(d);
    }

    @Transactional
    public void xoa(Long id) {
        DanhMuc d = danhMucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
                
        // Kiểm tra xem danh mục có danh mục con không
        List<DanhMuc> con = danhMucRepository.findByDanhMucCha_MaDanhMucAndDaXoaFalse(id);
        if (!con.isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục đang có danh mục con.");
        }
        
        // Kiểm tra xem danh mục có sản phẩm không (US-07 rule)
        boolean coSanPham = sanPhamRepository.existsByDanhMuc_MaDanhMucAndDaXoaFalse(id);
        if (coSanPham) {
            throw new RuntimeException("Không cho phép xóa Danh mục do đang có sản phẩm thuộc danh mục này.");
        }

        d.setDaXoa(true);
        danhMucRepository.save(d);
    }
}
