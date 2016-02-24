/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.osgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;
import javax.jcr.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.query.TeiidService;
import org.komodo.spi.runtime.version.DefaultTeiidVersion;
import org.komodo.spi.runtime.version.DefaultTeiidVersion.Version;
import org.komodo.spi.runtime.version.TeiidVersion;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class TestPluginService implements StringConstants {

    /**
     * The prefix of all teiid service bundles
     */
    private static final String TEIID_BUNDLE_PREFIX = "org.komodo.plugins.teiid" + DOT;

    private static final String TEST_BUNDLES_DIR = "testBundles";

    private static final String HELLO_PATH = TEST_BUNDLES_DIR + File.separator + "hello-1.0.jar";

    private PluginService service;

    private int bundleCount() throws Exception {
        URL idxUrl = getClass().getClassLoader().getResource(PluginService.INDEX);

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(idxUrl.openStream());

        // Use XPath to locate the teiid jar filenames
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        // XPath expression to find all the teiid jar filenames
        XPathExpression expr = xpath.compile("/teiid/filename/text()");

        //evaluate expression result on XML document
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        assertTrue(nodes.getLength() > 0);

        return nodes.getLength();
    }

    private void installTestBundle() throws Exception {
        URL helloBundleUrl = getClass().getClassLoader().getResource(HELLO_PATH);
        service.installBundle(helloBundleUrl);
    }

    @Before
    public void setup() throws Exception {
        bundleCount();

        if (service == null)
            service = PluginService.getInstance();

        service.start();
    }

    @After
    public void teardown() throws Exception {
        if (service == null)
            return;

        service.shutdown();
    }

    @Test
    public void testPluginServiceBasics() throws Exception {
        assertEquals(Bundle.ACTIVE, service.getState());

        assertEquals(bundleCount() + 1, service.installedBundles().size());

        service.shutdown();

        // Should be resolved but not active
        assertEquals(Bundle.RESOLVED, service.getState());
    }

    @Test
    public void testBundleInstallation() throws Exception {
        assertEquals(Bundle.ACTIVE, service.getState());

        installTestBundle();

        List<String> bundles = service.installedBundles();
        assertEquals(bundleCount() + 2, bundles.size()); // contains framework bundle, hello and teiid bundles

        boolean hasHello = false;
        for (String bundle : bundles) {
            if (bundle.equals("Hello")) {
                hasHello = true;
                break;
            }
        }

        assertTrue(hasHello);
    }

    @Test
    public void testBundleStartingStopping() throws Exception {
        assertEquals(Bundle.ACTIVE, service.getState());

        installTestBundle();

        String bundleName = "Hello";
        service.startBundle(bundleName);
        assertEquals(Bundle.ACTIVE, service.bundleState(bundleName));

        service.stopBundle(bundleName);
        assertEquals(Bundle.RESOLVED, service.bundleState(bundleName));
    }

    @Test
    public void testTeiidBundleStartingStopping() throws Exception {
        assertEquals(Bundle.ACTIVE, service.getState());

        TeiidVersion version = Version.TEIID_8_12.get();
        String bundleName = TEIID_BUNDLE_PREFIX + version.getMajor() + DOT + version.getMinor();
        service.startBundle(bundleName);
        assertEquals(Bundle.ACTIVE, service.bundleState(bundleName));

        service.stopBundle(bundleName);
        assertEquals(Bundle.RESOLVED, service.bundleState(bundleName));
    }

    @Test
    public void testBundleIndex() throws Exception {
        assertEquals(Bundle.ACTIVE, service.getState());

        Set<TeiidVersion> versions = service.getSupportedTeiidVersions();
        TeiidVersion TEIID_8_12_4 = Version.TEIID_8_12_4.get();
        versions.contains(TEIID_8_12_4);
        DefaultTeiidVersion TEIID_8_12_x = new DefaultTeiidVersion(
                                                                                       TeiidVersion.EIGHT, 
                                                                                       TeiidVersion.ONE + TeiidVersion.TWO,
                                                                                       TeiidVersion.WILDCARD);
        versions.contains(TEIID_8_12_x);

        TeiidService teiidService1 = service.getTeiidService(TEIID_8_12_4);
        assertNotNull(teiidService1);
        String teiidService1Bundle = teiidService1.getParentBundle();
        String teiidService1Version = teiidService1.getVersion().toString();

        TeiidService teiidService2 = service.getTeiidService(TEIID_8_12_x);
        assertNotNull(teiidService2);
        String teiidService2Bundle = teiidService2.getParentBundle();
        String teiidService2Version = teiidService2.getVersion().toString();

        // Same bundle referred to by both versions
        assertEquals(teiidService1Bundle, teiidService2Bundle);
        assertEquals(teiidService1Version, teiidService2Version);
    }

    @Test
    public void testFindTeiidBundleAndStart() throws Exception {
        assertEquals(Bundle.ACTIVE, service.getState());

        TeiidVersion version = Version.TEIID_8_12.get();
        TeiidService teiidService = service.getTeiidService(version);
        assertNotNull(teiidService);

        assertEquals(TEIID_BUNDLE_PREFIX + version.getMajor() + DOT + version.getMinor(),
                     teiidService.getParentBundle());
        TeiidVersion actualVersion = Version.TEIID_8_12_4.get();
        assertEquals(actualVersion, teiidService.getVersion());

        // Should NOT throw an exception but complete correctly
        teiidService.getVersion();
    }

    @Test
    @Ignore
    public void testFindTeiidBundlesAndStartStopStartDifferentOne() throws Exception {
        assertEquals(Bundle.ACTIVE, service.getState());

        TeiidVersion version = Version.TEIID_8_12.get();
        TeiidService teiidService = service.getTeiidService(version);
        assertNotNull(teiidService);

        assertEquals(TEIID_BUNDLE_PREFIX + version.getMajor() + DOT + version.getMinor(),
                     teiidService.getParentBundle());
        TeiidVersion actualVersion = Version.TEIID_8_12_4.get();
        assertEquals(actualVersion, teiidService.getVersion());

        // Should NOT throw an exception but complete correctly
        teiidService.getVersion();

        //
        // Fetch teiid 8.11 version
        // * Stops teiid 8.12
        // * Starts teiid 8.11
        // * Returns teiid service
        //

        version = Version.TEIID_8_11.get();
        teiidService = service.getTeiidService(version);
        assertNotNull(teiidService);

        String bundleId = TEIID_BUNDLE_PREFIX + version.getMajor() + DOT + version.getMinor();
        assertEquals(bundleId, teiidService.getParentBundle());
        actualVersion = Version.TEIID_8_11_5.get();
        assertEquals(actualVersion, teiidService.getVersion());

        // Should NOT throw an exception but complete correctly
        teiidService.getVersion();

        service.stopBundle(bundleId);
        assertNull(service.getTeiidService());
    }

    @Test
    public void testJcrPassesAcrossBundleClassLoadingFence() throws Exception {
        assertEquals(Bundle.ACTIVE, service.getState());

        TeiidVersion version = Version.TEIID_8_12.get();
        TeiidService teiidService = service.getTeiidService(version);
        assertNotNull(teiidService);

        // Call convert() to check that javax.jcr.Node is export by the PluginService and correctly
        // imported by the bundle
        // Calling convert with a null sql parameter should return the method without an exception
        Node node = Mockito.mock(Node.class);
        teiidService.nodeConvert(null, node);
    }
}
