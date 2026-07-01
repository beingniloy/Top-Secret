# Contributing to Top Secret

Thank you for your interest in contributing to Top Secret! We welcome community participation to make this vault even more secure and robust.

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

## Getting Started

1. **Fork the Repository**: Create your own copy of the repository.
2. **Clone Locally**: Clone your fork to your local development environment.
3. **Set Up SDK**: Open in Android Studio with JDK 17+ and compile.
4. **Create a Branch**: Always create a feature branch named like `feature/your-feature-name` or `bugfix/issue-description`.

## Code Guidelines

We use **ktlint** and **detekt** to ensure high quality and consistency of code style.

* **Format Check**: Run `./gradlew ktlintCheck` before committing.
* **Static Analysis**: Run `./gradlew detekt` to verify no complexity rule breaches occur.
* **Unit Tests**: Ensure all unit and screenshot tests pass with `./gradlew testDebugUnitTest`.

## Submitting Pull Requests

1. Commit your changes with descriptive messages.
2. Push your branch to GitHub.
3. Open a Pull Request targeting the `main` branch.
4. Fill out the PR template completely.
5. Ensure CI checks pass successfully before asking for reviews.
