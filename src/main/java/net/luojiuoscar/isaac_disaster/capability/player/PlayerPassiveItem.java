package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.manager.ItemManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

@AutoRegisterCapability
public class PlayerPassiveItem {
    //用编号代表道具。需要查询道具的时候再使用道具管理器
    private ArrayList<Integer> playerPassiveItems;

    //constructor
    public PlayerPassiveItem(){
        this.playerPassiveItems = new ArrayList<>();
    }

    public ArrayList<Integer> getPlayerPassiveItems(){
        return playerPassiveItems;
    }

    /**
     * 通过循环删除最先获取的道具来清空道具列表
     */
    public void clearPlayerPassiveItems(Player player){
        while(removeFirstItem(player));
    }

    //获取全部道具的数量
    public int getTotalItems(){
        return playerPassiveItems.size();
    }

    //获取某个道具的总数
    public int getItemCount(int itemId) {
        int count = 0;
        for (int id : playerPassiveItems) {
            if (id == itemId) {
                count++;
            }
        }
        return count;
    }

    //将一个新的道具添加到列表。同时触发添加道具的效果和直接添加效果
    public void addItem(Player player, int itemId){
        playerPassiveItems.add(itemId);
        ItemManager.getInstance().getItemFromId(itemId).obtainEffect(player);
        ItemManager.getInstance().getItemFromId(itemId).directObtainEffect(player);
    }

    //将一个新的道具添加到列表。同时触发添加道具的直接添加效果
    public void directAddItem(Player player, int itemId){
        playerPassiveItems.add(itemId);
        ItemManager.getInstance().getItemFromId(itemId).directObtainEffect(player);
    }

    //删除最先获取的一个道具
    public boolean removeFirstItem(Player player) {
        if (!playerPassiveItems.isEmpty()) {
            int removeId = playerPassiveItems.remove(0);
            ItemManager.getInstance().getItemFromId(removeId).removeEffect(player);
            return true;
        }
        return false;
    }

    //删除最后获取的一个道具
    public boolean removeLastItem(Player player) {
        if (!playerPassiveItems.isEmpty()) {
            int removeId = playerPassiveItems.remove(playerPassiveItems.size() - 1);
            ItemManager.getInstance().getItemFromId(removeId).removeEffect(player);
            return true;
        }
        return false;
    }

    /**
     * 依据ID删除最先获取的一个道具
     * 由C2S packet触发
     */
    public void removeFromId(Player player, int itemId) {
        // 使用迭代器遍历并移除第一个匹配的元素
        for (Iterator<Integer> iterator = playerPassiveItems.iterator(); iterator.hasNext(); ) {
            int id = iterator.next();
            if (id == itemId) {
                // 只移除最先获取的一个
                // 移除后一并触发效果
                iterator.remove();
                ItemManager.getInstance().getItemFromId(itemId).removeEffect(player);
                break;
            }
        }
    }

    //从目标处复制
    public void copyFrom(PlayerPassiveItem source) {
        this.playerPassiveItems = new ArrayList<>(source.playerPassiveItems);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putIntArray("passive_items", playerPassiveItems);
    }



    public void loadNBTData(CompoundTag nbt) {
        int[] ids = nbt.getIntArray("passive_items");
        // 将int[]转换为List<Integer>
        playerPassiveItems = Arrays.stream(ids)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));

    }
}
