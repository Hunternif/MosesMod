package hunternif.mc.moses.data;

import hunternif.mc.moses.MosesMod;
import hunternif.mc.moses.MosesSounds;
import hunternif.mc.moses.SoundPoint;
import hunternif.mc.moses.item.StaffOfMoses;
import hunternif.mc.moses.util.IntVec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.World;

public class MosesBlockProvider {
	private Map<World, List<MosesBlockData>> worldsMap = new ConcurrentHashMap<World, List<MosesBlockData>>();
	//TODO implement caching
	
	public void clearWaterBlockAt(World world, int playerEntityID, int x, int y, int z, int waterBlockID) {
		List<MosesBlockData> list = worldsMap.get(world);
		if (list == null) {
			list = Collections.synchronizedList(new ArrayList<MosesBlockData>());
			worldsMap.put(world, list);
		}
		IntVec3 coords = new IntVec3(x, y, z);
		// Check if the block with the same coordinates is owned by someone else
		MosesBlockData blockData = null;
		for (MosesBlockData data : list) {
			if (data.coords.equals(coords)) {
				blockData = data;
				break;
			}
		}
		if (blockData != null) {
			blockData.addOwner(playerEntityID);
		} else {
			// If the block was not cleared and owned before, clear it now
			blockData = new MosesBlockData(coords, waterBlockID, playerEntityID);
			list.add(blockData);
			world.setBlock(x, y, z, MosesMod.waterBlocker.blockID, 0, 2);
		}
	}
	
	public List<MosesBlockData> getBlocksOwnedBy(World world, int playerEntityID) {
		List<MosesBlockData> list = worldsMap.get(world);
		if (list == null) {
			return null;
		}
		List <MosesBlockData> result = new ArrayList<MosesBlockData>();
		for (MosesBlockData data : list) {
			if (data.isSoleOwner(playerEntityID)) {
				result.add(data);
			}
		}
		return result;
	}
	
	public void restoreAllOwnedBlocksAndPlaySound(World world, int playerEntityID) {
		List<MosesBlockData> list = worldsMap.get(world);
		if (list == null) {
			return;
		}
		
		List<SoundPoint> soundPoints = StaffOfMoses.initSoundPoints(world);
		
		synchronized (list) {
			Iterator<MosesBlockData> iter = list.iterator();
			while (iter.hasNext()) {
				MosesBlockData data = iter.next();
				if (data.isSoleOwner(playerEntityID)) {
					// Will clear only if playerEntityID is the sole owner
					// AND if the space has not been altered,
					// i.e. no other blocks have been placed there.
					if (MosesMod.materialWaterBlocker ==
							world.getBlockMaterial(data.coords.x, data.coords.y, data.coords.z)) {
						world.setBlock(data.coords.x, data.coords.y, data.coords.z, data.waterBlockID, 0, 2);
						
						// Check if we hit any player BBs
						for (SoundPoint sp : soundPoints) {
							if (sp.expandedAABB.isVecInside(data.coords.toVec3(world.getWorldVec3Pool()))) {
								double distance = data.coords.toVec3(world.getWorldVec3Pool()).distanceTo(world.getWorldVec3Pool().getVecFromPool(sp.player.posX, sp.player.posY, sp.player.posZ));
								if (sp.coords == null || distance < sp.distanceToPlayer) {
									sp.coords = new IntVec3(data.coords.toVec3(world.getWorldVec3Pool()));
									sp.distanceToPlayer = distance;
								}
							}
						}
					}
					iter.remove();
				} else {
					// Otherwise just attempt to disown this cleared block
					data.removeOwner(playerEntityID);
				}
			}
		}
		
		// Remove the SoundPoints which are too close
		StaffOfMoses.optimizeSoundPoints(soundPoints);
		
		// Play all sounds
		for (SoundPoint sp : soundPoints) {
			if (sp.coords != null) {
				world.playSoundEffect(sp.coords.x, sp.coords.y, sp.coords.z, MosesSounds.SEA_CLOSING, 1, 1);
			}
		}
	}
}
