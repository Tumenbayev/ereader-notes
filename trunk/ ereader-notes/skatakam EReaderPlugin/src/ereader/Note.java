package ereader;

import java.awt.Rectangle;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The class <code>Note</code> defines an object that represents an eReader
 * note.
 * <p>
 * This class defines fields that are of interest to an eReader delegate, plus
 * fields that are most meaningful to an eReader and its related classes.
 * <p>
 * Each note has an associated selection in the document. The selection is
 * either a text selection or an image selection.
 */
public class Note implements Serializable {
	private static final long serialVersionUID = 1L;
	private static DateFormat dateFormat = 
			SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	
	private String author;
	private Date date;
	private int pageNumber;
	private int rating = 5;
	private String text;//content
	
	private int topOffset;
	private Map<Integer, Rectangle[]> textSelection;
	private int[] imageSelection;
	
	private String noteid;
	private String docid;
	private String uid;
	List<String> groups;
	String status;
	String area;
	
	
	/**
	 * @return the area
	 */
	public String getArea() {
		return area;
	}

	/**
	 * @param area the area to set
	 */
	public void setArea(String area) {
		this.area = area;
	}

	/**
	 * @return the noteid
	 */
	public String getNoteid() {
		return noteid;
	}

	/**
	 * @param noteid the noteid to set
	 */
	public void setNoteid(String noteid) {
		this.noteid = noteid;
	}

	/**
	 * @return the docid
	 */
	public String getDocid() {
		return docid;
	}

	/**
	 * @param docid the docid to set
	 */
	public void setDocid(String docid) {
		this.docid = docid;
	}

	/**
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * @return the groups
	 */
	public List<String> getGroups() {
		return groups;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}


	

	/**
	 * Creates a note with the given author and date.
	 */
	public Note(String author, Date date) {
		this.author = author;
		this.date = date;
	}
	
	/** Returns the author. */
	public String getAuthor() {
		return author;
	}
	
	/** Sets the author for this note. */
	public void setAuthor(String author) {
		this.author = author;
	}
	
	/** Returns the date. */
	public Date getDate() {
		return date;
	}
	
	/** Returns the rating. */
	public int getRating() {
		return rating;
	}
	
	/** Sets the rating for this note. */
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	/** Returns the text. */
	public String getText() {
		return text;
	}
	
	/** Sets the text for this note. */
	public void setText(String text) {
		this.text = text;
	}
	
	/** Returns the page number. */
	public int getPageNumber() {
		return pageNumber;
	}

	/** Sets the page number for this note. */
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	
	/** Sets the top offset for this note. */
	public void setTopOffset(int topOffset) {
		this.topOffset = topOffset;
	}
	
	/**
	 * Returns the top offset, which is the distance in pixels from the top of
	 * the PDF document to the selection for this note.
	 */
	public int getTopOffset() {
		return topOffset;
	}

	/** Returns the text selection. */
	public Map<Integer, Rectangle[]> getTextSelection() {
		return textSelection;
	}
	
	/** Sets the text selection for this note. */
	public void setTextSelection(Map<Integer, Rectangle[]> textSelection) {
		this.textSelection = textSelection;
	}
	
	/** Returns the image selection. */
	public int[] getImageSelection() {
		return imageSelection;
	}
	
	/** Sets the image selection for this note. */
	public void setImageSelection(int[] imageSelection) {
		this.imageSelection = imageSelection;
	}

	/** 
	 * Returns the bounding rectangle around the current selection for this note. 
	 * The rectangle returned is in terms of PDF document coordinates with the
	 * origin in the lower left corner and the y axis increasing from bottom to top.
	 * To get the selection rectangle in Swing coordinates, use 
	 * <code>getswingSelectionRect</code>.
	 */
	public Rectangle getSelectionRect() {
		Rectangle srect = null;
		
		if (textSelection != null) {
			Rectangle[] rects = textSelection.values().iterator().next(); // get first map entry
			if (rects != null && rects.length > 0) {
				srect = new Rectangle(rects[0]);
				for (int i = 1; i < rects.length; i++) {
					srect.add(rects[i]);  // add does union
				}
			} else {
				System.out.println("Note.getSelectionRect: textSelection has INVALID DATA");
			}
		} else if (imageSelection != null) {
			srect = new Rectangle(imageSelection[0], imageSelection[1], imageSelection[2],
					imageSelection[3]);
		} else {
			System.out.println("Note.getSelectionRect: there is NO SELECTION");
		}
		
		return srect;
	}
	
	/** 
	 * Returns the bounding rectangle around the current selection for this note. 
	 * The rectangle returned is in terms of Swing coordinates with the
	 * origin in the upper left corner and the y axis increasing from top to bottom.
	 */
	public Rectangle getSwingSelectionRect() {
		Rectangle srect = getSelectionRect();
		srect.y = topOffset;
		return srect;
	}
	
	/** Returns a description for this note. */
	public String toString() {
		return date + ", author=" + author + ", pgno=" + pageNumber + ", rating=" + 
			   rating + ", topOffset=" + topOffset + ",\n" + text;
	}
	
	/** Returns a one line description for this note. */
	public String toShortString() {
		String s = text.substring(0, (text.length() < 10 ? text.length() : 10));
		return author + ", " + dateFormat.format(date) + ", p=" + pageNumber + 
			   ", r=" + rating + ", '" + s + "'";
	}
}
