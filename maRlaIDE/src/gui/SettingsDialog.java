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

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import problem.MarlaException;
import r.RProcessor;
import resource.Configuration;

/**
 * The New Problem Wizard Dialog.
 *
 * @author Alex Laird
 */
public class SettingsDialog extends EscapeDialog
{
	/**
	 * Construct the Settings dialog.
	 *
	 * @param viewPanel A reference to the view panel.
	 */
	public SettingsDialog(ViewPanel viewPanel)
	{
		super (viewPanel.mainFrame, viewPanel);

		initComponents ();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        settingsPanel = new javax.swing.JPanel();
        settingsTabbedPane = new javax.swing.JTabbedPane();
        preferencesPanel = new javax.swing.JPanel();
        lineWidthLabel = new javax.swing.JLabel();
        rPathTextField = new javax.swing.JTextField();
        rPathButton = new javax.swing.JButton();
        latexPathButton = new javax.swing.JButton();
        latexPathTextField = new javax.swing.JTextField();
        latexPathLabel = new javax.swing.JLabel();
        latexTemplateButton = new javax.swing.JButton();
        latexTemplateTextField = new javax.swing.JTextField();
        latexTemplateLabel = new javax.swing.JLabel();
        operationsButton = new javax.swing.JButton();
        operationsTextField = new javax.swing.JTextField();
        operationsLabel = new javax.swing.JLabel();
        includeProblemCheckBox = new javax.swing.JCheckBox();
        debugModeCheckBox = new javax.swing.JCheckBox();
        sendErrorReportsCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        lineIcon = new javax.swing.JLabel();
        rPathLabel1 = new javax.swing.JLabel();
        lineSpaceLabel = new javax.swing.JLabel();
        lineSpaceSpinner = new javax.swing.JSpinner();
        lineWidthSpinner = new javax.swing.JSpinner();
        lineLabel3 = new javax.swing.JLabel();
        preferencesLabel1 = new javax.swing.JLabel();
        studentInformationPanel = new javax.swing.JPanel();
        studentNameLabel = new javax.swing.JLabel();
        courseShortNameLabel = new javax.swing.JLabel();
        courseLongNameLabel = new javax.swing.JLabel();
        studentNameTextField = new javax.swing.JTextField();
        courseShortNameTextField = new javax.swing.JTextField();
        courseLongNameTextField = new javax.swing.JTextField();
        lineLabel2 = new javax.swing.JLabel();
        studentInformationLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Settings");
        setAlwaysOnTop(true);
        setIconImage(new ImageIcon (getClass ().getResource ("/images/settings_button.png")).getImage ());
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        settingsTabbedPane.setFont(new java.awt.Font("Verdana", 0, 12));

        preferencesPanel.setLayout(null);

        lineWidthLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        lineWidthLabel.setText("Workspace line width:");
        preferencesPanel.add(lineWidthLabel);
        lineWidthLabel.setBounds(330, 70, 140, 20);

        rPathTextField.setEditable(false);
        rPathTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        preferencesPanel.add(rPathTextField);
        rPathTextField.setBounds(70, 140, 330, 22);

        rPathButton.setFont(new java.awt.Font("Verdana", 0, 12));
        rPathButton.setText("Browse");
        rPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rPathButtonActionPerformed(evt);
            }
        });
        preferencesPanel.add(rPathButton);
        rPathButton.setBounds(410, 140, 90, 25);

        latexPathButton.setFont(new java.awt.Font("Verdana", 0, 12));
        latexPathButton.setText("Browse");
        latexPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                latexPathButtonActionPerformed(evt);
            }
        });
        preferencesPanel.add(latexPathButton);
        latexPathButton.setBounds(410, 180, 90, 25);

        latexPathTextField.setEditable(false);
        latexPathTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        preferencesPanel.add(latexPathTextField);
        latexPathTextField.setBounds(110, 180, 290, 22);

        latexPathLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        latexPathLabel.setText("PDFTex Path:");
        preferencesPanel.add(latexPathLabel);
        latexPathLabel.setBounds(10, 180, 100, 20);

        latexTemplateButton.setFont(new java.awt.Font("Verdana", 0, 12));
        latexTemplateButton.setText("Browse");
        latexTemplateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                latexTemplateButtonActionPerformed(evt);
            }
        });
        preferencesPanel.add(latexTemplateButton);
        latexTemplateButton.setBounds(410, 220, 90, 25);

        latexTemplateTextField.setEditable(false);
        latexTemplateTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        preferencesPanel.add(latexTemplateTextField);
        latexTemplateTextField.setBounds(130, 220, 270, 22);

        latexTemplateLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        latexTemplateLabel.setText("LaTeX Template:");
        preferencesPanel.add(latexTemplateLabel);
        latexTemplateLabel.setBounds(10, 220, 120, 20);

        operationsButton.setFont(new java.awt.Font("Verdana", 0, 12));
        operationsButton.setText("Browse");
        operationsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                operationsButtonActionPerformed(evt);
            }
        });
        preferencesPanel.add(operationsButton);
        operationsButton.setBounds(410, 260, 90, 25);

        operationsTextField.setEditable(false);
        operationsTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        preferencesPanel.add(operationsTextField);
        operationsTextField.setBounds(130, 260, 270, 22);

        operationsLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        operationsLabel.setText("Operations XML:");
        preferencesPanel.add(operationsLabel);
        operationsLabel.setBounds(10, 260, 120, 20);

        includeProblemCheckBox.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        includeProblemCheckBox.setText("Include current problem in error reports");
        includeProblemCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeProblemCheckBoxActionPerformed(evt);
            }
        });
        preferencesPanel.add(includeProblemCheckBox);
        includeProblemCheckBox.setBounds(30, 70, 300, 25);

        debugModeCheckBox.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        debugModeCheckBox.setText("Debug mode");
        debugModeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debugModeCheckBoxActionPerformed(evt);
            }
        });
        preferencesPanel.add(debugModeCheckBox);
        debugModeCheckBox.setBounds(10, 100, 140, 25);

        sendErrorReportsCheckBox.setFont(new java.awt.Font("Verdana", 0, 12));
        sendErrorReportsCheckBox.setText("Send error reports");
        sendErrorReportsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendErrorReportsCheckBoxActionPerformed(evt);
            }
        });
        preferencesPanel.add(sendErrorReportsCheckBox);
        sendErrorReportsCheckBox.setBounds(10, 40, 190, 25);
        preferencesPanel.add(jSeparator1);
        jSeparator1.setBounds(10, 130, 520, 10);

        lineIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/line.png"))); // NOI18N
        preferencesPanel.add(lineIcon);
        lineIcon.setBounds(20, 60, 10, 30);

        rPathLabel1.setFont(new java.awt.Font("Verdana", 0, 12));
        rPathLabel1.setText("R path:");
        preferencesPanel.add(rPathLabel1);
        rPathLabel1.setBounds(10, 140, 60, 20);

        lineSpaceLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        lineSpaceLabel.setText("Workspace line space:");
        preferencesPanel.add(lineSpaceLabel);
        lineSpaceLabel.setBounds(330, 40, 150, 20);

        lineSpaceSpinner.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        lineSpaceSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        lineSpaceSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lineSpaceSpinnerStateChanged(evt);
            }
        });
        preferencesPanel.add(lineSpaceSpinner);
        lineSpaceSpinner.setBounds(480, 40, 40, 22);

        lineWidthSpinner.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        lineWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        lineWidthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lineWidthSpinnerStateChanged(evt);
            }
        });
        preferencesPanel.add(lineWidthSpinner);
        lineWidthSpinner.setBounds(480, 70, 40, 22);

        lineLabel3.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        lineLabel3.setText("______________________________________________________");
        preferencesPanel.add(lineLabel3);
        lineLabel3.setBounds(10, 10, 500, 20);

        preferencesLabel1.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        preferencesLabel1.setText("Preferences");
        preferencesPanel.add(preferencesLabel1);
        preferencesLabel1.setBounds(10, 10, 250, 16);

        settingsTabbedPane.addTab("Preferences", preferencesPanel);

        studentInformationPanel.setLayout(null);

        studentNameLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        studentNameLabel.setText("Student name:");
        studentInformationPanel.add(studentNameLabel);
        studentNameLabel.setBounds(10, 50, 94, 20);

        courseShortNameLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        courseShortNameLabel.setText("Course short name:");
        studentInformationPanel.add(courseShortNameLabel);
        courseShortNameLabel.setBounds(10, 90, 126, 20);

        courseLongNameLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        courseLongNameLabel.setText("Course long name:");
        studentInformationPanel.add(courseLongNameLabel);
        courseLongNameLabel.setBounds(10, 130, 120, 20);

        studentNameTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        studentInformationPanel.add(studentNameTextField);
        studentNameTextField.setBounds(110, 50, 289, 22);

        courseShortNameTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        studentInformationPanel.add(courseShortNameTextField);
        courseShortNameTextField.setBounds(147, 90, 150, 22);

        courseLongNameTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        studentInformationPanel.add(courseLongNameTextField);
        courseLongNameTextField.setBounds(140, 130, 240, 22);

        lineLabel2.setFont(new java.awt.Font("Verdana", 0, 12));
        lineLabel2.setText("______________________________________________________");
        studentInformationPanel.add(lineLabel2);
        lineLabel2.setBounds(10, 10, 500, 20);

        studentInformationLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        studentInformationLabel.setText("Student Information");
        studentInformationPanel.add(studentInformationLabel);
        studentInformationLabel.setBounds(10, 10, 250, 16);

        settingsTabbedPane.addTab("Student Information", studentInformationPanel);

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        closeButton.setFont(new java.awt.Font("Verdana", 0, 12));
        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/settings_button.png"))); // NOI18N
        jLabel1.setText("Settings");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 376, Short.MAX_VALUE)
                .addComponent(closeButton)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(settingsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settingsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
	{//GEN-HEADEREND:event_formWindowClosing
		closeSettings();
	}//GEN-LAST:event_formWindowClosing

	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
	{//GEN-HEADEREND:event_closeButtonActionPerformed
		closeSettings();
	}//GEN-LAST:event_closeButtonActionPerformed

	private void rPathButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_rPathButtonActionPerformed
	{//GEN-HEADEREND:event_rPathButtonActionPerformed
		VIEW_PANEL.openChooserDialog.setDialogTitle(Configuration.getName(Configuration.ConfigType.R));
		VIEW_PANEL.openChooserDialog.resetChoosableFileFilters();
		VIEW_PANEL.openChooserDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// Display the chooser and retrieve the selected file
		int response = VIEW_PANEL.openChooserDialog.showOpenDialog(this);
		if(response == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				Configuration.getInstance().set(Configuration.ConfigType.R, VIEW_PANEL.openChooserDialog.getSelectedFile().getPath());
				rPathTextField.setText(VIEW_PANEL.openChooserDialog.getSelectedFile().getPath());
			}
			// If an exception occurs, the path should not be changed, so nothing happens in a caught exception
			catch (MarlaException ex) {}
		}
	}//GEN-LAST:event_rPathButtonActionPerformed

	private void latexPathButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_latexPathButtonActionPerformed
	{//GEN-HEADEREND:event_latexPathButtonActionPerformed
		VIEW_PANEL.openChooserDialog.setDialogTitle(Configuration.getName(Configuration.ConfigType.PdfTex));
		VIEW_PANEL.openChooserDialog.resetChoosableFileFilters();
		VIEW_PANEL.openChooserDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// Display the chooser and retrieve the selected file
		int response = VIEW_PANEL.openChooserDialog.showOpenDialog(this);
		if(response == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				Configuration.getInstance().set(Configuration.ConfigType.PdfTex, VIEW_PANEL.openChooserDialog.getSelectedFile().getPath());
				rPathTextField.setText(VIEW_PANEL.openChooserDialog.getSelectedFile().getPath());
			}
			// If an exception occurs, the path should not be changed, so nothing happens in a caught exception
			catch (MarlaException ex) {}
		}
	}//GEN-LAST:event_latexPathButtonActionPerformed

	private void latexTemplateButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_latexTemplateButtonActionPerformed
	{//GEN-HEADEREND:event_latexTemplateButtonActionPerformed
		VIEW_PANEL.openChooserDialog.setDialogTitle(Configuration.getName(Configuration.ConfigType.TexTemplate));
		VIEW_PANEL.openChooserDialog.resetChoosableFileFilters();
		VIEW_PANEL.openChooserDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// Display the chooser and retrieve the selected file
		int response = VIEW_PANEL.openChooserDialog.showOpenDialog(this);
		if(response == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				Configuration.getInstance().set(Configuration.ConfigType.TexTemplate, VIEW_PANEL.openChooserDialog.getSelectedFile().getPath());
				rPathTextField.setText(VIEW_PANEL.openChooserDialog.getSelectedFile().getPath());
			}
			// If an exception occurs, the path should not be changed, so nothing happens in a caught exception
			catch (MarlaException ex) {}
		}
	}//GEN-LAST:event_latexTemplateButtonActionPerformed

	private void operationsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_operationsButtonActionPerformed
	{//GEN-HEADEREND:event_operationsButtonActionPerformed
		VIEW_PANEL.openChooserDialog.setDialogTitle(Configuration.getName(Configuration.ConfigType.PrimaryOpsXML));
		VIEW_PANEL.openChooserDialog.resetChoosableFileFilters();
		VIEW_PANEL.openChooserDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// Display the chooser and retrieve the selected file
		int response = VIEW_PANEL.openChooserDialog.showOpenDialog(this);
		if(response == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				Configuration.getInstance().set(Configuration.ConfigType.PrimaryOpsXML, VIEW_PANEL.openChooserDialog.getSelectedFile().getPath());
				rPathTextField.setText(VIEW_PANEL.openChooserDialog.getSelectedFile().getPath());
				VIEW_PANEL.reloadOperations();
			}
			// If an exception occurs, the path should not be changed, so nothing happens in a caught exception
			catch (MarlaException ex) {}
		}
	}//GEN-LAST:event_operationsButtonActionPerformed

	private void includeProblemCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeProblemCheckBoxActionPerformed
		try
		{
			Configuration.getInstance().set(Configuration.ConfigType.ReportWithProblem, includeProblemCheckBox.isSelected());
		}
		catch (MarlaException ex)
		{
			Domain.logger.add(ex);
		}
	}//GEN-LAST:event_includeProblemCheckBoxActionPerformed

	private void debugModeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debugModeCheckBoxActionPerformed
		try
		{
			if (debugModeCheckBox.isSelected())
			{
				Configuration.getInstance().set(Configuration.ConfigType.DebugMode, RProcessor.RecordMode.FULL);
			}
			else
			{
				Configuration.getInstance().set(Configuration.ConfigType.DebugMode, RProcessor.RecordMode.DISABLED);
			}
		}
		catch (MarlaException ex)
		{
			Domain.logger.add(ex);
		}
	}//GEN-LAST:event_debugModeCheckBoxActionPerformed

	private void sendErrorReportsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendErrorReportsCheckBoxActionPerformed
		try
		{
			Configuration.getInstance().set(Configuration.ConfigType.SendErrorReports, sendErrorReportsCheckBox.isSelected());
		}
		catch (MarlaException ex)
		{
			Domain.logger.add(ex);
		}

		includeProblemCheckBox.setEnabled(sendErrorReportsCheckBox.isSelected());
		lineIcon.setEnabled(sendErrorReportsCheckBox.isSelected());
	}//GEN-LAST:event_sendErrorReportsCheckBoxActionPerformed

	private void lineSpaceSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lineSpaceSpinnerStateChanged
		try
		{
			Configuration.getInstance().set(Configuration.ConfigType.LineSpacing, lineSpaceSpinner.getValue());
		}
		catch (MarlaException ex)
		{
			Domain.logger.add(ex);
		}
	}//GEN-LAST:event_lineSpaceSpinnerStateChanged

	private void lineWidthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lineWidthSpinnerStateChanged
		try
		{
			Configuration.getInstance().set(Configuration.ConfigType.MinLineWidth, lineWidthSpinner.getValue());
		}
		catch (MarlaException ex)
		{
			Domain.logger.add(ex);
		}
	}//GEN-LAST:event_lineWidthSpinnerStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel courseLongNameLabel;
    private javax.swing.JTextField courseLongNameTextField;
    private javax.swing.JLabel courseShortNameLabel;
    private javax.swing.JTextField courseShortNameTextField;
    private javax.swing.JCheckBox debugModeCheckBox;
    private javax.swing.JCheckBox includeProblemCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton latexPathButton;
    private javax.swing.JLabel latexPathLabel;
    private javax.swing.JTextField latexPathTextField;
    private javax.swing.JButton latexTemplateButton;
    private javax.swing.JLabel latexTemplateLabel;
    private javax.swing.JTextField latexTemplateTextField;
    private javax.swing.JLabel lineIcon;
    private javax.swing.JLabel lineLabel2;
    private javax.swing.JLabel lineLabel3;
    private javax.swing.JLabel lineSpaceLabel;
    private javax.swing.JSpinner lineSpaceSpinner;
    private javax.swing.JLabel lineWidthLabel;
    private javax.swing.JSpinner lineWidthSpinner;
    private javax.swing.JButton operationsButton;
    private javax.swing.JLabel operationsLabel;
    private javax.swing.JTextField operationsTextField;
    private javax.swing.JLabel preferencesLabel1;
    private javax.swing.JPanel preferencesPanel;
    private javax.swing.JButton rPathButton;
    private javax.swing.JLabel rPathLabel1;
    private javax.swing.JTextField rPathTextField;
    private javax.swing.JCheckBox sendErrorReportsCheckBox;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JTabbedPane settingsTabbedPane;
    private javax.swing.JLabel studentInformationLabel;
    private javax.swing.JPanel studentInformationPanel;
    private javax.swing.JLabel studentNameLabel;
    private javax.swing.JTextField studentNameTextField;
    // End of variables declaration//GEN-END:variables

	/**
	 * Close the settings dialog.
	 */
	protected void closeSettings()
	{
		try
		{
			Configuration config = Configuration.getInstance();
			if (!config.get(Configuration.ConfigType.UserName).toString().equals (studentNameTextField.getText()))
			{
				config.set(Configuration.ConfigType.UserName, studentNameTextField.getText());
			}
			if (!config.get(Configuration.ConfigType.ClassShort).toString().equals (courseShortNameTextField.getText()))
			{
				config.set(Configuration.ConfigType.ClassShort, courseShortNameTextField.getText());
			}
			if (!config.get(Configuration.ConfigType.ClassLong).toString().equals (courseLongNameTextField.getText()))
			{
				config.set(Configuration.ConfigType.ClassLong, courseLongNameTextField.getText());
			}
		
			dispose();

			// Write it out
			config.save();
		}
		catch (MarlaException ex)
		{
			Domain.logger.add(ex);
		}
	}

	/**
	 * Launch the Settings dialog.
	 */
	protected void launchSettingsDialog()
	{
		try
		{
			Configuration config = Configuration.getInstance();
			rPathTextField.setText (config.get(Configuration.ConfigType.R).toString());
			latexPathTextField.setText (config.get(Configuration.ConfigType.PdfTex).toString());
			latexTemplateTextField.setText (config.get(Configuration.ConfigType.TexTemplate).toString());
			operationsTextField.setText (config.get(Configuration.ConfigType.PrimaryOpsXML).toString());
			studentNameTextField.setText (config.get(Configuration.ConfigType.UserName).toString());
			courseShortNameTextField.setText (config.get(Configuration.ConfigType.ClassShort).toString());
			courseLongNameTextField.setText (config.get(Configuration.ConfigType.ClassLong).toString());
			sendErrorReportsCheckBox.setSelected(Boolean.valueOf(config.get(Configuration.ConfigType.SendErrorReports).toString()));
			includeProblemCheckBox.setSelected(Boolean.valueOf(config.get(Configuration.ConfigType.ReportWithProblem).toString()));
			debugModeCheckBox.setSelected(((RProcessor.RecordMode) config.get(Configuration.ConfigType.DebugMode)) == RProcessor.RecordMode.FULL);
			lineSpaceSpinner.setValue(config.get(Configuration.ConfigType.LineSpacing));
			lineWidthSpinner.setValue(config.get(Configuration.ConfigType.MinLineWidth));
		}
		catch (MarlaException ex)
		{
			Domain.logger.add(ex);
		}

		// Pack and show the Settings dialog
		pack ();
		setLocationRelativeTo (VIEW_PANEL);
		setVisible (true);
	}
}
