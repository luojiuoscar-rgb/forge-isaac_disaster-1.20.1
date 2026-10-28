package net.luojiuoscar.isaac_disaster.manager.id;

public enum SetId {
    SPUN,
    ADULT,
    FUN_GUY,
    BOOK,
    MOM,
    CAT,
    SERAPHIM,
    OH_CRAP,
    BEELZEBUB,
    SPIDER_BABY,
    CONJOINED;


    private final int id;

    SetId() {
        this.id = ordinal();
    }

    public int getId() {
        return id;
    }
}
