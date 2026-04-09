package net.luojiuoscar.isaac_disaster.event.custom.misc;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.event.entity.living.LivingEvent;

public class GeneralLootModifyEvent extends LivingEvent {
    private final LootContext lootContext;
    private final ObjectArrayList<ItemStack> objectArrayList;


    public GeneralLootModifyEvent(LivingEntity entity, LootContext context, ObjectArrayList<ItemStack> objectArrayList) {
        super(entity);
        this.lootContext = context;
        this.objectArrayList = objectArrayList;
    }

    public LootContext getLootContext() {
        return lootContext;
    }

    public ObjectArrayList<ItemStack> getObjectArrayList() {
        return objectArrayList;
    }
}
