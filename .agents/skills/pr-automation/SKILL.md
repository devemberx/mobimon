---
name: pr-automation
description: Automates the creation of GitHub issues and pull requests while strictly adhering to the project's validate_pr.py formatting rules.
---

# PR and Issue Automation Skill

This skill ensures that all pull requests and issues created for this project strictly pass the alidate_pr.py CI checks and conform to the project's formatting standards.

## Workflow

When the user asks to "upload", "make a PR", or "create an issue", follow these steps:

1. **Pre-check (Code Quality)**:
   - Run local linting checks (e.g., ./gradlew ktlintFormat or ./gradlew ktlintCheck) to ensure no code style violations exist.
   - Run git status and git diff to understand the changes.

2. **Commit Changes**:
   - Ensure you are on an appropriate branch (e.g., eat/..., ix/...).
   - Stage and commit the changes if not already committed.

3. **Create Issue (Optional but Recommended)**:
   - If an issue does not already exist, create one using gh issue create.
   - The issue body can be a slightly simplified version of the PR body, but must explain the context clearly in English.

4. **Construct the PR Title**:
   - Must match the regex 	ype(scope): summary
   - Must be strictly **50 characters or less**.
   - Must **not** end with a period (.).
   - Must contain only printable ASCII characters.
   - Example: eat(db): add missing entity fields

5. **Construct the PR Body**:
   - **MUST NOT contain any Non-ASCII characters** (no Korean, no hidden BOM characters). Use plain English. Emoji and typographic punctuation are the only exceptions.
   - You MUST include exactly these five headings in this exact order:
     1. ## Summary
     2. ## Changes
     3. ## Verification
     4. ## Demo
     5. ## Shortcuts and Risks
   - **CRITICAL RULES for ## Changes**:
     - Must contain **exactly two** single-line bullets starting with - .
     - First bullet: Why the change is needed.
     - Second bullet: What was changed.
     - **No other text is allowed** in this section.
     - Each bullet must be at most 120 characters long (including the - ).
   - **CRITICAL RULES for Missing Information**:
     - Do not use standalone placeholders like None, N/A, or Not run except for Shortcuts and Risks which allows None.
     - If no demo is possible, write: N/A: <reason> under ## Demo.
     - If no verification was run, write: Not run: <reason> under ## Verification.

6. **Create the Pull Request**:
   - Write the constructed PR body to a text file using ASCII encoding to prevent BOM issues (e.g., using [IO.File]::WriteAllText("pr_body.txt", , [System.Text.Encoding]::ASCII) in PowerShell).
   - Run gh pr create --title "<Title>" --body-file pr_body.txt.