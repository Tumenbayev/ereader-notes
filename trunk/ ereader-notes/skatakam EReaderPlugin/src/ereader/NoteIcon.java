package ereader;

import java.awt.Color;
import java.awt.Graphics;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.JComponent;
import javax.swing.BorderFactory;

/**
 * The class <code>NoteIcon</code> defines an object that implements an icon for a
 * specific note. Note icons are displayed in the notes bar. 
 */
public class NoteIcon extends JComponent {
	private static final long serialVersionUID = 1L;
	private static DateFormat dateFormat = 
			SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	private static final Color borderColor = Color.ORANGE;
	private static final Color fillColor   = Color.YELLOW;
	
	private Note note;

	/** Creates a note icon for the given note. */
	public NoteIcon(Note note) {
		this.note = note;
		setBorder(BorderFactory.createLineBorder(borderColor));
		setToolTipText("page " + note.getPageNumber() + ": " + note.getAuthor() + 
				", " + dateFormat.format(note.getDate()));
	}
	
	/** Returns the note associated with this note icon. */
	public Note getNote() {
		return note;
	}
	
	/** Returns a description for this note icon. */
	public String toString() {
		return getToolTipText();
	}

	/** Paints this note icon. */
	protected void paintComponent(Graphics g) {
		g.setColor(fillColor);
		g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
	}
}