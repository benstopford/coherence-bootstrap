Coherence Bootstrap
===================
[![Build Status](https://travis-ci.org/benstopford/coherence-bootstrap.svg?branch=master)](https://travis-ci.org/benstopford/coherence-bootstrap)

Set of code snippets designed for getting started with Coherence quickly along with some more advanced tips and tricks.
The tests and examples are supported by a multi-process test suite which was a precursor to the little-grid / the coherence incubator.

Topics covered:

**Basics:**
* [Agregators](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/Aggregators.java)
* [Cache Stores (Async/Sync)](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/CacheStoreAsync.java)
* [CQCs](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/CQCs.java)
* [Entry Processors](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/EntryProcessors.java)
* [Extend Proxies](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/ExtendProxies.java)
* [Filters] (https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/Filters.java)
* [Index Performance](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/IndexesAreFast.java)
* [MapListeners](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/MapListeners.java)
* [Near Caching](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/NearCaching.java)
* [PartitionListeners (for detecting data loss)](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/ParitionListenerForDataLoss.java)
* [POF](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/POF.java)
* [Put & Get](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/PutAndGet.java)
* [Triggers](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/basic/Triggers.java)

**More Complex:**
* [Hopping between caches on the server](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/HopBetweenCaches.java)
* [Is POF always a good idea - testing serialisation times](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/PofEfficiency.java)
* [How listeners can lose data](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/ListenersCanLoseData.java)
* [Membership listeners](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/MembershipListeners.java)
* [Multithreaded clients](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/MultiThreadedExtendClientExample.java)
* [PutAll that reports individual errors](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/PutAllThatReportsIndividualExceptions.java)
* [Overflow caches](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/UsingAnOverflowCacheToExpireEntriesToDiskExample.java)
* [Using services to isolate work onto groups of hardware](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/UsingServicesToIsolateWorkOnDifferentSetsOfMachines.java)
* [Sizing the indexes in your Coherence Cache via JMX](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/sizing/CountIndexFootprintOverMultipleCachesViaJmx.java)
* [Exploring POF internal encodings](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/PofInternals.java)
* [Understanding POF performance, when it is faster and when it is not?](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/PofEfficiency.java)
* [Put backups on disk with elastic data](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/PutBackupsOnDiskUsingElasticData.java)
* [Put all data on disk with elastic data](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/PutDataOnDiskUsingElasticData.java)
* [Join, server side, using backing map access and key-association](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/JoinTwoCachesUsingBackingMapAccessAndKeyAssociation.java)


**Utilities**
* ClusterGC: GC all processes incrementally [[test](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/sizing/GarbageCollectWholeCluster.java)] [[code](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/structures/tools/jmx/ClusterGC.java)]
* BinaryCacheSizeCounter: Work out how much data is in your cluster. [[test](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/morecomplex/sizing/CountBinarySizeOfAllObjects.java) [code](https://github.com/benstopford/coherence-bootstrap/blob/master/src/com/benstopford/coherence/bootstrap/structures/tools/jmx/BinaryCacheSizeCounter.java)]

**Getting started is simple:**
* Download the zip or clone this repository
* If you wish, run 'ant test' to check everything works ok
* Create a project in your IDE of choice
* Add lib/ & "." to your classpath
* Refer to the ant script if you have any problems

**Outstanding Tasks**
* Port cluster framework to LittleGrid
