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
This project uses Gradle instead of Maven. Since you are familiar with Maven (`mvn`), Gradle uses a different command line tool (`gradle`) but has equivalent goals (called "tasks").

To start the Spring Boot backend server, run the following command from the root of this repository:
```powershell
gradle bootRun
```
*(Note: If you get a command not recognized error for `gradle`, you will need to [install Gradle](https://gradle.org/install/) on your Windows machine first, and ensure it is also added to your system `PATH`).*

**Common Maven vs. Gradle Commands Quick Reference:**
* `mvn clean` ➔ `gradle clean`
* `mvn clean install` ➔ `gradle clean build` (compiles code, runs tests, and assembles the jar/zip)
* `mvn compile` ➔ `gradle classes`
* `mvn test` ➔ `gradle test`
* `mvn spring-boot:run` ➔ `gradle bootRun`
