#!/usr/bin/env python3
"""
Test script to reproduce the Measurement Helper dialog issue.
This script demonstrates the blank page/PDF issue when printing calibration images.
"""

import subprocess
import time
import os
import sys

def log_message(message):
    print(f"[TEST] {message}")

def main():
    log_message("Starting Measurement Helper Dialog Test")
    log_message("=" * 60)
    
    # Check if ImageTiler is available
    if not os.path.exists("ImageTiler.jar"):
        log_message("ERROR: ImageTiler.jar not found. Please run build.sh first.")
        return
    
    log_message("Step 1: Starting ImageTiler application...")
    
    # Start the application and capture console output
    try:
        # Run the Java application and capture its output
        process = subprocess.Popen(
            ["java", "-jar", "ImageTiler.jar"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        
        log_message("Application started with PID: " + str(process.pid))
        log_message("Console output monitoring started...")
        
        # Give some time for the application to initialize
        time.sleep(3)
        
        log_message("\nSTEP 2: To reproduce the issue, please follow these steps:")
        log_message("1. Click on '🎯 Printer Calibration' button in the Tools & Helpers section")
        log_message("2. The Measurement Helper dialog should open")
        log_message("3. Click '1. Print Reference Page' button")
        log_message("4. OR click 'Save to PDF' from the main interface with calibration image loaded")
        log_message("5. Observe: You should see blank pages or PDF failure")
        
        log_message("\nEXPECTED BEHAVIOR:")
        log_message("- The calibration image should be printed/saved properly")
        log_message("- You should see a rectangle with measurement guides")
        
        log_message("\nACTUAL BEHAVIOR (if bug exists):")
        log_message("- Blank pages are printed/saved")
        log_message("- PDF may fail to generate correctly")
        
        log_message("\nPress Ctrl+C to stop monitoring...")
        
        # Monitor the process output
        while process.poll() is None:
            time.sleep(1)
            
    except KeyboardInterrupt:
        log_message("\nMonitoring stopped by user")
        if process.poll() is None:
            process.terminate()
            log_message("Application terminated")
    except Exception as e:
        log_message(f"Error: {e}")

if __name__ == "__main__":
    main()
