package com.AloisioUmerto.Tesi.DataHandler.config;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TelemetryService {

    private static final String FILE_PATH = "api_telemetry.xlsx";
    private static final String SHEET_NAME = "Telemetry";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AtomicLong requestCount = new AtomicLong(0);
    private final Object lock = new Object();

    // Colonne: #, Timestamp, Method, URL, Status, ResponseTime(ms), RequestSize(bytes), ResponseSize(bytes), ContentType
    private static final String[] HEADERS = {
            "#", "Timestamp", "Method", "URL", "Status",
            "ResponseTime(ms)", "RequestSize(bytes)", "ResponseSize(bytes)", "ContentType"
    };

    /**
     * Registra una chiamata API e salva immediatamente su Excel (append mode).
     */
    public void recordApiCall(String method, String url, int status, long responseTimeMs,
                              long requestSize, long responseSize, String contentType) {
        long count = requestCount.incrementAndGet();
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);

        synchronized (lock) {
            try {
                // Se il file esiste, leggilo e aggiungi una riga
                File file = new File(FILE_PATH);
                try (FileInputStream fis = new FileInputStream(file);
                     Workbook workbook = new XSSFWorkbook(fis)) {

                    Sheet sheet = workbook.getSheet(SHEET_NAME);
                    if (sheet == null) {
                        sheet = workbook.createSheet(SHEET_NAME);
                        createHeaderRow(workbook, sheet);
                    }

                    Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                    fillRow(row, count, timestamp, method, url, status, responseTimeMs, requestSize, responseSize, contentType);

                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        workbook.write(fos);
                    }
                }
            } catch (FileNotFoundException e) {
                // File non esiste: crealo da zero
                createNewFile(count, timestamp, method, url, status, responseTimeMs, requestSize, responseSize, contentType);
            } catch (Exception e) {
                System.err.println("Errore scrittura telemetria: " + e.getMessage());
            }
        }
    }

    private void createNewFile(long count, String timestamp, String method, String url, int status,
                               long responseTimeMs, long requestSize, long responseSize, String contentType) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            createHeaderRow(workbook, sheet);

            Row row = sheet.createRow(1);
            fillRow(row, count, timestamp, method, url, status, responseTimeMs, requestSize, responseSize, contentType);

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            System.err.println("Errore creazione file telemetria: " + e.getMessage());
        }
    }

    private void createHeaderRow(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));

        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        // Auto-size columns
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void fillRow(Row row, long count, String timestamp, String method, String url, int status,
                         long responseTimeMs, long requestSize, long responseSize, String contentType) {
        row.createCell(0).setCellValue(count);
        Cell cell1 = row.createCell(1);
        cell1.setCellValue(timestamp);
        cell1.setCellStyle(row.getSheet().getWorkbook().createCellStyle());

        row.createCell(2).setCellValue(method);
        row.createCell(3).setCellValue(url);
        row.createCell(4).setCellValue(status);
        row.createCell(5).setCellValue(responseTimeMs);
        row.createCell(6).setCellValue(requestSize);
        row.createCell(7).setCellValue(responseSize);
        row.createCell(8).setCellValue(contentType != null ? contentType : "");
    }

    public long getRequestCount() {
        return requestCount.get();
    }
}
