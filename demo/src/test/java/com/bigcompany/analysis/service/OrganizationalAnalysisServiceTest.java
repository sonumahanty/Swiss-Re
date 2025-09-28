package com.bigcompany.analysis.service;

import com.bigcompany.analysis.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationalAnalysisServiceTest {

    private List<Employee> employees;
    private OrganizationalAnalysisService analysisService;

    @BeforeEach
    void setUp() {
        employees = Arrays.asList(
                new Employee(1L, "CEO", "Boss", 100000.0, null),              // CEO
                new Employee(2L, "Manager1", "Good", 54000.0, 1L),            // Good manager (20% more than avg: 45000 * 1.2 = 54000)
                new Employee(3L, "Manager2", "Underpaid", 50000.0, 1L),       // Underpaid manager (should earn at least 54000: 45000 * 1.2)
                new Employee(4L, "Manager3", "Overpaid", 80000.0, 1L),        // Overpaid manager (max should be: 45000 * 1.5 = 67500)
                new Employee(5L, "Employee1", "One", 45000.0, 2L),            // Under Manager1 (Good)
                new Employee(6L, "Employee2", "Two", 45000.0, 2L),            // Under Manager1 (Good) 
                new Employee(7L, "Employee3", "Three", 40000.0, 3L),          // Under Manager2 (Underpaid) - avg = 45000
                new Employee(8L, "Employee4", "Four", 50000.0, 3L),           // Under Manager2 (Underpaid) - avg = 45000
                new Employee(9L, "Employee5", "Five", 45000.0, 4L),           // Under Manager3 (Overpaid) - avg = 45000
                new Employee(10L, "Deep1", "Level", 35000.0, 7L),             // Level 3 from CEO
                new Employee(11L, "Deep2", "Level", 30000.0, 10L),            // Level 4 from CEO
                new Employee(12L, "Deep3", "Level", 25000.0, 11L),            // Level 5 from CEO (too deep!)
                new Employee(13L, "Deep4", "Level", 20000.0, 12L)             // Level 6 from CEO (too deep!)
        );
        
        analysisService = new OrganizationalAnalysisService(employees);
    }

    @Test
    void testFindManagersEarningTooLittle() {
        List<OrganizationalAnalysisService.ManagerSalaryIssue> underpaidManagers = 
                analysisService.findManagersEarningTooLittle();

        // Only Manager2 should be underpaid
        // Manager2 has subordinates Employee3 (40000) and Employee4 (50000), avg = 45000
        // Minimum salary should be 45000 * 1.2 = 54000, but Manager2 earns 50000
        // So Manager2 is underpaid by 4000
        
        // Filter to find only the managers we expect
        List<OrganizationalAnalysisService.ManagerSalaryIssue> actualUnderpaid = underpaidManagers.stream()
                .filter(issue -> issue.getManager().getLastName().equals("Underpaid"))
                .toList();
                
        assertEquals(1, actualUnderpaid.size());
        
        OrganizationalAnalysisService.ManagerSalaryIssue issue = actualUnderpaid.get(0);
        assertEquals("Manager2 Underpaid", issue.getManager().getFullName());
        assertEquals(4000.0, issue.getAmount(), 0.01); // 54000 - 50000 = 4000
        assertEquals("UNDERPAID", issue.getType());
        assertEquals(45000.0, issue.getAvgSubordinateSalary(), 0.01);
        assertEquals(2, issue.getSubordinateCount());
    }

    @Test
    void testFindManagersEarningTooMuch() {
        List<OrganizationalAnalysisService.ManagerSalaryIssue> overpaidManagers = 
                analysisService.findManagersEarningTooMuch();

        // Only Manager3 should be overpaid
        // Manager3 has subordinate Employee5 (45000), avg = 45000
        // Maximum salary should be 45000 * 1.5 = 67500, but Manager3 earns 80000
        // So Manager3 is overpaid by 12500
        
        List<OrganizationalAnalysisService.ManagerSalaryIssue> actualOverpaid = overpaidManagers.stream()
                .filter(issue -> issue.getManager().getLastName().equals("Overpaid"))
                .toList();

        assertEquals(1, actualOverpaid.size());
        
        OrganizationalAnalysisService.ManagerSalaryIssue issue = actualOverpaid.get(0);
        assertEquals("Manager3 Overpaid", issue.getManager().getFullName());
        assertEquals(12500.0, issue.getAmount(), 0.01); // 80000 - 67500 = 12500
        assertEquals("OVERPAID", issue.getType());
        assertEquals(45000.0, issue.getAvgSubordinateSalary(), 0.01);
        assertEquals(1, issue.getSubordinateCount());
    }

    @Test
    void testFindEmployeesWithTooLongReportingLines() {
        List<OrganizationalAnalysisService.ReportingLineIssue> longReportingLines = 
                analysisService.findEmployeesWithTooLongReportingLines();

        assertEquals(2, longReportingLines.size());
        
        // Check first issue (Deep3 Level - 5 managers to CEO)
        OrganizationalAnalysisService.ReportingLineIssue issue1 = longReportingLines.get(0);
        assertEquals("Deep3 Level", issue1.getEmployee().getFullName());
        assertEquals(5, issue1.getActualManagerCount());
        assertEquals(1, issue1.getExcessManagerCount());
        
        // Check second issue (Deep4 Level - 6 managers to CEO)
        OrganizationalAnalysisService.ReportingLineIssue issue2 = longReportingLines.get(1);
        assertEquals("Deep4 Level", issue2.getEmployee().getFullName());
        assertEquals(6, issue2.getActualManagerCount());
        assertEquals(2, issue2.getExcessManagerCount());
    }

    @Test
    void testNoCEO() {
        // Create employees without a CEO
        List<Employee> employeesWithoutCEO = Arrays.asList(
                new Employee(1L, "Manager", "One", 50000.0, 2L),
                new Employee(2L, "Manager", "Two", 60000.0, 3L)
        );
        
        OrganizationalAnalysisService service = new OrganizationalAnalysisService(employeesWithoutCEO);
        
        assertThrows(IllegalStateException.class, () -> {
            service.findEmployeesWithTooLongReportingLines();
        });
    }

    @Test
    void testCircularReportingStructure() {
        // Create circular reporting structure
        List<Employee> circularEmployees = Arrays.asList(
                new Employee(1L, "Employee", "One", 50000.0, 2L),
                new Employee(2L, "Employee", "Two", 60000.0, 1L)  // Circular reference
        );
        
        OrganizationalAnalysisService service = new OrganizationalAnalysisService(circularEmployees);
        
        assertThrows(IllegalStateException.class, () -> {
            service.findEmployeesWithTooLongReportingLines();
        });
    }

    @Test
    void testManagerWithNoSubordinates() {
        // Create a simple hierarchy where a manager has no subordinates
        List<Employee> simpleHierarchy = Arrays.asList(
                new Employee(1L, "CEO", "Boss", 100000.0, null),
                new Employee(2L, "Manager", "Lonely", 50000.0, 1L)  // No subordinates
        );
        
        OrganizationalAnalysisService service = new OrganizationalAnalysisService(simpleHierarchy);
        
        List<OrganizationalAnalysisService.ManagerSalaryIssue> underpaid = service.findManagersEarningTooLittle();
        List<OrganizationalAnalysisService.ManagerSalaryIssue> overpaid = service.findManagersEarningTooMuch();
        
        // Manager with no subordinates should not appear in salary issues
        // Only check for the specific manager we're testing (not the CEO)
        boolean hasLonelyManagerIssues = underpaid.stream().anyMatch(issue -> 
                issue.getManager().getLastName().equals("Lonely")) ||
                overpaid.stream().anyMatch(issue -> 
                issue.getManager().getLastName().equals("Lonely"));
                
        assertFalse(hasLonelyManagerIssues, "Manager with no subordinates should not have salary issues");
    }

    @Test
    void testSimpleValidHierarchy() {
        // Test with data from the assignment example
        List<Employee> assignmentData = Arrays.asList(
                new Employee(123L, "Joe", "Doe", 60000.0, null),       // CEO
                new Employee(124L, "Martin", "Chekov", 45000.0, 123L), // Under CEO
                new Employee(125L, "Bob", "Ronstad", 47000.0, 123L),   // Under CEO
                new Employee(300L, "Alice", "Hasacat", 50000.0, 124L), // Under Martin
                new Employee(305L, "Brett", "Hardleaf", 34000.0, 300L) // Under Alice
        );
        
        OrganizationalAnalysisService service = new OrganizationalAnalysisService(assignmentData);
        
        // Check for salary issues
        List<OrganizationalAnalysisService.ManagerSalaryIssue> underpaid = service.findManagersEarningTooLittle();
        List<OrganizationalAnalysisService.ManagerSalaryIssue> overpaid = service.findManagersEarningTooMuch();
        
        // Joe (CEO) has Martin (45000) and Bob (47000) as subordinates, avg = 46000
        // Joe earns 60000, should earn at least 46000 * 1.2 = 55200 ✓ (not underpaid)
        // Joe earns 60000, should earn at most 46000 * 1.5 = 69000 ✓ (not overpaid)
        
        // Martin has Alice (50000) as subordinate, avg = 50000
        // Martin earns 45000, should earn at least 50000 * 1.2 = 60000 ✗ (underpaid by 15000)
        
        // Alice has Brett (34000) as subordinate, avg = 34000
        // Alice earns 50000, should earn at least 34000 * 1.2 = 40800 ✓ (not underpaid)
        // Alice earns 50000, should earn at most 34000 * 1.5 = 51000 ✓ (not overpaid)
        
        assertEquals(1, underpaid.size());
        assertEquals("Martin Chekov", underpaid.get(0).getManager().getFullName());
        assertEquals(15000.0, underpaid.get(0).getAmount(), 0.01);
        
        assertTrue(overpaid.isEmpty());
        
        // Check reporting line issues
        List<OrganizationalAnalysisService.ReportingLineIssue> longLines = service.findEmployeesWithTooLongReportingLines();
        assertTrue(longLines.isEmpty()); // No one has more than 4 managers above them
    }
}
