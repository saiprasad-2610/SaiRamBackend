package com.example.sairam_tea_backend.util;

import com.example.sairam_tea_backend.model.Order;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {

    public static byte[] exportOrdersToExcel(List<Order> orders) throws IOException {
        // Create a new workbook (for .xlsx format)
        Workbook workbook = new XSSFWorkbook();
        // Create a sheet named "Orders"
        Sheet sheet = workbook.createSheet("Orders");

        // Create header row
        String[] headers = {
                "Order ID", "Customer Name", "Phone", "Email", "Products Ordered",
                "Delivery Address", "Special Instructions", "Amount", "Order Date",
                "Status", "Razorpay Order ID", "Razorpay Payment ID", "Payment Method"
        };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            // Optional: Apply bold style to headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            cell.setCellStyle(headerStyle);
        }

        // Populate data rows
        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Order order : orders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getId());
            row.createCell(1).setCellValue(order.getCustomerName());
            row.createCell(2).setCellValue(order.getCustomerPhone());
            row.createCell(3).setCellValue(order.getCustomerEmail());
            row.createCell(4).setCellValue(order.getProductsOrdered());
            row.createCell(5).setCellValue(order.getDeliveryAddress());
            row.createCell(6).setCellValue(order.getSpecialInstructions());
            row.createCell(7).setCellValue(order.getAmount());
            row.createCell(8).setCellValue(order.getOrderDate().format(formatter));
            row.createCell(9).setCellValue(order.getStatus());
            row.createCell(10).setCellValue(order.getRazorpayOrderId() != null ? order.getRazorpayOrderId() : "N/A");
            row.createCell(11).setCellValue(order.getRazorpayPaymentId() != null ? order.getRazorpayPaymentId() : "N/A");
            row.createCell(12).setCellValue(order.getPaymentMethod());
        }

        // Auto-size columns for better readability
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the workbook to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close(); // Close the workbook to free up resources

        return outputStream.toByteArray();
    }
}