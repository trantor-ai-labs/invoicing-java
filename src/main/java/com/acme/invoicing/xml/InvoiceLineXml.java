package com.acme.invoicing.xml;

import java.math.BigDecimal;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"sku", "description", "quantity", "unitPrice", "total"})
public class InvoiceLineXml {
    private String sku;
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;
}
