package ereader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class NotesController {
	
	private NotesDAO dao = new NotesDAO();
	
	public boolean addNotes(Note notes) throws SQLException {
		return dao.addNotes(notes);
	}
	
	/*public boolean shareNotes(Notes notes) throws SQLException {
		return dao.shareNotes(notes);
	}*/
	
	public boolean shareNotes(String Noteid, String Groupid) throws SQLException {
		//	List <String> = notes.getGroups();// To do
			// iterate and call the share notes with nid and gpid 
			
			return dao.shareNotes(Noteid,Groupid);
		}
	
	public boolean editNotes(Note notes) throws SQLException {
		return dao.editNotes(notes);
	}
	
	public boolean deleteNotes(String noteid) throws SQLException {
		return dao.deleteNotes(noteid);
	}
	
	public ArrayList viewNotes() throws SQLException {
		return dao.viewNotes();
	}
	

}
