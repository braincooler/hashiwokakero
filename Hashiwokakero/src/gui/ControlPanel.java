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
 * Schaltfl�chen �Automatisch l�sen� und �N�chste Br�cke�, CheckBox
 * "Anzahl der fehlender Br�cken" und Statuszeile platziert.
 * 
 * @author W. Zeller
 */
@SuppressWarnings("serial")
public class ControlPanel extends JPanel {

	/** Gibt an, ob automatisches L�sen aktiviert ist */
	private boolean isAutoResolveStart = false;

	/** CheckBox "Anzahl fehlender Br�cken anzeigen" */
	private JCheckBox bridgeCountChkBox = new JCheckBox("Anzahl fehlender Br�cken anzeigen");

	/** Button "Automatisch l�sen" */
	private JButton autoResolveBtn = new JButton("Automatisch l�sen");

	/** Button "N�chste Br�cke" */
	private JButton nextBridgeBtn = new JButton("N�chste Br�cke");
	
	/** Spielstatusanzeige */
	private JLabel stateLine = new JLabel();

	/**
	 * Erstellt eine neue ControlPanel mit Referenz auf MainWindow
	 * 
	 * @param mainWindow
	 *            Referenz auf MainWindow
	 */
	ControlPanel(MainWindow mainWindow) {
		// Die Gr��e festsetzen
		setPreferredSize(new Dimension(250, 68));
		setLayout(null);

		// Schaltfl�che "Automatisch l�sen"
		// Die Gr��e festlegen
		autoResolveBtn.setBounds(4, 4, 150, 18);
		autoResolveBtn.setFocusPainted(false);
		autoResolveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// Umschalten, ob automatisches L�sen aktiviert ist, oder nicht
				if (isAutoResolveStart)
					isAutoResolveStart = false;
				else
					isAutoResolveStart = true;

				// Nach einer Spanne von 2 Sekunden wird eine sichere Br�cke
				// nizugef�gt
				Timer timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					public void run() {

						// Falls automatisches L�sen bereits aktiv ist, wird
						// Timer abgebrochen
						if (!isAutoResolveStart) {
							autoResolveBtn.setText("Automatisch l�sen");
							timer.cancel();
						} else {
							autoResolveBtn.setText("anhalten...");
						}

						// Falls keine weitere sichere Br�cke existiert, wird
						// Timer abgebrochen
						if (!mainWindow.getFieldPanel().addStableBridge()) {
							autoResolveBtn.setText("Automatisch l�sen");
							isAutoResolveStart = false;
							timer.cancel();
						}
					}
				}, 0, 2000);
			}
		});
		add(autoResolveBtn);

		// Schaltfl�che "N�chste Br�cke"
		nextBridgeBtn.setBounds(4, 23, 150, 18);
		nextBridgeBtn.setFocusPainted(false);
		nextBridgeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Eine sichere Br�cke einf�gen
				mainWindow.getFieldPanel().addStableBridge();
			}
		});
		add(nextBridgeBtn);

		// Kontrollk�stchen "Anzahl fehlender Br�cken anzeigen"
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
	 * Setzt den Text "Das R�tsel ist nicht mehr l�sbar!" auf die
	 * Spielstatusanzeige
	 */
	public void setPuzzleIsNotMoreSolvableText() {
		stateLine.setForeground(Color.RED);
		stateLine.setText(" Das R�tsel ist nicht mehr l�sbar!");
	}

	/**
	 * Setzt den Text "Das R�tsel enth�lt ein Fehler!" auf die
	 * Spielstatusanzeige
	 */
	public void setPuzzleHasErrorText() {
		stateLine.setForeground(Color.RED);
		stateLine.setText(" Das R�tsel enth�lt ein Fehler!");
	}

	/** Setzt den Text "Gel�st!" auf die Spielstatusanzeige */
	public void setGameOverText() {
		stateLine.setForeground(Color.GREEN);
		stateLine.setText(" Gel�st!");
	}

	/**
	 * Setzt den Text "Das R�tsel ist noch nicht gel�st!" auf die
	 * Spielstatuszeile
	 */
	public void setGameNotOverText() {
		stateLine.setForeground(Color.BLACK);
		stateLine.setText(" Das R�tsel ist noch nicht gel�st!");
	}

	// Getter/Setter
	public void setEmptyText() {
		stateLine.setText("");
	}

	public void setIsAutoResolveStart(boolean bool) {
		isAutoResolveStart = bool;

	}
}
