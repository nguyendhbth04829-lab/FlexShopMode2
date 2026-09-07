package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhanHoiApi<T> {
    private boolean thanhCong;
    private String thongBao;
    private T duLieu;
    @Builder.Default
    private LocalDateTime thoiGian = LocalDateTime.now();

    public static <T> PhanHoiApi<T> thanhCong(String thongBao, T duLieu) {
        return PhanHoiApi.<T>builder()
                .thanhCong(true)
                .thongBao(thongBao)
                .duLieu(duLieu)
                .thoiGian(LocalDateTime.now())
                .build();
    }

    public static <T> PhanHoiApi<T> thanhCong(T duLieu) {
        return thanhCong("Thao tác thành công", duLieu);
    }

    public static <T> PhanHoiApi<T> thatBai(String thongBao) {
        return PhanHoiApi.<T>builder()
                .thanhCong(false)
                .thongBao(thongBao)
                .duLieu(null)
                .thoiGian(LocalDateTime.now())
                .build();
    }

    // Alias tương thích để format json có { success, message, data, timestamp }
    public boolean isSuccess() {
        return thanhCong;
    }

    public String getMessage() {
        return thongBao;
    }

    public T getData() {
        return duLieu;
    }

    public LocalDateTime getTimestamp() {
        return thoiGian;
    }
}
