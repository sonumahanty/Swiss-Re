package com.bigcompany.analysis.service;

import com.bigcompany.analysis.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvReaderTest {

    @TempDir
    Path tempDir;
    
    private CsvReader csvReader;

    @BeforeEach
    void setUp() {
        csvReader = new CsvReader();
    }

    @Test
    void testReadValidCsv() throws IOException {
        Path csvFile = tempDir.resolve("employees.csv");
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,CEO,100000,
                2,Jane,Manager,60000,1
                3,Bob,Employee,50000,2
                """;
        
        Files.write(csvFile, csvContent.getBytes());
        
        List<Employee> employees = csvReader.readEmployees(csvFile.toString());
        
        assertEquals(3, employees.size());
        
        Employee ceo = employees.get(0);
        assertEquals(1L, ceo.getId());
        assertEquals("John", ceo.getFirstName());
        assertEquals("CEO", ceo.getLastName());
        assertEquals(100000.0, ceo.getSalary());
        assertNull(ceo.getManagerId());
        assertTrue(ceo.isCEO());
        
        Employee manager = employees.get(1);
        assertEquals(2L, manager.getId());
        assertEquals("Jane", manager.getFirstName());
        assertEquals("Manager", manager.getLastName());
        assertEquals(60000.0, manager.getSalary());
        assertEquals(1L, manager.getManagerId());
        assertFalse(manager.isCEO());
    }

    @Test
    void testReadCsvWithInvalidHeader() throws IOException {
        Path csvFile = tempDir.resolve("bad_header.csv");
        String csvContent = """
                ID,FirstName,LastName,Salary,ManagerID
                1,John,CEO,100000,
                """;
        
        Files.write(csvFile, csvContent.getBytes());
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> csvReader.readEmployees(csvFile.toString())
        );
        
        assertTrue(exception.getMessage().contains("Invalid CSV header"));
    }

    @Test
    void testReadCsvWithMissingFields() throws IOException {
        Path csvFile = tempDir.resolve("missing_fields.csv");
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,CEO,100000
                """;
        
        Files.write(csvFile, csvContent.getBytes());
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> csvReader.readEmployees(csvFile.toString())
        );
        
        assertTrue(exception.getMessage().contains("Expected 5 fields but found 4"));
    }

    @Test
    void testReadCsvWithInvalidSalary() throws IOException {
        Path csvFile = tempDir.resolve("invalid_salary.csv");
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,CEO,invalid,
                """;
        
        Files.write(csvFile, csvContent.getBytes());
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> csvReader.readEmployees(csvFile.toString())
        );
        
        assertTrue(exception.getMessage().contains("Invalid number format"));
    }

    @Test
    void testReadCsvWithNegativeSalary() throws IOException {
        Path csvFile = tempDir.resolve("negative_salary.csv");
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,CEO,-50000,
                """;
        
        Files.write(csvFile, csvContent.getBytes());
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> csvReader.readEmployees(csvFile.toString())
        );
        
        assertTrue(exception.getMessage().contains("Salary cannot be negative"));
    }

    @Test
    void testReadCsvWithMultipleCEOs() throws IOException {
        Path csvFile = tempDir.resolve("multiple_ceos.csv");
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,CEO,100000,
                2,Jane,CEO,110000,
                """;
        
        Files.write(csvFile, csvContent.getBytes());
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> csvReader.readEmployees(csvFile.toString())
        );
        
        assertTrue(exception.getMessage().contains("Multiple CEOs found"));
    }

    @Test
    void testReadCsvWithNoCEO() throws IOException {
        Path csvFile = tempDir.resolve("no_ceo.csv");
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,Manager,60000,2
                2,Jane,Employee,50000,1
                """;
        
        Files.write(csvFile, csvContent.getBytes());
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> csvReader.readEmployees(csvFile.toString())
        );
        
        assertTrue(exception.getMessage().contains("No CEO found"));
    }

    @Test
    void testReadCsvWithDuplicateIds() throws IOException {
        Path csvFile = tempDir.resolve("duplicate_ids.csv");
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,CEO,100000,
                1,Jane,Manager,60000,1
                """;
        
        Files.write(csvFile, csvContent.getBytes());
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> csvReader.readEmployees(csvFile.toString())
        );
        
        assertTrue(exception.getMessage().contains("Duplicate employee IDs found"));
    }

    @Test
    void testReadCsvWithNonexistentManager() throws IOException {
        Path csvFile = tempDir.resolve("nonexistent_manager.csv");
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,CEO,100000,
                2,Jane,Manager,60000,999
                """;
        
        Files.write(csvFile, csvContent.getBytes());
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> csvReader.readEmployees(csvFile.toString())
        );
        
        assertTrue(exception.getMessage().contains("references non-existent manager ID: 999"));
    }

    @Test
    void testReadCsvWithSelfReference() throws IOException {
        Path csvFile = tempDir.resolve("self_reference.csv");
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,CEO,100000,
                2,Jane,Manager,60000,2
                """;
        
        Files.write(csvFile, csvContent.getBytes());
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> csvReader.readEmployees(csvFile.toString())
        );
        
        assertTrue(exception.getMessage().contains("cannot be their own manager"));
    }

    @Test
    void testReadCsvWithEmptyNames() throws IOException {
        Path csvFile = tempDir.resolve("empty_names.csv");
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,,CEO,100000,
                """;
        
        Files.write(csvFile, csvContent.getBytes());
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> csvReader.readEmployees(csvFile.toString())
        );
        
        assertTrue(exception.getMessage().contains("firstName cannot be empty"));
    }

    @Test
    void testFileNotFound() {
        IOException exception = assertThrows(
                IOException.class, 
                () -> csvReader.readEmployees("/nonexistent/file.csv")
        );
        
        assertTrue(exception.getMessage().contains("No such file or directory") || 
                  exception.getMessage().contains("cannot find the file"));
    }
}
