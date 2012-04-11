package ereader;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;

/**
 * The class <code>NotesPanel</code> defines an object that implements the notes panel
 * for an eReader. A notes panel displays the total number of notes a the document,
 * Add and Delete buttons, a table of all notes in a document, and a note pane which
 * displays the entire content of the note selected in the notes table.
 */
public class NotesPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static DateFormat dateFormat = 
			SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	// UI components
	private JToolBar toolbar;
	private JLabel totalNotesLabel;
	private JButton addButton;
	private JButton deleteButton;
	private JButton shareButton;
	private JButton editButton;
	private JSplitPane splitter;
	private JTextArea notePane;
	private NotesTable notesTable;

	// state
	private EReader eReader;
	private Note note;
	private ArrayList<Note> notesList;

	/**
	 * Creates a notes panel for the given eReader.
	 */
	public NotesPanel(EReader eReader) {
		this.eReader = eReader;
		
		setLayout(new BorderLayout(0, 0));
		
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setBorder(BorderFactory.createEmptyBorder(6, 0, 8, 0));
		add(toolbar, BorderLayout.NORTH);
		
		toolbar.add(Box.createHorizontalStrut(2));
		totalNotesLabel = new JLabel("0 Notes");
		toolbar.add(totalNotesLabel);
		
		toolbar.add(Box.createHorizontalStrut(8));
		toolbar.add(Box.createHorizontalGlue());
		
		addButton = new JButton("Add");
		toolbar.add(addButton);
		toolbar.add(Box.createHorizontalStrut(4));
		
		deleteButton = new JButton("Delete");
		toolbar.add(deleteButton);
		toolbar.add(Box.createHorizontalStrut(2));
		addButton.setPreferredSize(deleteButton.getPreferredSize()); // eq width buttons look better here
		//$ share icon
		shareButton = new JButton("Share");
		toolbar.add(shareButton);
		toolbar.add(Box.createHorizontalStrut(6));
	//	deleteButton.setPreferredSize(shareButton.getPreferredSize()); // eq width buttons look better here
		//$ edit icon
		editButton = new JButton("Edit");
		toolbar.add(editButton);
		toolbar.add(Box.createHorizontalStrut(8));
		

		splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.setOneTouchExpandable(true);
		splitter.setDividerLocation(600);
		add(splitter, BorderLayout.CENTER);
	
		notesTable = new NotesTable();
		splitter.setTopComponent(new JScrollPane(notesTable));
		
		notePane = new JTextArea("", 10, 30);
		notePane.setMargin(new java.awt.Insets(6, 8, 6, 6));
		notePane.setLineWrap(true);
		notePane.setWrapStyleWord(true);
		JScrollPane noteScroller = new JScrollPane(notePane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		splitter.setBottomComponent(noteScroller);
		
		// add listeners
		addButton.addActionListener(new AddActionListener());
		deleteButton.addActionListener(new DeleteActionListener());
		//$
		shareButton.addActionListener(new ShareActionListener());
		editButton.addActionListener(new EditActionListener());
		notesTable.addMouseListener(new TableMouseListener());
		notesTable.getSelectionModel().addListSelectionListener(new TableSelectionListener());
		notesTable.getTableHeader().addMouseListener(new TableHeaderMouseListener());
	}
	
	void setNotes(ArrayList<Note> notesList) {
		this.notesList = notesList;
		notesTable.setNotes(notesList);
		//totalNotesLabel.setText(notesList.size() + " Notes");
	}
	
	/** 
	 * Adds the given note to the notes panel. This updates the total notes count, 
	 * updates the table, selects the note in the table, and displays the note in the 
	 * note pane.
	 */
	void addNote(Note note) {
		// note: at this point, the note has already been added to the notes list
		
		// update total notes count (this is now done in the delegate, not here)
		//totalNotesLabel.setText(notesList.size() + " Notes");
		
		setSelectedNote(note);
		notesTable.revalidate();
		notesTable.repaint();
	 }
	
	/**
	 * Sets the total notes count for this notes panel.
	 */
	void setTotalNotesCount(int count) {
		String ntext = (count == 1) ? "Note" : "Notes";
		totalNotesLabel.setText(count + " " + ntext);
	}

	/** 
	 * Updates the given note to the notes panel. This updates the table to show the new note content, 
	 * selects the note in the table, and displays the note in the note pane.
	 */
	void updateNote(Note note) {
		setSelectedNote(note);
		notesTable.revalidate();
		notesTable.repaint();
	 }
	 
	/** Selects the given Note in the notes table. If the given note is null, then the selection is cleared. */
	void setSelectedNote(Note note) {
		if (note == null) {
			notesTable.clearSelection();
			notePane.setText("");
		} else {
			int index = notesList.indexOf(note);
			if (index >= 0) {
				notesTable.setRowSelectionInterval(index, index);
				showNoteInNotePane(note);
			} else {
				System.out.println("NotesPanel.setSelectedNote: note NOT FOUND: " + note);
			}
		}
	}
	
	/** Shows the given Note in the note pane of the NotesPanel. */
	void showNoteInNotePane(Note note) {
		String text = "";
		if (note != null) {		
			text =	"Author: " + note.getAuthor() + "\n" +
					"Date: " + dateFormat.format(note.getDate()) + "\n" +
					"Page: " + note.getPageNumber() + "\n" +
					"Rating: " + note.getRating() + "\n" +
					"\n" + note.getText();
		}
		notePane.setText(text);
	}
	
	/** Returns the selected note in the notes table. */
	Note getSelectedNote() {
		Note selected = null;
		int row = notesTable.getSelectedRow();
		if (row >= 0) {
			selected = notesList.get(row);
		}
		return selected;
	}
	
	/**
	 * Clears the current selection in the notes table and clears the note pane
	 * content.
	 */
	void clearSelection() {
		notesTable.clearSelection();
		notesTable.revalidate();
		notePane.setText("");
		repaint();
	}
		
	/* field accessors */
	
	/* Returns the add button. */
	JButton getAddButton() {
		return addButton;
	}
	
	/* Returns the delete button. */
	JButton getDeleteButton() {
		return deleteButton;
	}
	
	/* listeners */
	
	/** Calls the eReader when the Add button is clicked. */
	private class AddActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			eReader.notesPanelAddClicked();
		}
	}
	
	/** 
	 * When the Delete button is clicked, confirms the delete request with the user
	 * and calls the eReader. 
	 */
	private class DeleteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Note note = getSelectedNote();
			if (note == null) {
				System.out.println("NotesPanel.DeleteActionListener.actionPerformed: there is no selected note");
			} else {
				int response = JOptionPane.NO_OPTION;
				
				// if the SHIFT key is ON, do delete without asking, else ask to confirm delete
				if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
					response = JOptionPane.YES_OPTION;
				} else {
					String msg = "Are you sure you want to permanently delete the selected Note?";
					response = eReader.getSwingGUI().showConfirmDialog(msg, "Confirm Delete", 
							JOptionPane.YES_NO_OPTION);
				}
				if (response == JOptionPane.YES_OPTION || response == JOptionPane.OK_OPTION) {
					try {
						eReader.notesPanelDeleteClicked(note);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}
	/**
	 * $ When the Share button is clicked.
	 */
	private class ShareActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Note note = getSelectedNote();
			System.out.println("in share");
			if (note == null) {
				System.out.println("NotesPanel.ShareActionListener.actionPerformed: there is no selected note");
			} else {
				int response = JOptionPane.NO_OPTION;
				eReader.notesPanelShareClicked(note);
				/*// if the SHIFT key is ON, do delete without asking, else ask to confirm delete
				if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
					response = JOptionPane.YES_OPTION;
				} else {
					String msg = "Are you sure you want to permanently delete the selected Note?";
					response = eReader.getSwingGUI().showConfirmDialog(msg, "Confirm Delete", 
							JOptionPane.YES_NO_OPTION);
				}
				if (response == JOptionPane.YES_OPTION || response == JOptionPane.OK_OPTION) {
					eReader.notesPanelDeleteClicked(note);
				}*/
				
			}
		}
	}
/*
 * $ When edit button is clicked
 */
	private class EditActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Note note = getSelectedNote();
			System.out.println("in edit");
			if (note == null) {
				System.out.println("NotesPanel.EditActionListener.actionPerformed: there is no selected note");
			} else {
				int response = JOptionPane.NO_OPTION;
				eReader.notesPanelEditClicked(note);
				/*// if the SHIFT key is ON, do delete without asking, else ask to confirm delete
				if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
					response = JOptionPane.YES_OPTION;
				} else {
					String msg = "Are you sure you want to permanently delete the selected Note?";
					response = eReader.getSwingGUI().showConfirmDialog(msg, "Confirm Delete", 
							JOptionPane.YES_NO_OPTION);
				}
				if (response == JOptionPane.YES_OPTION || response == JOptionPane.OK_OPTION) {
					eReader.notesPanelDeleteClicked(note);
				}*/
				
			}
		}
	}
	
	/** Calls the eReader when the a row is double-clicked in the notes table. */
	private class TableMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				int row = notesTable.getSelectedRow();
				if (row >= 0) {
					eReader.noteDoubleClickedInTable(notesList.get(row));
				}
			}
		}
	}
	
	/** Calls the eReader when a selection is made in the notes table. */
	private class TableSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false) {
				int row = notesTable.getSelectedRow();
				if (row >= 0) {
					eReader.noteSelectedInTable(notesList.get(row));
				}
			}
		}
	}
	
	/** Calls the eReader when a table header is clicked. */
	private class TableHeaderMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			TableColumnModel model = notesTable.getColumnModel();
			int i = model.getColumnIndexAtX(e.getPoint().x);
            int index = model.getColumn(i).getModelIndex();
            String header = notesTable.getHeaders()[index];
            try {
				eReader.sortNotes(header.toLowerCase());
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
