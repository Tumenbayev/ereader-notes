package org.jpedal.pdf.plugins.eclipse.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.jpedal.pdf.plugins.eclipse.editors.PDFEditor;

/**
 * a sample perspective - use Editor for full functions
 *
 */
public class PDFPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		
		if(PDFEditor.debug)
			System.out.println("createInitialLayout called");
		
		try{
			String editorArea=layout.getEditorArea();
			
			//Outline on left
			layout.addView(IPageLayout.ID_OUTLINE,IPageLayout.LEFT,0.25f,editorArea);
			
			layout.addView("org.jpedal.pdf.plugins.eclipse.views.PDFSearch",IPageLayout.BOTTOM,0.8f,editorArea);
			
			//layout.addView(IPageLayout.ID_EDITOR_AREA,IPageLayout.TOP,0.5f,editorArea);
			
			//tab at bottom for find
			//IFolderLayout bottom=layout.createFolder("bottom", IPageLayout.BOTTOM, 0.8f, editorArea);
		
			//bottom.addView("org.jpedal.pdf.plugins.eclipse.views.PDFSearch");
			
		}catch(Exception e){
			e.printStackTrace();
		}catch(Error e){
			e.printStackTrace();
		}
	}
}
