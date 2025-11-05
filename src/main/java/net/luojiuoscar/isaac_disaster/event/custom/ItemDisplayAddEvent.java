package net.luojiuoscar.isaac_disaster.event.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemDisplayAddEvent extends Event {
    private final ServerLevel level;
    private final Player player;
    private final BlockPos pos;
    private final List<ItemStack> itemDisplays = new ArrayList<>();

    public ItemDisplayAddEvent(ServerLevel level, Player player, BlockPos pos) {
        this.level = level;
        this.player = player;
        this.pos = pos;
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

    /**
     * 外部可以往这里添加展示型物品
     */
    public void addDisplayItem(@NotNull ItemStack stack) {
        itemDisplays.add(stack);
    }

    /**
     * 获取外部注册的展示物品
     */
    public List<ItemStack> getDisplayItems() {
        return itemDisplays;
    }
}
