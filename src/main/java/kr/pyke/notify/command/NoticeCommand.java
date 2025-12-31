package kr.pyke.notify.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import kr.pyke.notify.Notify;
import kr.pyke.notify.network.payload.s2c.S2C_SendNoticePayload;
import kr.pyke.notify.util.constants.CHAT_BG_COLOR;
import kr.pyke.notify.util.helper.NotifyHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class NoticeCommand {
    private NoticeCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        var commandNode = Commands.argument("duration", IntegerArgumentType.integer(1))
            .then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(NoticeCommand::sendNotice)
            );

        dispatcher.register(Commands.literal("알림")
            .requires(source -> source.hasPermission(2))
            .then(commandNode)
        );

        dispatcher.register(Commands.literal("notice")
            .requires(source -> source.hasPermission(2))
            .then(commandNode)
        );
    }

    private static int sendNotice(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        int duration = IntegerArgumentType.getInteger(ctx, "duration");
        String rawMessage = StringArgumentType.getString(ctx, "message");

        String formattedMessage = rawMessage.replace("&", "§");
        Component componentMessage = Component.literal(formattedMessage);

        S2C_SendNoticePayload packet = new S2C_SendNoticePayload(componentMessage, duration);

        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        for (ServerPlayer player : players) { ServerPlayNetworking.send(player, packet); }

        NotifyHelper.sendSystemMessage(players, CHAT_BG_COLOR.LIME, Component.literal("전체 알림을 전송했습니다. (지속시간: " + duration + "초)"));
        Notify.LOGGER.info("Notice: [{}] {}", duration, componentMessage);

        return 1;
    }
}