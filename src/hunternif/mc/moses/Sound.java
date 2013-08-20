package hunternif.mc.moses;

public enum Sound {
	SEA_PARTING("sea_parting"),
	SEA_CLOSING("sea_closing"),
	LAVA_PARTING("lava_parting"),
	LAVA_CLOSING("lava_closing"),
	MOSES("moses"),
	BURNT_STAFF("burnt_staff");
	
	private String name;
	private Sound(String name) {
		this.name = name;
	}
	public String toString() {
		return name;
	}
	
	public String getName() {
		return MosesMod.ID+":"+name;
	}
}
