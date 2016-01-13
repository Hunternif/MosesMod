package hunternif.mc.moses.block;

import hunternif.mc.moses.MosesMod;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBlood extends BlockFluidClassic {
	
	private IIcon[] icons = new IIcon[2];

	public BlockBlood(Fluid fluid, Material material) {
		super(fluid, material);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		icons = new IIcon[] {
			iconRegister.registerIcon(MosesMod.ID + ":blood_still"),
			iconRegister.registerIcon(MosesMod.ID + ":blood_flow")
		};
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side != 0 && side != 1 ? this.icons[1] : this.icons[0];
	}
	
	@Override
	public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
		return 128;
	}
}
