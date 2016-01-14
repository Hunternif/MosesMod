package hunternif.mc.moses;

import hunternif.mc.moses.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

/**
 * Watches tossed sticks to convert them into Staffs when on fire above a block
 * of leaves, and tossed Staffs to convert them to Burnt Staffs in lava.
 * Must be registered to event bus and as a tick handler.
 * @author Hunternif
 */
public class ItemWatcher {
	public List<EntityItem> tossedSticks = new ArrayList<EntityItem>();
	public List<EntityItem> tossedStaffs = new ArrayList<EntityItem>();
	
	@SubscribeEvent
	public void onItemToss(ItemTossEvent event) {
		ItemStack stack = event.entityItem.getEntityItem(); 
		if (stack.getItem() == Items.stick) {
			Log.info("Tracking a tossed stick.");
			tossedSticks.add(event.entityItem);
		} else if (stack.getItem() == MosesMod.staffOfMoses) {
			Log.info("Tracking a tossed staff.");
			tossedStaffs.add(event.entityItem);
		}
	}
	
	@SubscribeEvent
	public void onItemExpire(ItemExpireEvent event) {
		tossedSticks.remove(event.entityItem);
	}
	
	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent event) {
		tossedSticks.remove(event.item);
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event) {
		if (event.phase == Phase.START) {
			// Turn sticks on fire on leaves into Staff of Moses
			Iterator<EntityItem> iter = tossedSticks.iterator();
			while (iter.hasNext()) {
				EntityItem stick = iter.next();
				BlockPos pos = stick.getPosition();
				boolean foundFire = stick.worldObj.getBlockState(pos).getBlock() == Blocks.fire;
				Block blockBelow = stick.worldObj.getBlockState(pos.down()).getBlock();
				boolean foundBush = blockBelow == Blocks.leaves || blockBelow == Blocks.leaves2;
				if (stick.isBurning() && foundFire && foundBush) {
					Log.info("Transforming stick into Staff.");
					iter.remove();
					stick.worldObj.setBlockToAir(pos);
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
				BlockPos pos = staff.getPosition();
				Block block = staff.worldObj.getBlockState(pos).getBlock();
				if (block == Blocks.lava || block == Blocks.flowing_lava) {
					Log.info("Transforming Staff into Burnt Staff.");
					iter.remove();
					staff.worldObj.setBlockToAir(pos);
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
}