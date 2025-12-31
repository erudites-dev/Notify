package kr.pyke.notify.network.payload.s2c;

import kr.pyke.notify.Notify;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public record S2C_NamesCachePayload(HashMap<UUID, Component> names) implements CustomPacketPayload {
    public static final Type<S2C_NamesCachePayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Notify.MOD_ID, "s2c_names_cache"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_NamesCachePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ComponentSerialization.STREAM_CODEC), S2C_NamesCachePayload::names,
        S2C_NamesCachePayload::new
    );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
}