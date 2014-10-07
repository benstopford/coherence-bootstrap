package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.net.BackingMapContext;
import com.tangosol.util.ConditionalIndex;
import com.tangosol.util.Filter;
import com.tangosol.util.MapIndex;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.IndexAwareExtractor;

import java.util.Comparator;
import java.util.Map;

public class MyCustomIndex implements IndexAwareExtractor {
    public int getCount = 0;
    public int getIndexContentsCount = 0;

    static final Filter all = new Filter() {
        @Override
        public boolean evaluate(Object o) {
            return true;
        }
    };
    private ValueExtractor extractor;


    public MyCustomIndex(ValueExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public MapIndex createIndex(boolean b, Comparator comparator, Map map, BackingMapContext backingMapContext) {

        //put your own MapIndex implementation here
        boolean useForwardIndex = false;
        MapIndex index = wrap(new ConditionalIndex(all, extractor, false, null, useForwardIndex, null));
        map.put(extractor, index);
        return index;
    }

    @Override
    public MapIndex destroyIndex(Map map) {
        map.remove(extractor);
        return null;
    }

    @Override
    public Object extract(Object o) {
        return extractor.extract(o);
    }


    private MapIndex wrap(final ConditionalIndex index) {
        return new MapIndex() {
            @Override
            public ValueExtractor getValueExtractor() {
                return index.getValueExtractor();
            }

            @Override
            public boolean isOrdered() {
                return false;
            }

            @Override
            public boolean isPartial() {
                return false;
            }

            @Override
            public Map getIndexContents() {
                getIndexContentsCount++;
                return index.getIndexContents();
            }

            @Override
            public Object get(Object o) {
                getCount++;
                return index.get(o);
            }

            @Override
            public Comparator getComparator() {
                return index.getComparator();
            }

            @Override
            public void insert(Map.Entry entry) {
                index.insert(entry);
            }

            @Override
            public void update(Map.Entry entry) {
                index.update(entry);
            }

            @Override
            public void delete(Map.Entry entry) {
                index.delete(entry);
            }
        };
    }
}
