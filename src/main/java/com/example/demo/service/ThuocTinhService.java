package com.example.demo.service;

import com.example.demo.dto.ThuocTinhForm;
import com.example.demo.entity.GiaTriThuocTinh;
import com.example.demo.entity.SanPham;
import com.example.demo.entity.ThuocTinhSanPham;
import com.example.demo.repository.SanPhamRepository;
import com.example.demo.repository.ThuocTinhSanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ThuocTinhService {

    @Autowired
    private ThuocTinhSanPhamRepository thuocTinhRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    public List<ThuocTinhSanPham> layTheoSanPham(Long maSanPham) {
        return thuocTinhRepository.findBySanPham_MaSanPham(maSanPham);
    }

    @Transactional
    public ThuocTinhSanPham themThuocTinh(ThuocTinhForm form) {
        SanPham sp = sanPhamRepository.findById(form.getMaSanPham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        ThuocTinhSanPham tt = new ThuocTinhSanPham();
        tt.setSanPham(sp);
        tt.setTenThuocTinh(form.getTenThuocTinh().trim());

        if (form.getDanhSachGiaTri() != null) {
            for (String val : form.getDanhSachGiaTri()) {
                if (val != null && !val.trim().isEmpty()) {
                    GiaTriThuocTinh gt = new GiaTriThuocTinh();
                    gt.setThuocTinh(tt);
                    gt.setGiaTri(val.trim());
                    tt.getDanhSachGiaTri().add(gt);
                }
            }
        }

        return thuocTinhRepository.save(tt);
    }

    @Transactional
    public void xoaThuocTinh(Long id) {
        thuocTinhRepository.deleteById(id);
    }
}
