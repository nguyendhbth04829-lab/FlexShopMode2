package com.example.demo.service;

import com.example.demo.dto.ViTriKeKhoForm;
import com.example.demo.entity.KhoHang;
import com.example.demo.entity.ViTriKeKho;
import com.example.demo.repository.KhoHangRepository;
import com.example.demo.repository.ViTriKeKhoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ViTriKeKhoService {

    @Autowired
    private ViTriKeKhoRepository viTriKeKhoRepository;

    @Autowired
    private KhoHangRepository khoHangRepository;

    public List<ViTriKeKho> layTheoKho(Long maKho) {
        return viTriKeKhoRepository.findByKhoHang_MaKho(maKho);
    }

    @Transactional
    public ViTriKeKho themMoi(ViTriKeKhoForm form) {
        KhoHang kho = khoHangRepository.findById(form.getMaKho())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho hàng"));

        ViTriKeKho vt = new ViTriKeKho();
        vt.setKhoHang(kho);
        vt.setMaKhuVuc(form.getMaKhuVuc().trim().toUpperCase());
        return viTriKeKhoRepository.save(vt);
    }

    @Transactional
    public void xoa(Long id) {
        viTriKeKhoRepository.deleteById(id);
    }
}
