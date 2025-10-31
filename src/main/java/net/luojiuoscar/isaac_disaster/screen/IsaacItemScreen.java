package net.luojiuoscar.isaac_disaster.screen;

import net.luojiuoscar.isaac_disaster.client.ModKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class IsaacItemScreen extends Screen {

    private final Minecraft mc = Minecraft.getInstance();

    private final List<ItemStack> passiveItems = new ArrayList<>();
    private final List<ItemStack> trinketItems = new ArrayList<>();

    // 滚动与缩放参数
    private float scrollOffset = 0f;
    private int slotSize = 32;
    private static final int MIN_SLOT_SIZE = 16;
    private static final int MAX_SLOT_SIZE = 64;

    // 拖动状态
    private boolean draggingScrollbar = false;
    private int lastMouseY = 0;

    // 动态滚动最大值
    private float currentMaxScroll = 0f;

    public IsaacItemScreen(List<ItemStack> passiveList, List<ItemStack> trinketList) {
        super(Component.translatable("screen.isaac_disaster.passive_item_title"));
        passiveItems.addAll(passiveList);
        trinketItems.addAll(trinketList);

        if (passiveItems.isEmpty()) passiveItems.add(new ItemStack(Items.BARRIER));
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int startY = getStartY();
        float titleScale = slotSize / 30f;

        // 渲染被动道具区块
        int passiveEndY = renderSection(guiGraphics, mouseX, mouseY,
                passiveItems, startY, titleScale,
                Component.translatable("screen.isaac_disaster.passive_item_title"));

        int lastSectionEndY = passiveEndY;

        // 渲染饰品区块
        if (!trinketItems.isEmpty()) {
            int trinketStartY = passiveEndY + slotSize; // 间隔一行
            lastSectionEndY = renderSection(guiGraphics, mouseX, mouseY,
                    trinketItems, trinketStartY, titleScale,
                    Component.translatable("screen.isaac_disaster.trinket_title"));
        }

        // 根据渲染完的总高度计算 maxScroll
        int visibleHeight = this.height - getStartY() - 10;
        currentMaxScroll = Math.max(0, lastSectionEndY - getStartY() - visibleHeight);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * 渲染一个带标题的物品区块
     * 返回：这个区块使用到的绝对高度（供下一个区块或滚动计算使用）
     */
    private int renderSection(GuiGraphics guiGraphics, int mouseX, int mouseY,
                              List<ItemStack> items, int startY, float titleScale, Component title) {
        int titleHeight = (int) (20 * titleScale);

        // 绘制标题
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(10, startY - (int) scrollOffset, 0);
        guiGraphics.pose().scale(titleScale, titleScale, 1f);
        guiGraphics.drawString(mc.font, title, 0, 0, 0xFFFFFF, false);
        guiGraphics.pose().popPose();

        // 绘制物品
        int newY = startY + titleHeight;
        int newHeight = renderItemList(guiGraphics, mouseX, mouseY, items, newY);

        // 绘制滚动条
        int totalRowsAll = getTotalRowsCombined(); // 这里只是为了兼容旧逻辑
        int visibleRowsAll = Math.max(1, (this.height - getStartY() - 10) / slotSize);
        if (totalRowsAll > visibleRowsAll) {
            renderScrollBar(guiGraphics, getStartY(), totalRowsAll, visibleRowsAll);
        }

        return newHeight;
    }

    /**
     * 渲染物品列表
     * 返回绝对使用高度（不带 scrollOffset）
     */
    private int renderItemList(GuiGraphics guiGraphics, int mouseX, int mouseY,
                               List<ItemStack> items, int itemsStartY) {
        int startX = 10;
        int usableWidth = this.width - startX * 2;
        int itemsPerRow = Math.max(1, usableWidth / slotSize);
        float scale = slotSize / 16f;

        int totalRows = (items.size() + itemsPerRow - 1) / itemsPerRow;

        int visibleTop = getStartY();
        int visibleBottom = this.height - 10;

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            int row = i / itemsPerRow;
            int col = i % itemsPerRow;

            int x = startX + col * slotSize;
            int displayedY = itemsStartY - (int) scrollOffset + row * slotSize;
            int absoluteY = itemsStartY + row * slotSize;

            // 可见性检测
            if (displayedY + slotSize < visibleTop || displayedY > visibleBottom) continue;

            renderScaledItem(guiGraphics, stack, x, displayedY, scale);

            if (mouseX >= x && mouseX < x + slotSize && mouseY >= displayedY && mouseY < displayedY + slotSize) {
                if (mc.player != null) {
                    if (stack.isEmpty() || stack.getItem() == Items.BARRIER)
                        guiGraphics.renderTooltip(mc.font, Component.translatable("screen.isaac_disaster.empty"), mouseX, mouseY);
                    else
                        guiGraphics.renderTooltip(mc.font, stack, mouseX, mouseY);
                }
            }
        }

        return itemsStartY + totalRows * slotSize;
    }

    private void renderScaledItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.pose().translate((float) x / scale, (float) y / scale, 0f);
        guiGraphics.renderItem(stack, 0, 0);
        guiGraphics.renderItemDecorations(mc.font, stack, 0, 0);
        guiGraphics.pose().popPose();
    }

    private void renderScrollBar(GuiGraphics guiGraphics, int startY, int totalRows, int visibleRows) {
        int barX1 = this.width - 12;
        int barX2 = this.width - 4;
        int barHeight = this.height - startY - 10;

        // 用动态 maxScroll
        float maxScroll = currentMaxScroll;
        int sliderHeight = Math.max(10, (int) (barHeight * ((float) visibleRows / totalRows)));
        int sliderY = (barHeight - sliderHeight) > 0
                ? startY + (int) ((barHeight - sliderHeight) * (scrollOffset / maxScroll))
                : startY;

        guiGraphics.fill(barX1, startY, barX2, startY + barHeight, 0xFF444444);
        guiGraphics.fill(barX1, sliderY, barX2, sliderY + sliderHeight,
                draggingScrollbar ? 0xFFFFFFFF : 0xFFCCCCCC);
    }

    private int getTotalRowsCombined() {
        int usableWidth = this.width - 20;
        int perRow = Math.max(1, usableWidth / slotSize);
        int passiveRows = (passiveItems.size() + perRow - 1) / perRow;
        int trinketRows = trinketItems.isEmpty() ? 0 : (trinketItems.size() + perRow - 1) / perRow;
        int spacingRows = trinketItems.isEmpty() ? 0 : 1;
        return passiveRows + spacingRows + trinketRows;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (hasAltDown() || hasShiftDown()) {
            int oldSlotSize = slotSize;
            slotSize += delta > 0 ? 2 : -2;
            slotSize = Math.max(MIN_SLOT_SIZE, Math.min(MAX_SLOT_SIZE, slotSize));
            return true;
        }

        scrollOffset -= (float) (delta * 10);
        scrollOffset = Math.max(0, Math.min(scrollOffset, currentMaxScroll));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int startY = getStartY();
        int barX1 = this.width - 12, barX2 = this.width - 4;
        int barHeight = this.height - startY - 10;
        int visibleRows = Math.max(1, barHeight / slotSize);

        int sliderHeight = Math.max(10, (int) (barHeight * ((float) visibleRows / getTotalRowsCombined())));
        int sliderY = (barHeight - sliderHeight) > 0
                ? startY + (int) ((barHeight - sliderHeight) * (scrollOffset / currentMaxScroll))
                : startY;

        if (mouseX >= barX1 && mouseX <= barX2) {
            if (mouseY >= sliderY && mouseY <= sliderY + sliderHeight) {
                draggingScrollbar = true;
                lastMouseY = (int) mouseY;
                return true;
            } else if (mouseY < sliderY) {
                scrollOffset = Math.max(0, scrollOffset - visibleRows * slotSize);
                return true;
            } else {
                scrollOffset = Math.min(currentMaxScroll, scrollOffset + visibleRows * slotSize);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            int startY = getStartY();
            int barHeight = this.height - startY - 10;
            int visibleRows = Math.max(1, barHeight / slotSize);

            int sliderHeight = Math.max(10, (int) (barHeight * ((float) visibleRows / getTotalRowsCombined())));
            int deltaY = (int) mouseY - lastMouseY;
            lastMouseY = (int) mouseY;

            float scrollRatio = (barHeight - sliderHeight) > 0 ? (float) deltaY / (float) (barHeight - sliderHeight) : 0f;
            scrollOffset += scrollRatio * currentMaxScroll;
            scrollOffset = Math.max(0, Math.min(scrollOffset, currentMaxScroll));
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private int getStartY() {
        return 30;
    }

    // 关闭页面
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.mc.options.keyInventory.matches(keyCode, scanCode)
                || ModKeyMappings.OPEN_ISAAC_ITEM_SCREEN.matches(keyCode, scanCode))
        {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
