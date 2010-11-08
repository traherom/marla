/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package r;

/**
 *
 * @author Andrew
 */
import java.io.*;
import java.awt.Frame;
import java.awt.FileDialog;


import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.RMainLoopCallbacks;

class RInterface implements RMainLoopCallbacks
{

	public void rWriteConsole(Rengine re, String text, int oType)
	{
		//do nothing
	}

	public void rBusy(Rengine re, int which)
	{
		//do nothing
	}

	public String rReadConsole(Rengine re, String prompt, int addToHistory)
	{
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String s = br.readLine();
			return (s == null || s.length() == 0) ? s : s + "\n";
		}
		catch(Exception e)
		{
			//System.out.println("jriReadConsole exception: "+e.getMessage());
		}
		return null;
	}

	public void rShowMessage(Rengine re, String message)
	{
		//System.out.println("rShowMessage \""+message+"\"");
	}

	public String rChooseFile(Rengine re, int newFile)
	{
		FileDialog fd = new FileDialog(new Frame(), (newFile == 0) ? "Select a file" : "Select a new file", (newFile == 0) ? FileDialog.LOAD : FileDialog.SAVE);
		fd.setVisible(true);
		String res = null;
		if(fd.getDirectory() != null)
			res = fd.getDirectory();
		if(fd.getFile() != null)
			res = (res == null) ? fd.getFile() : (res + fd.getFile());
		return res;
	}

	public void rFlushConsole(Rengine re)
	{
	}

	public void rLoadHistory(Rengine re, String filename)
	{
	}

	public void rSaveHistory(Rengine re, String filename)
	{
	}

	/**
	 * Check the R/JRI installation on the system to ensure R can be used
	 * on the backend.
	 * @return true if R could be reached to perform calculations, false otherwise
	 */
	public static boolean checkRInstall()
	{
		Rengine rengine = new Rengine(null, false, new RInterface());
		return true;
	}

	public static void main(String[] args)
	{
		if(RInterface.checkRInstall())
			System.out.println("R and JRI setup correctly");
		else
			System.out.println("Environment does not have R/JRI setup correctly");
	}
}
