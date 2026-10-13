package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationMode;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class PetrifiedPoop implements IAbilityEffect {
    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.LOOT;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof GeneralLootModifyEvent event)) return false;
        var objectArrayList = event.getObjectArrayList();
        var lootContext = event.getLootContext();

        if (objectArrayList.isEmpty()) return true;

        if (!(event.getEntity() instanceof ServerPlayer player)) return false;

        if (!lootContext.getQueriedLootTableId().equals(ModLootTables.POOP)) return true;

        ServerLevel serverLevel = (ServerLevel) player.level();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, player.position());

        LootParams.Builder paramsBuilder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, pos)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withLuck((float) getLuck(player));
        LootParams params = paramsBuilder.create(LootContextParamSets.EMPTY);

        ObjectArrayList<ItemStack> generatedLoot = new ObjectArrayList<>(LootHelper.getLoot(
                serverLevel, ModLootTables.PETRIFIED_POOP, params, LootGenerationMode.REPLACEMENT_ROLL,
                Math.max(0, context.getOrDefault(ContextKeys.LOOT_COUNT, 1))));

        event.setObjectArrayList(generatedLoot);
        return true;
    }
}
