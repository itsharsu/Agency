package com.example.Agency.service;

import com.example.Agency.dto.ShopReport;
import com.example.Agency.repository.OrderRepository;
import com.example.Agency.repository.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final OrderRepository orderRepository;

    public ReportService(final ReportRepository reportRepository, final OrderRepository orderRepository) {
        this.reportRepository = reportRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Generates an Excel report and writes it to the HttpServletResponse.
     *
     * @param orderDate the order date in String format
     * @param shift     the shift indicator (true for morning, false for evening)
     * @param response  HttpServletResponse to write the report to
     * @throws IOException if an I/O error occurs
     */
    public void generateExcelReport(final String orderDate, final Boolean shift, final HttpServletResponse response) throws IOException {
        final List<Object[]> results = reportRepository.fetchReportData(orderDate, shift);

        try (Workbook workbook = new XSSFWorkbook()) {
            final Sheet sheet = workbook.createSheet("Sales Report");

            // Create cell styles
            final CellStyle boldStyle = createBoldCellStyle(workbook);

            // Supplier's Shop Name Header (merged A1 to F1)
            final Row shopNameRow = sheet.createRow(0);
            final Cell shopNameCell = shopNameRow.createCell(0);
            shopNameCell.setCellValue("Supplier: XYZ Supplier Shop");
            shopNameCell.setCellStyle(boldStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // Date and Shift Header
            final Row dateShiftRow = sheet.createRow(1);
            dateShiftRow.createCell(0).setCellValue("Date: " + orderDate);
            dateShiftRow.createCell(1).setCellValue("Shift: " + (shift ? "AM" : "PM"));

            // Column Headers (starting Row 4)
            final Row headerRow = sheet.createRow(3);
            final String[] headers = {"Date", "Shift", "Product Name", "Quantity", "Total Amount", "Total Cost"};
            for (int i = 0; i < headers.length; i++) {
                final Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(boldStyle);
            }

            // Data Rows (starting Row 5)
            int rowNum = 4;
            BigDecimal netTotalAmount = BigDecimal.ZERO;
            BigDecimal netCostAmount = BigDecimal.ZERO;

            for (final Object[] row : results) {
                final Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue((String) row[0]);

                final Boolean isMorning = (row[1] instanceof Boolean)
                        ? (Boolean) row[1]
                        : Boolean.valueOf(row[1].toString());
                dataRow.createCell(1).setCellValue(isMorning ? "Morning" : "Evening");

                dataRow.createCell(2).setCellValue((String) row[2]);
                dataRow.createCell(3).setCellValue(((Number) row[3]).longValue());

                final BigDecimal totalAmount = BigDecimal.valueOf(((Number) row[4]).doubleValue());
                final BigDecimal totalCost = BigDecimal.valueOf(((Number) row[5]).doubleValue());

                dataRow.createCell(4).setCellValue(totalAmount.doubleValue());
                dataRow.createCell(5).setCellValue(totalCost.doubleValue());

                netTotalAmount = netTotalAmount.add(totalAmount);
                netCostAmount = netCostAmount.add(totalCost);
            }

            // Net Totals Rows
            final Row totalRow = sheet.createRow(rowNum++);
            totalRow.createCell(3).setCellValue("Net Total Amount:");
            totalRow.createCell(4).setCellValue(netTotalAmount.doubleValue());

            final Row costRow = sheet.createRow(rowNum);
            costRow.createCell(3).setCellValue("Net Cost Amount:");
            costRow.createCell(5).setCellValue(netCostAmount.doubleValue());

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Prepare response with dynamic filename
            final String filename = "Sales_Report_" + orderDate + "_" + (shift ? "Morning" : "Evening") + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);

            try (ServletOutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            }
            log.info("Excel report generated successfully for date {} and shift {}", orderDate, shift ? "Morning" : "Evening");
        }
    }

    /**
     * Generates an Excel report and returns it as a ByteArrayInputStream.
     *
     * @param orderDate the order date as LocalDate
     * @param shift     the shift indicator (true for morning, false for evening)
     * @return ByteArrayInputStream containing the Excel report
     * @throws IOException if an I/O error occurs
     */
    public ByteArrayInputStream generateExcelReport(final LocalDate orderDate, final Boolean shift) throws IOException {
        final List<Object[]> reportData = orderRepository.findReportData(orderDate, shift);

        final List<String> productNames = new ArrayList<>();
        final Map<String, ShopReport> shopReportMap = new HashMap<>();

        // Process report data and build ShopReport mapping
        for (final Object[] row : reportData) {
            final String shopName = (String) row[0];
            final String productName = (String) row[1];
            final BigDecimal quantity = BigDecimal.valueOf(((Number) row[2]).doubleValue());
            final BigDecimal totalAmount = BigDecimal.valueOf(((Number) row[3]).doubleValue());
            final BigDecimal dueAmount = BigDecimal.valueOf(((Number) row[4]).doubleValue());
            final BigDecimal totalCost = BigDecimal.valueOf(((Number) row[5]).doubleValue());

            if (!productNames.contains(productName)) {
                productNames.add(productName);
            }

            final ShopReport shopReport = shopReportMap.getOrDefault(shopName, new ShopReport());
            shopReport.setShopName(shopName);
            shopReport.setDueAmount(dueAmount);
            shopReport.addProductData(productName, quantity, totalAmount, totalCost);
            shopReportMap.put(shopName, shopReport);
        }

        try (final XSSFWorkbook workbook = new XSSFWorkbook();
             final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final Sheet sheet = workbook.createSheet("sales_report");
            final int totalColumns = 1 + productNames.size() + 3;
            int rowNum = 0;

            // Title Row with Date and Shift
            final CellStyle titleStyle = createTitleCellStyle(workbook);
            final String titleText = String.format("Rajdeep sales - Date: %s - Shift: %s", orderDate, shift ? "AM" : "PM");
            final Row titleRow = sheet.createRow(rowNum++);
            final Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(titleText);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), 0, totalColumns - 1));

            // Blank row for spacing
            rowNum++;

            // Header Row
            final CellStyle headerCellStyle = createHeaderCellStyle(workbook);
            final Row headerRow = sheet.createRow(rowNum++);
            int colNum = 0;
            Cell cell = headerRow.createCell(colNum++);
            cell.setCellValue("Shop Name");
            cell.setCellStyle(headerCellStyle);

            for (final String productName : productNames) {
                cell = headerRow.createCell(colNum++);
                cell.setCellValue(productName);
                cell.setCellStyle(headerCellStyle);
            }
            cell = headerRow.createCell(colNum++);
            cell.setCellValue("Total Amount");
            cell.setCellStyle(headerCellStyle);
            cell = headerRow.createCell(colNum++);
            cell.setCellValue("Due Amount");
            cell.setCellStyle(headerCellStyle);
            cell = headerRow.createCell(colNum++);
            cell.setCellValue("Amount Paid");
            cell.setCellStyle(headerCellStyle);

            // Create common cell styles for data rows
            final CellStyle dataCellStyle = createDataCellStyle(workbook);
            final CellStyle numericCellStyle = createNumericCellStyle(workbook);
            final CellStyle integerCellStyle = createIntegerCellStyle(workbook);
            final CellStyle summaryCellStyle = createSummaryCellStyle(workbook);

            BigDecimal overallTotalAmount = BigDecimal.ZERO;
            BigDecimal overallTotalDue = BigDecimal.ZERO;
            BigDecimal overallTotalCost = BigDecimal.ZERO;
            final Map<String, Integer> productSummary = new HashMap<>();

            // Data Rows
            for (final ShopReport report : shopReportMap.values()) {
                final Row dataRow = sheet.createRow(rowNum++);
                colNum = 0;
                cell = dataRow.createCell(colNum++);
                cell.setCellValue(report.getShopName());
                cell.setCellStyle(dataCellStyle);

                for (final String productName : productNames) {
                    final BigDecimal quantity = report.getProductQuantities().getOrDefault(productName, BigDecimal.ZERO);
                    cell = dataRow.createCell(colNum++);
                    cell.setCellValue(quantity.intValue());
                    cell.setCellStyle(integerCellStyle);
                    productSummary.put(productName, productSummary.getOrDefault(productName, 0) + quantity.intValue());
                }

                cell = dataRow.createCell(colNum++);
                cell.setCellValue(report.getTotalAmount().doubleValue());
                cell.setCellStyle(numericCellStyle);
                overallTotalAmount = overallTotalAmount.add(report.getTotalAmount());

                cell = dataRow.createCell(colNum++);
                cell.setCellValue(report.getDueAmount().doubleValue());
                cell.setCellStyle(numericCellStyle);
                overallTotalDue = overallTotalDue.add(report.getDueAmount());

                cell = dataRow.createCell(colNum++);
                cell.setCellValue(""); // Amount Paid remains empty
                cell.setCellStyle(dataCellStyle);

                overallTotalCost = overallTotalCost.add(report.getTotalCost());
            }

            // Blank row for spacing before summary
            rowNum++;

            // Summary Row (Totals)
            final Row summaryRow = sheet.createRow(rowNum++);
            colNum = 0;
            cell = summaryRow.createCell(colNum++);
            cell.setCellValue("Total");
            cell.setCellStyle(summaryCellStyle);

            for (final String productName : productNames) {
                cell = summaryRow.createCell(colNum++);
                cell.setCellValue(productSummary.get(productName));
                cell.setCellStyle(summaryCellStyle);
            }
            cell = summaryRow.createCell(colNum++);
            cell.setCellValue(overallTotalAmount.doubleValue());
            cell.setCellStyle(summaryCellStyle);
            cell = summaryRow.createCell(colNum++);
            cell.setCellValue(overallTotalDue.doubleValue());
            cell.setCellStyle(summaryCellStyle);
            cell = summaryRow.createCell(colNum++);
            cell.setCellValue("");
            cell.setCellStyle(summaryCellStyle);

            // Extra spacing rows before cost and profit rows
            rowNum += 2;
            final int totalAmountColIndex = 1 + productNames.size();

            // Total Cost Amount Row
            final Row costRow = sheet.createRow(rowNum++);
            cell = costRow.createCell(totalAmountColIndex);
            cell.setCellValue("Total Cost Amount");
            cell.setCellStyle(summaryCellStyle);
            cell = costRow.createCell(totalAmountColIndex + 1);
            cell.setCellValue(overallTotalCost.doubleValue());
            cell.setCellStyle(numericCellStyle);

            // Net Profit Row
            final Row profitRow = sheet.createRow(rowNum++);
            cell = profitRow.createCell(totalAmountColIndex);
            cell.setCellValue("Net Profit (Revenue - Cost)");
            cell.setCellStyle(summaryCellStyle);
            cell = profitRow.createCell(totalAmountColIndex + 1);
            cell.setCellValue(overallTotalAmount.subtract(overallTotalCost).doubleValue());
            cell.setCellStyle(numericCellStyle);

            // Auto-size all columns
            for (int i = 0; i < totalColumns; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Excel report generated successfully for date {} and shift {}", orderDate, shift ? "AM" : "PM");
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /////////////// Helper Methods for Cell Styles ///////////////

    private CellStyle createBoldCellStyle(final Workbook workbook) {
        final CellStyle style = workbook.createCellStyle();
        final Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createTitleCellStyle(final Workbook workbook) {
        final CellStyle style = workbook.createCellStyle();
        final Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderCellStyle(final Workbook workbook) {
        final CellStyle style = workbook.createCellStyle();
        final Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        setThinBorders(style);
        return style;
    }

    private CellStyle createDataCellStyle(final Workbook workbook) {
        final CellStyle style = workbook.createCellStyle();
        setThinBorders(style);
        return style;
    }

    private CellStyle createNumericCellStyle(final Workbook workbook) {
        final CellStyle style = createDataCellStyle(workbook);
        final DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createIntegerCellStyle(final Workbook workbook) {
        final CellStyle style = createDataCellStyle(workbook);
        final DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("0"));
        return style;
    }

    private CellStyle createSummaryCellStyle(final Workbook workbook) {
        final CellStyle style = workbook.createCellStyle();
        final Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setThinBorders(style);
        return style;
    }

    private void setThinBorders(final CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
