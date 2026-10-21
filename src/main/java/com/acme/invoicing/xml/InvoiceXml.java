package com.acme.invoicing.xml;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import com.acme.invoicing.model.Invoice;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlRootElement(name = "invoice")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"number", "issueDate", "customerId", "customerName", "country", "currency", "lines",
        "subtotal", "discount", "vat", "total", "checksum"})
public class InvoiceXml {
    private String number;
    private String issueDate;
    private String customerId;
    private String customerName;
    private String country;
    private String currency;
    @XmlElementWrapper(name = "lines")
    @XmlElement(name = "line")
    private List<InvoiceLineXml> lines;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal vat;
    private BigDecimal total;
    private String checksum;

    public static InvoiceXml from(Invoice invoice) {
        InvoiceXml xml = new InvoiceXml();
        xml.number = invoice.getNumber();
        xml.issueDate = invoice.getIssueDate().toString();
        xml.customerId = invoice.getCustomer().getId();
        xml.customerName = invoice.getCustomer().getName();
        xml.country = invoice.getCountry();
        xml.currency = invoice.getCurrency().getCurrencyCode();
        xml.lines = invoice.getLines().stream()
                .map(l -> new InvoiceLineXml(l.getSku(), l.getDescription(), l.getQuantity(), l.getUnitPrice(),
                        l.getTotal()))
                .collect(Collectors.toList());
        xml.subtotal = invoice.getSubtotal();
        xml.discount = invoice.getDiscount();
        xml.vat = invoice.getVat();
        xml.total = invoice.getTotal();
        xml.checksum = invoice.getChecksum();
        return xml;
    }
}
