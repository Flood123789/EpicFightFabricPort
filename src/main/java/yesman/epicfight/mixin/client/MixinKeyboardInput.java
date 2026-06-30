package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import yesman.epicfight.client.ClientEngine;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInput extends Input {
	@Inject(method = "tick(ZF)V", at = @At("TAIL"))
	private void epicfight$afterKeyboardInputTick(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
		ClientEngine.getInstance().controlEngine.handleMovementInput((Input)(Object)this);
	}
}
