package ereaderdelegate;

import java.util.ArrayList;

import ereader.EReader;
import ereader.Note;

/** 
 * The class <code>EReaderDelegateProj5</code> defines an object that
 * provides a starting point for a solution to Project 5.
 */
public class EReaderDelegateProj5 extends EReaderDelegate {

	/** 
	 * Creates an <code>EReaderDelegateProj5Solution</code> for the given 
	 * <code>EReaderDelegate</code>.
	 */
	public EReaderDelegateProj5(EReader eReader) {
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
		System.out.println("page number: "+pageNumber);
		System.out.println("total no.of pages : "+eReader.getPageCount());
		
		
		System.out.println("total no.of notes: "+eReader.getNotes().length);//+notesList.size());
		Note[] notesList1;
		notesList1=eReader.getNotes();
		System.out.println("no.of notes in "+pageNumber+ " page : "+notesCount(notesList1,pageNumber));
	}
	
	
	/** 
	 * Called by the eReader to request this delegate to add the given note to the
	 * notes collection.
	 */
	public void addNote(Note note) {
		
		Note[] notesList1;
		notesList1=eReader.getNotes();
		int length=notesList1.length;
		
		Note[] temp=new Note[notesList1.length+1];
		System.arraycopy(notesList1, 0, temp, 0, notesList1.length);
		
		temp[length]=note;
		notesList1=temp;
		int pageNumber=eReader.getPageNumber();
		
		eReader.setNotes(notesList1);
		eReader.setNotesCountThisPage(notesCount(notesList1 , pageNumber));
		System.out.println("new notes count:"+notesList1.length);
		eReader.setTotalNotesCount(length + 1);
	}
	
	public int notesCount(Note[] notes ,int pageNumber)
	{
		
		int cnt=0;
		Note[] notesList1=notes;
		for(int i=0;i<notesList1.length;i++)
		{
			//System.out.println("notes pgno: "+notesList1[i].getPageNumber());
			
			if(notesList1[i].getPageNumber()==pageNumber)
			{
				cnt++;			
			}
			
		}
		return cnt;
	}
}
