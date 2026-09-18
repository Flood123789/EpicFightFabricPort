package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.forgecompat.network.NetworkEvent;
import yesman.epicfight.network.common.SyncAnimationPositionPacket;

public class SPSyncAnimationPosition extends SyncAnimationPositionPacket {
	public SPSyncAnimationPosition(int entityId, float elapsedTime, Vec3 position, int lerpSteps) {
		super(entityId, elapsedTime, position, lerpSteps);
	}
	
	public static SPSyncAnimationPosition fromBytes(FriendlyByteBuf buf) {
		return new SPSyncAnimationPosition(buf.readInt(), buf.readFloat(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readInt());
	}
	
	public static void toBytes(SPSyncAnimationPosition msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.elapsedTime);
		buf.writeDouble(msg.position.x);
		buf.writeDouble(msg.position.y);
		buf.writeDouble(msg.position.z);
		buf.writeInt(msg.lerpSteps);
	}
	
	public static void handle(SPSyncAnimationPosition msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientboundPacketBridge.handle(msg));
		ctx.get().setPacketHandled(true);
	}
}
