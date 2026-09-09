package com.example.demo.service;

import com.example.demo.entity.GianHang;
import com.example.demo.entity.LichSuSaoQuaTa;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.LichSuSaoQuaTaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaoQuaTaService {

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private LichSuSaoQuaTaRepository saoQuaTaRepository;

    @Transactional
    public void ghiNhanPhatSaoQuaTa(Long maGianHang, int soDiemPhat, String lyDo, String loaiViPham) {
        if (soDiemPhat <= 0) {
            throw new RuntimeException("Số điểm phạt phải lớn hơn 0");
        }

        GianHang gh = gianHangRepository.findById(maGianHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gian hàng"));

        // Cộng điểm phạt cho Gian Hàng
        gh.setDiemSaoQuaTa(gh.getDiemSaoQuaTa() + soDiemPhat);
        
        // Logic nghiệp vụ: Nếu điểm phạt vượt mốc, hạ hạng gian hàng
        if (gh.getDiemSaoQuaTa() >= 10 && gh.getHangGianHang().equals("YEU_THICH")) {
            gh.setHangGianHang("CHUAN"); // Mất danh hiệu Shop Yêu Thích
        }
        
        gianHangRepository.save(gh);

        // Ghi lại lịch sử
        LichSuSaoQuaTa ls = new LichSuSaoQuaTa();
        ls.setMaGianHang(maGianHang);
        ls.setSoDiemPhat(soDiemPhat);
        ls.setLyDo(lyDo);
        ls.setLoaiViPham(loaiViPham);
        
        saoQuaTaRepository.save(ls);
    }
}
