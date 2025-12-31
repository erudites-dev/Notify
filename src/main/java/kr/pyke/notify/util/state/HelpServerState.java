package kr.pyke.notify.util.state;

import kr.pyke.notify.data.request.HelpRequest;
import kr.pyke.notify.network.payload.s2c.S2C_HelpRequestAppendPayload;
import kr.pyke.notify.network.payload.s2c.S2C_HelpRequestInitializePayload;
import kr.pyke.notify.network.payload.s2c.S2C_HelpRequestUpdatePayload;
import kr.pyke.notify.util.constants.HELP_STATUS;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class HelpServerState {
    private static final List<HelpRequest> LIST = new CopyOnWriteArrayList<>();

    public static HelpRequest add(ServerPlayer sender, String msg) {
        HelpRequest request = new HelpRequest(UUID.randomUUID(), sender.getUUID(), msg, System.currentTimeMillis(), HELP_STATUS.PENDING);
        LIST.add(request);
        broadcastAppend(sender.server, request);
        return request;
    }

    public static void setStatus(MinecraftServer server, ServerPlayer operator, UUID uuid, HELP_STATUS status) {
        for (HelpRequest request : LIST) {
            if (!request.requestUUID.equals(uuid)) { continue; }

            request.status = status;
            request.handlerUUID = status != HELP_STATUS.PENDING ? operator.getUUID() : null;
            broadcastUpdate(server, request);
            return;
        }
    }

    public static void purgeResolved(MinecraftServer server) {
        boolean changed = LIST.removeIf(req -> req.status == HELP_STATUS.RESOLVED);
        if (!changed) return;

        broadcastFullSync(server);
    }

    public static List<HelpRequest> snapshot() { return List.copyOf(LIST); }

    private static void broadcastAppend(MinecraftServer server, HelpRequest request) {
        var payload = new S2C_HelpRequestAppendPayload(request);
        PlayerList playerlist = server.getPlayerList();

        for (ServerPlayer player : playerlist.getPlayers()) {
            if (playerlist.isOp(player.getGameProfile())) {
                ServerPlayNetworking.send(player, payload); //
            }
        }
    }

    private static void broadcastUpdate(MinecraftServer server, HelpRequest request) {
        var payload = new S2C_HelpRequestUpdatePayload(request);
        PlayerList playerlist = server.getPlayerList();

        for (ServerPlayer player : playerlist.getPlayers()) {
            if (playerlist.isOp(player.getGameProfile())) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    public static void broadcastFullSync(MinecraftServer server) {
        var payload = new S2C_HelpRequestInitializePayload(List.copyOf(LIST));
        PlayerList playerlist = server.getPlayerList();

        for (ServerPlayer player : playerlist.getPlayers()) {
            if (playerlist.isOp(player.getGameProfile())) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}