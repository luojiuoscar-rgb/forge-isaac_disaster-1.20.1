package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashSet;
import java.util.Set;

public class PlayerPerformAttackEvent extends Event {
    private final Player player;
    private int attackTypeId;
    private int bulletColorId;
    private final Set<Integer> hitEffects;
    private final Set<Integer> trajectories;

    public PlayerPerformAttackEvent(Player player, int attackTypeId, int bulletColorId, Set<Integer> trajectories) {
        this.player = player;
        this.attackTypeId = attackTypeId;
        this.bulletColorId = bulletColorId;
        this.hitEffects = new HashSet<>();
        this.trajectories = trajectories;
    }

    public Player getPlayer() {
        return player;
    }

    public int getAttackTypeId() { return attackTypeId; }

    public int getBulletColorId() {
        return bulletColorId;
    }

    public Set<Integer> getHitEffects() {
        return hitEffects;
    }

    public void setAttackTypeId(int input){
        this.attackTypeId = input;
    }

    public void setBulletColorId(int input){
        this.bulletColorId = input;
    }

    public void addHitEffect(int id){
        hitEffects.add(id);
    }

    public Set<Integer> getTrajectories(){
        return trajectories;
    }
}
