package hunternif.mc.moses;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;

public class MosesClientProxy extends MosesCommonProxy {
	@Override
	public void preInit() {
		super.preInit();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@ForgeSubscribe
	public void onSound(SoundLoadEvent event) {
        try {
        	for (Sound sound : Sound.values()) {
        		event.manager.soundPoolSounds.addSound(sound.getName()+".ogg");
        	}
        }
        catch (Exception e) {
        	MosesMod.logger.warning(": Failed to register one or more sounds.");
        }
    }
}
