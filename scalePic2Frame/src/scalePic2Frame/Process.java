package scalePic2Frame;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ListIterator;


public class Process {
	
	private Desktop desktop;
	
	public Process() {
	}
	
	public void start() {
		
		desktop = new Desktop(this);
		desktop.showWindow();		
	}
	
	public void processAllFiles(ArrayList<Path> allFiles) {
		
		/*
		 * Das Verarbeiten der Bilder muss in einen Extra Thread, sonst friert der JFrame ein.
		 */
		Thread scaleFiles = new Thread(new Runnable() {
            @Override
            public void run() {
            	ListIterator<Path> it = allFiles.listIterator();
        	    while(it.hasNext()) {
        	    	int current = it.nextIndex() + 1;
        	    	Path currentPath = it.next();
        	    	//this.desktop.newLogEntry("Datei: " + currentPath.toFile().getName(), current, allFiles.size());
        	    	desktop.startLogEntry("Datei: " + currentPath, current, allFiles.size());
        	    	processPicture(currentPath.toFile());
        	    }
            }
        });         
		scaleFiles.start();
	}
	
	private void processPicture(File picture) {
				
		Picture pic = new Picture(picture);
		if (pic.shouldWeProcessPicture()) {
			pic.scalePicture();
			pic.orientPicture();
			pic.drawOnPicture();
			desktop.newPreviewPicture(pic.getTargetPicture());
			desktop.endLogEntry(pic.getTargetPicturePath(),pic.getDateSource(), pic.getCommentSource(), pic.getComment());
			pic.savePicture();
		} else {
			desktop.endLogEntry(pic.getSkipInfo(),pic.getDateSource(), pic.getCommentSource(), pic.getComment());
		}
		
	}
}
