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
package org.drools.workbench.models.guided.dtable.shared.model;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class BaseColumnFieldDiffImplTest {
    @Parameterized.Parameter(0)
    public DTCellValue52 dcv1;

    @Parameterized.Parameter(1)
    public DTCellValue52 dcv2;

    @Parameterized.Parameter(2)
    public boolean isEqual;

    @Test
    public void check() {
        Assert.assertEquals(isEqual, BaseColumnFieldDiffImpl.isEqualOrNull(dcv1, dcv2));
    }
}

