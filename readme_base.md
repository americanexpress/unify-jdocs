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

##### Author and lead maintainer:

Deepak Arora, deepak.arora5@aexp.com, GitHub: @deepakarora3

##### Other maintainers:

Shamanth B Chandrashekar, shamanth.b.chandrashekar@aexp.com, GitHub: shamanth9

Deepika Sidana, deepika.sidana3@aexp.com, GitHub: deepikasidana89

Jyotsna Gandhi, jyotsna.gandhi1@aexp.com, GitHub: jodube21

Shailendra Bade, shailendra.bade@aexp.com, GitHub: finaspirant

Phil Lundrigan, phil.lundrigan@aexp.com, GitHub: lundriganp

##### Special mention:

A special thanks to Phil Lundrigan for his tremendous leadership, support and encouragement.

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

**Package com.aexp.acq.unify.base.decision_table**

This package implements a decision table functionality using Excel.

The excel based decision tables can be completely used from within an IDE and can be configuration managed (GIT). The
Excel files are stored as resource files just like any other source code artifact, checked in / out of the repository
and version controlled.

The decision table provide the following functionality:

* Passing of input data as a map of key value pairs
* Ability to add columns and rows as required
* "First match" and "All Matches" policies. First match policy returns when the first match is found. All match policy
  evaluates to the last row and returns results corresponding to all matching rows
* "Return None" and "Return Default" no match policies. This policy is applicable in case of no match found. In the case
  of return none, no values are returned while in the case of return default, the contents of the default row are
  returned back
* Standard operators =, <=, >=, <> that work on Boolean, double, integer, long and string data types
* A "Like" operator for string data type using which a cell can be evaluated against multiple values
* Ability to return contents of multiple columns for the matched rows
* For a return cell, ability to specify a function to be executed whose return value will be returned to the caller. To
  use this feature, enter the value of a return cell as "# full class path"
  and define a class which implements the "InvokableCell" interface. For example, enter
  "# com.americanexpress.unify.utils.decision_table.TestInvokableReturn". If the row is matched, then this class will be
  instantiated and execute method called on this class. The execute method will receive the map of input values passed
  to the decision table as well as a custom object which is up to the user of the decision table to define

Below is an example of a decision table implemented in Excel:

![Decision Table](decision_table.png)

A decision table is created as below:

```java
DecisionTable dt=DecisionTable.fromExcel("/base/DTTest1.xlsx");
```

The input values are collected in a map (Map<String, String>) as one or more key value pairs and passed into the
decision table for evaluation as below.

```java
Map<String, String> values=new HashMap<>();
        values.put("score","100");
        values.put("yob","1974");
        values.put("code","4GG");
```

The response of a decision table is a list of key values pairs in the form List<Map<String, RetDTCell> as below:

```java
List<Map<String, RetDTCell>>list=dt.evaluate(values);
        for(Map<String, RetDTCell> map:list){
        map.forEach((colName,dtCell)->System.out.println("Col name -> "+colName+", value -> "+dtCell.getValue()));
        System.out.println();
        }
```

The list can be traversed to get the individual rows returned from the decision table using standard Java for loops.
