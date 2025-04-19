
# Contract Finder Nexus App

A Kotlin-based Android application for searching contract details from CSV data.

## Features

- CSV file import with support for 400,000+ rows
- Fast search functionality by Contract or Contract Account
- CSV upload feature for data updates
- Compatible with Android 7.0 (API level 24) and above
- Efficient handling of large datasets

## CSV File Format

The application expects a CSV file with the following headers:
1. Contract
2. Contract Account
3. IBCName
4. Portfolio
5. CB Offer
6. Rebate Offer
7. PWO Scheme

## Building the App

### Prerequisites
- Android Studio (latest version recommended)
- JDK 8 or higher

### Steps to build

1. Clone the repository
2. Open the project in Android Studio
3. Wait for Gradle sync to complete
4. Click on "Build" → "Build Bundle(s) / APK(s)" → "Build APK(s)"
5. The APK will be generated in the `app/build/outputs/apk/debug/` directory

### Generate Signed APK

1. In Android Studio, click on "Build" → "Generate Signed Bundle / APK"
2. Select "APK"
3. Create or select your keystore file
4. Fill in the keystore password, key alias, and key password
5. Select the destination folder and finish

## Usage

1. Install the application on your Android device (Android 7.0+)
2. Open the app
3. Tap the "+" button to import a CSV file
4. Once imported, use the search bar to find contracts by Contract ID or Contract Account
5. Results will be displayed in a list

## Performance Considerations

The app is optimized to handle large CSV files (400,000+ rows) by:
- Using efficient parsing libraries
- Implementing background threading
- Using optimized data structures

## Web Demo Version

A web-based demo version is also available in this repository for testing purposes.
