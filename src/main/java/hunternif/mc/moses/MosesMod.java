package hunternif.mc.moses;

import hunternif.mc.moses.block.BlockBlood;
import hunternif.mc.moses.block.TransparentBlock;
import hunternif.mc.moses.data.MosesBlockProvider;
import hunternif.mc.moses.item.BurntStaffOfMoses;
import hunternif.mc.moses.item.StaffOfMoses;
import hunternif.mc.moses.material.MaterialWaterBlocker;
import hunternif.mc.moses.util.Log;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid=MosesMod.ID, name=MosesMod.NAME, version=MosesMod.VERSION)
public class MosesMod {
	public static final String ID = "mosesmod";
	public static final String NAME = "Moses Mod";
	public static final String CHANNEL = ID;
	public static final String VERSION = "@VERSION@";
	
	public static final String KEY_PASSAGE_HALF_WIDTH = "mosesPassageHalfWidth";
	public static final String KEY_PASSAGE_LENGTH = "mosesPassageLength";
	public static final String KEY_BLOOD_PUDDLE_RADIUS = "bloodPuddleRadius";
	
	public static StaffOfMoses staffOfMoses;
	public static BurntStaffOfMoses burntStaffOfMoses;
	
	private static int passageHalfWidth;
	public static double passageLength;
	
	public static Block waterBlocker;
	public static Material materialWaterBlocker = new MaterialWaterBlocker();
	public static BlockFluidClassic blockBlood;
	public static Fluid blood;
	public static int bloodPuddleRadius;
	
	public static MosesBlockProvider mosesBlockProvider = new MosesBlockProvider();
	
	public static Configuration config;
	
	@Instance(ID)
	public static MosesMod instance;
	
	@SidedProxy(clientSide="hunternif.mc.moses.MosesClientProxy", serverSide="hunternif.mc.moses.MosesCommonProxy")
	public static MosesCommonProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Log.setModID(ID);
		proxy.preInit();
		
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		Property propHalfWidth = config.get(Configuration.CATEGORY_GENERAL, KEY_PASSAGE_HALF_WIDTH, 2);
		propHalfWidth.comment = "Maximum width of a passage is 2*N + 1, where N is this value.";
		passageHalfWidth = propHalfWidth.getInt(2);
		
		Property propLength = config.get(Configuration.CATEGORY_GENERAL, KEY_PASSAGE_LENGTH, 64.0d);
		propLength.comment = "Maximum length of one passage.";
		passageLength  = propLength.getDouble(64.0d);
		
		Property propBloodPuddleRadius = config.get(Configuration.CATEGORY_GENERAL, KEY_BLOOD_PUDDLE_RADIUS, 16);
		propBloodPuddleRadius.comment = "Radius of the puddle of blood which is created when hitting water with the Staff.";
		bloodPuddleRadius = propBloodPuddleRadius.getInt(16);
		config.save();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		staffOfMoses = (StaffOfMoses) new StaffOfMoses().setCreativeTab(CreativeTabs.tabTools).setUnlocalizedName("staffOfMoses");
		GameRegistry.registerItem(staffOfMoses, "staffOfMoses");
		staffOfMoses.passageHalfWidth = passageHalfWidth;
		staffOfMoses.maxPassageLength = passageLength;
		staffOfMoses.bloodPuddleRadius = bloodPuddleRadius;
		
		burntStaffOfMoses = (BurntStaffOfMoses) new BurntStaffOfMoses().setCreativeTab(CreativeTabs.tabTools).setUnlocalizedName("burntStaffOfMoses");
		GameRegistry.registerItem(burntStaffOfMoses, "burntStaffOfMoses");
		burntStaffOfMoses.passageHalfWidth = passageHalfWidth;
		burntStaffOfMoses.maxPassageLength = passageLength;
		burntStaffOfMoses.bloodPuddleRadius = bloodPuddleRadius;
		
		waterBlocker = new TransparentBlock(materialWaterBlocker).setUnlocalizedName("mosesWaterBlocker").setStepSound(Block.soundTypeGlass).setCreativeTab(CreativeTabs.tabMisc);
		GameRegistry.registerBlock(waterBlocker, "mosesWaterBlocker");
		
		//TODO figure out model files for liquids
		blood = new Fluid("mosesBlood", new ResourceLocation(MosesMod.ID, "blocks/blood_still"), new ResourceLocation(MosesMod.ID, "blocks/blood_flow"));
		
		FluidRegistry.registerFluid(blood);
		blockBlood = new BlockBlood(blood, Material.water);
		blockBlood.setUnlocalizedName("mosesBlood");
		GameRegistry.registerBlock(blockBlood, "mosesBlood");
		
		proxy.init();
		ItemWatcher serverItemWatcher = new ItemWatcher();
		MinecraftForge.EVENT_BUS.register(serverItemWatcher);
		FMLCommonHandler.instance().bus().register(serverItemWatcher);
		
		PlayerWatcher playerWatcher = new PlayerWatcher();
		MinecraftForge.EVENT_BUS.register(playerWatcher);
		FMLCommonHandler.instance().bus().register(playerWatcher);
	}
}