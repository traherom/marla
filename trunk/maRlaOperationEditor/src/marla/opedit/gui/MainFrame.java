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
import java.awt.Toolkit;
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
	private final Dimension MINIMUM_WINDOW_SIZE = new Dimension(790, 400);
	/** The panel that is added to the frame.*/
	private static ViewPanel viewPanel;

	/**
	 * Constructs the frame for the stand-alone application.
	 */
	public MainFrame()
	{
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

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu2 = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconImage(new ImageIcon (getClass ().getResource ("/marla/ide/images/logo.png")).getImage ());
        getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        jMenu2.setText("Edit");
        jMenu2.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        undoMenuItem.setText("Undo");
        undoMenuItem.setEnabled(false);
        undoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(undoMenuItem);

        redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        redoMenuItem.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        redoMenuItem.setText("Redo");
        redoMenuItem.setEnabled(false);
        redoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(redoMenuItem);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoMenuItemActionPerformed
		viewPanel.undo();
	}//GEN-LAST:event_undoMenuItemActionPerformed

	private void redoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoMenuItemActionPerformed
		viewPanel.redo();
	}//GEN-LAST:event_redoMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    protected javax.swing.JMenuItem redoMenuItem;
    protected javax.swing.JMenuItem undoMenuItem;
    // End of variables declaration//GEN-END:variables
}
