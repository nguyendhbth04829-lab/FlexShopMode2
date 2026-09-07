package com.example.demo.service;

import com.example.demo.dto.ThuongHieuForm;
import com.example.demo.entity.ThuongHieu;
import com.example.demo.repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ThuongHieuService {

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    public Page<ThuongHieu> layDanhSach(String tuKhoa, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            return thuongHieuRepository.timKiem(tuKhoa.trim(), pageable);
        }
        return thuongHieuRepository.findByDaXoaFalse(pageable);
    }

    public List<ThuongHieu> layTatCa() {
        return thuongHieuRepository.findByDaXoaFalse();
    }

    @Transactional
    public ThuongHieu themMoi(ThuongHieuForm form) {
        if (thuongHieuRepository.findByTenThuongHieuAndDaXoaFalse(form.getTenThuongHieu().trim()).isPresent()) {
            throw new RuntimeException("Tên thương hiệu đã tồn tại!");
        }
        ThuongHieu th = new ThuongHieu();
        th.setTenThuongHieu(form.getTenThuongHieu().trim());
        th.setLinkLogo(form.getLinkLogo());
        th.setDangHoatDong(form.getDangHoatDong() != null ? form.getDangHoatDong() : true);
        return thuongHieuRepository.save(th);
    }

    @Transactional
    public ThuongHieu capNhat(Long id, ThuongHieuForm form) {
        ThuongHieu th = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));
        th.setTenThuongHieu(form.getTenThuongHieu().trim());
        th.setLinkLogo(form.getLinkLogo());
        th.setDangHoatDong(form.getDangHoatDong() != null ? form.getDangHoatDong() : true);
        return thuongHieuRepository.save(th);
    }

    @Transactional
    public void xoa(Long id) {
        ThuongHieu th = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));
        th.setDaXoa(true);
        thuongHieuRepository.save(th);
    }
}
