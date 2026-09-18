package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.forgecompat.network.NetworkEvent;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SPMoveAndPlayAnimation extends SPPlayAnimationAndSetTarget {
	protected double posX;
	protected double posY;
	protected double posZ;
	protected float yRot;
	
	public SPMoveAndPlayAnimation(Action action, int animation, int entityId, float modifyTime, boolean pause, int targetId, double posX, double posY, double posZ, float yRot) {
		super(action, animation, entityId, modifyTime, pause, targetId);
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.yRot = yRot;
	}
	
	public SPMoveAndPlayAnimation(Action action, AssetAccessor<? extends StaticAnimation> animation, float modifyTime, LivingEntityPatch<?> entitypatch) {
		super(action, animation, modifyTime, entitypatch);
		
		Vec3 position = entitypatch.getOriginal().position();
		this.posX = position.x;
		this.posY = position.y;
		this.posZ = position.z;
		this.yRot = entitypatch.getOriginal().yRotO;
	}
	
	@Override
	public void onArrive() {
		ClientboundPacketBridge.handle(this);
	}
	
	public static SPMoveAndPlayAnimation fromBytes(FriendlyByteBuf buf) {
		return new SPMoveAndPlayAnimation(buf.readEnum(Action.class), buf.readInt(), buf.readInt(), buf.readFloat(), buf.readBoolean(), buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat());
	}
	
	public static void toBytes(SPMoveAndPlayAnimation msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.action);
		buf.writeInt(msg.animationId);
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.transitionTimeModifier);
		buf.writeBoolean(msg.pause);
		buf.writeInt(msg.targetId);
		buf.writeDouble(msg.posX);
		buf.writeDouble(msg.posY);
		buf.writeDouble(msg.posZ);
		buf.writeFloat(msg.yRot);
	}

	public static void handler(SPMoveAndPlayAnimation msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientboundPacketBridge.handle(msg));
		ctx.get().setPacketHandled(true);
	}
}
