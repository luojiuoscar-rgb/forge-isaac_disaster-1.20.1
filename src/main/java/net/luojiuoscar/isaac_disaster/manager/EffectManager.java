package net.luojiuoscar.isaac_disaster.manager;

import net.minecraft.network.chat.Component;

public enum EffectManager {
    POISON("poison"),
    POWER_OF_BELIAL("power_of_belial"),
    DIZZINESS("dizziness"),
    TRANSCENDENCE("transcendence"),
    INVINCIBLE("invincible"),
    FRAILTY("frailty"),
    NECRONMICON_SHIELD("necronmicon_shield"),
    HOLY_SHIELD("holy_shield"),
    LACRIMAL_HYPOSECRETION("lacrimal_hyposecretion"),
    X_RAY_VISION("x_ray_vision"),
    CHARM("charm"),
    VULNERABLE("vulnerable"),
    PANIC("panic"),
    PAC_MAN("pac_man"),
    RAMPAGE("rampage"),
    FRAGILE_HEART("fragile_heart"),
    TELEPATHY("telepathy"),
    BABYLON("babylon"),
    SOUL_STATE("soul_state"),
    THE_WORLD("the_world"),
    ETERNAL_HEART("eternal_heart"),
    GILDING("gilding"),
    THE_WIZ("the_wiz"),
    GOLDEN("golden"),

    // Curses
    CURSE_OF_THE_BLIND("curse_of_the_blind"),
    CURSE_OF_THE_MAZE("curse_of_the_maze");

    private final int id;
    private final String name;

    EffectManager(String name) {
        this.id = this.ordinal(); // 自动生成递增 ID
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Component getExplainDesc(){
        return Component.translatable("effect.isaac_disaster." + name)
                .append(": ")
                .append(Component.translatable("effect.isaac_disaster." + name + ".description"));
    }
}
