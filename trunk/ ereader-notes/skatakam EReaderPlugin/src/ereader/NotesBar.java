package ereader;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * The class <code>NotesBar</code> defines an object that implements the notes
 * bar for an eReader. A notes bar is a vertical bar that display note icons
 * to indicate the the presence and relative location of notes in a document.
 * The look of a notes bar is similar to the vertical bars used in Eclipse to
 * indicate the presence and location of compiler errors and warnings.
 */
public class NotesBar extends JPanel implements ComponentListener, MouseListener {
	private static final long serialVersionUID = 1L;
	
	private static final int DEFAULT_WIDTH = 24;
	private static int ICON_HEIGHT = 5;
	
	private EReader eReader;
	private int pageCount = 0;     // total pages in current document
	private int pageHeight = 792;  // height, in pixels, of page 0 of current document
	private int lastHeight = 0;    // last height of this NotesBar from componentResized event
	
	/** Creates a notes bar for the given eReader. */
	public NotesBar(EReader eReader) {
		this.eReader = eReader;
		setBorder(BorderFactory.createLineBorder(java.awt.SystemColor.controlShadow));
		setLayout(null);
		setWidthPreferences(DEFAULT_WIDTH);
		addComponentListener(this);
	}
	
	/**
	 * Sets the preferred width of this notes bar by setting the given width as
	 * the width in this notes bar minimum size, preferred size, and maximum
	 * size.
	 */
	void setWidthPreferences(int width) {
		Dimension sz = getMinimumSize();
		sz.width = width;
		setMinimumSize(sz);
		
		sz.height = getPreferredSize().height;
		setPreferredSize(sz);
		
		sz.height = getMaximumSize().height;
		setMaximumSize(sz);
	}

	/**
	 * Sets the given page count and page height as document data that is
	 * uses in calculating the position of note icons. 
	 */
	void setDocumentData(int pageCount, int pageHeight) {
		this.pageHeight = pageHeight;
		this.pageCount = pageCount;
	}
	
	// TODO: initIcons, initIcon, positionIcons, positionIcon need to be 
	//       refactored to eliminate duplicate code
	
	/**  
	 * Initializes this notes bar by creating and adding a new note icon for 
	 * each note in the given array list.
	 */
	void initIcons(ArrayList<Note> notesList) {
		//System.out.println("NotesBar.initIcons: total notes = " + notesList.size());

		// remove all previous icons
		removeAll();

		// create an icon for each note and add it to this container
		int w = getWidth() - 4;
		for (Note n: notesList) {
			NoteIcon icon = new NoteIcon(n);
			icon.setBounds(2, 1, w, ICON_HEIGHT);
			add(icon);
			icon.addMouseListener(this);
		}
		
		positionIcons();
	}

	/** Adds a note icon for the given note to this notes bar. */
	void addIcon(Note note) {
		int w = getWidth() - 4;
		NoteIcon icon = new NoteIcon(note);
		icon.setBounds(2, 1, w, ICON_HEIGHT);
		add(icon);
		icon.addMouseListener(this);
		positionIcon(icon);
	}
	
	/**$ Adds a Share icon for the given note to this notes bar. */
	void shareIcon(Note note)
	{
		
	}
	
	/** Deletes the icon for the given note from this notes bar. */
	void deleteIconForNote(Note note) {
		// find the note icon for the given note
		NoteIcon iconToDelete = null;
		for (Component c : getComponents()) {
			if (c instanceof NoteIcon) {
				NoteIcon icon = ((NoteIcon) c);
				if (note == icon.getNote()) {
					iconToDelete = icon;
					break;
				}
			}
		}
		
		// if icon found, then delete it
		if (iconToDelete == null) {
			System.out.println("NotesBar.deleteIconForNote: icon NOT FOUND for note: " + 
					note.toShortString());
		} else {
			remove(iconToDelete);
			repaint();
		}
	}
	
	
	
	/** Calculates and assigns the positions for each note icon in this notes bar. */
	void positionIcons() {
		
		// if there are no icons, nothing to do, return
		if (getComponentCount() == 0) return;
		
		// calculate geometry
		int yRange = getHeight() - ICON_HEIGHT - 2;
		float pageRange = (float)yRange / (float)pageCount;
		
		// set the y position of each icon
		for (Component c: this.getComponents()) {
			if (c instanceof NoteIcon) {
				NoteIcon icon = (NoteIcon)c;
				Note note = icon.getNote();
				int pageOffset = Math.round(pageRange * (note.getPageNumber() - 1));
				int rangeOffset = Math.round(pageRange * note.getTopOffset() / (float)pageHeight);
				int y = pageOffset + rangeOffset + 1;
				icon.setLocation(2, y);
			}
		}
		
		revalidate();
		repaint();
	}
	
	/**
	 * Calculates and assigns the position for the given note icon in this notes
	 * bar.
	 */
	void positionIcon(NoteIcon icon) {

		// calculate geometry
		int yRange = getHeight() - ICON_HEIGHT - 2;
		float pageRange = (float)yRange / (float)pageCount;
		
		// set the y position for the icon
		Note note = icon.getNote();
		int pageOffset = Math.round(pageRange * (note.getPageNumber() - 1));
		int rangeOffset = Math.round(pageRange * note.getTopOffset() / (float)pageHeight);
		int y = pageOffset + rangeOffset + 1;
		icon.setLocation(2, y);
		
		revalidate();
		repaint();
	}
	
	/** Deletes the note icon for the given note from this notes bar. */
	void deleteIcon(Note note) {
		System.out.println("NotesBar.deleteIcon");
		// TO DO: if note in collection, remove it, else warning message
	}
	
	/*
	void addTestIcon() {
		System.out.println("NotesBar.addTestIcon");

		ArrayList<Note> list = new ArrayList<Note>();

		Note note = new Note("Bob Test", new Date());
		note.setTopOffset(0);
		note.setPageNumber(1);
		list.add(note);

		note = new Note("Jane Test", new Date());
		note.setTopOffset(pageHeight - 1);
		note.setPageNumber(pageCount);
		list.add(note);

		initIcons(list);
	}
	*/
	
	/* ComponentListener */
	// TODO: listeners should be re-factored to use private nested classes & event adapters

	/** Processes a resize event my recalculating the position of all note icons. */
	public void componentResized(ComponentEvent arg0) {
		//System.out.println("NotesBar.componentResized: new H = " + getHeight());
		
		// if the new height is the same as the last height, ignore event
		int h = getHeight();
		if (h == lastHeight) {
			//System.out.println("  new height, same as the last height");
			return;
		}
		
		// new height, save state, reposition icons
		lastHeight = h;
		positionIcons();
	}

	public void componentHidden(ComponentEvent arg0) { }
	public void componentMoved(ComponentEvent arg0) { }
	public void componentShown(ComponentEvent arg0) { }
	
	/* MouseListener implementation */
	
	/** Processes a mouse click event on a note icon by calling the eReader. */
	public void mouseClicked(MouseEvent e) {
		Object src = e.getSource();
		NoteIcon icon = null;
		if (src instanceof NoteIcon) {
			icon = (NoteIcon) src;
			eReader.noteIconClicked(icon, e.getClickCount());
		}
	}

	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mousePressed(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }
}
