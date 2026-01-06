package org.akashbag.ecommerce.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.model.*;
import org.akashbag.ecommerce.repository.InvoiceRepository;
import org.akashbag.ecommerce.repository.ProductRepository; // To fetch product names
import org.akashbag.ecommerce.repository.ProfileRepository;
import org.akashbag.ecommerce.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final UploadService uploadService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public String getInvoice(String orderId) {
        String fileName = "invoice_" + orderId + ".pdf";
        return uploadService.getPdfViewUrl(fileName);
    }

    public void saveInvoice(Order order){
        byte[] pdf = generateInvoice(order);

        String fileName = "invoice_" + order.getId() + ".pdf";
        String invoiceUrl = uploadService.uploadImageBytes(pdf,  fileName);

        Invoice invoice = Invoice.builder()
                .orderId(order.getId())
                .customerId(order.getUserId())
                .invoiceUrl(invoiceUrl)
                .build();
        invoiceRepository.save(invoice);
    }

    public byte[] generateInvoice(Order order) {
        Authentication  authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.getUserByEmail(authentication.getName());
        Profile profile = profileRepository.findByUserId(user.getId());

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // --- 1. Header ---
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
            Paragraph title = new Paragraph("AKASH BAG STORE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // --- 2. Details ---
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            document.add(new Paragraph("Invoice ID: " + "INV-" + order.getId(), normalFont));
            document.add(new Paragraph("Date: " + order.getOrderDate().toString(), normalFont));
            document.add(new Paragraph("Status: " + order.getPaymentStatus(), normalFont));
            document.add(new Paragraph("\n\n"));
            Paragraph header = new Paragraph("address: "+  order.getShippingAddress().toString());
            document.add(header);
            document.add(new Paragraph("\n\n"));


            document.add(new Paragraph("name: " + profile.getFirstName() + " "+ profile.getLastName()));
            document.add(new Paragraph("\n"));


            // --- 3. Table ---
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{4, 1, 2, 2});

            addTableHeader(table, "Product");
            addTableHeader(table, "Qty");
            addTableHeader(table, "Price");
            addTableHeader(table, "Total");

            double grandTotal = 0;
            Locale indiaLocale = new Locale("en", "IN");
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(indiaLocale);

            // ✅ SAFE LOOP: Handles potential nulls to prevent crashes
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    String productName = "Unknown Product";
                    if (item.getProductId() != null) {
                        productName = productRepository.findById(item.getProductId())
                                .map(p -> p.getName())
                                .orElse("Item (" + item.getProductId() + ")");
                    }

                    table.addCell(productName);
                    table.addCell(String.valueOf(item.getQuantity()));
                    table.addCell("Rs. " + item.getUnitPrice()); // Simple text to avoid encoding issues
                    table.addCell("Rs. " + item.getTotalPrice());

                    grandTotal += item.getTotalPrice();
                }
            }
            document.add(table);

            // --- 4. Total ---
            Paragraph totalPara = new Paragraph("Grand Total: Rs. " + grandTotal,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            totalPara.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalPara);

            document.add(new Paragraph("\n\n(Thank you for shopping!)"));

        } catch (Exception e) {
            e.printStackTrace(); // Log error to console
            throw new RuntimeException("Error generating PDF invoice", e);
        } finally {
            // ✅ CRITICAL: This MUST run, otherwise the PDF is corrupt (0 bytes)
            if (document.isOpen()) {
                document.close();
            }
        }

        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String headerTitle) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(Color.LIGHT_GRAY);
        header.setBorderWidth(1);
        header.setPhrase(new Phrase(headerTitle));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header);
    }
}