package yesman.epicfight.api.forgeevent;

import yesman.epicfight.forgecompat.eventbus.api.Event;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class InitAnimatorEvent extends Event {
	private final LivingEntityPatch<?> entitypatch;
	private final Animator animator;
	
	public InitAnimatorEvent(LivingEntityPatch<?> entitypatch, Animator animator) {
		this.entitypatch = entitypatch;
		this.animator = animator;
	}
	
	public LivingEntityPatch<?> getEntityPatch() {
		return this.entitypatch;
	}
	
	public Animator getAnimator() {
		return this.animator;
	}
}