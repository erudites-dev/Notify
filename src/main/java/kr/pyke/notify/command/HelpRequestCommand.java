package kr.pyke.notify.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import kr.pyke.notify.util.constants.CHAT_BG_COLOR;
import kr.pyke.notify.util.helper.NotifyHelper;
import kr.pyke.notify.util.state.HelpServerState;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class HelpRequestCommand {
        private HelpRequestCommand() { }

        private static long cooldownMs = 60000L; // 기본 60초
        private static final Map<UUID, Long> lastUsed = new HashMap<>(); // 쿨타임 저장용

        public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
            // 통합 명령어 등록
            dispatcher.register(Commands.literal("도움")
                .then(Commands.literal("쿨타임")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("ms", LongArgumentType.longArg(0))
                        .executes(HelpRequestCommand::setHelpCooldown)
                    ))

                .then(Commands.argument("message", StringArgumentType.greedyString())
                    .executes(HelpRequestCommand::requestHelp)
                )

                .executes(ctx -> requestHelp(ctx, "notify.command.help.message.default_toast"))
            );

            dispatcher.register(Commands.literal("staff-call")
                .then(Commands.literal("cooldown")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("ms", LongArgumentType.longArg(0))
                        .executes(HelpRequestCommand::setHelpCooldown)
                    ))

                .then(Commands.argument("message", StringArgumentType.greedyString())
                    .executes(HelpRequestCommand::requestHelp)
                )

                .executes(ctx -> requestHelp(ctx, "notify.command.help.message.default_toast"))
            );
        }

        // 호출 요청 처리
        private static int requestHelp(CommandContext<CommandSourceStack> ctx) {
            return requestHelp(ctx, StringArgumentType.getString(ctx, "message"));
        }

        private static int requestHelp(CommandContext<CommandSourceStack> ctx, String message) {
            CommandSourceStack source = ctx.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) return 0;

            // 쿨타임 체크
            long now = System.currentTimeMillis();
            long last = lastUsed.getOrDefault(player.getUUID(), 0L);

            if (now - last < cooldownMs) {
                long remaining = (cooldownMs - (now - last)) / 1000L;

                List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
                NotifyHelper.sendSystemMessage(
                    players,
                    CHAT_BG_COLOR.RED,
                    Component.translatable("notify.command.help.message.cooldown_active", remaining)
                );

                return 0;
            }

            // 호출 로직 실행
            HelpServerState.add(player, message);
            lastUsed.put(player.getUUID(), now);

            List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
            NotifyHelper.sendSystemMessage(
                players,
                CHAT_BG_COLOR.LIME,
                Component.translatable("notify.command.help.message.request_sent")
            );

            return 1;
        }

        // 쿨타임 설정 처리
        private static int setHelpCooldown(CommandContext<CommandSourceStack> ctx) {
            long seconds = LongArgumentType.getLong(ctx, "ms");
            cooldownMs = seconds * 1000L;

            List<ServerPlayer> players = ctx.getSource().getServer().getPlayerList().getPlayers();
            NotifyHelper.sendSystemMessage(
                players,
                CHAT_BG_COLOR.LIME,
                Component.translatable("notify.command.help.message.config_changed",
                    Component.literal(String.valueOf(seconds)).withStyle(ChatFormatting.YELLOW)
                )
            );

            return 1;
        }
    }