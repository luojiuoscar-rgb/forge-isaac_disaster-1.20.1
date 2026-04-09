package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class CharmOfTheVampire implements IAbilityEffect {
    private static final ResourceLocation DATA_KEY =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "charm_of_the_vampire");

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) return false;

        player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(
                extraData -> {
                    double bodyCount = extraData.getDouble(DATA_KEY) == null ? 0.0 : extraData.getDouble(DATA_KEY);

                    if (bodyCount >= 13){
                        StatManager.healHealth(player, 0.5f);
                        player.playNotifySound(ModSounds.RED_HEART.get(), SoundSource.PLAYERS,
                                1.0f, 1.0f);

                        extraData.setDouble(DATA_KEY, 0);
                    }
                    else{
                        extraData.setDouble(DATA_KEY, bodyCount + 1);
                    }
                }
        );

        return false;
    }
}
