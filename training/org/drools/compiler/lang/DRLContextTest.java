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
package org.drools.compiler.lang;


import DroolsEditorType.IDENTIFIER_VARIABLE;
import DroolsEditorType.KEYWORD;
import Location.LOCATION_LHS_BEGIN_OF_CONDITION;
import Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR;
import Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS;
import Location.LOCATION_LHS_BEGIN_OF_CONDITION_NOT;
import Location.LOCATION_LHS_FROM;
import Location.LOCATION_LHS_FROM_ACCUMULATE;
import Location.LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE;
import Location.LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE;
import Location.LOCATION_LHS_FROM_ACCUMULATE_RESULT_INSIDE;
import Location.LOCATION_LHS_FROM_COLLECT;
import Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT;
import Location.LOCATION_LHS_INSIDE_CONDITION_END;
import Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR;
import Location.LOCATION_LHS_INSIDE_CONDITION_START;
import Location.LOCATION_LHS_INSIDE_EVAL;
import Location.LOCATION_RHS;
import Location.LOCATION_RULE_HEADER;
import Location.LOCATION_RULE_HEADER_KEYWORD;
import java.util.LinkedList;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.drools.compiler.compiler.DroolsParserException;
import org.junit.Assert;
import org.junit.Test;


public class DRLContextTest {
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_OPERATORS_AND_COMPLEMENT1() throws RecognitionException, DroolsParserException {
        String input = "rule MyRule when Class ( property memberOf collection ";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_END, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_OPERATORS_AND_COMPLEMENT2() throws RecognitionException, DroolsParserException {
        String input = "rule MyRule when Class ( property not memberOf collection";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_COMPOSITE_OPERATOR1() throws RecognitionException, DroolsParserException {
        String input = "rule MyRule when Class ( property in ( ";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION1() throws RecognitionException, DroolsParserException {
        String input = "rule MyRule \n" + ("\twhen \n" + "		");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION2() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass( condition == true ) \n") + "		");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION3() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tclass: Class( condition == true, condition2 == null ) \n") + "		");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION4() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION5() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass( condition == true ) \n") + "		Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION6() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		class: Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION7() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		class:Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * Inside of condition: start
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START1() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class (");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    // (timeout=10*1000)
    @Test
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START2() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( na");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    // (timeout=10*1000)
    @Test
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START3() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( name.subProperty['test'].subsu");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START4() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( condition == true, ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START5() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( condition == true, na");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START6() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( \n") + "			");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START7() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( condition == true, \n") + "			");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START8() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( c: condition, \n") + "			");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        DroolsToken token = ((DroolsToken) (parser.getEditorInterface().get(0).getContent().get(11)));
        Assert.assertEquals("c", token.getText());
        Assert.assertEquals(IDENTIFIER_VARIABLE, token.getEditorType());
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START9a() {
        String input = "rule MyRule \n" + ("   when \n" + "       Class ( name:");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START9b() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( name: ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    // (timeout=10*1000)
    @Test
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START10() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( name:");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * Inside of condition: Operator
     */
    // (timeout=10*1000)
    @Test
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR1() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR2() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class(property ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR3() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( name : property ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR4() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class (name:property ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR5() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class (name:property   ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR6() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( name1 : property1, name : property ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR7() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\tClass ( name1 : property1 == \"value\", name : property ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR8() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\tClass ( name1 : property1 == \"value\",property ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR9() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( name1 : property1, \n") + "			name : property ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * Inside of condition: argument
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT1() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property == ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT2() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property== ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT3() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( name : property <= ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT4() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( name:property != ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT5() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( name1 : property1, property2 == ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT6() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class (name:property== ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT7a() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property == otherPropertyN");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT7b() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property == otherPropertyN ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_END, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT8() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\tClass ( property == \"someth");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT9a() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property contains ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT9b() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property not contains ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT10() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property excludes ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT11() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\tClass ( property matches \"prop");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT12() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property in ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END1() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property in ('1', '2') ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_END, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START11() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property in ('1', '2'), ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END2() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property not in ('1', '2') ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_END, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START12() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property not in ('1', '2'), ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT14() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property memberOf ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START13() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property memberOf collection, ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT15() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property not memberOf ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END4() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property not memberOf collection ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_END, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START14() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property not memberOf collection, ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        LinkedList list = parser.getEditorInterface().get(0).getContent();
        // for (Object o: list) {
        // System.out.println(o);
        // }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * EXISTS
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS1() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		exists ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS2() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		exists ( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS3() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		exists(");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS4() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		exists Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS5() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		exists ( Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS6() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		exists ( name : Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDeterminationINSIDE_CONDITION_START16() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		exists Class (");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\texists Class ( ) \n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * NOT
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_NOT1() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		not ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_NOT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_NOT2() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		not Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_NOT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS7() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		not ( exists ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS8() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		not ( exists Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START21() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		not Class (");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    // (timeout=10*1000)
    @Test
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START22() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		not ( exists Class (");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    // (timeout=10*1000)
    @Test
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START23() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		not ( exists name : Class (");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION9() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tnot Class () \n") + "		");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * AND
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR1() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) and ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR2() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) and  ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR3() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class () and   ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR4() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		name : Class ( name: property ) and ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
        DroolsToken token = ((DroolsToken) (parser.getEditorInterface().get(0).getContent().get(12)));
        Assert.assertEquals(IDENTIFIER_VARIABLE, token.getEditorType());
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR5() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( name: property ) \n") + "       and ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR6() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) and Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR7() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) and name : Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR8() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) and name : Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION31() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( ) and Class ( ) \n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION32() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( ) and not Class ( ) \n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION33() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( ) and exists Class ( ) \n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START20() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) and Class ( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR21() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) and Class ( name ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR22() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) and Class ( name == ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_NOT() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		exists Class ( ) and not ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_NOT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		exists Class ( ) and exists ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION30() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( ) and not Class ( ) \n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * OR
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR21() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) or ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR22() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) or ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR23() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class () or   ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR24() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		name : Class ( name: property ) or ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR25() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( name: property ) \n") + "       or ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR26() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) or Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR27() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) or name : Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR28() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) or name : Cl");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION40() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( ) or Class ( ) \n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START40() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) or Class ( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) or Class ( name ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT30() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( ) or Class ( name == ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_EGIN_OF_CONDITION_NOT() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		exists Class ( ) or not ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_NOT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS40() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		exists Class ( ) or exists ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * EVAL
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_EVAL1() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		eval ( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_EVAL, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_EVAL2() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		eval(");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_EVAL, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_EVAL3() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		eval( myCla");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_EVAL, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_EVAL4() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		eval( param.getMetho");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_EVAL, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    // (timeout=10*1000)
    @Test
    public void testCheckLHSLocationDetermination_INSIDE_EVAL5() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		eval( param.getMethod(");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_EVAL, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_EVAL6() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		eval( param.getMethod().get");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_EVAL, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_EVAL7() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\teval( param.getMethod(\"someStringWith)))\").get");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_EVAL, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_EVAL8() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\teval( param.getMethod(\"someStringWith(((\").get");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_EVAL, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_EVAL9() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		eval( true )");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION50() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		eval( param.getProperty(name).isTrue() )");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    // (timeout=10*1000)
    @Test
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION51() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\teval( param.getProperty(\"someStringWith(((\").isTrue() )");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_EVAL10() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		eval( param.getProperty((((String) s) )");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_EVAL, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION52() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		eval( param.getProperty((((String) s))))");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION53() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\teval( true ) \n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * MULTIPLE RESTRICTIONS
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR12() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 && ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR13() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( name : property1, property2 > 0 && ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR14() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property1 < 20, property2 > 0 && ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT20() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 && < ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END6() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 && < 10 ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_END, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START41() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 && < 10, ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR60() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 || ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR61() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( property > 0 && \n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR62() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( name : property1, property2 > 0 || ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR63() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property1 < 20, property2 > 0 || ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END10() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_END, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END11() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( property > 0 \n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_END, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END12() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 && < 10 ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_END, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END13() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 || < 10 ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_END, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END14() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\tClass ( property == \"test\" || == \"test2\" ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_END, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * FROM
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION60() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 ) ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION61() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 ) fr");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM1() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 ) from ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM2() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 ) from myGlob");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM3() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 ) from myGlobal.get");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION75() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( property > 0 ) from myGlobal.getList() \n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION71() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( property > 0 ) from getDroolsFunction() \n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * FROM ACCUMULATE
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE1() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 ) from accumulate ( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM_ACCUMULATE, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE2() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 ) from accumulate(");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM_ACCUMULATE, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION73() {
        String input = "rule MyRule \n" + ((((((("\twhen \n" + "\t\tClass ( property > 0 ) from accumulate( \n") + "\t\t\t$cheese : Cheese( type == $likes ), \n") + "\t\t\tinit( int total = 0; ), \n") + "\t\t\taction( total += $cheese.getPrice(); ), \n") + "           result( new Integer( total ) ) \n") + "\t\t) \n") + "		");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_INIT_INSIDE() {
        String input = "rule MyRule \n" + ((("\twhen \n" + "\t\tClass ( property > 0 ) from accumulate( \n") + "\t\t\t$cheese : Cheese( type == $likes ), \n") + "			init( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    // (timeout=10*1000)
    @Test
    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_ACTION_INSIDE() {
        String input = "rule MyRule \n" + (((("\twhen \n" + "\t\tClass ( property > 0 ) from accumulate( \n") + "\t\t\t$cheese : Cheese( type == $likes ), \n") + "\t\t\tinit( int total = 0; ), \n") + "			action( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_ACTION_INSIDE3() {
        String input = "rule MyRule \n" + (((("\twhen \n" + "\t\tClass ( property > 0 ) from accumulate( \n") + "\t\t\t$cheese : Cheese( type == $likes ), \n") + "\t\t\tinit( int total = 0; ), \n") + "			action( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    // (timeout=10*1000)
    @Test
    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_RESULT_INSIDE() {
        String input = "rule MyRule \n" + ((((("\twhen \n" + "\t\tClass ( property > 0 ) from accumulate( \n") + "\t\t\t$cheese : Cheese( type == $likes ), \n") + "\t\t\tinit( int total = 0; ), \n") + "\t\t\taction( total += $cheese.getPrice(); ), \n") + "           result( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM_ACCUMULATE_RESULT_INSIDE, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_INIT_INSIDE2() {
        String input = "rule MyRule \n" + ((("\twhen \n" + "\t\tClass ( property > 0 ) from accumulate( \n") + "\t\t\t$cheese : Cheese( type == $likes ), \n") + "			init( int total =");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_ACTION_INSIDE2() {
        String input = "rule MyRule \n" + (((("\twhen \n" + "\t\tClass ( property > 0 ) from accumulate( \n") + "\t\t\t$cheese : Cheese( type == $likes ), \n") + "\t\t\tinit( int total = 0; ), \n") + "			action( total += $ch");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_RESULT_INSIDE2() {
        String input = "rule MyRule \n" + ((((("\twhen \n" + "\t\tClass ( property > 0 ) from accumulate( \n") + "\t\t\t$cheese : Cheese( type == $likes ), \n") + "\t\t\tinit( int total = 0; ), \n") + "\t\t\taction( total += $cheese.getPrice(); ), \n") + "           result( new Integer( tot");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM_ACCUMULATE_RESULT_INSIDE, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( property > 0 ) from accumulate( \n") + "			$cheese : Cheese( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR40() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( property > 0 ) from accumulate( \n") + "			$cheese : Cheese( type ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( property > 0 ) from accumulate( \n") + "			$cheese : Cheese( type == ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * FROM COLLECT
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM_COLLECT1() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 ) from collect ( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM_COLLECT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM_COLLECT2() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		Class ( property > 0 ) from collect(");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM_COLLECT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION67() {
        String input = "rule MyRule \n" + (((("\twhen \n" + "\t\tClass ( property > 0 ) from collect ( \n") + "			Cheese( type == $likes )") + "\t\t) \n") + "		");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START31() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( property > 0 ) from collect ( \n") + "			Cheese( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR31() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( property > 0 ) from collect ( \n") + "			Cheese( type ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT21() {
        String input = "rule MyRule \n" + (("\twhen \n" + "\t\tClass ( property > 0 ) from collect ( \n") + "			Cheese( type == ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * NESTED FROM
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION68() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\tArrayList(size > 50) from collect( Person( disabled == \"yes\", income > 100000 ) ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM5() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\tArrayList(size > 50) from collect( Person( disabled == \"yes\", income > 100000 ) from ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION69() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\tArrayList(size > 50) from collect( Person( disabled == \"yes\", income > 100000 ) from town.getPersons() )");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION70() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\tArrayList(size > 50) from accumulate( Person( disabled == \"yes\", income > 100000 ) ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_FROM6() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\tArrayList(size > 50) from accumulate( Person( disabled == \"yes\", income > 100000 ) from ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_FROM, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    /**
     * FORALL
     */
    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION81() {
        String input = "rule MyRule \n" + ("\twhen \n" + "		forall ( ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START32() {
        String input = "rule MyRule \n" + (("\twhen \n" + "		forall ( ") + "           Class ( pr");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR32() {
        String input = "rule MyRule \n" + (("\twhen \n" + "		forall ( ") + "           Class ( property ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_OPERATOR, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT22() {
        String input = "rule MyRule \n" + (("\twhen \n" + "		forall ( ") + "           Class ( property == ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION76() {
        String input = "rule MyRule \n" + ((("\twhen \n" + "		forall ( ") + "           Class ( property == \"test\")") + "           C");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION77a() {
        String input = "rule MyRule \n" + ("\twhen \n" + "\t\tArrayList(size > 50) from accumulate( Person( disabled == \"yes\", income > 100000 ) from town.getPersons() ) ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION77b() {
        String input = "rule MyRule \n" + ("   when \n" + "       ArrayList(size > 50) from accumulate( Person( disabled == \"yes\", income > 100000 ) from town.getPersons() )");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START45a() {
        String input = "rule MyRule \n" + ("   when \n" + "       Class ( name :");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START45b() {
        String input = "rule MyRule \n" + ("   when \n" + "       Class ( name : ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckRHSLocationDetermination_firstLineOfLHS() {
        String input = "rule MyRule \n" + ((("\twhen\n" + "\t\tClass ( )\n") + "   then\n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_RHS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckRHSLocationDetermination_startOfNewlINE() {
        String input = "rule MyRule \n" + (((("\twhen\n" + "\t\tClass ( )\n") + "   then\n") + "       assert(null);\n") + "       ");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_RHS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckRHSLocationDetermination3() {
        String input = "rule MyRule \n" + ((("\twhen\n" + "\t\tClass ( )\n") + "   then\n") + "       meth");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_RHS, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
        Object lastElement = parser.getEditorInterface().get(0).getContent().getLast();
        Assert.assertTrue((lastElement instanceof Token));
        final Token lastToken = ((Token) (lastElement));
        Assert.assertEquals("meth", lastToken.getText());
    }

    @Test(timeout = 10 * 1000)
    public void testCheckRuleHeaderLocationDetermination() {
        String input = "rule MyRule ";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_RULE_HEADER, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckRuleHeaderLocationDetermination2() {
        String input = "rule MyRule \n" + "\tsalience 12 activation-group \"my";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        DroolsToken token = getLastTokenOnList(parser.getEditorInterface().get(0).getContent());
        Assert.assertEquals("group", token.getText().toLowerCase());
        Assert.assertEquals(KEYWORD, token.getEditorType());
        Assert.assertEquals(LOCATION_RULE_HEADER_KEYWORD, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckRuleHeaderLocationDetermination3() {
        String input = "rule \"Hello World\" ruleflow-group \"hello\" s";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_RULE_HEADER, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckRuleHeaderLocationDetermination_dialect1() {
        String input = "rule MyRule \n" + "\tdialect \"java\"";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_RULE_HEADER, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckRuleHeaderLocationDetermination_dialect2() {
        String input = "rule MyRule \n" + "\tdialect \"mvel\"";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_RULE_HEADER, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckRuleHeaderLocationDetermination_dialect3() {
        String input = "rule MyRule \n" + "	dialect ";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        DroolsToken token = getLastTokenOnList(parser.getEditorInterface().get(0).getContent());
        Assert.assertEquals("dialect", token.getText().toLowerCase());
        Assert.assertEquals(KEYWORD, token.getEditorType());
        Assert.assertEquals(LOCATION_RULE_HEADER_KEYWORD, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckRuleHeaderLocationDetermination_dialect4() {
        String input = "rule MyRule \n" + "\tdialect \"";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        DroolsToken token = getLastTokenOnList(parser.getEditorInterface().get(0).getContent());
        Assert.assertEquals("dialect", token.getText().toLowerCase());
        Assert.assertEquals(KEYWORD, token.getEditorType());
        Assert.assertEquals(LOCATION_RULE_HEADER_KEYWORD, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    // TODO: add tests for dialect defined at package header level
    @Test(timeout = 10 * 1000)
    public void testCheckQueryLocationDetermination_RULE_HEADER1() {
        String input = "query MyQuery ";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_RULE_HEADER, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckQueryLocationDetermination_RULE_HEADER2() {
        String input = "query \"MyQuery\" ";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_RULE_HEADER, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckQueryLocationDetermination_LHS_BEGIN_OF_CONDITION() {
        String input = "query MyQuery() ";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_BEGIN_OF_CONDITION, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    @Test(timeout = 10 * 1000)
    public void testCheckQueryLocationDetermination_LHS_INSIDE_CONDITION_START() {
        String input = "query MyQuery \n" + "	Class (";
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }

    // (timeout=10*1000)
    @Test
    public void testRuleParameters_PATTERN_1() {
        String input = "rule MyRule \n" + ("    when \n" + "        c: Class (");
        DRLParser parser = getParser(input);
        parser.enableEditorInterface();
        try {
            parser.compilationUnit();
        } catch (Exception ex) {
        }
        Assert.assertEquals(LOCATION_LHS_INSIDE_CONDITION_START, getLastIntegerValue(parser.getEditorInterface().get(0).getContent()));
    }
}

