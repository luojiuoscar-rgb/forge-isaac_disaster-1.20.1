package net.luojiuoscar.isaac_disaster.registries.attack_type.tags;

/**
 * An attack that competes for primary selection and delegates shots to the selected base attack.
 * Its own performAttack implementation does not create projectiles.
 */
public interface DelegatingAttackType {
}
