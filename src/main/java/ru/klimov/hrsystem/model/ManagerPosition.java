package ru.klimov.hrsystem.model;

import java.math.BigDecimal;

public final class ManagerPosition extends Position {
    private final int teamSize;

    public ManagerPosition(String title, BigDecimal baseSalary, int teamSize) {
        super(title, baseSalary);
        this.teamSize = teamSize;
    }

    public int getTeamSize() { return teamSize; }

    @Override
    public PositionType getType() { return PositionType.MANAGER; }
}
