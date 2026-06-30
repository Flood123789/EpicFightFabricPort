package yesman.epicfight.world.level.block.entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import yesman.epicfight.world.level.block.FractureBlockState;

public class FractureBlockEntity extends BlockEntity {
	private Vector3f translate;
	private Quaternionf rotation;
	private BlockState originalBlockState;
	private double bouncing;
	private int maxLifeTime;
	private int lifeTime = 0;
	private static Method clientParticleSpawner;
	
	public FractureBlockEntity(BlockPos blockPos, BlockState originalBlockState) {
		super(EpicFightBlockEntities.FRACTURE.get(), blockPos, originalBlockState);
	}
	
	public FractureBlockEntity(BlockPos blockPos, BlockState blockState, FractureBlockState fractureBlockState) {
		super(EpicFightBlockEntities.FRACTURE.get(), blockPos, blockState);
		
		this.originalBlockState = fractureBlockState.getOriginalBlockState(blockPos);
		this.bouncing = fractureBlockState.getBouncing();
		this.translate = fractureBlockState.getTranslate();
		this.rotation = fractureBlockState.getRotation();
		this.maxLifeTime = fractureBlockState.getLifeTime();
	}
	
	public BlockState getOriginalBlockState() {
		return this.originalBlockState;
	}
	
	public Vector3f getTranslate() {
		return this.translate;
	}
	
	public Quaternionf getRotation() {
		return this.rotation;
	}
	
	public double getBouncing() {
		return this.bouncing;
	}
	
	public int getMaxLifeTime() {
		return this.maxLifeTime;
	}
	
	public int getLifeTime() {
		return this.lifeTime;
	}
	
	public static void lifeTimeTick(Level level, BlockPos blockPos, BlockState blockState, FractureBlockEntity fractureBlockEntity) {
		if (level.isClientSide && fractureBlockEntity.originalBlockState.shouldSpawnParticlesOnBreak() && fractureBlockEntity.maxLifeTime - fractureBlockEntity.lifeTime < 10) {
			spawnBreakParticle(level, blockPos, fractureBlockEntity);
		}
		
		if (fractureBlockEntity.lifeTime++ > fractureBlockEntity.maxLifeTime) {
			level.removeBlockEntity(blockPos);
			FractureBlockState.remove(blockPos);
			level.setBlock(blockPos, fractureBlockEntity.getOriginalBlockState(), 0);
		}
	}

	private static void spawnBreakParticle(Level level, BlockPos blockPos, FractureBlockEntity fractureBlockEntity) {
		try {
			if (clientParticleSpawner == null) {
				clientParticleSpawner = Class.forName("yesman.epicfight.client.world.level.block.entity.FractureBlockEntityClient")
					.getMethod("spawnBreakParticle", Level.class, BlockPos.class, FractureBlockEntity.class);
			}

			clientParticleSpawner.invoke(null, level, blockPos, fractureBlockEntity);
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException("Failed to spawn fracture block particles", e);
		}
	}
}
