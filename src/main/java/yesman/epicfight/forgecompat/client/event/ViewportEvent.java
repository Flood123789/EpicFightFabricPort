package yesman.epicfight.forgecompat.client.event;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class ViewportEvent extends Event {
	private final GameRenderer renderer;
	private final Camera camera;
	private final double partialTick;

	public ViewportEvent(GameRenderer renderer, Camera camera, double partialTick) {
		this.renderer = renderer;
		this.camera = camera;
		this.partialTick = partialTick;
	}

	public GameRenderer getRenderer() {
		return this.renderer;
	}

	public Camera getCamera() {
		return this.camera;
	}

	public double getPartialTick() {
		return this.partialTick;
	}

	public static class ComputeCameraAngles extends ViewportEvent {
		private float yaw;
		private float pitch;
		private float roll;

		public ComputeCameraAngles(GameRenderer renderer, Camera camera, double partialTick, float yaw, float pitch, float roll) {
			super(renderer, camera, partialTick);
			this.yaw = yaw;
			this.pitch = pitch;
			this.roll = roll;
		}

		public float getYaw() {
			return this.yaw;
		}

		public void setYaw(float yaw) {
			this.yaw = yaw;
		}

		public float getPitch() {
			return this.pitch;
		}

		public void setPitch(float pitch) {
			this.pitch = pitch;
		}

		public float getRoll() {
			return this.roll;
		}

		public void setRoll(float roll) {
			this.roll = roll;
		}
	}

	public static class ComputeFov extends ViewportEvent {
		private double fov;

		public ComputeFov(GameRenderer renderer, Camera camera, double partialTick, double fov) {
			super(renderer, camera, partialTick);
			this.fov = fov;
		}

		public double getFOV() {
			return this.fov;
		}

		public void setFOV(double fov) {
			this.fov = fov;
		}
	}
}
