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
package org.drools.modelcompiler;


import Message.Level.ERROR;
import org.drools.modelcompiler.domain.Person;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.builder.Results;


public class CompilationFailuresTest extends BaseModelTest {
    public CompilationFailuresTest(BaseModelTest.RUN_TYPE testRunType) {
        super(testRunType);
    }

    @Test
    public void testNonQuotedStringComapre() {
        String drl = "declare Fact\n" + ((((("    field : String\n" + "end\n") + "rule R when\n") + "    Fact( field == someString )\n") + "then\n") + "end\n");
        Results results = getCompilationResults(drl);
        Assert.assertFalse(results.getMessages(ERROR).isEmpty());
    }

    @Test
    public void testUseNotExistingEnum() {
        String drl = ((((("import " + (CompilationFailuresTest.NumberRestriction.class.getCanonicalName())) + "\n") + "rule R when\n") + "    NumberRestriction( valueType == Field.INT || == Field.DOUBLE )\n") + "then\n") + "end\n";
        Results results = getCompilationResults(drl);
        Assert.assertFalse(results.getMessages(ERROR).isEmpty());
    }

    public static class NumberRestriction {
        private Number value;

        public void setValue(Number number) {
            this.value = number;
        }

        public boolean isInt() {
            return (value) instanceof Integer;
        }

        public Number getValue() {
            return value;
        }

        public String getValueAsString() {
            return value.toString();
        }

        public String getValueType() {
            return value.getClass().getName();
        }
    }

    @Test
    public void testBadQueryArg() {
        String drl = (((("import " + (Person.class.getCanonicalName())) + "\n") + "query queryWithParamWithoutType( tname , tage)\n") + "    person : Person(name == tname, age < tage )\n") + "end\n";
        Results results = getCompilationResults(drl);
        Assert.assertFalse(results.getMessages(ERROR).isEmpty());
    }
}

