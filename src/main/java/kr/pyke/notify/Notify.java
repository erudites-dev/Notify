package kr.pyke.notify;

import kr.pyke.notify.command.AnnouncementCommand;
import kr.pyke.notify.command.HelpRequestCommand;
import kr.pyke.notify.command.NoticeCommand;
import kr.pyke.notify.network.NotifyPacket;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Notify implements ModInitializer {
	public static final String MOD_ID = "notify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Component SYSTEM_PREFIX = Component.literal("[SYSTEM] ").withStyle(ChatFormatting.GOLD);

	@Override
	public void onInitialize() {
		NotifyPacket.registerCodec();
		NotifyPacket.registerServer();

		CommandRegistrationCallback.EVENT.register(AnnouncementCommand::register);
		CommandRegistrationCallback.EVENT.register(NoticeCommand::register);
		CommandRegistrationCallback.EVENT.register(HelpRequestCommand::register);
	}
}