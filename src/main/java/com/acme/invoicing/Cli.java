package com.acme.invoicing;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.acme.invoicing.io.ImportException;
import com.acme.invoicing.io.ImportResult;
import com.acme.invoicing.io.InMemoryOrderRepository;
import com.acme.invoicing.io.OrderCsvImporter;
import com.acme.invoicing.io.OrderRepository;
import com.acme.invoicing.model.Order;
import com.acme.invoicing.pricing.VatTable;
import com.acme.invoicing.render.InvoiceTextRenderer;
import com.acme.invoicing.service.CustomerSummary;
import com.acme.invoicing.service.InvoiceService;
import com.acme.invoicing.support.LifecycleRunner;
import com.acme.invoicing.xml.InvoiceXmlExporter;

public final class Cli {
    private static final String USAGE =
            "usage: invoicing render <order.csv> | export-xml <order.csv> [out.xml] | summary <dir>";

    private Cli() {
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        PrintStream out = new PrintStream(System.out, true, "UTF-8");
        PrintStream err = new PrintStream(System.err, true, "UTF-8");
        int code = run(args, out, err);
        out.flush();
        if (code != 0) {
            System.exit(code);
        }
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length < 2) {
            err.println(USAGE);
            return 2;
        }
        InvoiceService service = LifecycleRunner.initialise(new InvoiceService(new VatTable()));
        try {
            switch (args[0]) {
                case "render":
                    return render(Paths.get(args[1]), service, out, err);
                case "export-xml":
                    return exportXml(args, service, out, err);
                case "summary":
                    return summary(Paths.get(args[1]), service, out, err);
                default:
                    err.println(USAGE);
                    return 2;
            }
        } catch (ImportException | IOException | RuntimeException e) {
            err.println("error: " + e.getMessage());
            return 1;
        }
    }

    private static List<Order> load(Path file, PrintStream err) throws IOException, ImportException {
        ImportResult result = new OrderCsvImporter().importFile(file);
        result.getErrors().forEach(err::println);
        return result.getOrders();
    }

    private static int render(Path file, InvoiceService service, PrintStream out, PrintStream err)
            throws IOException, ImportException {
        List<Order> orders = load(file, err);
        if (orders.isEmpty()) {
            err.println("error: no orders in " + file);
            return 1;
        }
        InvoiceTextRenderer renderer = new InvoiceTextRenderer();
        String separator = "";
        for (Order order : orders) {
            out.print(separator);
            out.print(renderer.render(service.invoiceFor(order)));
            separator = System.lineSeparator();
        }
        return 0;
    }

    private static int exportXml(String[] args, InvoiceService service, PrintStream out, PrintStream err)
            throws IOException, ImportException {
        List<Order> orders = load(Paths.get(args[1]), err);
        if (orders.isEmpty()) {
            err.println("error: no orders in " + args[1]);
            return 1;
        }
        InvoiceXmlExporter exporter = new InvoiceXmlExporter();
        StringBuilder xml = new StringBuilder();
        for (Order order : orders) {
            xml.append(exporter.export(service.invoiceFor(order)));
        }
        if (args.length > 2) {
            Files.write(Paths.get(args[2]), xml.toString().getBytes(StandardCharsets.UTF_8));
            out.println("wrote " + args[2]);
        } else {
            out.print(xml);
        }
        return 0;
    }

    private static int summary(Path dir, InvoiceService service, PrintStream out, PrintStream err)
            throws IOException, ImportException {
        OrderRepository repository = new InMemoryOrderRepository();
        for (Path file : csvFiles(dir)) {
            load(file, err).forEach(repository::save);
        }
        new CustomerSummary(repository, service).lines().forEach(out::println);
        out.println(repository.describe());
        return 0;
    }

    private static List<Path> csvFiles(Path dir) throws IOException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.csv")) {
            stream.forEach(files::add);
        }
        Collections.sort(files);
        return files;
    }
}
