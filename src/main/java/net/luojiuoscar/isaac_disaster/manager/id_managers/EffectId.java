package net.luojiuoscar.isaac_disaster.manager.id_managers;

public enum EffectId {
    POISON,
    POWER_OF_BELIAL,
    DIZZINESS,
    TRANSCENDENCE,
    INVINCIBLE,
    FRAILTY,
    NECRONMICON_SHIELD,
    HOLY_SHIELD,
    LACRIMAL_HYPOSECRETION,
    X_RAY_VISION,
    CHARM,
    VULNERABLE,
    PANIC,
    PAC_MAN,
    RAMPAGE,
    FRAGILE_HEART,
    TELEPATHY,
    BABYLON,
    REPULSION_AURA,
    THE_WORLD,
    ETERNAL_HEART,
    GILDING;


    private final int id;

    // 构造方法：自动生成递增的ID
    EffectId() {
        this.id = ordinal();
    }

    // 获取ID的方法
    public int getId() {
        return id;
    }
}
