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


import DuplicateFunction.KEY;
import JavaDialectConfiguration.CompilerType.ECLIPSE;
import JavaDialectConfiguration.CompilerType.JANINO;
import ResultSeverity.ERROR;
import ResultSeverity.INFO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.lang.descr.AndDescr;
import org.drools.compiler.lang.descr.AttributeDescr;
import org.drools.compiler.lang.descr.BaseDescr;
import org.drools.compiler.lang.descr.EvalDescr;
import org.drools.compiler.lang.descr.FunctionDescr;
import org.drools.compiler.lang.descr.FunctionImportDescr;
import org.drools.compiler.lang.descr.ImportDescr;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.compiler.lang.descr.ProcessDescr;
import org.drools.compiler.lang.descr.RuleDescr;
import org.drools.compiler.rule.builder.AccumulateBuilder;
import org.drools.compiler.rule.builder.ConsequenceBuilder;
import org.drools.compiler.rule.builder.EnabledBuilder;
import org.drools.compiler.rule.builder.EntryPointBuilder;
import org.drools.compiler.rule.builder.FromBuilder;
import org.drools.compiler.rule.builder.GroupElementBuilder;
import org.drools.compiler.rule.builder.PackageBuildContext;
import org.drools.compiler.rule.builder.PatternBuilder;
import org.drools.compiler.rule.builder.PredicateBuilder;
import org.drools.compiler.rule.builder.QueryBuilder;
import org.drools.compiler.rule.builder.ReturnValueBuilder;
import org.drools.compiler.rule.builder.RuleBuildContext;
import org.drools.compiler.rule.builder.RuleClassBuilder;
import org.drools.compiler.rule.builder.RuleConditionBuilder;
import org.drools.compiler.rule.builder.SalienceBuilder;
import org.drools.compiler.rule.builder.dialect.java.JavaDialectConfiguration;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.definitions.impl.KnowledgePackageImpl;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.rule.Pattern;
import org.drools.core.rule.RuleConditionElement;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.io.Resource;
import org.kie.internal.builder.KnowledgeBuilderResult;
import org.kie.internal.builder.conf.KBuilderSeverityOption;
import org.kie.soup.project.datamodel.commons.types.TypeResolver;

import static DuplicateFunction.KEY;


public class PackageBuilderConfigurationTest {
    private static String droolsDialectJavaCompilerOrig;

    private static String droolsDialectDefaultOrig;

    @Test
    public void testSystemProperties() {
        KnowledgeBuilderConfigurationImpl cfg;
        JavaDialectConfiguration javaConf;
        System.setProperty("drools.dialect.java.compiler", "JANINO");
        cfg = new KnowledgeBuilderConfigurationImpl();
        javaConf = ((JavaDialectConfiguration) (cfg.getDialectConfiguration("java")));
        Assert.assertEquals(JANINO, javaConf.getCompiler());
        KnowledgeBuilderConfigurationImpl cfg2 = new KnowledgeBuilderConfigurationImpl();
        JavaDialectConfiguration javaConf2 = ((JavaDialectConfiguration) (cfg2.getDialectConfiguration("java")));
        Assert.assertEquals(javaConf.getCompiler(), javaConf2.getCompiler());
        System.setProperty("drools.dialect.java.compiler", "ECLIPSE");
        cfg = new KnowledgeBuilderConfigurationImpl();
        javaConf = ((JavaDialectConfiguration) (cfg.getDialectConfiguration("java")));
        Assert.assertEquals(ECLIPSE, javaConf.getCompiler());
        javaConf2.setCompiler(ECLIPSE);
        Assert.assertEquals(ECLIPSE, javaConf2.getCompiler());
        javaConf2.setCompiler(JANINO);
        Assert.assertEquals(JANINO, javaConf2.getCompiler());
        final KnowledgeBuilderConfigurationImpl cfg3 = new KnowledgeBuilderConfigurationImpl();
        JavaDialectConfiguration javaConf3 = ((JavaDialectConfiguration) (cfg3.getDialectConfiguration("java")));
        Assert.assertEquals(javaConf.getCompiler(), javaConf3.getCompiler());
    }

    @Test
    public void testProgrammaticProperties() {
        KnowledgeBuilderConfigurationImpl cfg = new KnowledgeBuilderConfigurationImpl();
        Assert.assertTrue(cfg.getDefaultDialect().equals("java"));
        Properties properties = new Properties();
        properties.setProperty("drools.dialect.default", "mvel");
        KnowledgeBuilderConfigurationImpl cfg1 = new KnowledgeBuilderConfigurationImpl(properties);
        Assert.assertEquals("mvel", cfg1.getDefaultDialect());
        final KnowledgeBuilderConfigurationImpl cfg2 = new KnowledgeBuilderConfigurationImpl(properties);
        Assert.assertEquals(cfg1.getDefaultDialect().getClass(), cfg2.getDefaultDialect().getClass());
    }

    @Test
    public void testProgramaticProperties2() {
        JavaDialectConfiguration javaConf = new JavaDialectConfiguration();
        javaConf.init(new KnowledgeBuilderConfigurationImpl());
        javaConf.setCompiler(ECLIPSE);
        KnowledgeBuilderConfigurationImpl cfg = new KnowledgeBuilderConfigurationImpl();
        cfg.setDialectConfiguration("java", javaConf);
        JavaDialectConfiguration javaConf2 = ((JavaDialectConfiguration) (cfg.getDialectConfiguration("java")));
        Assert.assertSame(javaConf, javaConf2);
        Assert.assertEquals(ECLIPSE, javaConf2.getCompiler());
        javaConf = new JavaDialectConfiguration();
        javaConf.init(new KnowledgeBuilderConfigurationImpl());
        javaConf.setCompiler(JANINO);
        cfg = new KnowledgeBuilderConfigurationImpl();
        cfg.setDialectConfiguration("java", javaConf);
        javaConf2 = ((JavaDialectConfiguration) (cfg.getDialectConfiguration("java")));
        Assert.assertSame(javaConf, javaConf2);
        Assert.assertEquals(JANINO, javaConf2.getCompiler());
    }

    @Test
    public void testResultSeverity() {
        System.setProperty(("drools.kbuilder.severity." + (KEY)), "ERROR");
        KnowledgeBuilderConfigurationImpl cfg = new KnowledgeBuilderConfigurationImpl();
        Assert.assertEquals(cfg.getOptionKeys(KBuilderSeverityOption.class).size(), 1);
        Assert.assertEquals(cfg.getOption(KBuilderSeverityOption.class, KEY).getSeverity(), ERROR);
    }

    @Test
    public void testResultSeverityNonExistingValueDefaultToInfo() {
        System.setProperty(("drools.kbuilder.severity." + (KEY)), "FOO");
        KnowledgeBuilderConfigurationImpl cfg = new KnowledgeBuilderConfigurationImpl();
        Assert.assertEquals(cfg.getOptionKeys(KBuilderSeverityOption.class).size(), 1);
        Assert.assertEquals(cfg.getOption(KBuilderSeverityOption.class, KEY).getSeverity(), INFO);
    }

    @Test
    public void testMockDialect() {
        InternalKnowledgePackage pkg = new KnowledgePackageImpl("org.pkg1");
        KnowledgeBuilderConfigurationImpl cfg1 = new KnowledgeBuilderConfigurationImpl();
        PackageBuilderConfigurationTest.MockDialectConfiguration mockConf = new PackageBuilderConfigurationTest.MockDialectConfiguration();
        // cfg1.buildDialectRegistry().addDialect( "mock",
        // mockConf.getDialect() );
        cfg1.addDialect("mock", mockConf);
        cfg1.setDefaultDialect("mock");
        KnowledgeBuilderImpl builder = new KnowledgeBuilderImpl(pkg, cfg1);
        PackageRegistry pkgRegistry = builder.getPackageRegistry(pkg.getName());
        DialectCompiletimeRegistry dialectRegistry = pkgRegistry.getDialectCompiletimeRegistry();
        PackageBuilderConfigurationTest.MockDialect mockDialect2 = ((PackageBuilderConfigurationTest.MockDialect) (dialectRegistry.getDialect(cfg1.getDefaultDialect())));
        Assert.assertSame(pkg, mockDialect2.getPkg());
        Assert.assertNull(mockDialect2.getRuleDescr());
        RuleDescr ruleDescr = new RuleDescr("test rule");
        ruleDescr.addAttribute(new AttributeDescr("dialect", "mock"));
        ruleDescr.setLhs(new AndDescr());
        EvalDescr evalDescr = new EvalDescr();
        ruleDescr.getLhs().addDescr(evalDescr);
        PackageDescr pkgDescr = new PackageDescr("org.pkg1");
        pkgDescr.addImport(new ImportDescr("java.util.HashMap"));
        FunctionImportDescr functionImportDescr = new FunctionImportDescr();
        functionImportDescr.setTarget("java.lang.System.currentTimeMillis");
        pkgDescr.addFunctionImport(functionImportDescr);
        pkgDescr.addRule(ruleDescr);
        builder.addPackage(pkgDescr);
        Assert.assertSame(ruleDescr, mockDialect2.getRuleDescr());
        Assert.assertTrue(mockDialect2.getImport().contains("java.util.HashMap"));
        Assert.assertTrue(mockDialect2.getStaticImport().contains("java.lang.System.currentTimeMillis"));
        Assert.assertEquals("eval was built", evalDescr.getContent());
        Assert.assertEquals("consequence was built", ruleDescr.getConsequence());
        Assert.assertTrue(mockDialect2.isCompileAll());
        Assert.assertNotNull(pkg.getRule("test rule"));
        // make sure there were no other general errors.
        Assert.assertFalse(builder.hasErrors());
    }

    public static class MockDialectConfiguration implements DialectConfiguration {
        private KnowledgeBuilderConfigurationImpl conf;

        public Dialect newDialect(ClassLoader rootClassLoader, KnowledgeBuilderConfigurationImpl pkgConf, PackageRegistry pkgRegistry, InternalKnowledgePackage pkg) {
            return new PackageBuilderConfigurationTest.MockDialect(rootClassLoader, pkgConf, pkgRegistry, pkg);
        }

        public KnowledgeBuilderConfigurationImpl getPackageBuilderConfiguration() {
            return this.conf;
        }

        public void init(KnowledgeBuilderConfigurationImpl configuration) {
            this.conf = configuration;
        }
    }

    public static class MockDialect implements Dialect {
        private InternalKnowledgePackage pkg;

        private RuleDescr ruleDescr;

        private RuleImpl rule;

        private List<String> imports = new ArrayList<String>();

        private boolean compileAll = false;

        public MockDialect(ClassLoader rootClassLoader, KnowledgeBuilderConfigurationImpl pkgConf, PackageRegistry pkgRegistry, InternalKnowledgePackage pkg) {
            this.pkg = pkg;
        }

        public void init(RuleDescr ruleDescr) {
            this.ruleDescr = ruleDescr;
        }

        public InternalKnowledgePackage getPkg() {
            return pkg;
        }

        public RuleDescr getRuleDescr() {
            return ruleDescr;
        }

        public void addFunction(FunctionDescr functionDescr, TypeResolver typeResolver) {
            // TODO Auto-generated method stub
        }

        public void addImport(ImportDescr importDescr) {
            this.imports.add(importDescr.getTarget());
        }

        public List getImport() {
            return this.imports;
        }

        public void addStaticImport(ImportDescr importDescr) {
            this.imports.add(importDescr.getTarget());
        }

        public List getStaticImport() {
            return this.imports;
        }

        public void addRule(RuleBuildContext context) {
            this.rule = context.getRule();
        }

        public RuleImpl getRule() {
            return this.rule;
        }

        public void compileAll() {
            this.compileAll = true;
        }

        public boolean isCompileAll() {
            return this.compileAll;
        }

        public AccumulateBuilder getAccumulateBuilder() {
            // TODO Auto-generated method stub
            return null;
        }

        public RuleConditionBuilder getBuilder(Class clazz) {
            if (clazz == (EvalDescr.class)) {
                return getEvalBuilder();
            } else
                if (clazz == (AndDescr.class)) {
                    return new GroupElementBuilder();
                } else {
                    throw new RuntimeException((("clazz " + clazz) + " is not yet configured "));
                }

        }

        public Map getBuilders() {
            return null;
        }

        public ConsequenceBuilder getConsequenceBuilder() {
            return new PackageBuilderConfigurationTest.MockConsequenceBuilder();
        }

        public RuleConditionBuilder getEvalBuilder() {
            return new PackageBuilderConfigurationTest.MockEvalBuilder();
        }

        public String getExpressionDialectName() {
            return null;
        }

        public FromBuilder getFromBuilder() {
            return null;
        }

        public PatternBuilder getPatternBuilder() {
            return null;
        }

        public PredicateBuilder getPredicateBuilder() {
            return null;
        }

        public QueryBuilder getQueryBuilder() {
            return null;
        }

        public List<KnowledgeBuilderResult> getResults() {
            return null;
        }

        public void clearResults() {
        }

        public ReturnValueBuilder getReturnValueBuilder() {
            return null;
        }

        public RuleClassBuilder getRuleClassBuilder() {
            return null;
        }

        public SalienceBuilder getSalienceBuilder() {
            return null;
        }

        public TypeResolver getTypeResolver() {
            return null;
        }

        public String getId() {
            return "mock";
        }

        public void init(ProcessDescr processDescr) {
            // TODO Auto-generated method stub
        }

        public EntryPointBuilder getEntryPointBuilder() {
            // TODO Auto-generated method stub
            return null;
        }

        public void postCompileAddFunction(FunctionDescr functionDescr, TypeResolver typeResolver) {
            // TODO Auto-generated method stub
        }

        public void preCompileAddFunction(FunctionDescr functionDescr, TypeResolver typeResolver) {
            // TODO Auto-generated method stub
        }

        public EnabledBuilder getEnabledBuilder() {
            // TODO Auto-generated method stub
            return null;
        }

        public void addFunction(FunctionDescr functionDescr, TypeResolver typeResolver, Resource resource) {
            // TODO Auto-generated method stub
        }

        public AnalysisResult analyzeExpression(PackageBuildContext context, BaseDescr descr, Object content, BoundIdentifiers availableIdentifiers) {
            // TODO Auto-generated method stub
            return null;
        }

        public AnalysisResult analyzeExpression(PackageBuildContext context, BaseDescr descr, Object content, BoundIdentifiers availableIdentifiers, Map<String, Class<?>> localTypes) {
            // TODO Auto-generated method stub
            return null;
        }

        public AnalysisResult analyzeBlock(PackageBuildContext context, BaseDescr descr, String text, BoundIdentifiers availableIdentifiers) {
            // TODO Auto-generated method stub
            return null;
        }

        public PackageRegistry getPackageRegistry() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public static class MockEvalBuilder implements RuleConditionBuilder {
        public RuleConditionElement build(RuleBuildContext context, BaseDescr descr) {
            EvalDescr evalDescr = ((EvalDescr) (descr));
            evalDescr.setContent("eval was built");
            return null;
        }

        public RuleConditionElement build(RuleBuildContext context, BaseDescr descr, Pattern prefixPattern) {
            return null;
        }
    }

    public static class MockConsequenceBuilder implements ConsequenceBuilder {
        public void build(RuleBuildContext context, String name) {
            context.getRuleDescr().setConsequence("consequence was built");
        }
    }
}

