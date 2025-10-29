package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.worldgen.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FissureDimensionEvent {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final ResourceLocation FISSURE_DIM_ID =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "fissure");

    private static final List<ResourceKey<Biome>> AVAILABLE_BIOMES = List.of(
            Biomes.PLAINS, Biomes.FOREST, Biomes.DESERT, Biomes.SNOWY_PLAINS
    );

    private static final Random RANDOM = new Random();
    private static final Map<ResourceLocation, Boolean> GENERATED_FLAGS = new HashMap<>();

    private static boolean isGenerated(ServerLevel level) {
        return GENERATED_FLAGS.getOrDefault(level.dimension().location(), false);
    }

    private static void setGenerated(ServerLevel level, boolean value) {
        GENERATED_FLAGS.put(level.dimension().location(), value);
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // 进入 fissure
        if (event.getTo().equals(ModDimensions.ISAACDIM_LEVEL_KEY)) {
            ServerLevel fissureLevel = player.getServer().getLevel(event.getTo());
            if (fissureLevel != null) {
                BlockPos center = new BlockPos(0, 100, 0);
                player.teleportTo(fissureLevel,
                        center.getX() + 0.5, center.getY(), center.getZ() + 0.5,
                        player.getYRot(), player.getXRot());
            }
        }
    }


}
