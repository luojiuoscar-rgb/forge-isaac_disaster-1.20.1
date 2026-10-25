package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client;

import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightPhase;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightSnapshot;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** A beacon clip belongs only to its strike's warning, even when the asset is longer. */
final class HolyLightWarningSound extends AbstractTickableSoundInstance {
    private final HolyLightClientState state;
    private final long id;

    HolyLightWarningSound(HolyLightSnapshot snapshot, HolyLightClientState state) {
        super(SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.state = state;
        id = snapshot.id();
        x = snapshot.position().x;
        y = snapshot.position().y;
        z = snapshot.position().z;
        volume = .45F;
        pitch = 1.15F;
        looping = false;
    }

    @Override
    public boolean canPlaySound() {
        var entry = state.get(id);
        return !isStopped() && entry != null && entry.phase() == HolyLightPhase.WARNING;
    }

    @Override
    public void tick() {
        if (!canPlaySound()) stop();
    }

    void cancel() {
        stop();
    }
}
