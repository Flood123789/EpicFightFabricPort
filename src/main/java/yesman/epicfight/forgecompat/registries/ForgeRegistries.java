package yesman.epicfight.forgecompat.registries;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.npc.VillagerProfession;
import yesman.epicfight.forgecompat.common.loot.IGlobalLootModifier;

@SuppressWarnings("unchecked")
public class ForgeRegistries {
	private static final Map<ResourceLocation, ForgeRegistry<?>> BY_NAME = new HashMap<>();

	public static final ForgeRegistry<Block> BLOCKS = create("block", BuiltInRegistries.BLOCK);
	public static final ForgeRegistry<Item> ITEMS = create("item", BuiltInRegistries.ITEM);
	public static final ForgeRegistry<MobEffect> MOB_EFFECTS = create("mob_effect", BuiltInRegistries.MOB_EFFECT);
	public static final ForgeRegistry<Potion> POTIONS = create("potion", BuiltInRegistries.POTION);
	public static final ForgeRegistry<Attribute> ATTRIBUTES = create("attribute", BuiltInRegistries.ATTRIBUTE);
	public static final ForgeRegistry<ParticleType<?>> PARTICLE_TYPES = (ForgeRegistry<ParticleType<?>>) (Object) create("particle_type", BuiltInRegistries.PARTICLE_TYPE);
	public static final ForgeRegistry<EntityType<?>> ENTITY_TYPES = (ForgeRegistry<EntityType<?>>) (Object) create("entity_type", BuiltInRegistries.ENTITY_TYPE);
	public static final ForgeRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = (ForgeRegistry<BlockEntityType<?>>) (Object) create("block_entity_type", BuiltInRegistries.BLOCK_ENTITY_TYPE);
	public static final ForgeRegistry<SoundEvent> SOUND_EVENTS = create("sound_event", BuiltInRegistries.SOUND_EVENT);
	public static final ForgeRegistry<PaintingVariant> PAINTING_VARIANTS = create("painting_variant", BuiltInRegistries.PAINTING_VARIANT);
	public static final ForgeRegistry<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = (ForgeRegistry<ArgumentTypeInfo<?, ?>>) (Object) create("command_argument_type", BuiltInRegistries.COMMAND_ARGUMENT_TYPE);
	public static final ForgeRegistry<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = (ForgeRegistry<EntityDataSerializer<?>>) (Object) create("entity_data_serializer", null);
	public static final ForgeRegistry<VillagerProfession> VILLAGER_PROFESSIONS = (ForgeRegistry<VillagerProfession>) (Object) create("villager_profession", BuiltInRegistries.VILLAGER_PROFESSION);
	public static final ForgeRegistry<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = create("global_loot_modifier_serializers", null);

	private static <T> ForgeRegistry<T> create(String path, net.minecraft.core.Registry<T> vanilla) {
		ResourceLocation id = new ResourceLocation(vanilla == null ? "forge" : "minecraft", path);
		ForgeRegistry<T> reg = new ForgeRegistry<>(id, vanilla);
		BY_NAME.put(id, reg);
		return reg;
	}

	@Nullable
	public static ForgeRegistry<?> getRegistryByName(ResourceLocation name) {
		return BY_NAME.get(name);
	}

	public static class Keys {
		public static final ResourceLocation GLOBAL_LOOT_MODIFIER_SERIALIZERS = new ResourceLocation("forge", "global_loot_modifier_serializers");
		public static final ResourceLocation ENTITY_DATA_SERIALIZERS = new ResourceLocation("minecraft", "entity_data_serializers");
	}
}
