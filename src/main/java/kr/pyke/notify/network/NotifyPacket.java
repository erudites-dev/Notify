package kr.pyke.notify.network;

import kr.pyke.notify.network.payload.c2s.*;
import kr.pyke.notify.network.payload.s2c.*;
import kr.pyke.notify.util.constants.HELP_STATUS;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class NotifyPacket {
    public static void registerCodec() {
        // S2C (Server → Client)
        PayloadTypeRegistry.playS2C().register(S2C_SendNoticePayload.ID, S2C_SendNoticePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_HelpRequestInitializePayload.ID, S2C_HelpRequestInitializePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_HelpRequestAppendPayload.ID, S2C_HelpRequestAppendPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_HelpRequestUpdatePayload.ID, S2C_HelpRequestUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_NamesCachePayload.ID, S2C_NamesCachePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_NameUpdatePayload.ID, S2C_NameUpdatePayload.STREAM_CODEC);

        // C2S (Client → Server)
        PayloadTypeRegistry.playC2S().register(C2S_HelpRequestChangeStatusPayload.ID, C2S_HelpRequestChangeStatusPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_HelpRequestPurgePayload.ID, C2S_HelpRequestPurgePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_HelpRequestTeleportPayload.ID, C2S_HelpRequestTeleportPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_HelpRequestInitializePayload.ID, C2S_HelpRequestInitializePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_NamesRequestPayload.ID, C2S_NamesRequestPayload.STREAM_CODEC);
    }

    public static void registerServer() {
        // C2S_HelpRequestChangeStatusPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_HelpRequestChangeStatusPayload.ID, C2S_HelpRequestChangeStatusPayload::handle);
        // C2S_HelpRequestInitializePayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_HelpRequestInitializePayload.ID, C2S_HelpRequestInitializePayload::handle);
        // C2S_HelpRequestPurgePayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_HelpRequestPurgePayload.ID, C2S_HelpRequestPurgePayload::handle);
        // C2S_HelpRequestTeleportPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_HelpRequestTeleportPayload.ID, C2S_HelpRequestTeleportPayload::handle);
        // C2S_NamesRequestPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_NamesRequestPayload.ID, C2S_NamesRequestPayload::handle);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        // S2C_SendNoticePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_SendNoticePayload.ID, S2C_SendNoticePayload::handle);
        // S2C_HelpRequestInitializePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_HelpRequestInitializePayload.ID, S2C_HelpRequestInitializePayload::handle);
        // S2C_HelpRequestAppendPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_HelpRequestAppendPayload.ID, S2C_HelpRequestAppendPayload::handle);
        // S2C_HelpRequestUpdatePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_HelpRequestUpdatePayload.ID, S2C_HelpRequestUpdatePayload::handle);
        // S2C_NamesCachePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_NamesCachePayload.ID, S2C_NamesCachePayload::handle);
        // S2C_NameUpdatePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_NameUpdatePayload.ID, S2C_NameUpdatePayload::handle);
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
