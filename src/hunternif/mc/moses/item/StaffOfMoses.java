package hunternif.mc.moses.item;

import hunternif.mc.moses.MosesMod;
import hunternif.mc.moses.MosesSounds;
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
	
	protected boolean isRemovableBlock(int blockID) {
		return blockID == Block.waterMoving.blockID || blockID == Block.waterStill.blockID;
	}
	
	protected boolean isRemovableOrBlockerBlock(int blockID) {
		return isRemovableBlock(blockID) || blockID == MosesMod.waterBlocker.blockID;
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
		if (world.isRemote) {
			return itemStack;
		}
		Vec3 position = getPlayerPosition(player);
		Vec3 look = player.getLookVec();
        Vec3 rayPosition = position.addVector(look.xCoord, look.yCoord, look.zCoord);
        double distanceTraced = 1;
        while (BlockUtil.isAir(world, rayPosition) && distanceTraced < maxReach) {
        	rayPosition = rayPosition.addVector(look.xCoord, look.yCoord, look.zCoord);
        	distanceTraced++;
        }
        int hitID = BlockUtil.getBlockID(world, rayPosition);
        if (!BlockUtil.isAir(world, rayPosition) && isRemovableBlock(hitID)) {
        	IntVec3 coords = new IntVec3(rayPosition);
			// Do sea parting
			// 1. Find surface
			while (true) {
				int blockID = world.getBlockId(coords.x, coords.y+1, coords.z);
				if (!isRemovableBlock(blockID)) {
					break;
				} else if (coords.y < world.getHeight() - 1){
					coords.y++;
				} else {
					break;
				}
			}
			
			List<SoundPoint> soundPoints = SoundPoint.initSoundPoints(world);
			
			// 2. Perform the wonder of sea parting
			Vec3 lookXZ = world.getWorldVec3Pool().getVecFromPool(-MathHelper.sin(player.rotationYaw*(float)Math.PI/180f), 0, MathHelper.cos(player.rotationYaw*(float)Math.PI/180f));
			Vec3 start = world.getWorldVec3Pool().getVecFromPool((double)coords.x+0.5, (double)coords.y+0.5, (double)coords.z+0.5);
			
			// Make a short passage in the opposite direction to reach the player:
			Vec3 startABitAhead = start.addVector(lookXZ.xCoord*2, 0, lookXZ.zCoord*2);
			Vec3 reversedLookXZ = world.getWorldVec3Pool().getVecFromPool(-lookXZ.xCoord, 0, -lookXZ.zCoord);
			Vec3[] wavefront = BlockUtil.buildFlatWaveFront(startABitAhead, reversedLookXZ, passageHalfWidth);
			createPassage(world, player, reversedLookXZ, lookBehindPassageLength, wavefront, soundPoints);
			
			// Regular passage ahead:
			wavefront = BlockUtil.buildFlatWaveFront(start, lookXZ, passageHalfWidth);
			createPassage(world, player, lookXZ, maxPassageLength-2, wavefront, soundPoints);
			
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
			MosesMod.mosesBlockProvider.restoreAllOwnedBlocksAndPlaySound(world, player.entityId);
		}
		return itemStack;
	}
	
	public void removeWaterBelow(World world, EntityPlayer player, IntVec3 coords, List<SoundPoint> soundPoints) {
		if (world.getBlockId(coords.x, coords.y+1, coords.z) == Block.waterlily.blockID) {
			world.setBlock(coords.x, coords.y+1, coords.z, 0, 0, 2);
			EntityItem entityLily = new EntityItem(world, (double)coords.x+0.5, (double)coords.y+1, (double)coords.z+0.5, new ItemStack(Block.waterlily));
			entityLily.delayBeforeCanPickup = 10;
			world.spawnEntityInWorld(entityLily);
		}
		IntVec3 coordsCopy = coords.copy();
		while (true) {
			int blockID = world.getBlockId(coordsCopy.x, coordsCopy.y, coordsCopy.z);
			if (isRemovableOrBlockerBlock(blockID)) {
				clearWaterBlock(world, player, coordsCopy, soundPoints);
			} else if (blockID != 0) {
				break;
			}
			coordsCopy.y--;
		}
	}
	
	public void clearWaterBlock(World world, EntityPlayer player, IntVec3 coords, List<SoundPoint> soundPoints) {
		int blockID = world.getBlockId(coords.x, coords.y, coords.z);
		if (isRemovableOrBlockerBlock(blockID)) {
			MosesMod.mosesBlockProvider.clearBlockAt(world, player.entityId, coords);
			if (isRemovableBlock(blockID)) {
				Vec3 vector = coords.toVec3(world.getWorldVec3Pool());
				// Check if we hit any player BBs, but only for non-empty blocks
				if (blockID != MosesMod.waterBlocker.blockID)
				for (SoundPoint sp : soundPoints) {
					if (sp.expandedAABB.isVecInside(vector)) {
						Vec3 playerPos = getPlayerPosition(sp.player);
						double distance = vector.distanceTo(playerPos);
						if (sp.coords == null || distance < sp.distanceToPlayer) {
							sp.coords = coords.copy();
							sp.distanceToPlayer = distance;
							sp.blockId = blockID;
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
				removeWaterBelow(world, player, new IntVec3(wavefront[i]), soundPoints);
				// We move half-step at a time so that there won't be any omitted columns of water left behind.
				wavefront[i] = wavefront[i].addVector(lookXZ.xCoord/2, 0, lookXZ.zCoord/2);
			}
		}
	}
	
	public static Vec3 getPlayerPosition(EntityPlayer player) {
		Vec3 position = player.worldObj.getWorldVec3Pool().getVecFromPool(player.posX, player.posY, player.posZ);
		if (!player.worldObj.isRemote) {
			// Because in server worlds the Y coordinate of a player is his feet's coordinate, without yOffset.
			position = position.addVector(0, 1.62D, 0);
		}
		return position;
	}
}
