package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.service.CsvImportService;
import com.whereismymoney.backend.service.ExcelImportService;
import com.whereismymoney.backend.service.PdfImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final CsvImportService csvImportService;
    private final ExcelImportService excelImportService;
    private final PdfImportService pdfImportService;

    public ImportController(
            CsvImportService csvImportService,
            ExcelImportService excelImportService,
            PdfImportService pdfImportService) {

        this.csvImportService = csvImportService;
        this.excelImportService = excelImportService;
        this.pdfImportService = pdfImportService;
    }

    @PostMapping(value = "/csv", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("portfolioId") Long portfolioId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                csvImportService.importCsv(
                        file,
                        portfolioId,
                        userId));
    }

    @PostMapping(value = "/excel", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("portfolioId") Long portfolioId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                excelImportService.importExcel(
                        file,
                        portfolioId,
                        userId));
    }

    @PostMapping(value = "/pdf", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> importPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("portfolioId") Long portfolioId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                pdfImportService.importPdf(
                        file,
                        portfolioId,
                        userId));
    }
}