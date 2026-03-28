---
trigger: always_on
---

# Agent Constraints
# This rule was put into place to curb a race condition between the AI agent and the IDE's file system watcher
- NEVER perform "drive-by" refactoring (e.g., unused imports) unless explicitly in the task scope.
- Consolidate all planned changes into a single "Plan" block before executing any file writes.
- Do not use "stream-of-consciousness" output; wait for full reasoning completion before applying changes.

# Agent Stability Rules

## Execution constraints
- **No Interjections:** Never use "Wait," "Actually," or "I'll just do it now." 
- **Sequential Writes:** Complete the entire logic plan before writing to the file system. 
- **Lock-Awareness:** If the IDE is re-indexing or compiling, wait for the process to finish before attempting a second file modification.
- **Scope Locking:** Do not perform secondary tasks (like fixing unused imports in `AssetSensorReadingControllerTest.java`) unless they are the primary focus of the current task.