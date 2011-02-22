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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import problem.DataColumn;
import problem.DataNotFoundException;
import problem.DataSet;
import problem.DuplicateNameException;
import problem.MarlaException;
import problem.Problem;

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
	/** The final reference to this dialog object.*/
	private final NewProblemWizardDialog NEW_PROBLEM_WIZARD = this;
	/** The list in the New Problem Wizard of sub problems within the current problem.*/
	private ArrayList<JPanel> subProblemPanels = new ArrayList<JPanel> ();
	/** True if the New Problem Wizard is being opened and actions should be ignored.*/
	private boolean openCloseWizard = false;
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

	/**
	 * Construct the New Problem Wizard dialog.
	 *
	 * @param viewPanel A reference to the view panel.
	 */
	public NewProblemWizardDialog(ViewPanel viewPanel, Domain domain)
	{
		super (viewPanel.mainFrame, viewPanel);
		this.domain = domain;

		initComponents ();

		dataSetTabbedPane.addMouseListener (new MouseAdapter ()
		{
			@Override
			public void mouseClicked(MouseEvent evt)
			{
				for (int i = 0; i < dataSetTabbedPane.getTabCount (); ++i)
				{
					if (dataSetTabbedPane.getUI ().getTabBounds (dataSetTabbedPane, i).contains (evt.getPoint ()) && evt.getClickCount () == 2)
					{
						Object name = JOptionPane.showInputDialog (NEW_PROBLEM_WIZARD,
																   "Give the data set a new name:",
																   "Data Set Name",
																   JOptionPane.QUESTION_MESSAGE,
																   null,
																   null,
																   dataSetTabbedPane.getTitleAt (i));
						if (name != null)
						{
							if (!name.toString ().equals (dataSetTabbedPane.getTitleAt (i)))
							{
								if (!dataSetNameExists (i, name.toString ()))
								{
									dataSetTabbedPane.setTitleAt (i, name.toString ());
								}
								else
								{
									JOptionPane.showMessageDialog (NEW_PROBLEM_WIZARD, "A column with that name already exists.", "Duplicate Column", JOptionPane.WARNING_MESSAGE);
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        stepsPanel.setBackground(new java.awt.Color(240, 239, 239));
        stepsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        stepsPanel.setLayout(null);

        stepsLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        stepsLabel.setText("Steps");
        stepsPanel.add(stepsLabel);
        stepsLabel.setBounds(10, 10, 170, 15);

        welcomeLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        welcomeLabel.setText("1. Welcome");
        stepsPanel.add(welcomeLabel);
        welcomeLabel.setBounds(20, 40, 140, 15);

        nameAndLocationLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        nameAndLocationLabel.setText("2. Name and Location");
        stepsPanel.add(nameAndLocationLabel);
        nameAndLocationLabel.setBounds(20, 60, 160, 15);

        descriptionLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        descriptionLabel.setText("3. Description");
        stepsPanel.add(descriptionLabel);
        descriptionLabel.setBounds(20, 80, 140, 15);

        valuesLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        valuesLabel.setText("5. Values");
        stepsPanel.add(valuesLabel);
        valuesLabel.setBounds(20, 120, 140, 15);

        stepsLineLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        stepsLineLabel.setText("_____________________");
        stepsPanel.add(stepsLineLabel);
        stepsLineLabel.setBounds(10, 10, 170, 20);

        subProblemsLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        subProblemsLabel.setText("4. Sub Problems");
        stepsPanel.add(subProblemsLabel);
        subProblemsLabel.setBounds(20, 100, 140, 15);

        wizardCardPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        wizardCardPanel.setLayout(new java.awt.CardLayout());

        welcomeCardPanel.setLayout(null);

        welcomeWizardLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        welcomeWizardLabel.setText("Welcome");
        welcomeCardPanel.add(welcomeWizardLabel);
        welcomeWizardLabel.setBounds(10, 10, 430, 15);

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
                .addContainerGap(256, Short.MAX_VALUE))
        );
        welcomePanelLayout.setVerticalGroup(
            welcomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(welcomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(welcomeTextLabel)
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
        nameAndLocationWizardLabel.setBounds(10, 10, 130, 15);

        problemNameLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        problemNameLabel.setText("Problem Name:");

        problemNameTextField.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
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
                        .addComponent(problemNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE))
                    .addGroup(nameAndLocationPanelLayout.createSequentialGroup()
                        .addComponent(problemLocationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(problemLocationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))
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
                .addContainerGap(207, Short.MAX_VALUE))
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
        descriptionWizardLabel.setBounds(10, 10, 80, 15);

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
                .addComponent(descroptionScollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
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
        subProblemsWizardLabel.setBounds(10, 10, 130, 15);

        subProblemsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        subProblemsScollablePanel.setLayout(new javax.swing.BoxLayout(subProblemsScollablePanel, javax.swing.BoxLayout.PAGE_AXIS));
        subProblemsScrollPane.setViewportView(subProblemsScollablePanel);

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
        addSubProblemButton.setBounds(285, 330, 70, 27);

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
        removeSubProblemButton.setBounds(370, 330, 90, 27);

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
        valuesWizardLabel.setBounds(10, 10, 50, 15);

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
        addDataSetButton.setBounds(285, 330, 70, 27);

        removeDataSetButton.setFont(new java.awt.Font("Verdana", 0, 12));
        removeDataSetButton.setText("Remove");
        removeDataSetButton.setToolTipText("Remove the last data set");
        removeDataSetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDataSetButtonActionPerformed(evt);
            }
        });
        valuesCardPanel.add(removeDataSetButton);
        removeDataSetButton.setBounds(370, 330, 90, 27);

        wizardCardPanel.add(valuesCardPanel, "card2");

        closeWizardButton.setFont(new java.awt.Font("Verdana", 0, 12));
        closeWizardButton.setText("Close");
        closeWizardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeWizardButtonActionPerformed(evt);
            }
        });

        nextWizardButton.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
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
                .addContainerGap(522, Short.MAX_VALUE)
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
            .addGap(0, 789, Short.MAX_VALUE)
            .addGroup(newProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(newProblemPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(newProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(newProblemPanelLayout.createSequentialGroup()
                            .addComponent(stepsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(wizardCardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE))
                        .addComponent(wizardControlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap()))
        );
        newProblemPanelLayout.setVerticalGroup(
            newProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 455, Short.MAX_VALUE)
            .addGroup(newProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(newProblemPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(newProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(wizardCardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                        .addComponent(stepsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(wizardControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 789, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(newProblemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 455, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(newProblemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
		// Construct the folder-based open chooser dialog
		VIEW_PANEL.openChooserDialog.setFileFilter (VIEW_PANEL.defaultFilter);
		VIEW_PANEL.openChooserDialog.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
		VIEW_PANEL.openChooserDialog.setSelectedFile (new File (""));
		VIEW_PANEL.openChooserDialog.setCurrentDirectory (new File (domain.lastGoodDir));
		// Display the chooser and retrieve the selected folder
		int response = VIEW_PANEL.openChooserDialog.showOpenDialog (this);
		if (response == JFileChooser.APPROVE_OPTION)
		{
			// If the user selected a folder that exists, point the problem's location to the newly selected location
			if (VIEW_PANEL.openChooserDialog.getSelectedFile ().exists ())
			{
				File file = VIEW_PANEL.openChooserDialog.getSelectedFile ();
				if (file.isDirectory ())
				{
					domain.lastGoodDir = file.toString ();
				}
				else
				{
					domain.lastGoodDir = file.toString ().substring (0, file.toString ().lastIndexOf (File.separatorChar));
				}
				problemLocationTextField.setText (domain.lastGoodDir);
			}
		}
}//GEN-LAST:event_browseButtonActionPerformed

	private void addSubProblemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSubProblemButtonActionPerformed
		// Create objects toward the new JPanel for the sub problem
		JPanel subProblemPanel = new JPanel ();
		JLabel label = new JLabel (ALPHABET[subProblemPanels.size ()]);
		label.setFont (new Font ("Verdana", Font.BOLD, 11));
		JTextArea textArea = new JTextArea ();
		if (newProblem != null)
		{
			newProblem.addSubProblem (ALPHABET[newProblem.getSubProblemCount ()], "");
		}
		else
		{
			domain.problem.addSubProblem (ALPHABET[domain.problem.getSubProblemCount ()], "");
		}
		JScrollPane scrollPane = new JScrollPane ();
		textArea.setLineWrap (true);
		textArea.setWrapStyleWord (true);
		scrollPane.setViewportView (textArea);

		// Add items to the new JPanel
		subProblemPanel.setLayout (new BorderLayout ());
		subProblemPanel.add (label, BorderLayout.NORTH);
		subProblemPanel.add (scrollPane, BorderLayout.CENTER);
		subProblemPanel.setPreferredSize (new Dimension (410, 100));
		subProblemPanel.setMinimumSize (new Dimension (410, 100));
		subProblemPanel.setMaximumSize (new Dimension (410, 100));

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
		subProblemsScollablePanel.scrollRectToVisible (new Rectangle (0, subProblemsScollablePanel.getHeight () + 100, 1, 1));

		textArea.requestFocus ();
}//GEN-LAST:event_addSubProblemButtonActionPerformed

	private void removeSubProblemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSubProblemButtonActionPerformed
		// Remove the JPanel from the list of sub problems and from the New Problem Wizard
		JPanel panel = subProblemPanels.remove (subProblemPanels.size () - 1);
		if (newProblem != null)
		{
			newProblem.removeSubProblem (newProblem.getSubProblem (newProblem.getSubProblemCount () - 1));
		}
		else
		{
			domain.problem.removeSubProblem (domain.problem.getSubProblem (domain.problem.getSubProblemCount () - 1));
		}
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
			((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (subProblemPanels.size () - 1)).getComponent (1)).getComponent (0)).getComponent (0)).requestFocus ();
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
		}
}//GEN-LAST:event_removeSubProblemButtonActionPerformed

	private void addDataSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDataSetButtonActionPerformed
		addingDataSet = true;

		final Problem problem;
		if (newProblem != null)
		{
			problem = newProblem;
		}
		else
		{
			problem = domain.problem;
		}

		int index = dataSetTabbedPane.getTabCount () + 1;
		while (dataSetNameExists (-1, "Data Set " + index))
		{
			++index;
		}
		DataSet dataSet = null;
		try
		{
			dataSet = problem.addData (new DataSet ("Data Set " + index));
			dataSet.addColumn ("Column 1");
			dataSet.getColumn (0).add (0.0);
			dataSet.getColumn (0).add (0.0);
			dataSet.getColumn (0).add (0.0);
			dataSet.getColumn (0).add (0.0);
			dataSet.getColumn (0).add (0.0);
			dataSet.addColumn ("Column 2");
			dataSet.getColumn (1).add (0.0);
			dataSet.getColumn (1).add (0.0);
			dataSet.getColumn (1).add (0.0);
			dataSet.getColumn (1).add (0.0);
			dataSet.getColumn (1).add (0.0);
			dataSet.addColumn ("Column 3");
			dataSet.getColumn (2).add (0.0);
			dataSet.getColumn (2).add (0.0);
			dataSet.getColumn (2).add (0.0);
			dataSet.getColumn (2).add (0.0);
			dataSet.getColumn (2).add (0.0);
		}
		// This check has already occured, so this exception will never be thrown
		catch (DuplicateNameException ex)
		{
		}

		problem.addData (dataSet);
		int x = 200;
		int y = 20;
		int count = problem.getDataCount () - 1;
		if (count > 0)
		{
			x = problem.getData (count - 1).getX () + 150;
		}
		problem.getData (count).setBounds (x, y, problem.getData (count).getPreferredSize ().width, problem.getData (count).getPreferredSize ().height);

		JPanel panel = createValuesTabbedPanel (problem);
		dataSetTabbedPane.add ("Data Set " + index, panel);
		dataSetTabbedPane.setSelectedIndex (dataSetTabbedPane.getTabCount () - 1);
		int columns = 3;
		int rows = 5;
		((JSpinner) panel.getComponent (2)).setValue (columns);
		((JSpinner) panel.getComponent (4)).setValue (rows);

		// Add minimum columns to the table model
		JTable table = ((JTable) ((JViewport) ((JScrollPane) panel.getComponent (0)).getComponent (0)).getComponent (0));
		final ExtendedTableModel newModel = new ExtendedTableModel ();
		newModel.addTableModelListener (new TableModelListener ()
		{
			@Override
			public void tableChanged(TableModelEvent evt)
			{
				fireTableChanged (problem, newModel, evt);
			}
		});
		DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel ();
		for (int i = 0; i < columns; ++i)
		{
			index = i + 1;
			while (columnNameExists (newModel, i, "Column " + (index)))
			{
				++index;
			}
			newColumnModel.addColumn (new TableColumn ());
			newModel.addColumn ("Column " + index);
		}
		table.setColumnModel (newColumnModel);
		// Add minimum rows to the table model
		for (int i = 0; i < rows; ++i)
		{
			newModel.addRow (new Object[columns]);
		}
		table.setModel (newModel);
		table.updateUI ();
		table.getTableHeader ().resizeAndRepaint ();

		if (dataSetTabbedPane.getTabCount () == 1)
		{
			removeDataSetButton.setEnabled (true);
		}

		addingDataSet = false;
}//GEN-LAST:event_addDataSetButtonActionPerformed

	private void removeDataSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDataSetButtonActionPerformed
		if (newProblem != null)
		{
			newProblem.removeData (newProblem.getData (dataSetTabbedPane.getSelectedIndex ()));
		}
		else
		{
			domain.problem.removeData (domain.problem.getData (dataSetTabbedPane.getSelectedIndex ()));
		}
		dataSetTabbedPane.remove (dataSetTabbedPane.getSelectedIndex ());
		if (dataSetTabbedPane.getTabCount () == 0)
		{
			removeDataSetButton.setEnabled (false);
		}
}//GEN-LAST:event_removeDataSetButtonActionPerformed

	private void closeWizardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeWizardButtonActionPerformed
		openCloseWizard = true;

		dispose ();
		VIEW_PANEL.requestFocus ();

		newProblem = null;
		openCloseWizard = false;
}//GEN-LAST:event_closeWizardButtonActionPerformed

	private void nextWizardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextWizardButtonActionPerformed
		if (welcomeCardPanel.isVisible ())
		{
			// Move to the next panel in the cards
			nameAndLocationCardPanel.setVisible (true);
			welcomeCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the next card
			welcomeLabel.setFont (ViewPanel.fontPlain12);
			nameAndLocationLabel.setFont (ViewPanel.fontBold12);

			// Set the focus properly for the new card
			problemNameTextField.requestFocus ();
			problemNameTextField.selectAll ();

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
					write.close ();
					file.delete ();
				}
				catch (IOException ex)
				{
					JOptionPane.showMessageDialog (this, "The problem name you have given contains characters that are\n"
														 + "not legal in a filename. Please rename your file and avoid\n"
														 + "using special characters.",
												   "Invalid Filename",
												   JOptionPane.WARNING_MESSAGE);

					continueAllowed = false;
				}
			}
			// Ensure the problem name given does not match an already existing file
			if (continueAllowed && file.exists () && !newProblemOverwrite && newProblem != null)
			{
				int response = JOptionPane.showConfirmDialog (this, "The given problem name already exists as a file\n"
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
				if (newProblem != null)
				{
					try
					{
						if (!file.getCanonicalPath ().equals (newProblem.getFileName ()))
						{
							newProblem.setFileName (file.getCanonicalPath ());
						}
					}
					catch (IOException ex)
					{
						Domain.logger.add (ex);
					}
				}

				// Move to the next panel in the cards
				descriptionCardPanel.setVisible (true);
				nameAndLocationCardPanel.setVisible (false);

				// Shift the boldness in the Steps panel to the next card
				nameAndLocationLabel.setFont (ViewPanel.fontPlain12);
				descriptionLabel.setFont (ViewPanel.fontBold12);

				// Set the focus properly for the new card
				descriptionTextArea.requestFocus ();
				descriptionTextArea.selectAll ();
			}
			else
			{
				// Since continuation wasn't allowed, the user needs to correct the problem name
				problemNameTextField.requestFocus ();
				problemNameTextField.selectAll ();
			}
		}
		else if (descriptionCardPanel.isVisible ())
		{
			if (newProblem != null)
			{
				if (!newProblem.getStatement ().equals (descriptionTextArea.getText ()));
				{
					newProblem.setStatement (descriptionTextArea.getText ());
				}
			}
			else
			{
				if (!domain.problem.getStatement ().equals (descriptionTextArea.getText ()));
				{
					domain.problem.setStatement (descriptionTextArea.getText ());
				}
			}

			// Move to the next panel in the cards
			subProblemsCardPanel.setVisible (true);
			descriptionCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the next card
			descriptionLabel.setFont (ViewPanel.fontPlain12);
			subProblemsLabel.setFont (ViewPanel.fontBold12);

			try
			{
				((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (subProblemPanels.size () - 1)).getComponent (1)).getComponent (0)).getComponent (0)).requestFocus ();
				((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (subProblemPanels.size () - 1)).getComponent (1)).getComponent (0)).getComponent (0)).selectAll ();
				subProblemsScollablePanel.scrollRectToVisible (new Rectangle (0, subProblemsScollablePanel.getHeight (), 1, 1));
			}
			catch (ArrayIndexOutOfBoundsException ex)
			{
			}
		}
		else if (subProblemsCardPanel.isVisible ())
		{
			if (newProblem != null)
			{
				for (int i = 0; i < newProblem.getSubProblemCount (); ++i)
				{
					if (!newProblem.getSubProblem (i).getStatement ().equals (((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (i)).getComponent (1)).getComponent (0)).getComponent (0)).getText ()))
					{
						newProblem.getSubProblem (i).setStatement (((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (i)).getComponent (1)).getComponent (0)).getComponent (0)).getText ());
					}
				}
			}
			else
			{
				for (int i = 0; i < domain.problem.getSubProblemCount (); ++i)
				{
					if (!domain.problem.getSubProblem (i).getStatement ().equals (((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (i)).getComponent (1)).getComponent (0)).getComponent (0)).getText ()))
					{
						domain.problem.getSubProblem (i).setStatement (((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (i)).getComponent (1)).getComponent (0)).getComponent (0)).getText ());
					}
				}
			}

			// Move to the next panel in the cards
			valuesCardPanel.setVisible (true);
			subProblemsCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the next card
			subProblemsLabel.setFont (ViewPanel.fontPlain12);
			valuesLabel.setFont (ViewPanel.fontBold12);

			nextWizardButton.setText ("Finish");
		}
		else
		{
			boolean editing = false;
			if (newProblem == null)
			{
				editing = true;
			}
			finishNewProblemWizard (editing);
		}
}//GEN-LAST:event_nextWizardButtonActionPerformed

	private void backWizardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backWizardButtonActionPerformed
		if (nameAndLocationCardPanel.isVisible ())
		{
			if (newProblem != null)
			{
				try
				{
					File file = new File (problemLocationTextField.getText (), problemNameTextField.getText ());
					if (!file.getCanonicalPath ().equals (newProblem.getFileName ()))
					{
						newProblem.setFileName (file.getCanonicalPath ());
					}
				}
				catch (IOException ex)
				{
					Domain.logger.add (ex);
				}
			}

			// Move to the previous panel in the cards
			welcomeCardPanel.setVisible (true);
			nameAndLocationCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the previous card
			welcomeLabel.setFont (ViewPanel.fontBold12);
			nameAndLocationLabel.setFont (ViewPanel.fontPlain12);

			backWizardButton.setEnabled (false);
		}
		else if (descriptionCardPanel.isVisible ())
		{
			if (newProblem != null)
			{
				if (!newProblem.getStatement ().equals (descriptionTextArea.getText ()));
				{
					newProblem.setStatement (descriptionTextArea.getText ());
				}
			}
			else
			{
				if (!domain.problem.getStatement ().equals (descriptionTextArea.getText ()));
				{
					domain.problem.setStatement (descriptionTextArea.getText ());
				}
			}

			// Move to the previous panel in the cards
			nameAndLocationCardPanel.setVisible (true);
			descriptionCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the previous card
			nameAndLocationLabel.setFont (ViewPanel.fontBold12);
			descriptionLabel.setFont (ViewPanel.fontPlain12);

			// Set the focus properly for the new card
			problemNameTextField.requestFocus ();
			problemNameTextField.selectAll ();
		}
		else if (subProblemsCardPanel.isVisible ())
		{
			if (newProblem != null)
			{
				for (int i = 0; i < newProblem.getSubProblemCount (); ++i)
				{
					if (!newProblem.getSubProblem (i).getStatement ().equals (((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (i)).getComponent (1)).getComponent (0)).getComponent (0)).getText ()))
					{
						newProblem.getSubProblem (i).setStatement (((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (i)).getComponent (1)).getComponent (0)).getComponent (0)).getText ());
					}
				}
			}
			else
			{
				for (int i = 0; i < domain.problem.getSubProblemCount (); ++i)
				{
					if (!domain.problem.getSubProblem (i).getStatement ().equals (((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (i)).getComponent (1)).getComponent (0)).getComponent (0)).getText ()))
					{
						domain.problem.getSubProblem (i).setStatement (((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (i)).getComponent (1)).getComponent (0)).getComponent (0)).getText ());
					}
				}
			}

			// Move to the previous panel in the cards
			descriptionCardPanel.setVisible (true);
			subProblemsCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the previous card
			descriptionLabel.setFont (ViewPanel.fontBold12);
			subProblemsLabel.setFont (ViewPanel.fontPlain12);

			// Set the focus properly for the new card
			descriptionTextArea.requestFocus ();
			descriptionTextArea.selectAll ();
		}
		else
		{
			// Move to the previous panel in the cards
			subProblemsCardPanel.setVisible (true);
			valuesCardPanel.setVisible (false);

			// Shift the boldness in the Steps panel to the previous card
			subProblemsLabel.setFont (ViewPanel.fontBold12);
			valuesLabel.setFont (ViewPanel.fontPlain12);

			nextWizardButton.setText ("Next >");

			try
			{
				((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (subProblemPanels.size () - 1)).getComponent (1)).getComponent (0)).getComponent (0)).requestFocus ();
				((JTextArea) ((JViewport) ((JScrollPane) ((JPanel) subProblemPanels.get (subProblemPanels.size () - 1)).getComponent (1)).getComponent (0)).getComponent (0)).selectAll ();
				subProblemsScollablePanel.scrollRectToVisible (new Rectangle (0, subProblemsScollablePanel.getHeight (), 1, 1));
			}
			catch (ArrayIndexOutOfBoundsException ex)
			{
			}
		}
}//GEN-LAST:event_backWizardButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDataSetButton;
    private javax.swing.JButton addSubProblemButton;
    protected javax.swing.JButton backWizardButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton closeWizardButton;
    protected javax.swing.JTabbedPane dataSetTabbedPane;
    protected javax.swing.JPanel descriptionCardPanel;
    protected javax.swing.JLabel descriptionLabel;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JLabel descriptionWizardLabel;
    private javax.swing.JScrollPane descroptionScollPane;
    protected javax.swing.JPanel nameAndLocationCardPanel;
    protected javax.swing.JLabel nameAndLocationLabel;
    private javax.swing.JPanel nameAndLocationPanel;
    private javax.swing.JLabel nameAndLocationWizardLabel;
    private javax.swing.JPanel newProblemPanel;
    protected javax.swing.JButton nextWizardButton;
    private javax.swing.JLabel problemDescriptionLabel;
    private javax.swing.JLabel problemLocationLabel;
    private javax.swing.JTextField problemLocationTextField;
    private javax.swing.JLabel problemNameLabel;
    private javax.swing.JTextField problemNameTextField;
    private javax.swing.JButton removeDataSetButton;
    private javax.swing.JButton removeSubProblemButton;
    private javax.swing.JLabel stepsLabel;
    private javax.swing.JLabel stepsLineLabel;
    private javax.swing.JPanel stepsPanel;
    protected javax.swing.JPanel subProblemsCardPanel;
    protected javax.swing.JLabel subProblemsLabel;
    private javax.swing.JPanel subProblemsPanel;
    protected javax.swing.JPanel subProblemsScollablePanel;
    private javax.swing.JScrollPane subProblemsScrollPane;
    private javax.swing.JLabel subProblemsWizardLabel;
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
    // End of variables declaration//GEN-END:variables

	/**
	 * Create a panel for display in the tabbed display for data set values.
	 *
	 * @param problem The problem object to create this values tabbed panel for.
	 * @return The panel created to be stored in the values tabbed panel.
	 */
	private JPanel createValuesTabbedPanel(final Problem problem)
	{
		final JPanel valuesPanel = new JPanel ();
		final JScrollPane scrollPane = new JScrollPane ();
		final ExtendedTableModel model = new ExtendedTableModel ();
		model.addTableModelListener (new TableModelListener ()
		{
			@Override
			public void tableChanged(TableModelEvent evt)
			{
				fireTableChanged (problem, model, evt);
			}
		});
		final ExtendedJTable table = new ExtendedJTable (model);
		table.getTableHeader ().setFont (ViewPanel.fontPlain12);
		table.getTableHeader ().addMouseListener (new MouseAdapter ()
		{
			@Override
			public void mouseReleased(MouseEvent evt)
			{
				int index = table.getTableHeader ().columnAtPoint (evt.getPoint ());
				Object name = JOptionPane.showInputDialog (NEW_PROBLEM_WIZARD, "Give the column a new name:", "Column Name",
														   JOptionPane.QUESTION_MESSAGE, null, null,
														   table.getColumnModel ().getColumn (index).getHeaderValue ());
				if (name != null)
				{
					if (!name.toString ().equals (((ExtendedTableModel) table.getModel ()).getColumnName (index)))
					{
						if (!columnNameExists ((ExtendedTableModel) table.getModel (), index, name.toString ()))
						{
							((ExtendedTableModel) table.getModel ()).setColumn (name.toString (), index);
							table.getColumnModel ().getColumn (index).setHeaderValue (name);
							table.getTableHeader ().resizeAndRepaint ();
							try
							{
								problem.getData (problem.getDataCount ()).getColumn (index).setName (name.toString ());
							}
							// Check has already occured, so exception will never ben thrown
							catch (DuplicateNameException ex)
							{
							}
							catch (MarlaException ex)
							{
							}
						}
						else
						{
							JOptionPane.showMessageDialog (NEW_PROBLEM_WIZARD, "A column with that name already exists.", "Duplicate Column", JOptionPane.WARNING_MESSAGE);
						}
					}
				}
			}
		});

		scrollPane.setViewportView (table);
		final JLabel columnsLabel = new JLabel ("Columns:");
		columnsLabel.setFont (new java.awt.Font ("Verdana", 0, 12));
		final JSpinner columnsSpinner = new JSpinner ();
		columnsSpinner.addChangeListener (new ChangeListener ()
		{
			@Override
			public void stateChanged(ChangeEvent evt)
			{
				if (!openCloseWizard)
				{
					int value = Integer.parseInt (columnsSpinner.getValue ().toString ());
					// Don't let column count be below 1
					if (value < 1)
					{
						columnsSpinner.setValue (1);
						value = 1;
					}
					ExtendedTableModel model = (ExtendedTableModel) table.getModel ();
					// If columns were removed, loop and delete from the end
					while (value < model.getColumnCount ())
					{
						problem.getData (dataSetTabbedPane.getSelectedIndex ()).removeColumn (table.getColumnCount () - 1);
						model.removeColumn (table.getColumnCount () - 1);
						table.getColumnModel ().removeColumn (table.getColumnModel ().getColumn (table.getColumnCount () - 1));
					}
					// If columns were added, loop and add to the end
					while (value > model.getColumnCount ())
					{
						int index = model.getColumnCount () + 1;
						while (columnNameExists (model, -1, "Column " + (index)))
						{
							++index;
						}
						TableColumn newColumn = new TableColumn ();
						newColumn.setHeaderValue ("Column " + index);
						table.getColumnModel ().addColumn (newColumn);
						model.addColumn ("Column " + index);
						try
						{
							problem.getData (dataSetTabbedPane.getSelectedIndex ()).addColumn ("Column " + index);
							for (int i = 0; i < model.getRowCount (); ++i)
							{
								problem.getData (dataSetTabbedPane.getSelectedIndex ()).getColumn (model.getColumnCount () - 1).add (0.0);
							}
						}
						// This exception should never be thrown
						catch (DuplicateNameException ex)
						{
						}
					}

					table.updateUI ();
					table.getTableHeader ().resizeAndRepaint ();
				}
			}
		});
		final JLabel rowsLabel = new JLabel ("Rows:");
		rowsLabel.setFont (new java.awt.Font ("Verdana", 0, 12));
		final JSpinner rowsSpinner = new JSpinner ();
		rowsSpinner.addChangeListener (new ChangeListener ()
		{
			@Override
			public void stateChanged(ChangeEvent evt)
			{
				if (!openCloseWizard)
				{
					int value = Integer.parseInt (rowsSpinner.getValue ().toString ());
					// Don't let row count be below 1
					if (value < 1)
					{
						rowsSpinner.setValue (1);
						value = 1;
					}
					ExtendedTableModel model = (ExtendedTableModel) table.getModel ();
					// If rows were removed, loop and delete from the end
					while (value < model.getRowCount ())
					{
						for (int i = 0; i < model.getColumnCount (); ++i)
						{
							problem.getData (dataSetTabbedPane.getSelectedIndex ()).getColumn (i).remove (model.getRowAt (i));
						}
						model.removeRow (table.getRowCount () - 1);
					}
					// If rows were added, loop and add to the end
					while (value > model.getRowCount ())
					{
						for (int i = 0; i < model.getColumnCount (); ++i)
						{
							problem.getData (dataSetTabbedPane.getSelectedIndex ()).getColumn (i).add (0.0);
						}
						model.addRow (new Object[table.getColumnCount ()]);
					}

					table.updateUI ();
				}
			}
		});
		final JButton button = new JButton ("Import from CSV");
		button.setFont (new java.awt.Font ("Verdana", 0, 12));
		button.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				// Construct the folder-based open chooser dialog
				VIEW_PANEL.openChooserDialog.setFileFilter (VIEW_PANEL.csvFilter);
				VIEW_PANEL.openChooserDialog.setFileSelectionMode (JFileChooser.FILES_ONLY);
				VIEW_PANEL.openChooserDialog.setCurrentDirectory (new File (domain.lastGoodCsvFile));
				if (new File (domain.lastGoodCsvFile).isFile ())
				{
					VIEW_PANEL.openChooserDialog.setSelectedFile (new File (domain.lastGoodCsvFile));
				}
				else
				{
					VIEW_PANEL.openChooserDialog.setSelectedFile (new File (""));
				}
				// Display the chooser and retrieve the selected folder
				int response = VIEW_PANEL.openChooserDialog.showOpenDialog (NEW_PROBLEM_WIZARD);
				if (response == JFileChooser.APPROVE_OPTION)
				{
					try
					{
						// If the user selected a file that exists, point the problem's location to the newly selected location
						if (VIEW_PANEL.openChooserDialog.getSelectedFile ().exists ())
						{
							domain.lastGoodCsvFile = VIEW_PANEL.openChooserDialog.getSelectedFile ().toString ();
							try
							{
								DataSet importedDataSet = DataSet.importFile (domain.lastGoodCsvFile);

								// Setup the values table to be ready for the import
								columnsSpinner.setValue (importedDataSet.getColumnCount ());
								rowsSpinner.setValue (importedDataSet.getColumnLength ());

								final ExtendedTableModel newModel = new ExtendedTableModel ();
								newModel.addTableModelListener (new TableModelListener ()
								{
									@Override
									public void tableChanged(TableModelEvent evt)
									{
										fireTableChanged (problem, newModel, evt);
									}
								});

								// Set the column headers
								String[] columnNames = importedDataSet.getColumnNames ();
								for (int i = 0; i < columnNames.length; ++i)
								{
									newModel.addColumn (columnNames[i]);
									table.getColumnModel ().getColumn (i).setHeaderValue (columnNames[i]);
								}
								// Initialize number of rows
								for (int i = 0; i < importedDataSet.getColumnLength (); ++i)
								{
									ArrayList<Integer> row = new ArrayList<Integer> ();
									for (int j = 0; j < importedDataSet.getColumnCount (); ++j)
									{
										row.add (0);
									}
									newModel.addRow (row.toArray ());
								}
								// Load imported data into the values table
								for (int i = 0; i < importedDataSet.getColumnCount (); ++i)
								{
									DataColumn column = importedDataSet.getColumn (i);
									for (int j = 0; j < column.size (); ++j)
									{
										newModel.setValueAt (column.get (j), j, i);
									}
								}
								table.setModel (newModel);
								table.updateUI ();
								table.getTableHeader ().resizeAndRepaint ();
							}
							catch (MarlaException ex)
							{
								JOptionPane.showMessageDialog (NEW_PROBLEM_WIZARD, ex.getMessage (), "Load failed", JOptionPane.WARNING_MESSAGE);
							}
						}
					}
					catch (FileNotFoundException e)
					{
					}
				}
			}
		});

		GroupLayout valuesPanelLayout = new GroupLayout (valuesPanel);
		valuesPanel.setLayout (valuesPanelLayout);
		valuesPanelLayout.setHorizontalGroup (
				valuesPanelLayout.createParallelGroup (GroupLayout.LEADING).add (valuesPanelLayout.createSequentialGroup ().addContainerGap ().add (valuesPanelLayout.createParallelGroup (GroupLayout.LEADING).add (scrollPane, GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE).add (valuesPanelLayout.createSequentialGroup ().add (columnsLabel).addPreferredGap (LayoutStyle.RELATED).add (columnsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).add (18, 18, 18).add (rowsLabel).addPreferredGap (LayoutStyle.RELATED).add (rowsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap (LayoutStyle.RELATED, 108, Short.MAX_VALUE).add (button))).addContainerGap ()));
		valuesPanelLayout.setVerticalGroup (
				valuesPanelLayout.createParallelGroup (GroupLayout.LEADING).add (valuesPanelLayout.createSequentialGroup ().add (scrollPane, GroupLayout.PREFERRED_SIZE, 238, GroupLayout.PREFERRED_SIZE).addPreferredGap (LayoutStyle.RELATED).add (valuesPanelLayout.createParallelGroup (GroupLayout.LEADING).add (button).add (valuesPanelLayout.createParallelGroup (GroupLayout.LEADING, false).add (columnsLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add (columnsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).add (valuesPanelLayout.createParallelGroup (GroupLayout.LEADING, false).add (rowsLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add (rowsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))).addContainerGap (GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		return valuesPanel;
	}

	/**
	 * Empty all text fields of text.
	 */
	protected void emptyTextFields()
	{
		problemNameTextField.setText ("");
		problemLocationTextField.setText ("");
		descriptionTextArea.setText ("");
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
	 * Ensure the new value for the table change event is valid, otherwise revert it.
	 *
	 * @param problem The problem this change occurs on.
	 * @param model The model for the data set.
	 * @param evt The table change event.
	 */
	private void fireTableChanged(Problem problem, ExtendedTableModel model, TableModelEvent evt)
	{
		if (!openCloseWizard && !addingDataSet && !changingDataSet)
		{
			// Ensure the new value is a valid double, otherwise revert
			try
			{
				String value = model.getValueAt (evt.getFirstRow (), evt.getColumn ()).toString ();
				try
				{
					int intValue = Integer.parseInt (value.toString ());
					changingDataSet = true;
					model.setValueAt (intValue, evt.getFirstRow (), evt.getColumn ());
					problem.getData (dataSetTabbedPane.getSelectedIndex ()).getColumn (evt.getColumn ()).set (evt.getFirstRow (), intValue);
					changingDataSet = false;
				}
				catch (NumberFormatException ex)
				{
					try
					{
						double doubleValue = Double.parseDouble (value.toString ());
						changingDataSet = true;
						model.setValueAt (doubleValue, evt.getFirstRow (), evt.getColumn ());
						problem.getData (dataSetTabbedPane.getSelectedIndex ()).getColumn (evt.getColumn ()).set (evt.getFirstRow (), doubleValue);
						changingDataSet = false;
					}
					catch (NumberFormatException innerEx)
					{
						changingDataSet = true;
						model.setValueAt (changingValue, evt.getFirstRow (), evt.getColumn ());
						problem.getData (dataSetTabbedPane.getSelectedIndex ()).getColumn (evt.getColumn ()).set (evt.getFirstRow (), changingValue);
						changingDataSet = false;
					}
				}
			}
			catch (NullPointerException ex)
			{
			}
		}
	}

	/**
	 * Launch the New Problem Wizard with the default characteristics.
	 *
	 * @param editing True when editing a problem, false when creating a new one.
	 */
	protected void launchNewProblemWizard(boolean editing)
	{
		openCloseWizard = true;

		newProblemOverwrite = false;

		// Set the first card panel as the only visible
		welcomeCardPanel.setVisible (true);
		nameAndLocationCardPanel.setVisible (false);
		descriptionCardPanel.setVisible (false);
		subProblemsCardPanel.setVisible (false);
		valuesCardPanel.setVisible (false);
		// Set the proper label to bold
		welcomeLabel.setFont (ViewPanel.fontBold12);
		nameAndLocationLabel.setFont (ViewPanel.fontPlain12);
		descriptionLabel.setFont (ViewPanel.fontPlain12);
		subProblemsLabel.setFont (ViewPanel.fontPlain12);
		valuesLabel.setFont (ViewPanel.fontPlain12);
		// Set forward/backward button states
		backWizardButton.setEnabled (false);
		nextWizardButton.setEnabled (true);
		nextWizardButton.setText ("Next >");
		// Set properties in the sub problems panel
		subProblemPanels.clear ();
		subProblemsScollablePanel.removeAll ();
		subProblemsScollablePanel.updateUI ();
		addSubProblemButton.setEnabled (true);
		removeSubProblemButton.setEnabled (true);
		// Set properties for the values tabs
		dataSetTabbedPane.removeAll ();

		setNewProblemWizardDefaultValues (editing);

		// Pack and show the New Problem Wizard dialog
		pack ();
		setLocationRelativeTo (VIEW_PANEL);
		setVisible (true);

		openCloseWizard = false;
	}

	/**
	 * Sets the default values for components in the New Problem Wizard.
	 *
	 * @param editing True when editing a problem, false when creating a new one.
	 */
	private void setNewProblemWizardDefaultValues(boolean editing)
	{
		if (!editing)
		{
			// Create the new Problem object with default values
			newProblem = new Problem ("");
			newProblem.setFileName (domain.lastGoodDir + "/" + "New Problem.marla");
			try
			{
				DataSet dataSet = new DataSet ("Data Set 1");
				newProblem.addData (dataSet);
				dataSet.addColumn ("Column 1");
				dataSet.getColumn (0).add (0.0);
				dataSet.getColumn (0).add (0.0);
				dataSet.getColumn (0).add (0.0);
				dataSet.getColumn (0).add (0.0);
				dataSet.getColumn (0).add (0.0);
				dataSet.addColumn ("Column 2");
				dataSet.getColumn (1).add (0.0);
				dataSet.getColumn (1).add (0.0);
				dataSet.getColumn (1).add (0.0);
				dataSet.getColumn (1).add (0.0);
				dataSet.getColumn (1).add (0.0);
				dataSet.addColumn ("Column 3");
				dataSet.getColumn (2).add (0.0);
				dataSet.getColumn (2).add (0.0);
				dataSet.getColumn (2).add (0.0);
				dataSet.getColumn (2).add (0.0);
				dataSet.getColumn (2).add (0.0);

				dataSet.setBounds (200, 20, dataSet.getPreferredSize ().width, dataSet.getPreferredSize ().height);
			}
			// Will never be thrown at this point
			catch (DuplicateNameException ex)
			{
			}

			// Set problem defaults for name and location
			problemNameTextField.setText ("New Problem");
			problemNameTextField.setEnabled (true);
			problemLocationTextField.setText (domain.lastGoodDir);
			browseButton.setEnabled (true);
			descriptionTextArea.setText ("");

			// By default, new problems have three columns and five rows
			dataSetTabbedPane.add ("Data Set 1", createValuesTabbedPanel (newProblem));
			int columns = 3;
			int rows = 5;
			((JSpinner) ((JPanel) dataSetTabbedPane.getComponent (0)).getComponent (2)).setValue (columns);
			((JSpinner) ((JPanel) dataSetTabbedPane.getComponent (0)).getComponent (4)).setValue (rows);

			// Add minimum columns to the table model
			JTable table = ((JTable) ((JViewport) ((JScrollPane) ((JPanel) dataSetTabbedPane.getComponent (0)).getComponent (0)).getComponent (0)).getComponent (0));
			final ExtendedTableModel newModel = new ExtendedTableModel ();
			newModel.addTableModelListener (new TableModelListener ()
			{
				@Override
				public void tableChanged(TableModelEvent evt)
				{
					fireTableChanged (newProblem, newModel, evt);
				}
			});
			DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel ();
			for (int i = 0; i < columns; ++i)
			{
				int index = i + 1;
				while (columnNameExists (newModel, i, "Column " + (index)))
				{
					++index;
				}
				newColumnModel.addColumn (new TableColumn ());
				newModel.addColumn ("Column " + index);
			}
			table.setColumnModel (newColumnModel);
			// Add minimum rows to the table model
			for (int i = 0; i < rows; ++i)
			{
				newModel.addRow (new Object[columns]);
			}
			table.setModel (newModel);
			table.updateUI ();
			table.getTableHeader ().resizeAndRepaint ();
		}
		else
		{
			newProblem = null;

			// Set problem defaults for name and location
			problemNameTextField.setText (domain.problem.getFileName ().substring (domain.problem.getFileName ().lastIndexOf (System.getProperty ("file.separator")) + 1, domain.problem.getFileName ().lastIndexOf (".")));
			problemNameTextField.setEnabled (false);
			problemLocationTextField.setText (domain.problem.getFileName ().substring (0, domain.problem.getFileName ().lastIndexOf (System.getProperty ("file.separator"))));
			browseButton.setEnabled (false);
			descriptionTextArea.setText (domain.problem.getStatement ());

			// Add sub problems to the panel
			for (int i = 0; i < domain.problem.getSubProblemCount (); ++i)
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
				subProblemPanel.setLayout (new BorderLayout ());
				subProblemPanel.add (label, BorderLayout.NORTH);
				subProblemPanel.add (scrollPane, BorderLayout.CENTER);
				subProblemPanel.setPreferredSize (new Dimension (410, 100));
				subProblemPanel.setMinimumSize (new Dimension (410, 100));
				subProblemPanel.setMaximumSize (new Dimension (410, 100));

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
			subProblemsScollablePanel.scrollRectToVisible (new Rectangle (0, subProblemsScollablePanel.getHeight (), 1, 1));

			for (int i = 0; i < domain.problem.getDataCount (); ++i)
			{
				DataSet dataSet = domain.problem.getData (i);
				JPanel panel = createValuesTabbedPanel (domain.problem);
				dataSetTabbedPane.add (dataSet.getName (), panel);
				int columns = dataSet.getColumnCount ();
				int rows = dataSet.getColumnLength ();
				((JSpinner) panel.getComponent (2)).setValue (columns);
				((JSpinner) panel.getComponent (4)).setValue (rows);

				// Add minimum columns to the table model
				JTable table = (JTable) ((JViewport) ((JScrollPane) panel.getComponent (0)).getComponent (0)).getComponent (0);
				final ExtendedTableModel newModel = new ExtendedTableModel ();
				newModel.addTableModelListener (new TableModelListener ()
				{
					@Override
					public void tableChanged(TableModelEvent evt)
					{
						fireTableChanged (domain.problem, newModel, evt);
					}
				});
				DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel ();
				for (int j = 0; j < columns; ++j)
				{
					newColumnModel.addColumn (new TableColumn ());
					newModel.addColumn (dataSet.getColumn (j).getName ());
					newColumnModel.getColumn (newModel.getColumnCount () - 1).setHeaderValue (dataSet.getColumn (j).getName ());
				}
				table.setColumnModel (newColumnModel);
				// Add minimum rows to the table model
				for (int j = 0; j < rows; ++j)
				{
					newModel.addRow (new Object[columns]);
				}
				// Fill in the values for all table elements
				for (int j = 0; j < columns; ++j)
				{
					for (int k = 0; k < dataSet.getColumn (j).size (); ++k)
					{
						newModel.setValueAt (dataSet.getColumn (j).get (k), k, j);
					}
				}
				table.setModel (newModel);
				table.updateUI ();
				table.getTableHeader ().resizeAndRepaint ();
			}
			if (dataSetTabbedPane.getTabCount () > 0)
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
	 * The New Problem Wizard is complete, so create the new problem and close
	 * the New Problem Wizard.
	 *
	 * @param editing True when editing a problem, false when creating a new one.
	 */
	private void finishNewProblemWizard(boolean editing)
	{
		// Close the current or old problem, if one is open
		VIEW_PANEL.closeProblem (editing);

		// Use values from the New Problem Wizard to construct a new problem
		if (newProblem != null)
		{
			domain.problem = newProblem;
			newProblem = null;
		}

		// Open data stored in the problem currently
		VIEW_PANEL.openProblem ();

		// Save the problem immedietly
		if (!editing)
		{
			domain.save ();
		}

		closeWizardButtonActionPerformed (null);
	}

	/**
	 * Check if the given data set name exists (ignoring the current index, which is itself).
	 *
	 * @param index The current index to be ignored.
	 * @param name The name to check for.
	 * @return True if the data set name already exists, false otherwise.
	 */
	private boolean dataSetNameExists(int curIndex, String name)
	{
		for (int i = 0; i < dataSetTabbedPane.getTabCount (); ++i)
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
	 * Adds a new data set in the New Problem Wizard.
	 */
	protected void addNewDataSet()
	{
		// Transition to the values card panel
		nextWizardButtonActionPerformed (null);
		nextWizardButtonActionPerformed (null);
		nextWizardButtonActionPerformed (null);
		nextWizardButtonActionPerformed (null);

		// Add the new data set
		addDataSetButtonActionPerformed (null);
	}

	/**
	 * Edit the currently selected data set.
	 */
	protected void editDataSet(DataSet dataSet)
	{
		// Transition to the values card panel
		nextWizardButtonActionPerformed (null);
		nextWizardButtonActionPerformed (null);
		nextWizardButtonActionPerformed (null);
		nextWizardButtonActionPerformed (null);

		// Add the new data set
		try
		{
			dataSetTabbedPane.setSelectedIndex (domain.problem.getDataIndex (dataSet.getName ()));
		}
		catch (DataNotFoundException ex)
		{
			Domain.logger.add (ex);
		}
	}

	/**
	 * Edit sub problems for the currently displayed problem.
	 */
	protected void editSubProblems()
	{
		// Transition to the values card panel
		nextWizardButtonActionPerformed (null);
		nextWizardButtonActionPerformed (null);
		nextWizardButtonActionPerformed (null);
	}
}
