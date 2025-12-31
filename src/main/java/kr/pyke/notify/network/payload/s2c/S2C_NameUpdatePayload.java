package kr.pyke.notify.network.payload.s2c;

import kr.pyke.notify.Notify;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record S2C_NameUpdatePayload(UUID uuid, Component name) implements CustomPacketPayload {
    public static final Type<S2C_NameUpdatePayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Notify.MOD_ID, "s2c_name_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_NameUpdatePayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, S2C_NameUpdatePayload::uuid,
        ComponentSerialization.STREAM_CODEC, S2C_NameUpdatePayload::name,
        S2C_NameUpdatePayload::new
    );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
}