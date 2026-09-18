/**
 * Per-player combat event system used by skills and player patches. Listeners
 * are registered by event type and UUID so a skill can cleanly detach its own
 * hooks when removed from a container.
 */
package yesman.epicfight.world.entity.eventlistener;
