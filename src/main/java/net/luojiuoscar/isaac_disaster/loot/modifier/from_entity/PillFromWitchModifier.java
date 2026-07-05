package net.luojiuoscar.isaac_disaster.loot.modifier.from_entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.loot.LootContextHelper;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationContext;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationMode;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class PillFromWitchModifier extends LootModifier {
    private static final EnumSet<LootGenerationMode> SUPPORTED_MODES =
            EnumSet.of(LootGenerationMode.NATURAL_DROP, LootGenerationMode.SPAWN_DROP);

    public static final Codec<PillFromWitchModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, PillFromWitchModifier::new));

    public PillFromWitchModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    private static final ResourceLocation PILLS = ModLootTables.RANDOM_PILLS;

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> objectArrayList, LootContext context) {
        if (!SUPPORTED_MODES.contains(LootGenerationContext.currentMode())) return objectArrayList;

        var entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof Witch)) {
            return objectArrayList;
        }

        ServerPlayer player = LootContextHelper.findResponsiblePlayer(context);
        if (player == null) return objectArrayList;
        var pos = context.getParamOrNull(LootContextParams.ORIGIN);
        if (pos == null) pos = entity.position();

        var level = context.getLevel();
        var server = level.getServer();
        var lootManager = server.getLootData();
        var lootTable = lootManager.getLootTable(PILLS);

        int rolls = 0;
        if (context.getRandom().nextFloat() < .4f) { // 40%
            rolls = context.getRandom().nextInt(1,3);
        }

        // 构造params
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, pos)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .create(LootContextParamSets.EMPTY);

        for (int i = 0; i < rolls; i++) {
            LootGenerationContext.run(LootGenerationMode.DERIVED_DROP,
                    () -> lootTable.getRandomItems(params, objectArrayList::add));
        }

        return objectArrayList;
    }


    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
