package hunternif.mc.moses;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import hunternif.mc.moses.util.IntVec3;

public class SoundPoint {
	public IntVec3 coords;
	public double distanceToPlayer = 100;
	public EntityPlayer player;
	public AxisAlignedBB expandedAABB;
}
