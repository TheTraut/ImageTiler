#!/bin/bash

# Calibration Test Runner
# Executes all automated tests for calibration functionality

echo "🧪 ImageTiler Calibration Test Suite"
echo "===================================="
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if Java is available
if ! command -v javac &> /dev/null; then
    echo -e "${RED}❌ Error: Java compiler (javac) not found.${NC}"
    echo "Please install Java JDK to run tests."
    exit 1
fi

# Check if JUnit is available (part of build setup)
echo -e "${BLUE}📦 Checking dependencies...${NC}"

# Create build directory if it doesn't exist
mkdir -p build/test

# Set classpath
CLASSPATH="lib/*:build:src"
JUNIT_JAR="lib/junit-platform-console-standalone-1.8.2.jar"

# Check if JUnit JAR exists, if not, try to find it or provide guidance
if [ ! -f "$JUNIT_JAR" ]; then
    echo -e "${YELLOW}⚠️  JUnit JAR not found at expected location: $JUNIT_JAR${NC}"
    echo "Searching for JUnit JAR in lib directory..."
    
    JUNIT_JAR=$(find lib -name "*junit*" -type f | head -1)
    if [ -z "$JUNIT_JAR" ]; then
        echo -e "${RED}❌ JUnit JAR not found in lib directory.${NC}"
        echo ""
        echo "To run tests, you need to download JUnit 5:"
        echo "1. Download from: https://junit.org/junit5/docs/current/user-guide/#running-tests-console-launcher"
        echo "2. Place the junit-platform-console-standalone JAR in the lib/ directory"
        echo "3. Or run: wget https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.8.2/junit-platform-console-standalone-1.8.2.jar -O lib/junit-platform-console-standalone-1.8.2.jar"
        echo ""
        exit 1
    else
        echo -e "${GREEN}✅ Found JUnit JAR: $JUNIT_JAR${NC}"
    fi
fi

# Update classpath to include JUnit
CLASSPATH="$JUNIT_JAR:$CLASSPATH"

echo -e "${BLUE}🔨 Compiling main sources...${NC}"

# Compile main Java sources first
javac -cp "$CLASSPATH" -d build src/*.java
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to compile main sources${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Main sources compiled successfully${NC}"

echo -e "${BLUE}🔨 Compiling test sources...${NC}"

# Compile test sources
javac -cp "$CLASSPATH" -d build/test src/test/java/*.java
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to compile test sources${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Test sources compiled successfully${NC}"
echo ""

# Function to run a specific test class
run_test_class() {
    local test_class=$1
    local test_name=$2
    
    echo -e "${BLUE}🧪 Running $test_name...${NC}"
    
    # Run the test using JUnit console launcher
    java -cp "$CLASSPATH:build/test" org.junit.platform.console.ConsoleLauncher \
        --class-path "build/test:build:$CLASSPATH" \
        --select-class "$test_class" \
        --details=verbose
    
    local exit_code=$?
    
    if [ $exit_code -eq 0 ]; then
        echo -e "${GREEN}✅ $test_name passed${NC}"
        return 0
    else
        echo -e "${RED}❌ $test_name failed${NC}"
        return 1
    fi
}

# Track test results
total_tests=0
passed_tests=0
failed_tests=0

# Run individual test suites
echo -e "${YELLOW}📋 Executing Test Suites${NC}"
echo "========================="
echo ""

# Test 1: Existing Calibration Image Test
if [ -f "src/test/java/CalibrationImageTest.java" ]; then
    total_tests=$((total_tests + 1))
    if run_test_class "CalibrationImageTest" "Calibration Image Basic Tests"; then
        passed_tests=$((passed_tests + 1))
    else
        failed_tests=$((failed_tests + 1))
    fi
    echo ""
fi

# Test 2: New ImagePanel Calibration Tests
if [ -f "src/test/java/ImagePanelCalibrationTest.java" ]; then
    total_tests=$((total_tests + 1))
    if run_test_class "ImagePanelCalibrationTest" "ImagePanel Calibration Unit Tests"; then
        passed_tests=$((passed_tests + 1))
    else
        failed_tests=$((failed_tests + 1))
    fi
    echo ""
fi

# Test 3: Integration Tests
if [ -f "src/test/java/CalibrationPrintingIntegrationTest.java" ]; then
    total_tests=$((total_tests + 1))
    if run_test_class "CalibrationPrintingIntegrationTest" "Calibration Printing Integration Tests"; then
        passed_tests=$((passed_tests + 1))
    else
        failed_tests=$((failed_tests + 1))
    fi
    echo ""
fi

# Test 4: Scale Calculator Tests (if they exist)
if [ -f "src/test/java/ScaleCalculatorTest.java" ]; then
    total_tests=$((total_tests + 1))
    if run_test_class "ScaleCalculatorTest" "Scale Calculator Tests"; then
        passed_tests=$((passed_tests + 1))
    else
        failed_tests=$((failed_tests + 1))
    fi
    echo ""
fi

# Summary
echo "========================="
echo -e "${BLUE}📊 Test Results Summary${NC}"
echo "========================="
echo ""
echo -e "Total test suites run: ${BLUE}$total_tests${NC}"
echo -e "Passed: ${GREEN}$passed_tests${NC}"
echo -e "Failed: ${RED}$failed_tests${NC}"
echo ""

if [ $failed_tests -eq 0 ]; then
    echo -e "${GREEN}🎉 All tests passed! Calibration functionality is working correctly.${NC}"
    echo ""
    echo -e "${YELLOW}📋 Next Steps:${NC}"
    echo "1. Review the manual testing guide: CALIBRATION_MANUAL_TESTING_GUIDE.md"
    echo "2. Perform manual testing on different printers and PDF viewers"
    echo "3. Verify physical measurements of printed calibration sheets"
    echo ""
    exit 0
else
    echo -e "${RED}⚠️  Some tests failed. Please review the test output above.${NC}"
    echo ""
    echo -e "${YELLOW}📋 Debugging Steps:${NC}"
    echo "1. Check that the calibration image exists at: src/main/resources/calibration/calibration.png"
    echo "2. Verify that all required dependencies are in the lib/ directory"
    echo "3. Review the test failure messages for specific issues"
    echo "4. Check console output for [CALIBRATION] debug messages"
    echo ""
    exit 1
fi
