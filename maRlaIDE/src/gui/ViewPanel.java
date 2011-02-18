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
import operation.OperationException;
import operation.OperationInfoCombo;
import operation.OperationInfoRequiredException;
import operation.OperationInformation;
import operation.OperationInformation.PromptType;
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

	/** The main frame of a stand-alone application.*/
    public MainFrame mainFrame;
	/** The domain object reference performs generic actions specific to the GUI.*/
    protected Domain domain = new Domain (this);
	/** The New Problem Wizard dialog.*/
	public final NewProblemWizardDialog NEW_PROBLEM_WIZARD_DIALOG = new NewProblemWizardDialog (this, domain);
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
	/** True if operation and column names are abbreviated, false otherwise.*/
	private boolean abbreviated = false;

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
				operation.setText("<html>" + operation.getDisplayString(abbreviated) + "</html>");
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
        tieSubProblemSubMenu = new javax.swing.JMenu();
        untieSubProblemSubMenu = new javax.swing.JMenu();
        menuSeparator = new javax.swing.JPopupMenu.Separator();
        rCodeMenuItem = new javax.swing.JMenuItem();
        toolBar = new javax.swing.JToolBar();
        newButton = new javax.swing.JLabel();
        openButton = new javax.swing.JLabel();
        saveButton = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jLabel1 = new javax.swing.JLabel();
        plusFontButton = new javax.swing.JLabel();
        minusFontButton = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jLabel2 = new javax.swing.JLabel();
        abbreviateButton = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        settingsButton = new javax.swing.JLabel();
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

        answerPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        answerPanel.setLayout(new javax.swing.BoxLayout(answerPanel, javax.swing.BoxLayout.PAGE_AXIS));
        answerDialog.getContentPane().add(answerPanel);

        solutionMenuItem.setText("Solution");
        solutionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                solutionMenuItemActionPerformed(evt);
            }
        });
        rightClickMenu.add(solutionMenuItem);

        tieSubProblemSubMenu.setText("Tie to Sub Problem");
        tieSubProblemSubMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
                tieSubProblemSubMenuMenuDeselected(evt);
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                tieSubProblemSubMenuMenuSelected(evt);
            }
        });
        rightClickMenu.add(tieSubProblemSubMenu);

        untieSubProblemSubMenu.setText("Untie from Sub Problem");
        untieSubProblemSubMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
                tieSubProblemSubMenuMenuDeselected(evt);
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                tieSubProblemSubMenuMenuSelected(evt);
            }
        });
        rightClickMenu.add(untieSubProblemSubMenu);
        rightClickMenu.add(menuSeparator);

        rCodeMenuItem.setText("View R Code");
        rCodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rCodeMenuItemActionPerformed(evt);
            }
        });
        rightClickMenu.add(rCodeMenuItem);

        setLayout(new java.awt.BorderLayout());

        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setPreferredSize(new java.awt.Dimension(13, 35));

        newButton.setFont(new java.awt.Font("Verdana", 0, 12));
        newButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/new.png"))); // NOI18N
        newButton.setToolTipText("New Problem");
        newButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                buttonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonMouseReleased(evt);
            }
        });
        toolBar.add(newButton);

        openButton.setFont(new java.awt.Font("Verdana", 0, 12));
        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open.png"))); // NOI18N
        openButton.setToolTipText("Open Problem");
        openButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                buttonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonMouseReleased(evt);
            }
        });
        toolBar.add(openButton);

        saveButton.setFont(new java.awt.Font("Verdana", 0, 12));
        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save.png"))); // NOI18N
        saveButton.setToolTipText("Save Problem");
        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                buttonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonMouseReleased(evt);
            }
        });
        toolBar.add(saveButton);
        toolBar.add(jSeparator1);

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel1.setText("Font Size:");
        toolBar.add(jLabel1);

        plusFontButton.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        plusFontButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/plus.png"))); // NOI18N
        plusFontButton.setToolTipText("Increase font size");
        plusFontButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                buttonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonMouseReleased(evt);
            }
        });
        toolBar.add(plusFontButton);

        minusFontButton.setFont(new java.awt.Font("Verdana", 0, 12));
        minusFontButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/minus.png"))); // NOI18N
        minusFontButton.setToolTipText("Decrease font size");
        minusFontButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                buttonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonMouseReleased(evt);
            }
        });
        toolBar.add(minusFontButton);
        toolBar.add(jSeparator3);
        toolBar.add(jLabel2);

        abbreviateButton.setFont(new java.awt.Font("Verdana", 0, 12));
        abbreviateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/unchecked.png"))); // NOI18N
        abbreviateButton.setText("Abbreviate");
        abbreviateButton.setToolTipText("Show abbreviated operation and column names");
        abbreviateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                buttonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonMouseReleased(evt);
            }
        });
        toolBar.add(abbreviateButton);
        toolBar.add(jSeparator2);

        settingsButton.setFont(new java.awt.Font("Verdana", 0, 12));
        settingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/settings.png"))); // NOI18N
        settingsButton.setToolTipText("Settings");
        settingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                buttonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonMouseReleased(evt);
            }
        });
        toolBar.add(settingsButton);

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
            .add(0, 478, Short.MAX_VALUE)
        );

        componentsCardPanel.add(emptyPalettePanel, "card3");

        componentsPanel.setLayout(new java.awt.GridLayout(1, 2));

        leftPanel.setLayout(new javax.swing.BoxLayout(leftPanel, javax.swing.BoxLayout.PAGE_AXIS));
        componentsPanel.add(leftPanel);

        rightPanel.setLayout(new javax.swing.BoxLayout(rightPanel, javax.swing.BoxLayout.PAGE_AXIS));
        componentsPanel.add(rightPanel);

        componentsCardPanel.add(componentsPanel, "card2");

        add(componentsCardPanel, java.awt.BorderLayout.EAST);

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
                .add(preWorkspaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 752, Short.MAX_VALUE)
                .addContainerGap())
        );
        preWorkspacePanelLayout.setVerticalGroup(
            preWorkspacePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(preWorkspacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(preWorkspaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                .addContainerGap())
        );

        workspaceCardPanel.add(preWorkspacePanel, "card3");

        workspacePanel.setBackground(new java.awt.Color(255, 255, 255));
        workspacePanel.addMouseListener(new java.awt.event.MouseAdapter() {
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
            .add(0, 780, Short.MAX_VALUE)
            .add(trayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(outputScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE))
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
		if (draggingComponent == null)
		{
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
								workspacePanel.setComponentZOrder(trashCan, workspacePanel.getComponentCount() - 1);
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
					domain.problem.markUnsaved();
				}
			}
		}

		dragInWorkspace(evt);
	}//GEN-LAST:event_workspacePanelMouseDragged

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
						domain.problem.removeData (dataSet);
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
						draggingComponent.setBorder (NO_BORDER);
						draggingComponent.setSize (draggingComponent.getPreferredSize ());
					}
					else
					{
						draggingComponent.setBorder (NO_BORDER);
						draggingComponent.setSize (draggingComponent.getPreferredSize ());
						rebuildTree ((DataSet) draggingComponent);
					}
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
				tieSubProblemSubMenu.removeAll ();
				untieSubProblemSubMenu.removeAll();
				for (int i = 0; i < domain.problem.getSubProblemCount(); ++i)
				{
					final SubProblem subProblem = domain.problem.getSubProblem(i);
					String name = subProblem.getSubproblemID();
					if (subProblem.isDataSourceInSolution ((DataSource) rightClickedComponent))
					{
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
										subProblem.setSolutionEnd(null);
									}
									else
									{
										subProblem.setSolutionEnd(null);
									}
								}
								else
								{
									subProblem.setSolutionEnd(null);
								}
								subProblem.setSolutionStart(null);
							}
						});
						untieSubProblemSubMenu.add (item);
					}
					else
					{
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
						tieSubProblemSubMenu.add (item);
					}
				}

				// Only enable the menu if there are components to tie/untie
				if (tieSubProblemSubMenu.getMenuComponentCount() == 0)
				{
					tieSubProblemSubMenu.setEnabled (false);
				}
				else
				{
					tieSubProblemSubMenu.setEnabled (true);
				}
				if (untieSubProblemSubMenu.getMenuComponentCount() == 0)
				{
					untieSubProblemSubMenu.setEnabled (false);
				}
				else
				{
					untieSubProblemSubMenu.setEnabled (true);
				}

				rightClickMenu.show (workspacePanel, evt.getX (), evt.getY());
			}
			else
			{
				rightClickedComponent = null;
				solutionMenuItem.setEnabled (false);
				tieSubProblemSubMenu.setEnabled (false);
				tieSubProblemSubMenu.removeAll ();
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

	private void tieSubProblemSubMenuMenuSelected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_tieSubProblemSubMenuMenuSelected
	{//GEN-HEADEREND:event_tieSubProblemSubMenuMenuSelected
		if (rightClickedComponent != null && tieSubProblemSubMenu.isEnabled())
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
	}//GEN-LAST:event_tieSubProblemSubMenuMenuSelected

	private void tieSubProblemSubMenuMenuDeselected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_tieSubProblemSubMenuMenuDeselected
	{//GEN-HEADEREND:event_tieSubProblemSubMenuMenuDeselected
		if (rightClickedComponent != null && tieSubProblemSubMenu.isEnabled())
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
	}//GEN-LAST:event_tieSubProblemSubMenuMenuDeselected

	private void workspacePanelComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_workspacePanelComponentResized
	{//GEN-HEADEREND:event_workspacePanelComponentResized
		trashCan.setLocation (workspacePanel.getWidth () - 40, workspacePanel.getHeight () - 40);
	}//GEN-LAST:event_workspacePanelComponentResized

	private void workspacePanelComponentAdded(java.awt.event.ContainerEvent evt)//GEN-FIRST:event_workspacePanelComponentAdded
	{//GEN-HEADEREND:event_workspacePanelComponentAdded
		workspacePanel.setComponentZOrder(trashCan, workspacePanel.getComponentCount() - 1);
	}//GEN-LAST:event_workspacePanelComponentAdded

	private void rCodeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rCodeMenuItemActionPerformed
		if (rightClickedComponent != null)
		{
			try
			{
				if (rightClickedComponent instanceof Operation)
				{
					domain.ensureRequirementsMet((Operation) rightClickedComponent);
				}

				answerPanel.removeAll ();
				answerPanel.add(new JLabel("<html>" + ((DataSource) rightClickedComponent).getRCommands().replaceAll ("\n", "<br />") + "</html>"));

				answerDialog.setTitle ("R Code");
				answerDialog.pack ();
				answerDialog.setLocation (answerDialogLocation);
				answerDialog.setVisible (true);
			}
			catch (MarlaException ex)
			{
				Domain.logger.add (ex);
			}
		}
	}//GEN-LAST:event_rCodeMenuItemActionPerformed

	private void buttonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMousePressed
		// TODO add your handling code here:
	}//GEN-LAST:event_buttonMousePressed

	private void buttonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMouseReleased
		JLabel button = (JLabel) evt.getSource ();
		if (button == newButton)
		{
			newProblem ();
		}
		else if(button == openButton)
		{
			domain.load ();
		}
		else if(button == saveButton)
		{
			domain.save ();
		}
		else if(button == plusFontButton)
		{
			
		}
		else if(button == minusFontButton)
		{

		}
		else if(button == abbreviateButton)
		{
			if (abbreviated)
			{
				abbreviateButton.setIcon(new ImageIcon(getClass().getResource("/images/unchecked.png")));
				abbreviated = false;
			}
			else
			{
				abbreviateButton.setIcon(new ImageIcon(getClass().getResource("/images/checked.png")));
				abbreviated = true;
			}

			if (domain.problem != null)
			{
				for (int i = 0; i < domain.problem.getDataCount(); ++i)
				{
					rebuildTree(domain.problem.getData (i));
				}
			}
		}
		else if(button == settingsButton)
		{

		}

	}//GEN-LAST:event_buttonMouseReleased

	private void buttonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMouseEntered
		// TODO add your handling code here:
	}//GEN-LAST:event_buttonMouseEntered

	private void buttonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMouseExited
		// TODO add your handling code here:
	}//GEN-LAST:event_buttonMouseExited

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
		// Set the label for the dataset itself
		dataSet.setText("<html>" + dataSet.getDisplayString(abbreviated) + "</html>");
		dataSet.setSize (dataSet.getPreferredSize ());

		if (dataSet.getOperationCount () > 0)
		{
			// Ensure updated sizes
			for (int i = 0; i < dataSet.getOperationCount(); ++i)
			{
				Operation operation = dataSet.getOperation (i);
				operation.setText ("<html>" + operation.getDisplayString (abbreviated) + "</html>");
				operation.setSize (operation.getPreferredSize());
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
					children.get (j).setText ("<html>" + children.get (j).getDisplayString (abbreviated) + "</html>");
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

	/**
	 * Display the Info Required dialog for the given operation.
	 *
	 * @param newOperation The operation to get information for.
	 * @throws MarlaException
	 */
	public void getRequiredInfoDialog(final Operation newOperation) throws MarlaException
	{
		// Create the dialog which will be launched to ask about requirements
		final List<OperationInformation> prompts = newOperation.getRequiredInfoPrompt();
		final JDialog dialog = new JDialog ();
		JPanel panel = new JPanel ();
		panel.setLayout (new GridLayout (prompts.size () + 1, 2));

		dialog.setTitle (newOperation.getName() + ": Information Required");
		dialog.setModal (true);
		dialog.add (panel);

		// This array will contain references to objects that will hold the values
		final List<Object> valueComponents = new ArrayList<Object> ();

		// Fill dialog with components
		for (OperationInformation question : prompts)
		{
			if (question.getType() == PromptType.STRING || question.getType() == PromptType.NUMERIC)
			{
				JPanel tempPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
				JLabel label = new JLabel (question.getPrompt());
				JTextField textField = new JTextField ();
				textField.setPreferredSize(new Dimension (150, textField.getPreferredSize().height));
				tempPanel.add (label);
				tempPanel.add (textField);
				valueComponents.add (textField);
				panel.add (tempPanel);
			}
			else if(question.getType() == PromptType.CHECKBOX)
			{
				JPanel tempPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
				JCheckBox checkBox = new JCheckBox (question.getPrompt());
				JLabel label = new JLabel ("");
				tempPanel.add (checkBox);
				tempPanel.add (label);
				valueComponents.add (checkBox);
				panel.add (tempPanel);
			}
			else if(question.getType() == PromptType.COMBO || question.getType() == PromptType.COLUMN)
			{
				JPanel tempPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
				JLabel label = new JLabel (question.getPrompt());
				DefaultComboBoxModel model = new DefaultComboBoxModel (((OperationInfoCombo)question).getOptions().toArray());
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
				boolean pass = true;
				for (int i = 0; i < prompts.size(); i++)
				{
					OperationInformation question = prompts.get(i);

					try
					{
						if(question.getType() == PromptType.CHECKBOX)
						{
							question.setAnswer(((JCheckBox) valueComponents.get (i)).isSelected());
						}
						else if(question.getType() == PromptType.COMBO || question.getType() == PromptType.COLUMN)
						{
							question.setAnswer(((JComboBox) valueComponents.get (i)).getSelectedItem());
						}
						else
						{
							question.setAnswer(((JTextField) valueComponents.get (i)).getText ());
						}
					}
					catch (OperationInfoRequiredException ex)
					{
						// If the users input was not valid, the form is not accepted and the dialog will not close
						((JTextField) valueComponents.get (i)).requestFocus();
						((JTextField) valueComponents.get (i)).selectAll();
						JOptionPane.showMessageDialog(viewPanel, ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
						pass = false;
					}
				}

				if (pass)
				{
					dialog.setVisible (false);
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
				workspacePanel.add (trashCan);

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
	 * Create a new problem.
	 */
	protected void newProblem()
	{
		NEW_PROBLEM_WIZARD_DIALOG.setTitle ("New Problem Wizard");
		NEW_PROBLEM_WIZARD_DIALOG.welcomeTextLabel.setText (ViewPanel.welcomeNewText);
		NEW_PROBLEM_WIZARD_DIALOG.launchNewProblemWizard ();
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
    private javax.swing.JLabel abbreviateButton;
    protected javax.swing.JDialog answerDialog;
    private javax.swing.JPanel answerPanel;
    private javax.swing.JPanel componentsCardPanel;
    protected javax.swing.JPanel componentsPanel;
    protected javax.swing.JPanel emptyPalettePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPopupMenu.Separator menuSeparator;
    private javax.swing.JLabel minusFontButton;
    private javax.swing.JLabel newButton;
    private javax.swing.JLabel openButton;
    protected javax.swing.JFileChooser openChooserDialog;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JTextArea outputTextArea;
    private javax.swing.JLabel plusFontButton;
    private javax.swing.JLabel preWorkspaceLabel;
    protected javax.swing.JPanel preWorkspacePanel;
    private javax.swing.JMenuItem rCodeMenuItem;
    private javax.swing.JPopupMenu rightClickMenu;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JLabel saveButton;
    protected javax.swing.JFileChooser saveChooserDialog;
    private javax.swing.JLabel settingsButton;
    private javax.swing.JMenuItem solutionMenuItem;
    private javax.swing.JMenu tieSubProblemSubMenu;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JLabel trashCan;
    private javax.swing.JPanel trayPanel;
    private javax.swing.JMenu untieSubProblemSubMenu;
    private javax.swing.JPanel workspaceCardPanel;
    protected javax.swing.JPanel workspacePanel;
    private javax.swing.JSplitPane workspaceSplitPane;
    // End of variables declaration//GEN-END:variables

}
