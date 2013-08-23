package hunternif.mc.moses.util;

import net.minecraft.block.Block;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public final class BlockUtil {
	public static boolean isSolid(World world, Vec3 vec) {
		IntVec3 intVec = new IntVec3(vec);
		return world.getBlockMaterial(intVec.x, intVec.y, intVec.z).isSolid();
	}
	
	public static boolean isAir(World world, Vec3 vec) {
		IntVec3 intVec = new IntVec3(vec);
		return world.isAirBlock(intVec.x, intVec.y, intVec.z);
	}
	
	public static boolean isWater(int blockID) {
		return blockID == Block.waterMoving.blockID || blockID == Block.waterStill.blockID;
	}
	
	public static boolean isLava(int blockID) {
		return blockID == Block.lavaMoving.blockID || blockID == Block.lavaStill.blockID;
	}
	
	public static int getBlockID(World world, Vec3 vec) {
		IntVec3 intVec = new IntVec3(vec);
		return world.getBlockId(intVec.x, intVec.y, intVec.z);
	}
	
	/**
	 * Returns an array of (halfWidth*2 + 1) vectors parallel to lookXZ and
	 * arranged at in a row* perpendicular to it at a distance of 1 block from
	 * each other.
	 */
	public static Vec3[] buildFlatWaveFront(Vec3 start, Vec3 lookXZ, int halfWidth) {
		Vec3[] wavefront = new Vec3[halfWidth*2+1];
		Vec3 startCopy = start.addVector(0, 0, 0);
		Vec3 lookOrtogonal = MathUtil.crossProduct(lookXZ, lookXZ.myVec3LocalPool.getVecFromPool(0, 1, 0));
		wavefront[halfWidth] = startCopy;
		for (int i = 0; i < halfWidth; i++) {
			Vec3 newVec = startCopy;
			for (int j = 0; j < i+1; j++) {
				newVec = newVec.addVector(lookOrtogonal.xCoord, lookOrtogonal.yCoord, lookOrtogonal.zCoord);
			}
			wavefront[halfWidth-1-i] = newVec;
		}
		lookOrtogonal = MathUtil.crossProduct(lookXZ, lookXZ.myVec3LocalPool.getVecFromPool(0, -1, 0));
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
