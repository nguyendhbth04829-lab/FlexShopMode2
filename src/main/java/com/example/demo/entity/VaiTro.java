package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vai_tro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaiTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_vai_tro")
    private Integer maVaiTro;

    @Column(name = "ten_vai_tro", length = 50, nullable = false, unique = true)
    private String tenVaiTro;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    public VaiTro(String tenVaiTro, String moTa) {
        this.tenVaiTro = tenVaiTro;
        this.moTa = moTa;
    }
}
