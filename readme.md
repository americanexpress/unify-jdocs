## JDocs - A new way of working with JSON documents

---
JDocs (JSON Documents) is a JSON manipulation library.
It completely eliminates the need to have model / POJO classes and instead works directly
on the JSON document. Once you use this library, you may never 
want to return to using Java model classes or using JSON schema for JSON document validation.

---

#### Getting JDocs package

JDocs is available as a jar file in Maven central with the following latest Maven coordinates.

````pom
<groupId>com.americanexpress.unify.jdocs</groupId>
<artifactId>unify-jdocs</artifactId>
<version>1.4.0</version>
````

---

#### Primer on model classes, marshalling and unmarshalling

When JSON is used in applications (either to store data or as a means to exchange data between
applications / systems), the JSON text document is converted into language specific data structures.
For the Java programming language, this consists of Java classes and objects.

For the following JSON:

```json
{
   "first_name": "Deepak",
   "last_name": "Arora",
   "phones": [
     {
     "type": "home",
     "number": "0134578965"
     },
     {
     "type": "mobile",
     "number": "04455678965"
     }
   ]
 }
```

the following Java classes would be created:

```java
public class Person {
  String first_name;
  String last_name;
  Phone[] phones;
}

public class Phone {
  String type;
  String number;
}
```
 
These Java classes created are referred to as model classes. The JSON document is parsed and converted into
Java objects which are then used in the program. The process of converting JSON documents into 
language specific objects is known as ‘unmarshalling" and of converting the language specific objects
back into JSON documents as "marshalling" i.e.

_Marshalling -> Java object to JSON document_

_Unmarshalling -> JSON document to Java object_

---

#### Challenges faced in using model classes to work with JSON documents

In an application program, model classes, marshalling and unmarshalling is extensively used to convert 
JSON into Java objects and vice versa. This approach has the following challenges associated with it:

1. The JSON to Java mapping and vice versa has to be created and maintained. Any time the JSON document
structure changes, these classes must also be updated as also the programs using these classes.
Having model objects creates an additional layer which always needs to be kept in sync with the JSON text
document structure. The complexity is compounded by the fact that JSON documents can be arbitrary levels
deep in which case keeping them in sync becomes ever more challenging. This also tightly couples the JSON
document, model classes and the business logic code making the application difficult to change
2. The problem of code bloat. Typically applications deal with multiple JSON document types.
Each JSON document type may map to multiple Java classes. This situation leads to a plethora of
Java classes and wrapper functions written to access fields in these classes. Over time
this leads to code bloat consisting of numerous Java classes with limited value except for reading and writing JSON
elements. Also, accessing nested fields / arrays may requires multiple lines of code as traversal of
fields needs to be done across levels and null values / absent entries dealt with

The consequences over time of the above are:
1. Inability to carry out a fast, accurate and exhaustive impact analysis. For example, where all in the code base is this json path used?
2. Changing JSON document structure becomes extremely difficult, tedious and error prone. In large code bases,
it becomes next to impossible
3. There is an adverse impact on performance leading to higher usage of system resources
4. The code comprehension and readability suffers leading to maintainability issues
5. Finally deterioration in quality, longer turnaround time for changes, higher efforts and
ultimately higher costs and risks

---

#### How does JDocs address these challenges?

JDocs has been designed to completely do away with model classes and provide advanced features for
manipulating JSON documents directly through JSON paths (with a slight home grown variation).

Doing away with model classes has the following benefits:

1. Reduces the amount of code by ~90% which in turns means significantly faster implementations,
reduced effort, improved quality and faster time to market
2. Helps developers concentrate on implementing business logic rather than spending time and effort on
manipulating model classes to access data
3. Simplifies the way code is written and makes it easily comprehensible.
To know which elements are being accessed, developers no longer need to go through lines and lines of
code that only deal with traversing model classes and have very little to do with business logic
4. Allows for fast, accurate and exhaustive impact analysis across the code base. Developers can in a matter of
seconds locate all usages of a json path across the code base
5. By always referring to data as JSON paths, it allows developers to gain a much better understanding of
the business domain, data and its usage

---

#### JDocs - The Core API

```java
public interface Document {

  /**
   * Returns a boolean specifying if this document is a typed document
   */
  boolean isTyped();

  /**
   * Returns the type of the document
   */
  String getType();

  /**
   * Returns the data type of the leaf node
   */
  DataType getLeafNodeDataType(String path, String... vargs);

  /**
   * Returns the date type of the leaf node
   */
  DataType getArrayValueLeafNodeDataType(String path, String... vargs);

  /**
   * Sets the type of a document. The model object needs to be already loaded. Validation against the model will
   * be carried out an an exception thrown if a violation is found
   */
  void setType(String type);

  /**
   * Sets the type of a document. The model object needs to be already loaded. Validation against the model will
   * be carried out as per the value of the variable validateAtReadWriteOnly and an exception thrown if a violation is found
   */
  void setType(String type, boolean validateAtReadWriteOnly);

  /**
   * Empty the contents of the document
   */
  void empty();

  /**
   * Delete the specified list of paths from the document
   * <p>
   * Throws an exception if the path is not found in the associated model document if document is typed or
   * if the name in name value pair is not of the right type
   *
   * @param pathsToDelete the list of paths
   */
  void deletePaths(List<String> pathsToDelete);

  /**
   * Delete the specified path from the document. Does nothing if the path is not found. The path specified
   * can be any path including pointing to a leaf, complex or an array node
   * <p>
   * Throws an exception if the path is not found in the associated model document
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   */
  void deletePath(String path, String... vargs);

  /**
   * Gets the size of the array at the path specified. The path specified has to be of array type in the json.
   * <p>
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return the size of the array. If the path is not found in the document, a value of 0 is returned
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the path specified does not contain empty square brackets in the end i.e. []
   *                        If the node in the json document corresponding to the path is not of an array type
   */
  int getArraySize(String path, String... vargs);

  /**
   * Gets the index of an element in the array that contains a field equal to the value as specified in the filter
   * criteria
   * <p>
   * Throws an exception
   * If the document is a typed document and the path is not found in the associated model document
   * If the path specified does not end with a filter criteria definition i.e. [field=value]
   * If the node in the json document corresponding to the path is not of an array type
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return the index of the matched element otherwise -1. The index of the first matched element will be returned
   */
  int getArrayIndex(String path, String... vargs);

  /**
   * Gets the value stored in the given path as a Boolean
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return For a base document, the Boolean value stored at the path else null
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified does not point to a leaf node
   */
  Boolean getBoolean(String path, String... vargs);

  /**
   * Gets the value stored in the given path as an Integer
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return For a base document, the Boolean value stored at the path else null
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified does not point to a leaf node
   */
  Integer getInteger(String path, String... vargs);

  /**
   * Gets the value stored in the given path as a String
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return For a base document, the Boolean value stored at the path else null
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified does not point to a leaf node
   */
  String getString(String path, String... vargs);

  /**
   * Gets the value stored in the given path as a Long
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return For a base document, the Boolean value stored at the path else null
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified does not point to a leaf node
   */
  Long getLong(String path, String... vargs);

  /**
   * Gets the value stored in the given path as a BigDecimal
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return For a base document, the BigDecimal value stored at the path else null
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified does not point to a leaf node
   */
  BigDecimal getBigDecimal(String path, String... vargs);

  /**
   * Gets the value stored in the given path as an Object
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return For a base document, the value stored at the path as an object of the type Integer / Long / BigDecimal / String / Boolean else null
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified does not point to a leaf node
   */
  Object getValue(String path, String... vargs);

  /**
   * Gets the value stored in the given array vaalue path as an Object
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return For a base document, the value stored at the path as an object of the type Integer / Long / BigDecimal / String / Boolean else null
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified does not point to a leaf node
   */
  Object getArrayValue(String path, String... vargs);

  /**
   * Gets the value stored in the array element in the given path as a Boolean
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return For a base document, the Boolean value stored at the path else null
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified does not point to a leaf node
   */
  Boolean getArrayValueBoolean(String path, String... vargs);

  /**
   * Gets the value stored in the array element in the given path as an Integer
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return For a base document, the Boolean value stored at the path else null
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified is not of array element type
   */
  Integer getArrayValueInteger(String path, String... vargs);

  /**
   * Gets the value stored in the array element in the given path as a String
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return For a base document, the Boolean value stored at the path else null
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified is not of array element type
   */
  String getArrayValueString(String path, String... vargs);

  /**
   * Gets the value stored in the array element in the given path as a Long
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return For a base document, the Boolean value stored at the path else null
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified is not of array element type
   */
  Long getArrayValueLong(String path, String... vargs);

  /**
   * Gets the value stored in the array element in the given path as a BigDecimal
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return For a base document, the Boolean value stored at the path else null
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified is not of array element type
   */
  BigDecimal getArrayValueBigDecimal(String path, String... vargs);

  /**
   * Get the JSON string for the document in a compressed format
   *
   * @return
   */
  String getJson();

  /**
   * Get the JSON string for the document in a pretty format
   *
   * @return
   */
  String getPrettyPrintJson();

  /**
   * Used to determine if the specified path exists in the document
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return true if the path exists else false
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   */
  boolean pathExists(String path, String... vargs);

   /**
    * Used to determine if the specified path to a leaf node is an array in the document
    *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return true if the path is an array else false
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   */
  boolean isArray(String path, String... vargs);

  /**
   * Get a document from a non leaf node
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return Document
   * @throws UnifyException If path is not found or path is a leaf node
   */
  Document getDocument(String path, String... vargs);

  /**
   * Returns the content of the specified path as a new document
   * The path can only point to a complex object or an array element. It can also point to an array only if
   * the includeFullkPath variable is true
   *
   * @param path                the path in the document from which the contents are to be extracted
   * @param returnTypedDocument only applicable if this document is a JDocument. If true, an instance of JDocument
   *                            is returned else an instance of BaseDocument
   * @param includeFullPath     the returned document is constructed with the full path from root else the path from root is skipped
   * @param vargs               the values to replace the % characters in path
   * @throws UnifyException If the path specified does not point to a complex object
   *                        If the return document is a JDocument but does not conform to the model document
   *                        If path points to an array but the includeFullPath is false
   */
  Document getContent(String path, boolean returnTypedDocument, boolean includeFullPath, String... vargs);

  /**
   * Copies content from document to this document. This functionality will overwrite all content
   * under the to path with the contents of the object in the from path
   *
   * @param fromDoc  the document to copy from
   * @param fromPath the path in the document to copy from (may contain % placeholders)
   * @param toPath   the path in this document to copy to (may contain % placeholders)
   * @param vargs    the values to replace the % characters in from path and to path
   * @throws UnifyException If the from document is a typed document and the path is not found in the associated model document
   *                        If the types of the from and to documents do not match
   *                        If either of the paths point to a leaf node
   *                        If the to document is a typed document and if any of the resulting paths is not present in the model document
   */
  void setContent(Document fromDoc, String fromPath, String toPath, String... vargs);

  /**
   * Set the specified value in the specified path
   *
   * @param path  the path
   * @param value the value to set in the path
   * @param vargs the values to replace the % characters in path
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the path specified does not point to a leaf node
   */
  void setBoolean(String path, boolean value, String... vargs);

  /**
   * Set the specified value in the specified path
   *
   * @param path  the path
   * @param value the value to set in the path
   * @param vargs the values to replace the % characters in path
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the path specified does not point to a leaf node
   */
  void setInteger(String path, int value, String... vargs);

  /**
   * Set the specified value in the specified path
   *
   * @param path  the path
   * @param value the value to set in the path
   * @param vargs the values to replace the % characters in path
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the path specified does not point to a leaf node
   */
  void setLong(String path, long value, String... vargs);

  /**
   * Set the specified value in the specified path
   *
   * @param path  the path
   * @param value the value to set in the path
   * @param vargs the values to replace the % characters in path
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the path specified does not point to a leaf node
   */
  void setBigDecimal(String path, BigDecimal value, String... vargs);

  /**
   * Set the specified value in the specified path
   *
   * @param path  the path
   * @param value the value to set in the path
   * @param vargs the values to replace the % characters in path
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the path specified does not point to a leaf node
   */
  void setString(String path, String value, String... vargs);

  /**
   * Sets the specified value in the specified array element in the given path
   *
   * @param path  the path
   * @param value the value to set
   * @param vargs the values to replace the % characters in path
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified is not of array element type
   */
  void setArrayValueBoolean(String path, boolean value, String... vargs);

  /**
   * Sets the specified value in the specified array element in the given path
   *
   * @param path  the path
   * @param value the value to set
   * @param vargs the values to replace the % characters in path
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified is not of array element type
   */
  void setArrayValueInteger(String path, int value, String... vargs);

  /**
   * Sets the specified value in the specified array element in the given path
   *
   * @param path  the path
   * @param value the value to set
   * @param vargs the values to replace the % characters in path
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified is not of array element type
   */
  void setArrayValueLong(String path, long value, String... vargs);

  /**
   * Sets the specified value in the specified array element in the given path
   *
   * @param path  the path
   * @param value the value to set
   * @param vargs the values to replace the % characters in path
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified is not of array element type
   */
  void setArrayValueBigDecimal(String path, BigDecimal value, String... vargs);

  /**
   * Sets the specified value in the specified array element in the given path
   *
   * @param path  the path
   * @param value the value to set
   * @param vargs the values to replace the % characters in path
   * @throws UnifyException If the document is a typed document and the path is not found in the associated model document
   *                        If the document is a typed document and the type of the path in the model document does not match
   *                        If the type of the value specified in the path in the document does not match
   *                        If the path specified is not of array element type
   */
  void setArrayValueString(String path, String value, String... vargs);

  /**
   * Create a copy of this document
   *
   * @return the new document
   */
  Document deepCopy();

  /**
   * Merge the contents of one document into another. Both the documents need to be typed documents and of the same type.
   * The key field of each array element in the document to be merged must be specified. The deletion of specified paths
   * is carried out prior to the merge of the document fragment
   *
   * @param d             the document fragment that needs to be merged into this document
   * @param pathsToDelete the list of paths to delete from this document before merge
   */
  void merge(Document d, List<String> pathsToDelete);

  /**
   * Get the list of paths existing in the document
   */
  List<String> flatten();

  /**
   * Get the list of paths existing in the document along with the value in each path as a string
   */
  List<PathValue> flattenWithValues();

  /**
   * Compares two documents and return the results in a list. The document on which the method is
   * invoked is assumed to be the left document
   *
   * @param right           the right document to compare
   * @param onlyDifferences specifies if only difference results are to be returned or all
   */
  List<DiffInfo> getDifferences(Document right, boolean onlyDifferences);

  /**
   * Compares two subdocuments and return the results in a list. The document on which the method is
   * invoked is assumed to be the left document
   *
   * @param leftPath        the subdocument inside of the specified path
   * @param right           the right document to compare
   * @param rightPath       the subdocument inside of the specified path
   * @param onlyDifferences specifies if only difference results are to be returned or all
   */
  public List<DiffInfo> getDifferences(String leftPath, Document right, String rightPath, boolean onlyDifferences);

  /**
   * validates a document as per a model and throws an exception if not met
   * All validation errors are collected and returned in the exception
   *
   * @param type the type of the document
   * @throws UnifyException exception containing the list of errors encountered
   */
  public void validate(String type);

  /**
   * checks if the path passed is a leaf node or not
   *
   * @param path  the path
   * @param vargs the values to replace the % characters in path
   * @return true if the path is a leaf node in the document
   * @throws UnifyException if the path is not found in the document
   */
  public boolean isLeafNode(String path, String... vargs);

}

```

---

#### JDocs in Action

Lets start with a sample JSON document as below:
```json
{
  "first_name": "Deepak",
  "last_name": "Arora",
  "is_married": true,
  "number_of_children": 2,
  "home_address": {
    "line_1": "XYZ, Greenway Pkwy, #ABC",
    "zip": "85254"
  }
}
```
 
##### Reading and writing elements

The first step is to get a `Document` by so:

```java
String json = "<contents of the example json file>"; 
Document d = new JDocument(json);
```

Reading and writing can be done using get and set methods of the API:

```java
String s = d.getString("$.first_name"); // will return Deepak
Boolean b = d.getBoolean("$.is_married"); // will return true
Integer i = d.getInteger("$.number_of_children"); // will return 2
s = d.getString("$.home_address.line_1"); // will return "XYZ, Greenway Pkwy, #ABC"
```

Similarly, set methods of the API are used to set values directly in the JSON document.
Let’s say you execute the following commands:

```java
d.setString("$.first_name", "John");
d.setString("$.last_name", "Ryan");
d.setString("$.middle_name", "Smith");
d.setString("$.is_married", false);
d.setInteger("$.number_of_children", 0);
d.setString("$.home_address.zip", "85032");
String s = d.getPrettyPrintJson();
```
 
The value of `s` will now be:
```json
{
  "first_name": "John",
  "middle_name": "Smith",
  "last_name": "Ryan",
  "is_married": false,
  "number_of_children": 0,
  "home_address": {
    "line_1": "XYZ, Greenway Pkwy, #ABC",
    "zip": "85032"
  }
}
```

One could also use the following method to get a compressed JSON document:

```java
String s = d.getJson();
```

See that the data element "middle_name" which was not existing earlier has been created.
The elements which already existed at the path specified have been updated.
You can create any arbitrary path in the document by specifying that path in the API method.
This can be used to create complex objects or objects inside objects.
Consider an empty JSON document on which the following commands are run:

```java
Document d = new JDocument();
d.setString("$.person.first_name", "John");
d.setString("$.person.last_name", "Ryan");
d.setString("$.person.middle_name", "Smith");
d.setString("$.person.address.city", "Phoenix");
String s = d.getPrettyPrintJson();
```

The value of `s` will be:

```json
{
  "person": {
    "first_name": "John",
    "middle_name": "Smith",
    "last_name": "Ryan",
    "address": {
      "city": "Phoenix"
    }
  }
}
```

From the above, note that complex objects person and person.address have automatically been created.

So far so good and you may ask whats so special about this? There are libraries available that
provide reading and writing of elements using JSON paths. Well, now let's start to make things interesting.

---

##### Reading and writing arrays

Consider the following JSON document. Lets refer to it as snippet 1:

```json
{
  "first_name": "Deepak",
  "phones": [
    {
      "type": "Home",
      "number": "123456"
    }
  ]
}
```

```java
Document d = new JDocument(json); // assuming json is a string containing snippet 1
String s = d.getString("$.phones[0].type"); // will return Home
s = getString("$.phones[0].number"); // will return 123456
```

Lets make things more interesting! You could refer to the array index by specifying a selection criteria. A
selection criteria is a simple `field=value` specification inside of the square brackets. It tells JDocs to
look for an element in the `phones` array which has a field by the name of type and whose value is "Home":

```java
d.getString("$.phones[type=Home].type"); // will return Home
d.getString("$.phones[type=Home].number"); // will return 123456
```

A similar construct could be used to set the contents of an array. Consider the following statements:

```java
Document d = new JDocument(json); // assuming json is a string containing snippet 1
d.setString("$.phones[0].number", "222222");
d.setString("$.phones[0].country", "USA");
d.setString("$.phones[1].type", "Cell");
d.setString("$.phones[1].number", "333333");
d.setString("$.phones[1].country", "USA");
String s = d.getPrettyPrintJson();
```

Would result in the following value of `s`:

```json
{
  "first_name": "Deepak",
  "phones": [
    {
      "type": "Home",
      "number": "222222",
      "country": "USA"
    },
    {
      "type": "Cell",
      "number": "333333",
      "country": "USA"
    }
  ]
}
```

Note that a new element has been created in the array. The same effect could also have been achieved by the following:

```java
Document d = new JDocument(json); // assuming json is a string containing snippet 1
d.setString("$.phones[type=Home].number", "222222")
d.setString("$.phones[type=Home].country", "USA")
d.setString("$.phones[type=Cell].number", "333333")
d.setString("$.phones[type=Cell].country", "USA")
String s = d.getPrettyPrintJson();
```

Note that JDocs when it did not find an array element with `type=Cell`, it went ahead and created one.
By default, since the value of the field `type` is not being set explicitly, it assumed the field to be of `String` type.
If you had not wanted that, you could very well have used something like below:
 
```java
Document d = new JDocument(json); // assuming json is a string containing snippet 1
d.setString("$.phones[type=Home].number", "222222")
d.setString("$.phones[type=Home].country", "USA")
d.setInteger("$.phones[type=0].type", 0)
d.setString("$.phones[type=0].number", "333333")
d.setString("$.phones[type=0].country", "USA")
String s = d.getPrettyPrintJson();
```

The above would result in the following value of `s`:

```json
{
  "first_name": "Deepak",
  "phones": [
    {
      "type": "Home",
      "number": "222222",
      "country": "USA"
    },
    {
      "type": 0,
      "number": "333333",
      "country": "USA"
    }
  ]
}
```

Now for some interesting scenarios. You may ask what if I do the following?

```java
Document d = new JDocument(json); // assuming json is a string containing snippet 1
d.setInteger("$.phones[type=Home].type", 0);
String s = d.getPrettyPrintJson();
```

Well, JDocs will try and search for the element with field type having a value Home and will implicitly
handle different data types both for searching and writing. In the case of the above,
even though the field was stored as `String` type, JDocs converted it to a number.
The following will be the value in `s`:

```json
{
  "first_name": "Deepak",
  "phones": [
    {
      "type": 0,
      "number": "123456"
    }
  ]
}
```

Now what if you were to do the following?

```java
Document d = new JDocument(); // creating an empty document
d.setInteger("$.phones[1].type", 0); // specifying array index as 1 without the element at 0 being present
String s = d.getPrettyPrintJson();
```

In this case, JDocs would throw an out of bounds exception as below:

```java
com.americanexpress.unify.jdocs.UnifyException: Array index out of bounds -> phones
``` 

JDocs realized that an element was being attempted to be added at an index which did not have an element
at the previous index and hence disallowed that by throwing an exception. In case an array index is specified,
JDocs will only add an array element if the previous element exists or if the element index has value 0.
If however, a selection criteria of the form of `type=value` was specified, and if no element existed
which had a field named type with the given value, it would then have created an element at the end of the array.

You could use this same notation to create new arrays, create complex objects within arrays,
create arrays within arrays etc. An example of this is below:

```java
Document d = new JDocument(json); // assuming json is a string containing snippet 1
d.setString("$.addresses[0].type", "Home")
d.setString("$.addresses[0].line_1", "Greenway Pkwy")
String s = d.getPrettyPrintJson();
```

Now `s` will have the following value:

```json
{
  "first_name": "Deepak",
  "phones": [
    {
      "type": "Home",
      "number": "123456"
    }
  ],
  "addresses": [
    {
      "type": "Home",
      "line_1": "Greenway Pkwy"
    }
  ]
}
```
Important points to keep in mind about array creation:

In case array indexes are used:
1. In case the index exists, the element in that index will be updated
2. In case the index does not exist and the index value is 0, an array element will be created
3. In case the index does not exist and the index value is greater than 0, an element
will be created only if the index is one greater than the maximum index in the existing array

In case an array selection criteria is used:
1. If an element exists which contains the field and value as specified in the selection criteria,
that element will be updated
2. If no element has a field as specified in the selection criteria, a new array element will
be created with a field set to the value specified in the criteria

---

##### Iterating arrays
Now, specifying array indexes hard coded as above is fine. But in the real world, you do not know the number
of elements that an array may have and so you need a way to find out the number of elements in the
array and to be able to traverse over all the elements, read them and them and maybe set fields in them
as you go along.

Consider the following JSON sample. Lets call it snippet 2.

```json
{
  "first_name": "Deepak",
  "phones": [
    {
      "type": "Home",
      "number": "222222",
      "country": "USA"
    },
    {
      "type": "Cell",
      "number": "333333",
      "country": "USA"
    }
  ]
}
```

You can find the size of an array as below:
```java
Document d = new JDocument(json); // assuming json is a string containing snippet 2
int size = d.getArraySize("$.phones[]"); // will contain the value 2
``` 

Note that in the above, empty square brackets was specified with phones. This is required so as
to inform JDocs that phones is an array in the JSON document.

Now traversing over the array is trivial. But note the technique used closely. You would not want to deviate
from this technique for the following reasons specified below.

CAUTION!!! deviating from this technique will negate one of the most important benefit of JDocs
which is the ability to carry out an exhaustive impact analysis of any JSON path used in the codebase.
Lets see the traversal first. Continuing from the above Java snippet:

```java
Document d = new JDocument(json); // assuming json is a string containing snippet 2
int size = d.getArraySize("$.phones[]"); // will contain the value 2
for (int i = 0; i < size; i++) {
  String index = i + "";
  String type = d.getString("$.phones[%].type", index); 
  String number = d.getString("$.phones[%].number", index); 
  String country = d.getString("$.phones[%].country", index);
  System.out.println(type);
  System.out.println(number);
  System.out.println(country);
}
```

will print:
```java
Home
222222
USA
Cell
333333
USA
```

Setting the elements while traversing is on exactly similar lines as below:

```java
Document d = new JDocument(json); // assuming json is a string containing snippet 2
int size = d.getArraySize("$.phones[]"); // will contain the value 2
for (int i = 0; i < size; i++) {
  String index = i + "";
  d.setString("$.phones[%].type", "Cell", index);
  d.setString("$.phones[%].number", "111111", index);
  d.setString("$.phones[%].country", "USA", index);
}
for (i = 0; i < size; i++) {
  String index = i + "";
  String type = d.getString("$.phones[%].type", index); 
  String number = d.getString("$.phones[%].number", index); 
  String country = d.getString("$.phones[%].country", index);
  System.out.println(type);
  System.out.println(number);
  System.out.println(country);
}
```
will print:
```java
Cell
111111
USA
Cell
111111
USA
```

Now lets discuss why deviating from the above technique is not be a good idea. See the code below:
```java
Document d = new JDocument(json); // assuming json is a string containing snippet 2
int size = d.getArraySize("$.phones[]"); // will contain the value 2
for (int i = 0; i < size; i++) {
  String index = i + "";
  String type = d.getString("$.phones[" + 
                             index + "].type"); 
  System.out.println(type);
}
```

Note that while the above code will work, notice two things:
1. The line containing the JSON path has been split into 2 lines
2. The construction of the JSON path is now split using +

What either of the above points mean is that it is no longer possible in an IDE to be able to use
regular expression searching for a JSON path in the codebase. Earlier, a regular
expression `"\$\.phones\[.*\].type"` could be used to search for all occurrences of this JSON path across
the code base but now that ability is lost. To be able to retain this ability is absolutely of crucial
importance as that is what one needs when something is changed in the JSON structure. One needs to
know where all it is used across the codebase.

Further more is also reduces code comprehension as the the JSON path is no longer visible
as a running continuous string. Unfortunately, this is one thing which cannot be controlled using
automation and the message just has to be drilled across and reviews conducted on code.

Many of the API methods which take JSON path as input use variable lenght string arguments so as to
be able to specify the JSON path as a running continuous string. The % symbol is used between square
brackets which is replaced in sequence by the variable arguments.

---

##### Deleting paths in a document

The API provides a method to delete paths in the document.
The path may be a leaf node or may point to a complex object or an array or an array element.

Consider the following JSON snippet 3:

```json
{
  "first_name": "Deepak",
  "national_ids": {
    "ssn": "2345678",
    "dl_number": "DL1234"
  },
  "phones": [
    {
      "type": "Home",
      "number": "222222",
      "country": "USA"
    },
    {
      "type": "Cell",
      "number": "333333",
      "country": "USA"
    }
  ]
}
```

The method is straightforward as below:

```java
Document d = new JDocument(json); // assuming json is a string containing snippet 3
d.deletePath("$.first_name"); // will delete the first_name field
d.deletePath("$.national_ids"); // will delete the whole complex object national_ids
d.deletePath("$.phones[]"); // will delete the whole phones array
d.deletePath("$.phones[0]"); // will delete the first element of the array
d.deletePath("$.phones[type=Home]"); // will delete that element of the array which has a type field equal to Home value
```

The method also takes variable length argument so as to be able to use it for arrays like so:
```java
Document d = new JDocument(json); // assuming json is a string containing snippet 3
d.deletePath("$.phones[type=%]", "Home"); // will delete that element of the array which has a type field equal to Home value
d.deletePath("$.phones[%]", 0 + ""); // will delete the first element
```

---

##### The concept of Base and Typed Documents

Till now we talked of operations which can be done on a free form JSON document meaning that it is 
possible to read and write pretty much any path we feel like. But what if we wanted to lock down the structure of a JSON
document? Typically we would use something like JSON schema. We thought that there was a far simpler and 
more intuitive way to do the same thing. This is where we now start to talk of Base and Typed documents.

In JDocs, there can be two types of documents. A Base document which we have worked with so far
above and a Typed document. A Base document is one that has a free structure i.e.
any element can be written at any path using the API.
A Typed document is one that has a defined structure that needs to be adhered to.
We define this structure in something called as a Model document.
In other words, a Model document locks the structure of a JSON document to a specific one.
Further, for each leaf element, a Model also defines the constraints in terms of field type, format etc.
Every typed document is associated with a model document.

A model documents is also a JSON document that contains all valid paths for a data document.
Against each leaf node path, it specifies the constraints applicable for that element.

In case of arrays, the model document contains only one element and constraints defined
in this one element are applicable for all elements of the array in the data document. While
it is true that JSON documents can have different data types for the same field across elements, but
if you really think about it, for the purpose of defining a structure, each element really
needs to be of the same type.

Lets understand this using an example. Consider the snippet 4 below:

```json
{
  "first_name": "Deepak",
  "start_date": "28-Jan-2020 21:18:10 America/Phoenix",
  "id": 12345,
  "phones": [
    {
      "type": "Home",
      "number": 111111
    },
    {
      "type": "Cell",
      "number": 222222
    }
  ]
}
```

The model for the above JSON document would be defined as below:

```json
{
  "first_name": "{\"type\":\"string\"}",
  "start_date": "{\"type\":\"date \"format\":\"dd-MM-yyyy HH:mm:ss VV",
  "id": "{\"type\":\"integer\", \"regex\":\".*\"}",
  "phones": [
    {
      "type": "{\"type\":\"string\"}",
      "number": "{\"type\":\"integer\", \"regex\":\".*\"}"
    }
  ]
}
```

---

**Loading model documents**

It is mandatory for a model to be loaded before a typed document of that type is created.
Model documents can be loaded using the following:

```java
JDocument.loadDocumentTypes(type, json);
``` 

`String type` specifies the type of the document
`String json` specifies the content of the model document

---

**Creating typed documents**

```java
Document d = new JDocument("model", null);
```

The above creates an empty document which is tied to a model document named as `model`
(expected to be already loaded as described in the above section).

```java
String s = "..."; // s contains the contents of a JSON document
Document d = new JDocument("model", s);
```

The above creates a typed `JDocument` from an existing JSON document stored in the string `s`.
JDocs, while loading this document, will run the validations on the document against the model
and if the structure / constraints do not match, the appropriate exception will be thrown.

Also, when writes to the document are done using setXXX methods,
the structure and constraints will be validated against the model.
For example, for snippet 4 above, the following calls will succeed as the paths and the
constraints on the elements are all valid:

```java
Document d = new JDocument("model", null);
d.setString("$.first_name", "Deepak1");
d.setString("$.phones[0].type", "Home");
d.setInteger("$.phones[0].number", 333333);
```

Whereas the following will fail:
```java
d.setString("$.first_name1", "Deepak1") // incorrect path
d.setString("$.start_date", "15-Apr-2019") // incorrect date format
d.setString("$.phones[0].number", "111111") // incorrect data type
```

As regards constraints, the validation is done against the specified regular expression. If a match returns true, the
validation is assumed to succeed else an exception is thrown.

The following attributes are implemented as part of specifying a constraint on a path:

S. No. | Field Name | Description | Type | Mandatory?
------ | --------------- | ------------| ---- | ----------
1 | type | Defines the type of the field. Possible values are string, integer, long, decimal, boolean, date | String | Yes
2 | regex | The pattern against which the value will be validated | string | No
3 | null_allowed | Specifies if null is a valid value for the field. If not specified, default is null not allowed. If value is null and allowed, regex will be ignored | boolean | No
4 | ignore_regex_if_empty_string | If not specified, default is false. If specified to true, regex will be ignored if value is empty. Applicable only for string type fields | boolean | No
5 | format | Only applicable for date type. Specification is as per DateTimeFormatter | string | Yes
6 | empty_date_allowed | Only applicable for date type fields. If not specified, default is true. If allowed format check is ignored  | boolean | No

**Validating typed documents**

By default, typed documents are validated when they are created and when any read / write operation is performed on
them. However there may be cases, when we do not want to validate the document at the time of creation but only at the
time of reading and writing. This is a typical scenario in the use of APIs which can return extra blocks and paths which
may not be present in the model document and which we may not be interested in. If we were to validate at the time of
creating the document, the model validations would fail unless we kept the model document in sync with all the changes
happening on the API side. Most of the times this is not possible as the teams are separate and there is no reason that
adding fields to the response which we are not interested in should cause a failure. To take care of this scenario, we
have two alternatives:

1. Use the overloaded JDocument constructor to specify that the validation should only be done while reading / writing
   paths. This way the default behaviour will not change but can be set at a per document level
2. Specify the default behaviour of JDocs to do the validation only while reading / writing paths. This can be set using
   the public static init method while initializing JDocs. Of course this can be overridden as desired by using the
   JDocument overloaded constructor

The two methods are shown below:

```java
public JDocument(String type,String json,boolean validateAtReadWriteOnly);
public static void init(boolean defaultValidateAtReadWriteOnly);
```

---

##### Fetching content from a document

Given a document, it is possible to extract content from it as a new document. Consider the snippet below:

```json
{
  "id": "id",
  "family" : {
    "number_of_members": 2,
    "members": [
      {
        "name": "Deepak Arora",
        "phones": [
          {
            "type": "Home",
            "number": "1111111111"
          },
          {
            "type": "Cell",
            "number": "2222222222"
          }
        ]
      },
      {
        "name": "Nitika Kaushal",
        "phones": [
          {
            "type": "Home",
            "number": "3333333333"
          },
          {
            "type": "Cell",
            "number": "4444444444"
          }
        ]
      }
    ]
  }
}
```

We can extract content like below:

```java
Document d = new JDocument(json); // assuming json is a string containing above snippet
Document d1 = d.getContent("$.family.members[1].phones[0]", false, true);
String s = d1.getPrettyPrintJson();
```

The value of `s` will contain:

```json
{
  "family" : {
    "members": [
      {
        "phones": [
          {
            "type": "Home",
            "number": "3333333333"
          }
        ]
      }
    ]
  }
}
```

Note that the third parameter (`includeFullPath`) is specified as `true`. This means that return the full path in the document. Had we set
that to `false`, the following would have been returned:

```json
{
    "type": "Home",
    "number": "3333333333"
}
```

The path specified in this method has to point to either:
* a complex object
* an array element (which also needs to be a complex object)
* an array (like `phones[]`). In this case, the `includeFullPath` parameter has to be false else the API
will throw an exception

In case the document we are extracting content from is a `TypedDocument`, then we have the option of returning
a `BaseDocument` or a `TypedDocument`. This is specified using the second parameter `returnTypedDocument`. If this
parameter is set to `true` and if the document from which content is being extracted is a `TypedDocument`, then
the return document will also be a `TypedDocument` of the same type. In case the document we are extracting content from
is not a `TypedDocument`, this parameter is ignored.

Of course, if a `TypedDocument` is being returned, its needs to conform to the structure of the model document. In
this situation, if we specify `includeFullPath = false`, it is possible that the returned document when constructed
will not conform to the model document in which case the API will throw an exception. 

As with other methods in the API, the path can contain `%` and the value specified in the last variable arguments parameter.

---

##### Copying content across documents

Given two Jdoc documents, it is possible to copy content from one document to another.
Using this functionality, one can copy complex objects from one document to another.
A complex object means any object in a document that is not a leaf node i.e.
it itself contains objects. A complex node may also be an array or an element of an array.
For reading and writing leaf elements, the get and set API is meant to be used.

Copy contents is also applicable on typed documents, in which case, all "from" paths and their values are
validated against the model and constraints and exceptions thrown in case of errors encountered.

Consider the following as snippet 5:

```json
{
  "first_name": "Deepak",
  "phones": [
    {
      "type": "Home",
      "number": 111111
    }
  ]
}
```

and the following as snippet 6:
```json
{
  "first_name": "Deepak",
  "addresses": [
    {
      "type": "Home",
      "number": 111111
    }
  ]
}
```

The following is am example of copying content:
```java
Document to = new JDocument(json); // assuming json is a string containing snippet 5
Document from = new JDocument(json1); // assuming json1 is a string containing snippet 6
to.setContent(from, "$.addresses[]", "$.addresses[]");
String s = to.getPrettyPrintJson();
```

The string `s` will contain the following:

```json
{
  "first_name": "Deepak",
  "phones": [
    {
      "type": "Home",
      "number": 111111
    }
  ],
  "addresses": [
    {
      "type": "Home",
      "number": 111111
    }
  ]
}
```

---

##### The concept of array keys

For typed documents, JDocs implements a unique concept of array keys.
An array key is a field in the array element which is unique across all elements in the array.
Think of it as an array having this field as its primary key.
Also, this key field is expected to be present in all elements in the array.

Consider the following snippet 6:

```json
{
  "first_name": "Deepak",
  "phones": [
    {
      "phone_type": "Home",
      "number": "222222",
      "country": "USA"
    },
    {
      "phone_type": "Cell",
      "number": "333333",
      "country": "USA"
    }
  ]
}
```

Here, the type field in each of the elements of the phones array can be used as an array key.
This field is defined in the model document using the reserved keyword "jdocs_arr_pk" as below:

```json
{
  "first_name": "{\"type\":\"string\"}",
  "phones": [
    {
      "jdocs_arr_pk": "{\"field\":\"phone_type\"}",
      "phone_type": "{\"type\":\"string\"}",
      "number": "{\"type\":\"string\"}",
      "country": "{\"type\":\"string\"}"
    }
  ]
}
```

Hence, for elements in arrays in typed documents, "jdocs_arr_pk" becomes a reserved value and cannot be used
to define a field name.

The concept of array key fields is used while merging typed documents into one another as
described in the next section. This is one of the most powerful features of JDocs.

---

##### Merging documents

Using this feature, you can merge the contents of one typed document into another. For discussing the
concept of merging, a "from" document and a "to" document is used. Both "from" and "to"
documents need to be typed documents having the same model.

Lets take snippet 6 as the starting point for understanding. Consider the following code:

```java
Document to = new JDocument("model", fromJson); // fromJson contains snippet 6 json
Document from = new JDocument("model", null); // create an empty document of type model
from.setString("$.phones[0].phone_type", "Office");
from.setString("$.phones[0].number", "888888");
from.setString("$.phones[0].country", "USA");
to.merge(from, null);
String s = to.getPrettyPrintJson();
```
A couple of points to note here:
1. The second parameter of `merge` is a variable string argument and can be used to specify paths
which need to be deleted in the to document before the merge is carried out
2. If array elements are being merged, JDocs expects the key field to be specified. This is required
so that JDocs can look up the array element in the to document to merge the from contents into. If no
such array element is found in the to document, a new array element with the specified key is created 

JDocs will throw an exception in case of mismatches encountered during merge, for example,
the data type of the "to" path may not match with that of the "from" path
or the path types may not match i.e. one path may be an array while the other may be an
object or a leaf node. In case any such case is encountered, an exception is thrown and
the operation aborted.

**CAUTION** -> In case of an exception encountered, whatever merges have been carried out into the 
from document will remain so and the document will not be reverted back. Ideally if both
are typed documents, this should not occur. Even then if the application wants
the document to be reverted, it should first create a copy of the to document, try the merge
into that first and then if it succeeds, carry it out in the first document.

This feature of merge is used extensively in scenarios where the master document is given to clients
as a read only document and along with it, an empty document (called a fragment) is provided.
Whatever the client has to update into the master, it writes into the fragement and returns
the fragment to the caller. The caller then merges the fragment into the main master document.
This gives programs the ability to record incremental updates to a document and later build
mechanism to play them back to see how the updates occurred.

---

##### Using @here in model documents

Consider a scenario where there is a loan processing application document and its associated model. Assume that
a service is called to the contents of the application document have to be passed in one of the 
JSON fields of the service request. In such a scenario, when a model document for the service request is created,
the contents of the model document of the application document have to be embedded in it. Imagine if we
had many more such cases and kept embedding application document everywhere as required. This
would lead to unnecessary code bloat and making any change to the application model document
would require changes at all places where the application model document was embedded. To avoid
this, the @here feature is used.

Consider the following application model document:

```json
{
  "first_name": "{\"type\":\"string\"}",
  "last_name": "{\"type\":\"string\"}",
  "phones": [
    {
      "jdocs_arr_pk": "{\"field\":\"phone_type\"}",
      "phone_type": "{\"type\":\"string\"}",
      "number": "{\"type\":\"string\"}",
      "country": "{\"type\":\"string\"}"
    }
  ]
}
```

Now consider that there is a `storeApplication` REST service which persists the contents of the document
to a database. This service has it own request JSON model document and the contents of the application
are present in a field. Without the use of @here feature, the model document for tthe service request
would look like: 

```json
{
  "request_id": "{\"type\":\"string\"}",
  "application": {
    "first_name": "{\"type\":\"string\"}",
    "last_name": "{\"type\":\"string\"}",
    "phones": [
      {
        "jdocs_arr_pk": "{\"field\":\"phone_type\"}",
        "phone_type": "{\"type\":\"string\"}",
        "number": "{\"type\":\"string\"}",
        "country": "{\"type\":\"string\"}"
      }
    ]
  }
}
```

Note that the contents of the first json are embedded in the path "$.application".

Using @here, the model document for the service request can be written as:

```json
{
  "request_id": "{\"type\":\"string\"}",
  "application": {
     "@here": "/common/docs/models/application.json"
  }
}
```

The @here specifies a file path in the resources folder from where the file contents are read and inserted into
the main model document. This eliminates duplication and allows for making the change only at one place.

---

##### Other features

**Searching for an array element**

The below snippet will search the document for an array element in the phones
array where the field type has the value home. Note that in order to use this API, the final element
needs to be an array in which a selection criteria is specified. Also note that the selection criteria
can refer to any field in the element and the index of the match of the first occurrence will be returned. In
case it is required to read further, an iteration as described previously is recommended. 

```java
Document d = new JDocument(s); // s contains a valid JSON string
int i = 0;
int index = d.getArrayIndex("$.applicants[%].phones[type=home]", i + "");
```

**Working with array values**

JSON notation also supports array values as below:

```json
{
  "valid_codes": [
    "V1",
    "V2",
    "V3"
  ]
}
```

JDocs provides a different set of read and write methods to work with such construct. These methods
are similar to getXXX and setXXX methods and are listed below:

```java
  Boolean getArrayValueBoolean(String path, String... vargs);
  Integer getArrayValueInteger(String path, String... vargs);
  String getArrayValueString(String path, String... vargs);
  Long getArrayValueLong(String path, String... vargs);
  BigDecimal getArrayValueBigDecimal(String path, String... vargs);
  
  void setArrayValueBoolean(String path, boolean value, String... vargs);
  void setArrayValueInteger(String path, int value, String... vargs);
  void setArrayValueLong(String path, long value, String... vargs);
  void setArrayValueBigDecimal(String path, BigDecimal value, String... vargs);
  void setArrayValueString(String path, String value, String... vargs);
```

The model document for the above would be defined as:

```json
{
  "valid_codes": [
    "{\"type\":\"string\"}"
  ]
}
```

**Flattening a JSON document**

Consider the following JSON snippet:

```json
{
  "id": "id_1",
  "family": {
    "members": [
      {
        "sex": "male",
        "first_name": "Deepak",
        "last_name": "Arora",
        "number_of_dependents": 3
      }
    ]
  }
}
```

The flatten API of JDocs gives us a list of all the paths present in the document with or without values.
The following gets the list of paths without values: 

```java
Document d = new JDocument(json); // assuming json is a string containing above snippet
List<String> paths = d.flatten();
paths.stream.forEach(s -> System.out.println(s));
```

The above code will print the following:

```text
$.id
$.family.members[0].sex
$.family.members[0].first_name
$.family.members[0].last_name
$.family.members[0].number_of_dependents
```

In case we wanted to get the flattened paths and also the values, we would use the `flattenWithValues` API as below:

```java
Document d = new JDocument(json); // assuming json is a string containing above snippet
List<PathValue> paths = d.flatten();
paths.stream.forEach(pv -> System.out.println(pv.getPath() + ", " + pv.getValue() + ", " + pv.getDataType()));
```

The above code will print the following:

```text
$.id, id_1, string
$.family.members[0].sex, male, string
$.family.members[0].first_name, Deepak, string
$.family.members[0].last_name, Arora, string
$.family.members[0].number_of_dependents, 3, integer
```

**Comparing two JSON documents**

We can use the `getDifferences` API to compare one JSON document with another. Consider the following
JSON documents:

JSON snippet 1:

```json
{
  "id": "id_1",
  "family": {
    "members": [
      {
        "first_name": "Deepak"
      }
    ]
  },
  "cars": [
    {
      "make": "Honda",
      "model": null
    }
  ],
  "vendors": [
    "v1",
    "v2"
  ]
}
```

JSON snippet 2:
```json
{
  "id": "id_2",
  "family": {
    "members": [
      {
        "first_name": "Deepak"
      },
      {
        "first_name": "Nitika"
      }
    ]
  },
  "vendors": [
    "v1",
    "v3"
  ]
}
```

The comparison can be done as below:

```java
Document ld = new JDocument(jsonLeft); // assuming jsonLeft is a string containing above snippet 1
Document rd = new JDocument(jsonRight); // assuming jsonRight is a string containing above snippet 2
List<DiffInfo> diList = ld.getDifferences(rd, true);
String s = "";
for (DiffInfo di : diList) {
  String lpath = (di.getLeft() == null) ? "null" : di.getLeft().getPath();
  String rpath = (di.getRight() == null) ? "null" : di.getRight().getPath();
  s = s + di.getDiffResult() + ", " + lpath + ", " + rpath + "\n";
}
System.out.println(s);
```

The above would print:

```text
DIFFERENT, $.id, $.id
ONLY_IN_LEFT, $.cars[0].make, null
DIFFERENT, $.vendors[1], $.vendors[1]
ONLY_IN_RIGHT, null, $.family.members[1].first_name
````

Of course, if we wanted to also get the values, we could always use `getValue` method of `PathValue`
object returned from the call `di.getLeft()` or `di.getRight`. We could also use the method
`getDataType` in case we wanted to get the data type of the value.

Please note the following regarding comparing JSON documents:
1. JDocs does a logical comparison of the documents. When fields with null values are encountered, they
are treated as being equivalent to the field not being present in the document. In the above example,
in the left document, `$.cars[0].model` has null value while this path is not present in the right document.
JDocs comparison assumes that these are equivalent. In other words a JSON path leaf node having a null values
is assumed to be the same as the path not existing at all.
1. If any one of the documents being compared is a typed document, the data type of the path will be determined
from the model document
 
---

##### What is unique about JDocs?

1. Ability to write data to JSON documents using JSON paths for fields AND complex objects AND arrays
2. A very simple and easy to use feature of binding the structure of a JSON document to a model
which can then be used to enforce the integrity of the structure during read and writes
3. Ability to define regex constraints on fields in the model which are used to
validate the field values at the time of writing
4. Ability to merge documents into one another including merging complex objects and arrays
nested any levels deep
5. Ability to reuse and embed models
6. Ability to copy content from one document to another using JSON paths
7. And most of all freedom from model classes and code bloat

---

##### What next?

Go through the unit test cases in the source code. Unit test cases are available in the location `src/test`

Provide us feedback. We would love to hear from you.

---

##### Author:
Deepak Arora, GitHub: @deepakarora3, Twitter: @DeepakAroraHi

---

## Contributing

We welcome Your interest in the American Express Open Source Community on Github. Any Contributor to
any Open Source Project managed by the American Express Open Source Community must accept and sign
an Agreement indicating agreement to the terms below. Except for the rights granted in this 
Agreement to American Express and to recipients of software distributed by American Express, You
reserve all right, title, and interest, if any, in and to Your Contributions. Please
[fill out the Agreement](https://cla-assistant.io/americanexpress/unify-jdocs).

## License

Any contributions made under this project will be governed by the
[Apache License 2.0](./LICENSE.txt).

## Code of Conduct

This project adheres to the [American Express Community Guidelines](./CODE_OF_CONDUCT.md). By
participating, you are expected to honor these guidelines.
