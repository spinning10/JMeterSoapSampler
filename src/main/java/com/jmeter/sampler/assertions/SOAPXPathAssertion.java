package com.jmeter.sampler.assertions;


import com.jmeter.sampler.util.SOAPUtils;
import com.jmeter.protocol.soap.sampler.SOAPSampleResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.transform.TransformerException;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.assertions.XPathAssertion;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.TidyException;
import org.apache.jmeter.util.XPathUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SOAPXPathAssertion extends XPathAssertion implements Serializable, Assertion {
    private static final long serialVersionUID = -6476762339455563003L;
    private static final String CONTENT_ID = "SOAPXPath.content_id";
    private static final String CHECK_CONTENT_ID = "SOAPXPath.check_content_id";
    private static final String CONTENT_TYPE = "SOAPXPath.content_type";
    private static final String CHECK_CONTENT_TYPE = "SOAPXPath.check_content_type";
    private static final String CHECK_MODE = "SOAPXPath.check_mode";
    private static final String NAMESPACE_COUNT = "SOAPXPath.namespace_count";
    private static final String NAMESPACE_PREFIX = "SOAPXPath.namespace_prefix";
    private static final String NAMESPACE_URI = "SOAPXPath.namespace_uri";
    public static final int MODE_CHECK_ENVELOPE = 0;
    public static final int MODE_CHECK_ANY = 1;
    public static final int MODE_CHECK_ALL = 2;
    private static final Logger log = LoggingManager.getLoggerForClass();

    public SOAPXPathAssertion() {
    }

    private ArrayList getMatchedAttachments(SOAPSampleResult response) {
        ArrayList result = new ArrayList();
        boolean checkContentID = this.isCheckContentID();
        String expectedContentID = this.getContentID();
        boolean checkContentType = this.isCheckContentType();
        String expectedContentType = this.getContentType();
        Iterator it = response.getAttachments();

        while(true) {
            AttachmentPart ap;
            do {
                do {
                    if(!it.hasNext()) {
                        return result;
                    }

                    ap = (AttachmentPart)it.next();
                } while(checkContentType && !expectedContentType.equals(ap.getContentType()));
            } while(checkContentID && !expectedContentID.equals(ap.getContentId()));

            result.add(ap);
        }
    }

    public AssertionResult getResult(SampleResult response) {
        int checkMode = this.getCheckMode();
        TreeMap map = new TreeMap();
        this.getNamespaceMap(map);
        if(!(response instanceof SOAPSampleResult)) {
            return this.getResult(response, map);
        } else {
            SOAPSampleResult soapResponse = (SOAPSampleResult)response;
            if(checkMode == 0) {
                SampleResult matchedAttachments1 = new SampleResult();
                if(soapResponse != null && soapResponse.getSOAPEnvelope() != null) {
                    matchedAttachments1.setResponseData(soapResponse.getSOAPEnvelope().getBytes());
                } else {
                    matchedAttachments1.setResponseData(new byte[0]);
                }

                return this.getResult(matchedAttachments1, map);
            } else {
                ArrayList matchedAttachments = this.getMatchedAttachments(soapResponse);
                if(matchedAttachments.size() == 0) {
                    AssertionResult it1 = new AssertionResult(this.getName());
                    it1.setFailure(true);
                    StringBuffer result3 = new StringBuffer();
                    boolean ioe1 = this.isCheckContentID();
                    String result5 = this.getContentID();
                    boolean dummyResult1 = this.isCheckContentType();
                    String expectedContentType = this.getContentType();
                    result3.append("No attachments ");
                    if(ioe1 || dummyResult1) {
                        if(ioe1 && dummyResult1) {
                            result3.append("matching content type \'").append(expectedContentType).append("\' and content ID \'").append(result5).append("\' ");
                        } else if(ioe1) {
                            result3.append("matching content type \'").append(expectedContentType).append("\' ");
                        } else {
                            result3.append("matching content ID \'").append(result5).append("\' ");
                        }
                    }

                    result3.append("found.");
                    it1.setFailureMessage(result3.toString());
                    return it1;
                } else {
                    Iterator it = matchedAttachments.iterator();

                    while(true) {
                        if(it.hasNext()) {
                            AttachmentPart result2 = (AttachmentPart)it.next();

                            AssertionResult result1;
                            try {
                                String ioe = SOAPUtils.attachmentToString(result2);
                                SampleResult result4 = new SampleResult();
                                result4.setResponseData(ioe.getBytes());
                                AssertionResult dummyResult = this.getResult(result4, map);
                                if(checkMode == 1 && !dummyResult.isError() && !dummyResult.isFailure()) {
                                    return dummyResult;
                                }

                                if(!dummyResult.isError() && !dummyResult.isFailure()) {
                                    continue;
                                }

                                return dummyResult;
                            } catch (SOAPException var12) {
                                result1 = new AssertionResult(this.getName());
                                result1.setError(true);
                                result1.setFailureMessage("SOAPException " + var12.getMessage() + " was thrown during evaluating the assertion");
                                return result1;
                            } catch (IOException var13) {
                                result1 = new AssertionResult(this.getName());
                                result1.setError(true);
                                result1.setFailureMessage("IOException " + var13.getMessage() + " was thrown during evaluating the assertion");
                                return result1;
                            }
                        }

                        AssertionResult result;
                        if(checkMode == 2) {
                            result = new AssertionResult(this.getName());
                            result.setFailure(false);
                            result.setError(false);
                            result.setFailureMessage("");
                            return result;
                        }

                        result = new AssertionResult(this.getName());
                        result.setFailure(true);
                        result.setFailureMessage("None of attachments matched " + this.getXPathString());
                        return result;
                    }
                }
            }
        }
    }

    public AssertionResult getResult(SampleResult response, TreeMap namespaceMap) {
        AssertionResult result = new AssertionResult(this.getName());
        byte[] responseData = response.getResponseData();
        if(responseData.length == 0) {
            return result.setResultForNull();
        } else {
            result.setFailure(false);
            result.setFailureMessage("");
            if(log.isDebugEnabled()) {
                log.debug("Validation is set to " + this.isValidating());
                log.debug("Whitespace is set to " + this.isWhitespace());
                log.debug("Tolerant is set to " + this.isTolerant());
            }

            Document doc = null;
            boolean isXML = JOrphanUtils.isXML(responseData);

            try {
                doc = XPathUtil.makeDocument(new ByteArrayInputStream(responseData), this.isValidating(), this.isWhitespace(), this.isNamespace(), this.isTolerant(), this.isQuiet(), this.showWarnings(), this.reportErrors(), isXML, false);
                //doc = XPathUtil.makeDocument(new ByteArrayInputStream(responseData), this.isValidating(), this.isWhitespace(), this.isNamespace(), this.isTolerant(), this.isQuiet(), this.showWarnings(), this.reportErrors(), isXML);
            } catch (SAXException var12) {
                log.debug("Caught sax exception: " + var12);
                result.setError(true);
                result.setFailureMessage("SAXException: " + var12.getMessage());
                return result;
            } catch (IOException var13) {
                log.warn("Cannot parse result content", var13);
                result.setError(true);
                result.setFailureMessage("IOException: " + var13.getMessage());
                return result;
            } catch (ParserConfigurationException var14) {
                log.warn("Cannot parse result content", var14);
                result.setError(true);
                result.setFailureMessage("ParserConfigurationException: " + var14.getMessage());
                return result;
            } catch (TidyException var15) {
                result.setError(true);
                result.setFailureMessage(var15.getMessage());
                return result;
            }

            if(doc != null && doc.getDocumentElement() != null) {
                Element nsResolver = doc.createElement("NamespaceResolver");

                Entry nodeList;
                String pathString;
                String i;
                for(Iterator nsIt = namespaceMap.entrySet().iterator(); nsIt.hasNext(); nsResolver.setAttribute(pathString, i)) {
                    nodeList = (Entry)nsIt.next();
                    pathString = (String)nodeList.getKey();
                    i = (String)nodeList.getValue();
                    if(!pathString.startsWith("xmlns:")) {
                        pathString = "xmlns:" + pathString;
                    }
                }

                nodeList = null;
                pathString = this.getXPathString();

                NodeList var17;
                try {
                    XObject var18 = XPathAPI.eval(doc, pathString, nsResolver);
                    switch(var18.getType()) {
                        case 1:
                            if(!var18.bool()) {
                                result.setFailure(!this.isNegated());
                                result.setFailureMessage("No Nodes Matched " + pathString);
                            }

                            return result;
                        case 4:
                            var17 = var18.nodelist();
                            break;
                        default:
                            result.setFailure(true);
                            result.setFailureMessage("Cannot understand: " + pathString);
                            return result;
                    }
                } catch (TransformerException var16) {
                    result.setError(true);
                    result.setFailureMessage("TransformerException: " + var16.getMessage() + " for:" + pathString);
                    return result;
                }

                if(var17 != null && var17.getLength() != 0) {
                    log.debug("nodeList length " + var17.getLength());
                    if(log.isDebugEnabled() && !this.isNegated()) {
                        for(int var19 = 0; var19 < var17.getLength(); ++var19) {
                            log.debug("nodeList[" + var19 + "] " + var17.item(var19));
                        }
                    }

                    result.setFailure(this.isNegated());
                    if(this.isNegated()) {
                        result.setFailureMessage("Specified XPath was found... Turn off negate if this is not desired");
                    }

                    return result;
                } else {
                    log.debug("nodeList null no match  " + pathString);
                    result.setFailure(!this.isNegated());
                    result.setFailureMessage("No Nodes Matched " + pathString);
                    return result;
                }
            } else {
                result.setError(true);
                result.setFailureMessage("Document is null, probably not parsable");
                return result;
            }
        }
    }

    public void setContentID(String contentID) {
        this.setProperty("SOAPXPath.content_id", contentID);
    }

    public String getContentID() {
        return this.getPropertyAsString("SOAPXPath.content_id");
    }

    public void setCheckContentID(boolean val) {
        this.setProperty("SOAPXPath.check_content_id", val, true);
    }

    public boolean isCheckContentID() {
        return this.getPropertyAsBoolean("SOAPXPath.check_content_id", true);
    }

    public void setContentType(String contentID) {
        this.setProperty("SOAPXPath.content_type", contentID);
    }

    public String getContentType() {
        return this.getPropertyAsString("SOAPXPath.content_type");
    }

    public void setCheckContentType(boolean val) {
        this.setProperty("SOAPXPath.check_content_type", val, true);
    }

    public boolean isCheckContentType() {
        return this.getPropertyAsBoolean("SOAPXPath.check_content_type", true);
    }

    public void setCheckMode(int mode) {
        this.setProperty("SOAPXPath.check_mode", "" + mode);
    }

    public int getCheckMode() {
        return this.getPropertyAsInt("SOAPXPath.check_mode");
    }

    public void setNamespaceMap(TreeMap map) {
        Iterator it = map.entrySet().iterator();
        int index = 0;
        this.setProperty("SOAPXPath.namespace_count", "" + map.size());

        while(it.hasNext()) {
            Entry e = (Entry)it.next();
            String prefix = (String)e.getKey();
            String value = (String)e.getValue();
            this.setProperty("SOAPXPath.namespace_prefix" + index, prefix);
            this.setProperty("SOAPXPath.namespace_uri" + index, value);
            ++index;
        }

    }

    public void getNamespaceMap(TreeMap map) {
        map.clear();
        int count = this.getPropertyAsInt("SOAPXPath.namespace_count");

        for(int index = 0; index < count; ++index) {
            String prefix = this.getPropertyAsString("SOAPXPath.namespace_prefix" + index);
            String uri = this.getPropertyAsString("SOAPXPath.namespace_uri" + index);
            map.put(prefix, uri);
        }

    }
}
