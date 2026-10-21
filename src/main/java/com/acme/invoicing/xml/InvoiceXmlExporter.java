package com.acme.invoicing.xml;

import java.io.StringReader;
import java.io.StringWriter;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import com.acme.invoicing.model.Invoice;

public class InvoiceXmlExporter {
    private final JAXBContext context;

    public InvoiceXmlExporter() {
        try {
            this.context = JAXBContext.newInstance(InvoiceXml.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot create JAXB context", e);
        }
    }

    public String export(Invoice invoice) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter writer = new StringWriter();
            marshaller.marshal(InvoiceXml.from(invoice), writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot export " + invoice.getNumber(), e);
        }
    }

    public InvoiceXml read(String xml) {
        try {
            return (InvoiceXml) context.createUnmarshaller().unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Not an invoice document", e);
        }
    }
}
