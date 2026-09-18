package yesman.epicfight.forgecompat.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;
import yesman.epicfight.forgecompat.eventbus.api.Event;

@Cancelable
public class RenderHandEvent extends Event {
	private final PoseStack poseStack;
	private final MultiBufferSource multiBufferSource;
	private final int packedLight;
	private final ItemStack itemStack;
	private final InteractionHand hand;
	private final float interpolatedPitch;
	private final float swingProgress;
	private final float equipProgress;

	public RenderHandEvent(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, ItemStack itemStack, InteractionHand hand, float interpolatedPitch, float swingProgress, float equipProgress) {
		this.poseStack = poseStack;
		this.multiBufferSource = multiBufferSource;
		this.packedLight = packedLight;
		this.itemStack = itemStack;
		this.hand = hand;
		this.interpolatedPitch = interpolatedPitch;
		this.swingProgress = swingProgress;
		this.equipProgress = equipProgress;
	}

	public PoseStack getPoseStack() {
		return this.poseStack;
	}

	public MultiBufferSource getMultiBufferSource() {
		return this.multiBufferSource;
	}

	public int getPackedLight() {
		return this.packedLight;
	}

	public ItemStack getItemStack() {
		return this.itemStack;
	}

	public InteractionHand getHand() {
		return this.hand;
	}

	public float getInterpolatedPitch() {
		return this.interpolatedPitch;
	}

	public float getSwingProgress() {
		return this.swingProgress;
	}

	public float getEquipProgress() {
		return this.equipProgress;
	}

	public float getPartialTick() {
		return net.minecraft.client.Minecraft.getInstance().getFrameTime();
	}
}
