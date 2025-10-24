#!/bin/bash

# Script to transfer Excel files from Mac to Android emulator
# Usage: ./transfer_excel.sh /path/to/your/file.xlsx

if [ $# -eq 0 ]; then
    echo "Usage: ./transfer_excel.sh /path/to/your/file.xlsx"
    echo "Example: ./transfer_excel.sh ~/Downloads/portfolio.xlsx"
    exit 1
fi

FILE_PATH="$1"

# Check if file exists
if [ ! -f "$FILE_PATH" ]; then
    echo "Error: File '$FILE_PATH' does not exist"
    exit 1
fi

# Check if file is Excel
if [[ ! "$FILE_PATH" =~ \.(xlsx|xls)$ ]]; then
    echo "Error: File must be an Excel file (.xlsx or .xls)"
    exit 1
fi

# Get filename
FILENAME=$(basename "$FILE_PATH")

echo "📁 Transferring $FILENAME to Android emulator..."

# Transfer file to emulator's Download folder
adb push "$FILE_PATH" /sdcard/Download/

if [ $? -eq 0 ]; then
    echo "✅ Successfully transferred $FILENAME to emulator"
    echo "📱 File is now available in the emulator's Downloads folder"
    echo "🎯 You can now use the 'Choose File' button in the app to select it"
else
    echo "❌ Failed to transfer file. Make sure:"
    echo "   - Android emulator is running"
    echo "   - ADB is properly configured"
    echo "   - File path is correct"
fi
