package ereaderdelegate;

import java.util.ArrayList;

import ereader.EReader;
//import ereader.Note;
import ereader.Note;

/** 
 * The class <code>EReaderDelegateEx27</code> defines an object that
 * provides a starting point for a solution to Exercise 27. 
 */
public class EReaderDelegateEx27 extends EReaderDelegate {
				
	/** 
	 * Creates an <code>EReaderDelegateEx27</code> for the given 
	 * <code>EReader</code>. 
	 */
	public EReaderDelegateEx27(EReader eReader) {
		super(eReader);
	}
	
	/** 
	 * Called by the eReader to inform this delegate that the document has been loaded. 
	 */
	public void documentLoaded(String filename) {
		System.out.println("Document is loading..."+filename);
		
	}
	
	/**
	 * Called by the eReader to inform this delegate that the page with the
	 * given page number has been displayed.
	 */
	public void pageDisplayed(int pageNumber) {
		System.out.println("page number..."+pageNumber);
		System.out.println("total no.of pages : "+eReader.getPageCount());
		Note[] notesList1;
		notesList1=eReader.getNotes();
		int cnt=0;
		
		for(int i=0;i<notesList1.length;i++)
		{
			//System.out.println("notes pgno: "+notesList1[i].getPageNumber());
			
			if(notesList1[i].getPageNumber()==pageNumber)
			{
				cnt++;			
			}
			
		}
		System.out.println("notes: "+eReader.getNotes().length);//+notesList.size());
	   
		System.out.println("no.of notes in "+pageNumber+ " page : "+cnt);
	}
	
	
}
