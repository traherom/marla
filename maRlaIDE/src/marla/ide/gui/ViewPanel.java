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
import java.awt.dnd.DragSourceContext;
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
import marla.ide.problem.DataSource;
import marla.ide.problem.MarlaException;
import marla.ide.operation.Operation;
import marla.ide.operation.OperationException;
import marla.ide.operation.OperationInfoCombo;
import marla.ide.operation.OperationInfoRequiredException;
import marla.ide.operation.OperationInformation;
import marla.ide.operation.OperationInformation.PromptType;
import marla.ide.operation.OperationXML;
import marla.ide.problem.DataSet;
import marla.ide.problem.InternalMarlaException;
import marla.ide.problem.Problem;
import marla.ide.problem.SubProblem;
import marla.ide.r.RProcessorException;
import marla.ide.resource.BackgroundThread;
import marla.ide.resource.Configuration;
import marla.ide.resource.DebugThread;
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
	/** The minimum font size.*/
	private final int MINIMUM_FONT_SIZE = 9;
	/** The domain object reference performs generic actions specific to the GUI.*/
	protected Domain domain = new Domain(this);
	/** The New Problem Wizard dialog.*/
	public final NewProblemWizardDialog newProblemWizardDialog = new NewProblemWizardDialog(this, domain);
	/** The Settings dialog.*/
	public final SettingsDialog settingsDialog = new SettingsDialog(this);
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
	private int spaceHeight = 15;
	/** The size of fonts.*/
	public static int fontSize = 12;
	/** Font size and style for workspace plain.*/
	public static Font workspaceFontPlain = new Font("Verdana", Font.PLAIN, ViewPanel.fontSize);
	/** Font size and style for workspace bold.*/
	public static Font workspaceFontBold = new Font("Verdana", Font.BOLD, ViewPanel.fontSize);
	/** The component under the mouse when it is pressed.*/
	protected JComponent componentUnderMouse = null;
	/** The data set being dragged.*/
	protected JComponent draggingComponent = null;
	/** The component currently being hovered over during a drag.*/
	protected JComponent hoverInDragComponent = null;
	/** The component currently being hovered over in the workspace (not during a drag).*/
	protected JComponent hoverComponent = null;
	/** The component that has been right-clicked on.*/
	protected JComponent rightClickedComponent = null;
	/** The x-offset for dragging an item*/
	protected int xDragOffset = -1;
	/** The y-offset for dragging an item*/
	protected int yDragOffset = -1;
	/** The initial x for dragging the component.*/
	private int startX = -1;
	/** The initial y for dragging the component.*/
	private int startY = -1;
	/** The counter illustrating what column we're adding to in the legend.*/
	protected int firstSubCounter = 3;
	/** The first placeholder (second column) in the legend.*/
	protected JLabel secondSub = null;
	/** The second placeholder (third column) in the legend.*/
	protected JLabel thirdSub = null;
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
	protected int buttonPressed = 0;
	/** The label that presents helpful hints on first run.*/
	protected JLabel firstRunLabel = new JLabel();
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
	/** The undo/redo object.*/
	protected UndoRedo<Problem> undoRedo = new UndoRedo<Problem>(50);

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

		// Launch the threads
		domain.debugThread = new DebugThread(debugTextArea);
		domain.backgroundThread = new BackgroundThread(domain);
		domain.debugThread.start();
		domain.backgroundThread.start();

		// Initially, simply display the welcome card until a problem is created new or loaded
		emptyPalettePanel.setVisible(true);
		componentsPanel.setVisible(false);
		preWorkspacePanel.setVisible(true);
		workspacePanel.setVisible(false);

		componentsScrollPane.getViewport().setOpaque(false);

		statusLabel.setText("");

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
			Domain.showWarningDialog(Domain.getTopWindow(), ex.getMessage(), Domain.prettyExceptionDetails(ex), "Reload Error");
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
		int maxWidth = 220;
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
					if (operation.getPreferredSize().width > maxWidth)
					{
						maxWidth = operation.getPreferredSize().width;
					}
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
			catHandlePanel.setPreferredSize(new Dimension(maxWidth, 20));
			catContentPanel.setPreferredSize(new Dimension(maxWidth, catContentPanel.getPreferredSize().height));

			compConstraints.gridy = catCount;
			compConstraints.weighty = 0;
			componentsScrollablePanel.add(wrapperPanel, compConstraints);
			++catCount;
		}
		// Ensure all categories are set to the proper width
		for (Component panel : componentsScrollablePanel.getComponents())
		{
			((JPanel) panel).getComponent (0).setPreferredSize(new Dimension(maxWidth, 20));
			((JPanel) panel).getComponent (1).setPreferredSize(new Dimension(maxWidth, ((JPanel) panel).getComponent (1).getPreferredSize().height));
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
        answersScrollPane = new javax.swing.JScrollPane();
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
        newButton = new ToolbarButton (new ImageIcon (getClass ().getResource (Domain.IMAGES_DIR + "new_button.png")));
        openButton = new ToolbarButton (new ImageIcon (getClass ().getResource (Domain.IMAGES_DIR + "open_button.png")));
        saveButton = new ToolbarButton (new ImageIcon (getClass ().getResource (Domain.IMAGES_DIR + "save_button.png")));
        jSeparator4 = new javax.swing.JToolBar.Separator();
        addDataButton = new ToolbarButton (new ImageIcon (getClass ().getResource (Domain.IMAGES_DIR + "add_data_button.png")));
        jSeparator1 = new javax.swing.JToolBar.Separator();
        fontSizeLabel = new javax.swing.JLabel();
        plusFontButton = new ToolbarButton (new ImageIcon (getClass ().getResource (Domain.IMAGES_DIR + "plus_button.png")));
        minusFontButton = new ToolbarButton (new ImageIcon (getClass ().getResource (Domain.IMAGES_DIR + "minus_button.png")));
        jSeparator3 = new javax.swing.JToolBar.Separator();
        abbreviateButton = new ToolbarButton (new ImageIcon (getClass ().getResource (Domain.IMAGES_DIR + "unchecked_button.png")));
        jSeparator2 = new javax.swing.JToolBar.Separator();
        settingsButton = new ToolbarButton (new ImageIcon (getClass ().getResource (Domain.IMAGES_DIR + "settings_button.png")));
        workspaceSplitPane = new javax.swing.JSplitPane();
        workspaceCardPanel = new javax.swing.JPanel();
        preWorkspacePanel = new javax.swing.JPanel();
        preWorkspaceLabel = new javax.swing.JLabel();
        workspacePanel = new WorkspacePanel (this);
        trashCan = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
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
        subProblemPanel = new javax.swing.JPanel();
        subProblemContentPanel = new javax.swing.JPanel();

        fileChooserDialog.setApproveButtonToolTipText("Open selection");
        fileChooserDialog.setDialogTitle("Browse Problem Location");
        fileChooserDialog.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        answerDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        answerDialog.setTitle("Solution to Point");
        answerDialog.setUndecorated(true);
        answerDialog.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                answerDialogWindowLostFocus(evt);
            }
        });
        answerDialog.getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        answersScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        answerPanel.setLayout(new javax.swing.BoxLayout(answerPanel, javax.swing.BoxLayout.PAGE_AXIS));
        answersScrollPane.setViewportView(answerPanel);

        answerDialog.getContentPane().add(answersScrollPane);

        rightClickMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                rightClickMenuPopupMenuCanceled(evt);
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

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
                .add(preWorkspaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 752, Short.MAX_VALUE)
                .addContainerGap())
        );
        preWorkspacePanelLayout.setVerticalGroup(
            preWorkspacePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(preWorkspacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(preWorkspaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE)
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
        workspacePanel.setLayout(null);

        trashCan.setIcon(new ImageIcon(getClass().getResource(Domain.IMAGES_DIR + "trash_button.png")));
        workspacePanel.add(trashCan);
        trashCan.setBounds(740, 590, 26, 31);

        statusLabel.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        statusLabel.setForeground(new java.awt.Color(153, 153, 153));
        statusLabel.setText("<<Status Label>>");
        workspacePanel.add(statusLabel);
        statusLabel.setBounds(10, 610, 430, 14);

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
            .add(0, 208, Short.MAX_VALUE)
        );
        emptyPalettePanelLayout.setVerticalGroup(
            emptyPalettePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 581, Short.MAX_VALUE)
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

        subProblemPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sub Problems", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N
        subProblemPanel.setLayout(new java.awt.GridLayout(1, 1));

        subProblemContentPanel.setLayout(new java.awt.GridLayout(0, 3));
        subProblemPanel.add(subProblemContentPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        rightSidePanel.add(subProblemPanel, gridBagConstraints);

        add(rightSidePanel, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

	private void workspacePanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMouseDragged
		// The mouse is being dragged, so when starX or startY escape their minimum drag bounds, set the
		// break flag to true; this will allow that operation to be separated from the chain it is currently a part of
		if(Math.abs(startX - evt.getX()) > MIN_DRAG_DIST || Math.abs(startY - evt.getY()) > MIN_DRAG_DIST)
		{
			broken = true;
		}

		// If the operation has been dragged far enough to break out of its chain, allow dragging
		// The following code only pertains to dragging within the workspace (not from out of the palette)
		if(broken)
		{
			// Everything within this condition only gets fired on the VERY FIRST pixel move of a drag;
			// It is essentially the drag initialization step.  Every instance of the drag after this point
			// will fail to meet this condition because componentUnderMouse is null and draggingComponent is
			// the component being dragged
			if(componentUnderMouse != null && draggingComponent == null)
			{
				// We only want to drag if the user is holding down the left button
				if(buttonPressed == MouseEvent.BUTTON1)
				{
					// Change beginning to indicate a undo can be done after this drag completes
					domain.changeBeginning(null);

					// If we are dragging an operation that is connected, our starting point is already defined with startX and startY
					Point point;
					if(startX != -1 && startY != -1)
					{
						point = new Point(startX, startY);
					}
					else
					{
						point = evt.getPoint();
					}

					draggingComponent = componentUnderMouse;
					componentUnderMouse = null;
					// If we're dragging an operation, check if it needs to be broken out of a data set chain
					if(draggingComponent instanceof Operation)
					{
						try
						{
							DataSource parent = ((Operation) draggingComponent).getParentData();
							// If we have a parent, remove from the parent, get our child, and set our child as our parent's new child
							if(parent != null)
							{
								parent.removeOperation((Operation) draggingComponent);
								domain.problem.addUnusedOperation(parent.removeOperation((Operation) draggingComponent));
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
					// We're dragging a data set, so just set the offsets
					else
					{
						xDragOffset = (int) point.getX() - draggingComponent.getX();
						yDragOffset = (int) point.getY() - draggingComponent.getY();
					}

					domain.problem.markUnsaved();
				}
			}

			// We have been dragging a data source within the workspace, but now move to the common dragging
			// code, which is shared with dragging out of the palette
			dragInWorkspace(evt);
		}
	}//GEN-LAST:event_workspacePanelMouseDragged

	private void workspacePanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMouseReleased
		// If the left mouse button was released
		if(buttonPressed == MouseEvent.BUTTON1)
		{
			// We only care if we were dragging something
			if(draggingComponent != null)
			{
				// If the component being dragged is touching the trash can, remove it from the workspace
				if(trashCan.getBounds().intersects(draggingComponent.getBounds()))
				{
					int response = JOptionPane.YES_OPTION;
					// If the component is a data set, prompt the user that it will remove the data set and all operations from the workspace
					if(draggingComponent instanceof DataSet)
					{
						response = Domain.showConfirmDialog(Domain.getTopWindow(), "You are about to remove this data set from the workspace.\nThe data set can be readded to the workspace anytime by dragging\nit back from the list of data sets to the right.\nAre you sure you want to remove this data set?", "Remove Data Set", JOptionPane.YES_NO_OPTION);
						if(response == JOptionPane.YES_OPTION)
						{
							// Remove the data set and all its operations from the workspace
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
							// Since the data set is not being remove from the problem, only the interface, set the data set to
							// "hidden" within the problem so it will no longer be painted
							dataSet.isHidden(true);
						}
					}
					// We are dragging an operation, so remove it from the workspace without a prompt
					else
					{
						// Remove the operation and all child components
						Operation operation = (Operation) draggingComponent;
						for(Operation childOp : operation.getAllChildOperations())
						{
							workspacePanel.remove(childOp);
						}
						workspacePanel.remove(operation);
						domain.problem.removeUnusedOperation(operation);
					}

					// If a component was removed, rebuild the workspace
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
				// We're not touching the trash can, so finish the drag
				else
				{
					// If we're dragging a component, call the "drop" function to ensure a clean drop
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
					// If we're dragging a data set, just drop it
					else
					{
						draggingComponent.setBackground(NO_BACKGROUND_WORKSPACE);
					}

					// Since we're finishing up a drag, rebuild the workspace
					rebuildTree((DataSource) draggingComponent);
				}

				draggingComponent = null;
			}
		}
		// If the right mouse button was released, show the right-click menu
		else if(buttonPressed == MouseEvent.BUTTON3)
		{
			if(showFifth)
			{
				showFifth = false;
				refreshTip();
			}

			// Check the component we're right-clicking on (if any), because what we show in the right-click menu is dependent on that
			JComponent component = (JComponent) workspacePanel.getComponentAt(evt.getPoint());
			if(component != null
			   && component != workspacePanel
			   && component != trashCan
			   && component != statusLabel
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
				// Iterate through all sub problems within this problem, checking if the current data source is contained
				// within any of them--if it is, add that sub problem to the sub problem menu
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
								rightClickedComponent.setBackground(NO_BACKGROUND_WORKSPACE);
								for(DataSource op : ((DataSource) rightClickedComponent).getAllChildOperations())
								{
									op.setBackground(NO_BACKGROUND_WORKSPACE);
								}
								workspacePanel.repaint();
								
								DND_LISTENER.endDrop(null, null);

								rebuildWorkspace();
							}
						});
						item.addMouseListener(new MouseAdapter()
						{
							@Override
							public void mouseEntered(MouseEvent e)
							{
								if(rightClickedComponent != null)
								{
									rightClickedComponent.setBackground(HOVER_BACKGROUND_COLOR);
									for(DataSource op : ((DataSource) rightClickedComponent).getAllChildOperations())
									{
										if(op.getSubProblems().contains(subProblem))
										{
											op.setBackground(HOVER_BACKGROUND_COLOR);
										}
									}
									workspacePanel.repaint();
								}
							}

							@Override
							public void mouseExited(MouseEvent e)
							{
								if(rightClickedComponent != null)
								{
									rightClickedComponent.setBackground(NO_BACKGROUND_WORKSPACE);
									for(DataSource op : ((DataSource) rightClickedComponent).getAllChildOperations())
									{
										if(op.getSubProblems().contains(subProblem))
										{
											op.setBackground(NO_BACKGROUND_WORKSPACE);
										}
									}
									workspacePanel.repaint();
								}
							}
						});
						if(rightClickedComponent != null && untieSubProblemSubMenu.isEnabled())
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
								
								DND_LISTENER.endDrop(null, null);

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

				// Display the data set right-click menu
				if(rightClickedComponent instanceof DataSet)
				{
					solutionMenuItem.setText("Summary");
					editDataSetMenuItem.setEnabled(true);
					changeInfoMenuItem.setEnabled(false);
					remarkMenuItem.setEnabled(false);
				}
				// Display the operation right-click menu
				else
				{
					solutionMenuItem.setText("Solution");
					editDataSetMenuItem.setEnabled(false);
					try
					{
						if (rightClickedComponent != null &&
								rightClickedComponent instanceof Operation)
						{
							if (((Operation) rightClickedComponent).isInfoRequired() &&
								((Operation) rightClickedComponent).getParentData() != null)
							{
								changeInfoMenuItem.setEnabled(true);
							}
							else
							{
								changeInfoMenuItem.setEnabled(false);
							}
							
							if (((Operation) rightClickedComponent).getParentData() != null)
							{
								rCodeMenuItem.setEnabled(true);
							}
							else
							{
								rCodeMenuItem.setEnabled(false);
							}
						}
					}
					catch(MarlaException ex)
					{
						rCodeMenuItem.setEnabled(false);
						changeInfoMenuItem.setEnabled(false);
						Domain.logger.add(ex);
					}
					remarkMenuItem.setEnabled(true);
				}
			}
			// We're not right-clicking on any components, so disable all except "Add Data Set..." operation on the menu
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

			setCursor(Cursor.getDefaultCursor());
			rightClickMenu.show(workspacePanel, evt.getX(), evt.getY());
		}
		else if(rightClickedComponent != null)
		{
			JComponent component = (JComponent) workspacePanel.getComponentAt(evt.getPoint());
			if(component != rightClickedComponent)
			{
				((DataSource) rightClickedComponent).setDefaultColor();
				workspacePanel.repaint();
			}

			rightClickedComponent = null;
		}

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

				boolean goodReq = true;
				if(rightClickedComponent instanceof Operation)
				{
					goodReq = domain.ensureRequirementsMet((Operation) rightClickedComponent);
				}

				if (goodReq)
				{
					answerPanel.removeAll();
					if(rightClickedComponent instanceof Operation && ((Operation) rightClickedComponent).hasPlot())
					{
						JLabel label = new JLabel("");
						label.setIcon(new ImageIcon(((Operation) rightClickedComponent).getPlot()));
						answerPanel.add(label);
					}

					// Always show data, even for graphs
					answerPanel.add(new JLabel("<html>" + ((DataSource) rightClickedComponent).toHTML() + "</html>"));

					if(rightClickedComponent instanceof Operation)
					{
						answerDialog.setTitle("Solution to Point");
					}
					else if(rightClickedComponent instanceof DataSet)
					{
						answerDialog.setTitle("Data Set Summary");
					}

					int width = mainFrame.getLocationOnScreen().x + mainFrame.getWidth() - answerDialogLocation.x;
					if (width > answerPanel.getPreferredSize().width)
					{
						width = answerPanel.getPreferredSize().width + answersScrollPane.getVerticalScrollBar().getPreferredSize().width + (answersScrollPane.getBorder().getBorderInsets(answersScrollPane).left * 2);
					}
					int height = mainFrame.getLocationOnScreen().y + mainFrame.getHeight() - answerDialogLocation.y;
					if (height > answerPanel.getPreferredSize().height)
					{
						height = answerPanel.getPreferredSize().height + answersScrollPane.getVerticalScrollBar().getPreferredSize().height + (answersScrollPane.getBorder().getBorderInsets(answersScrollPane).top * 2);
					}
					answerDialog.setSize(width, height);
					answerDialog.setLocation(answerDialogLocation);
					answerDialog.toFront();
					answerDialog.setVisible(true);
				}
			}
			catch(MarlaException ex)
			{
				Domain.logger.add(ex);
				Domain.showErrorDialog(Domain.getTopWindow(), ex.getMessage(), Domain.prettyExceptionDetails(ex), "Operation Error");
			}
			finally
			{
				startingAnswerPanelDisplay = false;
			}
		}
		DND_LISTENER.endDrop(null, null);
	}//GEN-LAST:event_solutionMenuItemActionPerformed

	private void tieSubProblemSubMenuMenuSelected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_tieSubProblemSubMenuMenuSelected
	{//GEN-HEADEREND:event_tieSubProblemSubMenuMenuSelected
		if(rightClickedComponent != null && tieSubProblemSubMenu.isEnabled())
		{
			rightClickedComponent.setBackground(HOVER_BACKGROUND_COLOR);
			for(DataSource op : ((DataSource) rightClickedComponent).getAllChildOperations())
			{
				op.setBackground(HOVER_BACKGROUND_COLOR);
			}
			workspacePanel.repaint();
		}
	}//GEN-LAST:event_tieSubProblemSubMenuMenuSelected

	private void tieSubProblemSubMenuMenuDeselected(javax.swing.event.MenuEvent evt)//GEN-FIRST:event_tieSubProblemSubMenuMenuDeselected
	{//GEN-HEADEREND:event_tieSubProblemSubMenuMenuDeselected
		if(rightClickedComponent != null && tieSubProblemSubMenu.isEnabled())
		{
			rightClickedComponent.setBackground(NO_BACKGROUND_WORKSPACE);
			for(DataSource op : ((DataSource) rightClickedComponent).getAllChildOperations())
			{
				op.setBackground(NO_BACKGROUND_WORKSPACE);
			}
			workspacePanel.repaint();
		}
	}//GEN-LAST:event_tieSubProblemSubMenuMenuDeselected

	private void workspacePanelComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_workspacePanelComponentResized
	{//GEN-HEADEREND:event_workspacePanelComponentResized
		firstRunLabel.setLocation((workspacePanel.getWidth() - firstRunLabel.getWidth()) / 2, (workspacePanel.getHeight() - firstRunLabel.getHeight()) / 2);
		trashCan.setLocation(workspacePanel.getWidth() - trashCan.getWidth() - 10, workspacePanel.getHeight() - trashCan.getHeight() - 10);
		statusLabel.setLocation(10, workspacePanel.getHeight() - statusLabel.getHeight() - 10);
		ensureComponentsVisible();
	}//GEN-LAST:event_workspacePanelComponentResized

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

				int width = mainFrame.getLocationOnScreen().x + mainFrame.getWidth() - answerDialogLocation.x;
				if (width > answerPanel.getPreferredSize().width)
				{
					width = answerPanel.getPreferredSize().width + answersScrollPane.getVerticalScrollBar().getPreferredSize().width + (answersScrollPane.getBorder().getBorderInsets(answersScrollPane).left * 2);
				}
				int height = mainFrame.getLocationOnScreen().y + mainFrame.getHeight() - answerDialogLocation.y;
				if (height > answerPanel.getPreferredSize().height)
				{
					height = answerPanel.getPreferredSize().height + answersScrollPane.getVerticalScrollBar().getPreferredSize().height + (answersScrollPane.getBorder().getBorderInsets(answersScrollPane).top * 2);
				}
				answerDialog.setSize(width, height);
				answerDialog.setLocation(answerDialogLocation);
				answerDialog.toFront();
				answerDialog.setVisible(true);
			}
			catch(MarlaException ex)
			{
				Domain.logger.add(ex);
			}
		}
		DND_LISTENER.endDrop(null, null);
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
				ImageIcon newIcon = new ImageIcon(getClass().getResource(Domain.IMAGES_DIR + "unchecked_button.png"));
				abbreviateButton.setIcon(newIcon);
				abbreviateButton.setIconStandards(newIcon);
				abbreviated = false;
			}
			else
			{
				ImageIcon newIcon = new ImageIcon(getClass().getResource(Domain.IMAGES_DIR + "checked_button.png"));
				abbreviateButton.setIcon(newIcon);
				abbreviateButton.setIconStandards(newIcon);
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
		if(((ToolbarButton) evt.getSource()).isEnabled()
		   && !((ToolbarButton) evt.getSource()).isSelected()
		   && !initLoading)
		{
			((ToolbarButton) evt.getSource()).setHover(true);
		}
	}//GEN-LAST:event_buttonMouseEntered

	private void buttonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMouseExited
		if(((ToolbarButton) evt.getSource()).isEnabled()
		   && !((ToolbarButton) evt.getSource()).isSelected()
		   && !initLoading)
		{
			((ToolbarButton) evt.getSource()).setHover(false);
		}
	}//GEN-LAST:event_buttonMouseExited

	private void editDataSetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editDataSetMenuItemActionPerformed
		if(rightClickedComponent != null)
		{
			newProblemWizardDialog.editDataSet((DataSet) rightClickedComponent);
		}
		DND_LISTENER.endDrop(null, null);
	}//GEN-LAST:event_editDataSetMenuItemActionPerformed

	private void changeInfoMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_changeInfoMenuItemActionPerformed
	{//GEN-HEADEREND:event_changeInfoMenuItemActionPerformed
		if(rightClickedComponent != null && rightClickedComponent instanceof Operation)
		{
			try
			{
				DataSource parentData = ((Operation) rightClickedComponent).getParentData();
				boolean allow = true;
				if (parentData instanceof Operation)
				{
					if (((Operation) parentData).isInfoUnanswered())
					{
						Domain.showInformationDialog(Domain.getTopWindow(), "The parent of this operation has unmet requirements. Set the parameters of all\nparent operations first, then you can set the parameters of this operation.", "Child Operation");
						allow = false;
					}
				}
				if(allow && ((Operation) rightClickedComponent).isInfoRequired())
				{
					ViewPanel.getRequiredInfoDialog((Operation) rightClickedComponent, true);
				}
			}
			catch(MarlaException ex)
			{
				Domain.logger.add(ex);
			}
		}
		DND_LISTENER.endDrop(null, null);
	}//GEN-LAST:event_changeInfoMenuItemActionPerformed

	private void answerDialogWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_answerDialogWindowLostFocus
		if(!startingAnswerPanelDisplay)
		{
			answerDialog.dispose();
			DND_LISTENER.endDrop(null, null);
		}
	}//GEN-LAST:event_answerDialogWindowLostFocus

	private void remarkMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remarkMenuItemActionPerformed
		if(rightClickedComponent != null && rightClickedComponent instanceof Operation)
		{
			String newRemark = Domain.showMultiLineInputDialog(Domain.getTopWindow(), "Give a remark for this operation:", "Operation Remark", ((Operation) rightClickedComponent).getRemark());
			if(!newRemark.equals(((Operation) rightClickedComponent).getRemark()))
			{
				((Operation) rightClickedComponent).setRemark(newRemark);
			}
		}
		DND_LISTENER.endDrop(null, null);
	}//GEN-LAST:event_remarkMenuItemActionPerformed

	private void workspacePanelMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_workspacePanelMousePressed
	{//GEN-HEADEREND:event_workspacePanelMousePressed
		// Since a drag operation does not properly return the button pressed, save it at the press start
		buttonPressed = evt.getButton();
		Component comp = workspacePanel.getComponentAt(evt.getPoint());
		// If we have pressed on a valid component, save the starting coordinates. If the component is an Operation that
		// is connected to a data set, later we only want to break it out of that data set if the mouse is dragged
		// a certain number of pixels out of the expected range
		if(comp != null
		   && comp != workspacePanel
		   && comp != trashCan
		   && comp != statusLabel
		   && comp != firstRunLabel)
		{
			// If we are hovering over a a component that we care about, set the cursor accordingly
			// and the component to a hovered state
			if(comp instanceof DataSource)
			{
				hoverComponent = (JComponent) comp;
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				hoverComponent.setForeground(Color.GRAY);
			}

			componentUnderMouse = (JComponent) comp;
			if(comp instanceof Operation && ((Operation) comp).getParentData() != null)
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
		// We only care if the mouse has moved when we're NOT dragging, and buttonPressed gets set on mousePressed
		if(buttonPressed == 0 && rightClickedComponent == null)
		{
			JComponent component = (JComponent) workspacePanel.getComponentAt(evt.getPoint());

			// This is from a previous movement--if we are no longer hovering over that component,
			// revert our cursor and that component back to it's proper state
			if(hoverComponent != null)
			{
				if(component != hoverComponent)
				{
					setCursor(Cursor.getDefaultCursor());
				}
				if(!rightClickMenu.isShowing())
				{
					((DataSource) hoverComponent).setDefaultColor();
					hoverComponent = null;
				}
			}

			// Check that the component we're over is not any of the special components that we want to ignore
			if(component != null
			   && component != workspacePanel
			   && component != trashCan
			   && component != statusLabel
			   && component != firstRunLabel)
			{
				// If we are hovering over a a component that we care about, set the cursor accordingly
				// and the component to a hovered state
				if(component instanceof DataSource)
				{
					hoverComponent = component;
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					hoverComponent.setForeground(Color.GRAY);
				}
			}

			workspacePanel.repaint();
		}
	}//GEN-LAST:event_workspacePanelMouseMoved

	private void addDataSetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDataSetMenuItemActionPerformed
		newProblemWizardDialog.addDataSet();
		DND_LISTENER.endDrop(null, null);
	}//GEN-LAST:event_addDataSetMenuItemActionPerformed

	private void rightClickMenuPopupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_rightClickMenuPopupMenuCanceled
		DND_LISTENER.endDrop(null, null);
	}//GEN-LAST:event_rightClickMenuPopupMenuCanceled

	/**
	 * Undo the last operation.
	 */
	protected void undo()
	{
		if(undoRedo.hasUndo())
		{
			String msg = undoRedo.undoMessage();
			Problem problem = undoRedo.undo(domain.problem);
			if(msg != null)
				domain.backgroundThread.addStatus(msg);
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
		if(undoRedo.hasRedo())
		{
			String msg = undoRedo.redoMessage();
			Problem problem = undoRedo.redo(domain.problem);
			if(msg != null)
				domain.backgroundThread.addStatus(msg);
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
		// We only drag in the workspace if a dragging component is known (either within the workspace or from the palette) and the
		// LEFT mouse button is being pressed
		if((dragFromPalette || draggingComponent != null) && buttonPressed == MouseEvent.BUTTON1)
		{
			if(dragFromPalette)
			{
				setCursor(((DragSourceContext) DND_LISTENER.getDragSourceContext()).getCursor());
			}

			// From the last iteration of the drag call, if we were over a component then, clear the hover on that component
			if(hoverInDragComponent != null)
			{
				hoverInDragComponent.setBackground(NO_BACKGROUND_WORKSPACE);
				hoverInDragComponent = null;
			}

			// Identify a component, if any, that we are currently dragging over in the workspace
			Component component = workspacePanel.getComponentAt(evt.getPoint().x, evt.getPoint().y, draggingComponent);
			if(component != null
			   && component != trashCan
			   && component != statusLabel
			   && component != firstRunLabel)
			{
				// We are dragging over a valid component, so set the drag border back
				if(component instanceof DataSource && (draggingComponent == null || (draggingComponent != null && draggingComponent instanceof Operation)))
				{
					hoverInDragComponent = (JComponent) workspacePanel.getComponentAt(evt.getPoint().x, evt.getPoint().y, draggingComponent);
					hoverInDragComponent.setBackground(HOVER_BACKGROUND_COLOR);
				}
			}

			// If we are dragging within the workspace (not from the palette), adjust the x/y position as necessary
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

				// Reposition every sub component under the dragging component
				rebuildTree((DataSource) draggingComponent);
			}
			else
			{
				// Repaint the workspace if we are dragging from the palette to ensure hover/unhover components are properly painted
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
	 * Rebuild the tree in the interface for the given data set. Assumes
	 * that the given DataSource is the top of the tree and works with 
	 * everything below it to center it
	 *
	 * @param ds The data set to rebuild in the interface.
	 */
	protected void rebuildTree(DataSource ds)
	{
		// Don't bother listening yet if the problem is still loading
		Problem prob = ds.getParentProblem();
		if(prob != null && prob.isLoading())
			return;
		
		// Actually work with the top of the tree
		DataSource topDS = ds.getRootDataSource();
		
		// Set all the labels to be the right width
		topDS.setFont(workspaceFontBold);
		topDS.setText("<html>" + topDS.getDisplayString(abbreviated) + "</html>");
		topDS.setSize(topDS.getPreferredSize());
		
		for(DataSource child : topDS.getAllChildOperations())
		{
			child.setFont(workspaceFontBold);
			child.setText("<html>" + child.getDisplayString(abbreviated) + "</html>");
			child.setSize(child.getPreferredSize());
		}
		
		// Rebuild ourselves just...somewhere
		int realX = topDS.getX();
		int realY = topDS.getY();
		rebuildTree(topDS, 0, 0);

		// Shift everything based on where we actually used to be
		shiftPosition(topDS, realX - topDS.getX(), realY - topDS.getY());

		// Redraw everything
		workspacePanel.repaint();
	}
	
	/**
	 * Rebuild the tree in the interface for the given data set.
	 *
	 * @param ds The data set to rebuild in the interface.
	 * @param leftX X coordinate which the tree may start after
	 * @param topY Y coordinate to start the tree at
	 * @param Right-most pixel we extend to
	 */
	private int rebuildTree(DataSource ds, int leftX, int topY)
	{
		// Tell each child to build themselves. They'll tell us where they end
		// and we pass that off to the next child
		int opCount = ds.getOperationCount();

		int totalWidth = (opCount - 1) * spaceWidth;

		int currX = leftX;
		int childY = topY + ds.getHeight() + spaceHeight;

		for(int i = 0; i < opCount; i++)
		{
			int childEndX = rebuildTree(ds.getOperation(i), currX, childY);

			totalWidth += childEndX - currX;

			// Shift next child to be beyond where this one ended
			currX = childEndX + spaceWidth;
		}

		// Position ourselves over our middle child (if applicable)
		if(opCount == 0)
		{
			// Just place ourselves at the given left
			ds.setLocation(leftX, topY);
		}
		else if(opCount % 2 == 0)
		{
			// Center over middle two children
			Operation centerChild1 = ds.getOperation(opCount / 2 - 1);
			Operation centerChild2 = ds.getOperation(opCount / 2);
			
			int centerChildX = (centerChild1.getX() + centerChild1.getWidth() + centerChild2.getX()) / 2;

			ds.setLocation(centerChildX - ds.getWidth() / 2, topY);
		}
		else
		{
			// Center over middle child
			Operation centerChild = ds.getOperation(opCount / 2);
			ds.setLocation(centerChild.getX() + centerChild.getWidth() / 2 - ds.getWidth() / 2, topY);
		}

		// If our children are not wider than us, shift their left side
		// We need to be the actual left side
		int ourWidth = ds.getWidth();
		if(totalWidth < ourWidth)
		{
			shiftPosition(ds, leftX - ds.getX(), 0);
			
			totalWidth = ourWidth;
		}

		// Return the last location we placed stuff at
		return leftX + totalWidth;
	}

	/**
	 * Shifts, location-wise, the given DataSource and all its children by the
	 * specified amount.
	 * @param ds DataSource to shift
	 * @param shiftX Number of pixels to move left (positive) or right
	 * @param shiftY Number of pixels to move up (positive) or down
	 */
	private void shiftPosition(DataSource ds, int shiftX, int shiftY)
	{
		ds.setLocation(ds.getX() + shiftX, ds.getY() + shiftY);
		
		for(DataSource child : ds.getAllChildOperations())
			child.setLocation(child.getX() + shiftX, child.getY() + shiftY);
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
		// If this operation was dragged from the palette, set its default colors back in the palette
		if(duplicate)
		{
			operation.setDefaultColor();
			operation.setBackground(NO_BACKGROUND_WORKSPACE);
			componentsScrollablePanel.repaint();
		}

		// Get the component we're trying to drop onto, if it exists
		JComponent component = (JComponent) workspacePanel.getComponentAt(location.x, location.y, operation);
		if(component != trashCan
		   && component != statusLabel
		   && component != firstRunLabel
		   && (component == null || component instanceof DataSet || component instanceof Operation))
		{
			Operation newOperation;
			// If we are dragging from the palette, we need to create a new instance of the palette's operation
			if(duplicate)
			{
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
			// Otherwise use the object we've been dragging
			else
			{
				newOperation = operation;
			}

			// We're dropping onto an operation, so insert as necessary
			if(component instanceof Operation)
			{
				setCursor(Cursor.getDefaultCursor());

				Operation dropOperation = (Operation) component;
				if(dropOperation != newOperation)
				{
					domain.problem.removeUnusedOperation(newOperation);
					dropOperation.addOperation(newOperation);
				}

				operation.setDefaultColor();
				operation.setBackground(NO_BACKGROUND_WORKSPACE);
				if(showThird)
				{
					showThird = false;
					refreshTip();
				}
			}
			// We are dropping from the palette onto nothing
			else if(component == null)
			{
				domain.problem.addUnusedOperation(newOperation);
				newOperation.setLocation((int) location.getX() - xDragOffset, (int) location.getY() - yDragOffset);
			}
			// We're likely dropping onto a data set
			else
			{
				if(component instanceof DataSet)
				{
					setCursor(Cursor.getDefaultCursor());

					// Add as child and ensure we're not listed as unused
					DataSet dataSet = (DataSet) component;
					domain.problem.removeUnusedOperation(newOperation);
					dataSet.addOperation(newOperation);
				}

				operation.setDefaultColor();
				operation.setBackground(NO_BACKGROUND_WORKSPACE);
				if(showThird)
				{
					showThird = false;
					refreshTip();
				}
			}

			workspacePanel.add(newOperation);
		}

		if(hoverInDragComponent != null)
		{
			hoverInDragComponent.setBackground(NO_BACKGROUND_WORKSPACE);
			hoverInDragComponent = null;
		}

		rebuildWorkspace();

		// The drag is complete, so revert the button pressed
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

				Object[] array = ((OperationInfoCombo) question).getOptions().toArray();
				for (int i = 0; i < array.length; ++i)
				{
					array[i] = "<html>" + array[i] + "</html>";
				}
				DefaultComboBoxModel model = new DefaultComboBoxModel(array);
				JComboBox comboBox = new JComboBox(model);
				if(question.getAnswer() != null)
					comboBox.setSelectedItem("<html>" + question.getAnswer() + "</html>");

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
		doneButton.getInsets().set(10, 10, 10, 10);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.getInsets().set(10, 10, 10, 10);
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
				Domain.cancelExport = true;
			}
		});
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.insets.set(3, 5, 3, 5);
		buttonPanel.add(doneButton, gbc);
		gbc.gridx = 1;
		buttonPanel.add(cancelButton, gbc);
		panel.add(buttonPanel);

		if(showDialog)
		{
			// Display dialog
			dialog.pack();
			dialog.setLocationRelativeTo(Domain.getTopWindow());
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
					String string = ((JComboBox) valueComponents.get(i)).getSelectedItem().toString();
					string = string.substring(6, string.length() - 7);
					question.setAnswer(string);
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
					Domain.showErrorDialog(Domain.getTopWindow(), ex.getMessage(), Domain.prettyExceptionDetails(ex), "Invalid Input");
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

			if(!isUndoRedo)
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

			rebuildDataSetLegend();
			rebuildSubProblemLegend();

			workspacePanel.repaint();
		}
	}
	
	/**
	 * Add all sub problems to the sub problem legend panel.
	 */
	protected void rebuildSubProblemLegend()
	{
		// Add sub problems to legend
		subProblemContentPanel.removeAll();
		
		((GridLayout) subProblemContentPanel.getLayout()).setColumns(3);
		((GridLayout) subProblemContentPanel.getLayout()).setRows(0);
		firstSubCounter = 3;
		for(int i = 0; i < domain.problem.getSubProblemCount(); ++i)
		{
			// Add sub problem to legend
			JLabel firstSub;
			if(firstSubCounter == 1)
			{
				firstSub = secondSub;
			}
			else if(firstSubCounter == 2)
			{
				firstSub = thirdSub;
			}
			else
			{
				firstSub = new JLabel("");
				secondSub = new JLabel("");
				thirdSub = new JLabel("");
			}
			firstSub.setFont(FONT_PLAIN_12);
			firstSub.setText(domain.problem.getSubProblem(i).getSubproblemID());
			firstSub.setForeground(domain.problem.getSubProblem(i).getColor());

			if(firstSubCounter == 3)
			{
				firstSubCounter = 0;

				GridLayout layout = (GridLayout) subProblemContentPanel.getLayout();
				layout.setRows(layout.getRows() + 1);

				subProblemContentPanel.add(firstSub);
				subProblemContentPanel.add(secondSub);
				subProblemContentPanel.add(thirdSub);
			}
			++firstSubCounter;
		}
		
		if(subProblemContentPanel.getComponentCount() == 0)
		{
			((GridLayout) subProblemContentPanel.getLayout()).setColumns(1);
			((GridLayout) dataSetContentPanel.getLayout()).setRows(1);
			JLabel noneLabel = new JLabel("-No Sub Problems-");
			noneLabel.setFont(FONT_BOLD_12);
			subProblemContentPanel.add(noneLabel);
		}
		
		subProblemContentPanel.invalidate();
		subProblemContentPanel.revalidate();
		subProblemContentPanel.repaint();
	}

	/**
	 * Add all data sets to the data set legend panel.
	 */
	protected void rebuildDataSetLegend()
	{
		// Add data sets to legend
		dataSetContentPanel.removeAll();
		
		((GridLayout) dataSetContentPanel.getLayout()).setColumns(3);
		((GridLayout) dataSetContentPanel.getLayout()).setRows(0);
		firstDataCounter = 3;
		for(int i = 0; i < domain.problem.getDataCount(); ++i)
		{
			// Add sub problem to legend
			JLabel firstData;
			if(firstDataCounter == 1)
			{
				firstData = secondData;
			}
			else if(firstDataCounter == 2)
			{
				firstData = thirdData;
			}
			else
			{
				firstData = new JLabel("");
				DRAG_SOURCE.createDefaultDragGestureRecognizer(firstData, DnDConstants.ACTION_MOVE, DND_LISTENER);
				final JLabel finalFirstLabel = firstData;
				firstData.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseEntered(MouseEvent evt)
					{
						if(!finalFirstLabel.getText().equals(""))
						{
							setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							finalFirstLabel.setForeground(Color.GRAY);
						}
					}

					@Override
					public void mouseExited(MouseEvent evt)
					{
						if(!finalFirstLabel.getText().equals(""))
						{
							setCursor(Cursor.getDefaultCursor());
							finalFirstLabel.setForeground(DataSet.getDefaultColor());
						}
					}

					@Override
					public void mousePressed(MouseEvent evt)
					{
						if(!finalFirstLabel.getText().equals(""))
						{
							buttonPressed = evt.getButton();
							xDragOffset = (int) evt.getLocationOnScreen().getX() - (int) finalFirstLabel.getLocationOnScreen().getX();
							yDragOffset = (int) evt.getLocationOnScreen().getY() - (int) finalFirstLabel.getLocationOnScreen().getY();
						}
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
						if(!finalSecondLabel.getText().equals(""))
						{
							setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							finalSecondLabel.setForeground(Color.GRAY);
						}
					}

					@Override
					public void mouseExited(MouseEvent evt)
					{
						if(!finalSecondLabel.getText().equals(""))
						{
							setCursor(Cursor.getDefaultCursor());
							finalSecondLabel.setForeground(DataSet.getDefaultColor());
						}
					}

					@Override
					public void mousePressed(MouseEvent evt)
					{
						if(!finalSecondLabel.getText().equals(""))
						{
							buttonPressed = evt.getButton();
							xDragOffset = (int) evt.getLocationOnScreen().getX() - (int) finalSecondLabel.getLocationOnScreen().getX();
							yDragOffset = (int) evt.getLocationOnScreen().getY() - (int) finalSecondLabel.getLocationOnScreen().getY();
						}
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
						if(!finalThirdLabel.getText().equals(""))
						{
							setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							finalThirdLabel.setForeground(Color.GRAY);
						}
					}

					@Override
					public void mouseExited(MouseEvent evt)
					{
						if(!finalThirdLabel.getText().equals(""))
						{
							setCursor(Cursor.getDefaultCursor());
							finalThirdLabel.setForeground(DataSet.getDefaultColor());
						}
					}

					@Override
					public void mousePressed(MouseEvent evt)
					{
						if(!finalThirdLabel.getText().equals(""))
						{
							buttonPressed = evt.getButton();
							xDragOffset = (int) evt.getLocationOnScreen().getX() - (int) finalThirdLabel.getLocationOnScreen().getX();
							yDragOffset = (int) evt.getLocationOnScreen().getY() - (int) finalThirdLabel.getLocationOnScreen().getY();
						}
					}
				});
			}
			firstData.setFont(FONT_PLAIN_12);
			firstData.setText(domain.problem.getData(i).getName());
			firstData.setForeground(DataSet.getDefaultColor());

			if(firstDataCounter == 3)
			{
				firstDataCounter = 0;

				GridLayout layout = (GridLayout) dataSetContentPanel.getLayout();
				layout.setRows(layout.getRows() + 1);

				dataSetContentPanel.add(firstData);
				dataSetContentPanel.add(secondData);
				dataSetContentPanel.add(thirdData);
			}
			++firstDataCounter;
		}
		
		if(dataSetContentPanel.getComponentCount() == 0)
		{
			((GridLayout) dataSetContentPanel.getLayout()).setColumns(1);
			((GridLayout) dataSetContentPanel.getLayout()).setRows(1);
			JLabel noneLabel = new JLabel("-No Data Sets-");
			noneLabel.setFont(FONT_BOLD_12);
			dataSetContentPanel.add(noneLabel);
		}
		
		dataSetContentPanel.invalidate();
		dataSetContentPanel.revalidate();
		dataSetContentPanel.repaint();
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
					response = Domain.showConfirmDialog(Domain.getTopWindow(), "Would you like to save changes to the currently open problem?", "Save Problem Changes", JOptionPane.YES_NO_CANCEL_OPTION);
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
						Domain.showErrorDialog(Domain.getTopWindow(), ex.getMessage(), Domain.prettyExceptionDetails(ex), "Save Failed");
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
				workspacePanel.add(statusLabel);

				emptyPalettePanel.setVisible(true);
				componentsPanel.setVisible(false);
				preWorkspacePanel.setVisible(true);
				workspacePanel.setVisible(false);

				dataSetContentPanel.removeAll();
				((GridLayout) dataSetContentPanel.getLayout()).setRows(0);
				subProblemContentPanel.removeAll();
				((GridLayout) subProblemContentPanel.getLayout()).setRows(0);

				domain.problem = null;

				mainFrame.setTitle(mainFrame.getDefaultTitle());
			}
		}
		workspacePanel.repaint();

		if(!isUndoRedo)
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

		boolean found = false;
		try
		{
			if(operationEditorExe.exists())
			{
				domain.desktop.open(operationEditorExe);
				found = true;
			}
			else if(operationEditorJar.exists())
			{
				if(Domain.OS_NAME.toLowerCase().contains("windows"))
				{
					domain.desktop.open(operationEditorJar);
				}
				else
				{
					Runtime.getRuntime().exec(new String[]
							{
								"java", "-jar", operationEditorJar.getCanonicalPath()
							}, null, null);
				}
				found = true;
			}
		}
		catch(IOException ex)
		{
			Domain.logger.add(ex);
		}

		if(!found)
		{
			Domain.showInformationDialog(Domain.getTopWindow(), "The maRla Operation Editor could not be found.", "maRla Operation Editor Not Found");
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

			// Tell threads to stop
			domain.backgroundThread.stopRunning();
			domain.debugThread.stopRunning();
			
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

			// Ensure both threads finished
			try
			{
				domain.backgroundThread.join(domain.backgroundThread.getDelay() + 3000);
				domain.debugThread.join(domain.debugThread.getDelay() + 3000);
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
    private marla.ide.gui.ToolbarButton abbreviateButton;
    protected marla.ide.gui.ToolbarButton addDataButton;
    private javax.swing.JMenuItem addDataSetMenuItem;
    public javax.swing.JDialog answerDialog;
    private javax.swing.JPanel answerPanel;
    private javax.swing.JScrollPane answersScrollPane;
    private javax.swing.JMenuItem changeInfoMenuItem;
    protected javax.swing.JPanel componentsPanel;
    private javax.swing.JScrollPane componentsScrollPane;
    protected javax.swing.JPanel componentsScrollablePanel;
    protected javax.swing.JPanel dataSetContentPanel;
    private javax.swing.JPanel dataSetsPanel;
    protected javax.swing.JScrollPane debugScrollPane;
    protected javax.swing.JTextArea debugTextArea;
    private javax.swing.JMenuItem editDataSetMenuItem;
    protected javax.swing.JPanel emptyPalettePanel;
    public javax.swing.JFileChooser fileChooserDialog;
    private javax.swing.JLabel fontSizeLabel;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator menuSeparator1;
    private javax.swing.JPopupMenu.Separator menuSeparator2;
    private javax.swing.JPopupMenu.Separator menuSeparator3;
    private marla.ide.gui.ToolbarButton minusFontButton;
    protected marla.ide.gui.ToolbarButton newButton;
    protected marla.ide.gui.ToolbarButton openButton;
    private javax.swing.JPanel paletteCardPanel;
    private marla.ide.gui.ToolbarButton plusFontButton;
    private javax.swing.JLabel preWorkspaceLabel;
    protected javax.swing.JPanel preWorkspacePanel;
    private javax.swing.JMenuItem rCodeMenuItem;
    private javax.swing.JMenuItem remarkMenuItem;
    private javax.swing.JPopupMenu rightClickMenu;
    private javax.swing.JPanel rightSidePanel;
    protected marla.ide.gui.ToolbarButton saveButton;
    protected marla.ide.gui.ToolbarButton settingsButton;
    private javax.swing.JMenuItem solutionMenuItem;
    protected javax.swing.JLabel statusLabel;
    protected javax.swing.JPanel subProblemContentPanel;
    private javax.swing.JPanel subProblemPanel;
    private javax.swing.JMenu tieSubProblemSubMenu;
    private javax.swing.JToolBar toolBar;
    protected javax.swing.JLabel trashCan;
    private javax.swing.JMenu untieSubProblemSubMenu;
    private javax.swing.JPanel workspaceCardPanel;
    protected marla.ide.gui.WorkspacePanel workspacePanel;
    protected javax.swing.JSplitPane workspaceSplitPane;
    // End of variables declaration//GEN-END:variables
}
