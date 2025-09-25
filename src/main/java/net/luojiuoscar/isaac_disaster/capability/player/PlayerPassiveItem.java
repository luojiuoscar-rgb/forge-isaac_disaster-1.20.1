package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerPassiveItem {
    //用编号代表道具。需要查询道具的时候再使用道具管理器
    private List<Integer> playerPassiveItems;

    //constructor
    public PlayerPassiveItem(){
        this.playerPassiveItems = new ArrayList<>();
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

    //获取一个新的道具
    public void addItem(int itemId){
        playerPassiveItems.add(itemId);
    }

    //删除最先获取的一个道具
    public void removeFirstItem() {
        if (!playerPassiveItems.isEmpty()) {
            playerPassiveItems.remove(0);
        }
    }

    //删除最后获取的一个道具
    public void removeLastItem() {
        if (!playerPassiveItems.isEmpty()) {
            playerPassiveItems.remove(playerPassiveItems.size() - 1);
        }
    }

    //依据ID删除最先获取的一个道具
    public void removeFromId(int itemId) {
        // 使用迭代器遍历并移除第一个匹配的元素
        for (Iterator<Integer> iterator = playerPassiveItems.iterator(); iterator.hasNext(); ) {
            int id = iterator.next();
            if (id == itemId) {
                iterator.remove();
                break; // 只移除最先获取的一个
            }
        }
    }

    //从目标处复制
    public void copyFrom(PlayerPassiveItem source) {
        this.playerPassiveItems = new ArrayList<>(source.playerPassiveItems);
    }

    public void saveNBTData(CompoundTag nbt){
        nbt.putIntArray("passive_items", playerPassiveItems);
    }



    public void loadNBTData(CompoundTag nbt) {
        int[] ids = nbt.getIntArray("passive_items"); // 建议键名用下划线（见建议6）
        // 将int[]转换为List<Integer>
        playerPassiveItems = Arrays.stream(ids)
                .boxed()
                .collect(Collectors.toList());
    }
}
