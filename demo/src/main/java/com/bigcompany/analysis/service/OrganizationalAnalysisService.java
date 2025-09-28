package com.bigcompany.analysis.service;

import com.bigcompany.analysis.model.Employee;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to analyze organizational structure and identify issues.
 * 
 * This service provides methods to identify:
 * - Managers who earn less than 20% more than their direct subordinates' average salary
 * - Managers who earn more than 50% more than their direct subordinates' average salary  
 * - Employees with more than 4 managers between them and the CEO
 */
public class OrganizationalAnalysisService {
    
    private final List<Employee> employees;
    private final Map<Long, Employee> employeeMap;
    private final Map<Long, List<Employee>> subordinatesMap;
    
    public OrganizationalAnalysisService(List<Employee> employees) {
        this.employees = employees;
        this.employeeMap = employees.stream()
                .collect(Collectors.toMap(Employee::getId, employee -> employee));
        this.subordinatesMap = buildSubordinatesMap();
    }
    
    /**
     * Builds a map of manager ID to list of direct subordinates.
     */
    private Map<Long, List<Employee>> buildSubordinatesMap() {
        Map<Long, List<Employee>> map = new HashMap<>();
        
        for (Employee employee : employees) {
            if (!employee.isCEO()) {
                Long managerId = employee.getManagerId();
                map.computeIfAbsent(managerId, k -> new ArrayList<>()).add(employee);
            }
        }
        
        return map;
    }
    
    /**
     * Finds managers who earn less than 20% more than the average salary of their direct subordinates.
     */
    public List<ManagerSalaryIssue> findManagersEarningTooLittle() {
        List<ManagerSalaryIssue> issues = new ArrayList<>();
        
        for (Employee manager : employees) {
            List<Employee> subordinates = subordinatesMap.get(manager.getId());
            if (subordinates != null && !subordinates.isEmpty()) {
                double avgSubordinateSalary = subordinates.stream()
                        .mapToDouble(Employee::getSalary)
                        .average()
                        .orElse(0.0);
                
                double minimumExpectedSalary = avgSubordinateSalary * 1.20; // 20% more
                
                if (manager.getSalary() < minimumExpectedSalary) {
                    double shortfall = minimumExpectedSalary - manager.getSalary();
                    issues.add(new ManagerSalaryIssue(manager, shortfall, "UNDERPAID", 
                            avgSubordinateSalary, subordinates.size()));
                }
            }
        }
        
        return issues;
    }
    
    /**
     * Finds managers who earn more than 50% more than the average salary of their direct subordinates.
     */
    public List<ManagerSalaryIssue> findManagersEarningTooMuch() {
        List<ManagerSalaryIssue> issues = new ArrayList<>();
        
        for (Employee manager : employees) {
            List<Employee> subordinates = subordinatesMap.get(manager.getId());
            if (subordinates != null && !subordinates.isEmpty()) {
                double avgSubordinateSalary = subordinates.stream()
                        .mapToDouble(Employee::getSalary)
                        .average()
                        .orElse(0.0);
                
                double maximumExpectedSalary = avgSubordinateSalary * 1.50; // 50% more
                
                if (manager.getSalary() > maximumExpectedSalary) {
                    double overpay = manager.getSalary() - maximumExpectedSalary;
                    issues.add(new ManagerSalaryIssue(manager, overpay, "OVERPAID",
                            avgSubordinateSalary, subordinates.size()));
                }
            }
        }
        
        return issues;
    }
    
    /**
     * Finds employees who have more than 4 managers between them and the CEO.
     */
    public List<ReportingLineIssue> findEmployeesWithTooLongReportingLines() {
        List<ReportingLineIssue> issues = new ArrayList<>();
        Employee ceo = findCEO();
        
        if (ceo == null) {
            throw new IllegalStateException("No CEO found (employee with no manager)");
        }
        
        for (Employee employee : employees) {
            if (!employee.isCEO()) {
                int managersToRoot = countManagersToRoot(employee);
                if (managersToRoot > 4) {
                    int excess = managersToRoot - 4;
                    issues.add(new ReportingLineIssue(employee, managersToRoot, excess));
                }
            }
        }
        
        return issues;
    }
    
    /**
     * Finds the CEO (employee with no manager).
     */
    private Employee findCEO() {
        return employees.stream()
                .filter(Employee::isCEO)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Counts the number of managers between an employee and the CEO.
     */
    private int countManagersToRoot(Employee employee) {
        int count = 0;
        Employee current = employee;
        Set<Long> visited = new HashSet<>(); // Prevent infinite loops
        
        while (current != null && !current.isCEO()) {
            if (visited.contains(current.getId())) {
                throw new IllegalStateException("Circular reporting structure detected for employee: " + current.getFullName());
            }
            visited.add(current.getId());
            
            current = employeeMap.get(current.getManagerId());
            count++;
        }
        
        return count;
    }
    
    /**
     * Data class representing a manager salary issue.
     */
    public static class ManagerSalaryIssue {
        private final Employee manager;
        private final double amount;
        private final String type; // UNDERPAID or OVERPAID
        private final double avgSubordinateSalary;
        private final int subordinateCount;
        
        public ManagerSalaryIssue(Employee manager, double amount, String type, 
                                 double avgSubordinateSalary, int subordinateCount) {
            this.manager = manager;
            this.amount = amount;
            this.type = type;
            this.avgSubordinateSalary = avgSubordinateSalary;
            this.subordinateCount = subordinateCount;
        }
        
        public Employee getManager() { return manager; }
        public double getAmount() { return amount; }
        public String getType() { return type; }
        public double getAvgSubordinateSalary() { return avgSubordinateSalary; }
        public int getSubordinateCount() { return subordinateCount; }
    }
    
    /**
     * Data class representing a reporting line issue.
     */
    public static class ReportingLineIssue {
        private final Employee employee;
        private final int actualManagerCount;
        private final int excessManagerCount;
        
        public ReportingLineIssue(Employee employee, int actualManagerCount, int excessManagerCount) {
            this.employee = employee;
            this.actualManagerCount = actualManagerCount;
            this.excessManagerCount = excessManagerCount;
        }
        
        public Employee getEmployee() { return employee; }
        public int getActualManagerCount() { return actualManagerCount; }
        public int getExcessManagerCount() { return excessManagerCount; }
    }
}
