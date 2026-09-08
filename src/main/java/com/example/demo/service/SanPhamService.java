package com.example.demo.service;

import com.example.demo.dto.BienTheForm;
import com.example.demo.dto.SanPhamForm;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class SanPhamService {

    @Autowired private SanPhamRepository sanPhamRepository;
    @Autowired private DanhMucRepository danhMucRepository;
    @Autowired private BienTheSanPhamRepository bienTheRepository;
    @Autowired private LichSuGiaBienTheRepository lichSuGiaRepository;
    @Autowired private HinhAnhSanPhamRepository hinhAnhRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private com.example.demo.repository.ThuocTinhSanPhamRepository thuocTinhSanPhamRepository;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public String taoSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    public Page<SanPham> layDanhSach(String tuKhoa, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (tuKhoa != null && !tuKhoa.isEmpty()) {
            return sanPhamRepository.timKiemSanPham(tuKhoa, pageable);
        }
        return sanPhamRepository.findByDaXoaFalse(pageable);
    }

    @Transactional
    public SanPham themSanPham(SanPhamForm form) {
        DanhMuc dm = danhMucRepository.findById(form.getMaDanhMuc())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        SanPham sp = new SanPham();
        sp.setMaGianHang(form.getMaGianHang());
        sp.setDanhMuc(dm);
        sp.setTenSanPham(form.getTenSanPham());
        
        String slug = taoSlug(form.getTenSanPham()) + "-" + System.currentTimeMillis();
        sp.setDuongDanSlug(slug);
        
        sp.setMoTaNgan(form.getMoTaNgan());
        sp.setMoTaChiTiet(form.getMoTaChiTiet());
        sp.setGiaCoBan(form.getGiaCoBan());
        sp.setCanNangGram(form.getCanNangGram());
        sp.setChieuDaiCm(form.getChieuDaiCm());
        sp.setChieuRongCm(form.getChieuRongCm());
        sp.setChieuCaoCm(form.getChieuCaoCm());

        SanPham savedSp = sanPhamRepository.save(sp);

        // Xử lý upload danh sách ảnh (US-13: Max 9 ảnh)
        if (form.getFileAnhList() != null && !form.getFileAnhList().isEmpty()) {
            int thuTu = 1;
            for (MultipartFile file : form.getFileAnhList()) {
                if (file != null && !file.isEmpty()) {
                    if (thuTu > 9) {
                        break; // Chỉ cho phép tối đa 9 ảnh
                    }
                    String fileName = fileStorageService.luuFile(file);
                    HinhAnhSanPham hinhAnh = new HinhAnhSanPham();
                    hinhAnh.setSanPham(savedSp);
                    hinhAnh.setLinkAnh(fileName);
                    hinhAnh.setLaAnhChinh(thuTu == 1);
                    hinhAnh.setThuTuHienThi(thuTu);
                    hinhAnhRepository.save(hinhAnh);
                    thuTu++;
                }
            }
        }

        return savedSp;
    }

    @Transactional
    public BienTheSanPham themBienThe(BienTheForm form) {
        // Validate US-13: SKU Unique
        if (bienTheRepository.existsByMaSku(form.getMaSku())) {
            throw new RuntimeException("Mã SKU đã tồn tại trên hệ thống!");
        }

        // Validate Giá gốc >= Giá bán
        if (form.getGiaGoc() != null && form.getGiaGoc().compareTo(form.getGiaBan()) < 0) {
            throw new RuntimeException("Giá gốc phải lớn hơn hoặc bằng giá bán!");
        }

        SanPham sp = sanPhamRepository.findById(form.getMaSanPham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        BienTheSanPham bt = new BienTheSanPham();
        bt.setSanPham(sp);
        bt.setMaSku(form.getMaSku());
        bt.setTenBienThe(form.getTenBienThe());
        bt.setGiaBan(form.getGiaBan());
        bt.setGiaGoc(form.getGiaGoc());
        bt.setLinkAnh(form.getLinkAnh());

        return bienTheRepository.save(bt);
    }

    @Transactional
    public BienTheSanPham capNhatGiaBienThe(Long idBienThe, BigDecimal giaMoi, Long nguoiThucHien) {
        BienTheSanPham bt = bienTheRepository.findById(idBienThe)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));

        if (giaMoi.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá bán phải lớn hơn 0");
        }
        if (bt.getGiaGoc() != null && bt.getGiaGoc().compareTo(giaMoi) < 0) {
            throw new RuntimeException("Giá gốc đang là " + bt.getGiaGoc() + ", không thể set giá bán cao hơn giá gốc!");
        }

        BigDecimal giaCu = bt.getGiaBan();
        
        if (giaCu.compareTo(giaMoi) != 0) {
            // Cập nhật giá mới
            bt.setGiaBan(giaMoi);
            bienTheRepository.save(bt);

            // Ghi lịch sử giá (US-14)
            LichSuGiaBienThe ls = new LichSuGiaBienThe();
            ls.setBienThe(bt);
            ls.setGiaCu(giaCu);
            ls.setGiaMoi(giaMoi);
            ls.setNguoiThayDoi(nguoiThucHien);
            lichSuGiaRepository.save(ls);
        }

        return bt;
    }

    @Transactional
    public void themHinhAnh(Long maSanPham, String linkAnh, boolean laAnhChinh) {
        // Validate US-12: Tối đa 9 ảnh (1 chính + 8 chi tiết)
        int count = hinhAnhRepository.countBySanPham_MaSanPham(maSanPham);
        if (count >= 9) {
            throw new RuntimeException("Sản phẩm chỉ được phép có tối đa 9 ảnh (1 chính + 8 chi tiết).");
        }

        SanPham sp = sanPhamRepository.findById(maSanPham)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        HinhAnhSanPham ha = new HinhAnhSanPham();
        ha.setSanPham(sp);
        ha.setLinkAnh(linkAnh);
        ha.setLaAnhChinh(laAnhChinh);
        ha.setThuTuHienThi(count + 1);

        hinhAnhRepository.save(ha);
    }

    public SanPham timTheoId(Long id) {
        return sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
    }

    public java.util.List<com.example.demo.entity.BienTheSanPham> layDanhSachBienThe(Long maSanPham) {
        return bienTheRepository.findBySanPham_MaSanPhamAndDaXoaFalse(maSanPham);
    }

    public java.util.List<com.example.demo.entity.ThuocTinhSanPham> layDanhSachThuocTinh(Long maSanPham) {
        // Trả về danh sách thuộc tính động của sản phẩm
        return thuocTinhSanPhamRepository.findBySanPham_MaSanPham(maSanPham);
    }
}
