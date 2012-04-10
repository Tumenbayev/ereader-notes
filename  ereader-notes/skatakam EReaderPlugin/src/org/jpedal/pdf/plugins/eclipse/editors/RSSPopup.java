package org.jpedal.pdf.plugins.eclipse.editors;


import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.jpedal.PdfDecoder;
import org.jpedal.pdf.plugins.eclipse.settings.PDFSettings;
import org.jpedal.utils.BrowserLauncher;

public class RSSPopup extends Dialog{
	
	
    private Composite dialogArea;
	private Shell shell;
    
    public RSSPopup(Shell parentShell){
        super(parentShell);
        
        this.shell=parentShell;
    }
    
    
    protected Control createDialogArea(Composite parent){
    	
    	try{
    		
    		dialogArea = (Composite) super.createDialogArea(parent);
    		dialogArea.getShell().setText("Subscribe to JPedal RSS Feed");
    		
    		dialogArea.setLayout(new FormLayout());
    		
    		Label text1 = new Label(dialogArea,SWT.BORDER);
    		text1.setText("Click on the link below to load a web browser and sign up to our RSS feed.\n");
    		FormData data = new FormData();
			data.left = new FormAttachment(5, 0);
			data.top = new FormAttachment(0, 5);
			text1.setLayoutData(data);
			
			Button message=new Button(dialogArea,SWT.PUSH);
			message.setText("http://www.jpedal.org/jpedal.rss");
			message.setAlignment(SWT.LEFT);

			data = new FormData();
			data.left = new FormAttachment(25,0);
			data.top = new FormAttachment(0,25);
			message.setLayoutData(data);
			message.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent event){
					try {
						BrowserLauncher.openURL("http://www.jpedal.org/jpedal.rss");
					} catch (Exception e1) {

						ErrorPopup error = new ErrorPopup(shell);
						error.open();

						//<start-full><start-demo>
						e1.printStackTrace();
						//<end-demo><end-full>
					}
				}
			});
		
    		//load image
    		Image image = new Image(shell.getDisplay(),
    				getClass().getResourceAsStream("/org/jpedal/examples/simpleviewer/res/rss.png"));
			
    		//put in widget to display
    		Label button = new Label(dialogArea,SWT.BORDER);
    		button.setImage(image);
    		data = new FormData();
			data.left = new FormAttachment(5, 0);
			data.top = new FormAttachment(0, 60);
			button.setLayoutData(data);
			
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