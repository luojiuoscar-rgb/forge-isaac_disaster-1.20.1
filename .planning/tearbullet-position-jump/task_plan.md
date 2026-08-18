# TearBullet Position Jump Diagnosis

## Goal
Identify the most likely cause of the one-time post-launch position jump in `TearBullet`, especially the interaction between server movement, trajectory application, and Forge client synchronization. This task is diagnostic only; do not modify mod source.

## Phases
- [completed] Inventory TearBullet movement, trajectory, spawn, and sync paths.
- [completed] Trace timing and coordinate sources for a one-time discontinuity.
- [completed] Cross-check current workspace diff and review rules for relevant files.
- [completed] Record evidence, rank hypotheses, and report the most likely reason.

## Constraints
- Forge 1.20.1 / 47.4.9.
- Preserve unrelated user changes.
- Record findings after every two search/read operations.

## Errors Encountered
| Error | Attempt | Resolution |
|---|---:|---|
| `python` and `py` are unavailable on PATH | 1 | Use direct PowerShell inspection; locate bundled runtime only if catch-up is required. |
| `ocr.ps1` is blocked by PowerShell execution policy | 1 | Use `ocr.cmd`/absolute executable discovery before retrying. |
| Installed OCR delegate CLI rejects documented JSON flags | 1 | Inspect its help; use default Markdown preview and complete rule coverage with `ocr.cmd`. |
