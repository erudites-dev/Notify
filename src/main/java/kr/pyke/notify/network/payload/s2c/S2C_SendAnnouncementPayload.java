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

public record S2C_SendAnnouncementPayload(int color, Component message) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2C_SendAnnouncementPayload> ID = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Notify.MOD_ID, "s2c_announcement_send"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_SendAnnouncementPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, S2C_SendAnnouncementPayload::color,
        ComponentSerialization.STREAM_CODEC, S2C_SendAnnouncementPayload::message,
        S2C_SendAnnouncementPayload::new
    );

    @Override public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return ID; }
}