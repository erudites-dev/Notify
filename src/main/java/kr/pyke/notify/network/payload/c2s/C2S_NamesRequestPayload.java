package kr.pyke.notify.network.payload.c2s;

import io.netty.buffer.ByteBuf;
import kr.pyke.notify.Notify;
import kr.pyke.notify.network.payload.s2c.S2C_NamesCachePayload;
import kr.pyke.notify.util.helper.NotifyHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public record C2S_NamesRequestPayload(List<UUID> uuids) implements CustomPacketPayload {
    public static final Type<C2S_NamesRequestPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Notify.MOD_ID, "c2s_names_request"));

    public static final StreamCodec<ByteBuf, C2S_NamesRequestPayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()), C2S_NamesRequestPayload::uuids,
        C2S_NamesRequestPayload::new
    );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(C2S_NamesRequestPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            var nameMap = new HashMap<UUID, Component>();
            for (UUID u : payload.uuids()) {
                nameMap.put(u, NotifyHelper.currentNameOf(context.server(), u));
            }
            ServerPlayNetworking.send(context.player(), new S2C_NamesCachePayload(nameMap));
        });
    }
}