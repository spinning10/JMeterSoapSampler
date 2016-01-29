package com.jmeter.protocol.soap.control.gui;


public class AttachmentDefinition {
    public static final int TYPE_RESOURCE = 1;
    public static final int TYPE_VARIABLE = 2;
    public String attachment;
    public String contentID;
    public String contentType;
    public int type;

    public AttachmentDefinition() {
    }

    public AttachmentDefinition duplicate() {
        AttachmentDefinition copy = new AttachmentDefinition();
        copy.attachment = this.attachment;
        copy.contentID = this.contentID;
        copy.contentType = this.contentType;
        copy.type = this.type;
        return copy;
    }
}
