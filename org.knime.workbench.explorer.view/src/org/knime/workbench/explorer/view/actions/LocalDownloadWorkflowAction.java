/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 * ------------------------------------------------------------------------
 */
package org.knime.workbench.explorer.view.actions;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.knime.core.node.NodeLogger;
import org.knime.workbench.core.util.ImageRepository;
import org.knime.workbench.core.util.ImageRepository.SharedImages;
import org.knime.workbench.explorer.ExplorerActivator;
import org.knime.workbench.explorer.filesystem.AbstractExplorerFileInfo;
import org.knime.workbench.explorer.filesystem.AbstractExplorerFileStore;
import org.knime.workbench.explorer.filesystem.LocalExplorerFileStore;
import org.knime.workbench.explorer.filesystem.RemoteDownloadStream;
import org.knime.workbench.explorer.filesystem.RemoteExplorerFileInfo;
import org.knime.workbench.explorer.filesystem.RemoteExplorerFileStore;
import org.knime.workbench.explorer.view.AbstractContentProvider;
import org.knime.workbench.explorer.view.ExplorerJob;
import org.knime.workbench.explorer.view.actions.imports.IWorkflowImportElement;
import org.knime.workbench.explorer.view.actions.imports.WorkflowImportElementFromArchive;
import org.knime.workbench.explorer.view.actions.imports.WorkflowImportOperation;
import org.knime.workbench.ui.navigator.ZipLeveledStructProvider;

/**
 *
 * @author ohl, KNIME.com, Zurich, Switzerland
 */
public class LocalDownloadWorkflowAction extends Action {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(LocalDownloadWorkflowAction.class);

    private static final ImageDescriptor IMG_DOWNLOAD = ImageRepository.getImageDescriptor(SharedImages.ServerDownload);

    /** The action's id. */
    public static final String ID = "com.knime.explorer.downloadaction";

    private final IProgressMonitor m_monitor;

    private final LocalExplorerFileStore m_targetDir;
    private final RemoteExplorerFileStore m_source;

    /**
     * Creates a action with the source and parent directory.
     *
     * @param source the source file store containing the workflow
     * @param target the target directory to download the workflow to
     * @since 6.4
     */
    public LocalDownloadWorkflowAction(final RemoteExplorerFileStore source,
            final LocalExplorerFileStore target) {
        this(source, target, null);
    }

    /**
     * Creates a action with the source and parent directory.
     *
     * @param source the source file store containing the workflow
     * @param target the target directory to download the workflow to
     * @param monitor the progress monitor to use
     * @since 6.4
     */
    public LocalDownloadWorkflowAction(final RemoteExplorerFileStore source, final LocalExplorerFileStore target,
            final IProgressMonitor monitor) {
        super("Download");
        m_source = source;
        m_targetDir = target;
        m_monitor = monitor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return IMG_DOWNLOAD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return getTargetDir() != null && getSourceFile() != null;
    }

    /**
     * @return the directory to save the download to
     * @since 6.4
     */
    protected LocalExplorerFileStore getTargetDir() {
        return m_targetDir;
    }

    /**
     * @return a string identifying the download target
     * @since 6.4
     */
    protected String getTargetIdentifier() {
        return m_targetDir.getMountIDWithFullPath();
    }

    /**
     * Perform preparations on the target. Nothing is done
     * in the base implementation.
     * @since 6.4
     */
    protected void prepareTarget() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    protected void refreshTarget() {
        getTargetDir().refresh();
    }


    /**
     * @return the file store to download
     * @since 6.4
     */
    protected RemoteExplorerFileStore getSourceFile() {
        return m_source;
    }

    /**
     * @return true if the download source provided by {@link #getSourceFile()}
     *      represents a workflow
     * @since 6.4
     */
    protected boolean isSourceSupported() {
        RemoteExplorerFileInfo sourceInfo = getSourceFile().fetchInfo();
        AbstractContentProvider targetContentProvider = getTargetDir().getContentProvider();

        return sourceInfo.isWorkflow() || sourceInfo.isWorkflowGroup() || sourceInfo.isSnapshot()
                || (sourceInfo.isFile() && targetContentProvider.canHostDataFiles())
                || (sourceInfo.isWorkflowTemplate() && targetContentProvider.canHostMetaNodeTemplates());
// copying of workflow jobs is disabled until implemented on server
//        RemoteExplorerFileInfo info = getSourceFile().fetchInfo();
//        return info.isWorkflow()
//            || (info.isWorkflowJob() && !info.isExecuting());
    }

    /**
     *
     */
    protected void extractDownloadToTarget(final File downloadedFile)
            throws Exception {
        AbstractExplorerFileStore source = getSourceFile();

        AbstractExplorerFileInfo info = source.fetchInfo();
        if (info.isSnapshot()) {
            source = source.getParent();
            info = source.fetchInfo();
        }

        if (info.isFile()) {
            FileUtils.copyFile(downloadedFile, getTargetDir().toLocalFile());
        } else if (info.isWorkflow() || info.isWorkflowTemplate() || info.isWorkflowGroup()) {
            unpackWorkflowIntoLocalDir(getTargetDir().getParent(), downloadedFile);
        } else {
            throw new IllegalArgumentException("Downloaded item '" + getSourceFile().getMountIDWithFullPath() + "'"
                    + " is neither a file nor a workflow or template.");
        }
    }

    private void unpackWorkflowIntoLocalDir(
            final LocalExplorerFileStore destWorkflowDir,
            final File zippedWorkflow) throws Exception {

        ZipFile zFile = new ZipFile(zippedWorkflow);
        ZipLeveledStructProvider importStructureProvider =
                new ZipLeveledStructProvider(zFile);
        importStructureProvider.setStrip(1);

        ZipEntry rootEntry = (ZipEntry)importStructureProvider.getRoot();
        List<ZipEntry> rootChild =
                importStructureProvider.getChildren(rootEntry);
        if (rootChild.size() == 1) {
            // the zipped workflow normally contains only one dir
            rootEntry = rootChild.get(0);
        }
        WorkflowImportElementFromArchive root =
                collectWorkflowsFromZipFile(zippedWorkflow);
        List<IWorkflowImportElement> flows =
                new LinkedList<IWorkflowImportElement>();
        IWorkflowImportElement element = null;
        if (root.getChildren().size() == 1) {
            element = root.getChildren().iterator().next();
        } else {
            element = root;
        }
        // rename the import element
        element.setName(getTargetDir().getName());
        flows.add(element);
        LOGGER.debug("Unpacking workflow \"" + element.getName()
                + "\" into destination: "
            + destWorkflowDir.getMountIDWithFullPath());
        final WorkflowImportOperation importOp =
                new WorkflowImportOperation(flows, destWorkflowDir, null);

        try {
            importOp.run(m_monitor);
        } finally {
            importStructureProvider.closeArchive();
        }
    }

    private WorkflowImportElementFromArchive collectWorkflowsFromZipFile(
            final File zipFile) throws ZipException, IOException {
        ILeveledImportStructureProvider provider = null;
        ZipFile sourceFile = new ZipFile(zipFile);
        provider = new ZipLeveledStructureProvider(sourceFile);
        // TODO: store only the workflows (dirs are created automatically)
        Object child = provider.getRoot();
        WorkflowImportElementFromArchive root =
                new WorkflowImportElementFromArchive(provider, child,
                        0);
        collectWorkflowsFromProvider(root);
        return root;
    }

    /**
     *
     * @param parent the archive element to collect the workflows from
     * @param monitor progress monitor
     */
    private void collectWorkflowsFromProvider(
            final WorkflowImportElementFromArchive parent) {
        ILeveledImportStructureProvider provider = parent.getProvider();
        Object entry = parent.getEntry();
        if (parent.isWorkflow() || parent.isTemplate()) {
            // abort recursion
            return;
        }
        List children = provider.getChildren(entry);
        if (children == null) {
            return;
        }
        Iterator childrenEnum = children.iterator();
        while (childrenEnum.hasNext()) {
            Object child = childrenEnum.next();
            if (provider.isFolder(child)) {
                WorkflowImportElementFromArchive childElement =
                        new WorkflowImportElementFromArchive(provider, child,
                                parent.getLevel() + 1);
                collectWorkflowsFromProvider(childElement);
                // either it's a workflow
                if (childElement.isWorkflow()
                // or it is a workflow group
                        || childElement.isWorkflowGroup()
                        // or it is a workflow template
                        || childElement.isTemplate()) {
                    /* because only workflows, templates and workflow groups are
                     * added */
                    parent.addChild(childElement);
                }
            }
        }
    }

    /**
     * @param monitor the monitor to report progress
     * @throws CoreException if the download does not complete without warnings
     *
     * @since 6.4
     */
    public final void runSync(final IProgressMonitor monitor)
            throws CoreException {
        String srcIdentifier = getSourceFile().getMountIDWithFullPath();
        if (!isSourceSupported()) {
            throw new IllegalArgumentException("Type of download source '"
                    + srcIdentifier
                    + "' is not supported.");
        }
        LOGGER.debug("Downloading '" + srcIdentifier
                + "' into local destination '" + getTargetIdentifier() + "'");

        final DownloadRunnable dwnLoader = new DownloadRunnable(
                getSourceFile());
        dwnLoader.run(monitor);

        // now wait for the download to finish
        boolean success = false;
        LOGGER.info("Waiting for download to finish...(" + srcIdentifier + ")");
        success = dwnLoader.waitUntilDone();

        // error handling if download failed
        File tmpLoc = dwnLoader.getTempFile();
        if (tmpLoc == null || !success) {
            String msg = "Unable to download workflow: ";
            if (success) {
                msg += dwnLoader.getErrorMessage();
                LOGGER.error(msg);
            } else {
                msg += " Download interrupted.";
                LOGGER.warn(msg);
            }
            if (getTargetDir().fetchInfo().exists()) {
                LOGGER.info("Existing destination not modified ("
                        + getTargetIdentifier() + ") ");
            }
            throw new CoreException(new Status(IStatus.WARNING,
                                ExplorerActivator.PLUGIN_ID, msg));
        }

        prepareTarget();

        try {
            extractDownloadToTarget(tmpLoc);
        } catch (Exception e) {
            LOGGER.error("Unable to extract the download. ", e);
        } finally {
            tmpLoc.delete();
        }

        refreshTarget();
        Status status = dwnLoader.getStatus();
        if (status != null) {
            throw new CoreException(status);
        }
    }

    /**
     * @since 6.4
     */
    public void schedule() {
        ExplorerJob j = new ExplorerJob("Download of " + getSourceFile().getName() + " to "
                + getTargetDir().getMountIDWithFullPath()) {
            @Override
            protected IStatus run(final IProgressMonitor monitor2) {
                monitor2.beginTask("Downloading " + getSourceFile().getName() + " to "
                        + getTargetDir().getMountIDWithFullPath() , 1);
                try {
                    runSync(monitor2);
                } catch (CoreException e) {
                    LOGGER.info("Failed downloading " + getSourceFile().getMountIDWithFullPath() + " to "
                        + getTargetDir().getMountIDWithFullPath() + ": " + e.getMessage(), e);
                    return e.getStatus();
                }
                return Status.OK_STATUS;
            }
        };
        j.schedule();
    }

    //=========================================================================

    /**
     * Downloads a remote file store to a local temp dir.
     *
     * @author Peter Ohl, KNIME.com, Zurich, Switzerland
     * @since 6.4
     *
     */
    protected static class DownloadRunnable implements Runnable {
        private static final NodeLogger LOGGER = NodeLogger.getLogger(
                DownloadRunnable.class);

        private final RemoteExplorerFileStore m_source;

        private final AtomicBoolean m_cancel = new AtomicBoolean(false);

        // this is also used as object to wait for the runnable to finish
        private final AtomicBoolean m_done = new AtomicBoolean(false);

        private File m_tmpFile;

        private String m_errorMsg;

        private MultiStatus m_status = null;

        /**
         * Returns the collected status of the download operation. If some
         * items could not be downloaded, e.g. due to missing permissions,
         * they are collected as a MultiStatus.
         *
         * @return the status of the download or null if no status messages
         *      are available
         *
         * @since 3.0
         */
        public Status getStatus() {
            return m_status;
        }

        /**
         * @param source the file store to download
         *
         */
        public DownloadRunnable(final RemoteExplorerFileStore source) {
            if (source == null) {
                throw new NullPointerException("Download source can't be null");
            }
            m_source = source;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            run(null);
        }

        /**
         * @param monitor the progress monitor
         */
        public void run(final IProgressMonitor monitor) {
            try {
                if (monitor != null) {
                    StringBuilder progMsg = null;
                    progMsg = new StringBuilder("Compressing workflow ");
                    progMsg.append(m_source.getFullName());
                    progMsg.append(" on the server. ");
                    monitor.setTaskName(progMsg.toString());
                    // we progress over kilobytes in case people download
                    // flows larger than 4GB. Have fun.
                    monitor.beginTask(progMsg.toString(),
                            IProgressMonitor.UNKNOWN);
                }
                RemoteDownloadStream in =
                        m_source.openDownloadStream();
                // wait for the server to finish zipping
                while (!in.readyForDownload()) {
                    if (monitor != null && monitor.isCanceled()) {
                        m_cancel.set(true);
                    }
                    if (m_cancel.get()) {
                        m_tmpFile = null;
                        m_errorMsg = "Canceled.";
                        // cancel server
                        in.close();
                        return;
                    }
                   Thread.sleep(1000);
                }
                if (monitor != null) {
                    int kbyte = IProgressMonitor.UNKNOWN;
                    long l = in.length();
                    if (l >= 0) {
                        kbyte = (int)(l >> 10);
                    }
                    StringBuilder progMsg = null;
                    progMsg = new StringBuilder("Downloading workflow ");
                    progMsg.append(m_source.getFullName());
                    progMsg.append(" (" + (kbyte >> 10) + "MB).");
                    monitor.setTaskName(progMsg.toString());
                    // we progress over kilobytes in case people download
                    // flows larger than 4GB. Have fun.
                    monitor.beginTask(progMsg.toString(), kbyte);
                }
                m_tmpFile = File.createTempFile("KNIMEServerDownload", ".tmp");
                LOGGER.debug("Received server download stream for '" + m_source
                        + "', storing it '"
                        + m_tmpFile.getAbsolutePath() + "'");
                BufferedInputStream inStream = null;
                FileOutputStream outStream = null;
                try {
                    inStream = new BufferedInputStream(in, 1024 * 1024);
                    outStream = new FileOutputStream(m_tmpFile);
                    int b;
                    byte[] buffer = new byte[1024 * 1024];
                    while ((b = inStream.read(buffer)) >= 0) {
                        outStream.write(buffer, 0, b);
                        if (monitor != null) {
                            monitor.worked(b >> 10);
                            if (monitor.isCanceled()) {
                                m_cancel.set(true);
                            }
                        }
                        if (m_cancel.get()) {
                            m_tmpFile.delete();
                            m_tmpFile = null;
                            m_errorMsg = "Canceled.";
                            return;
                        }
                    }
                } finally {
                    try {
                        if (inStream != null) {
                            inStream.close();
                        }
                    } catch (Exception ee) {
                        // not closing then...
                    }
                    try {
                        if (outStream != null) {
                            outStream.close();
                        }
                    } catch (Exception ee) {
                        // not closing then...
                    }
                }
                m_errorMsg = null;
                List<String> messages = null;
                try {
                    messages = in.getMessages();
                } catch (Exception e) {
                    messages = Collections.emptyList();
                    LOGGER.error("Could not retrieve download messages.", e);
                }

                if (messages.size() > 0) {
                    final List<IStatus> result = new LinkedList<IStatus>();
                    for (String msg : messages) {
                        result.add(new Status(IStatus.WARNING,
                                ExplorerActivator.PLUGIN_ID, msg));
                    }
                    m_status = new MultiStatus(
                            ExplorerActivator.PLUGIN_ID,
                            IStatus.WARNING, result.toArray(new IStatus[0]),
                            "Could not download all contained files due to "
                            + "missing permissions. Skipped items:", null);
                }
            } catch (Throwable e) {
                m_tmpFile = null;
                m_errorMsg = e.getMessage();
            } finally {
                done();
            }
        }

        /**
         * Doesn't cancel server side activities and doesn't interrupt until
         * server response is received.
         */
        public void cancel() {
            m_cancel.set(true);
        }

        private void done() {
            synchronized (m_done) {
                m_done.set(true);
                m_done.notifyAll();
            }
        }

        /**
         * Waits until the download has finished.
         *
         * @return true if the download has successfully finished, false
         *      if it has been canceled
         */
        public boolean waitUntilDone() {
            synchronized (m_done) {
                if (m_done.get()) {
                    return !m_cancel.get();
                }
                try {
                    m_done.wait();
                } catch (InterruptedException e) {
                    return !m_cancel.get();
                }
                return !m_cancel.get();
            }
        }

        /**
         * @return true if the download has successfully finished, false
         *      otherwise
         */
        public boolean finished() {
            synchronized (m_done) {
                return m_done.get();
            }
        }

        /**
         *
         * @return null until the jobs finishes. If successful a file with the
         *         archive. If unsuccessfully zipped, also null, and the error
         *         message is set.
         */
        public File getTempFile() {
            synchronized (m_done) {
                if (m_done.get()) {
                    return m_tmpFile;
                }
                return null;
            }
        }

        /**
         * Should contain something if {@link #getTempFile()} returns null.
         *
         * @return
         */
        public String getErrorMessage() {
            return m_errorMsg;
        }
    }

}
