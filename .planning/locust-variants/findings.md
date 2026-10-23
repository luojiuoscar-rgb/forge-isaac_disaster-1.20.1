# Findings

- AbstractFlyFamiliarEntity already exposes body color, contact damage, and a once-only post-consumption contact hook. All subclasses automatically share formation and targeting via FlyOwnerFrame.
- Familiar damage bypasses hurt cooldown through the existing damage-type tag, allowing Death's two separate 2D hits without editing invulnerability timers.
- DeadlyPoisonEffect quantizes per-pulse damage to 1 + 0.5 * amplifier, amplifier 0..255, and ticks whenever remaining duration is divisible by 20. Runtime test proved that applying 40 ticks immediately after contact loses its first pulse to vanilla hurt cooldown (only tick 21 damages). Locust therefore uses 59 ticks: exactly two attempts on ticks 20 and 40, snapshotting D from attemptedDamage / 2.
- Four new familiar descriptors should share the existing FLIES group (64 total) with blue flies. White locust has 2D per individual; generating 1..4 belongs to its future source item, outside this change.
- Wiki states Famine slows but does not specify a precise duration. Use an explicit mod adaptation, Slowness II for 40 ticks, and report it as such.
- Source references: https://bindingofisaacrebirth.wiki.gg/wiki/Locusts ; https://bindingofisaacrebirth.wiki.gg/wiki/Blue_Spiders ; https://isaacguru.com/wiki/isaac/t114
- IsaacBombExplosion currently requires an IsaacBomb. BombData.NORMAL provides centerDamage 60, damageRadius 4; inspect falloff, ownership, item callbacks, hurt cooldown and source compatibility before recommending War integration.
