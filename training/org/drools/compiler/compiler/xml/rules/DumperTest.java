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
package org.drools.compiler.compiler.xml.rules;


import org.assertj.core.api.Assertions;
import org.drools.compiler.compiler.DrlParser;
import org.drools.compiler.lang.DrlDumper;
import org.drools.compiler.lang.descr.PackageDescr;
import org.junit.Assert;
import org.junit.Test;
import org.kie.internal.builder.conf.LanguageLevelOption;


/**
 * Test the dump/convert format utilities.
 */
public class DumperTest {
    // Xml Dumper test
    @Test
    public void testRoundTripAccumulateXml() throws Exception {
        DumperTestHelper.XmlFile("test_ParseAccumulate.xml");
    }

    @Test
    public void testRoundTripCollectXml() throws Exception {
        DumperTestHelper.XmlFile("test_ParseCollect.xml");
    }

    @Test
    public void testRoundTripExistsXml() throws Exception {
        DumperTestHelper.XmlFile("test_ParseExists.xml");
    }

    @Test
    public void testRoundTripForallXml() throws Exception {
        DumperTestHelper.XmlFile("test_ParseForall.xml");
    }

    @Test
    public void testRoundTripFromXml() throws Exception {
        DumperTestHelper.XmlFile("test_ParseFrom.xml");
    }

    @Test
    public void testRoundTripComplexRuleXml() throws Exception {
        DumperTestHelper.XmlFile("test_RoundTrip.xml");
    }

    // Drl Dumper test
    @Test
    public void testRoundTripComplexRuleDrl() throws Exception {
        DumperTestHelper.DrlFile("test_RoundTrip.drl");
    }

    @Test
    public void testRoundTripCollectDrl() throws Exception {
        DumperTestHelper.DrlFile("../../../integrationtests/test_Collect.drl");
    }

    @Test
    public void testRoundTripAccumulateDrl() throws Exception {
        DumperTestHelper.DrlFile("test_accumulateall.drl");
    }

    @Test
    public void testRoundTripExistsDrl() throws Exception {
        DumperTestHelper.DrlFile("../../../integrationtests/test_exists.drl");
    }

    @Test
    public void testRoundTripForallDrl() throws Exception {
        DumperTestHelper.DrlFile("../../../integrationtests/test_Forall.drl");
    }

    @Test
    public void testRoundTripFromDrl() throws Exception {
        DumperTestHelper.DrlFile("test_from.drl");
    }

    @Test
    public void testRoundTripSimpleRuleDrl() throws Exception {
        DumperTestHelper.DrlFile("test_simplerule.drl");
    }

    @Test
    public void testRoundTripPComplexDrl() throws Exception {
        DumperTestHelper.DrlFile("test_complex.drl");
    }

    @Test
    public void testRoundTripDRLAnnotations() throws Exception {
        DumperTestHelper.DrlFile("test_DumpAnnotations.drl");
    }

    @Test
    public void testRoundTripDRLNamedConsequences() throws Exception {
        DumperTestHelper.DrlFile("test_NamedConsequences.drl");
    }

    @Test
    public void testRoundTripPComplexXml() throws Exception {
        DumperTestHelper.XmlFile("test_ParseComplex.xml");
    }

    @Test
    public void testRoundTripTraitDeclarations() throws Exception {
        DumperTestHelper.DrlFile("test_TraitDeclaration.drl");
        String out = DumperTestHelper.dump("test_TraitDeclaration.drl");
        Assert.assertTrue(out.contains("declare trait Foo"));
    }

    @Test
    public void testRoundTripEnumDeclarations() throws Exception {
        DumperTestHelper.DrlFile("test_EnumDeclaration.drl");
        String out = DumperTestHelper.dump("test_EnumDeclaration.drl");
        Assert.assertTrue(out.contains("declare enum Planets"));
        Assert.assertTrue(out.contains("MERCURY"));
        Assert.assertTrue(out.contains("7.1492e7"));
    }

    @Test
    public void testRoundTripAccumulate() throws Exception {
        String out = DumperTestHelper.dump("test_Accumulate.drl");
        Assert.assertTrue(out.contains("$sum : count( $s1 )"));
        Assert.assertFalse(out.contains("null : count( $s2 )"));
        Assert.assertTrue(out.contains("count( $s2 )"));
    }

    @Test
    public void testRoundTripDRLAccumulate() throws Exception {
        // RHDM-254
        String drl = "package org.test\n" + ((((((((("\n" + "rule \"last flown date\"\n") + "when\n") + "    $customer : Profile( $ceid : id )\n") + "    accumulate(\n") + "    Flight( status == \"Flown\", $dptDate: departureDate.time ) from $customer.flights,\n") + "        $cnt : count( $dptDate );\n") + "        $cnt > 0 )\n") + "then\n") + "end");
        DrlParser parser = new DrlParser(LanguageLevelOption.DRL6);
        final PackageDescr pkgOriginal = parser.parse(false, drl);
        final DrlDumper dumper = new DrlDumper();
        String out = dumper.dump(pkgOriginal);
        Assertions.assertThat(drl).isEqualToIgnoringWhitespace(out);
    }
}

