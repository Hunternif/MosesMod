package hunternif.mc.moses;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class MosesClientProxy extends MosesCommonProxy {
	@Override
	public void preInit() {
		super.preInit();
	}
	
	@Override
	public void init() {
		super.init();
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(MosesMod.staffOfMoses, 0, new ModelResourceLocation(MosesMod.ID + ":staffOfMoses", "inventory"));
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(MosesMod.burntStaffOfMoses, 0, new ModelResourceLocation(MosesMod.ID + ":burntStaffOfMoses", "inventory"));
	}
}
