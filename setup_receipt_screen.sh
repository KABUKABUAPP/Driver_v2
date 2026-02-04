#!/bin/bash

# Receipt Screen Setup Script
# This script helps complete the setup after creating the receipt screen

echo "==================================="
echo "Receipt Screen Setup"
echo "==================================="
echo ""

# Navigate to project directory
cd "$(dirname "$0")"

echo "Step 1: Stopping existing Gradle daemons..."
./gradlew --stop
echo "✓ Gradle daemons stopped"
echo ""

echo "Step 2: Cleaning project..."
./gradlew clean
echo "✓ Project cleaned"
echo ""

echo "Step 3: Syncing dependencies (this may take a while)..."
./gradlew dependencies --configuration implementation | grep capturable
echo ""

echo "Step 4: Building project..."
./gradlew :app:assembleDebug --console=plain
echo ""

if [ $? -eq 0 ]; then
    echo "==================================="
    echo "✓ Setup completed successfully!"
    echo "==================================="
    echo ""
    echo "Next steps:"
    echo "1. Open the project in Android Studio"
    echo "2. Run the app"
    echo "3. Navigate to Trips → Select a trip → Click 'View Receipt'"
    echo "4. Test the share functionality"
    echo ""
else
    echo "==================================="
    echo "✗ Build failed"
    echo "==================================="
    echo ""
    echo "Please:"
    echo "1. Open the project in Android Studio"
    echo "2. Click 'File' → 'Sync Project with Gradle Files'"
    echo "3. Wait for sync to complete"
    echo "4. Try building again"
    echo ""
fi
