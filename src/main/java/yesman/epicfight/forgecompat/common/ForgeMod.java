package yesman.epicfight.forgecompat.common;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.forgecompat.registries.RegistryObject;

public class ForgeMod {
	public static final RegistryObject<Attribute> ENTITY_GRAVITY = RegistryObject.create(
		new ResourceLocation("forge", "entity_gravity"),
		() -> new net.minecraft.world.entity.ai.attributes.RangedAttribute("attribute.name.generic.gravity", 0.08D, -1024.0D, 1024.0D).setSyncable(true)
	);
	public static final RegistryObject<Attribute> SWIM_SPEED = RegistryObject.of(() -> Attributes.MOVEMENT_SPEED);
	public static final RegistryObject<Attribute> STEP_HEIGHT_ADDITION = RegistryObject.of(() -> Attributes.MOVEMENT_SPEED);
}
