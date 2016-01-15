package hunternif.mc.moses.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class TransparentBlock extends Block {

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
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
		return null;
	}

}
