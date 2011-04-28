/*
 * The maRla Project - Graphical problem solver for statistical calculations.
 * Copyright © 2011 Cedarville University
 * http://marla.googlecode.com
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

package marla.ide.gui;

import marla.ide.gui.colorpicker.ColorPicker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import marla.ide.operation.Operation;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import marla.ide.problem.DataColumn;
import marla.ide.problem.DataNotFoundException;
import marla.ide.problem.DataSet;
import marla.ide.problem.DuplicateNameException;
import marla.ide.problem.MarlaException;
import marla.ide.problem.Problem;
import marla.ide.problem.SubProblem;
import marla.ide.r.RProcessorException;

/**
 * The New Problem Wizard Dialog.
 *
 * @author Alex Laird
 */
public class NewProblemWizardDialog extends EscapeDialog
{
	/** The alphabet.*/
	public static final String[] ALPHABET = new String[]
	{
		"a.", "b.", "c.", "d.", "e.", "f.",
		"g.", "h.", "i.", "j.", "k.", "l.",
		"m.", "n.", "o.", "p.", "q.", "r.",
		"s.", "t.", "u.", "v.", "w.", "x.",
		"y.", "z."
	};
	/** Tip text for the values panel.*/
	private final String VALUES_TIP_TEXT = "<html>Double-click on a data set tab to rename it.<br />Click on a column header in the table to rename it.</html>";
	/** The extensions file filter for CSV files.*/
	protected ExtensionFileFilter csvFilter = new ExtensionFileFilter("Comma Separated Value Files (.csv, .txt)", new String[]
			{
				"CSV", "TXT"
			});
	/** The list in the New Problem Wizard of sub problems within the current problem.*/
	private ArrayList<JPanel> subProblemPanels = new ArrayList<JPanel>();
	/** True if the New Problem Wizard is being opened and actions should be ignored.*/
	private boolean ignoreDataChanging = false;
	/** True when a data set tab is being added, false otherwise.*/
	private boolean addingDataSet = false;
	/** True when a data set value is being changed, false otherwise.*/
	private boolean changingDataSet = false;
	/** During a change, set with the old value of the table.*/
	protected static Object changingValue = null;
	/** Set true once the New Problem Wizard is told to overwrite existing files.*/
	private boolean newProblemOverwrite = false;
	/** A reference to the domain.*/
	private Domain domain;
	/** Object for the new problem being created in the wizard.*/
	protected Problem newProblem = null;
	/** True if the problem is being edited, false otherwise.*/
	private boolean editing = false;

	/**
	 * Construct the New Problem Wizard dialog.
	 *
	 * @param viewPanel A reference to the view panel.
	 * @param domain A reference to the domain.
	 */
	public NewProblemWizardDialog(final ViewPanel viewPanel, final Domain domain)
	{
		super(viewPanel.mainFrame, viewPanel);
		this.domain = domain;

		initComponents();

		// forward undo/redo key strokes to the main view
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK);
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke, KeyEvent.VK_Z + " | " + InputEvent.CTRL_MASK);
		rootPane.getActionMap().put(KeyEvent.VK_Z + " | " + InputEvent.CTRL_MASK, new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				viewPanel.undo();
			}
		});
		stroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK);
		inputMap.put(stroke, KeyEvent.VK_Y + " | " + InputEvent.CTRL_MASK);
		rootPane.getActionMap().put(KeyEvent.VK_Y + " | " + InputEvent.CTRL_MASK, new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				viewPanel.redo();
			}
		});

		dataSetTabbedPane.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent evt)
			{
				for(int i = 0; i < dataSetTabbedPane.getTabCount(); ++i)
				{
					if(dataSetTabbedPane.getUI().getTabBounds(dataSetTabbedPane, i).contains(evt.getPoint()) && evt.getClickCount() == 2)
					{
						String oldName = dataSetTabbedPane.getTitleAt(i);
						Object name = Domain.showInputDialog(Domain.getTopWindow(), "Give the data set a new name:", "Data Set Name", oldName);
						if(name != null)
						{
							if(!name.toString().equals(oldName))
							{
								if(!dataSetNameExists(i, name.toString()))
								{
									dataSetTabbedPane.setTitleAt(i, name.toString());
									try
									{
										domain.problem.getData(oldName).setDataName(name.toString());
									}
									catch(DataNotFoundException ex)
									{
									}
									catch(DuplicateNameException ex)
									{
									}

									if(editing)
									{
										updateLabelInRightPanel(oldName, name.toString());
									}
									viewPanel.workspacePanel.repaint();
								}
								else
								{
									Domain.showWarningDialog(Domain.getTopWindow(), "A column with that name already exists.", "Duplicate Column");
								}
							}
						}
					}
				}
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

        newProblemPanel = new javax.swing.JPanel();
        stepsPanel = new javax.swing.JPanel();
        stepsLabel = new javax.swing.JLabel();
        welcomeLabel = new javax.swing.JLabel();
        nameAndLocationLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        dataSetsLabel = new javax.swing.JLabel();
        stepsLineLabel = new javax.swing.JLabel();
        subProblemsLabel = new javax.swing.JLabel();
        informationLabel = new javax.swing.JLabel();
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
        subProblemsScrollablePanel = new javax.swing.JPanel();
        addSubProblemButton = new javax.swing.JButton();
        removeSubProblemButton = new javax.swing.JButton();
        dataSetsCardPanel = new javax.swing.JPanel();
        wizardLineCard4 = new javax.swing.JLabel();
        dataSetsWizardLabel = new javax.swing.JLabel();
        dataSetTabbedPane = new javax.swing.JTabbedPane();
        addDataSetButton = new javax.swing.JButton();
        removeDataSetButton = new javax.swing.JButton();
        tipTextLabel = new javax.swing.JLabel();
        addFromRLibButton = new javax.swing.JButton();
        informationCardPanel = new javax.swing.JPanel();
        studentNameLabel = new javax.swing.JLabel();
        courseShortNameLabel = new javax.swing.JLabel();
        courseLongNameLabel = new javax.swing.JLabel();
        chapterLabel = new javax.swing.JLabel();
        sectionLabel = new javax.swing.JLabel();
        problemNumberLabel = new javax.swing.JLabel();
        studentNameTextField = new javax.swing.JTextField();
        courseShortNameTextField = new javax.swing.JTextField();
        courseLongNameTextField = new javax.swing.JTextField();
        chapterTextField = new javax.swing.JTextField();
        sectionTextField = new javax.swing.JTextField();
        problemNumberTextField = new javax.swing.JTextField();
        wizardLineCard6 = new javax.swing.JLabel();
        informationWizardLabel = new javax.swing.JLabel();
        problemConclusionLabel = new javax.swing.JLabel();
        problemConclusionScrollPane = new javax.swing.JScrollPane();
        problemConclusionTextArea = new javax.swing.JTextArea();
        wizardControlPanel = new javax.swing.JPanel();
        closeWizardButton = new javax.swing.JButton();
        nextWizardButton = new javax.swing.JButton();
        backWizardButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconImage(new ImageIcon (getClass ().getResource (Domain.IMAGES_DIR + "new_button.png")).getImage ());
        setModal(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        stepsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        stepsPanel.setLayout(null);

        stepsLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        stepsLabel.setText("Steps");
        stepsPanel.add(stepsLabel);
        stepsLabel.setBounds(10, 10, 170, 16);

        welcomeLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        welcomeLabel.setText("1. Welcome");
        welcomeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseReleased(evt);
            }
        });
        stepsPanel.add(welcomeLabel);
        welcomeLabel.setBounds(20, 40, 140, 16);

        nameAndLocationLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        nameAndLocationLabel.setText("2. Name and Location");
        nameAndLocationLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseReleased(evt);
            }
        });
        stepsPanel.add(nameAndLocationLabel);
        nameAndLocationLabel.setBounds(20, 60, 160, 16);

        descriptionLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        descriptionLabel.setText("3. Description");
        descriptionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseReleased(evt);
            }
        });
        stepsPanel.add(descriptionLabel);
        descriptionLabel.setBounds(20, 80, 140, 16);

        dataSetsLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        dataSetsLabel.setText("5. Data Sets");
        dataSetsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseReleased(evt);
            }
        });
        stepsPanel.add(dataSetsLabel);
        dataSetsLabel.setBounds(20, 120, 140, 16);

        stepsLineLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        stepsLineLabel.setText("_____________________");
        stepsPanel.add(stepsLineLabel);
        stepsLineLabel.setBounds(10, 10, 170, 20);

        subProblemsLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        subProblemsLabel.setText("4. Sub Problems");
        subProblemsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseReleased(evt);
            }
        });
        stepsPanel.add(subProblemsLabel);
        subProblemsLabel.setBounds(20, 100, 140, 16);

        informationLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        informationLabel.setText("6. Information");
        informationLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                NewProblemWizardDialog.this.mouseReleased(evt);
            }
        });
        stepsPanel.add(informationLabel);
        informationLabel.setBounds(20, 140, 140, 16);

        wizardCardPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        wizardCardPanel.setLayout(new java.awt.CardLayout());

        welcomeCardPanel.setLayout(null);

        welcomeWizardLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        welcomeWizardLabel.setText("Welcome");
        welcomeCardPanel.add(welcomeWizardLabel);
        welcomeWizardLabel.setBounds(10, 10, 430, 16);

        wizardLineCard1.setFont(new java.awt.Font("Verdana", 0, 12));
        wizardLineCard1.setText("______________________________________________________");
        welcomeCardPanel.add(wizardLineCard1);
        wizardLineCard1.setBounds(10, 10, 440, 20);

        welcomeTextLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        welcomeTextLabel.setText("<<Welcome Text Placeholder>>");

        javax.swing.GroupLayout welcomePanelLayout = new javax.swing.GroupLayout(welcomePanel);
        welcomePanel.setLayout(welcomePanelLayout);
        welcomePanelLayout.setHorizontalGroup(
            welcomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(welcomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(welcomeTextLabel)
                .addContainerGap(247, Short.MAX_VALUE))
        );
        welcomePanelLayout.setVerticalGroup(
            welcomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(welcomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(welcomeTextLabel)
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

        javax.swing.GroupLayout nameAndLocationPanelLayout = new javax.swing.GroupLayout(nameAndLocationPanel);
        nameAndLocationPanel.setLayout(nameAndLocationPanelLayout);
        nameAndLocationPanelLayout.setHorizontalGroup(
            nameAndLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nameAndLocationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(nameAndLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(nameAndLocationPanelLayout.createSequentialGroup()
                        .addComponent(problemNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(problemNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE))
                    .addGroup(nameAndLocationPanelLayout.createSequentialGroup()
                        .addComponent(problemLocationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(problemLocationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE))
                    .addComponent(browseButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        nameAndLocationPanelLayout.setVerticalGroup(
            nameAndLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nameAndLocationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(nameAndLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(problemNameLabel)
                    .addComponent(problemNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(nameAndLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(problemLocationLabel)
                    .addComponent(problemLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton)
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

        javax.swing.GroupLayout descriptionPanelLayout = new javax.swing.GroupLayout(descriptionPanel);
        descriptionPanel.setLayout(descriptionPanelLayout);
        descriptionPanelLayout.setHorizontalGroup(
            descriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(descriptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(descriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descroptionScollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .addComponent(problemDescriptionLabel))
                .addContainerGap())
        );
        descriptionPanelLayout.setVerticalGroup(
            descriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(descriptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(problemDescriptionLabel)
                .addGap(18, 18, 18)
                .addComponent(descroptionScollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
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

        subProblemsScrollablePanel.setLayout(new javax.swing.BoxLayout(subProblemsScrollablePanel, javax.swing.BoxLayout.PAGE_AXIS));
        subProblemsScrollPane.setViewportView(subProblemsScrollablePanel);

        javax.swing.GroupLayout subProblemsPanelLayout = new javax.swing.GroupLayout(subProblemsPanel);
        subProblemsPanel.setLayout(subProblemsPanelLayout);
        subProblemsPanelLayout.setHorizontalGroup(
            subProblemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(subProblemsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(subProblemsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE))
        );
        subProblemsPanelLayout.setVerticalGroup(
            subProblemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(subProblemsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
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
        addSubProblemButton.setBounds(285, 330, 70, 28);

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
        removeSubProblemButton.setBounds(370, 330, 90, 28);

        wizardCardPanel.add(subProblemsCardPanel, "card6");

        dataSetsCardPanel.setLayout(null);

        wizardLineCard4.setFont(new java.awt.Font("Verdana", 0, 12));
        wizardLineCard4.setText("______________________________________________________");
        dataSetsCardPanel.add(wizardLineCard4);
        wizardLineCard4.setBounds(10, 10, 440, 20);

        dataSetsWizardLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        dataSetsWizardLabel.setText("Data Sets");
        dataSetsCardPanel.add(dataSetsWizardLabel);
        dataSetsWizardLabel.setBounds(10, 10, 160, 16);

        dataSetTabbedPane.setFont(new java.awt.Font("Verdana", 0, 12));
        dataSetsCardPanel.add(dataSetTabbedPane);
        dataSetTabbedPane.setBounds(0, 30, 460, 299);

        addDataSetButton.setFont(new java.awt.Font("Verdana", 0, 12));
        addDataSetButton.setText("Add");
        addDataSetButton.setToolTipText("Add a data set");
        addDataSetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDataSetButtonActionPerformed(evt);
            }
        });
        dataSetsCardPanel.add(addDataSetButton);
        addDataSetButton.setBounds(285, 330, 70, 28);

        removeDataSetButton.setFont(new java.awt.Font("Verdana", 0, 12));
        removeDataSetButton.setText("Remove");
        removeDataSetButton.setToolTipText("Remove the last data set");
        removeDataSetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDataSetButtonActionPerformed(evt);
            }
        });
        dataSetsCardPanel.add(removeDataSetButton);
        removeDataSetButton.setBounds(370, 330, 90, 28);

        tipTextLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        dataSetsCardPanel.add(tipTextLabel);
        tipTextLabel.setBounds(10, 360, 460, 30);

        addFromRLibButton.setFont(new java.awt.Font("Verdana", 0, 12));
        addFromRLibButton.setText("Add from R Library");
        addFromRLibButton.setToolTipText("Add a data set from an R library");
        addFromRLibButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFromRLibButtonActionPerformed(evt);
            }
        });
        dataSetsCardPanel.add(addFromRLibButton);
        addFromRLibButton.setBounds(0, 330, 150, 28);

        wizardCardPanel.add(dataSetsCardPanel, "card2");

        informationCardPanel.setLayout(null);

        studentNameLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        studentNameLabel.setText("Student name:");
        informationCardPanel.add(studentNameLabel);
        studentNameLabel.setBounds(10, 50, 94, 20);

        courseShortNameLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        courseShortNameLabel.setText("Course short name:");
        informationCardPanel.add(courseShortNameLabel);
        courseShortNameLabel.setBounds(10, 90, 126, 20);

        courseLongNameLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        courseLongNameLabel.setText("Course long name:");
        informationCardPanel.add(courseLongNameLabel);
        courseLongNameLabel.setBounds(10, 130, 120, 20);

        chapterLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        chapterLabel.setText("Chapter:");
        informationCardPanel.add(chapterLabel);
        chapterLabel.setBounds(10, 170, 60, 20);

        sectionLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        sectionLabel.setText("Section:");
        informationCardPanel.add(sectionLabel);
        sectionLabel.setBounds(10, 210, 51, 20);

        problemNumberLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        problemNumberLabel.setText("Problem number:");
        informationCardPanel.add(problemNumberLabel);
        problemNumberLabel.setBounds(10, 250, 108, 20);

        studentNameTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        informationCardPanel.add(studentNameTextField);
        studentNameTextField.setBounds(110, 50, 289, 26);

        courseShortNameTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        informationCardPanel.add(courseShortNameTextField);
        courseShortNameTextField.setBounds(147, 90, 150, 26);

        courseLongNameTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        informationCardPanel.add(courseLongNameTextField);
        courseLongNameTextField.setBounds(140, 130, 240, 26);

        chapterTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        informationCardPanel.add(chapterTextField);
        chapterTextField.setBounds(73, 170, 190, 26);

        sectionTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        informationCardPanel.add(sectionTextField);
        sectionTextField.setBounds(70, 210, 170, 26);

        problemNumberTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        informationCardPanel.add(problemNumberTextField);
        problemNumberTextField.setBounds(130, 250, 90, 26);

        wizardLineCard6.setFont(new java.awt.Font("Verdana", 0, 12));
        wizardLineCard6.setText("______________________________________________________");
        informationCardPanel.add(wizardLineCard6);
        wizardLineCard6.setBounds(10, 10, 440, 20);

        informationWizardLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        informationWizardLabel.setText("Information");
        informationCardPanel.add(informationWizardLabel);
        informationWizardLabel.setBounds(10, 10, 180, 16);

        problemConclusionLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        problemConclusionLabel.setText("Problem conclusion:");
        informationCardPanel.add(problemConclusionLabel);
        problemConclusionLabel.setBounds(10, 290, 130, 20);

        problemConclusionTextArea.setColumns(20);
        problemConclusionTextArea.setFont(new java.awt.Font("Verdana", 0, 12));
        problemConclusionTextArea.setLineWrap(true);
        problemConclusionTextArea.setRows(4);
        problemConclusionTextArea.setWrapStyleWord(true);
        problemConclusionScrollPane.setViewportView(problemConclusionTextArea);

        informationCardPanel.add(problemConclusionScrollPane);
        problemConclusionScrollPane.setBounds(150, 290, 350, 80);

        wizardCardPanel.add(informationCardPanel, "card7");

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

        javax.swing.GroupLayout wizardControlPanelLayout = new javax.swing.GroupLayout(wizardControlPanel);
        wizardControlPanel.setLayout(wizardControlPanelLayout);
        wizardControlPanelLayout.setHorizontalGroup(
            wizardControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, wizardControlPanelLayout.createSequentialGroup()
                .addContainerGap(516, Short.MAX_VALUE)
                .addComponent(backWizardButton, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextWizardButton, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(closeWizardButton)
                .addContainerGap())
        );
        wizardControlPanelLayout.setVerticalGroup(
            wizardControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(wizardControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(wizardControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeWizardButton)
                    .addComponent(nextWizardButton)
                    .addComponent(backWizardButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout newProblemPanelLayout = new javax.swing.GroupLayout(newProblemPanel);
        newProblemPanel.setLayout(newProblemPanelLayout);
        newProblemPanelLayout.setHorizontalGroup(
            newProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 787, Short.MAX_VALUE)
            .addGroup(newProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(newProblemPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(newProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(newProblemPanelLayout.createSequentialGroup()
                            .addComponent(stepsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(wizardCardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE))
                        .addComponent(wizardControlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap()))
        );
        newProblemPanelLayout.setVerticalGroup(
            newProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 486, Short.MAX_VALUE)
            .addGroup(newProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(newProblemPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(newProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(wizardCardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                        .addComponent(stepsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(wizardControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 787, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(newProblemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 486, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(newProblemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
		// Construct the folder-based open chooser dialog
		viewPanel.fileChooserDialog.setDialogTitle("Browse Problem Location");
		viewPanel.fileChooserDialog.setDialogType(JFileChooser.OPEN_DIALOG);
		viewPanel.fileChooserDialog.setFileFilter(viewPanel.defaultFilter);
		viewPanel.fileChooserDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		viewPanel.fileChooserDialog.setSelectedFile(new File(""));
		viewPanel.fileChooserDialog.setCurrentDirectory(new File(Domain.lastGoodDir));
		// Display the chooser and retrieve the selected folder
		int response = viewPanel.fileChooserDialog.showOpenDialog(Domain.getTopWindow());
		if(response == JFileChooser.APPROVE_OPTION)
		{
			// If the user selected a folder that exists, point the problem's location to the newly selected location
			if(viewPanel.fileChooserDialog.getSelectedFile().exists())
			{
				File file = viewPanel.fileChooserDialog.getSelectedFile();
				if(file.isDirectory())
				{
					Domain.lastGoodDir = file.toString();
				}
				else
				{
					Domain.lastGoodDir = file.getParent();
				}
				problemLocationTextField.setText(Domain.lastGoodDir);
			}
		}
}//GEN-LAST:event_browseButtonActionPerformed

	private void addSubProblemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSubProblemButtonActionPerformed
		domain.changeBeginning("adding subproblem");

		SubProblem subProblem;
		if(newProblem != null)
		{
			subProblem = newProblem.addSubProblem(ALPHABET[newProblem.getSubProblemCount()], "");
		}
		else
		{
			subProblem = domain.problem.addSubProblem(ALPHABET[domain.problem.getSubProblemCount()], "");
		}

		JPanel panel = createSubProblemPanel(subProblem);

		// Add the JPanel to the list of sub problem JPanels
		subProblemPanels.add(panel);
		if(subProblemPanels.size() == 1)
		{
			removeSubProblemButton.setEnabled(true);
		}
		else if(subProblemPanels.size() == 26)
		{
			addSubProblemButton.setEnabled(false);
		}

		// Add the JPanel to the New Problem Wizard
		subProblemsScrollablePanel.add(panel);
		subProblemsScrollablePanel.invalidate();
		subProblemsScrollablePanel.scrollRectToVisible(new Rectangle(0, subProblemsScrollablePanel.getHeight() + 150, 1, 1));

		if(editing)
		{
			viewPanel.rebuildSubProblemLegend();
		}

		((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get(subProblemPanels.size() - 1)).getComponent(1)).getComponent(0)).getComponent(0)).requestFocus();
}//GEN-LAST:event_addSubProblemButtonActionPerformed

	private void removeSubProblemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSubProblemButtonActionPerformed
		domain.changeBeginning("removing subproblem");

		// Remove the JPanel from the list of sub problems and from the New Problem Wizard
		JPanel panel = subProblemPanels.remove(subProblemPanels.size() - 1);
		if(newProblem != null)
		{
			newProblem.removeSubProblem(newProblem.getSubProblem(newProblem.getSubProblemCount() - 1));
		}
		else
		{
			domain.problem.removeSubProblem(domain.problem.getSubProblem(domain.problem.getSubProblemCount() - 1));
		}
		subProblemsScrollablePanel.remove(panel);

		if(subProblemPanels.isEmpty())
		{
			removeSubProblemButton.setEnabled(false);
		}
		else if(subProblemPanels.size() == 25)
		{
			addSubProblemButton.setEnabled(true);
		}

		subProblemsScrollablePanel.invalidate();
		subProblemsScrollablePanel.revalidate();
		subProblemsScrollablePanel.repaint();

		if(editing)
		{
			viewPanel.rebuildSubProblemLegend();
		}

		try
		{
			((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get(subProblemPanels.size() - 1)).getComponent(1)).getComponent(0)).getComponent(0)).requestFocus();
		}
		catch(ArrayIndexOutOfBoundsException ex)
		{
		}
}//GEN-LAST:event_removeSubProblemButtonActionPerformed

	private void addDataSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDataSetButtonActionPerformed
		addingDataSet = true;

		final Problem problem;
		if(newProblem != null)
		{
			problem = newProblem;
		}
		else
		{
			problem = domain.problem;
		}

		int index = dataSetTabbedPane.getTabCount() + 1;
		while(dataSetNameExists(-1, "Data Set " + index))
		{
			++index;
		}
		DataSet dataSet = null;
		try
		{
			dataSet = problem.addData(new DataSet("Data Set " + index));
			dataSet.addColumn("Column 1");
			dataSet.getColumn(0).add(0.0);
			dataSet.getColumn(0).add(0.0);
			dataSet.getColumn(0).add(0.0);
			dataSet.getColumn(0).add(0.0);
			dataSet.getColumn(0).add(0.0);
			dataSet.addColumn("Column 2");
			dataSet.getColumn(1).add(0.0);
			dataSet.getColumn(1).add(0.0);
			dataSet.getColumn(1).add(0.0);
			dataSet.getColumn(1).add(0.0);
			dataSet.getColumn(1).add(0.0);
			dataSet.addColumn("Column 3");
			dataSet.getColumn(2).add(0.0);
			dataSet.getColumn(2).add(0.0);
			dataSet.getColumn(2).add(0.0);
			dataSet.getColumn(2).add(0.0);
			dataSet.getColumn(2).add(0.0);
		}
		// This check has already occured, so this exception will never be thrown
		catch(DuplicateNameException ex)
		{
			Domain.logger.add(ex);
		}

		addDataSet(dataSet);

		addingDataSet = false;
}//GEN-LAST:event_addDataSetButtonActionPerformed

	private void removeDataSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDataSetButtonActionPerformed
		Problem problem;
		if(newProblem != null)
		{
			problem = newProblem;
		}
		else
		{
			problem = domain.problem;
		}
		int response = Domain.showConfirmDialog(Domain.getTopWindow(), "Removing a data set from the problem will remove it and all its\nattached operations from the workspace as well.\nAre you sure you want to permanently remove " + problem.getData(dataSetTabbedPane.getSelectedIndex()).getDisplayString(false) + "?", "Remove Data", JOptionPane.YES_NO_OPTION);
		if(response == JOptionPane.YES_OPTION)
		{
			DataSet removedData = null;
			if(newProblem != null)
			{
				removedData = newProblem.removeData(newProblem.getData(dataSetTabbedPane.getSelectedIndex()));
				repositionDataSets(newProblem);
			}
			else
			{
				removedData = domain.problem.removeData(domain.problem.getData(dataSetTabbedPane.getSelectedIndex()));
				for(int i = 0; i < removedData.getOperationCount(); ++i)
				{
					Operation op = removedData.getOperation(i);
					for(Operation childOp : op.getAllChildOperations())
					{
						viewPanel.workspacePanel.remove(childOp);
					}
					viewPanel.workspacePanel.remove(op);
				}
			}

			if(editing)
			{
				viewPanel.workspacePanel.remove(removedData);
				viewPanel.workspacePanel.repaint();
				viewPanel.rebuildDataSetLegend();
			}

			dataSetTabbedPane.remove(dataSetTabbedPane.getSelectedIndex());
			if(dataSetTabbedPane.getTabCount() > 0)
			{
				removeDataSetButton.setEnabled(true);
			}
			else
			{
				removeDataSetButton.setEnabled(false);
			}
		}
}//GEN-LAST:event_removeDataSetButtonActionPerformed

	private void closeWizardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeWizardButtonActionPerformed
		ignoreDataChanging = true;

		if(editing)
		{
			if(descriptionCardPanel.isVisible())
			{
				Problem problem = domain.problem;
				if(newProblem != null)
				{
					problem = newProblem;
				}
				verifyDescriptionPanel(problem);
			}
			else if(subProblemsCardPanel.isVisible())
			{
				Problem problem = domain.problem;
				if(newProblem != null)
				{
					problem = newProblem;
				}
				verifySubProblemsPanel(problem);
			}
			else if(informationCardPanel.isVisible())
			{
				Problem problem = domain.problem;
				if(newProblem != null)
				{
					problem = newProblem;
				}
				verifyInfoPanel(problem);
			}
		}

		dispose();
		viewPanel.requestFocus();

		newProblem = null;
		ignoreDataChanging = false;
}//GEN-LAST:event_closeWizardButtonActionPerformed

	private void nextWizardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextWizardButtonActionPerformed
		goNext();
}//GEN-LAST:event_nextWizardButtonActionPerformed

	private void backWizardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backWizardButtonActionPerformed
		if(nameAndLocationCardPanel.isVisible())
		{
			if(newProblem != null)
			{
				try
				{
					File file = new File(problemLocationTextField.getText(), problemNameTextField.getText());
					if(!file.getCanonicalPath().equals(newProblem.getFileName()))
					{
						newProblem.setFileName(file.getCanonicalPath());
					}
				}
				catch(IOException ex)
				{
					Domain.logger.add(ex);
				}
			}

			// Move to the previous panel in the cards
			welcomeCardPanel.setVisible(true);
			nameAndLocationCardPanel.setVisible(false);

			// Shift the boldness in the Steps panel to the previous card
			welcomeLabel.setFont(ViewPanel.FONT_BOLD_12);
			nameAndLocationLabel.setFont(ViewPanel.FONT_PLAIN_12);

			backWizardButton.setEnabled(false);
		}
		else if(descriptionCardPanel.isVisible())
		{
			Problem problem = domain.problem;
			if(newProblem != null)
			{
				problem = newProblem;
			}
			verifyDescriptionPanel(problem);

			// Move to the previous panel in the cards
			nameAndLocationCardPanel.setVisible(true);
			descriptionCardPanel.setVisible(false);

			// Shift the boldness in the Steps panel to the previous card
			nameAndLocationLabel.setFont(ViewPanel.FONT_BOLD_12);
			descriptionLabel.setFont(ViewPanel.FONT_PLAIN_12);

			// Set the focus properly for the new card
			problemNameTextField.requestFocus();
			problemNameTextField.selectAll();
		}
		else if(subProblemsCardPanel.isVisible())
		{
			Problem problem = domain.problem;
			if(newProblem != null)
			{
				problem = newProblem;
			}
			verifySubProblemsPanel(problem);

			// Move to the previous panel in the cards
			descriptionCardPanel.setVisible(true);
			subProblemsCardPanel.setVisible(false);

			// Shift the boldness in the Steps panel to the previous card
			descriptionLabel.setFont(ViewPanel.FONT_BOLD_12);
			subProblemsLabel.setFont(ViewPanel.FONT_PLAIN_12);

			// Set the focus properly for the new card
			descriptionTextArea.requestFocus();
			descriptionTextArea.selectAll();
		}
		else if(dataSetsCardPanel.isVisible())
		{
			// Move to the next panel in the cards
			subProblemsCardPanel.setVisible(true);
			dataSetsCardPanel.setVisible(false);

			tipTextLabel.setText("");

			// Shift the boldness in the Steps panel to the next card
			subProblemsLabel.setFont(ViewPanel.FONT_BOLD_12);
			dataSetsLabel.setFont(ViewPanel.FONT_PLAIN_12);

			// Set the focus properly for the new card
			try
			{
				((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get(subProblemPanels.size() - 1)).getComponent(1)).getComponent(0)).getComponent(0)).requestFocus();
				((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get(subProblemPanels.size() - 1)).getComponent(1)).getComponent(0)).getComponent(0)).selectAll();
				subProblemsScrollablePanel.scrollRectToVisible(new Rectangle(0, subProblemsScrollablePanel.getHeight(), 1, 1));
			}
			catch(ArrayIndexOutOfBoundsException ex)
			{
			}
		}
		else
		{
			Problem problem = domain.problem;
			if(newProblem != null)
			{
				problem = newProblem;
			}
			verifyInfoPanel(problem);

			// Move to the previous panel in the cards
			dataSetsCardPanel.setVisible(true);
			informationCardPanel.setVisible(false);

			tipTextLabel.setText(VALUES_TIP_TEXT);

			// Shift the boldness in the Steps panel to the previous card
			dataSetsLabel.setFont(ViewPanel.FONT_BOLD_12);
			informationLabel.setFont(ViewPanel.FONT_PLAIN_12);

			nextWizardButton.setText("Next >");
		}
}//GEN-LAST:event_backWizardButtonActionPerformed

	private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
	{//GEN-HEADEREND:event_formWindowClosing
		if(editing)
		{
			if(descriptionCardPanel.isVisible())
			{
				verifyDescriptionPanel(domain.problem);
			}
			else if(subProblemsCardPanel.isVisible())
			{
				verifySubProblemsPanel(domain.problem);
			}
			else if(informationCardPanel.isVisible())
			{
				verifyInfoPanel(domain.problem);
			}
		}

		dispose();
	}//GEN-LAST:event_formWindowClosing

	private void mouseEntered(java.awt.event.MouseEvent evt)//GEN-FIRST:event_mouseEntered
	{//GEN-HEADEREND:event_mouseEntered
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}//GEN-LAST:event_mouseEntered

	private void mouseExited(java.awt.event.MouseEvent evt)//GEN-FIRST:event_mouseExited
	{//GEN-HEADEREND:event_mouseExited
		setCursor(Cursor.getDefaultCursor());
	}//GEN-LAST:event_mouseExited

	private void mouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_mouseReleased
	{//GEN-HEADEREND:event_mouseReleased
		JLabel label = (JLabel) evt.getSource();
		while(backWizardButton.isEnabled())
		{
			backWizardButtonActionPerformed(null);
		}
		if(label.isEnabled() && label == welcomeLabel)
		{
			// do nothing, cuz we're here
		}
		else if(label.isEnabled() && label == nameAndLocationLabel)
		{
			goNext();
		}
		else if(label.isEnabled() && label == descriptionLabel)
		{
			boolean continueAllowed = goNext();
			if(continueAllowed)
			{
				continueAllowed = goNext();
			}
		}
		else if(label.isEnabled() && label == subProblemsLabel)
		{
			boolean continueAllowed = goNext();
			if(continueAllowed)
			{
				continueAllowed = goNext();
			}
			if(continueAllowed)
			{
				continueAllowed = goNext();
			}
		}
		else if(label.isEnabled() && label == dataSetsLabel)
		{
			boolean continueAllowed = goNext();
			if(continueAllowed)
			{
				continueAllowed = goNext();
			}
			if(continueAllowed)
			{
				continueAllowed = goNext();
			}
			if(continueAllowed)
			{
				continueAllowed = goNext();
			}
		}
		else if(label.isEnabled() && label == informationLabel)
		{
			boolean continueAllowed = goNext();
			if(continueAllowed)
			{
				continueAllowed = goNext();
			}
			if(continueAllowed)
			{
				continueAllowed = goNext();
			}
			if(continueAllowed)
			{
				continueAllowed = goNext();
			}
			if(continueAllowed)
			{
				continueAllowed = goNext();
			}
		}
	}//GEN-LAST:event_mouseReleased

	private void addFromRLibButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFromRLibButtonActionPerformed
		final JPanel optionPanel = new JPanel(new GridLayout(3, 1));
		JPanel libPanel = new JPanel(new BorderLayout());
		JLabel libLabel = new JLabel("Library: ");
		final JLabel docLabel = new JLabel("<html><u>Click here to view Devore7 documentation.</u></html>");
		docLabel.setForeground(Color.BLUE);
		JLabel label2 = new JLabel("Enter the name of the data set you'd like to add from the library.");
		final JTextField libTextField = new JTextField("Devore7");

		docLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent evt)
			{
				optionPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent evt)
			{
				optionPanel.setCursor(Cursor.getDefaultCursor());
			}

			@Override
			public void mouseReleased(MouseEvent evt)
			{
				if(viewPanel.domain.desktop != null)
				{
					try
					{
						if(libTextField.getText().equals("Devore5"))
						{
							viewPanel.domain.desktop.browse(new URI("http://cran.fyxm.net/web/packages/Devore5/Devore5.pdf"));
						}
						else if(libTextField.getText().equals("Devore6"))
						{
							viewPanel.domain.desktop.browse(new URI("http://cran.fyxm.net/web/packages/Devore6/Devore6.pdf"));
						}
						else if(libTextField.getText().equals("Devore7"))
						{
							viewPanel.domain.desktop.browse(new URI("http://cran.fyxm.net/web/packages/Devore7/Devore7.pdf"));
						}
						else if(libTextField.getText().equals("cluster"))
						{
							viewPanel.domain.desktop.browse(new URI("http://stat.ethz.ch/R-manual/R-devel/library/cluster/html/00Index.html"));
						}
						else if(libTextField.getText().equals("datasets"))
						{
							viewPanel.domain.desktop.browse(new URI("http://stat.ethz.ch/R-manual/R-devel/library/datasets/html/00Index.html"));
						}
					}
					catch(IOException ex)
					{
					}
					catch(URISyntaxException ex)
					{
					}
				}
			}
		});

		libTextField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyTyped(KeyEvent evt)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						if(isAcceptedLibrary(libTextField.getText()))
						{
							docLabel.setText("<html><u>Click here to view " + libTextField.getText() + " documentation.</u></html>");
							docLabel.setVisible(true);
						}
						else
						{
							docLabel.setVisible(false);
						}
					}
				});
			}
		});

		libPanel.add(libLabel, BorderLayout.WEST);
		libPanel.add(libTextField, BorderLayout.CENTER);

		optionPanel.add(libPanel);
		optionPanel.add(docLabel);
		optionPanel.add(label2);
		final Object response = Domain.showInputDialog(Domain.getTopWindow(), optionPanel, "Data Set from R Library", "");
		if(response != null)
		{
			Domain.setProgressTitle("Getting Library");
			Domain.setProgressVisible(true);
			Domain.setProgressIndeterminate(true);
			Domain.setProgressString("");
			Domain.setProgressStatus("Getting library from R...");
			MainFrame.progressFrame.setAlwaysOnTop(true);

			final NewProblemWizardDialog newProblemWizardDialog = this;
			setEnabled(false);
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						DataSet dataSet = DataSet.importFromR(libTextField.getText(), response.toString());

						addingDataSet = true;
						Problem problem;
						if(newProblem != null)
						{
							problem = newProblem;
						}
						else
						{
							problem = domain.problem;
						}

						problem.addData(dataSet);
						addDataSet(dataSet);

						addingDataSet = false;

						Domain.setProgressVisible(false);
					}
					catch(RProcessorException ex)
					{
						Domain.setProgressVisible(false);

						Domain.showWarningDialog(Domain.getTopWindow(), "The data set '" + response.toString() + "' could not be found in the " + libTextField.getText() + " library.", "Data Set Not Loaded");
					}
					catch(MarlaException ex)
					{
						Domain.setProgressVisible(false);

						Domain.showWarningDialog(Domain.getTopWindow(), ex.getMessage(), Domain.prettyExceptionDetails(ex), "Data Set Not Loadable");
					}
					finally
					{
						Domain.setProgressIndeterminate(false);
						MainFrame.progressFrame.setAlwaysOnTop(false);
						setEnabled(true);
					}
				}
			}).start();
		}
	}//GEN-LAST:event_addFromRLibButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDataSetButton;
    private javax.swing.JButton addFromRLibButton;
    private javax.swing.JButton addSubProblemButton;
    protected javax.swing.JButton backWizardButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel chapterLabel;
    private javax.swing.JTextField chapterTextField;
    private javax.swing.JButton closeWizardButton;
    private javax.swing.JLabel courseLongNameLabel;
    private javax.swing.JTextField courseLongNameTextField;
    private javax.swing.JLabel courseShortNameLabel;
    private javax.swing.JTextField courseShortNameTextField;
    protected javax.swing.JTabbedPane dataSetTabbedPane;
    protected javax.swing.JPanel dataSetsCardPanel;
    protected javax.swing.JLabel dataSetsLabel;
    private javax.swing.JLabel dataSetsWizardLabel;
    protected javax.swing.JPanel descriptionCardPanel;
    protected javax.swing.JLabel descriptionLabel;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JLabel descriptionWizardLabel;
    private javax.swing.JScrollPane descroptionScollPane;
    private javax.swing.JPanel informationCardPanel;
    protected javax.swing.JLabel informationLabel;
    private javax.swing.JLabel informationWizardLabel;
    protected javax.swing.JPanel nameAndLocationCardPanel;
    protected javax.swing.JLabel nameAndLocationLabel;
    private javax.swing.JPanel nameAndLocationPanel;
    private javax.swing.JLabel nameAndLocationWizardLabel;
    private javax.swing.JPanel newProblemPanel;
    protected javax.swing.JButton nextWizardButton;
    private javax.swing.JLabel problemConclusionLabel;
    private javax.swing.JScrollPane problemConclusionScrollPane;
    private javax.swing.JTextArea problemConclusionTextArea;
    private javax.swing.JLabel problemDescriptionLabel;
    private javax.swing.JLabel problemLocationLabel;
    private javax.swing.JTextField problemLocationTextField;
    private javax.swing.JLabel problemNameLabel;
    private javax.swing.JTextField problemNameTextField;
    private javax.swing.JLabel problemNumberLabel;
    private javax.swing.JTextField problemNumberTextField;
    private javax.swing.JButton removeDataSetButton;
    private javax.swing.JButton removeSubProblemButton;
    private javax.swing.JLabel sectionLabel;
    private javax.swing.JTextField sectionTextField;
    private javax.swing.JLabel stepsLabel;
    private javax.swing.JLabel stepsLineLabel;
    private javax.swing.JPanel stepsPanel;
    private javax.swing.JLabel studentNameLabel;
    private javax.swing.JTextField studentNameTextField;
    protected javax.swing.JPanel subProblemsCardPanel;
    protected javax.swing.JLabel subProblemsLabel;
    private javax.swing.JPanel subProblemsPanel;
    private javax.swing.JScrollPane subProblemsScrollPane;
    protected javax.swing.JPanel subProblemsScrollablePanel;
    private javax.swing.JLabel subProblemsWizardLabel;
    private javax.swing.JLabel tipTextLabel;
    protected javax.swing.JPanel welcomeCardPanel;
    protected javax.swing.JLabel welcomeLabel;
    private javax.swing.JPanel welcomePanel;
    protected javax.swing.JLabel welcomeTextLabel;
    private javax.swing.JLabel welcomeWizardLabel;
    protected javax.swing.JPanel wizardCardPanel;
    private javax.swing.JPanel wizardControlPanel;
    private javax.swing.JLabel wizardLineCard1;
    private javax.swing.JLabel wizardLineCard2;
    private javax.swing.JLabel wizardLineCard3;
    private javax.swing.JLabel wizardLineCard4;
    private javax.swing.JLabel wizardLineCard5;
    private javax.swing.JLabel wizardLineCard6;
    // End of variables declaration//GEN-END:variables

	/**
	 * Checks if a given library has a known documentation set.
	 *
	 * @param library The library to check for.
	 * @return True if the given library has known documentation, false otherwise.
	 */
	private boolean isAcceptedLibrary(String library)
	{
		if(library.equals("Devore5")
		   || library.equals("Devore6")
		   || library.equals("Devore7")
		   || library.equals("datasets")
		   || library.equals("cluster"))
		{
			return true;
		}

		return false;
	}

	/**
	 * Add the given data set to the given problem.
	 *
	 * @param dataSet The data set to add.
	 */
	private void addDataSet(DataSet dataSet)
	{
		viewPanel.ensureComponentsVisible();
		viewPanel.workspacePanel.repaint();

		JPanel panel = createDataSetTabbedPane(dataSet);
		dataSetTabbedPane.add(dataSet.getName(), panel);
		dataSetTabbedPane.setSelectedIndex(dataSetTabbedPane.getTabCount() - 1);
		int columns = dataSet.getColumnCount();
		int rows = dataSet.getColumnLength();

		// Add minimum columns to the table model
		ExtendedJTable table = (ExtendedJTable) ((JViewport) ((JScrollPane) panel.getComponent(0)).getComponent(2)).getComponent(0);
		final ExtendedTableModel newModel = new ExtendedTableModel(dataSet);
		newModel.addTableModelListener(new TableModelListener()
		{
			@Override
			public void tableChanged(TableModelEvent evt)
			{
				fireTableChanged(newModel, evt);
			}
		});

		DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel();
		for(int i = 0; i < columns; ++i)
		{
			newColumnModel.addColumn(new TableColumn());
			newColumnModel.getColumn(i).setHeaderValue(dataSet.getColumn(i).getName());
		}
		table.setColumnModel(newColumnModel);

		table.setModel(newModel);
		table.refreshTable();

		// Wait to change the spinners until after the model is set or they will
		// try to do stuff to the columns (they see an increase from 0 to 5)
		((JSpinner) panel.getComponent(2)).setValue(columns);
		((JSpinner) panel.getComponent(4)).setValue(rows);

		if(dataSetTabbedPane.getTabCount() > 0)
		{
			removeDataSetButton.setEnabled(true);
		}
		else
		{
			removeDataSetButton.setEnabled(false);
		}

		if(editing)
		{
			dataSet.setLocation((viewPanel.workspacePanel.getWidth() - dataSet.getWidth()) / 2, (viewPanel.workspacePanel.getHeight() - dataSet.getHeight()) / 2);
			while(viewPanel.workspacePanel.getComponentAt(dataSet.getLocation().x + 10, dataSet.getLocation().y + 10, dataSet) != null)
			{
				dataSet.setLocation(dataSet.getLocation().x, dataSet.getLocation().y + 20);
			}
			viewPanel.rebuildDataSetLegend();
		}
	}

	/**
	 * Go to the next card panel in the wizard.
	 */
	protected boolean goNext()
	{
		if(welcomeCardPanel.isVisible())
		{
			// Move to the next panel in the cards
			nameAndLocationCardPanel.setVisible(true);
			welcomeCardPanel.setVisible(false);

			// Shift the boldness in the Steps panel to the next card
			welcomeLabel.setFont(ViewPanel.FONT_PLAIN_12);
			nameAndLocationLabel.setFont(ViewPanel.FONT_BOLD_12);

			// Set the focus properly for the new card
			problemNameTextField.requestFocus();
			problemNameTextField.selectAll();

			backWizardButton.setEnabled(true);

			return true;
		}
		else if(nameAndLocationCardPanel.isVisible())
		{
			boolean continueAllowed = true;
			String fileName = problemNameTextField.getText();
			// Assuming the user didn't specify our file type, append the type
			if(!fileName.endsWith(".marla"))
			{
				fileName += ".marla";
			}
			// Before advancing to the next card, ensure a name is given for the new problem
			if(problemNameTextField.getText().replaceAll(" ", "").equals(""))
			{
				problemNameTextField.setText("New Problem");
			}
			File file = new File(problemLocationTextField.getText(), fileName);
			if(!file.exists())
			{
				// Ensure the problem name given is a valid filename
				try
				{
					FileWriter write = new FileWriter(file);
					write.close();
					file.delete();
				}
				catch(IOException ex)
				{
					Domain.showWarningDialog(Domain.getTopWindow(), "The problem name you have given contains characters that are\nnot legal in a filename. Please rename your file and avoid\nusing special characters.", "Invalid Filename");

					continueAllowed = false;
				}
			}
			// Ensure the problem name given does not match an already existing file
			if(continueAllowed && file.exists() && !newProblemOverwrite && newProblem != null)
			{
				int response = Domain.showConfirmDialog(Domain.getTopWindow(), "The given problem name already exists as a file\nat the specified location.\nWould you like to overwrite the existing file?", "Overwrite Existing File", JOptionPane.YES_NO_OPTION);
				if(response == JOptionPane.YES_OPTION)
				{
					newProblemOverwrite = true;
				}
				else
				{
					continueAllowed = false;
				}
			}

			if(continueAllowed)
			{
				if(newProblem != null)
				{
					try
					{
						if(!file.getCanonicalPath().equals(newProblem.getFileName()))
						{
							newProblem.setFileName(file.getCanonicalPath());
						}
					}
					catch(IOException ex)
					{
						Domain.logger.add(ex);
					}
				}

				// Move to the next panel in the cards
				descriptionCardPanel.setVisible(true);
				nameAndLocationCardPanel.setVisible(false);

				// Shift the boldness in the Steps panel to the next card
				nameAndLocationLabel.setFont(ViewPanel.FONT_PLAIN_12);
				descriptionLabel.setFont(ViewPanel.FONT_BOLD_12);

				// Set the focus properly for the new card
				descriptionTextArea.requestFocus();
				descriptionTextArea.selectAll();
			}
			else
			{
				// Since continuation wasn't allowed, the user needs to correct the problem name
				problemNameTextField.requestFocus();
				problemNameTextField.selectAll();
			}

			return continueAllowed;
		}
		else if(descriptionCardPanel.isVisible())
		{
			Problem problem = domain.problem;
			if(newProblem != null)
			{
				problem = newProblem;
			}
			verifyDescriptionPanel(problem);

			// Move to the next panel in the cards
			subProblemsCardPanel.setVisible(true);
			descriptionCardPanel.setVisible(false);

			// Shift the boldness in the Steps panel to the next card
			descriptionLabel.setFont(ViewPanel.FONT_PLAIN_12);
			subProblemsLabel.setFont(ViewPanel.FONT_BOLD_12);

			// Set the focus properly for the new card
			try
			{
				((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get(subProblemPanels.size() - 1)).getComponent(1)).getComponent(0)).getComponent(0)).requestFocus();
				((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get(subProblemPanels.size() - 1)).getComponent(1)).getComponent(0)).getComponent(0)).selectAll();
				subProblemsScrollablePanel.scrollRectToVisible(new Rectangle(0, subProblemsScrollablePanel.getHeight() + 150, 1, 1));
			}
			catch(ArrayIndexOutOfBoundsException ex)
			{
			}

			return true;
		}
		else if(subProblemsCardPanel.isVisible())
		{
			Problem problem = domain.problem;
			if(newProblem != null)
			{
				problem = newProblem;
			}
			verifySubProblemsPanel(problem);

			// Move to the next panel in the cards
			dataSetsCardPanel.setVisible(true);
			subProblemsCardPanel.setVisible(false);

			tipTextLabel.setText(VALUES_TIP_TEXT);

			// Shift the boldness in the Steps panel to the next card
			subProblemsLabel.setFont(ViewPanel.FONT_PLAIN_12);
			dataSetsLabel.setFont(ViewPanel.FONT_BOLD_12);

			return true;
		}
		else if(dataSetsCardPanel.isVisible())
		{
			// Move to the next panel in the cards
			informationCardPanel.setVisible(true);
			dataSetsCardPanel.setVisible(false);

			tipTextLabel.setText("");

			// Shift the boldness in the Steps panel to the next card
			dataSetsLabel.setFont(ViewPanel.FONT_PLAIN_12);
			informationLabel.setFont(ViewPanel.FONT_BOLD_12);

			// Set the focus properly for the new card
			studentNameTextField.requestFocus();
			studentNameTextField.selectAll();

			nextWizardButton.setText("Finish");

			return true;
		}
		else
		{
			Problem problem = domain.problem;
			if(newProblem != null)
			{
				problem = newProblem;
			}
			verifyInfoPanel(problem);

			boolean localEditing = false;
			if(newProblem == null)
			{
				localEditing = true;
			}
			finishNewProblemWizard(localEditing);

			return true;
		}
	}

	/**
	 * Update the value, if it exists, of the data set name in the right panel.
	 */
	private void updateLabelInRightPanel(String oldName, String name)
	{
		for(Component comp : viewPanel.dataSetContentPanel.getComponents())
		{
			if(((JLabel) comp).getText().equals(oldName))
			{
				((JLabel) comp).setText(name);
				viewPanel.dataSetContentPanel.invalidate();
				break;
			}
		}
	}

	/**
	 * Retrieve the label object in the legend with the given ID.
	 * 
	 * @param id The ID to retrieve the JLabel for from the legend.
	 * @return The label from the legend. NULL if not found.
	 */
	private JLabel findLabel(String id)
	{
		for(Component comp : viewPanel.subProblemContentPanel.getComponents())
		{
			if(((JLabel) comp).getText().equals(id))
			{
				return (JLabel) comp;
			}
		}

		return null;
	}

	/**
	 * Create a panel for a sub problem in the New Problem Wizard.
	 *
	 * @param subProblem A reference to the sub problem being created.
	 * @return The created sub problem panel that can be placed in the array and panel.
	 */
	private JPanel createSubProblemPanel(final SubProblem subProblem)
	{
		// Create objects toward the new JPanel for the sub problem
		JPanel subProblemPanel = new JPanel();
		JLabel label = new JLabel(ALPHABET[subProblemPanels.size()]);
		label.setFont(ViewPanel.FONT_BOLD_11);
		JTextArea textArea = new JTextArea();
		textArea.setFont(ViewPanel.FONT_PLAIN_12);

		JScrollPane scrollPane = new JScrollPane();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		scrollPane.setViewportView(textArea);

		JPanel conclusionPanel = new JPanel();
		JLabel colorLabel = new JLabel("Color: ");
		colorLabel.setFont(ViewPanel.FONT_BOLD_11);
		JLabel blankLabel = new JLabel(" ");
		JPanel colorPanel = new JPanel();
		colorPanel.setBackground(subProblem.getColor());
		colorPanel.setPreferredSize(new Dimension(25, 25));
		colorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		final NewProblemWizardDialog WIZARD = this;
		colorPanel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent evt)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent evt)
			{
				setCursor(Cursor.getDefaultCursor());
			}

			@Override
			public void mouseReleased(MouseEvent evt)
			{
				Color color = evt.getComponent().getBackground();
				Color newColor = ColorPicker.showDialog(WIZARD, "Select Color", color, false, viewPanel);
				if(newColor != null)
				{
					evt.getComponent().setBackground(newColor);
					subProblem.setColor(newColor);
					if(editing)
					{
						viewPanel.workspacePanel.repaint();
						findLabel(subProblem.getSubproblemID()).setForeground(newColor);
					}
				}
			}
		});

		JLabel conclusionLabel = new JLabel("Conclusion: ");
		conclusionLabel.setFont(ViewPanel.FONT_BOLD_11);
		JTextArea subConcTextArea = new JTextArea();
		subConcTextArea.setFont(ViewPanel.FONT_PLAIN_12);
		JScrollPane subConcScrollPane = new JScrollPane();
		subConcTextArea.setLineWrap(true);
		subConcTextArea.setWrapStyleWord(true);
		subConcScrollPane.setViewportView(subConcTextArea);
		subConcScrollPane.setPreferredSize(new Dimension(220, 60));
		conclusionPanel.add(colorLabel);
		conclusionPanel.add(colorPanel);
		conclusionPanel.add(blankLabel);
		conclusionPanel.add(conclusionLabel);
		conclusionPanel.add(subConcScrollPane);

		// Add items to the new JPanel
		subProblemPanel.setLayout(new BorderLayout());
		subProblemPanel.add(label, BorderLayout.NORTH);
		subProblemPanel.add(scrollPane, BorderLayout.CENTER);
		subProblemPanel.add(conclusionPanel, BorderLayout.SOUTH);
		subProblemPanel.setPreferredSize(new Dimension(410, 150));
		subProblemPanel.setMinimumSize(new Dimension(410, 150));
		subProblemPanel.setMaximumSize(new Dimension(410, 150));
		subProblemPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

		return subProblemPanel;
	}

	/**
	 * Verify components attached to the description panel.
	 *
	 * @param problem A reference to the problem currently being edited.
	 */
	private void verifyDescriptionPanel(Problem problem)
	{
		if(!problem.getStatement().equals(descriptionTextArea.getText()))
		{
			problem.setStatement(descriptionTextArea.getText());
		}
	}

	/**
	 * Verify components attached to the sub problems panel.
	 *
	 * @param problem A reference to the problem currently being edited.
	 */
	private void verifySubProblemsPanel(Problem problem)
	{
		for(int i = 0; i < problem.getSubProblemCount(); ++i)
		{
			JTextArea textArea = (JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get(i)).getComponent(1)).getComponent(0)).getComponent(0);
			Color color = ((JPanel) ((JPanel) subProblemPanels.get(i).getComponent(2)).getComponent(1)).getBackground();
			JTextArea conclusionTextArea = (JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get(i).getComponent(2)).getComponent(4)).getViewport()).getComponent(0);
			if(!problem.getSubProblem(i).getStatement().equals(textArea.getText()))
			{
				problem.getSubProblem(i).setStatement(textArea.getText());
			}
			if(!problem.getSubProblem(i).getConclusion().equals(conclusionTextArea.getText()))
			{
				problem.getSubProblem(i).setConclusion(conclusionTextArea.getText());
			}
			if(problem.getSubProblem(i).getColor().getRGB() != color.getRGB())
			{
				problem.getSubProblem(i).setColor(color);
			}
		}
	}

	/**
	 * Verify components attached to the information panel.
	 *
	 * @param problem A reference to the problem currently being edited.
	 */
	private void verifyInfoPanel(Problem problem)
	{
		if(!problem.getPersonName().equals(studentNameTextField.getText()))
		{
			problem.setPersonName(studentNameTextField.getText());
		}
		if(!problem.getShortCourse().equals(courseShortNameTextField.getText()))
		{
			problem.setShortCourse(courseShortNameTextField.getText());
		}
		if(!problem.getLongCourse().equals(courseLongNameTextField.getText()))
		{
			problem.setLongCourse(courseLongNameTextField.getText());
		}
		if(!problem.getChapter().equals(chapterTextField.getText()))
		{
			problem.setChapter(chapterTextField.getText());
		}
		if(!problem.getSection().equals(sectionTextField.getText()))
		{
			problem.setSection(sectionTextField.getText());
		}
		if(!problem.getProblemNumber().equals(problemNumberTextField.getText()))
		{
			problem.setProblemNumber(problemNumberTextField.getText());
		}
		if(!problem.getConclusion().equals(problemConclusionTextArea.getText()))
		{
			problem.setConclusion(problemConclusionTextArea.getText());
		}
	}

	/**
	 * Create a panel for display in the tabbed display for data set values.
	 *
	 * @return The panel created to be stored in the values tabbed panel.
	 */
	private JPanel createDataSetTabbedPane(final DataSet dataSet)
	{
		final JPanel valuesPanel = new JPanel();
		final ExtendedTableModel model = new ExtendedTableModel(dataSet);
		final ExtendedJTable table = new ExtendedJTable(model);
		final JScrollPane scrollPane = ExtendedJTable.createStripedJScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table.getTableHeader().setFont(ViewPanel.FONT_PLAIN_12);
		table.getTableHeader().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent evt)
			{
				if(table.getTableHeader().getCursor().getType() != Cursor.E_RESIZE_CURSOR)
				{
					int index = table.getTableHeader().columnAtPoint(evt.getPoint());
					if(index != -1)
					{
						String oldName = table.getColumnModel().getColumn(index).getHeaderValue().toString();
						Object name = Domain.showInputDialog(Domain.getTopWindow(), "Give the column a new name:", "Column Name", oldName);
						if(name != null)
						{
							if(!name.toString().equals(((ExtendedTableModel) table.getModel()).getColumnName(index)))
							{
								if(!columnNameExists((ExtendedTableModel) table.getModel(), index, name.toString()))
								{
									((ExtendedTableModel) table.getModel()).setColumn(name.toString(), index);
									table.getColumnModel().getColumn(index).setHeaderValue(name);
									table.getTableHeader().resizeAndRepaint();
								}
								else
								{
									Domain.showWarningDialog(Domain.getTopWindow(), "A column with that name already exists.", "Duplicate Column");
								}
							}
						}
					}
				}
			}
		});

		final JLabel columnsLabel = new JLabel("Columns:");
		columnsLabel.setFont(ViewPanel.FONT_PLAIN_12);
		final JSpinner columnsSpinner = new JSpinner();
		columnsSpinner.setPreferredSize(new Dimension(40, 20));
		columnsSpinner.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent evt)
			{
				if(!ignoreDataChanging)
				{
					int value = Integer.parseInt(columnsSpinner.getValue().toString());
					// Don't let column count be below 1
					if(value < 1)
					{
						columnsSpinner.setValue(1);
						value = 1;
					}
					ExtendedTableModel model = (ExtendedTableModel) table.getModel();
					// If columns were removed, loop and delete from the end
					while(value < model.getColumnCount())
					{
						model.removeColumn(table.getColumnCount() - 1);
						table.getColumnModel().removeColumn(table.getColumnModel().getColumn(table.getColumnCount() - 1));
					}
					// If columns were added, loop and add to the end
					while(value > model.getColumnCount())
					{
						int index = model.getColumnCount() + 1;
						while(columnNameExists(model, -1, "Column " + (index)))
						{
							++index;
						}

						try
						{
							DataColumn newCol = dataSet.addColumn("Column " + index);
							for(int i = 0; i < model.getRowCount(); ++i)
							{
								newCol.add(0.0);
							}

							TableColumn newColumn = new TableColumn(dataSet.getColumnCount() - 1);
							newColumn.setHeaderValue(newCol.getName());
							table.addColumn(newColumn);
						}
						// This exception should never be thrown
						catch(DuplicateNameException ex)
						{
						}
					}

					table.refreshTable();
				}
			}
		});
		final JLabel rowsLabel = new JLabel("Rows:");
		rowsLabel.setFont(ViewPanel.FONT_PLAIN_12);
		final JSpinner rowsSpinner = new JSpinner();
		rowsSpinner.setPreferredSize(new Dimension(40, 20));
		rowsSpinner.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent evt)
			{
				if(!ignoreDataChanging)
				{
					int value = Integer.parseInt(rowsSpinner.getValue().toString());
					// Don't let row count be below 1
					if(value < 1)
					{
						rowsSpinner.setValue(1);
						value = 1;
					}
					ExtendedTableModel model = (ExtendedTableModel) table.getModel();
					// If rows were removed, loop and delete from the end
					while(value < model.getRowCount())
					{
						model.removeRow(table.getRowCount() - 1);
					}
					// If rows were added, loop and add to the end
					while(value > model.getRowCount())
					{
						model.addRow();
					}

					table.refreshTable();
				}
			}
		});
		final JButton csvButton = new JButton("Import from CSV");
		csvButton.setFont(ViewPanel.FONT_PLAIN_12);
		csvButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				// Construct the folder-based open chooser dialog
				viewPanel.fileChooserDialog.setDialogTitle("Import CSV File");
				viewPanel.fileChooserDialog.setDialogType(JFileChooser.OPEN_DIALOG);
				viewPanel.fileChooserDialog.setFileFilter(csvFilter);
				viewPanel.fileChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
				viewPanel.fileChooserDialog.setCurrentDirectory(new File(Domain.lastGoodDir));
				if(new File(Domain.lastGoodDir).isFile())
				{
					viewPanel.fileChooserDialog.setSelectedFile(new File(Domain.lastGoodDir));
				}
				else
				{
					viewPanel.fileChooserDialog.setSelectedFile(new File(""));
				}
				// Display the chooser and retrieve the selected folder
				int response = viewPanel.fileChooserDialog.showOpenDialog(Domain.getTopWindow());
				if(response == JFileChooser.APPROVE_OPTION)
				{
					// If the user selected a file that exists, point the problem's location to the newly selected location
					if(viewPanel.fileChooserDialog.getSelectedFile().exists())
					{
						Domain.lastGoodDir = viewPanel.fileChooserDialog.getSelectedFile().getParent();
						setEnabled(false);
						new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								Domain.setProgressTitle("Importing");
								Domain.setProgressVisible(true);
								Domain.setProgressIndeterminate(true);
								Domain.setProgressString("");
								Domain.setProgressStatus("Importing from CSV file...");
								MainFrame.progressFrame.setAlwaysOnTop(true);

								try
								{
									ignoreDataChanging = true;
									DataSet importedDataSet = DataSet.importFile(viewPanel.fileChooserDialog.getSelectedFile().toString());

									// Clear existing data
									for(int i = dataSet.getColumnCount() - 1; 0 <= i; i--)
									{
										dataSet.removeColumn(i);
									}

									// Copy new columns
									for(int i = 0; i < importedDataSet.getColumnCount(); i++)
									{
										DataColumn importCol = importedDataSet.getColumn(i);
										DataColumn newCol = dataSet.addColumn(importCol.getName());
										newCol.setMode(importCol.getMode());
										newCol.addAll(importCol);
									}

									// Change spinners to new size
									columnsSpinner.setValue(dataSet.getColumnCount());
									rowsSpinner.setValue(dataSet.getColumnLength());

									// Change the model so that the old columns are no longer displayed
									final ExtendedTableModel newModel = new ExtendedTableModel(dataSet);
									table.setModel(newModel);
									newModel.addTableModelListener(new TableModelListener()
									{
										@Override
										public void tableChanged(TableModelEvent evt)
										{
											fireTableChanged(newModel, evt);
										}
									});

									// Set the column headers
									for(int i = 0; i < dataSet.getColumnCount(); i++)
									{
										table.getColumnModel().getColumn(i).setHeaderValue(dataSet.getColumn(i).getName());
									}

									table.setModel(newModel);
									table.refreshTable();

									ignoreDataChanging = false;
									Domain.setProgressVisible(false);
								}
								catch(MarlaException ex)
								{
									Domain.setProgressVisible(false);
									Domain.showWarningDialog(Domain.getTopWindow(), ex.getMessage(), Domain.prettyExceptionDetails(ex), "Load Failed");
								}
								finally
								{
									Domain.setProgressIndeterminate(false);
									MainFrame.progressFrame.setAlwaysOnTop(false);
									setEnabled(true);
								}
							}
						}).start();
					}
				}
			}
		});

		GroupLayout valuesPanelLayout = new GroupLayout(valuesPanel);
		valuesPanel.setLayout(valuesPanelLayout);
		valuesPanelLayout.setHorizontalGroup(
				valuesPanelLayout.createParallelGroup(GroupLayout.LEADING).add(valuesPanelLayout.createSequentialGroup().addContainerGap().add(valuesPanelLayout.createParallelGroup(GroupLayout.LEADING).add(scrollPane, GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE).add(valuesPanelLayout.createSequentialGroup().add(columnsLabel).addPreferredGap(LayoutStyle.RELATED).add(columnsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).add(18, 18, 18).add(rowsLabel).addPreferredGap(LayoutStyle.RELATED).add(rowsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.RELATED, 108, Short.MAX_VALUE).add(csvButton))).addContainerGap()));
		valuesPanelLayout.setVerticalGroup(
				valuesPanelLayout.createParallelGroup(GroupLayout.LEADING).add(valuesPanelLayout.createSequentialGroup().add(scrollPane, GroupLayout.PREFERRED_SIZE, 238, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.RELATED).add(valuesPanelLayout.createParallelGroup(GroupLayout.LEADING).add(csvButton).add(valuesPanelLayout.createParallelGroup(GroupLayout.LEADING, false).add(columnsLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(columnsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).add(valuesPanelLayout.createParallelGroup(GroupLayout.LEADING, false).add(rowsLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(rowsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		return valuesPanel;
	}

	/**
	 * Empty all text fields of text.
	 */
	protected void emptyTextFields()
	{
		problemNameTextField.setText("");
		problemLocationTextField.setText("");
		descriptionTextArea.setText("");
	}

	/**
	 * Check if the given column name exists (ignoring the current index, which is itself).
	 * @param model Table model to check names against.
	 * @param curIndex The current index to be ignored.
	 * @param name The name to check for.
	 * @return True if the column name already exists, false otherwise.
	 */
	private boolean columnNameExists(ExtendedTableModel model, int curIndex, String name)
	{
		for(int i = 0; i < model.getColumnCount(); ++i)
		{
			if(i == curIndex)
			{
				continue;
			}
			if(model.getColumnName(i).equals(name))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Ensure the new value for the table change event is valid, otherwise revert it.
	 *
	 * @param model The model for the data set.
	 * @param evt The table change event.
	 */
	private void fireTableChanged(ExtendedTableModel model, TableModelEvent evt)
	{
		if(!ignoreDataChanging && !addingDataSet && !changingDataSet)
		{
			// Ensure the new value is a valid double, otherwise revert
			try
			{
				String value = model.getValueAt(evt.getFirstRow(), evt.getColumn()).toString();
				changingDataSet = true;
				model.setValueAt(value, evt.getFirstRow(), evt.getColumn());
				changingDataSet = false;
			}
			catch(NullPointerException ex)
			{
			}
		}
	}

	/**
	 * Initialized the New Problem Wizard before a launch.
	 *
	 * @param editing True when editing a problem, false when creating a new one.
	 */
	protected void initializeNewProblemWizard(boolean editing)
	{
		ignoreDataChanging = true;

		this.editing = editing;
		newProblemOverwrite = false;

		// Set the first card panel as the only visible
		welcomeCardPanel.setVisible(true);
		nameAndLocationCardPanel.setVisible(false);
		descriptionCardPanel.setVisible(false);
		subProblemsCardPanel.setVisible(false);
		dataSetsCardPanel.setVisible(false);
		informationCardPanel.setVisible(false);
		// Set the proper label to bold
		welcomeLabel.setFont(ViewPanel.FONT_BOLD_12);
		nameAndLocationLabel.setFont(ViewPanel.FONT_PLAIN_12);
		descriptionLabel.setFont(ViewPanel.FONT_PLAIN_12);
		subProblemsLabel.setFont(ViewPanel.FONT_PLAIN_12);
		dataSetsLabel.setFont(ViewPanel.FONT_PLAIN_12);
		informationLabel.setFont(ViewPanel.FONT_PLAIN_12);
		// Set forward/backward button states
		backWizardButton.setEnabled(false);
		nextWizardButton.setEnabled(true);
		nextWizardButton.setText("Next >");
		// Set properties in the sub problems panel
		subProblemPanels.clear();
		subProblemsScrollablePanel.removeAll();
		subProblemsScrollablePanel.invalidate();
		addSubProblemButton.setEnabled(true);
		removeSubProblemButton.setEnabled(true);
		// Set properties for the values tabs
		dataSetTabbedPane.removeAll();

		if(editing)
		{
			setTitle("Edit Problem");
			welcomeTextLabel.setText(ViewPanel.welcomeEditText);
		}
		else
		{
			setTitle("New Problem Wizard");
			welcomeTextLabel.setText(ViewPanel.welcomeNewText);
		}

		setNewProblemWizardDefaultValues(editing);

		ignoreDataChanging = false;
	}

	/**
	 * Display the New Problem Wizard (only call this after it has been initialized).
	 */
	protected void launchNewProblemWizard()
	{
		// Pack and show the New Problem Wizard dialog
		pack();
		setLocationRelativeTo(viewPanel);
		setVisible(true);
	}

	/**
	 * Sets the default values for components in the New Problem Wizard.
	 *
	 * @param editing True when editing a problem, false when creating a new one.
	 */
	protected void setNewProblemWizardDefaultValues(boolean editing)
	{
		if(!editing)
		{
			// Create the new Problem object with default values
			newProblem = new Problem("");
			newProblem.setFileName(Domain.lastGoodDir + "/" + "New Problem.marla");
			DataSet dataSet = null;
			try
			{
				dataSet = new DataSet("Data Set 1");
				newProblem.addData(dataSet);
				dataSet.addColumn("Column 1");
				dataSet.getColumn(0).add(0.0);
				dataSet.getColumn(0).add(0.0);
				dataSet.getColumn(0).add(0.0);
				dataSet.getColumn(0).add(0.0);
				dataSet.getColumn(0).add(0.0);
				dataSet.addColumn("Column 2");
				dataSet.getColumn(1).add(0.0);
				dataSet.getColumn(1).add(0.0);
				dataSet.getColumn(1).add(0.0);
				dataSet.getColumn(1).add(0.0);
				dataSet.getColumn(1).add(0.0);
				dataSet.addColumn("Column 3");
				dataSet.getColumn(2).add(0.0);
				dataSet.getColumn(2).add(0.0);
				dataSet.getColumn(2).add(0.0);
				dataSet.getColumn(2).add(0.0);
				dataSet.getColumn(2).add(0.0);

				dataSet.setBounds(200, 20, dataSet.getPreferredSize().width, dataSet.getPreferredSize().height);
			}
			// Will never be thrown at this point
			catch(DuplicateNameException ex)
			{
				Domain.logger.add(ex);
			}

			// Set problem defaults for name and location
			problemNameTextField.setText("New Problem");
			problemNameTextField.setEnabled(true);
			problemLocationTextField.setText(Domain.lastGoodDir);
			browseButton.setEnabled(true);
			descriptionTextArea.setText("");
			studentNameTextField.setText(newProblem.getPersonName());
			courseShortNameTextField.setText(newProblem.getShortCourse());
			courseLongNameTextField.setText(newProblem.getLongCourse());
			chapterTextField.setText(newProblem.getChapter());
			sectionTextField.setText(newProblem.getSection());
			problemConclusionTextArea.setText(newProblem.getConclusion());
			problemNumberTextField.setText(newProblem.getProblemNumber());

			// By default, new problems have three columns and five rows
			dataSetTabbedPane.add(dataSet.getName(), createDataSetTabbedPane(dataSet));
			int columns = dataSet.getColumnCount();
			int rows = dataSet.getColumnLength();

			// Add minimum columns to the table model
			ExtendedJTable table = (ExtendedJTable) ((JViewport) ((JScrollPane) ((JPanel) dataSetTabbedPane.getComponent(0)).getComponent(0)).getComponent(2)).getComponent(0);
			final ExtendedTableModel newModel = new ExtendedTableModel(dataSet);
			newModel.addTableModelListener(new TableModelListener()
			{
				@Override
				public void tableChanged(TableModelEvent evt)
				{
					fireTableChanged(newModel, evt);
				}
			});
			DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel();
			for(int i = 0; i < columns; ++i)
			{
				newColumnModel.addColumn(new TableColumn());
			}
			table.setColumnModel(newColumnModel);

			table.setModel(newModel);
			table.refreshTable();

			// Wait to change spinners _after_ the model is set or we'll double the rows
			((JSpinner) ((JPanel) dataSetTabbedPane.getComponent(0)).getComponent(2)).setValue(columns);
			((JSpinner) ((JPanel) dataSetTabbedPane.getComponent(0)).getComponent(4)).setValue(rows);
		}
		else
		{
			newProblem = null;

			// Set problem defaults for name and location
			problemNameTextField.setText(domain.problem.getFileName().substring(domain.problem.getFileName().lastIndexOf(System.getProperty("file.separator")) + 1, domain.problem.getFileName().lastIndexOf(".")));
			problemNameTextField.setEnabled(false);
			problemLocationTextField.setText(new File(domain.problem.getFileName()).getAbsoluteFile().getParent());
			browseButton.setEnabled(false);
			descriptionTextArea.setText(domain.problem.getStatement());
			studentNameTextField.setText(domain.problem.getPersonName());
			courseShortNameTextField.setText(domain.problem.getShortCourse());
			courseLongNameTextField.setText(domain.problem.getLongCourse());
			chapterTextField.setText(domain.problem.getChapter());
			sectionTextField.setText(domain.problem.getSection());
			problemConclusionTextArea.setText(domain.problem.getConclusion());
			problemNumberTextField.setText(domain.problem.getProblemNumber());

			// Add sub problems to the panel
			for(int i = 0; i < domain.problem.getSubProblemCount(); ++i)
			{
				JPanel panel = createSubProblemPanel(domain.problem.getSubProblem(i));

				JTextArea textArea = (JTextArea) ((JViewport) ((JScrollPane) panel.getComponent(1)).getComponent(0)).getComponent(0);
				JPanel colorPanel = (JPanel) ((JPanel) panel.getComponent(2)).getComponent(1);
				JTextArea conclusionTextArea = (JTextArea) ((JViewport) ((JScrollPane) ((JPanel) panel.getComponent(2)).getComponent(4)).getViewport()).getComponent(0);
				textArea.setText(domain.problem.getSubProblem(i).getStatement());
				conclusionTextArea.setText(domain.problem.getSubProblem(i).getConclusion());
				colorPanel.setBackground(domain.problem.getSubProblem(i).getColor());

				// Add the JPanel to the list of sub problem JPanels
				subProblemPanels.add(panel);

				// Add the JPanel to the New Problem Wizard
				subProblemsScrollablePanel.add(panel);
			}
			if(subProblemPanels.size() > 0)
			{
				removeSubProblemButton.setEnabled(true);
			}
			else
			{
				removeSubProblemButton.setEnabled(false);
			}
			if(subProblemPanels.size() < 26)
			{
				addSubProblemButton.setEnabled(true);
			}
			else
			{
				addSubProblemButton.setEnabled(false);
			}
			subProblemsScrollablePanel.invalidate();
			subProblemsScrollablePanel.scrollRectToVisible(new Rectangle(0, subProblemsScrollablePanel.getHeight(), 1, 1));

			for(int i = 0; i < domain.problem.getDataCount(); ++i)
			{
				DataSet dataSet = domain.problem.getData(i);
				JPanel panel = createDataSetTabbedPane(dataSet);
				dataSetTabbedPane.add(dataSet.getName(), panel);
				int columns = dataSet.getColumnCount();
				int rows = dataSet.getColumnLength();
				((JSpinner) panel.getComponent(2)).setValue(columns);
				((JSpinner) panel.getComponent(4)).setValue(rows);

				// Add minimum columns to the table model
				ExtendedJTable table = (ExtendedJTable) ((JViewport) ((JScrollPane) panel.getComponent(0)).getComponent(2)).getComponent(0);
				final ExtendedTableModel newModel = new ExtendedTableModel(dataSet);
				newModel.addTableModelListener(new TableModelListener()
				{
					@Override
					public void tableChanged(TableModelEvent evt)
					{
						fireTableChanged(newModel, evt);
					}
				});

				DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel();
				for(int j = 0; j < columns; ++j)
				{
					newColumnModel.addColumn(new TableColumn());
				}
				table.setColumnModel(newColumnModel);

				table.setModel(newModel);
				table.refreshTable();
			}
		}

		if(dataSetTabbedPane.getTabCount() > 0)
		{
			removeDataSetButton.setEnabled(true);
		}
		else
		{
			removeDataSetButton.setEnabled(false);
		}
	}

	/**
	 * The New Problem Wizard is complete, so create the new problem and close
	 * the New Problem Wizard.
	 *
	 * @param editing True when editing a problem, false when creating a new one.
	 */
	private void finishNewProblemWizard(boolean editing)
	{
		// Close the current or old problem, if one is open
		viewPanel.closeProblem(editing, false);

		// Use values from the New Problem Wizard to construct a new problem
		if(newProblem != null)
		{
			repositionDataSets(newProblem);
			domain.problem = newProblem;
			newProblem = null;
		}

		// Open data stored in the problem currently
		viewPanel.openProblem(editing, false);

		// Save the problem immedietly
		if(!editing)
		{
			domain.save();
		}

		closeWizardButtonActionPerformed(null);
	}

	/**
	 * Check if the given data set name exists (ignoring the current index, which is itself).
	 *
	 * @param curIndex The current index to be ignored.
	 * @param name The name to check for.
	 * @return True if the data set name already exists, false otherwise.
	 */
	private boolean dataSetNameExists(int curIndex, String name)
	{
		for(int i = 0; i < dataSetTabbedPane.getTabCount(); ++i)
		{
			if(i == curIndex)
			{
				continue;
			}
			if(dataSetTabbedPane.getTitleAt(i).equals(name))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Assuming the New Problem Wizard is already launched, this function will
	 * move to edit sub problems for the currently displayed problem.
	 */
	protected void editSubProblems()
	{
		initializeNewProblemWizard(true);

		// Transition to the values card panel
		boolean continueAllowed = goNext();
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}

		launchNewProblemWizard();
	}

	/**
	 * Edit the current problem.
	 */
	protected void editProblem()
	{
		initializeNewProblemWizard(true);

		// Transition to the values card panel
		boolean continueAllowed = goNext();
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}

		launchNewProblemWizard();
	}

	/**
	 * Assuming the New Problem Wizard is already launched, this function will
	 * move to add a new data set in the New Problem Wizard.
	 */
	protected void addDataSet()
	{
		initializeNewProblemWizard(true);

		// Transition to the values card panel
		boolean continueAllowed = goNext();
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}

		// Add a new data set
		addDataSetButtonActionPerformed(null);

		launchNewProblemWizard();
	}

	/**
	 * Assuming the New Problem Wizard is already launched, this function will
	 * move to edit the currently selected data set.
	 *
	 * @param dataSet The data set to be edited, null if no data set should be edited.
	 */
	protected void editDataSet(DataSet dataSet)
	{
		initializeNewProblemWizard(true);

		// Transition to the values card panel
		boolean continueAllowed = goNext();
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}

		if(dataSet != null)
		{
			// Set to the editing the new data set
			try
			{
				dataSetTabbedPane.setSelectedIndex(domain.problem.getDataIndex(dataSet.getName()));
			}
			catch(DataNotFoundException ex)
			{
				Domain.logger.add(ex);
			}
		}

		launchNewProblemWizard();
	}

	/**
	 * Assuming the New Problem Wizard is already launched, this function will
	 * move to edit the conclusion of the current problem.
	 */
	protected void editConclusion()
	{
		initializeNewProblemWizard(true);

		// Transition to the values card panel
		boolean continueAllowed = goNext();
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}
		if(continueAllowed)
		{
			continueAllowed = goNext();
		}

		// Select the conclusion text area
		problemConclusionTextArea.requestFocus();
		problemConclusionTextArea.selectAll();

		launchNewProblemWizard();
	}

	/**
	 * Repositions all datasets in a problem. Only recommended for new problems
	 * @param problem Problem to move datasets around in
	 */
	protected void repositionDataSets(Problem problem)
	{
		// Position all datasets relative to each other
		// Get the total width we're going to need to cover
		List<DataSet> data = problem.getVisibleDataSets();
		int dsCount = data.size();
		int[] widths = new int[dsCount];
		for(int i = 0; i < dsCount; i++)
		{
			DataSet ds = data.get(i);

			// Set the label for the data source and get its width
			ds.setFont(ViewPanel.workspaceFontBold);
			ds.setText("<html>" + ds.getDisplayString(viewPanel.abbreviated) + "</html>");
			ds.setSize(ds.getPreferredSize());

			widths[i] = ds.getWidth();
		}

		// Figure out where the columns should start based on our center
		int wsWidth = viewPanel.workspacePanel.getWidth();
		int wsCenterX = wsWidth / 2;

		// Find the median value
		int spaceWidth = 20;
		int halfWidth = 0;
		if(dsCount == 1)
		{
			halfWidth = widths[0] / 2;
		}
		else if(dsCount % 2 == 0)
		{
			// Even number of datasets, balance them aronud center
			for(int i = 0; i < dsCount / 2; i++)
			{
				halfWidth += widths[i] + spaceWidth;
			}

			// Eliminate half of the middle spacing
			halfWidth -= spaceWidth / 2;
		}
		else
		{
			// Odd number of datasets, center the middle one
			for(int i = 0; i < dsCount / 2; i++)
			{
				halfWidth += widths[i] + spaceWidth;
			}

			// And add enough to move through half the middle column
			halfWidth += widths[dsCount / 2] / 2;
		}

		// Put datasets a third of the way down the workspace
		int y = viewPanel.workspacePanel.getHeight() / 3;

		int previousLeftX = wsCenterX - halfWidth;
		for(int i = 0; i < dsCount; i++)
		{
			DataSet ds = data.get(i);

			ds.setLocation(previousLeftX, y);
			previousLeftX += widths[i] + spaceWidth;

			viewPanel.rebuildTree(ds);
		}
	}
}
