package hunternif.mc.moses.item;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class BurntStaffOfMoses extends StaffOfMoses {

	public BurntStaffOfMoses() {
		super();
	}
	
	@Override
	protected boolean isRemovableBlock(Block block) {
		return super.isRemovableBlock(block) ||
				block == Blocks.lava || block == Blocks.flowing_lava;
	}
}
