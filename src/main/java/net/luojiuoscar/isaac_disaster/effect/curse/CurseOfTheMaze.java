package net.luojiuoscar.isaac_disaster.effect.curse;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class CurseOfTheMaze extends MobEffect {
    public CurseOfTheMaze(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of(new ItemStack(ModItems.BLACK_CANDLE.get()));
    }

    public static void onTriggered(LivingHurtEvent event){
        RandomSource random = event.getEntity().getRandom();

        if (random.nextDouble() < 0.2){ // 20%
            PlayerHelper.teleportToRandomLocation(event.getEntity(), 0.5 * StatManager.getNearbyRange());
        }

    }
}
