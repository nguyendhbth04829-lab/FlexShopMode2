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
        List<DanhMuc> tatCa = danhMucRepository.findByDaXoaFalseOrderByThuTuHienThiAscMaDanhMucDesc();
        
        if (tuKhoa != null && !tuKhoa.isEmpty()) {
            String kw = tuKhoa.toLowerCase();
            List<DanhMuc> filtered = tatCa.stream()
                .filter(d -> d.getTenDanhMuc().toLowerCase().contains(kw))
                .collect(java.util.stream.Collectors.toList());
            return phanTrangList(filtered, page, size);
        }

        List<DanhMuc> kq = new java.util.ArrayList<>();
        // Lấy cấp 1
        for (DanhMuc d1 : tatCa) {
            if (d1.getDanhMucCha() == null) {
                kq.add(d1);
                // Lấy cấp 2 của d1
                for (DanhMuc d2 : tatCa) {
                    if (d2.getDanhMucCha() != null && d2.getDanhMucCha().getMaDanhMuc().equals(d1.getMaDanhMuc())) {
                        kq.add(d2);
                        // Lấy cấp 3 của d2
                        for (DanhMuc d3 : tatCa) {
                            if (d3.getDanhMucCha() != null && d3.getDanhMucCha().getMaDanhMuc().equals(d2.getMaDanhMuc())) {
                                kq.add(d3);
                            }
                        }
                    }
                }
            }
        }
        return phanTrangList(kq, page, size);
    }

    private Page<DanhMuc> phanTrangList(List<DanhMuc> list, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int)pageable.getOffset(), list.size());
        int end = Math.min((start + size), list.size());
        return new org.springframework.data.domain.PageImpl<>(list.subList(start, end), pageable, list.size());
    }
    
    public List<DanhMuc> layTatCaKhongPhanTrang() {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "thuTuHienThi")
                .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "maDanhMuc"));
        return danhMucRepository.findByDaXoaFalseOrderByThuTuHienThiAscMaDanhMucDesc();
    }

    @Transactional
    public DanhMuc themMoi(DanhMucForm form) {
        if (danhMucRepository.existsByTenDanhMucAndDaXoaFalse(form.getTenDanhMuc().trim())) {
            throw new RuntimeException("Tên danh mục '" + form.getTenDanhMuc().trim() + "' đã tồn tại trong hệ thống.");
        }

        DanhMuc d = new DanhMuc();
        d.setTenDanhMuc(form.getTenDanhMuc().trim());
        
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
        if (danhMucRepository.existsByTenDanhMucAndMaDanhMucNotAndDaXoaFalse(form.getTenDanhMuc().trim(), id)) {
            throw new RuntimeException("Tên danh mục '" + form.getTenDanhMuc().trim() + "' đã tồn tại trong hệ thống.");
        }

        DanhMuc d = danhMucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        
        d.setTenDanhMuc(form.getTenDanhMuc().trim());
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
