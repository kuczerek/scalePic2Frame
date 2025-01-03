package kuczerek.scalePic2Frame;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.awt.image.BufferedImage;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.mp4.Mp4Directory;
import com.drew.metadata.mp4.media.Mp4VideoDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;



public class Video {
	
	private File sourceFile;
	private BufferedImage targetBi;
	private Metadata metadata;
	private Mp4Directory mp4Directory;
	private Mp4VideoDirectory mp4VideoDirectory;
	private QuickTimeDirectory quickTimeDirectory;
	private QuickTimeVideoDirectory quickTimeVideoDirectory;
	private FFmpegProbeResult ffmpegProbeResult;
	private FFmpegFormat ffmpegFormat;
	private FFmpegStream ffmpegVideoStream;
	
	private int currentDateSource;
	private int currentCommentSource;
	private String comment;
	private String ffmpegComment;
	private String ffmpegScale;
	private String skipInfo;
	private String exceptionMessage;
	
	public Video(File sourceFile) {

		currentDateSource = 0;
		currentCommentSource = 0;
		comment = new String();
		ffmpegComment = new String();
		skipInfo = new String();
		exceptionMessage = new String();
		
		this.sourceFile = sourceFile;
		
		/* 
		 * Wir setzten überwiegend auf ffprobe, statt den MetaData Extraktor
		 * ffprobe hat aber zum Beispiel Probleme mit "orientation"
		 */
		
		
		Metadata metadata = null;
		try {
			if (this.sourceFile.toString().toLowerCase().endsWith("mp4") || this.sourceFile.toString().toLowerCase().endsWith("mov")) {
				metadata = ImageMetadataReader.readMetadata(this.sourceFile);
				this.mp4Directory = metadata.getFirstDirectoryOfType(Mp4Directory.class);
				this.mp4VideoDirectory = metadata.getFirstDirectoryOfType(Mp4VideoDirectory.class);	
				this.quickTimeDirectory = metadata.getFirstDirectoryOfType(QuickTimeDirectory.class);	
				this.quickTimeVideoDirectory = metadata.getFirstDirectoryOfType(QuickTimeVideoDirectory.class);	
			}
			/*if (this.sourceFile.toString().toLowerCase().endsWith("mov")) {
				metadata = ImageMetadataReader.readMetadata(this.sourceFile);
				this.quickTimeDirectory = metadata.getFirstDirectoryOfType(QuickTimeDirectory.class);	
				this.quickTimeVideoDirectory = metadata.getFirstDirectoryOfType(QuickTimeVideoDirectory.class);	
			}*/
		} catch (ImageProcessingException e) {
			System.out.println("Exception, " + this.sourceFile.toString());
			exceptionMessage = e.getMessage();
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Exception, " + this.sourceFile.toString());
			exceptionMessage = e.getMessage();
			e.printStackTrace();
		}
		this.metadata = metadata;
		
		this.ffmpegProbeResult = null;
		try {
			FFprobe ffprobe = new FFprobe(Specs.ffmpegPath + File.separator + "ffprobe");
			this.ffmpegProbeResult = ffprobe.probe(sourceFile.toPath().toString());
			this.ffmpegFormat = this.ffmpegProbeResult.getFormat();
			this.ffmpegVideoStream = this.ffmpegProbeResult.getStreams().get(0);
		} catch (IOException e) {
			exceptionMessage = e.getMessage();
			e.printStackTrace();
		}
	}
	
	public boolean shouldWeProcessVideo () {
		
		if (this.ffmpegProbeResult == null) {
			//Metadaten konnten nicht gelesen werden
			skipInfo = "Fehler: " + exceptionMessage;
			return false;
		} /*else if ( this.ffmpegProbeResult == null && this.metadata == null) {
			//Metadaten konnten nicht gelesen werden
			skipInfo = "Fehler: " + exceptionMessage;
			return false;
		}*/
			
		return true;
	}

	public void generateScaleInformations() {
		
		int height = getRawSourceVideoHeightFromAllFrameworks();
		int width = getRawSourceVideoWidthFromAllFrameworks();
		int orientation = getOrientation();
						
		/*
		 * Um die Orientietation brauchen wir uns nicht kümment. ffmpeg dreht das
		 * Video automatisch in der Transkodierung, wenn die Rotation Daten in der
		 * Aufnahme richtig gesetzt wurden.
		 */
		
		
		/*		scale=1920:1080 										- Resize the input to 1920x1080 (setsar is used for fixing the aspect ratio).
		 *		crop=1584:896:172:92 									- Crops the part inside the black frame.
		 *		split[crp0][crp1] 										- Split the cropped output to two identical streams (two identical images).
		 *		[crp0]scale=1920:1080,setsar=1:1,gblur=sigma=30[blur] 	- Resize the cropped image to 1920x1080 and blur the resized image.
		 *																  Store the blurred image in the temporary variable [blur].
		 *		[blur][crp1]overlay= ... 								- Overlay [crp1] over the blurred image.
		 *
		 * Der Filter kann vieles davon alleine ausrechnen, wir rechnen die Daten aber lieber selbst. So kann man 
		 * die Daten auch debuggen.
		 * Hier der selbst gerechnete Filter als Beispiel, inklusive des Bildkommentars: "C:\Program Files\ffmpeg\bin\ffmpeg" -i 20230802_110927.mp4 -y -filter_complex "scale=w=-2:h=1200, pad=w=1920:h=1200:x=1920/2-iw/2:y=1200/2-iw/2:color=black, crop=750:1200:623:0,split[crp0][crp1];[crp0]scale=1920:1200,setsar=1:1,gblur=sigma=30[blur];[blur][crp1]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2, drawbox=x=0:y=ih-50-10:w=iw:h=50+2:t=fill:color=white@0.53, drawtext=text='Bildertest - Ährengemeinge am 05.08.2022':fontfile=C\\:/Windows/Fonts/FreeSansBold.ttf:fontsize=50:y_align=baseline:fontcolor=black:x=w/2-tw/2:y=h-10-th/10" -an "W:\Familie\Fotorahmen_neu\Sonstiges---scalePhotosTest---Videotest\output_fromBatch_Portrait.mp4"
		 */
		
		/*
		 * Das Video muss skaliert werden, damit es auf den Zielscreen passt.
		 */
		
		int scaleWidth = 0;
		int scaleHeight = 0;
		
		if (isPortrait()) {
			scaleHeight = Specs.maxScaleHeight;
			//Ist die orientation auf 90 oder -90 sind width und height vertauscht
			if (orientation == -90 || orientation == 90) {
				scaleWidth = (int) (((double) Specs.maxScaleHeight / (double) width) * (double) height);
			} else {
				scaleWidth = (int) (((double) Specs.maxScaleHeight / (double) height) * (double) width);
			}
		} 
		else if (isLandscape()) {
			scaleWidth = Specs.maxScaleWidth;
			//Ist die orientation auf 90 oder -90 sind width und height vertauscht
			if (orientation == -90 || orientation == 90) {
				scaleHeight = (int) (((double) Specs.maxScaleWidth / (double) height) * (double) width);
			} else {
				scaleHeight = (int) (((double) Specs.maxScaleWidth / (double) width) * (double) height);
			}
		} else {
			// Das Video ist quadratisch, wir behandeln es mit dem Portrait Code
			scaleHeight = Specs.maxScaleHeight;
			//Ist die orientation auf 90 oder -90 sind width und height vertauscht
			if (orientation == -90 || orientation == 90) {
				scaleWidth = (int) (((double) Specs.maxScaleHeight / (double) width) * (double) height);
			} else {
				scaleWidth = (int) (((double) Specs.maxScaleHeight / (double) height) * (double) width);
			}
		}
				
		this.ffmpegScale = "scale=w=" + scaleWidth + ":h=" + scaleHeight + ", ";
		
		/*
		 * Das runterskalierte Video muss mit grünen Balken aufgefüllt werden, um den Bidlschirm zu füllen
		 */
		
		int padWidth = 0;
		int padHeight = 0;
		int padX = 0;
		int padY = 0;
		
		padWidth = Specs.maxScaleWidth;
		padHeight = Specs.maxScaleHeight;
		padX = (int) ((double) Specs.maxScaleWidth / 2.0 - (double) scaleWidth / 2.0 );
		padY = (int) ((double) Specs.maxScaleHeight / 2.0 - (double) scaleHeight / 2.0 );
		
		/*
		 * Wir lassen den Verarbeitungsschritt weg, das Padding brauch wir nicht, weil später ein 
		 * hochskaliertes, geblurtes Bild für den Hintergrund erzeugen
		 */
		//this.ffmpegScale = this.ffmpegScale + "pad=w=" + padWidth + ":h=" + padHeight +	":x=" + padX + ":y=" + padY + ":color=green,";
				
		/*
		 * Der grüne Teile wird wieder weggeschnitten...
		 */
		
		int cropWidth = scaleWidth;
		int cropHeight = scaleHeight;
		int cropX = (Specs.maxScaleWidth - scaleWidth) / 2; 
		int cropY = (Specs.maxScaleHeight - scaleHeight) / 2;
						
		this.ffmpegScale = this.ffmpegScale + "crop=" + cropWidth + ":" + cropHeight + ":" + cropX + ":" + cropY +",";
		
		/*
		 * Aufsplitten des Kanals in die zwei Kanäle [crp0] und {crp1]
		 */
		
		this.ffmpegScale = this.ffmpegScale + "split[crp0][crp1];";
		
		/*
		 * Kanal {crp0] auf maximale Bildgröße aufdehnen, bluren und in der Variable [blur] speichern
		 */
		
		this.ffmpegScale = this.ffmpegScale + "[crp0]scale=" + Specs.maxScaleWidth + ":" + Specs.maxScaleHeight	+ ",setsar=1:1,gblur=sigma=30[blur];";
		
		/*
		 * [crp0} mittig über [blur] legen
		 */
		
		int overlayX = padX;
		int overlayY = padY;		
		
		/*
		 * Der fertige String sollte in etwas so aussehen:
		 * scale=w=1920:h=1080, crop=1920:1080:0:60,split[crp0][crp1];[crp0]scale=1920:1200,setsar=1:1,gblur=sigma=30[blur];[blur][crp1]overlay=0:60
		 */
		
		this.ffmpegScale = this.ffmpegScale + "[blur][crp1]overlay=" + overlayX + ":" + overlayY;		
	}

	public void createComment() {
		
		try {
			this.targetBi = new BufferedImage(getRawSourceVideoWidthFromAllFrameworks(), getRawSourceVideoHeightFromAllFrameworks(), BufferedImage.TYPE_4BYTE_ABGR);
		} catch (Exception e) {
			System.out.println("Exception, " + this.sourceFile.toString());
			e.printStackTrace();
		}
		Graphics2D g2d = targetBi.createGraphics();
			
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
	        
	    //Kommentar String erzeugen
	    CommentCreator cc = new CommentCreator(this.sourceFile);
	    MediaComment mc = cc.createVidComment(this.targetBi, g2d, mp4Directory, quickTimeDirectory);
	    this.comment = mc.getComment();
	    
	    /*
	     * Der fertige String sollte in etwas so aussehen:
	     * drawbox=x=0:y=ih-50-10:w=iw:h=50+2:t=fill:color=white@0.53, drawtext=text='Videotest am 29.12.2023':fontfile=C\\:/Windows/Fonts/FreeSansBold.ttf:fontsize=50:y_align=baseline:fontcolor=black:x=w/2-tw/2:y=h-10-th/10
	     */
	    
	    this.ffmpegComment = "drawbox=x=0:y=ih-" + Specs.textSize + "-10:w=iw:h=" + Specs.textSize + "+2:t=fill:color=white@0.53, "
	      + "drawtext=text='" + mc.getComment() + "':"
	      + "fontfile=C\\:/Windows/Fonts/FreeSansBold.ttf:fontsize=" + Specs.textSize + ":"
   		  + "y_align=baseline:fontcolor=black:x=w/2-tw/2:y=h-10-th/10";
	    
		this.currentDateSource = mc.getDateSource();
		this.currentCommentSource = mc.getCommentSource();
	}
	
	public void saveVideo() {
		
		//https://github.com/bramp/ffmpeg-cli-wrapper
		//https://mvnrepository.com/artifact/net.bramp.ffmpeg/ffmpeg
		//sowie: https://jar-download.com/artifacts/org.apache.commons/commons-lang3
		
		String strPath = sourceFile.toPath().getParent().toString().substring(Specs.photoAlbumPath.length());;
		strPath = strPath.replace("\\", "---");
		File targetVideo = new File(Specs.targetPath + File.separator + strPath + File.separator + sourceFile.getName());
		File targetPath = new File(Specs.targetPath + File.separator + strPath);
		targetPath.mkdirs();
		
		
		FFmpeg ffmpeg = null;
		FFprobe ffprobe = null;
		
		try {
			ffmpeg = new FFmpeg(Specs.ffmpegPath + File.separator + "ffmpeg");
			ffprobe = new FFprobe(Specs.ffmpegPath + File.separator + "ffprobe");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Beispiele: https://github.com/bramp/ffmpeg-cli-wrapper/blob/master/src/test/java/net/bramp/ffmpeg/ExamplesTest.java
		FFmpegBuilder builder = new FFmpegBuilder()
				.setInput(sourceFile.toPath().toString())
				.setComplexFilter(this.ffmpegScale +","+ this.ffmpegComment)
				.overrideOutputFiles(true) // Override the output if it exists
				.addOutput(targetVideo.getPath())   // Filename for the destination
				.disableAudio()
				.done();
		
		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

		// Run a one-pass encode without progress Information
		executor.createJob(builder).run();
		
		// Run a one-pass encode with progress Information
		/*FFmpegJob job = executor.createJob(builder, new ProgressListener() {
			
			@Override
			public void progress(Progress progress) {
				// Print out interesting information about the progress
				System.out.println("Frame: " + progress.frame);
			}
		});
		System.out.println("Sourcefile: " + sourceFile.toPath().toString());
		job.run();*/
	}
	
	public String getTargetVideoPath() {		
		
		String strPath = sourceFile.toPath().getParent().toString().substring(Specs.photoAlbumPath.length());;
		strPath = strPath.replace("\\", "---");
		File targetPicture = new File(Specs.targetPath + File.separator + strPath + File.separator + sourceFile.getName());
		
		return targetPicture.toPath().toString();
	}
	
	public int getTargetVideoWidth() {
		
		return Specs.maxScaleWidth;
	}
	
	public int getTargetVideoHeight() {
		
		return Specs.maxScaleHeight;
	}
	
	public int getSourceVideoWidth() {
		
		if (getOrientation() == 90 || getOrientation() == -90){
			return getRawSourceVideoHeightFromAllFrameworks();
		} else {
			return getRawSourceVideoWidthFromAllFrameworks();
		}
	}
	
	public int getSourceVideoHeight() {
		
		if (getOrientation() == 90 || getOrientation() == -90){
			return getRawSourceVideoWidthFromAllFrameworks();
		} else {
			return getRawSourceVideoHeightFromAllFrameworks();
		}
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
	
	public String getSkipInfo() {
		return skipInfo;
	}
	
	public BufferedImage getWorkingPicture() {
		
		return createInfoPicture("Videobearbeitung...");		
	}
	
	public BufferedImage getFinishedPicture() {
		
		return createInfoPicture("Videobearbeitung abgeschlossen.");		
	}
	
	private BufferedImage createInfoPicture(String infotext) {
	
		BufferedImage bi = new BufferedImage(800, 600, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = bi.createGraphics();
			
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
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawString(infotext, (bi.getWidth() - fm.stringWidth(infotext)) / 2, bi.getHeight() / 2);
		g2d.dispose();
		
		return bi;
	}
	
	private boolean isPortrait() {

		/*
		 * Sind wir nun in einem Hochkant Format, oder nicht?
		 * Wenn Orientation auf -90 oder 90 gesetzt ist, dann stehen in breite und höhe des Videos ganz normal die Format 
		 * DAten aus zum Beispiel 16 (Breite) : 9 (Höhe), beim Abspielen wird dies dann automatisch zu 9:16. 
		 * 
		 * ABER: Es gibt Videos die bereits hochkant entwickelt sind: Dann hat die Breite eine geringere Zahl als die Höhe, 
		 * aber Orientation ist 0. Man muss also beide Fälle checken... 
		 */
		
		int height = getRawSourceVideoHeightFromAllFrameworks();
		int width = getRawSourceVideoWidthFromAllFrameworks();
		int orientation = getOrientation();
		double calculatedOrientation = (double) height / (double) width;
		
		if ((orientation == -90 || orientation == 90) && calculatedOrientation < 1 ||
			(orientation == 0 || orientation == 180) && calculatedOrientation > 1) {	
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isLandscape() {
		
		/*
		 * Sind wir nun in einem Hochkant Format, oder nicht?
		 * Wenn Orientation auf -90 oder 90 gesetzt ist, dann stehen in breite und höhe des Videos ganz normal die Format 
		 * DAten aus zum Beispiel 16 (Breite) : 9 (Höhe), beim Abspielen wird dies dann automatisch zu 9:16. 
		 * 
		 * ABER: Es gibt Videos die bereits hochkant entwickelt sind: Dann hat die Breite eine geringere Zahl als die Höhe, 
		 * aber Orientation ist 0. Man muss also beide Fälle checken... 
		 */
		
		int height = getRawSourceVideoHeightFromAllFrameworks();
		int width = getRawSourceVideoWidthFromAllFrameworks();
		int orientation = getOrientation();
		double calculatedOrientation = (double) height / (double) width;
			
		if ((orientation == 0 || orientation == -180) && calculatedOrientation < 1 ||
		    (orientation == -90 || orientation == 90) && calculatedOrientation > 1) {
			return true;
		} else {
			return false;
		}
	}
	
	private int getRawSourceVideoHeightFromAllFrameworks() {
		
		int ffmpegHeight;
		int mp4VideoDirectoryHeight;
		int qtVideoDirectoryHeight;
		
		if (this.ffmpegVideoStream != null) {
			ffmpegHeight = this.ffmpegVideoStream.height;
		} else {
			ffmpegHeight = 0;
		}
		
		try {
			if (this.mp4VideoDirectory != null) {
				mp4VideoDirectoryHeight = this.mp4VideoDirectory.getInt(Mp4VideoDirectory.TAG_HEIGHT);
			} else {
				mp4VideoDirectoryHeight = 0;
			}
		} catch (MetadataException e1) {
			mp4VideoDirectoryHeight = 0;
			e1.printStackTrace();
		}
		
		try {
			if (this.quickTimeVideoDirectory != null) {
				qtVideoDirectoryHeight = this.quickTimeVideoDirectory.getInt(QuickTimeVideoDirectory.TAG_HEIGHT);
			} else {
				qtVideoDirectoryHeight = 0;
			}
		} catch (MetadataException e1) {
			qtVideoDirectoryHeight = 0;
			e1.printStackTrace();
		}
		
		//Manchmal liefern nicht alle Frameworks die gleichen Daten. Bei den Videos aus der App Lightcut liefert ffmpeg keine 
		//Information über Höhe und Breite. Sollte ffmpeg nichts liefern, greifen wir auf MP4VideoDirectory zurück
		
		if (ffmpegHeight > 0) {
			return ffmpegHeight;
		} else if (this.mp4Directory != null ) {
			return mp4VideoDirectoryHeight;
		} else if (this.quickTimeDirectory != null) {
			return qtVideoDirectoryHeight;
		} else {
			return 0;
		}
	}
	
	private int getRawSourceVideoWidthFromAllFrameworks() {
		
		int ffmpegWidth;
		int mp4VideoDirectoryWidth;
		int qtVideoDirectoryWidth;
		
		if (this.ffmpegVideoStream != null) {
			ffmpegWidth = this.ffmpegVideoStream.width;
		} else {
			ffmpegWidth = 0;
		}
		
		try {
			if (this.mp4VideoDirectory != null) {
				mp4VideoDirectoryWidth = this.mp4VideoDirectory.getInt(Mp4VideoDirectory.TAG_WIDTH);
			} else {
				mp4VideoDirectoryWidth = 0;
			}
		} catch (MetadataException e1) {
			mp4VideoDirectoryWidth = 0;
			e1.printStackTrace();
		}
		
		try {
			if (this.quickTimeVideoDirectory != null) {
				qtVideoDirectoryWidth = this.quickTimeVideoDirectory.getInt(QuickTimeVideoDirectory.TAG_WIDTH);
			} else {
				qtVideoDirectoryWidth = 0;
			}
		} catch (MetadataException e1) {
			qtVideoDirectoryWidth = 0;
			e1.printStackTrace();
		}
		
		//Manchmal liefern nicht alle Frameworks die gleichen Daten. Bei den Videos aus der App Lightcut liefert ffmpeg keine 
		//Information über Höhe und Breite. Sollte ffmpeg nichts liefern, greifen wir auf MP4VideoDirectory zurück
		
		
		if (ffmpegWidth > 0) {
			return ffmpegWidth;
		} else if (this.mp4Directory != null ) {
			return mp4VideoDirectoryWidth;
		} else if (this.quickTimeDirectory != null) {
			return qtVideoDirectoryWidth;
		} else {
			return 0;
		}
	}
	
	private int getOrientation() {
		
		int orientation = 0;
				
		if (this.mp4Directory != null) {
			try {
				orientation = this.mp4Directory.getInt(Mp4Directory.TAG_ROTATION);
			} catch (MetadataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (this.quickTimeDirectory != null) {
			try {
				orientation = this.quickTimeDirectory.getInt(QuickTimeDirectory.TAG_ROTATION);
			} catch (MetadataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return orientation;
	}
}
