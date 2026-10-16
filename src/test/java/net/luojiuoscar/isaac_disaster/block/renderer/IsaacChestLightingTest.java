package net.luojiuoscar.isaac_disaster.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.block.custom.chest.*;
import net.luojiuoscar.isaac_disaster.test.MinecraftBootstrapExtension;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MinecraftBootstrapExtension.class)
class IsaacChestLightingTest {
    @Test
    void animatedChestBodyIsNotAlsoDrawnWithChunkLighting() throws Exception {
        for (Class<? extends IsaacChestBlock> type : List.of(NormalChestBlock.class, LockedChestBlock.class,
                OldChestBlock.class, EternalChestBlock.class, RedChestBlock.class, WoodenChest.class, BombChest.class)) {
            IsaacChestBlock chest = withoutRegistration(type);
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                BlockState state = Blocks.CHEST.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
                assertEquals(RenderShape.ENTITYBLOCK_ANIMATED, chest.getRenderShape(state),
                        type.getSimpleName() + " " + facing);
            }
        }
    }

    @Test
    void placeholderWithoutLidKeepsItsStaticBody() throws Exception {
        ChestPlaceholderBlock placeholder = withoutRegistration(ChestPlaceholderBlock.class);
        assertEquals(RenderShape.MODEL, placeholder.getRenderShape(Blocks.CHEST.defaultBlockState()));
    }

    @Test
    void closedLidNormalMatchesTheBodyFacing() {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            PoseStack poses = new PoseStack();
            IsaacChestRenderer.applyLidTransform(poses, facing, 0.0F);
            Vector3f front = poses.last().normal().transform(new Vector3f(0, 0, -1));
            assertVector(front, facing.getStepX(), 0, facing.getStepZ());
            assertVector(poses.last().normal().transform(new Vector3f(0, 1, 0)), 0, 1, 0);
        }
    }

    @Test
    void hingeStaysFixedForEveryFacingAndOpening() {
        Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        float[][] hinges = {{0.5F, 0.45F, 0.875F}, {0.125F, 0.45F, 0.5F},
                {0.5F, 0.45F, 0.125F}, {0.875F, 0.45F, 0.5F}};
        for (int i = 0; i < facings.length; i++) {
            for (float openness : new float[]{0, 0.25F, 0.5F, 1}) {
                PoseStack poses = new PoseStack();
                IsaacChestRenderer.applyLidTransform(poses, facings[i], openness);
                Vector3f hinge = poses.last().pose().transformPosition(new Vector3f(0.5F, 0.45F, 0.875F));
                assertVector(hinge, hinges[i][0], hinges[i][1], hinges[i][2]);
            }
        }
    }

    @Test
    void lightingNormalsFollowTheEasedLidAnimation() {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            PoseStack halfOpen = new PoseStack();
            IsaacChestRenderer.applyLidTransform(halfOpen, facing, 0.5F);
            Vector3f front = halfOpen.last().normal().transform(new Vector3f(0, 0, -1));
            // Half of the raw opening eases to 78.75 degrees, rather than 45 degrees.
            assertVector(front, facing.getStepX() * 0.1950903F, 0.9807853F,
                    facing.getStepZ() * 0.1950903F);

            PoseStack fullyOpen = new PoseStack();
            IsaacChestRenderer.applyLidTransform(fullyOpen, facing, 1.0F);
            assertVector(fullyOpen.last().normal().transform(new Vector3f(0, 0, -1)), 0, 1, 0);
        }
    }

    private static void assertVector(Vector3f actual, float x, float y, float z) {
        assertEquals(x, actual.x, 0.00001F, "x");
        assertEquals(y, actual.y, 0.00001F, "y");
        assertEquals(z, actual.z, 0.00001F, "z");
    }

    // JUnit's vanilla bootstrap freezes the registry. Render-shape dispatch needs no instance fields.
    private static <T> T withoutRegistration(Class<T> type) throws Exception {
        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Field field = unsafeType.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return type.cast(unsafeType.getMethod("allocateInstance", Class.class).invoke(field.get(null), type));
    }
}
