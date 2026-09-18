package yesman.epicfight.forgecompat.event.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class EntityAttributeCreationEvent extends Event {
	private final Map<EntityType<? extends LivingEntity>, AttributeSupplier> map = new ConcurrentHashMap<>();

	public void put(EntityType<? extends LivingEntity> entityType, AttributeSupplier supplier) {
		map.put(entityType, supplier);
		try {
			FabricDefaultAttributeRegistry.register(entityType, supplier);
		} catch (Exception e) {
			// May already be registered or registered differently
		}
	}

	public AttributeSupplier get(EntityType<? extends LivingEntity> entityType) {
		return map.get(entityType);
	}
}
