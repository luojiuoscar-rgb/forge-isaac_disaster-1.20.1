package net.luojiuoscar.isaac_disaster.registries.ability_effect.profile;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;

public class PotionProfile {

    public final MobEffect effect;
    public final int duration;
    public final int amplifier;
    public final int duration_increment;
    public final int amplifier_increment;
    public final boolean has_particle;

    public PotionProfile(MobEffect effect, int duration, int amplifier){
        this.effect = effect;
        this.duration = duration;
        this.amplifier = Mth.clamp(amplifier, 0, 255);
        this.duration_increment = duration;
        this.amplifier_increment = 0;
        this.has_particle = true;
    }

    public PotionProfile(MobEffect effect,
                         int duration, int amplifier, int duration_increment,
                         int amplifier_increment, boolean has_particle){
        this.effect = effect;
        this.duration = duration;
        this.amplifier = Mth.clamp(amplifier, 0, 255);
        this.duration_increment = duration_increment;
        this.amplifier_increment = amplifier_increment;
        this.has_particle = has_particle;
    }

}
