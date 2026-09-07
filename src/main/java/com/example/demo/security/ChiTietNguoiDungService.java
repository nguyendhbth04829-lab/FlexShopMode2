package com.example.demo.security;

import com.example.demo.entity.NguoiDung;
import com.example.demo.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChiTietNguoiDungService implements UserDetailsService {

    private final NguoiDungRepository nguoiDungRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        NguoiDung nguoiDung = nguoiDungRepository.findByIdentifier(identifier.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với tài khoản: " + identifier));
        return NguoiDungPrincipal.tao(nguoiDung);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + id));
        return NguoiDungPrincipal.tao(nguoiDung);
    }
}
