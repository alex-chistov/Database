package org.example;

import java.io.Serializable;

public class Record implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id; // Ключевое поле
    private String name;
    private double salary;
    private boolean active;

    public Record(int id, String name, double salary, boolean active) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.active = active;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return String.format("ID: %d, Name: %s, Salary: %.2f, Active: %b", id, name, salary, active);
    }
}
