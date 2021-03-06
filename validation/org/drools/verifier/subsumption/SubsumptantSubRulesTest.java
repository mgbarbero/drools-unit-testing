/**
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.verifier.subsumption;


import ResourceType.DRL;
import VerifierComponentType.SUB_RULE;
import java.util.Collection;
import org.drools.verifier.Verifier;
import org.drools.verifier.builder.VerifierBuilder;
import org.drools.verifier.builder.VerifierBuilderFactory;
import org.drools.verifier.report.components.Subsumption;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.internal.io.ResourceFactory;


// /**
// * Empty pattern
// * @throws Exception
// */
// public void testSubpatternSubsumption2() throws Exception {
// 
// VerifierBuilder vBuilder = VerifierBuilderFactory.newVerifierBuilder();
// 
// Verifier verifier = vBuilder.newVerifier();
// 
// verifier.addResourcesToVerify( ResourceFactory.newClassPathResource( "SubsumptantSubPatterns2.drl",
// getClass() ),
// ResourceType.DRL );
// 
// for ( VerifierError error : verifier.getMissingClasses() ) {
// System.out.println( error.getMessage() );
// }
// 
// assertFalse( verifier.hasErrors() );
// 
// boolean noProblems = verifier.fireAnalysis();
// assertTrue( noProblems );
// 
// Collection<Object> subsumptionList = ((VerifierImpl) verifier).getKnowledgeSession().getObjects( new ClassObjectFilter( Subsumption.class ) );
// 
// int count = 0;
// for ( Object object : subsumptionList ) {
// //            System.out.println( " * " + ((Subsumption) object) );
// if ( ((Subsumption) object).getLeft().getCauseType().equals( CauseType.SUB_PATTERN ) ) {
// count++;
// }
// }
// assertEquals( 1,
// count );
// 
// verifier.dispose();
// }
// 
// /**
// * Different sources
// * @throws Exception
// */
// public void testSubpatternSubsumption3() throws Exception {
// 
// VerifierBuilder vBuilder = VerifierBuilderFactory.newVerifierBuilder();
// 
// Verifier verifier = vBuilder.newVerifier();
// 
// verifier.addResourcesToVerify( ResourceFactory.newClassPathResource( "SubsumptantSubPatterns3.drl",
// getClass() ),
// ResourceType.DRL );
// 
// for ( VerifierError error : verifier.getMissingClasses() ) {
// System.out.println( error.getMessage() );
// }
// 
// assertFalse( verifier.hasErrors() );
// 
// boolean noProblems = verifier.fireAnalysis();
// assertTrue( noProblems );
// 
// Collection<Object> subsumptionList = ((VerifierImpl) verifier).getKnowledgeSession().getObjects( new ClassObjectFilter( Subsumption.class ) );
// 
// int count = 0;
// for ( Object object : subsumptionList ) {
// //            System.out.println( " * " + ((Subsumption) object) );
// if ( ((Subsumption) object).getLeft().getCauseType().equals( CauseType.SUB_PATTERN ) ) {
// count++;
// }
// }
// assertEquals( 1,
// count );
// 
// verifier.dispose();
// }
// 
// /**
// * Patterns that use from
// * @throws Exception
// */
// @Test @Ignore
// public void testSubpatternSubsumption4() throws Exception {
// 
// VerifierBuilder vBuilder = VerifierBuilderFactory.newVerifierBuilder();
// 
// Verifier verifier = vBuilder.newVerifier();
// 
// verifier.addResourcesToVerify( ResourceFactory.newClassPathResource( "SubsumptantSubPatterns4.drl",
// getClass() ),
// ResourceType.DRL );
// 
// for ( VerifierError error : verifier.getMissingClasses() ) {
// System.out.println( error.getMessage() );
// }
// 
// assertFalse( verifier.hasErrors() );
// 
// boolean noProblems = verifier.fireAnalysis();
// assertTrue( noProblems );
// 
// Collection<Object> subsumptionList = ((VerifierImpl) verifier).getKnowledgeSession().getObjects( new ClassObjectFilter( Subsumption.class ) );
// 
// int count = 0;
// for ( Object object : subsumptionList ) {
// System.out.println( " * " + ((Subsumption) object) );
// if ( ((Subsumption) object).getLeft().getCauseType().equals( CauseType.SUB_PATTERN ) ) {
// count++;
// }
// }
// assertEquals( 1,
// count );
// 
// verifier.dispose();
// }
public class SubsumptantSubRulesTest {
    @Test
    public void testSubruleSubsumption1() throws Exception {
        VerifierBuilder vBuilder = VerifierBuilderFactory.newVerifierBuilder();
        Verifier verifier = vBuilder.newVerifier();
        verifier.addResourcesToVerify(ResourceFactory.newClassPathResource("SubsumptantSubRules1.drl", getClass()), DRL);
        // for ( VerifierError error : verifier.getMissingClasses() ) {
        // System.out.println( error.getMessage() );
        // }
        Assert.assertFalse(verifier.hasErrors());
        boolean noProblems = verifier.fireAnalysis();
        Assert.assertTrue(noProblems);
        Collection<? extends Object> subsumptionList = getKnowledgeSession().getObjects(new ClassObjectFilter(Subsumption.class));
        int count = 0;
        for (Object object : subsumptionList) {
            // System.out.println( " * " + ((Subsumption) object) );
            if (getVerifierComponentType().equals(SUB_RULE)) {
                // System.out.println( " ** " + ((SubRule) ((Subsumption) object).getLeft()).getItems() + " - " + ((SubRule) ((Subsumption) object).getRight()).getItems() );
                count++;
            }
        }
        Assert.assertEquals(2, count);
        verifier.dispose();
    }
}

