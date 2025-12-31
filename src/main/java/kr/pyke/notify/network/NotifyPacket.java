package kr.pyke.notify.network;

import kr.pyke.notify.client.cache.NameClientCache;
import kr.pyke.notify.util.state.HelpServerState;
import kr.pyke.notify.client.state.HelpClientState;
import kr.pyke.notify.client.hud.NoticeHud;
import kr.pyke.notify.network.payload.c2s.*;
import kr.pyke.notify.network.payload.s2c.*;
import kr.pyke.notify.util.helper.NotifyHelper;
import kr.pyke.notify.util.constants.HELP_STATUS;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class NotifyPacket {
    public static void registerCodec() {
        // S2C (Server → Client)
        PayloadTypeRegistry.playS2C().register(S2C_SendNoticePayload.ID, S2C_SendNoticePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_SendAnnouncementPayload.ID, S2C_SendAnnouncementPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_HelpRequestInitializePayload.ID, S2C_HelpRequestInitializePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_HelpRequestAppendPayload.ID, S2C_HelpRequestAppendPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_HelpRequestUpdatePayload.ID, S2C_HelpRequestUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_NamesCachePayload.ID, S2C_NamesCachePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_NameUpdatePayload.ID, S2C_NameUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_SendColorChatBox.ID, S2C_SendColorChatBox.STREAM_CODEC);

        // C2S (Client → Server)
        PayloadTypeRegistry.playC2S().register(C2S_HelpRequestChangeStatusPayload.ID, C2S_HelpRequestChangeStatusPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_HelpRequestPurgePayload.ID, C2S_HelpRequestPurgePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_HelpRequestTeleportPayload.ID, C2S_HelpRequestTeleportPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_HelpRequestInitializePayload.ID, C2S_HelpRequestInitializePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_NamesRequestPayload.ID, C2S_NamesRequestPayload.STREAM_CODEC);
    }

    public static void registerServer() {
        // C2S_HelpRequestChangeStatusPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_HelpRequestChangeStatusPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (!context.server().getPlayerList().isOp(context.player().getGameProfile())) { return; }
                HelpServerState.setStatus(context.server(), context.player(), payload.requestId(), payload.status());
            });
        });

        // C2S_HelpRequestInitializePayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_HelpRequestInitializePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (!context.server().getPlayerList().isOp(context.player().getGameProfile())) { return; }
                HelpServerState.broadcastFullSync(context.server());
            });
        });

        // C2S_HelpRequestPurgePayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_HelpRequestPurgePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (!context.server().getPlayerList().isOp(context.player().getGameProfile())) { return; }
                HelpServerState.purgeResolved(context.server());
            });
        });

        // C2S_HelpRequestTeleportPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_HelpRequestTeleportPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (!context.server().getPlayerList().isOp(context.player().getGameProfile())) { return; }

                ServerPlayer target = context.server().getPlayerList().getPlayer(payload.targetUuid());
                if (target == null) { return; }

                ServerPlayer player = context.player();
                if (player.isPassenger()) { player.stopRiding(); }
                player.fallDistance = 0f;
                player.teleportTo(target.serverLevel(), target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
            });
        });

        // C2S_NamesRequestPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_NamesRequestPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var nameMap = new HashMap<UUID, Component>();
                for (UUID u : payload.uuids()) {
                    nameMap.put(u, NotifyHelper.currentNameOf(context.server(), u));
                }
                ServerPlayNetworking.send(context.player(), new S2C_NamesCachePayload(nameMap));
            });
        });
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        // S2C_SendNoticePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_SendNoticePayload.ID, (payload, context) -> {
            context.client().execute(() -> NoticeHud.updateMessage(payload.message(), payload.expirationTime()));
        });

        // S2C_SendAnnouncementPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_SendAnnouncementPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                GuiMessageTag noticeTag = new GuiMessageTag(payload.color(), null, null, "Announcement");

                Minecraft.getInstance().gui.getChat().addMessage(Component.empty(), null, noticeTag);
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal("ꅑ ").append(payload.message()), null, noticeTag);
                Minecraft.getInstance().gui.getChat().addMessage(Component.empty(), null, noticeTag);
            });
        });

        // S2C_SendColorChatBox
        ClientPlayNetworking.registerGlobalReceiver(S2C_SendColorChatBox.ID, (payload, context) -> {
            context.client().execute(() -> {
                GuiMessageTag noticeTag = new GuiMessageTag(payload.color(), null, null, "color_chatbox");

                Minecraft.getInstance().gui.getChat().addMessage(payload.message(), null, noticeTag);
            });
        });

        // S2C_HelpRequestInitializePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_HelpRequestInitializePayload.ID, (payload, context) -> {
            context.client().execute(() -> HelpClientState.onFullSync(payload.requests()));
        });

        // S2C_HelpRequestAppendPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_HelpRequestAppendPayload.ID, (payload, context) -> {
            context.client().execute(() -> HelpClientState.onAppended(payload.request()));
        });

        // S2C_HelpRequestUpdatePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_HelpRequestUpdatePayload.ID, (payload, context) -> {
            context.client().execute(() -> HelpClientState.onUpdated(payload.request()));
        });

        // S2C_NamesCachePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_NamesCachePayload.ID, (payload, context) -> {
            context.client().execute(() -> NameClientCache.putAll(payload.names()));
        });

        // S2C_NameUpdatePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_NameUpdatePayload.ID, (payload, context) -> {
            context.client().execute(() -> NameClientCache.put(payload.uuid(), payload.name()));
        });
    }

    private NotifyPacket() { }

    public static void requestInitSync() { ClientPlayNetworking.send(new C2S_HelpRequestInitializePayload());}

    public static void requestSetStatus(UUID uuid, HELP_STATUS status) { ClientPlayNetworking.send(new C2S_HelpRequestChangeStatusPayload(uuid, status)); }

    public static void requestPurgeResolved() { ClientPlayNetworking.send(new C2S_HelpRequestPurgePayload()); }

    public static void requestTeleportTo(UUID targetUuid) { ClientPlayNetworking.send(new C2S_HelpRequestTeleportPayload(targetUuid)); }

    public static void requestNames(Collection<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) { return; }
        ClientPlayNetworking.send(new C2S_NamesRequestPayload(new ArrayList<>(uuids)));
    }
}
