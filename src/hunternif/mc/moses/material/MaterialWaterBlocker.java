package hunternif.mc.moses.material;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class MaterialWaterBlocker extends Material {

	public MaterialWaterBlocker() {
		super(MapColor.airColor);
		setImmovableMobility();
	}

	@Override
	public boolean blocksMovement() {
		return true;
	}
	
	@Override
	public boolean isReplaceable() {
		return true;
	}
	
	@Override
	public boolean isSolid() {
		return false;
	}
}
