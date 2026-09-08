package com.example.demo.controller.web;

import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.XacThucService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class XacThucWebController {

    private final XacThucService xacThucService;

    @GetMapping("/")
    public String trangChu() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            List<String> roles = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toList());
            return "redirect:" + xacThucService.xacDinhDuongDanDashboard(roles);
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String trangDangNhap(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            HttpServletResponse response,
            Model model) {

        // Nếu có tham số logout, dọn dẹp ngữ cảnh và cookie còn sót, hiển thị trang đăng nhập ngay lập tức
        if (logout != null) {
            SecurityContextHolder.clearContext();
            xoaCookie(response, "ACCESS_TOKEN");
            xoaCookie(response, "REFRESH_TOKEN");
            model.addAttribute("successMessage", "Bạn đã đăng xuất an toàn khỏi hệ thống FlexShop.");
            return "auth/dang-nhap";
        }

        if (registered != null) {
            model.addAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập để bắt đầu mua bán trên FlexShop.");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            List<String> roles = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toList());
            return "redirect:" + xacThucService.xacDinhDuongDanDashboard(roles);
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin!");
        }

        return "auth/dang-nhap";
    }

    @GetMapping("/register")
    public String trangDangKy() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/";
        }
        return "auth/dang-ky";
    }

    @GetMapping("/forgot-password")
    public String trangQuenMatKhau() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/";
        }
        return "auth/quen-mat-khau";
    }

    @GetMapping({"/logout", "/dang-xuat"})
    public String dangXuat(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = null;
        String refreshToken = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("ACCESS_TOKEN".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                } else if ("REFRESH_TOKEN".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        // 1. Xóa triệt để Cookie ACCESS_TOKEN và REFRESH_TOKEN trên trình duyệt
        xoaCookie(response, "ACCESS_TOKEN");
        xoaCookie(response, "REFRESH_TOKEN");

        // 2. Thu hồi token và blacklist
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (auth != null && auth.getPrincipal() instanceof NguoiDungPrincipal) {
            userId = ((NguoiDungPrincipal) auth.getPrincipal()).getMaNguoiDung();
        }

        xacThucService.dangXuat(accessToken, userId, refreshToken);

        // 3. Xóa SecurityContext và hủy Session
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/login?logout=true";
    }

    private void xoaCookie(HttpServletResponse response, String tenCookie) {
        Cookie cookie = new Cookie(tenCookie, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
    }
}
