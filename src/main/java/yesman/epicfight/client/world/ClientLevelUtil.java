package yesman.epicfight.client.world;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.network.server.SPFracture;

public final class ClientLevelUtil {
	private ClientLevelUtil() {
	}

	public static boolean groundSlamsEnabled() {
		return ClientConfig.groundSlams;
	}

	public static void createParticle(Level level, BlockPos bp, BlockState bs) {
		for (int i = 0; i < 4; i += level.getRandom().nextInt(4)) {
			double x = bp.getX() + (i % 2);
			double z = bp.getZ() + 1 - (i % 2);

			TerrainParticle blockParticle = new TerrainParticle((ClientLevel)level, x, bp.getY() + 1, z, 0, 0, 0, bs, bp);
			blockParticle.setParticleSpeed((Math.random() - 0.5D) * 0.3D, Math.random() * 0.5D, (Math.random() - 0.5D) * 0.3D);
			blockParticle.setLifetime(10 + new Random().nextInt(60));

			Minecraft.getInstance().particleEngine.add(blockParticle);
		}
	}

	public static void handleFracturePacket(SPFracture msg) {
		ClientLevel level = Minecraft.getInstance().level;

		if (level != null) {
			LevelUtil.circleSlamFracture(null, level, msg.location(), msg.radius(), msg.noSound(), msg.noParticle());
		}
	}
}
