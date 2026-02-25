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
import java.text.ParseException;
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
  public void testDateFormat() {
    //    String s = "2021-Jan-09 03:53:24.491 UTC";
    //    Instant instant = BaseUtils.getInstantFromString(s, "uuuu-MMM-dd HH:mm:ss.SSS VV");
    //    s = BaseUtils.fromInstant(instant, "uuuuDDD");
    //    System.out.println(s);
    //    s = BaseUtils.fromInstant(instant, "uuuuDDD", "America/Phoenix");
    //    System.out.println(s);
    //
    //    instant = Instant.now();
    //    s = BaseUtils.fromInstant(instant, "uuuuDDD");
    //    System.out.println(s);
    //    s = BaseUtils.fromInstant(instant, "uuuuDDD", "America/Phoenix");
    //    System.out.println(s);
    //
    //    Date d = new Date(new Timestamp(1).getTime());
    //    System.out.println(d);

    //    String s = "2021-Oct-26 12:00:00.000 UTC";
    //    Instant instant = BaseUtils.getInstantFromString(s, CONSTS_BASE.UNIFY_TS_FMT);
    //    System.out.println(instant);
    //    System.out.println(Timestamp.from(instant));
    //
    //    Timestamp ts = BaseUtils.getTimestampFromUnifyTsString(s);
    //    System.out.println(ts);
    //    Date d = new Date(ts.getTime());
    //    System.out.println(d);

    //    String s = "10/26/2021";
    //    Instant instant = BaseUtils.getInstantFromString(s, "MM/dd/uuuu");
    //    Timestamp ts = Timestamp.from(instant);
    //    Date d = new Date(ts.getTime());
    //    System.out.println(d);

    //    Instant instant = Instant.now();
    //    SimpleDateFormat sdf = new SimpleDateFormat("yyyyddd");
    //    String s = sdf.format(new Date(instant.toEpochMilli()));
    //    System.out.println(s);

    //    String s = "2021-Oct-26T12:00:00.000 UTC";
    //    Instant instant = BaseUtils.getInstantFromString(s, "uuuu-MMM-dd'T'HH:mm:ss.SSS VV");
    //    System.out.println(instant);

    Instant instant = Instant.now();
    System.out.println(instant);
  }

  @Test
  public void testTemp() throws ParseException {
    //    ZonedDateTime zdt = BaseUtils.getZonedDateTimeFromString("20210103121212000", "uuuuMMddHHmmssSSS");
    //    System.out.println(zdt);

    //    DateTimeFormatter df = DateTimeFormatter.ofPattern("uuuu-MM-dd").withLocale(Locale.US);
    //    LocalDate ld = LocalDate.parse("2021-08-03", df);
    //    LocalDateTime ldt = ld.atStartOfDay();
    //    ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
    //    Instant instant = zdt.toInstant();
    //    System.out.println(instant);

    //    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSS").withLocale(Locale.US);
    //    LocalDateTime ldt = LocalDateTime.parse("2021-Aug-13 00:00:00.000", df);
    //    System.out.println(ldt);

    //    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
    //    SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
    //    Date d = format1.parse("2021-Jan-14 14:23:23");
    //    String s = format2.format(d);
    //    System.out.println(s);

    //    DateTimeFormatter df1 = DateTimeFormatter.ofPattern("uuuu-MMM-dd HH:mm:ss");
    //    DateTimeFormatter df2 = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    //    TemporalAccessor ta = df1.parse("2021-Jan-14 14:23:23");
    //    String s = df2.format(ta);
    //    System.out.println(s);

    //    DateTimeFormatter df1 = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    //    TemporalAccessor ta = df1.parse("2021-Jan-14");

    //    Date d = Date.valueOf(BaseUtils.fromLocalDateString("20210113", "uuuuMMdd", "uuuu-MM-dd"));
    //    System.out.println(d);

    //    Date d = new Date(Instant.now().toEpochMilli());
    //    String zone = ZoneId.systemDefault().toString();
    //    System.out.println(zone);
    //    DateTimeFormatter df = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSXXX").withLocale(Locale.US).withZone(ZoneId.of(zone));
    //    Instant instant = Instant.ofEpochMilli(d.getTime());
    //    String s = df.format(instant);
    //    System.out.println(s);
    //    s = BaseUtils.fromDate(d, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    //    System.out.println(s);

    //    Instant instant = Instant.now();
    //    ZonedDateTime zdt = instant.atZone(ZoneId.of("UTC"));
    //    DateTimeFormatter df = DateTimeFormatter.ofPattern("uuuu-MMM-dd HH:mm:ss.SSS VV").withZone(ZoneId.of("UTC")).withLocale(Locale.US);
    //    String s = zdt.format(df);
    //    System.out.println(s);
    //    s = df.format(instant);
    //    System.out.println(s);

    //    String s = BaseUtils.fromLocalDateString("2021001", "uuuuDDD", "uuuu-MMM-dd");
    //    System.out.println(s);

    //    Instant now = Instant.now();
    //    String s = BaseUtils.fromInstant(now, "uuuuMMdd'T'HHmmss.SSS VV", "GMT");
    //    System.out.println(s);
    //
    //    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    //    SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss.SSS zzz");
    //    Date date = new Date();
    //    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    //    dateFormat.setLenient(true);
    //    timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    //    timeFormat.setLenient(true);
    //    String strDate = dateFormat.format(date);
    //    String time = timeFormat.format(date);
    //    String timestamp = strDate + "T" + time;
    //    System.out.println(timestamp);

    //    TimeZone tz = TimeZone.getTimeZone("UTC");
    //    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    //    df.setTimeZone(tz);
    //    String s = df.format(new Date());
    //    System.out.println(s);
    //
    //    s = BaseUtils.fromInstant(Instant.now(), "uuuu-MM-dd'T'HH:mm'Z'", CONSTS_BASE.UTC_TZ);
    //    System.out.println(s);

    //    DateTimeFormatter df = DateTimeFormatter.ofPattern("uuuu-MMM-dd HH:mm:ss VV");
    //    TemporalAccessor ta = df.parseBest("2021-Jan-01 23:23:23 America/Phoenix", ZonedDateTime::from, LocalDateTime::from, LocalDate::from, YearMonth::from, Year::from);
    //    System.out.println(ta.getClass().getSimpleName());

    //    ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of(CONSTS_BASE.UTC_TZ));
    //    Instant i1 = zdt.toInstant();
    //    Instant i2 = Instant.now();
    //    System.out.println(i1);
    //    System.out.println(i2);

    //    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss zzz");
    //    SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    //    Date d = format1.parse("2021-Jan-23 23:23:23 UTC");
    //    String s = format2.format(d);
    //    System.out.println(s);

    //    String s = BaseUtils.fromDateString("2021-Jan-23 23:23:23 America/Phoenix", "uuuu-MMM-dd HH:mm:ss VV", "uuuu-MM-dd");
    //    System.out.println(s);

    Instant instant = BaseUtils.getNextBusinessDay(Instant.now(), 5);
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
