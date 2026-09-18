package yesman.epicfight.main;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import yesman.epicfight.forgecompat.common.MinecraftForge;
import yesman.epicfight.forgecompat.common.capabilities.CapabilityManager;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.ItemSkinsReloadListener;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.ClientHangWatchdog;
import yesman.epicfight.client.events.ClientEvents;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.particle.AirBurstParticle;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.client.particle.AshDirectionalParticle;
import yesman.epicfight.client.particle.BladeRushParticle;
import yesman.epicfight.client.particle.BloodParticle;
import yesman.epicfight.client.particle.CatharsisParticle;
import yesman.epicfight.client.particle.CutParticle;
import yesman.epicfight.client.particle.DustParticle;
import yesman.epicfight.client.particle.EnderParticle;
import yesman.epicfight.client.particle.EntityAfterimageParticle;
import yesman.epicfight.client.particle.EviscerateParticle;
import yesman.epicfight.client.particle.FeatherParticle;
import yesman.epicfight.client.particle.ForceFieldEndParticle;
import yesman.epicfight.client.particle.ForceFieldParticle;
import yesman.epicfight.client.particle.GroundSlamParticle;
import yesman.epicfight.client.particle.HitBluntParticle;
import yesman.epicfight.client.particle.HitCutParticle;
import yesman.epicfight.client.particle.LaserParticle;
import yesman.epicfight.client.particle.ProjectileTrailParticle;
import yesman.epicfight.client.particle.TsunamiSplashParticle;
import yesman.epicfight.client.renderer.EpicFightShaders;
import yesman.epicfight.client.renderer.blockentity.FractureBlockRenderer;
import yesman.epicfight.client.renderer.entity.DroppedNetherStarRenderer;
import yesman.epicfight.client.renderer.entity.WitherGhostRenderer;
import yesman.epicfight.client.renderer.entity.WitherSkeletonMinionRenderer;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.client.renderer.patched.item.EpicFightItemProperties;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.compat.bettercombat.BetterCombatClientCompat;
import yesman.epicfight.compat.controlify.ControlifyCompat;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.network.server.ClientboundPacketHandlers;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.client.world.ClientWorldEventHelper;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributeSupplier;
import yesman.epicfight.forgecompat.event.entity.EntityJoinLevelEvent;
import yesman.epicfight.forgecompat.event.entity.living.LivingEvent;
import yesman.epicfight.forgecompat.event.TickEvent;
import yesman.epicfight.forgecompat.client.event.ClientPlayerNetworkEvent;
import yesman.epicfight.world.capabilities.entitypatch.HurtableEntityPatch;
import yesman.epicfight.world.level.block.entity.EpicFightBlockEntities;

/**
 * Client-only Fabric entry point declared by {@code fabric.mod.json}.
 *
 * <p>It registers presentation and input systems, translates Fabric client
 * callbacks into the shared event layer, and keeps the current
 * {@link LocalPlayerPatch} bound to {@link ClientEngine}. None of this class may
 * be referenced during dedicated-server startup.</p>
 */
public final class EpicFightFabricClientInitializer implements ClientModInitializer {
	private static boolean initialized;
	private static LocalPlayer synchronizedPlayer;
	
	@Override
	public void onInitializeClient() {
		if (initialized) {
			return;
		}
		
		initialized = true;
		// Set up pure client registries before callbacks can receive a tick or a
		// resource reload. Packet receivers come last so all handlers are ready.
		ClientHangWatchdog.start();
		registerClientEnums();
		registerClientKeyMappings();
		registerClientLifecycle();
		registerClientReloadListeners();
		registerClientEvents();
		registerFabricClientCallbacks();
		registerFabricRenderers();
		registerFabricParticleFactories();
		registerFabricShaders();
		registerRendererBootstrap();
		EpicFightNetworkManager.INSTANCE.initClientListener();
		ClientConfig.loadValues();
		EpicFightMod.LOGGER.info("Initialized Epic Fight native Fabric client bootstrap");
	}
	
	private static void registerClientEnums() {
		InputAction.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, EpicFightInputAction.class);
		InputAction.ENUM_MANAGER.registerEnumCls("minecraft", MinecraftInputAction.class);
		InputAction.ENUM_MANAGER.loadEnum();
	}
	
	private static void registerClientKeyMappings() {
		EpicFightKeyMappings.registerFabricKeys();
		EpicFightKeyMappings.sanitizeVanillaFallbackKeyConflicts();
		ControlifyCompat.registerBindings();
	}
	
	private static void registerClientLifecycle() {
		EpicFightMod.LOGGER.info("Skipping compute shader support probe during Fabric bootstrap");
		EntityPatchProvider.registerEntityPatchesClient();
		SkillBookScreen.registerIconItems();
		EpicFightItemProperties.registerItemProperties();
	}
	
	private static void registerRendererBootstrap() {
		// Renderer construction needs Minecraft's baked model sets and dispatchers,
		// which are not ready during the earlier mod initialization callback.
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			ClientHangWatchdog.markClientTick();
			EpicFightKeyMappings.sanitizeVanillaFallbackKeyConflicts();
			
			EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
			EntityRendererProvider.Context context = new EntityRendererProvider.Context(dispatcher, client.getItemRenderer(), client.getBlockRenderer(), dispatcher.getItemInHandRenderer(), client.getResourceManager(), client.getEntityModels(), client.font);
			
			ClientEngine.getInstance().renderEngine.reloadEntityRenderers(context);
			RenderItemBase.initItemRenderers(client);
		});
	}

	private static void registerFabricRenderers() {
		EntityRendererRegistry.register(EpicFightEntities.AREA_EFFECT_BREATH.get(), NoopRenderer::new);
		EntityRendererRegistry.register(EpicFightEntities.DROPPED_NETHER_STAR.get(), DroppedNetherStarRenderer::new);
		EntityRendererRegistry.register(EpicFightEntities.DEATH_HARVEST_ORB.get(), NoopRenderer::new);
		EntityRendererRegistry.register(EpicFightEntities.DODGE_LOCATION_INDICATOR.get(), NoopRenderer::new);
		EntityRendererRegistry.register(EpicFightEntities.WITHER_GHOST_CLONE.get(), WitherGhostRenderer::new);
		EntityRendererRegistry.register(EpicFightEntities.WITHER_SKELETON_MINION.get(), WitherSkeletonMinionRenderer::new);

		BlockEntityRendererRegistry.register(EpicFightBlockEntities.FRACTURE.get(), FractureBlockRenderer::new);
	}

	private static void registerFabricParticleFactories() {
		ParticleFactoryRegistry particles = ParticleFactoryRegistry.getInstance();
		particles.register(EpicFightParticles.ENDERMAN_DEATH_EMIT.get(), EnderParticle.EndermanDeathEmitProvider::new);
		particles.register(EpicFightParticles.HIT_BLUNT.get(), HitBluntParticle.Provider::new);
		particles.register(EpicFightParticles.HIT_BLADE.get(), new HitCutParticle.Provider());
		particles.register(EpicFightParticles.CUT.get(), CutParticle.Provider::new);
		particles.register(EpicFightParticles.NORMAL_DUST.get(), DustParticle.NormalDustProvider::new);
		particles.register(EpicFightParticles.DUST_EXPANSIVE.get(), DustParticle.ExpansiveDustProvider::new);
		particles.register(EpicFightParticles.DUST_CONTRACTIVE.get(), DustParticle.ContractiveDustProvider::new);
		particles.register(EpicFightParticles.EVISCERATE.get(), new EviscerateParticle.Provider());
		particles.register(EpicFightParticles.BLOOD.get(), BloodParticle.Provider::new);
		particles.register(EpicFightParticles.BLADE_RUSH_SKILL.get(), BladeRushParticle.Provider::new);
		particles.register(EpicFightParticles.GROUND_SLAM.get(), new GroundSlamParticle.Provider());
		particles.register(EpicFightParticles.BREATH_FLAME.get(), EnderParticle.BreathFlameProvider::new);
		particles.register(EpicFightParticles.FORCE_FIELD.get(), new ForceFieldParticle.Provider());
		particles.register(EpicFightParticles.FORCE_FIELD_END.get(), new ForceFieldEndParticle.Provider());
		particles.register(EpicFightParticles.ADRENALINE_PLAYER_BEATING.get(), new EntityAfterimageParticle.AdrenalineParticleProvider());
		particles.register(EpicFightParticles.WHITE_AFTERIMAGE.get(), new EntityAfterimageParticle.WhiteAfterimageProvider());
		particles.register(EpicFightParticles.LASER.get(), new LaserParticle.Provider());
		particles.register(EpicFightParticles.NEUTRALIZE.get(), new DustParticle.ExpansiveMetaParticle.Provider());
		particles.register(EpicFightParticles.BOSS_CASTING.get(), new DustParticle.ContractiveMetaParticle.Provider());
		particles.register(EpicFightParticles.TSUNAMI_SPLASH.get(), TsunamiSplashParticle.Provider::new);
		particles.register(EpicFightParticles.SWING_TRAIL.get(), new AnimationTrailParticle.Provider());
		particles.register(EpicFightParticles.PROJECTILE_TRAIL.get(), new ProjectileTrailParticle.Provider());
		particles.register(EpicFightParticles.FEATHER.get(), FeatherParticle.Provider::new);
		particles.register(EpicFightParticles.AIR_BURST.get(), new AirBurstParticle.Provider());
		particles.register(EpicFightParticles.ASH_DIRECTIONAL.get(), AshDirectionalParticle.Provider::new);
		particles.register(EpicFightParticles.CATHARSIS.get(), CatharsisParticle.Provider::new);
	}

	private static void registerFabricShaders() {
		CoreShaderRegistrationCallback.EVENT.register(context -> {
			context.register(EpicFightMod.identifier("solid_model"), DefaultVertexFormat.POSITION_COLOR_NORMAL, (ShaderInstance shader) -> {
				EpicFightShaders.positionColorNormalShader = shader;
			});
		});
	}
	
	private static void registerClientReloadListeners() {
		// Resource-pack data is client-only: joint masks, meshes, animation clips,
		// and item skins can all be rebuilt without restarting the game.
		ResourceManagerHelper clientResources = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES);
		clientResources.registerReloadListener(listener("joint_masks", new JointMaskReloadListener()));
		clientResources.registerReloadListener(listener("meshes", Meshes.INSTANCE));
		clientResources.registerReloadListener(listener("client_animations", AnimationManager.getInstance()));
		clientResources.registerReloadListener(listener("item_skins", ItemSkinsReloadListener.INSTANCE));
	}
	
	private static void registerClientEvents() {
		MinecraftForge.EVENT_BUS.register(ClientEvents.class);
		MinecraftForge.EVENT_BUS.register(ControlEngine.Events.class);
		MinecraftForge.EVENT_BUS.register(RenderEngine.Events.class);
	}

	private static void registerFabricClientCallbacks() {
		// These callbacks are the Fabric-facing edge. They either invoke a client
		// engine directly or translate the callback into a shared Forge-shaped event.
		ItemTooltipCallback.EVENT.register((stack, context, lines) -> ClientEngine.getInstance().renderEngine.applyItemTooltip(stack, lines));
		HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
			MinecraftForge.EVENT_BUS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.START, tickDelta));
			ClientEngine.getInstance().renderEngine.renderFabricHud(guiGraphics, tickDelta);
			MinecraftForge.EVENT_BUS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.END, tickDelta));
		});
		ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (!(entity instanceof AbstractClientPlayer)) {
				MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(entity, level));
			}
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (client.level != null) {
				ClientWorldEventHelper.loadLevel(client.level);
			}
			if (client.player != null) {
				MinecraftForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingIn(client.player, client.gameMode));
			}
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (client.player != null) {
				MinecraftForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingOut(client.player, client.gameMode));
			}
			ClientWorldEventHelper.unloadLevel();
		});
		ClientTickEvents.START_CLIENT_TICK.register(client ->
			MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START))
		);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientHangWatchdog.markClientTick();
			synchronizeLocalPlayer(client.player);
			tickFabricClientPlayers(client);
			ClientboundPacketHandlers.flushDeferredSkillPackets();
			ClientEngine.getInstance().controlEngine.flushQueuedPackets();
			BetterCombatClientCompat.synchronizeMode(client);
			if (client.level != null) {
				MinecraftForge.EVENT_BUS.post(new TickEvent.LevelTickEvent(TickEvent.Phase.END, client.level));
			}
			MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
		});
	}

	private static void tickFabricClientPlayers(net.minecraft.client.Minecraft client) {
		if (client.level == null) {
			return;
		}

		for (AbstractClientPlayer player : client.level.players()) {
			EpicFightCapabilities.getUnparameterizedEntityPatch(player, HurtableEntityPatch.class).ifPresent(playerPatch -> {
				EpicFightAttributeSupplier.ensureEpicFightAttributes(player);
				if (!playerPatch.isInitialized()) {
					playerPatch.onJoinWorld(player, new EntityJoinLevelEvent(player, player.level()));
				}
				playerPatch.tick(new LivingEvent.LivingTickEvent(player));
			});
		}
	}

	private static void synchronizeLocalPlayer(LocalPlayer player) {
		if (player == null) {
			synchronizedPlayer = null;
			return;
		}

		ClientEngine clientEngine = ClientEngine.getInstance();
		LocalPlayer previousPlayer = synchronizedPlayer;
		LocalPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, LocalPlayerPatch.class);

		if (playerPatch == null && player != synchronizedPlayer) {
			// A capability lookup can happen while LocalPlayer is still being constructed.
			// Refresh it once after joining so the client-specific patch is attached.
			CapabilityManager.invalidateCapabilities(player);
			playerPatch = EpicFightCapabilities.getEntityPatch(player, LocalPlayerPatch.class);
		}

		if (previousPlayer != null && previousPlayer != player && playerPatch != null) {
			MinecraftForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.Clone(previousPlayer, player, clientEngine.minecraft.gameMode));
		}

		synchronizedPlayer = player;

		if (playerPatch != null && clientEngine.controlEngine.getPlayerPatch() != playerPatch) {
			EpicFightAttributeSupplier.ensureEpicFightAttributes(player);
			if (!playerPatch.isInitialized()) {
				playerPatch.onJoinWorld(player, new EntityJoinLevelEvent(player, player.level()));
			}
			clientEngine.controlEngine.setPlayerPatch(playerPatch);
			clientEngine.renderEngine.initHUD();
			EpicFightMod.LOGGER.info(
				"Bound Epic Fight controls to Fabric local player (max stamina={}, basic attack ready={})",
				playerPatch.getMaxStamina(), !playerPatch.getSkill(yesman.epicfight.skill.SkillSlots.BASIC_ATTACK).isEmpty()
			);
		}
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
