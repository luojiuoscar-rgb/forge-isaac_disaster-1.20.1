package net.luojiuoscar.isaac_disaster.event.custom.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class ItemDisplayAddEvent extends Event {
    private final ServerLevel level;
    private final Player player;
    private final BlockPos pos;
    private final List<ItemStack> itemDisplays = new ArrayList<>();
    private final ResourceLocation lootTable;

    public ItemDisplayAddEvent(ServerLevel level, Player player, BlockPos pos, ResourceLocation lootTable) {
        this.level = level;
        this.player = player;
        this.pos = pos;
        this.lootTable = lootTable;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public Player getPlayer() {
        return player;
    }

    public BlockPos getPos() {
        return pos;
    }

    public ResourceLocation getLootTable() {return lootTable; }

    public void addDisplayItem(@NotNull ItemStack stack) {
        itemDisplays.add(stack);
    }

    public List<ItemStack> getDisplayItems() {
        return itemDisplays;
    }
}
