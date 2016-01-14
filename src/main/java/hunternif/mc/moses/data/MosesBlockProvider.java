package hunternif.mc.moses.data;

import hunternif.mc.moses.MosesMod;
import hunternif.mc.moses.Sound;
import hunternif.mc.moses.util.BlockUtil;
import hunternif.mc.moses.util.IntVec3;
import hunternif.mc.moses.util.SoundPoint;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.google.common.collect.Sets;

public class MosesBlockProvider {
	private Map<World, Map<IntVec3, MosesBlockData>> worldsMap = new ConcurrentHashMap<>();
	private Map<Integer /*player entityId*/, Map<World, Set<MosesBlockData>>> ownedBlocks = new ConcurrentHashMap<>();
	//TODO implement caching
	//FIXME optimize, optimize, optimize! Version 0.3 was SO MUCH faster. Try using multi-dimensional (jagged) array instead of the HashMap.
	
	public void clearBlockAt(World world, int playerEntityID, IntVec3 coords) {
		Map<IntVec3, MosesBlockData> map = worldsMap.get(world);
		if (map == null) {
			map = new ConcurrentHashMap<IntVec3, MosesBlockData>();
			worldsMap.put(world, map);
		}
		// Check if the block with the same coordinates is owned by someone else
		MosesBlockData blockData = map.get(coords);
		if (blockData != null) {
			blockData.addOwner(playerEntityID);
		} else {
			// If the block was not cleared and owned before, clear it now
			Block prevBlock = world.getBlock(coords.x, coords.y, coords.z);
			int prevMetadata = world.getBlockMetadata(coords.x, coords.y, coords.z);
			blockData = new MosesBlockData(coords.copy(), prevBlock, prevMetadata, playerEntityID);
			map.put(coords.copy(), blockData);
			world.setBlock(coords.x, coords.y, coords.z, MosesMod.waterBlocker, prevMetadata, 2);
		}
		Set<MosesBlockData> ownedByThisPlayer = getBlocksOwnedBy(world, playerEntityID);
		ownedByThisPlayer.add(blockData);
	}
	
	public MosesBlockData getBlockAt(World world, IntVec3 coords) {
		Map<IntVec3, MosesBlockData> map = worldsMap.get(world);
		if (map == null) {
			return null;
		}
		return map.get(coords);
	}
	
	public Set<MosesBlockData> getBlocksOwnedBy(World world, int playerEntityID) {
		Map<World, Set<MosesBlockData>> worldBlocks = ownedBlocks.get(playerEntityID);
		if (worldBlocks == null) {
			worldBlocks = new ConcurrentHashMap<>();
			ownedBlocks.put(playerEntityID, worldBlocks);
		}
		Set<MosesBlockData> blocks = worldBlocks.get(world);
		if (blocks == null) {
			blocks = Sets.newSetFromMap(new ConcurrentHashMap<MosesBlockData, Boolean>());
			worldBlocks.put(world, blocks);
		}
		return blocks;
	}
	
	/** Will restore blocks and play sound in all worlds. */
	public void restoreAllOwnedBlocksAndPlaySound(int playerEntityID) {
		for (World world : worldsMap.keySet()) {
			Map<IntVec3, MosesBlockData> map = worldsMap.get(world);
			List<SoundPoint> soundPoints = SoundPoint.initSoundPoints(world);
			
			Map<World, Set<MosesBlockData>> playerOwnedWorlds = ownedBlocks.get(playerEntityID);
			if (playerOwnedWorlds == null) continue;
			Set<MosesBlockData> playerOwnedBlocks = playerOwnedWorlds.get(world);
			if (playerOwnedBlocks == null) continue;
			for (MosesBlockData data : playerOwnedBlocks) {
				if (data.isSoleOwner(playerEntityID)) {
					// Will clear only if playerEntityID is the sole owner
					// AND if the space has not been altered,
					// i.e. no other blocks have been placed there.
					if (MosesMod.waterBlocker ==
							world.getBlock(data.coords.x, data.coords.y, data.coords.z)) {
						world.setBlock(data.coords.x, data.coords.y, data.coords.z, data.prevBlock, data.prevMetadata, 2);
						map.remove(data.coords);
						
						// Check if we hit any player BBs
						Vec3 vec = data.coords.toVec3();
						for (SoundPoint sp : soundPoints) {
							if (sp.expandedAABB.isVecInside(vec)) {
								double distance = data.coords.toVec3().distanceTo(Vec3.createVectorHelper(sp.player.posX, sp.player.posY, sp.player.posZ));
								if (sp.coords == null || distance < sp.distanceToPlayer) {
									sp.coords = data.coords.copy();
									sp.distanceToPlayer = distance;
									sp.block = world.getBlock(sp.coords.x, sp.coords.y, sp.coords.z);
								}
							}
						}
					}
				} else {
					// Otherwise just attempt to disown this cleared block
					data.removeOwner(playerEntityID);
				}
			}
			playerOwnedWorlds.remove(world);
			
			// Remove the SoundPoints which are too close
			SoundPoint.optimizeSoundPoints(soundPoints);
			
			// Play all sounds
			for (SoundPoint sp : soundPoints) {
				if (sp.coords != null) {
					boolean isLava = BlockUtil.isLava(sp.block);
					String sound = isLava ? Sound.LAVA_CLOSING.getName() : Sound.SEA_CLOSING.getName();
					world.playSoundEffect(sp.coords.x, sp.coords.y, sp.coords.z, sound, 1, 1);
				}
			}
		}
		ownedBlocks.remove(playerEntityID);
	}
}
