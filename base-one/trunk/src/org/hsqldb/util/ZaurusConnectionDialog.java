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
import java.applet.*;
import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Class declaration
 *
 *
 * @author ulrivo@users
 * @version 1.0.0.1
 */
public class ZaurusConnectionDialog extends ConnectionDialog implements ActionListener,
	ItemListener, KeyListener {

    final static String sJDBCTypes[][] = {
	 {
	    "HSQL In-Memory", "org.hsqldb.jdbcDriver",
	    "jdbc:hsqldb:."
	}, {
	    "HSQL Standalone", "org.hsqldb.jdbcDriver",
	    "jdbc:hsqldb:test"
	},  {
	    "MM.MySQL", "org.gjt.mm.mysql.Driver", 
	    "jdbc:mysql://localhost/"
	},{
	    "JDBC-ODBC Brigde from Sun", "sun.jdbc.odbc.JdbcOdbcDriver",
	    "jdbc:odbc:test"
	}, {
	    "Oracle", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:oci8:@"
	}, {
	    "IBM DB2", "COM.ibm.db2.jdbc.app.DB2Driver", "jdbc:db2:test"
	}, {
	    "Cloudscape RMI", "RmiJdbc.RJDriver",
	    "jdbc:rmi://localhost:1099/jdbc:cloudscape:test;create=true"
	}, {
	    "InstantDb", "jdbc.idbDriver", "jdbc:idb:sample.prp"
	}, {
	    "PointBase", "com.pointbase.jdbc.jdbcUniversalDriver",
	    "jdbc:pointbase://localhost/sample"
	},    // PUBLIC / public
    };
    /**
     * Constructor declaration
     *
     *
     * @param owner
     * @param title
     */
    ZaurusConnectionDialog(Frame owner, String title) {
        super(owner, title);
	addKeyListener(this);
    }

    /**
     * Method declaration
     *
     */
    void create(Insets defInsets) {

        setLayout(new BorderLayout());
        addKeyListener(this);

        Panel p = new Panel(new GridLayout(6, 2, 10, 10));
	p.addKeyListener(this);

        p.setBackground(SystemColor.control);
        p.add(createLabel("Type:"));

        Choice types = new Choice();

        types.addItemListener(this);
	types.addKeyListener(this);

        for (int i = 0; i < sJDBCTypes.length; i++) {
            types.add(sJDBCTypes[i][0]);
        }

        p.add(types);
        p.add(createLabel("Driver:"));

        mDriver = new TextField("org.hsqldb.jdbcDriver");
	mDriver.addKeyListener(this);

        p.add(mDriver);
        p.add(createLabel("URL:"));

        mURL = new TextField("jdbc:hsqldb:.");
	mURL.addKeyListener(this);

        p.add(mURL);
        p.add(createLabel("User:"));

        mUser = new TextField("sa");
	mUser.addKeyListener(this);

        p.add(mUser);
        p.add(createLabel("Password:"));

        mPassword = new TextField("");
	mPassword.addKeyListener(this);

        mPassword.setEchoChar('*');
        p.add(mPassword);

        Button b;

        b = new Button("Cancel");

        b.setActionCommand("ConnectCancel");
        b.addActionListener(this);
	b.addKeyListener(this);
        p.add(b);
        b = new Button("Ok");

        b.setActionCommand("ConnectOk");
        b.addActionListener(this);
	b.addKeyListener(this);
        p.add(b);

        setLayout(new BorderLayout());
        add("East", createLabel(" "));
        add("West", createLabel(" "));

        mError = new Label("");

        Panel pMessage = createBorderPanel(mError);
	pMessage.addKeyListener(this);

        add("South", pMessage);
	add("North", createLabel(""));
        add("Center", p);
        doLayout();
        pack();

        Dimension d    = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = getSize();

	if (d.width > 640) {
	    setLocation((d.width - size.width) / 2,
			      (d.height - size.height) / 2);
	} else if (defInsets.top > 0 && defInsets.left > 0) {
	    setLocation(defInsets.bottom, defInsets.right);
	    setSize(defInsets.top, defInsets.left);
	// full size on screen with less than 640 width
	} else {
	    setLocation(0,0);
	    setSize(d);
	}
	show();
    }
    /**
     * Method declaration
     *
     *
     * @param ev
     */
    public void actionPerformed(ActionEvent ev) {

        String s = ev.getActionCommand();

	//	System.out.println("Action performed " + s);
        if (s.equals("ConnectOk")) {
	    finishCreate();
        } else if (s.equals("ConnectCancel")) {
            dispose();
        }
    }

    //    public boolean isFocusTraversable() { return true; }

    public void keyPressed(KeyEvent k) {
	//	System.out.println("Key pressed: " + k.getKeyCode());
	if (k.getKeyCode() == KeyEvent.VK_ENTER) {
	    finishCreate();
	} else if (k.getKeyCode() == KeyEvent.VK_ESCAPE) {
	    dispose();
	}
    }

    public void keyTyped(KeyEvent k) {}

    public void keyReleased(KeyEvent k) {}

    /**
     * Method declaration
     *
     *
     * @param ev
     */
    public void windowClosing(WindowEvent ev) {

	//	System.out.println("windowClosing");
    }


    /**
     * Method declaration
     *
     *
     */
    protected void finishCreate() {
	try {
	    mConnection = createConnection(mDriver.getText(),
					   mURL.getText(),
					   mUser.getText(),
					   mPassword.getText());
	    
	    dispose();
	} catch (Exception e) {
	    e.printStackTrace();
	    mError.setText(e.toString());
	}
    }

    /**
     * Method declaration
     *
     *
     * @param owner
     * @param title
     *
     * @return
     */
    public static Connection createConnection(Frame owner, String title, Insets defInsets) {

        ZaurusConnectionDialog dialog = new ZaurusConnectionDialog(owner, title);

        dialog.create(defInsets);

        return dialog.mConnection;
    }
    /**
     * Method declaration
     *
     *
     * @param e
     */
    public void itemStateChanged(ItemEvent e) {

        String s = (String) e.getItem();

        for (int i = 0; i < sJDBCTypes.length; i++) {
            if (s.equals(sJDBCTypes[i][0])) {
                mDriver.setText(sJDBCTypes[i][1]);
                mURL.setText(sJDBCTypes[i][2]);
            }
        }
    }

}
