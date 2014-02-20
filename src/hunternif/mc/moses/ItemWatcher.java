package hunternif.mc.moses;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

/**
 * Watches tossed sticks to convert them into Staffs when on fire above a block
 * of leaves, and tossed Staffs to convert them to Burnt Staffs in lava.
 * Must be registered to event bus and as a tick handler.
 * @author Hunternif
 */
public class ItemWatcher implements ITickHandler {
	public List<EntityItem> tossedSticks = new ArrayList<EntityItem>();
	public List<EntityItem> tossedStaffs = new ArrayList<EntityItem>();
	
	@ForgeSubscribe
	public void onItemToss(ItemTossEvent event) {
		ItemStack stack = event.entityItem.getEntityItem(); 
		if (stack.itemID == Item.stick.itemID) {
			MosesMod.logger.fine("Tracking a tossed stick.");
			tossedSticks.add(event.entityItem);
		} else if (stack.itemID == MosesMod.staffOfMoses.itemID) {
			MosesMod.logger.fine("Tracking a tossed staff.");
			tossedStaffs.add(event.entityItem);
		}
	}
	
	@ForgeSubscribe
	public void onItemExpire(ItemExpireEvent event) {
		tossedSticks.remove(event.entityItem);
	}
	
	@ForgeSubscribe
	public void onItemPickup(EntityItemPickupEvent event) {
		tossedSticks.remove(event.item);
	}
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (type.contains(TickType.SERVER)) {
			// Turn sticks on fire on leaves into Staff of Moses
			Iterator<EntityItem> iter = tossedSticks.iterator();
			while (iter.hasNext()) {
				EntityItem stick = iter.next();
				int x = MathHelper.floor_double(stick.posX);
				int y = MathHelper.floor_double(stick.posY);
				int z = MathHelper.floor_double(stick.posZ);
				boolean foundFire = stick.worldObj.getBlockId(x, y, z) == Block.fire.blockID;
				boolean foundBush = stick.worldObj.getBlockId(x, y-1, z) == Block.leaves.blockID;
				if (stick.isBurning() && foundFire && foundBush) {
					MosesMod.logger.info("Transforming stick into Staff.");
					iter.remove();
					stick.worldObj.setBlockToAir(x, y, z);
					stick.worldObj.playSoundAtEntity(stick, Sound.MOSES.getName(), 1, 1);
					/*stick.extinguish();
					stick.setEntityItemStack(new ItemStack(MosesMod.staffOfMoses));*/
					stick.setDead();
					stick.worldObj.spawnEntityInWorld(new EntityItem(
							stick.worldObj, stick.posX, stick.posY, stick.posZ,
							new ItemStack(MosesMod.staffOfMoses)));
				}
			}
			// Turn Staff of Moses in lava into Burnt Staff of Moses
			iter = tossedStaffs.iterator();
			while (iter.hasNext()) {
				EntityItem staff = iter.next();
				int x = MathHelper.floor_double(staff.posX);
				int y = MathHelper.floor_double(staff.posY);
				int z = MathHelper.floor_double(staff.posZ);
				int blockID = staff.worldObj.getBlockId(x, y, z);
				if (blockID == Block.lavaMoving.blockID || blockID == Block.lavaStill.blockID) {
					MosesMod.logger.info("Transforming Staff into Burnt Staff.");
					iter.remove();
					staff.worldObj.setBlockToAir(x, y, z);
					staff.worldObj.playSoundAtEntity(staff, Sound.BURNT_STAFF.getName(), 1, 1);
					/*staff.extinguish();
					staff.setEntityItemStack(new ItemStack(MosesMod.burntStaffOfMoses));*/
					staff.setDead();
					staff.worldObj.spawnEntityInWorld(new EntityItem(
							staff.worldObj, staff.posX, staff.posY, staff.posZ,
							new ItemStack(MosesMod.burntStaffOfMoses)));
				}
			}
		}
	}
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
	}
	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.SERVER);
	}
	@Override
	public String getLabel() {
		return "MosesMod ItemWatcher";
	}
}