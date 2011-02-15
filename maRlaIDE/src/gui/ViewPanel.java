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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import problem.DataSet;
import problem.DataSource;
import problem.MarlaException;
import operation.Operation;
import operation.Operation.PromptType;
import operation.OperationException;
import operation.OperationXML;
import problem.SubProblem;
import r.RProcessorException;
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
	/** The default text for the New Problem Wizard.*/
	public static final String welcomeNewText = "<html>The New Problem Wizard will walk you through the setup of a new "
			+ "problem as it appears in your textbook.<br /><br />You will first be asked where you would like to save "
			+ "problem to.  Then, you will be able to give the problem a description and add sub problems (for "
			+ "instance, parts a., b., and c.).<br /><br />The New Problem Wizard also allows you to enter data set values "
			+ "manually or import the values from a CSV file.<br /><br />These values are not final; if you need to edit "
			+ "the problem or any of the data sets at any point in the future, you can by selecting \"Edit Problem\", "
			+ "\"New Data Set\", or \"Edit Data Set\" from the \"Problem\" menu.</html>";
	/** The default text for the New Problem Wizard when it is in edit mode.*/
	public static final String welcomeEditText = "<html>From here you can edit the details of your already existing problem."
			+ "<br /><br />Changing the problem's name or location will create a separate copy of your problem, similar to "
			+ "a \"Save As ...\"<br /><br />You will be able to change the problem's description and add "
			+ "more or remove current sub problems.<br /><br />Values given for the data sets can be changed.  If these "
			+ "values are already interacting with statistical interactions in the workspace, the results will be updated with "
			+ "new values when this dialog is closed.  More data sets can be added or current data sets may be removed.</html>";

	/** The main frame of a stand-alone application.*/
    public MainFrame mainFrame;
	/** The domain object reference performs generic actions specific to the GUI.*/
    protected Domain domain = new Domain (this);
	/** The set of operations contained in the XML file.*/
	private List<String> operations;
	/** The data set being dragged.*/
	protected JComponent draggingComponent = null;
	/** The component currently being hovered over.*/
	private JComponent hoveredComponent = null;
	/** The component that has been right-clicked on.*/
	private JComponent rightClickedComponent = null;
	/** The x-offset for dragging an item*/
	protected int xDragOffset = -1;
	/** The y-offset for dragging an item*/
	protected int yDragOffset = -1;
	/** The default file filter for a JFileChooser open dialog.*/
	protected FileFilter defaultFilter;
	/** The extensions file filter for CSV files.*/
	protected ExtensionFileFilter csvFilter = new ExtensionFileFilter ("Comma Separated Values (.csv, .txt)", new String[] {"CSV", "TXT"});
	/** The extensions file filter for CSV files.*/
	protected ExtensionFileFilter marlaFilter = new ExtensionFileFilter ("The maRla Project Files (.marla)", new String[] {"MARLA"});
	/** The extensions file filter for PDF files.*/
	protected ExtensionFileFilter pdfFilter = new ExtensionFileFilter ("PDF Files (.pdf)", new String[] {"PDF"});
	/** The extensions file filter for LaTeX files.*/
	protected ExtensionFileFilter latexFilter = new ExtensionFileFilter ("LaTeX Sweave Files (.rnw)", new String[] {"RNW"});
	/** The point in the view where the answer dialog shall appear.*/
	private Point answerDialogLocation = null;
	/** The width between two operations/data sets.*/
	private final int SPACE_WIDTH = 130;
	/** The height between two operations/data sets.*/
	private final int SPACE_HEIGHT = 30;
	/** No border.*/
	private final Border NO_BORDER = BorderFactory.createEmptyBorder();
	/** A red border display.*/
	private final Border RED_BORDER = BorderFactory.createLineBorder(Color.RED);
	/** A blue border display.*/
	private final Border BLUE_BORDER = BorderFactory.createLineBorder(Color.BLUE);
	/** A black border display.*/
	private final Border BLACK_BORDER = BorderFactory.createLineBorder(Color.BLACK);
	/** The source object for draggable assignments and events.*/
    public final DragSource DRAG_SOURCE = new DragSource ();
	/** The drag-and-drop listener for assignments and events.*/
    public final DragDrop DND_LISTENER = new DragDrop (this);
	/** The New Problem Wizard dialog.*/
	public final NewProblemWizardDialog NEW_PROBLEM_WIZARD_DIALOG = new NewProblemWizardDialog (this, domain);

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
		// Remap shortcut keys to system defaults
		Toolkit.getDefaultToolkit ().addAWTEventListener (new AWTEventListener ()
		{
			@Override
			public void eventDispatched(AWTEvent event)
			{
				KeyEvent kev = (KeyEvent) event;
				if (kev.getID () == KeyEvent.KEY_PRESSED || kev.getID () == KeyEvent.KEY_RELEASED || kev.getID () == KeyEvent.KEY_PRESSED)
				{
					if ((kev.getModifiersEx () & KeyEvent.META_DOWN_MASK) != 0 && !((kev.getModifiersEx () & KeyEvent.CTRL_DOWN_MASK) != 0))
					{
						kev.consume ();
						KeyEvent fake = new KeyEvent (kev.getComponent (), kev.getID (), kev.getWhen (), (kev.getModifiersEx () & ~KeyEvent.META_DOWN_MASK) | KeyEvent.CTRL_DOWN_MASK, kev.getKeyCode (), kev.getKeyChar ());
						Toolkit.getDefaultToolkit ().getSystemEventQueue ().postEvent (fake);
					}
				}
			}
		}, KeyEvent.KEY_EVENT_MASK);

		workspacePanel.setDropTarget (new DropTarget (workspacePanel, DnDConstants.ACTION_MOVE, DND_LISTENER));

		domain.loadSaveThread = new LoadSaveThread (this, domain);
		// Launch the save thread
		domain.loadSaveThread.start ();
		domain.setLoadSaveThread (domain.loadSaveThread);

		// Initially, simply display the welcome card until a problem is created new or loaded
		emptyPalettePanel.setVisible (true);
		componentsPanel.setVisible (false);
		preWorkspacePanel.setVisible (true);
		workspacePanel.setVisible (false);

		// Retrieve the default file filter from the JFileChooser before it is ever changed
		defaultFilter = openChooserDialog.getFileFilter();

		// Set custom behavior of JFileChooser
		JPanel access = (JPanel) ((JPanel) openChooserDialog.getComponent(3)).getComponent(3);
		((JButton) access.getComponent (1)).setToolTipText ("Cancel open");
		access = (JPanel) ((JPanel) saveChooserDialog.getComponent(3)).getComponent(3);
		((JButton) access.getComponent (1)).setToolTipText ("Cancel save");
    }

	/**
	 * Reloads the palette from the XML file.
	 */
	protected void reloadOperations()
	{
		try
		{
			OperationXML.loadXML();
			loadOperations ();
		}
		catch (MarlaException ex)
		{
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Reload Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Load the operations into the palette.
	 *
	 * @throws MarlaException Throws any Marla exceptions to the calling function.
	 */
	protected void loadOperations() throws MarlaException
	{
		operations = Operation.getAvailableOperationsList();

		// Add all operation types to the palette, adding listeners to the labels as we go
		for (int i = 0; i < operations.size (); ++i)
		{
			try
			{
				final Operation operation = Operation.createOperation(operations.get (i));
				DRAG_SOURCE.createDefaultDragGestureRecognizer (operation, DnDConstants.ACTION_MOVE, DND_LISTENER);

				if (i % 2 == 0)
				{
					leftPanel.add (operation);
				}
				else
				{
					rightPanel.add (operation);
				}
				operation.addMouseListener(new MouseAdapter ()
				{
					@Override
					public void mousePressed(MouseEvent evt)
					{
						xDragOffset = (int) evt.getLocationOnScreen ().getX () - (int) operation.getLocationOnScreen ().getX ();
						yDragOffset = (int) evt.getLocationOnScreen ().getY () - (int) operation.getLocationOnScreen ().getY ();
					}
				});
			}
			catch (OperationException ex)
			{
				// Unable to load, not a real operation
				System.err.println("Error loading operation '" + operations.get(i) + "'");
			}
		}
	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        openChooserDialog = new javax.swing.JFileChooser();
        saveChooserDialog = new javax.swing.JFileChooser();
        answerDialog = new javax.swing.JDialog();
        answerPanel = new javax.swing.JPanel();
        rightClickMenu = new javax.swing.JPopupMenu();
        solutionMenuItem = new javax.swing.JMenuItem();
        subProblemSubMenu = new javax.swing.JMenu();
        toolBar = new javax.swing.JToolBar();
        componentsCardPanel = new javax.swing.JPanel();
        emptyPalettePanel = new javax.swing.JPanel();
        componentsPanel = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        rightPanel = new javax.swing.JPanel();
        workspaceSplitPane = new javax.swing.JSplitPane();
        workspaceCardPanel = new javax.swing.JPanel();
        preWorkspacePanel = new javax.swing.JPanel();
        preWorkspaceLabel = new javax.swing.JLabel();
        workspacePanel = new WorkspacePanel (this);
        trashCan = new javax.swing.JLabel();
        trayPanel = new javax.swing.JPanel();
        outputScrollPane = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();

        openChooserDialog.setApproveButtonToolTipText("Open selected folder");
        openChooserDialog.setDialogTitle("Browse Problem Location");
        openChooserDialog.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        saveChooserDialog.setApproveButtonToolTipText("Save as selected file");
        saveChooserDialog.setDialogTitle("Save As Problem Location");
        saveChooserDialog.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);

        answerDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        answerDialog.setTitle("Solution to Point");
        answerDialog.setAlwaysOnTop(true);
        answerDialog.setUndecorated(true);
        answerDialog.getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        answerPanel.setLayout(new javax.swing.BoxLayout(answerPanel, javax.swing.BoxLayout.PAGE_AXIS));
        answerDialog.getContentPane().add(answerPanel);

        solutionMenuItem.setText("Solution");
        solutionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                solutionMenuItemActionPerformed(evt);
            }
        });
        rightClickMenu.add(solutionMenuItem);

        subProblemSubMenu.setText("Tie to Sub Problem");
        subProblemSubMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
                subProblemSubMenuMenuDeselected(evt);
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                subProblemSubMenuMenuSelected(evt);
            }
        });
        rightClickMenu.add(subProblemSubMenu);

        setLayout(new java.awt.BorderLayout());

        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setPreferredSize(new java.awt.Dimension(8, 30));
        add(toolBar, java.awt.BorderLayout.NORTH);

        componentsCardPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Palette", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N
        componentsCardPanel.setPreferredSize(new java.awt.Dimension(220, 592));
        componentsCardPanel.setLayout(new java.awt.CardLayout());

        org.jdesktop.layout.GroupLayout emptyPalettePanelLayout = new org.jdesktop.layout.GroupLayout(emptyPalettePanel);
        emptyPalettePanel.setLayout(emptyPalettePanelLayout);
        emptyPalettePanelLayout.setHorizontalGroup(
            emptyPalettePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 208, Short.MAX_VALUE)
        );
        emptyPalettePanelLayout.setVerticalGroup(
            emptyPalettePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 241, Short.MAX_VALUE)
        );

        componentsCardPanel.add(emptyPalettePanel, "card3");

        componentsPanel.setLayout(new java.awt.GridLayout(1, 2));

        leftPanel.setLayout(new javax.swing.BoxLayout(leftPanel, javax.swing.BoxLayout.PAGE_AXIS));
        componentsPanel.add(leftPanel);

        rightPanel.setLayout(new javax.swing.BoxLayout(rightPanel, javax.swing.BoxLayout.PAGE_AXIS));
        componentsPanel.add(rightPanel);

        componentsCardPanel.add(componentsPanel, "card2");

        add(componentsCardPanel, java.awt.BorderLayout.EAST);

        workspaceSplitPane.setDividerLocation(450);
        workspaceSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        workspaceSplitPane.setResizeWeight(1.0);

        workspaceCardPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        workspaceCardPanel.setLayout(new java.awt.CardLayout());

        preWorkspacePanel.setBackground(new java.awt.Color(204, 204, 204));

        preWorkspaceLabel.setFont(new java.awt.Font("Verdana", 1, 14));
        preWorkspaceLabel.setForeground(new java.awt.Color(102, 102, 102));
        preWorkspaceLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        preWorkspaceLabel.setText("<html><div align=\"center\">To get started, load a previous problem or use the<br /><em>New Problem Wizard</em> to create a new problem</div></html>");

        org.jdesktop.layout.GroupLayout preWorkspacePanelLayout = new org.jdesktop.layout.GroupLayout(preWorkspacePanel);
        preWorkspacePanel.setLayout(preWorkspacePanelLayout);
        preWorkspacePanelLayout.setHorizontalGroup(
            preWorkspacePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(preWorkspacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(preWorkspaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                .addContainerGap())
        );
        preWorkspacePanelLayout.setVerticalGroup(
            preWorkspacePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(preWorkspacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(preWorkspaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addContainerGap())
        );

        workspaceCardPanel.add(preWorkspacePanel, "card3");

        workspacePanel.setBackground(new java.awt.Color(255, 255, 255));
        workspacePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                workspacePanelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                workspacePanelMouseReleased(evt);
            }
        });
        workspacePanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                workspacePanelComponentResized(evt);
            }
        });
        workspacePanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                workspacePanelMouseDragged(evt);
            }
        });
        workspacePanel.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                workspacePanelComponentAdded(evt);
            }
        });
        workspacePanel.setLayout(null);

        trashCan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/trash.png"))); // NOI18N
        workspacePanel.add(trashCan);
        trashCan.setBounds(140, 220, 32, 40);

        workspaceCardPanel.add(workspacePanel, "card2");

        workspaceSplitPane.setTopComponent(workspaceCardPanel);

        outputScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        outputTextArea.setColumns(20);
        outputTextArea.setEditable(false);
        outputTextArea.setFont(new java.awt.Font("Monospaced", 0, 12));
        outputTextArea.setRows(5);
        outputScrollPane.setViewportView(outputTextArea);

        org.jdesktop.layout.GroupLayout trayPanelLayout = new org.jdesktop.layout.GroupLayout(trayPanel);
        trayPanel.setLayout(trayPanelLayout);
        trayPanelLayout.setHorizontalGroup(
            trayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 178, Short.MAX_VALUE)
            .add(trayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(outputScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE))
        );
        trayPanelLayout.setVerticalGroup(
            trayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 22, Short.MAX_VALUE)
            .add(trayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(outputScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, Short.MAX_VALUE))
        );

        workspaceSplitPane.setBottomComponent(trayPanel);

        add(workspaceSplitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

	private void workspacePanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMouseDragged
		dragInWorkspace(evt);
	}//GEN-LAST:event_workspacePanelMouseDragged

	private void workspacePanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMousePressed
		if (evt.getButton () == 0 || evt.getButton () == MouseEvent.BUTTON1)
		{
			JComponent component = (JComponent) workspacePanel.getComponentAt (evt.getPoint ());
			if (component != null &&
					component != workspacePanel &&
					component != trashCan)
			{
				draggingComponent = component;
				draggingComponent.setBorder (RED_BORDER);
				draggingComponent.setSize (component.getPreferredSize ());
				if (draggingComponent instanceof Operation)
				{
					DataSet parentData = null;
					if (((Operation) draggingComponent).getParentData() != null)
					{
						parentData = (DataSet) (((Operation) draggingComponent).getRootDataSource());
					}
					try
					{
						Operation childOperation = null;
						if (((Operation) draggingComponent).getOperationCount() > 0)
						{
							childOperation = ((Operation) draggingComponent).getOperation(0);
						}
						DataSource parent = ((Operation) draggingComponent).getParentData();
						if (parent != null)
						{
							parent.removeOperation((Operation) draggingComponent);
							workspacePanel.setComponentZOrder(draggingComponent, workspacePanel.getComponentCount() - 1);
						}
						if (childOperation != null)
						{
							parent.addOperation(childOperation);
						}
					}
					catch(MarlaException ex)
					{
						Domain.logger.add (ex);
					}
					catch(NullPointerException ex)
					{
						Domain.logger.add (ex);
					}
					xDragOffset = evt.getX() - draggingComponent.getX();
					yDragOffset = evt.getY() - draggingComponent.getY();
					if (parentData != null)
					{
						rebuildTree (parentData);
					}
					workspacePanel.repaint ();
				}
				else
				{
					xDragOffset = evt.getX() - draggingComponent.getX();
					yDragOffset = evt.getY() - draggingComponent.getY();
				}
				domain.problem.markChanged();
			}
		}
	}//GEN-LAST:event_workspacePanelMousePressed

	private void workspacePanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMouseReleased
		if (evt.getButton () == 0 || evt.getButton () == MouseEvent.BUTTON1)
		{
			if (draggingComponent != null)
			{
				if (trashCan.getBounds().contains(evt.getPoint()))
				{
					if (draggingComponent instanceof DataSet)
					{
						DataSet dataSet = (DataSet) draggingComponent;
						for (int i = 0; i < dataSet.getOperationCount(); ++i)
						{
							Operation operation = dataSet.getOperation(i);
							List<Operation> children = operation.getAllChildOperations();
							for (int j = 0; j < children.size(); ++j)
							{
								workspacePanel.remove (children.get (j));
							}
							workspacePanel.remove (operation);
						}
					}
					workspacePanel.remove (draggingComponent);
				}
				else
				{
					if (draggingComponent instanceof Operation)
					{
						try
						{
							drop((Operation) draggingComponent, false, evt.getPoint());
						}
						catch(OperationException ex)
						{
							Domain.logger.add (ex);
						}
						catch(RProcessorException ex)
						{
							Domain.logger.add (ex);
						}
						catch(MarlaException ex)
						{
							Domain.logger.add (ex);
						}
					}
					draggingComponent.setBorder (NO_BORDER);
					draggingComponent.setSize (draggingComponent.getPreferredSize ());
				}
				draggingComponent = null;
			}
			workspacePanel.repaint ();
		}
		else if (evt.getButton () == MouseEvent.BUTTON3)
		{
			JComponent component = (JComponent) workspacePanel.getComponentAt (evt.getPoint ());
			if (component != null &&
					component != workspacePanel &&
					component != trashCan)
			{
				rightClickedComponent = component;
				answerDialogLocation = evt.getLocationOnScreen();

				solutionMenuItem.setEnabled (true);
				subProblemSubMenu.removeAll ();
				if (domain.problem.getSubProblemCount() == 0)
				{
					subProblemSubMenu.setEnabled (false);
				}
				else
				{
					subProblemSubMenu.setEnabled (true);
					for (int i = 0; i < domain.problem.getSubProblemCount(); ++i)
					{
						final SubProblem subProblem = domain.problem.getSubProblem(i);
						String name = subProblem.getSubproblemID();
						/*if ()
						{
						    // TODO implement this condition
							name = "(Current) " + name;
						}*/
						JMenuItem item = new JMenuItem (name);
						item.addActionListener (new ActionListener ()
						{
							@Override
							public void actionPerformed(ActionEvent evt)
							{
								DataSource source = (DataSource) rightClickedComponent;
								if (rightClickedComponent instanceof Operation)
								{
									source = ((Operation) rightClickedComponent).getRootDataSource().getOperation (((Operation) rightClickedComponent).getIndexFromDataSet ());
									List<Operation> children = source.getAllChildOperations();
									if (children.size () > 0)
									{
										subProblem.setSolutionEnd(source.getAllChildOperations().get (source.getAllChildOperations().size () - 1));
									}
									else
									{
										subProblem.setSolutionEnd(source);
									}
								}
								else
								{
									subProblem.setSolutionEnd(source);
								}
								subProblem.setSolutionStart(source);
							}
						});
						subProblemSubMenu.add (item);
					}
				}
				rightClickMenu.show (workspacePanel, evt.getX (), evt.getY());
			}
			else
			{
				rightClickedComponent = null;
				solutionMenuItem.setEnabled (false);
				subProblemSubMenu.setEnabled (false);
				subProblemSubMenu.removeAll ();
			}
		}
	}//GEN-LAST:event_workspacePanelMouseReleased

	private void solutionMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_solutionMenuItemActionPerformed
	{//GEN-HEADEREND:event_solutionMenuItemActionPerformed
		if (rightClickedComponent != null)
		{
			try
			{
				if (rightClickedComponent instanceof Operation)
				{
					domain.ensureRequirementsMet((Operation) rightClickedComponent);
				}

				answerPanel.removeAll ();
				if (rightClickedComponent instanceof Operation && ((Operation) rightClickedComponent).hasPlot())
				{
					JLabel label = new JLabel ("");
					label.setIcon(new ImageIcon (((Operation) rightClickedComponent).getPlot()));
					answerPanel.add (label);
				}
				else
				{
					answerPanel.add(new JLabel("<html>" + ((DataSource) rightClickedComponent).toHTML() + "</html>"));
				}

				if (rightClickedComponent instanceof Operation)
				{
					answerDialog.setTitle ("Solution to Point");
				}
				else if (rightClickedComponent instanceof DataSet)
				{
					answerDialog.setTitle ("Data Set Summary");
				}
				answerDialog.pack ();
				answerDialog.setLocation (answerDialogLocation);
				answerDialog.setVisible (true);
			}
			catch (MarlaException ex)
			{
				Domain.logger.add (ex);
			}
		}
	}//GEN-LAST:event_solutionMenuItemActionPerformed

	private void subProblemSubMenuMenuSelected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_subProblemSubMenuMenuSelected
	{//GEN-HEADEREND:event_subProblemSubMenuMenuSelected
		if (rightClickedComponent != null && subProblemSubMenu.isEnabled())
		{
			rightClickedComponent.setBorder (BLACK_BORDER);
			rightClickedComponent.setSize (rightClickedComponent.getPreferredSize ());
			if (rightClickedComponent instanceof Operation)
			{
				DataSource source = ((Operation) rightClickedComponent).getRootDataSource().getOperation (((Operation) rightClickedComponent).getIndexFromDataSet ());
				((JComponent) source).setBorder (BLACK_BORDER);
				((JComponent) source).setSize (((JComponent) source).getPreferredSize ());
				List<Operation> tempOperations = source.getRootDataSource().getOperation (((Operation) source).getIndexFromDataSet ()).getAllChildOperations();
				for (int i = 0; i < tempOperations.size (); ++i)
				{
					tempOperations.get (i).setBorder (BLACK_BORDER);
					tempOperations.get (i).setSize (tempOperations.get (i).getPreferredSize ());
				}
			}
			else
			{
				DataSet root = (DataSet) rightClickedComponent;
				root.setBorder (BLACK_BORDER);
				root.setSize (root.getPreferredSize());
				for (int i = 0; i < root.getOperationCount(); ++i)
				{
					Operation operation = root.getOperation (i);
					operation.setBorder (BLACK_BORDER);
					operation.setSize (operation.getPreferredSize());
					List<Operation> tempOperations = operation.getAllChildOperations();
					for (int j = 0; j < tempOperations.size (); ++j)
					{
						tempOperations.get (j).setBorder (BLACK_BORDER);
						tempOperations.get (j).setSize (tempOperations.get (j).getPreferredSize ());
					}
				}
			}
		}
	}//GEN-LAST:event_subProblemSubMenuMenuSelected

	private void subProblemSubMenuMenuDeselected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_subProblemSubMenuMenuDeselected
	{//GEN-HEADEREND:event_subProblemSubMenuMenuDeselected
		if (rightClickedComponent != null && subProblemSubMenu.isEnabled())
		{
			rightClickedComponent.setBorder (NO_BORDER);
			rightClickedComponent.setSize (rightClickedComponent.getPreferredSize ());
			if (rightClickedComponent instanceof Operation)
			{
				DataSource source = ((Operation) rightClickedComponent).getRootDataSource().getOperation (((Operation) rightClickedComponent).getIndexFromDataSet ());
				((JComponent) source).setBorder (NO_BORDER);
				((JComponent) source).setSize (((JComponent) source).getPreferredSize ());
				List<Operation> tempOperations = source.getRootDataSource().getOperation (((Operation) source).getIndexFromDataSet ()).getAllChildOperations();
				for (int i = 0; i < tempOperations.size (); ++i)
				{
					tempOperations.get (i).setBorder (NO_BORDER);
					tempOperations.get (i).setSize (tempOperations.get (i).getPreferredSize ());
				}
			}
			else
			{
				DataSet root = (DataSet) rightClickedComponent;
				root.setBorder (NO_BORDER);
				root.setSize (root.getPreferredSize());
				for (int i = 0; i < root.getOperationCount(); ++i)
				{
					Operation operation = root.getOperation (i);
					operation.setBorder (NO_BORDER);
					operation.setSize (operation.getPreferredSize());
					List<Operation> tempOperations = operation.getAllChildOperations();
					for (int j = 0; j < tempOperations.size (); ++j)
					{
						tempOperations.get (j).setBorder (NO_BORDER);
						tempOperations.get (j).setSize (tempOperations.get (j).getPreferredSize ());
					}
				}
			}
		}
	}//GEN-LAST:event_subProblemSubMenuMenuDeselected

	private void workspacePanelComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_workspacePanelComponentResized
	{//GEN-HEADEREND:event_workspacePanelComponentResized
		trashCan.setLocation (workspacePanel.getWidth () - 40, workspacePanel.getHeight () - 40);
	}//GEN-LAST:event_workspacePanelComponentResized

	private void workspacePanelComponentAdded(java.awt.event.ContainerEvent evt)//GEN-FIRST:event_workspacePanelComponentAdded
	{//GEN-HEADEREND:event_workspacePanelComponentAdded
		workspacePanel.setComponentZOrder(trashCan, workspacePanel.getComponentCount() - 1);
	}//GEN-LAST:event_workspacePanelComponentAdded

	/**
	 * Manage drag events within the workspace panel
	 *
	 * @param evt The mouse event for the drag.
	 */
	protected void dragInWorkspace(MouseEvent evt)
	{
		if (evt.getButton() == 0 || evt.getButton () == MouseEvent.BUTTON1)
		{
			if (hoveredComponent != null)
			{
				hoveredComponent.setBorder (NO_BORDER);
				hoveredComponent.setSize (hoveredComponent.getPreferredSize ());
				hoveredComponent = null;
			}

			Component component = workspacePanel.getComponentAt (evt.getPoint ());
			if (component != null &&
					component != workspacePanel &&
					component != trashCan &&
					component != draggingComponent)
			{
				if ((component instanceof Operation && ((Operation) component).getParent () != null) ||
						component instanceof DataSet)
				{
					hoveredComponent = (JComponent) workspacePanel.getComponentAt (evt.getPoint ());
					hoveredComponent.setBorder (BLACK_BORDER);
					hoveredComponent.setSize (hoveredComponent.getPreferredSize ());
				}
				else if (hoveredComponent != null)
				{
					hoveredComponent.setBorder (NO_BORDER);
					hoveredComponent.setSize (hoveredComponent.getPreferredSize ());
					hoveredComponent = null;
				}
			}

			if (draggingComponent != null)
			{
				if (draggingComponent instanceof Operation)
				{
					draggingComponent.setLocation(evt.getX() - xDragOffset, evt.getY() - yDragOffset);
				}
				else if(draggingComponent instanceof DataSet)
				{
					draggingComponent.setLocation(evt.getX() - xDragOffset, evt.getY() - yDragOffset);
					rebuildTree ((DataSet) draggingComponent);
				}
			}
		}
	}

	/**
	 * Rebuild the tree in the interface for the given data set.
	 *
	 * @param dataSet The data set to rebuild in the interface.
	 */
	protected void rebuildTree(DataSet dataSet)
	{
		if (dataSet.getOperationCount () > 0)
		{
			dataSet.setSize (dataSet.getPreferredSize ());
			// Ensure updated sizes
			for (int i = 0; i < dataSet.getOperationCount(); ++i)
			{
				dataSet.getOperation (i).setSize (dataSet.getOperation (i).getPreferredSize());
			}

			int center = (dataSet.getX () + dataSet.getX () + dataSet.getWidth ()) / 2;
			int width = ((dataSet.getOperationCount () - 1) * SPACE_WIDTH) + dataSet.getOperation (dataSet.getOperationCount () - 1).getWidth ();
			int xStart = center - width / 2;

			for (int i = 0; i < dataSet.getOperationCount (); ++i)
			{
				Operation operation = dataSet.getOperation (i);
				operation.setLocation (xStart + (i * SPACE_WIDTH), dataSet.getY () + SPACE_HEIGHT);
				List<Operation> children = operation.getAllChildOperations();
				for (int j = 0; j < children.size (); ++j)
				{
					children.get (j).setSize (children.get (j).getPreferredSize ());
					int parentWidth;
					if (j > 0)
					{
						parentWidth = children.get (j - 1).getWidth ();
					}
					else
					{
						parentWidth = dataSet.getOperation (i).getWidth ();
					}
					children.get (j).setLocation (((xStart + (i * SPACE_WIDTH) + xStart + (i * SPACE_WIDTH) + parentWidth) / 2) - (children.get (j).getWidth () / 2),
							dataSet.getY () + ((j + 2) * SPACE_HEIGHT));
				}
			}
		}

		// Redraw everything
		workspacePanel.repaint();
	}

	/**
	 * Drop the given component at the current location that the mouse is hovering at in the Workspace Panel.
	 *
	 * @param operation The operation to drop.
	 * @param duplicate True if the drop should create a new instance of the given operation, false if it should drop the given instance.
	 * @param location The location of the mouse pointer.
	 * @throws OperationException
	 * @throws RProcessorException
	 * @throws MarlaException
	 */
	protected void drop(Operation operation, boolean duplicate, Point location) throws OperationException, RProcessorException, MarlaException
	{
		JComponent component = (JComponent) workspacePanel.getComponentAt(location);
		if (component != null &&
				component != workspacePanel &&
				component != operation &&
				component != trashCan &&
				(component instanceof DataSet || component instanceof Operation))
		{
			final Operation newOperation;
			if (duplicate)
			{
				newOperation = Operation.createOperation(operation.getName());
			}
			else
			{
				newOperation = operation;
			}
			int x = component.getX ();
			int y = component.getY ();

			DataSet dataSet = null;
			if (component instanceof Operation)
			{
				if (((Operation) component).getParentData () != null)
				{
					y = component.getY () + SPACE_HEIGHT;
					Operation dropOperation = (Operation) component;
					if (dropOperation.getRootDataSource () instanceof DataSet)
					{
						dataSet = (DataSet) dropOperation.getRootDataSource();
					}
					if (dropOperation.getOperationCount() > 0)
					{
						dropOperation.addOperation (newOperation);
						Operation childOperation = dropOperation.getOperation (0);
						childOperation.setParentData (newOperation);
						childOperation.setLocation (childOperation.getX (), childOperation.getY () + SPACE_HEIGHT);
					}
					else
					{
						dropOperation.addOperation (newOperation);
					}
				}
			}
			else if(component instanceof DataSet)
			{
				y = component.getY () + SPACE_HEIGHT;
				dataSet = (DataSet) component;
				dataSet.addOperation(newOperation);
				if (dataSet.getOperationCount() > 1)
				{
					x += (dataSet.getOperationCount () * SPACE_WIDTH);
				}
			}

			if ((component instanceof Operation && ((Operation) component).getParentData () != null) || component instanceof DataSet)
			{
				newOperation.setBounds (x, y, newOperation.getPreferredSize().width, newOperation.getPreferredSize().height);
			}
			workspacePanel.add (newOperation);
			if (dataSet != null)
			{
				rebuildTree(dataSet);
			}
			workspacePanel.repaint();
		}
		else if (component != trashCan)
		{
			final Operation newOperation;
			if (duplicate)
			{
				newOperation = Operation.createOperation(operation.getName());
			}
			else
			{
				newOperation = operation;
			}
			
			newOperation.setBounds ((int) location.getX () - xDragOffset, (int) location.getY () - yDragOffset, newOperation.getPreferredSize().width, newOperation.getPreferredSize().height);
			workspacePanel.add (newOperation);
			workspacePanel.repaint ();
		}
		if (hoveredComponent != null)
		{
			hoveredComponent.setBorder (NO_BORDER);
		}
	}

	public void getRequiredInfoDialog(final Operation newOperation) throws MarlaException
	{
		// Create the dialog which will be launched to ask about requirements
		final List<Object[]> prompt = newOperation.getRequiredInfoPrompt();
		final JDialog dialog = new JDialog ();
		JPanel panel = new JPanel ();
		panel.setLayout (new GridLayout (prompt.size () + 1, 2));

		dialog.setTitle (newOperation.getName() + ": Information Required");
		dialog.setModal (true);
		dialog.add (panel);

		// This array will contain references to objects that will hold the values
		final List<Object> valueComponents = new ArrayList<Object> ();

		// Fill dialog with components
		for (int i = 0; i < prompt.size(); ++i)
		{
			Object[] components = prompt.get (i);
			if (components[0] == PromptType.STRING || components[0] == PromptType.NUMERIC)
			{
				JPanel tempPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
				JLabel label = new JLabel (components[2].toString ());
				JTextField textField = new JTextField ();
				textField.setPreferredSize(new Dimension (150, textField.getPreferredSize().height));
				tempPanel.add (label);
				tempPanel.add (textField);
				valueComponents.add (textField);
				panel.add (tempPanel);
			}
			else if(components[0] == PromptType.CHECKBOX)
			{
				JPanel tempPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
				JCheckBox checkBox = new JCheckBox (components[2].toString ());
				JLabel label = new JLabel ("");
				tempPanel.add (checkBox);
				tempPanel.add (label);
				valueComponents.add (checkBox);
				panel.add (tempPanel);
			}
			else if(components[0] == PromptType.COMBO || components[0] == PromptType.COLUMN)
			{
				JPanel tempPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
				JLabel label = new JLabel (components[2].toString ());
				DefaultComboBoxModel model = new DefaultComboBoxModel ((Object[]) components[3]);
				JComboBox comboBox = new JComboBox (model);
				tempPanel.add (label);
				tempPanel.add (comboBox);
				valueComponents.add (comboBox);
				panel.add (tempPanel);
			}
		}

		JButton doneButton = new JButton ("Done");
		final ViewPanel viewPanel = this;
		// When the user is done with the assumptions, forms will be validated and their values stored into the operation before continuing
		doneButton.addActionListener (new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				List<Object> values = new ArrayList<Object> ();
				boolean pass = true;
				for (int i = 0; i < prompt.size (); ++i)
				{
					if (prompt.get (i)[0] == PromptType.NUMERIC)
					{
						try
						{
							values.add (Double.parseDouble (((JTextField) valueComponents.get (i)).getText ()));
						}
						catch (NumberFormatException ex)
						{
							// If the users input was not valid, the form is not accepted and the dialog will not close
							((JTextField) valueComponents.get (i)).requestFocus();
							((JTextField) valueComponents.get (i)).selectAll();
							JOptionPane.showMessageDialog(viewPanel, "You must enter a valid numerical value.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
							pass = false;
						}
					}
					else if(prompt.get(i)[0] == PromptType.STRING)
					{
						values.add (((JTextField) valueComponents.get (i)).getText ());
					}
					else if(prompt.get (i)[0] == PromptType.CHECKBOX)
					{
						values.add (Boolean.valueOf (((JCheckBox) valueComponents.get (i)).isSelected ()));
					}
					else if(prompt.get (i)[0] == PromptType.COMBO || prompt.get (i)[0] == PromptType.COLUMN)
					{
						values.add (((JComboBox) valueComponents.get (i)).getSelectedItem());
					}
				}

				if (pass)
				{
					try
					{
						// Hide the dialog and set the data
						newOperation.setRequiredInfo(values);
						dialog.setVisible (false);
					}
					catch(MarlaException ex)
					{
						JOptionPane.showMessageDialog(viewPanel, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});
		panel.add (doneButton);

		// Display dialog
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible (true);
	}

	/**
	 * Open the problem currently stored in the problem variable.
	 */
	protected void openProblem()
	{
		if (domain.problem != null)
		{
			int numShow = getShownDataSetCount ();
			for (int i = 0; i < domain.problem.getDataCount(); ++i)
			{
				if (i > numShow - 1)
				{
					DataSet dataSet = domain.problem.getData (i);
					// Add the new data set to the workspace
					workspacePanel.add (dataSet);

					for (int j = 0; j < dataSet.getOperationCount(); ++j)
					{
						Operation operation = dataSet.getOperation(j);
						workspacePanel.add (operation);
					}
				}
			}

			componentsPanel.setVisible (true);
			emptyPalettePanel.setVisible (false);
			workspacePanel.setVisible (true);
			preWorkspacePanel.setVisible (false);

			workspacePanel.updateUI();

			mainFrame.setTitle (mainFrame.getDefaultTitle () + " - " + domain.problem.getFileName().substring (domain.problem.getFileName ().lastIndexOf (System.getProperty ("file.separator")) + 1, domain.problem.getFileName ().lastIndexOf (".")));
		}
	}

	/**
	 * Close the currently open problem in the workspace.
	 *
	 * @return If the closing of the problem should be canceled, returns false, otherwise returns true.
	 */
	protected boolean closeProblem()
	{
		if (domain.problem != null)
		{
			// Check to save changes before closing the program
			if (domain.problem.isChanged())
			{
				int response = JOptionPane.YES_OPTION;
				if (!NEW_PROBLEM_WIZARD_DIALOG.editing)
				{
					response = JOptionPane.showConfirmDialog(this,
							"Would you like to save changes to the currently open problem?",
							"Save Problem Changes",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
				}
				if (response == JOptionPane.YES_OPTION)
				{
					try
					{
						domain.problem.save();
					}
					catch (MarlaException ex)
					{
						return false;
					}
				}
				else if(response == -1 || response == JOptionPane.CANCEL_OPTION)
				{
					return false;
				}
			}

			if (!NEW_PROBLEM_WIZARD_DIALOG.editing)
			{
				workspacePanel.removeAll();

				emptyPalettePanel.setVisible (true);
				componentsPanel.setVisible (false);
				preWorkspacePanel.setVisible (true);
				workspacePanel.setVisible (false);

				domain.problem = null;

				mainFrame.setTitle (mainFrame.getDefaultTitle ());
			}
		}
		workspacePanel.repaint ();

		return true;
	}

	/**
	 * Solve the data set as the currentDataSet in the domain.
	 */
	protected void solve()
	{
		try
		{
			outputTextArea.append ("Solution:\n");
			for(int i = 0; i < domain.problem.getDataCount(); i++)
			{
				DataSource ds = domain.problem.getAnswer(i);
				if(ds instanceof Operation)
				{
					domain.ensureRequirementsMet((Operation) ds);
				}

				outputTextArea.append(ds + "\n");
			}
		}
		catch (MarlaException ex)
		{
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Computation Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Retrieves the ith data set found in the workspace panel.
	 *
	 * @param i The data set index to return.
	 * @return The data set, if it exists.
	 */
	protected DataSet getDisplayedDataSet(int i)
	{
		int count = 0;
		for (int j = 0; j < workspacePanel.getComponentCount (); ++j)
		{
			if (workspacePanel.getComponent (j) instanceof DataSet &&
					!(workspacePanel.getComponent (j) instanceof Operation))
			{
				if (count == i)
				{
					return (DataSet) workspacePanel.getComponent (j);
				}
				++count;
			}
		}
		return null;
	}

	/**
	 * Retrieve the number of data sets currently displayed in the workspace panel.
	 *
	 * @return The number of data sets currently displayed in the workspace panel.
	 */
	protected int getShownDataSetCount()
	{
		int count = 0;

		for (int i = 0; i < workspacePanel.getComponentCount (); ++i)
		{
			if (workspacePanel.getComponent (i) instanceof DataSet &&
					!(workspacePanel.getComponent (i) instanceof Operation))
			{
				++count;
			}
		}

		return count;
	}

    /**
     * Ensures all processes are terminated and that all ways of exiting the
     * application result in the same closing process.
     *
     * @param forceQuit True if System.exit should be called, false if the caller
	 * plans to terminate the application.
     */
    protected void quit(boolean forceQuit)
    {
		// Save the maRla configuration
		try
		{
			resource.Configuration.save();
		}
		catch(MarlaException ex)
		{
			Domain.logger.add(ex);
		}

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
    protected javax.swing.JDialog answerDialog;
    private javax.swing.JPanel answerPanel;
    private javax.swing.JPanel componentsCardPanel;
    protected javax.swing.JPanel componentsPanel;
    protected javax.swing.JPanel emptyPalettePanel;
    private javax.swing.JPanel leftPanel;
    protected javax.swing.JFileChooser openChooserDialog;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JTextArea outputTextArea;
    private javax.swing.JLabel preWorkspaceLabel;
    protected javax.swing.JPanel preWorkspacePanel;
    private javax.swing.JPopupMenu rightClickMenu;
    private javax.swing.JPanel rightPanel;
    protected javax.swing.JFileChooser saveChooserDialog;
    private javax.swing.JMenuItem solutionMenuItem;
    private javax.swing.JMenu subProblemSubMenu;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JLabel trashCan;
    private javax.swing.JPanel trayPanel;
    private javax.swing.JPanel workspaceCardPanel;
    protected javax.swing.JPanel workspacePanel;
    private javax.swing.JSplitPane workspaceSplitPane;
    // End of variables declaration//GEN-END:variables

}
