### unify-base – A collection of utility classes

---

The POM project consists of a set of utility classes and methods. Many classes are self explanatory and some have the
documentation provided at a class level. The users of this module may also refer to the test cases in `src/test` folder.

Below is a high level overview of the key classes present in this module.

**Package com.aexp.acq.unify.base.utils**

`BaseUtils` class

This class consists of static helper methods. Please refer to the Javadoc documentation for these methods for further
details.

`BlockOnOfferQueue` class

This class is used as the queue supplied to an Executor Service so the Executor Services blocks when an item is
submitted for execution and there is no space in the queue to accept the item. In Java, there is no way to block in such
a scenario and hence this implementation. In this implementation, the offer method which is used by the Executor Service
is overridden and implemented using a put method which blocks. If we do not do this, then the alternatives are either an
exception is thrown or a rejected item handler needs to be defined and the task executed on the current thread. The
first is not acceptable and the second will lead to a multi threading situation where we specify a single thread in the
pool which is not what we want.

`ErrorMap` class

A class used to store the error strings stored as key value pairs.

`ERRORS_BASE` class

A collection of error messages for classes defined in this module.

`ErrorTuple` class

An class representing an error in terms of code, message details and is retryable.

`UnifyException` class

The base class for exceptions hierarchy.

**Package com.aexp.acq.unify.utils.stats**

This package contains classes that can be used to instrument an application. It provides a mechanism to collect the
information for the maximun, minimum and avarage response time for a function over one or more time durations.

**StatsReceiver**

This is the class that receives stat events. Stat is a short form for performance statistics. A `StatsReceiver` object
may contains one or more `StatRecorders`.

**StatsRecorder**

This class represents a stats recording abstraction. A recorder is an entity that is provided stat events to record.
This is an abstract base class which is extended for specific statistic recordings. Each `StatsRecorder` contains
multiple `TimeSlice` objects. Each time slice object specifies information of the time slice and stores the summary of
stats for that time slice.

**ServiceStatsRecorder**

This class extends `StatsRecorder` and is responsible for recording the performance statistics of services across
specified time slices. It is provided events of the type `ServiceStatEvent` to process.

**StatEvent**

This is an abstract base class representing a stat event.

**ServiceStatEvent**

A concrete class representing a stat event for measuring the performance of a service. Each object stores the name of
the service, the timestamp at which the event was created, the response time measured and whether it was a case of an
error or not.

**StatSummary**

This is the abstract base class for representing summary information.

**ServiceStatSummary**

This class is used to store the summary information for a specific service over a time slice. This class stores the
following information for a service for a time slice:

* Minimum response tome
* Maximum response time
* Average response time
* Number of calls made
* Number of error calls
* Total response time

Setting up stats recording for an application requires the following to be done:

*Initialize StatsReceiver*

```java
statsReceiver=new StatsReceiver(int numThreads,int queueSize);
```

`int numThreads` specifies the number of threads that will be used by StatsReceiver to process incoming events. Usually
a single thread should work.

`int queueSize` specifies the size of the queue to hold the events waiting to be processed. Usually a size of 8 to 16
should work

*Register Stat Recorders*

First create a new recorder object.

```java
ServiceStatsRecorder recorder=new ServiceStatsRecorder(int timeSlices,long timeSliceDurationInSec);
        statsReceiver.registerRecorder(recorder);
```

`int timeSlices` specifies the number of time slices to measure in the past

`long timeSliceDurationInSec` specifies the duration of each time slice in seconds

For example, (3, 259200) will mean "create 3 time slices, each of 3 days duration". Using this, the recorder will at any
point of time, maintain 3 time slices, one of which will be current

*Create a Stats Event*

```java
ServiceStatEvent e=new ServiceStatEvent(name,now,time,isError);
```

`String name` is the name of the service

`long now` is the time stamp

`long time` is the response time of the service

`boolean isError` specifies if an error occurred or not

*Send the event to the stats receiver*

```java
statsReceiver.accept(new Task(e));
```

##### What next?

Read the API documentation.

Go through the unit test cases in the source code. Unit test cases are available in the location `src/test`

Provide us feedback. We would love to hear from you. We can be reached here:

##### Author:

Deepak Arora, GitHub: @deepakarora3, Twitter: @DeepakAroraHi

## Contributing

We welcome Your interest in the American Express Open Source Community on Github. Any Contributor to any Open Source
Project managed by the American Express Open Source Community must accept and sign an Agreement indicating agreement to
the terms below. Except for the rights granted in this Agreement to American Express and to recipients of software
distributed by American Express, You reserve all right, title, and interest, if any, in and to Your Contributions.
Please
[fill out the Agreement](https://cla-assistant.io/americanexpress/unify-jdocs).

## License

Any contributions made under this project will be governed by the
[Apache License 2.0](./LICENSE.txt).

## Code of Conduct

This project adheres to the [American Express Community Guidelines](./CODE_OF_CONDUCT.md). By participating, you are
expected to honor these guidelines.
