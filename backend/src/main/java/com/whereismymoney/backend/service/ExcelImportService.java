package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.Transaction;
import com.whereismymoney.backend.repository.TransactionRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ExcelImportService {

    private final TransactionRepository transactionRepository;
    private final OwnershipService ownershipService;

    public ExcelImportService(
            TransactionRepository transactionRepository,
            OwnershipService ownershipService) {
        this.transactionRepository = transactionRepository;
        this.ownershipService = ownershipService;
    }

    public Map<String, Object> importExcel(
            MultipartFile file,
            Long portfolioId,
            Long userId) {

        ownershipService.getOwnedPortfolio(portfolioId, userId);

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Excel file is empty");
        }

        int imported = 0;
        int duplicates = 0;
        int errors = 0;

        List<String> errorDetails = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            Row header = sheet.getRow(0);

            if (header == null) {
                throw new RuntimeException("Excel file has no header row");
            }

            Map<String, Integer> columns = new HashMap<>();

            for (Cell cell : header) {
                columns.put(
                        cell.toString().trim().toLowerCase(),
                        cell.getColumnIndex());
            }

            if (!columns.containsKey("date")
                    || !columns.containsKey("amount")) {

                throw new RuntimeException(
                        "Excel must contain date and amount columns");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                try {
                    Row row = sheet.getRow(i);

                    if (row == null) {
                        continue;
                    }

                    Cell dateCell = row.getCell(columns.get("date"));

                    Cell amountCell = row.getCell(columns.get("amount"));

                    LocalDate date = readDate(dateCell);

                    BigDecimal amount = readAmount(amountCell);

                    if (date == null || amount == null) {
                        errors++;
                        errorDetails.add(
                                "Row " + (i + 1)
                                        + ": invalid date or amount");
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

                    transaction.setTransactionDate(date);

                    transaction.setTotalAmount(
                            absoluteAmount);

                    transaction.setTransactionType(
                            amount.signum() >= 0
                                    ? Transaction.TransactionType.BUY
                                    : Transaction.TransactionType.SELL);

                    transactionRepository.save(transaction);

                    imported++;

                } catch (Exception e) {

                    errors++;

                    errorDetails.add(
                            "Row " + (i + 1)
                                    + ": "
                                    + e.getMessage());
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to process Excel file: "
                            + e.getMessage(),
                    e);
        }

        Map<String, Object> result = new LinkedHashMap<>();

        result.put(
                "message",
                "Excel import completed");

        result.put(
                "imported",
                imported);

        result.put(
                "duplicates",
                duplicates);

        result.put(
                "errors",
                errors);

        result.put(
                "errorDetails",
                errorDetails);

        return result;
    }

    private LocalDate readDate(Cell cell) {

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {

            return cell
                    .getLocalDateTimeCellValue()
                    .toLocalDate();
        }

        String value = cell.toString().trim();

        return LocalDate.parse(value);
    }

    private BigDecimal readAmount(Cell cell) {

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {

            return BigDecimal.valueOf(
                    cell.getNumericCellValue());
        }

        String value = cell.toString()
                .trim()
                .replace(",", "")
                .replace("₹", "");

        return new BigDecimal(value);
    }
}