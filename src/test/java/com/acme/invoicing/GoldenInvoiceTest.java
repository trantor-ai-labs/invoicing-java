package com.acme.invoicing;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import com.acme.invoicing.io.OrderCsvImporter;
import com.acme.invoicing.model.Order;
import com.acme.invoicing.pricing.VatTable;
import com.acme.invoicing.render.InvoiceTextRenderer;
import com.acme.invoicing.service.InvoiceService;
import com.acme.invoicing.support.LifecycleRunner;

public class GoldenInvoiceTest {
    private InvoiceService service;

    @Before
    public void setUp() {
        service = LifecycleRunner.initialise(new InvoiceService(new VatTable()));
    }

    private void assertMatchesGolden(String number) throws Exception {
        Order order = new OrderCsvImporter().importFile(Paths.get("samples", "order-" + number + ".csv"))
                .getOrders().get(0);
        String rendered = new InvoiceTextRenderer().render(service.invoiceFor(order));
        byte[] expected = golden("/golden/order-" + number + ".txt");
        assertArrayEquals("golden mismatch for " + number + ":\n" + rendered, expected,
                rendered.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] golden(String resource) throws IOException {
        try (InputStream in = GoldenInvoiceTest.class.getResourceAsStream(resource)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        }
    }

    @Test
    public void frenchInvoice() throws Exception {
        assertMatchesGolden("1001");
    }

    @Test
    public void swissInvoice() throws Exception {
        assertMatchesGolden("1002");
    }

    @Test
    public void americanInvoice() throws Exception {
        assertMatchesGolden("1003");
    }

    @Test
    public void britishInvoice() throws Exception {
        assertMatchesGolden("1004");
    }

    @Test
    public void rendersTotalLine() throws Exception {
        Order order = new OrderCsvImporter().importFile(Paths.get("samples", "order-1003.csv")).getOrders().get(0);
        String rendered = new InvoiceTextRenderer().render(service.invoiceFor(order));
        assertEquals(true, rendered.contains("TOTAL USD"));
    }
}
