package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MrBoom extends ActiveAbility {
    private final IAbilityEffect effect = ModAbilityEffects.THROW_BOMB.get();

    public MrBoom(int id, int level) {
        super(id, level);
    }

    @Override
    protected AbilityEffectContext getCtx(ServerPlayer player, ItemStack stack, @Nullable InteractionHand hand, int amplifier) {
        var ctx = super.getCtx(player, stack, hand, amplifier);
        ctx.set(ContextKeys.DOUBLE, List.of(80., 7.));
        return ctx;
    }

    @Override
    protected IAbilityEffect getAbilityEffect() {
        return effect;
    }

    @Override
    public void triggerSFX(ServerPlayer player) {
        player.playNotifySound(SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 1.0f ,1.0f);
    }


    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.mr_boom.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.no_effect"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }
}
