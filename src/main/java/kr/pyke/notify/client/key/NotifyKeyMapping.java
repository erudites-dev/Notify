package kr.pyke.notify.client.key;

import kr.pyke.notify.client.gui.HelpScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class NotifyKeyMapping {
    private static KeyMapping requestUIKey;

    static private void bind() {
        requestUIKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.notify.openrequest", GLFW.GLFW_KEY_H, "category.notify.general"));
    }

    static public void register() {
        bind();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (requestUIKey.consumeClick()) {
                if (null != client.player && client.player.hasPermissions(2)) {
                    client.setScreen(new HelpScreen());
                }
            }
        });
    }

    private NotifyKeyMapping() { }
}
