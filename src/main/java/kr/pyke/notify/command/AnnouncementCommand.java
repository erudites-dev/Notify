package kr.pyke.notify.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import kr.pyke.notify.Notify;
import kr.pyke.notify.network.payload.s2c.S2C_SendAnnouncementPayload;
import kr.pyke.notify.util.constants.CHAT_BG_COLOR;
import kr.pyke.notify.util.helper.NotifyHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Objects;

public class AnnouncementCommand {
    private AnnouncementCommand() { }

    private static final SuggestionProvider<CommandSourceStack> COLOR_SUGGESTER = (ctx, builder) -> {
        for (CHAT_BG_COLOR color : CHAT_BG_COLOR.values()) { builder.suggest(color.toString()); }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        var commandNode = Commands.argument("color", StringArgumentType.word()).suggests(COLOR_SUGGESTER)
            .then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(AnnouncementCommand::sendAnnouncement)
            );

        dispatcher.register(Commands.literal("공지사항")
            .requires(source -> source.hasPermission(2))
            .then(commandNode)
        );

        dispatcher.register(Commands.literal("anc")
            .requires(source -> source.hasPermission(2))
            .then(commandNode)
        );
    }

    private static int sendAnnouncement(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        String typeStr = StringArgumentType.getString(ctx, "color");
        CHAT_BG_COLOR color = NotifyHelper.parseEnum(typeStr, CHAT_BG_COLOR.class);
        int colorRGB = NotifyHelper.parseColor(Objects.requireNonNull(color));

        String rawMessage = StringArgumentType.getString(ctx, "message");
        String formattedMessage = rawMessage.replace("&", "§");
        Component componentMessage = Component.literal(formattedMessage);

        S2C_SendAnnouncementPayload packet = new S2C_SendAnnouncementPayload(colorRGB, componentMessage);

        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        for (ServerPlayer player : players) { ServerPlayNetworking.send(player, packet); }

        Notify.LOGGER.info("Announcement: [{}] {}", color.name(), componentMessage);

        return 1;
    }
}
