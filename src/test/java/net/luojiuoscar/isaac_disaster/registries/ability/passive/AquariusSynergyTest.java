package net.luojiuoscar.isaac_disaster.registries.ability.passive;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.Aquarius;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AquariusSynergyTest {
    @Test
    void onlyOwnedPartnersShowTheirOwnSynergyAndRemovingThemHidesIt() {
        var cache = ClientDataManager.getInstance();
        var spoon = ModPassiveItems.SPOON_BENDER.getId();
        var ipecac = ModPassiveItems.IPECAC.getId();
        var fire = ModPassiveItems.FIRE_MIND.getId();
        int oldSpoon = cache.getItemCount(spoon);
        int oldIpecac = cache.getItemCount(ipecac);
        int oldFire = cache.getItemCount(fire);
        var ability = new Aquarius(0, 2);
        try {
            cache.setItemCount(spoon, 0);
            cache.setItemCount(ipecac, 0);
            cache.setItemCount(fire, 0);
            assertTrue(ability.getSynergyDesc(null, null).isEmpty());
            cache.setItemCount(spoon, 1);
            assertEquals(2, ability.getSynergyDesc(null, null).size());
            cache.setItemCount(ipecac, 1);
            assertEquals(3, ability.getSynergyDesc(null, null).size());
            cache.setItemCount(fire, 1);
            assertEquals(4, ability.getSynergyDesc(null, null).size());
            cache.setItemCount(spoon, 0);
            assertEquals(2, ability.getSynergyDesc(null, null).size());
            cache.setItemCount(ipecac, 0);
            cache.setItemCount(fire, 0);
            assertTrue(ability.getSynergyDesc(null, null).isEmpty());
        } finally {
            cache.setItemCount(spoon, oldSpoon);
            cache.setItemCount(ipecac, oldIpecac);
            cache.setItemCount(fire, oldFire);
        }
    }
}
