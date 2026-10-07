package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalBlockEntity;
import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalPrice;
import net.luojiuoscar.isaac_disaster.block.custom.PedestalBlock;
import net.luojiuoscar.isaac_disaster.block.ModBlocks;
import net.luojiuoscar.isaac_disaster.commands.gamerule.ModGameRules;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PedestalGameTests {
    private PedestalGameTests() {
    }

    @GameTest(template = "trajectory_empty")
    public static void pedestalRegistersItemAndRemovesBothIndexes(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos, ModBlocks.PEDESTAL_BLOCK.get().defaultBlockState());

        if (!(helper.getLevel().getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)) {
            throw new AssertionError("Pedestal block entity was not created");
        }
        BlockData data = BlockData.get(helper.getLevel());
        helper.assertTrue(data.getAllPedestals().contains(pos), "Pedestal position is registered");

        pedestal.setItem(new ItemStack(Items.STICK));
        helper.assertTrue(data.getAllItemBlocks().contains(pos), "Displayed item position is registered");

        helper.getLevel().removeBlock(pos, false);
        helper.assertTrue(!data.getAllPedestals().contains(pos), "Pedestal position is removed");
        helper.assertTrue(!data.getAllItemBlocks().contains(pos), "Displayed item position is removed");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void manualInteractionStoresOneItemAndReturnsIt(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos, ModBlocks.PEDESTAL_BLOCK.get().defaultBlockState());
        if (!(helper.getLevel().getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)) {
            throw new AssertionError("Pedestal block entity was not created");
        }
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "pedestal-test"));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK, 4));

        InteractionResult inserted = pedestal.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(inserted.consumesAction(), "Insertion consumes the interaction");
        helper.assertTrue(pedestal.getItem().getCount() == 1, "Pedestal stores one item");
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 3,
                "Insertion removes one item from the player");

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        InteractionResult removed = pedestal.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(removed.consumesAction(), "Removal consumes the interaction");
        helper.assertTrue(pedestal.getItem().isEmpty(), "Pedestal is empty after removal");
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.STICK),
                "Player receives the displayed item");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void failedGenerationDoesNotMarkPedestalGenerated(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos, ModBlocks.PEDESTAL_BLOCK.get().defaultBlockState());
        if (!(helper.getLevel().getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)) {
            throw new AssertionError("Pedestal block entity was not created");
        }
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "pedestal-generation-test"));
        pedestal.setItemLootTable("isaac_disaster:missing_table");

        boolean generated = pedestal.tryLootItem(helper.getLevel(), player, pos);
        helper.assertTrue(!generated, "Invalid loot table does not report success");
        helper.assertTrue(!pedestal.isGenerated(), "Failed generation remains retryable");
        helper.assertTrue(pedestal.getGenerationState() == PedestalBlockEntity.GenerationState.FAILED_RETRYABLE,
                "Failed generation has an explicit retryable state");
        helper.assertTrue(pedestal.getContentSource() == PedestalBlockEntity.ContentSource.LOOT_TABLE,
                "Failed generation preserves its configured source");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void facingChangePreservesIndexesAndLinks(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        PedestalBlockEntity other = place(helper, 2);
        pedestal.setItem(new ItemStack(Items.STICK));
        PedestalBlockEntity.linkPedestals(pedestal.getBlockPos(), other.getBlockPos(), helper.getLevel());
        helper.getLevel().setBlockAndUpdate(pedestal.getBlockPos(), pedestal.getBlockState()
                .setValue(PedestalBlock.FACING, Direction.SOUTH));
        BlockData data = BlockData.get(helper.getLevel());
        helper.assertTrue(data.getAllPedestals().contains(pedestal.getBlockPos()), "Facing keeps registration");
        helper.assertTrue(data.getAllItemBlocks().contains(pedestal.getBlockPos()), "Facing keeps item index");
        helper.assertTrue(other.getLinkedPedestals().contains(pedestal.getBlockPos()), "Facing keeps links");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void storedItemCannotBeMutatedByCaller(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        pedestal.setItem(new ItemStack(Items.STICK, 32));
        pedestal.getItem().shrink(1);
        helper.assertTrue(pedestal.getItem().getCount() == 1, "Queries return a defensive copy");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void configuredLootPedestalRejectsSurvivalInsertion(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        pedestal.setItemLootTable("isaac_disaster:missing_table");
        ServerPlayer player = player(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK, 4));
        pedestal.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(pedestal.getItem().isEmpty(), "Insertion does not overwrite loot configuration");
        helper.assertTrue(player.getMainHandItem().getCount() == 4, "Rejected insertion costs nothing");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void explicitNbtRoundTripsAndRepeatedLoadResetsState(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        CompoundTag settings = new CompoundTag();
        settings.putString("contentSource", "LOOT_TABLE");
        settings.putString("itemLootTable", "isaac_disaster:missing_table");
        settings.putString("priceType", "LIFE");
        settings.putInt("priceAmount", 2);
        settings.putBoolean("locked", true);
        settings.putBoolean("generated", true);
        settings.putString("breakPolicy", "DISCARD_CONTENT");
        settings.putBoolean("autoUseOnAcquire", true);
        pedestal.load(settings);
        helper.assertTrue(pedestal.getPrice().equals(PedestalPrice.life(2)), "Explicit price is loaded");
        helper.assertTrue(pedestal.getContentSource() == PedestalBlockEntity.ContentSource.LOOT_TABLE,
                "Explicit content source is loaded");
        PedestalBlockEntity restored = new PedestalBlockEntity(pedestal.getBlockPos(), pedestal.getBlockState());
        restored.load(pedestal.saveWithoutMetadata());
        helper.assertTrue(restored.getPrice().equals(pedestal.getPrice()) && restored.isLocked()
                && restored.isGenerated() && restored.isAutoUseOnAcquire()
                && restored.getBreakPolicy() == PedestalBlockEntity.BreakPolicy.DISCARD_CONTENT,
                "NBT preserves independent configuration");
        pedestal.load(new CompoundTag());
        helper.assertTrue(pedestal.getPrice().isFree() && !pedestal.isLocked() && !pedestal.isGenerated()
                && pedestal.getItemLootTable().isEmpty() && !pedestal.isAutoUseOnAcquire()
                && pedestal.getBreakPolicy() == PedestalBlockEntity.BreakPolicy.DROP_CONTENT,
                "Absent fields reset previous values");
        settings.remove("breakPolicy");
        settings.remove("autoUseOnAcquire");
        pedestal.load(settings);
        helper.assertTrue(!pedestal.isAutoUseOnAcquire()
                && pedestal.getBreakPolicy() == PedestalBlockEntity.BreakPolicy.DROP_CONTENT,
                "Loot source does not infer independent options");
        CompoundTag oldFields = new CompoundTag();
        oldFields.putBoolean("isDecoration", false);
        oldFields.putInt("lifeCost", 2);
        oldFields.putInt("moneyCost", 9);
        pedestal.load(oldFields);
        helper.assertTrue(pedestal.getContentSource() == PedestalBlockEntity.ContentSource.MANUAL
                && pedestal.getPrice().isFree(), "Removed legacy fields do not configure the pedestal");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void successfulGenerationMarksStateAndFailedRerollPreservesContent(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        pedestal.setItemLootTable("isaac_disaster:pedestal_test");
        ServerPlayer player = player(helper);
        helper.assertTrue(pedestal.tryLootItem(helper.getLevel(), player, pedestal.getBlockPos()),
                "Fixture produces a valid item");
        helper.assertTrue(pedestal.isGenerated() && !pedestal.getItemDisplayList().isEmpty(),
                "Successful operation marks generated immediately");
        ItemStack before = pedestal.getItem();
        pedestal.setItemLootTable("isaac_disaster:missing_table");
        pedestal.itemRollFromPlayer(player);
        helper.assertTrue(ItemStack.matches(before, pedestal.getItem()), "Failed reroll preserves the previous item");
        helper.assertTrue(!pedestal.isGenerated(), "Failed generation remains retryable");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void acquisitionConsumesLinkedManualPedestals(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        PedestalBlockEntity other = place(helper, 2);
        pedestal.setItem(new ItemStack(Items.STICK));
        other.setItem(new ItemStack(Items.APPLE));
        PedestalBlockEntity.linkPedestals(pedestal.getBlockPos(), other.getBlockPos(), helper.getLevel());
        ServerPlayer player = player(helper);
        BlockPos pos = pedestal.getBlockPos();
        pedestal.getBlockState().use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(pos.getCenter(), Direction.UP, pos, false));
        helper.assertTrue(pedestal.getItem().isEmpty() && other.getItem().isEmpty(),
                "Link consumption is independent of content source");
        helper.assertTrue(!BlockData.get(helper.getLevel()).getAllItemBlocks().contains(other.getBlockPos()),
                "Link consumption clears other item index");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void paymentIsRequiredBeforeAcquisitionAndCreativeBypassesIt(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        ServerPlayer player = player(helper);
        pedestal.setItem(new ItemStack(Items.STICK));
        int coinValue = Config.COIN_TIER_1_VALUE.get();
        player.setPos(pedestal.getBlockPos().getCenter());
        pedestal.setPrice(PedestalPrice.money(coinValue * 3));
        pedestal.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(!pedestal.getItem().isEmpty() && player.getMainHandItem().isEmpty(),
                "Insufficient money keeps the item on its pedestal");
        player.getInventory().setItem(1, new ItemStack(ForgeRegistries.ITEMS.getValue(
                ResourceLocation.parse(Config.COIN_TIER_1_ID.get())), 8));
        int moneyBefore = PlayerHelper.countMoney(player);
        pedestal.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(pedestal.getItem().isEmpty() && player.getMainHandItem().is(Items.STICK),
                "Successful purchase transfers the item");
        List<ItemStack> change = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(pedestal.getBlockPos()).inflate(0.5)).stream().map(ItemEntity::getItem).toList();
        helper.assertTrue(PlayerHelper.countMoney(player) + PlayerHelper.countMoney(change) == moneyBefore - coinValue * 3,
                "Successful purchase charges exactly its price");
        player.getInventory().clearContent();
        player.setGameMode(GameType.CREATIVE);
        pedestal.setItem(new ItemStack(Items.APPLE));
        pedestal.setPrice(PedestalPrice.money(999));
        pedestal.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(pedestal.getItem().isEmpty() && player.getMainHandItem().is(Items.APPLE),
                "Creative players bypass payment");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void lifePurchaseChargesHealthAndRejectsUnaffordablePrices(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        ServerPlayer player = player(helper);
        double maxHealth = player.getMaxHealth();
        double bonus = StatManager.MAX_HEALTH.getBonus();
        pedestal.setItem(new ItemStack(Items.STICK));
        pedestal.setPrice(PedestalPrice.life((int) (maxHealth / bonus) + 1));
        pedestal.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(!pedestal.getItem().isEmpty() && player.getMaxHealth() == maxHealth,
                "Unaffordable life purchase changes neither item nor health");
        pedestal.setPrice(PedestalPrice.life(1));
        pedestal.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(pedestal.getItem().isEmpty() && player.getMainHandItem().is(Items.STICK),
                "Affordable life purchase transfers item");
        helper.assertTrue(Math.abs(player.getMaxHealth() - (maxHealth - bonus)) < 0.001,
                "Life purchase uses the existing health-unit conversion");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void copyResetsGenerationAndPreservesIndependentConfiguration(GameTestHelper helper) {
        PedestalBlockEntity original = place(helper, 1);
        PedestalBlockEntity copy = place(helper, 2);
        original.setItemLootTable("isaac_disaster:pedestal_test");
        original.setPrice(PedestalPrice.money(7));
        original.setLocked(true);
        original.setBreakPolicy(PedestalBlockEntity.BreakPolicy.DISCARD_CONTENT);
        original.setGenerated(true);
        original.setItemDisplayList(List.of(new ItemStack(Items.STICK), new ItemStack(Items.APPLE)));
        copy.setItem(new ItemStack(Items.DIAMOND));
        copy.copyFromOriginal(original);
        helper.assertTrue(copy.getPrice().equals(original.getPrice()) && copy.isLocked()
                && copy.getItemLootTable().equals(original.getItemLootTable())
                && copy.getBreakPolicy() == original.getBreakPolicy(), "Copy preserves configuration");
        helper.assertTrue(copy.getGenerationState() == PedestalBlockEntity.GenerationState.READY,
                "Copied loot pedestal starts with fresh generation");
        helper.assertTrue(copy.getItem().isEmpty() && copy.getItemDisplayList().isEmpty(),
                "Copy duplicates configuration, never the original displayed item or alternatives");
        original.setContentSource(PedestalBlockEntity.ContentSource.MANUAL);
        helper.assertTrue(original.getBreakPolicy() == PedestalBlockEntity.BreakPolicy.DISCARD_CONTENT
                && original.getPrice().equals(PedestalPrice.money(7)), "Source changes do not redefine price or break policy");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void displayListPersistenceAndMutationKeepSingleItemInvariant(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        ItemStack external = new ItemStack(Items.STICK, 32);
        pedestal.setItemDisplayList(List.of(external, new ItemStack(Items.APPLE, 4)));
        external.shrink(32);
        pedestal.getItemDisplayList().get(0).shrink(1);
        List<ItemStack> snapshot = pedestal.getItemDisplayList();
        snapshot.add(new ItemStack(Items.DIAMOND));
        snapshot.clear();
        helper.assertTrue(pedestal.getItem().getCount() == 1
                && pedestal.getItemDisplayList().get(0).getCount() == 1
                && pedestal.getItemDisplayList().size() == 2, "Display inputs and queries cannot leak mutable stacks or lists");
        CompoundTag saved = pedestal.saveWithoutMetadata();
        pedestal.clearContent();
        pedestal.load(saved);
        helper.assertTrue(pedestal.getItem().is(Items.STICK) && pedestal.getItemDisplayList().size() == 2,
                "Save/load restores display contents");
        pedestal.rotateItemDisplay();
        helper.assertTrue(pedestal.getItem().is(Items.APPLE), "Explicit rotation synchronizes the displayed stack");
        pedestal.getItemDisplayCap().clear();
        helper.assertTrue(pedestal.getItem().isEmpty()
                && !BlockData.get(helper.getLevel()).getAllItemBlocks().contains(pedestal.getBlockPos()),
                "Capability clear updates the display and index");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void linkedLootConsumptionPersistsAndDoesNotGenerateAgain(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        PedestalBlockEntity other = place(helper, 2);
        pedestal.setItemLootTable("isaac_disaster:pedestal_test");
        other.setItemLootTable("isaac_disaster:pedestal_test");
        pedestal.setItem(new ItemStack(Items.STICK));
        other.setItem(new ItemStack(Items.APPLE));
        PedestalBlockEntity.linkPedestals(pedestal.getBlockPos(), other.getBlockPos(), helper.getLevel());
        pedestal.interact(player(helper), InteractionHand.MAIN_HAND);
        CompoundTag saved = other.saveWithoutMetadata();
        other.load(saved);
        PedestalBlockEntity.tick(helper.getLevel(), other.getBlockPos(), other.getBlockState(), other);
        helper.assertTrue(other.getItem().isEmpty() && other.isGenerated()
                && other.getContentSource() == PedestalBlockEntity.ContentSource.LOOT_TABLE,
                "Consumed choice remains empty with its original source after reload and tick");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void pendingLinkConsumptionSurvivesSaveAndIsAppliedOnLoad(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        pedestal.setItemLootTable("isaac_disaster:pedestal_test");
        pedestal.setItem(new ItemStack(Items.STICK));
        BlockData data = BlockData.get(helper.getLevel());
        data.queuePedestalClear(pedestal.getBlockPos());
        BlockData restored = BlockData.load(data.save(new CompoundTag()));
        helper.assertTrue(restored.takePendingPedestalClear(pedestal.getBlockPos()), "Pending choice consumption is persisted");
        pedestal.onLoad();
        helper.assertTrue(pedestal.getItem().isEmpty() && pedestal.isGenerated(), "Load consumes pending choice");
        helper.assertTrue(!data.takePendingPedestalClear(pedestal.getBlockPos()), "Pending record is removed after application");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void reconciliationRemovesStaleLoadedEntriesAndPreservesUnloadedOnes(GameTestHelper helper) {
        BlockData data = BlockData.get(helper.getLevel());
        BlockPos stale = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos unloaded = new BlockPos(30000000, 100, 30000000);
        helper.assertTrue(!helper.getLevel().hasChunkAt(unloaded), "Fixture uses an unloaded position");
        for (BlockPos pos : Set.of(stale, unloaded)) {
            data.addPedestal(pos);
            data.addItemBlock(pos);
        }
        data.reconcileLoaded(helper.getLevel());
        helper.assertTrue(!data.getAllPedestals().contains(stale) && !data.getAllItemBlocks().contains(stale),
                "Stale loaded entries are removed");
        helper.assertTrue(data.getAllPedestals().contains(unloaded) && data.getAllItemBlocks().contains(unloaded),
                "Unloaded entries remain indexed");
        helper.assertTrue(!helper.getLevel().hasChunkAt(unloaded), "Reconciliation does not load chunks");
        data.removePedestal(unloaded);
        data.removeItemBlock(unloaded);
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void serverTickKeepsFailuresRetryableAndGeneratesOnlyOnce(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        ServerPlayer player = player(helper);
        player.setPos(pedestal.getBlockPos().getCenter());
        helper.getLevel().addNewPlayer(player);
        boolean disabled = helper.getLevel().getGameRules().getBoolean(ModGameRules.DISABLE_PLACEHOLDER);
        helper.getLevel().getGameRules().getRule(ModGameRules.DISABLE_PLACEHOLDER).set(false,
                helper.getLevel().getServer());
        try {
            pedestal.setItemLootTable("isaac_disaster:missing_table");
            PedestalBlockEntity.tick(helper.getLevel(), pedestal.getBlockPos(), pedestal.getBlockState(), pedestal);
            helper.assertTrue(pedestal.getGenerationState() == PedestalBlockEntity.GenerationState.FAILED_RETRYABLE,
                    "Server tick does not mark failed generation as successful");
            pedestal.setItemLootTable("isaac_disaster:pedestal_test");
            PedestalBlockEntity.tick(helper.getLevel(), pedestal.getBlockPos(), pedestal.getBlockState(), pedestal);
            helper.assertTrue(pedestal.isGenerated() && !pedestal.getItem().isEmpty(), "Tick generates valid configured content");
            pedestal.interact(player, InteractionHand.MAIN_HAND);
            PedestalBlockEntity.tick(helper.getLevel(), pedestal.getBlockPos(), pedestal.getBlockState(), pedestal);
            helper.assertTrue(pedestal.isGenerated() && pedestal.getItem().isEmpty(), "Tick does not regenerate a consumed choice");
        } finally {
            helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
            helper.getLevel().getGameRules().getRule(ModGameRules.DISABLE_PLACEHOLDER).set(disabled,
                    helper.getLevel().getServer());
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void blockItemNbtAndMalformedInventoryAreNormalized(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        ItemStack blockItem = new ItemStack(ModBlocks.PEDESTAL_BLOCK.get());
        CompoundTag settings = new CompoundTag();
        settings.putString("contentSource", "LOOT_TABLE");
        settings.putString("itemLootTable", "isaac_disaster:pedestal_test");
        settings.putString("priceType", "MONEY");
        settings.putInt("priceAmount", 8);
        CompoundTag inventory = new CompoundTag();
        inventory.putInt("Size", 0);
        settings.put("inventory", inventory);
        blockItem.addTagElement("BlockEntityTag", settings);
        BlockItem.updateCustomBlockEntityTag(helper.getLevel(), player(helper), pedestal.getBlockPos(), blockItem);
        helper.assertTrue(pedestal.getPrice().equals(PedestalPrice.money(8)) && pedestal.getItem().isEmpty(),
                "Block item NBT configures pedestal and normalizes malformed inventory size");
        pedestal.setItem(new ItemStack(Items.STICK, 16));
        helper.assertTrue(pedestal.getItem().getCount() == 1
                && BlockData.get(helper.getLevel()).getAllPedestals().contains(pedestal.getBlockPos())
                && BlockData.get(helper.getLevel()).getAllItemBlocks().contains(pedestal.getBlockPos()),
                "NBT placement preserves registration and the single-slot invariant");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void breakPolicyControlsDropsAndExplicitPriceReplacesPreviousPrice(GameTestHelper helper) {
        PedestalBlockEntity drop = place(helper, 1);
        PedestalBlockEntity discard = place(helper, 2);
        drop.setItem(new ItemStack(Items.STICK));
        discard.setItem(new ItemStack(Items.APPLE));
        discard.setBreakPolicy(PedestalBlockEntity.BreakPolicy.DISCARD_CONTENT);
        drop.setPrice(PedestalPrice.life(2));
        drop.setPrice(PedestalPrice.money(7));
        helper.assertTrue(drop.getPrice().equals(PedestalPrice.money(7)), "Setting a price replaces the previous type");
        drop.setPrice(PedestalPrice.free());
        helper.assertTrue(drop.getPrice().isFree(), "Explicit free price removes payment conditions");
        helper.getLevel().removeBlock(drop.getBlockPos(), false);
        helper.getLevel().removeBlock(discard.getBlockPos(), false);
        List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(drop.getBlockPos(), discard.getBlockPos().offset(1, 1, 1)));
        helper.assertTrue(items.stream().anyMatch(item -> item.getItem().is(Items.STICK)), "Drop policy drops content");
        helper.assertTrue(items.stream().noneMatch(item -> item.getItem().is(Items.APPLE)), "Discard policy suppresses content drop");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void manualTakingDoesNotAutoUseUnlessExplicitlyConfigured(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        ServerPlayer player = player(helper);
        boolean previous = Config.AUTO_USE_PASSIVE_ITEM.get();
        boolean previousBackpack = Config.EXTRA_PASSIVE_ITEM_BACKPACK.get();
        Config.AUTO_USE_PASSIVE_ITEM.set(true);
        Config.EXTRA_PASSIVE_ITEM_BACKPACK.set(true);
        try {
            pedestal.setItem(new ItemStack(ModPassiveItems.BREAKFAST.get()));
            pedestal.interact(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.getMainHandItem().is(ModPassiveItems.BREAKFAST.get()),
                    "Manual display returns a passive item without activating it");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            pedestal.setAutoUseOnAcquire(true);
            pedestal.setItem(new ItemStack(ModPassiveItems.BREAKFAST.get()));
            pedestal.interact(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.getMainHandItem().isEmpty(), "Explicit auto use follows the global auto-use setting");
        } finally {
            Config.AUTO_USE_PASSIVE_ITEM.set(previous);
            Config.EXTRA_PASSIVE_ITEM_BACKPACK.set(previousBackpack);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void unloadedGroupUpdatesPersistAndApplyMembershipUnlockAndRemoval(GameTestHelper helper) {
        PedestalBlockEntity first = place(helper, 1);
        PedestalBlockEntity third = place(helper, 2);
        BlockPos unloaded = new BlockPos(30000000, 100, 30000000);
        first.setLinkedPedestals(Set.of(first.getBlockPos(), unloaded));
        first.setLocked(true);
        BlockData data = BlockData.get(helper.getLevel());
        PedestalBlockEntity.linkPedestals(first.getBlockPos(), third.getBlockPos(), helper.getLevel());
        first.unlockAll();
        helper.assertTrue(!helper.getLevel().hasChunkAt(unloaded), "Link updates do not load the absent member");
        BlockData restored = BlockData.load(data.save(new CompoundTag()));
        BlockData.PendingPedestalUpdate pending = restored.takePendingPedestalUpdate(unloaded);
        helper.assertTrue(pending != null && Boolean.FALSE.equals(pending.locked())
                && pending.linkedPositions().contains(third.getBlockPos()), "Unloaded member receives merged membership and unlock");
        data.queuePedestalLinks(third.getBlockPos(), pending.linkedPositions());
        data.queuePedestalLock(third.getBlockPos(), true);
        data.queuePedestalUnlink(third.getBlockPos(), first.getBlockPos());
        third.onLoad();
        helper.assertTrue(third.isLocked() && !third.getLinkedPedestals().contains(first.getBlockPos())
                && third.getLinkedPedestals().contains(unloaded), "Load applies pending membership, lock, and removed links");
        helper.getLevel().removeBlock(first.getBlockPos(), false);
        helper.assertTrue(data.takePendingPedestalUpdate(unloaded).removedLinks().contains(first.getBlockPos()),
                "Removing a loaded member queues unlink for an absent member");
        data.takePendingPedestalClear(unloaded);
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void invalidConfigurationUsesSafeDefaultsAndKeepsSharedInterface(GameTestHelper helper) {
        PedestalBlockEntity pedestal = place(helper, 1);
        helper.assertTrue(pedestal.getBlockState().getBlock() instanceof
                net.luojiuoscar.isaac_disaster.block.custom.ItemDisplayContainerBlock,
                "Pedestal retains the shared acquisition interface");
        pedestal.setBreakPolicy(null);
        helper.assertTrue(pedestal.getBreakPolicy() == PedestalBlockEntity.BreakPolicy.DROP_CONTENT,
                "Null break policy falls back to default");
        pedestal.setPrice(null);
        helper.assertTrue(pedestal.getPrice().isFree(), "Null price falls back to free");
        CompoundTag invalid = new CompoundTag();
        invalid.putString("contentSource", "invalid");
        invalid.putString("breakPolicy", "invalid");
        invalid.putString("priceType", "invalid");
        pedestal.load(invalid);
        helper.assertTrue(pedestal.getContentSource() == PedestalBlockEntity.ContentSource.MANUAL
                && pedestal.getBreakPolicy() == PedestalBlockEntity.BreakPolicy.DROP_CONTENT
                && pedestal.getPrice().isFree(), "Invalid NBT enums use defaults");
        invalid.putString("priceType", "MONEY");
        invalid.putInt("priceAmount", -7);
        pedestal.load(invalid);
        helper.assertTrue(pedestal.getPrice().isFree(), "Negative NBT price falls back to free");
        pedestal.setItemLootTable("Invalid table!");
        helper.assertTrue(!pedestal.tryLootItem(helper.getLevel(), player(helper), pedestal.getBlockPos())
                && pedestal.getGenerationState() == PedestalBlockEntity.GenerationState.FAILED_RETRYABLE,
                "Malformed table ID stays retryable without throwing");
        Set<BlockPos> invalidLinks = new java.util.HashSet<>();
        invalidLinks.add(null);
        invalidLinks.add(pedestal.getBlockPos());
        BlockData.PendingPedestalUpdate pending = new BlockData.PendingPedestalUpdate(null, invalidLinks, null);
        helper.assertTrue(pending.linkedPositions().equals(Set.of(pedestal.getBlockPos()))
                && pending.removedLinks().isEmpty(), "Pending update sanitizes null collections and members");
        CompoundTag invalidData = BlockData.get(helper.getLevel()).save(new CompoundTag());
        net.minecraft.nbt.ListTag identifiers = new net.minecraft.nbt.ListTag();
        CompoundTag invalidIdentifier = new CompoundTag();
        invalidIdentifier.putString("Type", "Invalid identifier!");
        identifiers.add(invalidIdentifier);
        invalidData.put("Identifiers", identifiers);
        BlockData restoredData = BlockData.load(invalidData);
        helper.assertTrue(restoredData.getAllIdentifiers().isEmpty()
                && restoredData.getAllPedestals().contains(pedestal.getBlockPos()),
                "Invalid saved identifiers are skipped without discarding valid pedestal data");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void giveBlockEntityTagPlacesFreeLootPedestal(GameTestHelper helper) {
        assertGiveAndPlace(helper, PedestalBlockEntity.ContentSource.LOOT_TABLE, PedestalPrice.free());
    }

    @GameTest(template = "trajectory_empty")
    public static void giveBlockEntityTagPlacesLifeShop(GameTestHelper helper) {
        assertGiveAndPlace(helper, PedestalBlockEntity.ContentSource.LOOT_TABLE, PedestalPrice.life(2));
    }

    @GameTest(template = "trajectory_empty")
    public static void giveBlockEntityTagPlacesMoneyShop(GameTestHelper helper) {
        assertGiveAndPlace(helper, PedestalBlockEntity.ContentSource.LOOT_TABLE, PedestalPrice.money(15));
    }

    @GameTest(template = "trajectory_empty")
    public static void giveBlockEntityTagPlacesManualPedestal(GameTestHelper helper) {
        assertGiveAndPlace(helper, PedestalBlockEntity.ContentSource.MANUAL, PedestalPrice.free());
    }

    private static void assertGiveAndPlace(GameTestHelper helper, PedestalBlockEntity.ContentSource source,
                                          PedestalPrice price) {
        ServerPlayer player = player(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        player.setPos(pos.getX() + 4, pos.getY(), pos.getZ() + 4);
        boolean loot = source == PedestalBlockEntity.ContentSource.LOOT_TABLE;
        String command = "give @s isaac_disaster:pedestal{BlockEntityTag:{contentSource:\"" + source
                + "\",priceType:\"" + price.type() + "\",priceAmount:" + price.amount()
                + (loot ? ",itemLootTable:\"isaac_disaster:pedestal_test\",breakPolicy:\"DISCARD_CONTENT\",autoUseOnAcquire:1b"
                        : ",breakPolicy:\"DROP_CONTENT\",autoUseOnAcquire:0b") + "}} 1";
        int result = helper.getLevel().getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack().withPermission(2).withSuppressedOutput(), command);
        helper.assertTrue(result == 1, "Give command parses SNBT and grants the configured block item");
        ItemStack stack = player.getInventory().items.stream()
                .filter(item -> item.is(ModBlocks.PEDESTAL_BLOCK.get().asItem())).findFirst().orElse(ItemStack.EMPTY);
        helper.assertTrue(!stack.isEmpty() && stack.getTagElement("BlockEntityTag") != null,
                "Granted item retains BlockEntityTag");
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.assertTrue(((BlockItem) stack.getItem()).place(new BlockPlaceContext(player,
                InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)))
                .consumesAction(), "Configured item is placed through the real BlockItem path");
        PedestalBlockEntity pedestal = (PedestalBlockEntity) helper.getLevel().getBlockEntity(pos);
        helper.assertTrue(pedestal != null && pedestal.getContentSource() == source
                && pedestal.getPrice().equals(price) && pedestal.isAutoUseOnAcquire() == loot
                && pedestal.getBreakPolicy() == (loot ? PedestalBlockEntity.BreakPolicy.DISCARD_CONTENT
                        : PedestalBlockEntity.BreakPolicy.DROP_CONTENT), "Placement retains independent options");
        helper.assertTrue(BlockData.get(helper.getLevel()).getAllPedestals().contains(pos),
                "Actual block-item placement registers its coordinates");
        if (loot) {
            helper.assertTrue(pedestal.getGenerationState() == PedestalBlockEntity.GenerationState.READY
                    && pedestal.tryLootItem(helper.getLevel(), player, pos)
                    && pedestal.getItem().is(ModPassiveItems.BREAKFAST.get()),
                    "Placed loot pedestal generates from its configured table");
        } else {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK, 2));
            pedestal.interact(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(pedestal.getItem().is(Items.STICK), "Placed manual pedestal accepts an item");
        }
        helper.assertTrue(BlockData.get(helper.getLevel()).getAllItemBlocks().contains(pos),
                "Placed pedestal registers its display after generation or insertion");
        helper.succeed();
    }

    private static PedestalBlockEntity place(GameTestHelper helper, int x) {
        BlockPos pos = helper.absolutePos(new BlockPos(x, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos, ModBlocks.PEDESTAL_BLOCK.get().defaultBlockState());
        return (PedestalBlockEntity) helper.getLevel().getBlockEntity(pos);
    }

    private static ServerPlayer player(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "pedestal-test"));
    }
}
