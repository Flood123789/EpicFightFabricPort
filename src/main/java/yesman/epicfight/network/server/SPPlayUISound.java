package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import yesman.epicfight.forgecompat.network.BufUtil;
import yesman.epicfight.forgecompat.network.NetworkEvent;
import yesman.epicfight.forgecompat.registries.ForgeRegistries;
import yesman.epicfight.client.ClientEngine;

public record SPPlayUISound(SoundEvent sound, float pitch, float volume) {
	public SPPlayUISound(SoundEvent sound) {
		this(sound, 1.0F, 1.0F);
	}
	
	public static SPPlayUISound fromBytes(FriendlyByteBuf buf) {
		return new SPPlayUISound(BufUtil.readRegistryId(buf), buf.readFloat(), buf.readFloat());
	}
	
	public static void toBytes(SPPlayUISound msg, FriendlyByteBuf buf) {
		BufUtil.writeRegistryId(buf, ForgeRegistries.SOUND_EVENTS, msg.sound);
		buf.writeFloat(msg.pitch());
		buf.writeFloat(msg.volume());
	}
	
	public static void handle(SPPlayUISound msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientboundPacketBridge.handle(msg));
		ctx.get().setPacketHandled(true);
	}
}
