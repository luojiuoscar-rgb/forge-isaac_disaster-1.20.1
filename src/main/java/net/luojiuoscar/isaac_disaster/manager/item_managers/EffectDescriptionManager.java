package net.luojiuoscar.isaac_disaster.manager.item_managers;

import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.EffectId;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EffectDescriptionManager {
    // 单例实例
    private static final EffectDescriptionManager INSTANCE = new EffectDescriptionManager();
    private EffectDescriptionManager() {}
    public static EffectDescriptionManager getInstance() {
        return INSTANCE;
    }

    private final Map<Integer, List<Component>> registeredItems = new HashMap<>();

    public void register(int effectId, List<Component> description) {
        registeredItems.put(effectId, description);
    }

    public List<Component> getDescriptionFromId(int effectId) {
        return registeredItems.get(effectId);
    }

    public void init(){
        register(EffectId.POISON.getId(), List.of(
                Component.translatable("effect.isaac_disaster.poison").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.poison.explain.1")),
                Component.translatable("effect.isaac_disaster.poison.explain.2")
        ));
        register(EffectId.POWER_OF_BELIAL.getId(), List.of(
                Component.translatable("effect.isaac_disaster.power_of_belial").append(": ")
                        .append(TextHelper.formatAttribute("effect.isaac_disaster.power_of_belial.explain.1", StatManager.getDamageBonus()))
        ));
        register(EffectId.DIZZINESS.getId(), List.of(
                Component.translatable("effect.isaac_disaster.dizziness").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.dizziness.explain.1"))
        ));
        register(EffectId.TRANSCENDENCE.getId(), List.of(
                Component.translatable("effect.isaac_disaster.transcendence").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.transcendence.explain.1"))
        ));
        register(EffectId.INVINCIBLE.getId(), List.of(
                Component.translatable("effect.isaac_disaster.invincible").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.invincible.explain.1"))
        ));
        register(EffectId.FRAILTY.getId(), List.of(
                Component.translatable("effect.isaac_disaster.frailty").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.frailty.explain.1"))
        ));
        register(EffectId.NECRONMICON_SHIELD.getId(), List.of(
                Component.translatable("effect.isaac_disaster.necronmicon_shield").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.necronmicon_shield.explain.1"))
        ));
        register(EffectId.HOLY_SHIELD.getId(), List.of(
                Component.translatable("effect.isaac_disaster.holy_shield").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.holy_shield.explain.1")),
                Component.translatable("effect.isaac_disaster.holy_shield.explain.2"),
                Component.translatable("effect.isaac_disaster.holy_shield.explain.3")
        ));
        register(EffectId.LACRIMAL_HYPOSECRETION.getId(), List.of(
                Component.translatable("effect.isaac_disaster.lacrimal_hyposecretion").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.lacrimal_hyposecretion.explain.1"))
        ));
        register(EffectId.X_RAY_VISION.getId(), List.of(
                Component.translatable("effect.isaac_disaster.x_ray_vision").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.x_ray_vision.explain.1"))
        ));
        register(EffectId.CHARM.getId(), List.of(
                Component.translatable("effect.isaac_disaster.charm").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.charm.explain.1"))
        ));
        register(EffectId.VULNERABLE.getId(), List.of(
                Component.translatable("effect.isaac_disaster.vulnerable").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.vulnerable.explain.1"))
        ));
        register(EffectId.PANIC.getId(), List.of(
                Component.translatable("effect.isaac_disaster.panic").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.panic.explain.1"))
        ));
        register(EffectId.PAC_MAN.getId(), List.of(
                Component.translatable("effect.isaac_disaster.pac_man").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.pac_man.explain.1")),
                Component.translatable("effect.isaac_disaster.pac_man.explain.2")
        ));
        register(EffectId.RAMPAGE.getId(), List.of(
                Component.translatable("effect.isaac_disaster.rampage").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.rampage.explain.1"))
        ));
        register(EffectId.FRAGILE_HEART.getId(), List.of(
                Component.translatable("effect.isaac_disaster.fragile_heart").append(": ")
                        .append(Component.translatable("effect.isaac_disaster.fragile_heart.explain.1")),
                Component.translatable("effect.isaac_disaster.fragile_heart.explain.2")
        ));
        register(EffectId.TELEPATHY.getId(), List.of(
                Component.translatable("effect.isaac_disaster.telepathy").append(": ")
                        .append(TextHelper.formatAttribute("effect.isaac_disaster.telepathy.explain.1", StatManager.getRangeBonus()))
        ));
        register(EffectId.BABYLON.getId(), List.of(
                Component.translatable("effect.isaac_disaster.babylon").append(": ")
                        .append(TextHelper.formatAttribute("effect.isaac_disaster.babylon.explain.1", StatManager.getDamageBonus())),
                TextHelper.formatAttribute("effect.isaac_disaster.babylon.explain.2", 1000*StatManager.getMovementSpeedBonus())
        ));
    }




}