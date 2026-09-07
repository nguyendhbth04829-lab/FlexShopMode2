package com.example.demo.service;

import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.entity.NguoiDung;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.security.NguoiDungPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NguoiDungService {

    private final NguoiDungRepository nguoiDungRepository;
    private final XacThucService xacThucService;

    @Transactional(readOnly = true)
    public NguoiDungResponse layNguoiDungHienTai() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new NgoaiLeUngDung("Chưa xác thực danh tính người dùng", HttpStatus.UNAUTHORIZED);
        }

        NguoiDungPrincipal principal = (NguoiDungPrincipal) authentication.getPrincipal();
        NguoiDung nguoiDung = nguoiDungRepository.findById(principal.getMaNguoiDung())
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND));

        return xacThucService.chuyenSangNguoiDungResponse(nguoiDung);
    }
}
