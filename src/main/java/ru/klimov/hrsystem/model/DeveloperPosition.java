package ru.klimov.hrsystem.model;

import java.math.BigDecimal;

public final class DeveloperPosition extends Position {
    private final String techStack;
    private final int grade;

    public DeveloperPosition(String title, BigDecimal baseSalary, String techStack, int grade) {
        super(title, baseSalary);
        this.techStack = techStack;
        this.grade = grade;
    }

    public String getTechStack() { return techStack; }
    public int getGrade() { return grade; }

    @Override
    public PositionType getType() { return PositionType.DEVELOPER; }
}
