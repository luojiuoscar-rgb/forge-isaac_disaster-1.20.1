package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerItemPoolsTest {
    private static final ResourceLocation POOL = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "pools/item/treasure");
    private static final ResourceLocation OTHER_POOL = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "pools/item/devil");
    private static final ResourceLocation SCOPED_ITEM = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "rock_bottom");
    private static final ResourceLocation GLOBAL_ITEM = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "sacred_heart");

    @Test
    void resourceLocationRulesIncludeGlobalEntriesWithoutMutatingAnotherPool() {
        PlayerItemPools pools = new PlayerItemPools();
        pools.removeFromPool(POOL, SCOPED_ITEM);
        pools.removeFromAll(GLOBAL_ITEM);

        assertEquals(Set.of(SCOPED_ITEM, GLOBAL_ITEM), pools.getRemoval(POOL));
        assertEquals(Set.of(GLOBAL_ITEM), pools.getRemoval(OTHER_POOL));
    }

    @Test
    void poolNbtWritesResourceLocationStrings() {
        PlayerItemPools pools = new PlayerItemPools();
        pools.addToPool(POOL, SCOPED_ITEM);

        CompoundTag nbt = new CompoundTag();
        pools.saveNBTData(nbt);

        ListTag poolEntries = nbt.getList("AddFromPool", Tag.TAG_COMPOUND);
        ListTag values = poolEntries.getCompound(0).getList("values", Tag.TAG_STRING);
        assertEquals(SCOPED_ITEM.toString(), values.getString(0));
    }

    @Test
    void legacyIntegerPoolNbtIsIgnored() {
        CompoundTag entry = new CompoundTag();
        entry.putString("id", POOL.toString());
        ListTag values = new ListTag();
        values.add(IntTag.valueOf(42));
        entry.put("values", values);
        ListTag entries = new ListTag();
        entries.add(entry);
        CompoundTag legacyNbt = new CompoundTag();
        legacyNbt.put("AddFromPool", entries);

        PlayerItemPools pools = new PlayerItemPools();
        pools.loadNBTData(legacyNbt);

        assertTrue(pools.getAddition(POOL).isEmpty());
    }
}
