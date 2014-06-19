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
 * ---------------------------------------------------------------------
 *
 */
package org.knime.workbench.ui.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.port.database.DatabaseDriverLoader;
import org.knime.workbench.core.KNIMECorePlugin;
import org.knime.workbench.core.preferences.HeadlessPreferencesConstants;

/**
 * Preference page used to load additional database drivers.
 *
 * @author Thomas Gabriel, University of Konstanz
 */
public class DatabasePreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    /**
	 *
	 */
	public DatabasePreferencePage() {
        super("Database preferences", null, GRID);
        setDescription("Let's you load additional database driver from Jar or Zip archive and set other database "
            + "related preferences.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFieldEditors() {
		final Shell shell = Display.getDefault().getActiveShell();
        addField(new ListEditor(HeadlessPreferencesConstants.P_DATABASE_DRIVERS,
        		"List of loaded database driver files:",
        		getFieldEditorParent()) {
 			@Override
			protected String[] parseString(final String string) {
 			    ArrayList<String> result = new ArrayList<String>();
 			    ArrayList<String> failed = new ArrayList<String>();
				String[] strings = string.split(";");
				for (String str : strings) {
					try {
					    if (str != null && !str.trim().isEmpty()) {
    						DatabaseDriverLoader.loadDriver(new File(str));
    						result.add(str);
					    }
					} catch (IOException ioe) {
					    failed.add(str);
					}
				}
				if (!failed.isEmpty()) {
                    setErrorMessage("Some driver file(s) are not available anymore: " + failed.toString());
				}
				return result.toArray(new String[0]);
			}

			@Override
			protected String getNewInputObject() {
			    FileDialog dialog = new FileDialog(shell);
			    dialog.setFilterExtensions(new String[]{"*.jar", "*.zip"});
				String fileName = dialog.open();
				if (fileName == null) {
				    return null;
				}
				try {
					DatabaseDriverLoader.loadDriver(new File(fileName));
					return fileName;
				} catch (IOException ioe) {
				    setErrorMessage(ioe.getMessage());
					return null;
				}
			}

			@Override
			protected String createList(final String[] string) {
				StringBuilder res = new StringBuilder();
				for (int i = 0; i < string.length; i++) {
					if (i > 0) {
						res.append(';');
					}
					res.append(string[i]);
				}
				return res.toString();
			}
		});

        addField(new IntegerFieldEditor(HeadlessPreferencesConstants.P_DATABASE_TIMEOUT,
            "Timeout for database operations (in seconds)", getFieldEditorParent(), 5));
        if (DatabaseConnectionSettings.getSystemPropertyDatabaseTimeout() >= 0) {
            setMessage("Database timeout preference will override system property", IMessageProvider.WARNING);
        }
	}

    /**
     * {@inheritDoc}
     */
	@Override
    public void init(final IWorkbench workbench) {
        IPreferenceStore corePrefStore =
            KNIMECorePlugin.getDefault().getPreferenceStore();
        setPreferenceStore(corePrefStore);
    }

}
