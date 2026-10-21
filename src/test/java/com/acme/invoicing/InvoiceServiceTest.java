package com.acme.invoicing;

import static com.acme.invoicing.TestData.line;
import static com.acme.invoicing.TestData.order;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.acme.invoicing.model.Invoice;
import com.acme.invoicing.pricing.DiscountPolicy;
import com.acme.invoicing.pricing.VatTable;
import com.acme.invoicing.service.InvoiceService;
import com.acme.invoicing.support.LifecycleRunner;

public class InvoiceServiceTest {
    private InvoiceService service;

    @Before
    public void setUp() {
        service = LifecycleRunner.initialise(new InvoiceService(new VatTable()));
    }

    @Test
    public void totalsLinesWithoutDiscount() {
        Invoice invoice = service.invoiceFor(order("1", "FR", "EUR", line("A", 2, "10.00"), line("B", 1, "5.50")));
        assertThat(invoice.getSubtotal(), comparesEqualTo(new BigDecimal("25.50")));
        assertThat(invoice.getDiscount(), comparesEqualTo(BigDecimal.ZERO));
        assertThat(invoice.getVat(), comparesEqualTo(new BigDecimal("5.10")));
        assertThat(invoice.getTotal(), comparesEqualTo(new BigDecimal("30.60")));
    }

    @Test
    public void roundsLineTotalsHalfUp() {
        Invoice invoice = service.invoiceFor(order("2", "GB", "GBP", line("A", 3, "14.995")));
        assertThat(invoice.getLines().get(0).getTotal(), is(new BigDecimal("44.99")));
    }

    @Test
    public void roundsVatHalfUp() {
        Invoice invoice = service.invoiceFor(order("3", "CH", "CHF", line("A", 1, "10.10")));
        assertThat(invoice.getVat(), is(new BigDecimal("0.78")));
        assertThat(invoice.getTotal(), is(new BigDecimal("10.88")));
    }

    @Test
    public void appliesVolumeDiscountBeforeVat() {
        Invoice invoice = service.invoiceFor(order("4", "FR", "EUR", line("A", 10, "100.00")));
        assertThat(invoice.getDiscount(), is(new BigDecimal("100.00")));
        assertThat(invoice.getNet(), is(new BigDecimal("900.00")));
        assertThat(invoice.getTotal(), is(new BigDecimal("1080.00")));
    }

    @Test
    public void usTaxRateIsZero() {
        Invoice invoice = service.invoiceFor(order("5", "US", "USD", line("A", 1, "19.99")));
        assertThat(invoice.getVat(), is(new BigDecimal("0.00")));
        assertThat(invoice.getTotal(), is(new BigDecimal("19.99")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnknownCountry() {
        service.invoiceFor(order("6", "ZZ", "EUR", line("A", 1, "1.00")));
    }

    @Test(expected = IllegalStateException.class)
    public void refusesToWorkBeforeInitialisation() {
        new InvoiceService(new VatTable()).invoiceFor(order("7", "FR", "EUR", line("A", 1, "1.00")));
    }

    @Test
    public void asksTheDiscountPolicyForTheRate() {
        DiscountPolicy policy = mock(DiscountPolicy.class);
        when(policy.rateFor(any(BigDecimal.class), eq(3))).thenReturn(new BigDecimal("0.50"));
        InvoiceService mocked = new InvoiceService(new VatTable(), policy);
        Invoice invoice = mocked.invoiceFor(order("8", "US", "USD", line("A", 3, "10.00")));
        assertThat(invoice.getDiscount(), is(new BigDecimal("15.00")));
        verify(policy).rateFor(new BigDecimal("30.00"), 3);
    }
}
