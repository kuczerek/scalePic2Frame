package kuczerek.scalePic2Frame;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
	
	public static void main(String[] args) {
		
		Specs specs = new Specs();
		specs.readUserPreferences();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		Process proc = new Process();
		proc.start();
	}
}
