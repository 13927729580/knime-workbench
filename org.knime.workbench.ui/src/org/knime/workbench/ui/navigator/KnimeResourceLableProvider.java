/* 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 * -------------------------------------------------------------------
 * 
 * History
 *   27.06.2006 (sieb): created
 */
package org.knime.workbench.ui.navigator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.SWTResourceUtil;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.knime.workbench.ui.KNIMEUIPlugin;

/**
 * Implements the label provider for the knime navigator. Mainly projects get
 * another image.
 * 
 * @author Christoph Sieb, University of Konstanz
 */
public class KnimeResourceLableProvider extends LabelProvider implements
        IColorProvider, IFontProvider {

    /**
     * Returns a workbench label provider that is hooked up to the decorator
     * mechanism.
     * 
     * @return a new <code>DecoratingLabelProvider</code> which wraps a new
     *         <code>WorkbenchLabelProvider</code>
     */
    public static ILabelProvider getDecoratingWorkbenchLabelProvider() {
        return new DecoratingLabelProvider(new WorkbenchLabelProvider(),
                PlatformUI.getWorkbench().getDecoratorManager()
                        .getLabelDecorator());
    }

    /**
     * Listener that tracks changes to the editor registry and does a full
     * update when it changes, since many workbench adapters derive their icon
     * from the file associations in the registry.
     */
    private IPropertyListener m_editorRegistryListener =
            new IPropertyListener() {
                public void propertyChanged(final Object source,
                        final int propId) {
                    if (propId == IEditorRegistry.PROP_CONTENTS) {
                        fireLabelProviderChanged(new LabelProviderChangedEvent(
                                KnimeResourceLableProvider.this));
                    }
                }
            };

    /**
     * Creates a new workbench label provider.
     */
    public KnimeResourceLableProvider() {
        PlatformUI.getWorkbench().getEditorRegistry().addPropertyListener(
                m_editorRegistryListener);
    }

    /**
     * Returns an image descriptor that is based on the given descriptor, but
     * decorated with additional information relating to the state of the
     * provided object.
     * 
     * Subclasses may reimplement this method to decorate an object's image.
     * 
     * @param input The base image to decorate.
     * @param element The element used to look up decorations.
     * @return the resuling ImageDescriptor.
     * @see org.eclipse.jface.resource.CompositeImageDescriptor
     */
    protected ImageDescriptor decorateImage(final ImageDescriptor input,
            final Object element) {
        return input;
    }

    /**
     * Returns a label that is based on the given label, but decorated with
     * additional information relating to the state of the provided object.
     * 
     * Subclasses may implement this method to decorate an object's label.
     * 
     * @param input The base text to decorate.
     * @param element The element used to look up decorations.
     * @return the resulting text
     */
    protected String decorateText(final String input, final Object element) {
        return input;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        PlatformUI.getWorkbench().getEditorRegistry().removePropertyListener(
                m_editorRegistryListener);
        super.dispose();
    }

    /**
     * Returns the implementation of IWorkbenchAdapter for the given object.
     * 
     * @param o the object to look up.
     * @return IWorkbenchAdapter or<code>null</code> if the adapter is not
     *         defined or the object is not adaptable.
     */
    protected final IWorkbenchAdapter getAdapter(final Object o) {
        if (!(o instanceof IAdaptable)) {
            return null;
        }
        return (IWorkbenchAdapter)((IAdaptable)o)
                .getAdapter(IWorkbenchAdapter.class);
    }

    /**
     * Returns the implementation of IWorkbenchAdapter2 for the given object.
     * 
     * @param o the object to look up.
     * @return IWorkbenchAdapter2 or<code>null</code> if the adapter is not
     *         defined or the object is not adaptable.
     */
    protected final IWorkbenchAdapter2 getAdapter2(final Object o) {
        if (!(o instanceof IAdaptable)) {
            return null;
        }
        return (IWorkbenchAdapter2)((IAdaptable)o)
                .getAdapter(IWorkbenchAdapter2.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Image getImage(final Object element) {

        if (element instanceof IResource) {
            if (((IResource)element).getType() == IResource.PROJECT) {
                return KNIMEUIPlugin.getDefault().getImage(
                        KNIMEUIPlugin.PLUGIN_ID, "icons/knimeProject.png");
            }
        }

        // obtain the base image by querying the element
        IWorkbenchAdapter adapter = getAdapter(element);
        if (adapter == null) {
            return null;
        }
        ImageDescriptor descriptor = adapter.getImageDescriptor(element);
        if (descriptor == null) {
            return null;
        }

        // add any annotations to the image descriptor
        descriptor = decorateImage(descriptor, element);

        Image image = (Image)SWTResourceUtil.getImageTable().get(descriptor);
        if (image == null) {
            image = descriptor.createImage();
            SWTResourceUtil.getImageTable().put(descriptor, image);
        }
        return image;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getText(final Object element) {
        // query the element for its label
        IWorkbenchAdapter adapter = getAdapter(element);
        if (adapter == null) {
            return ""; //$NON-NLS-1$
        }
        String label = adapter.getLabel(element);

        // return the decorated label
        return decorateText(label, element);
    }

    /**
     * {@inheritDoc}
     */
    public Color getForeground(final Object element) {
        return getColor(element, true);
    }

    /**
     * {@inheritDoc}
     */
    public Color getBackground(final Object element) {
        return getColor(element, false);
    }

    /**
     * {@inheritDoc}
     */
    public Font getFont(final Object element) {
        IWorkbenchAdapter2 adapter = getAdapter2(element);
        if (adapter == null) {
            return null;
        }

        FontData descriptor = adapter.getFont(element);
        if (descriptor == null) {
            return null;
        }

        Font font = (Font)SWTResourceUtil.getFontTable().get(descriptor);
        if (font == null) {
            font = new Font(Display.getCurrent(), descriptor);
            SWTResourceUtil.getFontTable().put(descriptor, font);
        }
        return font;
    }

    private Color getColor(final Object element, final boolean forground) {
        IWorkbenchAdapter2 adapter = getAdapter2(element);
        if (adapter == null) {
            return null;
        }
        RGB descriptor =
                forground ? adapter.getForeground(element) : adapter
                        .getBackground(element);
        if (descriptor == null) {
            return null;
        }

        Color color = (Color)SWTResourceUtil.getColorTable().get(descriptor);
        if (color == null) {
            color = new Color(Display.getCurrent(), descriptor);
            SWTResourceUtil.getColorTable().put(descriptor, color);
        }
        return color;
    }
}
