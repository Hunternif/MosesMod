package hunternif.mc.moses.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class SoundPoint {
	public static double soundDistanceThreshold = 10;
	
	public IntVec3 coords;
	public double distanceToPlayer = 1000;
	public EntityPlayer player;
	public Block block;
	public AxisAlignedBB expandedAABB;
	
	public static List<SoundPoint> initSoundPoints(World world) {
		List<SoundPoint> soundPoints = new ArrayList<SoundPoint>();
		for (Object curPlayer : world.playerEntities) {
			SoundPoint sp = new SoundPoint();
			sp.player = (EntityPlayer) curPlayer;
			sp.expandedAABB = sp.player.getEntityBoundingBox().expand(12, 10, 12);
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
						// blockId is inherited from sp2. Dunno if that's the best thing to do.
						iterSP.remove();
						break;
					}
				}
			}
		}
	}
}
