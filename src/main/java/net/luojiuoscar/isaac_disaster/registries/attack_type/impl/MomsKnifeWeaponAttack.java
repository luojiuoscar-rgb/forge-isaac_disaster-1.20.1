package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class MomsKnifeWeaponAttack extends AttackType implements IChargeableAttack {
    public MomsKnifeWeaponAttack() { super(300, 0); }

    @Override public ResourceLocation getId() { return ModAttackTypes.MOMS_KNIFE_WEAPON.getId(); }
    @Override public ResourceLocation getRootId() { return ModAttackTypes.MOMS_KNIFE.getId(); }

    @Nullable
    public static PlayerMomsKnifeEntity getFlyingKnife(ServerPlayer player) {
        var ability = player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).orElse(null);
        return ability == null ? null : ability.getRuntimeEntities().getEntity(
                ModAttackTypes.MOMS_KNIFE_WEAPON.getId(), player.serverLevel(), PlayerMomsKnifeEntity.class);
    }

    @Override
    public boolean isChargeEligible(ServerPlayer player) {
        return player.isAlive() && !player.isRemoved() && !player.isSpectator()
                && player.getMainHandItem().is(ModItems.MOMS_KNIFE_WEAPON.get())
                && !player.hasEffect(ModEffects.LACRIMAL_HYPOSECRETION.get())
                && !player.getCooldowns().isOnCooldown(ModItems.MOMS_KNIFE_WEAPON.get()) && getFlyingKnife(player) == null;
    }

    @Override public int getTotalCharge(Player player) { return Math.max(1, (int) Math.ceil(4 * getShotDelay(player))); }

    @Override
    public double getChargeFraction(ServerPlayer player) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).map(ability ->
                Math.min(1, (double) Math.max(1, ability.getChargeAmount(getChargeBarId())) / getTotalCharge(player))).orElse(0.0);
    }

    @Override public void onTick(ServerPlayer player) { addCharge(player, getChargePerTick(player)); }

    @Override
    public void onReleased(ServerPlayer player) {
        try { if (isChargeEligible(player)) shoot(plainContext(player)); }
        finally { clearCharge(player); }
    }

    private AttackContext plainContext(ServerPlayer player) {
        return AttackContext.builder(player, player).attackType(this).position(PlayerMomsKnifeMotion.idlePosition(player, 1))
                .mainAxis(player.getLookAngle()).range(getRange(player)).speed(getBulletSpeed(player)).build();
    }

    @Override public List<AttackContext> getAttackContexts(ServerPlayer player, int count) { return List.of(plainContext(player)); }

    @Override
    public void performAttack(List<AttackContext> contexts, Consumer<AttackContext> onFired) {
        if (!contexts.isEmpty()) shoot(contexts.get(0));
    }

    @Override
    public void shoot(AttackContext requested) {
        if (!(requested.getOwner() instanceof ServerPlayer player) || !isChargeEligible(player)) return;
        AttackContext plain = plainContext(player);
        double range = PlayerMomsKnifeMotion.range(plain.getBulletRange(), getChargeFraction(player));
        int slot = player.getInventory().selected;
        ItemStack weapon = player.getMainHandItem();
        var knife = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), player.serverLevel());
        if (!knife.initializeIndependentReturning(player, getId(), range, plain.getBulletSpeed(), plain, () -> {
            if (getFlyingKnife(player) == null) player.getCooldowns().removeCooldown(ModItems.MOMS_KNIFE_WEAPON.get());
        })) return;
        if (!player.serverLevel().addFreshEntity(knife)) { knife.discard(); return; }
        if (player.getInventory().selected != slot || player.getMainHandItem() != weapon || weapon.isEmpty()) {
            knife.discard();
            return;
        }
        if (!knife.carryItem(weapon, slot)) { knife.discard(); return; }
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability ->
                ability.getRuntimeEntities().register(getId(), knife.getUUID()));
        player.getCooldowns().addCooldown(ModItems.MOMS_KNIFE_WEAPON.get(), (int) Math.ceil(knife.getFlightDuration()) + 1);
    }

    @Override public void makeSound(LivingEntity entity) {}
}
