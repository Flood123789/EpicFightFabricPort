package yesman.epicfight.forgecompat.network;

import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.forgecompat.network.simple.SimpleChannel;

public class NetworkRegistry {
	public static SimpleChannel newSimpleChannel(ResourceLocation name, Supplier<String> networkProtocolVersion, Predicate<String> clientAcceptedVersions, Predicate<String> serverAcceptedVersions) {
		return new SimpleChannel(name);
	}
}
