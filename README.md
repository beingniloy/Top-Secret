# Top Secret Vault

[![Build and Check](https://github.com/shohanurrahman2580/TopSecret/actions/workflows/build.yml/badge.svg)](https://github.com/shohanurrahman2580/TopSecret/actions/workflows/build.yml)
[![Latest Release](https://img.shields.io/github/v/release/shohanurrahman2580/TopSecret?include_prereleases&logo=github&color=blue)](https://github.com/shohanurrahman2580/TopSecret/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Platform: Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

**Top Secret** is a state-of-the-art, high-security vault application for Android. Built to protect your most sensitive passwords, secure notes, credit cards, identity documents, and TOTP authenticators, Top Secret operates with an absolute **zero-knowledge, offline-first privacy model**.

Your data is protected by military-grade AES-256-GCM encryption derived from your master password and shielded by system-level biometric authentication, ensuring that only you hold the keys to your digital life.

---

## 🔒 Security & Cryptographic Model

Top Secret employs a mathematically sound, industry-standard security architecture:

1. **Zero-Knowledge Architecture**: No user passwords, keys, or decrypted data are ever transmitted to any server. Everything is stored locally on-device.
2. **Key Derivation (PBKDF2)**: Your master password undergoes rigorous key derivation using PBKDF2 with HMAC-SHA256 to generate a cryptographically strong 256-bit encryption key.
3. **AES-GCM Encryption**: All entries (passwords, notes, documents) are encrypted using AES-256 in Galois/Counter Mode (GCM). Each item is encrypted with a unique, cryptographically secure random Initialization Vector (IV).
4. **Biometric Integration & Android KeyStore**: Biometric locks utilize standard Android `BiometricPrompt` linked with secure cryptographic keys housed inside the hardware-backed **Android KeyStore** (TEE/StrongBox).
5. **Inactivity Auto-Lock**: A configurable background watchdog automatically lock-clears the in-memory session key after a user-defined timeout of idle inactivity.

---

## ✨ Features

* **Secure Notes**: Encrypted rich-text notepad with custom tags and markdown import.
* **Biometric Lock**: Unlock notes and vault data effortlessly using fingerprint or face unlock.
* **Auto-Lock Timeout**: Selectable timer ranging from 1 to 60 minutes to auto-seal the database when idle.
* **Multi-Category Vault**: Dedicated modules for Passwords, Cards, Identities, TOTP keys, and Documents.
* **Offline-First Storage**: Powered by a secure local SQLite Room database with automatic destructive fallback options in the event of tampering.
* **Screen Security**: Complete prevention of screenshots/recents screen previewing when active.

---

## 📸 Screenshots

*(To display preview, add high-resolution interface images to the `assets` folder and link them below)*

<p align="center">
  <img src="assets/onboarding_preview.png" width="30%" alt="Onboarding Screen"/>
  <img src="assets/vault_preview.png" width="30%" alt="Vault Dashboard"/>
  <img src="assets/biometric_preview.png" width="30%" alt="Biometric Lock"/>
</p>

---

## 🏗 Architecture

Top Secret follows **Modern Android Development (MAD)** practices and **Clean Architecture** patterns:

```
├── app
│   ├── src
│   │   ├── main
│   │   │   ├── java/com/example
│   │   │   │   ├── data
│   │   │   │   │   ├── crypto      # PBKDF2, AES-GCM engine, Keystore
│   │   │   │   │   ├── db          # Room Database interfaces
│   │   │   │   │   ├── model       # Data schemas (VaultItem)
│   │   │   │   │   └── prefs       # Encrypted Datastore & preferences
│   │   │   │   ├── ui              # M3 Jetpack Compose UI
│   │   │   │   │   ├── components  # Layout sub-elements
│   │   │   │   │   └── theme       # Colors, Typography & Shapes
│   │   │   │   └── MainActivity.kt # Entry point (FragmentActivity)
```

* **UI Layer**: Built 100% in declarative **Jetpack Compose** following Material Design 3 guidelines.
* **State Management**: Reactive data streams using **StateFlow** within Jetpack ViewModels.
* **Persistence**: Secure SQLite encapsulation using the **Room Database** framework.

---

## 🛠 Building & Compilation

### Prerequisites
* JDK 17
* Android Studio (Koala or newer)
* Android SDK 24+ (Min SDK 24, Target SDK 36)

### Local Build Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/shohanurrahman2580/TopSecret.git
   cd TopSecret
   ```
2. Build the project using Gradle:
   ```bash
   ./gradlew assembleDebug
   ```
3. Run the unit and Robolectric test suite:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 🚀 GitHub CI/CD & Automated Release Pipeline

We have automated verification and releases using GitHub Actions.

### 1. Build & Lint Pipeline (`build.yml`)
Runs automatically on every `push` or `pull_request` to the main branches:
* Formats code checking using **ktlint** (`./gradlew ktlintCheck`)
* Analyzes codebase using **detekt** (`./gradlew detekt`)
* Checks compilation and runs Android unit tests (`./gradlew testDebugUnitTest`)
* Compiles and outputs diagnostic APKs

### 2. CD Release Pipeline (`release.yml`)
Triggers automatically when a new Git tag is pushed (e.g., `v1.0.0`):
1. **Builds & Signs**: Compiles the release-optimized APK and AAB.
2. **Generates Checksums**: Creates a cryptographic SHA-256 manifest of the build files.
3. **Drafts Release**: Automatically populates release notes using commit headers and publishes a GitHub Release containing:
   * `TopSecret-vX.X.X.apk` (Signed Release APK)
   * `TopSecret-vX.X.X.aab` (Signed Google Play Bundle)
   * `SHA256SUMS.txt` (Sha256 verify checksums)
   * `mapping.txt` (R8/ProGuard de-obfuscation map)

---

## 🔑 Required GitHub Secrets Configuration

To run the release pipeline successfully, you must configure the following **Repository Secrets** in your GitHub repository (`Settings -> Secrets and variables -> Actions`):

| Secret Name | Description | Example / Instructions |
| --- | --- | --- |
| `SIGNING_KEYSTORE_BASE64` | The entire Android `.jks` or `.keystore` signing file converted to a base64 string. | Run `base64 -w 0 my-key.jks` and paste the output. |
| `SIGNING_STORE_PASSWORD` | The password configured to access the keystore file. | `myKeystorePassword123` |
| `SIGNING_KEY_ALIAS` | The alias identifier of the key inside the keystore. | `upload` / `release` |
| `SIGNING_KEY_PASSWORD` | The password configured specifically for the key alias. | `myKeyPassword123` |

---

## 📜 Privacy Policy
Top Secret does not collect, record, log, share, or transmit any user behavior data, files, credentials, passwords, metadata, or analytics. Your private keys never leave your physical handset.

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
