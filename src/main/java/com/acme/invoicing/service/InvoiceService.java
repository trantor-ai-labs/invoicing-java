package com.acme.invoicing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.acme.invoicing.model.Invoice;
import com.acme.invoicing.model.InvoiceLine;
import com.acme.invoicing.model.Order;
import com.acme.invoicing.pricing.DiscountPolicy;
import com.acme.invoicing.pricing.DiscountRules;
import com.acme.invoicing.pricing.VatTable;
import com.acme.invoicing.support.InvoiceChecksum;

public class InvoiceService {
    private static final Logger LOG = Logger.getLogger(InvoiceService.class.getName());
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final VatTable vatTable;
    private DiscountPolicy discountPolicy;

    public InvoiceService(VatTable vatTable) {
        this.vatTable = vatTable;
    }

    public InvoiceService(VatTable vatTable, DiscountPolicy discountPolicy) {
        this.vatTable = vatTable;
        this.discountPolicy = discountPolicy;
    }

    @PostConstruct
    public void init() {
        if (discountPolicy == null) {
            discountPolicy = DiscountRules.fromClasspath(DiscountRules.DEFAULT_RESOURCE);
            LOG.fine("Discount rules loaded");
        }
    }

    public Invoice invoiceFor(Order order) {
        if (discountPolicy == null) {
            throw new IllegalStateException("InvoiceService has not been initialised");
        }
        List<InvoiceLine> lines = order.getLines().stream()
                .map(l -> new InvoiceLine(l.getSku(), l.getDescription(), l.getQuantity(), l.getUnitPrice(), l.total()))
                .collect(Collectors.toList());
        BigDecimal subtotal = lines.stream().map(InvoiceLine::getTotal).reduce(ZERO, BigDecimal::add);
        BigDecimal discountRate = discountPolicy.rateFor(subtotal, order.unitCount());
        BigDecimal discount = money(subtotal.multiply(discountRate));
        BigDecimal net = subtotal.subtract(discount);
        BigDecimal vatRate = vatTable.find(order.getCountry())
                .orElseThrow(() -> new IllegalArgumentException("No VAT rate for country " + order.getCountry()));
        BigDecimal vat = money(net.multiply(vatRate));
        BigDecimal total = net.add(vat);
        String number = "INV-" + order.getId();
        return Invoice.builder()
                .number(number)
                .issueDate(order.getDate())
                .customer(order.getCustomer())
                .country(order.getCountry())
                .currency(order.getCurrency())
                .lines(lines)
                .subtotal(subtotal)
                .discountRate(discountRate)
                .discount(discount)
                .net(net)
                .vatRate(vatRate)
                .vat(vat)
                .total(total)
                .checksum(InvoiceChecksum.of(number, total, order.getCurrency().getCurrencyCode()))
                .build();
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
