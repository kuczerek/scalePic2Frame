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
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

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
	    CommentCreator cc = new CommentCreator(this.sourceFile);
	    MediaComment mc = cc.createPicComment(this.targetBi, g2d, this.exifSubIFDDirectory, this.exifIFD0Directory, this.jpegCommentDirectory);
	    this.comment = mc.getComment();
		this.currentDateSource = mc.getDateSource();
		this.currentCommentSource = mc.getCommentSource();

	    //Kommentar auf das Bild schreiben
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawString(comment, (this.targetBi.getWidth() - fm.stringWidth(comment)) / 2, this.targetBi.getHeight() - 10 - Specs.textSize / 10);
		g2d.dispose();
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
		File targetPath = new File(Specs.targetPath + File.separator + strPath);
		targetPath.mkdirs();
		
        BufferedImage convertedImg = new BufferedImage(this.targetBi.getWidth(), this.targetBi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        convertedImg.getGraphics().drawImage(this.targetBi, 0, 0, null);
        this.targetBi = convertedImg;
        
		try {
			ImageIO.write(this.targetBi, "jpg", targetPicture);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
