package com.bigcompany.analysis.model;

import java.util.Objects;

/**
 * Represents an employee in the organizational structure.
 * 
 * This is an immutable value object that represents a single employee
 * with their basic information and reporting relationship.
 */
public class Employee {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final Double salary;
    private final Long managerId; // null for CEO
    
    public Employee(Long id, String firstName, String lastName, Double salary, Long managerId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.managerId = managerId;
    }
    
    public Long getId() {
        return id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public Double getSalary() {
        return salary;
    }
    
    public Long getManagerId() {
        return managerId;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isCEO() {
        return managerId == null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Employee employee = (Employee) obj;
        return Objects.equals(id, employee.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", salary=" + salary +
                ", managerId=" + managerId +
                '}';
    }
}
