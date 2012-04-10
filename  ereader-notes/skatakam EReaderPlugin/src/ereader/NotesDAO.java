package ereader;
import java.awt.Rectangle;
import java.sql.*;
import java.util.Scanner;

import db.MysqlConnect;


public class NotesDAO {
	
	MysqlConnect conect = new MysqlConnect(); 
	ResultSet rs;
	
	public boolean addNotes(Note notes) throws SQLException {
		boolean isAdded = false;
		Connection conn  = conect.connect();
		Statement statement = conn.createStatement();
		java.util.Date utilDate = new java.util.Date();
		java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
		String noid = notes.getNoteid();
		/*Rectangle rec= notes.getArea();
		int w = rec.width;
		int h = rec.height;
		String area = rec.toString() ;
		String QString = "insert into notes values('"+noid+"','"+notes.getHtext()+"','"+notes.getNotes()+"','"+notes.getAuthor()+"','"+notes.getDate()+"','"+notes.getPage()+"','"+notes.getAuthRating()+"','"+notes.getStatus()+"','"+notes.getDocid()+"','"+area+"','"+notes.getUid()+"')";
		  int updatequery = statement.executeUpdate(QString);
		  if(updatequery != 0)
		  {
			  System.out.println("row inserted");
			  isAdded=true;
		  }
		*/  statement.close();
		conect.CloseConnection(conn);
		System.out.println(" Notes is added" + isAdded);
		return isAdded;
	}
	
	public boolean shareNotes(String nid,String gpid) throws SQLException {
		boolean isShared = false;
		// Our code goes here
				// data base connection should be established and tables should be called
		Connection conn  = conect.connect();
		Statement statement = conn.createStatement();
		String QString = "insert into sharenote values( '"+nid+"','"+gpid+"')";
		  int updatequery = statement.executeUpdate(QString);
		  if(updatequery != 0)
		  {
			  System.out.println("row shared");
			  isShared=true;
		  }
		  statement.close();
		conect.CloseConnection(conn);
		System.out.println(" Notes is shared" + isShared);
		return isShared;
	}
	
	public boolean editNotes(Note notes) throws SQLException {
		boolean isEdited = false;
		// Our code goes here
				// data base connection should be established and tables should be called
		String noteid = notes.getNoteid();
		Connection conn  = conect.connect();
		Statement statement = conn.createStatement();
		Scanner scan= new Scanner(System.in);
		System.out.println("enter content: ");
		String newcontent = scan.next();
		String QString = "update notes set content= '"+newcontent+"'  where notesid= '"+noteid+"' ";
    	  int updatequery = statement.executeUpdate(QString);
		  if(updatequery != 0)
		  {
			  System.out.println("row updated");
			  isEdited=true;
		  }
		  statement.close();
		conect.CloseConnection(conn);
		System.out.println(" Notes is edited" + isEdited);
		return isEdited;
	}
	
	public boolean deleteNotes(String noteid) throws SQLException {
		boolean isDeleted = false;
		// Our code goes here
				// data base connection should be established and tables should be called
		Connection conn  = conect.connect();
		Statement statement = conn.createStatement();
		String QString = "delete from notes where notesid= '"+noteid+"' ";
		  int updatequery = statement.executeUpdate(QString);
		  if(updatequery != 0)
		  {
			  System.out.println("row deleted");
			  isDeleted=true;
		  }
		  statement.close();
		conect.CloseConnection(conn);
		System.out.println(" Notes is delted" + isDeleted);
		return isDeleted;
	}
	
	public boolean viewNotes() throws SQLException {
		boolean isViewed = false;
		// Our code goes here
				// data base connection should be established and tables should be called
		Connection conn  = conect.connect();
		Statement statement = conn.createStatement();
		String QString = "select * from notes";
		  ResultSet rs= statement.executeQuery(QString);
		  while(rs.next())
		  {
			  System.out.println(" noteid: "+rs.getString("notesid")+"\n content:"+rs.getString("content")+"\n Author: "+rs.getString("author"));
			  System.out.println("------");
		  }
		  isViewed=true;
		  rs.close();
		  statement.close();
		conect.CloseConnection(conn);
		System.out.println(" Notes is isViewed" + isViewed);
		return isViewed;
	}

}
