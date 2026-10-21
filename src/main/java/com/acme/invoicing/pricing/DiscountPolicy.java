package com.acme.invoicing.pricing;

import java.math.BigDecimal;

public interface DiscountPolicy {
    BigDecimal rateFor(BigDecimal subtotal, int units);
}
