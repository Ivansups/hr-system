package ru.klimov.hrsystem.model;

import java.math.BigDecimal;

public abstract sealed class Position
        permits DeveloperPosition, ManagerPosition, SalespersonPosition {

    private Long id;
    private final String title;
    private final BigDecimal baseSalary;

    protected Position(String title, BigDecimal baseSalary) {
        this.title = title;
        this.baseSalary = baseSalary;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public BigDecimal getBaseSalary() { return baseSalary; }
    public abstract PositionType getType();
}
