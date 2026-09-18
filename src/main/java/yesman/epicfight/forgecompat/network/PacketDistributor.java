package yesman.epicfight.forgecompat.network;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.forgecompat.fml.common.ServerLifecycleHooks;

public class PacketDistributor<T> {
	public static final PacketDistributor<Supplier<Entity>> TRACKING_ENTITY = new PacketDistributor<>(sup -> {
		Entity entity = sup.get();
		if (entity != null && entity.level() instanceof ServerLevel serverLevel) {
			return serverLevel.getChunkSource().chunkMap.getPlayers(entity.chunkPosition(), false);
		}
		return Collections.emptyList();
	});

	public static final PacketDistributor<Supplier<Entity>> TRACKING_ENTITY_AND_SELF = new PacketDistributor<>(sup -> {
		Entity entity = sup.get();
		if (entity != null && entity.level() instanceof ServerLevel serverLevel) {
			Collection<ServerPlayer> tracking = new java.util.ArrayList<>(serverLevel.getChunkSource().chunkMap.getPlayers(entity.chunkPosition(), false));
			if (entity instanceof ServerPlayer player && !tracking.contains(player)) {
				tracking.add(player);
			}
			return tracking;
		}
		return Collections.emptyList();
	});

	public static final PacketDistributor<Supplier<LevelChunk>> TRACKING_CHUNK = new PacketDistributor<>(sup -> {
		LevelChunk chunk = sup.get();
		if (chunk != null && chunk.getLevel() instanceof ServerLevel serverLevel) {
			return serverLevel.getChunkSource().chunkMap.getPlayers(chunk.getPos(), false);
		}
		return Collections.emptyList();
	});

	public static final PacketDistributor<Supplier<ServerPlayer>> PLAYER = new PacketDistributor<>(sup -> {
		ServerPlayer player = sup.get();
		return player != null ? Collections.singletonList(player) : Collections.emptyList();
	});

	public static final PacketDistributor<Supplier<TargetPoint>> NEAR = new PacketDistributor<>(sup -> {
		TargetPoint tp = sup.get();
		if (tp != null && tp.level instanceof ServerLevel serverLevel) {
			Collection<ServerPlayer> result = new java.util.ArrayList<>();
			for (ServerPlayer player : serverLevel.players()) {
				if (player.distanceToSqr(tp.x, tp.y, tp.z) < tp.r2) {
					result.add(player);
				}
			}
			return result;
		}
		return Collections.emptyList();
	});

	public static final PacketDistributor<Supplier<ResourceKey<Level>>> DIMENSION = new PacketDistributor<>(sup -> {
		ResourceKey<Level> dim = sup.get();
		if (dim != null && ServerLifecycleHooks.getCurrentServer() != null) {
			ServerLevel serverLevel = ServerLifecycleHooks.getCurrentServer().getLevel(dim);
			return serverLevel != null ? serverLevel.players() : Collections.emptyList();
		}
		return Collections.emptyList();
	});

	public static final PacketDistributor<Void> ALL = new PacketDistributor<>(v -> {
		if (ServerLifecycleHooks.getCurrentServer() != null) {
			return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
		}
		return Collections.emptyList();
	});

	private final Function<T, Collection<ServerPlayer>> playerFinder;

	private PacketDistributor(Function<T, Collection<ServerPlayer>> playerFinder) {
		this.playerFinder = playerFinder;
	}

	public PacketTarget noArg() {
		return with((T) (Void) null);
	}

	public PacketTarget with(T param) {
		return new PacketTarget() {
			@Override
			public Collection<ServerPlayer> getPlayers() {
				return playerFinder.apply(param);
			}
		};
	}

	public interface PacketTarget {
		Collection<ServerPlayer> getPlayers();

		default NetworkDirection getDirection() {
			return NetworkDirection.PLAY_TO_CLIENT;
		}

		default void send(net.minecraft.network.protocol.Packet<?> packet) {
			for (ServerPlayer player : getPlayers()) {
				player.connection.send(packet);
			}
		}
	}

	public static class TargetPoint {
		public final ServerPlayer excluded;
		public final double x, y, z, r2;
		public final ResourceKey<Level> dim;
		public final Level level;

		public TargetPoint(@Nullable ServerPlayer excluded, double x, double y, double z, double r2, ResourceKey<Level> dim) {
			this.excluded = excluded;
			this.x = x;
			this.y = y;
			this.z = z;
			this.r2 = r2;
			this.level = null;
			this.dim = dim != null ? dim : Level.OVERWORLD;
		}

		public TargetPoint(@Nullable ServerPlayer excluded, double x, double y, double z, double r2, Level level) {
			this.excluded = excluded;
			this.x = x;
			this.y = y;
			this.z = z;
			this.r2 = r2;
			this.level = level;
			this.dim = level != null ? level.dimension() : Level.OVERWORLD;
		}
	}
}
