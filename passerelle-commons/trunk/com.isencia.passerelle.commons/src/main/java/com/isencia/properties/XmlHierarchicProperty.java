/* Copyright 2010 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.properties;

/**
 * Implementation of a hierarchical properties container based on an XML document.
 * 
 * @author erwin.de.ley@isencia.be
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class XmlHierarchicProperty implements IHierarchicProperty {

    /**
     * The JDOM outputter, used to present the hierarchical properties in XML,
     * in toString()
     */
    protected final static XMLOutputter outputter = new XMLOutputter();

    /**
     * The JDOM root element of the XML hierarchy.
     */
    protected Element rootElement = null;

    /**
     * XmlHierarchicProperty constructor.
     */
    protected XmlHierarchicProperty() {
        super();
    }

    /**
     * XmlHierarchicProperty constructor.
     */
    public XmlHierarchicProperty(Element newRootElement) {
        super();
        rootElement = newRootElement;
    }

    /**
     * Insert the method's description here.
     * 
     * @param props
     *            be.isencia.properties.IHierarchicProperty
     */
    public void addProperties(XmlHierarchicProperty props) {
        rootElement.addContent(props.rootElement);
    }

    /**
     * Recursively traverse the hierarchical property tree and assemble a plain,
     * linear java.util.Properties object from it. 
     * 
     * @return java.util.Properties
     */
    public java.util.Properties getLinearProperties() {
        java.util.Properties props = new java.util.Properties();

        List nl = rootElement.getChildren();
        if ((nl != null) && (nl.size() > 0)) {
            String propName = null;
            Element e = null;
            String attr = null;
            Iterator itr = nl.iterator();
            for (int i = 0; itr.hasNext(); ++i) {
                e = (Element) itr.next();
                propName = e.getName();

                // look for deeper property levels
                props = getLinearProperties(propName, e, props);

                // look for property at this level
                attr = e.getAttributeValue("value");
                if (attr != null) {
                    props.setProperty(propName, attr);
                }
            }
        }

        return props;
    }

    /**
     * Look for properties on the level beneath the given levelNode. 
     * 
     * @return java.util.Properties
     * @param levelName
     *            String the dot-separated namespace up to this level in the DOM
     * @param levelElement
     *            Element the base node of this level of properties in the DOM
     * @param props
     *            java.util.Properties
     */
    private java.util.Properties getLinearProperties(String levelName,
            Element levelElement, java.util.Properties props) {

        List nl = levelElement.getChildren();

        if ((nl != null) && (nl.size() > 0)) {
            String propName = null;
            Element e = null;
            String attr = null;
            Iterator itr = nl.iterator();
            for (int i = 0; itr.hasNext(); ++i) {
                e = (Element) itr.next();
                propName = levelName + "." + e.getName();

                // look for deeper property levels
                props = getLinearProperties(propName, e, props);

                // look for property at this level
                attr = e.getAttributeValue("value");
                if (attr != null) {
                    props.setProperty(propName, attr);
                }
            }
        }

        return props;
    }

    /**
     * Returns the name of this property hierarchy.
     * 
     * @return java.lang.String
     */
    public java.lang.String getName() {
        return rootElement.getName();
    }

    /**
     * Returns a sub-property that has the given nested name in this hierarchy.
     * The sub-property is also IHierarchicProperty.
     * 
     * @return be.isencia.properties.IHierarchicProperty
     * @param propName
     *            java.lang.String
     */
    public IHierarchicProperty getProperty(java.lang.String propName) {
        IHierarchicProperty props = null;

        // handle hierarchical property names
        Element node = getRootElement();

        if (propName != null && propName.length() > 0) {
            Element cursorNode = node;
            String cursorName = "";
            java.util.StringTokenizer propNameTkr = new java.util.StringTokenizer(
                    propName, ".", false);

            while (propNameTkr.hasMoreTokens() && (node != null)) {
                cursorNode = node;
                cursorName = propNameTkr.nextToken();
                node = cursorNode.getChild(cursorName);
            }
        }
        if (node != null) {
            // Found right level, create IHierarchicProperty
            props = new XmlHierarchicProperty(node);
        }

        return props;
    }

    /**
     * Returns a list of all sub-properties that have the given nested name in
     * this hierarchy. The sub-properties are also IHierarchicProperty-s.
     * 
     * @return java.util.List
     * @param propName
     *            java.lang.String
     */
    public java.util.List getPropertyList(java.lang.String propName) {
        List props = new ArrayList();

        // handle hierarchical property names
        Element node = getRootElement();
        Element cursorNode = node;
        String cursorName = null;

        if (propName != null && propName.length() > 0) {
            java.util.StringTokenizer propNameTkr = new java.util.StringTokenizer(
                    propName, ".", false);

            while (propNameTkr.hasMoreTokens() && (node != null)) {
                cursorNode = node;
                cursorName = propNameTkr.nextToken();
                node = cursorNode.getChild(cursorName);
            }
        }
        if (node != null) {
            // Found right level, now read list of nodes
            // with same name.
            List el = null;
            if (cursorName != null) {
                el = cursorNode.getChildren(cursorName);
            } else {
                el = cursorNode.getChildren();
            }
            Iterator itr = el.iterator();
            for (int i = 0; itr.hasNext(); ++i) {
                Element e = (Element) itr.next();
                props.add(new XmlHierarchicProperty(e));
            }
        }

        return props;
    }

    /**
     * Returns a map of property name/value pairs, for all properties within the
     * given scope in this container.
     * 
     * @return java.util.Map
     * @param propName
     *            java.lang.String
     */
    public Map getPropertyMap(String propName) {
        Map props = new HashMap(89);

        // handle hierarchical property names
        Element node = getRootElement();

        if (propName != null && propName.length() > 0) {
            Element cursorNode = node;
            String cursorName = "";
            java.util.StringTokenizer propNameTkr = new java.util.StringTokenizer(
                    propName, ".", false);

            while (propNameTkr.hasMoreTokens() && (node != null)) {
                cursorNode = node;
                cursorName = propNameTkr.nextToken();
                node = cursorNode.getChild(cursorName);
            }
        }
        if (node != null) {
            // Found right level, now read list of all sub nodes
            List el = node.getChildren();
            Iterator itr = el.iterator();
            for (int i = 0; itr.hasNext(); ++i) {
                Element e = (Element) itr.next();
                props.put(e.getName(), e.getAttributeValue("value"));
            }
        }

        return props;
    }

    /**
     * Returns the value of the property with the given nested/hierarchical
     * name. Names must have the format basename.subname1.subname2 etc.
     * 
     * @return java.lang.String
     * @param propName
     *            java.lang.String
     */
    public String getPropertyValue(String propName) {
        String propValue = "";

        Element node = getRootElement();

        if (propName != null && propName.length() > 0) {
            // handle hierarchical property names
            Element cursorNode = null;
            String cursorName = "";

            java.util.StringTokenizer propNameTkr = new java.util.StringTokenizer(
                    propName, ".", false /* do not return delims as tokens */);

            while (propNameTkr.hasMoreTokens() && (node != null)) {
                cursorNode = node;
                cursorName = propNameTkr.nextToken();
                node = cursorNode.getChild(cursorName);
            }
        }
        if (node != null) {
            // found right level
            propValue = node.getAttributeValue("value");
        }

        return propValue;
    }

    /**
     * Returns the value of the property with the given nested/hierarchical
     * name. Names must have the format basename.subname1.subname2 etc. If the
     * property with the given name is not defined, return a given default
     * value.
     * 
     * @return java.lang.String
     * @param propName
     *            java.lang.String
     * @param defaultValue
     *            java.lang.String
     */
    public java.lang.String getPropertyValue(java.lang.String propName,
            java.lang.String defaultValue) {
        String res = getPropertyValue(propName);
        if (res == null) {
            res = defaultValue;
        }
        return res;
    }

    /**
     * Returns a list of property values, for all properties with the given name
     * in this container.
     * 
     * @return java.lang.String[]
     * @param propName
     *            java.lang.String
     */
    public String[] getPropertyValueList(String propName) {
        String[] propValues = null;

        // handle hierarchical property names
        Element node = getRootElement();
        Element cursorNode = node;
        String cursorName = null;

        if (propName != null && propName.length() > 0) {
            java.util.StringTokenizer propNameTkr = new java.util.StringTokenizer(
                    propName, ".", false);

            while (propNameTkr.hasMoreTokens() && (node != null)) {
                cursorNode = node;
                cursorName = propNameTkr.nextToken();
                node = cursorNode.getChild(cursorName);
            }
        }
        if (node != null) {
            // Found right level, now read list of nodes
            // with same name.
            List el = null;
            if (cursorName != null) {
                el = cursorNode.getChildren(cursorName);
            } else {
                el = cursorNode.getChildren();
            }
            propValues = new String[el.size()];
            Iterator itr = el.iterator();
            for (int i = 0; itr.hasNext(); ++i) {
                Element e = (Element) itr.next();
                propValues[i] = e.getAttributeValue("value");
            }
        }

        return propValues;
    }

    /**
     * Returns the root element of the XML property definition document.
     * 
     * @return Element
     */
    protected Element getRootElement() {
        return rootElement;
    }

    /**
     * Returns a String that represents the value of this object.
     * 
     * @return a string representation of the receiver
     */
    public String toString() {
        java.io.Writer w = new java.io.StringWriter();
        try {
            outputter.output(rootElement, w);
        } catch (Exception e) {
        }

        return w.toString();
    }
}