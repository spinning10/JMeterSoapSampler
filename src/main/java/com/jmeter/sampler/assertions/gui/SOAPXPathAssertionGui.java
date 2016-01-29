package com.jmeter.sampler.assertions.gui;


import com.jmeter.sampler.assertions.SOAPXPathAssertion;
import com.jmeter.sampler.util.SOAPMessages;
import com.jmeter.protocol.soap.control.gui.CustomSOAPSamplerGui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.apache.jmeter.assertions.gui.AbstractAssertionGui;
import org.apache.jmeter.assertions.gui.XMLConfPanel;
import org.apache.jmeter.testelement.TestElement;

public class SOAPXPathAssertionGui extends AbstractAssertionGui {
    private static final long serialVersionUID = 7912496111524407199L;
    private XPathNSAwarePanel xpath;
    private XMLConfPanel xml;
    private JRadioButton soapEnvelopeRb;
    private JRadioButton anyAttachmentRb;
    private JRadioButton eachAttachmentsRb;
    private JCheckBox checkContentIDCheck;
    private JTextField checkContentIDText;
    private JCheckBox checkContentTypeCheck;
    private JComboBox checkContentTypeCbx;
    private JTable namespaceMap;
    private SOAPXPathAssertionGui.NamespaceMapTableModel model = new SOAPXPathAssertionGui.NamespaceMapTableModel();

    public SOAPXPathAssertionGui() {
        this.init();
    }

    public String getStaticLabel() {
        return SOAPMessages.getResString(this.getLabelResource());
    }

    public String getLabelResource() {
        return "soap_xpath_assertion_title";
    }

    public TestElement createTestElement() {
        SOAPXPathAssertion el = new SOAPXPathAssertion();
        this.modifyTestElement(el);
        return el;
    }

    public String getXPathAttributesTitle() {
        return SOAPMessages.getResString("soap_xpath_assertion_test");
    }

    public void configure(TestElement el) {
        super.configure(el);
        SOAPXPathAssertion assertion = (SOAPXPathAssertion)el;
        this.xpath.setXPath(assertion, assertion.getXPathString());
        this.xpath.setNegated(assertion.isNegated());
        this.xml.configure(assertion);
        this.checkContentIDCheck.setSelected(assertion.isCheckContentID());
        this.checkContentIDText.setText(assertion.getContentID());
        this.checkContentTypeCheck.setSelected(assertion.isCheckContentType());
        this.checkContentTypeCbx.setSelectedItem(assertion.getContentType());
        int mode = assertion.getCheckMode();
        switch(mode) {
            case 0:
                this.soapEnvelopeRb.setSelected(true);
                break;
            case 1:
                this.anyAttachmentRb.setSelected(true);
                break;
            case 2:
                this.eachAttachmentsRb.setSelected(true);
                break;
            default:
                this.soapEnvelopeRb.setSelected(true);
        }

        assertion.getNamespaceMap(this.model.getNamespaceMap());
        this.model.initFromTreeMap();
        this.fireActionEvent(this.checkContentIDCheck);
        this.fireActionEvent(this.checkContentTypeCheck);
        this.fireActionEvent(this.soapEnvelopeRb);
        this.fireActionEvent(this.anyAttachmentRb);
        this.fireActionEvent(this.eachAttachmentsRb);
    }

    private void init() {
        this.setLayout(new BorderLayout());
        this.setBorder(this.makeBorder());
        this.add(this.makeTitlePanel(), "North");
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new GridBagLayout());
        GridBagConstraints mainConstraints = new GridBagConstraints();
        mainConstraints.weightx = 1.0D;
        mainConstraints.weighty = 0.0D;
        mainConstraints.anchor = 18;
        mainConstraints.fill = 2;
        mainConstraints.gridwidth = 0;
        JPanel sizePanel = new JPanel(new BorderLayout());
        sizePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        sizePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), this.getXPathAttributesTitle()));
        this.xpath = new XPathNSAwarePanel();
        this.xpath.setNamespaceMap(this.model.getNamespaceMap());
        sizePanel.add(this.xpath);
        this.xml = new XMLConfPanel();
        this.xml.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), SOAPMessages.getResString("soap_xpath_assertion_option")));
        rootPanel.add(this.xml, mainConstraints);
        rootPanel.add(sizePanel, mainConstraints);
        JLabel evaluateAssertionLabel = new JLabel(SOAPMessages.getResString("soap_xpath_assertion_match_label"));
        this.soapEnvelopeRb = new JRadioButton(SOAPMessages.getResString("soap_xpath_assertion_type_envelope"));
        this.anyAttachmentRb = new JRadioButton(SOAPMessages.getResString("soap_xpath_assertion_type_anyattachment"));
        this.eachAttachmentsRb = new JRadioButton(SOAPMessages.getResString("soap_xpath_assertion_type_eachattachment"));
        this.soapEnvelopeRb.setSelected(true);
        ButtonGroup rbg = new ButtonGroup();
        rbg.add(this.soapEnvelopeRb);
        rbg.add(this.anyAttachmentRb);
        rbg.add(this.eachAttachmentsRb);
        this.soapEnvelopeRb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(SOAPXPathAssertionGui.this.soapEnvelopeRb.isSelected()) {
                    SOAPXPathAssertionGui.this.checkContentIDCheck.setEnabled(false);
                    SOAPXPathAssertionGui.this.checkContentIDText.setEnabled(false);
                    SOAPXPathAssertionGui.this.checkContentTypeCheck.setEnabled(false);
                    SOAPXPathAssertionGui.this.checkContentTypeCbx.setEnabled(false);
                }

            }
        });
        this.anyAttachmentRb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(SOAPXPathAssertionGui.this.anyAttachmentRb.isSelected()) {
                    SOAPXPathAssertionGui.this.checkContentIDCheck.setEnabled(true);
                    SOAPXPathAssertionGui.this.checkContentIDText.setEnabled(SOAPXPathAssertionGui.this.checkContentIDCheck.isSelected());
                    SOAPXPathAssertionGui.this.checkContentTypeCheck.setEnabled(true);
                    SOAPXPathAssertionGui.this.checkContentTypeCbx.setEnabled(SOAPXPathAssertionGui.this.checkContentTypeCheck.isSelected());
                }

            }
        });
        this.eachAttachmentsRb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(SOAPXPathAssertionGui.this.eachAttachmentsRb.isSelected()) {
                    SOAPXPathAssertionGui.this.checkContentIDCheck.setEnabled(true);
                    SOAPXPathAssertionGui.this.checkContentIDText.setEnabled(SOAPXPathAssertionGui.this.checkContentIDCheck.isSelected());
                    SOAPXPathAssertionGui.this.checkContentTypeCheck.setEnabled(true);
                    SOAPXPathAssertionGui.this.checkContentTypeCbx.setEnabled(SOAPXPathAssertionGui.this.checkContentTypeCheck.isSelected());
                }

            }
        });
        this.checkContentIDCheck = new JCheckBox(SOAPMessages.getResString("soap_xpath_assertion_check_att_contentid"));
        this.checkContentIDText = new JTextField(30);
        this.checkContentIDCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(SOAPXPathAssertionGui.this.checkContentIDCheck.isSelected()) {
                    SOAPXPathAssertionGui.this.checkContentIDText.setEnabled(SOAPXPathAssertionGui.this.checkContentIDCheck.isEnabled());
                } else {
                    SOAPXPathAssertionGui.this.checkContentIDText.setEnabled(false);
                }

            }
        });
        this.checkContentTypeCheck = new JCheckBox(SOAPMessages.getResString("soap_xpath_assertion_check_att_contenttype"));
        this.checkContentTypeCbx = new JComboBox(CustomSOAPSamplerGui.contentTypes);
        this.checkContentTypeCbx.setEditable(true);
        this.checkContentTypeCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(SOAPXPathAssertionGui.this.checkContentTypeCheck.isSelected()) {
                    SOAPXPathAssertionGui.this.checkContentTypeCbx.setEnabled(SOAPXPathAssertionGui.this.checkContentTypeCheck.isEnabled());
                } else {
                    SOAPXPathAssertionGui.this.checkContentTypeCbx.setEnabled(false);
                }

            }
        });
        JPanel soapOptionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = 0;
        gbc.anchor = 18;
        gbc.weighty = 0.0D;
        JPanel evalCbxPanel = new JPanel(new FlowLayout());
        evalCbxPanel.add(evaluateAssertionLabel);
        evalCbxPanel.add(this.soapEnvelopeRb);
        evalCbxPanel.add(this.anyAttachmentRb);
        evalCbxPanel.add(this.eachAttachmentsRb);
        gbc.gridwidth = 0;
        gbc.weightx = 1.0D;
        soapOptionsPanel.add(evalCbxPanel, gbc);
        JPanel chkContentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints chkPanelGbc = new GridBagConstraints();
        chkPanelGbc.anchor = 18;
        chkPanelGbc.weighty = 0.0D;
        chkPanelGbc.fill = 0;
        chkPanelGbc.weightx = 0.0D;
        chkPanelGbc.gridwidth = -1;
        chkContentPanel.add(this.checkContentIDCheck, chkPanelGbc);
        chkPanelGbc.weightx = 0.0D;
        chkPanelGbc.fill = 0;
        chkPanelGbc.gridwidth = 0;
        chkContentPanel.add(this.checkContentIDText, chkPanelGbc);
        chkPanelGbc.fill = 0;
        chkPanelGbc.weightx = 0.0D;
        chkPanelGbc.gridwidth = -1;
        chkContentPanel.add(this.checkContentTypeCheck, chkPanelGbc);
        chkPanelGbc.weightx = 1.0D;
        chkPanelGbc.fill = 0;
        chkPanelGbc.gridwidth = 0;
        chkContentPanel.add(this.checkContentTypeCbx, chkPanelGbc);
        gbc.fill = 2;
        soapOptionsPanel.add(chkContentPanel, gbc);
        this.namespaceMap = new JTable(this.model);
        JScrollPane scp = new JScrollPane(this.namespaceMap, 22, 30);
        Dimension prefSize = scp.getPreferredSize();
        prefSize.height = 75;
        scp.setPreferredSize(prefSize);
        Dimension minSize = scp.getMinimumSize();
        minSize.height = 75;
        scp.setMinimumSize(minSize);
        JButton addBtn = new JButton(SOAPMessages.getResString("soap_xpath_assertion_addbtn"));
        JButton removeBtn = new JButton(SOAPMessages.getResString("soap_xpath_assertion_removebtn"));
        JButton addDefault = new JButton(SOAPMessages.getResString("soap_xpath_assertion_adddefbtn"));
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(addBtn);
        btnPanel.add(addDefault);
        btnPanel.add(removeBtn);
        gbc.weightx = 1.0D;
        gbc.gridwidth = 0;
        gbc.weighty = 1.0D;
        gbc.fill = 1;
        gbc.gridheight = -1;
        soapOptionsPanel.add(scp, gbc);
        gbc.weightx = 1.0D;
        gbc.weighty = 0.0D;
        gbc.gridheight = 0;
        gbc.gridwidth = 0;
        gbc.anchor = 10;
        gbc.fill = 0;
        soapOptionsPanel.add(btnPanel, gbc);
        soapOptionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), SOAPMessages.getResString("soap_xpath_assertion_soap_options")));
        mainConstraints.gridheight = 0;
        mainConstraints.weighty = 1.0D;
        mainConstraints.fill = 1;
        rootPanel.add(soapOptionsPanel, mainConstraints);
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                SOAPXPathAssertionGui.this.model.add();
            }
        });
        removeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = SOAPXPathAssertionGui.this.namespaceMap.getSelectedRow();
                int[] selRows = SOAPXPathAssertionGui.this.namespaceMap.getSelectedRows();

                int selRow;
                for(selRow = selRows.length - 1; selRow >= 0; --selRow) {
                    SOAPXPathAssertionGui.this.model.remove(selRows[selRow]);
                }

                selRow = row;
                if(row >= SOAPXPathAssertionGui.this.model.getRowCount()) {
                    selRow = SOAPXPathAssertionGui.this.model.getRowCount() - 1;
                }

                if(selRow >= 0) {
                    SOAPXPathAssertionGui.this.namespaceMap.setRowSelectionInterval(selRow, selRow);
                }

            }
        });
        addDefault.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SOAPXPathAssertionGui.this.model.add("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
                SOAPXPathAssertionGui.this.model.add("eb", "http://www.ebxml.org/namespaces/messageHeader");
                SOAPXPathAssertionGui.this.model.add("xlink", "http://www.w3.org/1999/xlink");
                SOAPXPathAssertionGui.this.model.add("wsse", "http://schemas.xmlsoap.org/ws/2002/12/secext");
                SOAPXPathAssertionGui.this.model.add("xsd", "http://www.w3.org/1999/XMLSchema");
            }
        });
        this.add(rootPanel, "Center");
    }

    public void modifyTestElement(TestElement el) {
        super.configureTestElement(el);
        if(el instanceof SOAPXPathAssertion) {
            SOAPXPathAssertion assertion = (SOAPXPathAssertion)el;
            assertion.setNegated(this.xpath.isNegated());
            assertion.setXPathString(this.xpath.getXPath());
            this.xml.modifyTestElement(assertion);
            assertion.setCheckContentID(this.checkContentIDCheck.isSelected());
            assertion.setContentID(this.checkContentIDText.getText());
            assertion.setCheckContentType(this.checkContentTypeCheck.isSelected());
            assertion.setContentType((String)this.checkContentTypeCbx.getSelectedItem());
            byte mode = 0;
            if(this.soapEnvelopeRb.isSelected()) {
                mode = 0;
            } else if(this.anyAttachmentRb.isSelected()) {
                mode = 1;
            } else if(this.eachAttachmentsRb.isSelected()) {
                mode = 2;
            }

            assertion.setCheckMode(mode);
            assertion.setNamespaceMap(this.model.getNamespaceMap());
            this.fireActionEvent(this.checkContentIDCheck);
            this.fireActionEvent(this.checkContentTypeCheck);
            this.fireActionEvent(this.soapEnvelopeRb);
            this.fireActionEvent(this.anyAttachmentRb);
            this.fireActionEvent(this.eachAttachmentsRb);
        }

    }

    public void clearGui() {
        super.clearGui();
        this.xpath.clearXPath("/");
        this.xpath.setNegated(false);
        this.xml.setDefaultValues();
        this.checkContentIDCheck.setSelected(false);
        this.checkContentIDText.setText("");
        this.checkContentTypeCheck.setSelected(false);
        this.checkContentTypeCbx.setSelectedItem("text/xml");
        this.soapEnvelopeRb.setSelected(true);
    }

    private void fireActionEvent(AbstractButton btn) {
        ActionListener[] aci = btn.getActionListeners();

        for(int i = 0; i < aci.length; ++i) {
            ActionEvent ev = new ActionEvent(btn, 0, "");
            aci[i].actionPerformed(ev);
        }

    }

    public static class NamespaceMapTableModel implements TableModel {
        private ArrayList listeners = new ArrayList();
        private ArrayList namespaceMap = new ArrayList();
        private TreeMap namespaceTree = new TreeMap();

        public NamespaceMapTableModel() {
        }

        public void addTableModelListener(TableModelListener listener) {
            this.listeners.add(listener);
        }

        public TreeMap getNamespaceMap() {
            return this.namespaceTree;
        }

        public void initFromTreeMap() {
            this.namespaceMap.clear();
            Iterator it = this.namespaceTree.keySet().iterator();

            while(it.hasNext()) {
                String prefix = (String)it.next();
                String uri = (String)this.namespaceTree.get(prefix);
                this.namespaceMap.add(new SOAPXPathAssertionGui.NamespaceMapElement(prefix, uri));
            }

            this.postTableEvent();
        }

        public Class getColumnClass(int col) {
            return String.class;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int col) {
            return col == 0? SOAPMessages.getResString("soap_xpath_assertion_namespace_prefix"): SOAPMessages.getResString("soap_xpath_assertion_namespace_uri");
        }

        public int getRowCount() {
            return this.namespaceMap.size();
        }

        public Object getValueAt(int row, int col) {
            if(row < this.namespaceMap.size() && row >= 0 && !(col < 0 | col > 1)) {
                SOAPXPathAssertionGui.NamespaceMapElement elem = (SOAPXPathAssertionGui.NamespaceMapElement)this.namespaceMap.get(row);
                return col == 0?elem.prefix:elem.uri;
            } else {
                return null;
            }
        }

        public boolean isCellEditable(int arg0, int arg1) {
            return true;
        }

        public void removeTableModelListener(TableModelListener listener) {
            this.listeners.remove(listener);
        }

        public void clear() {
            this.namespaceMap.clear();
            this.namespaceTree.clear();
        }

        public void setValueAt(Object value, int row, int col) {
            if(row < this.namespaceMap.size() && row >= 0 && !(col < 0 | col > 1)) {
                SOAPXPathAssertionGui.NamespaceMapElement elem = (SOAPXPathAssertionGui.NamespaceMapElement)this.namespaceMap.get(row);
                if(col == 0) {
                    this.namespaceTree.remove(elem.prefix);
                    elem.prefix = (String)value;
                } else {
                    elem.uri = (String)value;
                }

                this.namespaceTree.put(elem.prefix, elem.uri);
                this.postTableEvent();
            }
        }

        public void add() {
            this.namespaceMap.add(new SOAPXPathAssertionGui.NamespaceMapElement());
            this.postTableEvent();
        }

        public void add(String prefix, String uri) {
            this.namespaceMap.add(new SOAPXPathAssertionGui.NamespaceMapElement(prefix, uri));
            this.namespaceTree.put(prefix, uri);
            this.postTableEvent();
        }

        public void remove(int index) {
            if(index >= 0 && index < this.namespaceMap.size()) {
                SOAPXPathAssertionGui.NamespaceMapElement elem = (SOAPXPathAssertionGui.NamespaceMapElement)this.namespaceMap.get(index);
                this.namespaceTree.remove(elem.prefix);
                this.namespaceMap.remove(index);
                this.postTableEvent();
            }
        }

        private void postTableEvent() {
            TableModelEvent event = new TableModelEvent(this);
            Iterator listIt = this.listeners.iterator();

            while(listIt.hasNext()) {
                ((TableModelListener)listIt.next()).tableChanged(event);
            }

        }
    }

    public static class NamespaceMapElement {
        public String prefix;
        public String uri;

        public NamespaceMapElement() {
            this.prefix = "";
            this.uri = "";
        }

        public NamespaceMapElement(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
        }
    }
}

