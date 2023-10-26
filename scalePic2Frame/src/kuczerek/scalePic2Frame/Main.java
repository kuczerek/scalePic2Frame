package kuczerek.scalePic2Frame;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
	
	/**
	 * Die Klasse mit der Main Methode
	 * - Erzeugt ein Objekt von kuczerek.scalePic2Frame.Specs und füllt es mit den aktuellen hinterlegten Parametern
	 * - Setzt das Look&Feel für das OS
	 * - Instanziert kuczerek.scalePic2Frame.Process, und startet darin die eigentliche Verarbeitung
	 * 
	 *  Main
	 *    + Specs
	 *    + Process
	 *        + Desktop (im ActionListenser des Dircetory Chooser wird auf Process zurückgesprungen und die Bilder verarbeitet)
	 *        |   + ControlComponent
	 *        |   + LogComponent
	 *        |   + ChangeSettings
	 *        |   + DirectoryChooser
	 *        + Picture
	 */
	
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
