package kr.pyke.notify.network.payload.c2s;

import kr.pyke.notify.Notify;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record C2S_HelpRequestPurgePayload() implements CustomPacketPayload {
    public static final Type<C2S_HelpRequestPurgePayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Notify.MOD_ID, "c2s_help_purge"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2S_HelpRequestPurgePayload> STREAM_CODEC = StreamCodec.unit(new C2S_HelpRequestPurgePayload());

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
}