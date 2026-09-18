package yesman.epicfight.api.utils;

public class AttackResult {
	public final ResultType resultType;
	public final float damage;
	
	public AttackResult(ResultType resultType, float damage) {
		// A successful vanilla hit can arrive without Forge's LivingAttackEvent on
		// Fabric. Never allow that missing bridge data to create an invalid result.
		this.resultType = resultType == null ? ResultType.SUCCESS : resultType;
		this.damage = damage;
	}
	
	public static AttackResult success(float damage) {
		return new AttackResult(ResultType.SUCCESS, damage);
	}
	
	public static AttackResult blocked(float damage) {
		return new AttackResult(ResultType.BLOCKED, damage);
	}
	
	public static AttackResult missed(float damage) {
		return new AttackResult(ResultType.MISSED, damage);
	}
	
	public static AttackResult of(AttackResult.ResultType resultType, float damage) {
		return new AttackResult(resultType, damage);
	}
	
	public static enum ResultType {
		SUCCESS(true, true), MISSED(false, false), BLOCKED(false, true);
		
		boolean dealtDamage;
		boolean shouldCount;
		
		ResultType(boolean dealtDamage, boolean countAsHitEntity) {
			this.dealtDamage = dealtDamage;
			this.shouldCount = countAsHitEntity;
		}
		
		public boolean dealtDamage() {
			return this.dealtDamage;
		}
		
		public boolean shouldCount() {
			return this.shouldCount;
		}
	}
}
