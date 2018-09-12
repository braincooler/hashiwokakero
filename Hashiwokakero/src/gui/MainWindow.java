package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import objects.BgsParser;
import objects.Field;

/**
 * Die Klasse stellt Das Hauptfenster dar, auf dem das Spielfeld und das
 * Kontrollpanel sind. Das Hauptfenster hat eine Menueleiste.
 * 
 * @author W. Zeller
 *
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame implements ActionListener {

	/** Das linke R�tsel und das rechte R�tsel */
	private Field leftField, rightField;

	/** Dateipfad, um das Raestel zu speichern */
	private File file;

	/** "Neues Spiel" Dialog */
	private NewGameDialog newGameDialog;

	/** Das Spielfeld */
	private Field fieldPanel;

	/** Das Kontrollpanel */
	private ControlPanel controlPanel;

	/** Menueleiste und ihre Elemente */
	private JMenuBar menuBar = new JMenuBar();
	private JMenu menu = new JMenu("Datei");
	private JMenuItem item1 = new JMenuItem("Neues R�tsel");
	private JMenuItem item2 = new JMenuItem("R�tsel neu starten");
	private JMenuItem item3 = new JMenuItem("R�tsel laden");
	private JMenuItem item4 = new JMenuItem("R�tsel speichern");
	private JMenuItem item5 = new JMenuItem("R�tsel speichern unter ...");
	private JMenuItem item6 = new JMenuItem("Beenden");
	private JMenuItem item7 = new JMenuItem("Neues R�tsel aus zwei bestehenden R�tseln ...");

	public static void main(String[] arg) {
		SwingUtilities.invokeLater(() -> {
				MainWindow mainWindow = new MainWindow();
				mainWindow.init();
			});
	}

	public void init() {
		// Die Fenstergr��e wird nicht ver�ndert.
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Men�elemente hinzuf�gen
		menu.add(item1);
		item1.addActionListener(this);
		menu.add(item2);
		item2.addActionListener(this);
		menu.add(item3);
		item3.addActionListener(this);
		menu.add(item4);
		item4.addActionListener(this);
		menu.add(item5);
		item5.addActionListener(this);
		menu.add(item6);
		item6.addActionListener(this);
		menu.add(item7);
		item7.addActionListener(this);
		// Die Men�leiste hinzuf�gen
		menuBar.add(menu);

		// Das Proramm erstellt beim Start ein R�tsel mit 2 Inseln
		fieldPanel = new Field(15, 10, 2);
		controlPanel = new ControlPanel(this);
		setNewFieldPanel(fieldPanel);
		setJMenuBar(menuBar);
		setVisible(true);
	}

	/**
	 * Entfernt altes und f�gt neues Spielfeld ein
	 * 
	 * @param field
	 *            neues Spielfeld
	 */
	public void setNewFieldPanel(Field field) {
		this.setLayout(new BorderLayout(3, 3));
		getContentPane().remove(fieldPanel);
		getContentPane().remove(controlPanel);
		fieldPanel = field;
		getContentPane().add(fieldPanel, BorderLayout.WEST);
		getContentPane().add(controlPanel, BorderLayout.CENTER);
		pack();
		repaint();
	}

	public void actionPerformed(ActionEvent e) {

		// Men� "Neues R�tsel"
		if (e.getSource() == item1) {

			// Dialog "Neues R�stel �ffnen
			newGameDialog = new NewGameDialog(fieldPanel);
			newGameDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			newGameDialog.setVisible(true);

			// Das neue Spielfeld aufdem Haputfenster platzieren
			setNewFieldPanel(newGameDialog.getField());

			if (fieldPanel != null) {
				// Pr�fen, ob R�tsel gel�st ist
				fieldPanel.isCompleted();
			}
		}
		// Men� "R�tsel neu starten"
		if (e.getSource() == item2) {
			int response = JOptionPane.showConfirmDialog(rootPane, "Alle Br�cken werden gel�scht!", "Confirm",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.YES_OPTION) {
				// Alle Br�cken entfernen
				fieldPanel.removeAllBridges();
				// Anzahl aller Br�cken berechnen
				// fieldPanel.calculateAllBridgeCount();
				// Automatisches L�sen deaktiviren
				controlPanel.setIsAutoResolveStart(false);
				// Statuszeile "R�tsel ist noch nicht gel�st"
				controlPanel.setGameNotOverText();
				// Das Hauptfenster neu zeichnen
				repaint();
			}
		}
		// Men� R�tsel laden
		if (e.getSource() == item3) {
			JFileChooser fc = new JFileChooser();
			// Dateifilter
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Bridges", "bgs");
			fc.setFileFilter(filter);
			// Aktueller Dateipfad merken
			fc.setCurrentDirectory(file);
			int returnVal = fc.showOpenDialog(rootPane);
			// Falls eine Datei ausgew�hlt wurde, wird der Pfad an die KLass
			// BgsParser weitergeleitet
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				BgsParser parser = new BgsParser(file);
				// Das alte Spielfeld wird mit dem neu eingelesenem Spielfeld
				// ersetzt
				Field field = parser.getFieldPanel();
				if (field != null) {
					controlPanel.setIsAutoResolveStart(false);
					setNewFieldPanel(field);
					fieldPanel.isCompleted();
					repaint();
				}
			} else
				JOptionPane.showMessageDialog(rootPane, "Keine Datei ausgew�hlt");
		}
		// Men� "Speichern"
		if (e.getSource() == item4) {
			// Aufforderung eine Datei auszuw�hlen, falls keine Datei ausgew�hlt
			// ist.
			if (file != null)
				fieldPanel.savePuzzleTo(file);
			else
				fieldPanel.savePuzzle(file);
		}

		// Men� "Speichern unter..."
		if (e.getSource() == item5) {
			fieldPanel.savePuzzle(file);
		}
		// Men� "Beenden"
		if (e.getSource() == item6) {
			System.exit(0);
		}
		// Menu Komposition zweier R�tsel
		if (e.getSource() == item7) {
			// Dateifilter
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Bridges", "bgs");

			// Das linke R�tsel ausw�hlen
			JFileChooser leftFc = new JFileChooser();
			leftFc.setDialogTitle("Das linke R�tsel ausw�hlen");
			leftFc.setFileFilter(filter);
			// Aktueller Dateipfad merken
			leftFc.setCurrentDirectory(file);
			int returnVal = leftFc.showOpenDialog(rootPane);
			// Falls eine Datei ausgew�hlt wurde, wird der Pfad an BgsParser
			// weitergeleitet
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = leftFc.getSelectedFile();
				BgsParser parser = new BgsParser(file);
				leftField = parser.getFieldPanel();
				// Alle Br�cken l�schen
				leftField.removeAllBridges();
				if (leftField != null) {
					controlPanel.setIsAutoResolveStart(false);

				}
			} else {
				JOptionPane.showMessageDialog(rootPane, "Keine Datei ausgew�hlt");
				return;
			}

			// Das rechte R�tsel ausw�hlen
			JFileChooser rightFc = new JFileChooser();
			rightFc.setDialogTitle("Das rechte R�tsel ausw�hlen");
			rightFc.setFileFilter(filter);

			// Aktueller Dateipfad merken
			rightFc.setCurrentDirectory(file);
			int returnVal2 = rightFc.showOpenDialog(rootPane);

			// Falls eine Datei ausgew�hlt wurde, wird der Pfad an BgsParser
			// weitergeleitet
			if (returnVal2 == JFileChooser.APPROVE_OPTION) {
				file = rightFc.getSelectedFile();
				BgsParser parser = new BgsParser(file);
				rightField = parser.getFieldPanel();
				rightField.removeAllBridges();
				if (rightField != null) {
					controlPanel.setIsAutoResolveStart(false);
				}
			} else {
				JOptionPane.showMessageDialog(rootPane, "Keine Datei ausgew�hlt");
				return;
			}
			// Maximale Gr��e zweier R�tsel 25x25
			if (leftField.getHeightPuzzle() + rightField.getHeightPuzzle() > 25) {
				JOptionPane.showMessageDialog(rootPane, "Hohe > 25");
				return;
			}
			if (leftField.getWidthPuzzle() + rightField.getWidthPuzzle() > 25) {
				JOptionPane.showMessageDialog(rootPane, "Breite > 25");
				return;
			}
			setNewFieldPanel(new Field(leftField, rightField));
			fieldPanel.isCompleted();
			repaint();
		}
	}

	// Getter/Setter
	public Field getFieldPanel() {
		return fieldPanel;
	}

	public ControlPanel getControlPanel() {
		return controlPanel;
	}
}
