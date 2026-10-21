package com.acme.invoicing;

import static com.acme.invoicing.TestData.line;
import static com.acme.invoicing.TestData.order;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.acme.invoicing.io.InMemoryOrderRepository;
import com.acme.invoicing.model.Invoice;
import com.acme.invoicing.model.Order;
import com.acme.invoicing.pricing.VatTable;
import com.acme.invoicing.service.InvoiceService;
import com.acme.invoicing.support.InvoiceChecksum;
import com.acme.invoicing.support.LifecycleRunner;
import com.acme.invoicing.support.ListDiagnostics;
import com.acme.invoicing.xml.InvoiceXml;
import com.acme.invoicing.xml.InvoiceXmlExporter;

public class ExportTest {
    private Invoice invoice;

    @Before
    public void setUp() {
        InvoiceService service = LifecycleRunner.initialise(new InvoiceService(new VatTable()));
        invoice = service.invoiceFor(order("77", "DE", "EUR", line("A", 2, "12.50"), line("B", 1, "3.25")));
    }

    @Test
    public void xmlHasInvoiceRootAndLines() {
        String xml = new InvoiceXmlExporter().export(invoice);
        assertThat(xml, containsString("<invoice>"));
        assertThat(xml, containsString("<number>INV-77</number>"));
        assertThat(xml, containsString("<sku>B</sku>"));
    }

    @Test
    public void xmlRoundTrips() {
        InvoiceXmlExporter exporter = new InvoiceXmlExporter();
        InvoiceXml back = exporter.read(exporter.export(invoice));
        assertThat(back, is(InvoiceXml.from(invoice)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readingGarbageFails() {
        new InvoiceXmlExporter().read("<nothing/>");
    }

    @Test
    public void checksumIsStableAndVerifies() {
        assertThat(invoice.getChecksum().length(), is(44));
        assertThat(InvoiceChecksum.verify(invoice), is(true));
        assertThat(InvoiceChecksum.of("INV-1", invoice.getTotal(), "EUR"),
                is(InvoiceChecksum.of("INV-1", invoice.getTotal(), "EUR")));
    }

    @Test
    public void checksumDetectsTampering() {
        Invoice forged = Invoice.builder().number(invoice.getNumber()).currency(invoice.getCurrency())
                .total(invoice.getTotal().add(java.math.BigDecimal.ONE)).checksum(invoice.getChecksum()).build();
        assertThat(InvoiceChecksum.verify(forged), is(false));
    }

    @Test
    public void probesListCapacity() {
        assertThat(ListDiagnostics.capacityOf(new ArrayList<String>(25)), is(25));
        ArrayList<String> grown = new ArrayList<>();
        grown.add("x");
        assertThat(ListDiagnostics.describe(grown), is("size=1 capacity=10"));
    }

    @Test
    public void repositoryReportsItsBuffer() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        Order a = order("1", "FR", "EUR", line("A", 1, "1.00"));
        repository.save(a);
        repository.save(a);
        assertThat(repository.findAll().size(), is(1));
        assertThat(repository.findById("1").isPresent(), is(true));
        assertThat(repository.describe(), is("orders: size=1 capacity=10"));
    }
}
