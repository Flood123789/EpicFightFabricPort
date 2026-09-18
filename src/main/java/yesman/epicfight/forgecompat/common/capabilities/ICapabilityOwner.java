package yesman.epicfight.forgecompat.common.capabilities;

/**
 * Stores attached capabilities on their owner instead of in a global map.
 *
 * <p>This is important for providers whose values point back to the owner. A
 * weak-key global map cannot collect such an entry because its strongly held
 * value keeps the weak key alive.</p>
 */
public interface ICapabilityOwner {
	CapabilityContainer epicfight$getCapabilityContainer();
}
