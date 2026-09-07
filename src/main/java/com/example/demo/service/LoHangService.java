package com.example.demo.service;

import com.example.demo.dto.LoHangForm;
import com.example.demo.entity.BienTheSanPham;
import com.example.demo.entity.LoHangSanPham;
import com.example.demo.repository.BienTheSanPhamRepository;
import com.example.demo.repository.LoHangSanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoHangService {

    @Autowired
    private LoHangSanPhamRepository loHangRepository;

    @Autowired
    private BienTheSanPhamRepository bienTheRepository;

    public List<LoHangSanPham> layTheoBienThe(Long maBienThe) {
        return loHangRepository.findByBienThe_MaBienThe(maBienThe);
    }

    public List<LoHangSanPham> layDanhSachSapHetHan() {
        // Hết hạn trong vòng 30 ngày tới
        return loHangRepository.findByHanSuDungBefore(LocalDate.now().plusDays(30));
    }

    @Transactional
    public LoHangSanPham themMoi(LoHangForm form) {
        if (form.getHanSuDung() != null && form.getHanSuDung().isBefore(form.getNgaySanXuat())) {
            throw new RuntimeException("Hạn sử dụng không được trước ngày sản xuất!");
        }

        BienTheSanPham bt = bienTheRepository.findById(form.getMaBienThe())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));

        LoHangSanPham lo = new LoHangSanPham();
        lo.setBienThe(bt);
        lo.setMaSoLo(form.getMaSoLo().trim());
        lo.setNgaySanXuat(form.getNgaySanXuat());
        lo.setHanSuDung(form.getHanSuDung());

        return loHangRepository.save(lo);
    }
}
