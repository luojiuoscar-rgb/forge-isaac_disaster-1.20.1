package net.luojiuoscar.isaac_disaster.loot.modifier.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class GeneralLootModifier extends LootModifier {

    public static final Codec<GeneralLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, GeneralLootModifier::new));

    public GeneralLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> objectArrayList, LootContext lootContext) {
        // position
        Vec3 pos = lootContext.getParamOrNull(LootContextParams.ORIGIN);
        if (pos == null) return objectArrayList;

        // player
        if (!(lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player))
            return objectArrayList;

        GeneralLootModifyEvent event = new GeneralLootModifyEvent(player, lootContext, objectArrayList);
        MinecraftForge.EVENT_BUS.post(event);

        // returns modified new list
        return event.getObjectArrayList();
    }
}
