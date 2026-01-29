package kr.pyke.notify.network.payload.c2s;

import kr.pyke.notify.Notify;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record C2S_HelpRequestTeleportPayload(UUID targetUuid) implements CustomPacketPayload {
    public static final Type<C2S_HelpRequestTeleportPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Notify.MOD_ID, "c2s_help_tp_sender"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2S_HelpRequestTeleportPayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC.cast(), C2S_HelpRequestTeleportPayload::targetUuid,
        C2S_HelpRequestTeleportPayload::new
    );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(C2S_HelpRequestTeleportPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            if (!context.server().getPlayerList().isOp(context.player().getGameProfile())) { return; }

            ServerPlayer target = context.server().getPlayerList().getPlayer(payload.targetUuid());
            if (target == null) { return; }

            ServerPlayer player = context.player();
            if (player.isPassenger()) { player.stopRiding(); }
            player.fallDistance = 0f;
            player.teleportTo(target.serverLevel(), target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
        });
    }
}