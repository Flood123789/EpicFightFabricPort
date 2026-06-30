package yesman.epicfight.main;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.common.MinecraftForge;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.ItemSkinsReloadListener;
import yesman.epicfight.client.ClientEngine;
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
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.level.block.entity.EpicFightBlockEntities;

public final class EpicFightFabricClientInitializer implements ClientModInitializer {
	private static boolean initialized;
	
	@Override
	public void onInitializeClient() {
		if (initialized) {
			return;
		}
		
		initialized = true;
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
	}
	
	private static void registerClientLifecycle() {
		EpicFightMod.LOGGER.info("Skipping compute shader support probe during Fabric bootstrap");
		EntityPatchProvider.registerEntityPatchesClient();
		SkillBookScreen.registerIconItems();
		EpicFightItemProperties.registerItemProperties();
	}
	
	private static void registerRendererBootstrap() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
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
		ItemTooltipCallback.EVENT.register((stack, context, lines) -> ClientEngine.getInstance().renderEngine.applyItemTooltip(stack, lines));
		HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> ClientEngine.getInstance().renderEngine.renderFabricHud(guiGraphics, tickDelta));
		ClientTickEvents.END_CLIENT_TICK.register(client -> ClientEngine.getInstance().controlEngine.flushQueuedPackets());
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
