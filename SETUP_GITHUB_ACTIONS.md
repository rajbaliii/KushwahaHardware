# GitHub Actions Setup Guide

This guide will help you set up automatic APK builds using GitHub Actions (FREE).

---

## Step 1: Create a GitHub Account (if you don't have one)

1. Go to https://github.com
2. Click "Sign up" and create a free account
3. Verify your email

---

## Step 2: Create a New Repository

1. Click the **+** icon in top right → **New repository**
2. Repository name: `KushwahaHardware`
3. Description: `Hardware store management app`
4. Make it **Public** (free) or **Private** (also free now!)
5. Click **Create repository**

---

## Step 3: Upload Your Code

### Option A: Using GitHub Website (Easiest)

1. On your new repository page, click **"uploading an existing file"**
2. Drag and drop ALL files from the KushwahaHardware folder
3. Click **Commit changes**

### Option B: Using Git Command Line

```bash
# Navigate to your project folder
cd KushwahaHardware

# Initialize git
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit"

# Add your GitHub repository (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/KushwahaHardware.git

# Push
git branch -M main
git push -u origin main
```

---

## Step 4: Add Gradle Wrapper (IMPORTANT)

The GitHub Actions workflow needs the Gradle wrapper. Run these commands locally:

### On Windows:
```cmd
cd KushwahaHardware
gradlew wrapper --gradle-version 8.4
```

### On Mac/Linux:
```bash
cd KushwahaHardware
./gradlew wrapper --gradle-version 8.4
```

**OR** if you have Android Studio:
1. Open the project in Android Studio
2. Open Terminal (bottom panel)
3. Run: `./gradlew wrapper --gradle-version 8.4`

Then upload the generated files:
- `gradlew` (already included)
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties` (already included)

---

## Step 5: Trigger the Build

Once your code is uploaded with the Gradle wrapper:

1. Go to your GitHub repository
2. Click **Actions** tab at the top
3. You'll see the workflow "Build Android APK"
4. Click **"Run workflow"** → **Run workflow**

---

## Step 6: Download Your APK

After the build completes (takes 5-10 minutes):

1. Go to **Actions** tab
2. Click on the latest workflow run
3. Scroll down to **Artifacts** section
4. Download:
   - `KushwahaHardware-Debug-APK` - Debug version (recommended for testing)
   - `KushwahaHardware-Release-APK` - Release version (unsigned)

---

## Step 7: Install on Your Phone

1. Download the APK file to your computer
2. Transfer to your phone (USB, email, WhatsApp, Google Drive)
3. On your phone, tap the APK file
4. Allow "Install from unknown sources" when prompted
5. Install and enjoy!

---

## Automatic Builds

Every time you push new code to GitHub, the APK will be built automatically!

You can also manually trigger builds anytime from the Actions tab.

---

## Troubleshooting

### Build Fails?

1. Check the build logs in Actions tab
2. Make sure all files are uploaded correctly
3. Ensure Gradle wrapper files are present

### Can't Install APK?

1. Enable "Install from unknown sources" in Settings > Security
2. Make sure you downloaded the debug APK (not release)

---

## Video Tutorial

If you prefer video: https://www.youtube.com/watch?v=0ncAL2T3C00
(Search: "GitHub Actions Android Build")

---

**Questions?** The GitHub Actions workflow file is already included in `.github/workflows/build.yml`