package hunternif.mc.moses;

import java.util.logging.Level;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.common.FMLLog;

public class MosesClientProxy extends MosesCommonProxy {
	@Override
	public void registerSounds() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@ForgeSubscribe
	public void onSound(SoundLoadEvent event) {
        try {
        	for (Sound sound : Sound.values()) {
        		event.manager.soundPoolSounds.addSound(sound.getName()+".wav");
        	}
        }
        catch (Exception e) {
        	FMLLog.log(MosesMod.ID, Level.WARNING, ": Failed to register one or more sounds.");
        }
    }
}
