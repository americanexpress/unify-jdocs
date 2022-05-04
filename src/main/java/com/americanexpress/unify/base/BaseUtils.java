package com.americanexpress.unify.base;

import com.github.lalyos.jfiglet.FigletFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAccessor;
import java.util.*;

public class BaseUtils {

  private static Logger logger = LoggerFactory.getLogger(BaseUtils.class);

  // Wrapping function over Java sleep to throws a UnifyException
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
   * @param dt        The date as a string
   * @param inPattern The pattern of the string representation
   * @return A ZonedDateTime object else throws UnifyException containing the underlying exception as a cause
   */
  public static ZonedDateTime getZonedDateTimeFromString(String dt, String inPattern) {
    try {
      DateTimeFormatter df = DateTimeFormatter.ofPattern(inPattern).withLocale(Locale.US);
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
   * @param dt        The date as a string
   * @param inPattern The pattern of the string representation
   * @return A LocalDateTime object
   */
  public static LocalDateTime getLocalDateTimeFromString(String dt, String inPattern) {
    try {
      DateTimeFormatter df = DateTimeFormatter.ofPattern(inPattern).withLocale(Locale.US);
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
   * @param dt        The date as a string
   * @param inPattern The pattern of the string representation
   * @return A LocalDate object
   */
  public static LocalDate getLocalDateFromString(String dt, String inPattern) {
    try {
      DateTimeFormatter df = DateTimeFormatter.ofPattern(inPattern).withLocale(Locale.US);
      LocalDate ldt = LocalDate.parse(dt, df);
      return ldt;
    }
    catch (Exception e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  // given an instant, returns a string representation as per the out pattern in the system default time zone
  public static String fromInstant(Instant instant, String outPattern) {
    try {
      return fromInstant(instant, outPattern, ZoneId.systemDefault().toString());
    }
    catch (Exception e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  // given an instant, returns a string representation as per the out pattern in the specified time zone
  public static String fromInstant(Instant instant, String outPattern, String outTz) {
    try {
      DateTimeFormatter df = DateTimeFormatter.ofPattern(outPattern).withZone(ZoneId.of(outTz)).withLocale(Locale.US);
      return df.format(instant);
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
   * @param outPattern The pattern of the output string
   * @param outTz      The timezone of the output string representation
   * @return The string representation of the date. If the input date is null or empty, an empty string is returned
   */
  public static String fromDateString(String dt, String inPattern, String outPattern, String outTz) {
    try {
      if ((dt == null) || (dt.isEmpty() == true)) {
        return "";
      }

      DateTimeFormatter df1 = DateTimeFormatter.ofPattern(inPattern).withLocale(Locale.US);
      DateTimeFormatter df2 = DateTimeFormatter.ofPattern(outPattern).withLocale(Locale.US).withZone(ZoneId.of(outTz));
      TemporalAccessor ta = df1.parse(dt);
      return df2.format(ta);
    }
    catch (Exception e) {
      throw new UnifyException("base_err_1", e);
    }
  }

  /**
   * Creates a string representation from a date represented as a string which
   * does NOT contain timezone information in both input and output string patterns OR
   * both input and output string patterns contain the same time zone information i.e. either zone id or offset
   * it cannot be that one string contains zone id and the other contains offset information OR
   * the input pattern contains a time zone info and the output does not in which case the source
   * time zone will be used for the output
   * <p>
   * Suffice to say that it is best to use this for cases where output pattern does not contain time zone info
   *
   * <p>
   * Default locale is used
   *
   * @param dt         The input string to be converted
   * @param inPattern  The input string pattern
   * @param outPattern The pattern of the output string
   * @return The string representation of the date. If the input date is null or empty, an empty string is returned
   */
  public static String fromDateString(String dt, String inPattern, String outPattern) {
    if ((dt == null) || (dt.isEmpty() == true)) {
      return "";
    }

    DateTimeFormatter df1 = DateTimeFormatter.ofPattern(inPattern).withLocale(Locale.US);
    DateTimeFormatter df2 = DateTimeFormatter.ofPattern(outPattern).withLocale(Locale.US);
    TemporalAccessor ta = df1.parse(dt);
    return df2.format(ta);
  }

  public static Timestamp getTimestampFromLocalDateTime(String strDate, String pattern) {
    DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern).withLocale(Locale.US);
    LocalDateTime ldt = LocalDateTime.parse(strDate, df);
    return Timestamp.valueOf(ldt);
  }

  /**
   * Creates a Timestamp object from a String representation of a date
   *
   * @param strDate The string representation of a date
   * @return Timestamp object for the string representation
   */
  // Given a date string, format it and return a timestamp of that date
  public static Timestamp getTimestampFromUnifyTsString(String strDate) {
    DateTimeFormatter df = DateTimeFormatter.ofPattern(CONSTS_BASE.UNIFY_TS_FMT).withLocale(Locale.US);
    TemporalAccessor ta = df.parse(strDate);
    return Timestamp.from(Instant.from(ta));
  }

  /**
   * Creates a Timestamp object from an Instant
   *
   * @param instant An instant
   * @return Timestamp object for the instant
   */
  // Given an instant, return a timestamp
  public static Timestamp getTimestampFromInstant(Instant instant) {
    return new Timestamp(instant.toEpochMilli());
  }

  public static Instant getInstantFromString(String sDate, String inPattern) {
    // creates an instant from an arbitrary pattern. The only requirement is that the year needs to be specified
    // else an exception will be thrown
    // Also the time zone / offset if specified will only be used if the full date time is specified upto seconds
    DateTimeFormatter df = DateTimeFormatter.ofPattern(inPattern).withResolverStyle(ResolverStyle.STRICT);

    TemporalAccessor ta = null;
    try {
      ta = df.parseBest(sDate, ZonedDateTime::from, LocalDateTime::from, LocalDate::from, YearMonth::from, Year::from);
    }
    catch (Exception e) {
      throw new UnifyException("base_err_14", e);
    }

    if (ta instanceof ZonedDateTime) {
      return ((ZonedDateTime)ta).toInstant();
    }
    else if (ta instanceof LocalDateTime) {
      LocalDateTime ldt = (LocalDateTime)ta;
      return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }
    else if (ta instanceof LocalDate) {
      LocalDate ld = (LocalDate)ta;
      return ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    }
    else if (ta instanceof YearMonth) {
      YearMonth ym = (YearMonth)ta;
      return ym.atDay(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    }
    else if (ta instanceof Year) {
      Year year = (Year)ta;
      return year.atMonth(1).atDay(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    }

    // should never reach here
    return null;
  }

  public static Instant getNextBusinessDay(Instant ts, int daysToAdd) {
    ZonedDateTime zdt = ts.atZone(ZoneId.of("UTC"));
    for (int i = 0; i < daysToAdd; i++) {
      zdt = zdt.plusDays(1);
      while ((zdt.getDayOfWeek() == DayOfWeek.SATURDAY) || (zdt.getDayOfWeek() == DayOfWeek.SUNDAY)) {
        zdt = zdt.plusDays(1);
      }
    }
    return zdt.toInstant();
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
    InputStream is = clazz.getResourceAsStream(filePath);

    if (is == null) {
      return null;
    }

    InputStream bis = new BufferedInputStream(is);
    s = getStringFromStream(bis);
    try {
      bis.close();
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
    Throwable e1 = null;
    if (e instanceof UnifyException) {
      e1 = e.getCause();
      if (e1 != null) {
        e = e1;
      }
    }

    return getStackTrace(e, 12);
  }

  /**
   * Returns the specified number of levels of the stack trace for a Throwable as a String
   *
   * @param e The exception from which to extract the stack trace
   * @return The stack trace as a String
   */
  public static String getStackTrace(Throwable e, int levels) {
    Throwable e1 = null;
    if (e instanceof UnifyException) {
      e1 = e.getCause();
      if (e1 != null) {
        e = e1;
      }
    }

    StackTraceElement[] se = e.getStackTrace();
    if (levels > se.length) {
      levels = se.length;
    }

    String s = "";
    for (int i = 0; i < levels; i++) {
      s = s + "at " + se[i].getClassName() + "(" + se[i].getFileName() + ":" + se[i].getLineNumber() + ")";
      if (i != (levels - 1)) {
        s = s + CONSTS_BASE.NEW_LINE;
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
   * @param canonicalName the full name of the class for example com.aexp.acq.Class1
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
    String s = "Error code -> " + code + CONSTS_BASE.NEW_LINE;
    s = s + "Error message -> " + e.getMessage() + CONSTS_BASE.NEW_LINE;
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
      s = "Error code -> " + e.getErrorCode() + CONSTS_BASE.NEW_LINE;
      s = s + "Error message -> " + e.getMessage() + CONSTS_BASE.NEW_LINE;
      s = s + "Error details -> " + getStackTrace(e);
    }
    else {
      s = "Error code -> " + e.getErrorCode() + CONSTS_BASE.NEW_LINE;
      s = s + "Error message -> " + cause.getMessage() + CONSTS_BASE.NEW_LINE;
      s = s + "Error details -> " + e.getDetails() + CONSTS_BASE.NEW_LINE;
      s = s + "Stack info -> " + getStackTrace(cause);
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
  public static BigDecimal getZeroWhenNull(BigDecimal input) {
    if (input != null) {
      return input;
    }
    else {
      return new BigDecimal("0.0");
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

  public static void showWelcomeBanner(String pathToBannerFile) {
    InputStream is = BaseUtils.class.getResourceAsStream(pathToBannerFile);
    List<String> list = new ArrayList<>();

    // read banner file
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isEmpty()) {
          continue;
        }

        list.add(line);
      }

      // get a random number
      int index = getRandom(0, list.size() - 1);
      String s = list.get(index);
      String[] tokens = s.split("\\|");
      String author = tokens[0].trim();
      String banner = tokens[1].trim();

      String asciiArt = FigletFont.convertOneLine(banner);
      System.out.println(asciiArt);
      System.out.println("Quote famously contributed by -> " + author);
      System.out.println();
      System.out.flush();
      Thread.sleep(3000);
    }
    catch (Exception e) {
      // nothing to do
    }
  }

  // generate a random number between the two specified number both sides included
  public static int getRandom(int from, int to) {
    Random random = new Random(System.nanoTime());
    int r = random.nextInt(to - from + 1) + from;
    return r;
  }

  public static void log(Logger logger, Level level, String message) {
    switch (level) {
      case DEBUG:
        logger.debug(message);
        break;

      case ERROR:
        logger.error(message);
        break;

      case INFO:
        logger.info(message);
        break;

      case TRACE:
        logger.trace(message);
        break;

      case WARN:
        logger.warn(message);
        break;
    }
  }

  public static void validateDate(String date, String pattern) {
    DateTimeFormatter dfs = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    dfs.parse(date);
  }

  public static void validateDateTime(String datetime, String pattern) {
    DateTimeFormatter dfs = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    dfs.parse(datetime);
  }

  public static void showSystemInfo() {
    int logicalProcessors = Runtime.getRuntime().availableProcessors();
    logger.info("Running on CPU with number of logical threads / cores -> {}", logicalProcessors);

    long m = Runtime.getRuntime().totalMemory() / (1024 * 1024);
    logger.info("Total JVM memory in mb -> {}", m);

    m = Runtime.getRuntime().maxMemory() / (1024 * 1024);
    logger.info("Max JVM memory in mb -> {}", m);
  }

  public static void showEnvVariables() {
    Map<String, String> envMap = System.getenv();
    Set<String> envSet = envMap.keySet();
    System.out.println();
    logger.info("Listing system environment variables");
    for (String name : envSet) {
      String value = System.getenv(name);
      logger.info("{} -> {}", name, value);
    }
    System.out.println();
  }

  public static Date getSqlDateFromString(String strDate, String pattern) {
    try {
      Instant instant = BaseUtils.getInstantFromString(strDate, pattern);
      Timestamp ts = Timestamp.from(instant);
      return new Date(ts.getTime());
    }
    catch (Exception e) {
      // nothing to do
    }
    return null;
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
