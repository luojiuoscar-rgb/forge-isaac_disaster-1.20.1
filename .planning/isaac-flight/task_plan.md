# Task Plan: Directional Isaac Flight

## Goal
Replace Isaac Disaster's vanilla `mayfly` grant with a server-authoritative, look-directed thrust controller while preserving client responsiveness and legacy-save compatibility.

## Phases

### Phase 1: Inspect Existing Ownership Boundaries
- [x] Locate flight flags, fly-time consumption, capability lifecycle, packet registration, key mappings, and config registration.
- **Status:** complete

### Phase 2: Flight State And Pure Rules
- [x] Add tests for speed caps, steering, and gravity compensation.
- [x] Add `PlayerIsaacFlight` capability and provider.
- [x] Implement shared flight input/rules/controller types.
- **Status:** complete

### Phase 3: Server And Network Integration
- [x] Register packets and capability lifecycle.
- [x] Add server tick validation, movement, timeout, consumption, and legacy migration.
- [x] Remove old mayfly refresh responsibilities.
- **Status:** complete

### Phase 4: Client Controls And Rendering
- [x] Add airborne re-press input semantics and heartbeat.
- [x] Add persistent enable/disable key and state confirmation.
- [x] Add forced-pose ownership and visual body tilt without real Elytra state.
- **Status:** complete

### Phase 5: Verification
- [x] Run unit tests and `compileJava`.
- [x] Run `git diff --check` and static forbidden-write searches.
- [x] Review the final flight-specific diff and ownership paths.
- **Status:** complete

## Decisions
- No flight Mixin and no vanilla fall-flying flag.
- `FLY_TIME` plus Transcendence remain the source predicate.
- Client predicts with the same formula; server remains authoritative.
- Legacy `mayfly` ownership fields remain for one migration cycle.
- Custom thrust temporarily owns `noGravity` so ordinary player travel cannot reapply gravity between controller updates; external no-gravity ownership is preserved.

## Errors Encountered
| Error | Attempt | Resolution |
|---|---:|---|
| `python` is unavailable on PATH for session catch-up | 1 | Use the known bundled Python path if catch-up is needed again. |
