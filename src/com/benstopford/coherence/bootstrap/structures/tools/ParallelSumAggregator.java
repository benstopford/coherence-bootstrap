package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.util.InvocableMap;

import java.util.Collection;
import java.util.Set;

public class ParallelSumAggregator implements InvocableMap.ParallelAwareAggregator
{
    private InvocableMap.EntryAggregator delegate;

    public ParallelSumAggregator(InvocableMap.EntryAggregator delegate) {
        this.delegate = delegate;
    }

    public InvocableMap.EntryAggregator getParallelAggregator() {
        return this;
    }

    public Object aggregateResults(Collection collection) {
        int result = 0;
        for(Object o: collection){
            result+= (Integer) o;
        }
        return result;
    }

    public Object aggregate(Set set) {
        return  delegate.aggregate(set);
    }
}
