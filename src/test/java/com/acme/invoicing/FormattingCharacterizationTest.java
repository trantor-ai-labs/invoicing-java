package com.acme.invoicing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Currency;

import org.junit.Test;

import com.acme.invoicing.render.MoneyFormatter;
import com.acme.invoicing.support.InvoiceChecksum;

/** Pins the money formatting, percent formatting, checksum and CLI exit codes as they behave today. */
public class FormattingCharacterizationTest {
    private static final BigDecimal AMOUNT = new BigDecimal("1480.00");

    @Test
    public void swissFormatUsesSfrAndApostrophes() {
        assertThat(MoneyFormatter.format(AMOUNT, Currency.getInstance("CHF"), "CH"), is("SFr. 1'480.00"));
    }

    @Test
    public void americanFormatUsesDollarAndCommas() {
        assertThat(MoneyFormatter.format(AMOUNT, Currency.getInstance("USD"), "US"), is("$1,480.00"));
    }

    @Test
    public void unknownCountryFallsBackToUsLocale() {
        assertThat(MoneyFormatter.format(AMOUNT, Currency.getInstance("USD"), "ZZ"), is("$1,480.00"));
    }

    @Test
    public void britishFormatUsesPoundSign() {
        assertThat(MoneyFormatter.format(AMOUNT, Currency.getInstance("GBP"), "GB"), is("£1,480.00"));
    }

    @Test
    public void frenchAndGermanFormatsPutEuroAfterCommaDecimals() {
        String fr = MoneyFormatter.format(AMOUNT, Currency.getInstance("EUR"), "FR");
        assertThat(fr, startsWith("1"));
        assertThat(fr, endsWith("480,00 €"));
        String de = MoneyFormatter.format(AMOUNT, Currency.getInstance("EUR"), "DE");
        assertThat(de, is("1.480,00 €"));
    }

    @Test
    public void percentStripsTrailingZeros() {
        assertThat(MoneyFormatter.percent(new BigDecimal("0.200")), is("20%"));
        assertThat(MoneyFormatter.percent(new BigDecimal("0.077")), is("7.7%"));
        assertThat(MoneyFormatter.percent(new BigDecimal("0.10")), is("10%"));
    }

    @Test
    public void checksumIsBase64OfSha256OfNumberTotalCurrency() {
        assertThat(InvoiceChecksum.of("INV-1", new BigDecimal("10.00"), "EUR"),
                is("Wltm9iFTu/McPc4redeNo2DzQImfyGejDfQYOtT+MyE="));
        assertThat(InvoiceChecksum.of("INV-9", BigDecimal.ZERO, "USD"),
                is("opN8zvYbTMSEeUQaQ9eWs72WdcI+qNDvgpomW9kSa+o="));
    }

    @Test
    public void checksumDependsOnScaleOfTotal() {
        assertThat(InvoiceChecksum.of("INV-1", new BigDecimal("10"), "EUR")
                .equals(InvoiceChecksum.of("INV-1", new BigDecimal("10.00"), "EUR")), is(false));
    }

    private static int run(String... args) {
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        PrintStream err = new PrintStream(new ByteArrayOutputStream());
        return Cli.run(args, out, err);
    }

    @Test
    public void exitCodesAreZeroOneAndTwo() {
        assertThat(run("render", "samples/order-1005.csv"), is(0));
        assertThat(run("export-xml", "samples/order-1001.csv"), is(0));
        assertThat(run("summary", "samples"), is(0));
        assertThat(run("render", "samples/missing.csv"), is(1));
        assertThat(run("bogus", "samples/order-1001.csv"), is(2));
        assertThat(run("render"), is(2));
    }

    @Test
    public void renderOfSampleEndsWithChecksumLine() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Cli.run(new String[] {"render", "samples/order-1003.csv"}, new PrintStream(out), new PrintStream(new ByteArrayOutputStream()));
        String text = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertThat(text.contains("Checksum: Svpoxy1ZgO563rI1xG+Hc/JIvpH/tqVGQNptnznI69A=\n"), is(true));
    }
}
