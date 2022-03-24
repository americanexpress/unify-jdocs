package com.americanexpress.unify.jdocs;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * The BaseUtils class contains general purpose static methods.
 * <p>
 * Unless otherwise specified, methods throw an instance of UnifyException which wraps the
 * underlying exception as the cause
 *
 * @author Deepak Arora
 */
public class BaseUtils {

  /**
   * Wrapping function over Java sleep to throws a UnifyException
   *
   * @param millis Time to sleep for in milliseconds
   */
  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException ex) {
      throw new UnifyException("base_err_4", ex);
    }
  }

  /**
   * Checks if the string value passed is null or empty
   * <p>
   * If string consists of only spaces, it is considered as empty
   *
   * @param s The string to check
   * @return True if string passed is null or empty else false
   */
  public static boolean isNullOrEmpty(String s) {
    if ((s == null) || (s.trim().isEmpty() == true)) {
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Removes all white spaces from a string
   * <p>
   * A whitespace is identified using the Java method Character.isWhiteSpace
   *
   * @param s The string containing white spaces
   * @return The string with white spaces removed
   */
  public static String removeWhiteSpaces(String s) {
    StringBuilder sb = new StringBuilder(s);
    int j = 0;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (Character.isWhitespace(c) == false) {
        sb.setCharAt(j, c);
        j++;
      }
    }
    sb.setLength(j);
    return sb.toString();
  }

  /**
   * Replaces all continuous multiple occurrences of a space with a single one
   *
   * @param s The string in which to replace the multiple continuous spaces
   * @return The String with the multiple spaces replaced
   */
  public static String removeDupSpaces(String s) {
    if ((s == null) || (s.isEmpty())) {
      return "";
    }

    StringBuilder sb = new StringBuilder(s.length());
    char prev = s.charAt(0);
    sb.append(prev);

    char next = 0;
    for (int i = 1; i < s.length(); i++) {
      next = s.charAt(i);
      if ((prev == ' ') && (prev == next)) {
        prev = next;
        continue;
      }
      sb.append(next);
      prev = next;
    }
    return sb.toString();
  }

  /**
   * Creates a zoned date time object from the string representation of a date
   * <p>
   * The input string is expected to have a timezone specified at the location specified in the pattern
   *
   * @param dt      The date as a string
   * @param pattern The pattern of the string representation
   * @param locale  The Locale in the string representation
   * @return A ZonedDateTime object else throws UnifyException containing the underlying exception as a cause
   */
  public static ZonedDateTime getZonedDateTimeFromString(String dt, String pattern, Locale locale) {
    try {
      DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern).withLocale(locale);
      ZonedDateTime zdt = ZonedDateTime.parse(dt, df);
      return zdt;
    }
    catch (Exception e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  /**
   * Creates a local date time object from the string representation of a date
   *
   * @param dt      The date as a string
   * @param pattern The pattern of the string representation
   * @param locale  The Locale in the string representation
   * @return A LocalDateTime object
   */
  public static LocalDateTime getLocalDateTimeFromString(String dt, String pattern, Locale locale) {
    try {
      DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern).withLocale(locale);
      LocalDateTime ldt = LocalDateTime.parse(dt, df);
      return ldt;
    }
    catch (Exception e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  /**
   * Creates a local date object from the string representation of a date
   *
   * @param dt      The date as a string
   * @param pattern The pattern of the string representation
   * @param locale  The Locale in the string representation
   * @return A LocalDate object
   */
  public static LocalDate getLocalDateFromString(String dt, String pattern, Locale locale) {
    try {
      DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern).withLocale(locale);
      LocalDate ldt = LocalDate.parse(dt, df);
      return ldt;
    }
    catch (Exception e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  /**
   * Creates a string representation of a date given a ZonedDateTime object
   * *
   *
   * @param zdt        The ZonedDateTime object to convert to String
   * @param outPattern The pattern of the string representation
   * @param outLocale  The Locale in the string representation
   * @param outTz      The timezone in the string representation
   * @return The string representation of the date
   */
  public static String fromZonedDateTime(ZonedDateTime zdt, String outPattern, Locale outLocale, String outTz) {
    try {
      DateTimeFormatter df = DateTimeFormatter.ofPattern(outPattern).withZone(ZoneId.of(outTz)).withLocale(outLocale);
      return zdt.format(df);
    }
    catch (Exception e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  /**
   * Creates a string representation from an Instant as per the given pattern, locale and timezone
   *
   * @param instant    The Instant to convert into a String
   * @param outPattern The pattern of the string representation
   * @param outLocale  The local in the output string representation
   * @param outTz      The timezone of the date in the string representation
   * @return The string representation of the date
   */
  public static String fromInstant(Instant instant, String outPattern, Locale outLocale, String outTz) {
    try {
      ZonedDateTime zdt = instant.atZone(ZoneId.of(outTz));
      DateTimeFormatter df = DateTimeFormatter.ofPattern(outPattern).withZone(ZoneId.of(outTz)).withLocale(outLocale);
      return zdt.format(df);
    }
    catch (Exception e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  /**
   * Creates a string representation from a date represented as a string which contains timezone information
   * <p>
   * The timezone of the input date is expected to be specified in the input string at the location specified in the pattern
   *
   * @param dt         The input string to be converted
   * @param inPattern  The input string pattern
   * @param inLocale   The local in the input string representation
   * @param outPattern The pattern of the output string
   * @param outLocale  The local in the output string representation
   * @param outTz      The timezone of the output string representation
   * @return The string representation of the date. If the input date is null or empty, an empty string is returned
   */
  public static String fromTzDateString(String dt, String inPattern, Locale inLocale, String outPattern, Locale outLocale, String outTz) {
    try {
      if ((dt == null) || (dt.isEmpty() == true)) {
        return "";
      }

      DateTimeFormatter df = DateTimeFormatter.ofPattern(inPattern).withLocale(inLocale);
      ZonedDateTime zdt = ZonedDateTime.parse(dt, df);
      DateTimeFormatter df1 = DateTimeFormatter.ofPattern(outPattern).withLocale(outLocale).withZone(ZoneId.of(outTz));
      return zdt.format(df1);
    }
    catch (Exception e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  /**
   * Creates a string representation from a date represented as a string which does NOT contain timezone information
   * <p>
   * Default locale is used
   *
   * @param dt         The input string to be converted
   * @param inPattern  The input string pattern
   * @param outPattern The pattern of the output string
   * @return The string representation of the date. If the input date is null or empty, an empty string is returned
   */
  public static String fromLocalDateString(String dt, String inPattern, String outPattern) {
    try {
      if ((dt == null) || (dt.isEmpty() == true)) {
        return "";
      }

      SimpleDateFormat format1 = new SimpleDateFormat(inPattern);
      SimpleDateFormat format2 = new SimpleDateFormat(outPattern);
      Date d = format1.parse(dt);
      return format2.format(d);
    }
    catch (ParseException e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  /**
   * Creates a date object from a string representation
   * <p>
   * Default locale is used
   *
   * @param s       String representation of the date
   * @param pattern Patter of the string representation
   * @return A Date object
   */
  public static Date getDateFromString(String s, String pattern) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat(pattern);
      Date d1 = sdf.parse(s);
      return d1;
    }
    catch (ParseException e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  /**
   * Returns a java.sql.Date from a string with the specified pattern
   *
   * @param s
   * @param pattern
   * @return
   */
  public static java.sql.Date getSqlDateFromString(String s, String pattern) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat(pattern);
      Date udl = sdf.parse(s);
      java.sql.Date d1 = new java.sql.Date(udl.getTime());
      return d1;
    }
    catch (ParseException e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  /**
   * Creates a String representation from a Date object
   *
   * @param d          The Date to be converted to string
   * @param outPattern The pattern of the string representation
   * @return The String representation
   */
  public static String fromDate(Date d, String outPattern) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat(outPattern);
      String s = sdf.format(d);
      return s;
    }
    catch (Exception e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  /**
   * Creates a String representation from a string containing Julian date
   *
   * @param dt         The string representation in Julian date format
   * @param outPattern The pattern of the output String representation
   * @return The String as per the pattern
   */
  public static String fromJulianDate(String dt, String outPattern) {
    try {
      DateFormat fmt1 = new SimpleDateFormat("yyyyDDD");
      Date date = null;
      date = fmt1.parse(dt);
      DateFormat fmt2 = new SimpleDateFormat(outPattern);
      return fmt2.format(date);
    }
    catch (ParseException e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  /**
   * Creates a Timestamp object from a String representation of a date
   *
   * @param strDate The string representation of a date
   * @return Timestamp object for the string representation
   */
  // Given a date string, format it and return a timestamp of that date
  public static Timestamp getTimestampFromString(String strDate) {
    ZonedDateTime zdt = BaseUtils.getZonedDateTimeFromString(strDate, "yyyy-MMM-dd HH:mm:ss.SSS VV", Locale.ENGLISH);
    return Timestamp.from(zdt.toInstant());
  }

  /**
   * Creates a Timestamp object from an Instant
   *
   * @param instant An instant
   * @return Timestamp object for the instant
   */
  // Given an instant, return a timestamp
  public static Timestamp fromInstant(Instant instant) {
    return new Timestamp(instant.toEpochMilli());
  }

  /**
   * Returns the contents of a resource as a String
   *
   * @param clazz    The class to use for getting the resource
   * @param filePath The complete filepath including resource name
   * @return The resource as a String
   */
  public static String getResourceAsString(Class clazz, String filePath) {
    String s = null;
    InputStream is = new BufferedInputStream(clazz.getResourceAsStream(filePath));
    s = getStringFromStream(is);
    try {
      is.close();
    }
    catch (IOException ex) {
      throw new UnifyException("base_err_3", ex);
    }
    return s;
  }

  /**
   * Reads the contents of the input stream and returns back a String
   *
   * @param inputStream The input stream to read
   * @return The contents of the stream as a String
   * @throws UnifyException
   */
  public static String getStringFromStream(InputStream inputStream) throws UnifyException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();

    byte[] buffer = new byte[1024];
    int length;
    String s = null;

    try {
      while ((length = inputStream.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }

      s = result.toString(StandardCharsets.UTF_8.toString());
      result.close();
    }
    catch (IOException ex) {
      throw new UnifyException("base_err_3", ex);
    }
    return s;
  }

  /**
   * Returns the contents of the stack trace for a Throwable as a String
   *
   * @param e The exception from which to extract the stack trace
   * @return The stack trace as a String. Only last 12 levels of the stack trace are returned
   */
  public static String getStackTrace(Throwable e) {
    StackTraceElement[] se = e.getStackTrace();
    String s = getStackTrace(e, 12);
    return s;
  }

  /**
   * Returns the specified number of levels of the stack trace for a Throwable as a String
   *
   * @param e The exception from which to extract the stack trace
   * @return The stack trace as a String
   */
  public static String getStackTrace(Throwable e, int levels) {
    StackTraceElement[] se = e.getStackTrace();
    if (levels > se.length) {
      levels = se.length;
    }

    String s = "";
    for (int i = 0; i < levels; i++) {
      s = s + "at " + se[i].getClassName() + "(" + se[i].getFileName() + ":" + se[i].getLineNumber() + ")";
      if (i != (levels - 1)) {
        s = s + CONSTS_JDOCS.NEW_LINE;
      }
    }

    return s;
  }

  /**
   * Returns the index of the specified occurrences of a character in a String
   *
   * @param s          The String in which to search
   * @param c          The character to search for
   * @param occurrence The 1 based occurrence to search for. A value of 1 means the first occurrence
   * @param fromStart  Search from beginning if true, else backwards from the end
   * @return the index of the occurrence if found else -1
   */
  public static int getIndexOfChar(String s, char c, int occurrence, boolean fromStart) {
    if (s.isEmpty()) {
      return -1;
    }

    if (fromStart == true) {
      int count = 0;
      int index = 0;
      int fromIndex = 0;

      while (true) {
        index = s.indexOf(c, fromIndex);

        if (index == -1) {
          break;
        }

        count++;

        if (count == occurrence) {
          break;
        }

        fromIndex = index + 1;
      }

      return index;
    }
    else {
      int count = 0;
      int index = 0;
      int fromIndex = s.length() - 1;

      while (true) {
        index = s.lastIndexOf(c, fromIndex);

        if (index == -1) {
          break;
        }

        count++;

        if (count == occurrence) {
          break;
        }

        fromIndex = index - 1;
      }

      return index;
    }
  }

  /**
   * Returns the number of occurrences of a character in a string
   *
   * @param s The String in which to search for
   * @param c The character to search for
   * @return The number of occurrences found
   */
  public static int getCount(String s, char c) {
    int count = 0;
    int index = 0;
    int fromIndex = 0;

    while (true) {
      index = s.indexOf(c, fromIndex);

      if (index == -1) {
        break;
      }

      count++;

      if (index == (s.length() - 1)) {
        break;
      }

      fromIndex = index + 1;
    }

    return count;
  }

  /**
   * Returns the class name without the package names
   *
   * @param canonicalName the full name of the class for example com.example.Class1
   * @return The class name. For the above Class1
   */
  public static String getSimpleClassName(String canonicalName) {
    String s = canonicalName;
    int index = canonicalName.lastIndexOf(".");
    if (index >= 0) {
      s = s.substring(index + 1);
    }
    return s;
  }

  /**
   * Escapes the specified characters with the escape character
   * <p>
   * If the String contains the escape character as data, that too will be escaped
   *
   * @param s     The String in which to look for characters to escape
   * @param ec    The escape character
   * @param chars The characters to escape
   * @return The String with the specified characters escaped
   */
  public static String escapeChars(String s, char ec, char... chars) {
    StringBuffer sb = new StringBuffer(s.length() + 10); // abitrarily assuming that there will not be more than 10 characters required to be escaped
    int size = s.length();

    for (int i = 0; i < size; i++) {
      char c = s.charAt(i);
      if ((BaseUtils.compareWithMany(c, chars) == true) || (c == ec)) {
        sb.append(ec);
      }
      sb.append(c);
    }

    return sb.toString().trim();
  }

  /**
   * Removes the escape character for the specified characters
   * <p>
   * If the String contains the escape character itself escaped, the escape character for this character will also be removed
   *
   * @param s     The String in which to remove the escape characters
   * @param ec    The escape character to remove
   * @param chars The escaped characters to look for
   * @return The String with the escaped character removed
   */
  public static String removeEscapeChars(String s, char ec, char... chars) {
    StringBuffer sb = new StringBuffer(s.length());
    int size = s.length();

    for (int i = 0; i < size; i++) {
      if (i == (size - 1)) {
        sb.append(s.charAt(i));
      }
      else {
        char c = s.charAt(i);
        if (c == ec) {
          c = s.charAt(i + 1);
          if ((BaseUtils.compareWithMany(c, chars) == true) || (c == ec)) {
            // we do not need to copy the escape char
          }
          else {
            sb.append(s.charAt(i));
          }
        }
        else {
          sb.append(s.charAt(i));
        }
      }
    }

    return sb.toString().trim();
  }

  /**
   * Compares the given character with more than one characters
   *
   * @param first  The character to compare
   * @param others The characters to compare against
   * @return True if first is matched to any of others else false
   */
  public static boolean compareWithMany(char first, char... others) {
    if (others == null) {
      return false;
    }

    for (int i = 0; i < others.length; i++) {
      if (first == others[i]) {
        return true;
      }
    }

    return false;
  }

  /**
   * Compares the given String with more than one Strings
   *
   * @param first  The String to compare
   * @param others The Strings to compare against
   * @return True if first is matched to any of others else false
   */
  public static boolean compareWithMany(String first, String... others) {
    if (others == null) {
      return false;
    }

    for (int i = 0; i < others.length; i++) {
      if (first.equals(others[i])) {
        return true;
      }
    }

    return false;
  }

  /**
   * Compares the given String with more than one Strings ignoring case
   *
   * @param first  The String to compare
   * @param others The Strings to compare against
   * @return True if first is matched to any of others else false
   */
  public static boolean compareWithManyIgnoreCase(String first, String... others) {
    if (others == null) {
      return false;
    }

    for (int i = 0; i < others.length; i++) {
      if (first.equalsIgnoreCase(others[i])) {
        return true;
      }
    }

    return false;
  }

  /**
   * Creates an error String from the specified exception and code
   * <p>
   * The error string consists of the following format
   * "Error code -> {0}\nError message -> {1}\nError details -> {2}" where
   * {0} is the String code
   * {1} is e.getMessage and
   * {2} is the String returned from getStackTrace(e, 12)
   *
   * @param e    The Exception
   * @param code The code
   * @return The error String
   */
  public static String getErrorString(Exception e, String code) {
    String s = "Error code -> " + code + CONSTS_JDOCS.NEW_LINE;
    s = s + "Error message -> " + e.getMessage() + CONSTS_JDOCS.NEW_LINE;
    s = s + "Error details -> " + getStackTrace(e, 12);
    return s;
  }

  /**
   * Creates an error String from a UnifyException
   * <p>
   * The error string consists of the following format if the cause inside UnifyException is null
   * "Error code -> {0}\nError message -> {1}\nError details -> {2}" where
   * {0} is e.getErrorCode
   * {1} is e.getMessage and
   * {2} is the String returned from getStackTrace(e, 12)
   * <p>
   * The error string consists of the following format if the cause inside UnifyException is not null
   * "Error code -> {0}\nError message -> {1}\nError details -> {2}\nStack Info -> {3}" where
   * {0} is e.getErrorCode
   * {1} is cause.getMessage and
   * {2} is e.getDetails
   * {3} is the String returned from getStackTrace(cause, 12)
   *
   * @param e The UnifyException
   * @return The error String
   */
  public static String getErrorString(UnifyException e) {
    Throwable cause = e.getCause();
    String s = null;
    if (cause == null) {
      s = "Error code -> " + e.getErrorCode() + CONSTS_JDOCS.NEW_LINE;
      s = s + "Error message -> " + e.getMessage() + CONSTS_JDOCS.NEW_LINE;
      s = s + "Error details -> " + getStackTrace(e, 12);
    }
    else {
      s = "Error code -> " + e.getErrorCode() + CONSTS_JDOCS.NEW_LINE;
      s = s + "Error message -> " + cause.getMessage() + CONSTS_JDOCS.NEW_LINE;
      s = s + "Error details -> " + e.getDetails() + CONSTS_JDOCS.NEW_LINE;
      s = s + "Stack info -> " + getStackTrace(cause, 12);
    }

    return s;
  }


  /**
   * Returns an empty String when the input String is null
   *
   * @param input The input String
   * @return The output String
   */
  public static String getEmptyWhenNull(String input) {
    if (input != null) {
      return input;
    }
    else {
      return "";
    }
  }

  /**
   * Returns a 0 when input is null
   *
   * @param input The input value
   * @return The output value
   */
  public static Integer getZeroWhenNull(Integer input) {
    if (input != null) {
      return input;
    }
    else {
      return 0;
    }
  }

  /**
   * Returns a 0 when input is null
   *
   * @param input The input value
   * @return The output value
   */
  public static Double getZeroWhenNull(Double input) {
    if (input != null) {
      return input;
    }
    else {
      return new Double("0.0");
    }
  }

  /**
   * Returns a 0 when input is null
   *
   * @param input The input value
   * @return The output value
   */
  public static Boolean getFalseWhenNull(Boolean input) {
    if (input != null) {
      return input;
    }
    else {
      return false;
    }
  }

  /**
   * Returns true if the input String is not null and not empty
   *
   * @param s The String to test
   * @return True if not empty else false
   */
  public static boolean isNotEmpty(String s) {
    if ((null != s) && (s.trim().isEmpty() == false)) {
      return true;
    }
    else {
      return false;
    }
  }

  // generate a random number between the two specified number both sides included
  public static int getRandom(int from, int to) {
    Random random = new Random(System.nanoTime());
    int r = random.nextInt(to - from + 1) + from;
    return r;
  }

  public static String getWithoutCarriageReturn(String s) {
    byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    for (int i = 0; i < bytes.length; i++) {
      if ((bytes[i] == '\r') == false) {
        result.write(bytes[i]);
      }
    }
    String s1 = result.toString();
    try {
      result.close();
    }
    catch (IOException e) {
    }
    return s1;
  }

}
