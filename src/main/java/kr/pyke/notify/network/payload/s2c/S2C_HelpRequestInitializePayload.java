package kr.pyke.notify.network.payload.s2c;

import kr.pyke.notify.Notify;
import kr.pyke.notify.data.request.HelpRequest;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record S2C_HelpRequestInitializePayload(List<HelpRequest> requests) implements CustomPacketPayload {
    public static final Type<S2C_HelpRequestInitializePayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Notify.MOD_ID, "s2c_help_init"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_HelpRequestInitializePayload> STREAM_CODEC = StreamCodec.composite(
        HelpRequest.STREAM_CODEC.apply(ByteBufCodecs.list()), S2C_HelpRequestInitializePayload::requests,
        S2C_HelpRequestInitializePayload::new
    );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
}
