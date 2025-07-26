// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/service/ExcelService.java
package com.example.sairam_tea_backend.service;

import com.example.sairam_tea_backend.model.Order; // Ensure this import is present
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelService {

    public byte[] exportOrdersToExcel(List<Order> orders) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Orders");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Order ID", "User ID", "Customer Name", "Customer Phone", "Customer Email",
                "Products Ordered", "Delivery Address", "Special Instructions", "Amount",
                "Order Date", "Status", "Payment Method", "Razorpay Order ID", "Razorpay Payment ID"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            // Optional: Style header
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
        }

        // Populate data rows
        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Order order : orders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getId());
            // CORRECTED LINE: Convert Long ID to String explicitly
            row.createCell(1).setCellValue(order.getUser() != null ? String.valueOf(order.getUser().getId()) : "Guest");
            row.createCell(2).setCellValue(order.getCustomerName());
            row.createCell(3).setCellValue(order.getCustomerPhone());
            row.createCell(4).setCellValue(order.getCustomerEmail());
            row.createCell(5).setCellValue(order.getProductsOrdered());
            row.createCell(6).setCellValue(order.getDeliveryAddress());
            row.createCell(7).setCellValue(order.getSpecialInstructions());
            row.createCell(8).setCellValue(order.getAmount());
            row.createCell(9).setCellValue(order.getOrderDate().format(formatter));
            // THIS IS THE CORRECT LINE: Convert the enum to its String name
            row.createCell(10).setCellValue(order.getStatus()); // <--- THIS LINE MUST BE EXACTLY THIS
            row.createCell(11).setCellValue(order.getPaymentMethod());
            row.createCell(12).setCellValue(order.getRazorpayOrderId() != null ? order.getRazorpayOrderId() : "");
            row.createCell(13).setCellValue(order.getRazorpayPaymentId() != null ? order.getRazorpayPaymentId() : "");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }
}
