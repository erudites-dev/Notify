package kr.pyke.notify.network.payload.s2c;

import kr.pyke.notify.Notify;
import kr.pyke.notify.data.request.HelpRequest;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record S2C_HelpRequestAppendPayload(HelpRequest request) implements CustomPacketPayload {
    public static final Type<S2C_HelpRequestAppendPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Notify.MOD_ID, "s2c_help_append"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_HelpRequestAppendPayload> STREAM_CODEC = StreamCodec.composite(
        HelpRequest.STREAM_CODEC, S2C_HelpRequestAppendPayload::request,
        S2C_HelpRequestAppendPayload::new
    );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
}