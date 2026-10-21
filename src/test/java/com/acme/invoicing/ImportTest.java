package com.acme.invoicing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Test;

import com.acme.invoicing.io.CsvParser;
import com.acme.invoicing.io.ImportException;
import com.acme.invoicing.io.ImportResult;
import com.acme.invoicing.io.OrderCsvImporter;

public class ImportTest {
    private static final String HEADER =
            "order_id,order_date,customer_id,customer_name,country,currency,sku,description,quantity,unit_price\n";

    private static ImportResult read(String rows) throws Exception {
        return new OrderCsvImporter().importFrom(new StringReader(HEADER + rows));
    }

    @Test
    public void parsesPlainFields() {
        assertThat(CsvParser.parseLine("a,b,c"), contains("a", "b", "c"));
    }

    @Test
    public void parsesQuotedCommaAndEscapedQuote() {
        assertThat(CsvParser.parseLine("x,\"a, b\",\"say \"\"hi\"\"\""), contains("x", "a, b", "say \"hi\""));
    }

    @Test
    public void keepsEmptyFields() {
        assertThat(CsvParser.parseLine("a,,c,"), is(Arrays.asList("a", "", "c", "")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnterminatedQuote() {
        CsvParser.parseLine("a,\"b");
    }

    @Test
    public void groupsRowsIntoOrders() throws Exception {
        ImportResult result = read("1,2024-01-01,C-1,Ann,FR,EUR,A,One,1,1.00\n"
                + "1,2024-01-01,C-1,Ann,FR,EUR,B,Two,2,2.00\n"
                + "2,2024-01-02,C-2,Bob,US,USD,C,Three,3,3.00\n");
        assertThat(result.hasErrors(), is(false));
        assertThat(result.getOrders(), hasSize(2));
        assertThat(result.getOrders().get(0).getLines(), hasSize(2));
        assertThat(result.getOrders().get(0).unitCount(), is(3));
    }

    @Test
    public void reportsMalformedRowsAndKeepsTheRest() throws Exception {
        ImportResult result = read("1,2024-01-01,C-1,Ann,FR,EUR,A,One,1,1.00\n"
                + "1,2024-01-01,C-1,Ann,FR,EUR,B,Two,many,2.00\n"
                + "1,2024-01-01,C-1,Ann,FR,EUR,C,Three\n"
                + "1,2024-01-01,C-1,Ann,FR,EUR,D,Four,1,-4\n"
                + "1,2024-13-01,C-1,Ann,FR,EUR,E,Five,1,1.00\n"
                + "1,2024-01-01,C-1,Ann,FR,XXX,F,Six,1,1.00\n");
        assertThat(result.getOrders(), hasSize(1));
        assertThat(result.getOrders().get(0).getLines(), hasSize(1));
        assertThat(result.getErrors(), hasSize(5));
        assertThat(result.getErrors().get(0), containsString("line 3"));
        assertThat(result.getErrors().get(0), containsString("bad quantity"));
    }

    @Test
    public void rejectsOrderWithTwoCustomers() throws Exception {
        ImportResult result = read("1,2024-01-01,C-1,Ann,FR,EUR,A,One,1,1.00\n"
                + "1,2024-01-01,C-9,Zed,FR,EUR,B,Two,1,1.00\n");
        assertThat(result.getErrors(), hasSize(1));
        assertThat(result.getErrors().get(0), containsString("two customers"));
    }

    @Test(expected = ImportException.class)
    public void rejectsUnknownHeader() throws Exception {
        new OrderCsvImporter().importFrom(new StringReader("id,date\n1,2\n"));
    }

    @Test
    public void importsASampleFile() throws Exception {
        ImportResult result = new OrderCsvImporter().importFile(Paths.get("samples", "order-1001.csv"));
        assertThat(result.getOrders(), hasSize(1));
        assertThat(result.getOrders().get(0).getLines().get(0).getDescription(), is("Widget, large"));
    }
}
