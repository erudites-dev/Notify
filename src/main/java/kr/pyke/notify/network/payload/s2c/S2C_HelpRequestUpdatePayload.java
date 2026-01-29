package kr.pyke.notify.network.payload.s2c;

import kr.pyke.notify.Notify;
import kr.pyke.notify.client.state.HelpClientState;
import kr.pyke.notify.data.request.HelpRequest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record S2C_HelpRequestUpdatePayload(HelpRequest request) implements CustomPacketPayload {
    public static final Type<S2C_HelpRequestUpdatePayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Notify.MOD_ID, "s2c_help_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_HelpRequestUpdatePayload> STREAM_CODEC = StreamCodec.composite(
        HelpRequest.STREAM_CODEC, S2C_HelpRequestUpdatePayload::request,
        S2C_HelpRequestUpdatePayload::new
    );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(S2C_HelpRequestUpdatePayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> HelpClientState.onUpdated(payload.request()));
    }
}