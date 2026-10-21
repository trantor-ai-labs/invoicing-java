package com.acme.invoicing.render;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public final class MoneyFormatter {
    private MoneyFormatter() {
    }

    public static String format(BigDecimal amount, Currency currency, String country) {
        NumberFormat format = NumberFormat.getCurrencyInstance(localeFor(country));
        format.setCurrency(currency);
        return format.format(amount);
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
