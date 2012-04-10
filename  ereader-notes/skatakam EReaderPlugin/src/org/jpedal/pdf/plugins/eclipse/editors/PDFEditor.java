package org.jpedal.pdf.plugins.eclipse.editors;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.SimpleViewer;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel;
import org.jpedal.examples.simpleviewer.gui.swing.SwingOutline;
import org.jpedal.external.JPedalActionHandler;
import org.jpedal.external.Options;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.io.ArrayDecoder;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.OutlineObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.pdf.plugins.eclipse.Activator;
import org.jpedal.pdf.plugins.eclipse.views.PDFOutline;
import org.jpedal.pdf.plugins.eclipse.views.PDFSearch;
import org.jpedal.utils.LogWriter;
import org.jpedal.Display;
import org.jpedal.PdfDecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ereader.EReader; // EREADER addition

public class PDFEditor extends MultiPageEditorPart {

	/**used in development*/
	final public static boolean debug=false;
	
	/**current page size 1=100% */
	private float scale = 1f;
	
	/**flag to stop multiple access*/
	boolean isDecoding=false;
	
	/**page count for PDF*/
	int numberOfPages=0;
	
	/**main SWT display elements*/
	private Composite viewer;
	private Frame frame;

	/**file name of PDF including path*/
	private String fileName="";

	/**actual JPedal library*/
	private PdfDecoder pdfDecoder;
	
	/**flag to switch to Viewer*/
	public static boolean useViewer=true;
	
	/**actual JPedal library*/
	private SimpleViewer PDFviewer;
	
	/** Current page number (first page is 1) */
	private int currentPage = 1;
	
	/**the outline window which is wrapped in SWT container*/
	private PDFOutline outlinePage;
	
	/**the outline window which is wrapped in SWT container*/
	private PDFSearch searchPage;

	/**SWT display component with pageNumber/totalPages*/
	private Label pageCount;
	
	// EREADER addition
	/** The EReader for this PDF editor. */
	protected EReader eReader;

	/**
	 * initial setup from Eclipse
	 */
	public PDFEditor() {

		super();

		if(!Activator.isBroken){

			if(PDFEditor.debug)
				System.out.println("PDFEditor Called1");

			IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();

			IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			if(activePage != null)
				activePage.setPerspective(reg.findPerspectiveWithId("org.jpedal.pdf.plugins.eclipse.perspective.PDFPerspective"));

			if(PDFEditor.debug)
				System.out.println("Called2");
		}
	}
	
	/**
	 * Kill the thread, that is performing the search, safely
	 *
	 */
	public void stopSearch(){

		if(useViewer){
		
			
			//done internally
			
		}else{
			if(pdfDecoder!=null)
				pdfDecoder.waitForDecodingToFinish();
		}
	}

	/**not needed us PDF but part of interface*/
	public void doSave(IProgressMonitor monitor) {}

	/**not needed us PDF but part of interface*/
	public void doSaveAs() {}

	/**setup path of PDF ready to use later*/
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {

		super.init(site,input);

		if(Activator.isBroken)
			return;

		setPartName(input.getName());
		
		if(debug)
			System.out.println("Init called ");
		
		/**get name and path of file*/
		if((input instanceof IPathEditorInput)){ //Support Backward compatibility - should not now be used
			IPathEditorInput fileInput=((IPathEditorInput)input);
			fileName=fileInput.getPath().toOSString();
		}else{ //Allow for 3.3 support
			FileStoreEditorInput fileInput=((FileStoreEditorInput)input);
			fileName=fileInput.getURI().getPath();
		}
		
		if(debug)
			System.out.println("fileName "+fileName);
	}

	/**not needed us PDF but part of interface*/
	public boolean isDirty() {
			return false;
	}
	
	/**not needed us PDF but part of interface*/
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * main routine to create the viewer
	 */
	protected void createPages() {

		if(Activator.isBroken)
			return;

		//check file exists
		boolean isEnabled=true;
		File checkFile=new File(fileName);
		if(!checkFile.exists()){

			System.out.println("file missing");
			isEnabled=false;
		}

		if(debug)
			System.out.println("createPages called");

		Composite container=this.getContainer();

		ToolBar toolbar;
		FormData data;
		Canvas canvas = null;
		
		/**
		 * setup all the GUI
		 */
		try{
			//Prevent pop ups before we create the user interface
			System.setProperty("org.jpedal.suppressViewerPopups", "true");
			
			viewer = new Composite(container, SWT.NONE);

			/**
			 * setup toolbar
			 */
			toolbar = new ToolBar(viewer, SWT.NONE);
			data = new FormData();
			data.top = new FormAttachment(0, 0);
			data.left = new FormAttachment(20, 0);
			toolbar.setLayoutData(data);

			canvas = new Canvas(viewer, SWT.NONE);
			data = new FormData();
			data.width = 24;
			if(useViewer)
				data.height = 0;
			else
				data.height = 24;
			
			data.top = new FormAttachment(0, 5);
			data.right = new FormAttachment(100, -5);
			canvas.setLayoutData(data);

			if(!useViewer){ //left for backward compatability
				/**
				 * add count and then buttons
				 */			
				pageCount = new Label(viewer, SWT.NONE);
				data = new FormData();
				data.width = 100;
				data.height = 24;
				data.left = new FormAttachment(toolbar, 0);			
				data.top = new FormAttachment(toolbar, -20);
				pageCount.setLayoutData(data);

				/**
				 * SWT buttons
				 **/
				ToolItem scaleOut =createSWTButton("/icons/smminus.gif","Zoom out of PDF",toolbar, isEnabled);
				if(isEnabled)
					scaleOut.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							setScale(2f/3f);
						}
					});

				ToolItem scaleIn =createSWTButton("/icons/smplus.gif","Zoom into PDF",toolbar, isEnabled);
				if(isEnabled)
					scaleIn.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							setScale(1.5f);
						}
					});

				ToolItem firstPage =createSWTButton("/icons/smstart.gif","Goto first page",toolbar, isEnabled);
				if(isEnabled)
					firstPage.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							firstPage();
						}
					});

				ToolItem fBack =createSWTButton("/icons/smfback.gif","Go back 10 pages",toolbar, isEnabled);
				if(isEnabled)
					fBack.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							movePage(-10);
						}
					});

				ToolItem back =createSWTButton("/icons/smback.gif","Go back 1 page",toolbar, isEnabled);
				if(isEnabled)
					back.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							movePage(-1);
						}
					});

				ToolItem forward =createSWTButton("/icons/smforward.gif","Go forward 1 page",toolbar, isEnabled);
				if(isEnabled)
					forward.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							movePage(1);
						}
					});

				ToolItem fforward =createSWTButton("/icons/smfforward.gif","Go forward 10 pages",toolbar,isEnabled);
				if(isEnabled)
					fforward.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							movePage(10);
						}
					});

				ToolItem lastPage =createSWTButton("/icons/smend.gif","Go to last page",toolbar, isEnabled);
				if(isEnabled)
					lastPage.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							lastPage();
						}
					});

				ToolItem shortcuts =createSWTButton("/icons/smabout.gif","InfoPopup box",toolbar, isEnabled);
				if(isEnabled)
					shortcuts.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							showInfo();
						}
					});


				ToolItem info =createSWTButton("/icons/smpdf.gif","Saved PDFs",toolbar, isEnabled); //smpdf crashes :-(
				info.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						showBookmarks();
					}
				});

				ToolItem rss =createSWTButton("/icons/rss.gif","RSS feed",toolbar, isEnabled);
				rss.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						RSSPopup rss = new RSSPopup(getSite().getShell());
						rss.open();
					}
				});

			}

			/**
			 * main display (Swing panel in SWT container
			 */
			
			//SWT bit
			viewer.setLayout(new FormLayout());

				
			data = new FormData();
			data.left = new FormAttachment(0, 0);
			data.top = new FormAttachment((Control)canvas, 5, SWT.DEFAULT);
			data.right = new FormAttachment(100, 0);
			data.bottom = new FormAttachment(100, 0);
			
			Composite browser = new Composite(viewer, SWT.EMBEDDED);
			browser.setLayoutData(data);
			
			/** setup GUI and wrapper around SWING component */
			frame = SWT_AWT.new_Frame(browser);
			
			//This panel is required in order to allow for mouse listeners
			Panel AWTDisplayPane = new Panel();
			frame.add(AWTDisplayPane);
			

			//reset in case file not opened
			numberOfPages=0;
			
			if(debug)
				System.out.println(">>about to open");
			
			/**
			 * setup JPedal instances
			 */
			if(useViewer){
				
				PDFviewer=new SimpleViewer(Values.RUNNING_PLUGIN); //Viewer does all setup for you				
				
				/**
				 * add a custom version of the Help command to over-ride original
				 */
				//the custom code to replace Info with custom code function
				JPedalActionHandler helpAction = new JPedalActionHandler() {
					public void actionPerformed(SwingGUI currentGUI, Commands commands) {
						new SwingInfoPopup().popupInfoBox(currentGUI.getFrame());
					}
				};
				
				//Map containing any commands - must exist in JPedal
				Map actions = new HashMap();
				actions.put(new Integer(Commands.INFO), helpAction);

				//tell JPedal to use it
				PDFviewer.addExternalHandler(actions, Options.JPedalActionHandler);

			}else{
				//Now open and decode the page
				pdfDecoder = new PdfDecoder();
			
				PdfDecoder.setFontReplacements(pdfDecoder);
				pdfDecoder.init(true);
				pdfDecoder.setExtractionMode(PdfDecoder.TEXT);
				
				//add a border and center
				pdfDecoder.setInset(5,5);
				pdfDecoder.setDisplayView(Display.SINGLE_PAGE,Display.DISPLAY_CENTERED);
				pdfDecoder.useHiResScreenDisplay(true);
		        
				pdfDecoder.setPDFBorder(BorderFactory.createLineBorder(Color.black, 1));
			}
			
			if(isEnabled){

				if(debug)
					System.out.println("open="+fileName);
				
				/**
				 * setup JPedal and open the first page
				 */
				if(useViewer){

					//get JPedal to use Eclipse as root container
					PDFviewer.setRootContainer(AWTDisplayPane);

					//we load a profile here called eclipse with all the menus/icons disabled except the display View ones
					PDFviewer.loadProperties("jar://org/jpedal/pdf/plugins/eclipse/res/eclipse.xml");
					
					//open the file
					PDFviewer.setupViewer();
					
					PDFviewer.openDefaultFile(fileName);

					//actual Eclipse display part
					frame.pack();					
					viewer.layout(true);
					addPage(viewer);
					
					numberOfPages=((Integer)PDFviewer.executeCommand(Commands.PAGECOUNT, null)).intValue();
					
					
					/**
					 * setup JPedal and open the first page
					 */
					if(outlinePage!=null){
						outlinePage.setPDFDecoder(this);
						outlinePage.setTree();
					}
					
				}else{
					pdfDecoder.openPdfFile(fileName);

					boolean fileCanBeOpened= false;
					if (pdfDecoder.isEncrypted() && !pdfDecoder.isFileViewable()) {

						InputDialog input = new InputDialog(getSite().getShell(),"Password","Enter a password","",null);
						input.open();

						String password = input.getValue();

						/** try and reopen with new password */
						if (password != null) {
							pdfDecoder.setEncryptionPassword(password);

							if (pdfDecoder.isFileViewable())
								fileCanBeOpened = true;
							else
								fileCanBeOpened = false;
						}

						if(!fileCanBeOpened)
							MessageDialog.openInformation(getSite().getShell(),"Password","No valid password");

					}else
						fileCanBeOpened=true; 

					if(fileCanBeOpened){

						numberOfPages=pdfDecoder.getPageCount();
						
						decodePage();

						if(outlinePage!=null){
							outlinePage.setPDFDecoder(this);
							outlinePage.setTree();
						}

						//complete display by adding our component to the display in a JScrollPane
						JScrollPane scrollPane = new JScrollPane();
						frame.add(scrollPane);
						
						scrollPane.getViewport().add(pdfDecoder);
						frame.pack();

						viewer.layout(true);
						addPage(viewer);
					}
				}
			}
			
			//set number of pages
			if(!useViewer)
			setPageCounter();

		}catch(Exception e){
			LogWriter.writeLog("Exception "+e+" in createPartControl >> PDFViewer");
			e.printStackTrace();
		}catch(Error e){
			LogWriter.writeLog("Error "+e+" in createPartControl >> PDFViewer");
			e.printStackTrace();
		}
		
		// EREADER addition: create the eReader for this PDF and configure the SimpleViewer
		eReader = new EReader(this);
		eReader.configureSimpleViewer();
	}
	
	public GUIThumbnailPanel getThumbnail() {
		
		final SwingOutline tree;
		
		if(useViewer){
			return (GUIThumbnailPanel) PDFviewer.executeCommand(Commands.GETTHUMBNAILPANEL, null);
		}else 
			return null;
	}
	
	public SwingOutline getOutline() {
		
		final SwingOutline tree;
		
		if(useViewer){
			return (SwingOutline) PDFviewer.executeCommand(Commands.GETOUTLINEPANEL, null);
		}else{
			Document XMLOutline = pdfDecoder.getOutlineAsXML();

			//System.out.println("setup tree"+XMLOutline);

			if (XMLOutline != null){

				Node rootNode = XMLOutline.getFirstChild();
				if (rootNode != null) {

					tree = new SwingOutline();
					tree.reset(rootNode);

					//Listen for when the selection changes - looks up dests at present
					((JTree) tree.getTree()).addTreeSelectionListener(new TreeSelectionListener() {

						/**
						 * Required by TreeSelectionListener interface*
						 */
						public void valueChanged(TreeSelectionEvent e) {

							if (tree.isIgnoreAlteredBookmark())
								return;

							DefaultMutableTreeNode node = tree.getLastSelectedPathComponent();

							if (node == null)
								return;


							/**
							 * get title and conver to ref if valid
							 **/
							 String title=(String)node.getUserObject();

							 JTree jtree = ((JTree) tree.getTree());

							 DefaultTreeModel treeModel = (DefaultTreeModel) jtree.getModel();

							 List flattenedTree = new ArrayList();

							 /** flatten out the tree so we can find the index of the selected node */
							 getFlattenedTreeNodes((TreeNode) treeModel.getRoot(), flattenedTree);
							 flattenedTree.remove(0); // remove the root node as we don't account for this

							 int index = flattenedTree.indexOf(node);

							 String ref = tree.convertNodeIDToRef(index);

							 /**
							  * and execute in either PdfDecoder or SimpleViewer
							  */
							 handleAction(ref);
							 
							 if(!useViewer)
							 setPageCounter();

						}
					});
					
					return tree;
				}				
			}				
		}
		
		return null;
	}
	
	public void decodePageFromOutline(int pageNumber, Point p) {

        if ((!isDecoding) && (this.currentPage != pageNumber)) {
        	currentPage = pageNumber;
            scale = 1f;
            pdfDecoder.setPageParameters(scale, currentPage);
            decodePage();
        }

        if (p != null)
            pdfDecoder.ensurePointIsVisible(p);

    }
	
	/**
     * modified version of gotoDest in DefaultActionHandler - if you use PDFdecoder
     * (if you use SimpleViewer you do not need to both with all this
     */
	public void handleAction(String Aref) {

		int pageToDisplay=-1;
		
		if(useViewer){
			//done internally
		}else{
			pageToDisplay=actionViaPdfDecoder(Aref);
			
			//open and ensure this point visible
			if (pageToDisplay != -1) {
				decodePageFromOutline(pageToDisplay, new Point(0,0));//tree.getPoint(title));
			}
			
		}
	}
	
	private int actionViaPdfDecoder(String Aref){
		
		final boolean debugDest=false;

		PdfObject aData=pdfDecoder.getOutlineData().getAobj(Aref);

		Point position=new Point(0,0);
		
		//aData can either be in top level of Form (as in Annots)
		//or second level (as in A/ /D - this allows for both
		//whoch this routine handles
		PdfObject a2=aData.getDictionary(PdfDictionary.A);
		if(a2!=null)
			aData=a2;

		//new page or -1 returned
		int page=-1;

		PdfArrayIterator Dest = aData.getMixedArray(PdfDictionary.Dest);
		if (Dest!=null) {

			//allow for it being an indirect named object and convert if so
			if(Dest.getTokenCount()==1){
				//					System.out.println("val="+ Dest.getNextValueAsString(false));

				String ref=pdfDecoder.getIO().convertNameToRef( Dest.getNextValueAsString(false));
				if(ref!=null){

					//can be indirect object stored between []
					if(ref.charAt(0)=='['){
						if(debugDest)
							System.out.println("data for named obj "+ref);

						byte[] raw=ref.getBytes();
						//replace char so subroutine works -ignored but used as flag in routine
						raw[0]= 0;

						ArrayDecoder objDecoder=new ArrayDecoder(pdfDecoder.getIO().getObjectReader(), 0, raw.length, PdfDictionary.VALUE_IS_MIXED_ARRAY,null, PdfDictionary.Names);
                        objDecoder.readArray(false, raw, aData, PdfDictionary.Dest);
                        
						Dest=aData.getMixedArray(PdfDictionary.Dest);

					}else{
						if(debugDest)
							System.out.println("convert named obj "+ref);

						aData=new OutlineObject(ref);
						pdfDecoder.getIO().readObject(aData);
						Dest=aData.getMixedArray(PdfDictionary.Dest);
					}
				}
			}

			String filename = aData.getTextStreamValue(PdfDictionary.F);

			if(filename==null){
				PdfObject fDic = aData.getDictionary(PdfDictionary.F);

				if(fDic!=null)
					filename = fDic.getTextStreamValue(PdfDictionary.F);
			}

			//add path if none present
			if(filename!=null && filename.indexOf('/')==-1 && filename.indexOf('\\')==-1)
				filename=pdfDecoder.getObjectStore().getCurrentFilepath()+filename;

			//if we have any \\ then replace with /
			if(filename!=null && filename.indexOf("\\")!=-1){
				//for some reason String.replaceAll didnt like "\\" so done custom
				int index = filename.indexOf("\\");
				while(index!=-1){
					filename = filename.substring(0,index)+
					"/"+filename.substring(index+("\\".length()),filename.length());
					index = filename.indexOf("\\");
				}

				//if we dont start with a /,./ or ../ or #:/ then add ./
				int slashIndex = filename.indexOf(":/");
				if(slashIndex==-1 || slashIndex>1){
					File fileStart = new File(pdfDecoder.getFileName());
					filename = fileStart.getParent()+"/"+filename;
				}
			}

			// new version - read Page Object to jump to
			String pageRef = "";

			if (Dest.getTokenCount() > 0){


				//get pageRef as number of ref
				int possiblePage=Dest.getNextValueAsInteger(false)+1;
				pageRef = Dest.getNextValueAsString(true);

				//convert to target page if ref or ignore

				if(pageRef.endsWith(" R"))
					page = pdfDecoder.getPageFromObjectRef(pageRef);
				else if(possiblePage>0){ //can also be a number (cant check range as not yet open)
					page=possiblePage;
				}

				if(debugDest)
					System.out.println("pageRef="+pageRef+" page="+page+" "+aData.getObjectRefAsString());

				//allow for named Dest
				if(page==-1){
					String newRef=pdfDecoder.getIO().convertNameToRef(pageRef);

					//System.out.println(newRef+" "+decode_pdf.getIO().convertNameToRef(pageRef+"XX"));

					if(newRef!=null && newRef.endsWith(" R"))
						page = pdfDecoder.getPageFromObjectRef(newRef);

				}
			}


			//read all the values
			if (Dest.getTokenCount()>1) {

				//get type of Dest
				//System.out.println("Next value as String="+Dest.getNextValueAsString(false)); //debug code to show actual value (note false so does not roll on)
				int type=Dest.getNextValueAsConstant(true);

				if(debugDest)
					System.out.println("Type="+PdfDictionary.showAsConstant(type));

				Integer scale = null;

				// - I have added all the keys for you and
				//changed code below. If you run this on baseline,
				//with new debug flag testActions on in DefaultAcroRender
				// it will exit when it hits one
				//not coded

				//type of Dest (see page 552 in 1.6Spec (Table 8.2) for full list)
				switch(type){
				case PdfDictionary.XYZ: //get X,y values and convert to rectangle which we store for later

					//get x and y, (null will return 0)
					float x=Dest.getNextValueAsFloat();
					float y=Dest.getNextValueAsFloat();

					//third value is zoom which is not implemented yet

					//create Rectangle to scroll to
					position.setLocation((int)x,(int)y);

					break;
				case PdfDictionary.Fit: //type sent in so that we scale to Fit.
					scale = new Integer(-3);//0 for width in scaling box and -3 to show its an index
					break;

				case PdfDictionary.FitB: 
					/*[ page /FitB ] - (PDF 1.1) Display the page designated by page, with its contents 
					 * magnified just enough to fit its bounding box entirely within the window both 
					 * horizontally and vertically. If the required horizontal and vertical magnification 
					 * factors are different, use the smaller of the two, centering the bounding box 
					 * within the window in the other dimension.
					 */
					//scale to same as Fit so use Fit.
					scale = new Integer(-3);//0 for width in scaling box and -3 to show its an index

					break;

				case PdfDictionary.FitH:
					/* [ page /FitH top ] - Display the page designated by page, with the vertical coordinate 
					 * top positioned at the top edge of the window and the contents of the page magnified 
					 * just enough to fit the entire width of the page within the window. A null value for 
					 * top specifies that the current value of that parameter is to be retained unchanged.
					 */
					//scale to width
					scale = new Integer(-1);//2 for width in scaling box and -3 to show its an index 

					//and then scroll to location
					float top=Dest.getNextValueAsFloat();

					//create Rectangle to scroll to
					position.setLocation((int)10,(int)top);


					break;

					/* [ page /FitV left ] - Display the page designated by page, with the horizontal 
					 * coordinate left positioned at the left edge of the window and the contents of 
					 * the page magnified just enough to fit the entire height of the page within the window. 
					 * A null value for left specifies that the current value of that parameter is to be 
					 * retained unchanged.
					 */

					/* [ page /FitR left bottom right top ] - Display the page designated by page, with its 
					 * contents magnified just enough to fit the rectangle specified by the coordinates left, 
					 * bottom, right, and topentirely within the window both horizontally and vertically. 
					 * If the required horizontal and vertical magnification factors are different, use 
					 * the smaller of the two, centering the rectangle within the window in the other 
					 * dimension. A null value for any of the parameters may result in unpredictable behavior.
					 */

					/* [ page /FitB ] - (PDF 1.1) Display the page designated by page, with its contents 
					 * magnified just enough to fit its bounding box entirely within the window both 
					 * horizontally and vertically. If the required horizontal and vertical magnification 
					 * factors are different, use the smaller of the two, centering the bounding box within 
					 * the window in the other dimension.
					 */

					/* [ page /FitBH top ] - (PDF 1.1) Display the page designated by page, with the vertical 
					 * coordinate top positioned at the top edge of the window and the contents of the page 
					 * magnified just enough to fit the entire width of its bounding box within the window. 
					 * A null value for top specifies that the current value of that parameter is to be retained 
					 * unchanged.
					 */
					/* [ page /FitBV left ] - (PDF 1.1) Display the page designated by page, with the horizontal 
					 * coordinate left positioned at the left edge of the window and the contents of the page 
					 * magnified just enough to fit the entire height of its bounding box within the window. 
					 * A null value for left specifies
					 */
				default:


				}
			}

		}

		return page;
	}
	
	/**convenience method to create our buttons*/
	private ToolItem createSWTButton(String iconPath, String toolTip, ToolBar toolbar, boolean isEnabled) {
		
		ToolItem item = new ToolItem(toolbar, SWT.PUSH);	

		Image icon = new Image(viewer.getDisplay(), getClass().getResourceAsStream(iconPath));
		item.setImage(icon);
		
		item.setToolTipText(toolTip);
		
		item.setEnabled(isEnabled);
		
		return item;
	}

	/**
	 * alter scaling and redraw
	 * @param rescale - old code used scaling 0-1 for 100% so have kept that
	 */
	private void setScale(float rescale) {

		float newFloat=scale*rescale;
		
		/**
		 * rescale if in sensible range
		 */
		if(newFloat>0.1f && newFloat<10){
			scale=scale*rescale;
			
			if(useViewer){
				//because it is a String wrapper on a float value (0,25,50,100) 100= 100% actual size
				try{
					PDFviewer.executeCommand(Commands.SCALING, new Object[]{""+(100*scale)});
				}catch(Exception ee){ //catch for wrong or unknown value
					ee.printStackTrace();
				}
				
			}else{
				pdfDecoder.setPageParameters(scale, currentPage); 
				pdfDecoder.updateUI();
				repaintPDF();
			}
		}
	}

	/**
	 * goto first page
	 */
	private void firstPage() {

        currentPage = 1;
        
        if(useViewer){
			PDFviewer.executeCommand(Commands.GOTO, new Object[]{""+currentPage});
		}else{
			decodePage();
		}
        
        //set number of pages
        if(!useViewer)
		setPageCounter();

    }

	/**
	 * goto last page
	 */
    public void lastPage() {

    	currentPage = numberOfPages;
    	
    	if(useViewer){
			PDFviewer.executeCommand(Commands.GOTO, new Object[]{""+currentPage});
		}else{
			decodePage();
		}
        
        //set number of pages
    	if(!useViewer)
		setPageCounter();


    }

    /**
     * goto another page
     */
    private void movePage(int change) {
        int newPage = currentPage + change;

        
    	if ((change < 0 && newPage > 0)|| newPage <= numberOfPages) {
    		
    		currentPage = newPage;
            
    		System.out.println(newPage+" "+currentPage);
            
    		if(useViewer){
    			PDFviewer.executeCommand(Commands.GOTO, new Object[]{""+newPage});    	            	
    		}else{
    			pdfDecoder.clearHighlights();	            	
                decodePage();
    		}
    		
    		//set number of pages
    		if(!useViewer)
    		setPageCounter();           
    	}   	    	
    }

    /**
	 * pages in this file (first page is 1)
	 * (there is an internal eclipse method called getPageCount() 
	 * so do not over-ride
	 */
    public int getPDFPageCount() {

    	return this.numberOfPages;
	}
    
	/**
	 * make currently displayed page available for other views to access
	 */
    public int getPage() {
    	if(PDFEditor.useViewer)
			return ((Integer)PDFviewer.executeCommand(Commands.CURRENTPAGE, null)).intValue();
		else
			return currentPage;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.setFocus();
		if(!useViewer)
		pdfDecoder.repaint();
	}

	/**
	 * allow external access to PDfDecoder for Search access
	 * @return
	 */
	public PdfDecoder getPDF() {
		return pdfDecoder;
	}

	private void showInfo(){

		InfoPopup info = new InfoPopup(getSite().getShell());
		info.open();
	}
	
	private void showBookmarks(){

		BookmarkPopup bookmarks = new BookmarkPopup(getSite().getShell());
		bookmarks.open();
	}

	/**
	 * part of interface used to handle our PDF outline
	 */
	public Object getAdapter(Class key){


		if(debug)
			System.out.println("Get adapter called with "+key);

		if(key.equals(IContentOutlinePage.class)&& !Activator.isBroken){

			if(outlinePage == null)
				outlinePage = new PDFOutline(this);
			return outlinePage;
		}

		return super.getAdapter(key);
	}

	/**
	 * set page number from search or outline and decode/display new page
	 */
	public void setPage(final int newPage) {

		if (newPage>0 && newPage <numberOfPages) {
			currentPage = newPage;
			
			if(useViewer){
				PDFviewer.executeCommand(Commands.GOTO, new Object[]{""+newPage});
			}else{
				decodePage();
			}
			if(!useViewer)
			setPageCounter();
		}
	}
	
	/**
	 * set page number from search or outline and decode/display new page
	 */
	public void setPage(final int newPage, float scaling) {

		if (newPage>0 && newPage <numberOfPages) {
			currentPage = newPage;
			
			setScale(scaling); //update as may change
			
			if(useViewer){
				PDFviewer.executeCommand(Commands.GOTO, new Object[]{""+newPage});
			}else{
				decodePage();
			}
			if(!useViewer)
			setPageCounter();
		}
	}
	
	/**
	 * update page number display
	 */
	public void setPageCounter() {

		PlatformUI.getWorkbench().getDisplay().syncExec(
				new Runnable() {
					public void run(){
						if(useViewer){
							//done internally
						}else
							pageCount.setText(currentPage+"/"+numberOfPages);	
					}
				}
				
		);
    }
	
	/**
	 * ensure redrawn
	 */
	public void repaintPDF() {
		if(!useViewer){
			pdfDecoder.invalidate();
			pdfDecoder.updateUI();
		}
        frame.repaint();
    }

    /**
     * decode page and display
     **/
    private void decodePage() {

    	pdfDecoder.clearHighlights();
    	
        scale=1f;
        /**
         * workout optimum scaling
         */
        PdfPageData pageData = pdfDecoder.getPdfPageData();
        int inset=10;
        int cw,ch,rotation=pageData.getRotation(currentPage);
        if(rotation==90 || rotation==270){
            cw = pageData.getCropBoxHeight(currentPage);
            ch = pageData.getCropBoxWidth(currentPage);
        }else{
            cw = pageData.getCropBoxWidth(currentPage);
            ch = pageData.getCropBoxHeight(currentPage);
        }

        //define pdf view width and height
        float width = (float) (pdfDecoder.getWidth()-inset-inset);
        float height = (float) (pdfDecoder.getHeight()-inset-inset);

        if((width>0)&&(height>0)){
            float x_factor=0,y_factor=0;
            x_factor = width / cw;
            y_factor = height / ch;

            if(x_factor<y_factor)
                scale = x_factor;
            else
                scale = y_factor;
        }

		pdfDecoder.setPageParameters(scale, currentPage);

        //repaintPDF();
        
        if (!isDecoding) {
            isDecoding = true;

            //Thread pageDecoder = new Thread() {
                //public void run() {
                    try {

                        pdfDecoder.decodePage(currentPage);
                        
                        repaintPDF();

                        isDecoding = false;
                    } catch (Exception e) {
                        isDecoding = false;
                        e.printStackTrace();
                    }
                //}
            //};

            //pageDecoder.start();
        }

    }

    /**
     * make it available externally (ie for Search outline)
     */
	public float getScale() {
		return scale;
	}

	public Object getSearchWindow() {
		
		return searchPage;
	}

	public void setSearchWindow(PDFSearch search) {
		searchPage=search;
		
	}

	public boolean isOpen() {
		return pdfDecoder!=null || PDFviewer!=null;
	}

	/**
	 * highlight an area on screen (assumes current page)
	 * @param highlight
	 * @param nextPage 
	 */
	public void setHighlight(final Rectangle highlight,final int nextPage) {

		if(useViewer){
			
			PlatformUI.getWorkbench().getDisplay().syncExec(
					new Runnable() {
						public void run(){

							PDFviewer.executeCommand(Commands.HIGHLIGHT, new Object[]{new Rectangle[]{highlight},new Integer(nextPage)});

							PDFviewer.executeCommand(Commands.SCROLL, new Rectangle[]{highlight});
						}
					});
		}else{
			//draw rectangle
			final int scrollInterval = pdfDecoder.getScrollInterval();
			//previous one to revert back to but other more accurate
			//		decode_pdf.scrollRectToVisible(new Rectangle((int)((highlight.x*scaling)+scrollInterval),(int)(mediaGUI.cropH-((highlight.y-currentGUI.cropY)*scaling)-scrollInterval*2),scrollInterval*4,scrollInterval*6));

			int inset=5;//decode_pdf.setInset();.getPDFDisplayInset();

			PdfPageData page=pdfDecoder.getPdfPageData();
			final int x = (int)((highlight.x-page.getCropBoxX(nextPage))*scale)+inset;
			final int y = (int)((page.getCropBoxHeight(nextPage)-(highlight.y-page.getCropBoxY(nextPage)))*scale)+inset;
			final int w = (int)(highlight.width*scale);
			final int h = (int)(highlight.height*scale);

			PlatformUI.getWorkbench().getDisplay().syncExec(
					new Runnable() {
						public void run(){

							Rectangle scrollto = new Rectangle(x-scrollInterval,y-h-scrollInterval,w+scrollInterval*2,h+scrollInterval*2);

							pdfDecoder.scrollRectToVisible(scrollto);

							pdfDecoder.clearHighlights();

							//As we use text coords in search we do not need to align this area
							//to the text as it is the text area for the search result.
							pdfDecoder.addHighlights(new Rectangle[]{highlight},true,nextPage);

							pdfDecoder.invalidate();
							pdfDecoder.repaint();

							pdfDecoder.validate();

						}
					});

		}
	}
	
	private void getFlattenedTreeNodes(TreeNode theNode, List items) {
		// add the item
		items.add(theNode);
	
		// recursion
		for (Enumeration theChildren = theNode.children(); theChildren.hasMoreElements();) {
			getFlattenedTreeNodes((TreeNode) theChildren.nextElement(), items);
		}
	}

	/**
	 * used by search to remove highlights
	 */
	public void clearSearchHighlights() {
		
		if(useViewer){
			this.PDFviewer.executeCommand(Commands.HIGHLIGHT,null);
		}else
			pdfDecoder.clearHighlights();	
	}

	/**
	 * raw text for the page
	 * @param i
	 * @return
	 * @throws Exception 
	 */
	public PdfGroupingAlgorithms getPdfGroupingObject(int i) throws Exception {
		
		if(useViewer){
			return (PdfGroupingAlgorithms) PDFviewer.executeCommand(Commands.PAGEGROUPING, new Object[]{new Integer(i)});
		}else{
			if(i==getPage())
				return pdfDecoder.getGroupingObject();
			else{
				pdfDecoder.decodePageInBackground(i);
				return pdfDecoder.getBackgroundGroupingObject();
			}
		}
	}

	public Rectangle getPageRectangle() {
		// TODO Auto-generated method stub
		return null;
	}

	// EREADER addition
	/**
	 * Returns the SimpleViewer for this PDF editor.
	 */
	public SimpleViewer getSimpleViewer() {
		return PDFviewer;
	}

	// EREADER addition
	/** 
	 * Returns the EReader for this PDF editor. 
	 */
	public EReader getEReader() {
		return eReader;
	}
	
}
