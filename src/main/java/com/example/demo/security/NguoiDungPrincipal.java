package com.example.demo.security;

import com.example.demo.entity.NguoiDung;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class NguoiDungPrincipal implements UserDetails {

    private final Long maNguoiDung;
    private final String email;
    private final String soDienThoai;
    private final String hoVaTen;

    @JsonIgnore
    private final String matKhau;

    private final Collection<? extends GrantedAuthority> danhSachQuyen;
    private final boolean hoatDong;

    public static NguoiDungPrincipal tao(NguoiDung nguoiDung) {
        List<GrantedAuthority> quyenList = nguoiDung.getDanhSachVaiTro().stream()
                .map(vaiTro -> {
                    String ten = vaiTro.getTenVaiTro();
                    if (!ten.startsWith("ROLE_")) {
                        ten = "ROLE_" + ten;
                    }
                    return new SimpleGrantedAuthority(ten);
                })
                .collect(Collectors.toList());

        boolean isHoatDong = "HOAT_DONG".equalsIgnoreCase(nguoiDung.getTrangThai())
                && (nguoiDung.getDaXoa() == null || !nguoiDung.getDaXoa());

        return NguoiDungPrincipal.builder()
                .maNguoiDung(nguoiDung.getMaNguoiDung())
                .email(nguoiDung.getEmail())
                .soDienThoai(nguoiDung.getSoDienThoai())
                .hoVaTen(nguoiDung.getHoVaTen())
                .matKhau(nguoiDung.getMatKhauMaHoa())
                .danhSachQuyen(quyenList)
                .hoatDong(isHoatDong)
                .build();
    }

    // Alias tương thích
    public Long getId() {
        return maNguoiDung;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return danhSachQuyen;
    }

    @Override
    public String getPassword() {
        return matKhau;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return hoatDong;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return hoatDong;
    }
}
