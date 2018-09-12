package objects;

import java.awt.Color;

/**
 * Die Klasse stellt eine Insel dar. Eine Insel hat Koordinaten auf einem
 * Spielfeld und Anzahl von Bruecken, die gesetzt werden muessen. Jede Insel
 * enthaelt Zeiger auf verbundene Nachbarn.
 * 
 * @author W. Zeller
 *
 */

public class Island {
	/** Insel ID */
	private int id;
	/** Anzahl der Bruecken */
	private int bridgeCount;
	/** Anzahl der bereits gesetzten Bruecken */
	private int mountedBridges;
	/** Zeiger auf die Nachbarn */
	private Island leftNgb, rightNgb, upNgb, downNgb;
	/** Moegliche Richtungen der Bruecken */
	private boolean isLeftNgbPossible, isRightNgbPossible, isUpNgbPossible, isDownNgbPossible;
	/** Ob eine Bruecke in eine entsprechende Richtung doppelt ist */
	private boolean leftDouble, rightDouble, upDouble, downDouble;
	/** Koordinaten auf dem Spielfeld */
	private int n, m;
	/** Die Farbe der Inseln */
	private Color color = new Color(200, 200, 200);

	/**
	 * Erstellt eine Insel mit (n,m) Koordinaten auf dem Spielfeld
	 * 
	 * @param n	n-Koordinate
	 * @param m m-Koordinate
	 */
	public Island(int n, int m) {
		setN(n);
		setM(m);
	}

	/**
	 * Erstellt eine Kopie von einer Insel
	 * 
	 * @param island die Insel, die kopiert werden muss
	 */
	public Island(Island island) {
		this.bridgeCount = island.bridgeCount;
		this.leftDouble = island.leftDouble;
		this.rightDouble = island.rightDouble;
		this.upDouble = island.upDouble;
		this.downDouble = island.downDouble;
		this.leftNgb = island.leftNgb;
		this.rightNgb = island.rightNgb;
		this.upNgb = island.upNgb;
		this.downNgb = island.downNgb;
		this.n = island.n;
		this.m = island.m;
	}
	
	

	// Setter/Getter */
	
	public boolean isLeftNgbPossible() {
		return isLeftNgbPossible;
	}

	public void setLeftNgbPossible(boolean isLeftNgbPossible) {
		this.isLeftNgbPossible = isLeftNgbPossible;
	}

	public boolean isRightNgbPossible() {
		return isRightNgbPossible;
	}

	public void setRightNgbPossible(boolean isRightNgbPossible) {
		this.isRightNgbPossible = isRightNgbPossible;
	}

	public boolean isUpNgbPossible() {
		return isUpNgbPossible;
	}

	public void setUpNgbPossible(boolean isUpNgbPossible) {
		this.isUpNgbPossible = isUpNgbPossible;
	}

	public boolean isDownNgbPossible() {
		return isDownNgbPossible;
	}

	public void setDownNgbPossible(boolean isDownNgbPossible) {
		this.isDownNgbPossible = isDownNgbPossible;
	}

	public int getBridgeCount() {
		return bridgeCount;
	}

	public void setBridgeCount(int bridgeCount) {
		this.bridgeCount = bridgeCount;
	}

	public void incrementBridgeCount() {
		this.bridgeCount++;
	}

	public void setLeftNgb(Island island) {
		if (island != null) {
			island.rightNgb = this;
			island.incrementMountedBridges();
			this.incrementMountedBridges();
		} else {
			this.decrementMountedBridges();
		}
		this.leftNgb = island;

	}

	public Island getLeftNgb() {
		return leftNgb;
	}

	public void setRightNgb(Island island) {
		if (island != null) {
			island.leftNgb = this;
			island.incrementMountedBridges();
			this.incrementMountedBridges();
		} else {
			this.decrementMountedBridges();
		}
		this.rightNgb = island;
	}

	public Island getRightNgb() {
		return rightNgb;
	}

	public void setUpNgb(Island island) {
		if (island != null) {
			island.downNgb = this;
			island.incrementMountedBridges();
			this.incrementMountedBridges();
		} else {
			this.decrementMountedBridges();
		}
		this.upNgb = island;
	}

	public Island getUpNgb() {
		return upNgb;
	}

	public void setDownNgb(Island island) {

		if (island != null) {
			island.upNgb = this;
			island.incrementMountedBridges();
			this.incrementMountedBridges();
		} else {
			this.decrementMountedBridges();
		}
		this.downNgb = island;
	}

	public Island getDownNgb() {
		return downNgb;
	}

	public boolean isLeftDouble() {
		return leftDouble;
	}

	public boolean isRightDouble() {
		return rightDouble;
	}

	public boolean isUpDouble() {
		return upDouble;
	}

	public boolean isDownDouble() {
		return downDouble;
	}

	public void setLeftDouble(boolean leftDouble) {
		if (leftDouble && !this.leftDouble)
			incrementMountedBridges();
		if (!leftDouble && this.leftDouble)
			decrementMountedBridges();
		this.leftDouble = leftDouble;
	}

	public void setRightDouble(boolean rightDouble) {
		if (rightDouble && !this.rightDouble)
			this.incrementMountedBridges();
		if (!rightDouble && this.rightDouble)
			this.decrementMountedBridges();
		this.rightDouble = rightDouble;
	}

	public void setUpDouble(boolean upDouble) {
		if (upDouble && !this.upDouble)
			this.incrementMountedBridges();
		if (!upDouble && this.upDouble)
			this.decrementMountedBridges();
		this.upDouble = upDouble;

	}

	public void setDownDouble(boolean downDouble) {
		if (downDouble && !this.downDouble)
			this.incrementMountedBridges();
		if (!downDouble && this.downDouble)
			this.decrementMountedBridges();
		this.downDouble = downDouble;

	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public int getM() {
		return m;
	}

	public void setM(int m) {
		this.m = m;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setID(int i) {
		id = i;
	}

	public int getID() {
		return id;
	}

	public void incrementMountedBridges() {
		if (mountedBridges <= 8) {
			mountedBridges++;
		}

	}

	public void decrementMountedBridges() {
		if (mountedBridges > 0) {
			mountedBridges--;
		}
	}

	public int getMountedBridges() {
		return mountedBridges;
	}

	public void resetMountedBridges() {
		mountedBridges = 0;
	}
}
