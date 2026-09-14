package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.EntryType;
import com.whereismymoney.backend.entity.Portfolio;
import com.whereismymoney.backend.entity.Transaction;
import com.whereismymoney.backend.repository.PortfolioRepository;
import com.whereismymoney.backend.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvImportService {

    private final TransactionRepository transactionRepository;
    private final PortfolioRepository portfolioRepository;

    public CsvImportService(
            TransactionRepository transactionRepository,
            PortfolioRepository portfolioRepository) {

        this.transactionRepository = transactionRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public Map<String, Object> importCsv(
            MultipartFile file,
            Long portfolioId,
            Long userId) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CSV file is required");
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !filename.toLowerCase().endsWith(".csv")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only CSV files are supported");
        }

        Portfolio portfolio = portfolioRepository
                .findByIdAndUserId(
                        portfolioId,
                        userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You do not have access to this portfolio"));

        int imported = 0;
        int duplicates = 0;
        int skipped = 0;

        List<String> errors = new ArrayList<>();

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                file.getInputStream(),
                                StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();

            if (headerLine == null ||
                    headerLine.isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "CSV file is empty");
            }

            String[] headers = headerLine
                    .toLowerCase()
                    .split(",");

            Map<String, Integer> columns = new HashMap<>();

            for (int i = 0; i < headers.length; i++) {

                columns.put(
                        headers[i]
                                .trim()
                                .replace("\"", ""),
                        i);
            }

            requireColumn(columns, "date");
            requireColumn(columns, "amount");

            String line;

            int rowNumber = 1;

            while ((line = reader.readLine()) != null) {

                rowNumber++;

                if (line.isBlank()) {
                    continue;
                }

                try {

                    String[] values = line.split(",", -1);

                    String dateValue = getValue(
                            values,
                            columns,
                            "date");

                    String amountValue = getValue(
                            values,
                            columns,
                            "amount");

                    if (dateValue.isBlank()) {

                        throw new IllegalArgumentException(
                                "Date is missing");
                    }

                    if (amountValue.isBlank()) {

                        throw new IllegalArgumentException(
                                "Amount is missing");
                    }

                    LocalDate transactionDate = LocalDate.parse(
                            clean(dateValue));

                    BigDecimal amount = new BigDecimal(
                            clean(amountValue)
                                    .replace(",", ""));

                    if (amount.compareTo(
                            BigDecimal.ZERO) == 0) {

                        skipped++;
                        continue;
                    }

                    BigDecimal absoluteAmount = amount.abs();

                    String type = getOptionalValue(
                            values,
                            columns,
                            "type");

                    EntryType entryType = determineEntryType(
                            amount,
                            type);

                    boolean duplicate = transactionRepository
                            .existsByPortfolioIdAndTransactionDateAndTotalAmount(
                                    portfolioId,
                                    transactionDate,
                                    absoluteAmount);

                    if (duplicate) {

                        duplicates++;
                        continue;
                    }

                    Transaction transaction = new Transaction();

                    transaction.setPortfolio(
                            portfolio);

                    transaction.setTransactionType(
                            amount.signum() > 0
                                    ? Transaction.TransactionType.BUY
                                    : Transaction.TransactionType.SELL);

                    transaction.setEntryType(
                            entryType);

                    transaction.setQuantity(
                            BigDecimal.ONE);

                    transaction.setPricePerUnit(
                            absoluteAmount);

                    transaction.setTransactionDate(
                            transactionDate);

                    transaction.setTotalAmount(
                            absoluteAmount);

                    transactionRepository.save(
                            transaction);

                    imported++;

                } catch (Exception rowException) {

                    skipped++;

                    errors.add(
                            "Row " +
                                    rowNumber +
                                    ": " +
                                    rowException.getMessage());
                }
            }

        } catch (ResponseStatusException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to process CSV file: " +
                            ex.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();

        result.put(
                "portfolioId",
                portfolioId);

        result.put(
                "imported",
                imported);

        result.put(
                "duplicates",
                duplicates);

        result.put(
                "skipped",
                skipped);

        result.put(
                "errors",
                errors);

        return result;
    }

    private void requireColumn(
            Map<String, Integer> columns,
            String column) {

        if (!columns.containsKey(column)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CSV must contain a '" +
                            column +
                            "' column");
        }
    }

    private String getValue(
            String[] values,
            Map<String, Integer> columns,
            String column) {

        Integer index = columns.get(column);

        if (index == null ||
                index >= values.length) {

            return "";
        }

        return clean(values[index]);
    }

    private String getOptionalValue(
            String[] values,
            Map<String, Integer> columns,
            String column) {

        if (!columns.containsKey(column)) {
            return "";
        }

        return getValue(
                values,
                columns,
                column);
    }

    private String clean(String value) {

        return value
                .trim()
                .replace("\"", "");
    }

    private EntryType determineEntryType(
            BigDecimal amount,
            String type) {

        String normalized = type == null
                ? ""
                : type.trim().toUpperCase();

        if (normalized.contains("INCOME") ||
                normalized.contains("CREDIT") ||
                normalized.contains("SALARY")) {

            return EntryType.INCOME;
        }

        if (normalized.contains("EXPENSE") ||
                normalized.contains("DEBIT")) {

            return EntryType.EXPENSE;
        }

        return amount.signum() > 0
                ? EntryType.INCOME
                : EntryType.EXPENSE;
    }
}