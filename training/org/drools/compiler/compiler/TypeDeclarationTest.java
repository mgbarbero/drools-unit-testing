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
package org.drools.compiler.compiler;


import KieServices.Factory;
import Message.Level.ERROR;
import ResourceType.DRL;
import ResultSeverity.WARNING;
import Role.Type.EVENT;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.drools.core.common.EventFactHandle;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.rule.TypeDeclaration;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.type.Annotation;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.builder.KnowledgeBuilderResults;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;


public class TypeDeclarationTest {
    @Test
    public void testClassNameClashing() {
        String str = "";
        str += "package org.kie \n" + (("declare org.kie.Character \n" + "    name : String \n") + "end \n");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), DRL);
        if (kbuilder.hasErrors()) {
            Assert.fail(kbuilder.getErrors().toString());
        }
    }

    @Test
    public void testAnnotationReDefinition() {
        String str1 = "";
        str1 += "package org.kie \n" + ((("declare org.kie.EventA \n" + "    name : String \n") + "    duration : Long \n") + "end \n");
        String str2 = "";
        str2 += "package org.kie \n" + ((("declare org.kie.EventA \n" + "    @role (event) \n") + "    @duration (duration) \n") + "end \n");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        kbuilder.add(ResourceFactory.newByteArrayResource(str2.getBytes()), DRL);
        if (kbuilder.hasErrors()) {
            Assert.fail(kbuilder.getErrors().toString());
        }
        // No Warnings
        KnowledgeBuilderResults warnings = kbuilder.getResults(WARNING);
        Assert.assertEquals(0, warnings.size());
        // just 1 package was created
        Assert.assertEquals(1, kbuilder.getKnowledgePackages().size());
        // Get the Fact Type for org.kie.EventA
        FactType factType = getFactType("org.kie.EventA");
        Assert.assertNotNull(factType);
        // 'name' field must still be there
        FactField field = factType.getField("name");
        Assert.assertNotNull(field);
        // 'duration' field must still be there
        field = factType.getField("duration");
        Assert.assertNotNull(field);
        // New Annotations must be there too
        TypeDeclaration typeDeclaration = getTypeDeclaration("EventA");
        Assert.assertEquals(EVENT, typeDeclaration.getRole());
        Assert.assertEquals("duration", typeDeclaration.getDurationAttribute());
    }

    @Test
    public void testNoAnnotationUpdateIfError() {
        String str1 = "";
        str1 += "package org.drools.compiler \n" + ((("declare org.drools.EventA \n" + "    name : String \n") + "    duration : Long \n") + "end \n");
        String str2 = "";
        str2 += "package org.drools.compiler \n" + (((("declare org.drools.EventA \n" + "    @role (event) \n") + "    @duration (duration) \n") + "    anotherField : String \n") + "end \n");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        kbuilder.add(ResourceFactory.newByteArrayResource(str2.getBytes()), DRL);
        if (!(kbuilder.hasErrors())) {
            Assert.fail("Errors Expected");
        }
        // No Warnings
        KnowledgeBuilderResults warnings = kbuilder.getResults(WARNING);
        Assert.assertEquals(0, warnings.size());
        // just 1 package was created
        Assert.assertEquals(0, kbuilder.getKnowledgePackages().size());
    }

    /**
     * The same resource (containing a type declaration) is added twice in the
     * kbuilder.
     */
    @Test
    public void testDuplicatedTypeDeclarationWith2FieldsInSameResource() {
        // same package, different resources
        String str1 = "";
        str1 += "package org.drools.compiler \n" + ((("declare org.drools.ClassA \n" + "    name : String \n") + "    lastName : String \n") + "end \n");
        Resource resource = ResourceFactory.newByteArrayResource(str1.getBytes());
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(resource, DRL);
        kbuilder.add(resource, DRL);
        if (kbuilder.hasErrors()) {
            Assert.fail(kbuilder.getErrors().toString());
        }
    }

    /**
     * 2 resources (containing a the same type declaration) are added to the
     * kbuilder.
     * The expectation here is to silently discard the second type declaration.
     */
    @Test
    public void testDuplicatedTypeDeclarationInDifferentResources() {
        // same package, different resources
        String str1 = "";
        str1 += "package org.drools.compiler \n" + (("declare org.drools.ClassA \n" + "    name : String \n") + "end \n");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        if (kbuilder.hasErrors()) {
            Assert.fail(kbuilder.getErrors().toString());
        }
    }

    /**
     * 2 resources (containing different declarations of the same type ) are added
     * to the kbuilder.
     * The expectation here is that compilation fails because we are changing
     * the type of a field
     */
    @Test
    public void testClashingTypeDeclarationInDifferentResources() {
        // same package, different resources
        String str1 = "";
        str1 += "package org.drools.compiler \n" + ((("declare org.drools.ClassA \n" + "    name : String \n") + "    age : Integer \n") + "end \n");
        String str2 = "";
        str2 += "package org.drools.compiler \n" + ((("declare org.drools.ClassA \n" + "    name : String \n") + "    age : String \n") + "end \n");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        kbuilder.add(ResourceFactory.newByteArrayResource(str2.getBytes()), DRL);
        if (!(kbuilder.hasErrors())) {
            Assert.fail("An error should have been generated, redefinition of ClassA is not allowed");
        }
    }

    /**
     * 2 resources (containing different declarations of the same type ) are added
     * to the kbuilder.
     * The expectation here is to silently discard the second type declaration.
     * This is because the new definition has less fields that the original
     * UPDATE : any use of the full-arg constructor in the second DRL will fail,
     * so we generate an error anyway
     */
    @Test
    public void testNotSoHarmlessTypeReDeclaration() {
        // same package, different resources
        String str1 = "";
        str1 += "package org.drools.compiler \n" + ((("declare org.drools.ClassA \n" + "    name : String \n") + "    age : Integer \n") + "end \n");
        String str2 = "";
        str2 += "package org.drools.compiler \n" + (("declare org.drools.ClassA \n" + "    name : String \n") + "end \n");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        kbuilder.add(ResourceFactory.newByteArrayResource(str2.getBytes()), DRL);
        if (!(kbuilder.hasErrors())) {
            Assert.fail("An error should have been generated, redefinition of ClassA is not allowed");
        }
        /* //1 Warning
        KnowledgeBuilderResults warnings = kbuilder.getResults( ResultSeverity.WARNING );
        Assert.assertEquals(1, warnings.size());
        System.out.println(warnings.iterator().next().getMessage());

        //just 1 package was created
        Assert.assertEquals(1, kbuilder.getKnowledgePackages().size());

        //Get the Fact Type for org.drools.ClassA
        FactType factType = ((KnowledgePackageImp)kbuilder.getKnowledgePackages().iterator().next()).pkg.getFactType("org.drools.ClassA");
        Assert.assertNotNull(factType);

        //'age' field must still be there
        FactField field = factType.getField("age");
        Assert.assertNotNull(field);

        //Assert that the 'name' field must be String and not Long
        Assert.assertEquals(Integer.class, field.getType());
         */
    }

    /**
     * 2 resources (containing different declarations of the same type ) are added
     * to the kbuilder.
     * The expectation here is that the compilation fails because we are
     * adding a new field to the declared Type
     */
    @Test
    public void testTypeReDeclarationWithExtraField() {
        // same package, different resources
        String str1 = "";
        str1 += "package org.drools.compiler \n" + ((("declare org.drools.ClassA \n" + "    name : String \n") + "    age : Integer \n") + "end \n");
        String str2 = "";
        str2 += "package org.drools.compiler \n" + ((("declare org.drools.ClassA \n" + "    name : String \n") + "    lastName : String \n") + "end \n");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        kbuilder.add(ResourceFactory.newByteArrayResource(str2.getBytes()), DRL);
        if (!(kbuilder.hasErrors())) {
            Assert.fail(kbuilder.getErrors().toString());
        }
    }

    /**
     * 2 resources (containing different declarations of the same type ) are added
     * to the kbuilder.
     * The expectation here is that the compilation fails because we are
     * trying to add an incompatible re-definition of the declared type:
     * it introduces a new field 'lastName'
     */
    @Test
    public void testTypeReDeclarationWithExtraField2() {
        // same package, different resources
        String str1 = "";
        str1 += "package org.drools.compiler \n" + ((("declare org.drools.ClassA \n" + "    name : String \n") + "    age : Integer \n") + "end \n");
        String str2 = "";
        str2 += "package org.drools.compiler \n" + ((("declare org.drools.ClassA \n" + "    name : String \n") + "    lastName : String \n") + "end \n");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        kbuilder.add(ResourceFactory.newByteArrayResource(str2.getBytes()), DRL);
        if (!(kbuilder.hasErrors())) {
            Assert.fail(kbuilder.getErrors().toString());
        }
    }

    @Test
    public void testDuplicateDeclaration() {
        String str = "";
        str += "package org.drools.compiler \n" + ((((("declare Bean \n" + "    name : String \n") + "end \n") + "declare Bean \n") + "    age : int \n") + "end \n");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), DRL);
        if (!(kbuilder.hasErrors())) {
            Assert.fail("Two definitions with the same name are not allowed, but it was not detected! ");
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface KlassAnnotation {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface FieldAnnotation {
        String prop();
    }

    @Test
    public void testTypeDeclarationMetadata() {
        String str = "";
        str += "package org.drools.compiler.test; \n" + ((((((((((("import org.drools.compiler.compiler.TypeDeclarationTest.KlassAnnotation; \n" + "import org.drools.compiler.compiler.TypeDeclarationTest.FieldAnnotation; \n") + "import org.drools.compiler.Person\n") + "\n") + "declare Bean \n") + "@role(event) \n") + "@expires( 1s ) \n") + "@KlassAnnotation( \"klass\" )") + "") + "    name : String @key @FieldAnnotation( prop = \"fld\" )\n") + "end \n") + "declare Person @role(event) end");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), DRL);
        System.err.println(kbuilder.getErrors());
        Assert.assertFalse(kbuilder.hasErrors());
        InternalKnowledgeBase kBase = KnowledgeBaseFactory.newKnowledgeBase();
        kBase.addPackages(kbuilder.getKnowledgePackages());
        FactType bean = kBase.getFactType("org.drools.compiler.test", "Bean");
        FactType pers = kBase.getFactType("org.drools", "Person");
        Assert.assertEquals("org.drools.compiler.test.Bean", bean.getName());
        Assert.assertEquals("Bean", bean.getSimpleName());
        Assert.assertEquals("org.drools.compiler.test", bean.getPackageName());
        Assert.assertEquals(3, bean.getClassAnnotations().size());
        Annotation ann = bean.getClassAnnotations().get(0);
        if (!(ann.getName().equals("org.drools.compiler.compiler.TypeDeclarationTest$KlassAnnotation"))) {
            ann = bean.getClassAnnotations().get(1);
        }
        if (!(ann.getName().equals("org.drools.compiler.compiler.TypeDeclarationTest$KlassAnnotation"))) {
            ann = bean.getClassAnnotations().get(2);
        }
        Assert.assertEquals("org.drools.compiler.compiler.TypeDeclarationTest$KlassAnnotation", ann.getName());
        Assert.assertEquals("klass", ann.getPropertyValue("value"));
        Assert.assertEquals(String.class, ann.getPropertyType("value"));
        Assert.assertEquals(2, bean.getMetaData().size());
        Assert.assertEquals("event", bean.getMetaData().get("role"));
        FactField field = bean.getField("name");
        Assert.assertNotNull(field);
        Assert.assertEquals(2, field.getFieldAnnotations().size());
        Annotation fnn = field.getFieldAnnotations().get(0);
        if (!(fnn.getName().equals("org.drools.compiler.compiler.TypeDeclarationTest$FieldAnnotation"))) {
            fnn = field.getFieldAnnotations().get(1);
        }
        Assert.assertEquals("org.drools.compiler.compiler.TypeDeclarationTest$FieldAnnotation", fnn.getName());
        Assert.assertEquals("fld", fnn.getPropertyValue("prop"));
        Assert.assertEquals(String.class, fnn.getPropertyType("prop"));
        Assert.assertEquals(1, field.getMetaData().size());
        Assert.assertTrue(field.getMetaData().containsKey("key"));
    }

    public static class EventBar {
        public static class Foo {}
    }

    @Test
    public void testTypeDeclarationWithInnerClasses() {
        // DROOLS-150
        String str = "";
        str += "package org.drools.compiler;\n" + ((((((("\n" + "import org.drools.compiler.compiler.TypeDeclarationTest.EventBar.*;\n") + "") + "declare Foo\n") + " @role( event )\n") + "end\n") + "") + "rule R when Foo() then end");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), DRL);
        System.err.println(kbuilder.getErrors());
        Assert.assertFalse(kbuilder.hasErrors());
        InternalKnowledgeBase kBase = KnowledgeBaseFactory.newKnowledgeBase();
        kBase.addPackages(kbuilder.getKnowledgePackages());
        KieSession knowledgeSession = kBase.newKieSession();
        FactHandle handle = knowledgeSession.insert(new TypeDeclarationTest.EventBar.Foo());
        Assert.assertTrue((handle instanceof EventFactHandle));
    }

    @Test
    public void testTypeDeclarationWithInnerClassesImport() {
        // DROOLS-150
        String str = "";
        str += "package org.drools.compiler;\n" + ((((((("\n" + "import org.drools.compiler.compiler.TypeDeclarationTest.EventBar.Foo;\n") + "") + "declare Foo\n") + " @role( event )\n") + "end\n") + "") + "rule R when Foo() then end");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), DRL);
        System.err.println(kbuilder.getErrors());
        Assert.assertFalse(kbuilder.hasErrors());
        InternalKnowledgeBase kBase = KnowledgeBaseFactory.newKnowledgeBase();
        kBase.addPackages(kbuilder.getKnowledgePackages());
        KieSession knowledgeSession = kBase.newKieSession();
        FactHandle handle = knowledgeSession.insert(new TypeDeclarationTest.EventBar.Foo());
        Assert.assertTrue((handle instanceof EventFactHandle));
    }

    static class ClassC {
        private String name;

        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

    @Test
    public void testTypeReDeclarationPojo() {
        String str1 = ((((((((("" + ("package org.drools \n" + "import ")) + (TypeDeclarationTest.class.getName())) + ".ClassC; \n") + "") + "declare ") + (TypeDeclarationTest.class.getName())) + ".ClassC \n") + "    name : String \n") + "    age : Integer \n") + "end \n";
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        if (kbuilder.hasErrors()) {
            Assert.fail(kbuilder.getErrors().toString());
        }
    }

    @Test
    public void testTypeReDeclarationPojoMoreFields() {
        String str1 = (((((((((("" + ("package org.drools \n" + "import ")) + (TypeDeclarationTest.class.getName())) + ".ClassC; \n") + "") + "declare ") + (TypeDeclarationTest.class.getName())) + ".ClassC \n") + "    name : String \n") + "    age : Integer \n") + "    address : Objet \n") + "end \n";
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        if (!(kbuilder.hasErrors())) {
            Assert.fail(kbuilder.getErrors().toString());
        }
    }

    @Test
    public void testTypeReDeclarationPojoLessFields() {
        String str1 = (((((((("" + ("package org.drools \n" + "import ")) + (TypeDeclarationTest.class.getName())) + ".ClassC; \n") + "") + "declare ") + (TypeDeclarationTest.class.getName())) + ".ClassC \n") + "    name : String \n") + "end \n";
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        if (!(kbuilder.hasErrors())) {
            Assert.fail(kbuilder.getErrors().toString());
        }
    }

    @Test
    public void testMultipleTypeReDeclaration() {
        // same package, different resources
        String str1 = "";
        str1 += "package org.drools \n" + ((("declare org.drools.ClassC \n" + "    name : String \n") + "    age : Integer \n") + "end \n");
        String str2 = "";
        str2 += "package org.drools \n" + ((("declare org.drools.ClassC \n" + "    name : String \n") + "    age : Integer \n") + "end \n");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        kbuilder.add(ResourceFactory.newByteArrayResource(str2.getBytes()), DRL);
        if (kbuilder.hasErrors()) {
            Assert.fail(kbuilder.getErrors().toString());
        }
    }

    @Test
    public void testDeclaresInForeignPackages() {
        String str1 = "" + ((("package org.drools \n" + "declare foreign.ClassC fld : foreign.ClassD end ") + "declare foreign.ClassD end ") + "");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        if (kbuilder.hasErrors()) {
            Assert.fail(kbuilder.getErrors().toString());
        }
    }

    @Test
    public void testDeclareFieldArray() {
        String str1 = "" + (((((((((((("package org.drools " + "declare Test end ") + "declare Pet ") + "    owners : Owner[] ") + "    twoDimArray : Foo[][] ") + "    friends : Pet[] ") + "    ages : int[] ") + "end ") + "declare Owner ") + "     name : String ") + "end ") + "declare Foo end ") + "");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str1.getBytes()), DRL);
        if (kbuilder.hasErrors()) {
            Assert.fail(kbuilder.getErrors().toString());
        }
        for (KiePackage kp : kbuilder.getKnowledgePackages()) {
            if (kp.getName().equals("org.drools")) {
                Collection<FactType> types = kp.getFactTypes();
                for (FactType type : types) {
                    if ("org.drools.Pet".equals(type.getName())) {
                        Assert.assertEquals(4, type.getFields().size());
                        FactField owners = type.getField("owners");
                        Assert.assertTrue((((owners != null) && (owners.getType().getSimpleName().equals("Owner[]"))) && (owners.getType().isArray())));
                        FactField twoDim = type.getField("twoDimArray");
                        Assert.assertTrue((((twoDim != null) && (twoDim.getType().getSimpleName().equals("Foo[][]"))) && (twoDim.getType().isArray())));
                        FactField friends = type.getField("friends");
                        Assert.assertTrue((((friends != null) && (friends.getType().getSimpleName().equals("Pet[]"))) && (friends.getType().isArray())));
                        FactField ages = type.getField("ages");
                        Assert.assertTrue((((ages != null) && (ages.getType().getSimpleName().equals("int[]"))) && (ages.getType().isArray())));
                    }
                }
            }
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPreventReflectionAPIsOnJavaClasses() {
        String drl = "package org.test; " + // existing java class
        ((("declare org.drools.compiler.Person " + "  @role(event) ") + "end \n") + "");
        KieBuilder kieBuilder = build(drl);
        Assert.assertFalse(kieBuilder.getResults().hasMessages(ERROR));
        KieBase kieBase = Factory.get().newKieContainer(kieBuilder.getKieModule().getReleaseId()).getKieBase();
        FactType type = kieBase.getFactType("org.drools.compiler", "Person");
    }

    @Test
    public void testCrossPackageDeclares() {
        String pkg1 = "package org.drools.compiler.test1; " + ((((((((("import org.drools.compiler.test2.GrandChild; " + "import org.drools.compiler.test2.Child; ") + "import org.drools.compiler.test2.BarFuu; ") + "declare FuBaz foo : String end ") + "declare Parent ") + "   unknown : BarFuu ") + "end ") + "declare GreatChild extends GrandChild ") + "   father : Child ") + "end ");
        String pkg2 = "package org.drools.compiler.test2; " + ((((((((("import org.drools.compiler.test1.Parent; " + "import org.drools.compiler.test1.FuBaz; ") + "declare BarFuu ") + "   baz : FuBaz ") + "end ") + "declare Child extends Parent ") + "end ") + "declare GrandChild extends Child ") + "   notknown : FuBaz ") + "end ");
        KieServices ks = Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.generateAndWritePomXML(ks.newReleaseId("test", "foo", "1.0"));
        KieModuleModel km = ks.newKieModuleModel();
        km.newKieBaseModel("rules").addPackage("org.drools.compiler.test2").addPackage("org.drools.compiler.test1");
        kfs.writeKModuleXML(km.toXML());
        KieResources kr = ks.getResources();
        Resource r1 = kr.newByteArrayResource(pkg1.getBytes()).setResourceType(DRL).setSourcePath("org/drools/compiler/test1/p1.drl");
        Resource r2 = kr.newByteArrayResource(pkg2.getBytes()).setResourceType(DRL).setSourcePath("org/drools/compiler/test2/p2.drl");
        kfs.write(r1);
        kfs.write(r2);
        KieBuilder builder = ks.newKieBuilder(kfs);
        builder.buildAll();
        Assert.assertEquals(Collections.emptyList(), builder.getResults().getMessages(ERROR));
        KieContainer kc = ks.newKieContainer(builder.getKieModule().getReleaseId());
        FactType ft = kc.getKieBase("rules").getFactType("org.drools.compiler.test2", "Child");
        Assert.assertNotNull(ft);
        Assert.assertNotNull(ft.getFactClass());
        Assert.assertEquals("org.drools.compiler.test1.Parent", ft.getFactClass().getSuperclass().getName());
    }

    @Test
    public void testUnknownField() throws IllegalAccessException, InstantiationException {
        // DROOLS-546
        String drl = "package org.test; " + (("declare Pet" + " ") + "end \n");
        KieBuilder kieBuilder = build(drl);
        Assert.assertFalse(kieBuilder.getResults().hasMessages(ERROR));
        KieBase kieBase = Factory.get().newKieContainer(kieBuilder.getKieModule().getReleaseId()).getKieBase();
        FactType factType = kieBase.getFactType("org.test", "Pet");
        Object instance = factType.newInstance();
        factType.get(instance, "unknownField");
        factType.set(instance, "unknownField", "myValue");
    }

    @Test
    public void testPositionalArguments() throws IllegalAccessException, InstantiationException {
        String drl = "package org.test;\n" + ((((((((("global java.util.List names;\n" + "declare Person\n") + "    name : String\n") + "    age : int\n") + "end\n") + "rule R when \n") + "    $p : Person( \"Mark\", 37; )\n") + "then\n") + "    names.add( $p.getName() );\n") + "end\n");
        KieBuilder kieBuilder = build(drl);
        Assert.assertFalse(kieBuilder.getResults().hasMessages(ERROR));
        KieBase kieBase = Factory.get().newKieContainer(kieBuilder.getKieModule().getReleaseId()).getKieBase();
        FactType factType = kieBase.getFactType("org.test", "Person");
        Object instance = factType.newInstance();
        factType.set(instance, "name", "Mark");
        factType.set(instance, "age", 37);
        List<String> names = new ArrayList<String>();
        KieSession ksession = kieBase.newKieSession();
        ksession.setGlobal("names", names);
        ksession.insert(instance);
        ksession.fireAllRules();
        Assert.assertEquals(1, names.size());
        Assert.assertEquals("Mark", names.get(0));
    }

    @Test
    public void testExplictPositionalArguments() throws IllegalAccessException, InstantiationException {
        String drl = "package org.test;\n" + ((((((((("global java.util.List names;\n" + "declare Person\n") + "    name : String @position(1)\n") + "    age : int @position(0)\n") + "end\n") + "rule R when \n") + "    $p : Person( 37, \"Mark\"; )\n") + "then\n") + "    names.add( $p.getName() );\n") + "end\n");
        KieBuilder kieBuilder = build(drl);
        Assert.assertFalse(kieBuilder.getResults().hasMessages(ERROR));
        KieBase kieBase = Factory.get().newKieContainer(kieBuilder.getKieModule().getReleaseId()).getKieBase();
        FactType factType = kieBase.getFactType("org.test", "Person");
        Object instance = factType.newInstance();
        factType.set(instance, "name", "Mark");
        factType.set(instance, "age", 37);
        List<String> names = new ArrayList<String>();
        KieSession ksession = kieBase.newKieSession();
        ksession.setGlobal("names", names);
        ksession.insert(instance);
        ksession.fireAllRules();
        Assert.assertEquals(1, names.size());
        Assert.assertEquals("Mark", names.get(0));
    }

    @Test
    public void testTooManyPositionalArguments() throws IllegalAccessException, InstantiationException {
        // DROOLS-559
        String drl = "package org.test;\n" + ((((((((("global java.util.List names;\n" + "declare Person\n") + "    name : String\n") + "    age : int\n") + "end\n") + "rule R when \n") + "    $p : Person( \"Mark\", 37, 42; )\n") + "then\n") + "    names.add( $p.getName() );\n") + "end\n");
        KieBuilder kieBuilder = build(drl);
        Assert.assertTrue(kieBuilder.getResults().hasMessages(ERROR));
    }

    @Test
    public void testOutOfRangePositions() throws IllegalAccessException, InstantiationException {
        // DROOLS-559
        String drl = "package org.test;\n" + ((((((((("global java.util.List names;\n" + "declare Person\n") + "    name : String @position(3)\n") + "    age : int @position(1)\n") + "end\n") + "rule R when \n") + "    $p : Person( 37, \"Mark\"; )\n") + "then\n") + "    names.add( $p.getName() );\n") + "end\n");
        KieBuilder kieBuilder = build(drl);
        Assert.assertTrue(kieBuilder.getResults().hasMessages(ERROR));
    }

    @Test
    public void testDuplicatedPositions() throws IllegalAccessException, InstantiationException {
        // DROOLS-559
        String drl = "package org.test;\n" + ((((((((("global java.util.List names;\n" + "declare Person\n") + "    name : String @position(1)\n") + "    age : int @position(1)\n") + "end\n") + "rule R when \n") + "    $p : Person( 37, \"Mark\"; )\n") + "then\n") + "    names.add( $p.getName() );\n") + "end\n");
        KieBuilder kieBuilder = build(drl);
        Assert.assertTrue(kieBuilder.getResults().hasMessages(ERROR));
    }

    @Test
    public void testMultipleAnnotationDeclarations() {
        String str1 = "";
        str1 += "package org.kie1 " + (((("" + "declare Foo \n") + "    name : String ") + "    age : int ") + "end ");
        String str2 = "";
        str2 += "package org.kie2 " + ((("" + "declare org.kie1.Foo ") + "    @role(event) ") + "end ");
        String str3 = "";
        str3 += "package org.kie3 " + ((("" + "declare org.kie1.Foo ") + "    @propertyReactive ") + "end ");
        String str4 = "" + (((((((("package org.kie4; " + "import org.kie1.Foo; ") + "") + "rule Check ") + "when ") + " $f : Foo( name == 'bar' ) ") + "then ") + " modify( $f ) { setAge( 99 ); } ") + "end ");
        KieHelper helper = new KieHelper();
        helper.addContent(str1, DRL);
        helper.addContent(str2, DRL);
        helper.addContent(str3, DRL);
        helper.addContent(str4, DRL);
        List<Message> msg = helper.verify().getMessages(ERROR);
        System.out.println(msg);
        Assert.assertEquals(0, msg.size());
        KieBase kieBase = helper.build();
        FactType type = kieBase.getFactType("org.kie1", "Foo");
        Assert.assertEquals(2, type.getFields().size());
        Object foo = null;
        try {
            foo = type.newInstance();
            type.set(foo, "name", "bar");
            Assert.assertEquals("bar", type.get(foo, "name"));
        } catch (InstantiationException e) {
            Assert.fail(e.getMessage());
        } catch (IllegalAccessException e) {
            Assert.fail(e.getMessage());
        }
        KieSession session = kieBase.newKieSession();
        FactHandle handle = session.insert(foo);
        int n = session.fireAllRules(5);
        Assert.assertTrue((handle instanceof EventFactHandle));
        Assert.assertEquals(1, n);
        Assert.assertEquals(99, type.get(foo, "age"));
    }

    @Test
    public void testTraitExtendPojo() {
        // DROOLS-697
        final String s1 = "package test;\n" + (((("declare Poojo " + "end ") + "declare trait Mask extends Poojo ") + "end ") + "");
        KieHelper kh = new KieHelper();
        kh.addContent(s1, DRL);
        Assert.assertEquals(1, kh.verify().getMessages(ERROR).size());
    }

    @Test
    public void testPojoExtendInterface() {
        // DROOLS-697
        final String s1 = "package test;\n" + (((("declare Poojo extends Mask " + "end ") + "declare trait Mask ") + "end ") + "");
        KieHelper kh = new KieHelper();
        kh.addContent(s1, DRL);
        Assert.assertEquals(1, kh.verify().getMessages(ERROR).size());
    }

    public static interface Base {
        public Object getFld();

        public void setFld(Object x);
    }

    public static interface Ext extends TypeDeclarationTest.Base {
        public String getFld();

        public void setFld(String s);
    }

    @Test
    public void testRedeclareWithInterfaceExtensionAndOverride() {
        final String s1 = ((((((((((("package test;\n" + "declare trait ") + (TypeDeclarationTest.Ext.class.getCanonicalName())) + " extends ") + (TypeDeclarationTest.Base.class.getCanonicalName())) + " ") + " fld : String ") + "end ") + "declare trait ") + (TypeDeclarationTest.Base.class.getCanonicalName())) + " ") + "end ") + "";
        KieHelper kh = new KieHelper();
        kh.addContent(s1, DRL);
        Assert.assertEquals(0, kh.verify().getMessages(ERROR).size());
    }

    @Test
    public void testDeclareWithExtensionAndOverride() {
        final String s1 = "package test; " + (((((((((((((((((("global java.util.List list; " + "declare Sub extends Sup ") + " fld : String ") + "end ") + "declare Sup ") + " fld : Object ") + "end ") + "rule Init when ") + "then insert( new Sub( 'aa' ) ); end ") + "rule CheckSup when ") + " $s : Sup( $f : fld == 'aa' ) ") + "then ") + "  list.add( \"Sup\" + $f );  ") + "end ") + "rule CheckSub when ") + " $s : Sub( $f : fld == 'aa' ) ") + "then ") + "  list.add( \"Sub\" + $f );  ") + "end ");
        KieHelper kh = new KieHelper();
        kh.addContent(s1, DRL);
        Assert.assertEquals(0, kh.verify().getMessages(ERROR).size());
        Assert.assertEquals(0, kh.verify().getMessages(Message.Level.WARNING).size());
        KieSession ks = kh.build().newKieSession();
        List list = new ArrayList();
        ks.setGlobal("list", list);
        ks.fireAllRules();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.containsAll(Arrays.asList("Supaa", "Subaa")));
        FactType sup = ks.getKieBase().getFactType("test", "Sup");
        FactType sub = ks.getKieBase().getFactType("test", "Sub");
        try {
            Method m1 = sup.getFactClass().getMethod("getFld");
            Assert.assertNotNull(m1);
            Assert.assertEquals(Object.class, m1.getReturnType());
            Method m2 = sub.getFactClass().getMethod("getFld");
            Assert.assertNotNull(m2);
            Assert.assertEquals(String.class, m2.getReturnType());
            Assert.assertEquals(0, sub.getFactClass().getFields().length);
            Assert.assertEquals(0, sub.getFactClass().getDeclaredFields().length);
            Assert.assertEquals(1, sup.getFactClass().getDeclaredFields().length);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    public static class SomeClass {}

    @Test
    public void testRedeclareClassAsTrait() {
        final String s1 = (("package test; " + ("global java.util.List list; " + "declare trait ")) + (TypeDeclarationTest.SomeClass.class.getCanonicalName())) + " end ";
        KieHelper kh = new KieHelper();
        kh.addContent(s1, DRL);
        Assert.assertEquals(1, kh.verify().getMessages(ERROR).size());
    }

    public static class BeanishClass {
        private int foo;

        public int getFoo() {
            return foo;
        }

        public void setFoo(int x) {
            foo = x;
        }

        public void setFooAsString(String x) {
            foo = Integer.parseInt(x);
        }
    }

    @Test
    public void testDeclarationOfClassWithNonStandardSetter() {
        final String s1 = (((((("package test; " + "import ") + (TypeDeclarationTest.BeanishClass.class.getCanonicalName())) + "; ") + "declare ") + (TypeDeclarationTest.BeanishClass.class.getSimpleName())) + " @propertyReactive end ") + "rule Check when BeanishClass() @Watch( foo ) then end ";
        KieHelper kh = new KieHelper();
        kh.addContent(s1, DRL);
        Assert.assertEquals(0, kh.verify().getMessages(ERROR).size());
    }

    @Test
    public void testDeclarationOfClassWithNonStandardSetterAndCanonicalName() {
        // DROOLS-815
        final String s1 = (((((("package test; " + "import ") + (TypeDeclarationTest.BeanishClass.class.getCanonicalName())) + "; ") + "declare ") + (TypeDeclarationTest.BeanishClass.class.getCanonicalName())) + " @propertyReactive end ") + "rule Check when BeanishClass() @Watch( foo ) then end ";
        KieHelper kh = new KieHelper();
        kh.addContent(s1, DRL);
        Assert.assertEquals(0, kh.verify().getMessages(ERROR).size());
    }

    @Test
    public void testDeclarationOfClassWithNonStandardSetterAndFulllName() {
        final String s1 = (((((("package test; " + "import ") + (TypeDeclarationTest.BeanishClass.class.getCanonicalName())) + "; ") + "declare ") + (TypeDeclarationTest.BeanishClass.class.getName())) + " @propertyReactive end ") + "rule Check when BeanishClass() @watch( foo ) then end ";
        KieHelper kh = new KieHelper();
        kh.addContent(s1, DRL);
        Assert.assertEquals(0, kh.verify().getMessages(ERROR).size());
    }
}

