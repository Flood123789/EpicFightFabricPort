/**
 * Server-data reload listeners that turn datapack JSON into runtime animation,
 * skill, item-capability, and mob-patch definitions. Prepare work may run off
 * thread; applying results must respect the Minecraft reload barrier.
 */
package yesman.epicfight.api.data.reloader;
