package kr.pyke.notify.util.constants;

public enum CHAT_BG_COLOR {
    RED(0),
    GOLD(1),
    YELLOW(2),
    LIME(3),
    AQUA(4),
    DARK_AQUA(5),
    BLUE(6),
    LIGHT_PURPLE(7),
    PURPLE(8);

    CHAT_BG_COLOR(int ID) { this.ID = ID; }

    private final int ID;

    public int getID() { return ID; }
    public static CHAT_BG_COLOR byID(int ID) {
        for (CHAT_BG_COLOR color : CHAT_BG_COLOR.values()) {
            if (color.ID == ID) { return color; }
        }

        return RED;
    }
}
