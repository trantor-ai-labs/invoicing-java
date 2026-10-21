package com.acme.invoicing.pricing;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class DiscountRules implements DiscountPolicy {
    public static final String DEFAULT_RESOURCE = "/rules/discounts.js";

    private final Invocable script;

    private DiscountRules(Invocable script) {
        this.script = script;
    }

    public static DiscountRules fromClasspath(String resource) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        if (engine == null) {
            throw new IllegalStateException("No script engine named nashorn");
        }
        try (InputStream in = DiscountRules.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing rules resource " + resource);
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                engine.eval(reader);
            }
        } catch (IOException | ScriptException e) {
            throw new IllegalStateException("Cannot load rules " + resource, e);
        }
        return new DiscountRules((Invocable) engine);
    }

    @Override
    public BigDecimal rateFor(BigDecimal subtotal, int units) {
        try {
            Object rate = script.invokeFunction("discountRate", subtotal.doubleValue(), units);
            return new BigDecimal(String.valueOf(rate)).setScale(2, RoundingMode.HALF_UP);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new IllegalStateException("Discount rule failed", e);
        }
    }
}
