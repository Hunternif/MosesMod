package hunternif.mc.moses;

import net.minecraftforge.common.MinecraftForge;

public class MosesClientProxy extends MosesCommonProxy {
	@Override
	public void registerSounds() {
		MinecraftForge.EVENT_BUS.register(new MosesSounds());
	}
}
