package yesman.epicfight.main;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.loot.v2.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.SynchedAnimationVariableKeys;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.data.loot.SkillBookLootModifier;
import yesman.epicfight.events.CapabilityEvents;
import yesman.epicfight.events.EntityEvents;
import yesman.epicfight.events.PlayerEvents;
import yesman.epicfight.events.WorldEvents;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EpicFightDataSerializers;
import yesman.epicfight.network.EntityPairingPacketType;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.server.commands.AnimatorCommand;
import yesman.epicfight.server.commands.PlayerModeCommand;
import yesman.epicfight.server.commands.PlayerSkillCommand;
import yesman.epicfight.server.commands.PlayerStaminaCommand;
import yesman.epicfight.server.commands.arguments.EpicFightCommandArgumentTypes;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.item.ItemKeywordReloadListener;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.ItemCapabilityProvider;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.effect.EpicFightPotions;
import yesman.epicfight.world.item.EpicFightCreativeTabs;
import yesman.epicfight.world.item.EpicFightItems;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.entity.WitherGhostClone;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.decoration.EpicFightPaintingVariants;
import yesman.epicfight.world.level.block.EpicFightBlocks;
import yesman.epicfight.world.level.block.entity.EpicFightBlockEntities;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public final class EpicFightFabricInitializer implements ModInitializer {
	private static boolean initialized;
	
	@Override
	public void onInitialize() {
		if (initialized) {
			return;
		}
		
		initialized = true;
		registerEnums();
		registerBuiltInAnimations();
		registerDeferredContent();
		registerLifecycle();
		registerBuiltinResourcePacks();
		registerEvents();
		registerFabricLootEvents();
		registerReloadListeners();
		registerCommands();
		EpicFightNetworkManager.registerPackets();
		EpicFightMod.LOGGER.info("Initialized Epic Fight native Fabric bootstrap");
	}
	
	private static void registerDeferredContent() {
		EpicFightFabricRegistryBridge.register(EpicFightBlocks.BLOCKS, ForgeRegistries.BLOCKS);
		EpicFightFabricRegistryBridge.register(EpicFightItems.ITEMS, ForgeRegistries.ITEMS);
		EpicFightFabricRegistryBridge.registerVanilla(EpicFightCreativeTabs.TABS, BuiltInRegistries.CREATIVE_MODE_TAB);
		EpicFightFabricRegistryBridge.register(EpicFightMobEffects.EFFECTS, ForgeRegistries.MOB_EFFECTS);
		EpicFightFabricRegistryBridge.register(EpicFightPotions.POTIONS, ForgeRegistries.POTIONS);
		EpicFightFabricRegistryBridge.register(EpicFightAttributes.ATTRIBUTES, ForgeRegistries.ATTRIBUTES);
		EpicFightFabricRegistryBridge.register(EpicFightParticles.PARTICLES, ForgeRegistries.PARTICLE_TYPES);
		EpicFightFabricRegistryBridge.register(EpicFightEntities.ENTITIES, ForgeRegistries.ENTITY_TYPES);
		EpicFightFabricRegistryBridge.register(EpicFightBlockEntities.BLOCK_ENTITIES, ForgeRegistries.BLOCK_ENTITY_TYPES);
		EpicFightFabricRegistryBridge.register(EpicFightLootTables.LOOT_MODIFIERS, ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS);
		EpicFightFabricRegistryBridge.register(EpicFightSounds.SOUNDS, ForgeRegistries.SOUND_EVENTS);
		EpicFightFabricRegistryBridge.register(EpicFightDataSerializers.ENTITY_DATA_SERIALIZER, ForgeRegistries.ENTITY_DATA_SERIALIZERS);
		SkillManager.registerFabricSkills(EpicFightFabricRegistryBridge.createRegistry(EpicFightMod.identifier("skill"), SkillManager.createFabricRegistryBuilder()));
		EpicFightFabricRegistryBridge.register(EpicFightConditions.CONDITIONS, EpicFightConditions.REGISTRY);
		EpicFightFabricRegistryBridge.register(SkillDataKeys.DATA_KEYS, SkillDataKeys.REGISTRY);
		EpicFightFabricRegistryBridge.register(SynchedAnimationVariableKeys.SYNCHED_ANIMATION_VARIABLE_KEYS, SynchedAnimationVariableKeys.REGISTRY);
		EpicFightFabricRegistryBridge.register(EpicFightPaintingVariants.PAINTING_VARIANTS, ForgeRegistries.PAINTING_VARIANTS);
		EpicFightFabricRegistryBridge.register(EpicFightCommandArgumentTypes.COMMAND_ARGUMENT_TYPES, ForgeRegistries.COMMAND_ARGUMENT_TYPES);
	}
	
	private static void registerEnums() {
		LivingMotion.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, LivingMotions.class);
		SkillCategory.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, SkillCategories.class);
		SkillSlot.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, SkillSlots.class);
		Style.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, Styles.class);
		WeaponCategory.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, WeaponCategories.class);
		Faction.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, Factions.class);
		EntityPairingPacketType.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, EntityPairingPacketTypes.class);
		
		LivingMotion.ENUM_MANAGER.loadEnum();
		SkillCategory.ENUM_MANAGER.loadEnum();
		SkillSlot.ENUM_MANAGER.loadEnum();
		Style.ENUM_MANAGER.loadEnum();
		WeaponCategory.ENUM_MANAGER.loadEnum();
		Faction.ENUM_MANAGER.loadEnum();
		EntityPairingPacketType.ENUM_MANAGER.loadEnum();
	}
	
	private static void registerBuiltInAnimations() {
		AnimationManager.addNoWarningModId(EpicFightMod.EPICSKINS_MODID);
		AnimationManager.AnimationRegistryEvent animationRegistryEvent = new AnimationManager.AnimationRegistryEvent();
		Animations.registerAnimations(animationRegistryEvent);
		animationRegistryEvent.getBuilders().stream()
			.sorted(java.util.Comparator.comparing(AnimationManager.AnimationBuilder::namespace))
			.forEach(builder -> builder.task().accept(builder));
	}
	
	private static void registerLifecycle() {
		
		Armatures.registerEntityTypes();
		EpicFightCommandArgumentTypes.registerArgumentTypes();
		EpicFightPotions.addRecipes();
		ItemCapabilityProvider.registerWeaponTypesByClass();
		EntityPatchProvider.registerEntityPatches();
		CommonConfig.SPEC.isLoaded();
		CommonConfig.loadValues();
		registerFabricEntityAttributes();
		registerFabricSpawnPlacements();
		EpicFightGameRules.registerGameRules();
		WeaponTypeReloadListener.registerDefaultWeaponTypes();
		EpicFightMobEffects.addOffhandModifier();
		EpicFightLootTables.registerLootItemFunctionType();
	}

	private static void registerBuiltinResourcePacks() {
		FabricLoader.getInstance().getModContainer(EpicFightMod.MODID).ifPresent(container -> {
			ResourceManagerHelper.registerBuiltinResourcePack(
				EpicFightMod.identifier("epicfight_legacy"),
				"packs/epicfight_legacy",
				container,
				false
			);
		});
	}

	private static void registerFabricEntityAttributes() {
		FabricDefaultAttributeRegistry.register(EpicFightEntities.WITHER_SKELETON_MINION.get(), AbstractSkeleton.createAttributes().build());
		FabricDefaultAttributeRegistry.register(EpicFightEntities.WITHER_GHOST_CLONE.get(), WitherGhostClone.createAttributes().build());
		FabricDefaultAttributeRegistry.register(EpicFightEntities.DODGE_LOCATION_INDICATOR.get(), LivingEntity.createLivingAttributes().build());
	}

	private static void registerFabricSpawnPlacements() {
		SpawnPlacements.register(EpicFightEntities.WITHER_SKELETON_MINION.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkAnyLightMonsterSpawnRules);
	}
	
	private static void registerEvents() {
		MinecraftForge.EVENT_BUS.register(CapabilityEvents.class);
		MinecraftForge.EVENT_BUS.register(EntityEvents.class);
		MinecraftForge.EVENT_BUS.register(PlayerEvents.class);
		MinecraftForge.EVENT_BUS.register(WorldEvents.class);
		MinecraftForge.EVENT_BUS.register(EpicFightLootTables.class);
	}

	private static void registerFabricLootEvents() {
		LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
			if (source.isBuiltin()) {
				EpicFightLootTables.modifyVanillaLootPools(id, pool -> ((FabricLootTableBuilder)tableBuilder).pool(pool));
				SkillBookLootModifier.createSkillLootTable();
				SkillBookLootModifier.SKILL_LOOT_TABLE.forEach((entityType, lootTable) -> {
					if (id.equals(entityType.getDefaultLootTable())) {
						for (net.minecraft.world.level.storage.loot.LootPool pool : lootTable.pools) {
							((FabricLootTableBuilder)tableBuilder).pool(pool);
						}
					}
				});
			}
		});
	}
	
	private static void registerReloadListeners() {
		ResourceManagerHelper serverData = ResourceManagerHelper.get(PackType.SERVER_DATA);
		serverData.registerReloadListener(listener("collider_preset", new ColliderPreset()));
		serverData.registerReloadListener(listener("skill_manager", new SkillManager()));
		serverData.registerReloadListener(listener("weapon_type", new WeaponTypeReloadListener()));
		serverData.registerReloadListener(listener("item_keyword", new ItemKeywordReloadListener()));
		serverData.registerReloadListener(listener("item_capability", new ItemCapabilityReloadListener()));
		serverData.registerReloadListener(listener("mob_patch", new MobPatchReloadListener()));
		serverData.registerReloadListener(listener("animation_manager", AnimationManager.getInstance()));
	}
	
	private static void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			PlayerModeCommand.register(dispatcher);
			PlayerSkillCommand.register(dispatcher);
			PlayerStaminaCommand.register(dispatcher);
			AnimatorCommand.register(dispatcher);
		});
	}
	
	private static IdentifiableResourceReloadListener listener(String path, PreparableReloadListener delegate) {
		return new IdentifiableResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return EpicFightMod.identifier(path);
			}
			
			@Override
			public java.util.concurrent.CompletableFuture<Void> reload(PreparationBarrier barrier, net.minecraft.server.packs.resources.ResourceManager resourceManager, net.minecraft.util.profiling.ProfilerFiller preparationsProfiler, net.minecraft.util.profiling.ProfilerFiller reloadProfiler, java.util.concurrent.Executor backgroundExecutor, java.util.concurrent.Executor gameExecutor) {
				return delegate.reload(barrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
			}
		};
	}
}
