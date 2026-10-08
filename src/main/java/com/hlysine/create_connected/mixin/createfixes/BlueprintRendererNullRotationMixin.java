package com.hlysine.create_connected.mixin.createfixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A Create Fly bug, fixed here: a crafting blueprint facing south (yaw 0) with a recipe in it
 * crashed every client that looked at it ({@code NullPointerException ... "quat" is null}).
 * {@code extractRenderState} stores the yaw rotation as {@code null} when the yaw is 0, to skip a
 * no-op, and {@code submit} guards that for the blueprint model but not for its items, where it
 * hands the null straight to {@code Matrix3f.rotate} and {@code Matrix4f.rotate}. A null rotation
 * here means "no rotation", so the matrix is left as it is, which is what Create does by rotating
 * by 0.
 */
@Environment(EnvType.CLIENT)
@Mixin(value = BlueprintRenderer.class, remap = false)
public class BlueprintRendererNullRotationMixin {
    @WrapOperation(
            method = "submit(Lcom/zurrtum/create/client/content/equipment/blueprint/BlueprintRenderer$BlueprintState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3f;rotate(Lorg/joml/Quaternionfc;)Lorg/joml/Matrix3f;")
    )
    private Matrix3f create_connected$skipNullNormalRotation(Matrix3f normal, Quaternionfc rotation, Operation<Matrix3f> original) {
        return rotation == null ? normal : original.call(normal, rotation);
    }

    @WrapOperation(
            method = "submit(Lcom/zurrtum/create/client/content/equipment/blueprint/BlueprintRenderer$BlueprintState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotate(Lorg/joml/Quaternionfc;)Lorg/joml/Matrix4f;")
    )
    private Matrix4f create_connected$skipNullPoseRotation(Matrix4f pose, Quaternionfc rotation, Operation<Matrix4f> original) {
        return rotation == null ? pose : original.call(pose, rotation);
    }
}
