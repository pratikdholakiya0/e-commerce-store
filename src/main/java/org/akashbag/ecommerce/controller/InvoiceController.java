package org.akashbag.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.model.Invoice;
import org.akashbag.ecommerce.service.InvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping("/{orderId}")
    public ResponseEntity<String> getDashboardStats(@PathVariable String orderId) {
        String invoice = invoiceService.getInvoice(orderId);
        return new ResponseEntity<>(invoice, HttpStatus.OK);
    }
}