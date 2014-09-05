package com.benstopford.coherence.bootstrap.structures.tools.index;

import java.util.Map;

public interface IndexSizerMBean {
    long getTotal();
    Map<String, Long> getIndexSizes();
}
