package net.luojiuoscar.isaac_disaster.test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Initializes Mojang registries before plain JUnit touches Forge registry keys. */
public final class MinecraftBootstrapExtension implements BeforeAllCallback {
    private static boolean initialized;

    @Override
    public void beforeAll(ExtensionContext context) {
        synchronized (MinecraftBootstrapExtension.class) {
            if (initialized) return;
            SharedConstants.tryDetectVersion();
            try {
                Bootstrap.bootStrap();
            } catch (ExceptionInInitializerError error) {
                if (!isPlainJUnitNetworkInitialization(error)) throw error;
            }
            initialized = true;
        }
    }

    private static boolean isPlainJUnitNetworkInitialization(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof NoSuchMethodException
                    && current.getMessage() != null
                    && current.getMessage().contains("net.minecraftforge.network.NetworkEvent.<init>()")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}