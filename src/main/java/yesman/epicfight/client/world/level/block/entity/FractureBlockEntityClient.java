package yesman.epicfight.client.world.level.block.entity;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.level.block.entity.FractureBlockEntity;

public final class FractureBlockEntityClient {
	private static final Random RANDOM = new Random();

	private FractureBlockEntityClient() {
	}

	public static void spawnBreakParticle(Level level, BlockPos blockPos, FractureBlockEntity fractureBlockEntity) {
		Particle blockParticle = new TerrainParticle((ClientLevel)level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0, 0, 0, fractureBlockEntity.getOriginalBlockState(), blockPos);
		blockParticle.setParticleSpeed((Math.random() - 0.5D) * 0.3D, Math.random() * 0.5D, (Math.random() - 0.5D) * 0.3D);
		blockParticle.setLifetime(10 + RANDOM.nextInt(60));

		Minecraft.getInstance().particleEngine.add(blockParticle);
	}
}
