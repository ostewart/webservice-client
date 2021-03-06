package com.trailmagic.webclient;

import com.trailmagic.webclient.http.EntityContentProcessor;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import java.io.Reader;

/**
 * Created by: oliver on Date: Dec 20, 2009 Time: 1:29:34 PM
 */
public abstract class XPathEntityContentProcessor<T> implements EntityContentProcessor<T> {
    @SuppressWarnings({"unchecked"})
    @Override
    public T process(Reader content) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        Document doc = builder.build(content);

        return processWithDocument(doc);
    }

    public abstract T processWithDocument(Document doc) throws Exception;
}
