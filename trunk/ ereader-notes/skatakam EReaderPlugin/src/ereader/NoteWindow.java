package ereader;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * The class <code>NoteWindow</code> defines an object that implements a note window
 * for an eReader. A note window is a standalone window that is used to create a new
 * note or view an existing note.
 */
public class NoteWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private static DateFormat dateFormat = 
			SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	
	// UI components
	private JTextField authorField;
	private JSlider    ratingSlider;
	private JLabel     pageNumberField;
	private JTextArea  noteTextArea;
	private JButton    cancelButton;
	private JButton    saveButton;
	
	// state
	private EReader eReader;
	private Note note;
	private boolean newNote;
	
	/**
	 * Creates a new note window for the given eReader and given note. The flag
	 * <code>newNote</code> specifies if the given note is new or if it already exits.
	 * If a note is new and the user clicks Save, then the eReader is called to
	 * save the note. If If the user clicks Cancel, then the window is closed
	 * and no futher action is taken. If the note is an existing note, then any
	 * allowed changes made in the user interface are saved to the note.
	 */
	public NoteWindow(EReader eReader, Note note, boolean newNote) {
		this.eReader = eReader;
		this.note = note;
		this.newNote = newNote;
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(newNote ? "New Note" : "New Note");
		Container cp = getContentPane();
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 8, 8));

		JLabel maxWidthLabel = new JLabel("Author: ");
		Dimension maxLabelSize = maxWidthLabel.getPreferredSize();
		
		Box row = new Box(BoxLayout.X_AXIS);
		JLabel lbl = new JLabel("Author: ", JLabel.RIGHT);
		lbl.setMinimumSize(maxLabelSize);
		lbl.setPreferredSize(maxLabelSize);
		row.add(lbl);
		row.add(Box.createHorizontalStrut(7));
		authorField = new JTextField(note.getAuthor(), 30);
		row.add(authorField);
		topPanel.add(row);
		
		topPanel.add(Box.createVerticalStrut(4));
		
		row = new Box(BoxLayout.X_AXIS);
		lbl = new JLabel("Date: ", JLabel.RIGHT);
		lbl.setMinimumSize(maxLabelSize);
		lbl.setPreferredSize(maxLabelSize);
		row.add(lbl);
		row.add(Box.createHorizontalStrut(11));
		JLabel dateField = new JLabel(dateFormat.format(note.getDate()));
		row.add(dateField);
		row.add(Box.createHorizontalGlue());
		topPanel.add(row);
		topPanel.add(Box.createVerticalStrut(6));
				
		row = new Box(BoxLayout.X_AXIS);
		lbl = new JLabel("Page: ", JLabel.RIGHT);
		lbl.setMinimumSize(maxLabelSize);
		lbl.setPreferredSize(maxLabelSize);
		row.add(lbl);
		row.add(Box.createHorizontalStrut(11));
		pageNumberField = new JLabel(Integer.toString(note.getPageNumber()));
		row.add(pageNumberField);
		row.add(Box.createHorizontalGlue());
		topPanel.add(row);
		
		row = new Box(BoxLayout.X_AXIS);
		lbl = new JLabel("Rating: ", JLabel.RIGHT);
		lbl.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
		lbl.setMinimumSize(maxLabelSize);
		lbl.setPreferredSize(maxLabelSize);
		lbl.setAlignmentY(0.0f);
		row.add(lbl);
		ratingSlider = new JSlider(1, 10, note.getRating());
		ratingSlider.setPaintTicks(true);
		ratingSlider.setPaintLabels(true);
		ratingSlider.setMajorTickSpacing(1);
		ratingSlider.setSnapToTicks(true);
		Dimension sz = ratingSlider.getPreferredSize();
		sz.width = 250;
		ratingSlider.setMaximumSize(sz);
		ratingSlider.setPreferredSize(sz);
		ratingSlider.setMinimumSize(sz);
		ratingSlider.setAlignmentY(0.0f);
		row.add(ratingSlider);
		row.add(Box.createHorizontalGlue());
		topPanel.add(row);
	
		cp.add(topPanel, BorderLayout.NORTH);
		
		JPanel p = new JPanel(new FlowLayout(java.awt.FlowLayout.RIGHT));
		cancelButton = new JButton("Cancel");
		saveButton = new JButton("Save");
		p.add(cancelButton);
		p.add(saveButton);
		cp.add(p, BorderLayout.SOUTH);
		
		noteTextArea = new JTextArea(14, 50);
		noteTextArea.setLineWrap(true);
		noteTextArea.setWrapStyleWord(true);
		JScrollPane scroller = new JScrollPane(noteTextArea,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		cp.add(scroller, BorderLayout.CENTER);
		
		getRootPane().setDefaultButton(saveButton);
		
		// add button action listener
		ButtonActionListener listnr = new ButtonActionListener();
		cancelButton.addActionListener(listnr);
		saveButton.addActionListener(listnr);
		
		// if this is an existing note, add text from note, make some fields read-only
		if (newNote == false) {
			noteTextArea.setText(note.getText());
			authorField.setEditable(true);// $ $			
			noteTextArea.setEditable(false);
		}
		
		pack();
		setLocation(300, 200);
	}
	
	/** This class handles button clicks for the Save and Cancel buttons. */
	private class ButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			
			if (cmd.equals("Save")) {
				
				// fill in note with content from the UI
				if (newNote) {
					note.setAuthor(authorField.getText());
					note.setText(noteTextArea.getText());
				}
				note.setRating(ratingSlider.getValue());
				
				// call the eReader 
				try {
					eReader.noteWindowSaveClicked(note, newNote);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			} // else if Cancel, nothing to do
			
			// close window
			dispose();
			setVisible(false);
		}
	}
	
}
