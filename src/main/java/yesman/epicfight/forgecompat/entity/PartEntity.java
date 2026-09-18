package yesman.epicfight.forgecompat.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;

public abstract class PartEntity<T extends Entity> extends Entity {
	private final T parent;

	public PartEntity(T parent) {
		super(parent.getType(), parent.level());
		this.parent = parent;
	}

	public T getParent() {
		return this.parent;
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return null;
	}
}
