package org.jpedal.pdf.plugins.eclipse.views;

import java.awt.Component;
import java.awt.Frame;
import java.awt.ScrollPane;

import javax.swing.JTabbedPane;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel;
import org.jpedal.examples.simpleviewer.gui.swing.SwingOutline;
import org.jpedal.pdf.plugins.eclipse.Activator;
import org.jpedal.pdf.plugins.eclipse.editors.PDFEditor;
import org.jpedal.utils.LogWriter;

public class PDFOutline implements IContentOutlinePage {
	
	private float scale;

	private Composite viewerPDF;
	
	private ScrollPane scrollPane=new ScrollPane();
	
	private SwingOutline tree = null;
	
	private boolean isDecoding;
	
	/**
     * current physical page number
     */
    private int page = 1;
    
	/**container for PDF*/
	private java.awt.Frame frame ;

	private PDFEditor pdfEditor;
	
	/**
	 * The constructor.
	 * @param editor 
	 */
	public PDFOutline(PDFEditor pdfEditor) {
		this.pdfEditor = pdfEditor;
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createControl(Composite parent) {
		
		if(Activator.isBroken)
			return;
		
		viewerPDF = new Composite(parent, SWT.EMBEDDED|SWT.NO_BACKGROUND);
		
		/**
		 * initialise JPedal PDF view on first call
		 */
		setTree();
	}

	public void setPDFDecoder(PDFEditor pdfEditor){
		this.pdfEditor=pdfEditor;
	}

	public void setTree() {
		
		try{
			
			JTabbedPane tabs=new JTabbedPane();
			
			/** setup GUI and wrapper around SWING component */		
			if(frame == null)
				frame = SWT_AWT.new_Frame(viewerPDF);
			else{
				frame.removeAll();
			}
			
			tree=pdfEditor.getOutline();
			GUIThumbnailPanel thumbnail = pdfEditor.getThumbnail();
			
			if(tree !=null)
			tabs.addTab("Outline", tree);
			
			if(thumbnail!=null) {
				// EREADER addition: change tabs label "thumbnails" to "Pages"
				//tabs.addTab("thumbnails",(Component) thumbnail);
				tabs.addTab("Pages", (Component) thumbnail);
			}
			
			// EREADER addition: add the eReader Notes Panel to the tabbed pane
			pdfEditor.getEReader().addNotesPanel(tabs);
			
			frame.add(tabs);

			frame.validate();
			
		}catch(Exception e){
			LogWriter.writeLog("Exception "+e+" in createPartControl >> PDFViewer");
			e.printStackTrace();
		}catch(Error e){
			LogWriter.writeLog("Error "+e+" in createPartControl >> PDFViewer");
			e.printStackTrace();
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewerPDF.setFocus();
		scrollPane.repaint();
	}
	
	public void dispose() {
		viewerPDF.dispose();
	}

	public Composite getViewer() {
		return viewerPDF;
	}

	public void repaint() {
		scrollPane.invalidate();
		frame.repaint();
	}

	public Control getControl() {
		return viewerPDF;
	}


	public void setActionBars(IActionBars actionBars) {
	}


	public void addSelectionChangedListener(ISelectionChangedListener listener) {
	}


	public ISelection getSelection() {
		return null;
	}


	public void removeSelectionChangedListener(ISelectionChangedListener listener) {		
	}


	public void setSelection(ISelection selection) {
	}
	
    private void repaintPDF() {
    	
    	pdfEditor.repaintPDF();
    	
        scrollPane.invalidate();
        scrollPane.repaint();
    }

	
	
	public Frame getBlank() {
		return frame;
	}
	
	
}