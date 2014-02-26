/* This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright by 
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
package org.knime.workbench.ui.metainfo.model;

import javax.xml.transform.sax.TransformerHandler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author Fabian Dill, KNIME.com AG
 */
public class MultilineMetaGUIElement extends MetaGUIElement {


    public MultilineMetaGUIElement(final String label, final String value,
            final boolean isReadOnly) {
        super(label, value, isReadOnly);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Control createGUIElement(final FormToolkit toolkit, final Composite parent) {
        int style = SWT.BORDER | SWT.MULTI | SWT.SCROLL_LINE;
        Text text = toolkit.createText(parent, getValue().trim(), style);
        GridData layout = new GridData(GridData.FILL_HORIZONTAL);
        layout.heightHint = 350;
        text.setLayoutData(layout);
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                fireModifiedEvent(e);
            }
        });
        text.setEnabled(!isReadOnly());
        setControl(text);
        return text;
    }


    private Text getTextControl() {
        return (Text)getControl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveTo(final TransformerHandler parentElement)
        throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(null, null, MetaGUIElement.FORM, "CDATA",
                "multiline");
        atts.addAttribute(null, null, MetaGUIElement.NAME, "CDATA", getLabel());
        atts.addAttribute(null, null, MetaGUIElement.READ_ONLY, "CDATA",
                "" + isReadOnly());
        parentElement.startElement(null, null, MetaGUIElement.ELEMENT, atts);
        char[] value = getTextControl().getText().trim().toCharArray();
        parentElement.characters(value, 0, value.length);
        parentElement.endElement(null, null, MetaGUIElement.ELEMENT);
    }


}
