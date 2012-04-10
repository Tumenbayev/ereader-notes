package ereader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ShareWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private static DateFormat dateFormat = 
			SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	
	// UI components
	private JTextField authorField;
	private JLabel pageNumberField;
	private JButton shareButton;
	// state
	private EReader eReader;
	private Note note;
	private boolean newNote;
	
	public ShareWindow(EReader eReader, Note note, boolean newNote) {
		this.eReader = eReader;
		this.note = note;
		this.newNote = newNote;
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Sharing Options");
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
		
		String gp[]= {"a","b","c"};
		JComboBox cb = new JComboBox(gp);
		cb.setBackground(Color.gray);
		cb.setForeground(Color.blue);
	//	cb.setSize(getPreferredSize());
		cb.setSize(getMinimumSize());
		
		//combo box item listner
		//ItemListener aListener = null;
	//	cb.addItemListener(aListener);
		topPanel.add(cb);
		
		cp.add(topPanel, BorderLayout.NORTH);
		
		JPanel p = new JPanel(new FlowLayout(java.awt.FlowLayout.RIGHT));
		
		shareButton = new JButton("Share");
		p.add(shareButton);
		cp.add(p, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(shareButton);
		int i=cb.getSelectedIndex();
		System.out.println("in cb:"+gp[i]);
		// share button action listener
		ButtonActionListener listnr = new ButtonActionListener();
		shareButton.addActionListener(listnr);
		
		pack();
		setLocation(200, 400);
	}
	
	/** This class handles button clicks for the  and Cancel buttons. */
	private class ButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			
			if (cmd.equals("share")) {
				
				System.out.println("shared");
				
			} // else if Cancel, nothing to do
			
			// close window
			dispose();
			setVisible(false);
		}
	}
	
}
