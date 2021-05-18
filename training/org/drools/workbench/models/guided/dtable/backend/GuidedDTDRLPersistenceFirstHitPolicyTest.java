/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.workbench.models.guided.dtable.backend;


import org.drools.workbench.models.guided.dtable.shared.model.AttributeCol52;
import org.drools.workbench.models.guided.dtable.shared.model.DTCellValue52;
import org.drools.workbench.models.guided.dtable.shared.model.GuidedDecisionTable52;
import org.junit.Test;


public class GuidedDTDRLPersistenceFirstHitPolicyTest {
    private GuidedDecisionTable52 dtable;

    @Test(expected = IllegalArgumentException.class)
    public void blockUseOfSalience() {
        final AttributeCol52 attributeCol52 = new AttributeCol52();
        attributeCol52.setAttribute("salience");
        attributeCol52.setDefaultValue(new DTCellValue52("123"));
        dtable.getAttributeCols().add(attributeCol52);
        GuidedDTDRLPersistence.getInstance().marshal(dtable);
    }

    @Test(expected = IllegalArgumentException.class)
    public void blockUseOfActivationGroup() {
        final AttributeCol52 attributeCol52 = new AttributeCol52();
        attributeCol52.setAttribute("activation-group");
        attributeCol52.setDefaultValue(new DTCellValue52("test"));
        dtable.getAttributeCols().add(attributeCol52);
        GuidedDTDRLPersistence.getInstance().marshal(dtable);
    }

    @Test
    public void allRulesHaveTheSameActivationGroup() throws Exception {
        final String drl = GuidedDTDRLPersistence.getInstance().marshal(dtable);
        TestUtil.assertContainsLinesInOrder(drl, "rule \"Row 1 First hit policy table\"", "activation-group \"first-hit-policy-group First hit policy table\"", "rule \"Row 2 First hit policy table\"", "activation-group \"first-hit-policy-group First hit policy table\"", "rule \"Row 3 First hit policy table\"", "activation-group \"first-hit-policy-group First hit policy table\"");
    }
}

