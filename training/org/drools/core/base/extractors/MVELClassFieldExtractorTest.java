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
package org.drools.core.base.extractors;


import java.util.Vector;
import org.drools.core.base.ClassFieldAccessorStore;
import org.drools.core.common.ProjectClassLoader;
import org.drools.core.rule.MVELDialectRuntimeData;
import org.drools.core.test.model.Address;
import org.drools.core.test.model.Person;
import org.junit.Assert;
import org.junit.Test;


public class MVELClassFieldExtractorTest {
    ClassFieldAccessorStore store = new ClassFieldAccessorStore();

    MVELObjectClassFieldReader extractor;

    private final Person[] person = new Person[2];

    private final Address[] business = new Address[2];

    private final Address[] home = new Address[2];

    @Test
    public void testGetBooleanValue() {
        try {
            this.extractor.getBooleanValue(null, this.person[0]);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetByteValue() {
        try {
            this.extractor.getByteValue(null, this.person[0]);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetCharValue() {
        try {
            this.extractor.getCharValue(null, this.person[0]);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetDoubleValue() {
        try {
            this.extractor.getDoubleValue(null, this.person[0]);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetFloatValue() {
        try {
            this.extractor.getFloatValue(null, this.person[0]);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetIntValue() {
        try {
            this.extractor.getIntValue(null, this.person[0]);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetLongValue() {
        try {
            this.extractor.getLongValue(null, this.person[0]);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetShortValue() {
        try {
            this.extractor.getShortValue(null, this.person[0]);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetValue() {
        try {
            Assert.assertEquals(home[0].getStreet(), this.extractor.getValue(null, this.person[0]));
            Assert.assertTrue(((this.extractor.getValue(null, this.person[0])) instanceof String));
        } catch (final Exception e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testIsNullValue() {
        try {
            Assert.assertFalse(this.extractor.isNullValue(null, this.person[0]));
            MVELObjectClassFieldReader nullExtractor = ((MVELObjectClassFieldReader) (store.getMVELReader(Person.class.getPackage().getName(), Person.class.getName(), "addresses['business'].phone", true, String.class)));
            MVELDialectRuntimeData data = new MVELDialectRuntimeData();
            data.addImport(Person.class.getSimpleName(), Person.class);
            data.onAdd(null, ProjectClassLoader.createProjectClassLoader());
            nullExtractor.compile(data);
            // 
            // InternalReadAccessor nullExtractor = store.getReader( Person.class,
            // "addresses['business'].phone",
            // getClass().getClassLoader() );
            Assert.assertTrue(nullExtractor.isNullValue(null, this.person[0]));
        } catch (final Exception e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testMultithreads() {
        final int THREAD_COUNT = 30;
        try {
            final Vector errors = new Vector();
            final Thread[] t = new Thread[THREAD_COUNT];
            for (int j = 0; j < 10; j++) {
                for (int i = 0; i < (t.length); i++) {
                    final int ID = i;
                    t[i] = new Thread() {
                        public void run() {
                            try {
                                final int ITERATIONS = 300;
                                for (int k = 0; k < ITERATIONS; k++) {
                                    String value = ((String) (extractor.getValue(null, person[(ID % 2)])));
                                    if (!(home[(ID % 2)].getStreet().equals(value))) {
                                        errors.add((((((("THREAD(" + ID) + "): Wrong value at iteration ") + k) + ". Value='") + value) + "\'\n"));
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                errors.add(ex);
                            }
                        }
                    };
                    t[i].start();
                }
                for (int i = 0; i < (t.length); i++) {
                    t[i].join();
                }
            }
            if (!(errors.isEmpty())) {
                Assert.fail((" Errors occured during execution\n" + (errors.toString())));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail(("Unexpected exception running test: " + (e.getMessage())));
        }
    }
}

