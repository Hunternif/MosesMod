package hunternif.mc.moses.item;

import hunternif.mc.moses.MosesMod;
import hunternif.mc.moses.MosesSounds;
import hunternif.mc.moses.data.MosesBlockData;
import hunternif.mc.moses.util.BlockUtil;
import hunternif.mc.moses.util.IntVec3;
import hunternif.mc.moses.util.SoundPoint;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
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
	
	public static double maxReach = 10;
	
	public int passageHalfWidth = 2;
	public double maxPassageLength = 64;
	private double lookBehindPassageLength = 4;
	
	public StaffOfMoses(int id) {
		super(id);
		setMaxStackSize(1);
		setFull3D();
	}
	
	protected boolean isWaterBlock(int blockID) {
		return blockID == Block.waterMoving.blockID || blockID == Block.waterStill.blockID;
	}
	
	protected boolean isWaterOrBlockerBlock(int blockID) {
		return isWaterBlock(blockID) || blockID == MosesMod.waterBlocker.blockID;
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
		} else {
			return itemStack;
		}
		Vec3 look = player.getLookVec();
        //Vec3 reach = position.addVector(look.xCoord * maxReach, look.yCoord * maxReach, look.zCoord * maxReach);
        //MovingObjectPosition hit = world.rayTraceBlocks_do(position, reach, true); // true to hit water too
        Vec3 rayPosition = position.addVector(look.xCoord, look.yCoord, look.zCoord);
        double distanceTraced = 1;
        while (BlockUtil.isAir(world, rayPosition) && distanceTraced < maxReach) {
        	rayPosition = rayPosition.addVector(look.xCoord, look.yCoord, look.zCoord);
        	distanceTraced++;
        }
        int hitID = BlockUtil.getBlockID(world, rayPosition);
        if (!BlockUtil.isAir(world, rayPosition) && isWaterBlock(hitID)) {
        	IntVec3 intVec = new IntVec3(rayPosition);
			int x = intVec.x;
			int y = intVec.y;
			int z = intVec.z;
			// Do sea parting
			// 1. Find surface
			while (true) {
				int blockID = world.getBlockId(x, y+1, z);
				if (!isWaterBlock(blockID)) {
					break;
				} else if (y < world.getHeight() - 1){
					y++;
				} else {
					break;
				}
			}
			
			List<SoundPoint> soundPoints = SoundPoint.initSoundPoints(world);
			
			// 2. Perform the wonder of sea parting
			Vec3 lookXZ = world.getWorldVec3Pool().getVecFromPool(-MathHelper.sin(player.rotationYaw*(float)Math.PI/180f), 0, MathHelper.cos(player.rotationYaw*(float)Math.PI/180f));
			Vec3 start = world.getWorldVec3Pool().getVecFromPool((double)x+0.5, (double)y+0.5, (double)z+0.5);
			
			// Make a short passage in the opposite direction to reach the player:
			Vec3 startABitAhead = start.addVector(lookXZ.xCoord*2, 0, lookXZ.zCoord*2);
			Vec3 reversedLookXZ = world.getWorldVec3Pool().getVecFromPool(-lookXZ.xCoord, 0, -lookXZ.zCoord);
			Vec3[] wavefront = BlockUtil.buildFlatWaveFront(startABitAhead, reversedLookXZ, passageHalfWidth);
			synchronized (this) {
				createPassage(world, player, reversedLookXZ, lookBehindPassageLength, wavefront, soundPoints);
				
				// Regular passage ahead:
				wavefront = BlockUtil.buildFlatWaveFront(start, lookXZ, passageHalfWidth);
				createPassage(world, player, lookXZ, maxPassageLength-2, wavefront, soundPoints);
			}
			
			// Remove the SoundPoints which are too close
			SoundPoint.optimizeSoundPoints(soundPoints);
			
			//TODO unify all this this sound point selection algorithm
			// Play all sounds
			for (SoundPoint sp : soundPoints) {
				if (sp.coords != null) {
					boolean isLava = BlockUtil.isLava(sp.blockId);
					String sound = isLava ? MosesSounds.LAVA_PARTING : MosesSounds.SEA_PARTING;
					world.playSoundEffect(sp.coords.x, sp.coords.y, sp.coords.z, sound, 1, 1);
				}
			}
		} else {
			// Close all passages
    		restoreWater(world, player);
		}
		return itemStack;
	}
	
	public void restoreWater(World world, EntityPlayer player) {
		List<MosesBlockData> list = MosesMod.mosesBlockProvider.getBlocksOwnedBy(world, player.entityId);
		MosesMod.mosesBlockProvider.restoreAllOwnedBlocksAndPlaySound(world, player.entityId);
	}
	
	public void removeWaterBelow(World world, EntityPlayer player, Vec3 vector, List<SoundPoint> soundPoints) {
		int x = MathHelper.floor_double(vector.xCoord);
		int y = MathHelper.floor_double(vector.yCoord);
		int z = MathHelper.floor_double(vector.zCoord);
		if (world.getBlockId(x, y+1, z) == Block.waterlily.blockID) {
			world.setBlock(x, y+1, z, 0, 0, 2);
			EntityItem entityLily = new EntityItem(world, (double)x+0.5, (double)y+1, (double)z+0.5, new ItemStack(Block.waterlily));
			entityLily.delayBeforeCanPickup = 10;
			world.spawnEntityInWorld(entityLily);
		}
		while (true) {
			int blockID = world.getBlockId(x, y, z);
			if (isWaterOrBlockerBlock(blockID)) {
				clearWaterBlock(world, player, x, y, z, soundPoints);
			} else if (blockID != 0) {
				break;
			}
			y--;
		}
	}
	
	public void clearWaterBlock(World world, EntityPlayer player, int x, int y, int z, List<SoundPoint> soundPoints) {
		int blockID = world.getBlockId(x, y, z);
		if (isWaterOrBlockerBlock(blockID)) {
			MosesMod.mosesBlockProvider.clearBlockAt(world, player.entityId, x, y, z);
			if (isWaterBlock(blockID)) {
				Vec3 vector = world.getWorldVec3Pool().getVecFromPool(x, y, z);
				// Check if we hit any player BBs
				for (SoundPoint sp : soundPoints) {
					if (sp.expandedAABB.isVecInside(vector)) {
						Vec3 playerPos = getPlayerPosition(world, sp.player);
						double distance = vector.distanceTo(playerPos);
						if (sp.coords == null || distance < sp.distanceToPlayer) {
							sp.coords = new IntVec3(x, y, z);
							sp.distanceToPlayer = distance;
							sp.blockId = world.getBlockId(sp.coords.x, sp.coords.y, sp.coords.z);
							// In order to play proper the sound, we must know the material of the block that was removed
							if (sp.blockId == MosesMod.waterBlocker.blockID) {
								MosesBlockData blockData = MosesMod.mosesBlockProvider.getBlockAt(world, sp.coords);
								if (blockData != null) {
									sp.blockId = blockData.prevBlockID;
								}
							}
						}
					}
				}
			}
		}
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
	
	public void createPassage(World world, EntityPlayer player, Vec3 lookXZ,
			double maxPassageLength, Vec3[] wavefront, List<SoundPoint> soundPoints) {
		Vec3 startPos = wavefront[MathHelper.floor_float((float)wavefront.length/2f)].addVector(0, 0, 0);
		for (int i = 0; i < wavefront.length; i++) {
			while (wavefront[i].distanceTo(startPos) < maxPassageLength
					&& !BlockUtil.isSolid(world, wavefront[i])) {
				Vec3 vec = wavefront[i];
				removeWaterBelow(world, player, vec, soundPoints);
				
				// We move half-step at a time so that there won't be any omitted columns of water left behind.
				wavefront[i] = vec.addVector(lookXZ.xCoord/2, 0, lookXZ.zCoord/2);
			}
		}
	}
	
	public static Vec3 getPlayerPosition(World world, EntityPlayer player) {
		Vec3 position = world.getWorldVec3Pool().getVecFromPool(player.posX, player.posY, player.posZ);
		if (!world.isRemote) {
			// Because in server worlds the Y coordinate of a player is his feet's coordinate, without yOffset.
			position = position.addVector(0, 1.62D, 0);
		}
		return position;
	}
}
