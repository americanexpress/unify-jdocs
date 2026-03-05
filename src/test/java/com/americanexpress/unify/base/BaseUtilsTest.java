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

package com.americanexpress.unify.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * @author Deepak Arora / Benjamin Kats
 */
public class BaseUtilsTest {

  // Test methods
  @Test
  public void testEscapeChars() {
    Logger logger = LogManager.getLogger(BaseUtilsTest.class);
    logger.info("Test message");
    String s = "Deepak\\ A.ro|ra";
    String s1 = BaseUtils.escapeCharsAndTrim(s, '\\', '.', '|');
    assertTrue(s1.equals("Deepak\\\\ A\\.ro\\|ra"));

  }

  @Test
  public void testRemoveEscapeChars() {
    String s = "Deepak\\\\ A\\.ro\\|ra \\";
    System.out.println(s);
    String s1 = BaseUtils.removeEscapeCharsAndTrim(s, '\\', '.', '|');
    System.out.println(s1);
    assertTrue(s1.equals("Deepak\\ A.ro|ra \\"));
  }

  @Test
  public void test() {
    try {
      String s = BaseUtils.fromInstant(Instant.now(), "uuuu-MM-dd HH:mm:ss VV", CONSTS_BASE.UTC_TZ);
      System.out.println(s);
      s = BaseUtils.fromInstant(Instant.now(), "uuuu-MM-dd HH:mm:ss VV", CONSTS_BASE.GMT_TZ);
      System.out.println(s);
    }
    catch (UnifyException e) {
      assert (false);
    }
  }

  @Test
  public void testDate1() {
    try {
      BaseUtils.fromInstant(Instant.now(), "uuuu-MM-dd HH:mm:ss VV", "America/Phoenix");
    }
    catch (UnifyException e) {
      assert (false);
    }
  }

  @Test
  public void testMessageFormat() {
    try {
      String select_sql = "select pcn from application_info where process_resume_ts is not null and ((process_resume_ts + interval ''{0} second'') < current_timestamp)";
      MessageFormat.format(select_sql, "6");
    }
    catch (UnifyException e) {
      assert (false);
    }
  }

  @Test
  public void testValidateDate() {
    try {
      String pattern = "uuuu-MMM-dd";
      String value = "2021-Feb-29";
      BaseUtils.validateDate(value, pattern);
      assert (false);
    }
    catch (Exception e) {
      assert (true);
    }
  }

  @Test
  public void testDateTimeFormat() {
    try {
      String pattern = "uuuu-MMM-dd";
      String value = "2020-Feb-02";
      DateTimeFormatter f = DateTimeFormatter.ofPattern(pattern);
      f.parse(value);
    }
    catch (UnifyException e) {
      assert (false);
    }
  }

  @Test
  public void testLocale() {
    Instant now = Instant.now();
    System.out.println(BaseUtils.fromInstant(now, "uuuu-MM-dd HH:mm:ss VV", CONSTS_BASE.UTC_TZ));
    System.out.println(BaseUtils.fromInstant(now, "uuuu-MM-dd HH:mm:ss VV", CONSTS_BASE.UTC_TZ));
  }

  @Test
  public void testDateTime() {
    Timestamp ts = BaseUtils.getTimestampFromInstant(Instant.now());
    System.out.println(ts);
    String s = BaseUtils.fromInstant(ts.toInstant(), CONSTS_BASE.UNIFY_TS_FMT, CONSTS_BASE.UTC_TZ);
    System.out.println(s);
  }

  @Test
  public void testZoneIDName() {
    String s = BaseUtils.fromInstant(Instant.now(), CONSTS_BASE.UNIFY_TS_FMT, CONSTS_BASE.UTC_TZ);
    String s1 = "2021-Oct-26 18:26:06.410 ET";
    Instant instant = BaseUtils.getInstantFromString(s1, "uuuu-MMM-dd HH:mm:ss.SSS zzz");
    System.out.println(instant);
  }

  @Test
  public void testRemoveNonNumeric() {
    String s = "980ty -  89 9ui%";
    String s1 = "980899";
    String s2 = BaseUtils.removeAllNonNumeric(s);
    assertEquals(s2, s1);
  }

  @Test
  public void testReplaceWhiteSpaceWithSingleSpace() {
    String s = "ﾄﾓｱｷ   kss    \n\r  ﾄﾓｱｷ";
    String s1 = "ﾄﾓｱｷ kss ﾄﾓｱｷ";
    String s2 = BaseUtils.replaceWhiteSpacesWithSingleSpace(s);
    assertEquals(s2, s1);
  }

  @Test
  public void testRemoveWhiteSpace() {
    String s = "ﾄﾓｱｷ   kss    \n\r  ﾄﾓｱｷ";
    String s1 = "ﾄﾓｱｷkssﾄﾓｱｷ";
    String s2 = BaseUtils.removeWhiteSpaces(s);
    assertEquals(s2, s1);
  }

  @Test
  public void testIsNullOrEmpty() {
    String s = null;
    assertTrue(BaseUtils.isNullOrEmpty(s));

    s = "";
    assertTrue(BaseUtils.isNullOrEmpty(s));

    s = " ";
    assertTrue(BaseUtils.isNullOrEmpty(s));

    s = "                        ";
    assertTrue(BaseUtils.isNullOrEmpty(s));

    s = "A";
    assertFalse(BaseUtils.isNullOrEmpty(s));

    s = "   A   ";
    assertFalse(BaseUtils.isNullOrEmpty(s));
  }

  @Test
  public void testIsNotNullOrEmpty() {
    String s = null;
    assertFalse(BaseUtils.isNotNullOrEmpty(s));

    s = "";
    assertFalse(BaseUtils.isNotNullOrEmpty(s));

    s = " ";
    assertFalse(BaseUtils.isNotNullOrEmpty(s));

    s = "                        ";
    assertFalse(BaseUtils.isNotNullOrEmpty(s));

    s = "   A   ";
    assertTrue(BaseUtils.isNotNullOrEmpty(s));

    s = "A";
    assertTrue(BaseUtils.isNotNullOrEmpty(s));
  }

  @Test
  public void testIsAnyNullOrEmpty() {
    final String EMPTY = "";
    final String ONE_SPACE = " ";
    final String MANY_SPACES = "                        ";
    assertTrue(BaseUtils.isAnyNullOrEmpty(null, null, null));
    assertTrue(BaseUtils.isAnyNullOrEmpty(EMPTY, EMPTY, EMPTY));
    assertTrue(BaseUtils.isAnyNullOrEmpty(ONE_SPACE, ONE_SPACE, ONE_SPACE));
    assertTrue(BaseUtils.isAnyNullOrEmpty(MANY_SPACES, MANY_SPACES, MANY_SPACES));
    assertTrue(BaseUtils.isAnyNullOrEmpty(null, EMPTY, ONE_SPACE, MANY_SPACES));
    assertTrue(BaseUtils.isAnyNullOrEmpty(null, "A", "A"));
    assertTrue(BaseUtils.isAnyNullOrEmpty("A", null, "A"));
    assertTrue(BaseUtils.isAnyNullOrEmpty("A", "A", null));
    assertFalse(BaseUtils.isAnyNullOrEmpty("A", "A", "A"));
  }

  @Test
  public void testIsAnyNotNullOrEmpty() {
    final String EMPTY = "";
    final String ONE_SPACE = " ";
    final String MANY_SPACES = "                        ";
    assertFalse(BaseUtils.isAnyNotNullOrEmpty(null, null, null));
    assertFalse(BaseUtils.isAnyNotNullOrEmpty(EMPTY, EMPTY, EMPTY));
    assertFalse(BaseUtils.isAnyNotNullOrEmpty(ONE_SPACE, ONE_SPACE, ONE_SPACE));
    assertFalse(BaseUtils.isAnyNotNullOrEmpty(MANY_SPACES, MANY_SPACES, MANY_SPACES));
    assertFalse(BaseUtils.isAnyNotNullOrEmpty(null, EMPTY, ONE_SPACE, MANY_SPACES));
    assertTrue(BaseUtils.isAnyNotNullOrEmpty(null, "A", "A"));
    assertTrue(BaseUtils.isAnyNotNullOrEmpty("A", null, "A"));
    assertTrue(BaseUtils.isAnyNotNullOrEmpty("A", "A", null));
    assertTrue(BaseUtils.isAnyNotNullOrEmpty("A", "A", "A"));
  }

  @Test
  public void testIsAllNullOrEmpty() {
    final String EMPTY = "";
    final String ONE_SPACE = " ";
    final String MANY_SPACES = "                        ";
    assertTrue(BaseUtils.isAllNullOrEmpty(null, null, null));
    assertTrue(BaseUtils.isAllNullOrEmpty(EMPTY, EMPTY, EMPTY));
    assertTrue(BaseUtils.isAllNullOrEmpty(ONE_SPACE, ONE_SPACE, ONE_SPACE));
    assertTrue(BaseUtils.isAllNullOrEmpty(MANY_SPACES, MANY_SPACES, MANY_SPACES));
    assertTrue(BaseUtils.isAllNullOrEmpty(null, EMPTY, ONE_SPACE, MANY_SPACES));
    assertFalse(BaseUtils.isAllNullOrEmpty(null, "A", "A"));
    assertFalse(BaseUtils.isAllNullOrEmpty("A", null, "A"));
    assertFalse(BaseUtils.isAllNullOrEmpty("A", "A", null));
    assertFalse(BaseUtils.isAllNullOrEmpty("A", "A", "A"));
  }

  @Test
  public void testIsAllNotNullOrEmpty() {
    final String EMPTY = "";
    final String ONE_SPACE = " ";
    final String MANY_SPACES = "                        ";
    assertFalse(BaseUtils.isAllNotNullOrEmpty(null, null, null));
    assertFalse(BaseUtils.isAllNotNullOrEmpty(EMPTY, EMPTY, EMPTY));
    assertFalse(BaseUtils.isAllNotNullOrEmpty(ONE_SPACE, ONE_SPACE, ONE_SPACE));
    assertFalse(BaseUtils.isAllNotNullOrEmpty(MANY_SPACES, MANY_SPACES, MANY_SPACES));
    assertFalse(BaseUtils.isAllNotNullOrEmpty(null, EMPTY, ONE_SPACE, MANY_SPACES));
    assertFalse(BaseUtils.isAllNotNullOrEmpty(null, "A", "A"));
    assertFalse(BaseUtils.isAllNotNullOrEmpty("A", null, "A"));
    assertFalse(BaseUtils.isAllNotNullOrEmpty("A", "A", null));
    assertTrue(BaseUtils.isAllNotNullOrEmpty("A", "A", "A"));
  }

  @Test
  public void testGetEnumByName() {
    String j1 = "SOME_JOURNEY_3";
    boolean isSwitched = false;

    switch (BaseUtils.getEnumByName(TestEnums.journey_name.class, j1)) {
      case SOME_JOURNEY_3:
        isSwitched = true;
        break;
      default:
        //    do nothing
    }
    assertTrue(isSwitched);

    assertNull(BaseUtils.getEnumByName(TestEnums.journey_name.class, "NOT_AN_ENUM"));
  }

  @Test
  public void testGetEnumByNameIgnoreCase() {
    String j1 = "SOME_JOURNEY_3";
    String j2 = "SoMe_jOUrNEY_3";
    String j3 = "some_journey_3";
    boolean isSwitched = false;

    switch (BaseUtils.getEnumByNameIgnoreCase(TestEnums.journey_name.class, j1)) {
      case SOME_JOURNEY_3:
        isSwitched = true;
        break;
      default:
        //    do nothing
    }
    assertTrue(isSwitched);

    isSwitched = false;
    switch (BaseUtils.getEnumByNameIgnoreCase(TestEnums.journey_name.class, j2)) {
      case SOME_JOURNEY_3:
        isSwitched = true;
        break;
      default:
        //    do nothing
    }
    assertTrue(isSwitched);

    isSwitched = false;
    switch (BaseUtils.getEnumByNameIgnoreCase(TestEnums.journey_name.class, j3)) {
      case SOME_JOURNEY_3:
        isSwitched = true;
        break;
      default:
        //    do nothing
    }
    assertTrue(isSwitched);

    assertNull(BaseUtils.getEnumByNameIgnoreCase(TestEnums.journey_name.class, "NOT_AN_ENUM"));
  }

  @Test
  public void testCompareWithMany() {
    String val = "Match";
    String[] vals = {"", null, "match"};
    assertFalse(BaseUtils.compareWithMany(val, vals));

    vals = new String[] {"", null, "match", "Match"};
    assertTrue(BaseUtils.compareWithMany(val, vals));

    assertFalse(BaseUtils.compareWithMany(null, vals));
    assertFalse(BaseUtils.compareWithMany(val, (String[])null));
    assertFalse(BaseUtils.compareWithMany(null, (String[])null));
  }

  @Test
  public void testCompareWithManyIgnoreCase() {
    String val = "MATCH";
    String[] vals = {"", null, "match"};
    assertTrue(BaseUtils.compareWithManyIgnoreCase(val, vals));

    assertFalse(BaseUtils.compareWithManyIgnoreCase(null, vals));
    assertFalse(BaseUtils.compareWithManyIgnoreCase(val, (String[])null));
    assertFalse(BaseUtils.compareWithManyIgnoreCase(null, (String[])null));

    vals = new String[] {"", null, "batch", "hatch"};
    assertFalse(BaseUtils.compareWithManyIgnoreCase(val, vals));
  }

  @Test
  public void testEvaluateJPath() {
    String jPath = "$.some.path[type=%].to.an[index=%].field[%]";
    assertEquals("$.some.path[type=PRIMARY].to.an[index=1].field[2]", BaseUtils.evaluateJPath(jPath, "PRIMARY", "1", "2"));
    assertEquals(jPath, BaseUtils.evaluateJPath(jPath, "%", "%", "%"));
    assertEquals("$.some.path[type=%%%%].to.an[index=%%%].field[%%]", BaseUtils.evaluateJPath(jPath, "%%%%", "%%%", "%%"));
    assertEquals("$.some.path[type=].to.an[index=  ].field[       ]", BaseUtils.evaluateJPath(jPath, "", "  ", "       "));
    assertEquals(jPath, BaseUtils.evaluateJPath(jPath, (String[])null));
    assertNull(BaseUtils.evaluateJPath(null, "PRIMARY", "1", "2"));

    UnifyException exception1 = assertThrows(UnifyException.class, () ->
            BaseUtils.evaluateJPath("$.some.path[type=%].to.an[index=%].field[%]", "PRIMARY", null, "2"));
    assertEquals("base_err_5", exception1.getErrorCode());

    UnifyException exception2 = assertThrows(UnifyException.class, () ->
            BaseUtils.evaluateJPath("$.some.path[type=%].to.an[index=%].field[%]", "PRIMARY", "2"));
    assertEquals("base_err_6", exception2.getErrorCode());

    UnifyException exception3 = assertThrows(UnifyException.class, () ->
            BaseUtils.evaluateJPath("$.some.path[type=%].to.an[index=%].field[%]", "PRIMARY", "1", "2", "3"));
    assertEquals("base_err_6", exception3.getErrorCode());

    UnifyException exception4 = assertThrows(UnifyException.class, () ->
            BaseUtils.evaluateJPath("$.some.path[type=0].to.an[index=0].field[0]", "PRIMARY", "1", "2", "3"));
    assertEquals("base_err_6", exception4.getErrorCode());
  }

}
