package com.acme.invoicing.render;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;

public final class MoneyFormatter {
    private MoneyFormatter() {
    }

    public static String format(BigDecimal amount, Currency currency, String country) {
        Locale locale = localeFor(country);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        String pattern;
        switch (country) {
            case "FR":
                symbols.setGroupingSeparator(' ');
                symbols.setDecimalSeparator(',');
                pattern = "#,##0.00 ¤";
                break;
            case "CH":
                symbols.setGroupingSeparator('\'');
                symbols.setDecimalSeparator('.');
                pattern = "¤ #,##0.00;¤-#,##0.00";
                break;
            case "GB":
                symbols.setGroupingSeparator(',');
                symbols.setDecimalSeparator('.');
                pattern = "¤#,##0.00";
                break;
            case "DE":
                symbols.setGroupingSeparator('.');
                symbols.setDecimalSeparator(',');
                pattern = "#,##0.00 ¤";
                break;
            default:
                symbols.setGroupingSeparator(',');
                symbols.setDecimalSeparator('.');
                pattern = "¤#,##0.00;(¤#,##0.00)";
                break;
        }
        symbols.setCurrency(currency);
        if ("CH".equals(country) && "CHF".equals(currency.getCurrencyCode())) {
            symbols.setCurrencySymbol("SFr.");
        }
        return new DecimalFormat(pattern, symbols).format(amount);
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
