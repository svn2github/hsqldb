/* Copyrights and Licenses
 *
 * This product includes Hypersonic SQL.
 * Originally developed by Thomas Mueller and the Hypersonic SQL Group. 
 *
 * Copyright (c) 1995-2000 by the Hypersonic SQL Group. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: 
 *     -  Redistributions of source code must retain the above copyright notice, this list of conditions
 *         and the following disclaimer. 
 *     -  Redistributions in binary form must reproduce the above copyright notice, this list of
 *         conditions and the following disclaimer in the documentation and/or other materials
 *         provided with the distribution. 
 *     -  All advertising materials mentioning features or use of this software must display the
 *        following acknowledgment: "This product includes Hypersonic SQL." 
 *     -  Products derived from this software may not be called "Hypersonic SQL" nor may
 *        "Hypersonic SQL" appear in their names without prior written permission of the
 *         Hypersonic SQL Group. 
 *     -  Redistributions of any form whatsoever must retain the following acknowledgment: "This
 *          product includes Hypersonic SQL." 
 * This software is provided "as is" and any expressed or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the Hypersonic SQL Group or its contributors be liable for any
 * direct, indirect, incidental, special, exemplary, or consequential damages (including, but
 * not limited to, procurement of substitute goods or services; loss of use, data, or profits;
 * or business interruption). However caused any on any theory of liability, whether in contract,
 * strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this
 * software, even if advised of the possibility of such damage. 
 * This software consists of voluntary contributions made by many individuals on behalf of the
 * Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2002, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer, including earlier
 * license statements (above) and comply with all above license conditions.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution, including earlier
 * license statements (above) and comply with all above license conditions.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG, 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.util;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

/**
 *  Utility program (or applet) for transferring tables between different
 *  databases via JDBC. Understands HSQLDB database particularly well.
 *
 * @version 1.7.0
 */

// fredt@users 20011220 - patch 481239 by xponsard@users - enhancements
// enhancements to support saving and loading of transfer settings,
// transfer of blobs, and catalog and schema names in source db
// changes by fredt to allow saving and loading of transfer settings
// fredt@users 20020215 - patch 516309 by Nicolas Bazin - enhancements
// sqlbob@users 20020325 - patch 1.7.0 - reengineering
// nicolas BAZIN 20020430 - add Catalog selection, correct a bug preventing table
//    edition, change double quotes to simple quotes for default values of CHAR type
public class TransferSwing extends JApplet
implements WindowListener, ActionListener, ListSelectionListener,
           ItemListener, Traceable {

    final static int SELECT_BEGIN   = 0;
    final static int SELECT_CATALOG = 1;
    final static int SELECT_SCHEMA  = 2;
    final static int SELECT_TABLES  = 3;
    final static int TRFM_TRANSFER  = 1;
    final static int TRFM_DUMP      = 2;
    final static int TRFM_RESTORE   = 3;
    JFrame           fMain;
    DataAccessPoint  sourceDb;
    DataAccessPoint  targetDb;
    TransferTable    tCurrent;
    int              iMaxRows;
    int              iSelectionStep = SELECT_BEGIN;
    Vector           tTable;
    JList            lTable;
    DefaultListModel lTableModel;
    String           sSchemas[];
    String           sCatalog;
    JTextField tSourceTable, tDestTable, tDestDropIndex, tDestCreateIndex;
    JTextField       tDestDrop, tDestCreate, tDestDelete, tDestAlter;
    JTextField       tSourceSelect, tDestInsert;
    JCheckBox        cTransfer, cDrop, cCreate, cDelete, cInsert, cAlter;
    JCheckBox        cCreateIndex, cDropIndex;
    JCheckBox        cFKForced, cIdxForced;
    JButton          bStart, bContinue;
    JTextField       tMessage;
    int              iTransferMode;
    boolean          displaying = false;
    static boolean   bMustExit;
    int              CurrentTransfer, CurrentAlter;

    /**
     * Method declaration
     *
     *
     * @param s
     */
    public void trace(String s) {

        if ((s != null) &&!s.equals("")) {
            tMessage.setText(s);
            tMessage.paintImmediately(tMessage.getX(), tMessage.getY(),
                                      tMessage.getWidth(),
                                      tMessage.getHeight());

            if (TRACE) {
                System.out.println(s);
            }
        }
    }

    /**
     * Entry point if invoking TransferSwing as an applet.
     */
    public void init() {

        TransferSwing m = new TransferSwing();

        m._main(null);
    }

    /**
     * Alternate entrypoint if calling this utility from within
     *  another program.
     */
    public static void work(String arg[]) {

        TransferSwing m = new TransferSwing();

        m._main(arg);
    }

    /**
     * Yes, you can invoke this program as a standalone application
     *  (no arguments required).
     */
    public static void main(String arg[]) {

        System.getProperties().put("sun.java2d.noddraw", "true");

        bMustExit = true;

        work(arg);
    }

    private boolean CatalogToSelect() {

        lTableModel.removeAllElements();

        try {
            Vector result = sourceDb.getCatalog();

            if (result.size() > 1) {

                //lTable.getSelectionModel().setSelectionMode(
                //    ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                bStart.setText("Select Catalog");
                bStart.invalidate();
                bStart.setEnabled(true);

                for (Enumeration e =
                        result.elements(); e.hasMoreElements(); ) {
                    lTableModel.addElement(e.nextElement().toString());
                }

                lTable.repaint();
                trace("Select correct Catalog");
            } else if (result.size() == 1) {
                sCatalog = (String) result.firstElement();

                try {
                    targetDb.setCatalog(sCatalog);
                } catch (Exception ex) {
                    trace("Catalog " + sCatalog
                          + " could not be selected in the target database");

                    sCatalog = null;
                }
            }
        } catch (Exception exp) {
            lTableModel.removeAllElements();
            trace("Exception reading catalog: " + exp);
            exp.printStackTrace();
        }

        return (lTableModel.getSize() > 0);
    }

    private boolean SchemaToSelect() {

        lTableModel.removeAllElements();

        try {
            Vector result = sourceDb.getSchemas();

            if (result.size() > 1) {
                lTable.getSelectionModel().setSelectionMode(
                    ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                bStart.setText("Select Schema");
                bStart.invalidate();
                bStart.setEnabled(true);

                for (Enumeration e =
                        result.elements(); e.hasMoreElements(); ) {
                    lTableModel.addElement(e.nextElement().toString());
                }

                lTable.repaint();
                trace("Select correct Schema or load Settings file");
            } else if (result.size() == 1) {
                sSchemas    = new String[1];
                sSchemas[0] = (String) result.firstElement();
            }
        } catch (Exception exp) {
            lTableModel.removeAllElements();
            trace("Exception reading schemas: " + exp);
            exp.printStackTrace();
        }

        return (lTableModel.getSize() > 0);
    }

    private void nextState() {

        switch (iSelectionStep) {

            case SELECT_BEGIN :
                iSelectionStep = SELECT_CATALOG;
                sCatalog       = null;

                if (CatalogToSelect()) {
                    fMain.show();

                    break;
                }
            case SELECT_CATALOG :
                iSelectionStep = SELECT_SCHEMA;
                sSchemas       = null;

                if (SchemaToSelect()) {
                    fMain.show();

                    break;
                }
            case SELECT_SCHEMA :
                if (iTransferMode == TRFM_TRANSFER) {
                    bStart.setText("Start Transfer");
                } else {
                    bStart.setText("Start Dump");
                }

                bStart.invalidate();
                bStart.setEnabled(false);
                lTable.getSelectionModel().setSelectionMode(
                    ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                lTableModel.removeAllElements();
                lTable.repaint();

                iSelectionStep = SELECT_TABLES;

                RefreshMainDisplay();
                break;
        }
    }

    void _main(String arg[]) {

        /*
        ** What function is asked from the transfer tool?
        */
        iTransferMode = TRFM_TRANSFER;

        if ((arg != null) && (arg.length > 0)) {
            if ((arg[0].toLowerCase().equals("-r"))
                    || (arg[0].toLowerCase().equals("--restore"))) {
                iTransferMode = TRFM_RESTORE;
            } else if ((arg[0].toLowerCase().equals("-d"))
                       || (arg[0].toLowerCase().equals("--dump"))) {
                iTransferMode = TRFM_DUMP;
            }
        }

        CommonSwing.setDefaultColor();

        fMain = new JFrame("HSQL Transfer Tool");

        // (ulrivo): An actual icon.
        fMain.setIconImage(CommonSwing.getIcon());
        fMain.addWindowListener(this);
        fMain.setSize(640, 480);
        fMain.getContentPane().add("Center", this);

        JMenuBar bar      = new JMenuBar();
        String   extras[] = {
            "Insert 10 rows only", "Insert 1000 rows only", "Insert all rows",
            "-", "Load Settings...", "Save Settings...", "-", "Exit"
        };
        JMenu menu = new JMenu("Options");

        addMenuItems(menu, extras);
        bar.add(menu);
        fMain.setJMenuBar(bar);
        initGUI();

        Dimension d    = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = fMain.getSize();

        // (ulrivo): full size on screen with less than 640 width
        if (d.width >= 640) {
            fMain.setLocation((d.width - size.width) / 2,
                              (d.height - size.height) / 2);
        } else {
            fMain.setLocation(0, 0);
            fMain.setSize(d);
        }

        fMain.setVisible(true);

        CurrentTransfer = CurrentAlter = 0;

        try {
            if ((iTransferMode == TRFM_DUMP)
                    || (iTransferMode == TRFM_TRANSFER)) {
                sourceDb = new TransferDb(
                    ConnectionDialog.createConnection(
                        fMain, "Source Database"), this);

                if (!sourceDb.isConnected()) {
                    cleanExit();

                    return;
                }
            }

            if ((iTransferMode == TRFM_RESTORE)
                    || (iTransferMode == TRFM_TRANSFER)) {
                targetDb = new TransferDb(
                    ConnectionDialog.createConnection(
                        fMain, "Target Database"), this);

                if (!targetDb.isConnected()) {
                    cleanExit();

                    return;
                }
            } else {
                FileDialog f = new FileDialog(fMain, "Dump FileName",
                                              FileDialog.SAVE);

                f.show();

                String sFileName = f.getFile();
                String Path      = f.getDirectory();

                if ((sFileName == null) || (sFileName.equals(""))) {
                    cleanExit();

                    return;
                } else {
                    targetDb = new TransferSQLText(Path + sFileName, this);
                }
            }
        } catch (Exception e) {
            cleanExit();
            e.printStackTrace();

            return;
        }

        if ((iTransferMode == TRFM_DUMP)
                || (iTransferMode == TRFM_TRANSFER)) {
            nextState();
        } else {
            fMain.show();
        }
    }

    private void RefreshMainDisplay() {

        lTableModel.removeAllElements();
        lTable.repaint();

        try {
            tTable = sourceDb.getTables(sCatalog, sSchemas);

            for (int i = 0; i < tTable.size(); i++) {
                TransferTable t = (TransferTable) tTable.elementAt(i);

                t.setDest(null, targetDb);
                t.getTableStructure();
                lTableModel.addElement(t.sSourceTable);
            }

            if (tTable.size() > 0) {
                lTable.setSelectedIndex(tTable.size() - 1);
            }

            bStart.setEnabled(true);

            if (iTransferMode == TRFM_TRANSFER) {
                trace("Edit definitions and press [Start Transfer]");
            } else if (iTransferMode == TRFM_DUMP) {
                trace("Edit definitions and press [Start Dump]");
            }
        } catch (Exception e) {
            trace("Exception reading source tables: " + e);
            e.printStackTrace();
        }

        fMain.show();
    }

    private void addMenuItems(JMenu f, String m[]) {

        for (int i = 0; i < m.length; i++) {
            if (m[i].equals("-")) {
                f.addSeparator();
            } else {
                JMenuItem item = new JMenuItem(m[i]);

                item.addActionListener(this);
                f.add(item);
            }
        }
    }

    /**
     * Method declaration
     *
     */
    private void saveTable() {

        if (tCurrent == null) {
            return;
        }

        TransferTable t = tCurrent;

        t.sSourceTable     = tSourceTable.getText();
        t.sDestTable       = tDestTable.getText();
        t.sDestDrop        = tDestDrop.getText();
        t.sDestCreateIndex = tDestCreateIndex.getText();
        t.sDestDropIndex   = tDestDropIndex.getText();
        t.sDestCreate      = tDestCreate.getText();
        t.sDestDelete      = tDestDelete.getText();
        t.sSourceSelect    = tSourceSelect.getText();
        t.sDestInsert      = tDestInsert.getText();
        t.bTransfer        = cTransfer.isSelected();
        t.bDrop            = cDrop.isSelected();
        t.bCreate          = cCreate.isSelected();
        t.bDelete          = cDelete.isSelected();
        t.bInsert          = cInsert.isSelected();
        t.bAlter           = cAlter.isSelected();
        t.bCreateIndex     = cCreateIndex.isSelected();
        t.bDropIndex       = cDropIndex.isSelected();

        boolean reparsetable = ((t.bFKForced != cFKForced.isSelected())
                                || (t.bIdxForced != cIdxForced.isSelected()));

        t.bFKForced  = cFKForced.isSelected();
        t.bIdxForced = cIdxForced.isSelected();

        if (reparsetable) {
            try {
                t.getTableStructure();
            } catch (Exception e) {
                trace("Exception reading source tables: " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @param t
     */
    private void displayTable(TransferTable t) {

        tCurrent = t;

        if (t == null) {
            return;
        }

        displaying = true;

        tSourceTable.setText(t.sSourceTable);
        tDestTable.setText(t.sDestTable);
        tDestDrop.setText(t.sDestDrop);
        tDestCreateIndex.setText(t.sDestCreateIndex);
        tDestDropIndex.setText(t.sDestDropIndex);
        tDestCreate.setText(t.sDestCreate);
        tDestDelete.setText(t.sDestDelete);
        tSourceSelect.setText(t.sSourceSelect);
        tDestInsert.setText(t.sDestInsert);
        tDestAlter.setText(t.sDestAlter);
        cTransfer.setSelected(t.bTransfer);
        cDrop.setSelected(t.bDrop);
        cCreate.setSelected(t.bCreate);
        cDropIndex.setSelected(t.bDropIndex);
        cCreateIndex.setSelected(t.bCreateIndex);
        cDelete.setSelected(t.bDelete);
        cInsert.setSelected(t.bInsert);
        cAlter.setSelected(t.bAlter);
        cFKForced.setSelected(t.bFKForced);
        cIdxForced.setSelected(t.bIdxForced);

        displaying = false;
    }

    /**
     * Method declaration
     *
     *
     * @param and
     */
    private void updateEnabled(boolean and) {

        boolean b = cTransfer.isSelected();

        tDestTable.setEnabled(and && b);
        tDestDrop.setEnabled(and && b && cDrop.isSelected());
        tDestCreate.setEnabled(and && b && cCreate.isSelected());
        tDestDelete.setEnabled(and && b && cDelete.isSelected());
        tDestCreateIndex.setEnabled(and && b && cCreateIndex.isSelected());
        tDestDropIndex.setEnabled(and && b && cDropIndex.isSelected());
        tSourceSelect.setEnabled(and && b);
        tDestInsert.setEnabled(and && b && cInsert.isSelected());
        tDestAlter.setEnabled(and && b && cAlter.isSelected());
        cDrop.setEnabled(and && b);
        cCreate.setEnabled(and && b);
        cDelete.setEnabled(and && b);
        cCreateIndex.setEnabled(and && b);
        cDropIndex.setEnabled(and && b);
        cInsert.setEnabled(and && b);
        cAlter.setEnabled(and && b);
        cFKForced.setEnabled(cAlter.isSelected());
        cIdxForced.setEnabled(cCreateIndex.isSelected());
        bStart.setEnabled(and);

        if (iTransferMode == TRFM_TRANSFER) {
            bContinue.setEnabled(and);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param ev
     */
    public void actionPerformed(ActionEvent ev) {

        if (ev.getSource() instanceof JTextField) {
            saveTable();

            return;
        }

        String    s = ev.getActionCommand();
        JMenuItem i = new JMenuItem();

        if (s == null) {
            if (ev.getSource() instanceof JMenuItem) {
                i = (JMenuItem) ev.getSource();
                s = i.getText();
            }
        }

        if (s.equals("Start Transfer") || s.equals("ReStart Transfer")) {
            bStart.setText("ReStart Transfer");
            bStart.invalidate();

            CurrentTransfer = 0;
            CurrentAlter    = 0;

            transfer();
        } else if (s.equals("Continue Transfer")) {
            transfer();
        } else if (s.equals("Start Dump")) {
            CurrentTransfer = 0;
            CurrentAlter    = 0;

            transfer();
        } else if (s.equals("Quit")) {
            cleanExit();
        } else if (s.equals("Select Catalog")) {
            sCatalog = (String) lTable.getSelectedValue();

            try {
                targetDb.setCatalog(sCatalog);
            } catch (Exception ex) {
                trace("Catalog " + sCatalog
                      + " could not be selected in the target database");

                sCatalog = null;
            }

            nextState();
        } else if (s.equals("Select Schema")) {
            sSchemas = (String[]) lTable.getSelectedValues();

            nextState();
        } else if (s.equals("Insert 10 rows only")) {
            iMaxRows = 10;
        } else if (s.equals("Insert 1000 rows only")) {
            iMaxRows = 1000;
        } else if (s.equals("Insert all rows")) {
            iMaxRows = 0;

            // fredt@users.sourceforge.net 20020130
        } else if (s.equals("Load Settings...")) {
            JFileChooser f = new JFileChooser(".");

            f.setDialogTitle("Load Settings");

            int option = f.showOpenDialog(fMain);

            if (option == JFileChooser.APPROVE_OPTION) {
                File file = f.getSelectedFile();

                if (file != null) {
                    LoadPrefs(file.getAbsolutePath());
                    displayTable(tCurrent);
                }
            }
        } else if (s.equals("Save Settings...")) {
            JFileChooser f = new JFileChooser(".");

            f.setDialogTitle("Save Settings");

            int option = f.showSaveDialog(fMain);

            if (option == JFileChooser.APPROVE_OPTION) {
                File file = f.getSelectedFile();

                if (file != null) {
                    SavePrefs(file.getAbsolutePath());
                }
            }
        } else if (s.equals("Exit")) {
            cleanExit();
        }
    }

    /**
     * This is a generic <code>ItemListener</code> for all
     *  user-accessible checkboxes in the UI.
     */
    public void itemStateChanged(ItemEvent e) {

        if (!displaying) {
            saveTable();
            updateEnabled(true);
        }
    }

    /**
     * Callback event whenever the current list selection has changed.
     */
    public void valueChanged(ListSelectionEvent e) {

        if (iSelectionStep == SELECT_TABLES) {
            int end = e.getLastIndex();

            for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
                if (lTable.isSelectedIndex(i)) {
                    String tableName = (String) lTableModel.getElementAt(i);

                    for (int j = 0; j < tTable.size(); j++) {
                        TransferTable t = (TransferTable) tTable.elementAt(j);

                        if (t != null && t.sSourceTable.equals(tableName)) {
                            saveTable();
                            displayTable(t);
                            updateEnabled(true);

                            break;
                        }
                    }

                    break;
                }
            }
        }
    }

    private void cleanExit() {

        try {
            if (sourceDb != null) {
                sourceDb.close();
            }

            if (targetDb != null) {
                targetDb.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            fMain.dispose();

            if (bMustExit) {
                System.exit(0);
            }
        }
    }

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    public void windowClosing(WindowEvent ev) {
        cleanExit();
    }

    private void initGUI() {

        getContentPane().setLayout(new BorderLayout());

        JPanel p = new JPanel();

        p.setLayout(new GridLayout(16, 1));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        Font fFont = new Font("Dialog", Font.PLAIN, 12);

        tSourceTable = new JTextField();

        tSourceTable.setEnabled(false);

        tDestTable = new JTextField();

        tDestTable.addActionListener(this);

        tDestDrop = new JTextField();

        tDestDrop.addActionListener(this);

        tDestCreate = new JTextField();

        tDestCreate.addActionListener(this);

        tDestDelete = new JTextField();

        tDestDelete.addActionListener(this);

        tDestCreateIndex = new JTextField();

        tDestCreateIndex.addActionListener(this);

        tDestDropIndex = new JTextField();

        tDestDropIndex.addActionListener(this);

        tSourceSelect = new JTextField();

        tSourceSelect.addActionListener(this);

        tDestInsert = new JTextField();

        tDestInsert.addActionListener(this);

        tDestAlter = new JTextField();

        tDestAlter.addActionListener(this);

        cTransfer = new JCheckBox("Transfer to destination table", true);

        cTransfer.addItemListener(this);

        cDrop = new JCheckBox("Drop destination table (ignore error)", true);

        cDrop.addItemListener(this);

        cCreate = new JCheckBox("Create destination table", true);

        cCreate.addItemListener(this);

        cDropIndex = new JCheckBox("Drop destination index (ignore error)",
                                   true);

        cDropIndex.addItemListener(this);

        cIdxForced = new JCheckBox("force Idx_ prefix for indexes names",
                                   false);

        cIdxForced.addItemListener(this);

        cCreateIndex = new JCheckBox("Create destination index", true);

        cCreateIndex.addItemListener(this);

        cDelete = new JCheckBox("Delete rows in destination table", true);

        cDelete.addItemListener(this);

        cInsert = new JCheckBox("Insert into destination", true);

        cInsert.addItemListener(this);

        cFKForced = new JCheckBox("force FK_ prefix for foreign key names",
                                  false);

        cFKForced.addItemListener(this);

        cAlter = new JCheckBox("Alter destination table", true);

        cAlter.addItemListener(this);
        p.add(createLabel("Source table"));
        p.add(tSourceTable);
        p.add(cTransfer);
        p.add(tDestTable);
        p.add(cDrop);
        p.add(tDestDrop);
        p.add(cCreate);
        p.add(tDestCreate);
        p.add(cDropIndex);
        p.add(tDestDropIndex);
        p.add(cCreateIndex);
        p.add(tDestCreateIndex);
        p.add(cDelete);
        p.add(tDestDelete);
        p.add(cAlter);
        p.add(tDestAlter);
        p.add(createLabel("Select source records"));
        p.add(tSourceSelect);
        p.add(cInsert);
        p.add(tDestInsert);
        p.add(createLabel(""));
        p.add(createLabel(""));
        p.add(cIdxForced);
        p.add(cFKForced);
        p.add(createLabel(""));
        p.add(createLabel(""));

        bStart    = new JButton("Start Transfer");
        bContinue = new JButton("Continue Transfer");

        bStart.addActionListener(this);
        p.add(bStart);
        bContinue.addActionListener(this);
        p.add(bContinue);
        bContinue.setEnabled(false);
        bStart.setEnabled(false);
        fMain.getContentPane().add(createBorderPanel(p), BorderLayout.CENTER);

        lTableModel = new DefaultListModel();
        lTable      = new JList(lTableModel);

        lTable.addListSelectionListener(this);

        JScrollPane scrollPane = new JScrollPane(lTable);

        fMain.getContentPane().add(scrollPane, BorderLayout.WEST);

        tMessage = new JTextField();

        JPanel pMessage = createBorderPanel(tMessage);

        fMain.getContentPane().add(pMessage, BorderLayout.SOUTH);
    }

    /**
     * Method declaration
     *
     *
     * @param center
     *
     * @return
     */
    private JPanel createBorderPanel(Component center) {

        JPanel p = new JPanel();

        p.setLayout(new BorderLayout());
        p.add(center, BorderLayout.CENTER);

        return p;
    }

    private JLabel createLabel(String s) {

        JLabel l = new JLabel(s);

        return l;
    }

    private void SavePrefs(String f) {
        saveTable();
        TransferCommon.savePrefs(f, sourceDb, targetDb, this, tTable);
    }

    private void LoadPrefs(String f) {

        TransferTable t;

        trace("Parsing Settings file");
        bStart.setEnabled(false);
        bContinue.setEnabled(false);

        tTable = TransferCommon.loadPrefs(f, sourceDb, targetDb, this);
        iSelectionStep = SELECT_TABLES;

        lTableModel.removeAllElements();

        for (int i = 0; i < tTable.size(); i++) {
            t = (TransferTable) tTable.elementAt(i);

            lTableModel.addElement(t.sSourceTable);
        }

        t = (TransferTable) tTable.elementAt(0);

        displayTable(t);
        lTable.setSelectedIndex(0);
        updateEnabled(true);
        lTable.invalidate();

        if (iTransferMode == TRFM_TRANSFER) {
            bStart.setText("Start Transfer");
            trace("Edit definitions and press [Start Transfer]");
        } else if (iTransferMode == TRFM_DUMP) {
            bStart.setText("Start Dump");
            trace("Edit definitions and press [Start Dump]");
        }

        bStart.invalidate();

        if (iTransferMode == TRFM_TRANSFER) {
            bContinue.setEnabled(false);
        }
    }

    /**
     * Method declaration
     *
     */
    private void transfer() {

        saveTable();
        updateEnabled(false);
        trace("Start Transfer");

        int           TransferIndex = CurrentTransfer;
        int           AlterIndex    = CurrentAlter;
        TransferTable t             = null;
        long          startTime, stopTime;

        startTime = System.currentTimeMillis();

        try {
            for (int i = TransferIndex; i < tTable.size(); i++) {
                CurrentTransfer = i;
                t               = (TransferTable) tTable.elementAt(i);

                lTable.setSelectedIndex(i);
                displayTable(t);
                t.transferStructure();
                t.transferData(iMaxRows);
            }

            for (int i = AlterIndex; i < tTable.size(); i++) {
                CurrentAlter = i;
                t            = (TransferTable) tTable.elementAt(i);

                lTable.setSelectedIndex(i);
                displayTable(t);
                t.transferAlter();
            }

            stopTime = System.currentTimeMillis();

            trace("Transfer finished successfully in: "
                  + (stopTime - startTime) / 1000.00 + " sec");

            if (iTransferMode == TRFM_TRANSFER) {
                bContinue.setText("Quit");
                bContinue.setEnabled(true);
                bContinue.invalidate();
            } else {
                bStart.setText("Quit");
                bStart.setEnabled(true);
                bStart.invalidate();
            }
        } catch (Exception e) {
            String last = tMessage.getText();

            trace("Transfer stopped - " + last + " /  / Error: "
                  + e.getMessage());
            e.printStackTrace();
        }

        if (iTransferMode == TRFM_TRANSFER) {
            bContinue.setEnabled((CurrentAlter < tTable.size()));
        }

        updateEnabled(true);
        System.gc();
    }
}
