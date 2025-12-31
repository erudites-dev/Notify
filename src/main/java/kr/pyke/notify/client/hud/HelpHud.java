package kr.pyke.notify.client.hud;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import kr.pyke.notify.Notify;
import kr.pyke.notify.client.state.HelpClientState;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

public class HelpHud {
    private static FlowLayout root;
    private static StackLayout stacked;
    private static FlowLayout badgeContainer;
    private static int currentCount = 0;
    private static Integer pendingCount = null;
    private static LabelComponent badgeLabelRef = null;

    private HelpHud() { }

    public static void register() {
        Hud.add(ResourceLocation.fromNamespaceAndPath(Notify.MOD_ID, "help_count"), () -> {
            root = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content()).positioning(Positioning.relative(100, 100)).margins(Insets.of(0, 8, 0, 8));

            StackLayout squareIcon = (StackLayout) Containers.stack(Sizing.fixed(24), Sizing.fixed(24)).surface(Surface.DARK_PANEL).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            LabelComponent ex = Components.label(net.minecraft.network.chat.Component.literal("!").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
            squareIcon.child(ex);

            badgeContainer = (FlowLayout) Containers.horizontalFlow(Sizing.content(), Sizing.content()).surface(Surface.flat(0xD32F2F)).padding(Insets.of(1)).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            LabelComponent badgeLabel = Components.label(net.minecraft.network.chat.Component.literal("").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
            badgeLabel.id("help_badge_label");
            badgeContainer.child(badgeLabel);
            badgeLabelRef = badgeLabel;

            stacked = Containers.stack(Sizing.content(), Sizing.content());
            stacked.child(squareIcon);
            badgeContainer.positioning(Positioning.relative(100, 0));
            stacked.child(badgeContainer);

            root.child(stacked);
            setVisibleBadge(false);
            if (null != pendingCount) {
                int v = pendingCount;
                pendingCount = null;
                updateCount(v);
            }
            else { refreshCountFromState(); }

            return root;
        });
    }

    public static void refreshCountFromState() {
        long reqCount = HelpClientState.RequestCount();
        updateCount((int) reqCount);
    }

    public static void updateCount(int newCount) {
        if (0 > newCount) { newCount = 0; }
        currentCount = newCount;

        if (null == root) {
            pendingCount = newCount;
            return;
        }

        setVisibleBadge(0 < currentCount);

        if (null != badgeLabelRef) {
            if (0 < currentCount) {
                badgeLabelRef.text(net.minecraft.network.chat.Component.literal(formatCountForBadge(currentCount)).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
            }
            else {
                badgeLabelRef.text(net.minecraft.network.chat.Component.literal("").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
            }
        }
    }

    private static String formatCountForBadge(int value) {
        if (99 < value) { return "99+"; }
        return String.valueOf(value);
    }

    private static void setVisibleBadge(boolean visible) {
        if (null == badgeContainer) { return; }
        if (null == stacked) { return; }

        stacked.removeChild(badgeContainer);
        if (visible) { stacked.child(badgeContainer); }
    }
}
