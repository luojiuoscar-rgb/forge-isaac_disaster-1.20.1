package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

final class LuckTriggerChance {
    private LuckTriggerChance() {
    }

    static double lokisHorns(double luck) {
        return Math.min(1.0, luck * 0.05 + 0.25);
    }

    static double momsEyeshadow(double luck) {
        return 1.0 / Math.max(1.0, 10.0 - luck / 3.0);
    }

    static double commonCold(double luck) {
        return 1.0 / Math.max(1.0, 4.0 - luck / 4.0);
    }

    static double ironBar(double luck) {
        return 1.0 / Math.max(1.0, 10.0 - luck / 3.0);
    }
}
