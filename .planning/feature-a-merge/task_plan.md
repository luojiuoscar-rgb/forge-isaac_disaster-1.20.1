# Feature A merge

Goal: merge feature/feature-a into master without committing or pushing.

- [x] Verify clean worktrees and fetch remote input refs.
- [x] Start merge without committing.
- [x] Resolve conflicts preserving both branches' intended behavior; ask for semantic ambiguities.
- [x] Compile main, JUnit and GameTest sources; run JUnit and data generation as needed.
- [x] Validate staged patch and hand off pending merge to user.

Inputs: master a8555626d31d88f97aac662e18787c557ee64727; feature A 0f78ea57fade728e4e28bc1a8cdbf11756f00fc3. Remote refs matched after fetch.
