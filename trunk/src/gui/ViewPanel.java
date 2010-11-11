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

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import problem.DataColumn;
import problem.DataSet;
import problem.FileException;
import problem.IncompleteInitialization;
import problem.Operation;
import problem.Problem;
import r.OperationMean;
import r.OperationStdDev;
import r.OperationSummation;
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
    public static final SimpleDateFormat fullTimeFormat = new SimpleDateFormat ("MM/dd/yyyy h:mm:ss a");
	
	
	/**************************************************************************/
	/* AVAILABLE FONT PROPERTIES                                              */
	/**************************************************************************/

	/** Default, plain, 12-point font.*/
	public static final Font FONT_PLAIN_12 = new Font ("Verdana", Font.PLAIN, 12);
	/** Default, bold, 12-point font.*/
	public static final Font FONT_BOLD_12 = new Font ("Verdana", Font.BOLD, 12);


	/**************************************************************************/
	/* DEFINE ALL STATISTICAL COMPONENTS THAT WILL BE LOADED INTO THE PALETTE */
	/* PAIN (THESE COMPONENTS MUST ALSO BE LISTED IN PALETTE_TYPES ARRAY)     */
	/**************************************************************************/
	
	private final Operation SUMMATION = new OperationSummation ();
	private final Operation MEAN = new OperationMean ();
	private final Operation STD_DEV = new OperationStdDev ();
	/** The list of types that will be loaded into the palette.*/
	private final Operation[] PALETTE_TYPES = new Operation[]
	{
		SUMMATION, MEAN, STD_DEV
	};

    /** The main frame of a stand-alone application.*/
    public MainFrame mainFrame;
	/** The domain object reference performs generic actions specific to the GUI.*/
    protected Domain domain = new Domain (this);
	/** The table model for values in the New Problem Wizard.*/
	private ExtendedTableModel valuesTableModel = new ExtendedTableModel ();
	/** True if the New Problem Wizard is being opened and actions should be ignored.*/
	private boolean openingWizard = false;
	/** Set true once the New Problem Wizard is told to overwrite existing files.*/
	private boolean newProblemOverwrite = false;
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

		// Set the font for the column headers in the values table
		valuesTable.getTableHeader ().setFont (FONT_PLAIN_12);
		// Add column header listeners
		valuesTable.getTableHeader ().addMouseListener(new MouseAdapter ()
		{
			@Override
			public void mouseReleased(MouseEvent evt)
			{
				int index = valuesTable.getTableHeader().columnAtPoint(evt.getPoint());
				Object name = JOptionPane.showInputDialog(newProblemWizardDialog,
						"Give the column a new name:",
						"Column Name",
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						valuesTable.getColumnModel ().getColumn(index).getHeaderValue());
				if (name != null)
				{
					valuesTableModel.setColumn (name.toString (), index);
					valuesTable.getColumnModel ().getColumn (index).setHeaderValue (name);
				}
			}
		});

		// Set custom behavior of JFileChooser
		JPanel access = (JPanel) ((JPanel) openChooserDialog.getComponent(3)).getComponent(3);
		((JButton) access.getComponent (1)).setToolTipText ("Cancel open");
		access = (JPanel) ((JPanel) saveChooserDialog.getComponent(3)).getComponent(3);
		((JButton) access.getComponent (1)).setToolTipText ("Cancel save");

		// Add all operation types to the palette, adding listeners to the labels as we go
		((GridLayout) componentsPanel.getLayout()).setRows((int) Math.round (PALETTE_TYPES.length / 2));
		for (int i = 0; i < PALETTE_TYPES.length; ++i)
		{
			final Operation operation = PALETTE_TYPES[i];

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
				public void mouseReleased(MouseEvent evt)
				{
					addToWorkspace (operation);
				}
			});
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
        jLabel1 = new javax.swing.JLabel();
        welcomeLabel = new javax.swing.JLabel();
        nameAndLocationLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        valuesLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        problemDescriptionLabel = new javax.swing.JLabel();
        subProblemsCardPanel = new javax.swing.JPanel();
        wizardLineCard5 = new javax.swing.JLabel();
        subProblemsWizardLabel = new javax.swing.JLabel();
        subProblemsPanel = new javax.swing.JPanel();
        valuesCardPanel = new javax.swing.JPanel();
        wizardLineCard4 = new javax.swing.JLabel();
        valuesWizardLabel = new javax.swing.JLabel();
        valuesPanel = new javax.swing.JPanel();
        valuesScrollPane = new javax.swing.JScrollPane();
        valuesTable = new ExtendedJTable ();
        rowsLabel = new javax.swing.JLabel();
        rowsSpinner = new javax.swing.JSpinner();
        columnsLabel = new javax.swing.JLabel();
        columnsSpinner = new javax.swing.JSpinner();
        importButton = new javax.swing.JButton();
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

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel1.setText("Steps");
        stepsPanel.add(jLabel1);
        jLabel1.setBounds(10, 10, 37, 16);

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

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel2.setText("_____________________");
        stepsPanel.add(jLabel2);
        jLabel2.setBounds(10, 10, 170, 20);

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
        welcomeTextLabel.setText("This will be welcome text ...");

        org.jdesktop.layout.GroupLayout welcomePanelLayout = new org.jdesktop.layout.GroupLayout(welcomePanel);
        welcomePanel.setLayout(welcomePanelLayout);
        welcomePanelLayout.setHorizontalGroup(
            welcomePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(welcomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(welcomeTextLabel)
                .addContainerGap(275, Short.MAX_VALUE))
        );
        welcomePanelLayout.setVerticalGroup(
            welcomePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(welcomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(welcomeTextLabel)
                .addContainerGap(292, Short.MAX_VALUE))
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
                        .add(problemNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE))
                    .add(nameAndLocationPanelLayout.createSequentialGroup()
                        .add(problemLocationLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(problemLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE))
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
                .addContainerGap(204, Short.MAX_VALUE))
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
        jScrollPane1.setViewportView(descriptionTextArea);

        problemDescriptionLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        problemDescriptionLabel.setText("Problem Description:");

        org.jdesktop.layout.GroupLayout descriptionPanelLayout = new org.jdesktop.layout.GroupLayout(descriptionPanel);
        descriptionPanel.setLayout(descriptionPanelLayout);
        descriptionPanelLayout.setHorizontalGroup(
            descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .add(problemDescriptionLabel))
                .addContainerGap())
        );
        descriptionPanelLayout.setVerticalGroup(
            descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(problemDescriptionLabel)
                .add(18, 18, 18)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
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

        subProblemsWizardLabel.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        subProblemsWizardLabel.setText("Sub Problems");
        subProblemsCardPanel.add(subProblemsWizardLabel);
        subProblemsWizardLabel.setBounds(10, 10, 130, 16);

        org.jdesktop.layout.GroupLayout subProblemsPanelLayout = new org.jdesktop.layout.GroupLayout(subProblemsPanel);
        subProblemsPanel.setLayout(subProblemsPanelLayout);
        subProblemsPanelLayout.setHorizontalGroup(
            subProblemsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 460, Short.MAX_VALUE)
        );
        subProblemsPanelLayout.setVerticalGroup(
            subProblemsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 320, Short.MAX_VALUE)
        );

        subProblemsCardPanel.add(subProblemsPanel);
        subProblemsPanel.setBounds(0, 40, 460, 320);

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

        valuesTable.setFont(new java.awt.Font("Verdana", 0, 12));
        valuesTable.setModel(valuesTableModel);
        valuesScrollPane.setViewportView(valuesTable);

        rowsLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        rowsLabel.setText("Rows:");

        rowsSpinner.setFont(new java.awt.Font("Verdana", 0, 12));
        rowsSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(3), Integer.valueOf(1), null, Integer.valueOf(1)));
        rowsSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rowsSpinnerStateChanged(evt);
            }
        });

        columnsLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        columnsLabel.setText("Columns:");

        columnsSpinner.setFont(new java.awt.Font("Verdana", 0, 12));
        columnsSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(5), Integer.valueOf(1), null, Integer.valueOf(1)));
        columnsSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                columnsSpinnerStateChanged(evt);
            }
        });

        importButton.setFont(new java.awt.Font("Verdana", 0, 12));
        importButton.setText("Import from CSV");
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout valuesPanelLayout = new org.jdesktop.layout.GroupLayout(valuesPanel);
        valuesPanel.setLayout(valuesPanelLayout);
        valuesPanelLayout.setHorizontalGroup(
            valuesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(valuesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(valuesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(valuesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .add(valuesPanelLayout.createSequentialGroup()
                        .add(columnsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(columnsSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(rowsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rowsSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 107, Short.MAX_VALUE)
                        .add(importButton)))
                .addContainerGap())
        );
        valuesPanelLayout.setVerticalGroup(
            valuesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(valuesPanelLayout.createSequentialGroup()
                .add(valuesScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 265, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(valuesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(importButton)
                    .add(valuesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(columnsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(columnsSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(valuesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(rowsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(rowsSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        valuesCardPanel.add(valuesPanel);
        valuesPanel.setBounds(0, 40, 460, 320);

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
                .addContainerGap(415, Short.MAX_VALUE)
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
                .add(wizardCardPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE))
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
        workspaceSplitPane.setDividerSize(5);
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
                .add(preWorkspaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
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
        workspacePanel.setLayout(null);
        workspaceCardPanel.add(workspacePanel, "card2");

        workspaceSplitPane.setTopComponent(workspaceCardPanel);

        outputScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        outputTextArea.setColumns(20);
        outputTextArea.setEditable(false);
        outputTextArea.setRows(5);
        outputScrollPane.setViewportView(outputTextArea);

        org.jdesktop.layout.GroupLayout trayPanelLayout = new org.jdesktop.layout.GroupLayout(trayPanel);
        trayPanel.setLayout(trayPanelLayout);
        trayPanelLayout.setHorizontalGroup(
            trayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 180, Short.MAX_VALUE)
            .add(trayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(outputScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
        );
        trayPanelLayout.setVerticalGroup(
            trayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 23, Short.MAX_VALUE)
            .add(trayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(outputScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, Short.MAX_VALUE))
        );

        workspaceSplitPane.setBottomComponent(trayPanel);

        add(workspaceSplitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

	private void closeWizardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeWizardButtonActionPerformed
		newProblemWizardDialog.dispose();
		mainFrame.requestFocus ();
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
			if (continueAllowed && file.exists () && !newProblemOverwrite)
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

	private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
		// Construct the folder-based open chooser dialog
		openChooserDialog.setFileFilter(csvFilter);
		openChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		openChooserDialog.setCurrentDirectory(new File (domain.lastGoodCsvFile));
		if (new File (domain.lastGoodCsvFile).isFile ())
		{
			openChooserDialog.setSelectedFile (new File (domain.lastGoodCsvFile));
		}
		else
		{
			openChooserDialog.setSelectedFile (new File (""));
		}
		// Display the chooser and retrieve the selected folder
		int response = openChooserDialog.showOpenDialog (newProblemWizardDialog);
		if (response == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				// If the user selected a file that exists, point the problem's location to the newly selected location
				if (openChooserDialog.getSelectedFile().exists ())
				{
					domain.lastGoodCsvFile = openChooserDialog.getSelectedFile().toString ();
					DataSet importedDataSet = DataSet.importFile(domain.lastGoodCsvFile);

					// Setup the values table to be ready for the import
					columnsSpinner.setValue (importedDataSet.getColumnCount());
					rowsSpinner.setValue (importedDataSet.getColumnLength());

					// Set the column headers
					String[] columnNames = importedDataSet.getColumnNames ();
					for (int i = 0; i < columnNames.length; ++i)
					{
						valuesTable.getColumnModel ().getColumn(i).setHeaderValue(columnNames[i]);
					}
					// Load imported data into the values table
					for (int i = 0; i < importedDataSet.getColumnCount(); ++i)
					{
						DataColumn column = importedDataSet.getColumn (i);
						for (int j = 0; j < column.size(); ++j)
						{
							valuesTableModel.setValueAt (column.get (j), j, i);
						}
					}
					valuesTable.updateUI ();
				}
			}
			catch(FileNotFoundException e) { }
		}
	}//GEN-LAST:event_importButtonActionPerformed

	private void columnsSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_columnsSpinnerStateChanged
		if (!openingWizard)
		{
			// If columns were removed, loop and delete from the end
			while (Integer.parseInt (columnsSpinner.getValue ().toString ()) < valuesTableModel.getColumnCount ())
			{
				valuesTableModel.removeColumn (valuesTable.getColumnCount () - 1);
				valuesTable.getColumnModel().removeColumn(valuesTable.getColumnModel().getColumn(valuesTable.getColumnCount () - 1));
			}
			// If columns were added, loop and add to the end
			while (Integer.parseInt (columnsSpinner.getValue ().toString ()) > valuesTableModel.getColumnCount ())
			{
				valuesTable.getColumnModel().addColumn (new TableColumn ());
				valuesTableModel.addColumn ("Column " + valuesTableModel.getColumnCount ());
				valuesTable.getColumnModel ().getColumn (valuesTableModel.getColumnCount () - 1).setHeaderValue ("Column " + valuesTableModel.getColumnCount ());
			}

			valuesTable.updateUI ();
		}
	}//GEN-LAST:event_columnsSpinnerStateChanged

	private void rowsSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rowsSpinnerStateChanged
		if (!openingWizard)
		{
			// If rows were removed, loop and delete from the end
			while (Integer.parseInt (rowsSpinner.getValue ().toString ()) < valuesTableModel.getRowCount ())
			{
				valuesTableModel.removeRow (valuesTable.getRowCount () - 1);
			}
			// If rows were added, loop and add to the end
			while (Integer.parseInt (rowsSpinner.getValue ().toString ()) > valuesTableModel.getRowCount ())
			{
				valuesTableModel.addRow (new Object[valuesTable.getColumnCount ()]);
			}

			valuesTable.updateUI ();
		}
	}//GEN-LAST:event_rowsSpinnerStateChanged

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
		Operation newOperation = operation.clone();
		newOperation.setBounds (x, y, 30, 16);
		domain.currentDataSet.addOperation(newOperation);
		workspacePanel.add (newOperation);
		workspacePanel.updateUI();
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
		domain.problem = new Problem (descriptionTextArea.getText ());
		domain.problem.markChanged();
		
		String fileName = problemNameTextField.getText ();
		if (!fileName.endsWith (".marla"))
		{
			fileName += ".marla";
		}
		domain.problem.setFileName(new File (problemLocationTextField.getText (), fileName).toString());
		domain.currentDataSet = new DataSet ("Data Set 1");
		domain.currentDataSet.setBounds (330, 50, domain.currentDataSet.getText ().length() * 8, 16);
		domain.problem.addData (domain.currentDataSet);
		// Add columns from the New Problem Wizard
		for (int i = 0; i < valuesTableModel.getColumnCount(); ++i)
		{
			DataColumn column = domain.currentDataSet.addColumn (valuesTableModel.getColumnName(i));
			// Add all rows within this column to the data set
			for (int j = 0; j < valuesTableModel.getRowCount (); ++j)
			{
				column.add (Double.parseDouble (valuesTableModel.getValueAt (j, i).toString ()));
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
				int response = 	JOptionPane.showConfirmDialog(this,
						"Would you like to save changes to the currently open problem?",
						"Save Problem Changes",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
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

		return true;
	}

	/**
	 * Open the problem currently stored in the problem variable.
	 */
	protected void openProblem()
	{
		if (domain.problem != null)
		{
			domain.currentDataSet = domain.problem.getData(0);
			for (int i = 0; i < domain.problem.getDataCount(); ++i)
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

			workspacePanel.updateUI();

			componentsPanel.setVisible (true);
			emptyPalettePanel.setVisible (false);
			workspacePanel.setVisible (true);
			preWorkspacePanel.setVisible (false);

			mainFrame.setTitle (mainFrame.getDefaultTitle () + " - " + domain.problem.getFileName().substring (domain.problem.getFileName ().lastIndexOf (System.getProperty ("file.separator")) + 1, domain.problem.getFileName ().lastIndexOf (".")));
		}
	}

	/**
	 * Sets the default values for components in the New Problem Wizard.
	 */
	private void setNewProblemDefaultValues()
	{
		// Set problem defaults for name and location
		problemNameTextField.setText ("New Problem");
		problemLocationTextField.setText (domain.lastGoodDir);
		descriptionTextArea.setText ("");

		// By default, new problems have three columns and five rows
		columnsSpinner.setValue (3);
		rowsSpinner.setValue (5);

		// Add minimum columns to the table model
		valuesTableModel = new ExtendedTableModel ();
		DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel ();
		for (int i = 0; i < Integer.parseInt (columnsSpinner.getValue ().toString ()); ++i)
		{
			newColumnModel.addColumn (new TableColumn ());
			valuesTableModel.addColumn("Column " + (i + 1));
		}
		valuesTable.setColumnModel (newColumnModel);
		// Add minimum rows to the table model
		for (int i = 0; i < Integer.parseInt (rowsSpinner.getValue ().toString ()); ++i)
		{
			valuesTableModel.addRow (new Object[Integer.parseInt (columnsSpinner.getValue ().toString ())]);
		}
		valuesTable.setModel (valuesTableModel);
		valuesTable.updateUI();
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
		valuesCardPanel.setVisible (false);
		// Set the proper label to bold
		welcomeLabel.setFont (FONT_BOLD_12);
		nameAndLocationLabel.setFont (FONT_PLAIN_12);
		descriptionLabel.setFont (FONT_PLAIN_12);
		valuesLabel.setFont (FONT_PLAIN_12);
		// Set forward/backward button states
		backWizardButton.setEnabled (false);
		nextWizardButton.setEnabled (true);
		nextWizardButton.setText ("Next >");

		setNewProblemDefaultValues ();

		// Pack and show the New Problem Wizard dialog
		newProblemWizardDialog.pack ();
		newProblemWizardDialog.setLocationRelativeTo (this);
		newProblemWizardDialog.setVisible(true);

		openingWizard = false;
	}

	/**
	 * Solve the data set as the currentDataSet in the domain.
	 */
	protected void solve()
	{
		Double[][] array = domain.currentDataSet.toArray();
		outputTextArea.append ("::Summation::\n");
		outputTextArea.append ("Values: ");
		for (int i = 0; i < array[0].length; ++i)
		{
			outputTextArea.append (array[0][i] + "");
			if (i < array[0].length - 1)
			{
				outputTextArea.append (", ");
			}
			else
			{
				outputTextArea.append ("\n");
			}
		}
		try
		{
			outputTextArea.append ("Solution: " + domain.problem.getAnswer());
		}
		catch (IncompleteInitialization ex) {}
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
				out.write ("Date: " + fullTimeFormat.format (date) + "\n");

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
    private javax.swing.JButton backWizardButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton closeWizardButton;
    private javax.swing.JLabel columnsLabel;
    private javax.swing.JSpinner columnsSpinner;
    private javax.swing.JPanel componentsCardPanel;
    protected javax.swing.JPanel componentsPanel;
    private javax.swing.JPanel descriptionCardPanel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JLabel descriptionWizardLabel;
    protected javax.swing.JPanel emptyPalettePanel;
    private javax.swing.JButton importButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPanel nameAndLocationCardPanel;
    private javax.swing.JLabel nameAndLocationLabel;
    private javax.swing.JPanel nameAndLocationPanel;
    private javax.swing.JLabel nameAndLocationWizardLabel;
    private javax.swing.JDialog newProblemWizardDialog;
    private javax.swing.JButton nextWizardButton;
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
    private javax.swing.JPanel rightPanel;
    private javax.swing.JLabel rowsLabel;
    private javax.swing.JSpinner rowsSpinner;
    protected javax.swing.JFileChooser saveChooserDialog;
    private javax.swing.JPanel stepsPanel;
    private javax.swing.JPanel subProblemsCardPanel;
    private javax.swing.JLabel subProblemsLabel;
    private javax.swing.JPanel subProblemsPanel;
    private javax.swing.JLabel subProblemsWizardLabel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JPanel trayPanel;
    private javax.swing.JPanel valuesCardPanel;
    private javax.swing.JLabel valuesLabel;
    private javax.swing.JPanel valuesPanel;
    private javax.swing.JScrollPane valuesScrollPane;
    private javax.swing.JTable valuesTable;
    private javax.swing.JLabel valuesWizardLabel;
    private javax.swing.JPanel welcomeCardPanel;
    private javax.swing.JLabel welcomeLabel;
    private javax.swing.JPanel welcomePanel;
    private javax.swing.JLabel welcomeTextLabel;
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