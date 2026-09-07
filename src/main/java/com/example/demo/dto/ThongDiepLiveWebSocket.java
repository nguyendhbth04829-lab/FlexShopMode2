package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Cấu trúc bản tin gửi nhận thời gian thực qua WebSocket Livestream (US-61)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThongDiepLiveWebSocket {

    /**
     * Phân loại sự kiện:
     * - GHIM_SAN_PHAM: Shop vừa ghim sản phẩm giảm giá sốc
     * - BO_GHIM_SAN_PHAM: Shop gỡ ghim sản phẩm
     * - BINH_LUAN_MOI: Có bình luận mới từ khách/shop
     * - THA_TIM: Thả tim tăng lượt tương tác
     * - CAP_NHAT_NGUOI_XEM: Số lượng mắt xem trực tiếp thay đổi
     * - THONG_BAO_MUA_HANG: Có khách vừa đặt mua thành công
     * - TRANG_THAI_LIVE: Cập nhật trạng thái phòng (đang live, kết thúc)
     */
    private String loai;

    private Long maLive;

    private Object duLieu;

    private String thoiGian = LocalDateTime.now().toString();

    public static ThongDiepLiveWebSocketBuilder builder() {
        return new ThongDiepLiveWebSocketBuilder();
    }

    public static class ThongDiepLiveWebSocketBuilder {
        private String loai;
        private Long maLive;
        private Object duLieu;
        private String thoiGian = LocalDateTime.now().toString();

        public ThongDiepLiveWebSocketBuilder() {
        }

        public ThongDiepLiveWebSocketBuilder loai(String loai) {
            this.loai = loai;
            return this;
        }

        public ThongDiepLiveWebSocketBuilder maLive(Long maLive) {
            this.maLive = maLive;
            return this;
        }

        public ThongDiepLiveWebSocketBuilder duLieu(Object duLieu) {
            this.duLieu = duLieu;
            return this;
        }

        public ThongDiepLiveWebSocketBuilder thoiGian(String thoiGian) {
            this.thoiGian = thoiGian;
            return this;
        }

        public ThongDiepLiveWebSocket build() {
            return new ThongDiepLiveWebSocket(this.loai, this.maLive, this.duLieu, this.thoiGian);
        }
    }
}
