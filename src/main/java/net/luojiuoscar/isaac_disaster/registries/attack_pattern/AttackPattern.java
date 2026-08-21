package net.luojiuoscar.isaac_disaster.registries.attack_pattern;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;

import java.util.List;

public interface AttackPattern {
    List<AttackContext> generate(AttackPatternContext context);
}
