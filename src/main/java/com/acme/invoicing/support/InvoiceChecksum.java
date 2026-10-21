package com.acme.invoicing.support;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import com.acme.invoicing.model.Invoice;

public final class InvoiceChecksum {
    private InvoiceChecksum() {
    }

    public static String of(String number, BigDecimal total, String currencyCode) {
        String payload = number + "|" + total.toPlainString() + "|" + currencyCode;
        return Base64.getEncoder().encodeToString(sha256(payload.getBytes(StandardCharsets.UTF_8)));
    }

    public static boolean verify(Invoice invoice) {
        String expected = of(invoice.getNumber(), invoice.getTotal(), invoice.getCurrency().getCurrencyCode());
        try {
            byte[] left = Base64.getMimeDecoder().decode(expected);
            byte[] right = Base64.getMimeDecoder().decode(invoice.getChecksum());
            return Arrays.equals(left, right);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
