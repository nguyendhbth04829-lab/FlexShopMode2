package com.example.demo.service;

import com.example.demo.entity.GianHang;
import com.example.demo.entity.SanPham;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DanhHieuScheduler {

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    // Chạy lúc 00:00 mỗi ngày để quét danh hiệu
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void quetVaCapDanhHieuTuDong() {
        System.out.println("========== BẮT ĐẦU QUÉT DANH HIỆU SHOP TỰ ĐỘNG ==========");
        List<GianHang> shops = gianHangRepository.findAll();
        List<SanPham> tatCaSanPham = sanPhamRepository.findAll();
        
        int count = 0;

        for (GianHang shop : shops) {
            long tongDaBan = tatCaSanPham.stream()
                    .filter(sp -> sp.getMaGianHang() != null && sp.getMaGianHang().equals(shop.getMaGianHang()))
                    .mapToLong(sp -> sp.getTongDaBan() != null ? sp.getTongDaBan() : 0)
                    .sum();
            
            String hangCu = shop.getHangGianHang();
            String hangMoi = "CHUAN";

            if (tongDaBan >= 50) {
                hangMoi = "MALL";
            } else if (tongDaBan >= 10) {
                hangMoi = "YEU_THICH";
            }

            if (!hangMoi.equals(hangCu)) {
                shop.setHangGianHang(hangMoi);
                gianHangRepository.save(shop);
                System.out.println("Cấp danh hiệu [" + hangMoi + "] cho Shop: " + shop.getTenGianHang());
                count++;
            }
        }
        System.out.println("========== HOÀN TẤT! CẬP NHẬT " + count + " GIAN HÀNG ==========");
    }
}
