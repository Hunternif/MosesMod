package hunternif.mc.moses;

import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.common.FMLLog;

public class MosesClientProxy extends MosesCommonProxy {
	/*@Override
	public void registerRenderers() {
		RenderBlocks renderBlocks = new RenderBlocks();
		MosesMod.blood.setStillIcon(renderBlocks.getBlockIcon(Block.waterStill));
		MosesMod.blood.setFlowingIcon(renderBlocks.getBlockIcon(Block.waterMoving));
	}*/
	
	@Override
	public void registerSounds() {
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
