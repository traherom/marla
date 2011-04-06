/*
 * The maRla Project - Graphical problem solver for statistics and probability problems.
 * Copyright (C) 2010 Cedarville University
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,Processor
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package marla.ide.gui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BoxLayout;
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
import javax.swing.filechooser.FileFilter;
import marla.ide.problem.DataSet;
import marla.ide.problem.DataSource;
import marla.ide.problem.MarlaException;
import marla.ide.operation.Operation;
import marla.ide.operation.OperationException;
import marla.ide.operation.OperationInfoCombo;
import marla.ide.operation.OperationInfoRequiredException;
import marla.ide.operation.OperationInformation;
import marla.ide.operation.OperationInformation.PromptType;
import marla.ide.operation.OperationXML;
import marla.ide.problem.InternalMarlaException;
import marla.ide.problem.Problem;
import marla.ide.problem.SubProblem;
import marla.ide.r.RProcessorException;
import marla.ide.resource.Configuration;
import marla.ide.resource.LoadSaveThread;
import marla.ide.resource.UndoRedo;
import marla.ide.resource.Updater;

/**
 * The view of the application, which contains all user interactive components.
 * Functions that can be (that are not directly related to the front-end) will
 * be abstracted out to the Domain class.
 *
 * @author Alex Laird
 */
public class ViewPanel extends JPanel
{
	/** The default text for the New Problem Wizard.*/
	public static final String welcomeNewText = "<html>The New Problem Wizard will walk you through the setup of a new statistical problem.<br /><br />"
												+ "Be as detailed as possible when filling in the problem description and adding sub problems (for "
												+ "instance, part a., b., etc.). Specifically, sub problems can be colored, which will make identifying "
												+ "these parts of the problem in the workspace much easier.<br /><br />You can import data values for "
												+ "your problem from a standard library (for instance, Devore7), or a CSV file. You can also enter data "
												+ "values manually.<br /><br />User and problem-specific information can be set on the last page of the "
												+ "wizard. This information is particularly useful when exporting the problem for LaTeX or as a PDF. "
												+ "Some of the values can be permanently set from \"Settings\".<br /><br />If you need to edit the problem "
												+ "or any of its data in the future, you can by selecting items from the \"Problem\" menu.</html>";
	/** The default text for the New Problem Wizard when it is in edit mode.*/
	public static final String welcomeEditText = "<html>When editing a problem, you cannot change the problem name or location. To rename the saved "
												 + "file, select \"Save As...\" from the \"File\" menu. To more quickly navigate to a page in "
												 + "the wizard, click on the name of the page to the left.<br /><br />The most common reason to edit a "
												 + "problem is to add conclusions. A conclusion can be added for each of the sub problems (from the \"Sub "
												 + "Problems\" page) as well as for the problem as a whole (from the \"Information\" page).<br /><br />"
												 + "Sub problems and data sets can be edited, added, or removed at any time. Data sets from a library can "
												 + "also be imported.</html";
	public static final String FIRST_TIP = "- Click the plus (+) symbols in the Palette to the right to expand the categories";
	public static final String SECOND_TIP = "- Drag operations from the Palette into the workspace";
	public static final String THIRD_TIP = "- Drag operations over data sets or other operations to connect them";
	public static final String FOURTH_TIP = "- Drag unused items over the trash can to remove them";
	public static final String FIFTH_TIP = "- Right-click on items in the workspace to have them perform specific tasks";
	/** A red border display.*/
	private final Color HOVER_BACKGROUND_COLOR = new Color(255, 255, 110);
	/** The default, no background workspace color.*/
	private final Color NO_BACKGROUND_WORKSPACE = new Color(255, 255, 255, 0);
	/** The source object for draggable assignments and events.*/
	public final DragSource DRAG_SOURCE = new DragSource();
	/** The drag-and-drop listener for assignments and events.*/
	public final DragDrop DND_LISTENER = new DragDrop(this);
	/** Default, plain, 12-point font.*/
	public static Font FONT_PLAIN_12 = new Font("Verdana", Font.PLAIN, 12);
	/** Default, bold, 14-point font.*/
	public static Font FONT_BOLD_14 = new Font("Verdana", Font.BOLD, 14);
	/** Default, plain, 11-point font.*/
	public static Font FONT_PLAIN_11 = new Font("Verdana", Font.PLAIN, 11);
	/** Default, bold, 11-point font.*/
	public static Font FONT_BOLD_11 = new Font("Verdana", Font.BOLD, 11);
	/** Default, bold, 12-point font.*/
	public static Font FONT_BOLD_12 = new Font("Verdana", Font.BOLD, 12);
	/** The minimum distance the mouse must be dragged before a component will break free.*/
	private final int MIN_DRAG_DIST = 15;
	/** The maximum font size.*/
	private final int MAXIMUM_FONT_SIZE = 36;
	/** The miniumu font size.*/
	private final int MINIMUM_FONT_SIZE = 8;
	/** The domain object reference performs generic actions specific to the GUI.*/
	protected Domain domain = new Domain(this);
	/** The New Problem Wizard dialog.*/
	public final NewProblemWizardDialog newProblemWizardDialog = new NewProblemWizardDialog(this, domain);
	/** The Settings dialog.*/
	public final SettingsDialog settingsDialog = new SettingsDialog(this);
	/** The Input dialog.*/
	public final InputDialog inputDialog = new InputDialog(this);
	/** The main frame of a stand-alone application.*/
	public MainFrame mainFrame;
	/**
	 * Denotes when either the R code or solution displays are building the 
	 * answer dialog. Used to prevent the main frame from trying dispose
	 * of the window too soon
	 */
	protected boolean startingAnswerPanelDisplay = false;
	/** A static reference to the view panel after it has been created.*/
	private static ViewPanel viewPanel = null;
	/** Layout constraints for the palette.*/
	public GridBagConstraints compConstraints = new GridBagConstraints();
	/** True while the program is in startup, false otherwise.*/
	protected boolean initLoading = true;
	/** The width between two operations/data sets.*/
	private int spaceWidth = 20;
	/** The height between two operations/data sets.*/
	private int spaceHeight = 30;
	/** The size of fonts.*/
	public static int fontSize = 12;
	/** Font size and style for workspace plain.*/
	public static Font workspaceFontPlain = new Font("Verdana", Font.PLAIN, ViewPanel.fontSize);
	/** Font size and style for workspace bold.*/
	public static Font workspaceFontBold = new Font("Verdana", Font.BOLD, ViewPanel.fontSize);
	/** The component under the mouse when it is pressed.*/
	private JComponent componentUnderMouse = null;
	/** The data set being dragged.*/
	protected JComponent draggingComponent = null;
	/** The component currently being hovered over during a drag.*/
	private JComponent hoverInDragComponent = null;
	/** The component currently being hovered over in the workspace (not during a drag).*/
	private JComponent hoverComponent = null;
	/** The component that has been right-clicked on.*/
	private JComponent rightClickedComponent = null;
	/** The x-offset for dragging an item*/
	protected int xDragOffset = -1;
	/** The y-offset for dragging an item*/
	protected int yDragOffset = -1;
	/** The initial x for dragging the component.*/
	private int startX = -1;
	/** The initial y for dragging the component.*/
	private int startY = -1;
	/** The counter illustrating what column we're adding to in the legend.*/
	protected int firstCounter = 3;
	/** The first placeholder (second column) in the legend.*/
	protected JLabel second = null;
	/** The second placeholder (third column) in the legend.*/
	protected JLabel third = null;
	/** The counter illustrating what column we're adding to in the data set panel.*/
	protected int firstDataCounter = 3;
	/** The first placeholder (second column) in the data set panel.*/
	protected JLabel secondData = null;
	/** The second placeholder (third column) in the data set panel.*/
	protected JLabel thirdData = null;
	/** True when the mouse has dragged far enough to break the component away, false otherwise.*/
	private boolean broken = false;
	/** True when dragging from the palette, false otherwise.*/
	protected boolean dragFromPalette = false;
	/** The default file filter for a JFileChooser open dialog.*/
	protected FileFilter defaultFilter;
	/** The extensions file filter for CSV files.*/
	protected ExtensionFileFilter csvFilter = new ExtensionFileFilter("Comma Separated Value Files.csv, .txt)", new String[]
			{
				"CSV", "TXT"
			});
	/** The extensions file filter for CSV files.*/
	protected ExtensionFileFilter marlaFilter = new ExtensionFileFilter("The maRla Project Files (.marla)", new String[]
			{
				"MARLA"
			});
	/** The extensions file filter for PDF files.*/
	protected ExtensionFileFilter pdfFilter = new ExtensionFileFilter("PDF Files (.pdf)", new String[]
			{
				"PDF"
			});
	/** The extensions file filter for LaTeX files.*/
	protected ExtensionFileFilter latexFilter = new ExtensionFileFilter("LaTeX Sweave Files (.Rnw)", new String[]
			{
				"RNW"
			});
	/** The point in the view where the answer dialog shall appear.*/
	private Point answerDialogLocation = null;
	/** True if operation and column names are abbreviated, false otherwise.*/
	protected boolean abbreviated = false;
	/** 0 when no button is pressed, otherwise the number of the button pressed.*/
	private int buttonPressed = 0;
	/** The label that presents helpful hints on first run.*/
	JLabel firstRunLabel = new JLabel();
	/** The tip to display in the workspace when problem is being edited.*/
	protected String tipRemainder = FIRST_TIP + "<br />" + SECOND_TIP + "<br />" + THIRD_TIP + "<br />" + FOURTH_TIP + "<br />" + FIFTH_TIP;
	/** True if the first tip should be shown.*/
	protected boolean showFirst = true;
	/** True if the second tip should be shown.*/
	protected boolean showSecond = true;
	/** True if the third tip should be shown.*/
	protected boolean showThird = true;
	/** True if the fourth tip should be shown.*/
	protected boolean showFourth = true;
	/** True if the fifth tip should be shown.*/
	protected boolean showFifth = true;
	/** True if the changeBeginning call needs to be made, false otherwise.*/
	private boolean markChangeBeginning = true;
	/** The undo/redo object.*/
	protected UndoRedo<Problem> undoRedo = new UndoRedo<Problem>();

	/**
	 * Creates new form MainFrame for a stand-alone application.
	 */
	public ViewPanel(MainFrame mainFrame)
	{
		this.mainFrame = mainFrame;
		init();
	}

	/**
	 * Calls initialization functions for the frame-based application.
	 */
	private void init()
	{
		initComponents();
		initMyComponents();
	}

	/**
	 * Custom initialization of specific components is done here.
	 */
	private void initMyComponents()
	{
		// Remap shortcut keys to system defaults
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener()
		{
			@Override
			public void eventDispatched(AWTEvent event)
			{
				KeyEvent kev = (KeyEvent) event;
				if(kev.getID() == KeyEvent.KEY_PRESSED || kev.getID() == KeyEvent.KEY_RELEASED || kev.getID() == KeyEvent.KEY_PRESSED)
				{
					if((kev.getModifiersEx() & KeyEvent.META_DOWN_MASK) != 0 && !((kev.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0))
					{
						kev.consume();
						KeyEvent fake = new KeyEvent(kev.getComponent(), kev.getID(), kev.getWhen(), (kev.getModifiersEx() & ~KeyEvent.META_DOWN_MASK) | KeyEvent.CTRL_DOWN_MASK, kev.getKeyCode(), kev.getKeyChar());
						Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(fake);
					}
				}
			}
		}, KeyEvent.KEY_EVENT_MASK);

		workspacePanel.setDropTarget(new DropTarget(workspacePanel, DnDConstants.ACTION_MOVE, DND_LISTENER));

		domain.loadSaveThread = new LoadSaveThread(domain);
		// Launch the save thread
		domain.loadSaveThread.start();
		domain.setLoadSaveThread(domain.loadSaveThread);

		// Initially, simply display the welcome card until a problem is created new or loaded
		emptyPalettePanel.setVisible(true);
		componentsPanel.setVisible(false);
		preWorkspacePanel.setVisible(true);
		workspacePanel.setVisible(false);

		componentsScrollPane.getViewport().setOpaque(false);

		// Retrieve the default file filter from the JFileChooser before it is ever changed
		defaultFilter = fileChooserDialog.getFileFilter();

		// Find the "Cancel" button and change the tooltip
		ViewPanel.setToolTipForButton(fileChooserDialog, "Cancel", "Cancel file selection");
	}

	/**
	 * Recurse through a given component to find the given JButton with the given string.
	 * Then set the tooltip of that JButton to the requested tooltip.
	 *
	 * @param comp The component to iterate through.
	 * @param string The string to search for in a JButton.
	 * @param toolTip The tooltip to set for the JButton.
	 */
	public static void setToolTipForButton(JComponent comp, String string, String toolTip)
	{
		if(comp instanceof JButton
		   && ((JButton) comp).getText() != null
		   && ((JButton) comp).getText().equals(string))
		{
			((JButton) comp).setToolTipText(toolTip);
		}
		for(int i = 0; i < comp.getComponentCount(); ++i)
		{
			if(comp.getComponent(i) instanceof JComponent)
			{
				setToolTipForButton((JComponent) comp.getComponent(i), string, toolTip);
			}
		}
	}

	/**
	 * If constructed, return the instance of the view panel.
	 *
	 * @return The instance of the view panel.
	 */
	public static ViewPanel getInstance()
	{
		if(viewPanel != null)
		{
			return viewPanel;
		}

		return null;
	}

	/**
	 * Reloads the palette from the XML file.
	 */
	protected void reloadOperations()
	{
		try
		{
			componentsScrollablePanel.removeAll();

			// Force operations to be reloadedEnhancement summary
			OperationXML.clearXMLOps();
			OperationXML.loadXML();

			// Reload operations in the interface
			loadOperations();
		}
		catch(MarlaException ex)
		{
			Domain.logger.add(ex);
			JOptionPane.showMessageDialog(domain.getTopWindow(), ex.getMessage(), "Reload Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Load the operations into the palette.
	 *
	 * @throws MarlaException Throws any Marla exceptions to the calling function.
	 */
	protected void loadOperations()
	{
		Map<String, List<String>> ops = Operation.getAvailableOperationsCategorized();
		Set<String> categories = ops.keySet();

		// Add all operation types to the palette, adding listeners to the labels as we go
		compConstraints.gridx = 0;
		compConstraints.gridwidth = 1;
		compConstraints.weighty = 1;
		compConstraints.fill = GridBagConstraints.VERTICAL;
		compConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		int catCount = 0;
		for(String key : categories)
		{
			List<String> operations = ops.get(key);

			final JPanel catContentPanel = new JPanel();
			CategoryHandle catHandlePanel = null;
			try
			{
				catHandlePanel = new CategoryHandle(this, key, new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent evt)
					{
						CategoryHandle catHandlePanel = (CategoryHandle) evt.getSource();
						if(catHandlePanel.target.contains(evt.getPoint()))
						{
							catHandlePanel.toggleSelection();
							if(catContentPanel.isShowing())
							{
								catContentPanel.setVisible(false);
							}
							else
							{
								catContentPanel.setVisible(true);
							}
						}
					}
				});
			}
			// Images are missing; should never happen
			catch(IOException ex)
			{
				Domain.logger.add(ex);
			}
			catContentPanel.setLayout(new GridBagLayout());
			GridBagConstraints catConstraints = new GridBagConstraints();
			catConstraints.fill = GridBagConstraints.VERTICAL;
			catConstraints.weighty = 0;
			catConstraints.weightx = 1;
			catConstraints.gridwidth = 1;
			catConstraints.anchor = GridBagConstraints.FIRST_LINE_START;

			for(int i = 0; i < operations.size(); ++i)
			{
				try
				{
					final Operation operation = Operation.createOperation(operations.get(i));
					operation.setFont(ViewPanel.FONT_PLAIN_12);
					operation.setDefaultColor();
					operation.setText("<html>" + operation.getDisplayString(abbreviated) + "</html>");
					operation.setToolTipText("<html>" + operation.getDescription() + "</html>");
					DRAG_SOURCE.createDefaultDragGestureRecognizer(operation, DnDConstants.ACTION_MOVE, DND_LISTENER);

					catConstraints.gridx = 0;
					catConstraints.gridy = i;
					catContentPanel.add(operation, catConstraints);
					operation.addMouseListener(new MouseAdapter()
					{
						@Override
						public void mouseEntered(MouseEvent evt)
						{
							setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							operation.setForeground(Color.GRAY);
							componentsScrollablePanel.repaint();
						}

						@Override
						public void mouseExited(MouseEvent evt)
						{
							setCursor(Cursor.getDefaultCursor());
							operation.setDefaultColor();
							componentsScrollablePanel.repaint();
						}

						@Override
						public void mousePressed(MouseEvent evt)
						{
							buttonPressed = evt.getButton();
							xDragOffset = (int) evt.getLocationOnScreen().getX() - (int) operation.getLocationOnScreen().getX();
							yDragOffset = (int) evt.getLocationOnScreen().getY() - (int) operation.getLocationOnScreen().getY();
						}
					});
				}
				catch(OperationException ex)
				{
					// Unable to load, not a real operation
					Domain.logger.add(ex);
					System.err.println("Error loading operation '" + operations.get(i) + "'");
				}
			}
			// Add final JLabel for pad filling
			catContentPanel.add(new JLabel(""), catConstraints);

			JPanel wrapperPanel = new JPanel();
			wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.PAGE_AXIS));
			wrapperPanel.add(catHandlePanel);
			wrapperPanel.add(catContentPanel);
			catContentPanel.setVisible(false);
			catHandlePanel.setPreferredSize(new Dimension(200, 20));
			catContentPanel.setPreferredSize(new Dimension(200, catContentPanel.getPreferredSize().height));

			compConstraints.gridy = catCount;
			compConstraints.weighty = 0;
			componentsScrollablePanel.add(wrapperPanel, compConstraints);
			++catCount;
		}

		// Add final component to offset weight
		compConstraints.gridy = catCount;
		compConstraints.weighty = 1;
		componentsScrollablePanel.add(new JLabel(""), compConstraints);

		componentsScrollablePanel.invalidate();
		componentsScrollablePanel.revalidate();
		componentsScrollablePanel.repaint();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        fileChooserDialog = new javax.swing.JFileChooser();
        answerDialog = new javax.swing.JDialog();
        answerPanel = new javax.swing.JPanel();
        rightClickMenu = new javax.swing.JPopupMenu();
        solutionMenuItem = new javax.swing.JMenuItem();
        changeInfoMenuItem = new javax.swing.JMenuItem();
        menuSeparator1 = new javax.swing.JPopupMenu.Separator();
        tieSubProblemSubMenu = new javax.swing.JMenu();
        untieSubProblemSubMenu = new javax.swing.JMenu();
        remarkMenuItem = new javax.swing.JMenuItem();
        menuSeparator2 = new javax.swing.JPopupMenu.Separator();
        rCodeMenuItem = new javax.swing.JMenuItem();
        menuSeparator3 = new javax.swing.JPopupMenu.Separator();
        addDataSetMenuItem = new javax.swing.JMenuItem();
        editDataSetMenuItem = new javax.swing.JMenuItem();
        toolBar = new javax.swing.JToolBar();
        newButton = new ToolbarButton (new ImageIcon (getClass ().getResource ("/marla/ide/images/new_button.png")));
        openButton = new ToolbarButton (new ImageIcon (getClass ().getResource ("/marla/ide/images/open_button.png")));
        saveButton = new ToolbarButton (new ImageIcon (getClass ().getResource ("/marla/ide/images/save_button.png")));
        jSeparator4 = new javax.swing.JToolBar.Separator();
        addDataButton = new ToolbarButton (new ImageIcon (getClass ().getResource ("/marla/ide/images/add_data_button.png")));
        jSeparator1 = new javax.swing.JToolBar.Separator();
        fontSizeLabel = new javax.swing.JLabel();
        plusFontButton = new ToolbarButton (new ImageIcon (getClass ().getResource ("/marla/ide/images/plus_button.png")));
        minusFontButton = new ToolbarButton (new ImageIcon (getClass ().getResource ("/marla/ide/images/minus_button.png")));
        jSeparator3 = new javax.swing.JToolBar.Separator();
        abbreviateButton = new ToolbarButton (new ImageIcon (getClass ().getResource ("/marla/ide/images/unchecked_button.png")));
        jSeparator2 = new javax.swing.JToolBar.Separator();
        settingsButton = new ToolbarButton (new ImageIcon (getClass ().getResource ("/marla/ide/images/settings_button.png")));
        workspaceSplitPane = new javax.swing.JSplitPane();
        workspaceCardPanel = new javax.swing.JPanel();
        preWorkspacePanel = new javax.swing.JPanel();
        preWorkspaceLabel = new javax.swing.JLabel();
        workspacePanel = new WorkspacePanel (this);
        trashCan = new javax.swing.JLabel();
        debugScrollPane = new javax.swing.JScrollPane();
        debugTextArea = new javax.swing.JTextArea();
        rightSidePanel = new javax.swing.JPanel();
        paletteCardPanel = new javax.swing.JPanel();
        emptyPalettePanel = new javax.swing.JPanel();
        componentsPanel = new javax.swing.JPanel();
        componentsScrollPane = new javax.swing.JScrollPane();
        componentsScrollablePanel = new javax.swing.JPanel();
        dataSetsPanel = new javax.swing.JPanel();
        dataSetContentPanel = new javax.swing.JPanel();
        legendPanel = new javax.swing.JPanel();
        legendContentPanel = new javax.swing.JPanel();

        fileChooserDialog.setApproveButtonToolTipText("Open selection");
        fileChooserDialog.setDialogTitle("Browse Problem Location");
        fileChooserDialog.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        answerDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        answerDialog.setTitle("Solution to Point");
        answerDialog.setAlwaysOnTop(true);
        answerDialog.setUndecorated(true);
        answerDialog.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                answerDialogWindowLostFocus(evt);
            }
        });
        answerDialog.getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        answerPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        answerPanel.setLayout(new javax.swing.BoxLayout(answerPanel, javax.swing.BoxLayout.PAGE_AXIS));
        answerDialog.getContentPane().add(answerPanel);

        solutionMenuItem.setFont(new java.awt.Font("Verdana", 0, 11));
        solutionMenuItem.setText("Solution");
        solutionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                solutionMenuItemActionPerformed(evt);
            }
        });
        rightClickMenu.add(solutionMenuItem);

        changeInfoMenuItem.setFont(new java.awt.Font("Verdana", 0, 11));
        changeInfoMenuItem.setText("Change Parameters");
        changeInfoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeInfoMenuItemActionPerformed(evt);
            }
        });
        rightClickMenu.add(changeInfoMenuItem);
        rightClickMenu.add(menuSeparator1);

        tieSubProblemSubMenu.setText("Tie to Sub Problem");
        tieSubProblemSubMenu.setFont(new java.awt.Font("Verdana", 0, 11));
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
        untieSubProblemSubMenu.setFont(new java.awt.Font("Verdana", 0, 11));
        untieSubProblemSubMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
                untieSubProblemSubMenuMenuDeselected(evt);
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                untieSubProblemSubMenuMenuSelected(evt);
            }
        });
        rightClickMenu.add(untieSubProblemSubMenu);

        remarkMenuItem.setFont(new java.awt.Font("Verdana", 0, 11));
        remarkMenuItem.setText("Remarks");
        remarkMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remarkMenuItemActionPerformed(evt);
            }
        });
        rightClickMenu.add(remarkMenuItem);
        rightClickMenu.add(menuSeparator2);

        rCodeMenuItem.setFont(new java.awt.Font("Verdana", 0, 11));
        rCodeMenuItem.setText("View R Code");
        rCodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rCodeMenuItemActionPerformed(evt);
            }
        });
        rightClickMenu.add(rCodeMenuItem);
        rightClickMenu.add(menuSeparator3);

        addDataSetMenuItem.setFont(new java.awt.Font("Verdana", 0, 11));
        addDataSetMenuItem.setText("Add Data Set...");
        addDataSetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDataSetMenuItemActionPerformed(evt);
            }
        });
        rightClickMenu.add(addDataSetMenuItem);

        editDataSetMenuItem.setFont(new java.awt.Font("Verdana", 0, 11));
        editDataSetMenuItem.setText("Edit Data Set...");
        editDataSetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editDataSetMenuItemActionPerformed(evt);
            }
        });
        rightClickMenu.add(editDataSetMenuItem);

        setLayout(new java.awt.BorderLayout());

        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setPreferredSize(new java.awt.Dimension(13, 35));

        newButton.setFont(new java.awt.Font("Verdana", 0, 12));
        newButton.setToolTipText("New Problem");
        newButton.setEnabled(false);
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
        openButton.setToolTipText("Open Problem");
        openButton.setEnabled(false);
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
        saveButton.setToolTipText("Save Problem");
        saveButton.setEnabled(false);
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

        jSeparator4.setEnabled(false);
        toolBar.add(jSeparator4);

        addDataButton.setFont(new java.awt.Font("Verdana", 0, 12));
        addDataButton.setToolTipText("Add Data Set");
        addDataButton.setEnabled(false);
        addDataButton.addMouseListener(new java.awt.event.MouseAdapter() {
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
        toolBar.add(addDataButton);

        jSeparator1.setEnabled(false);
        toolBar.add(jSeparator1);

        fontSizeLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        fontSizeLabel.setText("Font Size: ");
        fontSizeLabel.setEnabled(false);
        toolBar.add(fontSizeLabel);

        plusFontButton.setFont(new java.awt.Font("Verdana", 0, 12));
        plusFontButton.setToolTipText("Increase font size");
        plusFontButton.setEnabled(false);
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
        minusFontButton.setToolTipText("Decrease font size");
        minusFontButton.setEnabled(false);
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

        jSeparator3.setEnabled(false);
        toolBar.add(jSeparator3);

        abbreviateButton.setFont(new java.awt.Font("Verdana", 0, 12));
        abbreviateButton.setText("Abbreviate");
        abbreviateButton.setToolTipText("Show abbreviated operation and column names");
        abbreviateButton.setEnabled(false);
        abbreviateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                buttonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonMouseReleased(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseEntered(evt);
            }
        });
        toolBar.add(abbreviateButton);

        jSeparator2.setEnabled(false);
        toolBar.add(jSeparator2);

        settingsButton.setFont(new java.awt.Font("Verdana", 0, 12));
        settingsButton.setToolTipText("Settings");
        settingsButton.setEnabled(false);
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

        workspaceSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        workspaceSplitPane.setResizeWeight(1.0);

        workspaceCardPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        workspaceCardPanel.setLayout(new java.awt.CardLayout());

        preWorkspacePanel.setBackground(new java.awt.Color(204, 204, 204));

        preWorkspaceLabel.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        preWorkspaceLabel.setForeground(new java.awt.Color(102, 102, 102));
        preWorkspaceLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        preWorkspaceLabel.setText("<html><div align=\"center\">To get started, load a previous problem or use the<br /><em>New Problem Wizard</em> (File --> New Problem...) to<br />create a new problem</div></html>");

        org.jdesktop.layout.GroupLayout preWorkspacePanelLayout = new org.jdesktop.layout.GroupLayout(preWorkspacePanel);
        preWorkspacePanel.setLayout(preWorkspacePanelLayout);
        preWorkspacePanelLayout.setHorizontalGroup(
            preWorkspacePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(preWorkspacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(preWorkspaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                .addContainerGap())
        );
        preWorkspacePanelLayout.setVerticalGroup(
            preWorkspacePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(preWorkspacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(preWorkspaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
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
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                workspacePanelMouseMoved(evt);
            }
        });
        workspacePanel.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                workspacePanelComponentAdded(evt);
            }
        });
        workspacePanel.setLayout(null);

        trashCan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/marla/ide/images/trash_button.png"))); // NOI18N
        trashCan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                trashCanMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                trashCanMouseEntered(evt);
            }
        });
        workspacePanel.add(trashCan);
        trashCan.setBounds(730, 610, 26, 40);

        workspaceCardPanel.add(workspacePanel, "card4");

        workspaceSplitPane.setTopComponent(workspaceCardPanel);

        debugTextArea.setColumns(20);
        debugTextArea.setEditable(false);
        debugTextArea.setFont(new java.awt.Font("Courier New", 0, 12));
        debugTextArea.setLineWrap(true);
        debugTextArea.setRows(5);
        debugTextArea.setWrapStyleWord(true);
        debugScrollPane.setViewportView(debugTextArea);

        workspaceSplitPane.setBottomComponent(debugScrollPane);

        add(workspaceSplitPane, java.awt.BorderLayout.CENTER);

        rightSidePanel.setMaximumSize(new java.awt.Dimension(220, 2147483647));
        rightSidePanel.setPreferredSize(new java.awt.Dimension(220, 592));
        rightSidePanel.setLayout(new java.awt.GridBagLayout());

        paletteCardPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Palette", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N
        paletteCardPanel.setLayout(new java.awt.CardLayout());

        org.jdesktop.layout.GroupLayout emptyPalettePanelLayout = new org.jdesktop.layout.GroupLayout(emptyPalettePanel);
        emptyPalettePanel.setLayout(emptyPalettePanelLayout);
        emptyPalettePanelLayout.setHorizontalGroup(
            emptyPalettePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 204, Short.MAX_VALUE)
        );
        emptyPalettePanelLayout.setVerticalGroup(
            emptyPalettePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 569, Short.MAX_VALUE)
        );

        paletteCardPanel.add(emptyPalettePanel, "card3");

        componentsPanel.setLayout(new java.awt.GridLayout(1, 0));

        componentsScrollPane.setBorder(null);
        componentsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        componentsScrollPane.setOpaque(false);

        componentsScrollablePanel.setOpaque(false);
        componentsScrollablePanel.setLayout(new java.awt.GridBagLayout());
        componentsScrollPane.setViewportView(componentsScrollablePanel);

        componentsPanel.add(componentsScrollPane);

        paletteCardPanel.add(componentsPanel, "card2");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        rightSidePanel.add(paletteCardPanel, gridBagConstraints);

        dataSetsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data Sets", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N
        dataSetsPanel.setLayout(new java.awt.GridLayout(1, 1));

        dataSetContentPanel.setLayout(new java.awt.GridLayout(0, 3));
        dataSetsPanel.add(dataSetContentPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        rightSidePanel.add(dataSetsPanel, gridBagConstraints);

        legendPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Legend", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N
        legendPanel.setLayout(new java.awt.GridLayout(1, 1));

        legendContentPanel.setLayout(new java.awt.GridLayout(0, 3));
        legendPanel.add(legendContentPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        rightSidePanel.add(legendPanel, gridBagConstraints);

        add(rightSidePanel, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

	private void workspacePanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMouseDragged
		if(Math.abs(startX - evt.getX()) > MIN_DRAG_DIST || Math.abs(startY - evt.getY()) > MIN_DRAG_DIST)
		{
			broken = true;
		}

		if(broken)
		{
			if(componentUnderMouse != null && draggingComponent == null)
			{
				componentUnderMouse = null;
				if(buttonPressed == MouseEvent.BUTTON1)
				{
					Point point;
					if(startX != -1 && startY != -1)
					{
						point = new Point(startX, startY);
					}
					else
					{
						point = evt.getPoint();
					}
					JComponent component = (JComponent) workspacePanel.getComponentAt(point);
					if(component != null
					   && component != workspacePanel
					   && component != trashCan
					   && component != firstRunLabel)
					{
						if (markChangeBeginning)
						{
							markChangeBeginning = false;
							domain.changeBeginning();
						}

						draggingComponent = component;
						if(draggingComponent instanceof Operation)
						{
							try
							{
								Operation childOperation = null;
								if(((Operation) draggingComponent).getOperationCount() > 0)
								{
									childOperation = ((Operation) draggingComponent).getOperation(0);
								}
								DataSource parent = ((Operation) draggingComponent).getParentData();
								if(parent != null)
								{
									int oldIndex = parent.getOperationIndex((Operation) draggingComponent);

									parent.removeOperation((Operation) draggingComponent);
									domain.problem.addUnusedOperation(parent.removeOperation((Operation) draggingComponent));

									if(childOperation != null)
									{
										parent.addOperation(oldIndex, childOperation);
									}
								}
							}
							catch(MarlaException ex)
							{
								Domain.logger.add(ex);
							}
							catch(NullPointerException ex)
							{
								Domain.logger.add(ex);
							}
							xDragOffset = (int) point.getX() - draggingComponent.getX();
							yDragOffset = (int) point.getY() - draggingComponent.getY();
						}
						else
						{
							xDragOffset = (int) point.getX() - draggingComponent.getX();
							yDragOffset = (int) point.getY() - draggingComponent.getY();
						}
						workspacePanel.setComponentZOrder(trashCan, workspacePanel.getComponentCount() - 1);
						if(firstRunLabel.getParent() == workspacePanel)
						{
							workspacePanel.setComponentZOrder(firstRunLabel, workspacePanel.getComponentCount() - 1);
						}
						workspacePanel.setComponentZOrder(draggingComponent, 0);

						domain.problem.markUnsaved();
					}
				}
			}

			dragInWorkspace(evt);
		}
	}//GEN-LAST:event_workspacePanelMouseDragged

	private void workspacePanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMouseReleased
		markChangeBeginning = true;

		// If we were hovering, clear any of those elements
		if(hoverComponent != null)
		{
			if(hoverComponent != null)
			{
				((DataSource) hoverComponent).setDefaultColor();
				hoverComponent = null;
			}
		}

		if(buttonPressed == MouseEvent.BUTTON1)
		{
			if(draggingComponent != null)
			{
				if(trashCan.getBounds().intersects(draggingComponent.getBounds()))
				{
					int response = JOptionPane.YES_OPTION;
					if(draggingComponent instanceof DataSet)
					{
						response = JOptionPane.showConfirmDialog(this, "You are about to remove this data set from the workspace.\nThe data set can be readded to the workspace anytime by dragging\nit back from the list of data sets to the right.\nAre you sure you want to remove this data set?", "Remove Data Set", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if(response == JOptionPane.YES_OPTION)
						{
							DataSet dataSet = (DataSet) draggingComponent;
							for(int i = 0; i < dataSet.getOperationCount(); ++i)
							{
								Operation operation = dataSet.getOperation(i);
								List<Operation> children = operation.getAllChildOperations();
								for(int j = 0; j < children.size(); ++j)
								{
									workspacePanel.remove(children.get(j));
								}
								workspacePanel.remove(operation);
							}
							dataSet.isHidden(true);
							workspacePanel.remove(dataSet);
							workspacePanel.repaint();
						}
					}
					else
					{
						domain.problem.removeUnusedOperation((Operation) draggingComponent);
					}

					if(response == JOptionPane.YES_OPTION)
					{
						workspacePanel.remove(draggingComponent);
						rebuildWorkspace();

						if(showFourth)
						{
							showFourth = false;
							refreshTip();
						}
					}
				}
				else
				{
					if(draggingComponent instanceof Operation)
					{
						try
						{
							drop((Operation) draggingComponent, false, evt.getPoint());
						}
						catch(MarlaException ex)
						{
							Domain.logger.add(ex);
						}
						draggingComponent.setBackground(NO_BACKGROUND_WORKSPACE);
					}
					else
					{
						draggingComponent.setBackground(NO_BACKGROUND_WORKSPACE);
					}

					rebuildTree((DataSource) draggingComponent);
				}

				draggingComponent = null;
			}
		}
		else if(buttonPressed == MouseEvent.BUTTON3)
		{
			if(showFifth)
			{
				showFifth = false;
				refreshTip();
			}

			JComponent component = (JComponent) workspacePanel.getComponentAt(evt.getPoint());
			if(component != null
			   && component != workspacePanel
			   && component != trashCan
			   && component != firstRunLabel)
			{
				rightClickedComponent = component;
				answerDialogLocation = evt.getLocationOnScreen();

				solutionMenuItem.setEnabled(true);
				changeInfoMenuItem.setEnabled(true);
				tieSubProblemSubMenu.setEnabled(true);
				untieSubProblemSubMenu.setEnabled(true);
				remarkMenuItem.setEnabled(true);
				rCodeMenuItem.setEnabled(true);
				editDataSetMenuItem.setEnabled(true);
				tieSubProblemSubMenu.removeAll();
				untieSubProblemSubMenu.removeAll();
				for(int i = 0; i < domain.problem.getSubProblemCount(); ++i)
				{
					final SubProblem subProblem = domain.problem.getSubProblem(i);
					String name = subProblem.getSubproblemID();
					if(subProblem.isDataSourceInSolution((DataSource) rightClickedComponent))
					{
						JMenuItem item = new JMenuItem(name);
						item.setFont(FONT_PLAIN_11);
						item.addActionListener(new ActionListener()
						{
							@Override
							public void actionPerformed(ActionEvent evt)
							{
								DataSource ds = (DataSource) rightClickedComponent;

								// Untie
								subProblem.removeAllSubSteps(ds);

								rebuildWorkspace();
							}
						});
						untieSubProblemSubMenu.add(item);
					}
					else
					{
						JMenuItem item = new JMenuItem(name);
						item.setFont(FONT_PLAIN_11);
						item.addActionListener(new ActionListener()
						{
							@Override
							public void actionPerformed(ActionEvent evt)
							{
								DataSource ds = (DataSource) rightClickedComponent;

								// Tie
								subProblem.addAllSubSteps(ds);

								rebuildWorkspace();
							}
						});
						tieSubProblemSubMenu.add(item);
					}
				}

				// Only enable the menu if there are components to tie/untie
				if(tieSubProblemSubMenu.getMenuComponentCount() == 0)
				{
					tieSubProblemSubMenu.setEnabled(false);
				}
				else
				{
					tieSubProblemSubMenu.setEnabled(true);
				}
				if(untieSubProblemSubMenu.getMenuComponentCount() == 0)
				{
					untieSubProblemSubMenu.setEnabled(false);
				}
				else
				{
					untieSubProblemSubMenu.setEnabled(true);
				}

				if(rightClickedComponent instanceof DataSet)
				{
					solutionMenuItem.setText("Summary");
					editDataSetMenuItem.setEnabled(true);
					changeInfoMenuItem.setEnabled(false);
					remarkMenuItem.setEnabled(false);
				}
				else
				{
					solutionMenuItem.setText("Solution");
					editDataSetMenuItem.setEnabled(false);
					try
					{
						if(rightClickedComponent != null && rightClickedComponent instanceof Operation && ((Operation) rightClickedComponent).isInfoRequired())
						{
							changeInfoMenuItem.setEnabled(true);
						}
						else
						{
							changeInfoMenuItem.setEnabled(false);
						}
					}
					catch(MarlaException ex)
					{
						changeInfoMenuItem.setEnabled(false);
						Domain.logger.add(ex);
					}
					remarkMenuItem.setEnabled(true);
				}
			}
			else
			{
				solutionMenuItem.setEnabled(false);
				changeInfoMenuItem.setEnabled(false);
				tieSubProblemSubMenu.setEnabled(false);
				untieSubProblemSubMenu.setEnabled(false);
				remarkMenuItem.setEnabled(false);
				rCodeMenuItem.setEnabled(false);
				editDataSetMenuItem.setEnabled(false);
			}

			rightClickMenu.show(workspacePanel, evt.getX(), evt.getY());
		}
		else
		{
		}

		setCursor(Cursor.getDefaultCursor());

		buttonPressed = 0;
		startX = -1;
		startY = -1;
	}//GEN-LAST:event_workspacePanelMouseReleased

	private void solutionMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_solutionMenuItemActionPerformed
	{//GEN-HEADEREND:event_solutionMenuItemActionPerformed
		if(rightClickedComponent != null)
		{
			try
			{
				startingAnswerPanelDisplay = true;

				if(rightClickedComponent instanceof Operation)
				{
					domain.ensureRequirementsMet((Operation) rightClickedComponent);
				}

				answerPanel.removeAll();
				if(rightClickedComponent instanceof Operation && ((Operation) rightClickedComponent).hasPlot())
				{
					JLabel label = new JLabel("");
					label.setIcon(new ImageIcon(((Operation) rightClickedComponent).getPlot()));
					answerPanel.add(label);
				}
				else
				{
					answerPanel.add(new JLabel("<html>" + ((DataSource) rightClickedComponent).toHTML() + "</html>"));
				}

				if(rightClickedComponent instanceof Operation)
				{
					answerDialog.setTitle("Solution to Point");
				}
				else if(rightClickedComponent instanceof DataSet)
				{
					answerDialog.setTitle("Data Set Summary");
				}

				answerDialog.pack();
				answerDialog.setLocation(answerDialogLocation);
				answerDialog.setVisible(true);
			}
			catch(OperationException ex)
			{
				Domain.logger.add(ex);
				JOptionPane.showMessageDialog(domain.getTopWindow(), ex.getMessage(), "Operation Error", JOptionPane.ERROR_MESSAGE);
			}
			catch(MarlaException ex)
			{
				Domain.logger.add(ex);
			}
			finally
			{
				startingAnswerPanelDisplay = false;
			}
		}
	}//GEN-LAST:event_solutionMenuItemActionPerformed

	private void tieSubProblemSubMenuMenuSelected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_tieSubProblemSubMenuMenuSelected
	{//GEN-HEADEREND:event_tieSubProblemSubMenuMenuSelected
		if(rightClickedComponent != null && tieSubProblemSubMenu.isEnabled())
		{
			rightClickedComponent.setBackground(HOVER_BACKGROUND_COLOR);
			if(rightClickedComponent instanceof Operation)
			{
				DataSource source = ((Operation) rightClickedComponent).getRootDataSource().getOperation(((Operation) rightClickedComponent).getIndexFromDataSet());
				((JComponent) source).setBackground(HOVER_BACKGROUND_COLOR);
				List<Operation> tempOperations = source.getRootDataSource().getOperation(((Operation) source).getIndexFromDataSet()).getAllChildOperations();
				for(int i = 0; i < tempOperations.size(); ++i)
				{
					tempOperations.get(i).setBackground(HOVER_BACKGROUND_COLOR);
				}
			}
			else
			{
				DataSet root = (DataSet) rightClickedComponent;
				root.setBackground(HOVER_BACKGROUND_COLOR);
				for(int i = 0; i < root.getOperationCount(); ++i)
				{
					Operation operation = root.getOperation(i);
					operation.setBackground(HOVER_BACKGROUND_COLOR);
					List<Operation> tempOperations = operation.getAllChildOperations();
					for(int j = 0; j < tempOperations.size(); ++j)
					{
						tempOperations.get(j).setBackground(HOVER_BACKGROUND_COLOR);
					}
				}
			}
			workspacePanel.repaint();
		}
	}//GEN-LAST:event_tieSubProblemSubMenuMenuSelected

	private void tieSubProblemSubMenuMenuDeselected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_tieSubProblemSubMenuMenuDeselected
	{//GEN-HEADEREND:event_tieSubProblemSubMenuMenuDeselected
		if(rightClickedComponent != null && tieSubProblemSubMenu.isEnabled())
		{
			rightClickedComponent.setBackground(NO_BACKGROUND_WORKSPACE);
			if(rightClickedComponent instanceof Operation)
			{
				DataSource source = ((Operation) rightClickedComponent).getRootDataSource().getOperation(((Operation) rightClickedComponent).getIndexFromDataSet());
				((JComponent) source).setBackground(NO_BACKGROUND_WORKSPACE);
				List<Operation> tempOperations = source.getRootDataSource().getOperation(((Operation) source).getIndexFromDataSet()).getAllChildOperations();
				for(int i = 0; i < tempOperations.size(); ++i)
				{
					tempOperations.get(i).setBackground(NO_BACKGROUND_WORKSPACE);
				}
			}
			else
			{
				DataSet root = (DataSet) rightClickedComponent;
				root.setBackground(NO_BACKGROUND_WORKSPACE);
				for(int i = 0; i < root.getOperationCount(); ++i)
				{
					Operation operation = root.getOperation(i);
					operation.setBackground(NO_BACKGROUND_WORKSPACE);
					List<Operation> tempOperations = operation.getAllChildOperations();
					for(int j = 0; j < tempOperations.size(); ++j)
					{
						tempOperations.get(j).setBackground(NO_BACKGROUND_WORKSPACE);
					}
				}
			}
			workspacePanel.repaint();
		}
	}//GEN-LAST:event_tieSubProblemSubMenuMenuDeselected

	private void workspacePanelComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_workspacePanelComponentResized
	{//GEN-HEADEREND:event_workspacePanelComponentResized
		firstRunLabel.setLocation((workspacePanel.getWidth() - firstRunLabel.getWidth()) / 2, (workspacePanel.getHeight() - firstRunLabel.getHeight()) / 2);
		trashCan.setLocation(workspacePanel.getWidth() - 40, workspacePanel.getHeight() - 40);
		ensureComponentsVisible();
	}//GEN-LAST:event_workspacePanelComponentResized

	private void workspacePanelComponentAdded(java.awt.event.ContainerEvent evt)//GEN-FIRST:event_workspacePanelComponentAdded
	{//GEN-HEADEREND:event_workspacePanelComponentAdded
		workspacePanel.setComponentZOrder(trashCan, workspacePanel.getComponentCount() - 1);
		if(firstRunLabel.getParent() == workspacePanel)
		{
			workspacePanel.setComponentZOrder(firstRunLabel, workspacePanel.getComponentCount() - 1);
		}
	}//GEN-LAST:event_workspacePanelComponentAdded

	private void rCodeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rCodeMenuItemActionPerformed
		if(rightClickedComponent != null)
		{
			try
			{
				if(rightClickedComponent instanceof Operation)
				{
					domain.ensureRequirementsMet((Operation) rightClickedComponent);
				}

				answerPanel.removeAll();
				answerPanel.add(new JLabel("<html>" + ((DataSource) rightClickedComponent).getRCommands().replaceAll("\n", "<br />") + "</html>"));

				answerDialog.setTitle("R Code");
				answerDialog.pack();
				answerDialog.setLocation(answerDialogLocation);
				answerDialog.setVisible(true);
			}
			catch(MarlaException ex)
			{
				Domain.logger.add(ex);
			}
		}
	}//GEN-LAST:event_rCodeMenuItemActionPerformed

	private void buttonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMousePressed
		if(((ToolbarButton) evt.getSource()).isEnabled() && !initLoading)
		{
			((ToolbarButton) evt.getSource()).setDepressed(true);
		}
	}//GEN-LAST:event_buttonMousePressed

	private void buttonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMouseReleased
		ToolbarButton button = (ToolbarButton) evt.getSource();
		button.setDepressed(false);
		if(button.isEnabled() && button == newButton)
		{
			newProblem();
		}
		else if(button.isEnabled() && button == openButton)
		{
			domain.load();
		}
		else if(button.isEnabled() && button == saveButton)
		{
			domain.save();
		}
		else if(button.isEnabled() && button == plusFontButton)
		{
			if(fontSize + 1 < MAXIMUM_FONT_SIZE)
			{
				++fontSize;
				spaceWidth += 6;
				spaceHeight += 6;

				workspaceFontPlain = new Font("Verdana", Font.PLAIN, fontSize);
				workspaceFontBold = new Font("Verdana", Font.BOLD, fontSize);

				rebuildWorkspace();
			}
		}
		else if(button.isEnabled() && button == minusFontButton)
		{
			if(fontSize - 1 > MINIMUM_FONT_SIZE)
			{
				--fontSize;
				spaceWidth -= 6;
				spaceHeight -= 6;

				workspaceFontPlain = new Font("Verdana", Font.PLAIN, fontSize);
				workspaceFontBold = new Font("Verdana", Font.BOLD, fontSize);

				rebuildWorkspace();
			}
		}
		else if(button.isEnabled() && button == abbreviateButton)
		{
			if(abbreviated)
			{
				ImageIcon newIcon = new ImageIcon(getClass().getResource("/marla/ide/images/unchecked_button.png"));
				abbreviateButton.setIcon(newIcon);
				((ToolbarButton) abbreviateButton).setIconStandards(newIcon);
				abbreviated = false;
			}
			else
			{
				ImageIcon newIcon = new ImageIcon(getClass().getResource("/marla/ide/images/checked_button.png"));
				abbreviateButton.setIcon(newIcon);
				((ToolbarButton) abbreviateButton).setIconStandards(newIcon);
				abbreviated = true;
			}

			if(domain.problem != null)
			{
				rebuildWorkspace();
			}
		}
		else if(button.isEnabled() && button == settingsButton)
		{
			settingsDialog.initSettingsDialog();
			settingsDialog.launchSettingsDialog();
		}
		else if(button.isEnabled() && button == addDataButton)
		{
			newProblemWizardDialog.addDataSet();
		}
	}//GEN-LAST:event_buttonMouseReleased

	private void buttonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMouseEntered
		if(((ToolbarButton) evt.getSource()).isEnabled() && !((ToolbarButton) evt.getSource()).isSelected() && !initLoading)
		{
			((ToolbarButton) evt.getSource()).setHover(true);
		}
	}//GEN-LAST:event_buttonMouseEntered

	private void buttonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMouseExited
		if(((ToolbarButton) evt.getSource()).isEnabled() && !((ToolbarButton) evt.getSource()).isSelected() && !initLoading)
		{
			((ToolbarButton) evt.getSource()).setHover(false);
		}
	}//GEN-LAST:event_buttonMouseExited

	private void trashCanMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_trashCanMouseEntered
		trashCan.setIcon(new ImageIcon(getClass().getResource("/marla/ide/images/trash_button_hover.png")));
	}//GEN-LAST:event_trashCanMouseEntered

	private void trashCanMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_trashCanMouseExited
		trashCan.setIcon(new ImageIcon(getClass().getResource("/marla/ide/images/trash_button.png")));
	}//GEN-LAST:event_trashCanMouseExited

	private void editDataSetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editDataSetMenuItemActionPerformed
		if(rightClickedComponent != null)
		{
			newProblemWizardDialog.editDataSet((DataSet) rightClickedComponent);
		}
	}//GEN-LAST:event_editDataSetMenuItemActionPerformed

	private void changeInfoMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_changeInfoMenuItemActionPerformed
	{//GEN-HEADEREND:event_changeInfoMenuItemActionPerformed
		if(rightClickedComponent != null && rightClickedComponent instanceof Operation)
		{
			try
			{
				if(((Operation) rightClickedComponent).isInfoRequired())
				{
					ViewPanel.getRequiredInfoDialog((Operation) rightClickedComponent, true);
				}
			}
			catch(MarlaException ex)
			{
				Domain.logger.add(ex);
			}
		}
	}//GEN-LAST:event_changeInfoMenuItemActionPerformed

	private void answerDialogWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_answerDialogWindowLostFocus
		if(!startingAnswerPanelDisplay)
		{
			answerDialog.dispose();
		}
	}//GEN-LAST:event_answerDialogWindowLostFocus

	private void remarkMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remarkMenuItemActionPerformed
		if(rightClickedComponent != null && rightClickedComponent instanceof Operation)
		{
			String newRemark = inputDialog.launchInputDialog(this, "Operation Remark", "Give a remark for this operation:", ((Operation) rightClickedComponent).getRemark());
			if(!newRemark.equals(((Operation) rightClickedComponent).getRemark()))
			{
				((Operation) rightClickedComponent).setRemark(newRemark);
			}
		}
	}//GEN-LAST:event_remarkMenuItemActionPerformed

	private void untieSubProblemSubMenuMenuDeselected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_untieSubProblemSubMenuMenuDeselected
	{//GEN-HEADEREND:event_untieSubProblemSubMenuMenuDeselected
		if(rightClickedComponent != null && untieSubProblemSubMenu.isEnabled())
		{
			rightClickedComponent.setBackground(NO_BACKGROUND_WORKSPACE);
			workspacePanel.repaint();
		}
	}//GEN-LAST:event_untieSubProblemSubMenuMenuDeselected

	private void untieSubProblemSubMenuMenuSelected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_untieSubProblemSubMenuMenuSelected
	{//GEN-HEADEREND:event_untieSubProblemSubMenuMenuSelected
		if(rightClickedComponent != null && untieSubProblemSubMenu.isEnabled())
		{
			rightClickedComponent.setBackground(HOVER_BACKGROUND_COLOR);
			workspacePanel.repaint();
		}
	}//GEN-LAST:event_untieSubProblemSubMenuMenuSelected

	private void workspacePanelMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_workspacePanelMousePressed
	{//GEN-HEADEREND:event_workspacePanelMousePressed
		buttonPressed = evt.getButton();
		Component comp = workspacePanel.getComponentAt(evt.getPoint());
		if(comp != null)
		{
			componentUnderMouse = (JComponent) comp;
			if(comp instanceof Operation)
			{
				startX = evt.getX();
				startY = evt.getY();
				broken = false;
			}
			else
			{
				startX = -1;
				startY = -1;
				broken = true;
			}
		}
	}//GEN-LAST:event_workspacePanelMousePressed

	private void workspacePanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMouseMoved
		// We only care if the mouse has moved when we're NOT dragging
		if(buttonPressed == 0)
		{
			JComponent component = (JComponent) workspacePanel.getComponentAt(evt.getPoint());
			if(component != null
			   && component != workspacePanel
			   && component != trashCan
			   && component != firstRunLabel)
			{
				if(component != hoverComponent)
				{
					setCursor(Cursor.getDefaultCursor());
					if(hoverComponent != null)
					{
						((DataSource) hoverComponent).setDefaultColor();
						hoverComponent = null;
					}
				}

				if(component instanceof DataSource)
				{
					hoverComponent = component;
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					hoverComponent.setForeground(Color.GRAY);
				}
			}
			else if(hoverComponent != null)
			{
				setCursor(Cursor.getDefaultCursor());
				((DataSource) hoverComponent).setDefaultColor();
			}
			workspacePanel.repaint();
		}
	}//GEN-LAST:event_workspacePanelMouseMoved

	private void addDataSetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDataSetMenuItemActionPerformed
		newProblemWizardDialog.addDataSet();
	}//GEN-LAST:event_addDataSetMenuItemActionPerformed

	/**
	 * Undo the last operation.
	 */
	protected void undo()
	{
		if (undoRedo.hasUndo())
		{
			Problem problem = undoRedo.undo(domain.problem);
			closeProblem(false, true);
			domain.problem = problem;
			openProblem(false, true);

			domain.validateUndoRedoMenuItems();
		}
	}

	/**
	 * Redo the last "undo" operation.
	 */
	protected void redo()
	{
		if (undoRedo.hasRedo())
		{
			Problem problem = undoRedo.redo(domain.problem);
			closeProblem(false, true);
			domain.problem = problem;
			openProblem(false, true);

			domain.validateUndoRedoMenuItems();
		}
	}

	/**
	 * Refresh the text displayed for the tip.
	 */
	protected void refreshTip()
	{
		tipRemainder = "";

		if(showFirst)
		{
			tipRemainder += FIRST_TIP;
		}
		if(showSecond)
		{
			tipRemainder += ("<br />" + SECOND_TIP);
		}
		if(showThird)
		{
			tipRemainder += ("<br />" + THIRD_TIP);
		}
		if(showFourth)
		{
			tipRemainder += ("<br />" + FOURTH_TIP);
		}
		if(showFifth)
		{
			tipRemainder += ("<br />" + FIFTH_TIP);
		}

		if(tipRemainder.startsWith("<br />"))
		{
			tipRemainder = tipRemainder.substring(6, tipRemainder.length());
		}
		if(!tipRemainder.equals(""))
		{
			firstRunLabel.setText("<html><div align=\"center\">" + tipRemainder + "</div></html>");
			firstRunLabel.setSize(firstRunLabel.getPreferredSize());
			firstRunLabel.setLocation((workspacePanel.getWidth() - firstRunLabel.getWidth()) / 2, (workspacePanel.getHeight() - firstRunLabel.getHeight()) / 2);
		}
		else if(firstRunLabel.getParent() == workspacePanel)
		{
			workspacePanel.remove(firstRunLabel);
		}
		workspacePanel.repaint();
	}

	/**
	 * Ensure all data sets are within the bounds of the workspace.
	 */
	protected void ensureComponentsVisible()
	{
		// Ensure all datasets are within our new bounds
		if(domain.problem != null)
		{
			for(DataSource ds : domain.problem.getVisibleData())
			{
				int x = ds.getX();
				int y = ds.getY();

				// Move if we're not within the workspace
				if(x < 0)
					x = 0;
				if(x > workspacePanel.getWidth() - ds.getWidth())
					x = workspacePanel.getWidth() - ds.getWidth();

				if(y < 0)
					y = 0;
				if(y > workspacePanel.getHeight() - ds.getHeight())
					y = workspacePanel.getHeight() - ds.getHeight();

				// Add to workspace and move
				ds.setLocation(x, y);
				workspacePanel.add(ds);
			}

			rebuildWorkspace();
		}
	}

	/**
	 * Manage drag events within the workspace panel
	 *
	 * @param evt The mouse event for the drag.
	 */
	protected void dragInWorkspace(MouseEvent evt)
	{
		if((dragFromPalette || draggingComponent != null) && buttonPressed == MouseEvent.BUTTON1)
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			if(hoverInDragComponent != null)
			{
				hoverInDragComponent.setBackground(NO_BACKGROUND_WORKSPACE);
				hoverInDragComponent = null;
			}

			Component component = ((WorkspacePanel) workspacePanel).getComponentAt(evt.getPoint().x, evt.getPoint().y, draggingComponent);
			if(component != null
			   && component != trashCan
			   && component != firstRunLabel)
			{
				if(component instanceof DataSource)
				{
					hoverInDragComponent = (JComponent) ((WorkspacePanel) workspacePanel).getComponentAt(evt.getPoint().x, evt.getPoint().y, draggingComponent);
					hoverInDragComponent.setBackground(HOVER_BACKGROUND_COLOR);
				}
			}

			if(draggingComponent != null)
			{
				int x = evt.getX() - xDragOffset;
				int y = evt.getY() - yDragOffset;

				// Only do the move if we're within the workspace still
				if(x < 0 || x > workspacePanel.getWidth() - draggingComponent.getWidth())
				{
					x = draggingComponent.getX();
				}
				if(y < 0 || y > workspacePanel.getHeight() - draggingComponent.getHeight())
				{
					y = draggingComponent.getY();
				}

				draggingComponent.setLocation(x, y);

				// Just rebuild the dragged component
				rebuildTree((DataSource) draggingComponent);
			}
			else
			{
				workspacePanel.repaint();
			}
		}
	}

	/**
	 * Rebuild the entire workspace, including data sets and attached and unattached
	 * operations therein.
	 */
	protected void rebuildWorkspace()
	{
		Problem problem;
		if(newProblemWizardDialog.newProblem != null)
		{
			problem = newProblemWizardDialog.newProblem;
		}
		else
		{
			problem = domain.problem;
		}
		if(problem != null)
		{
			for(DataSet ds : problem.getVisibleDataSets())
			{
				rebuildTree(ds);
			}
			for(int i = 0; i < problem.getUnusedOperationCount(); ++i)
			{
				rebuildTree(problem.getUnusedOperation(i));
			}
			workspacePanel.repaint();
		}
	}

	/**
	 * Rebuild the tree in the interface for the given data set.
	 *
	 * @param ds The data set to rebuild in the interface.
	 */
	protected void rebuildTree(DataSource ds)
	{
		// Don't bother listening yet if the problem is still loading
		Problem prob = ds.getParentProblem();
		if(prob != null && prob.isLoading())
			return;

		// Set the label for the data source itself
		ds.setFont(workspaceFontBold);
		ds.setText("<html>" + ds.getDisplayString(abbreviated) + "</html>");
		ds.setSize(ds.getPreferredSize());

		int opCount = ds.getOperationCount();
		if(opCount > 0)
		{
			// Find widths of all our columns
			int[] widths = new int[opCount];
			for(int i = 0; i < opCount; ++i)
			{
				// Run down this operation chain in order to find the widest one
				widths[i] = rebuildOperationColumn(ds.getOperation(i), 0, true);
			}

			// Figure out where the columns should start based on our center
			int dsWidth = ds.getWidth();
			int dsCenterX = ds.getX() + dsWidth / 2;

			// Find the median value
			int halfWidth = 0;
			if(opCount == 1)
			{
				halfWidth = widths[0] / 2;
			}
			else if(opCount % 2 == 0)
			{
				// Even number of columns, balance them below dataset
				for(int i = 0; i < opCount / 2; i++)
				{
					halfWidth += widths[i] + spaceWidth;
				}

				// Eliminate half of the middle spacing
				halfWidth -= spaceWidth / 2;
			}
			else
			{
				// Odd number of columns, center the middle one under the
				// dataset
				for(int i = 0; i < opCount / 2; i++)
				{
					halfWidth += widths[i] + spaceWidth;
				}

				// And add enough to move through half the middle column
				halfWidth += widths[opCount / 2] / 2;
			}

			int farLeftX = dsCenterX - halfWidth;

			int previousLeftX = farLeftX;
			int[] centerXs = new int[opCount];
			for(int i = 0; i < opCount; i++)
			{
				centerXs[i] = previousLeftX + widths[i] / 2;
				previousLeftX += widths[i] + spaceWidth;
			}

			// Now rebuild each operation column, this time actually centering them
			// based on the dataset
			for(int i = 0; i < opCount; i++)
			{
				rebuildOperationColumn(ds.getOperation(i), centerXs[i], false);
			}
		}

		// Redraw everything
		workspacePanel.repaint();
	}

	/**
	 * Recursive portion of rebuildTree() that walks down the line of operations
	 * and centers them all on the widest one. This function assumes a single
	 * operation extends from each op, not a wide tree as is internally supported
	 * @param op Start of operation chain we're checking
	 * @param centerX x coordinate to center on
	 * @param shouldResize If true, sets the label of the operation and resizes it. However,
	 *		it will not attempt to center the labels in any way
	 * @return Width of the column, based on the widest operation in it
	 */
	protected int rebuildOperationColumn(Operation op, int centerX, boolean shouldResize)
	{
		Operation currOp = op;
		int widest = 0;

		boolean moreOps = true;
		while(moreOps)
		{
			// Stop after this loop?
			if(currOp.getOperationCount() == 0)
			{
				moreOps = false;
			}

			if(shouldResize)
			{
				// Update label
				currOp.setFont(workspaceFontBold);
				currOp.setText("<html>" + currOp.getDisplayString(abbreviated) + "</html>");
				currOp.setSize(currOp.getPreferredSize());
			}

			// Get width
			int width = currOp.getWidth();
			if(width > widest)
				widest = width;

			if(!shouldResize)
			{
				// Center off the given center x
				int x = centerX - width / 2;
				int y = ((JComponent) currOp.getParentData()).getY() + spaceHeight;
				currOp.setLocation(x, y);
			}

			// Next op
			if(moreOps)
			{
				currOp = currOp.getOperation(0);
			}
		}

		return widest;
	}

	/**
	 * Drop the given component at the current location that the mouse is hovering at in the Workspace Panel.
	 *
	 * @param operation The operation to drop.
	 * @param duplicate True if the drop should create a new instance of the given operation, false if it should drop the given instance.
	 * @param location The location of the mouse pointer.
	 */
	protected void drop(Operation operation, boolean duplicate, Point location) throws OperationException, RProcessorException, MarlaException
	{
		if(operation != null)
		{
			operation.setDefaultColor();
			operation.setBackground(NO_BACKGROUND_WORKSPACE);
			componentsScrollablePanel.repaint();
		}

		JComponent component = (JComponent) ((WorkspacePanel) workspacePanel).getComponentAt(location.x, location.y, operation);
		if(component != null
		   && component != trashCan
		   && component != firstRunLabel
		   && (component instanceof DataSet || component instanceof Operation))
		{
			final Operation newOperation;
			if(duplicate)
			{
				setCursor(Cursor.getDefaultCursor());

				newOperation = Operation.createOperation(operation.getName());
				newOperation.setFont(ViewPanel.workspaceFontBold);
				newOperation.setText("<html>" + newOperation.getDisplayString(abbreviated) + "</html>");
				newOperation.setSize(newOperation.getPreferredSize());

				if(showSecond)
				{
					showSecond = false;
					refreshTip();
				}
			}
			else
			{
				newOperation = operation;
			}

			if(component instanceof Operation)
			{
				Operation dropOperation = (Operation) component;
				if(dropOperation != newOperation)
				{
					if(dropOperation.getOperationCount() > 0)
					{
						// Add as child and ensure we're not listed as unused
						domain.problem.removeUnusedOperation(newOperation);
						dropOperation.addOperation(newOperation);

						Operation childOperation = dropOperation.getOperation(0);
						childOperation.setParentData(newOperation);
					}
					else
					{
						domain.problem.removeUnusedOperation(newOperation);
						dropOperation.addOperation(newOperation);
					}
				}

				if(showThird)
				{
					showThird = false;
					refreshTip();
				}
			}
			else
			{
				if(component instanceof DataSet)
				{
					// Add as child and ensure we're not listed as unused
					DataSet dataSet = (DataSet) component;
					domain.problem.removeUnusedOperation(newOperation);
					dataSet.addOperation(newOperation);
				}

				if(showThird)
				{
					showThird = false;
					refreshTip();
				}
			}

			workspacePanel.add(newOperation);
		}
		else if(component != trashCan)
		{
			final Operation newOperation;
			if(duplicate)
			{
				setCursor(Cursor.getDefaultCursor());

				newOperation = Operation.createOperation(operation.getName());
				newOperation.setFont(ViewPanel.workspaceFontBold);
				newOperation.setText("<html>" + newOperation.getDisplayString(abbreviated) + "</html>");
				newOperation.setSize(newOperation.getPreferredSize());

				domain.problem.addUnusedOperation(newOperation);

				if(showSecond)
				{
					showSecond = false;
					refreshTip();
				}
			}
			else
			{
				newOperation = operation;
			}

			if(duplicate)
			{
				newOperation.setLocation((int) location.getX() - xDragOffset, (int) location.getY() - yDragOffset);
				workspacePanel.add(newOperation);
			}
		}
		if(hoverInDragComponent != null)
		{
			hoverInDragComponent.setBackground(NO_BACKGROUND_WORKSPACE);
			hoverInDragComponent = null;
		}

		rebuildWorkspace();

		buttonPressed = 0;
	}

	/**
	 * Display the Info Required dialog for the given operation.
	 *
	 * @param newOperation The operation to get information for.
	 * @param showDialog True if the dialog should be shown, false if the panel should just be created and returned.
	 * @return Returns the panel created (element 0) and the list of value components within that panel (element 1).
	 * @throws MarlaException
	 */
	public static Object[] getRequiredInfoDialog(final Operation newOperation, final boolean showDialog) throws MarlaException
	{
		// Create the dialog which will be launched to ask about requirements
		final List<OperationInformation> prompts = newOperation.getRequiredInfoPrompt();
		final JDialog dialog = new JDialog();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(prompts.size() + 1, 2));

		dialog.setTitle(newOperation.getName() + ": Information Required");
		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.add(panel);

		// This array will contain references to objects that will hold the values
		final List<Object> valueComponents = new ArrayList<Object>();

		// Fill dialog with components
		for(OperationInformation question : prompts)
		{
			if(question.getType() == PromptType.STRING || question.getType() == PromptType.NUMERIC)
			{
				JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				JLabel label = new JLabel(question.getPrompt());

				JTextField textField = new JTextField();
				textField.setPreferredSize(new Dimension(150, textField.getPreferredSize().height));
				if(question.getAnswer() != null)
					textField.setText(question.getAnswer().toString());

				tempPanel.add(label);
				tempPanel.add(textField);
				valueComponents.add(textField);
				panel.add(tempPanel);
			}
			else if(question.getType() == PromptType.CHECKBOX)
			{
				JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

				JCheckBox checkBox = new JCheckBox(question.getPrompt());
				if(question.getAnswer() != null)
					checkBox.setSelected((Boolean) question.getAnswer());

				JLabel label = new JLabel("");

				tempPanel.add(checkBox);
				tempPanel.add(label);
				valueComponents.add(checkBox);
				panel.add(tempPanel);
			}
			else if(question.getType() == PromptType.COMBO || question.getType() == PromptType.COLUMN)
			{
				JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				JLabel label = new JLabel(question.getPrompt());

				DefaultComboBoxModel model = new DefaultComboBoxModel(((OperationInfoCombo) question).getOptions().toArray());
				JComboBox comboBox = new JComboBox(model);
				if(question.getAnswer() != null)
					comboBox.setSelectedItem(question.getAnswer());

				tempPanel.add(label);
				tempPanel.add(comboBox);
				valueComponents.add(comboBox);
				panel.add(tempPanel);
			}
			else if(question.getType() == PromptType.FIXED)
			{
				JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				JLabel label = new JLabel(question.getPrompt());

				JTextField textField = new JTextField();
				textField.setPreferredSize(new Dimension(150, textField.getPreferredSize().height));
				textField.setEnabled(false);
				if(question.getAnswer() != null)
					textField.setText(question.getAnswer().toString());

				tempPanel.add(label);
				tempPanel.add(textField);
				valueComponents.add(textField);
				panel.add(tempPanel);
			}
			else
				Domain.logger.add(new InternalMarlaException("Unhandled PromptType in question dialog"));
		}

		JButton doneButton = new JButton("Done");
		JButton cancelButton = new JButton("Cancel");
		final ViewPanel finalViewPanel = ViewPanel.getInstance();
		// When the user is done with the assumptions, forms will be validated and their values stored into the operation before continuing
		doneButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(requirementsButtonClick(prompts, valueComponents, showDialog))
				{
					dialog.setVisible(false);
				}
			}
		});
		cancelButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				dialog.setVisible(false);
			}
		});
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
		buttonPanel.add(doneButton);
		buttonPanel.add(cancelButton);
		panel.add(buttonPanel);

		if(showDialog)
		{
			// Display dialog
			dialog.pack();
			dialog.setLocationRelativeTo(viewPanel);
			dialog.setVisible(true);
		}

		return new Object[]
				{
					panel, valueComponents
				};
	}

	/**
	 * Save the user-entered options into the given prompts
	 * @param prompts Prompts to fill with answers
	 * @param valueComponents Elements which contain the user answers. Must be in the same
	 *		order as the prompts.
	 * @param showDialog If true, warns the user on invalid input
	 * @return true if all entered values successfully validated
	 */
	public static boolean requirementsButtonClick(List<OperationInformation> prompts, List<Object> valueComponents, boolean showDialog)
	{
		boolean pass = true;
		for(int i = 0; i < prompts.size(); i++)
		{
			OperationInformation question = prompts.get(i);

			try
			{
				if(question.getType() == PromptType.CHECKBOX)
				{
					question.setAnswer(((JCheckBox) valueComponents.get(i)).isSelected());
				}
				else if(question.getType() == PromptType.COMBO || question.getType() == PromptType.COLUMN)
				{
					question.setAnswer(((JComboBox) valueComponents.get(i)).getSelectedItem());
				}
				else if(question.getType() == PromptType.FIXED)
				{
					// Don't set the answer, we're not allowed to change this
				}
				else
				{
					question.setAnswer(((JTextField) valueComponents.get(i)).getText());
				}
			}
			catch(OperationInfoRequiredException ex)
			{
				// If the users input was not valid, the form is not accepted and the dialog will not close
				((JTextField) valueComponents.get(i)).requestFocus();
				((JTextField) valueComponents.get(i)).selectAll();
				if(showDialog)
				{
					JOptionPane.showMessageDialog(ViewPanel.getInstance().domain.getTopWindow(), ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
				}
				pass = false;
			}
		}

		if(pass)
		{
			return true;
		}
		return false;
	}

	/**
	 * Open the problem currently stored in the problem variable.
	 *
	 * @param editing True when editing a problem, false when creating a new one.
	 * @param isUndoRedo True if this close is a an undo/redo reload, false otherwise.
	 */
	protected void openProblem(boolean editing, boolean isUndoRedo)
	{
		if(domain.problem != null)
		{
			if(Domain.isFirstRun() && !isUndoRedo)
			{
				firstRunLabel.setFont(FONT_BOLD_14);
				firstRunLabel.setForeground(Color.LIGHT_GRAY);
				refreshTip();
				workspacePanel.add(firstRunLabel);
			}
			componentsPanel.setVisible(true);
			emptyPalettePanel.setVisible(false);
			workspacePanel.setVisible(true);
			preWorkspacePanel.setVisible(false);

			mainFrame.setTitle(mainFrame.getDefaultTitle() + " - " + domain.problem.getFileName().substring(domain.problem.getFileName().lastIndexOf(System.getProperty("file.separator")) + 1, domain.problem.getFileName().lastIndexOf(".")));

			if (!isUndoRedo)
			{
				if(!editing)
				{
					saveButton.setEnabled(false);
				}
				addDataButton.setEnabled(true);
				plusFontButton.setEnabled(true);
				minusFontButton.setEnabled(true);
				abbreviateButton.setEnabled(true);
			}

			// Move trees around if our workspace is smaller than the saving one
			ensureComponentsVisible();

			buildDataSetsOnRight();

			// Add sub problems to legend
			legendContentPanel.removeAll();
			((GridLayout) legendContentPanel.getLayout()).setRows(0);
			firstCounter = 3;
			for(int i = 0; i < domain.problem.getSubProblemCount(); ++i)
			{
				// Add sub problem to legend
				JLabel label;
				if(firstCounter == 1)
				{
					label = second;
				}
				else if(firstCounter == 2)
				{
					label = third;
				}
				else
				{
					label = new JLabel("");
					second = new JLabel("");
					third = new JLabel("");
				}
				label.setFont(FONT_PLAIN_12);
				label.setText(domain.problem.getSubProblem(i).getSubproblemID());
				label.setForeground(domain.problem.getSubProblem(i).getColor());

				if(firstCounter == 3)
				{
					firstCounter = 0;

					GridLayout layout = (GridLayout) legendContentPanel.getLayout();
					layout.setRows(layout.getRows() + 1);

					legendContentPanel.add(label);
					legendContentPanel.add(second);
					legendContentPanel.add(third);

					legendContentPanel.invalidate();
				}
				++firstCounter;
			}

			if(dataSetContentPanel.getComponentCount() == 0)
			{
				((GridLayout) dataSetContentPanel.getLayout()).setColumns(1);
				JLabel noneLabel = new JLabel("-No Data Sets-");
				noneLabel.setFont(FONT_BOLD_12);
				dataSetContentPanel.add(noneLabel);
			}
			if(legendContentPanel.getComponentCount() == 0)
			{
				((GridLayout) legendContentPanel.getLayout()).setColumns(1);
				JLabel noneLabel = new JLabel("-No Sub Problems-");
				noneLabel.setFont(FONT_BOLD_12);
				legendContentPanel.add(noneLabel);
			}
			dataSetContentPanel.invalidate();
			legendContentPanel.invalidate();

			workspacePanel.repaint();
		}
	}

	/**
	 * Assuming the data set content panel is empty, add all data sets to the
	 * panel on the right.
	 */
	protected void buildDataSetsOnRight()
	{
		// Add data sets to legend
		dataSetContentPanel.removeAll();
		((GridLayout) dataSetContentPanel.getLayout()).setRows(0);
		firstDataCounter = 3;
		for(int i = 0; i < domain.problem.getDataCount(); ++i)
		{
			// Add sub problem to legend
			JLabel label;
			if(firstDataCounter == 1)
			{
				label = secondData;
			}
			else if(firstDataCounter == 2)
			{
				label = thirdData;
			}
			else
			{
				label = new JLabel("");
				DRAG_SOURCE.createDefaultDragGestureRecognizer(label, DnDConstants.ACTION_MOVE, DND_LISTENER);
				final JLabel thisLabel = label;
				label.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseEntered(MouseEvent evt)
					{
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						thisLabel.setForeground(Color.GRAY);
					}

					@Override
					public void mouseExited(MouseEvent evt)
					{
						setCursor(Cursor.getDefaultCursor());
						thisLabel.setForeground(DataSet.getDefaultColor());
					}

					@Override
					public void mousePressed(MouseEvent evt)
					{
						buttonPressed = evt.getButton();
						xDragOffset = (int) evt.getLocationOnScreen().getX() - (int) thisLabel.getLocationOnScreen().getX();
						yDragOffset = (int) evt.getLocationOnScreen().getY() - (int) thisLabel.getLocationOnScreen().getY();
					}
				});
				secondData = new JLabel("");
				DRAG_SOURCE.createDefaultDragGestureRecognizer(secondData, DnDConstants.ACTION_MOVE, DND_LISTENER);
				final JLabel finalSecondLabel = secondData;
				secondData.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseEntered(MouseEvent evt)
					{
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						finalSecondLabel.setForeground(Color.GRAY);
					}

					@Override
					public void mouseExited(MouseEvent evt)
					{
						setCursor(Cursor.getDefaultCursor());
						finalSecondLabel.setForeground(DataSet.getDefaultColor());
					}

					@Override
					public void mousePressed(MouseEvent evt)
					{
						buttonPressed = evt.getButton();
						xDragOffset = (int) evt.getLocationOnScreen().getX() - (int) finalSecondLabel.getLocationOnScreen().getX();
						yDragOffset = (int) evt.getLocationOnScreen().getY() - (int) finalSecondLabel.getLocationOnScreen().getY();
					}
				});
				thirdData = new JLabel("");
				DRAG_SOURCE.createDefaultDragGestureRecognizer(thirdData, DnDConstants.ACTION_MOVE, DND_LISTENER);
				final JLabel finalThirdLabel = thirdData;
				thirdData.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseEntered(MouseEvent evt)
					{
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						finalThirdLabel.setForeground(Color.GRAY);
					}

					@Override
					public void mouseExited(MouseEvent evt)
					{
						setCursor(Cursor.getDefaultCursor());
						finalThirdLabel.setForeground(DataSet.getDefaultColor());
					}

					@Override
					public void mousePressed(MouseEvent evt)
					{
						buttonPressed = evt.getButton();
						xDragOffset = (int) evt.getLocationOnScreen().getX() - (int) finalThirdLabel.getLocationOnScreen().getX();
						yDragOffset = (int) evt.getLocationOnScreen().getY() - (int) finalThirdLabel.getLocationOnScreen().getY();
					}
				});
			}
			label.setFont(FONT_PLAIN_12);
			label.setText(domain.problem.getData(i).getName());
			label.setForeground(DataSet.getDefaultColor());

			if(firstDataCounter == 3)
			{
				firstDataCounter = 0;

				GridLayout layout = (GridLayout) dataSetContentPanel.getLayout();
				layout.setRows(layout.getRows() + 1);

				dataSetContentPanel.add(label);
				dataSetContentPanel.add(secondData);
				dataSetContentPanel.add(thirdData);

				dataSetContentPanel.invalidate();
			}
			++firstDataCounter;
		}
	}

	/**
	 * Close the currently open problem in the workspace.
	 *
	 * @param editing True when editing a problem, false when creating a new one.
	 * @param isUndoRedo True if this close is a an undo/redo reload, false otherwise.
	 * @return If the closing of the problem should be canceled, returns false, otherwise returns true.
	 */
	protected boolean closeProblem(boolean editing, boolean isUndoRedo)
	{
		// clear the undo/redo states if we are closing the problem
		if(!editing && !isUndoRedo)
		{
			undoRedo.clearHistory();
			mainFrame.undoMenuItem.setEnabled(false);
			mainFrame.redoMenuItem.setEnabled(false);
		}

		if(domain.problem != null)
		{
			// Check to save changes before closing the program
			if(domain.problem.isChanged())
			{
				int response = JOptionPane.YES_OPTION;
				if(!editing && !isUndoRedo)
				{
					response = JOptionPane.showConfirmDialog(domain.getTopWindow(),
															 "Would you like to save changes to the currently open problem?",
															 "Save Problem Changes",
															 JOptionPane.YES_NO_CANCEL_OPTION,
															 JOptionPane.QUESTION_MESSAGE);
				}
				if(!editing && !isUndoRedo && response == JOptionPane.YES_OPTION)
				{
					try
					{
						domain.problem.save();
					}
					catch(MarlaException ex)
					{
						Domain.logger.add(ex);
						JOptionPane.showMessageDialog(domain.getTopWindow(), ex.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				else if(!isUndoRedo && (response == -1 || response == JOptionPane.CANCEL_OPTION))
				{
					return false;
				}
			}

			if(!editing)
			{
				workspacePanel.removeAll();
				workspacePanel.add(trashCan);

				emptyPalettePanel.setVisible(true);
				componentsPanel.setVisible(false);
				preWorkspacePanel.setVisible(true);
				workspacePanel.setVisible(false);

				dataSetContentPanel.removeAll();
				((GridLayout) dataSetContentPanel.getLayout()).setRows(0);
				legendContentPanel.removeAll();
				((GridLayout) legendContentPanel.getLayout()).setRows(0);

				domain.problem = null;

				mainFrame.setTitle(mainFrame.getDefaultTitle());
			}
		}
		workspacePanel.repaint();

		if (!isUndoRedo)
		{
			if(!editing)
			{
				saveButton.setEnabled(false);
			}
			addDataButton.setEnabled(false);
			plusFontButton.setEnabled(false);
			minusFontButton.setEnabled(false);
			abbreviateButton.setEnabled(false);
		}

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
		for(int j = 0; j < workspacePanel.getComponentCount(); ++j)
		{
			if(workspacePanel.getComponent(j) instanceof DataSet
			   && !(workspacePanel.getComponent(j) instanceof Operation))
			{
				if(count == i)
				{
					return (DataSet) workspacePanel.getComponent(j);
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

		for(int i = 0; i < workspacePanel.getComponentCount(); ++i)
		{
			if(workspacePanel.getComponent(i) instanceof DataSet
			   && !(workspacePanel.getComponent(i) instanceof Operation))
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
		newProblemWizardDialog.initializeNewProblemWizard(false);
		newProblemWizardDialog.launchNewProblemWizard();
	}

	/**
	 * If it can be found, launch the Operation Editor.
	 */
	protected void launchOperationEditor()
	{
		File operationEditorExe = new File(Domain.CWD, "maRla Operation Editor.exe");
		File operationEditorJar = new File(Domain.CWD, "maRla Operation Editor.jar");

		try
		{
			if(operationEditorExe.exists())
			{
				Process proc = Runtime.getRuntime().exec(new String[]
						{
							operationEditorExe.getCanonicalPath(), Configuration.getInstance().get(Configuration.ConfigType.PrimaryOpsXML).toString()
						}, null, null);
			}
			else if(operationEditorJar.exists())
			{
				Process proc = Runtime.getRuntime().exec(new String[]
						{
							"java", "-jar", operationEditorJar.getCanonicalPath()
						}, null, null);
			}
			else
			{
				JOptionPane.showMessageDialog(this, "The maRla Operation Editor could not be found.", "Operation Editor Not Found", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		catch(IOException ex)
		{
			Domain.logger.add(ex);
		}

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
		if(closeProblem(false, false))
		{
			// Hide the main window to give the appearance of better responsiveness
			mainFrame.setVisible(false);

			// Write out any final errors we encountered and didn't hit yet
			// We do this now, then write the configuration because, if the loadsavethread
			// is already writing, then we'll give it a bit of extra time
			domain.flushLog();

			// Save the maRla configuration
			try
			{
				Configuration.getInstance().set(Configuration.ConfigType.FirstRun, false);
				Configuration.getInstance().save();
			}
			catch(MarlaException ex)
			{
				Domain.logger.add(ex);
			}

			// Tell thread to stop
			domain.loadSaveThread.stopRunning();

			try
			{
				// Wait for an extra couple seconds beyond the longest it'll take
				// the load save thread to get around to checking if it's closing again
				// The extra time lets it write if needed
				domain.loadSaveThread.join(domain.loadSaveThread.getDelay() + 3000);
			}
			catch(InterruptedException ex)
			{
				// Took too long to finish saving or whatever. Not much we can
				// do about that
				System.err.println("Delay in save thread exiting: " + ex.getMessage());
			}

			// All done
			Updater.notifyExit();
			if(forceQuit)
				System.exit(0);
		}
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel abbreviateButton;
    protected javax.swing.JLabel addDataButton;
    private javax.swing.JMenuItem addDataSetMenuItem;
    protected javax.swing.JDialog answerDialog;
    private javax.swing.JPanel answerPanel;
    private javax.swing.JMenuItem changeInfoMenuItem;
    protected javax.swing.JPanel componentsPanel;
    private javax.swing.JScrollPane componentsScrollPane;
    private javax.swing.JPanel componentsScrollablePanel;
    protected javax.swing.JPanel dataSetContentPanel;
    private javax.swing.JPanel dataSetsPanel;
    protected javax.swing.JScrollPane debugScrollPane;
    protected javax.swing.JTextArea debugTextArea;
    private javax.swing.JMenuItem editDataSetMenuItem;
    protected javax.swing.JPanel emptyPalettePanel;
    protected javax.swing.JFileChooser fileChooserDialog;
    private javax.swing.JLabel fontSizeLabel;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    protected javax.swing.JPanel legendContentPanel;
    private javax.swing.JPanel legendPanel;
    private javax.swing.JPopupMenu.Separator menuSeparator1;
    private javax.swing.JPopupMenu.Separator menuSeparator2;
    private javax.swing.JPopupMenu.Separator menuSeparator3;
    private javax.swing.JLabel minusFontButton;
    protected javax.swing.JLabel newButton;
    protected javax.swing.JLabel openButton;
    private javax.swing.JPanel paletteCardPanel;
    private javax.swing.JLabel plusFontButton;
    private javax.swing.JLabel preWorkspaceLabel;
    protected javax.swing.JPanel preWorkspacePanel;
    private javax.swing.JMenuItem rCodeMenuItem;
    private javax.swing.JMenuItem remarkMenuItem;
    private javax.swing.JPopupMenu rightClickMenu;
    private javax.swing.JPanel rightSidePanel;
    protected javax.swing.JLabel saveButton;
    protected javax.swing.JLabel settingsButton;
    private javax.swing.JMenuItem solutionMenuItem;
    private javax.swing.JMenu tieSubProblemSubMenu;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JLabel trashCan;
    private javax.swing.JMenu untieSubProblemSubMenu;
    private javax.swing.JPanel workspaceCardPanel;
    protected javax.swing.JPanel workspacePanel;
    protected javax.swing.JSplitPane workspaceSplitPane;
    // End of variables declaration//GEN-END:variables
}
