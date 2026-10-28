package net.luojiuoscar.isaac_disaster.item.weapon;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.luojiuoscar.isaac_disaster.client.renderer.item.MomsKnifeWeaponRenderer;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.item.IAttackLauncher;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackCandidate;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackSelection;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeWeaponAttack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class MomsKnifeWeapon extends SwordItem implements IAttackLauncher {
    private final Multimap<Attribute, AttributeModifier> modifiers = ImmutableMultimap.of(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 5, AttributeModifier.Operation.MULTIPLY_TOTAL),
            Attributes.ATTACK_SPEED,
            new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4F, AttributeModifier.Operation.ADDITION));

    public MomsKnifeWeapon(Properties properties) { super(Tiers.IRON, 0, -2.4F, properties.stacksTo(1)); }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? modifiers : ImmutableMultimap.of();
    }

    @Override public boolean isDamageable(ItemStack stack) { return false; }
    @Override public int getMaxDamage(ItemStack stack) { return 0; }
    @Override public boolean isEnchantable(@NotNull ItemStack stack) { return false; }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) { return false; }
    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
    @Override public boolean isValidRepairItem(@NotNull ItemStack stack, @NotNull ItemStack ingredient) { return false; }

    @Override
    public AttackSelection getAttackSelection(ServerPlayer player) {
        var attack = ModAttackTypes.MOMS_KNIFE_WEAPON.get();
        var candidate = new AttackCandidate(attack.getId(), attack, attack.getId(), 1,
                attack.getPriorityTier(), attack.getPriority());
        return new AttackSelection(candidate, candidate, List.of());
    }

    @Override public boolean usesVanillaItemUse() { return true; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.SPEAR; }
    @Override public int getUseDuration(ItemStack stack) { return 72000; }

    public static boolean isUnavailable(Player player) {
        if (player.getCooldowns().isOnCooldown(ModItems.MOMS_KNIFE_WEAPON.get())) return true;
        return player instanceof ServerPlayer server && MomsKnifeWeaponAttack.getFlyingKnife(server) != null;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
        if (isUnavailable(player) || player instanceof ServerPlayer server
                && !((MomsKnifeWeaponAttack) ModAttackTypes.MOMS_KNIFE_WEAPON.get()).isChargeEligible(server)) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        if (player instanceof ServerPlayer server) server.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .ifPresent(ability -> ability.handleAttackInput(server, true));
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof ServerPlayer player && player.getMainHandItem() == stack) {
            player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
                ability.refreshLauncher(player);
                if (ability.isHoldingRightClick()) ability.handleAttackInput(player, false);
            });
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (entity instanceof ServerPlayer player) player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
            ability.setHoldRightClick(false);
            ((MomsKnifeWeaponAttack) ModAttackTypes.MOMS_KNIFE_WEAPON.get()).clearCharge(player);
        });
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> text, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, text, flag);
        text.add(Component.translatable("item.isaac_disaster.moms_knife_weapon.lore.1"));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private MomsKnifeWeaponRenderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new MomsKnifeWeaponRenderer();
                return renderer;
            }
        });
    }
}
