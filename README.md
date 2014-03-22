Coherence Bootstrap
===================

Set of working code snippets designed for getting started with Coherence quickly along with some more advanced tips and tricks.
The tests and examples are supported by a multi-process test suite which was a precursor to the little-grid work done
in the coherence incubator*.

Topics covered:

**Basics:**
* Agregators
* Cache Stores (Async/Sync)
* CQCs
* Entry Processors
* Extend Proxies
* Filters
* Index Performance
* MapListeners
* Near Caching
* PartitionListeners (for detecting data loss)
* POF
* Put & Get
* Triggers

**More Complex:**
* Hopping between caches on the server
* Is POF always a good idea - testing serialisation times
* How listeners can lose data
* Membership listeners
* Multithreaded clients
* PutAll that reports individual errors
* Overflow caches
* Using services to isolate work onto groups of hardware

**Getting started is simple:**
* Download the zip or clone this repository
* Create a project in your IDE of choice
* Add the lib directory and "." to your classpath
* run/refer to the ant script if you have any problems

**Outstanding Tasks**
* Port cluster framework to LittleGrid
