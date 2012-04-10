/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project InfoPopup:  http://www.jpedal.org
 * Project Lead:  Mark Stephens (mark@idrsolutions.com)
 *
 * (C) Copyright 2007, IDRsolutions and Contributors.
 *
 * 	This file is part of JPedal
 *
 @LICENSE@
 *
 * ---------------

 * PDFSearch.java
 * ---------------
 * (C) Copyright 2006, by IDRsolutions and Contributors.
 *
 *
 * --------------------------
 */
package org.jpedal.pdf.plugins.eclipse.views;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import org.eclipse.jface.action.IToolBarManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.part.*;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.jpedal.exception.PdfException;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.grouping.SearchType;
import org.jpedal.utils.Messages;
import org.jpedal.pdf.plugins.eclipse.editors.PDFEditor;


/**
 * Sample PDF Search for Eclipse
 * 
 * Many thanks to sam@edges.org for the considerable help he gave me in setting up
 * <p>
 */

public class PDFSearch extends ViewPart{
	
	/**GUI components*/
	Text searchText;
	Text searchCount;
	Button searchAll;
	Button searchButton;
	List swtList;
	Display display;
	Composite composite;
	PDFEditor editorUI;
	
	/**GUI listeners*/
	MouseListener ML;
	SelectionListener AL;
	KeyListener KL;
	FocusListener FL;
	
	/**Search Window layout*/
	GridLayout grid = new GridLayout();
	
	/**swing thread to search in background*/
	Thread searcher=null;
	
	/**used when fiding text to highlight on page*/
	Map textPages=new HashMap();
	
	/**Highlights or results*/
	Map textRectangles=new HashMap();

	/**deletes message when user starts typing*/
	private boolean deleteOnClick;
	
	/**flag to stop multiple listeners*/
	private boolean isSetup=false;
	
	/**flag to show searching taking place*/
	public boolean isSearch=false;

	/**Default message for search text is messages aren't present*/
	String defaultMessage="Enter your text here";

	/**number fo search items*/
	private int itemFoundCount=0;
	
	/** store all searches so we can update*/
	Map storedData=new HashMap();
	
	private IPartListener partListener; // nh
	private String pdfTitle="";

	
	/**
	 * The constructor.
	 */
	public PDFSearch() {

		super();
		
		if(PDFEditor.debug)
			System.out.println("PDFView called");

	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
	
		this.composite=parent;

		grid.numColumns = 3;
		this.composite.setLayout(grid);

		
		display=composite.getDisplay();

		find(); 
		initializeToolBar();
		
		initEditorListener(); // nh
		
		handleActiveEditor(null);
		
	}
	
	// nh
	public void dispose() {		
		if (partListener != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener(partListener);
			partListener = null;
		}
		super.dispose();
	}


	/**
	 * When tab selected, give searchText the focus
	 */
	public void setFocus() {
		
		if(searchText!=null)
		searchText.forceFocus();
	}
		
	/**
	 * find text on page
	 */
	public void find(){

		/**
		 * pop up new window to search text (initialise if required)
		 */
		if(isSetup){ //global variable so do NOT reinitialise
			searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound")+" "+itemFoundCount);
			searchText.selectAll();
			searchText.forceFocus();
		}else{
			//Setup Search Window
			isSetup=true;
			itemFoundCount=0;
			textPages.clear();
			textRectangles.clear();

			//Setup layout structure
			GridData spanTwoCellsNoFillHorizontal = new GridData(GridData.GRAB_HORIZONTAL);
			spanTwoCellsNoFillHorizontal.horizontalSpan = 3;

			GridData spanTwoCellsFillHorizontal = new GridData(SWT.FILL,SWT.DEFAULT,true,false);
			spanTwoCellsFillHorizontal.horizontalSpan = 3;

			GridData fillHorizontalGrap = new GridData(SWT.FILL,SWT.DEFAULT,true,false);

			GridData spanTwoCellsFillVertical = new GridData(SWT.FILL,SWT.FILL,true,true);
			spanTwoCellsFillVertical.horizontalSpan = 3;

			//Searchtext, for pdf document
			defaultMessage=Messages.getMessage("PdfViewerSearchGUI.DefaultMessage");
			searchText=new Text(composite, SWT.BORDER);
			searchText.setText(defaultMessage);
			searchText.setLayoutData(fillHorizontalGrap);
			searchText.forceFocus();

			//Start search
			searchButton=new Button(composite, SWT.PUSH);
			searchButton.setText(Messages.getMessage("PdfViewerSearch.Button"));

			//Add components in order to allow for layout
			searchAll=new Button(composite, SWT.CHECK);
			searchAll.setSelection(true);
			searchAll.setText(Messages.getMessage("PdfViewerSearch.CheckBox"));
			//searchAll.setLayoutData(spanTwoCellsNoFillHorizontal);


			//Search counter for results
			searchCount=new Text(composite, SWT.BORDER);
			searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound")+" "+itemFoundCount);
			searchCount.setEditable(false);
			searchCount.setLayoutData(spanTwoCellsFillHorizontal);

			// List of Search results
			swtList = new List(composite, SWT.BORDER | SWT.V_SCROLL);
			swtList.setLayoutData(spanTwoCellsFillVertical);

			swtList.addMouseMoveListener(new MouseMoveListener(){

				public void mouseMove(MouseEvent e) {

					int index=e.y/20;


					final Object page=textPages.get(new Integer(index));

					display.asyncExec (new Runnable () {
						public void run () {
							if(page!=null)
								swtList.setToolTipText("Page "+page);
							else
								swtList.setToolTipText("");
						}
					});

				}});

			swtList.addMouseListener(new MouseListener(){
				public void mouseDoubleClick(MouseEvent e) {}
				public void mouseDown(MouseEvent e) {}
				public void mouseUp(MouseEvent e) {

					if(editorUI.isOpen()){//{if (!event.getValueIsAdjusting()) {
						final float scaling= (float) editorUI.getScale();

						int id=swtList.getSelectionIndex();

						//if a search highlight exists, highlight it
						if(id!=-1){

							Integer key=new Integer(id);
							Object newPage=textPages.get(key);

							if(newPage!=null){

								final int nextPage=((Integer)newPage).intValue();
								final Rectangle highlight=(Rectangle) textRectangles.get(key);

								//move to new page if needed
								if(editorUI.getPage()!=nextPage){

									PlatformUI.getWorkbench().getDisplay().syncExec(
											new Runnable() {
												public void run(){
													editorUI.setPage(nextPage,scaling);
												}
											});									
								}

								//update page counter
								if(!PDFEditor.useViewer)
								editorUI.setPageCounter();    
					    		
								//and highlight the text
								editorUI.setHighlight(highlight,nextPage);

							}
						}
					}
				}
			});

			//setup searching
			AL = new SelectionListener(){
				public void widgetDefaultSelected(SelectionEvent e) {}

				public void widgetSelected(SelectionEvent e) {
					search();

				}

				private void search() {
					if(!isSearch){

						try {
							searchText();
						} catch (Exception e1) {
							System.out.println("Exception "+e1);
							e1.printStackTrace();
						}
					}else{
						searcher.interrupt();
						isSearch=false;
						searchButton.setText(Messages.getMessage("PdfViewerSearch.Button"));
					}
				}
			};
	
			// Add action to button
			searchButton.addSelectionListener(AL);

			//By defaults (at start) all text is selected and will delete on user input
			searchText.selectAll();
			deleteOnClick=true;

			FL = new FocusListener(){

				public void focusGained(FocusEvent e) {
					// TODO Auto-generated method stub					
				}

				//ensure changes picked up
				public void focusLost(FocusEvent e) {
					//cacheData(editorUI,pdfTitle);
					//System.out.println("focusLost "+pdfTitle);
					
				}
			};
			
			KL = new KeyListener(){
				public void keyPressed(org.eclipse.swt.events.KeyEvent e) {

					//Make all text disappear when user selects search text
					if(deleteOnClick){
						deleteOnClick=false;
						searchText.setText("");
					}

					char key=e.character;

					// If user types 'enter'
					if(key==13){
						try {
							searchText();
						} catch (Exception e1) {
							System.out.println("Exception "+e1);
							e1.printStackTrace();
						}
					}
				}

				public void keyReleased(org.eclipse.swt.events.KeyEvent e) {/*Do Nothing*/}
			};
		
			//Place cursor at the end of the text
			searchText.setSelection(searchText.getText().length());
			searchText.addKeyListener(KL);
			searchText.addFocusListener(FL);
			
			
		}
	}

	/**
	 * Search Text from the currently displayed PDF
	 * @throws Exception (if not a user exit, display warning, else stop quietly)
	 */
	private void searchText() throws Exception {

		//find editor window if available and get link to PdfDecoder	
		IWorkbenchWindow[] aa = PlatformUI.getWorkbench().getWorkbenchWindows();
		for(int ii=0;ii<aa.length;ii++){
			IWorkbenchPage[] p = aa[ii].getPages();

			int count=p.length;
			for(int jj=0;jj<count;jj++){
				IEditorPart current = p[jj].getActiveEditor();
				if(current instanceof PDFEditor){
					jj=count;
					editorUI = (PDFEditor) current;

				}
			}
		}
		
		if(editorUI==null)
			return;

		/** if running terminate first */
		if (searcher != null)
			searcher.interrupt();

		searchButton.setText(Messages.getMessage("PdfViewerSearchButton.Stop"));
		//searchButton.invalidate();
		//searchButton.repaint();
		isSearch=true;

		searchCount.setText(Messages.getMessage("PdfViewerSearch.Scanning1"));
		searchCount.redraw();//repaint();

//		get text
		final String textToFind=searchText.getText();

		final boolean searchWholeDocument=searchAll.getSelection();

		searcher = new Thread() {

			public void run() {
				boolean userStopped = false;
				//<start-demo><start-full>
				long start=System.currentTimeMillis();
				//<end-full><end-demo>

				try {

					display.asyncExec (new Runnable () {
						public void run () {
							swtList.removeAll();
						}
					});

					int listCount=0;
					textPages.clear();

					textRectangles.clear();
					itemFoundCount=0;

					editorUI.clearSearchHighlights();
					
					//page range
					int startPage=1,endPage=editorUI.getPDFPageCount()+1;

					if(!searchWholeDocument){
						startPage=editorUI.getPage();
						endPage=startPage+1;
					}

					//search all pages
					for(int i=startPage;i<endPage;i++){

						if (Thread.interrupted()){
							userStopped = true;
							throw new InterruptedException();
						}
						
						
						/** create a grouping object to apply grouping to data */
						try {
							
							/** common extraction code */
							PdfGroupingAlgorithms currentGrouping = editorUI.getPdfGroupingObject(i);

							//tell JPedal we want teasers
							currentGrouping.generateTeasers();

							float[] co_ords=currentGrouping.findText(null,i,new String[]{textToFind},SearchType.DEFAULT);
							
							//	other pair of points so we can highlight
							final String[] teasers=currentGrouping.getTeasers();

							if (Thread.interrupted()){
								userStopped = true;
								throw new InterruptedException();
							}
							
							if (co_ords != null && teasers!=null) {
								int count = co_ords.length;
								for (int ii = 0; ii < count; ii = ii + 5) {

									int wx1 = (int) co_ords[ii];
									int wy1 = (int) co_ords[ii + 1];
									int wx2 = (int) co_ords[ii + 2];
									int wy2 = (int) co_ords[ii + 3];

									Rectangle rectangle = new Rectangle(wx1, wy2, wx2 - wx1, wy1 - wy2);

									int seperator = (int)co_ords[ii + 4];


									Integer key=new Integer(listCount);
									listCount++;
									textRectangles.put(key,rectangle);
									textPages.put(key,new Integer(i));
									
									final String tease=teasers[ii/5];
									display.asyncExec (new Runnable () {
										public void run () {
											
											if (!Thread.interrupted()){
												swtList.add(tease);
												swtList.redraw();
											}
										}
									});
								}
							}
							
							//new value or 16 pages elapsed
							if((co_ords!=null)|((i % 16) ==0)){

								final int count=i;
								display.asyncExec (new Runnable () {
									public void run () {
										if (!Thread.interrupted()){
										searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound")+" "+
												itemFoundCount+" "+ Messages.getMessage("PdfViewerSearch.Scanning")+count);
										searchCount.redraw();
										}
							}
								});
							}

						} catch (PdfException e1) {

							//<start-full><start-demo>
							e1.printStackTrace();
							//<end-demo><end-full>
						}
					}

					
					//reset search button
					isSearch=false;
					
					display.asyncExec (new Runnable () {
						public void run () {
							
							if (!Thread.interrupted()){
							searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound")+" "
									+itemFoundCount+"  "+Messages.getMessage("PdfViewerSearch.Done"));
							searchButton.setText(Messages.getMessage("PdfViewerSearch.Button"));
							}

						}
					});
					
					//<start-full><start-demo>
					/**
					 * show time and memory usage
					 */
					System.gc();

					System.out
					.println("Search memory="+((Runtime.getRuntime().totalMemory() - Runtime
							.getRuntime().freeMemory()) / 1000)
							+ "K");

					System.out.println("Search time="+(((float) Math.abs(((System
							.currentTimeMillis() - start) / 100))) / 10)
							+ "s");
					//<end-demo><end-full>

				}catch (Exception e) {
					//User did not terminate search
					if(!userStopped)
						JOptionPane.showMessageDialog(null, "An error has occured during this search, some results may not have been found.\n\nPlease send this file to IDRSolutions for investigation.");

					display.asyncExec (new Runnable () {
						public void run () {
							searchButton.setText(Messages.getMessage("PdfViewerSearch.Button"));
						}
					});
					
					isSearch=false;
					
					//<start-full><start-demo>
					e.printStackTrace();
					//<end-demo><end-full>
				}
					}
		};

		searcher.start();
		
	}
	
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}
	
	/*
	 * code input from sam at Neoharbor (many thanks for the help)
	 * 
	 */	
	class PDFEditorPartListener implements IPartListener { 
		public void partActivated(IWorkbenchPart part) {
			
			if(editorUI!=null)
			editorUI.stopSearch();
			
			/** if running terminate first */
			if (searcher != null){
				searcher.interrupt();
			}
			
			handleActiveEditor(part);			
		}

		public void partBroughtToTop(IWorkbenchPart part) {
			// TODO Auto-generated method stub
		}

		public void partClosed(IWorkbenchPart part) {
			
			//make sure no search in progress
			if(editorUI!=null)
			editorUI.stopSearch();
			
			/** if running terminate first */
			if (searcher != null){
				searcher.interrupt();
			}
		}

		public void partDeactivated(IWorkbenchPart part) {
			
			//make sure no search in progress
			if(editorUI!=null)
			editorUI.stopSearch();
			
			/** if running terminate first */
			if (searcher != null){
				searcher.interrupt();
			}	
		}

		public void partOpened(IWorkbenchPart part) {
			handleActiveEditor(part);
		}
	}
	
	// nh
	private void initEditorListener() {
		if (partListener == null) {	
			partListener = new PDFEditorPartListener();
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(partListener);
		}
	}
	
	// nh
	private void handleActiveEditor(IWorkbenchPart part) {
		IWorkbenchPart workingPart = null;
		
		workingPart = part;
		if (workingPart == null) {
			workingPart = getActiveEditor();
		}
		
		if (workingPart == null) {
			return;
		}
		
		
		/*
		 * ALL workbench parts (Viewer & editors) are sent here
		 * So, act only on our editor 
		 */
		
		if (workingPart instanceof org.jpedal.pdf.plugins.eclipse.editors.PDFEditor ) {
			
			//update objects and contents
			editorUI = (PDFEditor) workingPart;
			
			editorUI.stopSearch();
			
			/** if running terminate first */
			if (searcher != null){
				searcher.interrupt();
			}
			
			
			//cache data from old
			if(!pdfTitle.equals(editorUI.getTitle())){
				//System.out.println("b "+pdfTitle);
				cacheData(part,pdfTitle);
				
				display.asyncExec (new Runnable () {
					public void run () {
				
				/////store in Map so we can retrieve
				SearchData storedSearchData=(SearchData) storedData.get(pdfTitle);
				
				if(storedSearchData==null)
					storedSearchData=new SearchData();

						
					
				//save values from components
				searchAll.setSelection(storedSearchData.searchAllSelection);
				
				if(storedSearchData.searchAllText==null)
					searchAll.setText(Messages.getMessage("PdfViewerSearch.CheckBox"));
				else
					searchAll.setText(storedSearchData.searchAllText);
				
				//System.out.println("restore storedSearchData.searchTextText="+storedSearchData.searchTextText);
				
				if(storedSearchData.searchTextText==null){
					searchText.setText(Messages.getMessage("PdfViewerSearchGUI.DefaultMessage"));
				}else{
					searchText.setText(storedSearchData.searchTextText);
				}
				//Search counter for results
				if(storedSearchData.searchTextText==null)
					searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound")+" 0");
				else	
					searchCount.setText(storedSearchData.searchCountText);

				//reset List of Search results
				String[] listValues=storedSearchData.swtList;
				
				swtList.removeAll();
				
				if(listValues!=null){
					
					int count=listValues.length;	
					for(int j=0;j<count;j++)	
						swtList.add(listValues[j]);
					
				}
				
				//restore any search data
				textRectangles=storedSearchData.textRectangles;
				textPages=storedSearchData.textPages;
				
					}
				});
			}
			
			pdfTitle=editorUI.getTitle();
			
		}		
	}

	private void cacheData(IWorkbenchPart part, String title) {
		
		//System.out.println("cache "+title);
		//make sure no search in progress
		editorUI.stopSearch();
		
		/** if running terminate first */
		if (searcher != null){
			searcher.interrupt();
		}
		
		/////store in Map so we can retrieve
		
		SearchData storedSearchData=new SearchData();
		
		this.storedData.put(title, storedSearchData);
		
		//save values from components
		storedSearchData.searchAllSelection=searchAll.getSelection();
		
		storedSearchData.searchAllText=searchAll.getText();
		
		storedSearchData.searchTextText=searchText.getText();
		
		//System.out.println("Set storedSearchData.searchTextText="+storedSearchData.searchTextText);
		
		//Search counter for results
		storedSearchData.searchCountText=searchCount.getText();

		// List of Search results
		int count=swtList.getItemCount();	
		
		String[] listValues=new String[count];
		for(int j=0;j<count;j++)		
			listValues[j]=swtList.getItem(j);
			
		storedSearchData.swtList=listValues;
		
		//save any search data
		storedSearchData.textRectangles=this.textRectangles;
		storedSearchData.textPages=this.textPages;
		
	}
	
	/**
	 * Carefully, step by step obtains the active editor; 
	 * several situations occur where this is not straight forward (i.e, startup / shutdown)
	 * @return
	 */
	private static final IEditorPart getActiveEditor() {
		
		IWorkbenchPage activePage = null;
		IWorkbench workbench = null;
		IWorkbenchWindow activeWorkbenchWindow = null;
		IEditorPart activeEditor = null;
		
		// IResourceNavigateInquiry inquiry = null;
			
		workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
			if (activeWorkbenchWindow != null) {
				activePage = activeWorkbenchWindow.getActivePage();
				if (activePage != null) {
					activeEditor = activePage.getActiveEditor();
				}				
			}			
		}		 

		return activeEditor;
	}
	
	/**
	 * hold data for a search
	 * @author markee
	 *
	 */
	public class SearchData{
		
		public Map textPages=new HashMap();
		public Map textRectangles=new HashMap();
		public String[] swtList;
		public String searchCountText;
		public String searchTextText;
		public String searchAllText;
		public boolean searchAllSelection;
		
	}
}
