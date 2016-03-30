package com.jmeter.protocol.soap.control.gui;

import com.jmeter.sampler.config.gui.UndoableJTextArea;
import com.jmeter.sampler.config.gui.UndoableJTextField;
import com.jmeter.sampler.util.SOAPMessages;
import com.jmeter.protocol.soap.sampler.CustomSOAPSampler;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class CustomSOAPSamplerGui extends AbstractSamplerGui {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final long serialVersionUID = 76805212533699529L;
    private UndoableJTextField urlField;
    private UndoableJTextArea soapXml;
    private JCheckBox useRelativePaths;
    private JCheckBox updateAttachmentReferences;
    private JTextField attachmentName;
    private JTextField contentId;
    private JComboBox attachmentContentType;
    private JComboBox attachmentType;
    private JButton browseAttachmentButton;
    private JButton attachButton;
    private JButton removeButton;
    private JTable attachmentTable;
    private JCheckBox treatAttachmentAsResponseCbx;
    private JPanel attachmentSelectPanel;
    private JComboBox attachmentSelContentType;
    private JTextField attachmentSelContentId;
    private JRadioButton firstWithContentType;
    private JComboBox firstWithSoapProtocolVersion;
    private JRadioButton withContentId;
    private SOAPAttachmentTableModel soapTableModel = new SOAPAttachmentTableModel();
    public static final String[] contentTypes = new String[]{"auto", "text/plain", "text/xml", "text/html", "application/xml", "application/gzip"};
    public static final String[] soapProtocolVersions = new String[]{"1_1", "1_2"};
    public static final String[] attachmentTypes;

    public CustomSOAPSamplerGui() {
        this.init();
    }

    public String getStaticLabel() {
        return SOAPMessages.getResString(this.getLabelResource());
    }

    public String getLabelResource() {
        return "soap_sampler_title";
    }

    public TestElement createTestElement() {
        CustomSOAPSampler sampler = new CustomSOAPSampler();
        this.modifyTestElement(sampler);
        return sampler;
    }

    public void modifyTestElement(TestElement s) {
        this.configureTestElement(s);
        if(s instanceof CustomSOAPSampler) {
            CustomSOAPSampler sampler = (CustomSOAPSampler)s;
            sampler.setURLData(this.urlField.getText());
            sampler.setXmlData(this.soapXml.getText());
            sampler.setSoapProtocolVersion((String)this.firstWithSoapProtocolVersion.getSelectedItem());
            sampler.setAttachments(this.soapTableModel.getAttachments());
            sampler.setTreatAttachmentAsResponse(this.treatAttachmentAsResponseCbx.isSelected());
            sampler.setAttachmentAsResponseContentID(this.attachmentSelContentId.getText());
            sampler.setAttachmentAsResponseContentType((String)this.attachmentSelContentType.getSelectedItem());
            sampler.setAttachmentAsResponseMode(this.firstWithContentType.isSelected()?0:1);
            sampler.setUseRelativePaths(this.useRelativePaths.isSelected());
            sampler.setUpdateAttachmentReferences(this.updateAttachmentReferences.isSelected());
        }

    }

    public void clearGui() {
        super.clearGui();
        this.urlField.clear();
        this.soapXml.clear();
        this.treatAttachmentAsResponseCbx.setSelected(false);
        this.attachmentName.setText("");
        this.contentId.setText("");
        this.soapTableModel.clear();
        this.useRelativePaths.setSelected(true);
        this.updateAttachmentReferences.setSelected(true);
    }

    private void init() {
        this.setLayout(new BorderLayout());
        this.setBorder(this.makeBorder());
        this.add(this.makeTitlePanel(), "North");
        this.urlField = new UndoableJTextField("120");
        this.urlField.initActionMap(this);

        GridBagConstraints c = new GridBagConstraints();

        this.firstWithSoapProtocolVersion = new JComboBox(soapProtocolVersions);
        this.firstWithSoapProtocolVersion.setEnabled(true);
        this.firstWithSoapProtocolVersion.setSelectedItem(SOAPMessages.getResString("soap_protocol_version"));

        JLabel urlFieldLabel = new JLabel(JMeterUtils.getResString("url"));
        JPanel urlFieldPanel = new JPanel(new BorderLayout(5, 0));
        urlFieldPanel.add(urlFieldLabel, BorderLayout.WEST);
        urlFieldPanel.add(this.urlField, BorderLayout.CENTER);

        JPanel soapProtocolFieldPanel = new JPanel(new BorderLayout(5, 0));
        soapProtocolFieldPanel.add(new JLabel("Soap version"), BorderLayout.WEST);
        soapProtocolFieldPanel.add(this.firstWithSoapProtocolVersion, BorderLayout.EAST);

        JPanel soapProtocolUrlJointPanel = new JPanel(new BorderLayout(5, 0));

        soapProtocolUrlJointPanel.add(urlFieldPanel, BorderLayout.CENTER);
        soapProtocolUrlJointPanel.add(soapProtocolFieldPanel, BorderLayout.LINE_END);

        this.soapXml = new UndoableJTextArea();
        //JTextArea ta = new JTextArea();
        JScrollPane sp = new JScrollPane(this.soapXml); 
        this.soapXml.initActionMap(this);
        JPanel soapXmlPanel = new JPanel(new BorderLayout(5, 0));
//        soapXmlPanel.add(this.soapXml, "Center");
        soapXmlPanel.add(sp, "Center");
        this.treatAttachmentAsResponseCbx = new JCheckBox(SOAPMessages.getResString("soap_treat_attachment_as_response"));
        this.firstWithContentType = new JRadioButton(SOAPMessages.getResString("soap_use_first_att_content_type"));

        this.withContentId = new JRadioButton(SOAPMessages.getResString("soap_use_att_with_contentid"));
        this.firstWithContentType.setSelected(true);
        ButtonGroup bg = new ButtonGroup();
        bg.add(this.firstWithContentType);
        bg.add(this.withContentId);
        this.useRelativePaths = new JCheckBox(SOAPMessages.getResString("soap_use_relative_paths"));
        this.useRelativePaths.setSelected(true);
        this.updateAttachmentReferences = new JCheckBox(SOAPMessages.getResString("soap_update_att_ebxml_refs"));
        this.updateAttachmentReferences.setSelected(true);
        this.attachmentName = new JTextField(60);
        JLabel attachmentNameLabel = new JLabel(SOAPMessages.getResString("soap_attachment_name"));
        JPanel attachmentNamePanel = new JPanel(new BorderLayout(5, 0));
        attachmentNamePanel.add(attachmentNameLabel, "West");
        attachmentNamePanel.add(this.attachmentName, "Center");
        this.contentId = new JTextField("", 10);
        this.attachmentContentType = new JComboBox(contentTypes);
        this.attachmentContentType.setEditable(true);
        this.attachmentType = new JComboBox(attachmentTypes);
        this.browseAttachmentButton = new JButton(SOAPMessages.getResString("soap_browse_attachment"));
        this.attachButton = new JButton(SOAPMessages.getResString("soap_attach_attachment"));
        this.removeButton = new JButton(SOAPMessages.getResString("soap_remove_attachment"));
        JPanel soapActionPanel = new JPanel();
        soapActionPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.fill = 2;
        c.gridwidth = 0;
        c.weightx = 1.0D;
//        soapActionPanel.add(urlFieldPanel, c);
        soapActionPanel.add(soapProtocolUrlJointPanel, c);
        //soapActionPanel.add(soapProtocolFieldPanel, c);
        c.gridwidth = 0;
        c.fill = 2;
        c.weightx = 0.0D;
        soapActionPanel.add(this.treatAttachmentAsResponseCbx, c);
        this.attachmentSelContentType = new JComboBox(contentTypes);
        this.attachmentSelContentType.setEditable(true);
        this.attachmentSelContentId = new JTextField(15);
        this.attachmentSelectPanel = new JPanel(new GridBagLayout());
        GridBagConstraints aspc = new GridBagConstraints();
        aspc.weightx = 0.0D;
        aspc.weighty = 0.0D;
        aspc.anchor = 18;
        aspc.fill = 0;
        aspc.gridwidth = -1;
        Insets oldInsets = aspc.insets;
        aspc.insets = new Insets(0, 0, 3, 0);
        this.attachmentSelectPanel.add(this.firstWithContentType, aspc);
        aspc.gridwidth = 0;
        aspc.fill = 2;
        aspc.weightx = 1.0D;
        this.attachmentSelectPanel.add(this.attachmentSelContentType, aspc);
        aspc.insets = oldInsets;
        aspc.weightx = 0.0D;
        aspc.fill = 0;
        aspc.gridwidth = -1;
        this.attachmentSelectPanel.add(this.withContentId, aspc);
        aspc.weightx = 1.0D;
        aspc.fill = 2;
        aspc.gridwidth = 0;
        this.attachmentSelectPanel.add(this.attachmentSelContentId, aspc);
        c.weightx = 1.0D;
        oldInsets = c.insets;
        c.insets = new Insets(0, 25, 0, 0);
        soapActionPanel.add(this.attachmentSelectPanel, c);
        c.weightx = 0.0D;
        c.insets = oldInsets;
        this.attachmentTable = new JTable(this.soapTableModel);
        TableColumnModel tcm = this.attachmentTable.getColumnModel();
        TableColumn typeCol = tcm.getColumn(0);
        typeCol.setCellEditor(new CustomSOAPSamplerGui.ComboBoxTableCellEditor(attachmentTypes, false));
        TableColumn attNameCol = tcm.getColumn(1);
        CustomSOAPSamplerGui.ConditionalFileBrowseCellEditor fbce = new CustomSOAPSamplerGui.ConditionalFileBrowseCellEditor(this, 30, SOAPMessages.getResString("soap_browse_attachment"), 0, SOAPAttachmentTableModel.TYPE_RESOURCE);
        attNameCol.setCellEditor(fbce);
        TableColumn contentTypeCol = tcm.getColumn(2);
        contentTypeCol.setCellEditor(new CustomSOAPSamplerGui.ComboBoxTableCellEditor(contentTypes, true));
        this.attachmentTable.setRowHeight(fbce.getPreferredRowHeight());
        int totalSize = tcm.getTotalColumnWidth();
        int ordinaryColSize = totalSize / 6;
        tcm.getColumn(1).setPreferredWidth(totalSize - 3 * ordinaryColSize);
        tcm.getColumn(0).setPreferredWidth(ordinaryColSize);
        tcm.getColumn(2).setPreferredWidth(ordinaryColSize);
        tcm.getColumn(3).setPreferredWidth(ordinaryColSize);
        JPanel attachmentAddPanel = new JPanel(new GridBagLayout());
        attachmentAddPanel.setBorder(new TitledBorder(SOAPMessages.getResString("soap_attachment_definition")));
        JPanel attBrowsePanel = new JPanel(new GridBagLayout());
        GridBagConstraints atpc = new GridBagConstraints();
        atpc.insets = new Insets(0, 5, 0, 0);
        atpc.weighty = 0.0D;
        atpc.anchor = 17;
        atpc.fill = 2;
        atpc.weightx = 1.0D;
        attBrowsePanel.add(attachmentNamePanel, atpc);
        atpc.fill = 0;
        atpc.weightx = 0.0D;
        attBrowsePanel.add(this.browseAttachmentButton, atpc);
        attBrowsePanel.add(this.useRelativePaths, atpc);
        JPanel attDetailsPanel = new JPanel(new GridBagLayout());
        atpc = new GridBagConstraints();
        atpc.insets = new Insets(0, 5, 0, 0);
        atpc.fill = 0;
        atpc.weightx = 0.0D;
        attDetailsPanel.add(new JLabel(SOAPMessages.getResString("soap_content_id")), atpc);
        atpc.fill = 2;
        atpc.weightx = 1.0D;
        attDetailsPanel.add(this.contentId, atpc);
        atpc.fill = 0;
        atpc.weightx = 0.0D;
        attDetailsPanel.add(new JLabel(SOAPMessages.getResString("soap_attachment_type")), atpc);
        attDetailsPanel.add(this.attachmentType, atpc);
        attDetailsPanel.add(new JLabel(SOAPMessages.getResString("soap_content_type")), atpc);
        attDetailsPanel.add(this.attachmentContentType, atpc);
        attDetailsPanel.add(this.attachButton, atpc);
        atpc.gridwidth = 0;
        attDetailsPanel.add(this.removeButton, atpc);
        atpc.weightx = 1.0D;
        atpc.fill = 0;
        atpc.anchor = 17;
        attDetailsPanel.add(this.updateAttachmentReferences, atpc);
        JScrollPane scp = new JScrollPane(this.attachmentTable, 22, 30);
        Dimension prefSize = scp.getPreferredSize();
        prefSize.height = 75;
        scp.setPreferredSize(prefSize);
        Dimension minSize = scp.getMinimumSize();
        minSize.height = 75;
        scp.setMinimumSize(minSize);
        atpc = new GridBagConstraints();
        atpc.fill = 2;
        atpc.weighty = 0.0D;
        atpc.weightx = 1.0D;
        atpc.gridwidth = 0;
        attachmentAddPanel.add(attBrowsePanel, atpc);
        attachmentAddPanel.add(attDetailsPanel, atpc);
        atpc.weighty = 1.0D;
        atpc.fill = 1;
        attachmentAddPanel.add(scp, atpc);
        c.fill = 1;
        c.gridwidth = 0;
        c.weighty = 1.0D;
        c.weightx = 1.0D;
        c.gridheight = -1;
        c.anchor = 12;
        soapActionPanel.add(attachmentAddPanel, c);
        c.weighty = 3.0D;
        c.gridheight = 0;
        Border border = BorderFactory.createEtchedBorder();
        soapXmlPanel.setBorder(BorderFactory.createTitledBorder(border, SOAPMessages.getResString("soap_data_title")));
        Dimension minSizeSoapXML = soapXmlPanel.getMinimumSize();
        minSizeSoapXML.height = 75;
        soapXmlPanel.setMinimumSize(minSizeSoapXML);
        soapActionPanel.add(soapXmlPanel, c);
        this.attachButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SOAPAttachmentTableModel satm = (SOAPAttachmentTableModel)CustomSOAPSamplerGui.this.attachmentTable.getModel();
                satm.add((String)CustomSOAPSamplerGui.this.attachmentType.getSelectedItem(), CustomSOAPSamplerGui.this.attachmentName.getText(), (String)CustomSOAPSamplerGui.this.attachmentContentType.getSelectedItem(), CustomSOAPSamplerGui.this.contentId.getText());
            }
        });
        this.removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SOAPAttachmentTableModel satm = (SOAPAttachmentTableModel)CustomSOAPSamplerGui.this.attachmentTable.getModel();
                int row = CustomSOAPSamplerGui.this.attachmentTable.getSelectedRow();
                int[] selRows = CustomSOAPSamplerGui.this.attachmentTable.getSelectedRows();

                int selRow;
                for(selRow = selRows.length - 1; selRow >= 0; --selRow) {
                    satm.remove(selRows[selRow]);
                }

                selRow = row;
                if(row >= satm.getRowCount()) {
                    selRow = satm.getRowCount() - 1;
                }

                if(selRow >= 0) {
                    CustomSOAPSamplerGui.this.attachmentTable.setRowSelectionInterval(selRow, selRow);
                }

            }
        });
        this.browseAttachmentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CustomSOAPSamplerGui.ChooseResult cr = CustomSOAPSamplerGui.this.chooseFile(CustomSOAPSamplerGui.this.attachmentName.getText());
                if(cr.chosen) {
                    CustomSOAPSamplerGui.this.attachmentName.setText(cr.chosenValue);
                    CustomSOAPSamplerGui.this.attachmentType.setSelectedItem(SOAPAttachmentTableModel.TYPE_RESOURCE);
                }

            }
        });
        this.treatAttachmentAsResponseCbx.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                boolean enabled = CustomSOAPSamplerGui.this.treatAttachmentAsResponseCbx.isSelected();
                CustomSOAPSamplerGui.this.firstWithContentType.setEnabled(enabled);
                CustomSOAPSamplerGui.this.withContentId.setEnabled(enabled);
                if(enabled) {
                    CustomSOAPSamplerGui.this.attachmentSelContentType.setEnabled(CustomSOAPSamplerGui.this.firstWithContentType.isSelected());
                    CustomSOAPSamplerGui.this.attachmentSelContentId.setEnabled(CustomSOAPSamplerGui.this.withContentId.isSelected());
                } else {
                    CustomSOAPSamplerGui.this.attachmentSelContentType.setEnabled(false);
                    CustomSOAPSamplerGui.this.attachmentSelContentId.setEnabled(false);
                }

            }
        });
        this.firstWithContentType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CustomSOAPSamplerGui.this.attachmentSelContentType.setEnabled(CustomSOAPSamplerGui.this.firstWithContentType.isSelected());
                CustomSOAPSamplerGui.this.attachmentSelContentId.setEnabled(CustomSOAPSamplerGui.this.withContentId.isSelected());
            }
        });
        this.withContentId.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CustomSOAPSamplerGui.this.attachmentSelContentType.setEnabled(CustomSOAPSamplerGui.this.firstWithContentType.isSelected());
                CustomSOAPSamplerGui.this.attachmentSelContentId.setEnabled(CustomSOAPSamplerGui.this.withContentId.isSelected());
            }
        });
        this.treatAttachmentAsResponseCbx.setSelected(false);
        this.attachmentSelContentType.setEnabled(false);
        this.attachmentSelContentId.setEnabled(false);
        this.firstWithContentType.setEnabled(false);
        this.withContentId.setEnabled(false);
        CustomSOAPSamplerGui.SelectionListener listener = new CustomSOAPSamplerGui.SelectionListener(this.attachmentTable, this);
        this.attachmentTable.getSelectionModel().addListSelectionListener(listener);
        this.attachmentTable.getColumnModel().getSelectionModel().addListSelectionListener(listener);
        this.add(soapActionPanel, "Center");
    }

    public CustomSOAPSamplerGui.ChooseResult chooseFile(String originalContents) {
        CustomSOAPSamplerGui.ChooseResult cr = new CustomSOAPSamplerGui.ChooseResult();
        cr.chosen = false;
        cr.chosenValue = originalContents;
        File f = new File(originalContents);
        if(!f.exists()) {
            String chooser = FileServer.getFileServer().getBaseDir();
            if(!chooser.endsWith(File.separator)) {
                chooser = chooser + File.separator;
            }

            f = new File(chooser + originalContents);
            if(!f.exists()) {
                f = new File(chooser);
            }
        }

        JFileChooser chooser1 = new JFileChooser(f);
        if(!f.isDirectory()) {
            chooser1.setSelectedFile(f);
        }

        int returnVal = chooser1.showOpenDialog(this);
        if(returnVal == 0) {
            cr.chosenValue = this.convertFileName(chooser1.getSelectedFile());
            cr.chosen = true;
        }

        return cr;
    }

    public String convertFileName(File selectedFile) {
        try {
            String ioe = selectedFile.getCanonicalPath();
            if(!this.useRelativePaths.isSelected()) {
                return ioe;
            } else {
                String basePath = FileServer.getFileServer().getBaseDir();
                File baseDir = new File(basePath);
                String baseCanonPath = baseDir.getCanonicalPath();
                int lenAdjust = baseCanonPath.endsWith(File.separator)?0:1;
                String resolvedPath = ioe;
                if(ioe.startsWith(baseCanonPath)) {
                    resolvedPath = ioe.substring(basePath.length() + lenAdjust);
                } else {
                    ArrayList selFilePathComponents = new ArrayList();
                    ArrayList basePathComponents = new ArrayList();
                    splitFilePath(selectedFile, selFilePathComponents);
                    splitFilePath(baseDir, basePathComponents);
                    int minLen = Math.min(basePathComponents.size(), selFilePathComponents.size());
                    int lastMatchedIndex = -1;

                    for(int sbPath = 0; sbPath < minLen && basePathComponents.get(sbPath).equals(selFilePathComponents.get(sbPath)); ++sbPath) {
                        ++lastMatchedIndex;
                    }

                    if(lastMatchedIndex > 1) {
                        StringBuffer var16 = new StringBuffer();

                        for(int first = lastMatchedIndex + 1; first < basePathComponents.size(); ++first) {
                            var16.append("..").append(File.separator);
                        }

                        boolean var17 = true;

                        for(int selPathIndex = lastMatchedIndex + 1; selPathIndex < selFilePathComponents.size(); ++selPathIndex) {
                            if(!var17) {
                                var16.append(File.separator);
                            }

                            var17 = false;
                            var16.append((String)selFilePathComponents.get(selPathIndex));
                        }

                        resolvedPath = var16.toString();
                    }
                }

                return resolvedPath;
            }
        } catch (IOException var15) {
            log.error("Unable to resolve attachment path", var15);
            return selectedFile.getAbsolutePath();
        }
    }

    private static void splitFilePath(File f, ArrayList al) throws IOException {
        String fullName = f.getCanonicalPath();

        for(File parent = null; (parent = f.getParentFile()) != null; f = parent) {
            String parentFullName = parent.getCanonicalPath();
            int deltaLen = parentFullName.endsWith(File.separator)?0:File.separator.length();
            String pathComponent = fullName.substring(parentFullName.length() + deltaLen);
            fullName = parentFullName;
            al.add(0, pathComponent);
            if(deltaLen == 0) {
                al.add(0, parentFullName);
                return;
            }
        }

    }

    private void updateFromSelection(int index) {
        AttachmentDefinition atd = this.soapTableModel.getAttachment(index);
        if(atd != null) {
            this.attachmentName.setText(atd.attachment);
            this.contentId.setText(atd.contentID);
            this.attachmentContentType.setSelectedItem(atd.contentType);
            this.attachmentType.setSelectedItem(atd.type == 1?SOAPAttachmentTableModel.TYPE_RESOURCE:SOAPAttachmentTableModel.TYPE_VALUE);
        }

    }

    public void configure(TestElement el) {
        super.configure(el);
        CustomSOAPSampler sampler = (CustomSOAPSampler)el;
        this.urlField.switchDocument(this, sampler, sampler.getURLData());
        this.firstWithSoapProtocolVersion.setSelectedItem(sampler.getSoapProtocolVersion());
        this.soapXml.switchDocument(this, sampler, sampler.getXmlData());
        this.soapTableModel.clear();
        this.soapTableModel.replace(sampler.getAttachments());
        this.attachmentSelContentId.setText(sampler.getAttachmentAsResponseContentID());
        this.attachmentSelContentType.setSelectedItem(sampler.getAttachmentAsResponseContentType());
        this.firstWithContentType.setSelected(sampler.getAttachmentAsResponseMode() == 0);
        this.withContentId.setSelected(sampler.getAttachmentAsResponseMode() == 1);
        this.treatAttachmentAsResponseCbx.setSelected(sampler.getTreatAttachmentAsResponse());
        this.useRelativePaths.setSelected(sampler.getUseRelativePaths());
        this.updateAttachmentReferences.setSelected(sampler.getUpdateAttachmentReferences());
        this.fireActionEvent(this.withContentId);
        this.fireActionEvent(this.firstWithContentType);
        this.fireActionEvent(this.treatAttachmentAsResponseCbx);
    }

    private void fireActionEvent(AbstractButton btn) {
        ActionListener[] aci = btn.getActionListeners();

        for(int i = 0; i < aci.length; ++i) {
            ActionEvent ev = new ActionEvent(btn, 0, "");
            aci[i].actionPerformed(ev);
        }

    }

    public Dimension getPreferredSize() {
        return this.getMinimumSize();
    }

    static {
        attachmentTypes = new String[]{SOAPAttachmentTableModel.TYPE_RESOURCE, SOAPAttachmentTableModel.TYPE_VALUE};
    }

    public static class SelectionListener implements ListSelectionListener {
        JTable table;
        CustomSOAPSamplerGui gui;

        SelectionListener(JTable table, CustomSOAPSamplerGui gui) {
            this.table = table;
            this.gui = gui;
        }

        public void valueChanged(ListSelectionEvent e) {
            if(e.getSource() == this.table.getSelectionModel() && this.table.getRowSelectionAllowed()) {
                this.gui.updateFromSelection(this.table.getSelectedRow());
            }

        }
    }

    public static class ChooseResult {
        boolean chosen;
        String chosenValue;

        public ChooseResult() {
        }
    }

    public static class ConditionalFileBrowseCellEditor extends AbstractCellEditor implements TableCellEditor {
        private static final long serialVersionUID = -4864576806296680276L;
        private CustomSOAPSamplerGui gui;
        private JPanel browseView;
        private JTextField textField;
        private JTextField textView;
        private JButton button;
        private int typeColumn;
        private Object colValue;
        private JTextField currentEditor = null;

        public ConditionalFileBrowseCellEditor(CustomSOAPSamplerGui gui, int textFieldSize, String buttonTitle, int typeColumn, Object colValue) {
            this.gui = gui;
            this.colValue = colValue;
            this.browseView = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = 1;
            gbc.anchor = 11;
            this.textField = new JTextField(textFieldSize);
            this.button = new JButton(buttonTitle);
            this.browseView.add(this.textField, gbc);
            gbc.gridwidth = 0;
            gbc.fill = 0;
            gbc.weightx = 0.0D;
            this.browseView.add(this.button, gbc);
            this.button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    CustomSOAPSamplerGui.ChooseResult cr = ConditionalFileBrowseCellEditor.this.getGui().chooseFile(ConditionalFileBrowseCellEditor.this.textField.getText());
                    if(cr.chosen) {
                        ConditionalFileBrowseCellEditor.this.textField.setText(cr.chosenValue);
                    }

                }
            });
            this.textView = new JTextField(textFieldSize);
        }

        private CustomSOAPSamplerGui getGui() {
            return this.gui;
        }

        public int getPreferredRowHeight() {
            return this.button.getPreferredSize().height;
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {
            this.textField.setText((String)value);
            this.textView.setText((String)value);
            if(table.getModel().getValueAt(rowIndex, this.typeColumn).equals(this.colValue)) {
                this.currentEditor = this.textField;
                return this.browseView;
            } else {
                this.currentEditor = this.textView;
                return this.textView;
            }
        }

        public Object getCellEditorValue() {
            return this.currentEditor != null?this.currentEditor.getText():this.textField.getText();
        }
    }

    public static class ComboBoxTableCellEditor extends AbstractCellEditor implements TableCellEditor {
        private static final long serialVersionUID = -4864576806296680276L;
        private JComboBox component;
        private String[] values;

        public ComboBoxTableCellEditor(String[] values, boolean editable) {
            this.values = values;
            this.component = new JComboBox(this.values);
            this.component.setEditable(editable);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {
            this.component.setSelectedItem((String)value);
            return this.component;
        }

        public Object getCellEditorValue() {
            return this.component.getSelectedItem();
        }
    }
}
