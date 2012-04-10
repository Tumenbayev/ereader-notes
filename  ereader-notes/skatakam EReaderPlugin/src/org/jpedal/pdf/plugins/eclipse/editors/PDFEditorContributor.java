package org.jpedal.pdf.plugins.eclipse.editors;

import org.eclipse.ui.part.EditorActionBarContributor;

/**
 * used to define Editor in plugin.xml
 *
 */
public class PDFEditorContributor extends EditorActionBarContributor {

	public PDFEditorContributor() {
		super();
		
		if(PDFEditor.debug)
			System.out.println("Contributor Called");
	}
}
