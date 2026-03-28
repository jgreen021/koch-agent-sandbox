---
trigger: always_on
---

# Rule: OpenSpec Task Structure
- Whenever the user issues an `/opsx-propose` command, you MUST initialize the `tasks.md` file using the template found in `/openspec/templates/openspec-task-template.md.`.
- **Phase 1 (Discovery):** Must include MCP tool calls to verify existing Azure SQL schema or code state.
- **Phase 3 (TDD):** Must explicitly list failing test cases before implementation tasks.
- **Constraint:** Reject any task list that does not follow the phased execution model.