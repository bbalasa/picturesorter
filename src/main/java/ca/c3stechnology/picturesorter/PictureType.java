package ca.c3stechnology.picturesorter;
//just a comment to have the build triggered
public enum PictureType {
	TIFF("tiff"),
	JPG("jpg"),
	JPEG("jpeg"),
	JPG2("JPG"),
	JPEG2("JPEG"),
	BMP("bmp");
	
	private String fileType;
	private PictureType(String description){
		this.fileType = description;
	}
	
	public String fileType(){ return fileType; }
	
	public static boolean isPictureType(String extension){
		for(PictureType pt : PictureType.values()){
			if(pt.fileType.equalsIgnoreCase(extension))
				return true;
		}
		return false;
	}
	
}
