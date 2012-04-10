/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project InfoPopup:  http://www.jpedal.org
 *
 * (C) Copyright 2007, IDRsolutions and Contributors.
 *
 * 	This file is part of JPedal
 *
 @LICENSE@
  *
  * ---------------

  * PDFSettings.java
  * ---------------
  * (C) Copyright 2007, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.pdf.plugins.eclipse.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.PreferenceStore;
import org.jpedal.PdfDecoder;
import org.jpedal.pdf.plugins.eclipse.Activator;
import org.jpedal.pdf.plugins.eclipse.editors.BookmarkPopup;

public class PDFSettings {

	final private static String deliminator=""+(char) 65535;

    /**3 types of info stored*/
    final public static int PATH =0;
    final public static int DESCRIPTION=1;
    final public static int CACHEDFILE=2;
    
    //flag to show if we test for any new versions
    public static boolean checkForUpdates=true;
    public static String versionTested="";

    public static final String version="11-Oct-2011 ("+ PdfDecoder.version+")";


    /**lookup lists for values*/
    final int[] categories=new int[]{PDFSettings.PATH, PDFSettings.DESCRIPTION, PDFSettings.CACHEDFILE};
    final String[] name=new String[]{"Path", "Description", "cachedFile"};

    private static String[] files, description, cachedPath;

    /**default set*/
    final private static String[] rawFiles = {
        "http://www.jpedal.org/download/plugin/chapter01.pdf",
        "http://www.jpedal.org/download/plugin/chapter03.pdf",
        "http://www.jpedal.org/download/plugin/rcp-book-ch2.pdf",
        "http://java.sun.com/j2se/1.4/pdf/j2d-book.pdf"
    };
   
    final private static String[] rawDescription = {
        "iText in Action (Manning) ch1",
        "iText in Action (Manning) ch3",
        "Rich Client Programming (Prentice Hall) ch2",
        "Java Graphics2D reference"
    };
    
    final private static String[] rawCachedPath={null,null, null, null};

    static PreferenceStore store;
    
    //setup with default values or load
    static{
    	
    	//thanks to Sam for the idea
		try{
			BookmarkPopup.PDFcache = Activator.plugin.getDefault().getStateLocation().toFile().getAbsolutePath();
			if(!BookmarkPopup.PDFcache.endsWith(BookmarkPopup.separator))
				BookmarkPopup.PDFcache=BookmarkPopup.PDFcache+BookmarkPopup.separator;
		}catch(Exception e){
			System.out.println("No cached value for PDF plugin");
			//e.printStackTrace();
		}
		
    	//setup empty store and load if present
    	String configFile=BookmarkPopup.PDFcache+"config.txt";
    	store =new PreferenceStore();
        
    	File testExists=new File(configFile);
    	if(!testExists.exists()) //load new
    		resetValuesToDefaultValues();   
    	else{
    		try {
				store.load(new FileInputStream(configFile));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//copy values into variables
    		loadValuesFromEclipse();
    	}
    }

    /**
     * returns true if new version found
     * @param availableVersion
     * @return
     */
    public static boolean checkForUpdates(String availableVersion) {
    	if (!versionTested.equals(availableVersion)) {
           versionTested=availableVersion;
           saveValuesInEclipse();

           return true;
       }else
           return false;

    }
    
    public int getFileCount(){
        return files.length;
    }

    public static void resetValuesToDefaultValues(){
        int count=rawFiles.length;
        files=new String[count];
        description=new String[count];
        cachedPath=new String[count];

        System.arraycopy(rawFiles, 0, files, 0,count);
        System.arraycopy(rawDescription, 0, description, 0,count);
        System.arraycopy(rawCachedPath, 0, cachedPath, 0,count);
        
        //save to persistent state
        try{
        	saveValuesInEclipse();
        }catch(Exception e){
        	e.printStackTrace();
        	System.exit(1);
        }
    }

	public static void saveValuesInEclipse() {
		
		
		store.putValue("files",toString(files));
        store.putValue("description",toString(description));
        store.putValue("cachedPath",toString(cachedPath));
        
        store.putValue("versionTested",versionTested);
        store.putValue("checkForUpdates",""+checkForUpdates);
        
        try {
        	store.setFilename(BookmarkPopup.PDFcache+"config.txt");
			store.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void loadValuesFromEclipse() {
		
		files=fromString(store.getString("files"));
		description=fromString(store.getString("description"));
		cachedPath=fromString(store.getString("cachedPath"));
		
		versionTested=store.getString("versionTested");
		
		String update=store.getString("checkForUpdates");
		if(update!=null && update.equals("false"))
			checkForUpdates=false;
		else
			checkForUpdates=true;
        
	}
    
    private static String toString(String[] strings){
    	
    	int count=strings.length;
    	
    	if(count==0)
    		return "";
    	
    	String value=strings[0];
    	if(value==null)
    		value="null";
    	
    	StringBuffer returnString=new StringBuffer(value);
    	for(int ii=1;ii<count;ii++){
    		returnString.append(deliminator);
    		value=strings[ii];
        	if(value==null)
        		value="null";
    		returnString.append(value);
    	}
    	
    	return returnString.toString();
    	
    }
    
    private static String[] fromString(String string){
    	
    	StringTokenizer values=new StringTokenizer(string, deliminator);
    	
    	
    	int count=values.countTokens(),i=0;
    	String[] strings=new String[count];
    	while(values.hasMoreTokens()){
    		strings[i]=values.nextToken();
    		if(strings[i].equals("null"))
    			strings[i]=null;
    		i++;
    	}
    	
    	return strings;
    	
    }

    public String getValue(int id, int type){
        switch(type){
            case PATH:
                return files[id];

            case DESCRIPTION:
                return description[id];

            case CACHEDFILE:
                return cachedPath[id];
            default:
                return null;
        }
    }
    
    public static String[] getValues(int type){
        switch(type){
            case PATH:
                return files;

            case DESCRIPTION:
                return description;

            case CACHEDFILE:
                return cachedPath;
            default:
                return null;
        }
    }

    

    public void setValue(int id, String newValue, int type){

        switch(type){

            case PATH:
                files[id]=newValue;
                break;

            case DESCRIPTION:
                description[id]=newValue;
                break;

            case CACHEDFILE:
                cachedPath[id]=newValue;
                break;

        }
        
        saveValuesInEclipse();
    }

    public int[] getCategories() {
        return categories;
    }

    public String getCategoryName(int id) {
        return name[id];
    }

    public void resetValues(String[] newFiles,String[] newDescription, String[] newCache) {
        files=newFiles;
        description=newDescription;
        cachedPath=newCache;
        
        saveValuesInEclipse();
    }

    public void addPDFFile(String fileName, String fileDescription, String cachePath) {

        //resize
        int length=files.length;
        int newLength=length+1;

        String[] newFiles=new String[newLength];
        String[] newDesc=new String[newLength];
        String[] newCache=new String[newLength];

        System.arraycopy(files,0,newFiles,0,length);
        System.arraycopy(description,0,newDesc,0,length);
        System.arraycopy(cachedPath,0,newCache,0,length);

        files = newFiles;
        description = newDesc;
        cachedPath = newCache;

        files[length]=fileName;
        description[length]=fileDescription;
        cachedPath[length]=cachePath;

        saveValuesInEclipse();
    }

    /**
     * returns true if match found
     * @param fileName
     * @return
     */
    public boolean deletePDFFile(String fileName) {

        //resize
        int length=files.length;
        int newLength=length-1;

        boolean deleted=false;

        String[] newFiles=new String[newLength];
        String[] newDesc=new String[newLength];
        String[] newCache=new String[newLength];

        int j=0;
        for(int ii=0;ii<length;ii++){
            if(!deleted && files[ii].equals(fileName)){
                deleted=true;
                if(cachedPath[ii]!=null){
                    System.out.println("Delete");
                    java.io.File f=new java.io.File(cachedPath[ii]);
                    f.delete();

                }
            }else if(j<newLength){
                newFiles[j]=files[ii];
                newDesc[j]=description[ii];
                newCache[j]=cachedPath[ii];
                j++;
            }
        }

        if(deleted){
            files=newFiles;
            description=newDesc;
            cachedPath=newCache;
        }
        
        saveValuesInEclipse();

        return deleted;
    }
}
