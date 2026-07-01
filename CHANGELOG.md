# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-07-01

### Added
- **Web Authentication API Biometrics**: Replaced mock authentication with real system-level biometrics utilizing standard Android `BiometricPrompt` and Android KeyStore.
- **Auto Lock Inactivity Timer**: Configurable automatic security lock triggered after user-defined periods of inactivity.
- **Biometric App Lock Settings**: Users can toggle biometric protection and set fine-grained auto-lock intervals from the configuration menu.
- **GitHub CI/CD Automation**: Production pipelines for code validation (`detekt`, `ktlint`, test checking) and tagged automated signing/releasing of assets.

### Changed
- Migrated legacy `ComponentActivity` entrypoint to `FragmentActivity` for deep system-level Biometrics support.
- Fully removed pre-loaded demonstration data to guarantee clean database installation for absolute privacy.
- Relocated and cleaned application configuration properties using secure, modern Jetpack Compose states.
