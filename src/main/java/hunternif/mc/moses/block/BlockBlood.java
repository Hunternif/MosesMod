package hunternif.mc.moses.block;

import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockBlood extends BlockFluidClassic {

	public BlockBlood(Fluid fluid, Material material) {
		super(fluid, material);
	}
	
	@Override
	public int getLightOpacity(IBlockAccess world, BlockPos pos) {
		return 128;
	}
}
