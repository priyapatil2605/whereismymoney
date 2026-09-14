package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.Transaction;
import com.whereismymoney.backend.repository.TransactionRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class PdfImportService {

    private final TransactionRepository transactionRepository;
    private final OwnershipService ownershipService;

    public PdfImportService(
            TransactionRepository transactionRepository,
            OwnershipService ownershipService) {

        this.transactionRepository = transactionRepository;
        this.ownershipService = ownershipService;
    }

    public Map<String, Object> importPdf(
            MultipartFile file,
            Long portfolioId,
            Long userId) {

        ownershipService.getOwnedPortfolio(
                portfolioId,
                userId);

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("PDF file is empty");
        }

        int imported = 0;
        int duplicates = 0;

        try {

            byte[] bytes = file.getBytes();

            try (PDDocument document = Loader.loadPDF(bytes)) {

                PDFTextStripper stripper = new PDFTextStripper();

                String text = stripper.getText(document);

                List<String> lines = Arrays.asList(
                        text.split("\\r?\\n"));

                for (String line : lines) {

                    line = line.trim();

                    if (line.isEmpty()) {
                        continue;
                    }

                    String[] parts = line.split("\\s+");

                    if (parts.length < 2) {
                        continue;
                    }

                    LocalDate date;

                    try {

                        date = LocalDate.parse(parts[0]);

                    } catch (Exception ignored) {

                        continue;
                    }

                    BigDecimal amount;

                    try {

                        String amountText = parts[parts.length - 1]
                                .replace(",", "")
                                .replace("₹", "");

                        amount = new BigDecimal(amountText);

                    } catch (Exception ignored) {

                        continue;
                    }

                    BigDecimal absoluteAmount = amount.abs();

                    if (transactionRepository
                            .existsByPortfolioIdAndTransactionDateAndTotalAmount(
                                    portfolioId,
                                    date,
                                    absoluteAmount)) {

                        duplicates++;
                        continue;
                    }

                    Transaction transaction = new Transaction();

                    transaction.setPortfolio(
                            ownershipService.getOwnedPortfolio(
                                    portfolioId,
                                    userId));

                    transaction.setTransactionDate(
                            date);

                    transaction.setTotalAmount(
                            absoluteAmount);

                    transaction.setTransactionType(
                            amount.signum() >= 0
                                    ? Transaction.TransactionType.BUY
                                    : Transaction.TransactionType.SELL);

                    transactionRepository.save(
                            transaction);

                    imported++;
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to process PDF: "
                            + e.getMessage(),
                    e);
        }

        Map<String, Object> result = new LinkedHashMap<>();

        result.put(
                "message",
                "PDF import completed");

        result.put(
                "imported",
                imported);

        result.put(
                "duplicates",
                duplicates);

        return result;
    }
}