/**
 * Copyright 2005 JBoss Inc
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
package org.drools.compiler.integrationtests;


import ResourceType.DRL;
import java.util.ArrayList;
import java.util.List;
import org.drools.core.util.ClassUtils;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.rule.DataSource;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.RuleUnit;
import org.kie.api.runtime.rule.RuleUnitExecutor;
import org.kie.internal.utils.KieHelper;


public class RuleUnitCoordinationTest {
    @Test
    public void testCoordination() throws Exception {
        String drl1 = ((((((((((((((((((((((((((("package org.drools.compiler.integrationtests\n" + "unit ") + (ClassUtils.getCanonicalSimpleName(RuleUnitCoordinationTest.MasterModelUnit.class))) + ";\n") + "import ") + (RuleUnitCoordinationTest.MasterModel.class.getCanonicalName())) + "\n") + "import ") + (RuleUnitCoordinationTest.ApplicableModel.class.getCanonicalName())) + "\n") + "import ") + (RuleUnitCoordinationTest.ApplyMathModel.class.getCanonicalName())) + "\n") + "import ") + (RuleUnitCoordinationTest.ApplyStringModel.class.getCanonicalName())) + "\n") + "import ") + (RuleUnitCoordinationTest.ScheduledModelApplicationUnit.class.getCanonicalName())) + "\n") + "\n") + "rule FindModelToApply \n") + "when\n") + "   $mm: MasterModel( subModels != null ) from models\n") + "   $am: ApplicableModel( applied == false, $idx: index ) from $mm.subModels\n") + // "   not ApplicableModel( applied == false, index < $idx ) from $mm.subModels\n" +
        "then\n") + "   applicableModels.insert($am);\n") + "   drools.run(new ScheduledModelApplicationUnit(models,applicableModels));\n") + "end\n") + "";
        String drl2 = (((((((((((((((((((((((((((((((((((((((((((("package org.drools.compiler.integrationtests\n" + "unit ") + (ClassUtils.getCanonicalSimpleName(RuleUnitCoordinationTest.ScheduledModelApplicationUnit.class))) + ";\n") + "import ") + (RuleUnitCoordinationTest.MasterModel.class.getCanonicalName())) + "\n") + "import ") + (RuleUnitCoordinationTest.ApplicableModel.class.getCanonicalName())) + "\n") + "import ") + (RuleUnitCoordinationTest.ApplyMathModel.class.getCanonicalName())) + "\n") + "import ") + (RuleUnitCoordinationTest.ApplyStringModel.class.getCanonicalName())) + "\n") + "\n") + "rule Apply_ApplyMathModel_Addition \n") + "when\n") + "    $amm: ApplyMathModel( applied == false, inputValue1 != null, ") + "                          inputValue2 != null, operation == \"add\" ) from applicableModels\n") + "    $v1: Integer() from $amm.inputValue1 \n") + "    $v2: Integer() from $amm.inputValue2 \n") + "then\n") + "    modify($amm) { \n") + "       setResult($v1.intValue() + $v2.intValue()), \n") + "       setApplied(true) \n") + "    };\n") + "    System.out.println(\"Result = \"+$amm.getResult());\n") + "end\n") + "\n") + "rule Apply_ApplyStringModel_Concat \n") + "when\n") + "    $asm: ApplyStringModel( applied == false, inputString1 != null, ") + "                            inputString2 != null, operation == \"concat\" ) from applicableModels \n") + "    $v1: String() from $asm.inputString1 \n") + "    $v2: String() from $asm.inputString2 \n") + "then\n") + "    String result = $v1+\" \"+$v2; \n") + "    modify($asm) {\n") + "       setResult(result),\n") + "       setApplied(true)\n") + "    };\n") + "    System.out.println(\"Result = \"+$asm.getResult());\n") + "end\n") + "";
        RuleUnitCoordinationTest.MasterModel master = new RuleUnitCoordinationTest.MasterModel("TestMaster");
        RuleUnitCoordinationTest.ApplyMathModel mathModel = new RuleUnitCoordinationTest.ApplyMathModel(1, "Math1", "add", 10, 10);
        RuleUnitCoordinationTest.ApplyStringModel stringModel = new RuleUnitCoordinationTest.ApplyStringModel(2, "String1", "concat", "hello", "world");
        master.addSubModel(mathModel);
        master.addSubModel(stringModel);
        KieBase kbase = new KieHelper().addContent(drl1, DRL).addContent(drl2, DRL).build();
        RuleUnitExecutor executor = RuleUnitExecutor.create().bind(kbase);
        DataSource<RuleUnitCoordinationTest.MasterModel> masterModels = executor.newDataSource("models");
        DataSource<RuleUnitCoordinationTest.ApplicableModel> applicableModels = executor.newDataSource("applicableModel");
        FactHandle masterFH = masterModels.insert(master);
        RuleUnit unit = new RuleUnitCoordinationTest.MasterModelUnit(masterModels, applicableModels);
        // int x = 1;
        // while (x > 0) {
        // x = executor.run( unit );
        // System.out.println( x );
        // // I'm reinserting the master model to reinforce its evaluation, as I said in our call this is necessary because
        // // the second level froms are made on plain lists which are not normally re-evaluated by from nodes (regardless of rule units).
        // // In theory also the update should be more correct and enough to reinforce this re-evaluation, but for some reason
        // // in this case is not working. I suspect we have a bug in our NotNode related to this specific case, but I'm still
        // // evaluating it. For now the insert should be good enough to allow you to make some progress on this.
        // // masterModels.update( masterFH, master, "subModels" );
        // masterModels.insert( master );
        // }
        executor.run(unit);
        Assert.assertEquals(20, ((int) (mathModel.getResult())));
        Assert.assertEquals("hello world", stringModel.getResult());
    }

    public static class BasicModel {
        private int index;

        private String name;

        private String operation;

        public BasicModel(int index, String name, String operation) {
            this.index = index;
            this.name = name;
            this.operation = operation;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public String getOperation() {
            return operation;
        }
    }

    public static class MasterModel extends RuleUnitCoordinationTest.BasicModel {
        private List<RuleUnitCoordinationTest.ApplicableModel> subModels;

        public MasterModel(String name) {
            super(0, name, "master");
            subModels = new ArrayList<>();
        }

        public List<RuleUnitCoordinationTest.ApplicableModel> getSubModels() {
            return subModels;
        }

        public boolean addSubModel(RuleUnitCoordinationTest.ApplicableModel subModel) {
            return subModels.add(subModel);
        }
    }

    public abstract static class ApplicableModel extends RuleUnitCoordinationTest.BasicModel {
        private boolean applied;

        public ApplicableModel(int index, String name, String operation) {
            super(index, name, operation);
            this.applied = false;
        }

        public ApplicableModel(int index, String name, String operation, boolean applied) {
            super(index, name, operation);
            this.applied = applied;
        }

        public boolean isApplied() {
            return applied;
        }

        public void setApplied(boolean applied) {
            this.applied = applied;
        }

        public abstract Object getResult();
    }

    public static class ApplyMathModel extends RuleUnitCoordinationTest.ApplicableModel {
        private Integer inputValue1;

        private Integer inputValue2;

        private Integer result;

        public ApplyMathModel(int index, String name, String operation, Integer inputValue1, Integer inputValue2) {
            super(index, name, operation);
            this.inputValue1 = inputValue1;
            this.inputValue2 = inputValue2;
        }

        public Integer getInputValue1() {
            return inputValue1;
        }

        public void setInputValue1(Integer inputValue1) {
            this.inputValue1 = inputValue1;
        }

        public Integer getInputValue2() {
            return inputValue2;
        }

        public void setInputValue2(Integer inputValue2) {
            this.inputValue2 = inputValue2;
        }

        public Integer getResult() {
            return result;
        }

        public void setResult(Integer result) {
            this.result = result;
        }
    }

    public static class ApplyStringModel extends RuleUnitCoordinationTest.ApplicableModel {
        private String inputString1;

        private String inputString2;

        private String result;

        public ApplyStringModel(int index, String name, String operation, String inputString1, String inputString2) {
            super(index, name, operation);
            this.inputString1 = inputString1;
            this.inputString2 = inputString2;
        }

        public String getInputString1() {
            return inputString1;
        }

        public void setInputString1(String inputString1) {
            this.inputString1 = inputString1;
        }

        public String getInputString2() {
            return inputString2;
        }

        public void setInputString2(String inputString2) {
            this.inputString2 = inputString2;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

    public static class MasterModelUnit implements RuleUnit {
        private DataSource<RuleUnitCoordinationTest.MasterModel> models;

        private DataSource<RuleUnitCoordinationTest.ApplicableModel> applicableModels;

        public MasterModelUnit(DataSource<RuleUnitCoordinationTest.MasterModel> models, DataSource<RuleUnitCoordinationTest.ApplicableModel> applicableModels) {
            this.models = models;
            this.applicableModels = applicableModels;
        }

        public DataSource<RuleUnitCoordinationTest.MasterModel> getModels() {
            return models;
        }

        public DataSource<RuleUnitCoordinationTest.ApplicableModel> getApplicableModels() {
            return applicableModels;
        }
    }

    public static class ScheduledModelApplicationUnit implements RuleUnit {
        private DataSource<RuleUnitCoordinationTest.MasterModel> models;

        private DataSource<RuleUnitCoordinationTest.ApplicableModel> applicableModels;

        public ScheduledModelApplicationUnit(DataSource<RuleUnitCoordinationTest.MasterModel> models, DataSource<RuleUnitCoordinationTest.ApplicableModel> applicableModels) {
            this.models = models;
            this.applicableModels = applicableModels;
        }

        public DataSource<RuleUnitCoordinationTest.MasterModel> getModels() {
            return models;
        }

        public DataSource<RuleUnitCoordinationTest.ApplicableModel> getApplicableModels() {
            return applicableModels;
        }
    }
}

