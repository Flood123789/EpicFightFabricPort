/**
 * Network protocol registration, encoding, handling, and packet distribution.
 *
 * <p>Classes under {@code client} are named {@code CP*} because they travel
 * from client to server. Classes under {@code server} are named {@code SP*}
 * because they travel from server to client. Both sides must register packets
 * in the same order in {@code EpicFightNetworkManager}.</p>
 */
package yesman.epicfight.network;
