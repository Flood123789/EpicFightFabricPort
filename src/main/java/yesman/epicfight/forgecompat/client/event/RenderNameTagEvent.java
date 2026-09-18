package yesman.epicfight.forgecompat.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;
import yesman.epicfight.forgecompat.eventbus.api.Event;

@Cancelable
@Event.HasResult
public class RenderNameTagEvent extends Event {
	private final Entity entity;
	private Component content;
	private final Component originalContent;
	private final EntityRenderer<?> renderer;
	private final PoseStack poseStack;
	private final MultiBufferSource multiBufferSource;
	private final int packedLight;
	private final float partialTick;

	public RenderNameTagEvent(Entity entity, Component content, EntityRenderer<?> renderer, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, float partialTick) {
		this.entity = entity;
		this.content = content;
		this.originalContent = content;
		this.renderer = renderer;
		this.poseStack = poseStack;
		this.multiBufferSource = multiBufferSource;
		this.packedLight = packedLight;
		this.partialTick = partialTick;
	}

	public Entity getEntity() {
		return this.entity;
	}

	public Component getContent() {
		return this.content;
	}

	public void setContent(Component content) {
		this.content = content;
	}

	public Component getOriginalContent() {
		return this.originalContent;
	}

	public EntityRenderer<?> getRenderer() {
		return this.renderer;
	}

	public PoseStack getPoseStack() {
		return this.poseStack;
	}

	public MultiBufferSource getMultiBufferSource() {
		return this.multiBufferSource;
	}

	public int getPackedLight() {
		return this.packedLight;
	}

	public float getPartialTick() {
		return this.partialTick;
	}
}
