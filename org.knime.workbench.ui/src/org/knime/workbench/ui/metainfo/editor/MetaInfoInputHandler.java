/* This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2013
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 */
package org.knime.workbench.ui.metainfo.editor;

import java.util.ArrayList;
import java.util.List;

import org.knime.workbench.ui.metainfo.model.MetaGUIElement;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Fabian Dill, KNIME.com AG
 */
public class MetaInfoInputHandler extends DefaultHandler {


    private StringBuffer m_buffer = new StringBuffer();

    private final List<MetaGUIElement>m_elements
        = new ArrayList<MetaGUIElement>();

    private String m_currentForm;
    private String m_currentLabel;
    private boolean m_isReadOnly;

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        m_buffer.append(ch, start, length);
    }


    @Override
    public void startElement(final String uri, final String localName, final String name,
            final Attributes atts) throws SAXException {
        if (localName.equals(MetaGUIElement.ELEMENT)) {
            m_currentForm = atts.getValue(MetaGUIElement.FORM);
            m_currentLabel = atts.getValue(MetaGUIElement.NAME);
            m_isReadOnly = Boolean.valueOf(
                    atts.getValue(MetaGUIElement.READ_ONLY));

        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String name)
            throws SAXException {
        if (localName.equals(MetaGUIElement.ELEMENT)) {
            m_elements.add(MetaGUIElement.create(m_currentForm, m_currentLabel,
                    m_buffer.toString(), m_isReadOnly));
            m_buffer = new StringBuffer();
        }
    }

    public List<MetaGUIElement>getElements() {
        return m_elements;
    }


}
