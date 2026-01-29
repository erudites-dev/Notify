package kr.pyke.notify.util.helper;

import io.netty.buffer.ByteBuf;
import kr.pyke.notify.util.constants.HELP_STATUS;
import kr.pyke.util.constants.COLOR;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class NotifyHelper {
    public static final StreamCodec<ByteBuf, HELP_STATUS> STATUS_CODEC = ByteBufCodecs.VAR_INT.map(id -> HELP_STATUS.values()[id], HELP_STATUS::ordinal);

    public static <E extends Enum<E>> E parseEnum(String s, Class<E> cls) {
        try { return Enum.valueOf(cls, s.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { return null; }
    }

    public static int parseColor(COLOR color) {
        switch(color) {
            case RED -> { return 0xFF5555; }
            case GOLD -> { return 0xFFAA00; }
            case YELLOW -> { return 0xFFFF55; }
            case LIME -> { return 0x55FF55; }
            case AQUA -> { return 0x55FFFF; }
            case DARK_AQUA -> { return 0x00AAAA; }
            case BLUE -> { return 0x5555FF; }
            case LIGHT_PURPLE -> { return 0xFF55FF; }
            case PURPLE -> { return 0xAA00AA; }
        }

        return 0xFFFFFF;
    }

    public static Component currentNameOf(MinecraftServer server, UUID uuid) {
        ServerPlayer serverPlayer = server.getPlayerList().getPlayer(uuid);

        var cached = Objects.requireNonNull(server.getProfileCache()).get(uuid);
        if (cached.isPresent()) {
            String name = cached.get().getName();
            if (null != name && !name.isBlank()) { return Component.literal(name); }
        }

        return Component.literal("Unknown");
    }
}
