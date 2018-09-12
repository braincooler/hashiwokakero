package objects;

/**
 * Die Klasse stellt eine Brücke zwischen zwei Inseln dar. Gibt an, ob die
 * Brücke doppelt ist.
 * 
 * @author Zeller
 */

public class Bridge {
	/** Erste Insel */
	private Island island1;
	/** Zweite Insel */
	private Island island2;
	/** Gibt an, ob die Brücke doppelt ist */
	private boolean isDouble;

	/**
	 * Brücke zwischen 2 Inseln
	 * 
	 * @param island1 erste Insel
	 * @param island2 zweite Insel
	 */
	public Bridge(Island island1, Island island2) {
		super();
		this.island1 = island1;
		this.island2 = island2;
		this.isDouble = false;
	}

	/**
	 * gleiche Brücke mit gespiegelten Koordinaten
	 * 
	 * @param b eine Brücke
	 */
	public Bridge(Bridge b) {
		this.island1 = b.getIsland2();
		this.island2 = b.getIsland1();
		this.isDouble = b.isDouble;
	}

	// Getter/Setter */
	public boolean isDouble() {
		return isDouble;
	}

	public void setDouble(boolean b) {
		this.isDouble = b;
	}

	public Island getIsland1() {
		return island1;
	}

	public void setIsland1(Island i1) {
		this.island1 = i1;
	}

	public Island getIsland2() {
		return island2;
	}

	public void setIsland2(Island i2) {
		this.island2 = i2;
	}
}
