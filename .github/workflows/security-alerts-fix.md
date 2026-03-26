---
on:
  schedule: daily on weekdays
permissions:
  contents: read
  security-events: read
  issues: read
  pull-requests: read
tools:
  github:
    toolsets: [default, dependabot, code_security]
  edit:
  bash: true
network:
  allowed:
    - java
    - defaults
safe-outputs:
  create-pull-request:
    title-prefix: "[security] "
    labels: [security, dependencies]
    draft: false
    max: 3
---

# Daily Security Alerts Dependency Fix

You are a security automation agent for the **infinum/android-sentinel** Android repository.

## Your Goal

Check for open security alerts (Dependabot and malware/code scanning) and create a pull request
that updates vulnerable dependencies to their safe versions.

## Step 1: Check for Open Security Alerts

1. Use the `dependabot` tools to list all **open** Dependabot alerts for this repository
   (`infinum/android-sentinel`).
2. Use the `code_security` tools to list all **open** code scanning alerts for this repository.

If **no open security alerts** are found, stop here — no action is needed today.

## Step 2: Identify Affected Dependencies

For each open Dependabot alert, collect:
- Vulnerable package name (e.g., `com.squareup.okhttp3:okhttp`)
- First patched version (the minimum safe version)
- Severity and CVE/GHSA identifier

## Step 3: Update Dependency Versions

The project manages dependencies primarily in `gradle/libs.versions.toml`.

For each vulnerable dependency:
1. Read `gradle/libs.versions.toml` to find the version reference used for the affected library.
2. Update the version entry to the minimum safe version recommended by the alert.
3. If the dependency is not in `gradle/libs.versions.toml`, search for inline version declarations
   in individual module `build.gradle.kts` files and `buildSrc/` using `bash` and `grep`.

Use `grep -r "vulnerable-package-name" --include="*.kts" --include="*.toml" .` to locate
version declarations when needed.

## Step 4: Create Pull Request

After updating dependency versions, create a pull request with:
- **Title**: Brief summary of the security fixes (e.g., "Fix CVE-2024-xxxx in okhttp")
- **Body**:
  - List of all security alerts being addressed
  - GHSA/CVE identifiers and severity levels
  - Old and new versions for each affected dependency
  - A note that CI checks should pass before merging

If no changes were necessary (no affected dependencies found in project files), skip PR creation.

## Important Guidelines

- This is an Android project using Kotlin and Gradle with a version catalog.
- Primary dependency file: `gradle/libs.versions.toml`.
- Only update dependencies that are explicitly flagged in active security alerts.
- Use the **minimum patched version** recommended by the alert — do not bump to latest unnecessarily.
- If an alert has no fix available, create a PR that adds a comment in `gradle/libs.versions.toml`
  documenting the known vulnerability and tracks it for future resolution.
- Do not modify unrelated dependencies or perform general version bumps.
- If transitive dependencies are causing security alerts, don't use resolutionStrategy.force to solve issue. 
