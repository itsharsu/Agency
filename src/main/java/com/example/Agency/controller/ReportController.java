package com.example.Agency.controller;
import com.example.Agency.service.ReportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportReport(
            @RequestParam(required = false) String orderDate,
            @RequestParam(required = false) Boolean shift,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Sales_Report.xlsx");

        reportService.generateExcelReport(orderDate, shift, response);
    }


    @GetMapping("/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> downloadExcelReport(
            @RequestParam boolean shift,
            @RequestParam String date) throws Exception {
        // Parse the date parameter (assumed to be in ISO format, e.g., "2025-02-18")
        LocalDate reportDate = LocalDate.parse(date);
        ByteArrayInputStream in = reportService.generateExcelReport(reportDate,shift);
        String fileName = String.format("sales_report_%s_%s.xlsx", date, shift ? "AM" : "PM");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + fileName);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
