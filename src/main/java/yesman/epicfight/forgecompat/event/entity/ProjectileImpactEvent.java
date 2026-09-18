package yesman.epicfight.forgecompat.event.entity;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;

@Cancelable
public class ProjectileImpactEvent extends EntityEvent {
	private final HitResult rayTraceResult;
	private ImpactResult impactResult = ImpactResult.DEFAULT;

	public ProjectileImpactEvent(Projectile projectile, HitResult rayTraceResult) {
		super(projectile);
		this.rayTraceResult = rayTraceResult;
	}

	public Projectile getProjectile() {
		return (Projectile) getEntity();
	}

	public HitResult getRayTraceResult() {
		return this.rayTraceResult;
	}

	public ImpactResult getImpactResult() {
		return this.impactResult;
	}

	public void setImpactResult(ImpactResult impactResult) {
		this.impactResult = impactResult;
	}

	public enum ImpactResult {
		DEFAULT,
		SKIP_ENTITY,
		STOP_AT_CURRENT_NO_DAMAGE
	}
}
