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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import problem.MarlaException;
import problem.Problem;
import resource.BuildInfo;
import resource.Configuration;
import resource.Configuration.ConfigType;

/**
 * The main frame of the stand-alone application.
 *
 * @author Alex Laird
 */
public class MainFrame extends JFrame
{
	/** The minimum size the window frame is allowed to be.*/
	private final Dimension MINIMUM_WINDOW_SIZE = new Dimension(650, 400);
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
				progressFrame.progressBar.setString("10%");
				progressFrame.progressBar.setValue(10);
				progressFrame.statusLabel.setText("Initializing ...");
				
				// Configure
				Configuration conf = Configuration.getInstance();
				List<ConfigType> missed = conf.configureAll(args);

				int currIndex = 0;
				while(currIndex < missed.size())
				{
					ConfigType curr = missed.get(currIndex);

					boolean fixed = false;
					try
					{
						viewPanel.openChooserDialog.setDialogTitle(Configuration.getName(curr));
						viewPanel.openChooserDialog.resetChoosableFileFilters();
						viewPanel.openChooserDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						// Display the chooser and retrieve the selected file
						int response = viewPanel.openChooserDialog.showOpenDialog(viewPanel);
						if(response == JFileChooser.APPROVE_OPTION)
						{
							conf.set(curr, viewPanel.openChooserDialog.getSelectedFile().getPath());
							fixed = true;
						}
						else
						{
							JOptionPane.showMessageDialog(viewPanel, "The maRla Project cannot run without these resources.", "Fatal Error", JOptionPane.ERROR_MESSAGE);
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

				try
				{
					// Preemptively save config file
					conf.save();
				}
				catch(MarlaException ex)
				{
					System.out.println("Error saving configuration file: " + ex.getMessage());
				}

				try
				{
					viewPanel.loadOperations();
				}
				catch(MarlaException ex)
				{
					JOptionPane.showMessageDialog(viewPanel, ex.getMessage(), "Load Error", JOptionPane.WARNING_MESSAGE);
				}

				// If the final argument is a save file, open it right now
				if(args.length != 0 && args[args.length - 1].endsWith(".marla"))
				{
					try
					{
						viewPanel.domain.problem = Problem.load(args[args.length - 1]);
						viewPanel.openProblem(false);
					}
					catch(Exception ex)
					{
						System.out.println("Unable to load file from command line: " + ex.getMessage());
						System.out.println("Load through the GUI for more information.");
					}
				}

				progressFrame.setAlwaysOnTop(false);
				progressFrame.setVisible(false);
				setCursor(Cursor.getDefaultCursor());
			}
		}).start();

		progressFrame.setAlwaysOnTop(true);
		super.setVisible(visible);
		verifyBounds();
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
		setTitle(getDefaultTitle());

		// Add window listeners to ensure proper saving, sizing, and orientation is done when needed
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				viewPanel.quit(true);
			}
		});
	}

	/**
	 * Verify that the application is within the screen resolution both in size
	 * and in location.
	 */
	private void verifyBounds()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int x = getX();
		int y = getY();
		if(x > screenSize.getWidth())
		{
			x = (int) screenSize.getWidth() - getWidth();
		}
		if(x < 0 - getWidth())
		{
			x = 0;
		}
		if(y > screenSize.getHeight())
		{
			y = (int) screenSize.getHeight() - getHeight();
		}
		if(y < 0 - getHeight())
		{
			y = 0;
		}

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
		if(height > screenSize.height)
		{
			height = screenSize.height;
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
        fileMenu = new javax.swing.JMenu();
        newProblemMenuItem = new javax.swing.JMenuItem();
        openProblemMenuItem = new javax.swing.JMenuItem();
        closeProblemMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        fileSeparator1 = new javax.swing.JPopupMenu.Separator();
        exportToPdfMenuItem = new javax.swing.JMenuItem();
        exportForLatexMenuItem = new javax.swing.JMenuItem();
        fileSeparator2 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        editSeparator1 = new javax.swing.JPopupMenu.Separator();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        editSeparator2 = new javax.swing.JPopupMenu.Separator();
        selectAllMenuItem = new javax.swing.JMenuItem();
        problemMenu = new javax.swing.JMenu();
        editProblemMenuItem = new javax.swing.JMenuItem();
        editSubProblemsMenuItem = new javax.swing.JMenuItem();
        editConclusionMenuItem = new javax.swing.JMenuItem();
        problemSeparator1 = new javax.swing.JPopupMenu.Separator();
        newDataSetMenuItem = new javax.swing.JMenuItem();
        editDataSetsMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        reloadOperationgsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        settingsMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpContentsMenuItem = new javax.swing.JMenuItem();
        helpSeparator1 = new javax.swing.JPopupMenu.Separator();
        checkForUpdatesMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconImage(new ImageIcon (getClass ().getResource ("/images/logo.png")).getImage ());
        setName("mainFrame"); // NOI18N
        getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        fileMenu.setText("File");
        fileMenu.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        fileMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                fileMenuMenuSelected(evt);
            }
        });

        newProblemMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newProblemMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        newProblemMenuItem.setText("New Problem...");
        newProblemMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProblemMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newProblemMenuItem);

        openProblemMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openProblemMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        openProblemMenuItem.setText("Open Problem...");
        openProblemMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openProblemMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openProblemMenuItem);

        closeProblemMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        closeProblemMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        closeProblemMenuItem.setText("Close Problem");
        closeProblemMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeProblemMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeProblemMenuItem);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        saveMenuItem.setText("Save");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0));
        saveAsMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        saveAsMenuItem.setText("Save As...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(fileSeparator1);

        exportToPdfMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        exportToPdfMenuItem.setText("Export to PDF...");
        exportToPdfMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportToPdfMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportToPdfMenuItem);

        exportForLatexMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        exportForLatexMenuItem.setText("Export for LaTeX...");
        exportForLatexMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportForLatexMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportForLatexMenuItem);
        fileMenu.add(fileSeparator2);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        exitMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");
        editMenu.setEnabled(false);
        editMenu.setFont(new java.awt.Font("Verdana", 0, 12));
        editMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                editMenuMenuSelected(evt);
            }
        });

        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        undoMenuItem.setText("Undo");
        editMenu.add(undoMenuItem);

        redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        redoMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        redoMenuItem.setText("Redo");
        editMenu.add(redoMenuItem);
        editMenu.add(editSeparator1);

        cutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        cutMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        copyMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        pasteMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        deleteMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);
        editMenu.add(editSeparator2);

        selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAllMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        selectAllMenuItem.setText("Select All");
        editMenu.add(selectAllMenuItem);

        menuBar.add(editMenu);

        problemMenu.setText("Problem");
        problemMenu.setFont(new java.awt.Font("Verdana", 0, 12));
        problemMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                problemMenuMenuSelected(evt);
            }
        });

        editProblemMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        editProblemMenuItem.setText("Edit Problem...");
        editProblemMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProblemMenuItemActionPerformed(evt);
            }
        });
        problemMenu.add(editProblemMenuItem);

        editSubProblemsMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        editSubProblemsMenuItem.setText("Edit Sub Problems...");
        editSubProblemsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSubProblemsMenuItemActionPerformed(evt);
            }
        });
        problemMenu.add(editSubProblemsMenuItem);

        editConclusionMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        editConclusionMenuItem.setText("Edit Conclusion...");
        editConclusionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editConclusionMenuItemActionPerformed(evt);
            }
        });
        problemMenu.add(editConclusionMenuItem);
        problemMenu.add(problemSeparator1);

        newDataSetMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        newDataSetMenuItem.setText("New Data Set...");
        newDataSetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newDataSetMenuItemActionPerformed(evt);
            }
        });
        problemMenu.add(newDataSetMenuItem);

        editDataSetsMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        editDataSetsMenuItem.setText("Edit Data Sets...");
        editDataSetsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editDataSetsMenuItemActionPerformed(evt);
            }
        });
        problemMenu.add(editDataSetsMenuItem);

        menuBar.add(problemMenu);

        toolsMenu.setText("Tools");
        toolsMenu.setFont(new java.awt.Font("Verdana", 0, 12));
        toolsMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                toolsMenuMenuSelected(evt);
            }
        });

        reloadOperationgsMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        reloadOperationgsMenuItem.setText("Reload Operations");
        reloadOperationgsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadOperationgsMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(reloadOperationgsMenuItem);
        toolsMenu.add(jSeparator1);

        settingsMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        settingsMenuItem.setText("Settings");
        settingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(settingsMenuItem);

        menuBar.add(toolsMenu);

        helpMenu.setText("Help");
        helpMenu.setFont(new java.awt.Font("Verdana", 0, 12));
        helpMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                helpMenuMenuSelected(evt);
            }
        });

        helpContentsMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        helpContentsMenuItem.setText("Help Contents");
        helpContentsMenuItem.setEnabled(false);
        helpMenu.add(helpContentsMenuItem);
        helpMenu.add(helpSeparator1);

        checkForUpdatesMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        checkForUpdatesMenuItem.setText("Check for Updates");
        checkForUpdatesMenuItem.setEnabled(false);
        helpMenu.add(checkForUpdatesMenuItem);

        aboutMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        aboutMenuItem.setText("About");
        aboutMenuItem.setEnabled(false);
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitMenuItemActionPerformed
	{//GEN-HEADEREND:event_exitMenuItemActionPerformed
		viewPanel.quit(true);
	}//GEN-LAST:event_exitMenuItemActionPerformed

	private void newProblemMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProblemMenuItemActionPerformed
		viewPanel.newProblem();
	}//GEN-LAST:event_newProblemMenuItemActionPerformed

	private void openProblemMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openProblemMenuItemActionPerformed
		viewPanel.domain.load();
	}//GEN-LAST:event_openProblemMenuItemActionPerformed

	private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
		viewPanel.domain.save();
	}//GEN-LAST:event_saveMenuItemActionPerformed

	private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
		viewPanel.domain.saveAs();
	}//GEN-LAST:event_saveAsMenuItemActionPerformed

	private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_fileMenuMenuSelected
		if(viewPanel.initLoading)
		{
			for(int i = 0; i < fileMenu.getMenuComponentCount(); ++i)
			{
				fileMenu.getMenuComponent(i).setEnabled(false);
			}
		}
		else
		{
			newProblemMenuItem.setEnabled(true);
			openProblemMenuItem.setEnabled(true);
			exitMenuItem.setEnabled(true);

			if(viewPanel.domain.problem != null)
			{
				exportForLatexMenuItem.setEnabled(true);
				exportToPdfMenuItem.setEnabled(true);
				closeProblemMenuItem.setEnabled(true);
				saveAsMenuItem.setEnabled(true);
				if(viewPanel.domain.problem.isChanged())
				{
					saveMenuItem.setEnabled(true);
				}
				else
				{
					saveMenuItem.setEnabled(false);
				}
			}
			else
			{
				closeProblemMenuItem.setEnabled(false);
				saveMenuItem.setEnabled(false);
				saveAsMenuItem.setEnabled(false);
				exportForLatexMenuItem.setEnabled(false);
				exportToPdfMenuItem.setEnabled(false);
			}
		}
	}//GEN-LAST:event_fileMenuMenuSelected

	private void editMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_editMenuMenuSelected
		if(viewPanel.initLoading)
		{
			for(int i = 0; i < fileMenu.getMenuComponentCount(); ++i)
			{
				fileMenu.getMenuComponent(i).setEnabled(false);
			}
		}
		else
		{
			if(viewPanel.domain.problem != null)
			{
				undoMenuItem.setEnabled(true);
				redoMenuItem.setEnabled(true);
				cutMenuItem.setEnabled(true);
				copyMenuItem.setEnabled(true);
				pasteMenuItem.setEnabled(true);
				deleteMenuItem.setEnabled(true);
				selectAllMenuItem.setEnabled(true);
			}
			else
			{
				undoMenuItem.setEnabled(false);
				redoMenuItem.setEnabled(false);
				cutMenuItem.setEnabled(false);
				copyMenuItem.setEnabled(false);
				pasteMenuItem.setEnabled(false);
				deleteMenuItem.setEnabled(false);
				selectAllMenuItem.setEnabled(false);
			}
		}
	}//GEN-LAST:event_editMenuMenuSelected

	private void problemMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_problemMenuMenuSelected
		if(viewPanel.initLoading)
		{
			for(int i = 0; i < fileMenu.getMenuComponentCount(); ++i)
			{
				fileMenu.getMenuComponent(i).setEnabled(false);
			}
		}
		else
		{
			if(viewPanel.domain.problem != null)
			{
				editProblemMenuItem.setEnabled(true);
				editConclusionMenuItem.setEnabled(true);
				editSubProblemsMenuItem.setEnabled(true);
				newDataSetMenuItem.setEnabled(true);
				editDataSetsMenuItem.setEnabled(true);
			}
			else
			{
				editProblemMenuItem.setEnabled(false);
				editConclusionMenuItem.setEnabled(false);
				editSubProblemsMenuItem.setEnabled(false);
				newDataSetMenuItem.setEnabled(false);
				editDataSetsMenuItem.setEnabled(false);
			}
		}
	}//GEN-LAST:event_problemMenuMenuSelected

	private void toolsMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_toolsMenuMenuSelected
		if(viewPanel.initLoading)
		{
			for(int i = 0; i < fileMenu.getMenuComponentCount(); ++i)
			{
				fileMenu.getMenuComponent(i).setEnabled(false);
			}
		}
		else
		{
			if(viewPanel.domain.problem != null)
			{
				reloadOperationgsMenuItem.setEnabled(true);
			}
			else
			{
				reloadOperationgsMenuItem.setEnabled(false);
			}
		}
	}//GEN-LAST:event_toolsMenuMenuSelected

	private void closeProblemMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeProblemMenuItemActionPerformed
		viewPanel.closeProblem(false);
	}//GEN-LAST:event_closeProblemMenuItemActionPerformed

	private void editProblemMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editProblemMenuItemActionPerformed
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.editProblem();
	}//GEN-LAST:event_editProblemMenuItemActionPerformed

	private void newDataSetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newDataSetMenuItemActionPerformed
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.setTitle("Edit Problem");
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.welcomeTextLabel.setText(ViewPanel.welcomeEditText);
		editProblemMenuItemActionPerformed(null);
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.addNewDataSet();
	}//GEN-LAST:event_newDataSetMenuItemActionPerformed

	private void reloadOperationgsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadOperationgsMenuItemActionPerformed
		viewPanel.reloadOperations();
	}//GEN-LAST:event_reloadOperationgsMenuItemActionPerformed

	private void exportToPdfMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportToPdfMenuItemActionPerformed
		viewPanel.domain.exportToPdf();
	}//GEN-LAST:event_exportToPdfMenuItemActionPerformed

	private void exportForLatexMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportForLatexMenuItemActionPerformed
		viewPanel.domain.exportForLatex();
	}//GEN-LAST:event_exportForLatexMenuItemActionPerformed

	private void editSubProblemsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSubProblemsMenuItemActionPerformed
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.setTitle("Edit Problem");
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.welcomeTextLabel.setText(ViewPanel.welcomeEditText);
		editProblemMenuItemActionPerformed(null);
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.editSubProblems();
	}//GEN-LAST:event_editSubProblemsMenuItemActionPerformed

	private void helpMenuMenuSelected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_helpMenuMenuSelected
	{//GEN-HEADEREND:event_helpMenuMenuSelected
		if(viewPanel.initLoading)
		{
			for(int i = 0; i < fileMenu.getMenuComponentCount(); ++i)
			{
				fileMenu.getMenuComponent(i).setEnabled(false);
			}
		}
		else
		{
			for(int i = 0; i < fileMenu.getMenuComponentCount(); ++i)
			{
				fileMenu.getMenuComponent(i).setEnabled(true);
			}
		}
	}//GEN-LAST:event_helpMenuMenuSelected

	private void settingsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_settingsMenuItemActionPerformed
	{//GEN-HEADEREND:event_settingsMenuItemActionPerformed
		viewPanel.SETTINGS_DIALOG.launchSettingsDialog();
	}//GEN-LAST:event_settingsMenuItemActionPerformed

	private void editConclusionMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editConclusionMenuItemActionPerformed
	{//GEN-HEADEREND:event_editConclusionMenuItemActionPerformed
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.setTitle("Edit Problem");
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.welcomeTextLabel.setText(ViewPanel.welcomeEditText);
		editProblemMenuItemActionPerformed(null);
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.editConclusion();
	}//GEN-LAST:event_editConclusionMenuItemActionPerformed

	private void editDataSetsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editDataSetsMenuItemActionPerformed
	{//GEN-HEADEREND:event_editDataSetsMenuItemActionPerformed
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.setTitle("Edit Problem");
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.welcomeTextLabel.setText(ViewPanel.welcomeEditText);
		editProblemMenuItemActionPerformed(null);
		viewPanel.NEW_PROBLEM_WIZARD_DIALOG.editDataSet(null);
	}//GEN-LAST:event_editDataSetsMenuItemActionPerformed

	/**
	 * Retrieves the default title, which is the program name with it's version number.
	 *
	 * @return The default title of the application.
	 */
	protected String getDefaultTitle()
	{
		String title = Domain.NAME;
		if(!Domain.PRE_RELEASE.equals(""))
		{
			title += " " + Domain.VERSION + " " + Domain.PRE_RELEASE + " r" + BuildInfo.revisionNumber;
		}
		return title;
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem checkForUpdatesMenuItem;
    private javax.swing.JMenuItem closeProblemMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem editConclusionMenuItem;
    private javax.swing.JMenuItem editDataSetsMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editProblemMenuItem;
    private javax.swing.JPopupMenu.Separator editSeparator1;
    private javax.swing.JPopupMenu.Separator editSeparator2;
    private javax.swing.JMenuItem editSubProblemsMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem exportForLatexMenuItem;
    private javax.swing.JMenuItem exportToPdfMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JPopupMenu.Separator fileSeparator1;
    private javax.swing.JPopupMenu.Separator fileSeparator2;
    private javax.swing.JMenuItem helpContentsMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPopupMenu.Separator helpSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newDataSetMenuItem;
    private javax.swing.JMenuItem newProblemMenuItem;
    private javax.swing.JMenuItem openProblemMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenu problemMenu;
    private javax.swing.JPopupMenu.Separator problemSeparator1;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JMenuItem reloadOperationgsMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenuItem undoMenuItem;
    // End of variables declaration//GEN-END:variables
}
