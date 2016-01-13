package hunternif.mc.moses.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TransparentBlock extends Block {

	public TransparentBlock(Material material) {
		super(material);
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
        return false;
    }
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
    public int quantityDropped(Random par1Random) {
        return 0;
    }
	
	/*@Override
	public int getRenderType() {
        return -1;
    }*/
	
	@Override
	public boolean isCollidable() {
        return false;
    }
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
        return null;
    }
	
	@Override
	public void registerBlockIcons(IIconRegister p_149651_1_) {
		// Nop
		// TODO: when placing blocks inside a pink-and-black-textured block is spawned at player location.
	}

}
