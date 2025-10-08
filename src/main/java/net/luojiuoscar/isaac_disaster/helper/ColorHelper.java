package net.luojiuoscar.isaac_disaster.helper;

import java.util.List;

public class ColorHelper {

    /**
     * 多层滤镜混合
     * @param base 子弹基础颜色 (int RGB)
     * @param filters 滤镜颜色列表
     * @return 最终混合出的滤镜颜色
     */
    public static int blendFilters(int base, List<Integer> filters) {
        if (filters == null || filters.isEmpty()) {
            return base;
        }

        float br = ((base >> 16) & 0xFF) / 255f;
        float bg = ((base >> 8) & 0xFF) / 255f;
        float bb = (base & 0xFF) / 255f;

        float r = br, g = bg, b = bb;

        // 根据滤镜数量调整初始权重，防止过曝
        // 滤镜越多，每层影响越弱
        float baseAlpha = 0.45f / (float)Math.sqrt(filters.size());
        float decay = 0.85f;

        float currentWeight = baseAlpha;

        for (int filter : filters) {
            float fr = ((filter >> 16) & 0xFF) / 255f;
            float fg = ((filter >> 8) & 0xFF) / 255f;
            float fb = (filter & 0xFF) / 255f;

            // 透明混合：旧色 * (1 - α) + 新色 * α
            r = r * (1 - currentWeight) + fr * currentWeight;
            g = g * (1 - currentWeight) + fg * currentWeight;
            b = b * (1 - currentWeight) + fb * currentWeight;

            // 后加入的滤镜稍微更强
            currentWeight *= decay;
        }

        return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

    /**
     * 将最终滤镜颜色与子弹颜色混合
     * @param base 子弹原始颜色
     * @param filter 最终滤镜颜色
     * @param strength 滤镜强度（0.0~1.0）
     * @return 混合后的颜色
     */
    public static int blendColor(int base, int filter, float strength) {
        strength = Math.max(0, Math.min(1, strength)); // 限制范围

        float br = ((base >> 16) & 0xFF) / 255f;
        float bg = ((base >> 8) & 0xFF) / 255f;
        float bb = (base & 0xFF) / 255f;

        float fr = ((filter >> 16) & 0xFF) / 255f;
        float fg = ((filter >> 8) & 0xFF) / 255f;
        float fb = (filter & 0xFF) / 255f;

        // 简单线性插值
        float r = br * (1 - strength) + fr * strength;
        float g = bg * (1 - strength) + fg * strength;
        float b = bb * (1 - strength) + fb * strength;

        // 防止过曝：如果亮度超出1，则整体按比例缩放
        float max = Math.max(r, Math.max(g, b));
        if (max > 1.0f) {
            r /= max;
            g /= max;
            b /= max;
        }

        return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }
}
