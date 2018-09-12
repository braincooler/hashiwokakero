package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Die Klasse stellt das Kontrollpanel dar. Auf Dem Kontrollpanel sind zwei
 * Schaltflächen „Automatisch lösen“ und „Nächste Brücke“, CheckBox
 * "Anzahl der fehlender Brücken" und Statuszeile platziert.
 * 
 * @author W. Zeller
 */
@SuppressWarnings("serial")
public class ControlPanel extends JPanel {

	/** Gibt an, ob automatisches Lösen aktiviert ist */
	private boolean isAutoResolveStart = false;

	/** CheckBox "Anzahl fehlender Brücken anzeigen" */
	private JCheckBox bridgeCountChkBox = new JCheckBox("Anzahl fehlender Brücken anzeigen");

	/** Button "Automatisch lösen" */
	private JButton autoResolveBtn = new JButton("Automatisch lösen");

	/** Button "Nächste Brücke" */
	private JButton nextBridgeBtn = new JButton("Nächste Brücke");
	
	/** Spielstatusanzeige */
	private JLabel stateLine = new JLabel();

	/**
	 * Erstellt eine neue ControlPanel mit Referenz auf MainWindow
	 * 
	 * @param mainWindow
	 *            Referenz auf MainWindow
	 */
	ControlPanel(MainWindow mainWindow) {
		// Die Größe festsetzen
		setPreferredSize(new Dimension(250, 68));
		setLayout(null);

		// Schaltfläche "Automatisch lösen"
		// Die Größe festlegen
		autoResolveBtn.setBounds(4, 4, 150, 18);
		autoResolveBtn.setFocusPainted(false);
		autoResolveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// Umschalten, ob automatisches Lösen aktiviert ist, oder nicht
				if (isAutoResolveStart)
					isAutoResolveStart = false;
				else
					isAutoResolveStart = true;

				// Nach einer Spanne von 2 Sekunden wird eine sichere Brücke
				// nizugefügt
				Timer timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					public void run() {

						// Falls automatisches Lösen bereits aktiv ist, wird
						// Timer abgebrochen
						if (!isAutoResolveStart) {
							autoResolveBtn.setText("Automatisch lösen");
							timer.cancel();
						} else {
							autoResolveBtn.setText("anhalten...");
						}

						// Falls keine weitere sichere Brücke existiert, wird
						// Timer abgebrochen
						if (!mainWindow.getFieldPanel().addStableBridge()) {
							autoResolveBtn.setText("Automatisch lösen");
							isAutoResolveStart = false;
							timer.cancel();
						}
					}
				}, 0, 2000);
			}
		});
		add(autoResolveBtn);

		// Schaltfläche "Nächste Brücke"
		nextBridgeBtn.setBounds(4, 23, 150, 18);
		nextBridgeBtn.setFocusPainted(false);
		nextBridgeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Eine sichere Brücke einfügen
				mainWindow.getFieldPanel().addStableBridge();
			}
		});
		add(nextBridgeBtn);

		// Kontrollkästchen "Anzahl fehlender Brücken anzeigen"
		bridgeCountChkBox.setBounds(0, 64, 300, 13);
		bridgeCountChkBox.setFocusPainted(false);
		bridgeCountChkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				mainWindow.getFieldPanel().repaint();
			}
		});
		add(bridgeCountChkBox);

		// Statuszeile
		stateLine.setBounds(4, 120, 300, 18);
		this.setGameNotOverText();
		add(stateLine);
		setVisible(true);
	}

	public boolean isChkBoxSelected() {
		if (bridgeCountChkBox.isSelected())
			return true;
		return false;
	}

	/**
	 * Setzt den Text "Das Rätsel ist nicht mehr lösbar!" auf die
	 * Spielstatusanzeige
	 */
	public void setPuzzleIsNotMoreSolvableText() {
		stateLine.setForeground(Color.RED);
		stateLine.setText(" Das Rätsel ist nicht mehr lösbar!");
	}

	/**
	 * Setzt den Text "Das Rätsel enthält ein Fehler!" auf die
	 * Spielstatusanzeige
	 */
	public void setPuzzleHasErrorText() {
		stateLine.setForeground(Color.RED);
		stateLine.setText(" Das Rätsel enthält ein Fehler!");
	}

	/** Setzt den Text "Gelöst!" auf die Spielstatusanzeige */
	public void setGameOverText() {
		stateLine.setForeground(Color.GREEN);
		stateLine.setText(" Gelöst!");
	}

	/**
	 * Setzt den Text "Das Rätsel ist noch nicht gelöst!" auf die
	 * Spielstatuszeile
	 */
	public void setGameNotOverText() {
		stateLine.setForeground(Color.BLACK);
		stateLine.setText(" Das Rätsel ist noch nicht gelöst!");
	}

	// Getter/Setter
	public void setEmptyText() {
		stateLine.setText("");
	}

	public void setIsAutoResolveStart(boolean bool) {
		isAutoResolveStart = bool;

	}
}
