package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PutAllWithErrorReporting {
    private InvocationService invocationService;
    private DistributedCacheService cacheService;
    private String cacheName;
    private String configPath;

    public PutAllWithErrorReporting(InvocationService invocationService, DistributedCacheService cacheService, String cacheName, String configPath) {
        this.invocationService = invocationService;
        this.cacheService = cacheService;
        this.cacheName = cacheName;
        this.configPath = configPath;
    }

    public Map<Object, Throwable> putAll(Map map) {
        Map<Member, Map> entriesByMember = splitByMember(map);

        PutAllObserver observer = new PutAllObserver(entriesByMember.size());

        for (Member member : entriesByMember.keySet()) {
            Map entriesForThisMember = entriesByMember.get(member);
            System.out.println("running invocable on member "+member);
            invocationService.execute(
                    new PutAllInvocable(entriesForThisMember, cacheName, configPath),
                    Collections.singleton(member),
                    observer
            );
        }
        return observer.getErrors();
    }


    private HashMap<Member, Map> splitByMember(Map map) {
        HashMap<Member, Map> entriesByMember = new HashMap<Member, Map>();
        for (Object e : map.entrySet()) {
            Map.Entry entry = (Map.Entry) e;
            Member member = cacheService.getKeyOwner(entry.getKey());

            Map entriesForThisMember = entriesByMember.get(member);
            if (entriesForThisMember == null) {
                entriesForThisMember = new HashMap();
                entriesByMember.put(member, entriesForThisMember);
            }

            entriesForThisMember.put(entry.getKey(), entry.getValue());
        }
        return entriesByMember;
    }


    public class PutAllObserver implements InvocationObserver {

        private Map<Member, Map<Object, Throwable>> errors = new HashMap<Member, Map<Object, Throwable>>();
        private volatile int actualCount = 0;
        private int members;

        public PutAllObserver(int members) {
            this.members = members;
        }


        public synchronized void memberCompleted(Member member, Object memberResult) {
            System.out.println("'Completed' was called on Observer with arguments member: " + members  +  " result: " +actualCount);
            Map<Object, Throwable> errorsForMember = (Map<Object,Throwable>) memberResult;
            errors.put(member, errorsForMember);
            actualCount++;
            this.notifyAll();
        }

        public synchronized void memberFailed(Member member, Throwable throwable) {
            System.out.println("'Failed' was called on Observer with arguments member: " + members  +  " result: " +actualCount);
            errors.put(member, Collections.singletonMap((Object) "member-error", throwable));
            actualCount++;
            this.notifyAll();
        }

        public void memberLeft(Member member) {

        }

        public void invocationCompleted() {

        }

        public synchronized Map<Object, Throwable> getErrors() {
            while (actualCount < members) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("Observer Completed");
            Map<Object, Throwable> allErrors = new HashMap<Object, Throwable>();
            for(Member member: errors.keySet()){
                allErrors.putAll(errors.get(member));
            }
            return allErrors;
        }
    }
}
