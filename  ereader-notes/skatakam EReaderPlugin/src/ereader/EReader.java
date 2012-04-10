package ereader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.*;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.SimpleViewer;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.pdf.plugins.eclipse.editors.PDFEditor;

import ereaderdelegate.EReaderDelegate;

/** PDF Viewer for Eclipse Note Delete Add
 * The class <code>EReader</code> defines an object that implements the core
 * behavior of the eReader Eclipse Plugin. One instance of this class is created
 * for each PDF document that is opened by Eclipse.
 * <p>
 * In terms of Design Patterns, an <code>EReader</code> is a Mediator object
 * that is responsible for controlling and coordinating the interaction of the
 * JPedal UI components and data objects, the EReader UI components and data
 * objects, and an <code>EReaderDelegate</code>.
 * <p>
 * An <code>EReader</code> extends the behavior of the JPedal plugin to allow
 * the user to create, view, and interact with user notes associated with a PDF
 * document. An <code>EReader</code> then delegates the implementation of
 * certain functions to its delegate. One instance of an
 * <code>EReaderDelegate</code> is created for each <code>EReader</code>.
 * <p>
 * The most significant data object that an <code>EReader</code> maintains is a
 * collection of <code>Note</code> objects. The internal representation of the
 * notes is private, however, the API to get and set the notes is in terms of an
 * array of notes, or <code>Note[]</code>.
 * main
 */
public class EReader {
	
	// JPedal objects
	private SimpleViewer simpleViewer;
	private SwingGUI swingGUI;
	private PdfDecoder pdfDecoder;
	private JTextField pageNumberField;
	
	// eReader UI components
	private JLabel notesInfoLabel;
	private JLabel notificationsInfoLabel;
	//private JLabel alert= new JLabel("U hav been clicked");
	private NotesBar notesBar;
	private NotesPanel notesPanel;
	
	// eReader state and data model
	private EReaderDelegate delegate;
	private File notesFile;
	private ArrayList<Note> notesList = new ArrayList<Note>();
	
	/*
	 * public API for the Eclipse plugin
	 */
	
	/**
	 * Creates a new <code>EReader</code> for the given <code>PDFEditor</code>
	 * in the Eclipse plugin. This constructor also creates the
	 * <code>EReaderDelegate</code> for this <code>EReader</code>.
	 * <p>
	 * Note: This constructor is part of the public interface, however, it is
	 * invoked from the Eclipse plugin and is not intended for use by the
	 * eReader delegate.
	 */
	public EReader(PDFEditor pdfEditor) {
		simpleViewer = pdfEditor.getSimpleViewer();
		swingGUI = simpleViewer.currentGUI;
		pdfDecoder = swingGUI.getPdfDecoder();
		delegate = EReaderDelegate.createEReaderDelegate(this);
	}
	
	/** filename
	 * Configures the SimpleViewer (a JPedal PDF viewer) for use in an eReader. 
	 * <p>
	 * Note: This method is part of the public interface, however, it is
	 * invoked from the Eclipse plugin and is not intended for use by the
	 * eReader delegate.
	 */
	public void configureSimpleViewer() {
		addNotesInfoLabel();
		addNotificationInfoLabel();
		addNotesBar();
		addPageNumberFieldListener();
		cleanupSplitPane();
		notesPanel = new NotesPanel(this);
		//addToolbarTestButtons();
		
		invokeDocumentLoaded();
		invokePageDisplayed(1);
	}
	
	/** 
	 * Adds a notes panel to the given tabbed page. 
	 * <p>
	 * Note: This method is part of the public interface, however, it is
	 * invoked from the Eclipse plugin and is not intended for use by the
	 * eReader delegate.
	 */
	public void addNotesPanel(JTabbedPane tabbedPane) {
		tabbedPane.addTab("Notes", notesPanel);
	}
	
	/*
	 * public API for the eReader delegate
	 */

	/** Returns a copy of the notes collection as an array. */
	public Note[] getNotes() {
		//!*!
		//System.out.println("<> total no.of pages for the document: "+notesList.size());
		return notesList.toArray(new Note[notesList.size()]);
	}

	/**
	 * Sets the collection of notes in the eReader to the given array of notes.
	 * This method also updates the notes icon bar and the notes panel with the
	 * new notes.
	 */
	public void setNotes(Note[] notes) {

		// update notes collection
		notesList.clear();
		for (Note n : notes) {
			notesList.add(n);
		}
		
		// update notes icon bar
		notesBar.initIcons(notesList);
		
		// update notes table (by design, the notes table uses the notesList as its
		// model, so it is automatically updated when the notesList is updated.)
	}
	
	/** Returns the total number of pages for the PDF document. */
	public int getPageCount() {
		//!*!
		//System.out.println("total no.of pages in the document: "+pdfDecoder.getPageCount());
		return pdfDecoder.getPageCount();
	}
	
	/**
	 * Returns the current page number for the PDF document. The first page is
	 * page 1.
	 */
	public int getPageNumber() {
		//!*!
			//	System.out.println("the page number of the page displayed: "+swingGUI.getPageNumber());
		return swingGUI.getPageNumber();
	}

	/**
	 * Sets the total number of notes in the total count label in the notes
	 * panel.
	 */
	public void setTotalNotesCount(int count) {
		notesPanel.setTotalNotesCount(count);
	}
	
	/**
	 * Sets the number of notes for the current page in the
	 * "count for this page" label on the toolbar.
	 */
	public void setNotesCountThisPage(int count) {
		String ntext = (count == 1) ? "Note" : "Notes";
		String text = "<html><p align=right>" + count + "&nbsp;" + ntext + 
				"<br><font size=-2 color=#555555>this page  <font></p></html>";
		notesInfoLabel.setText(text);
		//!*!
		//System.out.println("...the number of notes for the page displayed:"+count);
		
	}

	/** Shows the page for the PDF document specified by the given page number. */
	public void showPage(int pageNumber) {
		swingGUI.currentCommands.gotoPage(Integer.toString(pageNumber));
	}
	
	/** Returns the filename for the PDF document. */
	public String getFilename() {
		return pdfDecoder.getFileName();
	}
	
	/*
	 * package API: interface to the other eReader classes
	 */

	/** 
	 * Shows the selection for the given note in the document by first showing the
	 * page for the given note, then scrolling the page to show all or part of the
	 * note's selection.
	 */
	void showNoteSelectionInDocument(Note note) {
		
		// show the page for the note
		showPage(note.getPageNumber());
		
		// scroll to the selection for the note
		Rectangle rect = note.getSelectionRect();
		pdfDecoder.scrollRectToVisible(rect);
		
		// show the note's selection
		pdfDecoder.setHighlightAreas(note.getTextSelection());
		pdfDecoder.setHighlightedImage(note.getImageSelection());

		pdfDecoder.repaint();
	}
	
	/** Clears the current selection in the document. */
	void clearDocumentSelection() {
		pdfDecoder.setHighlightAreas(null);
		pdfDecoder.setHighlightedImage(null);
		pdfDecoder.repaint();
	}
	
	/** 
	 * This method is invoked after a new document is loaded. It reads the document's
	 * notes file, initializes the collection of notes with the notes read from the file, 
	 * initializes the icons on the notes icon bar, and then calls the delegate's 
	 * <code>documentLoaded</code> method.
	 * 
	 */
	void documentLoaded(String filename) {		
		readNotesFile();
		
		// init notes bar
		int pageHeight = pdfDecoder.getPdfPageData().getMediaBoxHeight(0);
		notesBar.setDocumentData(getPageCount(), pageHeight);
		notesBar.initIcons(notesList);
		
		// init notes panel
		notesPanel.setNotes(notesList);
		
		// notify delegate
		delegate.documentLoaded(filename);
	}
	
	/** 
	 * This method is invoked after a new page is displayed. It calls the delegate's
	 * <code>pageDisplayed</code> method.
	 */
	void pageDisplayed(int pageNumber) {
		//setNotesInfoCount(getNotesCount(pageNumber));	
		//!*!
	//	System.out.println("the page number of the page displayed: "+pageNumber);
		delegate.pageDisplayed(pageNumber);
	}

	/** Returns the JPedal SwingGUI object for this eReader. */
	SwingGUI getSwingGUI() {
		return swingGUI;
	}
	
	/* callbacks from the notes bar */
	
	/** Called when the user has clicked on a note icon in the icon bar. */
	void noteIconClicked(NoteIcon icon, int clickCount) {
		//System.out.println("EReader.noteIconClicked: icon = " + icon + 
		//		", clickCount = " + clickCount);
		Note note = icon.getNote();
		showNoteSelectionInDocument(note);
		notesPanel.setSelectedNote(note);
	}
	
	/* callbacks from the notes panel & table */
	
	/** Called when the user has clicked Add in the notes panel. */
	void notesPanelAddClicked() {
		//System.out.println("EReader.notesPanelAddClicked");
		
		// create a new note
		Note newNote = new Note(System.getProperty("user.name"), new Date());
		int pgno = getPageNumber();
		newNote.setPageNumber(pgno);
	    int pageHeight = pdfDecoder.getPdfPageData().getMediaBoxHeight(pgno);
	    int topOffset = 0;
		
		// get current text or image selection, if no selection, then show message and return
		@SuppressWarnings("unchecked")
		Map<Integer, Rectangle[]> highlightAreas = pdfDecoder.getHighlightAreas();
		int[] highlightedImage = pdfDecoder.getHighlightImage();
		if (highlightAreas == null || highlightAreas.size() == 0) {
			if (highlightedImage == null || highlightedImage.length == 0) {
				String msg = "To add a Note, you must first select some text or an image.";
				swingGUI.showMessageDialog(msg, "No Selection", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			newNote.setImageSelection(highlightedImage);
			topOffset = pageHeight - highlightedImage[1] - highlightedImage[3];
		} else {
			newNote.setTextSelection(highlightAreas);
			Rectangle[] rects = highlightAreas.values().iterator().next(); // get first map entry
			if (rects == null || rects.length == 0) {
				System.out.println("EReader.notesPanelAddClicked: text selection, INVALID Rectangle data");
				return;
			}
			topOffset = pageHeight - rects[0].y;
		}
		newNote.setTopOffset(topOffset);
		
		NoteWindow noteWindow = new NoteWindow(this, newNote, true);
		noteWindow.setVisible(true);
	}
	//$ share note click
	void notesPanelShareClicked(Note newNote) 
	{
		System.out.println("EReader.notesPanelshareClicked");
		ShareWindow share_Window = new ShareWindow(this, newNote, true);
		share_Window.setVisible(true);
	}
	
	//$ edit note click
	void notesPanelEditClicked(Note newNote)
	{
		System.out.println("EReader.notesPaneleditClicked");
		EditWindow edit_Window = new EditWindow(this, newNote, true);
		edit_Window.setVisible(true);
	}
	
	/** 
	 * Called when the user has selected the given note from the notes table and 
	 * clicked Delete. This method first deletes the icon for the selected note from
	 * the icon bar, clears the selection in the document and clears the selection in the 
	 * notes table.
	 * It then calls the delegate to delete the note. After the delegate is called,
	 * then the notes are saved.
	 */
	void notesPanelDeleteClicked(Note note) {
		//System.out.println("EReader.notesPanelDeleteClicked: note = " + 
		//		note.toShortString());

		// delete the corresponding note icon
		notesBar.deleteIconForNote(note);

		// clear the selection in the document and the notes panel
		clearDocumentSelection();
		notesPanel.clearSelection();
		
		// delete the note from the model by calling the delegate
		//notesList.remove(note);
		delegate.deleteNote(note);
		
		// set new counts (this is now done in the delegate)
		//setNotesCountThisPage(getNotesCount(getPageNumber()));
		//notesPanel.setTotalNotesCount(notesList.size());
		
		// save notes
		writeNotesFile();
	}
	
	/** Called when the user has selected a note in the notes table. */
	void noteSelectedInTable(Note note) {
		//System.out.println("EReader.noteSelectedInTable: note = " + note.shortString());
		showNoteSelectionInDocument(note);
		notesPanel.showNoteInNotePane(note);
	}
	
	/** Called when the user has double-clicked on a note in the notes table. */
	void noteDoubleClickedInTable(Note note) {
		//System.out.println("EReader.noteDoubleClickedInTable: note = " + note.shortString());
		NoteWindow noteWindow = new NoteWindow(this, note, false);
		noteWindow.setVisible(true);
	}

	/* callbacks from the note window */
	
	/** 
	 * Called when the user has clicked on Save in a notes window, passing in the note
	 * the user has created or viewed, plus the <code>newNote</code> flag indicating
	 * whether the given note is a new or existing note. If the note is new, then
	 * the delegate is called to add the note to the notes collection. If the note is not
	 * new, then the notes panel is updated to display any changes to the note and the
	 * delegate is not called. In all cases, the notes are saved to the document's notes 
	 * file.
	 */
	void noteWindowSaveClicked(Note note, boolean newNote) {
		
		// if this is a new note, ask delegate to add note and set new note counts,
		// then update icon bar and notes panel with note.
		if (newNote) {
			//notesList.add(note);
			//setNotesCountThisPage(getNotesCount(getPageNumber()));
			delegate.addNote(note);
			notesBar.addIcon(note);
			notesPanel.addNote(note);
		} else {
			notesPanel.updateNote(note);
		}
		
		// save notes
		writeNotesFile();
	}
	
	/**
	 * Sorts the notes, based on the given key, by calling the delegate to do the sort.
	 * After the delegate is called, the selection in notes table is cleared, and
	 * the notes are saved to the document's notes file.
	 */
	void sortNotes(String key) {
		
		// originally sorting was done here, now it is done in the delegate
		delegate.sortNotes(key);
		
		notesPanel.clearSelection();

		// write notes to storage in the new order
		writeNotesFile();
	}

	/*
	 * private API
	 */

	/* methods for reading & writing to the document's notes file */
	
	/** Reads the document's notes file and sets the notes collection. */
	@SuppressWarnings("unchecked")
	private void readNotesFile() {
		notesList.clear();
		
		// compute path to notes file
		String fn = getFilename();
		if (fn.toLowerCase().endsWith(".pdf") == false) {
			System.out.println(
				"EReader.readNotesFile: WARNING: document not a PDF: " + fn);
			// could show an error dialog here
			return;
		}
		String notesFilename = fn.substring(0, fn.length() - 4) + ".notes";
		
		// init File notesFile
		if (notesFile == null) {
			notesFile = new File(notesFilename);
		}
		
		// if notes file does not exist, this is not an error, nothing to read, so return
		if (notesFile.exists() == false) {
			//System.out.println("EReader.readNotesFile: notes file does not exist for: "
			//	+ notesFilename);
			return;
		}
		
		// notes file does exist, read notes
		Object obj = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(notesFile));
			obj = in.readObject();
			if (obj instanceof ArrayList<?>) {
				ArrayList<?> alist = (ArrayList<?>)obj;
				if (alist.size() == 0) {
					// obj is an empty ArrayList, not an error, notesList already cleared
				} else if (alist.get(0) instanceof Note) {
					// if first object is a Note, assign whole list (could check all objects)
					notesList = (ArrayList<Note>)alist;
				} else {
					throw new IOException("First object in Notes file is not a Note");
				}
			} else {
				throw new IOException("Notes file does not contain an ArrayList");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/** Writes the notes collection to to the document's notes file. */
	private void writeNotesFile() {
		if (notesFile == null) {
			return;
		}
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(notesFile));
			out.writeObject(notesList);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/* methods for creating eReader UI components */
	
	/** Adds the notes info label to the UI. */
	private void addNotesInfoLabel() {
		JToolBar topbar = swingGUI.getTopButtonBar();
		notesInfoLabel = new JLabel();
		setNotesCountThisPage(0);
		notesInfoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
		topbar.getParent().add(notesInfoLabel, BorderLayout.EAST);
	}
	
	/**$ Adds the notifications info label to the UI. */
	private void addNotificationInfoLabel() {
		final JToolBar topbar = swingGUI.getTopButtonBar();
		notificationsInfoLabel = new JLabel();
		
		notificationsInfoLabel.setBorder(BorderFactory.createEtchedBorder(1));//.createEmptyBorder(0, 0, 0, 5));
		//notificationsInfoLabel.setBorder(BorderFactory.c)
		topbar.getParent().add(notificationsInfoLabel, BorderLayout.WEST);
		int count1=5;
		String ntext = (count1 == 1) ? "Notification" : "Notifications";
		String text = "<html><p align=right>" + count1 + "&nbsp;" + ntext + 
				"<br><font size=2 color=#555555>this page  <font></p></html>";
		notificationsInfoLabel.setText(text);
		
		//topbar.getParent().add(alert, BorderLayout.WEST);
		notificationsInfoLabel.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount()>0){
					System.out.print("in mouse click");
				
					
					 String subject[] = {"James added new note in A group", "RamSree shared a new note", "James added you in B group","Raju shared a new note", "John added you in B group"};
					  JFrame frame = new JFrame("Notifications");
					  JPanel panel = new JPanel();
					  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					  JList list = new JList(subject);
				
					  Color c=new Color(2);
					  Color c1=new Color(5);
					  frame.setBackground(c);
					 
					  frame.setDefaultCloseOperation(1);
					  //frame.set
					  frame.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
					  panel.add(list);
					  panel.setAutoscrolls(true);
					
					  frame.add(panel);
					  frame.setSize(300,300);
					  frame.setForeground(c1);
					  frame.setVisible(true);
					//  frame.pack();
					 // frame.setLocation(null);
					  
				}
			
			}}
		);
		
	
		
	}
	
	
	
	/** Adds a listener to the page number field in the UI. */
	private void addPageNumberFieldListener() {
		JToolBar topbar = swingGUI.getTopButtonBar();
		Component c = topbar.getComponent(16);
		if (c instanceof JToolBar) {
			JToolBar toolBar = (JToolBar) c;
			c = toolBar.getComponent(2);
			if (c instanceof JTextField) {
				pageNumberField = (JTextField) c;
				PageNumberFieldListener listnr = new PageNumberFieldListener();
				pageNumberField.getDocument().addDocumentListener(listnr);
				pageNumberField.addKeyListener(listnr);
			} else {
				System.out.println(
				  "EReader.addPageNumberFieldListener: FAILED, component not JTextField");
			}
		} else {
			System.out.println(
			  "EReader.addPageNumberFieldListener: FAILED, component not JToolBar");
		}
	}

	/** Adds the notes icon bar to the UI. */
	private void addNotesBar() {
		JComponent displayPane = swingGUI.getDisplayPane();
		if (displayPane instanceof JSplitPane) {
			JSplitPane dsp = (JSplitPane) displayPane;
			JPanel right = (JPanel) dsp.getRightComponent();
			JComponent dspScroller = (JComponent) 
				((BorderLayout) right.getLayout()).getLayoutComponent(BorderLayout.EAST);
			right.remove(dspScroller);

			JPanel eastPanel = new JPanel(new BorderLayout(0, 0));

			notesBar = new NotesBar(this);
			notesBar.setWidthPreferences(dspScroller.getPreferredSize().width + 0);

			eastPanel.add(dspScroller, BorderLayout.WEST);
			eastPanel.add(notesBar, BorderLayout.EAST);
			right.add(eastPanel, BorderLayout.EAST);
			right.validate();
		}
	}
	
	/** 
	 * Sets the left side of the SimpleViewer splitpane to null and sets the divider size 
	 * to 0. This removes unused components from the UI when in plugin mode. The unused  
	 * components are confusing and take up screen space.
	 */
	private void cleanupSplitPane() {
		JComponent displayPane = swingGUI.getDisplayPane();
		if (displayPane instanceof JSplitPane) {
			JSplitPane dsp = (JSplitPane) displayPane;
			dsp.setLeftComponent(null);
			dsp.setDividerSize(0);
		}
	}
	
	/** Adds test buttons to a toolbar. This is for testing only. */
	/*
	private void addToolbarTestButtons() {
		JToolBar navbar = swingGUI.getNavigationBar();		
		navbar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 2));
		
		JButton test1 = new JButton("Test 1");
		test1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doTest1();
			}
		});
		navbar.add(test1);
		
		navbar.add(javax.swing.Box.createHorizontalStrut(12));
	}
	
	private void doTest1() {
		System.out.println("EReader.doTest1");
		notesBar.addTestIcon();
	}
	*/
	
	/* listeners */
	
	/**
	 * The <code>PageNumberFieldListener</code> class defines an object that listens to 
	 * the page number text field in the toolbar on the PDF viewer. When the text field's 
	 * data model, or document, is updated with a new value, then the 
	 * <code>invokePageDisplayed</code> method is called to indicate a new page has been 
	 * displayed.
	 */
	/*
	 * Implementation Note: Ideally the JPedal code would have some form of "page loaded"
	 * callback, but it does not. So that is why this odd technique of listening to
	 * the page number field is used. It's an elegant hack.
	 */
	private class PageNumberFieldListener implements DocumentListener,  KeyListener {
		private int lastPageNumber = 0;
		private boolean inputInProgress = false;

		public void keyPressed(KeyEvent e) {
			inputInProgress = (e.getKeyCode() != KeyEvent.VK_ENTER);
		}

		public void keyReleased(KeyEvent e) { }

		public void keyTyped(KeyEvent e) { }
		
		public void changedUpdate(DocumentEvent e) { }
		
		public void insertUpdate(DocumentEvent e) {
			if (inputInProgress == false) {
				String text = pageNumberField.getText();
				int pageNumber = -1;
				try {
					pageNumber = Integer.parseInt(text);
				} catch (NumberFormatException ex) {
					// pageNumber remains -1
				}
				if ((pageNumber > 0) && (pageNumber <= getPageCount()) && 
					(pageNumber != lastPageNumber)) {
					lastPageNumber = pageNumber;
					invokePageDisplayed(pageNumber);
				}
			}
		}
		
		public void removeUpdate(DocumentEvent e) { }
	}
	
	/* queued invocation methods */
	
	/** 
	 * Queues an invocation of method <code>documentLoaded</code> on the AWT event thread.
	 * Using this method allows all events on the AWT event queue to be processed before
	 * <code>documentLoaded</code> is invoked. This technique helps to ensure that 
	 * document loading is complete before <code>documentLoaded</code> is invoked.
	 */
	private void invokeDocumentLoaded() {
		final String filename = getFilename();
		//!*!
		//System.out.println("•	the filename of the document that was loaded: "+filename);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				documentLoaded(filename);
			}
		});
	}
	
	/** 
	 * Queues an invocation of method <code>invokePageDisplayed</code> on the AWT event 
	 * thread. Using this method allows all events on the AWT event queue to be processed 
	 * before <code>invokePageDisplayed</code> is invoked. This technique helps ensure 
	 * that the page is finished being displayed before <code>invokePageDisplayed</code> 
	 * is invoked.
	 */
	private void invokePageDisplayed(final int pageNumber) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pageDisplayed(pageNumber);
			}
		});
	}

}
