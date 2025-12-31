package kr.pyke.notify.util.helper;

import io.netty.buffer.ByteBuf;
import kr.pyke.notify.Notify;
import kr.pyke.notify.network.payload.s2c.S2C_SendAnnouncementPayload;
import kr.pyke.notify.network.payload.s2c.S2C_SendColorChatBox;
import kr.pyke.notify.util.constants.CHAT_BG_COLOR;
import kr.pyke.notify.util.constants.HELP_STATUS;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class NotifyHelper {
    public static final StreamCodec<ByteBuf, HELP_STATUS> STATUS_CODEC = ByteBufCodecs.VAR_INT.map(id -> HELP_STATUS.values()[id], HELP_STATUS::ordinal);

    public static <E extends Enum<E>> E parseEnum(String s, Class<E> cls) {
        try { return Enum.valueOf(cls, s.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { return null; }
    }

    public static int parseColor(CHAT_BG_COLOR color) {
        switch(color) {
            case RED -> { return 0xFF5555; }
            case GOLD -> { return 0xFFAA00; }
            case YELLOW -> { return 0xFFFF55; }
            case LIME -> { return 0x55FF55; }
            case AQUA -> { return 0x55FFFF; }
            case DARK_AQUA -> { return 0x00AAAA; }
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

    public static void sendSystemMessage(List<ServerPlayer> players, CHAT_BG_COLOR color, Component message) {
        int colorRGB = NotifyHelper.parseColor(color);
        Component component = Notify.SYSTEM_PREFIX.copy().append(message.copy().withStyle(ChatFormatting.WHITE));

        S2C_SendColorChatBox packet = new S2C_SendColorChatBox(colorRGB, component);

        for (ServerPlayer serverPlayer : players) { ServerPlayNetworking.send(serverPlayer, packet); }
    }
}
