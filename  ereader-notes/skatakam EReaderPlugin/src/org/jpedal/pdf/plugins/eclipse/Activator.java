package org.jpedal.pdf.plugins.eclipse;


import java.util.ResourceBundle;

import javax.swing.UIManager;


import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

import org.jpedal.pdf.plugins.eclipse.editors.PDFEditor;
import org.jpedal.utils.Messages;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class which sets up the plugin
 */
public class Activator extends AbstractUIPlugin {

	//The shared instance.
	public static Activator plugin;
	
	public static boolean isBroken=false;
	
	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
		
		if(PDFEditor.debug)
			System.out.println("Activator Called");
		
		
		/**
		 * get machine - used in past due to 'bugs' in SWT/eclipse 
		 */
		try {
			
			
			Messages.setBundle(ResourceBundle.getBundle("org.jpedal.international.messages"));
			
			String name = System.getProperty("os.name");
			
			//now fixed
//			if (name.equals("Mac OS X")){
//				isBroken=true;
//				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
//						"Plugin will not run under OS X", 
//						"Will not run under OS X under major SWT-AWT bridge bug is fixed for Apple JVMs" +
//				"-see https://bugs.eclipse.org/bugs/show_bug.cgi?id=67384\n");
//			}
			
			String version=System.getProperty("java.version");
			
			if (name.equals("Linux")&&((version.startsWith("1.4")||(version.startsWith("1.3"))))){
				isBroken=true;
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
						"Plugin needs 1.5 under Linux","Linux needs Java 1.5 or greater to support PDF plugin");
			}
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		if(!isBroken)
		super.start(context);
		
		if(PDFEditor.debug)
			System.out.println("Activator start called");
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		if(!isBroken)
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("JPedalViewer", path);
	}
}
