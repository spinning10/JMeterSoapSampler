package com.jmeter.sampler.config.gui;

import javax.swing.border.Border;

import com.jmeter.sampler.util.SOAPMessages;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;

public class VariableDefinitionPanel extends AbstractConfigGui {
    private static final long serialVersionUID = -3946995645891732845L;
    private JLabel varNameLabel;
    private UndoableJTextField variableName;
    private UndoableJTextArea variableContents;

    public VariableDefinitionPanel() {
        this.init();
    }

    public String getStaticLabel() {
        return SOAPMessages.getResString(this.getLabelResource());
    }

    public String getLabelResource() {
        return "soap_var_def_panel";
    }

    public TestElement createTestElement() {
        Arguments args = new Arguments();
        this.modifyTestElement(args);
        return args;
    }

    public void modifyTestElement(TestElement args) {
        Arguments arguments = null;
        if(args instanceof Arguments) {
            arguments = (Arguments)args;
            arguments.clear();
            Argument arg = new Argument(this.variableName.getText(), this.variableContents.getText(), "=");
            arguments.addArgument(arg);
        }

        this.configureTestElement(args);
    }

    public void configure(TestElement el) {
        super.configure(el);
        String varName = "";
        String varContents = "";
        if(el instanceof Arguments) {
            PropertyIterator iter = ((Arguments)el).iterator();
            if(iter.hasNext()) {
                Argument arg = (Argument)iter.next().getObjectValue();
                varName = arg.getName();
                varContents = arg.getValue();
            }
        }

        this.variableName.switchDocument(this, el, varName);
        this.variableContents.switchDocument(this, el, varContents);
    }

    public void clearGui() {
        super.clearGui();
        this.variableName.clear();
        this.variableContents.clear();
    }

    private void init() {
        this.setLayout(new BorderLayout(0, 5));
        this.setBorder(this.makeBorder());
        this.add(this.makeTitlePanel(), "North");
        this.add(this.createGUI(), "Center");
    }

    private JPanel createGUI() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        this.varNameLabel = new JLabel(SOAPMessages.getResString("soap_var_panel_var_label"));
        this.variableName = new UndoableJTextField(20);
        this.variableName.initActionMap(this);
        this.variableName.setName("VAR_NAME_TEXTFIELD");
        this.varNameLabel.setLabelFor(this.variableName);
        this.variableContents = new UndoableJTextArea(30, 0);
        this.variableContents.initActionMap(this);
        this.variableContents.setLineWrap(true);
        this.variableContents.setName("REQUEST_DATA");
        JPanel variableContentsPanel = new JPanel(new BorderLayout());
        variableContentsPanel.add(this.variableContents, "Center");
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.fill = 0;
        c.anchor = 12;
        c.ipadx = 10;
        gridbag.setConstraints(this.varNameLabel, c);
        c.weightx = 0.0D;
        c.anchor = 18;
        c.gridwidth = 0;
        c.ipadx = 0;
        c.weightx = 1.0D;
        c.fill = 2;
        gridbag.setConstraints(this.variableName, c);
        c.weightx = 2.0D;
        c.weighty = 1.0D;
        c.fill = 1;
        gridbag.setConstraints(variableContentsPanel, c);
        JPanel guiPanel = new JPanel(gridbag);
        guiPanel.add(this.varNameLabel);
        guiPanel.add(this.variableName);
        Border border = BorderFactory.createEtchedBorder();
        variableContentsPanel.setBorder(BorderFactory.createTitledBorder(border, SOAPMessages.getResString("soap_var_contents")));
        guiPanel.add(variableContentsPanel);
        guiPanel.setBorder(BorderFactory.createEtchedBorder());
        return guiPanel;
    }
}
