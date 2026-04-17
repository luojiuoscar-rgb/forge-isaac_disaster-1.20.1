package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class GetAttackContextEvent extends Event {
    private final ServerPlayer player;
    private List<AttackContext> contexts;
    private final AttackType attackType;
    private final boolean directlyShotByPlayer;

    public GetAttackContextEvent(ServerPlayer player, List<AttackContext> contexts, AttackType attackType,
                                 boolean directlyShotByPlayer) {
        this.player = player;
        this.contexts = contexts;
        this.attackType = attackType;
        this.directlyShotByPlayer = directlyShotByPlayer;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public List<AttackContext> getContexts() {
        return contexts;
    }

    public void setContexts(List<AttackContext> contexts) {
        this.contexts = contexts;
    }

    public AttackType getAttackType() {
        return attackType;
    }

    public boolean isDirectlyShotByPlayer(){
        return directlyShotByPlayer;
    }
}
