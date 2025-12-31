package kr.pyke.notify.client.hud;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class NoticeHud {
    private static OwoUIAdapter<FlowLayout> hudAdapter;
    private static LabelComponent msgLabel;
    private static FlowLayout topBar;

    private static double currentX = 0.d;
    private static final double scrollSpeed = 1.0d;
    private static int textWidth = 0;

    private static boolean hasMessage = false;
    private static long expirationTime = 0;

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            int width = drawContext.guiWidth();
            int height = drawContext.guiHeight();

            if (null == hudAdapter) {
                hudAdapter = OwoUIAdapter.createWithoutScreen(0, 0, width, height, Containers::verticalFlow);

                topBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(9));
                topBar.surface(Surface.flat(0x80000000));
                topBar.allowOverflow(false);

                msgLabel = Components.label(Component.empty()).shadow(false);
                msgLabel.positioning(Positioning.absolute(width, 0));

                topBar.child(msgLabel);
                hudAdapter.rootComponent.child(topBar);

                hudAdapter.inflateAndMount();
            }

            if (!hasMessage) { return; }

            hudAdapter.moveAndResize(0, 0, width, height);

            if (msgLabel != null) {
                currentX -= scrollSpeed;

                if (currentX < -textWidth) {
                    if (System.currentTimeMillis() > expirationTime) {
                        hasMessage = false;
                        return;
                    }
                    else { currentX = width; }
                }

                msgLabel.positioning(Positioning.absolute((int) currentX, 0));
            }

            hudAdapter.render(drawContext, Integer.MIN_VALUE, Integer.MIN_VALUE, tickDelta.getGameTimeDeltaPartialTick(true));
        });
    }

    public static void updateMessage(Component message, int durationSeconds) {
        if (message.getString().isEmpty()) {
            hasMessage = false;
            return;
        }

        if (null != msgLabel) { msgLabel.text(message).shadow(false); }

        textWidth = Minecraft.getInstance().font.width(message);
        currentX = Minecraft.getInstance().getWindow().getGuiScaledWidth();

        expirationTime = System.currentTimeMillis() + (durationSeconds * 1000L);

        hasMessage = true;
    }
}