package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Product Backlog test page: US-01 -> US-68, 13 modules, 5 roles.
 * Chi phuc vu test + demo, khong nghiep vu.
 */
@Controller
public class ProductBacklogController {

    @GetMapping("/backlog")
    public String productBacklog() {
        return "product-backlog";
    }
}
