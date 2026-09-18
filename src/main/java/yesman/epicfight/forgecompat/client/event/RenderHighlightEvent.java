package yesman.epicfight.forgecompat.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;
import yesman.epicfight.forgecompat.eventbus.api.Event;

@Cancelable
public class RenderHighlightEvent extends Event {
	private final HitResult target;
	private final PoseStack poseStack;
	private final MultiBufferSource multiBufferSource;
	private final Camera camera;
	private final float partialTick;

	public RenderHighlightEvent(HitResult target, PoseStack poseStack, MultiBufferSource multiBufferSource, Camera camera, float partialTick) {
		this.target = target;
		this.poseStack = poseStack;
		this.multiBufferSource = multiBufferSource;
		this.camera = camera;
		this.partialTick = partialTick;
	}

	public HitResult getTarget() {
		return this.target;
	}

	public PoseStack getPoseStack() {
		return this.poseStack;
	}

	public MultiBufferSource getMultiBufferSource() {
		return this.multiBufferSource;
	}

	public Camera getCamera() {
		return this.camera;
	}

	public float getPartialTick() {
		return this.partialTick;
	}

	@Cancelable
	public static class Block extends RenderHighlightEvent {
		public Block(BlockHitResult target, PoseStack poseStack, MultiBufferSource multiBufferSource, Camera camera, float partialTick) {
			super(target, poseStack, multiBufferSource, camera, partialTick);
		}

		@Override
		public BlockHitResult getTarget() {
			return (BlockHitResult) super.getTarget();
		}
	}

	@Cancelable
	public static class Entity extends RenderHighlightEvent {
		public Entity(EntityHitResult target, PoseStack poseStack, MultiBufferSource multiBufferSource, Camera camera, float partialTick) {
			super(target, poseStack, multiBufferSource, camera, partialTick);
		}

		@Override
		public EntityHitResult getTarget() {
			return (EntityHitResult) super.getTarget();
		}
	}
}
