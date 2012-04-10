package org.jpedal.pdf.plugins.eclipse.editors;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.jpedal.examples.simpleviewer.SimpleViewer;
import org.jpedal.pdf.plugins.eclipse.editors.DialogUpdate;
import org.jpedal.pdf.plugins.eclipse.Activator;
import org.jpedal.pdf.plugins.eclipse.settings.PDFSettings;

/**
 * provides popup window to list PDFs and allow selection, in Plugin or external viewer
 */
public class BookmarkPopup extends Dialog{

	/**choose if we open in JPedal or Eclipse*/
	private static boolean useExternalWindow=false;

	final public static String separator = System.getProperty("file.separator");

	Table filelist;

	String file, description;

	/**color coding for files to show status*/
	final String[] strs={"Local File","Cached URL","Uncached URL"};
	Color[] cols=new Color[3];

	/**transient data*/
	PDFSettings settings;

	final private HashMap match = new HashMap();

	private Composite dialogArea;

	Shell shell;

	/**store for downloaded URLs*/
	public static String PDFcache="";
	
	public BookmarkPopup(Shell parentShell){
		super(parentShell);
		
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		
		
		/**
         * test for any updates and tell user if new version available
         */
        if(PDFSettings.checkForUpdates)
            checkForUpdates(true);

//        if (listModel.getSize() > 0) {
//            filelist.setSelectedIndex(0);
//            filelist.grabFocus();
//        }

		this.shell=parentShell;
		
		//define colors
		Display display = shell.getDisplay();
	    cols[0] = display.getSystemColor(SWT.COLOR_BLUE);
	    cols[1] = display.getSystemColor(SWT.COLOR_BLACK);
	    cols[2] = display.getSystemColor(SWT.COLOR_DARK_GRAY);
	    
	    
		//thanks to Sam for the idea
		try{
			PDFcache = Activator.plugin.getDefault().getStateLocation().toFile().getAbsolutePath();
			if(!PDFcache.endsWith(separator))
				PDFcache=PDFcache+separator;
		}catch(Exception e){
			e.printStackTrace();
		}
			
			settings=new PDFSettings();
	}
	
	 private boolean checkForUpdates(boolean alertUser) {

		 
	        boolean connectionSuccessful = true;
			boolean wasUpdateAvailable = false;

			try {

	            //read the available version from a file on the server
	            URL versionFile = new URL("http://www.jpedal.org/version_eclipse.txt");
	            URLConnection connection = versionFile.openConnection();

	            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
	            String availableVersion = in.readLine();

	            if(availableVersion.startsWith("version="))
	                availableVersion=availableVersion.substring(8);
	            
	            if(PDFSettings.checkForUpdates(availableVersion)){ // we have a later version

	            	DialogUpdate update = new DialogUpdate(shell, in, PDFSettings.version, availableVersion);
	                update.open();
	                wasUpdateAvailable = true;
	                
	            }

	            in.close();

			} catch (Exception e) {
				connectionSuccessful = false;
				//<start-full><start-demo>
				e.printStackTrace();
				//<end-demo><end-full>
			} finally {
//				if(!connectionSuccessful && showMessages){
//					currentGUI.showMessageDialog("Error making connection so unable to check for updates", "Error", JOptionPane.ERROR_MESSAGE);
//				}
			}

			 
			return wasUpdateAvailable;
		}

	public int open(){

		int condition=super.open();

		if(condition==0 && file!=null)
			openPDF(file);

		return condition;
	}

	private void openPDF(final String path){

		if(path.startsWith("http://")){ //if URL copy onto system first

			final String userDir = PDFcache+"downloadedPDFs"+separator;

			//get name from path less .pdf and full path
			int ptr=path.lastIndexOf("/")+1;

			final String name=path.substring(ptr);

			final String fullPath=userDir+separator+name;

			//check our cache exists
			File store=new File(userDir);
			if(!store.exists())
				store.mkdirs();

			//download if not stored or load from cache
			final File tmpFile= new File(fullPath);
			if(!tmpFile.exists()){ //download file and tell user

				try {
					new ProgressMonitorDialog(shell).run(true, true, new CacheURLonDisk(name, path, fullPath, tmpFile));
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					tmpFile.delete();
					MessageDialog.openError(shell, "Error", e.getMessage());
				} catch (InterruptedException e) {
					e.printStackTrace();
					tmpFile.delete();
					MessageDialog.openInformation(shell, "Cancelled", e.getMessage());
				}

				if(tmpFile.exists()){

					if(tmpFile!=null){
						if(useExternalWindow){

							//note needs thread
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									SimpleViewer viewer=new SimpleViewer();
									SimpleViewer.exitOnClose=false;
									//System.out.println("Open "+tmpFile.getPath());
									viewer.setupViewer();
									viewer.openDefaultFile(tmpFile.getPath());
								}
							});
						}else
							openFileInEditorWindow(tmpFile.getPath());
					}
				}

			}else{ //URL cached on disk so load from there

				//System.out.println("Cached URL fullPath="+fullPath+"\ntmpFile="+tmpFile+"\nfile="+file);

				if(tmpFile!=null){
					if(useExternalWindow){

						//note needs thread
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								SimpleViewer viewer=new SimpleViewer();
								viewer.setupViewer();
								SimpleViewer.exitOnClose=false;
								//System.out.println("2open "+fullPath);
								viewer.openDefaultFile(fullPath);
							}
						});

					}else
						openFileInEditorWindow(fullPath);
				}
			}

		}else{ //file on disk

			if(file!=null) {
				if(useExternalWindow){

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							SimpleViewer viewer=new SimpleViewer();
							SimpleViewer.exitOnClose=false;
							viewer.setupViewer();
							viewer.openDefaultFile(path);
						}
					});
				}else{
					openFileInEditorWindow(path);
				}
			}
		}
	}

	private void openFileInEditorWindow(final String path) {
		
		File fileToOpen = new File(path);
		 
		if (fileToOpen.exists() && fileToOpen.isFile()) {
		    IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
		    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		 
		    try {
		        IDE.openEditorOnFileStore( page, fileStore );
		    } catch ( PartInitException e ) {
		      e.printStackTrace();
		    }
		} else {
			MessageDialog.openError(shell, "File not found", "Cannot open file "+path);
		}
	}

	private void resetList() {

		filelist.removeAll();
		
		
		TableColumn column1 = new TableColumn(filelist, SWT.NONE);
		
		int count = settings.getFileCount();
		String desc,path,cache;
		for(int index=0;index<count;index++){
			
			desc=settings.getValue(index,PDFSettings.DESCRIPTION);
			path=settings.getValue(index, PDFSettings.PATH);
			cache=settings.getValue(index, PDFSettings.CACHEDFILE);
		    
			setListValue(path,desc, cache);
            	
			match.put(settings.getValue(index,PDFSettings.DESCRIPTION),settings.getValue(index,PDFSettings.PATH));
		}
		
		column1.pack();

	}

	/**
	 * add text and set color as appropriate
	 */
	private void setListValue(String path, String text,String cache) {
		
		TableItem item = new TableItem(filelist, SWT.NONE);
		item.setText(text);
		
		int i=0;
		
		if(path!=null && path.startsWith("http:")){
		    i=2;

		    if(cache==null){
		    	final String userDir = PDFcache+"downloadedPDFs"+separator;

				//get name from path less .pdf and full path
				int ptr=path.lastIndexOf("/")+1;

				final String name=path.substring(ptr);

				final String fullPath=userDir+separator+name;

				final File tmpFile= new File(fullPath);
				if(tmpFile.exists())
					i=1;
		    }else
		    	i=1;

		}
		
		item.setForeground(cols[i]);
		
	}

	protected Control createDialogArea(Composite parent){

		try{
			dialogArea = (Composite) super.createDialogArea(parent);
			
			FormLayout layout = new FormLayout();
			layout.marginBottom = 2;
			layout.marginTop = 2;
			layout.marginLeft = 5;
			layout.marginRight = 5;

			dialogArea.getShell().setText("JPedalPDF Bookmark selector");
			dialogArea.setLayout(layout);

			Label topInfo=new Label(dialogArea,SWT.PUSH);
			topInfo.setText("Select a file from the bookmarks below:");
			topInfo.setAlignment(SWT.CENTER);

			FormData data = new FormData();
			data.left = new FormAttachment(0, 0);
			data.top = new FormAttachment(0, 5);
			topInfo.setLayoutData(data);

			filelist=new Table(dialogArea,SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);	
			   

			resetList(); //PDF list of files
			data = new FormData();
			data.left = new FormAttachment(30, 0);
			data.top = new FormAttachment(10, 0);
			data.right = new FormAttachment(100, 0);
			data.bottom = new FormAttachment(90, 0);
			filelist.setLayoutData(data);
			
			if(filelist.getItemCount()>0){
				filelist.setSelection(0);
				
				//set file to open this
				TableItem[] files=filelist.getSelection();
				file=(String) match.get(files[0].getText());
			}
			
			filelist.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent event){
					TableItem[] files=filelist.getSelection();
					if(files!=null && files.length>0)
					file=(String) match.get(files[0].getText());
				}
			});

			Button add=new Button(dialogArea,SWT.PUSH);
			add.setText("Add file");
			data = new FormData();
			data.left = new FormAttachment(5, 0);
			data.top = new FormAttachment(10, 0);
			data.right = new FormAttachment(20, 0);
			data.bottom = new FormAttachment(20, 0);
			add.setLayoutData(data);
			add.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent event){
					FileDialog fc = new FileDialog(getShell(), SWT.OPEN);
					fc.setText("Choose PDF file to add");
					fc.setFilterExtensions(new String[]{"*.pdf"});
					fc.setFilterNames(new String[]{"PDF files (pdf)"});
					file=fc.open();

					if(file!=null){

						description=file;
						
						//get name from path less .pdf and full path
						int ptr=description.lastIndexOf("/")+1;
						if(ptr==0)
							ptr=description.lastIndexOf("\\")+1;

						if(ptr>0)
						description=description.substring(ptr);

						
						InputDialog dialog = new InputDialog(getShell(),"PDF description","Add a description of just use file name",description,null);
						if(dialog.open() == Window.OK){	

							description=dialog.getValue();
							
							
							if(!file.toLowerCase().endsWith(".pdf"))
								file=file+".pdf";

							settings.addPDFFile(file,description,null);
							setListValue(file, description, null);
							
							filelist.setSelection(filelist.getItemCount()-1);
				            
							match.put(description, file);
						}
					}
				}
			});

			Button addURL=new Button(dialogArea,SWT.PUSH);
			addURL.setText("Add URL");
			data = new FormData();
			data.left = new FormAttachment(5, 0);
			data.top = new FormAttachment(20, 0);
			data.right = new FormAttachment(20, 0);
			data.bottom = new FormAttachment(30, 0);
			addURL.setLayoutData(data);
			addURL.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent event){

					InputDialog dialog = new InputDialog(getShell(),"PDF URL","Please enter the pdf URL:","Type URL in here",null);
					if(dialog.open() == Window.OK){	

						file=dialog.getValue();
						//get file name
						String[] bits;
						bits = file.split("/");

						if(bits!=null)
							description = bits[bits.length-1];
						else
							description ="Unknown";

						InputDialog dialog2 = new InputDialog(getShell(),"PDF URL","Please enter a description:",description,null);
						if(dialog2.open() == Window.OK){	

							description=dialog2.getValue();

							settings.addPDFFile(file,description,null);
							setListValue(file, description, null);
							filelist.setSelection(filelist.getItemCount()-1);
				            
				            
							match.put(description,file.toString());
						}
					}
				}
			});

			Button remove=new Button(dialogArea,SWT.PUSH);
			remove.setText("Remove");
			data = new FormData();
			data.left = new FormAttachment(5, 0);
			data.top = new FormAttachment(30, 0);
			data.right = new FormAttachment(20, 0);
			data.bottom = new FormAttachment(40, 0);
			remove.setLayoutData(data);
			remove.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent event){
					if(filelist.getItemCount()>0){
						int idx=filelist.getSelectionIndex();
						if(idx!= -1){

							//String[] itemKeys=filelist.getSelection();
							String itemKey=null;//itemKeys[0];

							MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.YES | SWT.NO);
							messageBox.setMessage("Are you sure you want to remove\n"+itemKey);
							messageBox.setText("Confirm delete");
							int response = messageBox.open();
							if (response == SWT.YES){

								/* order of deletion is important here */                          	
								settings.deletePDFFile(match.get(itemKey).toString());
								match.remove(itemKey);
								filelist.remove(idx);
							}
						}else{
							MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.YES | SWT.NO);
							messageBox.setMessage("If you want to remove an item from the list make sure there is one selected");
							messageBox.setText("No file selected");
							int response = messageBox.open();
						}
					}
				}
			});

			Button reset=new Button(dialogArea,SWT.PUSH);
			reset.setText("Reset");
			data = new FormData();
			data.left = new FormAttachment(5, 0);
			data.top = new FormAttachment(40, 0);
			data.right = new FormAttachment(20, 0);
			data.bottom = new FormAttachment(50, 0);
			reset.setLayoutData(data);
			reset.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent event){

					MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.YES | SWT.NO);
					messageBox.setMessage("Are you sure you want to lose all your changes and reset the list and cache.");
					messageBox.setText("Confirm reset");
					int response = messageBox.open();

					/**reset back to default settings*/
					if (response == SWT.YES){

						//delete all files in cache
						int fileCount=settings.getFileCount();
						for(int jj=0;jj<fileCount;jj++)
							settings.deletePDFFile(settings.getValue(0, PDFSettings.PATH));

						settings.resetValuesToDefaultValues();
						resetList();
						
						file=null;
					}
				}
			});

			Button external=new Button(dialogArea,SWT.CHECK);
			external.setText("External");
			external.setSelection(useExternalWindow);
			data = new FormData();
			data.left = new FormAttachment(6, 0);
			data.top = new FormAttachment(52, 0);
			external.setLayoutData(data);
			external.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent event){
					useExternalWindow=!useExternalWindow;
				}
			});

			//key of values
			Composite colkey = new Composite(dialogArea, SWT.BORDER);
			colkey.setLayout(new FormLayout());
			data = new FormData();
			data.left = new FormAttachment(5, 0);
			data.top = new FormAttachment(60, 5);
			data.right = new FormAttachment(25, 0);
			data.bottom = new FormAttachment(88, 0);
			colkey.setLayoutData(data);

			Label keyTitle=new Label(colkey,SWT.HORIZONTAL);
			keyTitle.setText("Color key");
			keyTitle.setAlignment(SWT.CENTER);

			data = new FormData();
			data.left = new FormAttachment(0, 0);
			data.top = new FormAttachment(0, 0);
			keyTitle.setLayoutData(data);

			for(int ii=0;ii<strs.length;ii++){
				Label key=new Label(colkey,SWT.HORIZONTAL);
				key.setText(strs[ii]);
				key.setFont(new Font(null, "SansSerif",8 , SWT.BOLD));
				key.setForeground(new Color(null,cols[ii].getRed(),cols[ii].getGreen(),cols[ii].getBlue()));

				key.setAlignment(SWT.LEFT);

				data = new FormData();
				data.left = new FormAttachment(5, 0);
				data.top = new FormAttachment(24+(ii*20), 0);
				key.setLayoutData(data);
			}

			Label bottomInfo=new Label(dialogArea,SWT.PUSH);
			bottomInfo.setText("Also on FileMenu or keyboard shortcut ctrl shift D");
			bottomInfo.setAlignment(SWT.CENTER);

			data = new FormData();
			data.left = new FormAttachment(0, 0);
			data.top = new FormAttachment(92, 0);
			bottomInfo.setLayoutData(data);

			//needed to setsize
			getShell().setMinimumSize(450,300);

			dialogArea.layout(true);

		}catch(Exception e){
			e.printStackTrace();
		}catch(Error e){
			e.printStackTrace();
		}
		return dialogArea;
	}

	/**
	 * cacheURLondisk
	 */
	class CacheURLonDisk implements IRunnableWithProgress {

		private String path,name, fullPath;

		private File tmpFile;

		//pass in all the values used
		public CacheURLonDisk(String name, String path, String fullPath, File tmpFile) {

			this.name=name;
			this.path=path;
			this.fullPath=fullPath;
			this.tmpFile=tmpFile;
		}

		/**
		 * download file and save on disk
		 */
		public void run(IProgressMonitor monitor) throws InvocationTargetException,
		InterruptedException {

			monitor.beginTask("Downloading file - read 0 bytes (file will be cached for future use)",0);

			boolean userStopped=false;

			int flag=-1;

			try {
				int count=0,update=0;
				URL url;

				url = new URL(path);

				InputStream is = url.openStream();
				FileOutputStream fos=new FileOutputStream(tmpFile);
				
				int value;
				while ((value =is.read()) != -1){
					fos.write(value);

					if(update>1000){
						if(count<1024)
							monitor.beginTask("Downloading file ="+count+" bytes (file will be cached for future use)",flag);
						else if(count>1024*1024)
							monitor.beginTask("Downloading file ="+((10*count)/(1024*1024))/10f+" M (file will be cached for future use)",flag);
						else
							monitor.beginTask("Downloading file ="+count/(1024)+" K (file will be cached for future use)",flag);
						update=0;
					}
					update++;
					count++;

					if (monitor.isCanceled()){
						userStopped=true;
						break;
					}
				}

				is.close();
				fos.close();

			} catch (Exception e) {
				MessageDialog.openError(shell, "Problem with "+name, e.getMessage());
			}

			// store location or clear up stopped download
			if(!userStopped){
				int fileCount=settings.getFileCount();
				for(int ii=0;ii<fileCount;ii++){
					String PDFpath=settings.getValue(ii, PDFSettings.PATH);
					if(path.equals(PDFpath)){
						settings.setValue(ii, fullPath, PDFSettings.CACHEDFILE);
						//System.out.println("Match on "+PDFpath+" saved at "+fullPath);
					}
				}
			}else{
				tmpFile.delete();
			}

			monitor.done();

		}
	}
}