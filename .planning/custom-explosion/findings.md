# Review follow-up findings

- ClientPacketHandlers.handleExplosionKnockback applies only an additive impulse to the local player's current velocity; server feedback sends sound and particles separately, once.
- The nested AttributedExplosion preserves DamageSource.getEntity() as the living owner for vanilla block processing, including chain-primed TNT. The existing arbitraryDirectSourceRetainsOwnerForChainedTnt GameTest covers this behavior.
- ExplosionDamage has one production consumer, CustomExplosion; fold its ordinary and Giga formulas into that executor without coupling it to bomb entities. Preserve numerical regression coverage.
- IsaacBombExplosion has no production callers; only one redundant compatibility GameTest invokes it. Remove the adapter and that test path. IsaacBomb.explodeRegular and getExplosionParameters can then be private implementation details or folded into explode.
- Death locust currently applies two 2D hits using onContactHit; user requests one 4D hit via getContactDamage instead. Test the number of attacks as well as total damage and owner attribution.
- Invalid explosion centers currently throw before events. The correction must return false, log coordinates, and preserve the no-side-effects ordering.
- An unrelated current-input mismatch was discovered by the red run: Pestilence has 50-tick poison, while its test still assumes the former 59-tick duration. Real pulses [11,31] follow remaining-duration multiples of20; preserve the user's current 50-tick production setting and update the expectation.
