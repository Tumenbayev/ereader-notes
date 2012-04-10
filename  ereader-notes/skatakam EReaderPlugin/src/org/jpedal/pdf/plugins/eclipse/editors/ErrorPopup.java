package org.jpedal.pdf.plugins.eclipse.editors;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ErrorPopup extends Dialog{
	
	
    private Composite dialogArea;
	private Shell shell;
    
    public ErrorPopup(Shell parentShell){
        super(parentShell);
        
        this.shell=parentShell;
    }
    
    
    protected Control createDialogArea(Composite parent){
    	
    	try{
    		
    		dialogArea = (Composite) super.createDialogArea(parent);
    		dialogArea.getShell().setText("Error loading web browser");
    		
    		dialogArea.setLayout(new FormLayout());
    		
    		Label text1 = new Label(dialogArea,SWT.BORDER);
    		text1.setText("Your web browser could not be successfully loaded.  " +
					"Please copy and paste the URL below, manually into your web browser.");
    		FormData data = new FormData();
			data.left = new FormAttachment(5, 0);
			data.top = new FormAttachment(0, 5);
			text1.setLayoutData(data);
			
			
			Text message=new Text(dialogArea,(SWT.WRAP+SWT.READ_ONLY));
			message.setText(" http://www.jpedal.org/jpedal.rss             ");
			//message.setAlignment(SWT.LEFT);
			message.setSize(100, 30);
			message.setEditable(false);
			data = new FormData();
			data.left = new FormAttachment(25,0);
			data.top = new FormAttachment(0,25);
			message.setLayoutData(data);
			
			
    		dialogArea.pack();
    		getShell().setMinimumSize(340, 40);
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}catch(Error e){
    		e.printStackTrace();
    	}
    	return dialogArea;
    }
}