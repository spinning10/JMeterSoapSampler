package com.jmeter.protocol.soap.sampler;


import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.soap.AttachmentPart;
import org.apache.jmeter.samplers.SampleResult;

public class SOAPSampleResult extends SampleResult {
    private static final long serialVersionUID = 6674396447072516102L;
    private transient ArrayList soapAttachments = new ArrayList();
    private transient String soapEnvelope = "";

    public SOAPSampleResult() {
    }

    public SOAPSampleResult(long elapsed) {
        super(elapsed, true);
    }

    public void addAttachment(AttachmentPart attachment) {
        this.soapAttachments.add(attachment);
    }

    public Iterator getAttachments() {
        return this.soapAttachments.iterator();
    }

    public AttachmentPart getAttachment(String contentID) {
        Iterator it = this.soapAttachments.iterator();

        AttachmentPart ap;
        do {
            if(!it.hasNext()) {
                return null;
            }

            ap = (AttachmentPart)it.next();
        } while(!contentID.equals(ap.getContentId()));

        return ap;
    }

    public void setSOAPEnvelope(String envelope) {
        this.soapEnvelope = envelope;
    }

    public String getSOAPEnvelope() {
        return this.soapEnvelope;
    }

    public SOAPSampleResult(SOAPSampleResult res) {
        super(res);
        this.soapAttachments = res.soapAttachments;
        this.soapEnvelope = res.soapEnvelope;
    }
}
