package com.example.Fashion_Shop.controller;

import com.example.Fashion_Shop.response.statistic.MonthlyRevenueResponse;
import com.example.Fashion_Shop.response.statistic.TopSellingResponse;
import com.example.Fashion_Shop.service.statistic.StatisticService;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/statistics")
@AllArgsConstructor
public class StatisticController {
    private final StatisticService statisticsService;

    @GetMapping("/top-selling-sku")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<TopSellingResponse>> getTopSellingSKU(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(statisticsService.getTopSellingSKU(month, year));
    }

    @GetMapping("/monthly-revenue")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<MonthlyRevenueResponse>> getMonthlyRevenue(@RequestParam("year") int year) {
        List<MonthlyRevenueResponse> revenueData = statisticsService.getMonthlyRevenue(year);
        return ResponseEntity.ok(revenueData);
    }

    @PostMapping("/export-revenue")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ByteArrayResource> exportRevenueToDirectory(@RequestParam("year") int year) {
        try {
            // Fetch the monthly revenue data
            List<MonthlyRevenueResponse> monthlyRevenues = statisticsService.getMonthlyRevenue(year);

            // Create the Excel workbook
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("Doanh Thu "+year);

            Map<String, Object[]> result = new LinkedHashMap<>();
            result.put("1", new Object[]{"Tháng", "Tổng Doanh Thu (VNĐ)"});

// Tạo một map tháng từ 1 đến 12
            Map<Integer, Double> monthlyRevenueMap = new HashMap<>();
            for (MonthlyRevenueResponse revenue : monthlyRevenues) {
                monthlyRevenueMap.put(revenue.getMonth(), revenue.getTotalRevenue());
            }

// Thêm tất cả các tháng vào Excel, bao gồm cả tháng không có doanh thu
            for (int month = 1; month <= 12; month++) {
                Double totalRevenue = monthlyRevenueMap.getOrDefault(month, 0.0); // Nếu không có doanh thu, mặc định là 0
                result.put(String.valueOf(month + 1), new Object[]{
                        "Tháng " + month,
                        totalRevenue
                });
            }

            applyFormattingAndFill(workbook, sheet, result);

            // Write the workbook to a ByteArrayOutputStream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);
            workbook.close();

            // Set HTTP headers to trigger file download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DoanhThu_" + year + ".xls");

            // Convert ByteArrayOutputStream to a ByteArrayResource
            ByteArrayResource resource = new ByteArrayResource(byteArrayOutputStream.toByteArray());

            // Return the file as a Resource (ByteArrayResource is a subtype of Resource)
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);  // No need for explicit cast to Resource
        } catch (IOException e) {
            // In case of an error, return a 500 Internal Server Error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    private void applyFormattingAndFill(Workbook workbook, Sheet sheet, Map<String, Object[]> data) {
        // Create styles
        CellStyle headerStyle = workbook.createCellStyle();
        HSSFFont headerFont = ((HSSFWorkbook) workbook).createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setWrapText(true);

        // Create a data style for general data cells
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // Create a number style for formatting numbers with commas
        CellStyle numberStyle = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        numberStyle.setDataFormat(creationHelper.createDataFormat().getFormat("#,##0")); // Format numbers with commas

        // Fill data into the sheet
        Set<String> keyset = data.keySet();
        int rownum = 0;

        for (String key : keyset) {
            Row row = sheet.createRow(rownum++);
            Object[] objArr = data.get(key);
            int cellnum = 0;

            for (Object obj : objArr) {
                Cell cell = row.createCell(cellnum++);

                if (obj instanceof String) {
                    cell.setCellValue((String) obj);
                    cell.setCellStyle(rownum == 1 ? headerStyle : dataStyle); // Header for the first row
                } else if (obj instanceof Double) {
                    cell.setCellValue((Double) obj);
                    cell.setCellStyle(numberStyle); // Apply the number style for Double values
                }
            }
        }

        // Auto-size all columns
        for (int i = 0; i < data.get("1").length; i++) {
            sheet.autoSizeColumn(i);
        }
    }


}
