package hunternif.mc.moses.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class TransparentBlock extends Block {

	//TODO: fix lighting at the sea bottom.
	public TransparentBlock(Material material) {
		super(material);
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
    public int quantityDropped(Random par1Random) {
        return 0;
    }
	
	@Override
	public int getRenderType() {
        return -1;
    }
	
	@Override
	public boolean isCollidable() {
        return false;
    }

}
