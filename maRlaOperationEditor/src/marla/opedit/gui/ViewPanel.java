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

package marla.opedit.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import marla.ide.problem.DataSet;
import marla.ide.problem.DuplicateNameException;
import marla.ide.problem.MarlaException;
import marla.opedit.operation.OperationEditorException;
import marla.opedit.operation.OperationFile;
import marla.opedit.operation.OperationXMLEditable;
import marla.opedit.resource.LoadSaveThread;

/**
 * The view of the application, which contains all user interactive components.
 * Functions that can be (that are not directly related to the front-end) will
 * be abstracted out to the Domain class.
 *
 * @author Alex Laird
 */
public class ViewPanel extends JPanel
{
	/** A reference to the object reference of the view panel.*/
	private static ViewPanel viewPanel = null;
	/** The full time format for debug output.*/
	public static final SimpleDateFormat FULL_TIME_FORMAT = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
	/** Default, plain, 12-point font.*/
	public static final Font FONT_PLAIN_12 = new Font("Verdana", Font.PLAIN, 12);
	/** Default, bold, 12-point font.*/
	public static final Font FONT_BOLD_12 = new Font("Verdana", Font.BOLD, 12);
	/** The string shown when XML for an operation is valid.*/
	public final String VALID_XML_STRING = "Operation XML is valid!";
	/** The main frame of a stand-alone application.*/
	public MainFrame mainFrame;
	/** The model for the operations list.*/
	protected ExtendedTableModel operationsModel = new ExtendedTableModel();
	/** The model for the output table.*/
	protected DefaultTableModel outputModel = new DefaultTableModel();
	/** The domain object reference performs generic actions specific to the GUI.*/
	protected Domain domain = new Domain(this);
	/** True while the interface is loading, false otherwise.*/
	boolean initLoading;
	/** The last good problem directory.*/
	public String lastGoodDir = Domain.HOME_DIR;
	/** The extensions file filter for XML files.*/
	protected ExtensionFileFilter xmlFilter = new ExtensionFileFilter("The maRla Project Operation Files (.xml)", new String[]
		{
			"XML"
		});
	/** The extensions file filter for CSV files.*/
	protected ExtensionFileFilter csvFilter = new ExtensionFileFilter("The maRla Project Data Files (.csv)", new String[]
		{
			"CSV"
		});
	/** The path to the current data set.*/
	protected String currentDataSetPath = null;
	/** Current data the user wants to use for testing */
	protected DataSet currentDataSet = null;
	/** Current file the user has opened */
	protected OperationFile currentFile = null;
	/** Current operation the user is editing */
	protected OperationXMLEditable currentOperation = null;
	/** Ignore changes made to operation components when true, otherwise accept them.*/
	private boolean ignoreChanges = true;

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
		viewPanel = this;
	}

	/**
	 * Custom initialization of specific components is done here.
	 */
	private void initMyComponents()
	{
		domain.loadSaveThread = new LoadSaveThread(domain);
		// launch the save thread
		domain.loadSaveThread.start();
		domain.setLoadSaveThread(domain.loadSaveThread);

		operationsTable.getSelectionModel ().addListSelectionListener (new ListSelectionListener ()
		{
			@Override
			public void valueChanged(ListSelectionEvent evt)
			{
				operationsTableRowSelected (evt);
			}
		});
		operationsTable.getTableHeader ().setFont (FONT_PLAIN_12);
		operationsTable.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Operations");

		xmlStatusLabel.setText ("");
	}

	/**
	 * If constructed, return the instance of the view panel.
	 *
	 * @return The instance of the view panel.
	 */
	public static ViewPanel getInstance()
	{
		if (viewPanel != null)
		{
			return viewPanel;
		}

		return null;
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        testingPanel = new javax.swing.JPanel();
        dataCsvLabel = new javax.swing.JLabel();
        browseDataTextField = new javax.swing.JTextField();
        browseDataButton = new javax.swing.JButton();
        displayNameLabel = new javax.swing.JLabel();
        displayNameTextField = new javax.swing.JTextField();
        questionPanel = new javax.swing.JPanel();
        outputScrollPane = new javax.swing.JScrollPane();
        outputTable = new javax.swing.JTable();
        editingLabel = new javax.swing.JLabel();
        editingTextField = new javax.swing.JTextField();
        browseEditingButton = new javax.swing.JButton();
        opsNameLabel = new javax.swing.JLabel();
        categoryLabel = new javax.swing.JLabel();
        operationScrollPane = new javax.swing.JScrollPane();
        operationTextArea = new javax.swing.JTextArea();
        operationsNameTextField = new javax.swing.JTextField();
        categoryTextField = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        updateTestButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        newButton = new javax.swing.JButton();
        xmlStatusLabel = new javax.swing.JLabel();
        operationsScrollPane = new javax.swing.JScrollPane();
        operationsTable = new ExtendedJTable(operationsModel);
        innerXmlLinkLabel = new javax.swing.JLabel();

        fileChooser.setApproveButtonToolTipText("Choose selected file");
        fileChooser.setDialogTitle("Browse Operation File");
        fileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);

        testingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Testing", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        dataCsvLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        dataCsvLabel.setText("Data (CSV):");

        browseDataTextField.setEditable(false);
        browseDataTextField.setFont(new java.awt.Font("Verdana", 0, 12));

        browseDataButton.setFont(new java.awt.Font("Verdana", 0, 12));
        browseDataButton.setText("Browse");
        browseDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseDataButtonActionPerformed(evt);
            }
        });

        displayNameLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        displayNameLabel.setText("Display Name:");

        displayNameTextField.setEditable(false);
        displayNameTextField.setFont(new java.awt.Font("Verdana", 0, 12));

        questionPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.layout.GroupLayout questionPanelLayout = new org.jdesktop.layout.GroupLayout(questionPanel);
        questionPanel.setLayout(questionPanelLayout);
        questionPanelLayout.setHorizontalGroup(
            questionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 317, Short.MAX_VALUE)
        );
        questionPanelLayout.setVerticalGroup(
            questionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        outputTable.setModel(outputModel);
        outputScrollPane.setViewportView(outputTable);

        org.jdesktop.layout.GroupLayout testingPanelLayout = new org.jdesktop.layout.GroupLayout(testingPanel);
        testingPanel.setLayout(testingPanelLayout);
        testingPanelLayout.setHorizontalGroup(
            testingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, testingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(testingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, outputScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, testingPanelLayout.createSequentialGroup()
                        .add(dataCsvLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseDataTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseDataButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, testingPanelLayout.createSequentialGroup()
                        .add(displayNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(displayNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, questionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        testingPanelLayout.setVerticalGroup(
            testingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(testingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(testingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dataCsvLabel)
                    .add(browseDataTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseDataButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(questionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(testingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(displayNameLabel)
                    .add(displayNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                .addContainerGap())
        );

        editingLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        editingLabel.setText("Editing:");

        editingTextField.setEditable(false);
        editingTextField.setFont(new java.awt.Font("Verdana", 0, 12));

        browseEditingButton.setFont(new java.awt.Font("Verdana", 0, 12));
        browseEditingButton.setText("Browse");
        browseEditingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseEditingButtonActionPerformed(evt);
            }
        });

        opsNameLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        opsNameLabel.setText("Operation Name:");

        categoryLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        categoryLabel.setText("Category:");

        operationTextArea.setColumns(20);
        operationTextArea.setFont(new java.awt.Font("Verdana", 0, 12));
        operationTextArea.setRows(5);
        operationTextArea.setEnabled(false);
        operationTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                operationTextAreaFocusLost(evt);
            }
        });
        operationScrollPane.setViewportView(operationTextArea);

        operationsNameTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        operationsNameTextField.setEnabled(false);
        operationsNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                operationsNameTextFieldActionPerformed(evt);
            }
        });
        operationsNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                operationsNameTextFieldFocusLost(evt);
            }
        });

        categoryTextField.setFont(new java.awt.Font("Verdana", 0, 12));
        categoryTextField.setEnabled(false);
        categoryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoryTextFieldActionPerformed(evt);
            }
        });
        categoryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                categoryTextFieldFocusLost(evt);
            }
        });

        addButton.setFont(new java.awt.Font("Verdana", 0, 12));
        addButton.setText("Add");
        addButton.setEnabled(false);
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setFont(new java.awt.Font("Verdana", 0, 12));
        removeButton.setText("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        updateTestButton.setFont(new java.awt.Font("Verdana", 0, 12));
        updateTestButton.setText("Update Test");
        updateTestButton.setEnabled(false);
        updateTestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateTestButtonActionPerformed(evt);
            }
        });

        saveButton.setFont(new java.awt.Font("Verdana", 0, 12));
        saveButton.setText("Save");
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        newButton.setFont(new java.awt.Font("Verdana", 0, 12));
        newButton.setText("New");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        xmlStatusLabel.setFont(new java.awt.Font("Verdana", 1, 12));
        xmlStatusLabel.setText("Operation XML valid!");

        operationsTable.setFont(new java.awt.Font("Verdana", 0, 12));
        operationsTable.setModel(operationsModel);
        operationsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        operationsScrollPane.setViewportView(operationsTable);

        innerXmlLinkLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        innerXmlLinkLabel.setForeground(java.awt.Color.blue);
        innerXmlLinkLabel.setText("<html><u>View documentation for XML</u></html>");
        innerXmlLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                innerXmlLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                innerXmlLinkLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                innerXmlLinkLabelMouseReleased(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(newButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editingLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editingTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseEditingButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(saveButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(operationsScrollPane, 0, 0, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(operationScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(categoryLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(categoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(opsNameLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(operationsNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(innerXmlLinkLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(updateTestButton))
                            .add(xmlStatusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE))))
                .add(6, 6, 6)
                .add(testingPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, testingPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(editingTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(newButton)
                            .add(editingLabel)
                            .add(browseEditingButton)
                            .add(saveButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(operationsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(addButton)
                                    .add(removeButton)))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(opsNameLabel)
                                    .add(operationsNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(categoryLabel)
                                    .add(categoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(xmlStatusLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(operationScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(updateTestButton)
                                    .add(innerXmlLinkLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	private void addButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addButtonActionPerformed
	{//GEN-HEADEREND:event_addButtonActionPerformed
		try
		{
			OperationXMLEditable newOp = currentFile.addOperation();
			operationsModel.addRow(new Object[] {newOp.getName()});
			((ExtendedJTable) operationsTable).refreshTableUI();
			((ExtendedJTable) operationsTable).setSelectedRow(operationsModel.getRowCount() - 1);
			operationsTable.scrollRectToVisible (operationsTable.getCellRect (operationsModel.getRowCount() - 1, 0, false));
			removeButton.setEnabled(true);
		}
		catch(OperationEditorException ex)
		{
			Domain.logger.add(ex);
		}
	}//GEN-LAST:event_addButtonActionPerformed

	private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeButtonActionPerformed
	{//GEN-HEADEREND:event_removeButtonActionPerformed
		if (currentOperation != null)
		{
			int index = getOperationIndex(currentOperation.getName());
			operationsModel.removeRow(index);
			currentFile.removeOperation(currentOperation);
			((ExtendedJTable) operationsTable).refreshTableUI();
			if (operationsModel.getRowCount() == 0)
			{
				removeButton.setEnabled(false);
			}
			
			boolean changeCallNeeded = false;
			if (index >= operationsModel.getRowCount())
			{
				((ExtendedJTable) operationsTable).setSelectedRow(operationsTable.getSelectedRow() - 1);
			}
			else
			{
				changeCallNeeded = true;
			}
			if (operationsTable.getSelectedRow () != -1)
			{
				currentOperation = currentFile.getOperation(operationsTable.getValueAt(operationsTable.getSelectedRow(), 0).toString());
			}
			if (changeCallNeeded)
			{
				operationsTableRowSelected(null);
			}
		}
	}//GEN-LAST:event_removeButtonActionPerformed

	private void updateTestButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_updateTestButtonActionPerformed
	{//GEN-HEADEREND:event_updateTestButtonActionPerformed
		if (currentDataSet != null)
		{
			updateTest();
		}
		else
		{
			JOptionPane.showMessageDialog(this, "You must browse for a data file to use before you\ncan test an operation.", "No Data Set", JOptionPane.INFORMATION_MESSAGE);
		}
	}//GEN-LAST:event_updateTestButtonActionPerformed

	private void browseEditingButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseEditingButtonActionPerformed
	{//GEN-HEADEREND:event_browseEditingButtonActionPerformed
		try
		{
			// Construct the file-based open chooser dialog
			fileChooser.resetChoosableFileFilters();
			fileChooser.setFileFilter(xmlFilter);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
			fileChooser.setDialogTitle("Browse Operation File");
			fileChooser.setCurrentDirectory(new File(lastGoodDir));
			// Display the chooser and retrieve the selected file
			int response = fileChooser.showOpenDialog(this);
			while(response == JFileChooser.APPROVE_OPTION)
			{
				File file = fileChooser.getSelectedFile();
				if(!file.isFile() || !file.toString().endsWith(".xml"))
				{
					JOptionPane.showMessageDialog(this, "The specified file does not exist.", "Does Not Exist", JOptionPane.WARNING_MESSAGE);
					int lastIndex = fileChooser.getSelectedFile().toString().lastIndexOf(".");
					if(lastIndex == -1)
					{
						lastIndex = fileChooser.getSelectedFile().toString().length();
					}
					fileChooser.setSelectedFile(new File(fileChooser.getSelectedFile().toString().substring(0, lastIndex) + ".xml"));
					response = fileChooser.showOpenDialog(this);
					continue;
				}

				if(currentFile != null)
				{
					currentFile.save();
				}

				currentFile = new OperationFile(file.toString());
				if(file.isDirectory())
				{
					lastGoodDir = file.toString();
				}
				else
				{
					lastGoodDir = file.toString().substring(0, file.toString().lastIndexOf(File.separatorChar));
				}

				openFile();
				break;
			}
		}
		catch(OperationEditorException ex)
		{
			Domain.logger.add(ex);
		}
	}//GEN-LAST:event_browseEditingButtonActionPerformed

	private void operationsNameTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_operationsNameTextFieldActionPerformed
	{//GEN-HEADEREND:event_operationsNameTextFieldActionPerformed
		if (!ignoreChanges)
		{
			String newName = operationsNameTextField.getText();

			if(!currentFile.getOperationNames().contains(newName))
			{
				try
				{
					currentOperation.setEditableName(newName);
					operationsModel.setValueAt(newName, operationsTable.getSelectedRow(), 0);
					((ExtendedJTable) operationsTable).refreshTableUI();
				}
				catch(OperationEditorException ex)
				{
					Domain.logger.add(ex);
				}
			}
			else
			{
				// This should never happen
			}
		}
	}//GEN-LAST:event_operationsNameTextFieldActionPerformed

	private void categoryTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_categoryTextFieldActionPerformed
	{//GEN-HEADEREND:event_categoryTextFieldActionPerformed
		if (!ignoreChanges)
		{
			String newCat = operationsNameTextField.getText();

			try
			{
				currentOperation.setCategory(newCat);
			}
			catch(OperationEditorException ex)
			{
				Domain.logger.add(ex);
			}
		}
	}//GEN-LAST:event_categoryTextFieldActionPerformed

	private void browseDataButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseDataButtonActionPerformed
	{//GEN-HEADEREND:event_browseDataButtonActionPerformed
		try
		{
			// Construct the file-based open chooser dialog
			fileChooser.resetChoosableFileFilters();
			fileChooser.setFileFilter(csvFilter);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
			fileChooser.setDialogTitle("Browse Data File");
			fileChooser.setCurrentDirectory(new File(lastGoodDir));
			// Display the chooser and retrieve the selected file
			int response = fileChooser.showOpenDialog(this);
			while(response == JFileChooser.APPROVE_OPTION)
			{
				File file = fileChooser.getSelectedFile();
				if(!file.isFile() || !file.toString().endsWith(".csv"))
				{
					JOptionPane.showMessageDialog(this, "The specified file does not exist.", "Does Not Exist", JOptionPane.WARNING_MESSAGE);
					int lastIndex = fileChooser.getSelectedFile().toString().lastIndexOf(".");
					if(lastIndex == -1)
					{
						lastIndex = fileChooser.getSelectedFile().toString().length();
					}
					fileChooser.setSelectedFile(new File(fileChooser.getSelectedFile().toString().substring(0, lastIndex) + ".csv"));
					response = fileChooser.showOpenDialog(this);
					continue;
				}

				currentDataSetPath = file.toString();
				currentDataSet = DataSet.importFile(currentDataSetPath);

				if(currentOperation != null)
				{
					currentOperation.setParentData(currentDataSet);
				}

				if(file.isDirectory())
				{
					lastGoodDir = file.toString();
				}
				else
				{
					lastGoodDir = file.toString().substring(0, file.toString().lastIndexOf(File.separatorChar));
				}

				openDataSet();
				break;
			}
		}
		catch(DuplicateNameException ex)
		{
			Domain.logger.add(ex);
		}
		catch(MarlaException ex)
		{
			Domain.logger.add(ex);
		}
	}//GEN-LAST:event_browseDataButtonActionPerformed

	private void operationTextAreaFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_operationTextAreaFocusLost
	{//GEN-HEADEREND:event_operationTextAreaFocusLost
		if (!ignoreChanges)
		{
			try
			{
				setOperationInnerXml();
				xmlStatusLabel.setForeground(Color.BLACK);
				xmlStatusLabel.setText("<html>" + VALID_XML_STRING + "</html>");
			}
			catch (OperationEditorException ex)
			{
				xmlStatusLabel.setForeground(Color.RED);
				xmlStatusLabel.setText("<html>" + ex.getMessage() + "</html>");
			}
		}
	}//GEN-LAST:event_operationTextAreaFocusLost

	private void operationsNameTextFieldFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_operationsNameTextFieldFocusLost
	{//GEN-HEADEREND:event_operationsNameTextFieldFocusLost
		operationsNameTextFieldActionPerformed(null);
	}//GEN-LAST:event_operationsNameTextFieldFocusLost

	private void categoryTextFieldFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_categoryTextFieldFocusLost
	{//GEN-HEADEREND:event_categoryTextFieldFocusLost
		categoryTextFieldActionPerformed(null);
	}//GEN-LAST:event_categoryTextFieldFocusLost

	private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
		if(currentFile != null)
		{
			try
			{
				currentFile.save();
			}
			catch(OperationEditorException ex)
			{
				Domain.logger.add (ex);
			}
		}
	}//GEN-LAST:event_saveButtonActionPerformed

	private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
		try
		{
			// Construct the file-based open chooser dialog
			fileChooser.resetChoosableFileFilters();
			fileChooser.setFileFilter(xmlFilter);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			fileChooser.setDialogTitle("New Operation File");
			fileChooser.setCurrentDirectory(new File(lastGoodDir));
			// Display the chooser and retrieve the selected file
			int response = fileChooser.showOpenDialog(this);
			while(response == JFileChooser.APPROVE_OPTION)
			{
				int lastIndex = fileChooser.getSelectedFile().toString().lastIndexOf(".");
				if(lastIndex == -1)
				{
					lastIndex = fileChooser.getSelectedFile().toString().length();
					fileChooser.setSelectedFile(new File(fileChooser.getSelectedFile().toString().substring(0, lastIndex) + ".xml"));
				}
				File file = fileChooser.getSelectedFile();
				if(!file.toString().endsWith(".xml"))
				{
					JOptionPane.showMessageDialog(this, "The file must have a valid XML extension.", "Does Not Exist", JOptionPane.WARNING_MESSAGE);
					response = fileChooser.showOpenDialog(this);
					continue;
				}
				else if (file.exists())
				{
					response = JOptionPane.showOptionDialog(this, "The specified file already exists.  Overwrite?", "Overwrite File", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
					if(response != JOptionPane.YES_OPTION)
					{
						response = fileChooser.showOpenDialog(this);
						continue;
					}
				}

				if(currentFile != null)
				{
					currentFile.save();
				}

				currentFile = OperationFile.createNew(file.toString());
				if(file.isDirectory())
				{
					lastGoodDir = file.toString();
				}
				else
				{
					lastGoodDir = file.toString().substring(0, file.toString().lastIndexOf(File.separatorChar));
				}

				openFile();
				break;
			}
		}
		catch(OperationEditorException ex)
		{
			Domain.logger.add(ex);
		}
	}//GEN-LAST:event_newButtonActionPerformed

	private void innerXmlLinkLabelMouseEntered(java.awt.event.MouseEvent evt)//GEN-FIRST:event_innerXmlLinkLabelMouseEntered
	{//GEN-HEADEREND:event_innerXmlLinkLabelMouseEntered
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}//GEN-LAST:event_innerXmlLinkLabelMouseEntered

	private void innerXmlLinkLabelMouseExited(java.awt.event.MouseEvent evt)//GEN-FIRST:event_innerXmlLinkLabelMouseExited
	{//GEN-HEADEREND:event_innerXmlLinkLabelMouseExited
		setCursor(Cursor.getDefaultCursor());
	}//GEN-LAST:event_innerXmlLinkLabelMouseExited

	private void innerXmlLinkLabelMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_innerXmlLinkLabelMouseReleased
	{//GEN-HEADEREND:event_innerXmlLinkLabelMouseReleased
		boolean success = true;
		if (domain.desktop != null)
		{
			try
			{
				domain.desktop.browse(new URI("http://code.google.com/p/marla/wiki/XMLOperationSpecification"));
			}
			catch(IOException ex)
			{
				success = false;
			}
			catch(URISyntaxException ex)
			{
				success = false;
			}
		}
		else
		{
			success = false;
		}
		if (!success)
		{
			JOptionPane.showMessageDialog(this, "Launching the web browser failed. To view Inner XML documentation,\nvisit http://code.google.com/p/marla/wiki/XMLOperationSpecification manually in your web browser.", "Desktop Not Supported", JOptionPane.INFORMATION_MESSAGE);
		}
	}//GEN-LAST:event_innerXmlLinkLabelMouseReleased

	/**
	 * Update the test in the right panel.
	 */
	private void updateTest()
	{
		if (currentOperation != null)
		{
			if (currentDataSet != null)
			{
				try
				{
					currentOperation.setParentData(currentDataSet);

					questionPanel.removeAll();
					questionPanel.add(marla.ide.gui.ViewPanel.getRequiredInfoDialog(currentOperation, false));
				}
				catch(MarlaException ex)
				{
					JOptionPane.showMessageDialog(this, "The test panel could not be refresh.\n" + ex.getMessage(), "Error Testing", JOptionPane.ERROR_MESSAGE);
					Domain.logger.add(ex);
				}
			}
		}
	}

	/**
	 * Fired when a new item has been selected in the operations table.
	 *
	 * @param evt The list selection event.
	 */
	private void operationsTableRowSelected(ListSelectionEvent evt)
	{
		ignoreChanges = true;
		if (operationsTable.getSelectedRow() != -1)
		{
			try
			{
				setOperationInnerXml();

				operationsNameTextField.setEnabled (true);
				categoryTextField.setEnabled(true);
				operationTextArea.setEnabled(true);
				updateTestButton.setEnabled(true);

				currentOperation = currentFile.getOperation(operationsTable.getValueAt(operationsTable.getSelectedRow(), 0).toString());
				operationsNameTextField.setText(currentOperation.getName());
				categoryTextField.setText(currentOperation.getCategory());
				operationTextArea.setText(currentOperation.getInnerXML());
				xmlStatusLabel.setForeground(Color.BLACK);
				xmlStatusLabel.setText("<html>" + VALID_XML_STRING + "</html>");
			}
			catch (OperationEditorException ex)
			{
				xmlStatusLabel.setForeground(Color.RED);
				xmlStatusLabel.setText("<html>" + ex.getMessage() + "</html>");
			}
		}
		else
		{
			operationsNameTextField.setEnabled (false);
			operationsNameTextField.setText("");
			categoryTextField.setEnabled(false);
			categoryTextField.setText("");
			operationTextArea.setEnabled(false);
			operationTextArea.setText("");
			updateTestButton.setEnabled(false);
			xmlStatusLabel.setText("");

			currentOperation = null;
		}
		updateTest();
		ignoreChanges = false;
	}

	/**
	 * Set the inner XML code from the text area.
	 */
	public void setOperationInnerXml() throws OperationEditorException
	{
		if (currentOperation != null && !ignoreChanges)
		{
			currentOperation.setInnerXML(operationTextArea.getText());
		}
	}

	/**
	 * Open the current problem.
	 */
	public void openFile()
	{
		if(currentFile != null)
		{
			// Set UI states now that a file is open
			editingTextField.setText(currentFile.toString());
			addButton.setEnabled(true);

			operationsModel.removeAllRows();
			for (String item : currentFile.getOperationNames())
			{
				operationsModel.addRow(new Object[] {item});
			}
			if (operationsModel.getRowCount() != 0)
			{
				((ExtendedJTable) operationsTable).setSelectedRow(0);
				removeButton.setEnabled(true);
			}
			((ExtendedJTable) operationsTable).refreshTableUI();
		}
	}

	/**
	 * Close the current file.
	 */
	private boolean closeFile()
	{
		if(currentFile != null)
		{
			// Check to save changes before closing the program
			if(currentFile.isChanged())
			{
				int response = JOptionPane.YES_OPTION;
				response = JOptionPane.showConfirmDialog(this,
														 "Would you like to save changes to the current operations file?",
														 "Save Operation Changes",
														 JOptionPane.YES_NO_CANCEL_OPTION,
														 JOptionPane.QUESTION_MESSAGE);
				if(response == JOptionPane.YES_OPTION)
				{
					currentFile.save();
				}
				else if(response == -1 || response == JOptionPane.CANCEL_OPTION)
				{
					return false;
				}
			}
		}
		
		return true;
	}

	/**
	 * Find the index of the given operation name in the model.
	 *
	 * @param name The name of the operation to search for.
	 * @return The index of the operation.
	 */
	public int getOperationIndex(String name)
	{
		for (int i = 0; i < operationsModel.getRowCount(); ++i)
		{
			if (operationsModel.getValueAt(i, 0).toString().equals(name))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * Open the current data set.
	 */
	public void openDataSet()
	{
		if(currentDataSet != null)
		{
			browseDataTextField.setText(currentDataSetPath);

			updateTest();
		}
	}

	/**
	 * Ensure all text fields, if changed, are saved to the object properly.
	 */
	private void checkTextFieldChanges()
	{
		if (currentOperation != null)
		{
			if (!operationsNameTextField.getText().equals(currentOperation.getName()))
			{
				operationsNameTextFieldActionPerformed(null);
			}
			if (!categoryTextField.getText().equals(currentOperation.getCategory()))
			{
				categoryTextFieldActionPerformed(null);
			}
			if (!operationTextArea.getText().equals(currentOperation.getInnerXML()))
			{
				operationTextAreaFocusLost(null);
			}
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
		checkTextFieldChanges();
		
		if (closeFile ())
		{
			// Hide the main window to give the appearance of better responsiveness
			mainFrame.setVisible(false);

			// Write out any final errors we encountered and didn't hit yet
			// We do this now, then write the configuration because, if the loadsavethread
			// is already writing, then we'll give it a bit of extra time
			domain.flushLog();

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

			if(forceQuit)
			{
				System.exit(0);
			}
		}
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton browseDataButton;
    private javax.swing.JTextField browseDataTextField;
    private javax.swing.JButton browseEditingButton;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JTextField categoryTextField;
    private javax.swing.JLabel dataCsvLabel;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JTextField displayNameTextField;
    private javax.swing.JLabel editingLabel;
    private javax.swing.JTextField editingTextField;
    protected javax.swing.JFileChooser fileChooser;
    private javax.swing.JLabel innerXmlLinkLabel;
    private javax.swing.JButton newButton;
    private javax.swing.JScrollPane operationScrollPane;
    private javax.swing.JTextArea operationTextArea;
    private javax.swing.JTextField operationsNameTextField;
    private javax.swing.JScrollPane operationsScrollPane;
    private javax.swing.JTable operationsTable;
    private javax.swing.JLabel opsNameLabel;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JTable outputTable;
    private javax.swing.JPanel questionPanel;
    private javax.swing.JButton removeButton;
    protected javax.swing.JButton saveButton;
    private javax.swing.JPanel testingPanel;
    private javax.swing.JButton updateTestButton;
    private javax.swing.JLabel xmlStatusLabel;
    // End of variables declaration//GEN-END:variables
}
