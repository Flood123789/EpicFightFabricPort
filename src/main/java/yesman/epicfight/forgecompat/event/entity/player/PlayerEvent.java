package yesman.epicfight.forgecompat.event.entity.player;

import java.io.File;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import yesman.epicfight.forgecompat.event.entity.living.LivingEvent;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;

public class PlayerEvent extends LivingEvent {
	private final Player player;

	public PlayerEvent(Player player) {
		super(player);
		this.player = player;
	}

	public Player getEntity() {
		return this.player;
	}

	public static class StartTracking extends PlayerEvent {
		private final net.minecraft.world.entity.Entity target;

		public StartTracking(Player player, net.minecraft.world.entity.Entity target) {
			super(player);
			this.target = target;
		}

		public net.minecraft.world.entity.Entity getTarget() {
			return this.target;
		}
	}

	public static class StopTracking extends PlayerEvent {
		private final net.minecraft.world.entity.Entity target;

		public StopTracking(Player player, net.minecraft.world.entity.Entity target) {
			super(player);
			this.target = target;
		}

		public net.minecraft.world.entity.Entity getTarget() {
			return this.target;
		}
	}

	public static class LoadFromFile extends PlayerEvent {
		private final File playerDirectory;
		private final String playerUUID;

		public LoadFromFile(Player player, File playerDirectory, String playerUUID) {
			super(player);
			this.playerDirectory = playerDirectory;
			this.playerUUID = playerUUID;
		}

		public File getPlayerDirectory() {
			return this.playerDirectory;
		}

		public String getPlayerUUID() {
			return this.playerUUID;
		}
	}

	public static class Clone extends PlayerEvent {
		private final Player original;
		private final boolean wasDeath;

		public Clone(Player newPlayer, Player original, boolean wasDeath) {
			super(newPlayer);
			this.original = original;
			this.wasDeath = wasDeath;
		}

		public Player getOriginal() {
			return this.original;
		}

		public boolean isWasDeath() {
			return this.wasDeath;
		}
	}

	public static class PlayerChangedDimensionEvent extends PlayerEvent {
		private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> from;
		private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> to;

		public PlayerChangedDimensionEvent(Player player, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> from, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> to) {
			super(player);
			this.from = from;
			this.to = to;
		}

		public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getFrom() {
			return this.from;
		}

		public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getTo() {
			return this.to;
		}
	}

	public static class PlayerLoggedInEvent extends PlayerEvent {
		public PlayerLoggedInEvent(Player player) {
			super(player);
		}
	}

	public static class PlayerLoggedOutEvent extends PlayerEvent {
		public PlayerLoggedOutEvent(Player player) {
			super(player);
		}
	}

	public static class PlayerRespawnEvent extends PlayerEvent {
		private final boolean endConquered;

		public PlayerRespawnEvent(Player player, boolean endConquered) {
			super(player);
			this.endConquered = endConquered;
		}

		public boolean isEndConquered() {
			return this.endConquered;
		}
	}

	@Cancelable
	public static class BreakSpeed extends PlayerEvent {
		private final BlockState state;
		private final float originalSpeed;
		private float newSpeed;
		private final BlockPos pos;

		public BreakSpeed(Player player, BlockState state, float originalSpeed, BlockPos pos) {
			super(player);
			this.state = state;
			this.originalSpeed = originalSpeed;
			this.newSpeed = originalSpeed;
			this.pos = pos;
		}

		public BlockState getState() {
			return this.state;
		}

		public float getOriginalSpeed() {
			return this.originalSpeed;
		}

		public float getNewSpeed() {
			return this.newSpeed;
		}

		public void setNewSpeed(float newSpeed) {
			this.newSpeed = newSpeed;
		}

		public BlockPos getPos() {
			return this.pos;
		}
	}

	public static class HarvestCheck extends PlayerEvent {
		private final BlockState state;
		private boolean canHarvest;

		public HarvestCheck(Player player, BlockState state, boolean canHarvest) {
			super(player);
			this.state = state;
			this.canHarvest = canHarvest;
		}

		public BlockState getTargetBlock() {
			return this.state;
		}

		public boolean canHarvest() {
			return this.canHarvest;
		}

		public void setCanHarvest(boolean canHarvest) {
			this.canHarvest = canHarvest;
		}
	}
}
