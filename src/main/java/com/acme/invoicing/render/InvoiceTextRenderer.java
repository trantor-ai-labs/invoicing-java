package com.acme.invoicing.render;

import java.math.BigDecimal;
import java.util.Currency;

import com.acme.invoicing.model.Invoice;
import com.acme.invoicing.model.InvoiceLine;

public class InvoiceTextRenderer {
    private static final int WIDTH = 65;

    public String render(Invoice invoice) {
        StringBuilder out = new StringBuilder();
        out.append(String.format("INVOICE %s%n", invoice.getNumber()));
        out.append(String.format("Date:     %s%n", invoice.getIssueDate()));
        out.append(String.format("Customer: %s (%s)%n", invoice.getCustomer().getName(), invoice.getCustomer().getId()));
        out.append(String.format("Country:  %s%n", invoice.getCountry()));
        out.append(rule('='));
        out.append(String.format("%-8s %-24s %4s %12s %13s%n", "SKU", "Description", "Qty", "Unit price", "Total"));
        out.append(rule('-'));
        for (InvoiceLine line : invoice.getLines()) {
            out.append(String.format("%-8s %-24s %4d %12s %13s%n", line.getSku(), shorten(line.getDescription()),
                    line.getQuantity(), money(invoice, line.getUnitPrice()), money(invoice, line.getTotal())));
        }
        out.append(rule('-'));
        out.append(summary("Subtotal", invoice, invoice.getSubtotal()));
        out.append(summary("Discount " + MoneyFormatter.percent(invoice.getDiscountRate()), invoice,
                invoice.getDiscount().negate()));
        out.append(summary("Net", invoice, invoice.getNet()));
        out.append(summary("VAT " + MoneyFormatter.percent(invoice.getVatRate()), invoice, invoice.getVat()));
        out.append(rule('='));
        out.append(summary("TOTAL " + invoice.getCurrency().getCurrencyCode(), invoice, invoice.getTotal()));
        out.append(String.format("Checksum: %s%n", invoice.getChecksum()));
        return out.toString();
    }

    private static String summary(String label, Invoice invoice, BigDecimal amount) {
        String value = money(invoice, amount);
        int gap = Math.max(1, WIDTH - label.length() - value.length());
        StringBuilder row = new StringBuilder(label);
        for (int i = 0; i < gap; i++) {
            row.append(' ');
        }
        return row.append(value).append(String.format("%n")).toString();
    }

    private static String money(Invoice invoice, BigDecimal amount) {
        Currency currency = invoice.getCurrency();
        return MoneyFormatter.format(amount, currency, invoice.getCountry());
    }

    private static String shorten(String text) {
        return text.length() <= 24 ? text : text.substring(0, 23) + "~";
    }

    private static String rule(char c) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < WIDTH; i++) {
            sb.append(c);
        }
        return sb.append(String.format("%n")).toString();
    }
}
