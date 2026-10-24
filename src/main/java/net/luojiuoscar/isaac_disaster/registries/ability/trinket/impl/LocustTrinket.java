package net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl;

import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbilityContext;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.impl.LocustGeneration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public class LocustTrinket extends TrinketAbility {
    private final RegistryObject<RecursiveModule> module;

    public LocustTrinket(int id, int level, RegistryObject<RecursiveModule> module) {
        super(id, level);
        this.module = module;
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, TrinketAbilityContext context) {
        if (entity instanceof ServerPlayer player && module.get() instanceof LocustGeneration generation) {
            generation.generateForTrinket(player, context.isEnchanted ? 2 : 1);
        }
    }

    @Override
    public void onEquipped(LivingEntity entity, TrinketAbilityContext context) {
        if (entity instanceof ServerPlayer) StatManager.addRecursiveModule(entity, module.getId(), context.isEnchanted ? 2 : 1);
    }

    @Override
    public void onUnequipped(LivingEntity entity, TrinketAbilityContext context) {
        if (entity instanceof ServerPlayer) StatManager.addRecursiveModule(entity, module.getId(), context.isEnchanted ? -2 : -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(Component.translatable("item.isaac_disaster.locust_trinket.lore.1",
                BigDecimal.valueOf(StatManager.getTimeInterval(4) / 20.0).stripTrailingZeros().toPlainString(),
                getGenerationAmount(stack != null && Trinket.isEnchanted(stack) ? 2 : 1),
                Component.translatable("entity.isaac_disaster." + module.getId().getPath())));
    }

    protected Component getGenerationAmount(int amplifier) {
        return DescriptionHelper.dynamicNumber(amplifier);
    }
}
