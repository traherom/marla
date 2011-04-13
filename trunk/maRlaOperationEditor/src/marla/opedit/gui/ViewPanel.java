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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import marla.ide.gui.ExtensionFileFilter;
import marla.ide.operation.OperationXML;
import marla.ide.operation.OperationXMLException;
import marla.ide.problem.DataSet;
import marla.ide.problem.DuplicateNameException;
import marla.ide.problem.MarlaException;
import marla.ide.resource.Configuration;
import marla.ide.resource.UndoRedo;
import marla.opedit.gui.xmlpane.XmlTextPane;
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
	public final String VALID_XML_STRING = "valid";
	/** The main frame of a stand-alone application.*/
	public MainFrame mainFrame;
	/** The model for the operations list.*/
	protected ExtendedTableModel operationsModel = new ExtendedTableModel();
	/** The operations table.*/
	protected marla.ide.gui.ExtendedJTable operationsTable = new marla.ide.gui.ExtendedJTable(operationsModel); 
	/** The model for the output table.*/
	protected marla.ide.gui.ExtendedTableModel outputModel = new  marla.ide.gui.ExtendedTableModel(new DataSet ("empty"));
	/** The output table.*/
	protected marla.ide.gui.ExtendedJTable outputTable = new marla.ide.gui.ExtendedJTable(outputModel); 
	/** The domain object reference performs generic actions specific to the GUI.*/
	protected Domain domain = new Domain(this);
	/** True while the interface is loading, false otherwise.*/
	boolean initLoading;
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
	/** Ignore the second table change event when an operation is invalid.*/
	private boolean ignoreSecond = false;
	/** The undo/redo object.*/
	protected UndoRedo<OperationXMLEditable> undoRedo = new UndoRedo<OperationXMLEditable>(50);
	/** True when an undo/redo is requested, false otherwise.*/
	private boolean isUndoRedo = false;

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
		operationsTable.setFont(FONT_PLAIN_12);
		TableColumn column = new TableColumn();
		column.setHeaderValue("Operations");
		operationsTable.addColumn(column);
		operationsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		operationsModel.addColumn(column.getHeaderValue().toString());
		operationsTable.getTableHeader().setReorderingAllowed(false);
		operationsTable.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Operations");
		
		outputTable.getTableHeader().setReorderingAllowed(false);
		outputTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		outputTable.getTableHeader ().setFont (FONT_PLAIN_12);
		outputTable.setFont(FONT_PLAIN_12);

		xmlStatusLabel.setText ("");

		// Find the "Cancel" button and change the tooltip
		marla.ide.gui.ViewPanel.setToolTipForButton(fileChooserDialog, "Cancel", "Cancel file selection");
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

        fileChooserDialog = new javax.swing.JFileChooser();
        answerDialog = new javax.swing.JDialog();
        answerPanel = new javax.swing.JPanel();
        testingPanel = new javax.swing.JPanel();
        dataCsvLabel = new javax.swing.JLabel();
        browseDataTextField = new javax.swing.JTextField();
        browseDataButton = new javax.swing.JButton();
        questionPanel = new javax.swing.JPanel();
        displayNameLabel = new javax.swing.JLabel();
        displayNameScrollPane = new javax.swing.JScrollPane();
        displayNameTextPane = new javax.swing.JTextPane();
        outputScrollPane = marla.ide.gui.ExtendedJTable.createCorneredJScrollPane(outputTable);
        editingLabel = new javax.swing.JLabel();
        editingTextField = new javax.swing.JTextField();
        browseEditingButton = new javax.swing.JButton();
        opsNameLabel = new javax.swing.JLabel();
        categoryLabel = new javax.swing.JLabel();
        operationsNameTextField = new javax.swing.JTextField();
        categoryTextField = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        updateTestButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        newButton = new javax.swing.JButton();
        xmlStatusLabel = new javax.swing.JLabel();
        innerXmlLinkLabel = new javax.swing.JLabel();
        operationScrollPane = new javax.swing.JScrollPane();
        operationTextPane = new XmlTextPane();
        hasPlotCheckBox = new javax.swing.JCheckBox();
        saveAsButton = new javax.swing.JButton();
        operationsScrollPane = marla.ide.gui.ExtendedJTable.createCorneredJScrollPane(operationsTable);

        fileChooserDialog.setApproveButtonToolTipText("Choose selected file");
        fileChooserDialog.setDialogTitle("Browse Operation File");
        fileChooserDialog.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);

        answerDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        answerDialog.setTitle("Solution to Point");
        answerDialog.setAlwaysOnTop(true);
        answerDialog.setResizable(false);
        answerDialog.getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        answerPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        answerPanel.setLayout(new javax.swing.BoxLayout(answerPanel, javax.swing.BoxLayout.PAGE_AXIS));
        answerDialog.getContentPane().add(answerPanel);

        testingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Testing", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        dataCsvLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        dataCsvLabel.setText("Data (CSV):");

        browseDataTextField.setEditable(false);
        browseDataTextField.setFont(new java.awt.Font("Verdana", 0, 12));

        browseDataButton.setFont(new java.awt.Font("Verdana", 0, 12));
        browseDataButton.setText("Browse");
        browseDataButton.setEnabled(false);
        browseDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseDataButtonActionPerformed(evt);
            }
        });

        questionPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        questionPanel.setLayout(new java.awt.BorderLayout());

        displayNameLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        displayNameLabel.setText("Display name:");

        displayNameScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        displayNameScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        displayNameTextPane.setContentType("text/html");
        displayNameTextPane.setEditable(false);
        displayNameTextPane.setFont(new java.awt.Font("Verdana", 0, 12));
        displayNameTextPane.setOpaque(false);
        displayNameScrollPane.setViewportView(displayNameTextPane);

        outputScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.layout.GroupLayout testingPanelLayout = new org.jdesktop.layout.GroupLayout(testingPanel);
        testingPanel.setLayout(testingPanelLayout);
        testingPanelLayout.setHorizontalGroup(
            testingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, testingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(testingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, outputScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, questionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, testingPanelLayout.createSequentialGroup()
                        .add(dataCsvLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseDataTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseDataButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, testingPanelLayout.createSequentialGroup()
                        .add(displayNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(displayNameScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)))
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
                .add(testingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(displayNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(displayNameScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                .addContainerGap())
        );

        editingLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        editingLabel.setText("Editing:");

        editingTextField.setEditable(false);
        editingTextField.setFont(new java.awt.Font("Verdana", 0, 12));

        browseEditingButton.setFont(new java.awt.Font("Verdana", 0, 12));
        browseEditingButton.setText("Browse");
        browseEditingButton.setEnabled(false);
        browseEditingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseEditingButtonActionPerformed(evt);
            }
        });

        opsNameLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        opsNameLabel.setText("Operation name:");

        categoryLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        categoryLabel.setText("Category:");

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
        newButton.setEnabled(false);
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        xmlStatusLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        xmlStatusLabel.setText("<html><b>XML status:</b> valid</html>");

        innerXmlLinkLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        innerXmlLinkLabel.setForeground(java.awt.Color.blue);
        innerXmlLinkLabel.setText("<html><u>View documentation for Operation XML</u></html>");
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

        operationTextPane.setFont(new java.awt.Font("Courier New", 0, 12));
        operationTextPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                operationTextPaneFocusLost(evt);
            }
        });
        operationTextPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                operationTextPaneKeyTyped(evt);
            }
        });
        operationScrollPane.setViewportView(operationTextPane);

        hasPlotCheckBox.setFont(new java.awt.Font("Verdana", 0, 12));
        hasPlotCheckBox.setText("Has plot");
        hasPlotCheckBox.setEnabled(false);
        hasPlotCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hasPlotCheckBoxActionPerformed(evt);
            }
        });

        saveAsButton.setFont(new java.awt.Font("Verdana", 0, 12));
        saveAsButton.setText("Save As");
        saveAsButton.setEnabled(false);
        saveAsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsButtonActionPerformed(evt);
            }
        });

        operationsScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

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
                        .add(editingTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseEditingButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(saveButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(saveAsButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(operationsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(hasPlotCheckBox)
                            .add(layout.createSequentialGroup()
                                .add(categoryLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(categoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(opsNameLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(operationsNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(innerXmlLinkLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(updateTestButton))
                            .add(operationScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
                            .add(xmlStatusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
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
                            .add(saveAsButton)
                            .add(saveButton)
                            .add(browseEditingButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(opsNameLabel)
                                    .add(operationsNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(categoryLabel)
                                    .add(categoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(hasPlotCheckBox)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(xmlStatusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(operationScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(updateTestButton)
                                    .add(innerXmlLinkLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(layout.createSequentialGroup()
                                .add(operationsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(addButton)
                                    .add(removeButton))))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	private void addButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addButtonActionPerformed
	{//GEN-HEADEREND:event_addButtonActionPerformed
		try
		{
			OperationXMLEditable newOp = currentFile.addOperation();
			operationsModel.addRow(new Object[] {newOp.getName()});
			((marla.ide.gui.ExtendedJTable) operationsTable).refreshTable();
			((marla.ide.gui.ExtendedJTable) operationsTable).setSelectedRow(operationsModel.getRowCount() - 1);
			operationsTable.scrollRectToVisible (operationsTable.getCellRect (operationsModel.getRowCount() - 1, 0, false));
			removeButton.setEnabled(true);
		}
		catch(OperationEditorException ex)
		{
			Domain.logger.addLast(ex);
		}
	}//GEN-LAST:event_addButtonActionPerformed

	private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeButtonActionPerformed
	{//GEN-HEADEREND:event_removeButtonActionPerformed
		if (currentOperation != null)
		{
			int index = getOperationIndex(currentOperation.getName());
			operationsModel.removeRow(index);
			currentFile.removeOperation(currentOperation);
			((marla.ide.gui.ExtendedJTable) operationsTable).refreshTable();
			if (operationsModel.getRowCount() == 0)
			{
				removeButton.setEnabled(false);
			}
			
			boolean changeCallNeeded = false;
			if (index >= operationsModel.getRowCount())
			{
				((marla.ide.gui.ExtendedJTable) operationsTable).setSelectedRow(operationsTable.getSelectedRow() - 1);
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
			fileChooserDialog.resetChoosableFileFilters();
			fileChooserDialog.setFileFilter(xmlFilter);
			fileChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooserDialog.setDialogType(JFileChooser.OPEN_DIALOG);
			fileChooserDialog.setDialogTitle("Browse Operation File");
			fileChooserDialog.setSelectedFile(new File(""));
			fileChooserDialog.setCurrentDirectory(new File(marla.ide.gui.Domain.lastGoodDir));
			// Display the chooser and retrieve the selected file
			int response = fileChooserDialog.showOpenDialog(this);
			while(response == JFileChooser.APPROVE_OPTION)
			{
				File file = fileChooserDialog.getSelectedFile();
				if(!file.isFile() || !file.toString().endsWith(".xml"))
				{
					JOptionPane.showMessageDialog(this, "The specified file does not exist.", "Does Not Exist", JOptionPane.WARNING_MESSAGE);
					int lastIndex = fileChooserDialog.getSelectedFile().toString().lastIndexOf(".");
					if(lastIndex == -1)
					{
						lastIndex = fileChooserDialog.getSelectedFile().toString().length();
					}
					fileChooserDialog.setSelectedFile(new File(fileChooserDialog.getSelectedFile().toString().substring(0, lastIndex) + ".xml"));
					response = fileChooserDialog.showOpenDialog(this);
					continue;
				}

				save();

				if(file.isDirectory())
				{
					marla.ide.gui.Domain.lastGoodDir = file.toString();
				}
				else
				{
					marla.ide.gui.Domain.lastGoodDir = file.toString().substring(0, file.toString().lastIndexOf(File.separatorChar));
				}

				// Warn if it's the primary xml
				if(file.equals(new File(OperationXML.getPrimaryXMLPath())))
				{
					int ret = JOptionPane.showConfirmDialog(this, "This file is the primary XML file. It may be overwritten without warning if maRla is updated.\nInstead, create a new file and set it as maRla's user operations XML file.\nDo you still want to edit this file?", "Editing Not Recommended", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if(ret == JOptionPane.NO_OPTION)
						return;
				}

				try
				{
					if (closeFile())
					{
						currentFile = new OperationFile(file.toString());
						openFile();
					}
				}
				catch(OperationEditorException ex)
				{
					JOptionPane.showMessageDialog(this, ex.getMessage(), "Unable to Open", JOptionPane.WARNING_MESSAGE);
				}


				break;
			}
		}
		catch(OperationEditorException ex)
		{
			Domain.logger.addLast(ex);
		}
	}//GEN-LAST:event_browseEditingButtonActionPerformed

	private void operationsNameTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_operationsNameTextFieldActionPerformed
	{//GEN-HEADEREND:event_operationsNameTextFieldActionPerformed
		if (isUndoRedo || (!ignoreChanges && !operationsNameTextField.getText().equals(currentOperation.getName())))
		{
			String newName = operationsNameTextField.getText();

			if(!currentFile.getOperationNames().contains(newName))
			{
				try
				{
					currentOperation.setEditableName(newName);
					operationsModel.setValueAt(newName, operationsTable.getSelectedRow(), 0);
					((marla.ide.gui.ExtendedJTable) operationsTable).refreshTable();
				}
				catch(OperationEditorException ex)
				{
					Domain.logger.addLast(ex);
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
		if (isUndoRedo || (!ignoreChanges && !categoryTextField.getText().equals(currentOperation.getCategory())))
		{
			String newCat = categoryTextField.getText();

			try
			{
				currentOperation.setCategory(newCat);
			}
			catch(OperationEditorException ex)
			{
				Domain.logger.addLast(ex);
			}
		}
	}//GEN-LAST:event_categoryTextFieldActionPerformed

	private void browseDataButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseDataButtonActionPerformed
	{//GEN-HEADEREND:event_browseDataButtonActionPerformed
		try
		{
			// Construct the file-based open chooser dialog
			fileChooserDialog.resetChoosableFileFilters();
			fileChooserDialog.setFileFilter(csvFilter);
			fileChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooserDialog.setDialogType(JFileChooser.OPEN_DIALOG);
			fileChooserDialog.setDialogTitle("Browse Data File");
			fileChooserDialog.setSelectedFile(new File(""));
			fileChooserDialog.setCurrentDirectory(new File(marla.ide.gui.Domain.lastGoodDir));
			// Display the chooser and retrieve the selected file
			int response = fileChooserDialog.showOpenDialog(this);
			while(response == JFileChooser.APPROVE_OPTION)
			{
				File file = fileChooserDialog.getSelectedFile();
				if(!file.isFile() || !file.toString().endsWith(".csv"))
				{
					JOptionPane.showMessageDialog(this, "The specified file does not exist.", "Does Not Exist", JOptionPane.WARNING_MESSAGE);
					int lastIndex = fileChooserDialog.getSelectedFile().toString().lastIndexOf(".");
					if(lastIndex == -1)
					{
						lastIndex = fileChooserDialog.getSelectedFile().toString().length();
					}
					fileChooserDialog.setSelectedFile(new File(fileChooserDialog.getSelectedFile().toString().substring(0, lastIndex) + ".csv"));
					response = fileChooserDialog.showOpenDialog(this);
					continue;
				}

				currentDataSetPath = file.toString();
				currentDataSet = DataSet.importFile(currentDataSetPath);
				outputModel.setData(currentDataSet);
				((marla.ide.gui.ExtendedJTable) outputTable).refreshTable();

				if(currentOperation != null)
				{
					currentOperation.setParentData(currentDataSet);
				}

				if(file.isDirectory())
				{
					marla.ide.gui.Domain.lastGoodDir = file.toString();
				}
				else
				{
					marla.ide.gui.Domain.lastGoodDir = file.toString().substring(0, file.toString().lastIndexOf(File.separatorChar));
				}

				openDataSet();
				break;
			}
		}
		catch(DuplicateNameException ex)
		{
			Domain.logger.addLast(ex);
		}
		catch(MarlaException ex)
		{
			Domain.logger.addLast(ex);
		}
	}//GEN-LAST:event_browseDataButtonActionPerformed

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
			save();
		}
	}//GEN-LAST:event_saveButtonActionPerformed

	private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
		try
		{
			// Construct the file-based open chooser dialog
			fileChooserDialog.resetChoosableFileFilters();
			fileChooserDialog.setFileFilter(xmlFilter);
			fileChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooserDialog.setDialogType(JFileChooser.SAVE_DIALOG);
			fileChooserDialog.setDialogTitle("New Operation File");
			fileChooserDialog.setSelectedFile(new File(""));
			fileChooserDialog.setCurrentDirectory(new File(marla.ide.gui.Domain.lastGoodDir));
			// Display the chooser and retrieve the selected file
			int response = fileChooserDialog.showSaveDialog(this);
			while(response == JFileChooser.APPROVE_OPTION)
			{
				int lastIndex = fileChooserDialog.getSelectedFile().toString().lastIndexOf(".");
				if(lastIndex == -1)
				{
					lastIndex = fileChooserDialog.getSelectedFile().toString().length();
					fileChooserDialog.setSelectedFile(new File(fileChooserDialog.getSelectedFile().toString().substring(0, lastIndex) + ".xml"));
				}
				File file = fileChooserDialog.getSelectedFile();
				if(!file.toString().endsWith(".xml"))
				{
					JOptionPane.showMessageDialog(this, "The file must have a valid XML extension.", "Does Not Exist", JOptionPane.WARNING_MESSAGE);
					response = fileChooserDialog.showOpenDialog(this);
					continue;
				}
				else if (file.exists())
				{
					response = JOptionPane.showOptionDialog(this, "The specified file already exists.  Overwrite?", "Overwrite File", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
					if(response != JOptionPane.YES_OPTION)
					{
						response = fileChooserDialog.showSaveDialog(this);
						continue;
					}
				}

				save();

				if(file.isDirectory())
				{
					marla.ide.gui.Domain.lastGoodDir = file.toString();
				}
				else
				{
					marla.ide.gui.Domain.lastGoodDir = file.toString().substring(0, file.toString().lastIndexOf(File.separatorChar));
				}

				if (closeFile())
				{
					currentFile = OperationFile.createNew(file.toString());
					openFile();
				}

				break;
			}
		}
		catch(OperationEditorException ex)
		{
			Domain.logger.addLast(ex);
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

	private void operationTextPaneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_operationTextPaneFocusLost
		if (isUndoRedo || (!ignoreChanges && !operationTextPane.getText().replaceAll("\\r\\n", "\\\n").equals(currentOperation.getInnerXML().replaceAll("\\r\\n", "\\\n"))))
		{
			try
			{
				setOperationInnerXml();
				xmlStatusLabel.setForeground(Color.BLACK);
				xmlStatusLabel.setText("<html><b>XML status:</b> " + VALID_XML_STRING + "</html>");
			}
			catch (OperationEditorException ex)
			{
				xmlStatusLabel.setForeground(Color.RED);
				xmlStatusLabel.setText("<html><b>XML status:</b> " + ex.getMessage() + "</html>");
			}
		}
	}//GEN-LAST:event_operationTextPaneFocusLost

	private void hasPlotCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_hasPlotCheckBoxActionPerformed
	{//GEN-HEADEREND:event_hasPlotCheckBoxActionPerformed
		if (isUndoRedo || (!ignoreChanges && currentOperation != null))
		{
			currentOperation.setHasPlot(hasPlotCheckBox.isSelected());
		}
	}//GEN-LAST:event_hasPlotCheckBoxActionPerformed

	private void saveAsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsButtonActionPerformed
		try
		{
			// Construct the file-based open chooser dialog
			fileChooserDialog.resetChoosableFileFilters();
			fileChooserDialog.setFileFilter(xmlFilter);
			fileChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooserDialog.setDialogType(JFileChooser.SAVE_DIALOG);
			fileChooserDialog.setDialogTitle("Save As Operation File");
			fileChooserDialog.setSelectedFile (new File(""));
			fileChooserDialog.setCurrentDirectory(new File(marla.ide.gui.Domain.lastGoodDir));
			// Display the chooser and retrieve the selected file
			int response = fileChooserDialog.showSaveDialog(this);
			while(response == JFileChooser.APPROVE_OPTION)
			{
				int lastIndex = fileChooserDialog.getSelectedFile().toString().lastIndexOf(".");
				if(lastIndex == -1)
				{
					lastIndex = fileChooserDialog.getSelectedFile().toString().length();
					fileChooserDialog.setSelectedFile(new File(fileChooserDialog.getSelectedFile().toString().substring(0, lastIndex) + ".xml"));
				}
				File file = fileChooserDialog.getSelectedFile();
				if(!file.toString().endsWith(".xml"))
				{
					JOptionPane.showMessageDialog(this, "The file must have a valid XML extension.", "Does Not Exist", JOptionPane.WARNING_MESSAGE);
					response = fileChooserDialog.showSaveDialog(this);
					continue;
				}
				else if (file.exists())
				{
					response = JOptionPane.showOptionDialog(this, "The specified file already exists.  Overwrite?", "Overwrite File", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
					if(response != JOptionPane.YES_OPTION)
					{
						response = fileChooserDialog.showOpenDialog(this);
						continue;
					}
				}

				currentFile = new OperationFile(currentFile);
				currentFile.setFilePath(file.toString());
				save();

				if(file.isDirectory())
				{
					marla.ide.gui.Domain.lastGoodDir = file.toString();
				}
				else
				{
					marla.ide.gui.Domain.lastGoodDir = file.toString().substring(0, file.toString().lastIndexOf(File.separatorChar));
				}

				openFile();
				break;
			}
		}
		catch(OperationEditorException ex)
		{
			Domain.logger.addLast(ex);
		}
	}//GEN-LAST:event_saveAsButtonActionPerformed

	private void operationTextPaneKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_operationTextPaneKeyTyped
		// We want to ignore any options when control is held
		if (!evt.isControlDown())
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					Domain.changeBeginning();
					operationTextPaneFocusLost(null);
				}
			});
		}
	}//GEN-LAST:event_operationTextPaneKeyTyped

	/**
	 * Undo the last operation.
	 */
	protected void undo()
	{
		if (undoRedo.hasUndo())
		{
			isUndoRedo = true;
			OperationXMLEditable opXml = undoRedo.undo(currentOperation);
			currentFile.replaceOperation(currentOperation.getName(), opXml);
			currentOperation = opXml;

			operationsNameTextField.setText(currentOperation.getName());
			operationsNameTextFieldActionPerformed(null);
			categoryTextField.setText(currentOperation.getCategory());
			categoryTextFieldActionPerformed(null);
			hasPlotCheckBox.setSelected(currentOperation.hasPlot());
			hasPlotCheckBoxActionPerformed(null);
			operationTextPane.setText(currentOperation.getInnerXML().replaceAll("\\r\\n", "\\\n"));
			operationTextPaneFocusLost(null);

			domain.validateUndoRedoMenuItems();
			isUndoRedo = false;
		}
	}

	/**
	 * Redo the last "undo" operation.
	 */
	protected void redo()
	{
		if (undoRedo.hasRedo())
		{
			isUndoRedo = true;
			OperationXMLEditable opXml = undoRedo.redo(currentOperation);
			currentFile.replaceOperation(currentOperation.getName(), opXml);
			currentOperation = opXml;

			operationsNameTextField.setText(currentOperation.getName());
			operationsNameTextFieldActionPerformed(null);
			categoryTextField.setText(currentOperation.getCategory());
			categoryTextFieldActionPerformed(null);
			hasPlotCheckBox.setSelected(currentOperation.hasPlot());
			hasPlotCheckBoxActionPerformed(null);
			operationTextPane.setText(currentOperation.getInnerXML().replaceAll("\\r\\n", "\\\n"));
			operationTextPaneFocusLost(null);

			domain.validateUndoRedoMenuItems();
			isUndoRedo = false;
		}
	}

	/**
	 * Save any changes to the current file.
	 */
	private void save()
	{
		if (currentFile != null)
		{
			try
			{
				currentFile.save();
			}
			catch(OperationEditorException ex)
			{
				JOptionPane.showMessageDialog(this, "You do not have permission to write to the file at its current location.\nYou will need to save the file to a new location to save your changes.", "Access Denied", JOptionPane.WARNING_MESSAGE);
				saveAsButtonActionPerformed(null);
			}
		}
	}

	/**
	 * Update the test in the right panel.
	 */
	private void updateTest()
	{
		boolean clearTest = false;
		if (currentOperation != null)
		{
			if (currentDataSet != null)
			{
				try
				{
					currentOperation.setParentData(currentDataSet);

					questionPanel.removeAll();
					Object[] items = marla.ide.gui.ViewPanel.getRequiredInfoDialog(currentOperation, false);
					final JPanel panel = (JPanel) items[0];
					final List<Object> valueComponents = (List<Object>) items[1];
					// Hunt down the button(s) in the panel and remove the action listener
					for (int i = 0; i < panel.getComponentCount(); ++i)
					{
						if (panel.getComponent(i) instanceof JPanel)
						{
							JPanel buttonPanel = (JPanel) panel.getComponent(i);
							if (buttonPanel.getComponent(0) instanceof JButton)
							{
								final JButton doneButton = (JButton) buttonPanel.getComponent (0);
								for (ActionListener listener : doneButton.getActionListeners())
								{
									doneButton.removeActionListener(listener);
									final ViewPanel finalViewPanel = this;
									doneButton.addActionListener(new ActionListener()
									{
										public void actionPerformed(ActionEvent evt)
										{
											if (marla.ide.gui.ViewPanel.requirementsButtonClick(currentOperation.getRequiredInfoPrompt(), valueComponents, false))
											{
												try
												{
													fillOutputTable();
												}
												catch(OperationXMLException ex)
												{
													JOptionPane.showMessageDialog(finalViewPanel, ex.getMessage(), "No Data", JOptionPane.INFORMATION_MESSAGE);
												}
											}
										}
									});
								}
								final JButton cancelButton = (JButton) buttonPanel.getComponent (1);
								for (ActionListener listener : doneButton.getActionListeners())
								{
									cancelButton.removeActionListener(listener);
								}
								break;
							}
						}
					}
					questionPanel.add(panel, BorderLayout.CENTER);
					testingPanel.invalidate();
					testingPanel.revalidate();
					testingPanel.repaint();
					
					DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel();
					outputTable.setColumnModel(newColumnModel);
					outputModel.setData (new DataSet("empty"));
					outputTable.refreshTable();

					displayNameTextPane.setText("<html><div style=\"font-family: Verdana, sans-serif;font-size: 10px;\">" + currentOperation.getDisplayString(false).trim() + "</div></html>");
				}
				catch(MarlaException ex)
				{
					clearTest = true;

					JOptionPane.showMessageDialog(this, "The test panel could not be refresh.\n" + ex.getMessage(), "Error Testing", JOptionPane.ERROR_MESSAGE);
					Domain.logger.addLast(ex);
				}
			}
			else
			{
				clearTest = true;
				displayNameTextPane.setText("<html><div style=\"font-family: Verdana, sans-serif;font-size: 10px;\">" + currentOperation.getDisplayString(false).trim() + "</div></html>");
			}
		}
		else
		{
			clearTest = true;
			displayNameTextPane.setText("");
		}
		
		if (clearTest)
		{
			testingPanel.invalidate();
			testingPanel.revalidate();
			testingPanel.repaint();

			outputModel.setData (new DataSet("empty"));
			outputTable.refreshTable();
		}
	}

	/**
	 * 
	 */
	public void fillOutputTable()
	{
		if (currentOperation != null && !currentOperation.isInfoUnanswered())
		{
			DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel();
			for (int i = 0; i < currentOperation.getColumnCount(); i++)
			{
				TableColumn column = new TableColumn(i);
				column.setHeaderValue(currentOperation.getColumn(i).getName());
				newColumnModel.addColumn(column);
			}
			outputTable.setColumnModel(newColumnModel);
			outputModel.setData (currentOperation);
			outputTable.refreshTable();

			if (currentOperation.hasPlot())
			{
				answerPanel.removeAll();
				JLabel label = new JLabel("");
				label.setIcon(new ImageIcon((currentOperation.getPlot())));
				answerPanel.add(label);

				answerDialog.setTitle("Solution to Point");

				answerDialog.pack();
				answerDialog.setLocationRelativeTo(this);
				answerDialog.setVisible(true);
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
		undoRedo.clearHistory();
		mainFrame.undoMenuItem.setEnabled(false);
		mainFrame.redoMenuItem.setEnabled(false);
		
		if (operationsTable.getSelectedRow() != -1)
		{
			try
			{
				setOperationInnerXml();

				operationsNameTextField.setEnabled (true);
				categoryTextField.setEnabled(true);
				hasPlotCheckBox.setEnabled(true);
				operationTextPane.setEnabled(true);
				updateTestButton.setEnabled(true);

				currentOperation = currentFile.getOperation(operationsTable.getValueAt(operationsTable.getSelectedRow(), 0).toString());
				operationsNameTextField.setText(currentOperation.getName());
				categoryTextField.setText(currentOperation.getCategory());
				hasPlotCheckBox.setSelected(currentOperation.hasPlot());
				operationTextPane.setText(currentOperation.getInnerXML().replaceAll("\\r\\n", "\\\n"));
				xmlStatusLabel.setForeground(Color.BLACK);
				xmlStatusLabel.setText("<html><b>XML status:</b> " + VALID_XML_STRING + "</html>");
			}
			catch (OperationEditorException ex)
			{
				xmlStatusLabel.setForeground(Color.RED);
				xmlStatusLabel.setText("<html><b>XML status:</b> " + ex.getMessage() + "</html>");

				operationsTable.setSelectedRow (getOperationIndex(currentOperation.getName()));

				// Since the table change event will fire twice, ignore the second change back to the invalid operation
				if(!ignoreSecond)
				{
					ignoreSecond = true;
					JOptionPane.showMessageDialog(this, "The XML entered is not valid. You cannot edit another operation\nthe current operation has been made valid.", "Invalid XML", JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					ignoreSecond = false;
				}
			}
		}
		else
		{
			operationsNameTextField.setEnabled (false);
			operationsNameTextField.setText("");
			categoryTextField.setEnabled(false);
			categoryTextField.setText("");
			hasPlotCheckBox.setEnabled(false);
			operationTextPane.setEnabled(false);
			operationTextPane.setText("");
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
		if (currentOperation != null)
		{
			if (!currentOperation.getInnerXML().replaceAll("\\r\\n", "\\\n").equals(operationTextPane.getText().replaceAll("\\r\\n", "\\\n")))
			{
				currentOperation.setInnerXML(operationTextPane.getText());
			}
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
			saveAsButton.setEnabled(true);

			operationsModel.removeAllRows();
			for (String item : currentFile.getOperationNames())
			{
				operationsModel.addRow(new Object[] {item});
			}
			if (operationsModel.getRowCount() != 0)
			{
				operationsTable.setSelectedRow(0);
				removeButton.setEnabled(true);
			}
			operationsTable.refreshTable();
		}
	}

	/**
	 * Close the current file.
	 */
	private boolean closeFile()
	{
		if(currentFile != null)
		{
			checkTextFieldChanges();
			
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
					save();
				}
				else if(response == -1 || response == JOptionPane.CANCEL_OPTION)
				{
					return false;
				}
			}

			operationsTable.setSelectedRow(-1);
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
			operationsNameTextFieldActionPerformed(null);
			categoryTextFieldActionPerformed(null);
			operationTextPaneFocusLost(null);
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
		if (closeFile ())
		{
			if (Domain.passedInFile != null)
			{
				JOptionPane.showMessageDialog(this, "It looks like you may have launched the maRla Operation Editor from within maRla IDE.\nIf this is the case and you've made changes to the operations file, you'll want\nto restart maRla IDE or select \"Reload Operations\" from the Tools menu.", "Reload Operations", JOptionPane.INFORMATION_MESSAGE);
			}
			
			// Hide the main window to give the appearance of better responsiveness
			mainFrame.setVisible(false);

			Configuration.getInstance().save();

			// Write out any final errors we encountered and didn't hit yet
			// We do this now, then write the configuration because, if the loadsavethread
			// is already writing, then we'll give it a bit of extra time
			domain.flushLog();

			// Save the maRla configuration
			try
			{
				Configuration.getInstance().save();
			}
			catch(MarlaException ex)
			{
				Domain.logger.addLast(ex);
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

			if(forceQuit)
			{
				System.exit(0);
			}
		}
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    protected javax.swing.JDialog answerDialog;
    private javax.swing.JPanel answerPanel;
    protected javax.swing.JButton browseDataButton;
    private javax.swing.JTextField browseDataTextField;
    protected javax.swing.JButton browseEditingButton;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JTextField categoryTextField;
    private javax.swing.JLabel dataCsvLabel;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JScrollPane displayNameScrollPane;
    private javax.swing.JTextPane displayNameTextPane;
    private javax.swing.JLabel editingLabel;
    private javax.swing.JTextField editingTextField;
    protected javax.swing.JFileChooser fileChooserDialog;
    private javax.swing.JCheckBox hasPlotCheckBox;
    private javax.swing.JLabel innerXmlLinkLabel;
    protected javax.swing.JButton newButton;
    private javax.swing.JScrollPane operationScrollPane;
    private javax.swing.JTextPane operationTextPane;
    private javax.swing.JTextField operationsNameTextField;
    private javax.swing.JScrollPane operationsScrollPane;
    private javax.swing.JLabel opsNameLabel;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JPanel questionPanel;
    private javax.swing.JButton removeButton;
    protected javax.swing.JButton saveAsButton;
    protected javax.swing.JButton saveButton;
    private javax.swing.JPanel testingPanel;
    private javax.swing.JButton updateTestButton;
    private javax.swing.JLabel xmlStatusLabel;
    // End of variables declaration//GEN-END:variables
}
