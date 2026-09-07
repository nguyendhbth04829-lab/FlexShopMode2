package com.example.demo.service;

import com.example.demo.dto.ChiTietPhieuKhoForm;
import com.example.demo.dto.PhieuNhapXuatKhoForm;
import com.example.demo.entity.KhoHang;
import com.example.demo.entity.TonKhoChiTiet;
import com.example.demo.repository.KhoHangRepository;
import com.example.demo.repository.TonKhoChiTietRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class KhoHangServiceTest {

    @Autowired
    private KhoHangService khoHangService;

    @Autowired
    private KhoHangRepository khoHangRepository;

    @Autowired
    private TonKhoChiTietRepository tonKhoRepository;

    @Test
    public void testPhieuXuatKho_VuotQuaTonKho_NenThrowException() {
        // Lưu ý: Test này giả định database đã có sẵn dữ liệu từ file kho-hang-insert.sql
        // Mã kho = 1, Mã biến thể = 1 (Số lượng tồn = 50)
        
        // Kiểm tra kho có tồn tại không để test
        Optional<KhoHang> khoOpt = khoHangRepository.findById(1L);
        if(khoOpt.isEmpty()) {
            System.out.println("Bỏ qua test vì chưa chạy file SQL insert kho hàng");
            return;
        }

        PhieuNhapXuatKhoForm form = new PhieuNhapXuatKhoForm();
        form.setMaKho(1L);
        form.setLoaiPhieu("XUAT_KHO");
        form.setMaChungTuLienQuan("PX-TEST-001");
        
        List<ChiTietPhieuKhoForm> chiTietList = new ArrayList<>();
        ChiTietPhieuKhoForm ct1 = new ChiTietPhieuKhoForm();
        ct1.setMaBienThe(1L);
        ct1.setSoLuong(100); // Tồn kho chỉ có 50, xuất 100 sẽ báo lỗi
        chiTietList.add(ct1);
        
        form.setChiTietList(chiTietList);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            khoHangService.taoPhieuNhapXuatKho(form, 1L);
        });

        assertTrue(exception.getMessage().contains("Số lượng khả dụng không đủ để xuất"));
    }
}
