package hunternif.mc.moses;

import hunternif.mc.moses.block.BlockBlood;
import hunternif.mc.moses.block.TransparentBlock;
import hunternif.mc.moses.data.MosesBlockProvider;
import hunternif.mc.moses.item.BurntStaffOfMoses;
import hunternif.mc.moses.item.StaffOfMoses;
import hunternif.mc.moses.material.MaterialWaterBlocker;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid=MosesMod.ID, name=MosesMod.NAME, version=MosesMod.VERSION)
@NetworkMod(clientSideRequired=true, serverSideRequired=true)
public class MosesMod {
	public static final String ID = "MosesMod";
	public static final String NAME = "Moses Mod";
	public static final String VERSION = "1.3.1";
	public static final String CHANNEL = ID;
	
	public static final String KEY_PASSAGE_HALF_WIDTH = "mosesPassageHalfWidth";
	public static final String KEY_PASSAGE_LENGTH = "mosesPassageLength";
	public static final String KEY_BLOOD_PUDDLE_RADIUS = "bloodPuddleRadius";
	
	public static Logger logger;
	
	public static List<Item> itemList = new ArrayList<Item>();
	private static int staffOfMosesId;
	public static Item staffOfMoses;
	private static int burntStaffOfMosesId;
	public static Item burntStaffOfMoses;
	private static int passageHalfWidth;
	public static double passageLength;
	private static int waterBlockerID;
	public static Block waterBlocker;
	public static Material materialWaterBlocker = new MaterialWaterBlocker();
	private static int blockBloodID;
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
		logger = event.getModLog();
		proxy.preInit();
		MinecraftForge.EVENT_BUS.register(this);
		
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		staffOfMosesId = config.getItem("staffOfMoses", 26999).getInt();
		burntStaffOfMosesId = config.getItem("burntStaffOfMoses", 26998).getInt();
		waterBlockerID = config.getBlock("waterBlocker", 2699).getInt();
		blockBloodID = config.getBlock("blockBloodID", 2698).getInt();
		
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
		staffOfMoses = new StaffOfMoses(staffOfMosesId).setCreativeTab(CreativeTabs.tabTools).setUnlocalizedName("staffOfMoses");
		LanguageRegistry.addName(staffOfMoses, "Staff Of Moses");
		itemList.add(staffOfMoses);
		GameRegistry.registerItem(staffOfMoses, "staffOfMoses");
		((StaffOfMoses)staffOfMoses).passageHalfWidth = passageHalfWidth;
		((StaffOfMoses)staffOfMoses).maxPassageLength = passageLength;
		((StaffOfMoses)staffOfMoses).bloodPuddleRadius = bloodPuddleRadius;
		
		burntStaffOfMoses = new BurntStaffOfMoses(burntStaffOfMosesId).setCreativeTab(CreativeTabs.tabTools).setUnlocalizedName("burntStaffOfMoses");
		LanguageRegistry.addName(burntStaffOfMoses, "Burnt Staff Of Moses");
		itemList.add(burntStaffOfMoses);
		GameRegistry.registerItem(burntStaffOfMoses, "burntStaffOfMoses");
		((BurntStaffOfMoses)burntStaffOfMoses).passageHalfWidth = passageHalfWidth;
		((BurntStaffOfMoses)burntStaffOfMoses).maxPassageLength = passageLength;
		((BurntStaffOfMoses)burntStaffOfMoses).bloodPuddleRadius = bloodPuddleRadius;
		
		waterBlocker = new TransparentBlock(waterBlockerID, materialWaterBlocker).setStepSound(Block.soundGlassFootstep).setUnlocalizedName("waterBlocker").setCreativeTab(CreativeTabs.tabMisc);
		GameRegistry.registerBlock(waterBlocker, "waterBlocker");
		LanguageRegistry.addName(waterBlocker, "Water Blocker");
		
		blood = new Fluid("mosesBlood");
		
		FluidRegistry.registerFluid(blood);
		blockBlood = new BlockBlood(blockBloodID, blood, Material.water);
		blockBlood.setUnlocalizedName("blood");
		GameRegistry.registerBlock(blockBlood, "blood");
		LanguageRegistry.addName(blockBlood, "Blood");
		
		proxy.init();
		ItemWatcher serverItemWatcher = new ItemWatcher();
		MinecraftForge.EVENT_BUS.register(serverItemWatcher);
		TickRegistry.registerTickHandler(serverItemWatcher, Side.SERVER);
		GameRegistry.registerPlayerTracker(new PlayerTracker());
	}
}