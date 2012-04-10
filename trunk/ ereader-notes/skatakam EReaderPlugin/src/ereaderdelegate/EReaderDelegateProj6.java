package ereaderdelegate;

import ereader.EReader;
import ereader.Note;
import ereader.NotesTable;

/** 
 * The class <code>EReaderDelegateProj6</code> defines an object that
 * provides a starting point for a solution to Project 6.
 */
public class EReaderDelegateProj6 extends EReaderDelegate {

	/** 
	 * Creates an <code>EReaderDelegateProj6</code> for the given 
	 * <code>EReaderDelegate</code>.
	 */
	public EReaderDelegateProj6(EReader eReader) {
		super(eReader);
	}
	
	/** 
	 * Called by the eReader to inform this delegate that the document has been loaded. 
	 */
	public void documentLoaded(String filename) {
		System.out.println("Document is loading..."+filename);
			Note notes[] = eReader.getNotes();
			eReader.setTotalNotesCount(notes.length);
			eReader.setNotesCountThisPage(notesCount(notes , eReader.getPageNumber()));
	}
	
	/**
	 * Called by the eReader to inform this delegate that the page with the
	 * given page number has been displayed.
	 */
	public void pageDisplayed(int pageNumber) {
		System.out.println("page number: "+pageNumber);
		System.out.println("total no.of pages : "+eReader.getPageCount());		
		System.out.println("total no.of notes: "+eReader.getNotes().length);
		Note[] notesList1;
		notesList1=eReader.getNotes();
		eReader.setNotesCountThisPage(notesCount(notesList1 , eReader.getPageNumber()));
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
			if(notesList1[i].getPageNumber()==pageNumber)
			{
				cnt++;			
			}
			
		}
		return cnt;
	}
	
	/** 
	 * Called by the eReader to request this delegate to delete the given note from the 
	 * notes collection.
	 */
	public void deleteNote(Note note) {
		Note[] notesList1;
		notesList1=eReader.getNotes();
		int length=notesList1.length,cnt=0;
		
		Note[] temp=new Note[notesList1.length-1];
		for(int i=0;i<notesList1.length;i++)
		{
			if(note.equals(notesList1[i]))
			{
				System.out.println("");
			}
			else
			{
				temp[cnt]=notesList1[i];
				cnt++;
			}
			
			if(cnt==length)
				System.out.println("note does'nt exist");
		}
		
		int pageNumber=eReader.getPageNumber();
		
		eReader.setNotes(temp);
		eReader.setNotesCountThisPage(notesCount(temp , pageNumber));
		System.out.println("new notes count:"+(temp.length));
		eReader.setTotalNotesCount(length - 1);
		eReader.setNotesCountThisPage(notesCount(temp , eReader.getPageNumber()));
	}
	
	/** 
	 * Called by the eReader to request this delegate to sort the notes collection
	 * by the given key. 
	 */
	public void sortNotes(String key) 
	{
		Note[] notesList1;
		notesList1=eReader.getNotes();
		Note temp;
		int j;
		if(key.equalsIgnoreCase("author"))
		{
			for(int i=0;i<notesList1.length;i++)
			{
				j=i;
				temp = notesList1[i];
				while(j!=0 && temp.getAuthor().compareTo(notesList1[j-1].getAuthor()) <0 )
				{
					notesList1[j] = notesList1[j-1];
					j--;
				}
			notesList1[j] = temp; 
			}
			eReader.setNotes(notesList1);
		}
		if(key.equalsIgnoreCase("date"))
		{
			for(int i=0;i<notesList1.length;i++)
			{
				j=i;
				temp = notesList1[i];
				while(j!=0 && temp.getDate().compareTo(notesList1[j-1].getDate()) <0 )
				{
					notesList1[j] = notesList1[j-1];
					j--;
				}
			notesList1[j] = temp; 
			}
			eReader.setNotes(notesList1);
		}
		if(key.equalsIgnoreCase("page"))
		{
			for(int i=0;i<notesList1.length;i++)
			{
				j=i;
				temp = notesList1[i];
				while(j!=0 && temp.getPageNumber() < (notesList1[j-1].getPageNumber()) )
				{
					notesList1[j] = notesList1[j-1];
					j--;
				}
			notesList1[j] = temp; 
			}
			eReader.setNotes(notesList1);
		}
		if(key.equalsIgnoreCase("rating"))
		{
			for(int i=0;i<notesList1.length;i++)
			{
				j=i;
				temp = notesList1[i];
				while(j!=0 && temp.getRating()< (notesList1[j-1].getRating()) )
				{
					notesList1[j] = notesList1[j-1];
					j--;
				}
			notesList1[j] = temp; 
			}
			eReader.setNotes(notesList1);
			eReader.setNotesCountThisPage(notesCount(notesList1 , eReader.getPageNumber()));
		}
	}
}
