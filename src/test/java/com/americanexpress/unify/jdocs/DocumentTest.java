/*
 * Copyright 2020 American Express Travel Related Services Company, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.americanexpress.unify.jdocs;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/*
 * @author Deepak Arora
 */
public class DocumentTest {

  @BeforeAll
  private static void setup() {
    ERRORS_JDOCS.load();
  }

  private String getCompressedJson(String filePath) {
    String json = BaseUtils.getResourceAsString(DocumentTest.class, filePath);
    Document d = new BaseDocument(json);
    return d.getJson();
  }

  private Document getBaseDocument(String filePath) {
    String json = BaseUtils.getResourceAsString(DocumentTest.class, filePath);
    return new BaseDocument(json);
  }

  private Document getTypedDocument(String type, String filePath) {
    setDocModel(type);

    String json = null;
    if (filePath == null) {
      json = "{}";
    }
    else {
      json = BaseUtils.getResourceAsString(DocumentTest.class, filePath);
    }

    return new TypedDocument(type, json);
  }

  private void setDocModel(String type) {
    String json = BaseUtils.getResourceAsString(DocumentTest.class, "/jdocs/" + type + ".json");
    Document d = new BaseDocument(json);
    TypedDocument.setDocumentModel(type, d);
  }

  @Test
  void testMisc() {
    setDocModel("sample_1_model");
    String json = BaseUtils.getResourceAsString(DocumentTest.class, "/jdocs/sample_1.json");
    Document d = new TypedDocument("sample_1_model", json);

    d.deletePath("$.members[index=%].first_name", "0");
    assertFalse(d.pathExists("$.members[index=%].first_name", "0"));
    assertEquals(d.getArraySize("$.members[0].phones[]"), 2);

    d = new TypedDocument("sample_1_model");
    assertEquals(d.getPrettyPrintJson(), "{ }");
  }

  @Test
  void testRead() {
    Document d = new ReadOnlyDocument(getTypedDocument("sample_1_model", "/jdocs/sample_1.json"));

    // check string reads
    assertEquals("id", d.getString("$.id"));
    assertEquals("Deepak", d.getString("$.members[0].first_name"));
    assertEquals("Deepak", d.getString("$.members[index=0].first_name"));
    assertEquals("Nitika", d.getString("$.members[index=1].first_name"));
    assertEquals("Arora", d.getString("$.members[sex=male].last_name"));
    assertEquals("9999999999", d.getString("$.members[sex=male].phones[type=mobile].number"));

    // check int and long reads
    assertEquals(new Integer(0), d.getInteger("$.members[0].index"));
    assertEquals(new Long(0), d.getLong("$.members[0].index"));

    // check boolean reads
    assertEquals(new Boolean(true), d.getBoolean("$.members[0].is_married"));
    assertEquals(new Boolean(false), d.getBoolean("$.members[1].is_married"));

    // check null read
    assertEquals(null, d.getString("$.info.iid"));

    // unhappy path scenarios
    UnifyException e = assertThrows(UnifyException.class, () -> {
      d.getString("$.id1");
    });
    assertEquals("jdoc_err_38", e.getErrorCode());

    e = assertThrows(UnifyException.class, () -> {
      d.getString("$.members[2].first_name");
    });
    assertEquals("jdoc_err_8", e.getErrorCode());

    e = assertThrows(UnifyException.class, () -> {
      d.getString("$.err[pid=xxxx].err");
    });
    assertEquals("jdoc_err_38", e.getErrorCode());
  }

  @Test
  void testDouble() {
    Document d = getTypedDocument("sample_8_model", "/jdocs/sample_8.json");
    assertEquals((Double)45.00, d.getDouble("$.values[0].val_1"));
    assertEquals((Double)45.876, d.getDouble("$.values[0].val_2"));

    d = new BaseDocument();
    d.setDouble("$.value", 45.876);
    assertEquals((Double)45.876, d.getDouble("$.value"));

    d.setInteger("$.value", 10);
    assertEquals((Double)10.00, d.getDouble("$.value"));
  }

  @Test
  void testWrite() {
    Document d = new BaseDocument();

    d.setString("$.id", "id");
    d.setString("$.info.iid", "iid");

    d.setString("$.members[0].first_name", "Deepak");
    d.setString("$.members[%].first_name", "Deepak", "0");
    d.setString("$.members[sex=male].first_name", "Deepak");
    d.setString("$.members[sex=male].last_name", "Arora");

    d.setString("$.members[sex=male].phones[type=home].type", "home");
    d.setString("$.members[sex=%].phones[type=%].type", "home", "male", "home");

    d.setString("$.members[0].phones[type=home].number", "9999999999");
    d.setString("$.info.iid", "12345678");
    d.setString("$.addresses[0].line_1", "line 1");
    d.setString("$.addresses[1].line_1", "line 1");
    d.setString("$.addresses[2].line_1", "line 1");
    d.setInteger("$.members[0].index", 0);
    d.setLong("$.members[0].index", 0);
    d.setBoolean("$.members[0].phones[type=home].is_primary", true);
    d.setString("$.members[sex=female].first_name", "Nitika");

    String expected = getCompressedJson("/jdocs/sample_5_expected.json");
    String actual = d.getJson();
    assertEquals(expected, actual);

    // exception scenarios
    UnifyException e = assertThrows(UnifyException.class, () -> {
      d.setString("$.addresses[4].line_1", "line 1");
    });
    assertEquals("jdoc_err_17", e.getErrorCode());
  }

  @Test
  void testWriteTyped() {
    Document d = getTypedDocument("sample_7_model", null);

    d.setString("$.id", "id");
    d.setString("$.info.iid", "iid");
    d.setString("$.members[0].first_name", "Deepak");
    d.setInteger("$.members[0].age", 48);
    d.setString("$.members[type=basic].first_name", "Deepak");
    d.setString("$.members[type=basic].last_name", "Arora");
    d.setString("$.members[type=basic].phones[type=home].type", "Home");
    d.setString("$.members[0].phones[type=Home].number", "11111111111");
    d.setString("$.info.iid", "12345678");
    d.setLong("$.members[0].index", 0);
    d.setBoolean("$.members[0].phones[type=Home].is_primary", true);
    d.setString("$.members[type=supp].first_name", "Nitika");

    String expected = getCompressedJson("/jdocs/sample_7_expected.json");
    String actual = d.getJson();
    assertEquals(expected, actual);

    // exception scenarios
    UnifyException e = assertThrows(UnifyException.class, () -> {
      d.setString("$.addresses[4].line_1", "line 1");
    });
    assertEquals("jdoc_err_38", e.getErrorCode());
  }

  @Test
  void testDeepCopy() {
    Document d = getBaseDocument("/jdocs/sample_1.json");
    String expected = d.getJson();

    Document d1 = d.deepCopy();
    d.empty();
    String actual = d1.getJson();

    assertEquals(expected, actual);
  }

  @Test
  void testDelete() {
    Document d = getBaseDocument("/jdocs/sample_1.json");

    boolean b = d.pathExists("$.members[0].phones[0].number");
    assertEquals(true, b);

    d.deletePath("$.members[0].phones[0].number");
    b = d.pathExists("$.members[0].phones[0].number");
    assertEquals(false, b);

    d.deletePath("$.members[0].phones[0]");
    int size = d.getArraySize("$.members[0].phones[]");
    assertEquals(size, 1);

    d.deletePath("$.members[sex=female]");
    b = d.pathExists("$.members[sex=female]");
    assertEquals(false, b);

    String expected = getCompressedJson("/jdocs/sample_4_expected.json");
    String actual = d.getJson();

    assertEquals(expected, actual);

    // exception scenarios
    UnifyException e = assertThrows(UnifyException.class, () -> {
      d.deletePath("$.members[2]");
    });
    assertEquals("jdoc_err_17", e.getErrorCode());
  }

  @Test
  void testArraySize() {
    Document d = new BaseDocument();

    d.setString("$.members[0].name", "Deepak");
    d.setString("$.members[1].name", "Deepak");
    d.setString("$.members[2].name", "Deepak");
    d.setString("$.members[3].name", "Deepak");
    d.setString("$.members[4].name", "Deepak");
    d.setArrayValueString("$.members[0].phones[0]", "1234");
    d.setArrayValueString("$.members[0].phones[1]", "1234");
    d.setArrayValueString("$.members[1].phones[0]", "1234");

    int size = d.getArraySize("$.members[]");
    assertEquals(size, 5);

    size = d.getArraySize("$.members[0].phones[]");
    assertEquals(size, 2);

    size = d.getArraySize("$.members[1].phones[]");
    assertEquals(size, 1);

    size = d.getArraySize("$.application[]");
    assertEquals(size, 0);
  }

  @Test
  void testNativeArray() {
    Document d = getBaseDocument("/jdocs/native_array.json");
    String s = d.getArrayValueString("$.valid_states[0].states[0]");
    assertEquals(s, "AZ");

    s = d.getArrayValueString("$.valid_states[0].states[2]");
    assertEquals(s, "NY");

    d.setArrayValueString("$.valid_states[0].states[3]", "GA1");
    s = d.getArrayValueString("$.valid_states[0].states[3]");
    assertEquals(s, "GA1");

    d.setArrayValueString("$.valid_states[0].states_1[0]", "AZ1");
    s = d.getArrayValueString("$.valid_states[0].states_1[0]");
    assertEquals(s, "AZ1");

    d.setArrayValueInteger("$.valid_states[0].senators[0]", 20);
    int age = d.getArrayValueInteger("$.valid_states[0].senators[0]");
    assertEquals(age, 20);

    d = getTypedDocument("native_array1_model", "/jdocs/native_array1.json");
    s = d.getArrayValueString("$.codes[0]");
    assertEquals(s, "V1");
    d.setArrayValueString("$.codes[0]", "0");
    s = d.getArrayValueString("$.codes[0]");
    assertEquals(s, "0");
    d.setArrayValueString("$.codes[3]", "3");
    s = d.getArrayValueString("$.codes[3]");
    assertEquals(s, "3");
  }

  @Test
  void testCopy() {
    // test case 1
    Document fromDoc = getBaseDocument("/jdocs/sample_1.json");
    Document toDoc = new BaseDocument();
    toDoc.setContent(fromDoc, "$.info", "$.info");
    toDoc.setContent(fromDoc, "$.members[%]", "$.members[%]", 0 + "", 0 + "");

    try {
      toDoc.setContent(fromDoc, "$.id", "$.id");
    }
    catch (UnifyException e) {
      assertEquals(UnifyException.class, e.getClass());
      assertEquals(e.getErrorCode(), "jdoc_err_22");
    }

    fromDoc.deletePath("$.id");
    fromDoc.deletePath("$.members[1]");
    String expected = fromDoc.getJson();
    String actual = toDoc.getJson();
    assertEquals(expected, actual);

    // test case 2
    fromDoc = getBaseDocument("/jdocs/sample_2.json");
    toDoc = new BaseDocument();
    toDoc.setContent(fromDoc, "$.info", "$");
    expected = getCompressedJson("/jdocs/sample_2_1_expected.json");
    actual = toDoc.getJson();
    assertEquals(expected, actual);

    // test case 3
    fromDoc = getBaseDocument("/jdocs/sample_2.json");
    toDoc = new BaseDocument();
    toDoc.setContent(fromDoc, "$", "$.members[type=basic].block");
    expected = getCompressedJson("/jdocs/sample_2_2_expected.json");
    actual = toDoc.getJson();
    assertEquals(expected, actual);

    // test case 4
    fromDoc = getTypedDocument("sample_6_model", "/jdocs/sample_6.json");
    toDoc = getTypedDocument("sample_6_model", null);
    toDoc.setContent(fromDoc, "$", "$");
    expected = getCompressedJson("/jdocs/sample_6_2_expected.json");
    actual = toDoc.getJson();
    assertEquals(expected, actual);

    // test case 5
    fromDoc = getTypedDocument("sample_6_model", "/jdocs/sample_6.json");
    toDoc = getTypedDocument("sample_6_model", null);
    toDoc.setContent(fromDoc, "$.application.members[%]", "$.application.members[%]", 0 + "", 0 + "");
    expected = getCompressedJson("/jdocs/sample_6_3_expected.json");
    actual = toDoc.getJson();
    assertEquals(expected, actual);

    // test case 6
    fromDoc = getTypedDocument("sample_6_model", "/jdocs/sample_6.json");
    toDoc = getTypedDocument("sample_6_model", null);
    toDoc.setContent(fromDoc, "$.application.members[]", "$.application.members[]");
    expected = getCompressedJson("/jdocs/sample_6_4_expected.json");
    actual = toDoc.getJson();
    assertEquals(expected, actual);
  }

  @Test
  void testArrayIndex() {
    Document d = getTypedDocument("sample_3_model", "/jdocs/sample_3.json");
    assertEquals(d.getArrayIndex("$.members[index=0]"), 0);
    assertEquals(d.getArrayIndex("$.members[type=basic]"), 0);
    assertEquals(d.getArrayIndex("$.members[index=1]"), 1);
    assertEquals(d.getArrayIndex("$.members[type=supp]"), 1);
    assertEquals(d.getArrayIndex("$.members[0].phones[type=home]"), 0);
    assertEquals(d.getArrayIndex("$.members[1].phones[type=mobile]"), 1);
  }

  @Test
  void testDocumentValidation() {
    UnifyException e = assertThrows(UnifyException.class, () -> {
      Document d = getTypedDocument("sample_3_model", "/jdocs/sample_3_err.json");
    });
    assertEquals("jdoc_err_28", e.getErrorCode());
  }

  @Test
  void testMerge() throws IOException {
    Document fromDoc = getTypedDocument("sample_6_model", "/jdocs/sample_6_frag.json");
    Document toDoc = getTypedDocument("sample_6_model", "/jdocs/sample_6.json");
    toDoc.merge(fromDoc, null);
    String expected = getCompressedJson("/jdocs/sample_6_1_expected.json");
    String actual = toDoc.getJson();
    assertEquals(expected, actual);

    fromDoc = getTypedDocument("sample_9_model", "/jdocs/sample_9_frag.json");
    toDoc = getTypedDocument("sample_9_model", "/jdocs/sample_9.json");
    toDoc.merge(fromDoc, null);
    expected = getCompressedJson("/jdocs/sample_9_expected.json");
    actual = toDoc.getJson();
    assertEquals(expected, actual);

    // this tests the merging of value arrays
    fromDoc = getTypedDocument("sample_10_model", "/jdocs/sample_10_frag.json");
    toDoc = getTypedDocument("sample_10_model", "/jdocs/sample_10.json");
    toDoc.merge(fromDoc, null);
    expected = getCompressedJson("/jdocs/sample_10_expected.json");
    actual = toDoc.getJson();
    assertEquals(expected, actual);
  }

  @Test
  void testFilterFields() {
    Document doc = getTypedDocument("sample_12_model", "/jdocs/sample_12.json");
    doc.setString("$.application.members[number=0].name", "Nitika1");
    doc.setString("$.application.members[number=0].contacts.addresses[type=home].line1", "Greenwood City");
    String expected = getCompressedJson("/jdocs/sample_12_expected.json");
    String actual = doc.getJson();
    assertEquals(expected, actual);
  }

  @Test
  void testPathExists() throws IOException {
    Document d = getTypedDocument("sample_1_model", "/jdocs/sample_1.json");
    boolean b = d.pathExists("$.info");
    assertEquals(b, true);
    b = d.pathExists("$.members[0].phones");
    assertEquals(b, true);
    b = d.pathExists("$.members[]");
    assertEquals(b, true);
    b = d.pathExists("$.members");
    assertEquals(b, true);

    d = getBaseDocument("/jdocs/sample_1.json");
    b = d.pathExists("$.members[]");
    assertEquals(b, true);
    b = d.pathExists("$.members");
    assertEquals(b, true);
    b = d.pathExists("$.apdsfgplicants");
    assertEquals(b, false);
  }

  @Test
  void testSetArray() throws IOException {
    Document d = getBaseDocument("/jdocs/sample_11.json");
    Document master = new BaseDocument();
    master.setContent(d, "$.members[0]", "$.members[0]");
    master.setContent(d, "$.members[0]", "$.members[1]");
    master.setContent(d, "$.members[0]", "$.members[2]");
    String expected = getCompressedJson("/jdocs/sample_11_expected.json");
    String actual = master.getJson();
    assertEquals(expected, actual);
  }

  @Test
  void testOverwrite() {
    Document d = new BaseDocument();
    d.setInteger("$.members[number=0].number", 0);
    String expected = getCompressedJson("/jdocs/overwrite_expected.json");
    assertEquals(d.getJson(), expected);

    d = new BaseDocument();
    d.setInteger("$.members[0].number", 0);
    d.setString("$.members[0].first_name", "Deepak");
    expected = getCompressedJson("/jdocs/overwrite_expected_1.json");
    assertEquals(d.getJson(), expected);

    d.setString("$.members[number=0].first_name", "Arora");
    expected = getCompressedJson("/jdocs/overwrite_expected_2.json");
    assertEquals(d.getJson(), expected);

    d.setString("$.members[number=0].number", "1");
    expected = getCompressedJson("/jdocs/overwrite_expected_3.json");
    assertEquals(d.getJson(), expected);

    d.empty();
    d.setString("$.members[0].number", "0");
    d.setString("$.members[0].first_name", "Deepak");
    d.setInteger("$.members[number=0].number", 0);
    expected = getCompressedJson("/jdocs/overwrite_expected_4.json");
    assertEquals(d.getJson(), expected);

    d.empty();
    d.setString("$.phones[type=Home].number", "222222");
    d.setString("$.phones[type=Home].country", "USA");
    d.setInteger("$.phones[type=0].type", 0);
    d.setString("$.phones[type=0].number", "333333");
    d.setString("$.phones[type=0].country", "USA");
    expected = getCompressedJson("/jdocs/overwrite_expected_5.json");
    assertEquals(d.getJson(), expected);
  }

  @Test
  void testRegex() {
    Document d = getTypedDocument("sample_13_model", "/jdocs/sample_13.json");
    assert (d != null);
  }

  @Test
  void test() {
    Document d = getTypedDocument("sample_12_model", null);
    int size = d.getArraySize("$.application.members[]");
    assert (size == 0);
  }

  @Test
  void testGetValue() {
    Document d = getBaseDocument("/jdocs/sample_14.json");

    Object o = d.getValue("$.int");
    assertEquals(o.getClass().getName(), Integer.class.getName());

    o = d.getValue("$.double");
    assertEquals(o.getClass().getName(), Double.class.getName());

    o = d.getValue("$.string");
    assertEquals(o.getClass().getName(), String.class.getName());

    o = d.getValue("$.boolean");
    assertEquals(o.getClass().getName(), Boolean.class.getName());
  }

}
