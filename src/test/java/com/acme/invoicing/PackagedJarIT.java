package com.acme.invoicing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class PackagedJarIT {
    private static final String SAMPLE_ORDER = "samples/order-1003.csv";

    private Process start(String... command) throws Exception {
        String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        String[] full = new String[command.length + 2];
        full[0] = java;
        full[1] = "-jar";
        System.arraycopy(command, 0, full, 2, command.length);
        return new ProcessBuilder(full).redirectErrorStream(true).start();
    }

    private String runJar(String... args) throws Exception {
        String[] command = new String[args.length + 1];
        command[0] = new File("target/invoicing.jar").getPath();
        System.arraycopy(args, 0, command, 1, args.length);
        Process process = start(command);
        byte[] output = readAll(process);
        assertThat("jar finished", process.waitFor(60, TimeUnit.SECONDS), is(true));
        String text = new String(output, StandardCharsets.UTF_8);
        assertThat(text, process.exitValue(), is(0));
        return text;
    }

    private static byte[] readAll(Process process) throws Exception {
        return process.getInputStream().readAllBytes();
    }

    @Test
    public void packagedJarRendersASampleOrder() throws Exception {
        String output = runJar("render", SAMPLE_ORDER);
        assertThat(output, containsString("INVOICE INV-1003"));
        assertThat(output, containsString("TOTAL USD"));
    }

    @Test
    public void packagedJarExportsXml() throws Exception {
        String output = runJar("export-xml", "samples/order-1004.csv");
        assertThat(output, containsString("<number>INV-1004</number>"));
    }
}
