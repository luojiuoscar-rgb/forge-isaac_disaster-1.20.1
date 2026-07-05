package net.luojiuoscar.isaac_disaster.registries.attack_type;

/**
 * Marker interface for attack types that choose another attack type as their payload.
 *
 * <p>Examples include high-level firing patterns such as Neptunus or Cursed Eye. They still
 * participate in normal attack selection, but their implementation may delegate the actual shot
 * creation to a lower-priority attack type.</p>
 */
public interface DelegatingAttackType {
}
