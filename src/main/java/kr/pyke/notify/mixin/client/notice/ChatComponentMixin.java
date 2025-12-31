package kr.pyke.notify.mixin.client.notice;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    // ordinal = 0: 채팅창에서 여러 사각형(배경, 메시지 태그, 스크롤바 등) 중에서 첫 번째로 실행되는 배경 사각형만 대상으로 설정
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0))
    private void handleBackgroundFill(GuiGraphics instance, int x1, int y1, int x2, int y2, int color, @Local GuiMessage.Line line) {
        GuiMessageTag tag = line.tag();

        if (null != tag) {
            int alpha = (color >> 24) & 0xFF;
            int startColor = (alpha << 24) | tag.indicatorColor();

            int middleX = x1 + (int)((x2 - x1) * 0.3);
            instance.fill(x1, y1, middleX, y2, startColor);
            this.drawHorizontalGradient(instance, middleX, y1, x2, y2, startColor);
        }
    }

    @Unique
    private void drawHorizontalGradient(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int startColor) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        var consumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
        var matrix = guiGraphics.pose().last().pose();

        float a1 = (float)(startColor >> 24 & 255) / 255.0F;
        float r1 = (float)(startColor >> 16 & 255) / 255.0F;
        float g1 = (float)(startColor >> 8 & 255) / 255.0F;
        float b1 = (float)(startColor & 255) / 255.0F;

        float a2 = 0.0f;
        float r2 = 0.0f;
        float g2 = 0.0f;
        float b2 = 0.0f;

        consumer.addVertex(matrix, (float)x1, (float)y1, 0.0F).setColor(r1, g1, b1, a1);
        consumer.addVertex(matrix, (float)x1, (float)y2, 0.0F).setColor(r1, g1, b1, a1);
        consumer.addVertex(matrix, (float)x2, (float)y2, 0.0F).setColor(r2, g2, b2, a2);
        consumer.addVertex(matrix, (float)x2, (float)y1, 0.0F).setColor(r2, g2, b2, a2);

        guiGraphics.flush();

        RenderSystem.disableBlend();
    }
}
