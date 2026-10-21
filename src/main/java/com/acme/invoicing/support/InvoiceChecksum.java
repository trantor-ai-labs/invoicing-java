package com.acme.invoicing.support;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.acme.invoicing.model.Invoice;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public final class InvoiceChecksum {
    private InvoiceChecksum() {
    }

    public static String of(String number, BigDecimal total, String currencyCode) {
        String payload = number + "|" + total.toPlainString() + "|" + currencyCode;
        return new BASE64Encoder().encode(sha256(payload.getBytes(StandardCharsets.UTF_8)));
    }

    public static boolean verify(Invoice invoice) {
        String expected = of(invoice.getNumber(), invoice.getTotal(), invoice.getCurrency().getCurrencyCode());
        try {
            byte[] left = new BASE64Decoder().decodeBuffer(expected);
            byte[] right = new BASE64Decoder().decodeBuffer(invoice.getChecksum());
            return Arrays.equals(left, right);
        } catch (IOException e) {
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
