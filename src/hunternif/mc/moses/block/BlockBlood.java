package hunternif.mc.moses.block;

import hunternif.mc.moses.MosesMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockBlood extends BlockFluidClassic {
	
	private Icon[] icons = new Icon[2];

	public BlockBlood(int id, Fluid fluid, Material material) {
		super(id, fluid, material);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		icons = new Icon[] {
			iconRegister.registerIcon(MosesMod.ID + ":blood_still"),
			iconRegister.registerIcon(MosesMod.ID + ":blood_flow")
		};
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata) {
		return side != 0 && side != 1 ? this.icons[1] : this.icons[0];
	}
	
	@Override
	public int getRenderBlockPass() {
		return 1;
	}
	
	@Override
	public int getRenderType() {
		return 4;
	}
}