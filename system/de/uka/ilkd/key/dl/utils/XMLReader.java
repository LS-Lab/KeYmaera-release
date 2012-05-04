/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
/* $Id: XMLReader.java 1.2 Fri, 24 Aug 2007 14:27:50 +0200 quesel $
 *
 * Copyright (C) 2006, Jan-David Quesel
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package de.uka.ilkd.key.dl.utils;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author jdq
 * @version $Id: XMLReader.java 1.2 Fri, 24 Aug 2007 14:27:50 +0200 quesel $
 */
public class XMLReader {

    private String fileName;

    private Document document;

    public XMLReader(String fileName) {
        this.fileName = fileName;
        document = readData();
    }

    private Document readData() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // factory.setValidating(true);
        // factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(getClass().getResourceAsStream(
                    "/" + fileName));
            // builder.reset();
            return document;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return Returns the document.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * @param document
     *                The document to set.
     */
    protected void setDocument(Document document) {
        this.document = document;
    }

    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }

}
