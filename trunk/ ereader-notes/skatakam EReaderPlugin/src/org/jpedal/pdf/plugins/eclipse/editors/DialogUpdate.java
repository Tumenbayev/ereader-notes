package org.jpedal.pdf.plugins.eclipse.editors;


import java.io.BufferedReader;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

import org.eclipse.swt.widgets.*;

import org.jpedal.PdfDecoder;
import org.jpedal.pdf.plugins.eclipse.settings.PDFSettings;
import org.jpedal.utils.BrowserLauncher;

public class DialogUpdate extends Dialog{
	
	//IDs 
	final private int SupportID=-1;
	final private int ContinueID=-2;
	
    private Composite dialogArea;
	private Shell shell;
    
    public DialogUpdate(Shell parentShell){
        super(parentShell);
        
        this.shell=parentShell;
    }
    
    private String availableVersion;
	private String currentVersion;
    private BufferedReader in;

	public DialogUpdate(Shell parentShell, BufferedReader in, String currentVersion, String availableVersion){

		super(parentShell);
		
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
			
		this.currentVersion = currentVersion;
		this.availableVersion = availableVersion;
		this.in=in;
		
	}
	
	protected Control createDialogArea(Composite parent){

		try{
			dialogArea = (Composite) super.createDialogArea(parent);
			
			FormLayout layout = new FormLayout();
			layout.marginBottom = 2;
			layout.marginTop = 2;
			layout.marginLeft = 5;
			layout.marginRight = 5;

			dialogArea.getShell().setText("JPedal PDFviewer Plugin Update Info");
			dialogArea.setLayout(layout);
			
			int boldSize=12,stdSize=10;
			
			if(PdfDecoder.isRunningOnMac){
				boldSize=14;
				stdSize=12;
				
			}

			Font bold=new Font(null, "SansSerif" ,boldSize , SWT.BOLD);
    		Font std=new Font(null, "SansSerif" ,stdSize , SWT.NORMAL);
    		
    		addLabel(5, 15, bold, "A new version of this plugin is available\n(this message will appear only once for each new update).");
			
    		addLabel(5, 70, std, "Your current version:");
    		addLabel(50, 70, std, currentVersion);
    		addLabel(5, 85, std, "Available version:");
    		addLabel(50, 85, std, availableVersion);
    		
    		addLabel(5, 130, bold, "New in this release");
    		
    		//read info from file and add to display
            String nextLine="";
            int h=150;
            try {
            
	            while(true){
	                nextLine=in.readLine();
	
	                if(nextLine==null || nextLine.startsWith("version="))
	                    break;
	            
	                addLabel(5, h, std, nextLine);
	                h=h+15;
	        		
	            }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            Button disableCheck =new Button(dialogArea,SWT.CHECK);
            disableCheck.setText("Check enabled");
            disableCheck.setSelection(PDFSettings.checkForUpdates);
            FormData data = new FormData();
			data.left = new FormAttachment(5, 0);
			data.top = new FormAttachment(0, 298);
			disableCheck.setLayoutData(data);
			disableCheck.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent event){
					PDFSettings.checkForUpdates=!PDFSettings.checkForUpdates;
                    if(!PDFSettings.checkForUpdates){
                    	MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
    					messageBox.setMessage("Plugin will no longer check for new updates");
    					messageBox.setText("Confirm reset");
    					PDFSettings.saveValuesInEclipse();
                    }   
				}
			});
			
			//needed to setsize
			getShell().setMinimumSize(500,300);

			dialogArea.layout(true);

		}catch(Exception e){
			e.printStackTrace();
		}catch(Error e){
			e.printStackTrace();
		}
		return dialogArea;
	}
	
	/**
	 * standard way to access button actions
	 */
	protected void buttonPressed(int id){
		if(id==SupportID){
			
			MessageBox box = new MessageBox(getShell(),SWT.OK);
            box.setMessage("Viewer will try to open page in browser - there may be a slight delay");
            box.open();
            
			try {
				BrowserLauncher.openURL("http://www.jpedal.org/support.php");
			} catch (Exception e1) {

				ErrorPopup error = new ErrorPopup(shell);
				error.open();

				//<start-full><start-demo>
				e1.printStackTrace();
				//<end-demo><end-full>
			}
		}else if(id==ContinueID){
			close();
		}
	}
	
	/**
	 * our 2 buttons to click
	 */
	protected void createButtonsForButtonBar(Composite parent){
		
		this.createButton(parent, SupportID, "Support", false);
		this.createButton(parent, ContinueID, "Continue", true);
	}

	/**
	 * convenience method to take hassle out of adding a label
	 * @param w
	 * @param h
	 * @param font
	 * @param message
	 */
	private void addLabel(int w, int h, Font font, String message) {
		Label text1 = new Label(dialogArea,SWT.LEFT_TO_RIGHT);
		text1.setText(message);
		text1.setFont(font);
		FormData data = new FormData();
		data.left = new FormAttachment(w, 0);
		data.top = new FormAttachment(0, h);
		text1.setLayoutData(data);
	}
}