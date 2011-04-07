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

package marla.opedit.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import marla.ide.gui.ProgressFrame;
import marla.ide.problem.MarlaException;
import marla.ide.resource.Configuration;
import marla.ide.resource.Configuration.ConfigType;
import marla.opedit.operation.OperationFile;

/**
 * The main frame of the stand-alone application.
 *
 * @author Alex Laird
 */
public class MainFrame extends JFrame
{
	/** The minimum size the window frame is allowed to be.*/
	private final Dimension MINIMUM_WINDOW_SIZE = new Dimension(790, 400);
	/** The progress frame.*/
	public static ProgressFrame progressFrame;
	/** The panel that is added to the frame.*/
	private static ViewPanel viewPanel;

	/**
	 * Constructs the frame for the stand-alone application.
	 *
	 * @param progressFrame A reference to the progress frame.
	 */
	public MainFrame(ProgressFrame progressFrame)
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		MainFrame.progressFrame = progressFrame;

		// Construct the view panel
		viewPanel = new ViewPanel(this);
		// Add the view to the frame
		add(viewPanel);

		// Initialize frame components
		initComponents();
		initMyComponents();
	}

	/**
	 * Initializes the frame for the stand-alone application.
	 */
	private void initMyComponents()
	{
		// Set the minimum size a user can adjust the frame to
		setMinimumSize(MINIMUM_WINDOW_SIZE);
		// Set the location of the frame to the center of the screen
		setLocationRelativeTo(null);
		// Set the title of the frame, displaying the version number only if we're in pre-release
		setTitle(Domain.NAME);

		// Add window listeners to ensure proper saving, sizing, and orientation is done when needed
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				viewPanel.quit(true);
			}
		});

		verifyBounds();
	}

	/**
	 * Verify that the application is within the screen resolution both in size
	 * and in location.
	 */
	private void verifyBounds()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int width = getWidth();
		int height = getHeight();
		if(width < MINIMUM_WINDOW_SIZE.width)
		{
			width = MINIMUM_WINDOW_SIZE.width;
		}
		if(height < MINIMUM_WINDOW_SIZE.height)
		{
			height = MINIMUM_WINDOW_SIZE.height;
		}
		if(width > screenSize.width)
		{
			width = screenSize.width;
		}
		if(height > screenSize.height - 30)
		{
			height = screenSize.height - 30;
		}

		int x = getX();
		int y = getY();
		if(x > screenSize.getWidth())
		{
			x = (int) screenSize.getWidth() - width;
		}
		if(x < 0)
		{
			x = 0;
		}
		if(y > screenSize.getHeight())
		{
			y = (int) screenSize.getHeight() - height;
		}
		if(y < 0)
		{
			y = 0;
		}

		setBounds(x, y, width, height);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menuBar = new javax.swing.JMenuBar();
        editMenuItem = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconImage(new ImageIcon (getClass ().getResource ("/marla/ide/images/logo.png")).getImage ());
        getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        editMenuItem.setText("Edit");
        editMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));

        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        undoMenuItem.setText("Undo");
        undoMenuItem.setEnabled(false);
        undoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoMenuItemActionPerformed(evt);
            }
        });
        editMenuItem.add(undoMenuItem);

        redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        redoMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        redoMenuItem.setText("Redo");
        redoMenuItem.setEnabled(false);
        redoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoMenuItemActionPerformed(evt);
            }
        });
        editMenuItem.add(redoMenuItem);

        menuBar.add(editMenuItem);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoMenuItemActionPerformed
		viewPanel.undo();
	}//GEN-LAST:event_undoMenuItemActionPerformed

	private void redoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoMenuItemActionPerformed
		viewPanel.redo();
	}//GEN-LAST:event_redoMenuItemActionPerformed

	/**
	 * Set the configuration arguments and the visible state of the MainFrame.
	 *
	 * @param args Arguments for configuration.
	 * @param visible The visible state to set to.
	 */
	public void setVisible(final String[] args, boolean visible)
	{
		new Thread (new Runnable()
		{
			@Override
			public void run()
			{
				Domain.setProgressString("10%");
				Domain.setProgressValue(10);
				Domain.setProgressStatus("Loading configuration...");

				// Configure
				Configuration conf = Configuration.getInstance();
				List<ConfigType> missed = conf.configureAll(args);

				Domain.setProgressString("90%");
				Domain.setProgressValue(90);
				Domain.setProgressStatus("Validating configuration...");

				int currIndex = 0;
				while(currIndex < missed.size())
				{
					ConfigType curr = missed.get(currIndex);

					boolean fixed = false;
					try
					{
						viewPanel.fileChooserDialog.setDialogTitle(Configuration.getName(curr));
						viewPanel.fileChooserDialog.setDialogType(JFileChooser.OPEN_DIALOG);
						viewPanel.fileChooserDialog.resetChoosableFileFilters();
						viewPanel.fileChooserDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						// Display the chooser and retrieve the selected file
						int response = viewPanel.fileChooserDialog.showOpenDialog(progressFrame);
						if(response == JFileChooser.APPROVE_OPTION)
						{
							conf.set(curr, viewPanel.fileChooserDialog.getSelectedFile().getPath());
							fixed = true;
						}
						else
						{
							JOptionPane.showMessageDialog(viewPanel.domain.getTopWindow(), "The maRla Project cannot run without these resources.", "Fatal Error", JOptionPane.ERROR_MESSAGE);
							System.exit(1);
						}
					}
					catch(MarlaException ex)
					{
						System.out.println(ex.getMessage());
						fixed = false;
					}

					// If we succeed, find the next thing
					if(fixed)
						currIndex++;
				}

				Domain.setProgressString("95%");
				Domain.setProgressValue(95);
				Domain.setProgressStatus("Initializating workspace...");

				try
				{
					// Preemptively save config file
					conf.save();
				}
				catch(MarlaException ex)
				{
					System.out.println("Error saving configuration file: " + ex.getMessage());
				}

				viewPanel.initLoading = false;
				viewPanel.newButton.setEnabled(true);
				viewPanel.browseEditingButton.setEnabled(true);
				viewPanel.browseDataButton.setEnabled(true);
				progressFrame.setAlwaysOnTop(false);

				// Done!
				Domain.setProgressString("100%");
				Domain.setProgressValue(100);
				Domain.setProgressStatus("Complete...");

				progressFrame.setVisible(false);
				setCursor(Cursor.getDefaultCursor());

				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						// If the final argument is a save file, open it right now
						if(args.length != 0 && args[args.length - 1].endsWith(".xml"))
						{
							try
							{
								Domain.passedInFile = new File (args[0]);
								viewPanel.currentFile = new OperationFile(Domain.passedInFile.getCanonicalPath());
								viewPanel.openFile();
							}
							catch (IOException ex)
							{
								System.out.println("Unable to load file from command line: " + ex.getMessage());
								System.out.println("Load through the GUI for more information.");

								Domain.passedInFile = null;
							}
						}
					}
				});
			}
		}).start();

		progressFrame.setAlwaysOnTop(true);
		super.setVisible(visible);
		verifyBounds();
		progressFrame.setLocationRelativeTo(this);
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu editMenuItem;
    private javax.swing.JMenuBar menuBar;
    protected javax.swing.JMenuItem redoMenuItem;
    protected javax.swing.JMenuItem undoMenuItem;
    // End of variables declaration//GEN-END:variables
}
