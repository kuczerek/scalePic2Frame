package kuczerek.scalePic2Frame;

import java.util.prefs.Preferences;

public class Specs {
	
	public static int maxScaleHeight =  1200;
	public static int maxScaleWidth =  1920;
	public static int minHeight = 550;
	public static int minWidth = 550;
	public static int textSize = 50;
	public static String photoAlbumPath = "P:\\2022\\2022-08-10 Bildertest für Schnuckihase";
	public static String targetPath = "C:\\Users\\indy\\tmp";
	public static String ffmpegPath = "C:\\Program Files\\ffmpeg\\bin\\";
	
	private Preferences userPrefs;
	
	public Specs() {
		userPrefs = Preferences.userNodeForPackage(getClass()); 
	}

	public void readUserPreferences() {
		
		maxScaleHeight = userPrefs.getInt("maxScaleHeight", 768);
		maxScaleWidth = userPrefs.getInt("maxScaleWidth", 1024);
		minHeight = userPrefs.getInt("minHeight", 550);
		minWidth = userPrefs.getInt("minWidth", 550);
		textSize = userPrefs.getInt("textSize", 30);
		photoAlbumPath = userPrefs.get("photoAlbumPath", "P:\\2022\\2022-08-10 Bildertest für Schnuckihase");
		targetPath = userPrefs.get("targetPath", "C:\\Users\\indy\\tmp");
		ffmpegPath = userPrefs.get("ffmpegPath", "C:\\Program Files\\ffmpeg\\bin\\");
	}
	
	public void writeUserPreferences() {
		
		userPrefs.putInt("maxScaleHeight", maxScaleHeight);
		userPrefs.putInt("maxScaleWidth", maxScaleWidth);
		userPrefs.putInt("minHeight", minHeight);
		userPrefs.putInt("minWidth", minWidth);
		userPrefs.putInt("textSize", textSize);
		userPrefs.put("photoAlbumPath", photoAlbumPath);
		userPrefs.put("targetPath", targetPath);
		userPrefs.put("ffmpegPath", ffmpegPath);
	}
	
	
}
