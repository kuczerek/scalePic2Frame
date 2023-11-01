package kuczerek.scalePic2Frame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import javax.imageio.ImageIO;


import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegCommentDirectory;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;

public class Picture {
	
	/**
	 * Führt die gesamte Manipulation der Bilder durch. 
	 */
	
	private File sourceFile;
	private BufferedImage targetBi;
	private BufferedImage origBi;
	private Metadata metadata;
	private ExifIFD0Directory exifIFD0Directory;
	private ExifSubIFDDirectory exifSubIFDDirectory;
	private JpegCommentDirectory jpegCommentDirectory;
	
	private int currentDateSource;
	private int currentCommentSource;
	private String comment;
	private String skipInfo;
	private String exceptionMessage;
	private static String lastSourcePath ;
	private static String lastCommentFromProperty;
	private static LocalDate lastLocalDateFromProperty;
	
	public static final int DATESOURCE_EXIF = 1;
	public static final int DATESOURCE_DIRECTORY = 2;
	public static final int DATESOURCE_FILE = 3;
	public static final int DATESOURCE_PROPERTY = 4;
	public static final int COMMENTSOURCE_EXIF = 1;
	public static final int COMMENTSOURCE_DIRECTORYONLY = 2;
	public static final int COMMENTSOURCE_PROPERTYONLY = 3;
	public static final int COMMENTSOURCE_DIRECTORY_JPEG= 4;
	public static final int COMMENTSOURCE_PROPERTY_JPEG = 5;
	public static final int COMMENTSOURCE_JPEG = 6;
	

	public Picture(File sourceFile) {

		currentDateSource = 0;
		currentCommentSource = 0;
		comment = new String();
		skipInfo = new String();
		exceptionMessage = new String();
		
		this.sourceFile = sourceFile;
		
		BufferedImage origBi = null;
		try {
			origBi = ImageIO.read(this.sourceFile);
		} catch (IOException e1) {
			exceptionMessage = e1.getMessage();
			e1.printStackTrace();
		}
		this.origBi = origBi;
		
		Metadata metadata = null;
		try {
			metadata = ImageMetadataReader.readMetadata(this.sourceFile);
			this.exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			this.exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			this.jpegCommentDirectory = metadata.getFirstDirectoryOfType(JpegCommentDirectory.class);
		} catch (ImageProcessingException e) {
			exceptionMessage = e.getMessage();
			e.printStackTrace();
		} catch (IOException e) {
			exceptionMessage = e.getMessage();
			e.printStackTrace();
		}
		this.metadata = metadata;
	}

	public boolean shouldWeProcessPicture () {
		
		if (this.origBi ==  null) {
			//Bild konnte nicht gelesen werden
			skipInfo = "Fehler: " + exceptionMessage;
			return false;
		} else if (this.metadata ==  null) {
			//Metadaten konnten nicht gelesen werden
			skipInfo = "Fehler: " + exceptionMessage;
			return false;
		} else if (this.origBi.getHeight() <= Specs.minHeight) {
			//Bild ist zu klein
			skipInfo = "Übersprungen, das Bild ist zu klein.";
			return false;
		} else if (this.origBi.getWidth() <= Specs.minWidth) {
			//Bild ist zu schmal
			skipInfo = "Übersprungen, das Bild ist zu schmal.";
			return false;
		} else if ((float) this.origBi.getWidth() / this.origBi.getWidth() >= 2.0) {
			//Ist wohl ein Panorama, machen wir trotzdem
			skipInfo = "Übersprungen, das Bild ist ein Panorama.";
			return true;
		} else {
			return true;
		}
			
	}
	
	/**
	 * 
	 */
	public void scalePicture() {
		
		int origHeight = this.origBi.getHeight();
		int origWidth = this.origBi.getWidth();
		float origRatio = (float) origWidth / origHeight;
		float targetRatio = (float) Specs.maxScaleWidth / Specs.maxScaleHeight;
		int orientation = 1;
		
		//Lesen der Orientierung
		try {
			orientation = this.exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
		} catch (Exception e) {
			//e.printStackTrace();
			orientation = 1;
		}
		
		/*
		 * Skalieren des Zielbilds
		 */
		
		Image scaledImage = null;
		if (origRatio < targetRatio && orientation <= 4) {
			
			/* Das Bild hat ein kleineres Seitenverhältnis als das Ziel, ist aber Landscape
			 * Die maximale Ausdehnung wird also von der Breite angegeben 
			 * Überstehende Höhe wird später abgeschnitten 
			 */
			scaledImage = this.origBi.getScaledInstance(Specs.maxScaleWidth, -1, Image.SCALE_SMOOTH);
		} else if (origRatio > targetRatio && orientation <= 4) {
			
			/* Das Bild hat ein größeres Seitenverhältnis als das Ziel und ist Landscape
			 * Die maximale Ausdehnung wird also von der Höhe angegeben
			 * Überstehende Breite wird später abgeschnitten
			 */
			scaledImage = this.origBi.getScaledInstance(-1, Specs.maxScaleHeight, Image.SCALE_SMOOTH);
		} else if (origRatio == targetRatio && orientation <= 4) {
			
			/*
			 * Das Bild hat ein gleiches Seitenverhältnis als das Ziel und ist Landscape
			 */
			scaledImage = this.origBi.getScaledInstance(Specs.maxScaleWidth, Specs.maxScaleHeight, Image.SCALE_SMOOTH);
		} else if (orientation > 4) {
			
			/*
			 * Das Bild ist Portrait
			 * Die maximale Ausdehnung wird also von der Höhe angegeben
			 */
			scaledImage = this.origBi.getScaledInstance(Specs.maxScaleHeight, -1, Image.SCALE_SMOOTH);
		}
		BufferedImage scaledBi = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
	    Graphics2D bGr = scaledBi.createGraphics();
	    bGr.drawImage(scaledImage, 0, 0, null);
	    bGr.dispose();
		this.targetBi = scaledBi;
		
		/*
		 * Croppen des Zielbilds
		 */
	    
	    BufferedImage cropedBi;	    
	    if (origRatio < targetRatio && orientation <= 4) {
			
			/* Das Bild hat ein kleineres Seitenverhältnis als das Ziel, ist aber Landscape
			 * Die maximale Ausdehnung wurde also von der Breite angegeben 
			 * Überstehende Höhe wird nun vermittelt und abgeschnitten 
			 */
	    	
	    	int y = (this.targetBi.getHeight() - Specs.maxScaleHeight) / 2;
	    	cropedBi = this.targetBi.getSubimage(0, y, Specs.maxScaleWidth, Specs.maxScaleHeight);
	    	this.targetBi = cropedBi;
		} else if (origRatio > targetRatio && orientation <= 4 && origRatio < 2.5) {
			
			/* Das Bild hat ein größeres Seitenverhältnis als das Ziel und ist Landscape
			 * Die maximale Ausdehnung wurde also von der Höhe angegeben
			 * Überstehende Breite wird nun vermittelt und abgeschnitten
			 * --> Aber nur, wenn es ein Bild mit einem Verhältnis kleiner 2,5 ist (kein Panorama)
			 */
			 
			int x = (this.targetBi.getWidth() - Specs.maxScaleWidth) / 2;
			cropedBi = this.targetBi.getSubimage(x, 0, Specs.maxScaleWidth, Specs.maxScaleHeight);
			this.targetBi = cropedBi;
		}
	    
	}

	public void orientPicture() {
		
		int width = this.targetBi.getWidth();
		int height = this.targetBi.getHeight();
		int orientation = 1;
		
		//Lesen der Orientierung
		try {
			orientation = this.exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
		} catch (Exception e) {
			//e.printStackTrace();
			orientation = 1;
		}
		
		/*
		 * The 8 EXIF orientation values are numbered 1 to 8.
		 * 1 = 0 degrees: the correct orientation, no adjustment is required.
		 * 2 = 0 degrees, mirrored: image has been flipped back-to-front.
		 * 3 = 180 degrees: image is upside down.
		 * 4 = 180 degrees, mirrored: image has been flipped back-to-front and is upside down.
		 * 5 = 90 degrees: image has been flipped back-to-front and is on its side.
		 * 6 = 90 degrees, mirrored: image is on its side.
		 * 7 = 270 degrees: image has been flipped back-to-front and is on its far side.
		 * 8 = 270 degrees, mirrored: image is on its far side.
		 */
	
		//Modell für die Transformation in Abhängigkeit von der Orientierung erstellen
		AffineTransform at = new AffineTransform();
	    switch (orientation) {
	    case 1:
	        break;
	    case 2: // Flip X
	        at.scale(-1.0, 1.0);
	        at.translate(-width, 0);
	        break;
	    case 3: // PI rotation 
	        at.translate(width, height);
	        at.rotate(Math.PI);
	        break;
	    case 4: // Flip Y
	        at.scale(1.0, -1.0);
	        at.translate(0, -height);
	        break;
	    case 5: // - PI/2 and Flip X
	        at.rotate(-Math.PI / 2);
	        at.scale(-1.0, 1.0);
	        break;
	    case 6: // -PI/2 and -width
	        at.translate(height, 0);
	        at.rotate(Math.PI / 2);
	        break;
	    case 7: // PI/2 and Flip
	        at.scale(-1.0, 1.0);
	        at.translate(-height, 0);
	        at.translate(0, width);
	        at.rotate(  3 * Math.PI / 2);
	        break;
	    case 8: // PI / 2
	        at.translate(0, width);
	        at.rotate(  3 * Math.PI / 2);
	        break;
	    }
	    
	    AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
	    BufferedImage rotatedImage = op.createCompatibleDestImage(this.targetBi, null);
	    this.targetBi = op.filter(this.targetBi, rotatedImage);
	}
	
	public void drawOnPicture() {
		
		Graphics2D g2d = targetBi.createGraphics();
		
		//grauen, halbtransparenten Streifen erstellen
		g2d.setColor(new Color(255, 255, 255, 136));
		g2d.fillRect(0, this.targetBi.getHeight() - Specs.textSize - 10 , this.targetBi.getWidth(), Specs.textSize + 2);
		
		//Schrift erzeugen
		Font font = null;
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = loader.getResourceAsStream("FreeSansBold.ttf")) {
            font = Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (FontFormatException | IOException ex) {
        	ex.printStackTrace();
        }
		
		Hashtable<TextAttribute, Object> attributes = new Hashtable<TextAttribute, Object>();
		attributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
		attributes.put(TextAttribute.SIZE, Specs.textSize);
	    font = font.deriveFont(attributes);
	    g2d.setFont(font);
	    g2d.setColor(new Color(0, 0, 0));
	    
	    
	    //Schriftausmaße ermitteln
	    FontMetrics fm = g2d.getFontMetrics();
	    
	    //Kommentar String erzeugen
	    comment = createCommentString(fm);

	    //Kommentar auf das Bild schreiben
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawString(comment, (this.targetBi.getWidth() - fm.stringWidth(comment)) / 2, this.targetBi.getHeight() - 10 - Specs.textSize / 10);
		
		g2d.dispose();
	}
	
	private String createCommentString(FontMetrics fm) {
		
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
		
		String comment;
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

		/*
		 * Wir holen erstmal ein paar Daten aus dem Dateisystem,
		 * falls wir später keine EXIF Daten finden
		 */
		
		
		/*
		 * comment aus dem übergeordneten Pfad extrahieren
		 */
		Path path = Paths.get(this.sourceFile.getPath());
		//Übergeordneten Ordner extrahieren
		commentFromPath = path.getName(path.getNameCount() - 2).toString();
		if (commentFromPath.matches("^[\\d\\-\\s]{10,11}(.*)")) {
			
			/*
			 * Datum entfernen für den Kommentar
			 */
			commentFromPath = commentFromPath.replaceAll("^[\\d\\-\\s]{10,11}(.*)", "$1");
			
			/*
			 * Datum zwischenspeichern, falls wir das noch für die Beschriftung brauchen
			 */
			String dateString = path.getName(path.getNameCount() - 2).toString();
			
			
			
			dateString = dateString.replaceAll("^([\\d\\-\\s]{10,11}).*", "$1");
			localDateDirString = createDatefromString(dateString);
			
		}
		commentFromPath = commentFromPath.trim();
		
		/*
		 * Wir checken, ob es eine property Datei im zu verarbeitenden Verzeichnis gibt,
		 * welche die Verzeichnisdaten überschreibt
		 * 
		 * Dabei checken wir nicht jedesmal auf eine Property Datei, um Zugriffszeit zu sparen. Wenn
		 * der Dateipfad gegenüber dem vorherigen durchgang gleich geblieben ist, holen wir uns die 
		 * Daten aus statischen Variablen
		 */
		
		//System.out.println("Pic: " + path.toString() + " | Current: " + path.getParent().toString() + " | Last: " + lastSourcePath);
		if (path.getParent().toString().equals(lastSourcePath)) {
			commentFromProperty = lastCommentFromProperty;
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
				
				//OK, wir haben eine Property Datei und lesen den zu überschreibenen Verzeichnisnamen aus
				commentFromProperty = props.getProperty("VerzeichnisNameUeberschreiben", "");
				if (commentFromProperty.equals("")) {
					commentFromProperty = null;
				} else {
					commentFromProperty = commentFromProperty.trim();
					
					//Speichern der Daten in der static Variable für den nächsten Durchlauf
					lastCommentFromProperty = commentFromProperty;
				}
				
				//und das überschreibende Verzechnisdatum
				String dateStringFromProperty = props.getProperty("VerzeichnisDatumUeberschreiben", "");
				if (!dateStringFromProperty.equals("")) {
					dateStringFromProperty = dateStringFromProperty.trim();
					localDateFromProperty = createDatefromString(dateStringFromProperty);
					
					//Speichern der Daten in der static Variable für den nächsten Durchlauf
					lastLocalDateFromProperty = localDateFromProperty;
				}					
			} else {
				//Keine Property Datei vorhanden, also alle Daten aus den Properties nullen
				commentFromProperty = null;
				localDateFromProperty = null;
				lastCommentFromProperty = null;
				lastLocalDateFromProperty = null;				
			}
		}
		lastSourcePath = path.getParent().toString();
		
		/*
		 * Datum aus dem cDate der Bilddatei extrahieren
		 */
		BasicFileAttributes attr = null;
	   	try {
			attr = Files.readAttributes(path, BasicFileAttributes.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    localDateFileCreation = new Date(attr.creationTime().toMillis()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		
		/*
		 * Jetzt besorgen wir EXIF Daten, wenn welche vorliegen
		 * Es gibt tausend Möglichkeiten für Kommentare und Datümer. Wir holen mal ein paar.
		 */
	    
	    //EXIF Datum aus exifSubIFDDirectory
	    if (this.exifSubIFDDirectory != null) {
			Date dateExifSubIFD = this.exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
			if (dateExifSubIFD != null) {
				localDateExifSubIFD = dateExifSubIFD.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}
		}
	    
	    //Kommentar aus exifSubIFDDirectory
	    if (this.exifSubIFDDirectory != null) {
			commentExifSubIFD = this.exifSubIFDDirectory.getDescription(ExifSubIFDDirectory.TAG_USER_COMMENT);
			if (commentExifSubIFD != null) {
				commentExifSubIFD = commentExifSubIFD.trim();
			}
		}
	    
	    //Kommentar aus exifIFD0Directory
	    if (this.exifIFD0Directory != null) {
			commentExifIFD0 = this.exifIFD0Directory.getString(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION);
			if (commentExifIFD0 != null) {
				commentExifIFD0 = commentExifIFD0.trim();
			}
		}

	    //Kommentar aus jpegCommentDirectory
	    if (this.jpegCommentDirectory != null) {
			commentJpegComment = this.jpegCommentDirectory.getString(JpegCommentDirectory.TAG_COMMENT, "UTF-8");
			if (commentJpegComment != null) {
				commentJpegComment = commentJpegComment.trim();
			}
		}
	 
		/*
		 * Zusammenbau des vollen Kommentars
		 */    
		
		
	    /*
	     * Wenn Verzeichnisdaten aus einer Property Datei vorhanden sind, dann nehmen wir diese,
	     * statt des echten Verzeichnisses 
	     */
	    
	    String commentVerzData;
	    if (commentFromProperty != null) {
	    	commentVerzData = new String (commentFromProperty);
	    	currentCommentSource = COMMENTSOURCE_PROPERTYONLY;
	    } else {
	    	commentVerzData = new String(commentFromPath);
			currentCommentSource = COMMENTSOURCE_DIRECTORYONLY;
	    }
	    
	    /*
		 * Wir beladen den Kommentar mit den Verzeichnisdaten vor
		 */
	    comment = new String(commentVerzData);
	    
	    /*
	     * Welchen Kommentar wollen wir jetzt für das Bild nehmen?
	     * Wir entscheiden uns für den JPG Kommentar
	     */
	    
	    String commentMetaData = new String();
	    if (commentJpegComment != null && !commentJpegComment.isEmpty() ) {
			/*
			 * Es gibt einen Kommentar in den JPG Daten, den legen wir jetzt für die
			 * Weitervarbeitung in commentMetaData. Den Kommentartyp haben wir auch
			 * im Perl Skript genutzt.
			 */
	    	
	    	switch (currentCommentSource) {
	    	case COMMENTSOURCE_PROPERTYONLY:
	    		currentCommentSource = COMMENTSOURCE_PROPERTY_JPEG;
	    		break;
	    	case COMMENTSOURCE_DIRECTORYONLY:
	    		currentCommentSource = COMMENTSOURCE_DIRECTORY_JPEG;
	    		break;
	    	}
			commentMetaData = commentJpegComment;
		}
		
		
		if (commentMetaData != null && !commentMetaData.isEmpty() ) {
			/*
			 * Es gibt einen Kommentar in den META Daten, den hängen wir an den bestenhenen 
			 * Kommentar aus dem Pfad dran
			 */
			comment = comment + " - " + commentMetaData;
		}
		
		if (localDateExifSubIFD != null) {
			/*
			 * Es gibt ein Datum aus den EXIF Daten
			 */
			currentDateSource = DATESOURCE_EXIF;
			comment = comment + " am " + localDateExifSubIFD.format(formatter);
		} else if ( localDateFromProperty != null ){
			/*
			 * Es gibt offenbar kein Datum in den EXIF Daten, dann nehmen wir das Datum aus der Property Datei
			 */
			currentDateSource = DATESOURCE_PROPERTY;
			comment = comment + " am " + localDateFromProperty.format(formatter);
		} else if ( localDateDirString != null ){
			/*
			 * Es gibt offenbar kein Datum aus einer Property, dann nehmen wir das Datum aus dem Verzeichnisnamen
			 */
			currentDateSource = DATESOURCE_DIRECTORY;
			comment = comment + " am " + localDateDirString.format(formatter);
		} else {
			/*
			 * Es gibt kein Datum aus den EXIF Daten und keines aus dem Verzeichnisnamen, wir nehmen das cDate
			 */
			currentDateSource = DATESOURCE_FILE;
			comment = comment + " am " + localDateFileCreation.format(formatter);
		}
		
		/*
		 * Prüfen, ob der Kommentar auf das Bild passt
		 */
		if (fm.stringWidth(comment) > this.targetBi.getWidth() - 20) {
			
			/*
			 * Scheint nicht so, also lassen wir mal den Pfad weg, wenn es einen META Kommentar gibt
			 * Andernfalls setzen wir nur den Pfad
			 */
			if (commentMetaData != null && !commentMetaData.isEmpty() ) {
				comment = new String(commentMetaData);
			} else {
				comment = new String(commentVerzData);
			}
			if (localDateExifSubIFD != null) {
				comment = comment + " am " + localDateExifSubIFD.format(formatter);
			} else {
				comment = comment + " am " + localDateFileCreation.format(formatter);
			}
		}
			
		/*
		 * Erneut Prüfen, ob der Kommentar auf das Bild passt
		 */
			
		if (fm.stringWidth(comment) > this.targetBi.getWidth() - 20) {
			
			/*
			 * Scheint immer noch nicht so, also lassen wir mal den Pfad weg, wenn es einen META Kommentar gibt
			 * Andernfalls setzen wir nur den Pfad und lassen auch das Datum weg
			 */
			if (commentMetaData != null && !commentMetaData.isEmpty() ) {
				comment = new String(commentMetaData);
			} else {
				comment = new String(commentVerzData);
			}
		}
		
		/*
		 * Erneut Prüfen, ob der Kommentar jetzt auf das Bild passt
		 */
		while(fm.stringWidth(comment) > this.targetBi.getWidth() - 20) {
			
			/*
			 * Wir brechen aus der Schleife aus, wenn es keine Whitespace Character mehr gibt
			 */
			if (!comment.matches(".*\\s.*")) {
				break;
			}
			
			/*
			 * Wir kürzen so lange ganze Wörter weg, bis es passt
			 */
			comment = comment.replaceAll("(.*)\\s.*", "$1");
			comment = comment + "...";
		}
		
		/*
		 * Erneut Prüfen, ob der Kommentar jetzt auf das Bild passt
		 */
		while(fm.stringWidth(comment) > this.targetBi.getWidth() - 20 && comment.length() > 5) {
			
			/*
			 * Jetzt geht es ans Eingemachte, wir nehmen jeden einzelnen Buchstaben 
			 * nacheinander weg
			 */
			comment = comment.substring(0, comment.length()-4);
			comment = comment + "...";
		}
		
		

		return comment;
	}
	
	public BufferedImage getTargetPicture() {
		
		return this.targetBi;
	}
	
	public String getTargetPicturePath() {		
		
		String strPath = sourceFile.toPath().getParent().toString().substring(Specs.photoAlbumPath.length());;
		strPath = strPath.replace("\\", "---");
		File targetPicture = new File(Specs.targetPath + File.separator + strPath + File.separator + sourceFile.getName());
		
		return targetPicture.toPath().toString();
	}
	
	public int getDateSource() {
		return currentDateSource;
	}
	
	public int getCommentSource() {
		return currentCommentSource;
	}
	
	public String getComment() {
		return comment;
	}
	
	public int getSourcePictureWidth() {
		
		if (origBi != null) {
			return origBi.getWidth();
		}
		else {
			return 0;
		}
	}
	
	public int getSourcePictureHeight() {
		
		if (origBi != null) {
			return origBi.getHeight();
		} else {
			return 0;
		}
		
	}
	
	public int getTargetPictureWidth() {
		
		if (targetBi != null) {
			return targetBi.getWidth();
		} else {
			return 0;
		}
	}
	
	public int getTargetPictureHeight() {
		
		if (targetBi != null) {
			return targetBi.getHeight();
		} else {
			return 0;
		}
	}
	
	public String getSkipInfo() {
		return skipInfo;
	}
	
	public void savePicture () {
		
		String strPath = sourceFile.toPath().getParent().toString().substring(Specs.photoAlbumPath.length());;
		strPath = strPath.replace("\\", "---");
		File targetPicture = new File(Specs.targetPath + File.separator + strPath + File.separator + sourceFile.getName());
		targetPicture.mkdirs();
		
        BufferedImage convertedImg = new BufferedImage(this.targetBi.getWidth(), this.targetBi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        convertedImg.getGraphics().drawImage(this.targetBi, 0, 0, null);
        this.targetBi = convertedImg;
        
		try {
			ImageIO.write(this.targetBi, "jpg", targetPicture);
		} catch (IOException e) {
			e.printStackTrace();
		}	
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
}
