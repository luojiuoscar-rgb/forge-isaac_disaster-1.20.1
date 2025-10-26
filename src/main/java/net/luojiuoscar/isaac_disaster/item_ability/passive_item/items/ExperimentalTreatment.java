package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ExperimentalTreatment implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.EXPERIMENTAL_TREATMENT.getId();
    }

    @Override
    public void onFirstObtain(Player player) {
    }

    @Override
    public void onObtain(Player player) {
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), 1);
        RandomSource random = player.getRandom();

        // health
        if (random.nextDouble() < 0.6){
            StatManager.modifyMaxHealth(player, 1);
            player.sendSystemMessage(Component.translatable("item.isaac_disaster.experimental_treatment")
                    .append(">").append(TextHelper.formatAttribute("item.isaac_disaster.attribute.health", StatManager.getHealthBonus()))
                    .withStyle(style -> style.withColor(ColorManager.UNCOMMON_GREEN)));
        }else {
            StatManager.modifyMaxHealth(player, -1);
            player.sendSystemMessage(Component.translatable("item.isaac_disaster.experimental_treatment")
                    .append(">").append(TextHelper.formatAttribute("item.isaac_disaster.attribute.health_remove", StatManager.getHealthBonus()))
                    .withStyle(style -> style.withColor(ColorManager.LEGEND_RED)));
        }

        // damage
        if (random.nextDouble() < 0.6){
            StatManager.modifyDamageAdder(player, 1);
            player.sendSystemMessage(Component.translatable("item.isaac_disaster.experimental_treatment")
                    .append(">").append(TextHelper.formatAttribute("item.isaac_disaster.attribute.damage", StatManager.getDamageBonus()))
                    .withStyle(style -> style.withColor(ColorManager.UNCOMMON_GREEN)));
        }else {
            StatManager.modifyDamageAdder(player, -1);
            player.sendSystemMessage(Component.translatable("item.isaac_disaster.experimental_treatment")
                    .append(">").append(TextHelper.formatAttribute("item.isaac_disaster.attribute.damage_remove", StatManager.getDamageBonus()))
                    .withStyle(style -> style.withColor(ColorManager.LEGEND_RED)));
        }

        // tears correction
        if (random.nextDouble() < 0.6){
            StatManager.modifyTearsCorrectionAdder(player, 1);
            player.sendSystemMessage(Component.translatable("item.isaac_disaster.experimental_treatment")
                    .append(">").append(TextHelper.formatAttribute("item.isaac_disaster.attribute.tears_correction", StatManager.getTearsCorrectionBonus()))
                    .withStyle(style -> style.withColor(ColorManager.UNCOMMON_GREEN)));
        }else {
            StatManager.modifyTearsCorrectionAdder(player, -1);
            player.sendSystemMessage(Component.translatable("item.isaac_disaster.experimental_treatment")
                    .append(">").append(TextHelper.formatAttribute("item.isaac_disaster.attribute.tears_correction_remove", StatManager.getTearsCorrectionBonus()))
                    .withStyle(style -> style.withColor(ColorManager.LEGEND_RED)));
        }

        // speed
        if (random.nextDouble() < 0.6){
            StatManager.modifyMovementSpeedAdder(player, 1);
            player.sendSystemMessage(Component.translatable("item.isaac_disaster.experimental_treatment")
                    .append(">").append(TextHelper.formatAttribute("item.isaac_disaster.attribute.movement_speed", 1000*StatManager.getMovementSpeedBonus()))
                    .withStyle(style -> style.withColor(ColorManager.UNCOMMON_GREEN)));
        }else {
            StatManager.modifyMovementSpeedAdder(player, -1);
            player.sendSystemMessage(Component.translatable("item.isaac_disaster.experimental_treatment")
                    .append(">").append(TextHelper.formatAttribute("item.isaac_disaster.attribute.movement_speed_remove", 1000*StatManager.getMovementSpeedBonus()))
                    .withStyle(style -> style.withColor(ColorManager.LEGEND_RED)));
        }

        // luck
        if (random.nextDouble() < 0.6){
            StatManager.modifyLuckAdder(player, 1);
            player.sendSystemMessage(Component.translatable("item.isaac_disaster.experimental_treatment")
                    .append(">").append(TextHelper.formatAttribute("item.isaac_disaster.attribute.luck", StatManager.getLuckBonus()))
                    .withStyle(style -> style.withColor(ColorManager.UNCOMMON_GREEN)));
        }else {
            StatManager.modifyLuckAdder(player, -1);
            player.sendSystemMessage(Component.translatable("item.isaac_disaster.experimental_treatment")
                    .append(">").append(TextHelper.formatAttribute("item.isaac_disaster.attribute.luck_remove", StatManager.getLuckBonus()))
                    .withStyle(style -> style.withColor(ColorManager.LEGEND_RED)));
        }

        // shot speed
        if (random.nextDouble() < 0.6){
            StatManager.modifyBulletSpeedAdder(player, 1);
            player.sendSystemMessage(Component.translatable("item.isaac_disaster.experimental_treatment")
                    .append(">").append(TextHelper.formatAttribute("item.isaac_disaster.attribute.bullet_speed", StatManager.getBulletSpeedBonus()))
                    .withStyle(style -> style.withColor(ColorManager.UNCOMMON_GREEN)));
        }else {
            StatManager.modifyBulletSpeedAdder(player, -1);
            player.sendSystemMessage(Component.translatable("item.isaac_disaster.experimental_treatment")
                    .append(">").append(TextHelper.formatAttribute("item.isaac_disaster.attribute.bullet_speed_remove", StatManager.getBulletSpeedBonus()))
                    .withStyle(style -> style.withColor(ColorManager.LEGEND_RED)));
        }


    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), -1);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.EXPERIMENTAL_TREATMENT.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.experimental_treatment.lore.1"),
                Component.translatable("item.isaac_disaster.experimental_treatment.lore.2")
        );
    }

    @Override
    public List<Component> getSynergyDescription(){
        return SetManager.getInstance().getSetFromId(SetId.SPUN.getId()).getSynergyDescription();
    }

    @Override
    public List<Component> getExplain(){
        return SetManager.getInstance().getSetFromId(SetId.SPUN.getId()).getExplain();
    }
}
