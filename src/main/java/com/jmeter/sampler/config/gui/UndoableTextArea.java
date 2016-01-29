package com.jmeter.sampler.config.gui;

import java.awt.event.ActionEvent;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

public class UndoableTextArea extends JTextArea {
    private static final long serialVersionUID = -5621249603198330705L;
    private UndoManager currentUndoManager = null;
    private TreeMap undoableDocumentMap = new TreeMap();

    public UndoableTextArea() {
    }

    public UndoableTextArea(Document doc) {
        super(doc);
    }

    public UndoableTextArea(Document doc, String text, int rows, int columns) {
        super(doc, text, rows, columns);
    }

    public UndoableTextArea(int rows, int columns) {
        super(rows, columns);
    }

    public UndoableTextArea(String text) {
        super(text);
    }

    public UndoableTextArea(String text, int rows, int columns) {
        super(text, rows, columns);
    }

    public void initActionMap(String actionMapKeyPrefix) {
        String undoActionKey = actionMapKeyPrefix + "_UNDO";
        String redoActionKey = actionMapKeyPrefix + "_REDO";
        this.getDocument().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                if(UndoableTextArea.this.currentUndoManager != null) {
                    UndoableEdit ue = e.getEdit();
                    UndoableTextArea.this.currentUndoManager.addEdit(ue);
                }

            }
        });
        InputMap im = this.getInputMap();
        ActionMap am = this.getActionMap();
        im.put(KeyStroke.getKeyStroke(90, 2, true), undoActionKey);
        am.put(undoActionKey, new AbstractAction() {
            private static final long serialVersionUID = -2864952973651930733L;

            public void actionPerformed(ActionEvent e) {
                if(UndoableTextArea.this.currentUndoManager != null && UndoableTextArea.this.currentUndoManager.canUndo()) {
                    try {
                        UndoableTextArea.this.currentUndoManager.undo();
                    } catch (CannotUndoException var3) {
                        ;
                    }
                }

            }
        });
        im.put(KeyStroke.getKeyStroke(89, 2, true), redoActionKey);
        am.put(redoActionKey, new AbstractAction() {
            private static final long serialVersionUID = -2864952973651930733L;

            public void actionPerformed(ActionEvent e) {
                if(UndoableTextArea.this.currentUndoManager != null && UndoableTextArea.this.currentUndoManager.canRedo()) {
                    try {
                        UndoableTextArea.this.currentUndoManager.redo();
                    } catch (CannotUndoException var3) {
                        ;
                    }
                }

            }
        });
    }

    public void switchDocument(Object key, String contents) {
        this.currentUndoManager = null;
        UndoableTextArea.UndoableDocument ud = (UndoableTextArea.UndoableDocument)this.undoableDocumentMap.get(key);
        if(ud == null) {
            ud = new UndoableTextArea.UndoableDocument();
            ud.undoManager = new UndoManager();
            ud.document = new PlainDocument();

            try {
                ud.document.insertString(0, contents, (AttributeSet)null);
            } catch (BadLocationException var5) {
                ;
            }

            this.undoableDocumentMap.put(key, ud);
            ud.document.addUndoableEditListener(new UndoableEditListener() {
                public void undoableEditHappened(UndoableEditEvent e) {
                    if(UndoableTextArea.this.currentUndoManager != null) {
                        UndoableEdit ue = e.getEdit();
                        UndoableTextArea.this.currentUndoManager.addEdit(ue);
                    }

                }
            });
        }

        this.setDocument(ud.document);
        this.currentUndoManager = ud.undoManager;
    }

    public void clear() {
        this.currentUndoManager = null;
        this.setDocument(new PlainDocument());
    }

    public static class UndoableDocument {
        public Document document = null;
        public UndoManager undoManager = null;

        public UndoableDocument() {
        }
    }
}
