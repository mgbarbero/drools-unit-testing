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
package org.drools.scorecards;


import java.util.List;
import org.drools.compiler.compiler.ScoreCardProvider;
import org.drools.core.impl.InternalKnowledgeBase;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.pmml.PMML4Data;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.runtime.rule.DataSource;
import org.kie.api.runtime.rule.RuleUnit;
import org.kie.api.runtime.rule.RuleUnitExecutor;
import org.kie.internal.utils.KieHelper;


public class ScorecardProviderPMMLTest {
    private static String drl;

    private ScoreCardProvider scoreCardProvider;

    @Test
    public void testKnowledgeBaseWithExecution() {
        KieBase kbase = new KieHelper().addFromClassPath("/SimpleScorecard.pmml").build();
        RuleUnitExecutor executor = RuleUnitExecutor.create().bind(kbase);
        Assert.assertNotNull(executor);
        DataSource<PMMLRequestData> data = executor.newDataSource("request");
        DataSource<PMML4Result> resultData = executor.newDataSource("results");
        DataSource<PMML4Data> pmmlData = executor.newDataSource("pmmlData");
        PMMLRequestData request = new PMMLRequestData("123", "SampleScore");
        request.addRequestParam("age", 33.0);
        request.addRequestParam("occupation", "PROGRAMMER");
        request.addRequestParam("residenceState", "KN");
        request.addRequestParam("validLicense", true);
        data.insert(request);
        PMML4Result resultHolder = new PMML4Result("123");
        resultData.insert(resultHolder);
        List<String> possiblePackages = TestUtil.calculatePossiblePackageNames("Sample Score", "org.drools.scorecards.example");
        Class<? extends RuleUnit> ruleUnitClass = TestUtil.getStartingRuleUnit("RuleUnitIndicator", ((InternalKnowledgeBase) (kbase)), possiblePackages);
        int executions = executor.run(ruleUnitClass);
        Assert.assertTrue((executions > 0));
        Double calculatedScore = resultHolder.getResultValue("Scorecard_calculatedScore", "value", Double.class).orElse(null);
        Assert.assertEquals(56.0, calculatedScore, 1.0E-6);
    }
}

