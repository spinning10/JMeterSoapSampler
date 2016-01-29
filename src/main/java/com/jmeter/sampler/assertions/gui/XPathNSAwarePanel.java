package com.jmeter.sampler.assertions.gui;


import com.jmeter.sampler.config.gui.UndoableJTextField;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.XPathUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class XPathNSAwarePanel extends JPanel {
    private static final long serialVersionUID = -4429688995482863889L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private Document testDoc;
    private JCheckBox negated;
    private UndoableJTextField xpath;
    private JButton checkXPath;
    private TreeMap namespaceMap = null;
    private Element namespaceResolver = null;
    private boolean hasDefaultLayout = false;

    public XPathNSAwarePanel() {
        super(new GridBagLayout());
        this.hasDefaultLayout = true;
        this.init();
    }

    public void setNamespaceMap(TreeMap map) {
        this.namespaceMap = map;
    }

    public XPathNSAwarePanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        this.init();
    }

    public XPathNSAwarePanel(LayoutManager layout) {
        super(layout);
        this.init();
    }

    public XPathNSAwarePanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        this.init();
    }

    private void init() {
        if(this.hasDefaultLayout) {
            GridBagConstraints hbox = new GridBagConstraints();
            hbox.anchor = 18;
            hbox.weightx = 1.0D;
            hbox.weighty = 0.0D;
            hbox.fill = 2;
            hbox.gridwidth = -1;
            this.xpath = new UndoableJTextField("/", 50);
            this.xpath.initActionMap(this);
            this.add(this.xpath, hbox);
            hbox.fill = 0;
            hbox.weightx = 0.0D;
            hbox.gridwidth = 0;
            this.add(this.getCheckXPathButton(), hbox);
            this.add(this.getNegatedCheckBox(), hbox);
        } else {
            Box hbox1 = Box.createHorizontalBox();
            hbox1.add(Box.createHorizontalGlue());
            this.xpath = new UndoableJTextField("/", 50);
            this.xpath.initActionMap(this);
            hbox1.add(this.xpath);
            hbox1.add(Box.createHorizontalGlue());
            hbox1.add(this.getCheckXPathButton());
            Box vbox = Box.createVerticalBox();
            vbox.add(hbox1);
            vbox.add(Box.createVerticalGlue());
            vbox.add(this.getNegatedCheckBox());
            this.add(vbox);
        }

    }

    public String getXPath() {
        return this.xpath.getText();
    }

    public void setXPath(Object source, String xpath) {
        this.xpath.switchDocument(this, source, xpath);
    }

    public void clearXPath(String value) {
        this.xpath.clear(value);
    }

    public boolean isNegated() {
        return this.negated.isSelected();
    }

    public void setNegated(boolean negated) {
        this.negated.setSelected(negated);
    }

    public JCheckBox getNegatedCheckBox() {
        if(this.negated == null) {
            this.negated = new JCheckBox(JMeterUtils.getResString("xpath_assertion_negate"), false);
        }

        return this.negated;
    }

    public JButton getCheckXPathButton() {
        if(this.checkXPath == null) {
            this.checkXPath = new JButton(JMeterUtils.getResString("xpath_assertion_button"));
            this.checkXPath.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    XPathNSAwarePanel.this.validXPath(XPathNSAwarePanel.this.xpath.getText(), true);
                }
            });
        }

        return this.checkXPath;
    }

    public JTextField getXPathTextField() {
        if(this.xpath == null) {
            this.xpath = new UndoableJTextField(50);
            this.xpath.initActionMap(this);
        }

        return this.xpath;
    }

    public boolean isShowNegated() {
        return this.getNegatedCheckBox().isVisible();
    }

    public void setShowNegated(boolean showNegate) {
        this.getNegatedCheckBox().setVisible(showNegate);
    }

    private boolean validXPath(String xpathString, boolean showDialog) {
        String ret = null;
        boolean success = true;

        try {
            if(this.testDoc == null) {
                this.testDoc = XPathUtil.makeDocumentBuilder(false, false, false, false).newDocument();
                Element e = this.testDoc.createElement("root");
                this.testDoc.appendChild(e);
                if(this.namespaceMap != null && this.namespaceResolver == null) {
                    this.namespaceResolver = this.testDoc.createElement("NamespaceResolver");
                }
            }

            if(this.namespaceResolver != null) {
                NamedNodeMap var11 = this.namespaceResolver.getAttributes();

                for(int nsIt = var11.getLength() - 1; nsIt >= 0; --nsIt) {
                    Attr key = (Attr)var11.item(nsIt);
                    this.namespaceResolver.removeAttributeNode(key);
                }

                String uri;
                String var13;
                for(Iterator var12 = this.namespaceMap.keySet().iterator(); var12.hasNext(); this.namespaceResolver.setAttribute(var13, uri)) {
                    var13 = (String)var12.next();
                    uri = (String)this.namespaceMap.get(var13);
                    if(!var13.startsWith("xmlns:")) {
                        var13 = "xmlns:" + var13;
                    }
                }

                if(XPathAPI.eval(this.testDoc, xpathString, this.namespaceResolver) == null) {
                    log.warn("xpath eval was null ");
                    ret = "xpath eval was null";
                    success = false;
                }
            } else if(XPathAPI.eval(this.testDoc, xpathString) == null) {
                log.warn("xpath eval was null ");
                ret = "xpath eval was null";
                success = false;
            }
        } catch (ParserConfigurationException var9) {
            success = false;
            ret = var9.getLocalizedMessage();
        } catch (TransformerException var10) {
            success = false;
            ret = var10.getLocalizedMessage();
        }

        if(showDialog) {
            JOptionPane.showMessageDialog((Component)null, success?JMeterUtils.getResString("xpath_assertion_valid"):ret, success?JMeterUtils.getResString("xpath_assertion_valid"):JMeterUtils.getResString("xpath_assertion_failed"), success?1:0);
        }

        return success;
    }
}
