package hunternif.mc.moses;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Removes all passages when the player logs out.
 * @author Hunternif
 */
public class PlayerWatcher {

	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event) {
		//TODO: Test player logout. This should work in SMP
		MosesMod.mosesBlockProvider.restoreAllOwnedBlocksAndPlaySound(
				event.player.worldObj, event.player.getEntityId());
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onWorldUnload(WorldEvent.Unload event) {
		// This is for SP
		if (!event.world.isRemote) {
			MosesMod.mosesBlockProvider.restoreAllOwnedBlocksAndPlaySound(
					event.world, Minecraft.getMinecraft().thePlayer.getEntityId());
			//TODO: blocks are restored but not saved when the player exits.
		}
	}

}
