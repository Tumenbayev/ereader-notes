package org.jpedal.pdf.plugins.eclipse.views;


import java.awt.Color;
import java.awt.ScrollPane;

import javax.swing.BorderFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.part.*;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.SWT;
import org.jpedal.PdfDecoder;
import org.jpedal.pdf.plugins.eclipse.editors.BookmarkPopup;
import org.jpedal.pdf.plugins.eclipse.editors.InfoPopup;
import org.jpedal.pdf.plugins.eclipse.editors.PDFEditor;
import org.jpedal.utils.LogWriter;

/**
 * Sample PDF View for Eclipse
 * <p>
 */

public class PDFView extends ViewPart {

	private float scaling = .5f;

	private Composite viewer;

	/**actual JPedal library*/
	private PdfDecoder decodePDF=new PdfDecoder();

	/** Current page number (first page is 1) */
	private int currentPage = 1;

	/**container for PDF*/
	private java.awt.Frame frame ;


	/**
	 * The constructor.
	 */
	public PDFView() {

		if(PDFEditor.debug)
			System.out.println("PDFView called");
	}


	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		/**
		 * initialise JPedal PDF view on first call
		 */
		try{
			viewer = new Composite(parent, SWT.EMBEDDED|SWT.NO_BACKGROUND);

			//add a border and center
			decodePDF.setPDFBorder(BorderFactory.createLineBorder(Color.black, 1));
			decodePDF.setInset(5,5); 

			/** setup GUI and wrapper around SWING component - 
			 * does not work on MAC until AWT bridge bug fixed */
			frame = SWT_AWT.new_Frame(viewer);
			ScrollPane scrollPane = new ScrollPane();
			scrollPane.add(decodePDF);
			frame.add(scrollPane);
			frame.pack();
		}catch(Exception e){
			LogWriter.writeLog("Exception "+e+" in createPartControl >> PDFViewer");
			e.printStackTrace();
		}catch(Error e){
			LogWriter.writeLog("Error "+e+" in createPartControl >> PDFViewer");
			e.printStackTrace();
		}
	}

	public void showInfo(){

		InfoPopup info = new InfoPopup(getSite().getShell());
		info.open();
	}
	
	public void showBookmarks(){

		BookmarkPopup bookmarks = new BookmarkPopup(getSite().getShell());
		bookmarks.open();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.setFocus();
		decodePDF.repaint();
	}

	public void dispose() {
		viewer.dispose();
		super.dispose();
	}

	public Composite getViewer() {
		return viewer;

	}


	public PdfDecoder getPDF() {
		return decodePDF;
	}


	public void repaint() {
		decodePDF.invalidate();
		frame.repaint();

	}


	public void openPDF(String fileName) {

		currentPage=1;
		scaling=.5f;
		try {
			decodePDF.openPdfFile(fileName);
			decodePDF.setPageParameters(scaling, currentPage); 
			decodePDF.decodePage(currentPage);
			repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void zoom(float scale) {

		scaling=scaling*scale;
		decodePDF.setPageParameters(scaling, currentPage); 
		decodePDF.updateUI();
		repaint();

	}

	public void changePage(int pageChange) {
		
		int newPage=currentPage+pageChange;
		
		/**check page in range*/
		if((pageChange==-1 && currentPage>1) || (pageChange==1 && currentPage < decodePDF.getPageCount()-1)||
				(pageChange==-10 && currentPage>10) || (pageChange==10 && currentPage < decodePDF.getPageCount()-10)){

			currentPage=newPage;
			try {
				decodePDF.clearHighlights();
				//decodePDF.addHighlights(null,false);
				decodePDF.setPageParameters(scaling, currentPage); 
				decodePDF.decodePage(currentPage);
				decodePDF.updateUI();
				repaint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}