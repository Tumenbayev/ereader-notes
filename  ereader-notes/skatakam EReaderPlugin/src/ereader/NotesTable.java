package ereader;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

/**
 * The class <code>NotesTable</code> defines an object that implements the notes table
 * for an eReader. A notes table is located inside the eReader notes panel.
 * The table displays a row for each note in the document.
 */
public class NotesTable extends JTable {
	private static final long serialVersionUID = 1L;
	
	private String[] headers = { "Author", "Date", "Page", "Rating" };
	
	/** Creates a notes table. */
	public NotesTable() {
		
		// configure table
		setAutoResizeMode(AUTO_RESIZE_NEXT_COLUMN);
		setRowHeight(getRowHeight() + 2);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// JTables can do sorting by column, turn on sorting with the following
		//setAutoCreateRowSorter(true);
	}
	
	/** Sets the given array list of notes as the notes for the table to display. */
	void setNotes(ArrayList<Note> notesList) {
		clearSelection();
		
		setModel(new NotesTableModel(notesList, headers));
		
		// configure column widths
		TableColumnModel tcm = getColumnModel();
		tcm.getColumn(0).setPreferredWidth(110); // author
		tcm.getColumn(1).setPreferredWidth(88);  // date
		tcm.getColumn(2).setPreferredWidth(44);  // page
		tcm.getColumn(3).setPreferredWidth(44);  // rating
	}
	
	/** Returns the table headers. */
	String[] getHeaders() {
		return headers;
	}
}

/** This class implements the table model for a notes table. */
class NotesTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private String[] headers;	
	private ArrayList<Note> notesList;

	NotesTableModel(ArrayList<Note> notesList, String[] headers) {
		this.notesList = notesList;
		this.headers = headers;
	}

	public int getRowCount() {
		return notesList.size();
	}

	public int getColumnCount() {
		return headers.length;
	}

	public String getColumnName(int columnIndex) {
		return headers[columnIndex];
	}

	public Class<?> getColumnClass(int columnIndex) {
		return getValueAt(0, columnIndex).getClass();
	}

	public Object getValueAt(int row, int col) {
		Note note = notesList.get(row);
		switch (col) {
		case 0: return note.getAuthor();
		case 1: return note.getDate();
		case 2: return note.getPageNumber();
		case 3: return note.getRating();
		}
		return null;
	}

	public boolean isCellEditable(int row, int col) {
		return false;  // all cells are read-only
	}
	
}
