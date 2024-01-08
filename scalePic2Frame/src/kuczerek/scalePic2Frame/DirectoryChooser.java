package kuczerek.scalePic2Frame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;

public class DirectoryChooser {
	
	/**
	 * Dialog zur Auswahl von Verzeichnissen. Durchsucht die markierten Verzeichnisse nach Dateien mit der Endung "jpg|jpeg" und
	 * liefert diese in einer ArrayList zurück 
	 */
	
	public ArrayList<Path> getChosenDirectories () {
		    
		ArrayList<Path> allFiles = new ArrayList<Path>();	
		
		File dir = new File(Specs.photoAlbumPath);
		File[] selectedDirectories = null;
		
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Ordner auswählen");
		chooser.setCurrentDirectory(dir);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setMultiSelectionEnabled(true);
	    int returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	selectedDirectories = chooser.getSelectedFiles();
	    	
		    for (File selectedDirectory : selectedDirectories) {
		        
		        Path startDir = selectedDirectory.toPath();
		        String searchPattern = "*.{jpg,jpeg,mp4,mov,avi}";
		        boolean searchInFile = false;
		        List<Path> result;

		        // Geht rekursiv durch alle Unterverzeichnisse
		        // Rekursion wird in der Methode walkFileTree() gemacht
				try {
					result = SearchFileVisitor.searchFor(startDir, searchPattern, searchInFile);
					//System.out.println("Anzahl der Dateien: " + result.size());
			        for(Path path : result)
			        {
			        	//System.out.println("Datei: " + path.toFile().getName());
			        	allFiles.add(path);
			        }
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		    Collections.sort(allFiles);
	    }	    
	    return allFiles;
	}
	
	public ArrayList<Path> getTestDirectory (Path startDir) {
		
		ArrayList<Path> allFiles = new ArrayList<Path>();
        String searchPattern = "*.{jpg,jpeg,mp4,mov,avi}";
        boolean searchInFile = false;
        List<Path> result;

        // Geht rekursiv durch alle Unterverzeichnisse
        // Rekursion wird in der Methode walkFileTree() gemacht
		try {
			result = SearchFileVisitor.searchFor(startDir, searchPattern, searchInFile);
			//System.out.println("Anzahl der Dateien: " + result.size());
	        for(Path path : result)
	        {
	        	//System.out.println("Datei: " + path.toFile().getName());
	        	allFiles.add(path);
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Collections.sort(allFiles);
		
		return allFiles;
		
	}
}
