package hunternif.mc.moses.data;

import hunternif.mc.moses.MosesMod;
import hunternif.mc.moses.Sound;
import hunternif.mc.moses.util.BlockUtil;
import hunternif.mc.moses.util.IntVec3;
import hunternif.mc.moses.util.SoundPoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class MosesBlockProvider {
	private Map<World, ConcurrentHashMap<IntVec3, MosesBlockData>> worldsMap = new ConcurrentHashMap<World, ConcurrentHashMap<IntVec3, MosesBlockData>>();
	private ConcurrentHashMap<Integer /*player entityId*/, Set<MosesBlockData>> ownedBlocks = new ConcurrentHashMap<Integer, Set<MosesBlockData>>(100000);
	//TODO implement caching
	//FIXME optimize, optimize, optimize! Version 0.3 was SO MUCH faster. Try using multi-dimensional (jagged) array instead of the HashMap.
	
	public void clearBlockAt(World world, int playerEntityID, IntVec3 coords) {
		ConcurrentHashMap<IntVec3, MosesBlockData> map = worldsMap.get(world);
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
			int prevBlockID = world.getBlockId(coords.x, coords.y, coords.z);
			int prevMetadata = world.getBlockMetadata(coords.x, coords.y, coords.z);
			blockData = new MosesBlockData(coords.copy(), prevBlockID, prevMetadata, playerEntityID);
			map.put(coords.copy(), blockData);
			world.setBlock(coords.x, coords.y, coords.z, MosesMod.waterBlocker.blockID, prevMetadata, 2);
		}
		Set<MosesBlockData> ownedByThisPlayer = ownedBlocks.get(Integer.valueOf(playerEntityID));
		if (ownedByThisPlayer == null) {
			ownedByThisPlayer = new HashSet<MosesBlockData>();
			ownedBlocks.put(Integer.valueOf(playerEntityID), ownedByThisPlayer);
		}
		ownedByThisPlayer.add(blockData);
	}
	
	public MosesBlockData getBlockAt(World world, IntVec3 coords) {
		Map<IntVec3, MosesBlockData> map = worldsMap.get(world);
		if (map == null) {
			return null;
		}
		return map.get(coords);
	}
	
	public List<MosesBlockData> getBlocksOwnedBy(World world, int playerEntityID) {
		Set<MosesBlockData> ownedByThisPlayer = ownedBlocks.get(Integer.valueOf(playerEntityID));
		return ownedByThisPlayer == null ? null : new ArrayList<MosesBlockData>(ownedByThisPlayer);
		/*ConcurrentHashMap<IntVec3, MosesBlockData> map = worldsMap.get(world);
		if (map == null) {
			return null;
		}
		List <MosesBlockData> result = new ArrayList<MosesBlockData>();
		for (MosesBlockData data : map.values()) {
			if (data.isSoleOwner(playerEntityID)) {
				result.add(data);
			}
		}
		return result;*/
	}
	
	public void restoreAllOwnedBlocksAndPlaySound(World world, int playerEntityID) {
		ConcurrentHashMap<IntVec3, MosesBlockData> map = worldsMap.get(world);
		if (map == null) {
			return;
		}
		List<SoundPoint> soundPoints = SoundPoint.initSoundPoints(world);
		
		List<MosesBlockData> ownedByThisPlayer = getBlocksOwnedBy(world, playerEntityID);
		for (MosesBlockData data : ownedByThisPlayer) {
			if (data.isSoleOwner(playerEntityID)) {
				// Will clear only if playerEntityID is the sole owner
				// AND if the space has not been altered,
				// i.e. no other blocks have been placed there.
				if (MosesMod.materialWaterBlocker ==
						world.getBlockMaterial(data.coords.x, data.coords.y, data.coords.z)) {
					world.setBlock(data.coords.x, data.coords.y, data.coords.z, data.prevBlockID, data.prevMetadata, 2);
					map.remove(data.coords);
					
					// Check if we hit any player BBs
					Vec3 vec = data.coords.toVec3(world.getWorldVec3Pool());
					for (SoundPoint sp : soundPoints) {
						if (sp.expandedAABB.isVecInside(vec)) {
							double distance = data.coords.toVec3(world.getWorldVec3Pool()).distanceTo(world.getWorldVec3Pool().getVecFromPool(sp.player.posX, sp.player.posY, sp.player.posZ));
							if (sp.coords == null || distance < sp.distanceToPlayer) {
								sp.coords = data.coords.copy();
								sp.distanceToPlayer = distance;
								sp.blockId = world.getBlockId(sp.coords.x, sp.coords.y, sp.coords.z);
							}
						}
					}
				}
			} else {
				// Otherwise just attempt to disown this cleared block
				data.removeOwner(playerEntityID);
			}
		}
		ownedBlocks.put(Integer.valueOf(playerEntityID), new HashSet<MosesBlockData>());
		
		// Remove the SoundPoints which are too close
		SoundPoint.optimizeSoundPoints(soundPoints);
		
		// Play all sounds
		for (SoundPoint sp : soundPoints) {
			if (sp.coords != null) {
				boolean isLava = BlockUtil.isLava(sp.blockId);
				String sound = isLava ? Sound.LAVA_CLOSING.getName() : Sound.SEA_CLOSING.getName();
				world.playSoundEffect(sp.coords.x, sp.coords.y, sp.coords.z, sound, 1, 1);
			}
		}
	}
}
