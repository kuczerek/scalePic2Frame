package kuczerek.scalePic2Frame;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ListIterator;


public class Process {
	
	/**
	 * Hauptklasse, in der der die Verarbeitung der Bildskallierung gesteuert wird. 
	 * - mit start() wird das Hauptfenster repräsentiert durch kuczerek.scalePic2Frame.Desktop aufgebaut.
	 * - mit processAllFiles() wird ein neuer Thread eröffnet, in welchem jedes einzelene Bild aus der übergebenen
	 *   Arrylist verarbeitet wird und gleichzeitig Log-Ausgaben auf den Desktop geschrieben werden.
	 * 
	 */
	
	private Desktop desktop;
	
	public Process() {
	}
	
	public void start() {
		
		desktop = new Desktop(this);
		desktop.showWindow();
		
		//Gleich mit dem Testverzeichnis durchstarten?
		//Path startDir = new File("P:\\Sonstiges\\scalePhotosTest\\Videotest").toPath();
		//desktop.chooseTestData(startDir);
	}
	
	public void processAllFiles(ArrayList<Path> allFiles) {
		
		/*
		 * Das Verarbeiten der Bilder muss in einen Extra Thread, sonst friert der JFrame ein.
		 */
		Thread scaleFiles = new Thread(new Runnable() {
            @Override
            public void run() {
            	
            	Picture pic;
            	Video vid;
            	ListIterator<Path> it = allFiles.listIterator();

            	while(it.hasNext()) {

            		int current = it.nextIndex() + 1;
        	    	Path currentPath = it.next();
        	    	desktop.startLogEntry(currentPath.toString(), current, allFiles.size());
        	    	
        	    	//Verarbeitung der Bilder
        	    	if (currentPath.toString().toLowerCase().endsWith("jpg") || currentPath.toString().toLowerCase().endsWith("jpeg")) {
        	    		
	    		    	pic = new Picture(currentPath.toFile());
	    		    	desktop.mediaLoadedLogEntry(pic.getSourcePictureWidth(), pic.getSourcePictureHeight());
	    				if (pic.shouldWeProcessPicture()) {
	    					pic.scalePicture();
	    					pic.orientPicture();
	    					pic.drawOnPicture();
	    					desktop.newPreviewPicture(pic.getTargetPicture());
	    					desktop.endLogEntry(pic.getTargetPicturePath(), pic.getTargetPictureWidth(), pic.getTargetPictureHeight(), pic.getDateSource(), pic.getCommentSource(), pic.getComment());
	    					pic.savePicture();
	    				} else {
	    					desktop.endLogEntry(pic.getSkipInfo(), pic.getTargetPictureWidth(), pic.getTargetPictureHeight(), pic.getDateSource(), pic.getCommentSource(), pic.getComment());
	    				}
        	    	} else if (currentPath.toString().toLowerCase().endsWith("mp4") || currentPath.toString().toLowerCase().endsWith("mov") || currentPath.toString().toLowerCase().endsWith("avi")) {
        	    		
        	    		vid = new Video(currentPath.toFile());
        	    		desktop.mediaLoadedLogEntry(vid.getSourceVideoWidth(), vid.getSourceVideoHeight());
        	    		if (vid.shouldWeProcessVideo()) {
        	    			vid.createComment();
        	    			vid.generateScaleInformations();
        	    			desktop.endLogEntry(vid.getTargetVideoPath(), vid.getTargetVideoWidth(), vid.getTargetVideoHeight(), vid.getDateSource(), vid.getCommentSource(), vid.getComment());
        	    			desktop.newPreviewPicture(vid.getWorkingPicture());
        	    			vid.saveVideo();
        	    			desktop.newPreviewPicture(vid.getFinishedPicture());
        	    		} else {
        	    			desktop.endLogEntry(vid.getSkipInfo(), vid.getTargetVideoWidth(), vid.getTargetVideoHeight(), vid.getDateSource(), vid.getCommentSource(), vid.getComment());
        	    		}
        	    	}
        	    }
            }
        });         
		scaleFiles.start();
	}
	
}
