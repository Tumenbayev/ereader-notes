package org.jpedal.pdf.plugins.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jpedal.pdf.plugins.eclipse.editors.BookmarkPopup;
import org.jpedal.pdf.plugins.eclipse.editors.PDFEditor;

/**
 * Open our dialog window for user to select PDF to view
 */
public class PDFHandler extends AbstractHandler {
	
	public PDFHandler() {
	}

	/**
	 * execute the command
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		//id is in event - we only have single event so no need to check it
		//System.out.println(event);
		
		Command com = event.getCommand();
		String commandID = com.getId();
		
		//Bookmarks popup window
		if(commandID.equals("pdfviewer.commands.Bookmark")){
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			BookmarkPopup bookmarks = new BookmarkPopup(window.getShell());
			bookmarks.open();
		}
		
		return null;
	}
}
