package net.luojiuoscar.isaac_disaster.gametest;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** An opt-in integrated-client smoke test, isolated from normal client and GameTest runs. */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, value = Dist.CLIENT)
public final class SlotClientSmoke {
    private static final String WORLD = "slot-client-" + System.currentTimeMillis();
    private static int stage;
    private static int ticks;
    private static CompletableFuture<Void> serverWork;
    private static boolean stopped;
    private static boolean transitioning;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (!Boolean.getBoolean("isaac.slotClientSmoke") || stopped || transitioning || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        ticks++;
        if (stage == 0 && ticks == 1) IsaacDisaster.LOGGER.info("SLOT_CLIENT_SMOKE_ENABLED");
        try {
            if (ticks > 2400) throw new IllegalStateException("Client smoke timed out at stage " + stage);
            if (serverWork != null) {
                if (!serverWork.isDone()) return;
                serverWork.join();
                serverWork = null;
                ticks = 0;
            }
            switch (stage) {
                case 0 -> {
                    if (mc.screen != null && mc.screen.getClass().getSimpleName().contains("AccessibilityOnboarding")) {
                        mc.setScreen(new TitleScreen());
                    }
                    if (!(mc.screen instanceof TitleScreen) || ticks < 20) return;
                    mc.options.renderDistance().set(2);
                    mc.options.simulationDistance().set(5);
                    mc.options.pauseOnLostFocus = false;
                    next(1);
                    mc.createWorldOpenFlows().createFreshLevel(WORLD,
                            new LevelSettings("Curios Slot Validation", GameType.CREATIVE, false, Difficulty.PEACEFUL,
                                    true, new GameRules(), WorldDataConfiguration.DEFAULT),
                            new WorldOptions(20261014L, false, false), WorldPresets::createNormalWorldDimensions);
                }
                case 1 -> {
                    if (!ready(mc) || ticks < 40) return;
                    work(player -> {
                        require(serverSlots(player, CurioSlotManager.TRINKET) == 1 && serverSlots(player, CurioSlotManager.PASSIVE_ITEM) == 5, "Fresh configured capacities");
                        set(player, 0, ModPassiveItems.MOMS_PURSE.get());
                    });
                    next(2);
                }
                case 2 -> {
                    if (!ready(mc) || ticks < 40) return;
                    require(clientSlots(mc, CurioSlotManager.TRINKET) == 2, "Client received purse capacity");
                    NetworkHandler.INSTANCE.sendToServer(new CPacketOpenCurios(ItemStack.EMPTY));
                    next(3);
                }
                case 3 -> {
                    if (!curiosScreen(mc) || ticks < 25) return;
                    capture(mc, "01-purse-expanded.png");
                    work(player -> {
                        set(player, 1, ModPassiveItems.MOMS_CONTACTS.get());
                        set(player, 2, ModPassiveItems.DARK_MATTER.get());
                        set(player, 3, ModPassiveItems.CONE_HEAD.get());
                        set(player, 4, ModPassiveItems.SCORPIO.get());
                    });
                    next(4);
                }
                case 4 -> {
                    if (ticks < 30) return;
                    work(player -> player.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(1));
                    next(5);
                }
                case 5 -> {
                    if (!curiosScreen(mc) || ticks < 35) return;
                    require(clientSlots(mc, CurioSlotManager.PASSIVE_ITEM) == 1, "Client received five-to-one shrink");
                    capture(mc, "02-five-to-one.png");
                    work(player -> {
                        for (Item item : new Item[]{ModPassiveItems.MOMS_CONTACTS.get(), ModPassiveItems.DARK_MATTER.get(), ModPassiveItems.CONE_HEAD.get(), ModPassiveItems.SCORPIO.get()}) {
                            int count = player.getInventory().items.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
                            require(count == 1, "Overflow returned exactly once: " + item);
                        }
                        player.getAttribute(ModAttributes.TRINKET_SLOTS.get()).setBaseValue(0);
                        player.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(0);
                    });
                    next(6);
                }
                case 6 -> {
                    if (!ready(mc) || ticks < 40) return;
                    require(clientSlots(mc, CurioSlotManager.TRINKET) == 0 && clientSlots(mc, CurioSlotManager.PASSIVE_ITEM) == 0, "Client received both zero capacities");
                    capture(mc, "03-zero-slots.png");
                    work(player -> {
                        player.getAttribute(ModAttributes.TRINKET_SLOTS.get()).setBaseValue(1);
                        player.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(5);
                    });
                    next(7);
                }
                case 7 -> {
                    if (!ready(mc) || ticks < 40) return;
                    require(clientSlots(mc, CurioSlotManager.TRINKET) == 1 && clientSlots(mc, CurioSlotManager.PASSIVE_ITEM) == 5, "Client recovered capacity after zero");
                    if (!curiosScreen(mc)) NetworkHandler.INSTANCE.sendToServer(new CPacketOpenCurios(ItemStack.EMPTY));
                    next(8);
                }
                case 8 -> {
                    if (!curiosScreen(mc) || ticks < 25) return;
                    capture(mc, "04-recovered.png");
                    work(player -> set(player, 0, ModPassiveItems.MOMS_PURSE.get()));
                    next(9);
                }
                case 9 -> {
                    if (!ready(mc) || ticks < 40) return;
                    require(clientSlots(mc, CurioSlotManager.TRINKET) == 2, "Purse equipped before real save");
                    transitioning = true;
                    try {
                        mc.level.disconnect();
                        mc.clearLevel(new TitleScreen());
                        next(10);
                    } finally {
                        transitioning = false;
                    }
                }
                case 10 -> {
                    if (mc.getSingleplayerServer() != null || !(mc.screen instanceof TitleScreen) || ticks < 30) return;
                    next(11);
                    mc.createWorldOpenFlows().loadLevel(new TitleScreen(), WORLD);
                }
                case 11 -> {
                    if (!ready(mc) || ticks < 60) return;
                    require(clientSlots(mc, CurioSlotManager.TRINKET) == 2, "Actual world reopen keeps exactly one purse bonus");
                    work(player -> {
                        require(player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow().getActiveCurioSlots().size() == 1, "Reloaded equipment effect record");
                        player.level().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, player.server);
                        player.kill();
                    });
                    next(12);
                }
                case 12 -> {
                    if (!(mc.screen instanceof DeathScreen) || mc.player == null) return;
                    mc.player.respawn();
                    next(13);
                }
                case 13 -> {
                    if (!ready(mc) || mc.player.getHealth() <= 0 || ticks < 60) return;
                    require(clientSlots(mc, CurioSlotManager.TRINKET) == 2, "Real respawn retains the purse bonus once");
                    work(player -> player.teleportTo(player.server.getLevel(Level.NETHER), 0, 100, 0, 0, 0));
                    next(14);
                }
                case 14 -> {
                    if (!ready(mc) || mc.level.dimension() != Level.NETHER || ticks < 60) return;
                    require(clientSlots(mc, CurioSlotManager.TRINKET) == 2, "Actual dimension change preserves capacity");
                    Files.writeString(Path.of(mc.gameDirectory.getAbsolutePath(), "slot-smoke-result.txt"),
                            "PASS: real Curios GUI, purse equip, five-to-one return, zero/recovery synchronization, world save/reopen, retained death/respawn and Nether transfer\nworld=" + WORLD);
                    IsaacDisaster.LOGGER.info("SLOT_CLIENT_SMOKE_PASS world={}", WORLD);
                    stopped = true;
                    mc.stop();
                }
            }
        } catch (Throwable error) {
            IsaacDisaster.LOGGER.error("SLOT_CLIENT_SMOKE_FAIL stage=" + stage, error);
            stopped = true;
            mc.stop();
        }
    }

    private static boolean ready(Minecraft mc) {
        return mc.player != null && mc.level != null && mc.getSingleplayerServer() != null;
    }

    private static boolean curiosScreen(Minecraft mc) {
        return mc.screen != null && mc.screen.getClass().getName().contains("curios");
    }

    private static int clientSlots(Minecraft mc, String type) {
        return CuriosApi.getCuriosInventory(mc.player).resolve().orElseThrow().getStacksHandler(type).orElseThrow().getSlots();
    }

    private static int serverSlots(ServerPlayer player, String type) {
        return CuriosApi.getCuriosInventory(player).resolve().orElseThrow().getStacksHandler(type).orElseThrow().getSlots();
    }

    private static void set(ServerPlayer player, int index, Item item) {
        CuriosApi.getCuriosInventory(player).resolve().orElseThrow().getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow()
                .getStacks().setStackInSlot(index, new ItemStack(item));
        CurioSlotManager.requestReconcile(player);
    }

    private static void work(Consumer<ServerPlayer> action) {
        var server = Minecraft.getInstance().getSingleplayerServer();
        serverWork = new CompletableFuture<>();
        var future = serverWork;
        server.execute(() -> {
            try {
                action.accept(server.getPlayerList().getPlayers().get(0));
                future.complete(null);
            } catch (Throwable error) {
                future.completeExceptionally(error);
            }
        });
    }

    private static void next(int value) {
        stage = value;
        ticks = 0;
        IsaacDisaster.LOGGER.info("SLOT_CLIENT_STAGE {}", value);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private static void capture(Minecraft mc, String name) throws Exception {
        Path folder = Path.of(mc.gameDirectory.getAbsolutePath(), "screenshots");
        Files.createDirectories(folder);
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { image.writeToFile(folder.resolve(name)); }
        IsaacDisaster.LOGGER.info("SLOT_CLIENT_SCREENSHOT {}", name);
    }
}
