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

import com.americanexpress.unify.base.BaseUtils;
import com.americanexpress.unify.base.UnifyException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.americanexpress.unify.jdocs.CONSTS_JDOCS.VALIDATION_TYPE.*;
import static org.junit.jupiter.api.Assertions.*;

/*
 * @author Deepak Arora
 */
class DocumentTest {

  @BeforeAll
  static void setup() {
    JDocument.init();
  }

  private String getCompressedJson(String filePath) {
    String json = BaseUtils.getResourceAsString(DocumentTest.class, filePath);
    Document d = new JDocument(json);
    return d.getJson();
  }

  private String getCompressedJson1(String json) {
    Document d = new JDocument(json);
    return d.getJson();
  }

  private Document getBaseDocument(String filePath) {
    String json = BaseUtils.getResourceAsString(DocumentTest.class, filePath);
    return new JDocument(json);
  }

  private Document getTypedDocument(String type, String filePath) {
    return getTypedDocumentHelper(type, filePath, ALL_DATA_PATHS);
  }

  private Document getTypedDocument(String type, String filePath, CONSTS_JDOCS.VALIDATION_TYPE validationType) {
    return getTypedDocumentHelper(type, filePath, validationType);
  }

  private Document getTypedDocumentHelper(String type, String filePath, CONSTS_JDOCS.VALIDATION_TYPE validationType) {
    setDocModel(type);

    String json = null;
    if (filePath == null) {
      json = "{}";
    }
    else {
      json = BaseUtils.getResourceAsString(DocumentTest.class, filePath);
    }

    return new JDocument(type, json, validationType);
  }

  private void setDocModel(String type) {
    String json = BaseUtils.getResourceAsString(DocumentTest.class, "/jdocs/" + type + ".json");
    Document d = new JDocument(json);
    JDocument.setDocumentModel(type, d);
  }

  @Test
  void testMisc() {
    setDocModel("sample_1_model");
    String json = BaseUtils.getResourceAsString(DocumentTest.class, "/jdocs/sample_1.json");
    Document d = new JDocument("sample_1_model", json);

    d.deletePath("$.members[index=%].first_name", "0");
    assertFalse(d.pathExists("$.members[index=%].first_name", "0"));
    assertEquals(2, d.getArraySize("$.members[0].phones[]"));

    d = new JDocument("sample_1_model", null);
    assertEquals("{ }", d.getPrettyPrintJson());

    d = new JDocument();
    d.setString("$.house.family.father_name", "Deepak");
    boolean b = d.pathExists("$.house");
    assertEquals(true, b);

    d = new JDocument();
    d.setString("$.house[0].family.father_name", "Deepak");
    b = d.pathExists("$.house");
    assertEquals(true, b);

    b = d.isArray("$.house");
    assertEquals(true, b);

    b = d.isArray("$.house[]");
    assertEquals(true, b);

    b = d.isArray("$.house[0].family");
    assertEquals(false, b);

    d = new JDocument("[]");
    d.setString("$.[0].name", "Deepak");
    d.setString("$.[1].name", "Nitika");
    Document d1 = new JDocument();
    d1.setContent(d, "$.[]", "$.application[]");

    {
      // this will run ok
      d = new JDocument();
      d.setString("$.pod_info.fname", "Deepak");
      d.setString("$.pod_info.lname", "Arora");
      Document rd = new JDocument("[]");
      rd.setContent(d, "$.pod_info", "$.[0]");
    }
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
    assertEquals(Integer.valueOf(0), d.getInteger("$.members[0].index"));
    assertEquals(Long.valueOf(0), d.getLong("$.members[0].index"));

    // check boolean reads
    assertEquals(Boolean.valueOf(true), d.getBoolean("$.members[0].is_married"));
    assertEquals(Boolean.valueOf(false), d.getBoolean("$.members[1].is_married"));

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
  void testBigDecimal() {
    // do not use new BigDecimal constructor as it gives unpredicable results. Read java documentation
    Document d = getTypedDocument("sample_8_model", "/jdocs/sample_8.json");
    assertEquals(BigDecimal.valueOf(45), d.getBigDecimal("$.values[0].val_1"));

    BigDecimal bd1 = BigDecimal.valueOf(45.876);
    BigDecimal bd2 = d.getBigDecimal("$.values[0].val_2");
    assertEquals(bd1, bd2);

    d = new JDocument();
    d.setBigDecimal("$.value", BigDecimal.valueOf(45.876));
    assertEquals(BigDecimal.valueOf(45.876), d.getBigDecimal("$.value"));

    d.setInteger("$.value", 10);
    assertEquals(BigDecimal.valueOf(10), d.getBigDecimal("$.value"));

    Document d1 = getTypedDocument("sample_8_model", null);
    Document d2 = getTypedDocument("sample_8_model", null);
    d2.setBigDecimal("$.decimal_value", BigDecimal.valueOf(45.876));
    d1.merge(d2, null);

    // test out of bound
    assertThrows(UnifyException.class, () -> {
      Document td = getTypedDocument("sample_8_model", "/jdocs/sample_8.json");
      td.getBigDecimal("$.values[10].val_1");
    });

  }

  @Test
  void testWrite() {
    Document d = new JDocument();

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

    // nothing should happen
    d.deletePath("$.laksdlkj");

    boolean b = d.pathExists("$.members[0].phones[0].number");
    assertEquals(true, b);

    d.deletePath("$.members[0].phones[0].number");
    b = d.pathExists("$.members[0].phones[0].number");
    assertEquals(false, b);

    d.deletePath("$.members[0].phones[0]");
    int size = d.getArraySize("$.members[0].phones[]");
    assertEquals(1, size);

    d.deletePath("$.members[sex=female]");
    b = d.pathExists("$.members[sex=female]");
    assertEquals(false, b);

    String expected = getCompressedJson("/jdocs/sample_4_expected.json");
    String actual = d.getJson();

    assertEquals(expected, actual);

    // nothing should happen
    d.deletePath("$.members[2]");
    d.deletePath("$.members[0].phones[1]");

    // just to show that we can specify [] to denote we are deleting an array block
    d.deletePath("$.members[]");
  }

  @Test
  void testDelete1() {
    Document d = getBaseDocument("/jdocs/sample_1.json");
    d.deletePath("$.members[]");
    d.deletePath("$.id");
    d.deletePath("$.info");
    String expected = "{ }";
    String actual = d.getPrettyPrintJson();
    assertEquals(expected, actual);
  }

  @Test
  void testArraySize() {
    Document d = new JDocument();

    d.setString("$.members[0].name", "Deepak");
    d.setString("$.members[1].name", "Deepak");
    d.setString("$.members[2].name", "Deepak");
    d.setString("$.members[3].name", "Deepak");
    d.setString("$.members[4].name", "Deepak");
    d.setArrayValueString("$.members[0].phones[0]", "1234");
    d.setArrayValueString("$.members[0].phones[1]", "1234");
    d.setArrayValueString("$.members[1].phones[0]", "1234");

    int size = d.getArraySize("$.members[]");
    assertEquals(5, size);

    size = d.getArraySize("$.membasdfsdfers[]");
    assertEquals(0, size);

    size = d.getArraySize("$.members[0].phones[]");
    assertEquals(2, size);

    size = d.getArraySize("$.members[1].phones[]");
    assertEquals(1, size);

    size = d.getArraySize("$.application[]");
    assertEquals(0, size);

    d = getTypedDocument("sample_12_model", null);
    size = d.getArraySize("$.application.members[]");
    assertEquals(0, size);
  }

  @Test
  void testNativeArray() {
    Document d = getBaseDocument("/jdocs/native_array.json");

    int size = d.getArraySize("$.valid_states[0].states[]");
    assertEquals(5, size);

    boolean pathExists = d.pathExists("$.valid_states[0].states[0]");
    assertEquals(true, pathExists);

    pathExists = d.pathExists("$.valid_states[0].states[5]");
    assertEquals(false, pathExists);

    String s = d.getArrayValueString("$.valid_states[0].states[0]");
    assertEquals("AZ", s);

    s = d.getArrayValueString("$.valid_states[0].states[2]");
    assertEquals("NY", s);

    d.setArrayValueString("$.valid_states[0].states[3]", "GA1");
    s = d.getArrayValueString("$.valid_states[0].states[3]");
    assertEquals("GA1", s);

    d.setArrayValueString("$.valid_states[0].states_1[0]", "AZ1");
    s = d.getArrayValueString("$.valid_states[0].states_1[0]");
    assertEquals("AZ1", s);

    d.setArrayValueInteger("$.valid_states[0].senators[0]", 20);
    int age = d.getArrayValueInteger("$.valid_states[0].senators[0]");
    assertEquals(20, age);

    d = getTypedDocument("native_array1_model", "/jdocs/native_array1.json");
    s = d.getArrayValueString("$.codes[0]");
    assertEquals("V1", s);
    d.setArrayValueString("$.codes[0]", "0");
    s = d.getArrayValueString("$.codes[0]");
    assertEquals("0", s);
    d.setArrayValueString("$.codes[3]", "3");
    s = d.getArrayValueString("$.codes[3]");
    assertEquals("3", s);
  }

  @Test
  void testNativeArrayValue() {
    Document d = getBaseDocument("/jdocs/native_array2.json");
    String s = (String)d.getArrayValue("$.valid_states[0].states[0]");
    assertEquals("AZ", s);

    Integer code = (Integer)d.getArrayValue("$.valid_states[0].codes[0]");
    assertEquals(code, Integer.valueOf(1));
  }

  @Test
  void testCopy() {
    // test case 1
    Document fromDoc = getBaseDocument("/jdocs/sample_1.json");
    Document toDoc = new JDocument();
    toDoc.setContent(fromDoc, "$.info", "$.info");
    toDoc.setContent(fromDoc, "$.members[%]", "$.members[%]", 0 + "", 0 + "");

    try {
      toDoc.setContent(fromDoc, "$.id", "$.id");
    }
    catch (UnifyException e) {
      assertEquals(UnifyException.class, e.getClass());
      assertEquals("jdoc_err_22", e.getErrorCode());
    }

    fromDoc.deletePath("$.id");
    fromDoc.deletePath("$.members[1]");
    String expected = fromDoc.getJson();
    String actual = toDoc.getJson();
    assertEquals(expected, actual);

    // test case 2
    fromDoc = getBaseDocument("/jdocs/sample_2.json");
    toDoc = new JDocument();
    toDoc.setContent(fromDoc, "$.info", "$");
    expected = getCompressedJson("/jdocs/sample_2_1_expected.json");
    actual = toDoc.getJson();
    assertEquals(expected, actual);

    // test case 3
    fromDoc = getBaseDocument("/jdocs/sample_2.json");
    toDoc = new JDocument();
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

    // test case 7
    fromDoc = getTypedDocument("sample_6_model", null);
    fromDoc.setInteger("$.application.members[0].phones[0].docs[0].index", 0);
    fromDoc.setString("$.application.members[0].phones[0].docs[0].name", "Deepak");
    toDoc = getTypedDocument("sample_6_model", null);
    toDoc.setContent(fromDoc, "$.application.members[0].phones[0].docs[%]", "$.application.members[%].phones[%].docs[%]", 0 + "", 0 + "", 0 + "", 0 + "");
    expected = getCompressedJson("/jdocs/sample_6_5_expected.json");
    actual = toDoc.getJson();
    assertEquals(expected, actual);

    // test case 8 - we just check that it does not throw an exception
    fromDoc = getBaseDocument("/jdocs/sample_25.json");
    toDoc = new JDocument("[]");
    toDoc.setContent(fromDoc, "$.addresses[]", "$.[]");

    toDoc = new JDocument("[]");
    toDoc.setContent(fromDoc, "$.addresses", "$.[]");

    toDoc = new JDocument();
    toDoc.setContent(fromDoc, "$.addresses", "$.addresses[]");

    try {
      toDoc = new JDocument();
      toDoc.setContent(fromDoc, "$.addresses", "$.addresses");
    }
    catch (UnifyException e) {
      assertEquals(UnifyException.class, e.getClass());
    }
    assertTrue(true);
  }

  @Test
  void testArrayIndex() {
    Document d = getTypedDocument("sample_3_model", "/jdocs/sample_3.json");
    assertEquals(0, d.getArrayIndex("$.members[index=0]"));
    assertEquals(0, d.getArrayIndex("$.members[type=basic]"));
    assertEquals(1, d.getArrayIndex("$.members[index=1]"));
    assertEquals(1, d.getArrayIndex("$.members[type=supp]"));
    assertEquals(0, d.getArrayIndex("$.members[0].phones[type=home]"));
    assertEquals(1, d.getArrayIndex("$.members[1].phones[type=mobile]"));
  }

  @Test
  void testDocumentValidation() {
    UnifyException e = assertThrows(UnifyException.class, () -> {
      getTypedDocument("sample_3_model", "/jdocs/sample_3_err.json");
    });
    assertEquals("jdoc_err_28", e.getErrorCode());
  }

  @Test
  void testMerge() {
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
  void testPathExists() {
    Document d = getTypedDocument("sample_1_model", "/jdocs/sample_1.json");
    boolean b = d.pathExists("$.info");
    assertEquals(true, b);
    b = d.pathExists("$.members[0].phones");
    assertEquals(true, b);
    b = d.pathExists("$.members[]");
    assertEquals(true, b);
    b = d.pathExists("$.members");
    assertEquals(true, b);

    d = getBaseDocument("/jdocs/sample_1.json");
    b = d.pathExists("$.members[]");
    assertEquals(true, b);
    b = d.pathExists("$.members");
    assertEquals(true, b);
    b = d.pathExists("$.apdsfgplicants");
    assertEquals(false, b);

    b = d.pathExists("$.members[3].phones");
    assertEquals(false, b);
    b = d.pathExists("$.members[0].laskdjfh");
    assertEquals(false, b);
  }

  @Test
  void testSetArray() {
    Document d = getBaseDocument("/jdocs/sample_11.json");
    Document primary = new JDocument();
    primary.setContent(d, "$.members[0]", "$.members[0]");
    primary.setContent(d, "$.members[0]", "$.members[1]");
    primary.setContent(d, "$.members[0]", "$.members[2]");
    String expected = getCompressedJson("/jdocs/sample_11_expected.json");
    String actual = primary.getJson();
    assertEquals(expected, actual);
  }

  @Test
  void testSetArrayNull() {
    Document d = getTypedDocument("sample_17_model", null);
    d.setString("$.version", null);
    d.setArrayValueString("$.names[0]", null);
    d.setArrayValueString("$.names[1]", null);
    d.setArrayValueString("$.names[2]", "deepak");

    d = new JDocument();
    d.setString("$.version", null);
    assertTrue(true);
  }

  @Test
  void testOverwrite() {
    Document d = new JDocument();
    d.setInteger("$.members[number=0].number", 0);
    String expected = getCompressedJson("/jdocs/overwrite_expected.json");
    assertEquals(d.getJson(), expected);

    d = new JDocument();
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
    getTypedDocument("sample_13_model", "/jdocs/sample_13.json");
    assertTrue(true);
  }

  @Test
  void testGetValue() {
    Document d = getBaseDocument("/jdocs/sample_14.json");

    Object o = d.getValue("$.int");
    assertEquals(o.getClass().getName(), Integer.class.getName());

    o = d.getValue("$.decimal");
    assertEquals(o.getClass().getName(), BigDecimal.class.getName());

    o = d.getValue("$.string");
    assertEquals(o.getClass().getName(), String.class.getName());

    o = d.getValue("$.boolean");
    assertEquals(o.getClass().getName(), Boolean.class.getName());
  }

  @Test
  void testGetContent() {
    JDocument d = (JDocument)getBaseDocument("/jdocs/sample_15.json");
    String json = null;

    // read a complex object
    json = d.getContent("$.header.transaction", false, true).getJson();
    assertEquals(json, getCompressedJson1("{\n" +
                                                  "  \"header\" : {\n" +
                                                  "    \"transaction\" : {\n" +
                                                  "      \"tid\" : \"tid\"\n" +
                                                  "    }\n" +
                                                  "  }\n" +
                                                  "}"));

    // read the whole document
    json = d.getContent("$", false, true).getJson();
    assertEquals(json, d.getJson());

    // read a complex object inside of an array
    json = d.getContent("$.family.members[1].home_address", false, true).getJson();
    assertEquals(json, getCompressedJson1("{\n" +
                                                  "  \"family\" : {\n" +
                                                  "    \"members\" : [ {\n" +
                                                  "      \"home_address\" : {\n" +
                                                  "        \"line_1\" : \"Nitika address line 1\",\n" +
                                                  "        \"line_2\" : \"Nitika address line 2\"\n" +
                                                  "      }\n" +
                                                  "    } ]\n" +
                                                  "  }\n" +
                                                  "}"));

    // read an array element
    json = d.getContent("$.family.members[0].phones[1]", false, true).getJson();
    assertEquals(json, getCompressedJson1("{\n" +
                                                  "  \"family\" : {\n" +
                                                  "    \"members\" : [ {\n" +
                                                  "      \"phones\" : [ {\n" +
                                                  "        \"type\" : \"mobile\",\n" +
                                                  "        \"number\" : \"2222222222\"\n" +
                                                  "      } ]\n" +
                                                  "    } ]\n" +
                                                  "  }\n" +
                                                  "}"));

    // read the whole array
    json = d.getContent("$.family.members[0].phones[]", false, true).getJson();
    assertEquals(json, getCompressedJson1("{\n" +
                                                  "  \"family\" : {\n" +
                                                  "    \"members\" : [ {\n" +
                                                  "      \"phones\" : [ {\n" +
                                                  "        \"type\" : \"home\",\n" +
                                                  "        \"number\" : \"1111111111\"\n" +
                                                  "      }, {\n" +
                                                  "        \"type\" : \"mobile\",\n" +
                                                  "        \"number\" : \"2222222222\"\n" +
                                                  "      } ]\n" +
                                                  "    } ]\n" +
                                                  "  }\n" +
                                                  "}"));

    // read whole array but without full path -> note that this is not allowed
    try {
      d.getContent("$.family.members[0].phones[]", false, false).getPrettyPrintJson();
    }
    catch (UnifyException e) {
      assertEquals("jdoc_err_24", e.getErrorCode());
    }

    // now we do tests on typed document
    d = (JDocument)getTypedDocument("sample_1_model", "/jdocs/sample_1.json");

    // read a complex object
    json = d.getContent("$.info", true, true).getJson();
    assertEquals(json, getCompressedJson1("{\n" +
                                                  "  \"info\" : {\n" +
                                                  "    \"iid\" : null\n" +
                                                  "  }\n" +
                                                  "}\n"));

    // read a complex object inside of an array
    json = d.getContent("$.members[0].phones[0]", true, true).getJson();
    assertEquals(json, getCompressedJson1("{\n" +
                                                  "  \"members\" : [ {\n" +
                                                  "    \"phones\" : [ {\n" +
                                                  "      \"type\" : \"home\",\n" +
                                                  "      \"number\" : \"11111111111\"\n" +
                                                  "    } ]\n" +
                                                  "  } ]\n" +
                                                  "}\n"));

    // read a complex object inside of an array without full path -> note this is not allowed
    try {
      d.getContent("$.members[0].phones[0]", true, false).getJson();
    }
    catch (UnifyException e) {
      assertEquals("jdoc_err_28", e.getErrorCode());
    }

  }

  @Test
  void testGetLeafNodeDataType() {
    // now we do tests on typed document
    Document d = getTypedDocument("sample_1_model", "/jdocs/sample_1.json");

    DataType ldt = d.getLeafNodeDataType("$.members[0].first_name");
    assertEquals("string", ldt.toString());

    ldt = d.getLeafNodeDataType("$.members[0].is_married");
    assertEquals("boolean", ldt.toString());
  }

  @Test
  void testDate() {
    Document d = getTypedDocument("sample_18_model", null);

    assertThrows(UnifyException.class, () -> {
      d.setString("$.ts", "1981-Feb-29");
    });

    d.setString("$.ts", "");
    assertEquals("", d.getString("$.ts"));
  }

  @Test
  void testEmptyArray() {
    Document d = getTypedDocument("sample_18_model", "/jdocs/sample_18_1.json");
    Integer number = d.getInteger("$.addresses[0].number");
    assertEquals(null, number);
  }

  @Test
  void testValidateAtReadWriteOnly() {
    Document d = null;

    try {
      getTypedDocument("sample_23_model", "/jdocs/sample_23.json");
      fail();
    }
    catch (Exception e) {
    }

    d = getTypedDocument("sample_23_model", "/jdocs/sample_23.json", ONLY_AT_READ_WRITE);

    try {
      d.getString("$.phone_cell");
      fail();
    }
    catch (Exception e) {
    }

    try {
      d.getString("$.giberish");
      fail();
    }
    catch (Exception e) {
    }

    String s = d.getString("$.first_name");
    assertEquals("Deepak", s);

    d = getBaseDocument("/jdocs/sample_23.json");

    s = d.getString("$.giberish");
    assertEquals(null, s);

    s = d.getString("$.last_name");
    assertEquals("Arora", s);

    s = d.getString("$.phone_home");
    assertEquals("1234", s);

    try {
      d.validateAllPaths("sample_23_model");
      fail();
    }
    catch (Exception e) {
    }

    try {
      d.setType("sample_23_model");
      fail();
    }
    catch (Exception e) {
    }

    d.setType("sample_23_model", CONSTS_JDOCS.VALIDATION_TYPE.ONLY_AT_READ_WRITE);

    try {
      d.validateAllPaths("sample_23_model");
      fail();
    }
    catch (Exception e) {
    }

    // we now set the default to be true
    JDocument.init(CONSTS_JDOCS.VALIDATION_TYPE.ONLY_AT_READ_WRITE);

    d = getBaseDocument("/jdocs/sample_23.json");
    d.setType("sample_23_model"); // will not validate and hence no exception thrown
    try {
      d.validateAllPaths("sample_23_model"); // will fail validation here
      fail();
    }
    catch (Exception e) {
    }

    try {
      getTypedDocument("sample_23_model", "/jdocs/sample_23.json"); // will fail validation here
      fail();
    }
    catch (Exception e) {
    }

    getTypedDocument("sample_23_model", "/jdocs/sample_23.json", ONLY_AT_READ_WRITE); // this should pass

    // restore it back
    JDocument.init(CONSTS_JDOCS.VALIDATION_TYPE.ALL_DATA_PATHS);
  }

  @Test
  void testRootArray() {
    Document d = getBaseDocument("/jdocs/sample_20.json");
    int size = d.getArraySize("$.[]");
    assertEquals(2, size);

    d = getTypedDocument("sample_20_model", "/jdocs/sample_20.json");
    size = d.getArraySize("$.[]");
    assertEquals(2, size);

    String s = d.getString("$.[0].address.line_1");
    assertEquals("Happening road", s);

    s = d.getString("$.[0].cars[0].model");
    assertEquals("Accord", s);

    s = d.getString("$.[0].cars[1].model");
    assertEquals("Camry", s);

    s = d.getString("$.[1].cars[1].model");
    assertNull(s);

    d = new JDocument("{\n" +
                              "  \"response\": \"\",\n" +
                              "  \"status\": 500\n" +
                              "}");
    boolean b = d.pathExists("$.[0].raw_response");
    assertFalse(b);
  }

  @Test
  void testRootNativeArray() {
    Document d = getBaseDocument("/jdocs/sample_21.json");
    int size = d.getArraySize("$.[]");
    assertEquals(3, size);

    d = getTypedDocument("sample_21_model", "/jdocs/sample_21.json");
    size = d.getArraySize("$.[]");
    assertEquals(3, size);

    String s = d.getArrayValueString("$.[0]");
    assertEquals("code_1", s);

    s = d.getArrayValueString("$.[1]");
    assertEquals("code_2", s);

    assertThrows(UnifyException.class, () -> {
      Document td = getTypedDocument("sample_21_model", "/jdocs/sample_21.json");
      td.getArrayValueString("$.[6]");
    });
  }

  @Test
  void testErrorCollection() {
    String expected = BaseUtils.getResourceAsString(DocumentTest.class, "/jdocs/sample_19_expected.txt");
    expected = BaseUtils.getWithoutCarriageReturn(expected);
    try {
      getTypedDocument("sample_19_model", "/jdocs/sample_19.json");
    }
    catch (UnifyException e) {
      String actual = BaseUtils.getWithoutCarriageReturn(e.getMessage());
      assertEquals(actual, expected);
    }
  }

  @Test
  void testGetDifferences() {
    JDocument ld = (JDocument)getBaseDocument("/jdocs/sample_16_1.json");
    JDocument rd = (JDocument)getBaseDocument("/jdocs/sample_16_2.json");
    List<DiffInfo> diList = ld.getDifferences(rd, true);
    String s = "";
    for (DiffInfo di : diList) {
      s = s + printDiffInfo(di);
    }

    s = BaseUtils.getWithoutCarriageReturn(s);
    String expected = BaseUtils.getResourceAsString(DocumentTest.class, "/jdocs/sample_16_expected.txt");
    expected = BaseUtils.getWithoutCarriageReturn(expected);
    assertEquals(s, expected);
  }

  private String printDiffInfo(DiffInfo di) {
    PathValue lpv = di.getLeft();
    PathValue rpv = di.getRight();
    String lpath = (lpv == null) ? null : lpv.getPath();
    String rpath = (rpv == null) ? null : rpv.getPath();
    Object lval = (lpv == null) ? null : lpv.getValue();
    Object rval = (rpv == null) ? null : rpv.getValue();
    String s = "Result = " + di.getDiffResult() +
            ", lpath = " + lpath + " (" + lval + ")" +
            ", rpath = " + rpath + " (" + rval + ")";
    s = s + "\n";
    return s;
  }

  @Test
  void testGetDifferences1() {
    String left = "{\n" +
            "  \"id\": \"id_1\",\n" +
            "  \"family\": {\n" +
            "    \"members\": [\n" +
            "      {\n" +
            "        \"first_name\": \"Deepak\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"cars\": [\n" +
            "    {\n" +
            "      \"make\": \"Honda\",\n" +
            "      \"model\": null\n" +
            "    }\n" +
            "  ],\n" +
            "  \"vendors\": [\n" +
            "    \"v1\",\n" +
            "    \"v2\"\n" +
            "  ]\n" +
            "}\n";
    JDocument ld = new JDocument(left);

    String right = "{\n" +
            "  \"id\": \"id_2\",\n" +
            "  \"family\": {\n" +
            "    \"members\": [\n" +
            "      {\n" +
            "        \"first_name\": \"Deepak\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"first_name\": \"Nitika\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"vendors\": [\n" +
            "    \"v1\",\n" +
            "    \"v3\"\n" +
            "  ]\n" +
            "}\n";
    JDocument rd = new JDocument(right);

    List<DiffInfo> diList = ld.getDifferences(rd, true);
    String s = "";
    for (DiffInfo di : diList) {
      String lpath = (di.getLeft() == null) ? "null" : di.getLeft().getPath();
      String rpath = (di.getRight() == null) ? "null" : di.getRight().getPath();
      s = s + di.getDiffResult() + ", " + lpath + ", " + rpath + "\n";
    }

    String expected = "DIFFERENT, $.id, $.id\n" +
            "ONLY_IN_LEFT, $.cars[0].make, null\n" +
            "DIFFERENT, $.vendors[1], $.vendors[1]\n" +
            "ONLY_IN_RIGHT, null, $.family.members[1].first_name\n";
    assertEquals(s, expected);
  }

  @Test
  void testFlatten() {
    JDocument d = (JDocument)getBaseDocument("/jdocs/native_array.json");
    List<String> list = d.flatten();
    String s = "";
    String expected = "$.valid_states[0].country\n" +
            "$.valid_states[0].states[0]\n" +
            "$.valid_states[0].states[1]\n" +
            "$.valid_states[0].states[2]\n" +
            "$.valid_states[0].states[3]\n" +
            "$.valid_states[0].states[4]\n";

    for (String path : list) {
      s = s + path + "\n";
    }

    assertEquals(s, expected);
  }

  @Test
  void testFlattenWithValues() {
    JDocument d = (JDocument)getBaseDocument("/jdocs/native_array.json");
    List<PathValue> list = d.flattenWithValues();
    String s = "";
    String expected = "$.valid_states[0].country, USA, string\n" +
            "$.valid_states[0].states[0], AZ, string\n" +
            "$.valid_states[0].states[1], NJ, string\n" +
            "$.valid_states[0].states[2], NY, string\n" +
            "$.valid_states[0].states[3], GA, string\n" +
            "$.valid_states[0].states[4], TX, string\n";

    for (PathValue pv : list) {
      s = s + pv.getPath() + ", " + pv.getValue() + ", " + pv.getDataType() + "\n";
    }

    assertEquals(s, expected);
  }

  @Test
  void testMergeKeyBeingObject() {
    Document d = getTypedDocument("sample_22_model", "/jdocs/sample_22.json");
    Document frag = getTypedDocument("sample_22_model", "/jdocs/sample_22_frag.json");

    assertThrows(UnifyException.class, () -> {
      d.merge(frag, null);
    });
  }

  @Test
  void testIsLeafNode() {
    Document d = getTypedDocument("sample_22_model", "/jdocs/sample_22.json");

    boolean b = d.isLeafNode("$.addresses[0].block.field1");
    assertEquals(true, b);

    b = d.isLeafNode("$.addresses[0].block");
    assertEquals(false, b);

    b = d.isLeafNode("$.addresses[0]");
    assertEquals(false, b);

    b = d.isLeafNode("$.addresses");
    assertEquals(false, b);

    b = d.isLeafNode("$.addresses[0].block");
    assertEquals(false, b);

    assertThrows(UnifyException.class, () -> {
      d.isLeafNode("$.addresses12345[0].block");
    });

  }

  @Test
  void testMergeDelete() {
    Document d = getTypedDocument("sample_24_model", "/jdocs/sample_24.json");
    Document appFrag = new JDocument("sample_24_model", null);
    List<String> list = new ArrayList<>();
    boolean b;

    // check path not in model
    list.add("$.members[2].salkdf[5].phones");
    try {
      b = true;
      d.merge(appFrag, list);
    }
    catch (UnifyException e) {
      b = false;
    }
    assertFalse(b);

    // index out of bound
    list.clear();
    d = getTypedDocument("sample_24_model", "/jdocs/sample_24.json");
    list.add("$.members[2]");
    d.merge(appFrag, list);
    list.clear();
    d = getTypedDocument("sample_24_model", "/jdocs/sample_24.json");

    // name value mismatch
    list.add("$.members[index=deepak].index");
    try {
      b = true;
      d.merge(appFrag, list);
    }
    catch (UnifyException e) {
      b = false;
    }
    assertFalse(b);

    list.clear();
    d = getTypedDocument("sample_24_model", "/jdocs/sample_24.json");
    list.add("$.members[type=basic].phones[type=home].number");
    d.merge(appFrag, list);
    assertFalse(d.pathExists("$.members[type=basic].phones[type=home].number"));

    list.clear();
    d = getTypedDocument("sample_24_model", "/jdocs/sample_24.json");
    list.add("$.members[0].phones[type=mobile].number");
    d.merge(appFrag, list);
    assertFalse(d.pathExists("$.members[0].phones[type=mobile].number"));

    list.clear();
    d = getTypedDocument("sample_24_model", "/jdocs/sample_24.json");
    list.add("$.members[0].phones[type=mobile]");
    d.merge(appFrag, list);
    assertFalse(d.pathExists("$.members[0].phones[type=mobile]"));
    assertFalse(d.pathExists("$.members[0].phones[1]"));

    list.clear();
    d = getTypedDocument("sample_24_model", "/jdocs/sample_24.json");
    list.add("$.members[type=supp].phones[type=mobile]");
    d.merge(appFrag, list);
    assertFalse(d.pathExists("$.members[1].phones[type=mobile]"));
    assertFalse(d.pathExists("$.members[1].phones[1]"));

    list.clear();
    d = getTypedDocument("sample_24_model", "/jdocs/sample_24.json");
    list.add("$.members[first_name=Chintu]");
    list.add("$.members[0]");
    list.add("$.members[0]");
    list.add("$.members[3]");
    list.add("$.members[first_name=Chini].phones[type=home].number");
    list.add("$.members[2].phones[type=mobile].number");
    d.merge(appFrag, list);
    assertFalse(d.pathExists("$.members[first_name=Chintu].phones[type=mobile]"));
    assertFalse(d.pathExists("$.members[first_name=jenny].phones[1]"));
    assertFalse(d.pathExists("$.members[2].phones[1]"));
    list.add("$.members[first_name=Jenny].phones[type=mobile].number");
    list.add("$.members[first_name=Chini].phones[type=home].number");

    list.clear();
    d = getTypedDocument("sample_24_model", "/jdocs/sample_24.json");
    list.add("$.members[first_name=ufiedoep].phones[1]");
    list.add("$.members[3].phones[0]");
    d.merge(appFrag, list);
    assertFalse(d.pathExists("$.members[3].phones[1]"));

    list.clear();
    d = getTypedDocument("sample_24_model", "/jdocs/sample_24.json");
    list.add("$.members[0].phones[]");
    d.merge(appFrag, list);

    list.clear();
    d = getTypedDocument("sample_24_model", "/jdocs/sample_24.json");
    list.add("$.members[0].last_name");
    d.merge(appFrag, list);
    assertFalse(d.pathExists("$.members[0].last_name"));
    list.clear();
    getTypedDocument("sample_24_model", "/jdocs/sample_24.json");
  }

  @Test
  void testIgnoreRegexIfEmpty() {
    setDocModel("sample_26_model");
    Document d = new JDocument("sample_26_model", null);

    d.setString("$.id1", "GO2");

    assertThrows(UnifyException.class, () -> {
      d.setString("$.id1", "");
    });

    d.setString("$.id2", "");

    d.setString("$.id2", "GNA");

    assertThrows(UnifyException.class, () -> {
      d.setString("$.id2", "hhh");
    });

    d.setString("$.id3", "2023-Jan-26");

    d.setString("$.id3", "");

    assertThrows(UnifyException.class, () -> {
      d.setString("$.id4", "");
    });
  }

  @Test
  void testFieldValidationInGetMethods() {
    setDocModel("sample_26_model");

    Document d = new JDocument();
    d.setString("$.id1", "GO2");
    d.setType("sample_26_model", CONSTS_JDOCS.VALIDATION_TYPE.ONLY_AT_READ_WRITE);
    d.getString("$.id1");

    d = new JDocument();
    d.setString("$.id1", "giberish");
    d.setType("sample_26_model", CONSTS_JDOCS.VALIDATION_TYPE.ONLY_AT_READ_WRITE);
    try {
      d.getString("$.id1");
      fail();
    }
    catch (Exception e) {
    }

    d = new JDocument();
    d.setString("$.id1", "giberish");

    try {
      d.setType("sample_26_model", CONSTS_JDOCS.VALIDATION_TYPE.ALL_DATA_PATHS);
      fail();
    }
    catch (Exception e) {
    }
  }

  @Test
  void testValidation() {
    Document d = null;

    // test - will fail
    try {
      getTypedDocument("sample_23_model", "/jdocs/sample_23.json", ALL_DATA_PATHS);
      fail();
    }
    catch (Exception e) {
    }

    // test - doc construction will succeed but read of field will fail
    d = getTypedDocument("sample_23_model", "/jdocs/sample_23.json", ONLY_MODEL_PATHS);
    try {
      d.getString("$.phone_cell");
      fail();
    }
    catch (Exception e) {
    }

    // test - doc construction will succeed but read of field will fail
    d = getTypedDocument("sample_23_model", "/jdocs/sample_23.json", ONLY_AT_READ_WRITE);
    try {
      d.getString("$.phone_cell");
      fail();
    }
    catch (Exception e) {
    }

    // valid test
    String s = d.getString("$.first_name");
    assertEquals("Deepak", s);

    // above tests but this time using set type / validate methods
    d = getBaseDocument("/jdocs/sample_23.json");

    s = d.getString("$.giberish");
    assertEquals(null, s);

    s = d.getString("$.last_name");
    assertEquals("Arora", s);

    s = d.getString("$.phone_home");
    assertEquals("1234", s);

    // try to validate a base document against a model - will succeed
    d.validateModelPaths("sample_23_model");

    // set type by default - will throw exception
    try {
      d.setType("sample_23_model");
      fail();
    }
    catch (Exception e) {
    }

    // test only model paths -  will succeed
    d.setType("sample_23_model", ONLY_MODEL_PATHS);

    // now validate all paths - will fail
    try {
      d.validateAllPaths("sample_23_model");
      fail();
    }
    catch (Exception e) {
    }

    // we now set the default to only at read write
    JDocument.init(ONLY_AT_READ_WRITE);

    d = getBaseDocument("/jdocs/sample_23.json");
    d.setType("sample_23_model"); // will not validate and hence no exception thrown
    try {
      d.validateAllPaths("sample_23_model"); // will fail validation here
      fail();
    }
    catch (Exception e) {
    }
    d.validateModelPaths("sample_23_model"); // will succeed

    try {
      getTypedDocument("sample_23_model", "/jdocs/sample_23.json"); // will fail validation here
      fail();
    }
    catch (Exception e) {
    }

    getTypedDocument("sample_23_model", "/jdocs/sample_23.json", ONLY_MODEL_PATHS); // this should pass
    getTypedDocument("sample_23_model", "/jdocs/sample_23.json", ONLY_AT_READ_WRITE); // this should pass

    // restore it back
    JDocument.init(ALL_DATA_PATHS);

    // we now set the default to model validation
    JDocument.init(ONLY_MODEL_PATHS);

    d = getBaseDocument("/jdocs/sample_23.json");
    d.setType("sample_23_model"); // will validate and hence no exception thrown
    try {
      d.validateAllPaths("sample_23_model"); // will fail validation here
      fail();
    }
    catch (Exception e) {
    }
    d.validateModelPaths("sample_23_model"); // will succeed

    try {
      getTypedDocument("sample_23_model", "/jdocs/sample_23.json"); // will fail validation here
      fail();
    }
    catch (Exception e) {
    }

    getTypedDocument("sample_23_model", "/jdocs/sample_23.json", ONLY_MODEL_PATHS); // this should pass
    getTypedDocument("sample_23_model", "/jdocs/sample_23.json", ONLY_AT_READ_WRITE); // this should pass

    // restore it back
    JDocument.init(ALL_DATA_PATHS);
  }

  @Test
  void testMissingParams() {
    Document d = new JDocument();

    try {
      d.getString("$.names[name=%].first_name");
      fail();
    }
    catch (Exception e) {
    }

    try {
      d.getString("$.names[name=%].other[number=%].first_name");
      fail();
    }
    catch (Exception e) {
    }

    try {
      d.getString("$.names[name=%].other[number=%].first_name", "Deepak");
      fail();
    }
    catch (Exception e) {
    }

    try {
      d.getString("$.names[%].first_name");
      fail();
    }
    catch (Exception e) {
    }

  }

  @Test
  void testNumber() {
    try {
      // all primitive types at root are valid JSONs
      new JDocument("14");
      // use below to read the value of such a JSON document
      // String s = d.getJson();
      // long value = Long.valueOf(s);
      new JDocument("14.1");
      new JDocument("true");
      new JDocument("\"hello\"");
    }
    catch (Exception e) {
      fail();
    }
  }

  @Test
  void testTemp() {
    // a placeholder to run any temporary test
    JDocument d = (JDocument)getBaseDocument("/jdocs/temp.json");
    d.getString("$.id");
    assertTrue(true);
  }

  @Test
  void testDocumentMinMaxLengthValidation() {
    UnifyException e = assertThrows(UnifyException.class, () -> {
      getTypedDocument("sample_27_model", "/jdocs/sample_27_1.json");
    });
    assertEquals("jdoc_err_28", e.getErrorCode());
    String s = BaseUtils.removeWhiteSpaces("Max length validation failed for path -> $.addresses[1].line_1\n" +
                                                   "Min length validation failed for path -> $.addresses[2].line_1\n");
    assertEquals(s, BaseUtils.removeWhiteSpaces(e.getMessage()));
  }

  @Test
  void testDocumentMinMaxValueValidationInteger() {
    UnifyException e = assertThrows(UnifyException.class, () -> {
      getTypedDocument("sample_27_model", "/jdocs/sample_27_2.json");
    });
    assertEquals("jdoc_err_28", e.getErrorCode());
    String s = BaseUtils.removeWhiteSpaces("Min value validation failed for path -> $.test[3].int_no\n" +
                                                   "Max value validation failed for path -> $.test[4].int_no\n");
    assertEquals(s, BaseUtils.removeWhiteSpaces(e.getMessage()));
  }

  @Test
  void testDocumentMinMaxValueValidationLong() {
    UnifyException e = assertThrows(UnifyException.class, () -> {
      getTypedDocument("sample_27_model", "/jdocs/sample_27_3.json");
    });
    assertEquals("jdoc_err_28", e.getErrorCode());
    String s = BaseUtils.removeWhiteSpaces("Min value validation failed for path -> $.test[3].long_no\n" +
                                                   "Max value validation failed for path -> $.test[4].long_no\n");
    assertEquals(s, BaseUtils.removeWhiteSpaces(e.getMessage()));
  }

  @Test
  void testDocumentMinMaxValueValidationDecimal() {
    UnifyException e = assertThrows(UnifyException.class, () -> {
      getTypedDocument("sample_27_model", "/jdocs/sample_27_4.json");
    });
    assertEquals("jdoc_err_28", e.getErrorCode());
    String s = BaseUtils.removeWhiteSpaces("Min value validation failed for path -> $.test[3].decimal_no\n" +
                                                   "Max value validation failed for path -> $.test[4].decimal_no\n");
    assertEquals(s, BaseUtils.removeWhiteSpaces(e.getMessage()));
  }

  @Test
  void testDocumentMinMaxDateValidation1() {
    UnifyException e = assertThrows(UnifyException.class, () -> {
      getTypedDocument("sample_27_model", "/jdocs/sample_27_5_1.json");
    });
    assertEquals("jdoc_err_28", e.getErrorCode());
    String s = BaseUtils.removeWhiteSpaces("Min date validation failed for path -> $.test[3].date_1\n" +
                                                   "Max date validation failed for path -> $.test[4].date_1\n");
    assertEquals(s, BaseUtils.removeWhiteSpaces(e.getMessage()));
  }

  @Test
  void testDocumentMinMaxDateValidation2() {
    UnifyException e = assertThrows(UnifyException.class, () -> {
      getTypedDocument("sample_27_model", "/jdocs/sample_27_5_2.json");
    });
    assertEquals("jdoc_err_28", e.getErrorCode());
    String s = BaseUtils.removeWhiteSpaces("Min date validation failed for path -> $.test[3].date_2\n" +
                                                   "Max date validation failed for path -> $.test[4].date_2\n");
    assertEquals(s, BaseUtils.removeWhiteSpaces(e.getMessage()));
  }

  @Test
  void testDocumentMinMaxDateValidation3() {
    UnifyException e = assertThrows(UnifyException.class, () -> {
      getTypedDocument("sample_27_model", "/jdocs/sample_27_5_3.json");
    });
    assertEquals("jdoc_err_28", e.getErrorCode());
    String s = BaseUtils.removeWhiteSpaces("Min date validation failed for path -> $.test[3].date_3\n" +
                                                   "Max date validation failed for path -> $.test[4].date_3\n");
    assertEquals(s, BaseUtils.removeWhiteSpaces(e.getMessage()));
  }

  @Test
  void testLoadLong() {
    Document d = getTypedDocument("sample_28_model", "/jdocs/sample_28.json", ALL_DATA_PATHS);
    Long val = d.getLong("$.test.long_no");
    assertEquals(5000, val);
  }

  @Test
  void testSetContentAcrossDocTypes() {
    Document fromDoc = getTypedDocument("sample_29_from_model", "/jdocs/sample_29_from.json", ALL_DATA_PATHS);
    Document toDoc = getTypedDocument("sample_29_to_model", "/jdocs/sample_29_to.json", ALL_DATA_PATHS);
    toDoc.setContent(fromDoc, "$.level_from.level_2.level_3", "$.level_to.level_2.level_3");
    assertEquals("field_1", toDoc.getString("$.level_to.level_2.level_3.field_1"));
    assertEquals("field_2", toDoc.getString("$.level_to.level_2.level_3.field_2"));
    assertEquals("val_6", toDoc.getString("$.level_to.level_2.level_3.field_to_1"));
    assertEquals("val_7", toDoc.getString("$.level_to.level_2.level_3.field_to_2"));
  }

}
