package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.forgecompat.network.NetworkEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SPUpdatePlayerInput {
	int entityId;
	float forward;
	float strafe;

	public SPUpdatePlayerInput() {
	}

	public SPUpdatePlayerInput(int entityId, float forward, float strafe) {
		this.entityId = entityId;
		this.forward = forward;
		this.strafe = strafe;
	}

	public static SPUpdatePlayerInput fromBytes(FriendlyByteBuf buf) {
		return new SPUpdatePlayerInput(buf.readInt(), buf.readFloat(), buf.readFloat());
	}

	public static void toBytes(SPUpdatePlayerInput msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.forward);
		buf.writeFloat(msg.strafe);
	}
	
	public static void handle(SPUpdatePlayerInput msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientboundPacketBridge.handle(msg));
		ctx.get().setPacketHandled(true);
	}
}
