/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.compiler.compiler.xml.changeset;


import DecisionTableInputType.XLS;
import ResourceType.CHANGE_SET;
import ResourceType.DRL;
import ResourceType.DTABLE;
import ResourceType.PKG;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.drools.compiler.CommonTestMethodBase;
import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.io.impl.UrlResource;
import org.drools.core.xml.XmlChangeSetReader;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieSession;
import org.kie.internal.ChangeSet;
import org.kie.internal.builder.DecisionTableConfiguration;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.xml.sax.SAXException;


public class ChangeSetTest extends CommonTestMethodBase {
    @Test
    public void testXmlParser() throws IOException, SAXException {
        KnowledgeBuilderConfigurationImpl conf = new KnowledgeBuilderConfigurationImpl();
        XmlChangeSetReader xmlReader = new XmlChangeSetReader(conf.getSemanticModules());
        xmlReader.setClassLoader(ChangeSetTest.class.getClassLoader(), ChangeSetTest.class);
        String str = "";
        str += "<change-set ";
        str += "xmlns='http://drools.org/drools-5.0/change-set' ";
        str += "xmlns:xs='http://www.w3.org/2001/XMLSchema-instance' ";
        str += "xs:schemaLocation='http://drools.org/drools-5.0/change-set http://anonsvn.jboss.org/repos/labs/labs/jbossrules/trunk/drools-api/src/main/resources/change-set-1.0.0.xsd' >";
        str += "    <add> ";
        str += "        <resource source='http://www.domain.com/test.drl' type='DRL' />";
        str += "        <resource source='http://www.domain.com/test.xls' type='DTABLE' >";
        str += "            <decisiontable-conf worksheet-name='sheet10' input-type='XLS' />";
        str += "        </resource>";
        str += "    </add> ";
        str += "</change-set>";
        StringReader reader = new StringReader(str);
        ChangeSet changeSet = xmlReader.read(reader);
        Assert.assertEquals(2, changeSet.getResourcesAdded().size());
        UrlResource resource = ((UrlResource) (((List) (changeSet.getResourcesAdded())).get(0)));
        Assert.assertEquals("http://www.domain.com/test.drl", resource.getURL().toString());
        Assert.assertEquals(DRL, resource.getResourceType());
        resource = ((UrlResource) (((List) (changeSet.getResourcesAdded())).get(1)));
        Assert.assertEquals("http://www.domain.com/test.xls", resource.getURL().toString());
        Assert.assertEquals(DTABLE, resource.getResourceType());
        DecisionTableConfiguration dtConf = ((DecisionTableConfiguration) (resource.getConfiguration()));
        Assert.assertEquals(XLS, dtConf.getInputType());
    }

    @Test
    public void testIntegregation() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("changeset1Test.xml", getClass()), CHANGE_SET);
        Assert.assertFalse(kbuilder.hasErrors());
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        KieSession ksession = createKnowledgeSession(kbase);
        List list = new ArrayList();
        ksession.setGlobal("list", list);
        ksession.fireAllRules();
        ksession.dispose();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.containsAll(Arrays.asList(new String[]{ "rule1", "rule2" })));
    }

    @Test
    public void testBasicAuthentication() throws IOException, SAXException {
        KnowledgeBuilderConfigurationImpl conf = new KnowledgeBuilderConfigurationImpl();
        XmlChangeSetReader xmlReader = new XmlChangeSetReader(conf.getSemanticModules());
        xmlReader.setClassLoader(ChangeSetTest.class.getClassLoader(), ChangeSetTest.class);
        String str = "";
        str += "<change-set ";
        str += "xmlns='http://drools.org/drools-5.0/change-set' ";
        str += "xmlns:xs='http://www.w3.org/2001/XMLSchema-instance' ";
        str += "xs:schemaLocation='http://drools.org/drools-5.0/change-set http://anonsvn.jboss.org/repos/labs/labs/jbossrules/trunk/drools-api/src/main/resources/change-set-1.0.0.xsd' >";
        str += "    <add> ";
        str += "        <resource source='http://localhost:8081/jboss-brms/org.kie.guvnor.Guvnor/package/defaultPackage/LATEST' type='PKG' basicAuthentication='enabled' username='admin' password='pwd'/>";
        str += "    </add> ";
        str += "</change-set>";
        StringReader reader = new StringReader(str);
        ChangeSet changeSet = xmlReader.read(reader);
        Assert.assertEquals(1, changeSet.getResourcesAdded().size());
        UrlResource resource = ((UrlResource) (((List) (changeSet.getResourcesAdded())).get(0)));
        Assert.assertEquals("http://localhost:8081/jboss-brms/org.kie.guvnor.Guvnor/package/defaultPackage/LATEST", resource.getURL().toString());
        Assert.assertEquals("enabled", resource.getBasicAuthentication());
        Assert.assertEquals("admin", resource.getUsername());
        Assert.assertEquals("pwd", resource.getPassword());
        Assert.assertEquals(PKG, resource.getResourceType());
    }

    @Test(timeout = 10000)
    public void testCustomClassLoader() throws Exception {
        // JBRULES-3630
        String absolutePath = new File("file").getAbsolutePath();
        URL url = ChangeSetTest.class.getResource(((ChangeSetTest.class.getSimpleName()) + ".class"));
        File file = new File(url.toURI());
        File jar = null;
        while (true) {
            file = file.getParentFile();
            jar = new File(file, "/src/test/resources/org/drools/compiler/compiler/xml/changeset/changeset.jar");
            if (jar.exists()) {
                break;
            }
        } 
        ClassLoader classLoader = URLClassLoader.newInstance(new URL[]{ jar.toURI().toURL() }, getClass().getClassLoader());
        Resource changeSet = ResourceFactory.newClassPathResource("changeset1.xml", classLoader);
        KnowledgeBuilderConfiguration conf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(null, classLoader);
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder(conf);
        builder.add(changeSet, CHANGE_SET);
    }
}

