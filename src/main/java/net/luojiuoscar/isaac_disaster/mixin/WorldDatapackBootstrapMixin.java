package net.luojiuoscar.isaac_disaster.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.luojiuoscar.isaac_disaster.datapack.init.WorldDatapackBootstrap;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplaySettings;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import java.nio.file.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Dedicated Main calls the Path overload directly; integrated worlds delegate to it as well. */
@Mixin(ServerPacksSource.class)
public abstract class WorldDatapackBootstrapMixin {
    @ModifyReturnValue(method = "createPackRepository(Ljava/nio/file/Path;)Lnet/minecraft/server/packs/repository/PackRepository;", at = @At("RETURN"))
    private static PackRepository isaac$seedWorldPack(PackRepository repository, Path directory) {
        Path normalized = directory.toAbsolutePath().normalize();
        if (normalized.getFileName() != null && normalized.getFileName().toString().equals("datapacks")) {
            WorldDatapackBootstrap.ensure(normalized.getParent(), GameplaySettings::fromTemplate);
        }
        return repository;
    }
}
