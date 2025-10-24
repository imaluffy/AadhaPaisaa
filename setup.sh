#!/bin/bash

# AadhaPaisa KMM Portfolio Management App Setup Script

echo "🚀 Setting up AadhaPaisa KMM Portfolio Management App..."

# Check if Android Studio is installed
if ! command -v android &> /dev/null; then
    echo "⚠️  Android Studio not found. Please install Android Studio first."
    exit 1
fi

# Check if Xcode is installed (macOS only)
if [[ "$OSTYPE" == "darwin"* ]]; then
    if ! command -v xcodebuild &> /dev/null; then
        echo "⚠️  Xcode not found. Please install Xcode for iOS development."
    fi
fi

# Create local.properties if it doesn't exist
if [ ! -f "local.properties" ]; then
    echo "📝 Creating local.properties from template..."
    cp local.properties.template local.properties
    echo "✅ Created local.properties. Please edit it with your API keys."
fi

# Make gradlew executable
chmod +x gradlew

# Clean and build
echo "🧹 Cleaning project..."
./gradlew clean

echo "🔨 Building project..."
./gradlew build

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "📱 To run the app:"
    echo "   Android: ./gradlew :androidApp:installDebug"
    echo "   iOS: Open iosApp/iosApp.xcodeproj in Xcode"
    echo ""
    echo "🔧 Next steps:"
    echo "   1. Edit local.properties with your API keys"
    echo "   2. Open project in Android Studio"
    echo "   3. Run on Android device/emulator"
    echo "   4. For iOS: Open iosApp/iosApp.xcodeproj in Xcode"
    echo ""
    echo "🎉 Setup complete! Happy coding!"
else
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi




