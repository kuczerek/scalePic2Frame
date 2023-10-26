package kuczerek.scalePic2Frame;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;


import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ControlComponent extends JPanel {
	
	/**
	 * Die Klasse erzeugt ein JPanel, welches im oberen Teil des Fensters angezeigt wird. Hierdrin befinden sich die Button
	 * zum um Verzeichnisse zum Verarbeiten auszuwählen, sowie ein Button um die Einstellungen zu ändern.
	 * Darüber hinaus werden die aktuellen Einstellungen aus kuczerek.scalePic2Frame.Specs ausgegeben
	 * 
	 * Wird instanziert von Desktop
	 */
	private static final long serialVersionUID = -6764173087459147523L;
	public final static String BUT_VERZ_LABEL = "Verzeichnisse zur Fotoskalierung auswählen";
	public final static String BUT_EINST_LABEL = "Einstellungen ändern";
	
    private JLabel jLPhotoAlbumPath;
    private JLabel jLTargetPath;
    private JLabel jLMinWidthHeight;
    private JLabel jLMaxScaleWidthHeight;
    private JLabel jLTextSize;
	
	public ControlComponent(Desktop desktop) {
		
		super(new GridBagLayout());
		
		JPanel leer;
		GridBagConstraints c = new GridBagConstraints();
	    //c.gridwidth = GridBagConstraints.REMAINDER;
	
	    c.fill = GridBagConstraints.BOTH;
	    c.weightx = 0;
	    c.gridx = 0;
	    c.gridy = GridBagConstraints.RELATIVE;

	    /*
	     * Erzeugen der ersten Spalte
	     */
	    
	    leer = new JPanel();
	    c.weighty = 1;
	    add(leer,c);

	    c.weighty = 0;
	    JButton butVerz = new JButton(BUT_VERZ_LABEL);
	    butVerz.addActionListener(desktop);
	    add(butVerz, c);
	    
	    c.weighty = 0;
	    JButton butEinst = new JButton(BUT_EINST_LABEL);
	    butEinst.addActionListener(desktop);
	    add(butEinst, c);
	    
	    leer = new JPanel();
	    c.weighty = 1;
	    add(leer,c);
	    
	    /*
	     * Erzeugen der zweiten Spalte als leer Spalte
	     */
	    
	    leer = new JPanel();
	    c.weightx = 1;
	    c.weighty = 1;
	    c.gridx = 1;
	    c.gridy = 0;
	    c.gridheight = GridBagConstraints.REMAINDER;
	    add(leer,c);
	    
	    /*
	     * Erzeugen der drittem Spalte mit den Einstellungen
	     */
	    
	    JPanel einstellungen = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.gridy = GridBagConstraints.RELATIVE;
	    gbc.gridx = 0;
	    
	    einstellungen.add(createLabel("Pfad zum Photoalbum:"), gbc);
	    einstellungen.add(createLabel("Pfad zum Bilderrahmen:"), gbc);
	    einstellungen.add(createLabel("Minimale Quellbildgröße:"), gbc);
	    einstellungen.add(createLabel("Zielgröße:"), gbc);
	    einstellungen.add(createLabel("Textgröße:"), gbc);

	    gbc.gridx = 1;
	    
	    jLPhotoAlbumPath = new JLabel();
	    jLTargetPath  = new JLabel();
	    jLMinWidthHeight  = new JLabel();
	    jLMaxScaleWidthHeight  = new JLabel();
	    jLTextSize  = new JLabel();
	    fillSpecLabels();
	    
	    einstellungen.add(jLPhotoAlbumPath, gbc);
	    einstellungen.add(jLTargetPath, gbc);
	    einstellungen.add(jLMinWidthHeight, gbc);
	    einstellungen.add(jLMaxScaleWidthHeight, gbc);
	    einstellungen.add(jLTextSize, gbc);
	    
	    c.gridx = 2;
	    c.weightx = 0;
	    add(einstellungen,c);
	}
	
	private JLabel createLabel(String string) {
		
		JLabel jlabel = new JLabel (string);
		jlabel.setBorder(new EmptyBorder(0, 10, 0, 10));
		
		return jlabel;
	}
	
	public void fillSpecLabels() {
		
	    jLPhotoAlbumPath.setText(Specs.photoAlbumPath);
	    jLTargetPath.setText(Specs.targetPath);
	    jLMinWidthHeight.setText(Specs.minWidth + "x" + Specs.minHeight);
	    jLMaxScaleWidthHeight.setText(Specs.maxScaleWidth + "x" + Specs.maxScaleHeight);
	    jLTextSize.setText(Integer.toString(Specs.textSize));
	}
}
