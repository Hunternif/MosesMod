package hunternif.mc.moses.item;

import hunternif.mc.moses.MosesMod;
import hunternif.mc.moses.MosesSounds;
import hunternif.mc.moses.SoundPoint;
import hunternif.mc.moses.data.MosesBlockData;
import hunternif.mc.moses.data.MosesBlockProvider;
import hunternif.mc.moses.util.BlockUtil;
import hunternif.mc.moses.util.IntVec3;
import hunternif.mc.moses.util.MathUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class StaffOfMoses extends Item {
	public static double playerReach = 5;
	public static double soundDistanceThreshold = 10;
	
	public static double maxReach = 10;
	
	public int passageHalfWidth = 2;
	public double maxPassageLength = 64;
	
	private MosesBlockProvider mosesBlockProvider = new MosesBlockProvider();
	
	public StaffOfMoses(int id) {
		super(id);
		setMaxStackSize(1);
		setFull3D();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		String name = getUnlocalizedName();
		if (name.startsWith("item.")) {
			name = name.substring(5);
		}
		this.itemIcon = iconRegister.registerIcon(MosesMod.ID + ":" + name);
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		Vec3 position = world.getWorldVec3Pool().getVecFromPool(player.posX, player.posY, player.posZ);
		if (!world.isRemote) {
			// Because in server worlds the Y coordinate of a player is his feet's coordinate, without yOffset.
			position = position.addVector(0, 1.62D, 0);
		}
		Vec3 look = player.getLookVec();
        Vec3 reach = position.addVector(look.xCoord * maxReach, look.yCoord * maxReach, look.zCoord * maxReach);
        MovingObjectPosition hit = world.rayTraceBlocks_do(position, reach, true); // true to hit water too
        int hitID = -1;
        if (hit != null) {
			int x = hit.blockX;
			int y = hit.blockY;
			int z = hit.blockZ;
	        hitID = world.getBlockId(x, y, z);
			if (hitID == Block.waterMoving.blockID || hitID == Block.waterStill.blockID || hitID == MosesMod.waterBlocker.blockID) {
				// Do sea parting
				if (!world.isRemote) {
					//world.playSoundAtEntity(player, MosesSounds.SEA_PARTING, 1, 1);
					// 1. Find surface
					while (true) {
						int blockID = world.getBlockId(x, y+1, z);
						if (blockID != Block.waterMoving.blockID && blockID != Block.waterStill.blockID) {
							break;
						} else if (y < world.getHeight() - 1){
							y++;
						} else {
							break;
						}
					}
					
					List<SoundPoint> soundPoints = initSoundPoints(world);
					
					// 2. Perform the wonder of sea parting
					Vec3 lookXZ = world.getWorldVec3Pool().getVecFromPool(-MathHelper.sin(player.rotationYaw*(float)Math.PI/180f), 0, MathHelper.cos(player.rotationYaw*(float)Math.PI/180f));
					final Vec3 start = world.getWorldVec3Pool().getVecFromPool(x, y, z); // Vec3 start must not be changed!
					Vec3[] wavefront = makeFlatWaveFront(start, lookXZ, passageHalfWidth);
					//TODO in case of irregular shore I'll have to traverse some distance in the water without stopping on solid blocks
					for (int i = 0; i < wavefront.length; i++) {
						while (wavefront[i].distanceTo(position) < maxPassageLength
								&& !BlockUtil.isSolid(world, wavefront[i])) {
							Vec3 vec = wavefront[i];
							removeWaterBelow(world, player, vec);
							
							// Check if we hit any player BBs
							for (SoundPoint sp : soundPoints) {
								if (sp.expandedAABB.isVecInside(wavefront[i])) {
									double distance = wavefront[i].distanceTo(world.getWorldVec3Pool().getVecFromPool(sp.player.posX, sp.player.posY, sp.player.posZ));
									if (sp.coords == null || distance < sp.distanceToPlayer) {
										sp.coords = new IntVec3(wavefront[i]);
										sp.distanceToPlayer = distance;
									}
								}
							}
							
							// We move half-step at a time so that there won't be any omitted columns of water left behind.
							wavefront[i] = vec.addVector(lookXZ.xCoord/2, 0, lookXZ.zCoord/2);
						}
					}
					
					// Remove the SoundPoints which are too close
					optimizeSoundPoints(soundPoints);
					
					//TODO unify all this this sound point selection algorithm
					// Play all sounds
					for (SoundPoint sp : soundPoints) {
						if (sp.coords != null) {
							world.playSoundEffect(sp.coords.x, sp.coords.y, sp.coords.z, MosesSounds.SEA_PARTING, 1, 1);
						}
					}
				}
			}
		}
        if (hit == null || !isWaterOrBlockerBlock(hitID)) {
			// Close all passages
        	if (!world.isRemote) {
        		restoreWater(world, player);
        	}
		}
		return itemStack;
	}
	
	public void restoreWater(World world, EntityPlayer player) {
		List<MosesBlockData> list = mosesBlockProvider.getBlocksOwnedBy(world, player.entityId);
		mosesBlockProvider.restoreAllOwnedBlocksAndPlaySound(world, player.entityId);
	}
	
	public void removeWaterBelow(World world, EntityPlayer player, Vec3 vector) {
		int x = MathHelper.floor_double(vector.xCoord);
		int y = MathHelper.floor_double(vector.yCoord);
		int z = MathHelper.floor_double(vector.zCoord);
		if (world.getBlockId(x, y+1, z) == Block.waterlily.blockID) {
			world.setBlock(x, y+1, z, 0, 0, 2);
		}
		while (true) {
			int blockID = world.getBlockId(x, y, z);
			if (isWaterOrBlockerBlock(blockID)) {
				clearWaterBlock(world, player, x, y, z);
			} else if (blockID != 0) {
				break;
			}
			y--;
		}
	}
	
	public void clearWaterBlock(World world, EntityPlayer player, int x, int y, int z) {
		int blockID = world.getBlockId(x, y, z);
		if (isWaterOrBlockerBlock(blockID)) {
			mosesBlockProvider.clearWaterBlockAt(world, player.entityId, x, y, z, blockID);
		}
	}
	
	/**
	 * Returns an array of (halfWidth*2 + 1) vectors parallel to lookXZ and
	 * arranged at in a row* perpendicular to it at a distance of 1 block from
	 * each other.
	 */
	public Vec3[] makeFlatWaveFront(Vec3 start, Vec3 lookXZ, int halfWidth) {
		Vec3[] wavefront = new Vec3[halfWidth*2+1];
		Vec3 lookOrtogonal = MathUtil.crossProduct(lookXZ, lookXZ.myVec3LocalPool.getVecFromPool(0, 1, 0));
		wavefront[halfWidth] = start;
		for (int i = 0; i < halfWidth; i++) {
			Vec3 newVec = start;
			for (int j = 0; j < i+1; j++) {
				newVec = newVec.addVector(lookOrtogonal.xCoord, lookOrtogonal.yCoord, lookOrtogonal.zCoord);
			}
			wavefront[halfWidth-1-i] = newVec;
		}
		lookOrtogonal = MathUtil.crossProduct(lookXZ, lookXZ.myVec3LocalPool.getVecFromPool(0, -1, 0));
		for (int i = 0; i < halfWidth; i++) {
			Vec3 newVec = start;
			for (int j = 0; j < i+1; j++) {
				newVec = newVec.addVector(lookOrtogonal.xCoord, lookOrtogonal.yCoord, lookOrtogonal.zCoord);
			}
			wavefront[halfWidth+1+i] = newVec;
		}
		return wavefront;
	}
	
	/**
	 * Makes a source of water out of stone.
	 */
	@Override
	public boolean onEntitySwing(EntityLiving entityLiving, ItemStack stack) {
		Vec3 position = entityLiving.worldObj.getWorldVec3Pool().getVecFromPool(entityLiving.posX, entityLiving.posY, entityLiving.posZ);
		if (!entityLiving.worldObj.isRemote) {
			// Because in server worlds the Y coordinate of a player is his feet's coordinate, without yOffset.
			position = position.addVector(0, 1.62D, 0);
		}
		Vec3 look = entityLiving.getLookVec();
        Vec3 reach = position.addVector(look.xCoord * playerReach, look.yCoord * playerReach, look.zCoord * playerReach);
        MovingObjectPosition hit = entityLiving.worldObj.rayTraceBlocks(position, reach);
        if (hit != null) {
        	int x = hit.blockX;
			int y = hit.blockY;
			int z = hit.blockZ;
	        int hitID = entityLiving.worldObj.getBlockId(x, y, z);
	        if (hitID == Block.stone.blockID) {
	        	entityLiving.worldObj.setBlock(x, y, z, Block.waterMoving.blockID, 0, 3);
	        }
        }
		return super.onEntitySwing(entityLiving, stack);
	}
	
	public static List<SoundPoint> initSoundPoints(World world) {
		List<SoundPoint> soundPoints = new ArrayList<SoundPoint>();
		for (Object curPlayer : world.playerEntities) {
			SoundPoint sp = new SoundPoint();
			sp.player = (EntityPlayer) curPlayer;
			sp.expandedAABB = sp.player.boundingBox.expand(20, 20, 20);
			soundPoints.add(sp);
		}
		return soundPoints;
	}
	
	/** If a pair of SoundPoints is too close, one of them will be removed,
	 * and the other positioned between them. */
	public static void optimizeSoundPoints(List<SoundPoint> soundPoints) {
		Iterator<SoundPoint> iterSP = soundPoints.iterator();
		while (iterSP.hasNext()) {
			SoundPoint sp = iterSP.next();
			if (sp.coords == null) {
				iterSP.remove();
			} else {
				for (SoundPoint sp2 : soundPoints) {
					if (sp2.coords == null) {
						continue;
					}
					if (sp2 != sp && sp.coords.distanceTo(sp2.coords) < soundDistanceThreshold) {
						sp2.coords.x = (sp.coords.x + sp2.coords.x)/2;
						sp2.coords.y = (sp.coords.y + sp2.coords.y)/2;
						sp2.coords.z = (sp.coords.z + sp2.coords.z)/2;
						iterSP.remove();
						break;
					}
				}
			}
		}
	}
	
	public static boolean isWaterOrBlockerBlock(int blockID) {
		return blockID == Block.waterStill.blockID
				|| blockID == Block.waterMoving.blockID
				|| blockID == MosesMod.waterBlocker.blockID;
	}
}
