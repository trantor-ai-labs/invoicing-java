package com.acme.invoicing.xml;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

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
