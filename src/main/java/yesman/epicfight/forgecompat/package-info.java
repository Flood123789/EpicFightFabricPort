/**
 * A small Forge-shaped adapter used by the Fabric port.
 *
 * <p>It preserves APIs expected by shared/upstream Epic Fight code—events,
 * capabilities, deferred registries, and networking—while its implementations
 * delegate to Fabric or vanilla Minecraft. Gameplay features should normally
 * live in the regular Epic Fight packages, not in this compatibility layer.</p>
 */
package yesman.epicfight.forgecompat;
