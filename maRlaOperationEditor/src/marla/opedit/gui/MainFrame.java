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

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * The main frame of the stand-alone application.
 *
 * @author Alex Laird
 */
public class MainFrame extends JFrame
{
	/** The minimum size the window frame is allowed to be.*/
	private final Dimension MINIMUM_WINDOW_SIZE = new Dimension(600, 400);
	/** The panel that is added to the frame.*/
	private static ViewPanel viewPanel;

	/**
	 * Constructs the frame for the stand-alone application.
	 */
	public MainFrame()
	{
		// Construct the view panel
		viewPanel = new ViewPanel(this);
		// Add the shutdown hook to ensure saving prior to a close
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				viewPanel.quit(false);
			}
		});

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
        newOperationMenuItem = new javax.swing.JMenuItem();
        openOperationMenuItem = new javax.swing.JMenuItem();
        closeOperationMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        fileSeparator1 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpContentsMenuItem = new javax.swing.JMenuItem();
        helpSeparator1 = new javax.swing.JPopupMenu.Separator();
        checkForUpdatesMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon (getClass ().getResource ("/marla/ide/images/logo.png")).getImage ());
        getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        fileMenu.setText("File");
        fileMenu.setFont(new java.awt.Font("Verdana", 0, 12));
        fileMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                fileMenuMenuSelected(evt);
            }
        });

        newOperationMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newOperationMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        newOperationMenuItem.setText("New Operation XML");
        newOperationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newOperationMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newOperationMenuItem);

        openOperationMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openOperationMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        openOperationMenuItem.setText("Open Operation XML...");
        openOperationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openOperationMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openOperationMenuItem);

        closeOperationMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        closeOperationMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        closeOperationMenuItem.setText("Close Operation XML");
        closeOperationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeOperationMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeOperationMenuItem);

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

	private void helpMenuMenuSelected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_helpMenuMenuSelected
	{//GEN-HEADEREND:event_helpMenuMenuSelected
		if(viewPanel.initLoading)
		{
			for(int i = 0; i < helpMenu.getMenuComponentCount(); ++i)
			{
				helpMenu.getMenuComponent(i).setEnabled(false);
			}
		}
		else
		{
			for(int i = 0; i < helpMenu.getMenuComponentCount(); ++i)
			{
				helpMenu.getMenuComponent(i).setEnabled(true);
			}
		}
}//GEN-LAST:event_helpMenuMenuSelected

	private void newOperationMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newOperationMenuItemActionPerformed
	{//GEN-HEADEREND:event_newOperationMenuItemActionPerformed
		viewPanel.newOperation();
}//GEN-LAST:event_newOperationMenuItemActionPerformed

	private void openOperationMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openOperationMenuItemActionPerformed
	{//GEN-HEADEREND:event_openOperationMenuItemActionPerformed
		viewPanel.domain.load();
}//GEN-LAST:event_openOperationMenuItemActionPerformed

	private void closeOperationMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeOperationMenuItemActionPerformed
	{//GEN-HEADEREND:event_closeOperationMenuItemActionPerformed
		viewPanel.closeOperation();
}//GEN-LAST:event_closeOperationMenuItemActionPerformed

	private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveMenuItemActionPerformed
	{//GEN-HEADEREND:event_saveMenuItemActionPerformed
		viewPanel.domain.save();
}//GEN-LAST:event_saveMenuItemActionPerformed

	private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveAsMenuItemActionPerformed
	{//GEN-HEADEREND:event_saveAsMenuItemActionPerformed
		viewPanel.domain.saveAs();
}//GEN-LAST:event_saveAsMenuItemActionPerformed

	private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitMenuItemActionPerformed
	{//GEN-HEADEREND:event_exitMenuItemActionPerformed
		viewPanel.quit(true);
}//GEN-LAST:event_exitMenuItemActionPerformed

	private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_fileMenuMenuSelected
	{//GEN-HEADEREND:event_fileMenuMenuSelected
		if(viewPanel.initLoading)
		{
			for(int i = 0; i < fileMenu.getMenuComponentCount(); ++i)
			{
				fileMenu.getMenuComponent(i).setEnabled(false);
			}
		}
		else
		{
			newOperationMenuItem.setEnabled(true);
			openOperationMenuItem.setEnabled(true);
			exitMenuItem.setEnabled(true);

			if(viewPanel.domain.operationFile != null)
			{
				closeOperationMenuItem.setEnabled(true);
				saveAsMenuItem.setEnabled(true);
				if(viewPanel.domain.isChanged)
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
				closeOperationMenuItem.setEnabled(false);
				saveMenuItem.setEnabled(false);
				saveAsMenuItem.setEnabled(false);
			}
		}
}//GEN-LAST:event_fileMenuMenuSelected

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem checkForUpdatesMenuItem;
    private javax.swing.JMenuItem closeOperationMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JPopupMenu.Separator fileSeparator1;
    private javax.swing.JMenuItem helpContentsMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPopupMenu.Separator helpSeparator1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newOperationMenuItem;
    private javax.swing.JMenuItem openOperationMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    // End of variables declaration//GEN-END:variables
}
