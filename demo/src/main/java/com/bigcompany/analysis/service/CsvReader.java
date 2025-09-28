package com.bigcompany.analysis.service;

import com.bigcompany.analysis.model.Employee;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to read employee data from CSV files.
 * 
 * Expected CSV format:
 * Id,firstName,lastName,salary,managerId
 * 123,Joe,Doe,60000,
 * 124,Martin,Chekov,45000,123
 * 
 * Assumptions:
 * - CSV has header row
 * - CEO has empty managerId
 * - All fields are present and valid
 * - IDs are unique integers
 * - Salaries are valid numbers
 */
public class CsvReader {
    
    /**
     * Reads employees from a CSV file.
     * 
     * @param filePath Path to the CSV file
     * @return List of employees
     * @throws IOException If file reading fails
     * @throws IllegalArgumentException If CSV format is invalid
     */
    public List<Employee> readEmployees(String filePath) throws IOException {
        List<Employee> employees = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }
                
                // Skip header row (first non-empty line)
                if (isFirstLine) {
                    isFirstLine = false;
                    validateHeader(line, lineNumber);
                    continue;
                }
                
                try {
                    Employee employee = parseCsvLine(line, lineNumber);
                    employees.add(employee);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        String.format("Error parsing line %d: '%s'. %s", lineNumber, line, e.getMessage()), e);
                }
            }
        }
        
        if (employees.isEmpty()) {
            throw new IllegalArgumentException("No employee data found in CSV file");
        }
        
        validateEmployeeData(employees);
        return employees;
    }
    
    /**
     * Validates that the header row has the expected format.
     */
    private void validateHeader(String headerLine, int lineNumber) {
        String expectedHeader = "Id,firstName,lastName,salary,managerId";
        if (!headerLine.equals(expectedHeader)) {
            throw new IllegalArgumentException(
                String.format("Invalid CSV header at line %d. Expected: '%s', Found: '%s'", 
                              lineNumber, expectedHeader, headerLine));
        }
    }
    
    /**
     * Parses a single CSV line into an Employee object.
     */
    private Employee parseCsvLine(String line, int lineNumber) {
        String[] fields = line.split(",", -1); // -1 to preserve empty trailing fields
        
        if (fields.length != 5) {
            throw new IllegalArgumentException(
                String.format("Expected 5 fields but found %d", fields.length));
        }
        
        try {
            // Parse ID
            Long id = Long.parseLong(fields[0].trim());
            
            // Parse names
            String firstName = fields[1].trim();
            String lastName = fields[2].trim();
            
            if (firstName.isEmpty()) {
                throw new IllegalArgumentException("firstName cannot be empty");
            }
            if (lastName.isEmpty()) {
                throw new IllegalArgumentException("lastName cannot be empty");
            }
            
            // Parse salary
            Double salary = Double.parseDouble(fields[3].trim());
            if (salary < 0) {
                throw new IllegalArgumentException("Salary cannot be negative");
            }
            
            // Parse managerId (can be empty for CEO)
            Long managerId = null;
            String managerIdStr = fields[4].trim();
            if (!managerIdStr.isEmpty()) {
                managerId = Long.parseLong(managerIdStr);
            }
            
            return new Employee(id, firstName, lastName, salary, managerId);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates the consistency of employee data.
     */
    private void validateEmployeeData(List<Employee> employees) {
        // Check for exactly one CEO
        long ceoCount = employees.stream().filter(Employee::isCEO).count();
        if (ceoCount == 0) {
            throw new IllegalArgumentException("No CEO found (employee with no manager)");
        }
        if (ceoCount > 1) {
            throw new IllegalArgumentException("Multiple CEOs found (employees with no manager)");
        }
        
        // Check for unique IDs
        long uniqueIdCount = employees.stream().map(Employee::getId).distinct().count();
        if (uniqueIdCount != employees.size()) {
            throw new IllegalArgumentException("Duplicate employee IDs found");
        }
        
        // Check that all manager IDs reference existing employees
        List<Long> allIds = employees.stream().map(Employee::getId).toList();
        
        for (Employee employee : employees) {
            if (!employee.isCEO()) {
                Long managerId = employee.getManagerId();
                if (!allIds.contains(managerId)) {
                    throw new IllegalArgumentException(
                        String.format("Employee %s (ID: %d) references non-existent manager ID: %d", 
                                      employee.getFullName(), employee.getId(), managerId));
                }
                
                // Check that employee doesn't reference themselves as manager
                if (managerId.equals(employee.getId())) {
                    throw new IllegalArgumentException(
                        String.format("Employee %s (ID: %d) cannot be their own manager", 
                                      employee.getFullName(), employee.getId()));
                }
            }
        }
    }
}
