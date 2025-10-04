# Workflow

Our workflow specifications are completely necessary for the effectiveness of a group project. This document will showcase which routines are in place to ensure a clean workspace.

## Branching strategy

### Main branch

- Stable, tested code only
- Every merge into main must be able to run

### Feature branches

- One branch per feature or task
- Maintaining one branch per task, and one task per branch, ensures readability of Git history

### Branch naming conventions

- The branches have precise descriptions of their task

## Task management

### GitHub 

- Issues are created for each task
- The branch names are in correspondence with issue names
- The project issue board is frequently updated to provide accessible overview for each member
- Branches are created and deleted through GitHub

### Delegation

- Tasks are usually assigned in weekly meetings
- Additional tasks are delegated in the Slack channel "Things-to-do"

## Commit practices

- The commit messages include a "Co-authored-by:" footer when coding in pairs
- The commit messages aim to precisely describe what the commit includes
- The commit message is written in imperative

## Pull Request / Code Review

- Open a pull request when a feature or fix is ready
- Require at least one team member review before merging
- Address feedback before merging
- Merge strategy; use merge commit

## Testing & Quality Assurance

- Manual testing when necessary
- Run tests locally before committing

## Continuous Integration

- Ensure builds pass before merging to main
- Handle build failures promptly and communicate issues to the team

## Coding Standards

- Follow the coding standards defined in [code-quality.md](./code-quality.md)

## Documentation Updates

- Update inline comments or other documentation when adding new features or changes
- Document decisions, workflow changes or relevant context in project documentation

## Workflow Updates

- Any team member can propose changes to the workflow
- Changes are discussed in weekly meetings and communicated to the entire team