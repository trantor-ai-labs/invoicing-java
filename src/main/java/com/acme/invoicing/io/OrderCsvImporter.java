package com.acme.invoicing.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.acme.invoicing.model.Customer;
import com.acme.invoicing.model.Order;
import com.acme.invoicing.model.OrderLine;

public class OrderCsvImporter {
    static final String HEADER =
            "order_id,order_date,customer_id,customer_name,country,currency,sku,description,quantity,unit_price";
    private static final int COLUMNS = 10;
    private static final Logger LOG = Logger.getLogger(OrderCsvImporter.class.getName());

    public ImportResult importFile(Path file) throws IOException, ImportException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return importFrom(reader);
        }
    }

    public ImportResult importFrom(Reader source) throws IOException, ImportException {
        BufferedReader reader = new BufferedReader(source);
        String header = reader.readLine();
        if (header == null || !HEADER.equals(header.trim())) {
            throw new ImportException("Unexpected header: " + header);
        }
        Map<String, Draft> drafts = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 1;
        String line;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                addRow(drafts, CsvParser.parseLine(line));
            } catch (IllegalArgumentException | DateTimeParseException e) {
                String message = "line " + lineNumber + ": " + e.getMessage();
                LOG.warning("Skipping row, " + message);
                errors.add(message);
            }
        }
        List<Order> orders = drafts.values().stream().map(Draft::build).collect(Collectors.toList());
        return new ImportResult(orders, errors);
    }

    private void addRow(Map<String, Draft> drafts, List<String> f) {
        if (f.size() != COLUMNS) {
            throw new IllegalArgumentException("expected " + COLUMNS + " columns but found " + f.size());
        }
        int quantity = parseQuantity(f.get(8));
        BigDecimal price = parsePrice(f.get(9));
        LocalDate date = LocalDate.parse(f.get(1));
        String country = f.get(4).trim().toUpperCase();
        Currency currency = Currency.getInstance(f.get(5).trim());
        Draft draft = drafts.get(f.get(0));
        if (draft == null) {
            draft = new Draft(f.get(0), date, new Customer(f.get(2), f.get(3)), country, currency);
            drafts.put(f.get(0), draft);
        } else if (!draft.customer.getId().equals(f.get(2))) {
            throw new IllegalArgumentException("order " + f.get(0) + " has two customers");
        } else if (!draft.currency.equals(currency) || !draft.country.equals(country) || !draft.date.equals(date)) {
            throw new IllegalArgumentException("order " + f.get(0) + " changes date, country or currency");
        }
        draft.lines.add(new OrderLine(f.get(6), f.get(7), quantity, price));
    }

    private static int parseQuantity(String text) {
        try {
            int quantity = Integer.parseInt(text.trim());
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive: " + text);
            }
            return quantity;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bad quantity: " + text);
        }
    }

    private static BigDecimal parsePrice(String text) {
        try {
            BigDecimal price = new BigDecimal(text.trim());
            if (price.signum() < 0) {
                throw new IllegalArgumentException("price must not be negative: " + text);
            }
            return price;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bad price: " + text);
        }
    }

    private static final class Draft {
        private final String id;
        private final LocalDate date;
        private final Customer customer;
        private final String country;
        private final Currency currency;
        private final List<OrderLine> lines = new ArrayList<>();

        Draft(String id, LocalDate date, Customer customer, String country, Currency currency) {
            this.id = id;
            this.date = date;
            this.customer = customer;
            this.country = country;
            this.currency = currency;
        }

        Order build() {
            return new Order(id, date, customer, country, currency, lines);
        }
    }
}
