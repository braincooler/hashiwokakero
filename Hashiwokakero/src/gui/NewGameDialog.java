package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import objects.Field;

/**
 * Die Klasse stellt ein Dialog zum Erzeugen eines neuen Rätsels dar.
 * 
 * @author W. Zeller
 */
@SuppressWarnings("serial")
public class NewGameDialog extends JDialog {
	private GridBagConstraints gbc = new GridBagConstraints();

	/** Das Label "Breite" */
	private JLabel widthLabel = new JLabel("Breite: ");

	/** Das Textfeld "Breite" */
	private JTextField widthText = new JTextField("10", 5);

	/** Das Label "Höhe" */
	private JLabel heightLabel = new JLabel("Höhe: ");

	/** Das Textfeld "Höhe" */
	private JTextField heightText = new JTextField("10", 5);

	/** CheckBox "Inselanzahl festlegen:" */
	private JCheckBox islandChkBox = new JCheckBox("Inselanzahl festlegen:");

	/** Das Textfeld "Inselnanzahl" */
	private JTextField islandText = new JTextField("20", 5);

	/** OK Button */
	private JButton okButton = new JButton("OK");

	/** Das Optionsfeld "Automatische Größe und Inselanzahl" */
	private JRadioButton randomRadioButton = new JRadioButton("Automatische Größe und Inselanzahl");

	/** Das Optionsfeld "Größe und/oder Inselanzahl festlegen" */
	private JRadioButton customRadioButton = new JRadioButton("Größe und/oder Inselanzahl festlegen");

	/** Referenz auf das neu erstelltes Spielfeld */
	private Field savedField;

	/** Breite, Höhe und Inselanzahl des Spielfeldes */
	private int m, n, c;

	public NewGameDialog(Field f) {
		savedField = f;
		setSize(325, 250);
		setResizable(false);
		setLayout(new GridBagLayout());
		setModal(true);
		setLocationRelativeTo(null);
		// RadioButtons zusammen fassen um sicher zu sein, dass nur eine gewählt
		// ist
		ButtonGroup bg = new ButtonGroup();
		randomRadioButton.setSelected(true);
		bg.add(randomRadioButton);
		bg.add(customRadioButton);

		// OkButton action
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Zufälliges Rätsel wird erstellt
				if (randomRadioButton.isSelected()) {
					m = 4 + (int) (Math.random() * 22);
					n = 4 + (int) (Math.random() * 22);
					c = 2 + (int) (Math.random() * (0.2 * m * n - 2));
					savedField = new Field(m, n, c);
					dispose();
				} else {
					// Rätsel mit vorgegebener Breite und Höhe wird erstellt
					try {
						m = Integer.parseInt(widthText.getText());
						n = Integer.parseInt(heightText.getText());

						// entweder Rätsel mit festem Inselanzahl oder zufällig
						// aus dem Intervall [min(n, m), n*m*0.2]
						if (islandChkBox.isSelected()) {
							c = Integer.parseInt(islandText.getText());
						} else {
							c = Math.min(n, m) + (int) (Math.random() * (Math.max(n, m) / 5 - Math.min(n, m)));
						}
						if (n < 4 || m < 4 || n > 25 || m > 25 || c < 2 || c > n * m * 0.2) {
							throw new IllegalArgumentException();
						} else {
							savedField = new Field(m, n, c);
							dispose();
						}
					} catch (NumberFormatException inte) {
						JOptionPane.showMessageDialog(rootPane, "Geben Sie bitte nur Zahlen ein");
					} catch (IllegalArgumentException ille) {
						JOptionPane.showMessageDialog(rootPane, "Falsches Bereich");
					}
				}
			}
		});
		// cancelButton action
		JButton cancelButton = new JButton("Abbrechen");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Dialog schließen
				dispose();
			}
		});

		// 1 Spalte
		gbc.insets = new Insets(5, 0, 0, 5);
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridx = 1;
		gbc.gridy = 1;
		add(randomRadioButton, gbc);
		gbc.gridy = 2;
		add(customRadioButton, gbc);
		gbc.gridy = 7;
		add(okButton, gbc);

		// 2. Spalte
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.gridwidth = 1;
		gbc.gridx = 2;
		gbc.gridy = 3;
		add(widthLabel, gbc);
		gbc.gridy = 4;
		add(heightLabel, gbc);
		gbc.gridy = 5;
		add(islandChkBox, gbc);
		gbc.gridy = 7;
		add(cancelButton, gbc);

		// 3.Spalte
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.gridx = 3;
		gbc.gridy = 3;
		add(widthText, gbc);
		gbc.gridy = 4;
		add(heightText, gbc);
		gbc.gridy = 5;
		add(islandText, gbc);
	}

	/**
	 * Gibt das neu erstellte Spielfeld zurück
	 * 
	 * @return das neue Spielfeld
	 */
	public Field getField() {
		return savedField;
	}
}
