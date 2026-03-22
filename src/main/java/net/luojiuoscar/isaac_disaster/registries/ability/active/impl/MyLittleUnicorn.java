package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MyLittleUnicorn extends ActiveAbility {
    private final IAbilityEffect effect = ModAbilityEffects.POTION.get();

    public MyLittleUnicorn(int id, int level) {
        super(id, level);
    }

    @Override
    protected IAbilityEffect getAbilityEffect() {
        return effect;
    }

    @Override
    protected AbilityEffectContext getCtx(ServerPlayer player, ItemStack stack, @Nullable InteractionHand hand, int amplifier) {
        AbilityEffectContext ctx = super.getCtx(player, stack, hand, amplifier);
        int duration = 120;
        ctx.set(ContextKeys.POTIONS, List.of(
                new PotionProfile(MobEffects.MOVEMENT_SPEED, duration, 0),
                new PotionProfile(ModEffects.INVINCIBLE.get(), duration, 0),
                new PotionProfile(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration, 0),
                new PotionProfile(ModEffects.RAMPAGE.get(), duration, 3)
        ));
        return ctx;
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.special.invincible", 6),
                Component.translatable("item.isaac_disaster.my_little_unicorn.lore.1"),
                Component.translatable("item.isaac_disaster.my_little_unicorn.lore.2"),
                Component.translatable("item.isaac_disaster.my_little_unicorn.lore.3")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.increase_duration"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack){
        List<Component> description = new ArrayList<>();

        description.add(EffectManager.INVINCIBLE.getExplainDesc());
        description.add(EffectManager.LACRIMAL_HYPOSECRETION.getExplainDesc());
        description.add(EffectManager.RAMPAGE.getExplainDesc());

        return description;
    }
}
