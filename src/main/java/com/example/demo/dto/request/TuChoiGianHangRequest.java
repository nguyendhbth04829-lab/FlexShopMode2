package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TuChoiGianHangRequest {

    @NotBlank(message = "Lý do từ chối không được để trống")
    @Size(min = 5, max = 255, message = "Lý do từ chối phải có độ dài từ 5 đến 255 ký tự")
    private String lyDoTuChoi;
}
