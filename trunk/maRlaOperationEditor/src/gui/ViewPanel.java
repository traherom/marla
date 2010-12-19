/*
 * The maRla Project - Graphical problem solver for statistics and probability problems.
 * Copyright (C) 2010 Cedarville University
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package gui;

import java.awt.Font;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JPanel;
import resource.LoadSaveThread;

/**
 * The view of the application, which contains all user interactive components.
 * Functions that can be (that are not directly related to the front-end) will
 * be abstracted out to the Domain class.
 *
 * @author Alex Laird
 */
public class ViewPanel extends JPanel
{
    /** The full time format for debug output.*/
    public static final SimpleDateFormat FULL_TIME_FORMAT = new SimpleDateFormat ("MM/dd/yyyy h:mm:ss a");
    /** Default, plain, 12-point font.*/
    public static final Font FONT_PLAIN_12 = new Font ("Verdana", Font.PLAIN, 12);
    /** Default, bold, 12-point font.*/
    public static final Font FONT_BOLD_12 = new Font ("Verdana", Font.BOLD, 12);

    /** The main frame of a stand-alone application.*/
    public MainFrame mainFrame;
    /** The domain object reference performs generic actions specific to the GUI.*/
    protected Domain domain = new Domain (this);

    /**
     * Creates new form MainFrame for a stand-alone application.
     */
    public ViewPanel(MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
        init ();
    }

    /**
     * Calls initialization functions for the frame-based application.
     */
    private void init()
    {
        initComponents ();
        initMyComponents ();
    }

    /**
     * Custom initialization of specific components is done here.
     */
    private void initMyComponents()
    {
        domain.loadSaveThread = new LoadSaveThread (this, domain);
        // launch the save thread
        domain.loadSaveThread.start ();
        domain.setLoadSaveThread (domain.loadSaveThread);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Ensures all processes are terminated and that all ways of exiting the
     * application result in the same closing process.
     *
     * @param forceQuit True if System.exit should be called, false if the caller
	 * plans to terminate the application.
     */
    protected void quit(boolean forceQuit)
    {
		if (Domain.logger.size () > 0)
		{
			try
			{
				BufferedWriter out = new BufferedWriter (new FileWriter (domain.logFile, true));
				Date date = new Date ();
				out.write ("------------------------------------\n");
				out.write ("Date: " + FULL_TIME_FORMAT.format (date) + "\n");

				for (int i = 0; i < Domain.logger.size (); ++i)
				{
					Exception ex = (Exception) Domain.logger.get (i);
					out.write ("Error: " + ex.getClass () + "\n");
					out.write ("Message: " + ex.getMessage () + "\n--\nTrace:\n");
					Object[] trace = ex.getStackTrace ();
					for (int j = 0; j < trace.length; ++j)
					{
						out.write ("  " + trace[j].toString () + "\n");
					}
					out.write ("--\n\n");
					out.write ("----\n");
				}

				out.write ("------------------------------------\n\n\n");
				out.flush ();
			}
			catch (IOException ex)
			{
			}
		}

		domain.loadSaveThread.stopRunning ();
		domain.loadSaveThread.save ();

        if (forceQuit)
        {
            System.exit (0);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
