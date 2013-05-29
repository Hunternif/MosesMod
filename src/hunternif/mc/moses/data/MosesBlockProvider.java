package hunternif.mc.moses.data;

import hunternif.mc.moses.MosesMod;
import hunternif.mc.moses.MosesSounds;
import hunternif.mc.moses.item.StaffOfMoses;
import hunternif.mc.moses.util.BlockUtil;
import hunternif.mc.moses.util.IntVec3;
import hunternif.mc.moses.util.SoundPoint;

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
	//FIXME optimize, optimize, optimize! Version 0.3 was SO MUCH faster.
	
	public void clearBlockAt(World world, int playerEntityID, int x, int y, int z) {
		List<MosesBlockData> list = worldsMap.get(world);
		if (list == null) {
			list = Collections.synchronizedList(new ArrayList<MosesBlockData>());
			worldsMap.put(world, list);
		}
		//synchronized (list) {
		// This is just too slow
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
				int prevBlockID = world.getBlockId(x, y, z);
				int prevMetadata = world.getBlockMetadata(x, y, z);
				blockData = new MosesBlockData(coords, prevBlockID, prevMetadata, playerEntityID);
				list.add(blockData);
				world.setBlock(x, y, z, MosesMod.waterBlocker.blockID, 0, 2);
			}
		//}
	}
	
	public MosesBlockData getBlockAt(World world, IntVec3 coords) {
		List<MosesBlockData> list = worldsMap.get(world);
		if (list == null) {
			return null;
		}
		List <MosesBlockData> result = new ArrayList<MosesBlockData>();
		synchronized (list) {
			for (MosesBlockData data : list) {
				if (data.coords.equals(coords)) {
					return data;
				}
			}
		}
		return null;
	}
	
	public List<MosesBlockData> getBlocksOwnedBy(World world, int playerEntityID) {
		List<MosesBlockData> list = worldsMap.get(world);
		if (list == null) {
			return null;
		}
		List <MosesBlockData> result = new ArrayList<MosesBlockData>();
		synchronized (list) {
			for (MosesBlockData data : list) {
				if (data.isSoleOwner(playerEntityID)) {
					result.add(data);
				}
			}
		}
		return result;
	}
	
	public void restoreAllOwnedBlocksAndPlaySound(World world, int playerEntityID) {
		List<MosesBlockData> list = worldsMap.get(world);
		if (list == null) {
			return;
		}
		
		List<SoundPoint> soundPoints = SoundPoint.initSoundPoints(world);
		
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
						world.setBlock(data.coords.x, data.coords.y, data.coords.z, data.prevBlockID, data.prevMetadata, 2);
						
						// Check if we hit any player BBs
						for (SoundPoint sp : soundPoints) {
							if (sp.expandedAABB.isVecInside(data.coords.toVec3(world.getWorldVec3Pool()))) {
								double distance = data.coords.toVec3(world.getWorldVec3Pool()).distanceTo(world.getWorldVec3Pool().getVecFromPool(sp.player.posX, sp.player.posY, sp.player.posZ));
								if (sp.coords == null || distance < sp.distanceToPlayer) {
									sp.coords = new IntVec3(data.coords.toVec3(world.getWorldVec3Pool()));
									sp.distanceToPlayer = distance;
									sp.blockId = world.getBlockId(sp.coords.x, sp.coords.y, sp.coords.z);
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
		SoundPoint.optimizeSoundPoints(soundPoints);
		
		// Play all sounds
		for (SoundPoint sp : soundPoints) {
			if (sp.coords != null) {
				boolean isLava = BlockUtil.isLava(sp.blockId);
				String sound = isLava ? MosesSounds.LAVA_CLOSING : MosesSounds.SEA_CLOSING;
				world.playSoundEffect(sp.coords.x, sp.coords.y, sp.coords.z, sound, 1, 1);
			}
		}
	}
}
