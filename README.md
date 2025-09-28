# Swiss-Re
Code exercise 106
# Organizational Analysis Tool

A Java console application to analyze organizational structure and identify potential improvements.

## Overview

This tool analyzes employee data from CSV files and identifies:

1. **Managers earning too little**: Managers who earn less than 20% more than the average salary of their direct subordinates
2. **Managers earning too much**: Managers who earn more than 50% more than the average salary of their direct subordinates  
3. **Long reporting lines**: Employees who have more than 4 managers between them and the CEO

## Requirements Met

- ✅ **Java SE only** - Uses only standard Java libraries (no Spring Boot)
- ✅ **JUnit for testing** - Comprehensive test suite with 23+ test cases
- ✅ **Maven project structure** - Standard Maven layout and configuration
- ✅ **Console application** - Reads from file and prints results to console
- ✅ **Simple, readable code** - Clean, well-documented implementation
- ✅ **Testable architecture** - Services are easily unit testable
- ✅ **Extensible design** - Modular components that can be extended

## Project Structure

```
src/
├── main/java/com/bigcompany/analysis/
│   ├── OrganizationalAnalysisApplication.java  # Main console application
│   ├── model/
│   │   └── Employee.java                       # Employee data model
│   └── service/
│       ├── CsvReader.java                      # CSV file reading service
│       └── OrganizationalAnalysisService.java # Core analysis logic
└── test/java/com/bigcompany/analysis/
    ├── model/EmployeeTest.java
    └── service/
        ├── CsvReaderTest.java
        └── OrganizationalAnalysisServiceTest.java
```

## Building and Running

### Build the Application

```bash
mvn clean package
```

This creates an executable JAR: `target/organizational-analysis-1.0.0.jar`

### Run the Application

```bash
java -jar target/organizational-analysis-1.0.0.jar <csv-file-path>
```

**Example:**
```bash
java -jar target/organizational-analysis-1.0.0.jar employees.csv
```

### Run Tests

```bash
mvn test
```

## CSV File Format

Expected format:
```csv
Id,firstName,lastName,salary,managerId
123,Joe,Doe,60000,
124,Martin,Chekov,45000,123
125,Bob,Ronstad,47000,123
```

### Requirements:
- Header row must match exactly: `Id,firstName,lastName,salary,managerId`
- CEO has empty `managerId` field
- All employee IDs must be unique
- Manager IDs must reference existing employees
- Salaries must be non-negative numbers

## Analysis Rules

### Manager Salary Analysis
- **Minimum**: Manager should earn at least 20% more than average of direct subordinates
- **Maximum**: Manager should earn at most 50% more than average of direct subordinates
- Only managers with direct subordinates are analyzed

### Reporting Line Analysis
- Maximum of 4 managers allowed between any employee and the CEO
- Detects and prevents infinite loops in reporting structure

## Design Principles

### 1. Testable Code
- Services accept dependencies via constructor injection
- Pure functions with no side effects where possible
- Comprehensive test coverage including edge cases

### 2. Simple Implementation
- Clear separation of concerns (reading, parsing, analysis, output)
- Minimal dependencies (only JUnit for testing)
- Straightforward algorithms without over-engineering

### 3. Extensible Architecture
- `CsvReader` can be easily replaced with other data sources
- `OrganizationalAnalysisService` contains reusable analysis logic
- New analysis rules can be added as additional methods

### 4. Error Handling
- Comprehensive validation of CSV data
- Clear error messages for common issues
- Graceful handling of file reading errors

## Sample Output

```
=== BIG COMPANY Organizational Analysis ===
Analyzing file: employees.csv

Total employees found: 15

=== MANAGERS EARNING LESS THAN THEY SHOULD ===
Joe Doe (ID: 123) earns $4800.00 less than they should
  Current salary: $60000.00, Should earn at least: $64800.00
  Based on 3 direct subordinates with average salary: $54000.00

=== MANAGERS EARNING MORE THAN THEY SHOULD ===  
Linda Smith (ID: 400) earns $28500.00 more than they should
  Current salary: $70000.00, Should earn at most: $41500.00
  Based on 3 direct subordinates with average salary: $27666.67

=== EMPLOYEES WITH TOO LONG REPORTING LINES ===
Too Deep (ID: 504) has reporting line that is too long
  Current managers above them: 7, Excess: 3 (maximum allowed: 4)

=== ANALYSIS COMPLETE ===
```

## Key Features

- **Robust CSV parsing** with detailed validation
- **Circular reference detection** in reporting structure  
- **Comprehensive error handling** with helpful messages
- **Clean console output** with detailed explanations
- **Extensive test suite** covering normal and edge cases
- **Production-ready code** with proper documentation
