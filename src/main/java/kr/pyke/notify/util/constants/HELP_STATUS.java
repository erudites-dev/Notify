package kr.pyke.notify.util.constants;

public enum HELP_STATUS {
    PENDING(0),
    PROCESSING(1),
    RESOLVED(2);

    HELP_STATUS(int ID) { this.ID = ID; }

    private final int ID;

    public int getID() { return ID; }
    public static HELP_STATUS byID(int ID) {
        for (HELP_STATUS color : HELP_STATUS.values()) {
            if (color.ID == ID) { return color; }
        }

        return PENDING;
    }
}
