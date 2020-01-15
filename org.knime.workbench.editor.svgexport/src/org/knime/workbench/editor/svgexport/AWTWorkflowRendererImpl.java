/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Jan 15, 2020 (hornm): created
 */
package org.knime.workbench.editor.svgexport;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.renderer.StaticRenderer;
import org.apache.batik.util.XMLConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.swt.widgets.Display;
import org.knime.core.node.workflow.WorkflowManager;
import org.knime.core.ui.util.AWTWorkflowRenderer;
import org.knime.core.ui.wrapper.WorkflowManagerWrapper;
import org.knime.workbench.editor.svgexport.actions.SVGExporter;
import org.knime.workbench.editor2.WorkflowEditPartFactory;
import org.knime.workbench.editor2.editparts.WorkflowRootEditPart;
import org.knime.workbench.editor2.svgexport.SVGExportException;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.SAXException;

/**
 *
 * @author Martin Horn, KNIME GmbH, Konstanz, Germany
 */
public class AWTWorkflowRendererImpl implements AWTWorkflowRenderer {

    @Override
    public void renderWorkflow(final WorkflowManager wfm, final Graphics2D graphics) {
        AtomicReference<SVGDocument> doc = new AtomicReference<SVGDocument>();
        Display.getDefault().syncExec(() -> {
            GraphicalViewerImpl viewer = new GraphicalViewerImpl();
            viewer.setEditPartFactory(new WorkflowEditPartFactory());
            viewer.setContents(WorkflowManagerWrapper.wrap(wfm));
            WorkflowRootEditPart root = (WorkflowRootEditPart)viewer.getRootEditPart().getContents();
            root.getFigure().setBounds(new org.eclipse.draw2d.geometry.Rectangle(0, 0, 2000, 2000));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                SVGExporter.export(viewer, out);
                doc.set(toSVGDocument(new ByteArrayInputStream(out.toByteArray())));
            } catch (SVGExportException | IOException e) {
                //TODO
                throw new RuntimeException(e);
            }
        });

        Rectangle componentBounds = new Rectangle(200, 200);
        paint(doc.get(), graphics, componentBounds, true);
    }

    private SVGDocument toSVGDocument(final InputStream in) throws IOException {

        SAXSVGDocumentFactory f = newSAXSVGDocumentFactory();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        // workaround for MacOS that does not have a proper context classloader in the main thread
        if (cl == null) {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            try {
                // We need to set the document uri here, because batik expects the uri to be non-null in
                // certain error handling methods. An empty string is sufficient for the uri parser used.
                return f.createSVGDocument("", in);
            } finally {
                Thread.currentThread().setContextClassLoader(null);
            }
        } else {
            return f.createSVGDocument("", in);
        }
    }

    /**
     * Create new instance of of the factory (includes names space 'fixes'). Batik 1.7.x requires proper namespace
     * declaration is required. As this presents a backward compatibility issue we force the name space on the document,
     * see also https://issues.apache.org/jira/browse/BATIK-764 and https://knime-com.atlassian.net/browse/AP-3136
     *
     * @return new instance of a document factory.
     */
    private static final SAXSVGDocumentFactory newSAXSVGDocumentFactory() {
        String parserClass = XMLResourceDescriptor.getXMLParserClassName();

        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parserClass) {
            @Override
            public void startDocument() throws SAXException {
                super.startDocument();
                if (namespaces.get("") == null) {
                    namespaces.put("", SVGDOMImplementation.SVG_NAMESPACE_URI);
                }
                if (namespaces.get("xlink") == null) {
                    namespaces.put("xlink", XMLConstants.XLINK_NAMESPACE_URI);
                }
            }
        };
        return f;
    }

    private static final Font NO_SVG_FONT = new Font(Font.SANS_SERIF, Font.ITALIC, 12);

    private static final UserAgent UA = new UserAgentAdapter();

    private static final RenderingHints R_HINTS =
        new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    /**
     * Renders an SVG document on a graphics object. The image is scaled to fit in the specified bounds.
     *
     * @param doc an SVG document
     * @param g the graphics object
     * @param componentBounds the bound in which the image should be drawn
     * @param keepAspectRatio <code>true</code> if the aspect ratio should be kept, <code>false</code> if the image
     *            should be scaled in both direction to the maximum
     */
    private static void paint(final SVGDocument doc, final Graphics2D g, final Rectangle componentBounds,
        final boolean keepAspectRatio) {
        if ((componentBounds.getHeight() < 1) || (componentBounds.getWidth() < 1)) {
            return;
        }

        GVTBuilder gvtBuilder = new GVTBuilder();
        BridgeContext bridgeContext = new BridgeContext(UA);
        GraphicsNode gvtRoot = gvtBuilder.build(bridgeContext, doc);

        Rectangle2D svgBounds = gvtRoot.getBounds();
        if (svgBounds == null) {
            g.setFont(NO_SVG_FONT);
            g.drawString("Invalid SVG", 2, 14);
            return;
        }

        double scaleX = (componentBounds.getWidth() - 2) / svgBounds.getWidth();
        double scaleY = (componentBounds.getHeight() - 2) / svgBounds.getHeight();
        if (keepAspectRatio) {
            scaleX = Math.min(scaleX, scaleY);
            scaleY = Math.min(scaleX, scaleY);
        }

        AffineTransform transform = new AffineTransform();
        transform.scale(scaleX, scaleY);
        transform.translate(-svgBounds.getX(), -svgBounds.getY());

        StaticRenderer renderer = new StaticRenderer(R_HINTS, transform);
        renderer.setTree(gvtRoot);
        renderer.updateOffScreen((int)componentBounds.getWidth(), (int)componentBounds.getHeight());
        renderer.clearOffScreen();
        renderer.repaint(componentBounds);
        final BufferedImage image = renderer.getOffScreen();

        double heightDiff = componentBounds.getHeight() - scaleY * svgBounds.getHeight();

        double widthDiff = componentBounds.getWidth() - scaleX * svgBounds.getWidth();

        g.drawImage(image, (int)(widthDiff / 2), (int)(heightDiff / 2), null);
    }

}
