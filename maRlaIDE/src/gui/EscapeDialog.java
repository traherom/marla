/*
 * Get Organized - Organize your schedule, course assignments, and grades
 * Copyright (C) 2010 Alex Laird
 * alexdlaird@gmail.com
 * www.alexlaird.net
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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/** 
 * Creates a standard JDialog with the extension that closes when the
 * escape key is pressed.
 *
 * @author Alex Laird
 */
public class EscapeDialog extends JDialog
{
    /** A reference to the main panel of the application.*/
    private ViewPanel viewPanel;

    /**
     * Constructs a standard Escape Dialog.
     */
    public EscapeDialog(ViewPanel viewPanel)
    {
        super ();
		setMainPanel (viewPanel);
    }

    /**
     * Constructs an Escape Dialog with a dialog as its parent.
     *
     * @param parent The dialog to be the parent.
     */
    public EscapeDialog(JDialog parent, ViewPanel viewPanel)
    {
        super (parent);
		setMainPanel (viewPanel);
    }

    /**
     * Sets a reference to the main panel of the escape dialog.
     *
     * @param viewPanel The main panel of the escape dialog.
     */
    public final void setMainPanel(ViewPanel viewPanel)
    {
        this.viewPanel = viewPanel;
    }

    /**
     * Overrides the root pane method to create it in the same way but with the
     * added functionality that supports an escape key listener. This allows
     * the dialog to perform some exit operations when escape is pressed prior
     * to disposing the dialog, and this also allows the escape key to act
     * the same way as pressing the close button.
     *
     * @return The root pane.
     */
    @Override
    protected JRootPane createRootPane()
    {
        Action actionListener = new AbstractAction ()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                ((JDialog) rootPane.getParent ()).dispose ();
                viewPanel.setEnabled (true);
                viewPanel.requestFocus ();
            }
        };
        rootPane = new JRootPane ();
        KeyStroke stroke = KeyStroke.getKeyStroke ("ESCAPE");
        InputMap inputMap = rootPane.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put (stroke, "ESCAPE");
        rootPane.getActionMap ().put ("ESCAPE", actionListener);

        return rootPane;
    }
}
