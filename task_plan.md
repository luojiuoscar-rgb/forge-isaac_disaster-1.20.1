# Isaac Disaster Project Plan

## Status

No project-wide implementation plan is active. Feature work belongs in
`.planning/<task-id>/`.

## Project Rules

- Target Forge `1.20.1`, Forge `47.4.9`, and the repository's configured Java
  toolchain.
- Read this file, `findings.md`, and the tail of `progress.md` when recovering
  project-level context.
- For an active feature, read that feature's `.planning/<task-id>/task_plan.md`,
  `findings.md`, and `progress.md` first.
- Keep detailed design decisions, experiments, errors, and test output in the
  feature directory. Promote only durable cross-feature facts to root
  `findings.md`.
- Do not use root `progress.md` as a complete historical log.

## Planning Directory Convention

Each substantial feature or investigation gets:

```text
.planning/<task-id>/
├── task_plan.md
├── findings.md
└── progress.md
```

Completed task directories remain as historical implementation records. Current
source code takes precedence over any archived plan.

## Root Maintenance

- Keep this file limited to project-wide workflow and release constraints.
- Keep stable architecture and unresolved cross-feature issues in
  `findings.md`.
- Keep only recent project-level maintenance entries in `progress.md`.
