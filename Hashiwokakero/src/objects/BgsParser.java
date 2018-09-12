package objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Parst eine Datei und speichert abgelesenes Rätsel in Field
 * 
 * @author wzell
 *
 */

public class BgsParser {
	/** Dateipfad */
	private File file;

	/** Referenz auf das Spielfeld */
	private Field fieldPanel;

	/** Liste mit allen Zeilen aus einer Datei */
	public LinkedList<String> lines = new LinkedList<String>();

	/**
	 * Parst file
	 * 
	 * @param file der Dateipfad
	 */
	public BgsParser(File file) {
		this.file = file;
		// Die Datei lesen
		readFile();
		// Die Spielfeldgrößen ablesen
		readFieldSize();
		// Fehlermeldung, falls die Inseln falsch auf dem Spielfeld platziert
		// sind
		if (!readIslands()) {
			JOptionPane op = new JOptionPane("Das Rätsel ist ungültig");
			JDialog dialog = op.createDialog("");
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
		}
		readBridges();
	}

	/** Liest alle Zeilen aus der Datei und speichert die in lines */
	private void readFile() {
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader in = new BufferedReader(fileReader);
			String line = null;
			try {
				while ((line = in.readLine()) != null) {
					if (!line.startsWith("#") && !line.isEmpty()) {
						line = line.replaceAll("\\s", "");
						lines.add(line);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** Liest die Spielfeldgröße ab */
	private void readFieldSize() {
		String[] s = lines.get(1).split("[x| ]");
		try {
			int width = Integer.valueOf(s[0]);
			int height = Integer.valueOf(s[1]);
			if (width < 4 || width > 25 || height < 4 || height > 25)
				throw new IllegalArgumentException();
			fieldPanel = new Field(Integer.valueOf(s[0]), Integer.valueOf(s[1]));
			fieldPanel.setIslandCount(Integer.valueOf(s[2]));
			// Array mit leeren Inseln fülen
			for (int i = 0; i < fieldPanel.getField().length; i++)
				for (int j = 0; j < fieldPanel.getField()[i].length; j++) {
					if (fieldPanel.getField()[i][j] == null) {
						fieldPanel.getField()[i][j] = new Island(i, j);
						fieldPanel.getField()[i][j].setBridgeCount(0);
					}
				}
			lines.removeFirst();
			lines.removeFirst();
		} catch (NumberFormatException nfe) {
			JOptionPane op = new JOptionPane("Das Rätsel ist ungültig");
			JDialog dialog = op.createDialog("");
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
		}
	}

	/**
	 * Liest Inseln ab
	 * 
	 * @return true wenn die Inseln korrekt platziet sind und false wenn nicht.
	 */
	private boolean readIslands() {
		lines.removeFirst();
		int num = 0;
		while (!lines.isEmpty() && !lines.getFirst().equals("BRIDGES")) {
			String s = lines.getFirst();
			lines.removeFirst();
			String s1 = s.replaceAll("[()| ]", ",");
			String[] s2 = s1.split(",");
			int x = Integer.valueOf(s2[1]) + 1;
			int y = Integer.valueOf(s2[2]) + 1;
			int n = Integer.valueOf(s2[3]);
			fieldPanel.getField()[x][y] = new Island(x, y);
			fieldPanel.getField()[x][y].setBridgeCount(n);
			fieldPanel.getField()[x][y].setID(num);
			num++;
		}
		// Prüfen, ob zwischen Inseln Abstand = 1 ist.
		for (Island i[] : fieldPanel.getField())
			for (Island j : i)
				if (j.getBridgeCount() > 0) {
					if (fieldPanel.getField()[j.getN() + 1][j.getM()].getBridgeCount() > 0
							|| fieldPanel.getField()[j.getN()][j.getM() - 1].getBridgeCount() > 0)
						return false;
					if (fieldPanel.getField()[j.getN() - 1][j.getM()].getBridgeCount() > 0
							|| fieldPanel.getField()[j.getN()][j.getM() + 1].getBridgeCount() > 0)
						return false;
				}
		return true;
	}

	/** Liest Brücken ab */
	private void readBridges() {
		if (!lines.isEmpty()) {
			// erstellen einer Liste mit allen Inseln
			LinkedList<Island> allBridges = new LinkedList<Island>();
			for (Island i[] : fieldPanel.getField())
				for (Island j : i)
					if (j.getBridgeCount() > 0)
						allBridges.add(j);
			fieldPanel.setAnyIsland(allBridges.getFirst());
			// String "BRIDGES" entfernen
			lines.removeFirst();
			// void abbrechen falls keine Brücken da sind
			if (lines.isEmpty())
				return;
			// Brücken einlesen
			while (lines.getFirst() != null) {
				// einzelne Brücke setzen
				if (lines.getFirst().contains("false")) {
					String s = lines.getFirst();
					String s1 = s.replaceAll("[()|falsetrue ]", ",");
					String[] s2 = s1.split(",");
					Island a = allBridges.get(Integer.valueOf(s2[1]));
					Island b = allBridges.get(Integer.valueOf(s2[2]));
					fieldPanel.connect(a, b);
				}
				// doppelte Brücke einsetzen
				if (lines.getFirst().contains("true")) {
					String s = lines.getFirst();
					String s1 = s.replaceAll("[()|falsetrue ]", ",");
					String[] s2 = s1.split(",");
					Island a = allBridges.get(Integer.valueOf(s2[1]));
					Island b = allBridges.get(Integer.valueOf(s2[2]));
					fieldPanel.connect(a, b);
					fieldPanel.connect(a, b);
				}
				lines.removeFirst();
				if (lines.isEmpty())
					break;
			}
		}
	}

	// Setter/Getter */
	public Field getFieldPanel() {
		return fieldPanel;
	}
}
