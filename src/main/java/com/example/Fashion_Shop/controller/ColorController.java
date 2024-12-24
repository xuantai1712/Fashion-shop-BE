package com.example.Fashion_Shop.controller;

import com.example.Fashion_Shop.dto.attribute_values.ColorDTO;
import com.example.Fashion_Shop.service.Color.ColorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/colors")
@AllArgsConstructor
public class ColorController {

    private ColorService colorService;

    // API để thêm màu mới
   @GetMapping()
   public ResponseEntity<List<ColorDTO>> getAllColors() {
       List<ColorDTO> colors = colorService.getAllColors(); // Lấy danh sách màu sắc từ service
       return ResponseEntity.ok(colors); // Trả về danh sách màu sắc
   }
}
