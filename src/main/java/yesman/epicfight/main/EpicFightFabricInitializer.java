package yesman.epicfight.main;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.MapMaker;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v2.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import yesman.epicfight.forgecompat.common.MinecraftForge;
import yesman.epicfight.forgecompat.common.ForgeMod;
import yesman.epicfight.forgecompat.registries.ForgeRegistries;
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
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributeSupplier;
import yesman.epicfight.forgecompat.common.capabilities.CapabilityManager;
import yesman.epicfight.forgecompat.event.entity.EntityJoinLevelEvent;
import yesman.epicfight.forgecompat.event.entity.living.LivingEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

/**
 * Common Fabric entry point declared by {@code fabric.mod.json}.
 *
 * <p>This class is the composition root for gameplay-side code: it creates
 * registries first, installs data reloaders and callbacks next, then enables
 * networking. Client rendering and input are deliberately initialized by
 * {@link EpicFightFabricClientInitializer} instead.</p>
 */
public final class EpicFightFabricInitializer implements ModInitializer {
	private static boolean initialized;
	// A respawn creates a new ServerPlayer with the same UUID. These guards therefore
	// use weak identity keys rather than account UUIDs.
	private static final Set<ServerPlayer> SERVER_PATCH_REFRESH_ATTEMPTED = Collections.newSetFromMap(new MapMaker().weakKeys().<ServerPlayer, Boolean>makeMap());
	private static final Set<ServerPlayer> SERVER_PATCH_FAILURE_LOGGED = Collections.newSetFromMap(new MapMaker().weakKeys().<ServerPlayer, Boolean>makeMap());
	
	@Override
	public void onInitialize() {
		if (initialized) {
			return;
		}
		
		initialized = true;
		// Registration order matters. Extendable enums and built-in assets must
		// exist before deferred content or datapack reloaders refer to them.
		registerEnums();
		registerBuiltInAnimations();
		registerDeferredContent();
		registerLifecycle();
		registerBuiltinResourcePacks();
		registerEvents();
		registerFabricLootEvents();
		registerReloadListeners();
		registerCommands();
		registerFabricPlayerLifecycle();
		EpicFightFabricEventBridge.register();
		EpicFightNetworkManager.registerPackets();
		EpicFightNetworkManager.INSTANCE.initServerListener();
		EpicFightMod.LOGGER.info("Initialized Epic Fight native Fabric bootstrap");
	}

	private static void registerFabricPlayerLifecycle() {
		// Fabric has no direct equivalent for every Forge player-capability tick.
		// Tick the attached ServerPlayerPatch here so stamina, skills, and
		// animation state advance exactly once per server tick.
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				ServerPlayerPatch playerPatch = initializeServerPlayer(player);
				if (playerPatch != null) {
					playerPatch.tick(new LivingEvent.LivingTickEvent(player));
				}
			}
		});
	}

	/** Ensures C2S handlers and the server tick use the same initialized player patch. */
	public static ServerPlayerPatch initializeServerPlayer(ServerPlayer player) {
		if (player == null) {
			return null;
		}

		EpicFightAttributeSupplier.ensureEpicFightAttributes(player);
		ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
		if (playerPatch == null && SERVER_PATCH_REFRESH_ATTEMPTED.add(player)) {
			// A capability lookup can occur while ServerPlayer is still inside its base
			// constructor. Refresh once now that the complete server player exists.
			CapabilityManager.invalidateCapabilities(player);
			playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
		}

		if (playerPatch == null) {
			if (SERVER_PATCH_FAILURE_LOGGED.add(player)) {
				EntityPatch<?> actualPatch = EpicFightCapabilities.getEntityPatch(player, EntityPatch.class);
				EpicFightMod.LOGGER.error(
					"Could not attach the Epic Fight server player patch for {} (actual patch={})",
					player.getScoreboardName(), actualPatch == null ? "none" : actualPatch.getClass().getName()
				);
			}
			return null;
		}

		if (!playerPatch.isInitialized()) {
			playerPatch.onJoinWorld(player, new EntityJoinLevelEvent(player, player.level()));
			playerPatch.setStamina(playerPatch.getMaxStamina());
			EpicFightMod.LOGGER.info(
				"Initialized Epic Fight server player {} (max stamina={}, stamina={})",
				player.getScoreboardName(), playerPatch.getMaxStamina(), playerPatch.getStamina()
			);
		}

		return playerPatch;
	}
	
	private static void registerDeferredContent() {
		// Most upstream content is still declared with DeferredRegister. The
		// registry bridge resolves those declarations into Fabric/vanilla
		// registries and then updates each RegistryObject reference.
		registerForgeCompatibilityAttributes();
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

	private static void registerForgeCompatibilityAttributes() {
		ResourceLocation gravityId = ForgeMod.ENTITY_GRAVITY.getId();

		if (!BuiltInRegistries.ATTRIBUTE.containsKey(gravityId)) {
			Registry.register(BuiltInRegistries.ATTRIBUTE, gravityId, ForgeMod.ENTITY_GRAVITY.get());
		}

		// Reuse Kilt/Forge's attribute when it is available; otherwise point the shim at
		// the standalone Fabric registration above.
		ForgeMod.ENTITY_GRAVITY.updateReference(BuiltInRegistries.ATTRIBUTE);
	}
	
	private static void registerEnums() {
		// Epic Fight's ExtendableEnum mechanism lets addons contribute values.
		// Register every source enum before freezing/loading their managers.
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
		// Builders are sorted to keep numeric animation IDs deterministic across
		// physical sides; those IDs are used by network synchronization.
		AnimationManager.addNoWarningModId(EpicFightMod.EPICSKINS_MODID);
		AnimationManager.AnimationRegistryEvent animationRegistryEvent = new AnimationManager.AnimationRegistryEvent();
		Animations.registerAnimations(animationRegistryEvent);
		animationRegistryEvent.getBuilders().stream()
			.sorted(java.util.Comparator.comparing(AnimationManager.AnimationBuilder::namespace))
			.forEach(builder -> builder.task().accept(builder));
	}
	
	private static void registerLifecycle() {
		// These calls populate lookup tables and other shared runtime state after
		// registries exist but before a world or resource reload can use them.
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
		// Shared gameplay code subscribes to the local Forge-shaped event bus.
		// EpicFightFabricEventBridge supplies it with native Fabric callbacks.
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
		// Order expresses dependencies between data sets. For example, item
		// capabilities may refer to weapon types registered immediately before it.
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
