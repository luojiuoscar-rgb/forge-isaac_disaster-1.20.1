# Findings: Directional Isaac Flight

- The project is Forge 1.20.1 / Forge 47.4.9 / Java 17.
- Old flight ownership lives in `PlayerStatModifier`; `FlightHelper` grants/revokes vanilla `mayfly` and `ServerTickEvent.updateFly()` refreshes it every four ticks.
- `TranscendenceEffect` directly invokes the old grant helper each tick and must become a passive source only.
- Player capabilities are attached and copied in `ForgeEvents`; networking uses `ModMessages` and explicit packet directions.
- Client input and key handling belong in `ClientForgeEvents`; key registration belongs in `ClientModEvents`.
- The common config is `Config`; the in-game catalog must expose the new numeric settings if they are intended to remain user-configurable.
- Client-only Minecraft/render classes must remain outside common packet/controller class loading paths.
- `PlayerTickEvent.END` runs after the normal player travel step, so writing `deltaMovement` there affects the next tick but does not bypass the next tick's ordinary gravity. The custom thrust state now owns `noGravity` transiently to make the shared look-directed velocity authoritative without a Mixin.
- `TranscendenceEffect` is a source marker rather than a direct mayfly grant. `FlightHelper.hasIsaacFlightSource` accepts it even when `FLY_TIME` is zero, and both server resource consumption paths skip it while the effect is active.
- The previous `mayfly` grant implicitly made players immune to natural fall damage. The custom controller must preserve that independently of thrusting by cancelling `LivingFallEvent` whenever an Isaac flight source exists.
- The speed multiplier is wired correctly as `min(MOVEMENT_SPEED * multiplier, absoluteCap)`. The original `0.3` cap and 0.2 steering made large multiplier changes appear weak; the public cap is now `1.0`, while steering is intentionally internal.
