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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.jdom.JDOMException;
import problem.CalcException;
import problem.DataColumn;
import problem.DataNotFound;
import problem.DataSet;
import problem.FileException;
import problem.IncompleteInitialization;
import problem.Operation;
import problem.Problem;
import r.OperationXML;
import r.OperationXMLException;
import r.RProcessorException;
import r.RProcessorParseException;
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
	/** The alphabet.*/
	public static final String[] ALPHABET = new String[] {"a.", "b.", "c.", "d.", "e.", "f.",
														  "g.", "h.", "i.", "j.", "k.", "l.",
														  "m.", "n.", "o.", "p.", "q.", "r.",
														  "s.", "t.", "u.", "v.", "w.", "x.",
														  "y.", "z."};
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
	/** The list in the New Problem Wizard of sub problems within the current problem.*/
	private ArrayList<JPanel> subProblemPanels = new ArrayList<JPanel> ();
	/** True if the New Problem Wizard is being opened and actions should be ignored.*/
	private boolean openingWizard = false;
	/** Set true once the New Problem Wizard is told to overwrite existing files.*/
	private boolean newProblemOverwrite = false;
	/** The set of operations contained in the XML file.*/
	private ArrayList<String> operations;
	/** True if the current problem is being edited in the wizard, false if creating a new problem in the wizard.*/
	protected boolean editing = false;
	/** The operation being dragged.*/
	private Operation operationDragging = null;
	/** The data set being dragged.*/
	private DataSet dataSetDragging = null;
	/** The operation currently selected.*/
	private Operation selectedOperation = null;
	/** The data set currently selected.*/
	private DataSet selectedDataSet = null;
	/** The component currently being hovered over.*/
	private JComponent hoveredComponent = null;
	/** The x-offset for dragging an item*/
	private int xDragOffset = -1;
	/** The y-offset for dragging an item*/
	private int yDragOffset = -1;
	/** The default file filter for a JFileChooser open dialog.*/
	private FileFilter defaultFilter;
	/** The extensions file filter for CSV files.*/
	private ExtensionFileFilter csvFilter = new ExtensionFileFilter ("Comma Separated Values (.csv, .txt)", new String[] {"CSV", "TXT"});
	/** The extensions file filter for CSV files.*/
	protected ExtensionFileFilter marlaFilter = new ExtensionFileFilter ("The maRla Project Files (.marla)", new String[] {"MARLA"});

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

		dataSetTabbedPane.addMouseListener (new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent evt)
			{
				for (int i = 0; i < dataSetTabbedPane.getTabCount(); ++i)
				{
					if (dataSetTabbedPane.getUI().getTabBounds(dataSetTabbedPane, i).contains (evt.getPoint ()) && evt.getClickCount () == 2)
					{
						Object name = JOptionPane.showInputDialog(newProblemWizardDialog,
								"Give the data set a new name:",
								"Data Set Name",
								JOptionPane.QUESTION_MESSAGE,
								null,
								null,
								dataSetTabbedPane.getTitleAt(i));
						if (name != null)
						{
							if (!name.toString ().equals (dataSetTabbedPane.getTitleAt(i)))
							{
								if (!dataSetNameExists (i, name.toString ()))
								{
									dataSetTabbedPane.setTitleAt (i, name.toString ());
								}
								else
								{
									JOptionPane.showMessageDialog(newProblemWizardDialog, "A column with that name already exists.", "Duplicate Column", JOptionPane.WARNING_MESSAGE);
								}
							}
						}
					}
				}
			}
		});
		
		try		
		{
			OperationXML.loadXML(Domain.xmlPath);
			operations = OperationXML.getAvailableOperations();

			// Add all operation types to the palette, adding listeners to the labels as we go
			for (int i = 0; i < operations.size (); ++i)
			{
				final OperationXML operation = OperationXML.createOperation(operations.get (i));

				operation.addMouseListener(new MouseAdapter ()
				{
					@Override
					public void mouseReleased(MouseEvent evt)
					{
						addToWorkspace (operation);
					}
				});

				if (i % 2 == 0)
				{
					leftPanel.add (operation);
				}
				else
				{
					rightPanel.add (operation);
				}
			}
		}
		catch (JDOMException ex)
		{
		}
		catch (IOException ex)
		{
		}
		catch (OperationXMLException ex)
		{
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

        newProblemWizardDialog = new EscapeDialog (this);
        stepsPanel = new javax.swing.JPanel();
        stepsLabel = new javax.swing.JLabel();
        welcomeLabel = new javax.swing.JLabel();
        nameAndLocationLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        valuesLabel = new javax.swing.JLabel();
        stepsLineLabel = new javax.swing.JLabel();
        subProblemsLabel = new javax.swing.JLabel();
        wizardCardPanel = new javax.swing.JPanel();
        welcomeCardPanel = new javax.swing.JPanel();
        welcomeWizardLabel = new javax.swing.JLabel();
        wizardLineCard1 = new javax.swing.JLabel();
        welcomePanel = new javax.swing.JPanel();
        welcomeTextLabel = new javax.swing.JLabel();
        nameAndLocationCardPanel = new javax.swing.JPanel();
        wizardLineCard2 = new javax.swing.JLabel();
        nameAndLocationWizardLabel = new javax.swing.JLabel();
        nameAndLocationPanel = new javax.swing.JPanel();
        problemNameLabel = new javax.swing.JLabel();
        problemNameTextField = new javax.swing.JTextField();
        problemLocationLabel = new javax.swing.JLabel();
        problemLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        descriptionCardPanel = new javax.swing.JPanel();
        wizardLineCard3 = new javax.swing.JLabel();
        descriptionWizardLabel = new javax.swing.JLabel();
        descriptionPanel = new javax.swing.JPanel();
        descroptionScollPane = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        problemDescriptionLabel = new javax.swing.JLabel();
        subProblemsCardPanel = new javax.swing.JPanel();
        wizardLineCard5 = new javax.swing.JLabel();
        subProblemsWizardLabel = new javax.swing.JLabel();
        subProblemsPanel = new javax.swing.JPanel();
        subProblemsScrollPane = new javax.swing.JScrollPane();
        subProblemsScollablePanel = new javax.swing.JPanel();
        addSubProblemButton = new javax.swing.JButton();
        removeSubProblemButton = new javax.swing.JButton();
        valuesCardPanel = new javax.swing.JPanel();
        wizardLineCard4 = new javax.swing.JLabel();
        valuesWizardLabel = new javax.swing.JLabel();
        dataSetTabbedPane = new javax.swing.JTabbedPane();
        addDataSetButton = new javax.swing.JButton();
        removeDataSetButton = new javax.swing.JButton();
        wizardControlPanel = new javax.swing.JPanel();
        closeWizardButton = new javax.swing.JButton();
        nextWizardButton = new javax.swing.JButton();
        backWizardButton = new javax.swing.JButton();
        openChooserDialog = new javax.swing.JFileChooser();
        saveChooserDialog = new javax.swing.JFileChooser();
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
        workspacePanel = new javax.swing.JPanel();
        trayPanel = new javax.swing.JPanel();
        outputScrollPane = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();

        newProblemWizardDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        newProblemWizardDialog.setTitle("New Problem Wizard");
        newProblemWizardDialog.setResizable(false);

        stepsPanel.setBackground(new java.awt.Color(240, 239, 239));
        stepsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        stepsPanel.setLayout(null);

        stepsLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        stepsLabel.setText("Steps");
        stepsPanel.add(stepsLabel);
        stepsLabel.setBounds(10, 10, 37, 16);

        welcomeLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        welcomeLabel.setText("1. Welcome");
        stepsPanel.add(welcomeLabel);
        welcomeLabel.setBounds(20, 40, 140, 16);

        nameAndLocationLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        nameAndLocationLabel.setText("2. Name and Location");
        stepsPanel.add(nameAndLocationLabel);
        nameAndLocationLabel.setBounds(20, 60, 160, 16);

        descriptionLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        descriptionLabel.setText("3. Description");
        stepsPanel.add(descriptionLabel);
        descriptionLabel.setBounds(20, 80, 140, 16);

        valuesLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        valuesLabel.setText("5. Values");
        stepsPanel.add(valuesLabel);
        valuesLabel.setBounds(20, 120, 140, 16);

        stepsLineLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        stepsLineLabel.setText("_____________________");
        stepsPanel.add(stepsLineLabel);
        stepsLineLabel.setBounds(10, 10, 170, 20);

        subProblemsLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        subProblemsLabel.setText("4. Sub Problems");
        stepsPanel.add(subProblemsLabel);
        subProblemsLabel.setBounds(20, 100, 140, 16);

        wizardCardPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        wizardCardPanel.setLayout(new java.awt.CardLayout());

        welcomeCardPanel.setLayout(null);

        welcomeWizardLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        welcomeWizardLabel.setText("Welcome");
        welcomeCardPanel.add(welcomeWizardLabel);
        welcomeWizardLabel.setBounds(10, 10, 70, 16);

        wizardLineCard1.setFont(new java.awt.Font("Verdana", 0, 12));
        wizardLineCard1.setText("______________________________________________________");
        welcomeCardPanel.add(wizardLineCard1);
        wizardLineCard1.setBounds(10, 10, 440, 20);

        welcomeTextLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        welcomeTextLabel.setText("<<Welcome Text Placeholder>>");

        org.jdesktop.layout.GroupLayout welcomePanelLayout = new org.jdesktop.layout.GroupLayout(welcomePanel);
        welcomePanel.setLayout(welcomePanelLayout);
        welcomePanelLayout.setHorizontalGroup(
            welcomePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(welcomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(welcomeTextLabel)
                .addContainerGap(249, Short.MAX_VALUE))
        );
        welcomePanelLayout.setVerticalGroup(
            welcomePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(welcomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(welcomeTextLabel)
                .addContainerGap(293, Short.MAX_VALUE))
        );

        welcomeCardPanel.add(welcomePanel);
        welcomePanel.setBounds(0, 40, 460, 320);

        wizardCardPanel.add(welcomeCardPanel, "card2");

        nameAndLocationCardPanel.setLayout(null);

        wizardLineCard2.setFont(new java.awt.Font("Verdana", 0, 12));
        wizardLineCard2.setText("______________________________________________________");
        nameAndLocationCardPanel.add(wizardLineCard2);
        wizardLineCard2.setBounds(10, 10, 440, 20);

        nameAndLocationWizardLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        nameAndLocationWizardLabel.setText("Name and Location");
        nameAndLocationCardPanel.add(nameAndLocationWizardLabel);
        nameAndLocationWizardLabel.setBounds(10, 10, 130, 16);

        problemNameLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        problemNameLabel.setText("Problem Name:");

        problemNameTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        problemNameTextField.setText("New Problem");

        problemLocationLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        problemLocationLabel.setText("Problem Location:");

        problemLocationTextField.setEditable(false);
        problemLocationTextField.setFont(new java.awt.Font("Verdana", 0, 12));

        browseButton.setFont(new java.awt.Font("Verdana", 0, 12));
        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout nameAndLocationPanelLayout = new org.jdesktop.layout.GroupLayout(nameAndLocationPanel);
        nameAndLocationPanel.setLayout(nameAndLocationPanelLayout);
        nameAndLocationPanelLayout.setHorizontalGroup(
            nameAndLocationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(nameAndLocationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(nameAndLocationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameAndLocationPanelLayout.createSequentialGroup()
                        .add(problemNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(problemNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE))
                    .add(nameAndLocationPanelLayout.createSequentialGroup()
                        .add(problemLocationLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(problemLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, browseButton))
                .addContainerGap())
        );
        nameAndLocationPanelLayout.setVerticalGroup(
            nameAndLocationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(nameAndLocationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(nameAndLocationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(problemNameLabel)
                    .add(problemNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(nameAndLocationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(problemLocationLabel)
                    .add(problemLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton)
                .addContainerGap(216, Short.MAX_VALUE))
        );

        nameAndLocationCardPanel.add(nameAndLocationPanel);
        nameAndLocationPanel.setBounds(0, 40, 460, 320);

        wizardCardPanel.add(nameAndLocationCardPanel, "card2");

        descriptionCardPanel.setLayout(null);

        wizardLineCard3.setFont(new java.awt.Font("Verdana", 0, 12));
        wizardLineCard3.setText("______________________________________________________");
        descriptionCardPanel.add(wizardLineCard3);
        wizardLineCard3.setBounds(10, 10, 440, 20);

        descriptionWizardLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        descriptionWizardLabel.setText("Description");
        descriptionWizardLabel.setToolTipText("description");
        descriptionCardPanel.add(descriptionWizardLabel);
        descriptionWizardLabel.setBounds(10, 10, 80, 16);

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setFont(new java.awt.Font("Verdana", 0, 12));
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setRows(5);
        descriptionTextArea.setWrapStyleWord(true);
        descroptionScollPane.setViewportView(descriptionTextArea);

        problemDescriptionLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        problemDescriptionLabel.setText("Problem Description:");

        org.jdesktop.layout.GroupLayout descriptionPanelLayout = new org.jdesktop.layout.GroupLayout(descriptionPanel);
        descriptionPanel.setLayout(descriptionPanelLayout);
        descriptionPanelLayout.setHorizontalGroup(
            descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(descroptionScollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(problemDescriptionLabel))
                .addContainerGap())
        );
        descriptionPanelLayout.setVerticalGroup(
            descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(problemDescriptionLabel)
                .add(18, 18, 18)
                .add(descroptionScollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                .addContainerGap())
        );

        descriptionCardPanel.add(descriptionPanel);
        descriptionPanel.setBounds(0, 40, 460, 320);

        wizardCardPanel.add(descriptionCardPanel, "card2");

        subProblemsCardPanel.setLayout(null);

        wizardLineCard5.setFont(new java.awt.Font("Verdana", 0, 12));
        wizardLineCard5.setText("______________________________________________________");
        subProblemsCardPanel.add(wizardLineCard5);
        wizardLineCard5.setBounds(10, 10, 440, 20);

        subProblemsWizardLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        subProblemsWizardLabel.setText("Sub Problems");
        subProblemsCardPanel.add(subProblemsWizardLabel);
        subProblemsWizardLabel.setBounds(10, 10, 130, 16);

        subProblemsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        subProblemsScollablePanel.setLayout(new javax.swing.BoxLayout(subProblemsScollablePanel, javax.swing.BoxLayout.PAGE_AXIS));
        subProblemsScrollPane.setViewportView(subProblemsScollablePanel);

        org.jdesktop.layout.GroupLayout subProblemsPanelLayout = new org.jdesktop.layout.GroupLayout(subProblemsPanel);
        subProblemsPanel.setLayout(subProblemsPanelLayout);
        subProblemsPanelLayout.setHorizontalGroup(
            subProblemsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(subProblemsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(subProblemsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE))
        );
        subProblemsPanelLayout.setVerticalGroup(
            subProblemsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(subProblemsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
        );

        subProblemsCardPanel.add(subProblemsPanel);
        subProblemsPanel.setBounds(0, 40, 461, 290);

        addSubProblemButton.setFont(new java.awt.Font("Verdana", 0, 12));
        addSubProblemButton.setText("Add");
        addSubProblemButton.setToolTipText("Add a sub problem");
        addSubProblemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSubProblemButtonActionPerformed(evt);
            }
        });
        subProblemsCardPanel.add(addSubProblemButton);
        addSubProblemButton.setBounds(285, 330, 70, 29);

        removeSubProblemButton.setFont(new java.awt.Font("Verdana", 0, 12));
        removeSubProblemButton.setText("Remove");
        removeSubProblemButton.setToolTipText("Remove the last sub problem");
        removeSubProblemButton.setEnabled(false);
        removeSubProblemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSubProblemButtonActionPerformed(evt);
            }
        });
        subProblemsCardPanel.add(removeSubProblemButton);
        removeSubProblemButton.setBounds(370, 330, 90, 29);

        wizardCardPanel.add(subProblemsCardPanel, "card6");

        valuesCardPanel.setLayout(null);

        wizardLineCard4.setFont(new java.awt.Font("Verdana", 0, 12));
        wizardLineCard4.setText("______________________________________________________");
        valuesCardPanel.add(wizardLineCard4);
        wizardLineCard4.setBounds(10, 10, 440, 20);

        valuesWizardLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        valuesWizardLabel.setText("Values");
        valuesWizardLabel.setToolTipText("values");
        valuesCardPanel.add(valuesWizardLabel);
        valuesWizardLabel.setBounds(10, 10, 50, 16);

        dataSetTabbedPane.setFont(new java.awt.Font("Verdana", 0, 12));
        valuesCardPanel.add(dataSetTabbedPane);
        dataSetTabbedPane.setBounds(0, 30, 460, 299);

        addDataSetButton.setFont(new java.awt.Font("Verdana", 0, 12));
        addDataSetButton.setText("Add");
        addDataSetButton.setToolTipText("Add a data set");
        addDataSetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDataSetButtonActionPerformed(evt);
            }
        });
        valuesCardPanel.add(addDataSetButton);
        addDataSetButton.setBounds(285, 330, 70, 29);

        removeDataSetButton.setFont(new java.awt.Font("Verdana", 0, 12));
        removeDataSetButton.setText("Remove");
        removeDataSetButton.setToolTipText("Remove the last data set");
        removeDataSetButton.setEnabled(false);
        removeDataSetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDataSetButtonActionPerformed(evt);
            }
        });
        valuesCardPanel.add(removeDataSetButton);
        removeDataSetButton.setBounds(370, 330, 90, 29);

        wizardCardPanel.add(valuesCardPanel, "card2");

        closeWizardButton.setFont(new java.awt.Font("Verdana", 0, 12));
        closeWizardButton.setText("Close");
        closeWizardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeWizardButtonActionPerformed(evt);
            }
        });

        nextWizardButton.setFont(new java.awt.Font("Verdana", 0, 12));
        nextWizardButton.setText("Next >");
        nextWizardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextWizardButtonActionPerformed(evt);
            }
        });

        backWizardButton.setFont(new java.awt.Font("Verdana", 0, 12));
        backWizardButton.setText("< Back");
        backWizardButton.setEnabled(false);
        backWizardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backWizardButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout wizardControlPanelLayout = new org.jdesktop.layout.GroupLayout(wizardControlPanel);
        wizardControlPanel.setLayout(wizardControlPanelLayout);
        wizardControlPanelLayout.setHorizontalGroup(
            wizardControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, wizardControlPanelLayout.createSequentialGroup()
                .addContainerGap(455, Short.MAX_VALUE)
                .add(backWizardButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(nextWizardButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(closeWizardButton)
                .addContainerGap())
        );
        wizardControlPanelLayout.setVerticalGroup(
            wizardControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(wizardControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(wizardControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(closeWizardButton)
                    .add(nextWizardButton)
                    .add(backWizardButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout newProblemWizardDialogLayout = new org.jdesktop.layout.GroupLayout(newProblemWizardDialog.getContentPane());
        newProblemWizardDialog.getContentPane().setLayout(newProblemWizardDialogLayout);
        newProblemWizardDialogLayout.setHorizontalGroup(
            newProblemWizardDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(newProblemWizardDialogLayout.createSequentialGroup()
                .add(stepsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 193, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(wizardCardPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE))
            .add(wizardControlPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        newProblemWizardDialogLayout.setVerticalGroup(
            newProblemWizardDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(newProblemWizardDialogLayout.createSequentialGroup()
                .add(newProblemWizardDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(wizardCardPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                    .add(stepsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 364, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(wizardControlPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        openChooserDialog.setApproveButtonToolTipText("Open selected folder");
        openChooserDialog.setDialogTitle("Browse Problem Location");
        openChooserDialog.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        saveChooserDialog.setApproveButtonToolTipText("Save as selected file");
        saveChooserDialog.setDialogTitle("Save As Problem Location");
        saveChooserDialog.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);

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
            .add(0, 242, Short.MAX_VALUE)
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
                .add(preWorkspaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                .addContainerGap())
        );
        preWorkspacePanelLayout.setVerticalGroup(
            preWorkspacePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(preWorkspacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(preWorkspaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addContainerGap())
        );

        workspaceCardPanel.add(preWorkspacePanel, "card3");

        workspacePanel.setBackground(new java.awt.Color(255, 255, 255));
        workspacePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                workspacePanelMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                workspacePanelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                workspacePanelMouseReleased(evt);
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
        workspaceCardPanel.add(workspacePanel, "card2");

        workspaceSplitPane.setTopComponent(workspaceCardPanel);

        outputScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        outputTextArea.setColumns(20);
        outputTextArea.setEditable(false);
        outputTextArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        outputTextArea.setRows(5);
        outputScrollPane.setViewportView(outputTextArea);

        org.jdesktop.layout.GroupLayout trayPanelLayout = new org.jdesktop.layout.GroupLayout(trayPanel);
        trayPanel.setLayout(trayPanelLayout);
        trayPanelLayout.setHorizontalGroup(
            trayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 176, Short.MAX_VALUE)
            .add(trayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(outputScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE))
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

	private void closeWizardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeWizardButtonActionPerformed
		newProblemWizardDialog.dispose();
		mainFrame.requestFocus ();

		editing = false;
	}//GEN-LAST:event_closeWizardButtonActionPerformed

	private void nextWizardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextWizardButtonActionPerformed
		if (welcomeCardPanel.isVisible ())
		{
			// Move to the next panel in the cards
			nameAndLocationCardPanel.setVisible (true);
			welcomeCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the next card
			welcomeLabel.setFont (FONT_PLAIN_12);
			nameAndLocationLabel.setFont (FONT_BOLD_12);

			// Set the focus properly for the new card
			problemNameTextField.requestFocus();
			problemNameTextField.selectAll();

			backWizardButton.setEnabled (true);
		}
		else if (nameAndLocationCardPanel.isVisible ())
		{
			boolean continueAllowed = true;
			String fileName = problemNameTextField.getText ();
			// Assuming the user didn't specify our file type, append the type
			if (!fileName.endsWith (".marla"))
			{
				fileName += ".marla";
			}
			// Before advancing to the next card, ensure a name is given for the new problem
			if (problemNameTextField.getText ().replaceAll (" ", "").equals (""))
			{
				problemNameTextField.setText ("New Problem");
			}
			File file = new File (problemLocationTextField.getText (), fileName);
			if (!file.exists ())
			{
				// Ensure the problem name given is a valid filename
				try
				{
					FileWriter write = new FileWriter (file);
					write.close();
					file.delete();
				}
				catch (IOException ex)
				{
					JOptionPane.showMessageDialog(newProblemWizardDialog, "The problem name you have given contains characters that are\n"
							+ "not legal in a filename. Please rename your file and avoid\n"
							+ "using special characters.",
							"Invalid Filename",
							JOptionPane.WARNING_MESSAGE);

					continueAllowed = false;
				}
			}
			// Ensure the problem name given does not match an already existing file
			if (continueAllowed && file.exists () && !newProblemOverwrite && !editing)
			{
				int response = JOptionPane.showConfirmDialog(newProblemWizardDialog, "The given problem name already exists as a file\n"
						+ "at the specified location. If you would not like to overwrite the\n"
						+ "existing file, change the problem name or the problem location.\n"
						+ "Would you like to overwrite the existing file?",
						"Overwrite Existing File",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.YES_OPTION)
				{
					newProblemOverwrite = true;
				}
				else
				{
					continueAllowed = false;
				}
			}

			if (continueAllowed)
			{
				// Move to the next panel in the cards
				descriptionCardPanel.setVisible (true);
				nameAndLocationCardPanel.setVisible (false);

				// Shift the boldness in the Steps panel to the next card
				nameAndLocationLabel.setFont (FONT_PLAIN_12);
				descriptionLabel.setFont (FONT_BOLD_12);

				// Set the focus properly for the new card
				descriptionTextArea.requestFocus();
			}
			else
			{
				// Since continuation wasn't allowed, the user needs to correct the problem name
				problemNameTextField.requestFocus();
				problemNameTextField.selectAll();
			}
		}
		else if (descriptionCardPanel.isVisible ())
		{
			// Move to the next panel in the cards
			subProblemsCardPanel.setVisible (true);
			descriptionCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the next card
			descriptionLabel.setFont (FONT_PLAIN_12);
			subProblemsLabel.setFont (FONT_BOLD_12);

			try
			{
				((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (subProblemPanels.size() - 1)).getComponent (1)).getComponent (0)).getComponent (0)).requestFocus ();
				subProblemsScollablePanel.scrollRectToVisible(new Rectangle(0, subProblemsScollablePanel.getHeight(), 1, 1));
			}
			catch (ArrayIndexOutOfBoundsException ex) {}
		}
		else if (subProblemsCardPanel.isVisible ())
		{
			// Move to the next panel in the cards
			valuesCardPanel.setVisible (true);
			subProblemsCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the next card
			subProblemsLabel.setFont (FONT_PLAIN_12);
			valuesLabel.setFont (FONT_BOLD_12);

			nextWizardButton.setText ("Finish");
		}
		else
		{
			finishNewProblemWizard ();
		}
	}//GEN-LAST:event_nextWizardButtonActionPerformed

	private void backWizardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backWizardButtonActionPerformed
		if (nameAndLocationCardPanel.isVisible ())
		{
			// Move to the previous panel in the cards
			welcomeCardPanel.setVisible (true);
			nameAndLocationCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the previous card
			welcomeLabel.setFont (FONT_BOLD_12);
			nameAndLocationLabel.setFont (FONT_PLAIN_12);

			backWizardButton.setEnabled (false);
		}
		else if (descriptionCardPanel.isVisible ())
		{
			// Move to the previous panel in the cards
			nameAndLocationCardPanel.setVisible (true);
			descriptionCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the previous card
			nameAndLocationLabel.setFont (FONT_BOLD_12);
			descriptionLabel.setFont (FONT_PLAIN_12);

			// Set the focus properly for the new card
			problemNameTextField.requestFocus();
			problemNameTextField.selectAll();
		}
		else if (subProblemsCardPanel.isVisible ())
		{
			// Move to the previous panel in the cards
			descriptionCardPanel.setVisible (true);
			subProblemsCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the previous card
			descriptionLabel.setFont (FONT_BOLD_12);
			subProblemsLabel.setFont (FONT_PLAIN_12);

			// Set the focus properly for the new card
			descriptionTextArea.requestFocus();
		}
		else
		{
			// Move to the previous panel in the cards
			subProblemsCardPanel.setVisible (true);
			valuesCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the previous card
			subProblemsLabel.setFont (FONT_BOLD_12);
			valuesLabel.setFont (FONT_PLAIN_12);

			nextWizardButton.setText ("Next >");

			try
			{
				((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (subProblemPanels.size() - 1)).getComponent (1)).getComponent (0)).getComponent (0)).requestFocus ();
				subProblemsScollablePanel.scrollRectToVisible(new Rectangle(0, subProblemsScollablePanel.getHeight(), 1, 1));
			}
			catch (ArrayIndexOutOfBoundsException ex) {}
		}
	}//GEN-LAST:event_backWizardButtonActionPerformed

	private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
		// Construct the folder-based open chooser dialog
		openChooserDialog.setFileFilter(defaultFilter);
		openChooserDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		openChooserDialog.setCurrentDirectory (new File (domain.lastGoodDir));
		openChooserDialog.setSelectedFile(new File (""));
		// Display the chooser and retrieve the selected folder
		int response = openChooserDialog.showOpenDialog (newProblemWizardDialog);
		if (response == JFileChooser.APPROVE_OPTION)
		{
			// If the user selected a folder that exists, point the problem's location to the newly selected location
			if (openChooserDialog.getSelectedFile().exists ())
			{
				domain.lastGoodDir = openChooserDialog.getSelectedFile().toString ();
				problemLocationTextField.setText (domain.lastGoodDir);
			}
		}
	}//GEN-LAST:event_browseButtonActionPerformed

	private void removeSubProblemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSubProblemButtonActionPerformed
		// Remove the JPanel from the list of sub problems and from the New Problem Wizard
		JPanel panel = subProblemPanels.remove (subProblemPanels.size () - 1);
		subProblemsScollablePanel.remove (panel);

		if (subProblemPanels.isEmpty ())
		{
			removeSubProblemButton.setEnabled (false);
		}
		else if (subProblemPanels.size () == 25)
		{
			addSubProblemButton.setEnabled (true);
		}

		subProblemsScollablePanel.updateUI ();

		try
		{
			((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (subProblemPanels.size() - 1)).getComponent (1)).getComponent (0)).getComponent (0)).requestFocus ();
		}
		catch (ArrayIndexOutOfBoundsException ex) {}
	}//GEN-LAST:event_removeSubProblemButtonActionPerformed

	private void addSubProblemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSubProblemButtonActionPerformed
		// Create objects toward the new JPanel for the sub problem
		JPanel subProblemPanel = new JPanel ();
		JLabel label = new JLabel (ALPHABET[subProblemPanels.size ()]);
		label.setFont (new Font ("Verdana", Font.BOLD, 11));
		JTextArea textArea = new JTextArea ();
		JScrollPane scrollPane = new JScrollPane ();
		textArea.setLineWrap (true);
		textArea.setWrapStyleWord (true);
        scrollPane.setViewportView (textArea);

		// Add items to the new JPanel
		subProblemPanel.setLayout (new BorderLayout());
		subProblemPanel.add (label, BorderLayout.NORTH);
		subProblemPanel.add (scrollPane, BorderLayout.CENTER);
		subProblemPanel.setPreferredSize (new Dimension(410, 100));
		subProblemPanel.setMinimumSize (new Dimension(410, 100));
		subProblemPanel.setMaximumSize (new Dimension(410, 100));

		// Add the JPanel to the list of sub problem JPanels
		subProblemPanels.add (subProblemPanel);
		if (subProblemPanels.size () == 1)
		{
			removeSubProblemButton.setEnabled (true);
		}
		else if (subProblemPanels.size () == 26)
		{
			addSubProblemButton.setEnabled (false);
		}

		// Add the JPanel to the New Problem Wizard
		subProblemsScollablePanel.add (subProblemPanel);
		subProblemsScollablePanel.updateUI ();
		subProblemsScollablePanel.scrollRectToVisible(new Rectangle(0, subProblemsScollablePanel.getHeight() + 100, 1, 1));

		textArea.requestFocus ();
	}//GEN-LAST:event_addSubProblemButtonActionPerformed

	private void addDataSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDataSetButtonActionPerformed
		JPanel panel =  createValuesTabbedPanel ();
		int index = dataSetTabbedPane.getTabCount () + 1;
		while (dataSetNameExists(-1, "Data Set " + (index)))
		{
			++index;
		}
		dataSetTabbedPane.add ("Data Set " + index, panel);
		dataSetTabbedPane.setSelectedIndex (dataSetTabbedPane.getTabCount () - 1);
		int columns = 3;
		int rows = 5;
		((JSpinner) panel.getComponent (2)).setValue (columns);
		((JSpinner) panel.getComponent (4)).setValue (rows);

		// Add minimum columns to the table model
		JTable table = ((JTable) ((JViewport) ((JScrollPane) panel.getComponent (0)).getComponent (0)).getComponent (0));
		ExtendedTableModel newTableModel = new ExtendedTableModel ();
		DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel ();
		for (int i = 0; i < columns; ++i)
		{
			index = i + 1;
			while (columnNameExists(newTableModel, i, "Column " + (index)))
			{
				++index;
			}
			newColumnModel.addColumn (new TableColumn ());
			newTableModel.addColumn("Column " + index);
		}
		table.setColumnModel (newColumnModel);
		// Add minimum rows to the table model
		for (int i = 0; i < rows; ++i)
		{
			newTableModel.addRow (new Object[columns]);
		}
		table.setModel (newTableModel);
		table.updateUI();
		table.getTableHeader().resizeAndRepaint ();

		if (dataSetTabbedPane.getTabCount() == 2)
		{
			removeDataSetButton.setEnabled (true);
		}
	}//GEN-LAST:event_addDataSetButtonActionPerformed

	private void removeDataSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDataSetButtonActionPerformed
		dataSetTabbedPane.remove (dataSetTabbedPane.getTabCount() - 1);
		if (dataSetTabbedPane.getTabCount() == 1)
		{
			removeDataSetButton.setEnabled (false);
		}
	}//GEN-LAST:event_removeDataSetButtonActionPerformed

	private void workspacePanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMouseDragged
		workspacePanelMouseClicked (evt);

		if (dataSetDragging != null)
		{
			dataSetDragging.setLocation(evt.getX() - xDragOffset, evt.getY() - yDragOffset);
			domain.problem.markChanged();
		}
		else if (operationDragging != null)
		{
			operationDragging.setLocation(evt.getX() - xDragOffset, evt.getY() - yDragOffset);
			domain.problem.markChanged();
		}
	}//GEN-LAST:event_workspacePanelMouseDragged

	private void workspacePanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMousePressed
		Component component = workspacePanel.getComponentAt (evt.getPoint ());
		if (component != null)
		{
			if (component instanceof Operation)
			{
				operationDragging = (Operation) component;
				xDragOffset = evt.getX() - operationDragging.getX();
                yDragOffset = evt.getY() - operationDragging.getY();
			}
			else if(component instanceof DataSet)
			{
				dataSetDragging = (DataSet) component;
				xDragOffset = evt.getX() - dataSetDragging.getX();
                yDragOffset = evt.getY() - dataSetDragging.getY();
			}
		}
	}//GEN-LAST:event_workspacePanelMousePressed

	private void workspacePanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMouseReleased
		dataSetDragging = null;
		operationDragging = null;
	}//GEN-LAST:event_workspacePanelMouseReleased

	private void workspacePanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMouseMoved
		if (workspacePanel.getComponentAt(evt.getPoint ()) != null &&
				workspacePanel.getComponentAt(evt.getPoint ()) != workspacePanel)
		{
			if (workspacePanel.getComponentAt(evt.getPoint ()) != selectedDataSet &&
					workspacePanel.getComponentAt(evt.getPoint ()) != selectedOperation)
			{
				hoveredComponent = (JComponent) workspacePanel.getComponentAt (evt.getPoint ());
				hoveredComponent.setSize(hoveredComponent.getPreferredSize());

				hoveredComponent.setBorder (BorderFactory.createLineBorder(Color.BLACK));
				hoveredComponent.setSize(hoveredComponent.getPreferredSize());
			}
		}
		else if (workspacePanel.getComponentAt (evt.getPoint ()) != null &&
				 workspacePanel.getComponentAt(evt.getPoint ()) != workspacePanel &&
				 workspacePanel.getComponentAt (evt.getPoint ()) != hoveredComponent)
		{
			if (workspacePanel.getComponentAt(evt.getPoint ()) != selectedDataSet &&
					workspacePanel.getComponentAt(evt.getPoint ()) != selectedOperation)
			{
				hoveredComponent.setBorder (BorderFactory.createEmptyBorder());
				hoveredComponent.setSize(hoveredComponent.getPreferredSize());

				hoveredComponent = (JComponent) workspacePanel.getComponentAt (evt.getPoint ());
				hoveredComponent.setBorder (BorderFactory.createLineBorder(Color.BLACK));
				hoveredComponent.setSize(hoveredComponent.getPreferredSize());
			}
		}
		else if (hoveredComponent != null)
		{
			hoveredComponent.setBorder (BorderFactory.createEmptyBorder());
			hoveredComponent.setSize(hoveredComponent.getPreferredSize());
			hoveredComponent = null;
		}
	}//GEN-LAST:event_workspacePanelMouseMoved

	private void workspacePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workspacePanelMouseClicked
		if (hoveredComponent != null)
		{
			if (hoveredComponent instanceof Operation)
			{
				if (selectedOperation != null)
				{
					selectedOperation.setBorder (BorderFactory.createEmptyBorder());
					selectedOperation.setSize(selectedOperation.getPreferredSize());
				}
				selectedOperation = (Operation) hoveredComponent;
				domain.currentOperation = selectedOperation;
				selectedOperation.setBorder (BorderFactory.createLineBorder(Color.BLUE));
				selectedOperation.setSize(selectedOperation.getPreferredSize());
				hoveredComponent = null;
			}
			else if (hoveredComponent instanceof DataSet)
			{
				if (selectedDataSet != null)
				{
					selectedDataSet.setBorder (BorderFactory.createEmptyBorder());
					selectedDataSet.setSize(selectedDataSet.getPreferredSize());
				}
				selectedDataSet = (DataSet) hoveredComponent;
				domain.currentDataSet = selectedDataSet;
				selectedDataSet.setBorder (BorderFactory.createLineBorder(Color.RED));
				selectedDataSet.setSize(selectedDataSet.getPreferredSize());
				hoveredComponent = null;
			}
		}
	}//GEN-LAST:event_workspacePanelMouseClicked

	/**
	 * Check if the given data set name exists (ignoring the current index, which is itself).
	 *
	 * @param index The current index to be ignored.
	 * @param name The name to check for.
	 * @return True if the data set name already exists, false otherwise.
	 */
	private boolean dataSetNameExists(int curIndex, String name)
	{
		for (int i = 0; i < dataSetTabbedPane.getTabCount(); ++i)
		{
			if (i == curIndex)
			{
				continue;
			}
			if (dataSetTabbedPane.getTitleAt (i).equals (name))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the given column name exists (ignoring the current index, which is itself).
	 *
	 * @param index The current index to be ignored.
	 * @param name The name to check for.
	 * @return True if the column name already exists, false otherwise.
	 */
	private boolean columnNameExists(ExtendedTableModel model, int curIndex, String name)
	{
		for (int i = 0; i < model.getColumnCount (); ++i)
		{
			if (i == curIndex)
			{
				continue;
			}
			if (model.getColumnName (i).equals (name))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Create a panel for display in the tabbed display for data set values.
	 *
	 * @return The panel created to be stored in the values tabbed panel.
	 */
	private JPanel createValuesTabbedPanel()
	{
		final ViewPanel viewPanel = this;
		
		final JPanel valuesPanel = new JPanel ();
		final JScrollPane scrollPane = new JScrollPane ();
		ExtendedTableModel model = new ExtendedTableModel ();
		final JTable table = new JTable (model);
		table.getTableHeader ().setFont (FONT_PLAIN_12);
		table.getTableHeader ().addMouseListener(new MouseAdapter ()
		{
			@Override
			public void mouseReleased(MouseEvent evt)
			{
				int index = table.getTableHeader().columnAtPoint(evt.getPoint());
				Object name = JOptionPane.showInputDialog(newProblemWizardDialog,
						"Give the column a new name:",
						"Column Name",
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						table.getColumnModel ().getColumn(index).getHeaderValue());
				if (name != null)
				{
					if (!name.toString ().equals (((ExtendedTableModel) table.getModel ()).getColumnName(index)))
					{
						if (!columnNameExists ((ExtendedTableModel) table.getModel (), index, name.toString ()))
						{
							((ExtendedTableModel) table.getModel ()).setColumn (name.toString (), index);
							table.getColumnModel ().getColumn (index).setHeaderValue (name);
							table.getTableHeader().resizeAndRepaint ();
						}
						else
						{
							JOptionPane.showMessageDialog(newProblemWizardDialog, "A column with that name already exists.", "Duplicate Column", JOptionPane.WARNING_MESSAGE);
						}
					}
				}
			}
		});
		scrollPane.setViewportView(table);
		final JLabel columnsLabel = new JLabel ("Columns:");
		columnsLabel.setFont(new java.awt.Font("Verdana", 0, 12));
		final JSpinner columnsSpinner = new JSpinner ();
		columnsSpinner.addChangeListener(new ChangeListener()
		{
			@Override
            public void stateChanged(ChangeEvent evt)
			{
				if (!openingWizard)
				{
					int value = Integer.parseInt(columnsSpinner.getValue().toString());
					// Don't let column count be below 1
					if (value < 1)
					{
						columnsSpinner.setValue (1);
						value = 1;
					}
					ExtendedTableModel model = (ExtendedTableModel) table.getModel ();
					// If columns were removed, loop and delete from the end
					while (value < model.getColumnCount())
					{
						model.removeColumn(table.getColumnCount() - 1);
						table.getColumnModel().removeColumn(table.getColumnModel().getColumn(table.getColumnCount() - 1));
					}
					// If columns were added, loop and add to the end
					while (value > model.getColumnCount())
					{
						int index = model.getColumnCount() + 1;
						while (columnNameExists(model, -1, "Column " + (index)))
						{
							++index;
						}
						table.getColumnModel().addColumn(new TableColumn());
						model.addColumn("Column " + index);
						table.getColumnModel().getColumn(model.getColumnCount() - 1).setHeaderValue("Column " + index);
						for (int i = 0; i < model.getRowCount(); ++i)
						{
							model.setValueAt (0, i, model.getColumnCount () - 1);
						}
					}

					table.updateUI();
					table.getTableHeader().resizeAndRepaint ();
				}
            }
        });
		final JLabel rowsLabel = new JLabel ("Rows:");
		rowsLabel.setFont(new java.awt.Font("Verdana", 0, 12));
		final JSpinner rowsSpinner = new JSpinner ();
		rowsSpinner.addChangeListener(new ChangeListener()
		{
			@Override
            public void stateChanged(ChangeEvent evt)
			{
				if (!openingWizard)
				{
					int value = Integer.parseInt(rowsSpinner.getValue().toString());
					// Don't let row count be below 1
					if (value < 1)
					{
						rowsSpinner.setValue (1);
						value = 1;
					}
					ExtendedTableModel model = (ExtendedTableModel) table.getModel ();
					// If rows were removed, loop and delete from the end
					while (value < model.getRowCount())
					{
						model.removeRow(table.getRowCount() - 1);
					}
					// If rows were added, loop and add to the end
					while (value > model.getRowCount())
					{
						model.addRow(new Object[table.getColumnCount()]);
					}

					table.updateUI();
				}
            }
        });
		final JButton button = new JButton ("Import from CSV");
		button.setFont(new java.awt.Font("Verdana", 0, 12));
        button.addActionListener(new ActionListener()
		{
			@Override
            public void actionPerformed(ActionEvent evt)
			{
                // Construct the folder-based open chooser dialog
				openChooserDialog.setFileFilter(csvFilter);
				openChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
				openChooserDialog.setCurrentDirectory(new File(domain.lastGoodCsvFile));
				if (new File(domain.lastGoodCsvFile).isFile()) {
					openChooserDialog.setSelectedFile(new File(domain.lastGoodCsvFile));
				} else {
					openChooserDialog.setSelectedFile(new File(""));
				}
				// Display the chooser and retrieve the selected folder
				int response = openChooserDialog.showOpenDialog(newProblemWizardDialog);
				if (response == JFileChooser.APPROVE_OPTION) {
					try {
						// If the user selected a file that exists, point the problem's location to the newly selected location
						if (openChooserDialog.getSelectedFile().exists()) {
							domain.lastGoodCsvFile = openChooserDialog.getSelectedFile().toString();
							try {
								DataSet importedDataSet = DataSet.importFile(domain.lastGoodCsvFile);

								// Setup the values table to be ready for the import
								columnsSpinner.setValue(importedDataSet.getColumnCount());
								rowsSpinner.setValue(importedDataSet.getColumnLength());

								ExtendedTableModel newModel = new ExtendedTableModel();
								// Set the column headers
								String[] columnNames = importedDataSet.getColumnNames();
								for (int i = 0; i < columnNames.length; ++i) {
									newModel.addColumn(columnNames[i]);
									table.getColumnModel().getColumn(i).setHeaderValue(columnNames[i]);
								}
								// Initialize number of rows
								for (int i = 0; i < importedDataSet.getColumnLength(); ++i) {
									ArrayList<Integer> row = new ArrayList<Integer> ();
									for (int j = 0; j < importedDataSet.getColumnCount(); ++j) {
										row.add(0);
									}
									newModel.addRow(row.toArray());
								}
								// Load imported data into the values table
								for (int i = 0; i < importedDataSet.getColumnCount(); ++i) {
									DataColumn column = importedDataSet.getColumn(i);
									for (int j = 0; j < column.size(); ++j) {
										newModel.setValueAt(column.get(j), j, i);
									}
								}
								table.setModel(newModel);
								table.updateUI();
								table.getTableHeader().resizeAndRepaint ();
							} catch (CalcException ex) {
								JOptionPane.showMessageDialog(viewPanel, "The requested R package either cannot be located or is not installed.", "Missing Package", JOptionPane.WARNING_MESSAGE);
							} catch (RProcessorParseException ex) {
								JOptionPane.showMessageDialog(viewPanel, "Loading of file failed, it may be invalid", "Load failed", JOptionPane.WARNING_MESSAGE);
							} catch (RProcessorException ex) {
								JOptionPane.showMessageDialog(viewPanel, "Loading of file failed, R could not be loaded", "Load failed", JOptionPane.WARNING_MESSAGE);
							}
						}
					} catch(FileNotFoundException e) { }
				}
            }
        });

		GroupLayout valuesPanelLayout = new GroupLayout(valuesPanel);
        valuesPanel.setLayout(valuesPanelLayout);
        valuesPanelLayout.setHorizontalGroup(
            valuesPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(valuesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(valuesPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(scrollPane, GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                    .add(valuesPanelLayout.createSequentialGroup()
                        .add(columnsLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(columnsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(rowsLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(rowsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED, 108, Short.MAX_VALUE)
                        .add(button)))
                .addContainerGap())
        );
        valuesPanelLayout.setVerticalGroup(
            valuesPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(valuesPanelLayout.createSequentialGroup()
                .add(scrollPane, GroupLayout.PREFERRED_SIZE, 238, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(valuesPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(button)
                    .add(valuesPanelLayout.createParallelGroup(GroupLayout.LEADING, false)
                        .add(columnsLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(columnsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .add(valuesPanelLayout.createParallelGroup(GroupLayout.LEADING, false)
                        .add(rowsLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(rowsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

		return valuesPanel;
	}

	/**
	 * Add this operation to the workspace under the current data set.
	 *
	 * @param operation The operation to add to the current data set and to the workspace.
	 */
	private void addToWorkspace(Operation operation)
	{
		domain.problem.markChanged();

		int x = domain.currentDataSet.getX ();
		int y = domain.currentDataSet.getY () + 20;
		if (domain.currentDataSet.getOperationCount() > 0)
		{
			y += 20;
		}
		final OperationXML newOperation;
		try
		{
			newOperation = OperationXML.createOperation(operation.getName());
			newOperation.setBounds (x, y, newOperation.getPreferredSize().width, newOperation.getPreferredSize().height);
			try
			{
				if (newOperation.isInfoRequired())
				{
					// Create the dialog which will be launched to ask about requirements
					final ArrayList<Object[]> prompt = newOperation.getRequiredInfoPrompt();
					final JDialog dialog = new JDialog ();
					JPanel panel = new JPanel ();
					panel.setLayout (new GridLayout (prompt.size () + 1, 2));

					dialog.setTitle ("Information Required");
					dialog.setModal (true);
					dialog.add (panel);

					// This array will contain references to objects that will hold the values
					final ArrayList<Object> valueComponents = new ArrayList<Object> ();

					// Fill dialog with components
					for (int i = 0; i < prompt.size(); ++i)
					{
						Object[] components = prompt.get (i);
						if (components[1] == Domain.PromptType.TEXT)
						{
							JPanel tempPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
							JLabel label = new JLabel (components[0].toString ());
							JTextField textField = new JTextField ();
							textField.setPreferredSize(new Dimension (150, textField.getPreferredSize().height));
							tempPanel.add (label);
							tempPanel.add (textField);
							valueComponents.add (textField);
							panel.add (tempPanel);
						}
						else if(components[1] == Domain.PromptType.CHECKBOX)
						{
							JPanel tempPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
							JCheckBox checkBox = new JCheckBox (components[0].toString ());
							JLabel label = new JLabel ("");
							tempPanel.add (checkBox);
							tempPanel.add (label);
							valueComponents.add (checkBox);
							panel.add (tempPanel);
						}
						else if(components[1] == Domain.PromptType.COMBO)
						{
							JPanel tempPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
							JLabel label = new JLabel (components[0].toString ());
							DefaultComboBoxModel model = new DefaultComboBoxModel ((Object[]) components[2]);
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
							ArrayList<Object> values = new ArrayList<Object> ();
							boolean pass = true;
							for (int i = 0; i < prompt.size (); ++i)
							{
								if (prompt.get (i)[1] == Domain.PromptType.TEXT)
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
								else if(prompt.get (i)[1] == Domain.PromptType.CHECKBOX)
								{
									values.add (Boolean.valueOf (((JCheckBox) valueComponents.get (i)).isSelected ()));
								}
								else if(prompt.get (i)[1] == Domain.PromptType.COMBO)
								{
									values.add (((JComboBox) valueComponents.get (i)).getSelectedItem());
								}
							}

							if (pass)
							{
								newOperation.setRequiredInfo(values);
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
				domain.currentDataSet.addOperationToEnd (newOperation);
			}
			catch (CalcException ex)
			{
				JOptionPane.showMessageDialog(this, "The requested R package either cannot be located or is not installed.", "Missing Package", JOptionPane.WARNING_MESSAGE);
			}
			workspacePanel.add (newOperation);
			workspacePanel.updateUI();
		}
		catch(OperationXMLException ex)
		{
			Logger.getLogger(ViewPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * The new problem wizard is complete, so create the new problem and close
	 * the New Problem Wizard.
	 */
	private void finishNewProblemWizard()
	{
		// Close the current or old problem, if one is open
		closeProblem ();
		
		// Use values from the New Problem Wizard to construct a new problem
		if (!editing)
		{
			domain.problem = new Problem (descriptionTextArea.getText ());
		}

		domain.problem.markChanged();
		// Construct sub problems from the New Problem Wizard
		for (int i = 0; i < subProblemPanels.size (); ++i)
		{
			if (i < domain.problem.getSubProblemCount())
			{
				domain.problem.getSubProblem(i).setStatement(((JTextArea) ((JViewport) ((JScrollPane) subProblemPanels.get (i).getComponent (1)).getComponent(0)).getComponent(0)).getText ());
			}
			else
			{
				domain.problem.addSubProblem(ALPHABET[i], ((JTextArea) ((JViewport) ((JScrollPane) subProblemPanels.get (i).getComponent (1)).getComponent(0)).getComponent(0)).getText ());
			}
		}
		
		String fileName = problemNameTextField.getText ();
		if (!fileName.endsWith (".marla"))
		{
			fileName += ".marla";
		}
		domain.problem.setFileName(new File (problemLocationTextField.getText (), fileName).toString());

		for (int i = 0; i < dataSetTabbedPane.getTabCount(); ++i)
		{
			DataSet dataSet = new DataSet (dataSetTabbedPane.getTitleAt (i));
			// The data set should already exist in the workspace
			if (i < domain.problem.getDataCount())
			{
				dataSet = domain.problem.getData (i);
				if (!dataSetTabbedPane.getTitleAt(i).equals (dataSet.getName ()))
				{
					dataSet.setName(dataSetTabbedPane.getTitleAt (i));
				}
			}
			// This is a new data set, so add it to the workspace and to the problem
			else
			{
				domain.problem.addData (dataSet);
				int x = 200;
				int y = 20;
				if (i > 0)
				{
					x = domain.problem.getData (i - 1).getX () + 150;
				}
				dataSet.setBounds (x, y, dataSet.getPreferredSize().width, dataSet.getPreferredSize().height);
			}
			// Add columns from the New Problem Wizard
			ExtendedTableModel tableModel = (ExtendedTableModel) ((JTable) ((JViewport) ((JScrollPane) ((JPanel) dataSetTabbedPane.getComponent (i)).getComponent (0)).getComponent (0)).getComponent (0)).getModel ();
			for (int j = 0; j < tableModel.getColumnCount(); ++j)
			{
				try
				{
					if (j < dataSet.getColumnCount ())
					{
						DataColumn column = dataSet.getColumn (j);
						if (!dataSet.getColumn (j).getName ().equals (tableModel.getColumnName (j)))
						{
							column.setName (tableModel.getColumnName (j));
						}
						for (int k = 0; k < tableModel.getRowCount(); ++k)
						{
							column.set (k, Double.parseDouble (tableModel.getValueAt (k, j).toString ()));
						}
						for (int k = tableModel.getRowCount (); k < dataSet.getColumnLength (); ++k)
						{
							column.remove (k);
						}
					}
					else
					{
						DataColumn column = dataSet.addColumn (tableModel.getColumnName(j));

						// Add all rows within this column to the data set
						for (int k = 0; k < tableModel.getRowCount (); ++k)
						{
							column.add (Double.parseDouble (tableModel.getValueAt (k, j).toString ()));
						}
					}
				}
				catch (CalcException ex)
				{
					JOptionPane.showMessageDialog(this, "The requested R package either cannot be located or is not installed.", "Missing Package", JOptionPane.WARNING_MESSAGE);
				}
			}
		}
		
		// If a data set was removed, remove it from the display along with all its operations
		if (editing && dataSetTabbedPane.getTabCount () < getShownDataSetCount ())
		{
			int numShown = getShownDataSetCount ();
			for (int i = 0; i < numShown; ++i)
			{
				DataSet dataSet = getDisplayedDataSet (i);
				if (!tabNameExists (dataSet.getName ()))
				{
					// Since the data set is no longer in the problem, remove it from the workspace
					for (int j = 0; j < dataSet.getOperationCount(); ++j)
					{
						workspacePanel.remove (dataSet.getOperation (j));
					}
					domain.problem.removeData (dataSet);
					workspacePanel.remove (dataSet);
					workspacePanel.updateUI ();
				}
			}
		}

		// Open data stored in the problem currently
		openProblem ();

		// Save the problem immedietly
		domain.save();

		closeWizardButtonActionPerformed(null);
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
				if (!editing)
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
					catch (FileException ex)
					{
						return false;
					}
					catch (IOException ex)
					{
						return false;
					}
				}
				else if(response == -1 || response == JOptionPane.CANCEL_OPTION)
				{
					return false;
				}
			}

			if (!editing)
			{
				workspacePanel.removeAll();

				emptyPalettePanel.setVisible (true);
				componentsPanel.setVisible (false);
				preWorkspacePanel.setVisible (true);
				workspacePanel.setVisible (false);

				domain.problem = null;
				domain.currentDataSet = null;
				domain.currentOperation = null;

				mainFrame.setTitle (mainFrame.getDefaultTitle ());
			}
		}

		return true;
	}

	/**
	 * Retrieves the ith data set found in the workspace panel.
	 *
	 * @param i The data set index to return.
	 * @return The data set, if it exists.
	 */
	private DataSet getDisplayedDataSet(int i)
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
	private int getShownDataSetCount()
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
	 * Checks if a tab of the given name exists.
	 *
	 * @param name Tab name to look for.
	 * @return True if the tab name was found, false otherwise.
	 */
	private boolean tabNameExists(String name)
	{
		for (int i = 0; i < dataSetTabbedPane.getTabCount (); ++i)
		{
			if (dataSetTabbedPane.getTitleAt (i).equals (name))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Open the problem currently stored in the problem variable.
	 */
	protected void openProblem()
	{
		if (domain.problem != null)
		{
			if (domain.currentDataSet == null)
			{
				domain.currentDataSet = domain.problem.getData(0);
			}

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
	 * Sets the default values for components in the New Problem Wizard.
	 */
	private void setNewProblemWizardDefaultValues()
	{
		if (!editing)
		{
			// Set problem defaults for name and location
			problemNameTextField.setText ("New Problem");
			problemLocationTextField.setText (domain.lastGoodDir);
			descriptionTextArea.setText ("");

			// By default, new problems have three columns and five rows
			dataSetTabbedPane.add ("Data Set 1", createValuesTabbedPanel ());
			int columns = 3;
			int rows = 5;
			((JSpinner) ((JPanel) dataSetTabbedPane.getComponent (0)).getComponent (2)).setValue (columns);
			((JSpinner) ((JPanel) dataSetTabbedPane.getComponent (0)).getComponent (4)).setValue (rows);

			// Add minimum columns to the table model
			JTable table = ((JTable) ((JViewport) ((JScrollPane) ((JPanel) dataSetTabbedPane.getComponent (0)).getComponent (0)).getComponent (0)).getComponent (0));
			ExtendedTableModel newTableModel = new ExtendedTableModel ();
			DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel ();
			for (int i = 0; i < columns; ++i)
			{
				int index = i + 1;
				while (columnNameExists(newTableModel, i, "Column " + (index)))
				{
					++index;
				}
				newColumnModel.addColumn (new TableColumn ());
				newTableModel.addColumn("Column " + index);
			}
			table.setColumnModel (newColumnModel);
			// Add minimum rows to the table model
			for (int i = 0; i < rows; ++i)
			{
				newTableModel.addRow (new Object[columns]);
			}
			table.setModel (newTableModel);
			table.updateUI();
			table.getTableHeader().resizeAndRepaint ();
		}
		else
		{
			// Set problem defaults for name and location
			problemNameTextField.setText (domain.problem.getFileName().substring (domain.problem.getFileName ().lastIndexOf (System.getProperty ("file.separator")) + 1, domain.problem.getFileName ().lastIndexOf (".")));
			problemLocationTextField.setText (domain.problem.getFileName().substring (0, domain.problem.getFileName ().lastIndexOf (System.getProperty ("file.separator"))));
			descriptionTextArea.setText (domain.problem.getStatement ());

			// Add sub problems to the panel
			for (int i = 0; i < domain.problem.getSubProblemCount(); ++i)
			{
				// Create objects toward the new JPanel for the sub problem
				JPanel subProblemPanel = new JPanel ();
				JLabel label = new JLabel (ALPHABET[i]);
				label.setFont (new Font ("Verdana", Font.BOLD, 11));
				JTextArea textArea = new JTextArea (domain.problem.getSubProblem (i).getStatement ());
				JScrollPane scrollPane = new JScrollPane ();
				textArea.setLineWrap (true);
				textArea.setWrapStyleWord (true);
				scrollPane.setViewportView (textArea);

				// Add items to the new JPanel
				subProblemPanel.setLayout (new BorderLayout());
				subProblemPanel.add (label, BorderLayout.NORTH);
				subProblemPanel.add (scrollPane, BorderLayout.CENTER);
				subProblemPanel.setPreferredSize (new Dimension(410, 100));
				subProblemPanel.setMinimumSize (new Dimension(410, 100));
				subProblemPanel.setMaximumSize (new Dimension(410, 100));

				// Add the JPanel to the list of sub problem JPanels
				subProblemPanels.add (subProblemPanel);

				// Add the JPanel to the New Problem Wizard
				subProblemsScollablePanel.add (subProblemPanel);
			}
			if (subProblemPanels.size () > 0)
			{
				removeSubProblemButton.setEnabled (true);
			}
			else
			{
				removeSubProblemButton.setEnabled (false);
			}
			if (subProblemPanels.size () < 26)
			{
				addSubProblemButton.setEnabled (true);
			}
			else
			{
				addSubProblemButton.setEnabled (false);
			}
			subProblemsScollablePanel.updateUI ();
			subProblemsScollablePanel.scrollRectToVisible(new Rectangle(0, subProblemsScollablePanel.getHeight(), 1, 1));

			for (int i = 0; i < domain.problem.getDataCount(); ++i)
			{
				DataSet dataSet = domain.problem.getData (i);
				JPanel panel = createValuesTabbedPanel ();
				dataSetTabbedPane.add (dataSet.getName (), panel);
				int columns = dataSet.getColumnCount ();
				int rows = dataSet.getColumnLength ();
				((JSpinner) panel.getComponent (2)).setValue (columns);
				((JSpinner) panel.getComponent (4)).setValue (rows);

				// Add minimum columns to the table model
				JTable table = (JTable) ((JViewport) ((JScrollPane) panel.getComponent (0)).getComponent (0)).getComponent (0);
				ExtendedTableModel newTableModel = new ExtendedTableModel ();
				DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel ();
				for (int j = 0; j < columns; ++j)
				{
					newColumnModel.addColumn (new TableColumn ());
					newTableModel.addColumn(dataSet.getColumn(j).getName());
					newColumnModel.getColumn (newTableModel.getColumnCount () - 1).setHeaderValue(dataSet.getColumn (j).getName ());
				}
				table.setColumnModel (newColumnModel);
				// Add minimum rows to the table model
				for (int j = 0; j < rows; ++j)
				{
					newTableModel.addRow (new Object[columns]);
				}
				// Fill in the values for all table elements
				for (int j = 0; j < columns; ++j)
				{
					for (int k = 0; k < dataSet.getColumn (j).size(); ++k)
					{
						newTableModel.setValueAt(dataSet.getColumn (j).get (k), k, j);
					}
				}
				table.setModel (newTableModel);
				table.updateUI ();
				table.getTableHeader().resizeAndRepaint ();
			}
			if (dataSetTabbedPane.getTabCount () > 1)
			{
				removeDataSetButton.setEnabled (true);
			}
			else
			{
				removeDataSetButton.setEnabled (false);
			}
		}
	}

	/**
	 * Launch the New Problem Wizard with the default characteristics.
	 */
	protected void launchNewProblemWizard()
	{
		openingWizard = true;

		newProblemOverwrite = false;

		// Set the first card panel as the only visible
		welcomeCardPanel.setVisible (true);
		nameAndLocationCardPanel.setVisible (false);
		descriptionCardPanel.setVisible (false);
		subProblemsCardPanel.setVisible (false);
		valuesCardPanel.setVisible (false);
		// Set the proper label to bold
		welcomeLabel.setFont (FONT_BOLD_12);
		nameAndLocationLabel.setFont (FONT_PLAIN_12);
		descriptionLabel.setFont (FONT_PLAIN_12);
		subProblemsLabel.setFont (FONT_PLAIN_12);
		valuesLabel.setFont (FONT_PLAIN_12);
		// Set forward/backward button states
		backWizardButton.setEnabled (false);
		nextWizardButton.setEnabled (true);
		nextWizardButton.setText ("Next >");
		// Set properties in the sub problems panel
		subProblemPanels.clear();
		subProblemsScollablePanel.removeAll ();
		subProblemsScollablePanel.updateUI ();
		addSubProblemButton.setEnabled (true);
		removeSubProblemButton.setEnabled (true);
		// Set properties for the values tabs
		dataSetTabbedPane.removeAll ();

		setNewProblemWizardDefaultValues ();

		// Pack and show the New Problem Wizard dialog
		newProblemWizardDialog.pack ();
		newProblemWizardDialog.setLocationRelativeTo (this);
		newProblemWizardDialog.setVisible (true);

		openingWizard = false;
	}

	/**
	 * Edit the currently selected data set.
	 */
	protected void editDataSet()
	{
		// Transition to the values card panel
		nextWizardButtonActionPerformed(null);
		nextWizardButtonActionPerformed(null);
		nextWizardButtonActionPerformed(null);
		nextWizardButtonActionPerformed(null);

		// Add the new data set
		try
		{
			dataSetTabbedPane.setSelectedIndex (domain.problem.getDataIndex (domain.currentDataSet.getName ()));
		}
		catch (DataNotFound ex) {}
	}

	/**
	 * Adds a new data set in the New Problem Wizard.
	 */
	protected void addNewDataSet()
	{
		// Transition to the values card panel
		nextWizardButtonActionPerformed(null);
		nextWizardButtonActionPerformed(null);
		nextWizardButtonActionPerformed(null);
		nextWizardButtonActionPerformed(null);

		// Add the new data set
		addDataSetButtonActionPerformed(null);
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
				outputTextArea.append(domain.problem.getAnswer(i) + "\n");
			}
		}
		catch (IncompleteInitialization ex) {}
		catch (CalcException ex)
		{
			JOptionPane.showMessageDialog(this, "The requested R package either cannot be located or is not installed.", "Missing Package", JOptionPane.WARNING_MESSAGE);
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
    private javax.swing.JButton addDataSetButton;
    private javax.swing.JButton addSubProblemButton;
    protected javax.swing.JButton backWizardButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton closeWizardButton;
    private javax.swing.JPanel componentsCardPanel;
    protected javax.swing.JPanel componentsPanel;
    private javax.swing.JTabbedPane dataSetTabbedPane;
    protected javax.swing.JPanel descriptionCardPanel;
    protected javax.swing.JLabel descriptionLabel;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JLabel descriptionWizardLabel;
    private javax.swing.JScrollPane descroptionScollPane;
    protected javax.swing.JPanel emptyPalettePanel;
    private javax.swing.JPanel leftPanel;
    protected javax.swing.JPanel nameAndLocationCardPanel;
    protected javax.swing.JLabel nameAndLocationLabel;
    private javax.swing.JPanel nameAndLocationPanel;
    private javax.swing.JLabel nameAndLocationWizardLabel;
    protected javax.swing.JDialog newProblemWizardDialog;
    protected javax.swing.JButton nextWizardButton;
    protected javax.swing.JFileChooser openChooserDialog;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JTextArea outputTextArea;
    private javax.swing.JLabel preWorkspaceLabel;
    protected javax.swing.JPanel preWorkspacePanel;
    private javax.swing.JLabel problemDescriptionLabel;
    private javax.swing.JLabel problemLocationLabel;
    private javax.swing.JTextField problemLocationTextField;
    private javax.swing.JLabel problemNameLabel;
    private javax.swing.JTextField problemNameTextField;
    private javax.swing.JButton removeDataSetButton;
    private javax.swing.JButton removeSubProblemButton;
    private javax.swing.JPanel rightPanel;
    protected javax.swing.JFileChooser saveChooserDialog;
    private javax.swing.JLabel stepsLabel;
    private javax.swing.JLabel stepsLineLabel;
    private javax.swing.JPanel stepsPanel;
    protected javax.swing.JPanel subProblemsCardPanel;
    protected javax.swing.JLabel subProblemsLabel;
    private javax.swing.JPanel subProblemsPanel;
    private javax.swing.JPanel subProblemsScollablePanel;
    private javax.swing.JScrollPane subProblemsScrollPane;
    private javax.swing.JLabel subProblemsWizardLabel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JPanel trayPanel;
    protected javax.swing.JPanel valuesCardPanel;
    protected javax.swing.JLabel valuesLabel;
    private javax.swing.JLabel valuesWizardLabel;
    protected javax.swing.JPanel welcomeCardPanel;
    protected javax.swing.JLabel welcomeLabel;
    private javax.swing.JPanel welcomePanel;
    protected javax.swing.JLabel welcomeTextLabel;
    private javax.swing.JLabel welcomeWizardLabel;
    private javax.swing.JPanel wizardCardPanel;
    private javax.swing.JPanel wizardControlPanel;
    private javax.swing.JLabel wizardLineCard1;
    private javax.swing.JLabel wizardLineCard2;
    private javax.swing.JLabel wizardLineCard3;
    private javax.swing.JLabel wizardLineCard4;
    private javax.swing.JLabel wizardLineCard5;
    private javax.swing.JPanel workspaceCardPanel;
    protected javax.swing.JPanel workspacePanel;
    private javax.swing.JSplitPane workspaceSplitPane;
    // End of variables declaration//GEN-END:variables

}
