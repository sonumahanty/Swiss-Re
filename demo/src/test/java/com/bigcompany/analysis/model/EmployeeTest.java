package com.bigcompany.analysis.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    void testEmployeeCreation() {
        Employee employee = new Employee(123L, "John", "Doe", 50000.0, 456L);
        
        assertEquals(123L, employee.getId());
        assertEquals("John", employee.getFirstName());
        assertEquals("Doe", employee.getLastName());
        assertEquals(50000.0, employee.getSalary());
        assertEquals(456L, employee.getManagerId());
        assertEquals("John Doe", employee.getFullName());
        assertFalse(employee.isCEO());
    }

    @Test
    void testCEOCreation() {
        Employee ceo = new Employee(1L, "Jane", "CEO", 100000.0, null);
        
        assertEquals(1L, ceo.getId());
        assertEquals("Jane", ceo.getFirstName());
        assertEquals("CEO", ceo.getLastName());
        assertEquals(100000.0, ceo.getSalary());
        assertNull(ceo.getManagerId());
        assertEquals("Jane CEO", ceo.getFullName());
        assertTrue(ceo.isCEO());
    }

    @Test
    void testEmployeeEquality() {
        Employee emp1 = new Employee(123L, "John", "Doe", 50000.0, 456L);
        Employee emp2 = new Employee(123L, "Jane", "Smith", 60000.0, 789L); // Same ID, different details
        Employee emp3 = new Employee(124L, "John", "Doe", 50000.0, 456L); // Different ID, same details
        
        assertEquals(emp1, emp2); // Same ID should be equal
        assertNotEquals(emp1, emp3); // Different ID should not be equal
        assertEquals(emp1.hashCode(), emp2.hashCode()); // Same hash for same ID
    }

    @Test
    void testToString() {
        Employee employee = new Employee(123L, "John", "Doe", 50000.0, 456L);
        String expected = "Employee{id=123, firstName='John', lastName='Doe', salary=50000.0, managerId=456}";
        assertEquals(expected, employee.toString());
    }
}
