package hunternif.mc.moses.item;

import net.minecraft.block.Block;

public class BurntStaffOfMoses extends StaffOfMoses {

	public BurntStaffOfMoses(int id) {
		super(id);
	}
	
	@Override
	protected boolean isWaterBlock(int blockID) {
		return super.isWaterBlock(blockID) ||
				blockID == Block.lavaMoving.blockID ||
				blockID == Block.lavaStill.blockID;
	}
}
