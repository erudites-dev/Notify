package kr.pyke.notify.network.payload.c2s;

import kr.pyke.notify.Notify;
import kr.pyke.notify.util.helper.NotifyHelper;
import kr.pyke.notify.util.constants.HELP_STATUS;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record C2S_HelpRequestChangeStatusPayload(UUID requestId, HELP_STATUS status) implements CustomPacketPayload {
    public static final Type<C2S_HelpRequestChangeStatusPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Notify.MOD_ID, "c2s_help_change_status"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2S_HelpRequestChangeStatusPayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC.cast(), C2S_HelpRequestChangeStatusPayload::requestId,
        NotifyHelper.STATUS_CODEC, C2S_HelpRequestChangeStatusPayload::status,
        C2S_HelpRequestChangeStatusPayload::new
    );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
}
