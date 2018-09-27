package com.jmeter.protocol.soap.sampler;


import com.jmeter.sampler.util.SOAPUtils;
import com.jmeter.sampler.util.SOAPMessages;
import com.jmeter.sampler.util.StringDataSource;
import com.jmeter.protocol.soap.control.gui.AttachmentDefinition;

import java.awt.Component;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import javax.activation.DataHandler;
import javax.swing.JOptionPane;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.apache.log.Logger;

public class CustomSOAPSampler extends AbstractSampler {
    private static final long serialVersionUID = -2412344727364753799L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    public static final String XML_DATA = "HTTPSamper.xml_data";
    public static final String URL_DATA = "SoapSampler.URL_DATA";
    private static final String ATTACHMENT_COUNT = "SOAPAttachmentCount";
    private static final String ATTACHMENT_DATA = "SOAPAttachmentData";
    private static final String ATTACHMENT_AS_RESPONSE = "SOAPAttachmentAsResponse";
    private static final String ATTACHMENT_RESPONSE_MODE = "SOAPAttachmentAsResponseMode";
    private static final String ATTACHMENT_RESPONSE_CT = "SOAPAttachmentAsResponseCT";
    private static final String ATTACHMENT_RESPONSE_CID = "SOAPAttachmentAsResponseCID";
    private static final String USE_RELATIVE_PATHS = "SOAPUseRelativePaths";
    private static final String UPDATE_ATTACHMENT_REFS = "SOAPUpdateAttachmentReferences";
    public static final int ATTACHMENT_AS_RESPONSE_CONTENTTYPE = 0;
    public static final int ATTACHMENT_AS_RESPONSE_CONTENTID = 1;
    private transient AuthManager authManager;
    private transient CookieManager cookieManager;
    private transient HeaderManager headerManager;

    public CustomSOAPSampler() {
    }

    public void setXmlData(String data) {
        this.setProperty("HTTPSamper.xml_data", data);
    }

    public String getXmlData() {
        return this.getPropertyAsString("HTTPSamper.xml_data");
    }

    public void setAttachmentAsResponseMode(int mode) {
        this.setProperty("SOAPAttachmentAsResponseMode", "" + mode);
    }

    public int getAttachmentAsResponseMode() {
        return this.getPropertyAsInt("SOAPAttachmentAsResponseMode");
    }

    public void setAttachmentAsResponseContentType(String contentType) {
        this.setProperty("SOAPAttachmentAsResponseCT", contentType);
    }

    public String getAttachmentAsResponseContentType() {
        return this.getPropertyAsString("SOAPAttachmentAsResponseCT");
    }

    public String getSoapProtocolVersion() {
        return this.getPropertyAsString("SoapProtocolVersion");
    }

    public void setAttachmentAsResponseContentID(String contentId) {
        this.setProperty("SOAPAttachmentAsResponseCID", contentId);
    }

    public String getAttachmentAsResponseContentID() {
        return this.getPropertyAsString("SOAPAttachmentAsResponseCID");
    }

    AttachmentDefinition getAttachmentDefinition() {
        return new AttachmentDefinition();
    }

    public void setAttachments(ArrayList attachments) {
        this.setProperty("SOAPAttachmentCount", "" + attachments.size());

        for(int i = 0; i < attachments.size(); ++i) {
            AttachmentDefinition atd = (AttachmentDefinition)attachments.get(i);
            String baseName = "SOAPAttachmentData_" + i + "_";
            this.setProperty(baseName + "attachment", atd.attachment);
            this.setProperty(baseName + "contentid", atd.contentID);
            this.setProperty(baseName + "contenttype", atd.contentType);
            this.setProperty(baseName + "type", "" + atd.type);
        }

    }

    public ArrayList getAttachments() {
        ArrayList attachments = new ArrayList();
        int count = this.getPropertyAsInt("SOAPAttachmentCount");

        for(int i = 0; i < count; ++i) {
            AttachmentDefinition atd = new AttachmentDefinition();
            String baseName = "SOAPAttachmentData_" + i + "_";
            atd.attachment = this.getPropertyAsString(baseName + "attachment");
            atd.contentID = this.getPropertyAsString(baseName + "contentid");
            atd.contentType = this.getPropertyAsString(baseName + "contenttype");
            atd.type = this.getPropertyAsInt(baseName + "type");
            attachments.add(atd);
        }

        return attachments;
    }

    public String getURLData() {
        return this.getPropertyAsString("SoapSampler.URL_DATA");
    }

    public void setURLData(String url) {
        this.setProperty("SoapSampler.URL_DATA", url);
    }

    public void setSoapProtocolVersion(String protocolValue) {
        this.setProperty("SoapProtocolVersion", protocolValue);
    }

    public void setTreatAttachmentAsResponse(boolean treat) {
        this.setProperty("SOAPAttachmentAsResponse", String.valueOf(treat));
    }

    public boolean getTreatAttachmentAsResponse() {
        return this.getPropertyAsBoolean("SOAPAttachmentAsResponse");
    }

    public void setUseRelativePaths(boolean use) {
        this.setProperty("SOAPUseRelativePaths", use, true);
    }

    public boolean getUseRelativePaths() {
        return this.getPropertyAsBoolean("SOAPUseRelativePaths", true);
    }

    public void setUpdateAttachmentReferences(boolean use) {
        this.setProperty("SOAPUpdateAttachmentReferences", use, true);
    }

    public boolean getUpdateAttachmentReferences() {
        return this.getPropertyAsBoolean("SOAPUpdateAttachmentReferences", true);
    }

    public SampleResult sample(Entry e) {
        String url = this.getURLData();
        String name = this.getName();
        SOAPSampleResult result = new SOAPSampleResult();
        //result.setSampleLabel(url);
        result.setSampleLabel(name);
        result.sampleStart();

        try {
            MessageFactory ecx = null;
            if (getSoapProtocolVersion().equals("1_2")) {
                ecx = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            } else if (getSoapProtocolVersion().equals("1_1")) {
                ecx = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
            } else {
                System.out.println("ERROR: Not allowed value in soap protocol");
            }
            SOAPMessage message = ecx.createMessage();
            String xmlSoap = this.getXmlData();
            StringReader sr = new StringReader(xmlSoap);
            StreamSource ssrc = new StreamSource(sr);
            SOAPPart soapPart = message.getSOAPPart();
            soapPart.setContent(ssrc);
            ArrayList attachments = this.getAttachments();
            Iterator it = attachments.iterator();

            while(true) {
                while(it.hasNext()) {
                    AttachmentDefinition baos = (AttachmentDefinition)it.next();
                    AttachmentPart fullRequest = null;
                    String endpoint;
                    if(baos.type == 1) {
                        String soapConnectionFactory = baos.attachment;
                        if(soapConnectionFactory.indexOf("://") < 0) {
                            File connection = new File(soapConnectionFactory);
                            if(!connection.exists()) {
                                endpoint = FileServer.getFileServer().getBaseDir();
                                if(!endpoint.endsWith(File.separator)) {
                                    endpoint = endpoint + File.separator;
                                }

                                soapConnectionFactory = endpoint + soapConnectionFactory;
                                connection = new File(soapConnectionFactory);
                            }

                            if(!connection.exists()) {
                                log.warn("Ignoring invalid attachment: " + baos.attachment);
                                continue;
                            }

                            soapConnectionFactory = "file:///" + connection.getAbsolutePath();
                        }

                        URL connection1 = new URL(soapConnectionFactory);
                        DataHandler endpoint1 = new DataHandler(connection1);
                        String response = baos.contentType.equals("auto")?endpoint1.getContentType():baos.contentType;
                        if(!response.startsWith("text/") && !response.endsWith("/xml")) {
                            fullRequest = message.createAttachmentPart(endpoint1);
                            fullRequest.setContentId("<" + baos.contentID + ">");
                            fullRequest.setMimeHeader("Content-Transfer-Encoding", "binary");
                            fullRequest.setMimeHeader("Content-Disposition", "attachment");
                            if(!baos.contentType.equals("auto")) {
                                fullRequest.setContentType(baos.contentType);
                            }
                        } else {
                            String respSoapPart = SOAPUtils.dataHandlerToString(endpoint1);
                            CompoundVariable transformerFactory = new CompoundVariable(respSoapPart);
                            String transformer = transformerFactory.execute();
                            StringDataSource sourceContent = new StringDataSource(transformer, response);
                            DataHandler sw = new DataHandler(sourceContent);
                            fullRequest = message.createAttachmentPart(sw);
                            fullRequest.setContentId(baos.contentID);
                        }
                    } else {
                        CompoundVariable soapConnectionFactory1 = new CompoundVariable(baos.attachment);
                        String connection2 = soapConnectionFactory1.execute();
                        endpoint = null;
                        StringDataSource endpoint2;
                        if(baos.contentType.equals("auto")) {
                            endpoint2 = new StringDataSource(connection2);
                        } else {
                            endpoint2 = new StringDataSource(connection2, baos.contentType);
                        }

                        DataHandler response1 = new DataHandler(endpoint2);
                        fullRequest = message.createAttachmentPart(response1);
                        fullRequest.setContentId(baos.contentID);
                    }

                    message.addAttachmentPart(fullRequest);
                }

                if(this.getUpdateAttachmentReferences()) {
                    this.updateAttachmentReferences(message);
                }

                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                message.writeTo(baos1);
                baos1.close();
                String fullRequest1 = url + "\n" + baos1.toString();
                if(log.isDebugEnabled()) {
                    log.debug("Full request: \n" + fullRequest1);
                }

                result.setSamplerData(fullRequest1);
                SOAPConnectionFactory soapConnectionFactory2 = SOAPConnectionFactory.newInstance();
                SOAPConnection connection3 = soapConnectionFactory2.createConnection();
                URL endpoint3 = new URL(url);
                addAdditionalHeaders(message, endpoint3);
                SOAPMessage response2 = connection3.call(message, endpoint3);
                result.sampleEnd();
                if(log.isDebugEnabled()) {
                    ByteArrayOutputStream respSoapPart1 = new ByteArrayOutputStream();
                    response2.writeTo(respSoapPart1);
                    log.debug("Full response: \n" + respSoapPart1.toString());
                }

                SOAPPart respSoapPart2 = response2.getSOAPPart();
                TransformerFactory transformerFactory1 = TransformerFactory.newInstance();
                Transformer transformer1 = transformerFactory1.newTransformer();
                Source sourceContent1 = respSoapPart2.getContent();
                StringWriter sw1 = new StringWriter();
                StreamResult strResult = new StreamResult(sw1);
                transformer1.transform(sourceContent1, strResult);
                result.setSOAPEnvelope(sw1.toString());
                String responseHeaders = SOAPUtils.headersToString(respSoapPart2.getAllMimeHeaders());
                result.setResponseHeaders(responseHeaders);
                result.setHeadersSize(responseHeaders.length());
                SOAPBody respBody = response2.getSOAPBody();
                if(respBody.hasFault()) {
                    SOAPFault attachmentFound1 = respBody.getFault();
                    result.setResponseCode(attachmentFound1.getFaultCode() + " @ " + attachmentFound1.getFaultActor());
                    result.setResponseMessage(attachmentFound1.getFaultString());
                    result.setSuccessful(false);
                    result.setResponseData(sw1.toString().getBytes());
                    result.setDataType("text");
                    return result;
                }

                boolean attachmentFound = !this.getTreatAttachmentAsResponse();
                String respContents = attachmentFound?sw1.toString():"";
                int attachmentMode = this.getAttachmentAsResponseMode();
                String attExpectedContentType = this.getAttachmentAsResponseContentType();
                String attExpectedContentID = this.getAttachmentAsResponseContentID();
                Object rootResult = null;
                if(this.getTreatAttachmentAsResponse()) {
                    rootResult = new SampleResult();
                    ((SampleResult)rootResult).sampleStart();
                    ((SampleResult)rootResult).setContentType("text/xml");
                    ((SampleResult)rootResult).setResponseCodeOK();
                    ((SampleResult)rootResult).setResponseMessageOK();
                    ((SampleResult)rootResult).setSampleLabel(SOAPMessages.getResString("soap_complete_soap_message"));
                    ((SampleResult)rootResult).setResponseData(sw1.toString().getBytes());
                    ((SampleResult)rootResult).setDataType("text");
                    ((SampleResult)rootResult).setSuccessful(true);
                    ((SampleResult)rootResult).sampleEnd();
                    result.addSubResult((SampleResult)rootResult);
                } else {
                    rootResult = result;
                }

                Iterator attIt = response2.getAttachments();

                while(attIt.hasNext()) {
                    AttachmentPart ap = (AttachmentPart)attIt.next();
                    result.addAttachment(ap);

                    int attSize = ap.getSize();
                    //DataHandler dh = ap.getDataHandler();
                    //InputStream is = (InputStream) ap.getContent();
                    //copyInputStreamToFile(is, new File("c:/Users/userX/temp/" + ap.getContentId().replace(">", "").replace("<", "")));
                    String attachmentContentType = ap.getContentType();
                    String textRepresentation = null;
                    boolean isBinary;
                    if(!attachmentContentType.startsWith("text/") && !attachmentContentType.endsWith("/xml")) {
                        isBinary = true;
                        textRepresentation = "(binary data, size in bytes: " + attSize + ")";
                    } else {
                        isBinary = false;
                        textRepresentation = SOAPUtils.attachmentToString(ap);
                    }

                    SampleResult subResult = new SampleResult();
                    subResult.setContentType(attachmentContentType);
                    subResult.setResponseCodeOK();
                    subResult.setResponseMessageOK();
                    subResult.setSampleLabel(ap.getContentId() + " (" + ap.getContentType() + ")");
                    if (isBinary) {
                        subResult.setResponseData(ap.getRawContentBytes());
                        subResult.setDataType(SampleResult.BINARY);
                    } else {
                        subResult.setResponseData(textRepresentation);
                        subResult.setDataType(SampleResult.TEXT);
                    }
                    subResult.setSuccessful(true);
                    responseHeaders = SOAPUtils.headersToString(ap.getAllMimeHeaders());
                    subResult.setResponseHeaders(responseHeaders);
                    subResult.setHeadersSize(responseHeaders.length());
                    ((SampleResult)rootResult).addSubResult(subResult);
                    if(!attachmentFound) {
                        boolean attachmentMatched = false;
                        switch(attachmentMode) {
                            case 0:
                                attachmentMatched = attExpectedContentType.equals(ap.getContentType());
                                break;
                            case 1:
                                attachmentMatched = attExpectedContentID.equals(ap.getContentId());
                        }

                        if(attachmentMatched) {
                            respContents = textRepresentation;
                            attachmentFound = true;
                        }
                    }
                }

                result.setResponseData(respContents.getBytes());
                result.setDataType("text");
                result.setResponseCodeOK();
                result.setResponseMessageOK();
                result.setSuccessful(true);
                return result;
            }
        } catch (SOAPException var37) {
            result.sampleEnd();
            result.setResponseCode("SOAPException");
            result.setResponseMessage(var37.getMessage());
            result.setSuccessful(false);
            log.error("Exception in SOAP communication", var37);
            return result;
        } catch (MalformedURLException var38) {
            result.sampleEnd();
            result.setResponseCode("MalformedURLException");
            result.setResponseMessage(var38.getMessage());
            result.setSuccessful(false);
            log.error("Exception in SOAP communication", var38);
            return result;
        } catch (IOException var39) {
            result.sampleEnd();
            result.setResponseCode("IOException");
            result.setResponseMessage(var39.getMessage());
            result.setSuccessful(false);
            log.error("Exception in SOAP communication", var39);
            return result;
        } catch (TransformerConfigurationException var40) {
            result.sampleEnd();
            result.setResponseCode("TransformerConfigurationException");
            result.setResponseMessage(var40.getMessage());
            result.setSuccessful(false);
            log.error("Exception in SOAP communication", var40);
            return result;
        } catch (TransformerException var41) {
            result.sampleEnd();
            result.setResponseCode("TransformerException");
            result.setResponseMessage(var41.getMessage());
            result.setSuccessful(false);
            log.error("Exception in SOAP communication", var41);
            return result;
        }
    }

    private void copyInputStreamToFile( InputStream in, File file ) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateAttachmentReferences(SOAPMessage message) {
        TreeSet missingAttachments = new TreeSet();
        String ebUri = "http://www.ebxml.org/namespaces/messageHeader";
        String xlinkUri = "http://www.w3.org/1999/xlink";
        String soapUri = "http://schemas.xmlsoap.org/soap/envelope/";
        String ebManifest = "Manifest";
        String ebReference = "Reference";
        String xlinkHref = "href";
        String xlinkType = "type";
        String typeVal = "simple";
        String soapMustUnderstand = "mustUnderstand";
        String ebVersion = "version";

        AttachmentPart soapExc;
        for(Iterator apIt = message.getAttachments(); apIt.hasNext(); missingAttachments.add("cid:" + soapExc.getContentId())) {
            soapExc = (AttachmentPart)apIt.next();
            if(soapExc.getContentId().length() == 0) {
                soapExc.setContentId("attachment_" + soapExc.hashCode());
            }
        }

        try {
            ArrayList soapExc1 = new ArrayList();
            SOAPPart sp = message.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            Iterator prefIt = se.getNamespacePrefixes();
            String ebPrefix = "eb";
            String xlinkPrefix = "xlink";
            String soapPrefix = se.getPrefix();

            while(prefIt.hasNext()) {
                String manifestElem = (String)prefIt.next();
                String body = se.getNamespaceURI(manifestElem);
                if(body.equals(ebUri)) {
                    ebPrefix = manifestElem;
                } else if(body.equals(xlinkUri)) {
                    xlinkPrefix = manifestElem;
                }
            }

            SOAPElement manifestElem1 = null;
            SOAPBody body1 = se.getBody();
            QName addIt;
            Iterator elem;
            if(body1 != null) {
                addIt = new QName(ebUri, ebManifest);
                elem = body1.getChildElements(addIt);
                if(elem.hasNext()) {
                    manifestElem1 = (SOAPElement)elem.next();
                    QName contentId = new QName(ebUri, ebReference);
                    Iterator xlinkHrefName = manifestElem1.getChildElements(contentId);

                    while(xlinkHrefName.hasNext()) {
                        SOAPElement xlinkTypeName = (SOAPElement)xlinkHrefName.next();
                        String sel = xlinkTypeName.getAttributeNS(xlinkUri, xlinkHref);
                        if(!missingAttachments.contains(sel)) {
                            soapExc1.add(xlinkTypeName);
                        } else {
                            missingAttachments.remove(sel);
                        }
                    }

                    Iterator xlinkTypeName1 = soapExc1.iterator();

                    while(xlinkTypeName1.hasNext()) {
                        SOAPElement sel1 = (SOAPElement)xlinkTypeName1.next();
                        sel1.detachNode();
                    }
                }
            }

            if(missingAttachments.size() != 0) {
                if(body1 == null) {
                    body1 = se.addBody();
                }

                if(manifestElem1 == null) {
                    manifestElem1 = body1.addChildElement(ebManifest, ebPrefix, ebUri);
                    addIt = new QName(soapUri, soapMustUnderstand, soapPrefix);
                    QName elem1 = new QName(ebUri, ebVersion, ebPrefix);
                    manifestElem1.addAttribute(addIt, "1");
                    manifestElem1.addAttribute(elem1, "1.0");
                }

                Iterator addIt1 = missingAttachments.iterator();

                while(addIt1.hasNext()) {
                    elem = null;
                    String contentId1 = (String)addIt1.next();
                    SOAPElement elem2 = manifestElem1.addChildElement(ebReference, ebPrefix, ebUri);
                    QName xlinkHrefName1 = new QName(xlinkUri, xlinkHref, xlinkPrefix);
                    elem2.addAttribute(xlinkHrefName1, contentId1);
                    QName xlinkTypeName2 = new QName(xlinkUri, xlinkType, xlinkPrefix);
                    elem2.addAttribute(xlinkTypeName2, typeVal);
                }

            }
        } catch (SOAPException var29) {
            log.error("Caught exception while updating attachments", var29);
            JOptionPane.showMessageDialog((Component)null, "Unable to update attachment references, see log file for details", "Error", 0);
            throw new JMeterStopThreadException("Unable to update attachment references");
        }
    }

    protected void addAdditionalHeaders(SOAPMessage message, URL endpoint) {
        MimeHeaders headers = message.getMimeHeaders();
        if (headers == null) {
            log.error("This should never happen: Mime Headers undefined!");
            return;
        }
        if (authManager != null) {
            String header = authManager.getAuthHeaderForURL(endpoint);
            log.debug("Add auth header "+header);
            headers.setHeader("Authorization", header);
        }
        if (cookieManager != null) {
            String cookieHeader = cookieManager.getCookieHeaderForURL(endpoint);
            if (cookieHeader != null) {
                log.debug("Add cookies "+cookieHeader);
                headers.setHeader("Cookie", cookieHeader);
            }
        }
        if (headerManager != null) {
            for (int i = 0; i < headerManager.size(); i++) {
                Header header = headerManager.get(i);
                if (header.getName().trim().length() != 0) {
                    log.debug("Add header "+header);
                    headers.setHeader(header.getName(), header.getValue());
                }
            }
        }
    }

    @Override
    public void addTestElement(TestElement el) {
        if (el instanceof AuthManager) {
            authManager = (AuthManager)el;
        } else if (el instanceof CookieManager) {
            cookieManager = (CookieManager)el;
        } else if (el instanceof HeaderManager) {
            headerManager = (HeaderManager)el;
        } else {
            super.addTestElement(el);
        }
    }
}
