package net.luojiuoscar.isaac_disaster.client.renderer.creature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.client.animation.SpiderAnimations;
import net.luojiuoscar.isaac_disaster.client.model.creature.SpiderModel;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider.SpiderMovement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class SpiderRendererTest {
    @Test
    void allVariantsFlashForHostileViewersAndAlliesAndUnknownOwnersKeepTheirColors() {
        UUID viewer=UUID.randomUUID(),owner=UUID.randomUUID();
        var scoreboard=new Scoreboard();
        var team=scoreboard.addPlayerTeam("friends");team.setAllowFriendlyFire(true);
        var enemies=scoreboard.addPlayerTeam("enemies");
        for(int color:new int[]{0x4077E0,0xD04030,0x308050}) {
            for(double seconds:new double[]{0,0.0625,0.12499}) {
                assertEquals(seconds < 0.0625 ? color : 0xD8D8D8,SpiderRenderer.renderColor(color,viewer,null,owner,null,seconds),"Every variant inherits the hostile flash");
                assertEquals(color,SpiderRenderer.renderColor(color,viewer,null,viewer,null,seconds));
                assertEquals(color,SpiderRenderer.renderColor(color,viewer,team,owner,team,seconds));
                assertEquals(color,SpiderRenderer.renderColor(color,null,null,owner,null,seconds));
                assertEquals(color,SpiderRenderer.renderColor(color,viewer,null,null,null,seconds));
            }
            assertEquals(0xD8D8D8,SpiderRenderer.renderColor(color,viewer,team,owner,enemies,0.0625));
            assertEquals(0xD8D8D8,SpiderRenderer.renderColor(color,viewer,null,owner,team,0.0625));
            assertEquals(0xD8D8D8,SpiderRenderer.renderColor(color,viewer,team,owner,null,0.0625));
        }
    }

    @Test
    void allVariantsHardSwitchBetweenTheirCodeColorAndGrayEightFullCyclesPerSecond() {
        UUID viewer=UUID.randomUUID(),owner=UUID.randomUUID();
        for(int color:new int[]{0x4077E0,0xD04030,0x308050}) {
            for(int cycle=0;cycle<8;cycle++) {
                double start=cycle*0.125;
                assertEquals(color,SpiderRenderer.renderColor(color,viewer,null,owner,null,start));
                assertEquals(color,SpiderRenderer.renderColor(color,viewer,null,owner,null,start+0.06249));
                assertEquals(0xD8D8D8,SpiderRenderer.renderColor(color,viewer,null,owner,null,start+0.0625));
                assertEquals(0xD8D8D8,SpiderRenderer.renderColor(color,viewer,null,owner,null,start+0.12499));
            }
            assertEquals(color,SpiderRenderer.renderColor(color,viewer,null,owner,null,1));
        }
    }

    @Test
    void plantedFeetPushOppositeWorldTravelForAllThreeDirectionsAndHeadings() {
        var model = new SpiderModel<Entity>(SpiderModel.createBodyLayer().bakeRoot());
        String[] clips = {"MOVE_FORWARD", "MOVE_LEFT", "MOVE_RIGHT"};
        SpiderMovement.Gait[] movements={SpiderMovement.Gait.FORWARD,SpiderMovement.Gait.LEFT,SpiderMovement.Gait.RIGHT};
        // The approved fangs point toward source -Z; left is source -X.
        Vector3f[] local = {new Vector3f(0,0,-1),new Vector3f(-1,0,0),new Vector3f(1,0,0)};
        for (float yaw : new float[]{0,90,180,270,37.5F}) {
            PoseStack transform = new PoseStack();
            SpiderRenderer.applyModelTransform(transform,yaw);
            for (int i=0;i<clips.length;i++) {
                Vector3f travel=transform.last().pose().transformDirection(new Vector3f(local[i]));
                assertEquals(movements[i],SpiderMovement.animation(new Vec3(travel.x,travel.y,travel.z),yaw),
                        clips[i]+" world direction at yaw="+yaw);
                var clip=SpiderAnimations.Clip.valueOf(clips[i]);
                assertEquals(clip,SpiderAnimations.movementClip(movements[i]));
                Vector3f from=worldFoot(model,clip,0.04F,yaw);
                Vector3f to=worldFoot(model,clip,0.08F,yaw);
                Vector3f push=to.sub(from);
                assertTrue(push.dot(travel)<-0.019F,clips[i]+" stance direction at yaw="+yaw);
                assertTrue(new Vector3f(push).cross(travel).length()<0.0001F,
                        clips[i]+" stance must not drift vertically or sideways");
                assertEquals(0.12F/16,from.y,0.00001F,"stance foot stays on the authored floor");
            }
        }
    }

    private static Vector3f worldFoot(SpiderModel<Entity> model,SpiderAnimations.Clip clip,float time,float yaw) {
        model.applyAnimation(clip,time);
        PoseStack stack=new PoseStack();
        SpiderRenderer.applyModelTransform(stack,yaw);
        var root=model.root().getChild("spider_root");
        var leg=root.getChild("leg_left_1");
        root.translateAndRotate(stack);
        leg.translateAndRotate(stack);
        leg.getChild("leg_left_1_knee").translateAndRotate(stack);
        return stack.last().pose().transformPosition(new Vector3f(-0.45F/16,2.03F/16,-0.75F/16));
    }
    @Test
    void modelFeetStayAtEntityPositionAndTheBodyStaysAboveGroundForEveryHeading() {
        for (float yaw : new float[]{0, 90, 180, 270, 37.5F}) {
            PoseStack stack = new PoseStack();
            SpiderRenderer.applyModelTransform(stack, yaw);
            Vector3f feet = stack.last().pose().transformPosition(new Vector3f(0, 1.5F, 0));
            Vector3f body = stack.last().pose().transformPosition(new Vector3f(0, 22.9F / 16, 0));
            assertEquals(0, feet.length(), 0.000001F);
            assertEquals(1.1F / 16, body.y, 0.000001F);
        }
    }

}
