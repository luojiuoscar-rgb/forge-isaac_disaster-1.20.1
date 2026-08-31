package net.luojiuoscar.isaac_disaster.manager.data;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerItemPoolsDataTest {
    private static final ResourceLocation POOL = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "pools/item/treasure");
    private static final ResourceLocation OTHER_POOL = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "pools/item/devil");
    private static final ResourceLocation SCOPED_ITEM = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "rock_bottom");
    private static final ResourceLocation GLOBAL_ITEM = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "sacred_heart");

    @Test
    void resourceLocationRulesApplyToSharedPools() {
        ServerItemPoolsData pools = new ServerItemPoolsData();
        pools.addToPool(POOL, SCOPED_ITEM);
        pools.addToAll(GLOBAL_ITEM);

        assertEquals(Set.of(SCOPED_ITEM, GLOBAL_ITEM), pools.getAddition(POOL));
        assertEquals(Set.of(GLOBAL_ITEM), pools.getAddition(OTHER_POOL));
    }
}
