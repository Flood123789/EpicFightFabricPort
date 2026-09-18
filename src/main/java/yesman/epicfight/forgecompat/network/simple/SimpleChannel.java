package yesman.epicfight.forgecompat.network.simple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.forgecompat.network.NetworkDirection;
import yesman.epicfight.forgecompat.network.NetworkEvent;
import yesman.epicfight.forgecompat.network.PacketDistributor;

@SuppressWarnings("unchecked")
public class SimpleChannel {
	private final ResourceLocation name;
	private final Map<Integer, CodecEntry<?>> byId = new ConcurrentHashMap<>();
	private final Map<Class<?>, Integer> byClass = new ConcurrentHashMap<>();
	private boolean serverReceiverRegistered = false;
	private boolean clientReceiverRegistered = false;

	private record CodecEntry<MSG>(int id, Class<MSG> clazz, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer) {}

	public SimpleChannel(ResourceLocation name) {
		this.name = name;
	}

	public <MSG> void registerMessage(int id, Class<MSG> clazz, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer) {
		CodecEntry<MSG> entry = new CodecEntry<>(id, clazz, encoder, decoder, consumer);
		byId.put(id, entry);
		byClass.put(clazz, id);
	}

	public void sendToServer(Object message) {
		Packet<?> packet = toVanillaPacket(message, NetworkDirection.PLAY_TO_SERVER);
		if (packet != null && net.minecraft.client.Minecraft.getInstance().getConnection() != null) {
			net.minecraft.client.Minecraft.getInstance().getConnection().send(packet);
		}
	}

	public void send(PacketDistributor.PacketTarget target, Object message) {
		Packet<?> packet = toVanillaPacket(message, target.getDirection());
		if (packet != null) {
			target.send(packet);
		}
	}

	public Packet<?> toVanillaPacket(Object message, NetworkDirection direction) {
		if (message == null) return null;
		Integer id = byClass.get(message.getClass());
		if (id == null) {
			throw new IllegalArgumentException("Unregistered packet class: " + message.getClass().getName());
		}
		CodecEntry<Object> entry = (CodecEntry<Object>) byId.get(id);
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(id);
		entry.encoder.accept(message, buf);

		if (direction == NetworkDirection.PLAY_TO_CLIENT) {
			return ServerPlayNetworking.createS2CPacket(this.name, buf);
		} else {
			return ClientPlayNetworking.createC2SPacket(this.name, buf);
		}
	}

	public void initServerListener() {
		if (serverReceiverRegistered) return;
		serverReceiverRegistered = true;
		ServerPlayNetworking.registerGlobalReceiver(this.name, (server, player, handler, buf, responseSender) -> {
			int id = buf.readVarInt();
			CodecEntry<Object> entry = (CodecEntry<Object>) byId.get(id);
			if (entry != null) {
				Object msg = entry.decoder.apply(buf);
				entry.consumer.accept(msg, () -> new NetworkEvent.Context(handler.connection, player));
			}
		});
	}

	public void initClientListener() {
		if (clientReceiverRegistered) return;
		clientReceiverRegistered = true;
		ClientPlayNetworking.registerGlobalReceiver(this.name, (client, handler, buf, responseSender) -> {
			int id = buf.readVarInt();
			CodecEntry<Object> entry = (CodecEntry<Object>) byId.get(id);
			if (entry != null) {
				Object msg = entry.decoder.apply(buf);
				entry.consumer.accept(msg, () -> new NetworkEvent.Context(handler.getConnection(), null));
			}
		});
	}
}
