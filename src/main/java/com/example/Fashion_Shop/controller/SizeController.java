package com.example.Fashion_Shop.controller;

import com.example.Fashion_Shop.dto.attribute_values.ColorDTO;
import com.example.Fashion_Shop.dto.attribute_values.SizeDTO;
import com.example.Fashion_Shop.service.Color.ColorService;
import com.example.Fashion_Shop.service.Size.SizeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/size")
@AllArgsConstructor
public class SizeController {

    private SizeService sizeService;

    // API để thêm màu mới
    @GetMapping()
    public ResponseEntity<List<SizeDTO>> getAllSizes() {
        List<SizeDTO> size = sizeService.getAllSizes(); // Lấy danh sách màu sắc từ service
        return ResponseEntity.ok(size); // Trả về danh sách màu sắc
    }
}
