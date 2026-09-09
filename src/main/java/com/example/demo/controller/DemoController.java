package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Cong test/demo: liet ke 11 US Logistics (US-31 -> US-41) theo role.
 * Chi phuc vu test + demo, khong nghiep vu.
 */
@Controller
public class DemoController {

    @GetMapping("/demo")
    public String demo() {
        return "demo";
    }
}
