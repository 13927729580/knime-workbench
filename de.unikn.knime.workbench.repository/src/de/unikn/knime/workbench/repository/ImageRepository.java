/* @(#)$$RCSfile$$ 
 * $$Revision$$ $$Date$$ $$Author$$
 * 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   ${date} (${user}): created
 */
package de.unikn.knime.workbench.repository;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This manages all the (internal) images of the Editor-Plugin. Images are
 * registered given their path relative to the plugin's base. The getter methods
 * automatically try to add an image first, if it's not already registered - so
 * there is no need to explicitly add them before ;-) In fact, you can also
 * cache images that are *not* located in the editor-plugin by adding valid
 * image-descriptors. They must, however, explicitly registered and can only be
 * retrieved using the identical descriptor as a key !
 * 
 * Some common images are listed as public constants for convinience.
 * 
 * @author Florian Georg, University of Konstanz
 */
public final class ImageRepository {

    private ImageRepository() {
        // hidden, dude !
    }

    /** Image: editor. */
    public static final String IMAGE_EDITOR = "icons/editor.gif";

    /** Image: connection. */
    public static final String IMAGE_PALETTE_CONNECTION = "icons/"
            + "connection.gif";

    /** Image: description. */
    public static final String IMAGE_PALETTE_DESCRIPTION = "icons/"
            + "description.gif";

    /** Image: in port. */
    public static final String IMAGE_PORT_IN = "icons/port_in.gif";

    /** Image: out port. */
    public static final String IMAGE_PORT_OUT = "icons/port_in.gif";

    /** Image: reader. */
    public static final String IMAGE_TYPE_READER = "icons/palette/reader.gif";

    /** Image: filter. */
    public static final String IMAGE_TYPE_FILTER = "icons/palette/filter.gif";

    /** Image: learner. */
    public static final String IMAGE_TYPE_LEARNER = "icons/palette/learner.gif";

    /** Image: scorer. */
    public static final String IMAGE_TYPE_SCORER = "icons/palette/scorer.gif";

    /** Image: writer. */
    public static final String IMAGE_TYPE_WRITER = "icons/palette/writer.gif";

    /** Image: handler. */
    public static final String IMAGE_TYPE_HANDLER = "icons/palette/handler.gif";

    /** Image: viewer. */
    public static final String IMAGE_TYPE_VIEWER = "icons/palette/viewer.gif";

    /** Image: default for an algorithm. */
    public static final String IMAGE_DEFAULT_ALGORITHM = "icons/alg/"
            + "default.gif";

    // The internal registry
    private static ImageRegistry registry = KNIMERepositoryPlugin.getDefault()
            .getImageRegistry();

    // default plugin ID, used to lookup the default "scope"
    private static final String PLUGIN_ID = KNIMERepositoryPlugin.PLUGIN_ID;

    /**
     * Adds an image(-descriptor) to the registry.
     * 
     * @param relPath The plugin-relative path to image
     */
    public static void addImage(final String relPath) {
        registry.put(relPath, AbstractUIPlugin.imageDescriptorFromPlugin(
                PLUGIN_ID, relPath));
    }

    /**
     * Adds an image to the registry.
     * 
     * @param iconURL the url of the icon
     */
    public static void addImage(final URL iconURL) {

        Image image = createImageFromURL(iconURL);

        // only add if not null
        if (image != null) {
            registry.put(iconURL.toString(), image);
        }
    }

    /**
     * Returns an Image. If the cache doesn't contain this image, it is added
     * first.
     * 
     * @param key The key
     * @return The cached image,
     */
    public static Image getImage(final String key) {
        if (registry.get(key) == null) {
            addImage(key);
        }

        return registry.get(key);
    }

    /**
     * Returns an scaled Image. If the cache doesn't contain this image, it is
     * added first.
     * 
     * @param iconURL the url of the icon
     * @param width width to which the image should be scaled to
     * @param height height to which the image should be scaled to
     * @return The cached image, should never return <code>null</code>
     */
    public static Image getScaledImage(final URL iconURL, final int width,
            final int height) {

        if (iconURL == null) {
            return null;
        }

        String key = iconURL.toString();
        String finalKey = key + "_y" + width + "_x" + height;

        if (registry.get(finalKey) == null) {
            Image unscaledImg = getImage(iconURL);
            if (unscaledImg != null) {
                Image scaledImage = new Image(Display.getDefault(), unscaledImg
                        .getImageData().scaledTo(width, height));
                registry.put(finalKey, scaledImage);
            }

        }

        return registry.get(finalKey);
    }

    /**
     * Returns an Image.
     * 
     * @param iconURL the url of the icon
     * @return The cached image, or <code>null</code>
     */
    public static Image getImage(final URL iconURL) {
        if (registry.get(iconURL.toString()) == null) {
            addImage(iconURL);
        }

        return registry.get(iconURL.toString());
    }

    private static Image createImageFromURL(final URL iconURL) {
        try {
            return new Image(Display.getDefault(), iconURL.openStream());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns an ImageDescriptor for an relative path.
     * 
     * @param relPath The plugin-relative path to the image
     * @return The cached descriptor, or <code>null</code>
     */
    public static ImageDescriptor getImageDescriptor(final String relPath) {
        if (registry.getDescriptor(relPath) == null) {
            addImage(relPath);
        }

        return registry.getDescriptor(relPath);
    }

}
