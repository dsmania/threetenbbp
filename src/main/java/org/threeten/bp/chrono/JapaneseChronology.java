/*
 * Copyright (c) 2007-2013, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.threeten.bp.chrono;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.threeten.bp.Clock;
import org.threeten.bp.DateTimeException;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Year;
import org.threeten.bp.ZoneId;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.TemporalAccessor;
import org.threeten.bp.temporal.ValueRange;

import sun.util.calendar.CalendarSystem;
import sun.util.calendar.LocalGregorianCalendar;

import static org.threeten.bp.jdk7.Jdk7Methods.Objects_requireNonNull;

/**
 * The Japanese Imperial calendar system.
 * <p>
 * This chronology defines the rules of the Japanese Imperial calendar system.
 * This calendar system is primarily used in Japan.
 * The Japanese Imperial calendar system is the same as the ISO calendar system
 * apart from the era-based year numbering.
 * <p>
 * Only Meiji (1865-04-07 - 1868-09-07) and later eras are supported.
 * Older eras are handled as an unknown era where the year-of-era is the ISO year.
 *
 * <h3>Specification for implementors</h3>
 * This class is immutable and thread-safe.
 */
public final class JapaneseChronology extends Chronology implements Serializable {
    // TODO: definition for unknown era may break requirement that year-of-era >= 1

    static final LocalGregorianCalendar JCAL =
        (LocalGregorianCalendar) CalendarSystem.forName("japanese");

    // Locale for creating a JapaneseImpericalCalendar.
    static final Locale LOCALE = new Locale("ja", "JP", "JP"); // BBP: Locale.forLanguageTag("ja-JP-u-ca-japanese");

    /**
     * Singleton instance for Japanese chronology.
     */
    public static final JapaneseChronology INSTANCE = new JapaneseChronology();

    /**
     * The singleton instance for the before Meiji era ( - 1868-09-07)
     * which has the value -999.
     */
    public static final Era ERA_SEIREKI = JapaneseEra.SEIREKI;
    /**
     * The singleton instance for the Meiji era (1868-09-08 - 1912-07-29)
     * which has the value -1.
     */
    public static final Era ERA_MEIJI = JapaneseEra.MEIJI;
    /**
     * The singleton instance for the Taisho era (1912-07-30 - 1926-12-24)
     * which has the value 0.
     */
    public static final Era ERA_TAISHO = JapaneseEra.TAISHO;
    /**
     * The singleton instance for the Showa era (1926-12-25 - 1989-01-07)
     * which has the value 1.
     */
    public static final Era ERA_SHOWA = JapaneseEra.SHOWA;
    /**
     * The singleton instance for the Heisei era (1989-01-08 - current)
     * which has the value 2.
     */
    public static final Era ERA_HEISEI = JapaneseEra.HEISEI;
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 459996390165777884L;

    /**
     * Narrow names for eras.
     */
    private static final Map<String, String[]> ERA_NARROW_NAMES = new HashMap<String, String[]>();
    /**
     * Short names for eras.
     */
    private static final Map<String, String[]> ERA_SHORT_NAMES = new HashMap<String, String[]>();
    /**
     * Full names for eras.
     */
    private static final Map<String, String[]> ERA_FULL_NAMES = new HashMap<String, String[]>();
    /**
     * Fallback language for the era names.
     */
    private static final String FALLBACK_LANGUAGE = "en";
    /**
     * Language that has the era names.
     */
    private static final String TARGET_LANGUAGE = "ja";

    /**
     * Name data.
     */
    // TODO: replace all the hard-coded Maps with locale resources
    static {
        ERA_NARROW_NAMES.put(FALLBACK_LANGUAGE, new String[]{"Unknown", "K", "M", "T", "S", "H"});
        ERA_NARROW_NAMES.put(TARGET_LANGUAGE, new String[]{"Unknown", "K", "M", "T", "S", "H"});
        ERA_SHORT_NAMES.put(FALLBACK_LANGUAGE, new String[]{"Unknown", "K", "M", "T", "S", "H"});
        ERA_SHORT_NAMES.put(TARGET_LANGUAGE, new String[]{"Unknown", "\u6176", "\u660e", "\u5927", "\u662d", "\u5e73"});
        ERA_FULL_NAMES.put(FALLBACK_LANGUAGE, new String[]{"Unknown", "Keio", "Meiji", "Taisho", "Showa", "Heisei"});
        ERA_FULL_NAMES.put(TARGET_LANGUAGE,
                new String[]{"Unknown", "\u6176\u5fdc", "\u660e\u6cbb", "\u5927\u6b63", "\u662d\u548c", "\u5e73\u6210"});
    }

    //-----------------------------------------------------------------------
    /**
     * Restricted constructor.
     */
    private JapaneseChronology() {
    }

    /**
     * Resolve singleton.
     *
     * @return the singleton instance, not null
     */
    private Object readResolve() {
        return INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the ID of the chronology - 'Japanese'.
     * <p>
     * The ID uniquely identifies the {@code Chrono}.
     * It can be used to lookup the {@code Chrono} using {@link #of(String)}.
     *
     * @return the chronology ID - 'Japanese'
     * @see #getCalendarType()
     */
    @Override
    public String getId() {
        return "Japanese";
    }

    /**
     * Gets the calendar type of the underlying calendar system - 'japanese'.
     * <p>
     * The calendar type is an identifier defined by the
     * <em>Unicode Locale Data Markup Language (LDML)</em> specification.
     * It can be used to lookup the {@code Chrono} using {@link #of(String)}.
     * It can also be used as part of a locale, accessible via
     * {@link Locale#getUnicodeLocaleType(String)} with the key 'ca'.
     *
     * @return the calendar system type - 'japanese'
     * @see #getId()
     */
    @Override
    public String getCalendarType() {
        return "japanese";
    }

    //-----------------------------------------------------------------------
    @Override  // override with covariant return type
    public JapaneseDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        if (era instanceof JapaneseEra == false) {
            throw new DateTimeException("Era must be JapaneseEra");
        }
        return JapaneseDate.of((JapaneseEra) era, yearOfEra, month, dayOfMonth);
    }

    @Override  // override with covariant return type
    public JapaneseDate date(int prolepticYear, int month, int dayOfMonth) {
        return new JapaneseDate(LocalDate.of(prolepticYear, month, dayOfMonth));
    }

    @Override  // override with covariant return type
    public JapaneseDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return (JapaneseDate) super.dateYearDay(era, yearOfEra, dayOfYear);
    }

    @Override  // override with covariant return type
    public JapaneseDate dateYearDay(int prolepticYear, int dayOfYear) {
        LocalDate date = LocalDate.ofYearDay(prolepticYear, dayOfYear);
        return date(prolepticYear, date.getMonthValue(), date.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    @Override  // override with covariant return type
    public JapaneseDate date(TemporalAccessor temporal) {
        if (temporal instanceof JapaneseDate) {
            return (JapaneseDate) temporal;
        }
        return new JapaneseDate(LocalDate.from(temporal));
    }

    @Override  // override with covariant return type
    public ChronoLocalDateTime<JapaneseDate> localDateTime(TemporalAccessor temporal) {
        return (ChronoLocalDateTime<JapaneseDate>) super.localDateTime(temporal);
    }

    @Override  // override with covariant return type
    public ChronoZonedDateTime<JapaneseDate> zonedDateTime(TemporalAccessor temporal) {
        return (ChronoZonedDateTime<JapaneseDate>) super.zonedDateTime(temporal);
    }

    @Override  // override with covariant return type
    public ChronoZonedDateTime<JapaneseDate> zonedDateTime(Instant instant, ZoneId zone) {
        return (ChronoZonedDateTime<JapaneseDate>) super.zonedDateTime(instant, zone);
    }

    //-----------------------------------------------------------------------
    @Override  // override with covariant return type
    public JapaneseDate dateNow() {
        return (JapaneseDate) super.dateNow();
    }

    @Override  // override with covariant return type
    public JapaneseDate dateNow(ZoneId zone) {
        return (JapaneseDate) super.dateNow(zone);
    }

    @Override  // override with covariant return type
    public JapaneseDate dateNow(Clock clock) {
        Objects_requireNonNull(clock, "clock");
        return (JapaneseDate) super.dateNow(clock);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the specified year is a leap year.
     * <p>
     * Japanese calendar leap years occur exactly in line with ISO leap years.
     * This method does not validate the year passed in, and only has a
     * well-defined result for years in the supported range.
     *
     * @param prolepticYear  the proleptic-year to check, not validated for range
     * @return true if the year is a leap year
     */
    @Override
    public boolean isLeapYear(long prolepticYear) {
        return IsoChronology.INSTANCE.isLeapYear(prolepticYear);
    }

    @Override
    public int prolepticYear(Era era, int yearOfEra) {
        if (era instanceof JapaneseEra == false) {
            throw new DateTimeException("Era must be JapaneseEra");
        }
        JapaneseEra jera = (JapaneseEra) era;
        int gregorianYear = jera.getPrivateEra().getSinceDate().getYear() + yearOfEra - 1;
        if (yearOfEra == 1) {
            return gregorianYear;
        }
        LocalGregorianCalendar.Date jdate = JCAL.newCalendarDate(null);
        jdate.setEra(jera.getPrivateEra()).setDate(yearOfEra, 1, 1);
        JCAL.normalize(jdate);
        if (jdate.getNormalizedYear() == gregorianYear) {
            return gregorianYear;
        }
        throw new DateTimeException("invalid yearOfEra value");
    }

    /**
     * Returns the calendar system era object from the given numeric value.
     *
     * See the description of each Era for the numeric values of:
     * {@link #ERA_HEISEI}, {@link #ERA_SHOWA},{@link #ERA_TAISHO},
     * {@link #ERA_MEIJI}), only Meiji and later eras are supported.
     * Prior to Meiji {@link #ERA_SEIREKI} is used.
     *
     * @param eraValue  the era value
     * @return the Japanese {@code Era} for the given numeric era value
     * @throws DateTimeException if {@code eraValue} is invalid
     */
    @Override
    public Era eraOf(int eraValue) {
        return JapaneseEra.of(eraValue);
    }

    @Override
    public List<Era> eras() {
        return Arrays.<Era>asList(JapaneseEra.values());
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(ChronoField field) {
        switch (field) {
            case DAY_OF_MONTH:
            case DAY_OF_WEEK:
            case MICRO_OF_DAY:
            case MICRO_OF_SECOND:
            case HOUR_OF_DAY:
            case HOUR_OF_AMPM:
            case MINUTE_OF_DAY:
            case MINUTE_OF_HOUR:
            case SECOND_OF_DAY:
            case SECOND_OF_MINUTE:
            case MILLI_OF_DAY:
            case MILLI_OF_SECOND:
            case NANO_OF_DAY:
            case NANO_OF_SECOND:
            case CLOCK_HOUR_OF_DAY:
            case CLOCK_HOUR_OF_AMPM:
            case EPOCH_DAY:
            case EPOCH_MONTH:
                return field.range();
        }
        Calendar jcal = Calendar.getInstance(LOCALE);
        int fieldIndex;
        switch (field) {
            case ERA:
                return ValueRange.of(jcal.getMinimum(Calendar.ERA) - JapaneseEra.ERA_OFFSET,
                                             jcal.getMaximum(Calendar.ERA) - JapaneseEra.ERA_OFFSET);
            case YEAR:
            case YEAR_OF_ERA:
                return ValueRange.of(Year.MIN_VALUE, jcal.getGreatestMinimum(Calendar.YEAR),
                                             jcal.getLeastMaximum(Calendar.YEAR), Year.MAX_VALUE);
            case MONTH_OF_YEAR:
                return ValueRange.of(jcal.getMinimum(Calendar.MONTH) + 1, jcal.getGreatestMinimum(Calendar.MONTH) + 1,
                                             jcal.getLeastMaximum(Calendar.MONTH) + 1, jcal.getMaximum(Calendar.MONTH) + 1);
            case DAY_OF_YEAR:
                fieldIndex = Calendar.DAY_OF_YEAR;
                break;
            default:
                 // TODO: review the remaining fields
                throw new UnsupportedOperationException("Unimplementable field: " + field);
        }
        return ValueRange.of(jcal.getMinimum(fieldIndex), jcal.getGreatestMinimum(fieldIndex),
                                     jcal.getLeastMaximum(fieldIndex), jcal.getMaximum(fieldIndex));
    }

}
