package de.dropall;

/** Die drei Drop-Modi. */
public enum DropMode {
	INVENTORY("inventory"),
	CHEST("chest"),
	BOTH("both");

	private final String id;

	DropMode(String id) {
		this.id = id;
	}

	public boolean includesInventory() {
		return this != CHEST;
	}

	public boolean includesContainer() {
		return this != INVENTORY;
	}

	public String buttonKey() {
		return "dropall.button." + id;
	}

	public String shortKey() {
		return "dropall.mode." + id;
	}

	public String tipKey() {
		return "dropall.mode." + id + ".tip";
	}

	public static DropMode fromName(String name) {
		try {
			return valueOf(name);
		} catch (Exception e) {
			return INVENTORY;
		}
	}
}
