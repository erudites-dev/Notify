package kr.pyke.notify.data.request;

import kr.pyke.notify.util.constants.HELP_STATUS;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

public class HelpRequest {
    public final UUID requestUUID;
    public final UUID senderUUID;
    public final String message;
    public final long createAt;

    public HELP_STATUS status;
    public UUID handlerUUID;

    public HelpRequest(UUID requestUUID, UUID senderUUID, String message, long createAt, HELP_STATUS status) {
        this.requestUUID = requestUUID;
        this.senderUUID = senderUUID;
        this.message = message;
        this.createAt = createAt;
        this.status = status;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, HelpRequest> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, req -> req.requestUUID,
        UUIDUtil.STREAM_CODEC, req -> req.senderUUID,
        ByteBufCodecs.STRING_UTF8, req -> req.message,
        ByteBufCodecs.VAR_LONG, req -> req.createAt,
        ByteBufCodecs.VAR_INT.map(id -> HELP_STATUS.values()[id], HELP_STATUS::ordinal).cast(), req -> req.status,
        ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), req -> Optional.ofNullable(req.handlerUUID),
        (id, sender, msg, time, stat, handler) -> new HelpRequest(id, sender, msg, time, stat)
    );
}