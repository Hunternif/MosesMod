package hunternif.mc.moses;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class MosesSounds {
	public static final String SEA_PARTING = "sea_parting";
	public static final String SEA_CLOSING = "sea_closing";
	public static final String MOSES = "moses";
	
	private static String[] sounds = {SEA_PARTING, SEA_CLOSING, MOSES};
	
	@ForgeSubscribe
	public void onSound(SoundLoadEvent event) {
        try {
        	for (int i = 0; i < sounds.length; i++) {
        		event.manager.soundPoolSounds.addSound(sounds[i]+".wav", MosesMod.class.getResource("/mods/"+MosesMod.ID+"/sounds/"+sounds[i]+".wav"));
        	}
        }
        catch (Exception e) {
            System.err.println(MosesMod.NAME + ": Failed to register one or more sounds.");
        }
    }
}
