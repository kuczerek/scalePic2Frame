package kuczerek.scalePic2Frame;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;

public class LogComponent extends JPanel {
    /**
	 * Die Klasse erzeugt ein JPanel, welches im mittleren Teil des Fensters angezeigt wird. Hier werden alle LogAusgaben der
	 * Verarbeitung dargestellt und erzeugt
	 * 
	 * Wird instanziert von Desktop
	 */
	private static final long serialVersionUID = -7492857679540435978L;
	private DefaultTableModel dtm;
    private JScrollPane scrollPane;
    
    
    public LogComponent() {
	  super(new GridBagLayout());
	  
	  JTable logTable = new JTable();
	  logTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	  dtm = new DefaultTableModel(0, 0);

	  //String header[] = new String[] { "Anzahl", "Quelle", "Größe", "Verhältnis", "Ziel", "Größe", "Verhältnis", "Datumquelle", "Kommentarquelle", "Beschriftung" };
	  //Spalten                            0         1        2       3       4            5               6                  7               8                  9
	  String header[] = new String[] { "Anzahl", "Quelle", "Größe", "Ziel", "Größe", "Datumquelle", "Kommentarquelle", "Beschriftung" };
	  
	  //add header in table model     
	  dtm.setColumnIdentifiers(header);	  
	  
	  //set model into the table object
	  logTable.setModel(dtm);
	  logTable.getColumnModel().getColumn(0).setPreferredWidth(80);
	  logTable.getColumnModel().getColumn(1).setPreferredWidth(520);
	  logTable.getColumnModel().getColumn(2).setPreferredWidth(70);
	  //logTable.getColumnModel().getColumn(3).setPreferredWidth(50);
	  logTable.getColumnModel().getColumn(3).setPreferredWidth(520);
	  logTable.getColumnModel().getColumn(4).setPreferredWidth(70);
	  logTable.getColumnModel().getColumn(5).setPreferredWidth(70);
	  logTable.getColumnModel().getColumn(6).setPreferredWidth(110);
	  logTable.getColumnModel().getColumn(7).setPreferredWidth(400);

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
    }
    
    public void picLoadedEntry(int width, int height) {
    	dtm.setValueAt(width + "x" + height, dtm.getRowCount()-1, 2);
    	//dtm.setValueAt(createRatioString(width, height), dtm.getRowCount()-1, 3);
	    JScrollBar vertical = scrollPane.getVerticalScrollBar();
	    vertical.setValue( vertical.getMaximum() );
    }

	public void endEntry(String targetFile, int width, int height, int dateSource, int commentSource, String comment) {
		
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
	    case Picture.DATESOURCE_PROPERTY:
	    	dateSourceString = "Property";
	        break;
	    default:
	    	dateSourceString = "";
		}
		
		switch (commentSource) {
	    case Picture.COMMENTSOURCE_EXIF:
	    	commentSourceString = "EXIF";
	        break;
	    case Picture.COMMENTSOURCE_JPEG:
	    	commentSourceString = "JPEG";
	        break;
	    case Picture.COMMENTSOURCE_DIRECTORYONLY:
	    	commentSourceString = "Verzeichnis";
	        break;
	    case Picture.COMMENTSOURCE_PROPERTYONLY:
	    	commentSourceString = "Property";
	        break;
	    case Picture.COMMENTSOURCE_DIRECTORY_JPEG:
	    	commentSourceString = "Verzeichnis + JPEG";
	        break;
	    case Picture.COMMENTSOURCE_PROPERTY_JPEG:
	    	commentSourceString = "Property + JPEG";
	        break;
	    default:
	    	commentSourceString = "";
		}
		
		dtm.setValueAt(targetFile, dtm.getRowCount()-1, 3);
		dtm.setValueAt(width + "x" + height, dtm.getRowCount()-1, 4);
		//dtm.setValueAt(createRatioString(width, height), dtm.getRowCount()-1, 6);
		dtm.setValueAt(dateSourceString, dtm.getRowCount()-1, 5);
		dtm.setValueAt(commentSourceString, dtm.getRowCount()-1, 6);
		dtm.setValueAt(comment, dtm.getRowCount()-1, 7);

	}
	
//	private String createRatioString(int width, int height) {
//		
//		String ratioString;
//		
//		//Das Verhältnis wird mit 100 multipliziert und in int umgerechnet
//		int ratio;
//		
//		if (width > height) {
//			ratio = (int)(( (float) width/ (float) height) * 100f);
//		} else {
//			ratio = (int)(( (float) height/ (float) width) * 100f);
//		}
//		
//		switch (ratio) {
//		case 133:
//			ratioString = "4:3";
//			break;
//		case 150:
//			ratioString = "3:2";
//			break;
//		case 177:
//			ratioString = "16:9";
//			break;
//		case 233:
//			ratioString = "21:9";
//			break;
//		default:
//			ratioString = String.valueOf(ratio);
//		}
//		
//		
//		return ratioString;
//	}
}