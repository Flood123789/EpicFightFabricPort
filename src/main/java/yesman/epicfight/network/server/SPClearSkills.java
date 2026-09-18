package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.forgecompat.network.NetworkEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public record SPClearSkills(int entityId) {
	public static SPClearSkills fromBytes(FriendlyByteBuf buf) {
		return new SPClearSkills(buf.readInt());
	}
	
	public static void toBytes(SPClearSkills msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId());
	}
	
	public static void handle(SPClearSkills msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientboundPacketBridge.handle(msg));
		ctx.get().setPacketHandled(true);
	}
}
