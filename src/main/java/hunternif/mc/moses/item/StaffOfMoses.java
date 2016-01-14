package hunternif.mc.moses.item;

import hunternif.mc.moses.MosesMod;
import hunternif.mc.moses.Sound;
import hunternif.mc.moses.util.BlockUtil;
import hunternif.mc.moses.util.IntVec3;
import hunternif.mc.moses.util.Log;
import hunternif.mc.moses.util.SoundPoint;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class StaffOfMoses extends Item {
	public double playerReach = 5;
	public double maxReach = 10;
	public int bloodPuddleRadius = 16;
	
	public int passageHalfWidth = 2;
	public double maxPassageLength = 64;
	private double lookBehindPassageLength = 4;
	
	public StaffOfMoses() {
		setMaxStackSize(1);
		setFull3D();
	}
	
	protected boolean isRemovableBlock(Block block) {
		return block == Blocks.water || block == Blocks.flowing_water ||
				block == MosesMod.blockBlood;
	}
	
	protected boolean isRemovableOrBlockerBlock(Block block) {
		return isRemovableBlock(block) || block == MosesMod.waterBlocker;
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
        Block hitBlock = BlockUtil.getBlock(world, rayPosition);
        if (!BlockUtil.isAir(world, rayPosition) && isRemovableBlock(hitBlock)) {
        	BlockPos coords = new BlockPos(rayPosition).up();
			// Do sea parting
			// 1. Find surface
			while (true) {
				Block block = world.getBlockState(coords).getBlock();
				if (!isRemovableBlock(block)) {
					break;
				} else if (coords.getY() < world.getHeight()){
					coords = coords.up();
				} else {
					break;
				}
			}
			
			List<SoundPoint> soundPoints = SoundPoint.initSoundPoints(world);
			
			// 2. Perform the wonder of sea parting
			Vec3 lookXZ = new Vec3(-MathHelper.sin(player.rotationYaw*(float)Math.PI/180f), 0, MathHelper.cos(player.rotationYaw*(float)Math.PI/180f));
			Vec3 start = new Vec3((double)coords.getX()+0.5, (double)coords.getY()+0.5, (double)coords.getZ()+0.5);
			
			// Make a short passage in the opposite direction to reach the player:
			Vec3 startABitAhead = start.addVector(lookXZ.xCoord*2, 0, lookXZ.zCoord*2);
			Vec3 reversedLookXZ = new Vec3(-lookXZ.xCoord, 0, -lookXZ.zCoord);
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
					boolean isLava = BlockUtil.isLava(sp.block);
					String sound = isLava ? Sound.LAVA_PARTING.getName() : Sound.SEA_PARTING.getName();
					world.playSoundEffect(sp.coords.x, sp.coords.y, sp.coords.z, sound, 1, 1);
				}
			}
		} else {
			// Close all passages
			MosesMod.mosesBlockProvider.restoreAllOwnedBlocksAndPlaySound(player.getEntityId());
		}
		return itemStack;
	}
	
	public void removeWaterBelow(World world, EntityPlayer player, BlockPos coords, List<SoundPoint> soundPoints) {
		BlockPos pos = coords.up();
		if (world.getBlockState(pos).getBlock() == Blocks.waterlily) {
			world.setBlockState(pos, Blocks.air.getDefaultState(), 2);
			EntityItem entityLily = new EntityItem(world, (double)coords.getX()+0.5, (double)coords.getY(), (double)coords.getZ()+0.5, new ItemStack(Blocks.waterlily));
			world.spawnEntityInWorld(entityLily);
		}
		pos = pos.down();
		while (true) {
			Block block = world.getBlockState(pos).getBlock();
			if (isRemovableOrBlockerBlock(block)) {
				clearWaterBlock(world, player, pos, soundPoints);
			} else if (block != Blocks.air) {
				break;
			}
			pos = pos.down();
		}
	}
	
	public void clearWaterBlock(World world, EntityPlayer player, BlockPos pos, List<SoundPoint> soundPoints) {
		Block block = world.getBlockState(pos).getBlock();
		IntVec3 coords = new IntVec3(pos.getX(), pos.getY(), pos.getZ());
		if (isRemovableOrBlockerBlock(block)) {
			MosesMod.mosesBlockProvider.clearBlockAt(world, player.getEntityId(), coords);
			if (isRemovableBlock(block)) {
				Vec3 vector = new Vec3(coords.x, coords.y, coords.z);
				// Check if we hit any player BBs, but only for non-empty blocks
				if (block != MosesMod.waterBlocker) {
					for (SoundPoint sp : soundPoints) {
						if (sp.expandedAABB.isVecInside(vector)) {
							Vec3 playerPos = getPlayerPosition(sp.player);
							double distance = vector.distanceTo(playerPos);
							if (sp.coords == null || distance < sp.distanceToPlayer) {
								sp.coords = coords.copy();
								sp.distanceToPlayer = distance;
								sp.block = block;
							}
						}
					}
				}
			}
		}
	}
	
	
	private Map<EntityLivingBase, Integer> swungOnTicks = new ConcurrentHashMap<EntityLivingBase, Integer>();
	/**
	 * Because there is no method for left-clicking blocks.
	 */
	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
		if (entityLiving.worldObj.isRemote) {
			return false;
		}
		// For some reason this method is called several times when breaking blocks,
		// which causes water out of stone to be turned into blood immediately.
		// To prevent this only proceed if last time was at least 2 ticks ago:
		Integer lastSwing = swungOnTicks.get(entityLiving);
		if (lastSwing == null || entityLiving.ticksExisted - lastSwing.intValue() > 2) {
			lastSwing = Integer.valueOf(entityLiving.ticksExisted);
			swungOnTicks.put(entityLiving, lastSwing);
		} else {
			return true;
		}
		Vec3 position = new Vec3(entityLiving.posX, entityLiving.posY, entityLiving.posZ);
		// Because in server worlds the Y coordinate of a player is his feet's coordinate, without yOffset.
		position = position.addVector(0, 1.62D, 0);
		Vec3 look = entityLiving.getLookVec();
        Vec3 reach = position.addVector(look.xCoord * playerReach, look.yCoord * playerReach, look.zCoord * playerReach);
        MovingObjectPosition hit = entityLiving.worldObj.rayTraceBlocks(position, reach, true); //raytrace
        if (hit != null) {
        	BlockPos pos = hit.getBlockPos();
	        Block hitBlock = entityLiving.worldObj.getBlockState(pos).getBlock();
	        if (hitBlock == Blocks.stone) {
	        	// Makes a source of water out of stone:
        		entityLiving.worldObj.setBlockState(pos, Blocks.flowing_water.getDefaultState(), 3);
        		Log.info(String.format("Made water out of stone at %s", pos));
	        } else if (BlockUtil.isWater(hitBlock)) {
	        	// Turn water into blood:
        		replaceWaterWithBlood(entityLiving.worldObj, pos.getX(), pos.getZ());
        		Log.info(String.format("Replaced water with blood at (%d, %d)", pos.getX(), pos.getZ()));
        		entityLiving.worldObj.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), Sound.BLOOD.getName(), 0.7f, 1);
	        } else if (hitBlock == MosesMod.blockBlood) {
	        	// Turn blood back into water:
	        	replaceBloodWithWater(entityLiving.worldObj, pos.getX(), pos.getZ());
        		Log.info(String.format("Replaced blood with water at (%d, %d)", pos.getX(), pos.getZ()));
	        }
        }
		return false;
	}
	
	public void createPassage(World world, EntityPlayer player, Vec3 lookXZ,
			double maxPassageLength, Vec3[] wavefront, List<SoundPoint> soundPoints) {
		Vec3 startPos = wavefront[MathHelper.floor_float((float)wavefront.length/2f)].addVector(0, 0, 0);
		for (int i = 0; i < wavefront.length; i++) {
			while (wavefront[i].distanceTo(startPos) < maxPassageLength
					&& !BlockUtil.isSolid(world, wavefront[i])) {
				removeWaterBelow(world, player, new BlockPos(wavefront[i]), soundPoints);
				// We move half-step at a time so that there won't be any omitted columns of water left behind.
				wavefront[i] = wavefront[i].addVector(lookXZ.xCoord/2, 0, lookXZ.zCoord/2);
			}
		}
	}
	
	public static Vec3 getPlayerPosition(EntityPlayer player) {
		Vec3 position = new Vec3(player.posX, player.posY, player.posZ);
		if (!player.worldObj.isRemote) {
			// Because in server worlds the Y coordinate of a player is his feet's coordinate, without yOffset.
			position = position.addVector(0, 1.62D, 0);
		}
		return position;
	}
	
	/** Replaces all water with blood in a diamond-shaped area. */
	protected void replaceWaterWithBlood(World world, int x, int z) {
		for (int dx = 0; dx <= bloodPuddleRadius; dx++) {
			for (int dz = bloodPuddleRadius - dx; dz >= 0; dz--) {
				replaceWaterWithBloodInColumn(world, x + dx, z + dz);
				replaceWaterWithBloodInColumn(world, x - dx, z + dz);
				replaceWaterWithBloodInColumn(world, x + dx, z - dz);
				replaceWaterWithBloodInColumn(world, x - dx, z - dz);
			}
		}
	}
	private static void replaceWaterWithBloodInColumn(World world, int x, int z) {
		for (BlockPos pos = new BlockPos(x, 0, z); pos.getY() < world.getHeight(); pos = pos.up()) {
			IBlockState state = world.getBlockState(pos);
			Material material = state.getBlock().getMaterial();
			if (material == Material.water) {
				int metadata = state.getBlock().getMetaFromState(state);
				state = MosesMod.blockBlood.getStateFromMeta(metadata);
				world.setBlockState(pos, state, 3);
			}
		}
	}
	
	protected void replaceBloodWithWater(World world, int x, int z) {
		for (int dx = 0; dx <= bloodPuddleRadius; dx++) {
			for (int dz = bloodPuddleRadius - dx; dz >= 0; dz--) {
				replaceBloodWithWaterInColumn(world, x + dx, z + dz);
				replaceBloodWithWaterInColumn(world, x - dx, z + dz);
				replaceBloodWithWaterInColumn(world, x + dx, z - dz);
				replaceBloodWithWaterInColumn(world, x - dx, z - dz);
			}
		}
	}
	private static void replaceBloodWithWaterInColumn(World world, int x, int z) {
		for (BlockPos pos = new BlockPos(x, 0, z); pos.getY() < world.getHeight(); pos = pos.up()) {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() == MosesMod.blockBlood) {
				int metadata = MosesMod.blockBlood.getMetaFromState(state);
				Block block = MosesMod.blockBlood.isSourceBlock(world, pos) ?
						Blocks.water : Blocks.flowing_water;
				state = block.getStateFromMeta(metadata);
				world.setBlockState(pos, state, 3);
			}
		}
	}
}
