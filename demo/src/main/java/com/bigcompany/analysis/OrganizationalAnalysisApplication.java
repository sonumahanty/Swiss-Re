package com.bigcompany.analysis;

import com.bigcompany.analysis.model.Employee;
import com.bigcompany.analysis.service.CsvReader;
import com.bigcompany.analysis.service.OrganizationalAnalysisService;

import java.io.File;
import java.util.List;

/**
 * Main application to analyze organizational structure.
 * 
 * This application reads employee data from a CSV file and identifies:
 * - Managers who earn less than they should (less than 20% more than average of direct subordinates)
 * - Managers who earn more than they should (more than 50% more than average of direct subordinates)  
 * - Employees with reporting lines that are too long (more than 4 managers between them and CEO)
 */
public class OrganizationalAnalysisApplication {
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar organizational-analysis.jar <csv-file-path>");
            System.err.println("Example: java -jar organizational-analysis.jar employees.csv");
            System.exit(1);
        }
        
        String csvFilePath = args[0];
        
        // Validate file exists
        File csvFile = new File(csvFilePath);
        if (!csvFile.exists()) {
            System.err.println("Error: File '" + csvFilePath + "' does not exist.");
            System.exit(1);
        }
        
        if (!csvFile.isFile()) {
            System.err.println("Error: '" + csvFilePath + "' is not a file.");
            System.exit(1);
        }
        
        try {
            System.out.println("=== BIG COMPANY Organizational Analysis ===");
            System.out.println("Analyzing file: " + csvFilePath);
            System.out.println();
            
            // Read employees from CSV
            CsvReader csvReader = new CsvReader();
            List<Employee> employees = csvReader.readEmployees(csvFilePath);
            
            System.out.println("Total employees found: " + employees.size());
            System.out.println();
            
            // Perform analysis
            OrganizationalAnalysisService analysisService = new OrganizationalAnalysisService(employees);
            
            // 1. Display managers earning too little
            printUnderpaidManagers(analysisService);
            
            // 2. Display managers earning too much  
            printOverpaidManagers(analysisService);
            
            // 3. Display employees with too long reporting lines
            printLongReportingLines(analysisService);
            
            System.out.println("=== ANALYSIS COMPLETE ===");
            
        } catch (Exception e) {
            System.err.println("Error during analysis: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void printUnderpaidManagers(OrganizationalAnalysisService analysisService) {
        System.out.println("=== MANAGERS EARNING LESS THAN THEY SHOULD ===");
        
        List<OrganizationalAnalysisService.ManagerSalaryIssue> underpaidManagers = 
                analysisService.findManagersEarningTooLittle();
        
        if (underpaidManagers.isEmpty()) {
            System.out.println("No underpaid managers found.");
        } else {
            for (OrganizationalAnalysisService.ManagerSalaryIssue issue : underpaidManagers) {
                System.out.printf("%s (ID: %d) earns $%.2f less than they should%n",
                        issue.getManager().getFullName(),
                        issue.getManager().getId(),
                        issue.getAmount());
                        
                System.out.printf("  Current salary: $%.2f, Should earn at least: $%.2f%n",
                        issue.getManager().getSalary(),
                        issue.getManager().getSalary() + issue.getAmount());
                        
                System.out.printf("  Based on %d direct subordinates with average salary: $%.2f%n",
                        issue.getSubordinateCount(),
                        issue.getAvgSubordinateSalary());
                System.out.println();
            }
        }
        System.out.println();
    }
    
    private static void printOverpaidManagers(OrganizationalAnalysisService analysisService) {
        System.out.println("=== MANAGERS EARNING MORE THAN THEY SHOULD ===");
        
        List<OrganizationalAnalysisService.ManagerSalaryIssue> overpaidManagers = 
                analysisService.findManagersEarningTooMuch();
        
        if (overpaidManagers.isEmpty()) {
            System.out.println("No overpaid managers found.");
        } else {
            for (OrganizationalAnalysisService.ManagerSalaryIssue issue : overpaidManagers) {
                System.out.printf("%s (ID: %d) earns $%.2f more than they should%n",
                        issue.getManager().getFullName(),
                        issue.getManager().getId(),
                        issue.getAmount());
                        
                System.out.printf("  Current salary: $%.2f, Should earn at most: $%.2f%n",
                        issue.getManager().getSalary(),
                        issue.getManager().getSalary() - issue.getAmount());
                        
                System.out.printf("  Based on %d direct subordinates with average salary: $%.2f%n",
                        issue.getSubordinateCount(),
                        issue.getAvgSubordinateSalary());
                System.out.println();
            }
        }
        System.out.println();
    }
    
    private static void printLongReportingLines(OrganizationalAnalysisService analysisService) {
        System.out.println("=== EMPLOYEES WITH TOO LONG REPORTING LINES ===");
        
        List<OrganizationalAnalysisService.ReportingLineIssue> longReportingLines = 
                analysisService.findEmployeesWithTooLongReportingLines();
        
        if (longReportingLines.isEmpty()) {
            System.out.println("No employees with excessively long reporting lines found.");
        } else {
            for (OrganizationalAnalysisService.ReportingLineIssue issue : longReportingLines) {
                System.out.printf("%s (ID: %d) has reporting line that is too long%n",
                        issue.getEmployee().getFullName(),
                        issue.getEmployee().getId());
                        
                System.out.printf("  Current managers above them: %d, Excess: %d (maximum allowed: 4)%n",
                        issue.getActualManagerCount(),
                        issue.getExcessManagerCount());
                System.out.println();
            }
        }
        System.out.println();
    }
}
