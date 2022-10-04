package kuczerek.scalePic2Frame;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class Desktop extends JFrame implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5482877999870934340L;
	private ControlComponent controlComp;
	private LogComponent logComp;
	private JPanel previewComp;
	private Dimension bottomDimension;
	Process proc;
	
	public Desktop(Process proc)  {
		
		this.proc = proc;
		controlComp = new ControlComponent(this);
		logComp = new LogComponent();
		previewComp = new JPanel();	
		
		controlComp.setBorder(new EmptyBorder(10, 10, 10, 10));
		logComp.setBorder(new EmptyBorder(10, 10, 10, 10));
		previewComp.setBorder(new EmptyBorder(10, 10, 10, 10));
	}
	
	public void showWindow() {
		
		setTitle("scalePicFrame");
		setSize(1100,900);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    Container pane = getContentPane();
	    pane.setLayout(new GridBagLayout());
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = GridBagConstraints.RELATIVE;
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.weightx = 1;
	    
	    gbc.weighty = 0;
	    controlComp.setMinimumSize(new Dimension(300, 100));
		controlComp.setMaximumSize(new Dimension(900, 100));
		controlComp.setPreferredSize(new Dimension(900, 100));
	    pane.add(controlComp, gbc);
	    
	    gbc.weighty = 0.5;
		pane.add(logComp, gbc);
		
		previewComp.setLayout( new java.awt.BorderLayout() );
		JLabel jLabel = new JLabel("Vorschau", SwingConstants.CENTER);
		previewComp.add(jLabel, java.awt.BorderLayout.CENTER);
		previewComp.setMinimumSize(new Dimension(300, 320));
		previewComp.setMaximumSize(new Dimension(900, 320));
		previewComp.setPreferredSize(new Dimension(900, 320));
		gbc.weighty = 0;
	    pane.add(previewComp, gbc);
		
	    setLocationRelativeTo(null);
	    setVisible(true);
        bottomDimension = previewComp.getPreferredSize();
	}
	
	public void startLogEntry(String log, int current, int total) {
		logComp.startEntry(log, current, total);
		validate();
		repaint();
	}
	
	public void picLoadedLogEntry(int width, int height) {
		logComp.picLoadedEntry(width, height);
		validate();
		repaint();
	}
	
	public void endLogEntry(String log, int width, int height, int dateSource, int commentSource, String comment) {
		logComp.endEntry(log, width, height, dateSource, commentSource, comment);
	}
	
	public void newPreviewPicture(BufferedImage img) {
		
		Image scaledImage = img.getScaledInstance(-1, 300, Image.SCALE_SMOOTH);
		previewComp.removeAll();
		JLabel jLabel = new JLabel(new ImageIcon(scaledImage));
		previewComp.add(jLabel, java.awt.BorderLayout.CENTER);
		previewComp.setPreferredSize(bottomDimension);
		previewComp.validate();
		previewComp.repaint();
	}
	
	public void refillSpecData() {
		controlComp.fillSpecLabels();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals(ControlComponent.BUT_VERZ_LABEL)) {
			ArrayList<Path> allFiles;
			
			DirectoryChooser dc = new DirectoryChooser();
			allFiles = dc.getChosenDirectories();
			
			if (allFiles != null) {
				proc.processAllFiles(allFiles);
			}
		}
		
		if (e.getActionCommand().equals(ControlComponent.BUT_EINST_LABEL)) {
			
			ChangeSettings cs = new ChangeSettings(this);
			cs.showWindow();
		}
	}
}
