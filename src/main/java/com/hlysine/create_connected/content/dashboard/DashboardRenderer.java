package com.hlysine.create_connected.content.dashboard;

import com.hlysine.create_connected.content.dashboard.DashboardRenderer.DashboardRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Text renderer, so it follows vanilla's {@code AbstractSignRenderer} rather than the
 * {@code SuperByteBuffer} shape the other renderers here use: the block entity's text and layout
 * are extracted, and the font work happens in submit through {@code SubmitNodeCollector.submitText}
 * -- {@code Font.drawInBatch} took a {@code MultiBufferSource}, which no longer exists.
 * <p>
 * {@code SignRenderer.getDarkColor} moved to {@code AbstractSignRenderer}.
 */
public class DashboardRenderer implements BlockEntityRenderer<DashboardBlockEntity, DashboardRenderState> {

    //taken from sign renderer
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);

    private final Font font;

    public DashboardRenderer(final BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public DashboardRenderState createRenderState() {
        return new DashboardRenderState();
    }

    @Override
    public void extractRenderState(
            DashboardBlockEntity be,
            DashboardRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable CrumblingOverlay crumblingOverlay
    ) {
        SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);

        state.text = be.text;
        state.lineHeight = be.getTextLineHeight();
        state.maxWidth = be.getMaxTextLineWidth();
        state.facing = state.blockState.getValue(DashboardBlock.FACING);
        state.textFilteringEnabled = Minecraft.getInstance().isTextFilteringEnabled();
        state.drawOutline = isOutlineVisible(state.blockPos, state.text.getColor().getTextColor());
    }

    @Override
    public void submit(
            DashboardRenderState state,
            PoseStack ps,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        final SignText text = state.text;
        if (text == null)
            return;

        final int midpoint = SignText.LINES * state.lineHeight / 2;

        ps.pushPose();

        ps.translate(0.5, 0.5, 0.5);
        ps.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
        ps.translate(-0.5, -0.5, -0.5);

        ps.translate(0.5, 12 / 16f, 9 / 16f);
        ps.mulPose(Axis.XP.rotationDegrees(-66.80141f));
        ps.translate(0, 3.5 / 16f, 0.15 / 16f);

        float scale = 0.015625f * 0.52f;
        ps.scale(scale, -scale, scale);

        FormattedCharSequence[] sequences = text.getRenderMessages(state.textFilteringEnabled, line -> {
            List<FormattedCharSequence> list = font.split(line, state.maxWidth);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
        });

        final int darkColor = AbstractSignRenderer.getDarkColor(text);
        final int textColor;
        final boolean glowing;
        final int light;
        if (text.hasGlowingText()) {
            textColor = text.getColor().getTextColor();
            glowing = state.drawOutline;
            light = 15728880;
        } else {
            textColor = darkColor;
            glowing = false;
            light = state.lightCoords;
        }

        for (int i = 0; i < SignText.LINES; ++i) {
            FormattedCharSequence sequence = sequences[i];
            float x = (float) (-font.width(sequence) / 2);
            float y = (float) (i * state.lineHeight - midpoint);
            queue.submitText(ps, x, y, sequence, false, Font.DisplayMode.POLYGON_OFFSET,
                    light, textColor, 0, glowing ? darkColor : 0);
        }

        ps.popPose();
    }

    //taken from sign renderer
    private static boolean isOutlineVisible(final BlockPos blockPos, final int i) {
        if (i == DyeColor.BLACK.getTextColor()) {
            return true;
        } else {
            final Minecraft minecraft = Minecraft.getInstance();
            final LocalPlayer localPlayer = minecraft.player;
            if (localPlayer != null && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping()) {
                return true;
            } else {
                final Entity entity = minecraft.getCameraEntity();
                return entity != null && entity.distanceToSqr(Vec3.atCenterOf(blockPos)) < (double) OUTLINE_RENDER_DISTANCE;
            }
        }
    }

    public static class DashboardRenderState extends BlockEntityRenderState {
        public @Nullable SignText text;
        public int lineHeight;
        public int maxWidth;
        public Direction facing = Direction.NORTH;
        public boolean textFilteringEnabled;
        public boolean drawOutline;
    }
}
