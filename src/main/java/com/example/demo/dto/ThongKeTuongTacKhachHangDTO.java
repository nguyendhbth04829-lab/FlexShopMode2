package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thống kê hoạt động tương tác của Khách hàng (Wishlist, Follow Shop, Q&A)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeTuongTacKhachHangDTO {

    private Long tongSoSanPhamYeuThich;
    private Long tongSoShopTheoDoi;
    private Long tongSoCauHoiDaDat;
}
