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
package org.drools.core.rule;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.CodeSource;
import org.drools.core.WorkingMemory;
import org.drools.core.base.ClassFieldAccessorCache;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.definitions.impl.KnowledgePackageImpl;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.spi.EvalExpression;
import org.drools.core.spi.Tuple;
import org.junit.Assert;
import org.junit.Test;


public class PackageCompilationDataTest {
    public static class TestEvalExpression implements EvalExpression {
        public Object createContext() {
            return null;
        }

        public boolean evaluate(Tuple t, Declaration[] d, WorkingMemory w, Object context) {
            return false;
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        }

        public void writeExternal(ObjectOutput out) throws IOException {
        }

        public Declaration[] getRequiredDeclarations() {
            return null;
        }

        public void replaceDeclaration(Declaration declaration, Declaration resolved) {
        }

        public EvalExpression clone() {
            return this;
        }
    }

    @Test
    public void testCodeSourceUrl() throws Exception {
        final String className = PackageCompilationDataTest.TestEvalExpression.class.getName();
        KnowledgeBaseImpl kBase = new KnowledgeBaseImpl("xxx", null);
        InternalKnowledgePackage pkg = new KnowledgePackageImpl("org.drools");
        pkg.setClassFieldAccessorCache(new ClassFieldAccessorCache(Thread.currentThread().getContextClassLoader()));
        JavaDialectRuntimeData data = new JavaDialectRuntimeData();
        data.onAdd(pkg.getDialectRuntimeRegistry(), kBase.getRootClassLoader());
        pkg.getDialectRuntimeRegistry().setDialectData("java", data);
        kBase.addPackage(pkg);
        final JavaDialectRuntimeData pcData = ((JavaDialectRuntimeData) (pkg.getDialectRuntimeRegistry().getDialectData("java")));
        final EvalCondition invoker = new EvalCondition(null);
        pcData.putInvoker(className, invoker);
        final InputStream is = getClass().getClassLoader().getResourceAsStream(((className.replace('.', '/')) + ".class"));
        try {
            pcData.write(((className.replace('.', '/')) + ".class"), PackageCompilationDataTest.read(is));
        } finally {
            is.close();
        }
        pcData.onAdd(pkg.getDialectRuntimeRegistry(), kBase.getRootClassLoader());
        pcData.onBeforeExecute();
        Class cls = kBase.getRootClassLoader().loadClass("org.drools.core.rule.PackageCompilationDataTest$TestEvalExpression");
        final CodeSource codeSource = invoker.getEvalExpression().getClass().getProtectionDomain().getCodeSource();
        Assert.assertNotNull(codeSource.getLocation());
    }
}

