package net.luojiuoscar.isaac_disaster.manager.id;

public enum SetId {
    SPUN,
    ADULT,
    FUN_GUY,
    BOOK,
    MOM;


    private final int id;

    SetId() {
        this.id = ordinal();
    }

    public int getId() {
        return id;
    }
}
