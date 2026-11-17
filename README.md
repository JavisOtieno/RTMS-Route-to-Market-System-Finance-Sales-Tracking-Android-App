# RTMS – Route to Market System  
## Finance Sales Tracking Android App

A native Android application for field sales teams to track sales, visits, route plans, check-ins, and financial performance in a Route-to-Market (RTM) system.

[![Android](https://img.shields.io/badge/Android-Java-3DDC84?style=flat&logo=android)](https://developer.android.com)
[![minSdk](https://img.shields.io/badge/minSdk-26-blue)](https://developer.android.com)

## Overview

**RTMS Finance Sales Tracking** is a comprehensive Sales Force Automation (SFA) mobile app designed for finance-focused route-to-market operations. It enables sales representatives to:

- Clock in/out and check in to outlets
- Record and manage sales
- Plan and follow routes
- Track visits and follow-ups
- Capture competitive analysis and feedback
- View customers, products, and performance

The app works with the related **RTMS Sales Tracking Dashboard** (Laravel) and the retail version of the Android app.

**Package:** `com.scg.rtmssf`

## Key Features

- **Attendance** – Clock In / Clock Out
- **Outlet Check-in / Check-out** – Location-based visits
- **Sales Management** – Create, view, complete, and cancel sales
- **Route Plans** – View and map route plans
- **Visits Tracking** – Individual visit details and history
- **Follow-ups** – Create, edit, and manage follow-ups
- **Customers / Outlets** – List, create, and view outlets
- **Products** – Product catalog and individual product details
- **Competitive Analysis** – Capture and review competitor data
- **Feedback** – Collect and view field feedback
- **POS Audits**
- **Maps & Location** – Google Maps integration for routes and check-ins
- **Dashboard & Activities**
- **Token Refresh** – Secure authentication with refresh tokens
- **Charts** – Performance visualization (MPAndroidChart)
- **Secure Storage** – Encrypted preferences

## Tech Stack

| Component              | Technology                          |
|------------------------|-------------------------------------|
| Language               | Java                                |
| UI                     | AndroidX + Material + ViewBinding   |
| Networking             | Retrofit 2 + OkHttp                 |
| Maps & Location        | Google Maps + Play Services Location|
| Image Loading          | Picasso                             |
| Charts                 | MPAndroidChart                      |
| Background Tasks       | WorkManager                         |
| Security               | AndroidX Security Crypto, RootBeer, Play Integrity |
| Analytics              | Firebase Analytics + Crashlytics    |
| Min SDK                | 26                                  |
| Target / Compile SDK   | 34                                  |

## Prerequisites

- Android Studio (Ladybug or newer recommended)
- JDK 8+
- Android SDK 34
- Google Maps API key
- Firebase project (`google-services.json`)
- Git

## Getting Started

```bash
# Clone the repository
git clone https://github.com/JavisOtieno/RTMS-Route-to-Market-System-Finance-Sales-Tracking-Android-App.git
cd RTMS-Route-to-Market-System-Finance-Sales-Tracking-Android-App

# Open in Android Studio
# File → Open → select the project folder

# Add google-services.json to the app/ directory
# Configure Maps API key if required

# Sync Gradle and run
# Build → Make Project
# Run on emulator or physical device
