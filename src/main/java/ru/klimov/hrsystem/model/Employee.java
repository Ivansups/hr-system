package ru.klimov.hrsystem.model;

import java.time.LocalDate;

public final class Employee {
    private Long id;
    private String fullName;
    private LocalDate hireDate;
    private Department department;
    private Position position;

    public Employee(String fullName, LocalDate hireDate, Department department, Position position) {
        this.fullName = fullName;
        this.hireDate = hireDate;
        this.department = department;
        this.position = position;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
}
