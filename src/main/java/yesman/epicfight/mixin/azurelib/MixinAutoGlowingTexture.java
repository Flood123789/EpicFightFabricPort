package yesman.epicfight.mixin.azurelib;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.systems.RenderSystem;

import mod.azure.azurelib.cache.texture.AutoGlowingTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(value = AutoGlowingTexture.class, remap = false)
public abstract class MixinAutoGlowingTexture {
	@Shadow(remap = false) @Final
	protected ResourceLocation textureBase;

	@Redirect(
		method = "loadTexture",
		at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;get()Ljava/lang/Object;"),
		require = 0,
		remap = false
	)
	private Object epicfight$avoidRenderThreadTextureSelfWait(CompletableFuture<?> future, ResourceManager resourceManager, Minecraft minecraft) throws InterruptedException, ExecutionException {
		if (!future.isDone() && RenderSystem.isOnRenderThreadOrInit()) {
			return minecraft.getTextureManager().getTexture(this.textureBase);
		}

		return future.get();
	}
}
