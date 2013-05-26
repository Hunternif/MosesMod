package hunternif.mc.moses.data;

import hunternif.mc.moses.util.IntVec3;

import java.util.HashSet;
import java.util.Set;

public class MosesBlockData {
	public IntVec3 coords;
	public int waterBlockID;
	private Set<Integer> ownerEntityIDs = new HashSet<Integer>();
	
	public MosesBlockData(IntVec3 coords, int waterBlockID, int ownerID) {
		this.coords = coords;
		this.waterBlockID = waterBlockID;
		addOwner(ownerID);
	}
	
	/**
	 * Will do nothing if the block is already owned by this player.
	 */
	public boolean addOwner(int playerEntityID) {
		boolean result = ownerEntityIDs.add(Integer.valueOf(playerEntityID));
		//System.out.println("added owner " + playerEntityID + ". Owners: " + ownerEntityIDs.toString());
		return result;
	}
	
	public boolean removeOwner(int playerEntityID) {
		boolean result = ownerEntityIDs.remove(Integer.valueOf(playerEntityID));
		//System.out.println("removed owner " + playerEntityID + ". Owners: " + ownerEntityIDs.toString());
		return result;
	}
	
	public boolean hasOwner(int playerEntityID) {
		return ownerEntityIDs.contains(Integer.valueOf(playerEntityID));
	}
	
	public boolean isSoleOwner(int playerEntityID) {
		return hasOwner(playerEntityID) && ownerEntityIDs.size() == 1;
	}
}
