package scalePic2Frame;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;

public class LogComponent extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7492857679540435978L;
	private DefaultTableModel dtm;
    private JScrollPane scrollPane;
    
    
    public LogComponent() {
	  super(new GridBagLayout());
	  
	  JTable logTable = new JTable();
	  logTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	  dtm = new DefaultTableModel(0, 0);
	  String header[] = new String[] { "Anzahl", "Quelle", "Ziel", "Datumquelle", "Kommentarquelle", "Beschriftung" };
	  
	  //add header in table model     
	  dtm.setColumnIdentifiers(header);	  
	  
	  //set model into the table object
	  logTable.setModel(dtm);
	  logTable.getColumnModel().getColumn(0).setPreferredWidth(80);
	  logTable.getColumnModel().getColumn(1).setPreferredWidth(550);
	  logTable.getColumnModel().getColumn(2).setPreferredWidth(550);
	  logTable.getColumnModel().getColumn(3).setPreferredWidth(70);
	  logTable.getColumnModel().getColumn(4).setPreferredWidth(70);
	  logTable.getColumnModel().getColumn(5).setPreferredWidth(400);

	  scrollPane = new JScrollPane(logTable);
	  GridBagConstraints c = new GridBagConstraints();
	  c.gridwidth = GridBagConstraints.REMAINDER;
	
	  c.fill = GridBagConstraints.BOTH;
	  c.weightx = 1.0;
	  c.weighty = 1.0;
	  add(scrollPane, c);
	}
	  
    public void startEntry(String entry, int current, int total) {
	    dtm.addRow(new Object[]{ "[" + current + " von " + total + "] ", entry, "", "", "", "" });
	    JScrollBar vertical = scrollPane.getVerticalScrollBar();
	    vertical.setValue( vertical.getMaximum() );
	    System.out.println("done");
    }

	public void endEntry(String targetFile, int dateSource, int commentSource, String comment) {
		
		String dateSourceString;
		String commentSourceString;
		
		switch (dateSource) {
	    case Picture.DATESOURCE_EXIF:
	    	dateSourceString = "EXIF";
	        break;
	    case Picture.DATESOURCE_DIRECTORY:
	    	dateSourceString = "Verzeichnis";
	        break;
	    case Picture.DATESOURCE_FILE:
	    	dateSourceString = "Datei";
	        break;
	    default:
	    	dateSourceString = "";
		}
		
		switch (commentSource) {
	    case Picture.COMMENTSOURCE_EXIF:
	    	commentSourceString = "EXIF";
	        break;
	    case Picture.COMMENTSOURCE_DIRECTORYONLY:
	    	commentSourceString = "Verzeichnis";
	        break;
	    default:
	    	commentSourceString = "";
		}
		
		dtm.setValueAt(targetFile, dtm.getRowCount()-1, 2);
		dtm.setValueAt(dateSourceString, dtm.getRowCount()-1, 3);
		dtm.setValueAt(commentSourceString, dtm.getRowCount()-1, 4);
		dtm.setValueAt(comment, dtm.getRowCount()-1, 5);

	}
}