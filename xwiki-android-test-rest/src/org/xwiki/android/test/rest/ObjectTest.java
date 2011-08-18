package org.xwiki.android.test.rest;

import org.xwiki.android.resources.Attribute;
import org.xwiki.android.resources.Object;
import org.xwiki.android.resources.Objects;
import org.xwiki.android.resources.Properties;
import org.xwiki.android.resources.Property;
import org.xwiki.android.rest.Requests;

import android.test.AndroidTestCase;

public class ObjectTest extends AndroidTestCase
{
    private String wikiName, spaceName, pageName, url, username, password, classname, objectNo, propertyName;

    private boolean isSecured = true;

    private Requests request;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        wikiName = TestResources.WIKI_NAME;
        spaceName = TestResources.SPACE_NAME;
        pageName = TestResources.PAGE_NAME;
        url = TestResources.URL;
        classname = TestResources.CLASS_NAME;
        objectNo = TestResources.OBJECT_NUMBER;
        propertyName = TestResources.OBJECT_PROPERTY_NAME;

        if (isSecured) {
            username = TestResources.USERNAME;
            password = TestResources.PASSWORD;
        }

        request = new Requests(url);

        if (isSecured) {
            request.setAuthentication(username, password);
        }
    }

    public void testRequestAllObjects() throws Throwable
    {
        Objects objects = request.requestAllObjects(wikiName, spaceName, pageName);
        assertNotNull(objects);
    }

    public void testAddObject() throws Throwable
    {
        Object obj = new Object();

        Attribute att = new Attribute();
        att.setName("name");
        att.setValue("category");

        Property prop = new Property();
        prop.setType("com.xpn.xwiki.objects.classes.TextAreaClass");
        prop.setName("content");
        prop.setValue("Blog.Other");
        prop.getAttributes().add(att);

        obj.getProperties().add(prop);

        String s = request.addObject(wikiName, spaceName, pageName, obj);
        assertNotNull(s);
    }

    public void testRequestObjectsInClass() throws Throwable
    {
        Objects objects = request.requestObjectsInClass(wikiName, spaceName, pageName, classname);
        assertNotNull(objects);
    }

    public void testRequestObject() throws Throwable
    {
        Object object = request.requestObject(wikiName, spaceName, pageName, classname, objectNo);
        assertNotNull(object);
    }

    public void testRequestObjectAllProperties() throws Throwable
    {
        Properties properties = request.requestObjectAllProperties(wikiName, spaceName, pageName, classname, objectNo);
        assertNotNull(properties);
    }

    public void testRequestObjectProperty() throws Throwable
    {
        Property property =
            request.requestObjectProperty(wikiName, spaceName, pageName, classname, objectNo, propertyName);
        assertNotNull(property);
    }

    // Object should be exist for adding property
    // public void testAddObjectProperty() throws Throwable
    // {
    // Attribute att = new Attribute();
    // att.setName("name");
    // att.setValue("category");
    //
    // Property prop = new Property();
    // prop.setType("com.xpn.xwiki.objects.classes.TextAreaClass");
    // prop.setName("content");
    // prop.setValue("Blog.Other");
    // prop.getAttributes().add(att);
    //
    // String s = request.addProperty(wikiName, spaceName, pageName, classname, objectNo, prop);
    // assertNotNull(s);
    // }

    public void testDeleteObject() throws Throwable
    {
        Property property =
            request.requestObjectProperty(wikiName, spaceName, pageName, classname, objectNo, propertyName);
        assertNotNull(property);
    }
}