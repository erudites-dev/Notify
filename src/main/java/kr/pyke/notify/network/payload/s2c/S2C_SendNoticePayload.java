package kr.pyke.notify.network.payload.s2c;

import kr.pyke.notify.Notify;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record S2C_SendNoticePayload(Component message, int expirationTime) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2C_SendNoticePayload> ID = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Notify.MOD_ID, "s2c_notice_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_SendNoticePayload> STREAM_CODEC = StreamCodec.composite(
        ComponentSerialization.STREAM_CODEC, S2C_SendNoticePayload::message,
        ByteBufCodecs.VAR_INT, S2C_SendNoticePayload::expirationTime,
        S2C_SendNoticePayload::new
    );

    @Override public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return ID; }
}