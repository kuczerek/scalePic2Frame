package scalePic2Frame;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFormattedTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;

public class ChangeSettings extends JFrame implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1064574073584749978L;

	Desktop desktop;
	
	JFormattedTextField JtfMaxScaleHeight;
	JFormattedTextField JtfMinScaleWidth;
	JFormattedTextField JtfMinHeight;
	JFormattedTextField JtfMinWidth;
	JFormattedTextField JtfTextSize;
	JLabel JtfPhotoAlbumPath;
	JLabel JtfTargetPath;	
	JButton JButPhotoAlbumPath;
	JButton JButTargetPath;
	
	public ChangeSettings(Desktop desktop) {
		this.desktop = desktop;
	}

	public void showWindow() {
		
		setTitle("Einstellungen");
		setSize(650,250);
	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    Container pane = getContentPane();
	    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
	        
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = GridBagConstraints.RELATIVE;
	    gbc.fill = GridBagConstraints.BOTH;
	    //gbc.weightx = 1;
	    
	    /*
	     * JPanel erzeugen und erste Spalte füllen
	     */
	    JPanel settings = new JPanel();
	    settings.setLayout(new GridBagLayout());
	    settings.add(createLabel("Zielskallierung"), gbc);
	    settings.add(createLabel("Mindestgröße der Quellbilder"), gbc);
	    settings.add(createLabel("Schriftgröße"), gbc);
	    settings.add(createLabel("Pfad zum Fotoalbum"), gbc);
	    settings.add(createLabel("Zielpfad für die skalierten Bilder"), gbc);

	    	    
	    /*
	     * Textfelder für die zweite Spalte vorbereiten
	     */
		
		JtfMaxScaleHeight = buildIntegerTextFields();
		JtfMinScaleWidth = buildIntegerTextFields();
		JtfMinHeight = buildIntegerTextFields();
		JtfMinWidth = buildIntegerTextFields();
		JtfTextSize = buildIntegerTextFields();
		JtfPhotoAlbumPath = new JLabel();
		JtfTargetPath = new JLabel();
		
		;
		
	    JtfMaxScaleHeight.setText(Integer.toString(Specs.maxScaleHeight));
	    JtfMinScaleWidth.setText(Integer.toString(Specs.maxScaleWidth));
		JtfMinHeight.setText(Integer.toString(Specs.minHeight));
		JtfMinWidth.setText(Integer.toString(Specs.minWidth));
		JtfTextSize.setText(Integer.toString(Specs.textSize));
		JtfPhotoAlbumPath.setText(Specs.photoAlbumPath);
		JtfTargetPath.setText(Specs.targetPath);
	    		
		/*
		 * Zweite Spalte zusammenbauen
		 */
		
		/*
		 * Erste Zeile in der zweiten Spalte
		 */
		
	    gbc.gridx = 1;
	    gbc.gridy = 0;
	    gbc.weightx = 0;
	    gbc.fill=GridBagConstraints.NONE;
	    settings.add(JtfMaxScaleHeight, gbc);
	    gbc.gridx = GridBagConstraints.RELATIVE;
	    settings.add(new JLabel("x"), gbc);
	    settings.add(JtfMinScaleWidth, gbc);
	    gbc.weightx = 1;
	    gbc.fill=GridBagConstraints.HORIZONTAL;
	    settings.add(new JLabel(), gbc);
	    
	    /*
	     * Zweite Zeile in der zweiten Spalte
	     */
	    gbc.gridx = 1;
	    gbc.gridy = 1;
	    gbc.weightx = 0;
	    gbc.fill=GridBagConstraints.NONE;
	    settings.add(JtfMinHeight, gbc);
	    gbc.gridx = GridBagConstraints.RELATIVE;
	    settings.add(new JLabel("x"), gbc);
	    settings.add(JtfMinWidth, gbc);
	    gbc.weightx = 1;
	    gbc.fill=GridBagConstraints.HORIZONTAL;
	    settings.add(new JLabel(), gbc);
	    
	    /*
	     * Dritte Zeile
	     */
	    gbc.gridx = 1;
	    gbc.gridy = 2;
	    gbc.weightx = 0;
	    gbc.fill=GridBagConstraints.NONE;
	    settings.add(JtfTextSize, gbc);
	    gbc.weightx = 1;
	    gbc.gridwidth = 3;
	    gbc.fill=GridBagConstraints.HORIZONTAL;
	    settings.add(new JLabel(), gbc);
	    
	    /*
	     * Restlichen Zeilen
	     */
	    gbc.gridx = 1;
	    gbc.gridy = GridBagConstraints.RELATIVE;
	    gbc.gridwidth = 4;
	    settings.add(JtfPhotoAlbumPath, gbc);
	    gbc.gridx = 5;
	    gbc.gridy = 3;
	    JButPhotoAlbumPath = new JButton("Ändern");
	    JButPhotoAlbumPath.addActionListener(this);
	    settings.add(JButPhotoAlbumPath, gbc);
	    gbc.gridx = 1;
	    gbc.gridy = GridBagConstraints.RELATIVE;
	    settings.add(JtfTargetPath, gbc);
	    gbc.gridx = 5;
	    gbc.gridy = 4;
	    JButTargetPath = new JButton("Ändern");
	    JButTargetPath.addActionListener(this);
	    settings.add(JButTargetPath, gbc);
	    
	    /*
	     * Fußzeilen
	     */
	    
	    JPanel buttons = new JPanel();
	    JButton button;
	    
	    button = new JButton("OK");
	    button.addActionListener(this);
	    buttons.add(button);
	    button = new JButton("Abbruch");
	    button.addActionListener(this);
	    buttons.add(button);
	    
	    settings.setBorder(new EmptyBorder(10, 10, 10, 10));
	    buttons.setBorder(new EmptyBorder(10, 10, 10, 10));
	    
	    pane.add(new JLabel("Einstellungen"));
	    pane.add(settings);
	    pane.add(buttons);
	    	    		
	    setLocationRelativeTo(null);
	    setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("OK")) {
						
			Specs.maxScaleHeight =  Integer.parseInt(JtfMaxScaleHeight.getText());
			Specs.maxScaleWidth =  Integer.parseInt(JtfMinScaleWidth.getText());
			Specs.minHeight = Integer.parseInt(JtfMinHeight.getText());
			Specs.minWidth = Integer.parseInt(JtfMinWidth.getText());
			Specs.textSize = Integer.parseInt(JtfTextSize.getText());
			Specs.photoAlbumPath = JtfPhotoAlbumPath.getText();
			Specs.targetPath = JtfTargetPath.getText();
			desktop.refillSpecData();
			Specs specs = new Specs();
			specs.writeUserPreferences();
			this.dispose();
		}
				
		if (e.getActionCommand().equals("Abbruch")) {
			this.dispose();			
		}
		
		if (e.getSource() == JButPhotoAlbumPath){
			
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Ordner auswählen");
			//chooser.setCurrentDirectory(dir);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(false);
		    int returnVal = chooser.showOpenDialog(null);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	JtfPhotoAlbumPath.setText(chooser.getSelectedFile().getAbsolutePath());
		    }
		}
		
		if (e.getSource() == JButTargetPath){
			
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Ordner auswählen");
			//chooser.setCurrentDirectory(dir);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(false);
		    int returnVal = chooser.showOpenDialog(null);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	JtfTargetPath.setText(chooser.getSelectedFile().getAbsolutePath());
		    }
		}
		
	}
	
	private JFormattedTextField buildIntegerTextFields() {
		
		JFormattedTextField jtf;
		
	    /*
	     * Formatierung für das JFormattedTextField bauen
	     */
	    NumberFormat format = NumberFormat.getInstance();
	    format.setGroupingUsed(false);
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(0);
	    formatter.setMaximum(Integer.MAX_VALUE);
	    formatter.setAllowsInvalid(false);
	    formatter.setCommitsOnValidEdit(true);
	    
	    
	    /*
	     * Größe der Textfelder mus festgelegt werden, sonst frisst das GridBagLayout die Felder auf
	     */
	    jtf = new JFormattedTextField(formatter);
	    jtf.setPreferredSize(new Dimension(60, (int)(jtf.getPreferredSize().getHeight())));
	    jtf.setMinimumSize(jtf.getPreferredSize());
	    
	    return jtf;
	}
	
	private JLabel createLabel(String string) {
		
		JLabel jlabel = new JLabel (string);
		jlabel.setBorder(new EmptyBorder(5, 10, 5, 10));
		
		return jlabel;
	}

}
