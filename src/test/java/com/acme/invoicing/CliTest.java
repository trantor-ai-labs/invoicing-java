package com.acme.invoicing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.acme.invoicing.io.OrderRepository;
import com.acme.invoicing.pricing.VatTable;
import com.acme.invoicing.service.CustomerSummary;
import com.acme.invoicing.service.InvoiceService;
import com.acme.invoicing.support.LifecycleRunner;

public class CliTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private PrintStream originalOut;
    private ByteArrayOutputStream captured;

    @Before
    public void capture() {
        originalOut = System.out;
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true));
    }

    @After
    public void restore() {
        System.setOut(originalOut);
    }

    private String output() throws Exception {
        return new String(captured.toByteArray(), StandardCharsets.UTF_8);
    }

    @Test
    public void rendersAnInvoice() throws Exception {
        Cli.main(new String[] {"render", "samples/order-1003.csv"});
        assertThat(output(), containsString("INVOICE INV-1003"));
        assertThat(output(), containsString("TOTAL USD"));
    }

    @Test
    public void exportsXmlToStdout() throws Exception {
        Cli.main(new String[] {"export-xml", "samples/order-1004.csv"});
        assertThat(output(), containsString("<number>INV-1004</number>"));
    }

    @Test
    public void exportsXmlToAFile() throws Exception {
        Path target = tmp.newFile("out.xml").toPath();
        Cli.main(new String[] {"export-xml", "samples/order-1002.csv", target.toString()});
        assertThat(new String(Files.readAllBytes(target), StandardCharsets.UTF_8), containsString("<currency>CHF</currency>"));
    }

    @Test
    public void summarisesADirectory() throws Exception {
        Cli.main(new String[] {"summary", "samples"});
        assertThat(output(), containsString("Maison Dupont"));
        assertThat(output(), containsString(" 2 order(s)"));
        assertThat(output(), containsString("orders: size=5"));
    }

    @Test
    public void usageErrorReturnsTwo() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int code = Cli.run(new String[] {"frobnicate", "x"}, new PrintStream(captured), new PrintStream(err));
        assertThat(code, is(2));
    }

    @Test
    public void missingFileReturnsOne() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int code = Cli.run(new String[] {"render", "samples/nope.csv"}, new PrintStream(captured), new PrintStream(err));
        assertThat(code, is(1));
        assertThat(new String(err.toByteArray(), StandardCharsets.UTF_8), containsString("error:"));
    }

    @Test
    public void summaryLinesComeFromTheRepository() {
        OrderRepository repository = Mockito.mock(OrderRepository.class);
        Mockito.when(repository.findAll()).thenReturn(Arrays.asList(
                TestData.order("1", "US", "USD", TestData.line("A", 1, "10.00")),
                TestData.order("2", "US", "USD", TestData.line("B", 2, "5.00"))));
        InvoiceService service = LifecycleRunner.initialise(new InvoiceService(new VatTable()));
        List<String> lines = new CustomerSummary(repository, service).lines();
        assertThat(lines.size(), is(1));
        assertThat(lines.get(0), containsString("2 order(s) $20.00"));
        Mockito.verify(repository).findAll();
    }
}
