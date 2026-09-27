package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.*;
import java.util.List;

/** Real attack definitions usable without bootstrapping Forge registries. */
public final class TestAttackTypes {
    private TestAttackTypes() { }
    public static final AttackType BULLET = new BulletAttack(0);
    public static final AttackType C_SECTION = new CSectionAttack(0);
    public static final AttackType LASER = new LaserAttack(0);
    public static final AttackType BRIMSTONE = new BrimstoneAttack(0);
    public static final List<AttackType> PROJECTILES = List.of(BULLET, C_SECTION, LASER, BRIMSTONE);
}
