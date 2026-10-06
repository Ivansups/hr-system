package ru.klimov.hrsystem.model;

public final class Department {
    private Long id;
    private String name;

    public Department(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
