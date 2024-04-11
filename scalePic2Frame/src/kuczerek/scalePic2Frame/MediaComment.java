package kuczerek.scalePic2Frame;

public class MediaComment {
	
	public static final int DATESOURCE_EXIF = 1;
	public static final int DATESOURCE_DIRECTORY = 2;
	public static final int DATESOURCE_FILE = 3;
	public static final int DATESOURCE_PROPERTY = 4;
	public static final int DATESOURCE_MP4 = 5;
	public static final int DATESOURCE_QT = 6;
	public static final int COMMENTSOURCE_EXIF = 1;
	public static final int COMMENTSOURCE_DIRECTORYONLY = 2;
	public static final int COMMENTSOURCE_PROPERTYONLY = 3;
	public static final int COMMENTSOURCE_DIRECTORY_JPEG= 4;
	public static final int COMMENTSOURCE_PROPERTY_JPEG = 5;
	public static final int COMMENTSOURCE_JPEG = 6;
	
	private int dateSource;
	private int commentSource;
	private String comment;
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public void addCommentBehindCurrent(String comment) {
		this.comment = this.comment + comment;
	}
	public void addCommentBeforeCurrent(String comment) {
		this.comment = comment + this.comment;
	}
	public int getDateSource() {
		return dateSource;
	}
	public void setDateSource(int dateSource) {
		this.dateSource = dateSource;
	}
	public int getCommentSource() {
		return commentSource;
	}
	public void setCommentSource(int commentSource) {
		this.commentSource = commentSource;
	}

}
