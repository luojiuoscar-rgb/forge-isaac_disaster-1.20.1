package net.luojiuoscar.isaac_disaster.client.renderer.creature;

import net.minecraft.world.scores.Scoreboard;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FlyRendererTest {
    @Test
    void allVariantsFlashForHostileViewersAndAlliesAndUnknownOwnersKeepTheirColors() {
        UUID viewer=UUID.randomUUID(),owner=UUID.randomUUID();
        var scoreboard=new Scoreboard();
        var team=scoreboard.addPlayerTeam("friends");team.setAllowFriendlyFire(true);
        var enemies=scoreboard.addPlayerTeam("enemies");
        for(int color:new int[]{0x4077E0,0xD04030,0x308050}) {
            for(double seconds:new double[]{0,0.0625,0.12499}) {
                assertEquals(seconds < 0.0625 ? color : 0xD8D8D8,FlyRenderer.renderColor(color,viewer,null,owner,null,seconds),"Every variant inherits the hostile flash");
                assertEquals(color,FlyRenderer.renderColor(color,viewer,null,viewer,null,seconds));
                assertEquals(color,FlyRenderer.renderColor(color,viewer,team,owner,team,seconds));
                assertEquals(color,FlyRenderer.renderColor(color,null,null,owner,null,seconds));
                assertEquals(color,FlyRenderer.renderColor(color,viewer,null,null,null,seconds));
            }
            assertEquals(0xD8D8D8,FlyRenderer.renderColor(color,viewer,team,owner,enemies,0.0625));
            assertEquals(0xD8D8D8,FlyRenderer.renderColor(color,viewer,null,owner,team,0.0625));
            assertEquals(0xD8D8D8,FlyRenderer.renderColor(color,viewer,team,owner,null,0.0625));
        }
    }

    @Test
    void allVariantsHardSwitchBetweenTheirCodeColorAndGrayEightFullCyclesPerSecond() {
        UUID viewer=UUID.randomUUID(),owner=UUID.randomUUID();
        for(int color:new int[]{0x4077E0,0xD04030,0x308050}) {
            for(int cycle=0;cycle<8;cycle++) {
                double start=cycle*0.125;
                assertEquals(color,FlyRenderer.renderColor(color,viewer,null,owner,null,start));
                assertEquals(color,FlyRenderer.renderColor(color,viewer,null,owner,null,start+0.06249));
                assertEquals(0xD8D8D8,FlyRenderer.renderColor(color,viewer,null,owner,null,start+0.0625));
                assertEquals(0xD8D8D8,FlyRenderer.renderColor(color,viewer,null,owner,null,start+0.12499));
            }
            assertEquals(color,FlyRenderer.renderColor(color,viewer,null,owner,null,1));
        }
    }

}
