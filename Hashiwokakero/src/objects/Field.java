package objects;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import gui.MainWindow;

/**
 * Die Klasse stellt das Spielfeld dar.
 * 
 * @author W. Zeller
 */

@SuppressWarnings("serial")
public class Field extends JPanel implements MouseListener {

	/** Liste mit Inseldaten(Koordinaten, Inselnanzahl) */
	private LinkedList<Integer> islandDataList = new LinkedList<Integer>();

	/** Zeiger auf einen zufälligen Nachbar */
	private Island randomNgb;

	/** Inselradius */
	private static final int radius = 26;

	/** Klicktoleranz */
	private static final int tolerance = 2;

	/** Rätsel - Breite, Höhe, Inselanzahl */
	private int widthPuzzle, heightPuzzle, islandCount;

	/** SpielfeldPanel - Breite, Höhe */
	private int widthField, heightField;

	/** Spielfeld */
	private Island grid[][];

	/** Liste mit eingefügten Brücken */
	private LinkedList<Bridge> lastBridgesList = new LinkedList<Bridge>();

	/** Liste mit allen Inseln */
	private LinkedList<Island> allIslands = new LinkedList<Island>();

	/** HilfsPointer auf einen Insel */
	@SuppressWarnings("unused")
	private Island anyIsland;

	/**
	 * Grundgerüst des Spielfeldes
	 * 
	 * @param h
	 *            Höhe des Rätsels
	 * @param w
	 *            Breite des Rätsels
	 */
	public Field(int h, int w) {
		// Kanten
		MatteBorder insideBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(200, 200, 200));
		MatteBorder outsideBorder = BorderFactory.createMatteBorder(3, 3, 3, 0, new Color(238, 238, 238));
		setBorder(new CompoundBorder(outsideBorder, insideBorder));
		setBackground(Color.WHITE);
		this.widthPuzzle = h;
		this.heightPuzzle = w;
		// +2 - Kanten auf dem SpielfeldArray.
		grid = new Island[widthPuzzle + 2][heightPuzzle + 2];
		// Panelgröße wird an Rätselgröße angepasst
		this.widthField = grid.length;
		this.heightField = grid[0].length;
		setPreferredSize(new Dimension(widthField * radius, heightField * radius));
		addMouseListener(this);
	}

	/**
	 * Erzeugt ein zufälliges Spielfeld h - Höhe w - Breite c - Inseln
	 * 
	 * @param h
	 *            Höhe
	 * @param w
	 *            Breite
	 * @param c
	 *            Inselnanzahl
	 * @throws IllegalArgumentException
	 *             wenn Benutzereingaben im falschem Bereich liegen
	 */
	public Field(int h, int w, int c) throws IllegalArgumentException {
		this(h, w);
		if (h < 4 || w < 4 || h > 25 || w > 25 || c < 2 || c > h * w * 0.2)
			throw new IllegalArgumentException();
		this.islandCount = c;
		// Schleife läuft so lange, bis ein Rätsel erstellt ist.
		while (!createPuzzle()) {
			allIslands.clear();
		}
	}

	/**
	 * Erzeugt ein neus Rätsel aus Komposition zweier Rätsel
	 * 
	 * @param leftField
	 *            das linke Rätsel
	 * @param rightField
	 *            das rechte Rätsel
	 */
	public Field(Field leftField, Field rightField) {
		this(leftField.getHeightPuzzle() + rightField.getHeightPuzzle() + 3,
				leftField.getWidthPuzzle() + rightField.getWidthPuzzle() + 3);
		initField();
		this.islandCount = leftField.getIslandCount() + rightField.getIslandCount();
		
		for (int i = 1; i < leftField.grid.length; i++)
			for (int j = 1; j < leftField.grid[i].length; j++) {
				grid[i][j] = new Island(leftField.grid[i][j]);
			}

		int leftFieldMinM = 25;
		Island leftIsland = null;
		for (Island[] i : leftField.grid)
			for (Island j : i) {
				if (j.getBridgeCount() > 0)
					if (nextRightNgb(j) == null)
						if (j.getM() < leftFieldMinM) {
							leftFieldMinM = j.getM();
							leftIsland = j;
						}
			}
		
		int rightFieldMinM = 25;
		Island rightIsland = null;
		for (Island[] i : rightField.grid)
			for (Island j : i) {
				if (j.getBridgeCount() > 0)
					if (nextLeftNgb(j) == null)
						if (j.getM() < rightFieldMinM) {
							rightFieldMinM = j.getM();
							rightIsland = j;
						}
			}
		
		int diff = rightIsland.getM() - leftIsland.getM();
		for (int i = 1; i < rightField.grid.length; i++)
			for (int j = 1; j < rightField.grid[i].length; j++) {
				if (rightField.grid[i][j].getBridgeCount() != 0) {
					grid[i + leftIsland.getN() + 1][j - diff] = new Island(rightField.grid[i][j]);
					grid[i + leftIsland.getN() + 1][j - diff].setN(i + leftIsland.getN() + 1);
					grid[i + leftIsland.getN() + 1][j - diff].setM(j - diff);
				}
			}
		this.grid[leftIsland.getN()][leftIsland.getM()].incrementBridgeCount();
		this.grid[leftIsland.getN()+rightIsland.getN()+1][rightIsland.getM()-diff].incrementBridgeCount();
	}

	/**
	 * Gibt nächsten unteren Nachbarn zurück
	 * 
	 * @param island
	 *            eine Insel
	 * @return nächster unterer Nachbar, wenn keiner existiert gibt null zurück
	 */
	public Island nextDownNgb(Island island) {
		for (int i = island.getM() + 1; i < grid[0].length; i++) {
			if (grid[island.getN()][i].getBridgeCount() > 0)
				return grid[island.getN()][i];
		}
		return null;
	}

	/**
	 * Gibt nächsten oberen Nachbarn zurück
	 * 
	 * @param island
	 *            eine Insel
	 * @return nächster oberer Nachbar, wenn keiner existiert gibt null zurück
	 */
	public Island nextUpNgb(Island island) {
		for (int i = island.getM() - 1; i > 0; i--) {
			if (grid[island.getN()][i].getBridgeCount() > 0)
				return grid[island.getN()][i];
		}
		return null;
	}

	/**
	 * Gibt nächsten rechten Nachbarn zurück
	 * 
	 * @param island
	 *            eine Insel
	 * @return nächster rechter Nachbar, wenn keiner existiert gibt null zurück
	 */

	public Island nextRightNgb(Island island) {
		for (int i = island.getN() + 1; i < grid.length - 1; i++) {
			if (grid[i][island.getM()].getBridgeCount() > 0)
				return grid[i][island.getM()];
		}
		return null;
	}

	/**
	 * Gibt nächsten linken Nachbarn zurück
	 * 
	 * @param island
	 *            eine Insel
	 * @return nächster linker Nachbar, wenn keiner existiert gibt null zurück
	 */
	public Island nextLeftNgb(Island island) {
		for (int i = island.getN() - 1; i > 0; i--) {
			if (grid[i][island.getM()].getBridgeCount() > 0)
				return grid[i][island.getM()];
		}
		return null;
	}

	/**
	 * Initialisiert das Feld. Das Spielfeld wird mit leeren Inseln (Anzahl von
	 * Brücken = 0) gefüllt
	 */
	private void initField() {
		for (int i = 0; i < grid.length; i++)
			for (int j = 0; j < grid[i].length; j++) {
				grid[i][j] = new Island(i, j);
				grid[i][j].setBridgeCount(0);
			}
	}

	/**
	 * Erzeugt ein zufälliges Rätsel. Gibt true zurück, wenn Rätsel fertig ist
	 * und false, falls Rätsel nicht erstellt werden kann.
	 * 
	 * @return true - Rätsel ist fertig. false - Rätsel wurde nicht erstellt.
	 */
	private boolean createPuzzle() {

		// Hilfspointer
		Island pnt1;
		// Anzahl fehlender Brücken
		int rest = islandCount;
		// Das Sielfeld initialisieren
		initField();
		// Koordinaten der erster zufälliger Insel
		int n = 1 + (int) ((Math.random()) * (widthPuzzle - 1));
		int m = 1 + (int) ((Math.random()) * (heightPuzzle - 1));
		// Anzahl von Brücken ehöhen
		grid[n][m].incrementBridgeCount();
		// Eine Insel auf dem Spielfeld merken
		anyIsland = grid[n][m];
		// Die frisch gebackene Insel zu der Liste mit allen Brücken hinzufügen
		allIslands.add(grid[n][m]);
		// zufälliger Nachbar zu der Insel finden
		while (!findRandomNgbTo(grid[n][m]))
			;
		pnt1 = randomNgb;
		// Die zweite gefundene Insel zu der Liste mit allen Brücken hinzufügen
		allIslands.add(pnt1);
		// Anzahl von Brücken erhöhen
		pnt1.incrementBridgeCount();
		// Beide Inseln verbinden
		connect(pnt1, grid[n][m]);
		// Sonderfall. Rätsel nur mit 2 Inseln
		if (rest == 2) {
			// Das Rätsel zentrieren
			centerPuzzle();
			// Alle Brücken entfernen
			removeAllBridges();
			return true;
		}
		// Bis alle Inseln zum Spielfeld eingefügt wurden.
		while (rest > 2) {
			// Timer. Es gibt 1000 Versuche einen zufälligen Nachbarn zu finden.
			int timer = 1000;
			do {
				// Eine zufällige Insel auf dem Spielfeld merken
				pnt1 = selectRandomIsland();
				timer--;
				// Wenn Timer abläuft, wird die Methode mit false beendet.
				if (timer < 1) {
					return false;
				}
				// bis ein zufälliger Nachbar gefunden wird.
			} while (!findRandomNgbTo(pnt1));
			// Anzahl von Brücken der beiden Inseln erhöhen
			pnt1.incrementBridgeCount();
			randomNgb.incrementBridgeCount();
			// Die Inseln müssen erstmal verbunden werden
			// um sicher zu sein, dass das Rätsel lösbar ist
			connect(pnt1, randomNgb);
			// Die zuletzt gefundene Insel zu der Liste mit allen Brücken
			// hinzufügen
			allIslands.add(randomNgb);
			rest--;
		}

		// Zyklen einfügen und Inseln numerieren
		// Zähler
		int num = 0;
		for (Island[] i : grid)
			for (Island j : i) {
				if (j.getBridgeCount() > 0) {
					j.setID(num);
					num++;
					// Auf horizontaler Achse zusätzliche Brücken einfügen
					// Rechter Nachbar
					Island rightNgb = nextRightNgb(j);
					// Wenn die Inseln noch nicht verbunden sind und Chance
					// false ist
					if (j.getRightNgb() == null && !Chance())
						// Wenn ein Nachbar rechts existert und wenn der
						// verbunden werden kann
						if (rightNgb != null && connectable(rightNgb, j)) {
						// Die Inseln verbinden und Anzahl von Brücken erhöhen
						connect(rightNgb, j);
						rightNgb.incrementBridgeCount();
						j.incrementBridgeCount();
						}
					// Auf vertikaler Achse zusätzliche Brücken einfügen
					// Unterer Nachbar
					Island downNgb = nextDownNgb(j);
					// Wenn die Inseln noch nicht verbunden sind und Chance
					// false ist
					if (j.getDownNgb() == null && !Chance())
						// Wenn ein Nachbar unten existert und wenn der
						// verbunden werden kann
						if (downNgb != null && connectable(downNgb, j)) {
						// Die Inseln verbinden und Anzahl von Brücken erhöhen
						connect(downNgb, j);
						downNgb.incrementBridgeCount();
						j.incrementBridgeCount();
						}
				}
			}
		// 2x-Brücken einfügen
		// Wenn Inseln verbunden sind, wird die Brücke mit einer Chance 50/50 zu
		// 2x-Brücke
		for (Island[] i : grid)
			for (Island j : i) {
				if (j.getBridgeCount() > 0) {
					// horizontale Achse
					if (j.getLeftNgb() != null && Chance()) {
						j.setLeftDouble(true);
						j.getLeftNgb().setRightDouble(true);
						j.incrementBridgeCount();
						j.getLeftNgb().incrementBridgeCount();
					}
					// vertikale Achse
					if (j.getDownNgb() != null && Chance()) {
						j.setDownDouble(true);
						j.getDownNgb().setUpDouble(true);
						j.incrementBridgeCount();
						j.getDownNgb().incrementBridgeCount();
					}
				}
			}
		// Alle Brücken entfernen
		removeAllBridges();
		// Das Rätsel ist fertig
		return true;
	}

	/**
	 * Gibt eine zufällige Insel auf dem Spielfeld zurück
	 * 
	 * @return eine zufällige Insel auf dem Spielfeld
	 */
	public Island selectRandomIsland() {
		int x = (int) (Math.random() * allIslands.size());
		return allIslands.get(x);
	}

	/**
	 * Findet einen zufälligen Nachbarn für die Inseln und spechert den unter
	 * 
	 * @param island
	 *            die Insel, zu der ein zufälliger Nachbar gesucht wird.
	 * @return true, wenn ein Nachbar gefunden wurde, false wenn nicht.
	 */
	public boolean findRandomNgbTo(Island island) {
		int n1 = island.getN(), m1 = island.getM();
		// timer
		int timer = 12;
		do {
			// Es gibt 12 Versuche einen zufälligen Nachbar zu finden
			timer--;
			// Zufällige Richtung: 0 - Links, 1 - Oben, 2 - Rechts, 3 - Unten
			switch ((int) ((Math.random() * 4))) {
			case 0:
				n1 = (int) ((Math.random() * (island.getN())));
				break;
			case 1:
				m1 = (int) ((Math.random() * (island.getM())));
				break;
			case 2:
				n1 = 1 + island.getN() + (int) ((Math.random() * (widthPuzzle - island.getN())));
				break;
			case 3:
				m1 = 1 + island.getM() + (int) ((Math.random() * (heightPuzzle - island.getM())));
				break;
			}
			// Prüfen, ob gefundene Insel Abstand 1 mit den Nachbarn hat.
			if (n1 > 0 && n1 < (widthPuzzle + 1))
				if (m1 < (heightPuzzle + 1) && m1 > 0)
					if (grid[n1 + 1][m1].getBridgeCount() < 1 && grid[n1][m1 - 1].getBridgeCount() < 1)
						if (grid[n1 - 1][m1].getBridgeCount() < 1 && grid[n1][m1 + 1].getBridgeCount() < 1)
							if ((grid[n1][m1].getBridgeCount() == 0) && connectable(island, grid[n1][m1])) {
								randomNgb = grid[n1][m1];
								return true;
							}
		} while (timer > 0);
		return false;
	}

	/**
	 * Loescht eine Bruecke aus einer Liste
	 * 
	 * @param list
	 *            eine Liste mit Bruecken
	 * @param bridge
	 *            die Brücke, die geloescht werden muss
	 */

	public void removeBridgeFromList(LinkedList<Bridge> list, Bridge bridge) {
		for (ListIterator<Bridge> iter = list.listIterator(list.size()); iter.hasPrevious();) {
			Bridge data = iter.previous();
			if (data.getIsland1().equals(bridge.getIsland1()) || data.getIsland2().equals(bridge.getIsland1()))
				if (data.getIsland1().equals(bridge.getIsland2()) || data.getIsland2().equals(bridge.getIsland2())) {
					iter.remove();
					break;
				}
		}
	}

	/** Löscht alle Brücken und mit -1 gesperrte Inseln */
	public void removeAllBridges() {
		for (Island[] i : grid)
			for (Island j : i) {
				// Entfernt alle Zeiger auf Nachbarn
				if (j.getBridgeCount() > 0) {
					j.setLeftDouble(false);
					j.setLeftNgbPossible(false);
					j.setLeftNgb(null);
					j.setUpDouble(false);
					j.setUpNgbPossible(false);
					j.setUpNgb(null);
					j.setRightDouble(false);
					j.setRightNgbPossible(false);
					j.setRightNgb(null);
					j.setDownDouble(false);
					j.setDownNgbPossible(false);
					j.setDownNgb(null);
				} else
					// gesperrte Inseln freigeben
					j.setBridgeCount(0);
				// Anzahl der eingefügten Brücken zurücksetzen
				j.resetMountedBridges();
			}
	}

	/** Rätsel aud dem Spielfeld zentrieren */
	public void centerPuzzle() {
		for (int i = 1; i < widthField / 2; i++) {
			if (!connectable(grid[i][0], grid[i][heightField - 1])
					&& connectable(grid[widthField - i - 1][0], grid[widthField - i - 1][heightField - 1])) {
				shiftPuzzleToRight();
			}
			if (connectable(grid[i][0], grid[i][heightField - 1])
					&& !connectable(grid[widthField - i - 1][0], grid[widthField - i - 1][heightField - 1]))
				shiftPuzzleToLeft();
		}
		for (int i = 1; i < heightField / 2; i++) {
			if (!connectable(grid[0][i], grid[widthField - 1][i])
					&& connectable(grid[0][heightField - i - 1], grid[widthField - 1][heightField - i - 1])) {
				shiftPuzzleDown();
			}
			if (connectable(grid[0][i], grid[widthField - 1][i])
					&& !connectable(grid[0][heightField - i - 1], grid[widthField - 1][heightField - i - 1])) {
				shiftPuzzleUp();
			}
		}
	}

	/** Array nach rechts shiften */
	public void shiftPuzzleToRight() {
		for (int i = 1; i < heightField; i++)
			for (int j = widthField - 1; j > 0; j--) {
				grid[j][i] = new Island(grid[j - 1][i]);
				grid[j][i].setN(j);
			}
	}

	/** Array nach links shiften */
	public void shiftPuzzleToLeft() {
		for (int i = 1; i < heightField; i++)
			for (int j = 1; j < widthField - 1; j++) {
				grid[j][i] = new Island(grid[j + 1][i]);
				grid[j][i].setN(j);
			}
	}

	/** Array nach oben shiften */
	public void shiftPuzzleUp() {
		for (int j = 1; j < widthField; j++)
			for (int i = 1; i < heightField - 1; i++) {
				grid[j][i] = new Island(grid[j][i + 1]);
				grid[j][i].setM(i);
			}
	}

	/** Array nach unten shiften */
	public void shiftPuzzleDown() {
		for (int j = 1; j < widthField; j++)
			for (int i = heightField - 1; i > 0; i--) {
				grid[j][i] = new Island(grid[j][i - 1]);
				grid[j][i].setM(i);
			}
	}

	/**
	 * Gibt true oder false mit einer Chance 50% zurück
	 * 
	 * @return true oder false mit einer Chance 50%
	 */
	public boolean Chance() {
		if (Math.random() > 0.5)
			return false;
		else
			return true;
	}

	/**
	 * Fügt eine Brücke zwischen zwei Inseln. Alle Inseln dazwischen werden mit
	 * Anzahl von Brücken -1 gesperrt.
	 * 
	 * @param a
	 *            erste Insel
	 * @param b
	 *            zweite Insel
	 */
	public void connect(Island a, Island b) {
		// Falls beide Inseln bereits mit einer Brücke verbunden sind, wird eine
		// doppelte Brücke eingefügt
		if (a.getLeftNgb() == b || a.getRightNgb() == b || a.getUpNgb() == b || a.getDownNgb() == b) {
			// Vertikale Achse
			if (a.getN() == b.getN())
				if (!grid[a.getN()][Math.min(a.getM(), b.getM())].isDownDouble()) {
					grid[a.getN()][Math.min(a.getM(), b.getM())].setDownDouble(true);
					grid[a.getN()][Math.max(a.getM(), b.getM())].setUpDouble(true);
				}
			// Horizontale Achse
			if (a.getM() == b.getM())
				if (!grid[Math.min(a.getN(), b.getN())][a.getM()].isRightDouble()) {
					grid[Math.min(a.getN(), b.getN())][a.getM()].setRightDouble(true);
					grid[Math.max(a.getN(), b.getN())][a.getM()].setLeftDouble(true);
				}
		}
		// Prüfen, ob man die Inseln verbinden darf;
		if (connectable(a, b)) {
			// Vertikale Achse
			if (a.getN() == b.getN()) {
				// Alle 0-Inseln, die dazwischen liegen, werden mit -1 gesperrt
				for (int i = Math.min(a.getM(), b.getM()) + 1; i < Math.max(a.getM(), b.getM()); i++)
					grid[a.getN()][i].setBridgeCount(-1);
				// Zeiger auf die Nachbarn setzen
				grid[a.getN()][Math.min(a.getM(), b.getM())].setDownNgb(grid[a.getN()][Math.max(a.getM(), b.getM())]);
			}
			// Horizontale Achse
			if (a.getM() == b.getM()) {
				// Alle 0-Inseln, die dazwischen liegen, werden mit -1 gesperrt
				for (int i = Math.min(a.getN(), b.getN()) + 1; i < Math.max(a.getN(), b.getN()); i++)
					grid[i][a.getM()].setBridgeCount(-1);
				// Zeiger auf Nachbarn setzen
				grid[Math.min(a.getN(), b.getN())][a.getM()].setRightNgb(grid[Math.max(a.getN(), b.getN())][a.getM()]);
			}
		}

	}

	/**
	 * Entfern eine Brücke zwischen zwei Inseln. Alle Inseln dazwischen werden
	 * mit Anzahl von Brücken 0 freigegeben
	 * 
	 * @param a
	 *            erste Insel
	 * @param b
	 *            zweite Insel
	 */
	public void removeBridge(Island a, Island b) {
		// Aus der Liste der zuletzt eingefügten Brücken entfernen
		removeBridgeFromList(lastBridgesList, new Bridge(a, b));
		// Falls die beiden Inseln mit zwei Brücken verbunden sind, wird eine
		// entfernt
		if (a.getLeftNgb() == b || a.getRightNgb() == b || a.getUpNgb() == b || a.getDownNgb() == b) {
			// Vertikale Achse
			if (a.getN() == b.getN())
				if (grid[a.getN()][Math.min(a.getM(), b.getM())].isDownDouble()) {
					grid[a.getN()][Math.min(a.getM(), b.getM())].setDownDouble(false);
					grid[a.getN()][Math.max(a.getM(), b.getM())].setUpDouble(false);
					return;
				}
			// Horizontale Achse
			if (a.getM() == b.getM())
				if (grid[Math.min(a.getN(), b.getN())][a.getM()].isRightDouble()) {
					grid[Math.min(a.getN(), b.getN())][a.getM()].setRightDouble(false);
					grid[Math.max(a.getN(), b.getN())][a.getM()].setLeftDouble(false);
					return;
				}
		}
		// Eine Brücke entfernen, falls die auf vetikal ist.
		if (a.getN() == b.getN()) {
			if (!grid[a.getN()][Math.min(a.getM(), b.getM())].isDownDouble()) {
				for (int i = Math.min(a.getM(), b.getM()) + 1; i < Math.max(a.getM(), b.getM()); i++)
					grid[a.getN()][i].setBridgeCount(0);
				grid[a.getN()][Math.min(a.getM(), b.getM())].setDownNgb(null);
				grid[a.getN()][Math.max(a.getM(), b.getM())].setUpNgb(null);
			} else {
				grid[a.getN()][Math.min(a.getM(), b.getM())].setDownDouble(false);
				grid[a.getN()][Math.max(a.getM(), b.getM())].setUpDouble(false);
			}
		}

		// Eine Brücke entfernen, falls die auf horizontal ist.
		if (a.getM() == b.getM()) {
			if (!grid[Math.min(a.getN(), b.getN())][a.getM()].isRightDouble()) {
				for (int i = Math.min(a.getN(), b.getN()) + 1; i < Math.max(a.getN(), b.getN()); i++)
					grid[i][a.getM()].setBridgeCount(0);
				grid[Math.min(a.getN(), b.getN())][a.getM()].setRightNgb(null);
				grid[Math.max(a.getN(), b.getN())][a.getM()].setLeftNgb(null);
			} else {
				grid[Math.min(a.getN(), b.getN())][a.getM()].setRightDouble(false);
				grid[Math.max(a.getN(), b.getN())][a.getM()].setLeftDouble(false);
			}
		}
	}

	/**
	 * Prüft, ob man 2 Inseln durch eine einzelne Brücke verbinden kann.
	 * 
	 * @param a
	 *            erste Insel
	 * @param b
	 *            zweite Insel
	 * @return true die einzelne Brücke darf gesetzt werden, false - wenn nicht.
	 */
	public boolean connectable(Island a, Island b) {
		// Es wird geprüft, ob beide Inseln auf einer Achse sind
		if ((a.getN() != b.getN()) && (a.getM() != b.getM()))
			return false;
		// Es wird geprüft, ob es zwischen den Inseln keine Sperrung ist.
		if (a.getM() == b.getM())
			for (int i = Math.min(a.getN(), b.getN()) + 1; i < Math.max(a.getN(), b.getN()); i++)
				if (grid[i][a.getM()].getBridgeCount() != 0)
					return false;
		if (a.getN() == b.getN())
			for (int i = Math.min(a.getM(), b.getM()) + 1; i < Math.max(a.getM(), b.getM()); i++)
				if (grid[a.getN()][i].getBridgeCount() != 0)
					return false;
		return true;
	}

	/**
	 * Prüft, ob Rätsel gelöst ist
	 * 
	 * @return true wenn das Rätsel gelöst ist, false - wenn nicht
	 */
	public boolean isCompleted() {
		// Referenz auf MainWindow
		MainWindow mainWindow = (MainWindow) SwingUtilities.getWindowAncestor(getParent());
		// Prüfen, ob das Rätsel nicht mehr lösbar ist
		for (Island[] i : grid) {
			for (Island j : i) {
				if (j.getBridgeCount() > 0) {
					if (j.getBridgeCount() > j.getMountedBridges()) {
						if (!isStableBridgePossible(j)) {
							mainWindow.getControlPanel().setPuzzleIsNotMoreSolvableText();
							return false;
						}
					}
				}
			}
		}
		// Liste mit mit anyIsland verbundenen Inseln
		LinkedList<Island> linkedIsland = new LinkedList<>();
//		linkedIsland.add(anyIsland);
		for (Island[] i : grid) {
			for (Island j : i) {
				if (j.getBridgeCount()>0){
					linkedIsland.add(j);
					break;
				}
				
			}
		}
		// Verbundene Nachbarn von anyIsland und Nachbarn der Nachbarn werden
		// zur linkedIsland hinzugefügt
		for (int i = 0; i < linkedIsland.size(); i++) {
			Island island = linkedIsland.get(i);
			if (island != null) {
				if (island.getLeftNgb() != null)
					if (!linkedIsland.contains(island.getLeftNgb()))
						linkedIsland.add(island.getLeftNgb());
				if (island.getRightNgb() != null)
					if (!linkedIsland.contains(island.getRightNgb()))
						linkedIsland.add(island.getRightNgb());
				if (island.getUpNgb() != null)
					if (!linkedIsland.contains(island.getUpNgb()))
						linkedIsland.add(island.getUpNgb());
				if (island.getDownNgb() != null)
					if (!linkedIsland.contains(island.getDownNgb()))
						linkedIsland.add(island.getDownNgb());
			}
		}

		// Wenn zu linkedIsland alle Inseln hinzugefügt wurden, dann sind alle
		// miteinander verbunden.
		if (linkedIsland.size() != islandCount) {
			mainWindow.getControlPanel().setGameNotOverText();

			return false;
		}
		linkedIsland  = null;
		// Prüfen, ob alle Inseln gültige Anzahl der eingefügten Brücken hat
		for (Island[] i : grid) {
			for (Island j : i) {
				if (j.getBridgeCount() > 0) {
					if (j.getBridgeCount() != j.getMountedBridges()) {
						mainWindow.getControlPanel().setGameNotOverText();
						return false;
					}
				}
			}
		}
		// "Rätsel gelöst" Text
		mainWindow.getControlPanel().setGameOverText();
		return true;
	}

	/**
	 * Prüft, ob es eine Brücke existiert, die die Spielregeln nicht verletzt.
	 * 
	 * @param island
	 *            Insel
	 * @return true wenn eine Brücke existiert, die die Spielregeln nicht
	 *         verletzt, false wenn nicht.
	 */
	public boolean isStableBridgePossible(Island island) {
		Island upNgb = nextUpNgb(island);
		if (upNgb != null && upNgb.getBridgeCount() > upNgb.getMountedBridges()) {
			if (connectable(island, upNgb))
				return true;
			if (island.getUpNgb() != null && !island.isUpDouble())
				return true;
		}
		Island rightNgb = nextRightNgb(island);
		if (rightNgb != null && rightNgb.getBridgeCount() > rightNgb.getMountedBridges()) {
			if (connectable(island, rightNgb))
				return true;
			if (island.getRightNgb() != null && !island.isRightDouble())
				return true;
		}
		Island downNgb = nextDownNgb(island);
		if (downNgb != null && downNgb.getBridgeCount() > downNgb.getMountedBridges()) {
			if (connectable(island, downNgb))
				return true;
			if (island.getDownNgb() != null && !island.isDownDouble())
				return true;
		}
		Island leftNgb = nextLeftNgb(island);
		if (leftNgb != null && leftNgb.getBridgeCount() > leftNgb.getMountedBridges()) {
			if (connectable(island, leftNgb))
				return true;
			if (island.getLeftNgb() != null && !island.isLeftDouble())
				return true;
		}
		return false;
	}

	/**
	 * Fügt eine neue sichere Brücke zum Spielfeld ein, falls eine existiret
	 * 
	 * @return true eine Brücke wurde hinzugefügt, false - keine weitere sichere
	 *         Brücke gefunden
	 */
	public boolean addStableBridge() {
		// Finde eine sichere Brücke
		Bridge b = getStableBridge();
		if (b != null) {
			// Einfügen zur Liste der zuletzt eingefügten Brücken
			lastBridgesList.add(b);
			// Die Brücke setzten
			connect(b.getIsland1(), b.getIsland2());
			// // Anzahl von fehlender brücken -1
			// unMountedBridgeCount--;
			// Das Spieldfeld neu zeichnen
			repaint();
			// Prüfen, on das Rätsel gelöst ist
			isCompleted();
			return true;
		}
		if (!isCompleted()) {
			// Falls keine sichere Brücke gefunden wurde
			JOptionPane.showMessageDialog(this, "Keine konfliktfreie Brücke kann gefunden werden :(");
		}
		return false;
	}

	/**
	 * Gibt eine sichere Brücke zurück
	 * 
	 * @return eine sichere Brücke
	 */
	public Bridge getStableBridge() {
		// Anzahl von möglichen Nachbarn
		int ngb = 0;
		// Sichere einzelne Brücken ermitteln
		for (Island[] i : grid) {
			for (Island j : i) {
				if (j.getBridgeCount() > 0) {
					// Anzahl von möglichen Nachbarn
					ngb = 0;
					// mögliche Nachbarn
					Island rNgb = nextRightNgb(j);
					Island dNgb = nextDownNgb(j);
					Island uNgb = nextUpNgb(j);
					Island lNgb = nextLeftNgb(j);

					if (rNgb != null)
						if (connectable(j, rNgb) || j.getRightNgb() != null) {
							ngb++;
							j.setRightNgbPossible(true);
						}
					if (dNgb != null)
						if (connectable(j, dNgb) || j.getDownNgb() != null) {
							ngb++;
							j.setDownNgbPossible(true);
						}
					if (lNgb != null)
						if (connectable(j, lNgb) || j.getLeftNgb() != null) {
							ngb++;
							j.setLeftNgbPossible(true);
						}
					if (uNgb != null)
						if (connectable(j, uNgb) || j.getUpNgb() != null) {
							ngb++;
							j.setUpNgbPossible(true);
						}

					if (j.getBridgeCount() == ngb * 2 - 1 || j.getBridgeCount() == ngb * 2) {
						if (j.isUpNgbPossible() && j.getUpNgb() == null)
							return new Bridge(j, uNgb);
						if (j.isRightNgbPossible() && j.getRightNgb() == null)
							return new Bridge(j, rNgb);
						if (j.isDownNgbPossible() && j.getDownNgb() == null)
							return new Bridge(j, dNgb);
						if (j.isLeftNgbPossible() && j.getLeftNgb() == null)
							return new Bridge(j, lNgb);
					}
					j.setUpNgbPossible(false);
					j.setRightNgbPossible(false);
					j.setDownNgbPossible(false);
					j.setLeftNgbPossible(false);
				}
			}
		}
		// Brücken ermitteln, die nur eine einzige Verbindungsmöglichkeit haben.
		for (Island[] i : grid) {
			for (Island j : i) {
				if (j.getBridgeCount() > j.getMountedBridges()) {
					// Anzahl von möglichen Brücken
					int n = 0;
					// Anzahl von mögliche Brücken in jede Richtung
					int nLeft = 0;
					int nUp = 0;
					int nRight = 0;
					int nDown = 0;
					// mögliche Nachbarn
					Island rNgb = nextRightNgb(j);
					Island dNgb = nextDownNgb(j);
					Island uNgb = nextUpNgb(j);
					Island lNgb = nextLeftNgb(j);
					// Falls oben ein Nachbar existiert und bei ihm noch nicht
					// alle Brücken gesetzt sind
					if (uNgb != null && uNgb.getBridgeCount() - uNgb.getMountedBridges() > 0) {
						// Falls die Inseln mit einer Brücke verbunden sind
						if (j.getUpNgb() != null)
							if (!j.isUpDouble()) {
								nUp++;
								j.setUpNgbPossible(true);
							}
						// Falls die Inseln noch nicht verbunden sind
						if (connectable(j, uNgb)) {
							if (uNgb.getBridgeCount() - uNgb.getMountedBridges() > 1) {
								nUp = nUp + 2;
							}
							if (uNgb.getBridgeCount() - uNgb.getMountedBridges() == 1) {
								nUp++;
							}
							j.setUpNgbPossible(true);
						}
					}
					// Falls rechts ein Nachbar existiert und bei ihm noch nicht
					// alle Brücken gesetzt sind
					if (rNgb != null && rNgb.getBridgeCount() - rNgb.getMountedBridges() >= 1) {
						// Falls die Inseln mit einer Brücke verbunden sind
						if (j.getRightNgb() != null)
							if (!j.isRightDouble()) {
								nRight++;
								j.setRightNgbPossible(true);
							}
						// Falls die Inseln noch nicht verbunden sind
						if (connectable(j, rNgb)) {
							if (rNgb.getBridgeCount() - rNgb.getMountedBridges() > 1) {
								nRight = nRight + 2;
							}
							if (rNgb.getBridgeCount() - rNgb.getMountedBridges() == 1) {
								nRight++;
							}
							j.setRightNgbPossible(true);
						}
					}
					// Falls unten ein Nachbar existiert und bei ihm noch nicht
					// alle Brücken gesetzt sind
					if (dNgb != null && dNgb.getBridgeCount() - dNgb.getMountedBridges() >= 1) {
						// Falls die Inseln mit einer Brücke verbunden sind
						if (j.getDownNgb() != null)
							if (!j.isDownDouble()) {
								nDown++;
								j.setDownNgbPossible(true);
							}
						// Falls die Inseln noch nicht verbunden sind
						if (connectable(j, dNgb)) {
							if (dNgb.getBridgeCount() - dNgb.getMountedBridges() > 1) {
								nDown = nDown + 2;
							}
							if (dNgb.getBridgeCount() - dNgb.getMountedBridges() == 1) {
								nDown++;
							}
							j.setDownNgbPossible(true);
						}
					}
					// Falls links ein Nachbar existiert und bei ihm noch nicht
					// alle Brücken gesetzt sind
					if (lNgb != null && lNgb.getBridgeCount() - lNgb.getMountedBridges() >= 1) {
						// Falls die Inseln mit einer Brücke verbunden sind
						if (j.getLeftNgb() != null)
							if (!j.isLeftDouble()) {
								nLeft++;
								j.setLeftNgbPossible(true);
							}
						// Falls die Inseln noch nicht verbunden sind
						if (connectable(j, lNgb)) {
							if (lNgb.getBridgeCount() - lNgb.getMountedBridges() >= 2) {
								nLeft = nLeft + 2;
							}
							if (lNgb.getBridgeCount() - lNgb.getMountedBridges() == 1) {
								nLeft++;
							}
							j.setLeftNgbPossible(true);
						}
					}
					// Falls Anzahl von möglichen Brücken gleich dem Anzahl von
					// fehlender Brücken ist, dann sind alle Brücken sicher
					n = nUp + nRight + nDown + nLeft;
					if (j.getBridgeCount() > 1 && (j.getBridgeCount() - j.getMountedBridges()) == n) {
						if (j.isRightNgbPossible()) {
							return new Bridge(j, rNgb);
						}
						if (j.isDownNgbPossible()) {
							return new Bridge(j, dNgb);
						}
						if (j.isLeftNgbPossible()) {
							return new Bridge(j, lNgb);
						}
						if (j.isUpNgbPossible()) {
							return new Bridge(j, uNgb);
						}
					}
					if (j.getBridgeCount() > 1 && (j.getBridgeCount() - j.getMountedBridges()) == n - 1) {
						if (nRight == 2)
							return new Bridge(j, rNgb);
						if (nDown == 2)
							return new Bridge(j, dNgb);
						if (nLeft == 2)
							return new Bridge(j, lNgb);
						if (nUp == 2)
							return new Bridge(j, uNgb);
					}
					if (j.getBridgeCount() == 1) {
						if (uNgb != null && uNgb.getBridgeCount() == 1) {
							nUp = 0;
							j.setUpNgbPossible(false);
						}
						if (rNgb != null && rNgb.getBridgeCount() == 1) {
							nRight = 0;
							j.setRightNgbPossible(false);
						}
						if (dNgb != null && dNgb.getBridgeCount() == 1) {
							nDown = 0;
							j.setDownNgbPossible(false);
						}
						if (lNgb != null && lNgb.getBridgeCount() == 1) {
							nLeft = 0;
							j.setLeftNgbPossible(false);
						}
					}
					// Brücken, die nur einen möglichen Nachbar haben
					if (j.getMountedBridges() < j.getBridgeCount()) {
						if (j.isUpNgbPossible() && !j.isDownNgbPossible() && !j.isRightNgbPossible()
								&& !j.isLeftNgbPossible())
							return new Bridge(j, uNgb);
						if (!j.isUpNgbPossible() && j.isDownNgbPossible() && !j.isRightNgbPossible()
								&& !j.isLeftNgbPossible())
							return new Bridge(j, dNgb);
						if (!j.isUpNgbPossible() && !j.isDownNgbPossible() && j.isRightNgbPossible()
								&& !j.isLeftNgbPossible())
							return new Bridge(j, rNgb);
						if (!j.isUpNgbPossible() && !j.isDownNgbPossible() && !j.isRightNgbPossible()
								&& j.isLeftNgbPossible())
							return new Bridge(j, lNgb);
					}
					j.setUpNgbPossible(false);
					j.setRightNgbPossible(false);
					j.setDownNgbPossible(false);
					j.setLeftNgbPossible(false);
					n = 0;
				}
			}
		}
		return null;
	}

	/**
	 * Spiechert das Rätsel unter einem ausgewählten Pfad
	 * 
	 * @param file
	 *            Dateipfad
	 */
	public void savePuzzle(File file) {
		// leeres Spielfeld wird nicht gespeichert
		if (getIslandCount() > 0) {
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Bridges", "bgs");
			fc.setFileFilter(filter);
			fc.setCurrentDirectory(file);
			int returnVal = fc.showSaveDialog(getRootPane());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				// Dateierweiterung anhängen
				if (!file.toString().endsWith(".bgs"))
					file = new File(file.toString() + ".bgs");
				// Datei ersetzen Dialog
				if (file.exists()) {
					int response = JOptionPane.showConfirmDialog(getRootPane(),
							"Die Datei ist bereits vorhanden. Möchten Sie sie ersetzen?", "Confirm",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (response == JOptionPane.YES_OPTION)
						savePuzzleTo(file);
				} else {
					savePuzzleTo(file);
				}
			}
		} else {
			// Fehlermeldung nichts zum Speichern
			JOptionPane.showMessageDialog(getRootPane(), "Spielfeld ist leer!");
		}
	}

	/**
	 * Spiechert das Rätsel unter in file gespeichertem Pfad
	 * 
	 * @param file
	 *            Dateipfad
	 */
	public void savePuzzleTo(File file) {
		try {
			PrintWriter out = new PrintWriter(file);
			// Spieldfeldgröße speichern
			out.println("FIELD");
			out.println(getWidthPuzzle() + " x " + getHeightPuzzle() + " | " + getIslandCount());
			// Inseln numerieren, in eine Liste einfügen und speichern
			out.println("ISLANDS");
			for (Island[] i : getField())
				for (Island j : i) {
					if (j.getBridgeCount() > 0) {
						islandDataList.add(j.getN() - 1);
						out.print("( " + (j.getN() - 1));
						islandDataList.add(j.getM() - 1);
						out.print(", " + (j.getM() - 1));
						islandDataList.add(j.getBridgeCount());
						out.println(" | " + j.getBridgeCount() + " )");
					}
				}
			// Alle Brücken schreiben
			out.println("BRIDGES");
			Iterator<Integer> listIterator = islandDataList.listIterator();
			while (listIterator.hasNext()) {
				int n = listIterator.next();
				int m = listIterator.next();
				listIterator.next();
				Island target = getField()[n + 1][m + 1];
				if (target.getDownNgb() != null) {
					out.print("( " + target.getID());
					out.print(", " + target.getDownNgb().getID() + " | ");
					if (target.isDownDouble()) {
						out.println("true )");
					} else {
						out.println("false )");
					}
				}
				if (target.getRightNgb() != null) {
					out.print("( " + target.getID());
					out.print(", " + target.getRightNgb().getID() + " | ");
					if (target.isRightDouble()) {
						out.println("true )");
					} else {
						out.println("false )");
					}
				}
			}
			// Liste mit Inseln löschen
			islandDataList.clear();
			out.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		;
	}

	protected void paintComponent(Graphics g) {
		MainWindow mainWindow = (MainWindow) SwingUtilities.getWindowAncestor(getParent());
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g);
		for (Island[] i : grid) {
			for (Island j : i) {
				// horizontale Brücken zeichnen
				g2.setColor(Color.black);
				// zuletzt gesetzte Brücke ist grün
				if (!lastBridgesList.isEmpty())
					if (j == lastBridgesList.getLast().getIsland1() || j == lastBridgesList.getLast().getIsland2())
						if (j.getLeftNgb() == lastBridgesList.getLast().getIsland1()
								|| j.getLeftNgb() == lastBridgesList.getLast().getIsland2())
							g2.setColor(Color.green);

				if (j.isLeftDouble()) {
					g2.drawLine(radius / 2 + j.getN() * radius, radius / 2 + j.getM() * radius - 3,
							radius + j.getLeftNgb().getN() * radius - tolerance,
							radius / 2 + j.getLeftNgb().getM() * radius - 3);
					g2.drawLine(radius / 2 + j.getN() * radius, radius / 2 + j.getM() * radius,
							radius + j.getLeftNgb().getN() * radius - tolerance,
							radius / 2 + j.getLeftNgb().getM() * radius);
				} else if (j.getLeftNgb() != null) {
					g2.drawLine(radius / 2 + j.getN() * radius, radius / 2 + j.getM() * radius - 2,
							radius + j.getLeftNgb().getN() * radius - tolerance,
							radius / 2 + j.getLeftNgb().getM() * radius - 2);
				}
				// vertikale Brücken zeichnen
				g2.setColor(Color.black);
				if (!lastBridgesList.isEmpty())
					if (j == lastBridgesList.getLast().getIsland1() || j == lastBridgesList.getLast().getIsland2())
						if (j.getDownNgb() == lastBridgesList.getLast().getIsland1()
								|| j.getDownNgb() == lastBridgesList.getLast().getIsland2())
							g2.setColor(Color.green);

				if (j.isDownDouble()) {
					g2.drawLine(radius / 2 + j.getN() * radius - 3, j.getM() * radius,
							radius / 2 + j.getDownNgb().getN() * radius - 3, j.getDownNgb().getM() * radius);
					g2.drawLine(radius / 2 + j.getN() * radius, j.getM() * radius,
							radius / 2 + j.getDownNgb().getN() * radius, j.getDownNgb().getM() * radius);
				} else if (j.getDownNgb() != null) {
					g2.drawLine(radius / 2 + j.getN() * radius - 2, j.getM() * radius,
							radius / 2 + j.getDownNgb().getN() * radius - 2, j.getDownNgb().getM() * radius);
				}
				if (j.getBridgeCount() > 0) {
					// Kreis. d-1 = Diameter - Abstand zwischen Inseln
					g2.setColor(j.getColor());
					// "Gelöste" Inseln markieren
					if (j.getBridgeCount() == j.getMountedBridges())
						g.setColor(new Color(103, 255, 103));
					if (j.getBridgeCount() < j.getMountedBridges()) {
						g.setColor(new Color(255, 103, 103));
						mainWindow.getControlPanel().setPuzzleHasErrorText();
					}
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					drawCircle(g2, radius / 2 + j.getN() * radius, radius / 2 + j.getM() * radius, radius - tolerance);
					// Inselanzahl auf dem Kreis zeichnen
					g2.setColor(Color.black);
					g2.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
					// Anazahl der Brücken anzeigen
					int number = j.getBridgeCount();

					// oder Anzahl der Rest-Brücken, wenn checkbox ausgewählt
					// ist
					if (mainWindow.getControlPanel().isChkBoxSelected()) {
						number = j.getBridgeCount() - j.getMountedBridges();
						if (number < 0)
							number = 0;
					}
					g2.drawString(String.valueOf(number), radius / 2 - 5 + j.getN() * radius,
							radius / 2 + 4 + j.getM() * radius);
				}
			}
		}
	}

	/**
	 * Zeichnet ein Kreis
	 * 
	 * @param g
	 *            graphische Komponente
	 * @param x
	 *            x-Koordinate
	 * @param y
	 *            y-Koordinate
	 * @param r
	 *            Radius
	 */
	public void drawCircle(Graphics2D g, int x, int y, int r) {
		g.fillOval(x - radius / 2, y - radius / 2, r, r);
	}

	/** Setzt Invalid-Cursor */
	public void setInvalidCursor() {
		try {
			setCursor(Cursor.getSystemCustomCursor("Invalid.32x32"));
		} catch (HeadlessException e1) {
			// ignore
			e1.printStackTrace();
		} catch (AWTException e1) {
			// ignore
			e1.printStackTrace();
		}
	}

	public void setAnyIsland(Island i) {
		this.anyIsland = i;
	}

	public int getDiameter() {
		return radius;
	}

	public int getWidthPuzzle() {
		return widthPuzzle;
	}

	public void setWidthPuzzle(int w) {
		this.widthPuzzle = w;
	}

	public int getHeightPuzzle() {
		return heightPuzzle;
	}

	public void setHeightPuzzle(int h) {
		this.heightPuzzle = h;
	}

	public int getIslandCount() {
		return islandCount;
	}

	public void setIslandCount(int islandCount) {
		this.islandCount = islandCount;
	}

	public Island[][] getField() {
		return grid;
	}

	public LinkedList<Bridge> getLastBridgesList() {
		return lastBridgesList;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {

		// Klickkoordinaten
		int xClick = e.getX();
		int yClick = e.getY();
		// den angecklikten Insel idetifizieren
		int n = Math.round((xClick) / radius);
		int m = Math.round((yClick) / radius);
		Island f = grid[n][m];
		int yIsland = m * radius + radius / 2;
		int xIsland = n * radius + radius / 2;
		// leere Inseln ignorieren
		if (n < widthField && m < heightField) {
			if (f.getBridgeCount() > 0) {
				// Rechter Sektor
				if ((xClick > xIsland) && (Math.abs(xClick - xIsland) >= Math.abs(yClick - yIsland))) {
					// linke Maustastede
					if (SwingUtilities.isLeftMouseButton(e)) {
						if (f.getRightNgb() != null && !f.isRightDouble()) {
							connect(f, nextRightNgb(f));
							lastBridgesList.add(new Bridge(f, nextRightNgb(f)));
						} else if (nextRightNgb(f) != null && f.getRightNgb() == null) {
							connect(f, nextRightNgb(f));
							lastBridgesList.add(new Bridge(f, nextRightNgb(f)));
						} else
							setInvalidCursor();
					}
					// rechte Maustaste
					if (SwingUtilities.isRightMouseButton(e)) {
						if (f.getRightNgb() != null) {
							removeBridge(f, f.getRightNgb());
						}
					}
				}
				// Linker Sektor
				if ((xClick < xIsland) && (Math.abs(xClick - xIsland) >= Math.abs(yClick - yIsland))) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						if (f.getLeftNgb() != null && !f.isLeftDouble()) {
							connect(f, nextLeftNgb(f));
							lastBridgesList.add(new Bridge(f, nextLeftNgb(f)));
						} else if (nextLeftNgb(f) != null && f.getLeftNgb() == null) {
							connect(f, nextLeftNgb(f));
							lastBridgesList.add(new Bridge(f, nextLeftNgb(f)));
						} else
							setInvalidCursor();
					}
					// rechte Maustaste
					if (SwingUtilities.isRightMouseButton(e)) {
						if (f.getLeftNgb() != null) {
							removeBridge(f, f.getLeftNgb());
						}
					}
				}
				// Oberer Sektor
				if ((yClick < yIsland) && (Math.abs(xClick - xIsland) < Math.abs(yClick - yIsland))) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						if (f.getUpNgb() != null && !f.isUpDouble()) {
							connect(f, nextUpNgb(f));
							lastBridgesList.add(new Bridge(f, nextUpNgb(f)));
						} else if (nextUpNgb(f) != null && f.getUpNgb() == null) {
							connect(f, nextUpNgb(f));
							lastBridgesList.add(new Bridge(f, nextUpNgb(f)));
						} else
							setInvalidCursor();
					}
					// rechte Maustaste
					if (SwingUtilities.isRightMouseButton(e)) {
						if (f.getUpNgb() != null) {
							removeBridge(f, f.getUpNgb());
						}
					}
				}
				// Unterer Sektor
				if ((yClick > yIsland) && (Math.abs(xClick - xIsland) < Math.abs(yClick - yIsland))) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						if (f.getDownNgb() != null && !f.isDownDouble()) {
							connect(f, nextDownNgb(f));
							lastBridgesList.add(new Bridge(f, nextDownNgb(f)));

						} else if (nextDownNgb(f) != null && f.getDownNgb() == null) {
							connect(f, nextDownNgb(f));
							lastBridgesList.add(new Bridge(f, nextDownNgb(f)));
						} else
							setInvalidCursor();
					}
					// rechte Maustaste
					if (SwingUtilities.isRightMouseButton(e)) {
						if (f.getDownNgb() != null) {
							removeBridge(f, f.getDownNgb());
						}
					}
				}
			}
			repaint();
			// Prüfen, ob Rätsel gelöst ist
//			isCompleted();
		}
		isCompleted();
	}

	public void mouseReleased(MouseEvent e) {
		setCursor(Cursor.getDefaultCursor());
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
}
