package kr.pyke.notify;

import kr.pyke.notify.client.hud.HelpHud;
import kr.pyke.notify.client.hud.NoticeHud;
import kr.pyke.notify.client.key.NotifyKeyMapping;
import kr.pyke.notify.network.NotifyPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NotifyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NotifyPacket.registerClient();

        NotifyKeyMapping.register();
        HelpHud.register();

        NoticeHud.register();
    }
}
