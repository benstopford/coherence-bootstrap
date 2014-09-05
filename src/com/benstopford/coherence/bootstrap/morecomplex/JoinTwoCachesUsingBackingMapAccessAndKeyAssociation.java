package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.tangosol.net.BackingMapContext;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.KeyAssociation;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.NotFilter;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Very simple example of server side joins using two caches in the same cache
 * service, an Aggregator and Key Association (affinity) to keep them together.
 *
 * This is just an example to be extended. It ignores key elements that you
 * should include in production implementations such as the use of POF for
 * efficient extraction and the use of indexes for the the join itself rather
 * than iterating over entries in the surrogate cache.
 */
public class JoinTwoCachesUsingBackingMapAccessAndKeyAssociation extends ClusterRunner implements Serializable {
    private static NotFilter grabEverything = new NotFilter(new EqualsFilter("toString", "it won't be this"));
    private static final Random random = new Random();

    @Test
    public void shouldJoinDataInDifferentCaches() throws IOException, InterruptedException {
        startBasicCacheProcess();
        startBasicCacheProcess();
        NamedCache trades = getBasicCache("trade");
        NamedCache valuations = getBasicCache("valuations");
        assertClusterStarted();

        //Add 10 trades with two corresponding valuations for each of them
        for (int i = 1; i < 11; i++) {
            Trade trade = new Trade(i);
            Valuation valuation1 = new Valuation(new ValuationId(trade.getId(),i), trade.getId(), random.nextLong());
            Valuation valuation2 = new Valuation(new ValuationId(trade.getId(),-i), trade.getId(), random.nextLong());
            valuations.put(valuation1.getId(), valuation1);
            valuations.put(valuation2.getId(), valuation2);
            trades.put(trade.getId(), trade);
        }
        assertThat(trades.size(), is(10));
        assertThat(valuations.size(), is(20));

        //Run an Aggregator that joins together different trades and valuations
        Object aggregate = trades.aggregate(grabEverything, new JoinAggregator());
        Map<Trade, Valuation[]> result = (Map<Trade, Valuation[]>) aggregate;

        //Each trade should have two joined valuations
        assertThat(result.size(), is(10));
        for (Trade trade : result.keySet()) {
            assertThat(result.get(trade).length, is(2));
            assertThat(result.get(trade)[0].getTradeId(), is(trade.getId()));
            assertThat(result.get(trade)[1].getTradeId(), is(trade.getId()));
        }
    }

    class JoinAggregator implements InvocableMap.ParallelAwareAggregator {

        public InvocableMap.EntryAggregator getParallelAggregator() {
            return this;
        }

        public Object aggregateResults(Collection collection) {
            Map<Trade, Valuation[]> result = new HashMap<Trade, Valuation[]>();
            for (Object nodeResult : collection) {
                result.putAll((Map<Trade, Valuation[]>) nodeResult);
            }
            return result;
        }

        public Object aggregate(Set set) {
            List<Valuation> matchingValuations;
            Map<Trade, Valuation[]> all = new HashMap<Trade, Valuation[]>();

            for (BinaryEntry entry : (Set<BinaryEntry>) set) {
                long tradeId = (Long) entry.getKey();
                BackingMapContext context = entry.getContext().getBackingMapContext("valuations");
                Collection<Binary> valBackMap = context.getBackingMap().values();
                matchingValuations = new ArrayList<Valuation>();
                for (Binary val : valBackMap) {
                    Valuation valuation = (Valuation) ExternalizableHelper.fromBinary(val, entry.getSerializer());
                    if (valuation.getTradeId() == tradeId) {
                        matchingValuations.add(valuation);
                    }
                }
                all.put((Trade) entry.getValue(), matchingValuations.toArray(new Valuation[]{}));
            }

            return all;
        }
    }

    public class Trade implements Serializable {
        private final long id;

        public Trade(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Trade{" +
                    "id=" + id +
                    '}';
        }
    }

    public class Valuation implements Serializable {
        private ValuationId id;
        private long tradeId;
        private long pv;

        public Valuation(ValuationId id, long tradeId, long pv) {
            this.id = id;
            this.tradeId = tradeId;
            this.pv = pv;
        }

        public ValuationId getId() {
            return id;
        }

        public long getTradeId() {
            return tradeId;
        }

        public long getPv() {
            return pv;
        }

        @Override
        public String toString() {
            return "Valuation{" +
                    "id=" + id +
                    ", tradeId=" + tradeId +
                    '}';
        }
    }

    public class ValuationId implements Serializable,KeyAssociation{
        private long tradeId;
        private long valId;

        public ValuationId(long tradeId, long valId) {
            this.tradeId = tradeId;
            this.valId = valId;
        }

        @Override
        public Object getAssociatedKey() {
            return tradeId;
        }

        @Override
        public String toString() {
            return "ValuationId{" +
                    "tradeId=" + tradeId +
                    ", valId=" + valId +
                    '}';
        }
    }
}
