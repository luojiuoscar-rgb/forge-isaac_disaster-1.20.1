package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TheWizEffect extends MobEffect {
    public TheWizEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }

    public static void onTriggered(GetAttackContextEvent event){
        if (!event.isDirectlyShotByPlayer()) return;
        ServerPlayer player = event.getPlayer();

        if (!player.hasEffect(ModEffects.THE_WIZ.get())) return;
        int amplifier = player.getEffect(ModEffects.THE_WIZ.get()).getAmplifier();
        int bulletCount = (event.getContexts().size() + 1) / 2;
        AttackType attack = event.getAttackType();

        if (amplifier > 0){
            List<AttackContext> newContexts = new ArrayList<>();
            for (AttackContext context : attack.getAttackContexts(player, bulletCount)) {
                context.setYRot(context.getYRot() - 45);
                newContexts.add(context);
            }

            for (AttackContext context : attack.getAttackContexts(player, bulletCount)){
                context.setYRot(context.getYRot() + 45);
                newContexts.add(context);
            }
            event.setContexts(newContexts);

        }else if (player.getRandom().nextDouble() < 0.5){
            for (AttackContext context : event.getContexts()){
                context.setYRot(context.getYRot() - 45);
            }
        }else{
            for (AttackContext context : event.getContexts()){
                context.setYRot(context.getYRot() + 45);
            }
        }
    }
}
