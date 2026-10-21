package com.acme.invoicing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;

import org.junit.BeforeClass;
import org.junit.Test;

import com.acme.invoicing.pricing.DiscountRules;
import com.acme.invoicing.pricing.VatTable;

public class PricingTest {
    private static DiscountRules rules;

    @BeforeClass
    public static void loadRules() {
        rules = DiscountRules.fromClasspath(DiscountRules.DEFAULT_RESOURCE);
    }

    @Test
    public void noDiscountForSmallOrders() {
        assertThat(rules.rateFor(new BigDecimal("499.99"), 5), is(new BigDecimal("0.00")));
    }

    @Test
    public void fivePercentFromFiveHundred() {
        assertThat(rules.rateFor(new BigDecimal("500.00"), 5), is(new BigDecimal("0.05")));
    }

    @Test
    public void tenPercentFromOneThousand() {
        assertThat(rules.rateFor(new BigDecimal("1000.00"), 5), is(new BigDecimal("0.10")));
    }

    @Test
    public void bulkBonusAddsTwoPoints() {
        assertThat(rules.rateFor(new BigDecimal("1500.00"), 50), is(new BigDecimal("0.12")));
    }

    @Test(expected = IllegalStateException.class)
    public void missingRulesResourceFails() {
        DiscountRules.fromClasspath("/rules/absent.js");
    }

    @Test
    public void vatRatesByCountry() {
        VatTable table = new VatTable();
        assertThat(table.find("FR").get(), is(new BigDecimal("0.200")));
        assertThat(table.find("ch").get(), is(new BigDecimal("0.077")));
        assertThat(table.find("DE").get(), is(new BigDecimal("0.190")));
    }

    @Test
    public void unknownCountryHasNoVatRate() {
        assertThat(new VatTable().find("XX").isPresent(), is(false));
        assertThat(new VatTable().find(null).isPresent(), is(false));
    }
}
