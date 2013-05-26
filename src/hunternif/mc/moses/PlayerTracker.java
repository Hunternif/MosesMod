package hunternif.mc.moses;

import hunternif.mc.moses.item.StaffOfMoses;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.IPlayerTracker;

public class PlayerTracker implements IPlayerTracker {

	@Override
	public void onPlayerLogin(EntityPlayer player) {
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		// TODO close all of this player's passages
		if (!player.worldObj.isRemote) {
			((StaffOfMoses)MosesMod.staffOfMoses).restoreWater(player.worldObj, player);
		}
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
	}

}
