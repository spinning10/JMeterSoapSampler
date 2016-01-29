package com.jmeter.sampler.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import javax.activation.DataSource;

public class StringDataSource implements DataSource {
    private String str;
    private String contentType;

    public StringDataSource(String str, String contentType) {
        this.str = "";
        this.contentType = "text/plain";
        this.str = str;
        this.contentType = contentType;
    }

    public StringDataSource(String str) {
        this(str, "text/plain");
    }

    public String getContentType() {
        return this.contentType;
    }

    public InputStream getInputStream() throws IOException {
        return new StringDataSource.StringInputStream(this.str);
    }

    public String getName() {
        return "StringDataSource";
    }

    public OutputStream getOutputStream() throws IOException {
        throw new IOException("Unable to write to the string");
    }

    public static class StringInputStream extends InputStream {
        private StringReader sr = null;

        public StringInputStream(String s) {
            this.sr = new StringReader(s);
        }

        public int read() throws IOException {
            return this.sr.read();
        }
    }
}
