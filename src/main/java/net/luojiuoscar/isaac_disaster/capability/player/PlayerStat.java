package net.luojiuoscar.isaac_disaster.capability.player;



public class PlayerStat {
    //将玩家新增的属性附加到玩家身上
    private int maxHealthBoost;

    public PlayerStat(){
        maxHealthBoost = 0;
    }

    public void modifyMaxHealth(int amount){
        maxHealthBoost = Math.max(maxHealthBoost + amount, 0);
    }

    public int getMaxHealth(int amount){
        return maxHealthBoost;
    }
}
