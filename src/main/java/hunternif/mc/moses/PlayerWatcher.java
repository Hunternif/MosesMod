package hunternif.mc.moses;

import hunternif.mc.moses.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Removes all passages when the player logs out.
 * @author Hunternif
 */
public class PlayerWatcher {

	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event) {
		// This is for multiplayer
		Log.info("Player %s logged out. Restoring their passages...", event.player.getGameProfile().getName());
		MosesMod.mosesBlockProvider.restoreAllOwnedBlocksAndPlaySound(event.player.getEntityId());
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onWorldUnload(WorldEvent.Unload event) {
		// This is for singleplayer
		if (!event.world.isRemote) {
			Log.info("Exiting world. Restoring passages...");
			MosesMod.mosesBlockProvider.restoreAllOwnedBlocksAndPlaySound(Minecraft.getMinecraft().thePlayer.getEntityId());
			try {
				((WorldServer) event.world).saveAllChunks(true, null);
			} catch (MinecraftException ex) {
				Log.error(ex, "Saving world after restoring passages");
			}
		}
	}

}
