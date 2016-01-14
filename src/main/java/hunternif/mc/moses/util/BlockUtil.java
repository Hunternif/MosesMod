package hunternif.mc.moses.util;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public final class BlockUtil {
	public static boolean isSolid(World world, Vec3 vec) {
		return world.getBlockState(new BlockPos(vec)).getBlock().getMaterial().isSolid();
	}
	
	public static boolean isAir(World world, Vec3 vec) {
		return world.isAirBlock(new BlockPos(vec));
	}
	
	public static boolean isWater(Block block) {
		return block == Blocks.water || block == Blocks.flowing_water;
	}
	
	public static boolean isLava(Block block) {
		return block == Blocks.lava || block == Blocks.flowing_lava;
	}
	
	public static Block getBlock(World world, Vec3 vec) {
		return world.getBlockState(new BlockPos(vec)).getBlock();
	}
	
	public static final Vec3 UNIT_Y_UP = new Vec3(0, 1, 0);
	public static final Vec3 UNIT_Y_DOWN = new Vec3(0, -1, 0);
	
	/**
	 * Returns an array of (halfWidth*2 + 1) vectors parallel to lookXZ and
	 * arranged at in a row* perpendicular to it at a distance of 1 block from
	 * each other.
	 */
	public static Vec3[] buildFlatWaveFront(Vec3 start, Vec3 lookXZ, int halfWidth) {
		Vec3[] wavefront = new Vec3[halfWidth*2+1];
		Vec3 startCopy = start.addVector(0, 0, 0);
		Vec3 lookOrtogonal = lookXZ.crossProduct(UNIT_Y_UP);
		wavefront[halfWidth] = startCopy;
		for (int i = 0; i < halfWidth; i++) {
			Vec3 newVec = startCopy;
			for (int j = 0; j < i+1; j++) {
				newVec = newVec.addVector(lookOrtogonal.xCoord, lookOrtogonal.yCoord, lookOrtogonal.zCoord);
			}
			wavefront[halfWidth-1-i] = newVec;
		}
		lookOrtogonal = lookXZ.crossProduct(UNIT_Y_DOWN);
		for (int i = 0; i < halfWidth; i++) {
			Vec3 newVec = startCopy;
			for (int j = 0; j < i+1; j++) {
				newVec = newVec.addVector(lookOrtogonal.xCoord, lookOrtogonal.yCoord, lookOrtogonal.zCoord);
			}
			wavefront[halfWidth+1+i] = newVec;
		}
		return wavefront;
	}
}
