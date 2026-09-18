package yesman.epicfight.forgecompat.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class RenderLevelStageEvent extends Event {
	public enum Stage {
		AFTER_SKY,
		AFTER_ENTITIES,
		AFTER_BLOCK_ENTITIES,
		AFTER_PARTICLES,
		AFTER_WEATHER,
		AFTER_TRIPWIRE_BLOCKS,
		AFTER_SOLID_BLOCKS,
		AFTER_CUTOUT_BLOCKS,
		AFTER_TRANSLUCENT_BLOCKS
	}

	private final Stage stage;
	private final LevelRenderer levelRenderer;
	private final PoseStack poseStack;
	private final Matrix4f projectionMatrix;
	private final int renderTick;
	private final float partialTick;
	private final Camera camera;
	private final Frustum frustum;

	public RenderLevelStageEvent(Stage stage, LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f projectionMatrix, int renderTick, float partialTick, Camera camera, Frustum frustum) {
		this.stage = stage;
		this.levelRenderer = levelRenderer;
		this.poseStack = poseStack;
		this.projectionMatrix = projectionMatrix;
		this.renderTick = renderTick;
		this.partialTick = partialTick;
		this.camera = camera;
		this.frustum = frustum;
	}

	public Stage getStage() {
		return this.stage;
	}

	public LevelRenderer getLevelRenderer() {
		return this.levelRenderer;
	}

	public PoseStack getPoseStack() {
		return this.poseStack;
	}

	public Matrix4f getProjectionMatrix() {
		return this.projectionMatrix;
	}

	public int getRenderTick() {
		return this.renderTick;
	}

	public float getPartialTick() {
		return this.partialTick;
	}

	public Camera getCamera() {
		return this.camera;
	}

	public Frustum getFrustum() {
		return this.frustum;
	}

	public static class RegisterStageEvent extends Event {
		public RegisterStageEvent() {
		}
	}
}
