# Findings

## Current Contract

- `SimpleTrigger` stores `IExecutableEffect` and dispatches `apply(context)`.
- `IAbilityEffect` adds `applyEffect(context): boolean`, but its `apply()`
  failure branch remains a TODO; `IExecutableEffect` has no result contract.
- Existing trigger type placeholder is misspelled `ModTriggerTypes.EMTPY`.

## Annotation Rule

- Add one required trigger type only for effects with one unambiguous trigger
  owner. Multi-trigger effects must remain compatible with all their existing
  registrations.

## Applied Overrides

- `LOOT`: `TransformLootTypesToAnother`, `ChestLootTrinket`, `PennyTrinket`,
  `ContractFromBelow`, `DaemonsTail`, `MomsKey`, `PetrifiedPoop`, and `SackHead`.
- `ON_HURT`: `Callus`, `ExplosionImmune`, `ExplosionRegeneration`,
  `FragileHeartActive`, and `MidasTouch`.
- `ON_HURT_NEGATIVE`: `EternalHeartPunish`, `HolyShieldActive`, and
  `NecronmiconShieldActive`.
- `BULLET_HIT_BLOCK`: `BulletBounceOnBlock`.
- `BULLET_HIT_ENTITY_AFTER`: `BulletBounceOnEntity`.
- `BULLET_HIT_ENTITY_BEFORE`: `LaserPlusBrimstone`.
- `HIT_ENTITY_RESTRICTED`: `RandomHarmfulPotion`.
- `ATTACK_PLAN`: `FiringModifierAttackPlan` and `TheWizAttackPlan`.
- `GET_SHOT_DELAY`: `FiringModifierShotDelay`.
- `DEATH`: `ReviveExecutableEffect` and its concrete revive effects.

## Intentionally Unchanged

- `Ipecac`, `IronBar`, `MomsEyeshadow`, `MomsPerfume`, `TheCommonCold`, and
  `Volt45` are registered for more than one trigger type and therefore cannot
  accurately expose one required type.
- `DullRazor` and `LokisHorns` inspect the event only for optional branching or
  recursion protection; that does not make one trigger type a requirement.
