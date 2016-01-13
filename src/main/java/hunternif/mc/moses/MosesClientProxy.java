package hunternif.mc.moses;

import net.minecraftforge.common.MinecraftForge;

public class MosesClientProxy extends MosesCommonProxy {
	@Override
	public void preInit() {
		super.preInit();
		MinecraftForge.EVENT_BUS.register(this);
	}
}
