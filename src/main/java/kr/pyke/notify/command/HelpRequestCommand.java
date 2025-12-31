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
            // 1. 관리자용 쿨타임 설정 노드
            var cooldownNode = Commands.literal("쿨타임")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("ms", LongArgumentType.longArg(0))
                    .executes(HelpRequestCommand::setHelpCooldown)
                );

            // 2. 통합 명령어 등록
            dispatcher.register(Commands.literal("도움")
                .then(cooldownNode) // /도움 쿨타임 <초>
                .then(Commands.argument("메시지", StringArgumentType.greedyString()) // /도움 <메시지>
                    .executes(HelpRequestCommand::requestHelp)
                )
                .executes(ctx -> requestHelp(ctx, "도움이 필요합니다!")) // 메시지 없이 /도움 만 쳤을 때
            );
        }

        // 호출 요청 처리
        private static int requestHelp(CommandContext<CommandSourceStack> ctx) {
            return requestHelp(ctx, StringArgumentType.getString(ctx, "메시지"));
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
                NotifyHelper.sendSystemMessage(players, CHAT_BG_COLOR.RED, Component.literal("아직 다시 호출할 수 없습니다. (남은 시간: " + remaining + "초)"));

                return 0;
            }

            // 호출 로직 실행
            HelpServerState.add(player, message);
            lastUsed.put(player.getUUID(), now);

            List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
            NotifyHelper.sendSystemMessage(players, CHAT_BG_COLOR.LIME, Component.literal("운영진에게 도움을 요청했습니다."));

            return 1;
        }

        // 쿨타임 설정 처리
        private static int setHelpCooldown(CommandContext<CommandSourceStack> ctx) {
            long seconds = LongArgumentType.getLong(ctx, "ms");
            cooldownMs = seconds * 1000L;

            List<ServerPlayer> players = ctx.getSource().getServer().getPlayerList().getPlayers();
            NotifyHelper.sendSystemMessage(players, CHAT_BG_COLOR.LIME, Component.literal("도움 명령어 대기시간이 ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal(String.valueOf(seconds)).withStyle(ChatFormatting.YELLOW))
                .append("초로 변경되었습니다.")
            );

            return 1;
        }
    }