package com.hlysine.create_connected.content.goggleslot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the goggles in the goggle slot on the player's head -- or, over a helmet, pushed up onto
 * the forehead. The pose is the one Create Fly's own Trinkets renderer uses
 * ({@code GoggleTrinketRenderer}), so goggles look the same whichever slot they are in.
 * <p>
 * The render state carries no reference to the entity, but it does carry its id, so the stack is
 * read off the player at submit time rather than copied into the state by a mixin on
 * {@code AvatarRenderer.extractRenderState}. Other players' slots reach this client because the
 * attachment syncs to everyone tracking the wearer (see {@link GoggleSlot}).
 */
public class GoggleSlotLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public GoggleSlotLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @SuppressWarnings("unchecked")
    public static void register() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof AvatarRenderer<?> avatar)
                helper.register(new GoggleSlotLayer((RenderLayerParent<AvatarRenderState, PlayerModel>) (Object) avatar));
        });
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       AvatarRenderState state, float yRot, float xRot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !GoggleSlot.isActive())
            return;
        Entity entity = mc.level.getEntity(state.id);
        if (!(entity instanceof Player player))
            return;
        ItemStack stack = GoggleSlot.get(player);
        if (!GoggleSlot.isGoggles(stack))
            return;

        PlayerModel model = getParentModel();
        poseStack.pushPose();
        model.root().translateAndRotate(poseStack);
        model.getHead().translateAndRotate(poseStack);
        poseStack.translate(0.0F, -0.25F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        if (!state.headEquipment.isEmpty()) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.translate(0.0F, -0.25F, 0.0F);
        }
        ItemStackRenderState item = new ItemStackRenderState();
        mc.getItemModelResolver().updateForLiving(item, stack, ItemDisplayContext.HEAD, player);
        item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
