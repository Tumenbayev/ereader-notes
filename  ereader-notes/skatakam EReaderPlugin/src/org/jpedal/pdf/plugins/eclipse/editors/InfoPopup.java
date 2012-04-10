package org.jpedal.pdf.plugins.eclipse.editors;


import java.io.IOException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.jpedal.PdfDecoder;
import org.jpedal.pdf.plugins.eclipse.settings.PDFSettings;
import org.jpedal.utils.BrowserLauncher;

public class InfoPopup extends Dialog{
	
	
    private Composite dialogArea;
	private Shell shell;
    
    public InfoPopup(Shell parentShell){
        super(parentShell);
        
        this.shell=parentShell;
    }

    protected Control createDialogArea(Composite parent){
    	
    	try{
    		
    		dialogArea = (Composite) super.createDialogArea(parent);
    		dialogArea.getShell().setText("IDR Solutions - Contact Us");
    		
    		dialogArea.setLayout(new FormLayout());
    		
    		//load image
    		Image image = new Image(shell.getDisplay(),
    				getClass().getResourceAsStream("/icons/ceo.jpg"));
			
    		//put in widget to display
    		Label button = new Label(dialogArea,SWT.BORDER);
    		button.setImage(image);
    		FormData data = new FormData();
			data.left = new FormAttachment(5, 0);
			data.top = new FormAttachment(0, 5);
			button.setLayoutData(data);
			
			Button message=new Button(dialogArea,SWT.PUSH);
			message.setText("JPedal library from www.jpedal.org");
			message.setAlignment(SWT.CENTER);

			data = new FormData();
			data.left = new FormAttachment(10,0);
			data.top = new FormAttachment(0,190);
			message.setLayoutData(data);
			message.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent event){
					try {
    					BrowserLauncher.openURL("http://www.jpedal.org");
    				} catch (IOException e1) {
    					MessageDialog.openError(shell, "Unable to launch browser","Cannot launch Brower in separate process");
    				}
				}
			});
			
			Label version=new Label(dialogArea,SWT.PUSH);
			version.setText(PDFSettings.version);
			version.setAlignment(SWT.CENTER);

			data = new FormData();
			data.left = new FormAttachment(25, 0);
			data.top = new FormAttachment(0, 230);
			version.setLayoutData(data);
    		
    		dialogArea.pack();
    		getShell().setMinimumSize(340, 344);
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}catch(Error e){
    		e.printStackTrace();
    	}
    	return dialogArea;
    }
    
    /**
	 * one button to click
	 */
	protected void createButtonsForButtonBar(Composite parent){
		
		this.createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	
	}
}