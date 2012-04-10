package ereaderdelegate;

import ereader.EReader;
import ereader.Note;

/**
 * The <code>EReaderDelegate</code> class defines an object that implements
 * various operations for an <code>EReader</code>. An eReader delegate is called
 * at various times throughout the execution of an eReader Eclipse plugin to
 * perform specific operations.
 */
public class EReaderDelegate {
	EReader eReader;
	
	/**
	 * Returns an instance of an <code>EReaderDelegate</code> to be used by the
	 * given <code>EReader</code>. This technique allows a subclass of an
	 * <code>EReaderDelegate</code> to be returned without modifying code in the
	 * eReader. In Design Patterns, this is known as a factory method.
	 */
	public static EReaderDelegate createEReaderDelegate(EReader eReader) {
		
		/* 
		 * NOTE TO STUDENTS
		 * 
		 * Add or remove comments below to choose the exact type of EReaderDelegate you
		 * want to create and return to the eReader.
		 * You may also add code to return an instance of any other class of your own,
		 * provided it is a subclass of EReaderDelegate.
		 */
		
		/* return a delegate for Exercise 27 */
		//return new EReaderDelegateEx27(eReader);
		
		/* return a delegate for Exercise 27 solution */
		//return new EReaderDelegateEx27Solution(eReader);
		
		/* return a delegate for Project 5 */
		//return new EReaderDelegateProj5(eReader);
		
		/* return a delegate for Project 5 solution */
		//return new EReaderDelegateProj5Solution(eReader);
		
		/* return a delegate for Project 6 */
		return new EReaderDelegateProj6(eReader);
		
		/* return a delegate for Project 6 solution */
		//return new EReaderDelegateProj6Solution(eReader);		
	}
	
	/** 
	 * Creates an <code>EReaderDelegate</code> for the given <code>EReader</code>. 
	 */
	public EReaderDelegate(EReader eReader) {
		this.eReader = eReader;
	}
	
	/** 
	 * Called by the eReader to inform this delegate that the document has been loaded. 
	 */
	public void documentLoaded(String filename) {
	}
	
	/**
	 * Called by the eReader to inform this delegate that the page with the
	 * given page number has been displayed.
	 */
	public void pageDisplayed(int pageNumber) {
	}
	
	/** 
	 * Called by the eReader to request this delegate to add the given note to the
	 * notes collection.
	 */
	public void addNote(Note note) {
	}
	
	/** 
	 * Called by the eReader to request this delegate to delete the given note from the 
	 * notes collection.
	 */
	public void deleteNote(Note note) {
	}
	
	/** 
	 * Called by the eReader to request this delegate to sort the notes collection
	 * by the given key. 
	 */
	public void sortNotes(String key) {
	}
	
}
