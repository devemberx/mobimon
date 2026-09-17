---
name: pr-description
description: Generates exactly two English lines starting directly with '-' when the user asks "description 적어줘", "description 써줘", or requests PR/commit descriptions.
---

# PR Description Generator Skill

This skill formats PR and commit descriptions strictly according to project conventions whenever the user requests a description (e.g., "description 적어줘", "description 써줘", "PR description", etc.).

## Trigger Phrases
- "description 적어줘"
- "description 써줘"
- "PR description 작성해줘"
- "description 알려줘"
- Any request asking for PR or commit descriptions

## Output Rules (STRICT)
1. **The output MUST start immediately with `- ` as the very first character.**
2. **DO NOT include any greeting, preamble, introductory text, markdown code blocks (no ```), or commentary.**
3. **DO NOT include any trailing explanation, notes, or sign-off.**
4. The response consists of **EXACTLY TWO LINES**:
   - Line 1 starts with `- ` and explains **why the change is needed** (max 120 characters total, ASCII only).
   - Line 2 starts with `- ` and explains **what was changed** (max 120 characters total, ASCII only).
5. Both lines must be single-line English bullets containing only printable ASCII characters.

## Required Output Format
- <Why the change is needed: single line, max 120 chars including '- '>
- <What was changed: single line, max 120 chars including '- '>
