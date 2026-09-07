package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class NgoaiLeUngDung extends RuntimeException {
    private final HttpStatus maTrangThai;

    public NgoaiLeUngDung(String thongBao) {
        super(thongBao);
        this.maTrangThai = HttpStatus.BAD_REQUEST;
    }

    public NgoaiLeUngDung(String thongBao, HttpStatus maTrangThai) {
        super(thongBao);
        this.maTrangThai = maTrangThai;
    }

    public HttpStatus getMaTrangThai() {
        return maTrangThai;
    }

    public HttpStatus getStatus() {
        return maTrangThai;
    }
}
