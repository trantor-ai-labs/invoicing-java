package com.acme.invoicing.render;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public final class MoneyFormatter {
    private MoneyFormatter() {
    }

    public static String format(BigDecimal amount, Currency currency, String country) {
        NumberFormat format = NumberFormat.getCurrencyInstance(localeFor(country));
        format.setCurrency(currency);
        if (format instanceof DecimalFormat) {
            keepLegacySeparators((DecimalFormat) format, currency, country);
        }
        return format.format(amount);
    }

    // JDK 9+ defaults to CLDR locale data; invoices keep the separators and symbol the JDK 8 data produced.
    private static void keepLegacySeparators(DecimalFormat format, Currency currency, String country) {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        switch (country) {
            case "FR":
                format.applyPattern("#,##0.00 ¤");
                symbols.setGroupingSeparator(' ');
                symbols.setMonetaryGroupingSeparator(' ');
                break;
            case "CH":
                format.applyPattern("¤ #,##0.00;¤-#,##0.00");
                symbols.setGroupingSeparator('\'');
                symbols.setMonetaryGroupingSeparator('\'');
                if ("CHF".equals(currency.getCurrencyCode())) {
                    symbols.setCurrencySymbol("SFr.");
                }
                break;
            case "GB":
            case "DE":
                break;
            default:
                format.applyPattern("¤#,##0.00;(¤#,##0.00)");
                break;
        }
        format.setDecimalFormatSymbols(symbols);
    }

    static Locale localeFor(String country) {
        switch (country) {
            case "FR":
                return Locale.FRANCE;
            case "CH":
                return new Locale("de", "CH");
            case "GB":
                return Locale.UK;
            case "DE":
                return Locale.GERMANY;
            default:
                return Locale.US;
        }
    }

    public static String percent(BigDecimal rate) {
        return rate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%";
    }
}
