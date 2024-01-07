package kuczerek.scalePic2Frame;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegCommentDirectory;
import com.drew.metadata.mp4.Mp4Directory;

public class CommentCreator {

	private File sourceFile;	
	private static String lastCommentSourcePath;
	private static String lastDateSourcePath;
	private static String lastCommentFromProperty;
	private static LocalDate lastLocalDateFromProperty;
	
	public CommentCreator (File sourceFile) {
		
		this.sourceFile = sourceFile;	
	}
	
	public MediaComment createPicComment(BufferedImage targetBi, Graphics2D g2d, ExifSubIFDDirectory exifSubIFDDirectory, ExifIFD0Directory exifIFD0Directory, JpegCommentDirectory jpegCommentDirectory) {
		
		/*
		 * Der Kommentar auf den Bildern ist aus drei Teilen zusammengesetzt
		 * (1) Verzeichnisname - (2) Beschriftung des Bildes in EXIF Daten - (3) Datum
		 * 
		 * Zu (1): 
		 * Der Verzeichnisname wird aus dem Pfad des Bildes ausgelesen. Dabei wird angenommen,
		 * dass der Pfad dem Muster 'JJJJ-MM-TT Titel des Albums' folgt. Als Verzeichnisname
		 * wird dabei der String 'Titel des Albums' extrahiert
		 * 
		 * Zu (2):
		 * Wird in den Metadaten ein weitere Kommentar als Bildbeschriftung gefunden, wird der
		 * Verzeichnisname um den Medatendatenkommentar ergänzt. Dieser Schritt enfällt, wenn kein
		 * Metadeaten Kommentar vorhanden ist.
		 * 
		 * Zu (3):
		 * Das angehängte Datum wird auf mehreren Wegen ermittelt. Wird in den EXIF Daten ein Datum
		 * gefunden nehmen wir dies, find wir dort keines, nehmen wir das Datum welches in (1) enthalten
		 * ist, finden wir dort auch keines, dann das letzte Ändeurngsdatum der DAtei.
		 */
		
		//Schriftausmaße ermitteln
		FontMetrics fm = g2d.getFontMetrics();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		String commentJpegComment = null;
		String commentExifIFD0 = null;
		String commentExifSubIFD = null;
		String commentFromPath = null;
		String commentFromProperty = null;
		LocalDate localDateExifSubIFD = null;
		LocalDate localDateDirString = null;
		LocalDate localDateFileCreation = null;
		LocalDate localDateFromProperty = null;
			
		//Daten aus dem übergeordneten Pfad extrahieren
		commentFromPath = getCommentFromPath();
		localDateDirString = getDateFromPath();
		
		//Wir checken, ob es eine property Datei im zu verarbeitenden Verzeichnis gibt
		commentFromProperty = getCommentFromProperty();
		localDateFromProperty = getDateFromProperty();
		
		//Datum aus dem cDate der Bilddatei extrahieren
		localDateFileCreation = getDateFromFileCreation();
		
		//Daten aus untereschiedlichen EXIF Daten holen
	    localDateExifSubIFD = getDateFromExifSubIFD(exifSubIFDDirectory);
	    commentExifSubIFD = getCommentFromExifSubIFD(exifSubIFDDirectory);  
	    commentExifIFD0 = getCommentFromExifIFD0(exifIFD0Directory);    
	    commentJpegComment = getCommentFromJpegCommentD(jpegCommentDirectory);
	 
		/*
		 * Zusammenbau des vollen Kommentars
		 */    		
		MediaComment mediaComment = new MediaComment();
		
	    /*
	     * Wir beladen den Kommentar mit den Verzeichnisdaten vor
	     * Wenn Verzeichnisdaten aus einer Property Datei vorhanden sind, dann nehmen wir diese,
	     * statt des echten Verzeichnisses 
	     */
	    
	    String commentVerzData;    //Die Verzeichnisdaten merken wir uns fürs spätere Kürzen extra
	    if (commentFromProperty != null) {
	    	commentVerzData = new String (commentFromProperty);
	    	mediaComment.setComment(new String (commentFromProperty));
	    	mediaComment.setCommentSource(MediaComment.COMMENTSOURCE_PROPERTYONLY);
	    } else {
	    	commentVerzData = new String(commentFromPath);
	    	mediaComment.setComment(new String(commentFromPath));
	    	mediaComment.setCommentSource(MediaComment.COMMENTSOURCE_DIRECTORYONLY);
	    }
	    
	    /*
	     * Welchen Kommentar wollen wir jetzt für das Bild nehmen?
	     * Wir entscheiden uns für den JPG Kommentar
	     */
	    
	    String commentMetaData = new String();  //Die MetaDaten merken wir uns fürs spätere Kürzen extra
	    if (commentJpegComment != null && !commentJpegComment.isEmpty() ) {
			/*
			 * Es gibt einen Kommentar in den JPG Daten, den legen wir jetzt für die
			 * Weitervarbeitung in commentMetaData. Den Kommentartyp haben wir auch
			 * im Perl Skript genutzt.
			 */
	    	
	    	switch (mediaComment.getCommentSource()) {
	    	case MediaComment.COMMENTSOURCE_PROPERTYONLY:
	    		mediaComment.setCommentSource(MediaComment.COMMENTSOURCE_PROPERTY_JPEG);
	    		break;
	    	case MediaComment.COMMENTSOURCE_DIRECTORYONLY:
	    		mediaComment.setCommentSource(MediaComment.COMMENTSOURCE_DIRECTORY_JPEG);
	    		break;
	    	}
			commentMetaData = commentJpegComment;
			mediaComment.addCommentBehindCurrent(" - " + commentJpegComment);
		}
		
	    /*
	     * Datum anhängen
	     */
	    
		if (localDateExifSubIFD != null) {
			/*
			 * Es gibt ein Datum aus den EXIF Daten
			 */
			mediaComment.setDateSource(MediaComment.DATESOURCE_EXIF);
			mediaComment.addCommentBehindCurrent(" am " + localDateExifSubIFD.format(formatter));
			
		} else if ( localDateFromProperty != null ){
			/*
			 * Es gibt offenbar kein Datum in den EXIF Daten, dann nehmen wir das Datum aus der Property Datei
			 */
			mediaComment.setDateSource(MediaComment.DATESOURCE_PROPERTY);
			mediaComment.addCommentBehindCurrent(" am " + localDateFromProperty.format(formatter));
		} else if ( localDateDirString != null ){
			/*
			 * Es gibt offenbar kein Datum aus einer Property, dann nehmen wir das Datum aus dem Verzeichnisnamen
			 */
			mediaComment.setDateSource(MediaComment.DATESOURCE_DIRECTORY);
			mediaComment.addCommentBehindCurrent(" am " + localDateDirString.format(formatter));
		} else {
			/*
			 * Es gibt kein Datum aus den EXIF Daten und keines aus dem Verzeichnisnamen, wir nehmen das cDate
			 */
			mediaComment.setDateSource(MediaComment.DATESOURCE_FILE);
			mediaComment.addCommentBehindCurrent(" am " + localDateFileCreation.format(formatter));
		}
		
		/*
		 * Prüfen, ob der Kommentar auf das Bild passt
		 */
		if (fm.stringWidth(mediaComment.getComment()) > targetBi.getWidth() - 20) {
			
			/*
			 * Scheint nicht so, also lassen wir mal den Pfad weg, wenn es einen META Kommentar gibt
			 * Andernfalls setzen wir nur den Pfad
			 */
			if (commentMetaData != null && !commentMetaData.isEmpty() ) {
				mediaComment.setComment(new String(commentMetaData));
			} else {
				mediaComment.setComment(new String(commentVerzData));
			}
			
			if (localDateExifSubIFD != null) {
				/*
				 * Es gibt ein Datum aus den EXIF Daten
				 */
				mediaComment.setDateSource(MediaComment.DATESOURCE_EXIF);
				mediaComment.addCommentBehindCurrent(" am " + localDateExifSubIFD.format(formatter));
				
			} else if ( localDateFromProperty != null ){
				/*
				 * Es gibt offenbar kein Datum in den EXIF Daten, dann nehmen wir das Datum aus der Property Datei
				 */
				mediaComment.setDateSource(MediaComment.DATESOURCE_PROPERTY);
				mediaComment.addCommentBehindCurrent(" am " + localDateFromProperty.format(formatter));
			} else if ( localDateDirString != null ){
				/*
				 * Es gibt offenbar kein Datum aus einer Property, dann nehmen wir das Datum aus dem Verzeichnisnamen
				 */
				mediaComment.setDateSource(MediaComment.DATESOURCE_DIRECTORY);
				mediaComment.addCommentBehindCurrent(" am " + localDateDirString.format(formatter));
			} else {
				/*
				 * Es gibt kein Datum aus den EXIF Daten und keines aus dem Verzeichnisnamen, wir nehmen das cDate
				 */
				mediaComment.setDateSource(MediaComment.DATESOURCE_FILE);
				mediaComment.addCommentBehindCurrent(" am " + localDateFileCreation.format(formatter));
			}
		}
			
		/*
		 * Erneut Prüfen, ob der Kommentar auf das Bild passt
		 */
			
		if (fm.stringWidth(mediaComment.getComment()) > targetBi.getWidth() - 20) {
			
			/*
			 * Scheint immer noch nicht so, also lassen wir mal den Pfad weg, wenn es einen META Kommentar gibt
			 * Andernfalls setzen wir nur den Pfad und lassen auch das Datum weg
			 */
			if (commentMetaData != null && !commentMetaData.isEmpty() ) {
				mediaComment.setComment(new String(commentMetaData));
			} else {
				mediaComment.setComment(new String(commentVerzData));
			}
		}
		
		/*
		 * Erneut Prüfen, ob der Kommentar jetzt auf das Bild passt
		 */
		while(fm.stringWidth(mediaComment.getComment()) > targetBi.getWidth() - 20) {
			
			/*
			 * Wir brechen aus der Schleife aus, wenn es keine Whitespace Character mehr gibt
			 */
			if (!mediaComment.getComment().matches(".*\\s.*")) {
				break;
			}
			
			/*
			 * Wir kürzen so lange ganze Wörter weg, bis es passt
			 */
			mediaComment.setComment(mediaComment.getComment().replaceAll("(.*)\\s.*", "$1"));
			mediaComment.addCommentBehindCurrent("...");
		}
		
		/*
		 * Erneut Prüfen, ob der Kommentar jetzt auf das Bild passt
		 */
		while(fm.stringWidth(mediaComment.getComment()) > targetBi.getWidth() - 20 && mediaComment.getComment().length() > 5) {
			
			/*
			 * Jetzt geht es ans Eingemachte, wir nehmen jeden einzelnen Buchstaben 
			 * nacheinander weg
			 */
			
			mediaComment.setComment(mediaComment.getComment().substring(0, mediaComment.getComment().length()-4));
			mediaComment.addCommentBehindCurrent("...");
		}
		
		return mediaComment;
	}
	
	public MediaComment createVidComment(BufferedImage targetBi, Graphics2D g2d, Mp4Directory mp4Directory) {
		
		/*
		 * Der Kommentar auf den Bildern ist aus drei Teilen zusammengesetzt
		 * (1) Verzeichnisname - (2) Beschriftung des Bildes in EXIF Daten - (3) Datum
		 * 
		 * Zu (1): 
		 * Der Verzeichnisname wird aus dem Pfad des Bildes ausgelesen. Dabei wird angenommen,
		 * dass der Pfad dem Muster 'JJJJ-MM-TT Titel des Albums' folgt. Als Verzeichnisname
		 * wird dabei der String 'Titel des Albums' extrahiert
		 * 
		 * Zu (2):
		 * Wird in den Metadaten ein weitere Kommentar als Bildbeschriftung gefunden, wird der
		 * Verzeichnisname um den Medatendatenkommentar ergänzt. Dieser Schritt enfällt, wenn kein
		 * Metadeaten Kommentar vorhanden ist.
		 * 
		 * Zu (3):
		 * Das angehängte Datum wird auf mehreren Wegen ermittelt. Wird in den EXIF Daten ein Datum
		 * gefunden nehmen wir dies, find wir dort keines, nehmen wir das Datum welches in (1) enthalten
		 * ist, finden wir dort auch keines, dann das letzte Ändeurngsdatum der DAtei.
		 */
		
		//Schriftausmaße ermitteln
		FontMetrics fm = g2d.getFontMetrics();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		String commentFromPath = null;
		String commentFromProperty = null;
		LocalDate localDateMp4 = null;
		LocalDate localDateDirString = null;
		LocalDate localDateFileCreation = null;
		LocalDate localDateFromProperty = null;
			
		//Daten aus dem übergeordneten Pfad extrahieren
		commentFromPath = getCommentFromPath();
		localDateDirString = getDateFromPath();
		
		//Wir checken, ob es eine property Datei im zu verarbeitenden Verzeichnis gibt
		commentFromProperty = getCommentFromProperty();
		localDateFromProperty = getDateFromProperty();
		
		//Datum aus dem cDate der Bilddatei extrahieren
		localDateFileCreation = getDateFromFileCreation();
		
		//Datum aus dem MP4 Directory lesen
		localDateMp4 = getDateFromMp4Meta(mp4Directory);
		 
		/*
		 * Zusammenbau des vollen Kommentars
		 */    
		MediaComment mediaComment = new MediaComment();
		
	    /*
	     * Wir beladen den Kommentar mit den Verzeichnisdaten vor
	     * Wenn Verzeichnisdaten aus einer Property Datei vorhanden sind, dann nehmen wir diese,
	     * statt des echten Verzeichnisses 
	     */
	    
	    String commentVerzData;    //Die Verzeichnisdaten merken wir uns fürs spätere Kürzen extra
	    if (commentFromProperty != null) {
	    	commentVerzData = new String(commentFromProperty);
	    	mediaComment.setComment(new String (commentFromProperty));
	    	mediaComment.setCommentSource(MediaComment.COMMENTSOURCE_PROPERTYONLY);
	    } else {
	    	commentVerzData = new String(commentFromPath);
	    	mediaComment.setComment(new String(commentFromPath));
	    	mediaComment.setCommentSource(MediaComment.COMMENTSOURCE_DIRECTORYONLY);
	    }
	    
	    /*
	     * Datum anhängen
	     */	

	    if ( localDateMp4 != null ){
			/*
			 * wir nehmen das Datum aus den MP4 Daten
			 */
			mediaComment.setDateSource(MediaComment.DATESOURCE_MP4);
			mediaComment.addCommentBehindCurrent(" am " + localDateMp4.format(formatter));
		} else if ( localDateFromProperty != null ){
			/*
			 * wir nehmen das Datum aus der Property Datei
			 */
			mediaComment.setDateSource(MediaComment.DATESOURCE_PROPERTY);
			mediaComment.addCommentBehindCurrent(" am " + localDateFromProperty.format(formatter));
		} else if ( localDateDirString != null ){
			/*
			 * Es gibt offenbar kein Datum aus einer Property, dann nehmen wir das Datum aus dem Verzeichnisnamen
			 */
			mediaComment.setDateSource(MediaComment.DATESOURCE_DIRECTORY);
			mediaComment.addCommentBehindCurrent(" am " + localDateDirString.format(formatter));
		} else {
			/*
			 * Es gibt kein Datum aus den META Daten und keines aus dem Verzeichnisnamen, wir nehmen das cDate
			 */
			mediaComment.setDateSource(MediaComment.DATESOURCE_FILE);
			mediaComment.addCommentBehindCurrent(" am " + localDateFileCreation.format(formatter));
		}
		
		/*
		 * Prüfen, ob der Kommentar auf das Bild passt
		 */
			
		if (fm.stringWidth(mediaComment.getComment()) > targetBi.getWidth() - 20) {
			
			/*
			 * Scheint immer nicht so, also das Datum weg
			 */
			mediaComment.setComment(new String(commentVerzData));
		}
		
		/*
		 * Erneut Prüfen, ob der Kommentar jetzt auf das Bild passt
		 */
		while(fm.stringWidth(mediaComment.getComment()) > targetBi.getWidth() - 20) {
			
			/*
			 * Wir brechen aus der Schleife aus, wenn es keine Whitespace Character mehr gibt
			 */
			if (!mediaComment.getComment().matches(".*\\s.*")) {
				break;
			}
			
			/*
			 * Wir kürzen so lange ganze Wörter weg, bis es passt
			 */
			mediaComment.setComment(mediaComment.getComment().replaceAll("(.*)\\s.*", "$1"));
			mediaComment.addCommentBehindCurrent("...");
		}
		
		/*
		 * Erneut Prüfen, ob der Kommentar jetzt auf das Bild passt
		 */
		while(fm.stringWidth(mediaComment.getComment()) > targetBi.getWidth() - 20 && mediaComment.getComment().length() > 5) {
			
			/*
			 * Jetzt geht es ans Eingemachte, wir nehmen jeden einzelnen Buchstaben 
			 * nacheinander weg
			 */
			
			mediaComment.setComment(mediaComment.getComment().substring(0, mediaComment.getComment().length()-4));
			mediaComment.addCommentBehindCurrent("...");
		}
		
		return mediaComment;
	}
	
	private String getCommentFromPath() {

		/*
		 * comment aus dem übergeordneten Pfad extrahieren
		 */
		
		String commentFromPath = null;
				
		Path path = Paths.get(this.sourceFile.getPath());
		//Übergeordneten Ordner extrahieren
		commentFromPath = path.getName(path.getNameCount() - 2).toString();
		if (commentFromPath.matches("^[\\d\\-\\s]{10,11}(.*)")) {
			
			/*
			 * Datum entfernen für den Kommentar
			 */
			commentFromPath = commentFromPath.replaceAll("^[\\d\\-\\s]{10,11}(.*)", "$1");
			
		}
		commentFromPath = commentFromPath.trim();
		
		return commentFromPath;
		
	}
	
	private LocalDate getDateFromPath() {
		
		/*
		 * comment aus dem übergeordneten Pfad extrahieren
		 */
		
		String commentFromPath = null;
		LocalDate localDateDirString = null;
		
		Path path = Paths.get(this.sourceFile.getPath());
		//Übergeordneten Ordner extrahieren
		commentFromPath = path.getName(path.getNameCount() - 2).toString();
		if (commentFromPath.matches("^[\\d\\-\\s]{10,11}(.*)")) {
			
			String dateString = path.getName(path.getNameCount() - 2).toString();			
			dateString = dateString.replaceAll("^([\\d\\-\\s]{10,11}).*", "$1");
			localDateDirString = createDatefromString(dateString);
			
		}

		return localDateDirString;		
	}
	
	private String getCommentFromProperty() {
		
		/*
		 * Wir checken, ob es eine property Datei im zu verarbeitenden Verzeichnis gibt,
		 * welche die Verzeichnisdaten überschreibt
		 * 
		 * Dabei checken wir nicht jedesmal auf eine Property Datei, um Zugriffszeit zu sparen. Wenn
		 * der Dateipfad gegenüber dem vorherigen durchgang gleich geblieben ist, holen wir uns die 
		 * Daten aus statischen Variablen
		 */
		
		String commentFromProperty = null;
		Path path = Paths.get(this.sourceFile.getPath());
		
		//System.out.println("Pic: " + path.toString() + " | Current: " + path.getParent().toString() + " | Last: " + lastSourcePath);
		if (path.getParent().toString().equals(lastCommentSourcePath)) {
			commentFromProperty = lastCommentFromProperty;
		}
		else {
			File propertyFile = new File(path.getParent().toString() + File.separator +  "scalePicFrame.properties");
			if (propertyFile.exists()) {
				Properties props = new Properties();
				try {
					props.load(new FileInputStream(propertyFile));
				} catch (Exception e) {
					//do Nothing
				}
				
				//OK, wir haben eine Property Datei und lesen den zu überschreibenen Verzeichnisnamen aus
				commentFromProperty = props.getProperty("VerzeichnisNameUeberschreiben", "");
				if (commentFromProperty.equals("")) {
					commentFromProperty = null;
				} else {
					commentFromProperty = commentFromProperty.trim();
					
					//Speichern der Daten in der static Variable für den nächsten Durchlauf
					lastCommentFromProperty = commentFromProperty;
				}
				
			} else {
				//Keine Property Datei vorhanden, also alle Daten aus den Properties nullen
				commentFromProperty = null;
				lastCommentFromProperty = null;		
			}
		}
		lastCommentSourcePath = path.getParent().toString();
		return commentFromProperty;
	}
	
	private LocalDate getDateFromProperty() {
		
		/*
		 * Wir checken, ob es eine property Datei im zu verarbeitenden Verzeichnis gibt,
		 * welche die Verzeichnisdaten überschreibt
		 * 
		 * Dabei checken wir nicht jedesmal auf eine Property Datei, um Zugriffszeit zu sparen. Wenn
		 * der Dateipfad gegenüber dem vorherigen durchgang gleich geblieben ist, holen wir uns die 
		 * Daten aus statischen Variablen
		 */
		
		LocalDate localDateFromProperty = null;
		Path path = Paths.get(this.sourceFile.getPath());
		
		//System.out.println("Pic: " + path.toString() + " | Current: " + path.getParent().toString() + " | Last: " + lastSourcePath);
		if (path.getParent().toString().equals(lastDateSourcePath)) {
			localDateFromProperty = lastLocalDateFromProperty;
		}
		else {
			File propertyFile = new File(path.getParent().toString() + File.separator +  "scalePicFrame.properties");
			if (propertyFile.exists()) {
				Properties props = new Properties();
				try {
					props.load(new FileInputStream(propertyFile));
				} catch (Exception e) {
					//do Nothing
				}
				
				//OK, wir haben eine Property Datei und lesen das zu überschreibende Verzechnisdatum
				String dateStringFromProperty = props.getProperty("VerzeichnisDatumUeberschreiben", "");
				if (!dateStringFromProperty.equals("")) {
					dateStringFromProperty = dateStringFromProperty.trim();
					localDateFromProperty = createDatefromString(dateStringFromProperty);
					
					//Speichern der Daten in der static Variable für den nächsten Durchlauf
					lastLocalDateFromProperty = localDateFromProperty;
				}					
			} else {
				//Keine Property Datei vorhanden, also alle Daten aus den Properties nullen
				localDateFromProperty = null;
				lastLocalDateFromProperty = null;				
			}
		}
		lastDateSourcePath = path.getParent().toString();
		return localDateFromProperty;
	}
	
	private LocalDate getDateFromFileCreation() {
		
		/*
		 * Datum aus dem cDate der Bilddatei extrahieren
		 */
		LocalDate localDateFileCreation = null;
		Path path = Paths.get(this.sourceFile.getPath());
		
		BasicFileAttributes attr = null;
	   	try {
			attr = Files.readAttributes(path, BasicFileAttributes.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    localDateFileCreation = new Date(attr.creationTime().toMillis()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		
	    return localDateFileCreation;
	}
	
	
	private String getCommentFromExifSubIFD(ExifSubIFDDirectory exifSubIFDDirectory) {
		
		String commentExifSubIFD = null;
		
	    //Kommentar aus exifSubIFDDirectory
	    if (exifSubIFDDirectory != null) {
			commentExifSubIFD = exifSubIFDDirectory.getDescription(ExifSubIFDDirectory.TAG_USER_COMMENT);
			if (commentExifSubIFD != null) {
				commentExifSubIFD = commentExifSubIFD.trim();
			}
		}
	    
	    return commentExifSubIFD;
	}
	
	private LocalDate getDateFromExifSubIFD(ExifSubIFDDirectory exifSubIFDDirectory) {
		
		LocalDate localDateExifSubIFD = null;
		
	    if (exifSubIFDDirectory != null) {
			Date dateExifSubIFD = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
			if (dateExifSubIFD != null) {
				localDateExifSubIFD = dateExifSubIFD.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}
	    }
	    
	    return localDateExifSubIFD;
	}
	
	private String getCommentFromExifIFD0(ExifIFD0Directory exifIFD0Directory) {
		
		String commentExifIFD0 = null;
		
	    //Kommentar aus exifIFD0Directory
	    if (exifIFD0Directory != null) {
			commentExifIFD0 = exifIFD0Directory.getString(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION);
			if (commentExifIFD0 != null) {
				commentExifIFD0 = commentExifIFD0.trim();
			}
		}
	    
	    return commentExifIFD0;
	}
	
	private String getCommentFromJpegCommentD(JpegCommentDirectory jpegCommentDirectory) {
		
		String commentJpegComment = null;
		
	    //Kommentar aus jpegCommentDirectory
	    if (jpegCommentDirectory != null) {
			commentJpegComment = jpegCommentDirectory.getString(JpegCommentDirectory.TAG_COMMENT, "UTF-8");
			if (commentJpegComment != null) {
				commentJpegComment = commentJpegComment.trim();
			}
		}
	    
	    return commentJpegComment;
	}
	
	
	private LocalDate createDatefromString(String dateString) {
		
		String[] dateStringSplitted = dateString.split("-");
		
		int jahr = Integer.parseInt(dateStringSplitted[0].trim());
		int monat = Integer.parseInt(dateStringSplitted[1].trim());
		int tag = Integer.parseInt(dateStringSplitted[2].trim());
		
		/*
		 * Wir machen daraus ein Datum, wenn Tag und Monat nicht 0 sind
		 */
		if (monat > 0 && tag > 0) {
			return LocalDate.of(jahr, monat, tag);
		} else {		
			return null;
		}
	}
	
	private LocalDate getDateFromMp4Meta(Mp4Directory mp4Directory) {
		
		LocalDate localDateMp4 = null;
		
	    if (mp4Directory != null) {
			Date dateMp4 = mp4Directory.getDate(Mp4Directory.TAG_CREATION_TIME );
			if (dateMp4 != null) {
				localDateMp4 = dateMp4.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				
				//Wenn kein Datum gespeichert ist, wird offenbar 01.01.1904 geliefert. Wir machen
				//daraus lieber null.
				if (localDateMp4.isBefore(LocalDate.parse("1905-01-01"))) {
					localDateMp4 = null;
				}
			}
	    }
	    
	    return localDateMp4;
	}
}
