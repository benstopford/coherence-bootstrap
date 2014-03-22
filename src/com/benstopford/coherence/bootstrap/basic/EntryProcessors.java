package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.processor.AbstractProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * BTS, 07-Dec-2007
 */
public final class EntryProcessors extends ClusterRunner {

    @Test
    public void doSomethingAtomicallyWithAnEntryProcessor() {
        NamedCache cache = getBasicCache("stuff");

        cache.put("Key", "Value");

        Object returnVal = cache.invoke("Key", new ValueChangingEntryProcessor("Value2"));

        assertEquals("The value has been set to Value2", returnVal);
    }

    static final class ValueChangingEntryProcessor extends AbstractProcessor {
        private final String newValue;

        public ValueChangingEntryProcessor(String newValue) {
            this.newValue = newValue;
        }

        public Object process(InvocableMap.Entry entry) {
            entry.setValue(newValue);
            return "The value has been set to " + newValue;
        }
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
