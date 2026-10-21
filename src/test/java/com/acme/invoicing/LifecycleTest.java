package com.acme.invoicing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import jakarta.annotation.PostConstruct;

import org.junit.Test;

import com.acme.invoicing.support.LifecycleRunner;

public class LifecycleTest {
    public static class Bean {
        int started;

        @PostConstruct
        public void start() {
            started++;
        }
    }

    public static class Failing {
        @PostConstruct
        public void start() {
            throw new IllegalStateException("boom");
        }
    }

    @Test
    public void runsPostConstructMethods() {
        assertThat(LifecycleRunner.initialise(new Bean()).started, is(1));
    }

    @Test(expected = IllegalStateException.class)
    public void propagatesInitialiserFailure() {
        LifecycleRunner.initialise(new Failing());
    }
}
