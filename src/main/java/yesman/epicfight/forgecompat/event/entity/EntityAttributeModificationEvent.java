package yesman.epicfight.forgecompat.event.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class EntityAttributeModificationEvent extends Event {
	private final Map<EntityType<? extends LivingEntity>, List<Attribute>> additions = new ConcurrentHashMap<>();

	public void add(EntityType<? extends LivingEntity> entityType, Attribute attribute) {
		add(entityType, attribute, attribute.getDefaultValue());
	}

	public void add(EntityType<? extends LivingEntity> entityType, Attribute attribute, double value) {
		additions.computeIfAbsent(entityType, k -> new ArrayList<>()).add(attribute);
	}

	public boolean has(EntityType<? extends LivingEntity> entityType, Attribute attribute) {
		List<Attribute> list = additions.get(entityType);
		return list != null && list.contains(attribute);
	}
}
