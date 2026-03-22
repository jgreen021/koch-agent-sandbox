# koch-agent-sandbox

This is a sandbox repository created by the Antigravity Agent for learning and testing GitHub integrations!

## Initial Environment Setup

### 1. Node.js and npm Configuration
If you find that commands like `node -v` work in PowerShell but `npm -v` does not, or if **neither** command is recognized inside of Antigravity:
* **The Issue:** Your system's `PATH` environment variable is likely missing the directory where `npm` is installed, or the Antigravity environment was launched before Node.js was added to your PATH.
* **The Fix:** 
  1. Download and reinstall Node.js using the official Windows installer from [nodejs.org](https://nodejs.org/). 
  2. During installation, ensure the option **"Add to PATH"** is checked (this is usually selected by default).
  3. **Crucially:** Restart Antigravity and any open PowerShell windows entirely. Environment variables are loaded on startup, so the IDE needs to be restarted to recognize the new path to both `node` and `npm`.

### 2. Starting the Server (Gradle for Maven Users)
This project uses Gradle instead of Maven. Since you are familiar with Maven (`mvn`), Gradle uses a similar concept but uses the provided **Gradle Wrapper** (`gradlew`) to ensure everyone uses the exact same Gradle version without needing to install it manually.

To start the Spring Boot backend server, run the following command from the root of this repository in your PowerShell terminal:
```powershell
.\gradlew bootRun
```
*(Note: If you run into issues, make sure your JDK is configured correctly and your JAVA_HOME environment variable is pointing to the right JDK!)*

**Common Maven vs. Gradle Commands Quick Reference:**
* `mvn clean` ➔ `.\gradlew clean`
* `mvn clean install` ➔ `.\gradlew clean build` (compiles code, runs tests, and assembles the jar/zip)
* `mvn compile` ➔ `.\gradlew classes`
* `mvn test` ➔ `.\gradlew test`
* `mvn spring-boot:run` ➔ `.\gradlew bootRun`
