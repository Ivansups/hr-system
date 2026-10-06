package ru.klimov.hrsystem.model;

import java.math.BigDecimal;

public final class SalespersonPosition extends Position {
    private final BigDecimal salesPercent;

    public SalespersonPosition(String title, BigDecimal baseSalary, BigDecimal salesPercent) {
        super(title, baseSalary);
        this.salesPercent = salesPercent;
    }

    public BigDecimal getSalesPercent() { return salesPercent; }

    @Override
    public PositionType getType() { return PositionType.SALESPERSON; }
}
