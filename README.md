# invoicing-java

A small order-to-invoice service in plain Java 17 and Maven, with no framework. It reads orders from
CSV files, prices them (VAT by country, volume discounts, BigDecimal rounding), and writes the
invoice as fixed-width text or as XML.

The before-state of the upgrade to Java 17 is kept as a git tag, not on this branch.
It is upgraded by the platform, not by hand.

## Build and test

With Maven and a JDK 17 on the path:

```bash
mvn -B verify
```

Or in Docker, without installing anything:

```bash
docker run --rm -v "$PWD":/work -v invoicing-m2:/root/.m2 -w /work \
  maven:3.9-eclipse-temurin-17 mvn -B verify
```

`verify` runs the JUnit suite (reports in `target/surefire-reports`), writes the JaCoCo coverage
report to `target/site/jacoco`, and builds `target/invoicing.jar`.

## Run

```bash
java -Djava.locale.providers=COMPAT -jar target/invoicing.jar render samples/order-1001.csv        # print the invoice
java -Djava.locale.providers=COMPAT -jar target/invoicing.jar export-xml samples/order-1001.csv    # invoice as XML on stdout
java -Djava.locale.providers=COMPAT -jar target/invoicing.jar export-xml samples/order-1001.csv out.xml
java -Djava.locale.providers=COMPAT -jar target/invoicing.jar summary samples/                     # totals per customer
```

`./run.sh render samples/order-1001.csv` does the same and builds the jar first if it is missing.
The `Dockerfile` packages the jar on `eclipse-temurin:17-jre`:

```bash
docker build -t invoicing . && docker run --rm invoicing render samples/order-1002.csv
```

## Input format

One CSV row per order line, with the order fields repeated on each row:

```
order_id,order_date,customer_id,customer_name,country,currency,sku,description,quantity,unit_price
```

Rows that cannot be read are skipped and reported with their line number.

## Layout

- `model` holds customers, orders, order lines and invoices.
- `pricing` holds the VAT table and the discount rules (`src/main/resources/rules/discounts.js`).
- `io` holds the CSV parser and importer and the in-memory repository.
- `render` and `xml` write the invoice as text and as XML.
- `Cli` is the command line entry point.

## License

Apache-2.0, see `LICENSE`.
