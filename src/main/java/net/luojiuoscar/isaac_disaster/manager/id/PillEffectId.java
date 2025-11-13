package net.luojiuoscar.isaac_disaster.manager.id;

public enum PillEffectId {
    I_FOUND_PILLS,
    BALLS_OF_STEEL,
    FULL_HEALTH,
    HEALTH_DOWN,
    HEALTH_UP,
    BAD_GAS,
    BAD_TRIP,
    EXPLOSIVE_DIARRHEA,
    PUBERTY,
    RANGE_UP,
    RANGE_DOWN,
    SPEED_UP,
    SPEED_DOWN,
    TEARS_UP,
    TEARS_DOWN,
    LUCK_UP,
    LUCK_DOWN,
    TELEPILLS,
    HEMATEMESIS,
    PARALYSIS,
    I_CAN_SEE_FOREVER,
    PHEROMONES,
    AMNESIA,
    LEMON_PARTY,
    PERCS,
    ADDICTED,
    ONE_MAKES_YOU_LARGER,
    ONE_MAKES_YOU_SMALL,
    POWER_PILL,
    RETRO_VISION,
    FRIENDS_TILL_THE_END,
    SOMETHINGS_WRONG,
    IM_DROWSY,
    IM_EXCITED,
    FEELS_LIKE_IM_WALKING_ON_SUNSHINE,
    SHOT_SPEED_DOWN,
    SHOT_SPEED_UP,
    EXPERIMENTAL_PILL,
    GULP,
    VURP;


    private final int id;

    // 构造方法：自动生成递增的ID
    PillEffectId() {
        this.id = ordinal();
    }

    // 获取ID的方法
    public int getId() {
        return id;
    }
}
