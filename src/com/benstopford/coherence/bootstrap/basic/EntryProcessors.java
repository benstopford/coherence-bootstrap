package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.CoherenceClusteredTest;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * BTS, 07-Dec-2007
 */
public final class EntryProcessors extends CoherenceClusteredTest {

    public void testDoSomethingAtomicallyWithAnEntryProcessor() {
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

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
