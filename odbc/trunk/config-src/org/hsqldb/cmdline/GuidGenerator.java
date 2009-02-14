/**  
 * @(#)$Id$
 *
 * HyperSQL ODBC Driver.
 *
 * Copyright (C) 2009 Blaine Simpson and the HSQL Development Group.
 * Copyright (C) <year>  <name of author>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package org.hsqldb.cmdline;

import com.eaio.uuid.UUID;

public class GuidGenerator {
    /**
     * Writes a new GUID to stdout, with upper case letters, and with no line
     * terminator character.
     */
    static public void main(String[] sa) {
        System.out.print(new UUID().toString().toUpperCase());
    }
}
