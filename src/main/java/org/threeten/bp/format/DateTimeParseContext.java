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
package org.threeten.bp.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.threeten.bp.DateTimeException;
import org.threeten.bp.ZoneId;
import org.threeten.bp.chrono.Chronology;
import org.threeten.bp.chrono.IsoChronology;
import org.threeten.bp.jdk8.DefaultInterfaceTemporalAccessor;
import org.threeten.bp.jdk8.Jdk8Methods;
import org.threeten.bp.temporal.TemporalField;
import org.threeten.bp.temporal.TemporalQueries;
import org.threeten.bp.temporal.TemporalQuery;

/**
 * Context object used during date and time parsing.
 * <p>
 * This class represents the current state of the parse.
 * It has the ability to store and retrieve the parsed values and manage optional segments.
 * It also provides key information to the parsing methods.
 * <p>
 * Once parsing is complete, the {@link #toBuilder()} is typically used
 * to obtain a builder that can combine the separate parsed fields into meaningful values.
 *
 * <h3>Specification for implementors</h3>
 * This class is a mutable context intended for use from a single thread.
 * Usage of the class is thread-safe within standard parsing as a new instance of this class
 * is automatically created for each parse and parsing is single-threaded
 */
final class DateTimeParseContext {

    /**
     * The locale, not null.
     */
    private Locale locale;
    /**
     * The symbols, not null.
     */
    private DateTimeFormatSymbols symbols;
    /**
     * The chronology, not null.
     */
    private Chronology chronology;
    /**
     * Whether to parse using case sensitively.
     */
    private boolean caseSensitive = true;
    /**
     * Whether to parse using strict rules.
     */
    private boolean strict = true;
    /**
     * The list of parsed data.
     */
    private final ArrayList<Parsed> parsed = new ArrayList<Parsed>();

    /**
     * Creates a new instance of the context.
     *
     * @param formatter  the formatter controlling the parse, not null
     */
    DateTimeParseContext(DateTimeFormatter formatter) {
        super();
        this.locale = formatter.getLocale();
        this.symbols = formatter.getSymbols();
        this.chronology = formatter.getChronology();
        parsed.add(new Parsed());
    }

    // for testing
    DateTimeParseContext(Locale locale, DateTimeFormatSymbols symbols, Chronology chronology) {
        super();
        this.locale = locale;
        this.symbols = symbols;
        this.chronology = chronology;
        parsed.add(new Parsed());
    }

    /**
     * Creates a copy of this context.
     */
    DateTimeParseContext copy() {
        return new DateTimeParseContext(locale, symbols, chronology);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the locale.
     * <p>
     * This locale is used to control localization in the parse except
     * where localization is controlled by the symbols.
     *
     * @return the locale, not null
     */
    Locale getLocale() {
        return locale;
    }

    /**
     * Gets the formatting symbols.
     * <p>
     * The symbols control the localization of numeric parsing.
     *
     * @return the formatting symbols, not null
     */
    DateTimeFormatSymbols getSymbols() {
        return symbols;
    }

    /**
     * Gets the effective chronology during parsing.
     *
     * @return the effective parsing chronology, not null
     */
    Chronology getEffectiveChronology() {
        Chronology chrono = currentParsed().chrono;
        if (chrono == null) {
            chrono = chronology;
            if (chrono == null) {
                chrono = IsoChronology.INSTANCE;
            }
        }
        return chrono;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if parsing is case sensitive.
     *
     * @return true if parsing is case sensitive, false if case insensitive
     */
    boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Sets whether the parsing is case sensitive or not.
     *
     * @param caseSensitive  changes the parsing to be case sensitive or not from now on
     */
    void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * Helper to compare two {@code CharSequence} instances.
     * This uses {@link #isCaseSensitive()}.
     *
     * @param cs1  the first character sequence, not null
     * @param offset1  the offset into the first sequence, valid
     * @param cs2  the second character sequence, not null
     * @param offset2  the offset into the second sequence, valid
     * @param length  the length to check, valid
     * @return true if equal
     */
    boolean subSequenceEquals(CharSequence cs1, int offset1, CharSequence cs2, int offset2, int length) {
        if (offset1 + length > cs1.length() || offset2 + length > cs2.length()) {
            return false;
        }
        if (isCaseSensitive()) {
            for (int i = 0; i < length; i++) {
                char ch1 = cs1.charAt(offset1 + i);
                char ch2 = cs2.charAt(offset2 + i);
                if (ch1 != ch2) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < length; i++) {
                char ch1 = cs1.charAt(offset1 + i);
                char ch2 = cs2.charAt(offset2 + i);
                if (ch1 != ch2 && Character.toUpperCase(ch1) != Character.toUpperCase(ch2) &&
                        Character.toLowerCase(ch1) != Character.toLowerCase(ch2)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Helper to compare two {@code char}.
     * This uses {@link #isCaseSensitive()}.
     *
     * @param ch1  the first character
     * @param ch2  the second character
     * @return true if equal
     */
    boolean charEquals(char ch1, char ch2) {
        if (isCaseSensitive()) {
            return ch1 == ch2;
        }
        return charEqualsIgnoreCase(ch1, ch2);
    }

    /**
     * Compares two characters ignoring case.
     *
     * @param c1  the first
     * @param c2  the second
     * @return true if equal
     */
    static boolean charEqualsIgnoreCase(char c1, char c2) {
        return c1 == c2 ||
                Character.toUpperCase(c1) == Character.toUpperCase(c2) ||
                Character.toLowerCase(c1) == Character.toLowerCase(c2);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if parsing is strict.
     * <p>
     * Strict parsing requires exact matching of the text and sign styles.
     *
     * @return true if parsing is strict, false if lenient
     */
    boolean isStrict() {
        return strict;
    }

    /**
     * Sets whether parsing is strict or lenient.
     *
     * @param strict  changes the parsing to be strict or lenient from now on
     */
    void setStrict(boolean strict) {
        this.strict = strict;
    }

    //-----------------------------------------------------------------------
    /**
     * Starts the parsing of an optional segment of the input.
     */
    void startOptional() {
        parsed.add(currentParsed().copy());
    }

    /**
     * Ends the parsing of an optional segment of the input.
     *
     * @param successful  whether the optional segment was successfully parsed
     */
    void endOptional(boolean successful) {
        if (successful) {
            parsed.remove(parsed.size() - 2);
        } else {
            parsed.remove(parsed.size() - 1);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the currently active temporal objects.
     *
     * @return the current temporal objects, not null
     */
    private Parsed currentParsed() {
        return parsed.get(parsed.size() - 1);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the first value that was parsed for the specified field.
     * <p>
     * This searches the results of the parse, returning the first value found
     * for the specified field. No attempt is made to derive a value.
     * The field may have an out of range value.
     * For example, the day-of-month might be set to 50, or the hour to 1000.
     *
     * @param field  the field to query from the map, null returns null
     * @return the value mapped to the specified field, null if field was not parsed
     */
    Long getParsed(TemporalField field) {
        return currentParsed().fieldValues.get(field);
    }

    /**
     * Stores the parsed field.
     * <p>
     * This stores a field-value pair that has been parsed.
     * The value stored may be out of range for the field - no checks are performed.
     *
     * @param field  the field to set in the field-value map, not null
     * @param value  the value to set in the field-value map
     * @param errorPos  the position of the field being parsed
     * @param successPos  the position after the field being parsed
     * @return the new position
     */
    int setParsedField(TemporalField field, long value, int errorPos, int successPos) {
        Objects.requireNonNull(field, "field");
        Long old = currentParsed().fieldValues.put(field, value);
        return (old != null && old.longValue() != value) ? ~errorPos : successPos;
    }

    /**
     * Stores the parsed chronology.
     * <p>
     * This stores the chronology that has been parsed.
     * No validation is performed other than ensuring it is not null.
     *
     * @param chrono  the parsed chronology, not null
     */
    void setParsed(Chronology chrono) {
        Objects.requireNonNull(chrono, "chrono");
        currentParsed().chrono = chrono;
    }

    /**
     * Stores the parsed zone.
     * <p>
     * This stores the zone that has been parsed.
     * No validation is performed other than ensuring it is not null.
     *
     * @param zone  the parsed zone, not null
     */
    void setParsed(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        currentParsed().zone = zone;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a {@code TemporalAccessor} that can be used to interpret
     * the results of the parse.
     *
     * @return an accessor with the results of the parse, not null
     */
    Parsed toParsed() {
        return currentParsed();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string version of the context for debugging.
     *
     * @return a string representation of the context data, not null
     */
    @Override
    public String toString() {
        return currentParsed().toString();
    }

    //-----------------------------------------------------------------------
    /**
     * Temporary store of parsed data.
     */
    final class Parsed extends DefaultInterfaceTemporalAccessor {
        Chronology chrono = null;
        ZoneId zone = null;
        final Map<TemporalField, Long> fieldValues = new HashMap<TemporalField, Long>();
        private Parsed() {
        }
        protected Parsed copy() {
            Parsed cloned = new Parsed();
            cloned.chrono = this.chrono;
            cloned.zone = this.zone;
            cloned.fieldValues.putAll(this.fieldValues);
            return cloned;
        }
        @Override
        public String toString() {
            return fieldValues.toString() + "," + chrono + "," + zone;
        }
        @Override
        public boolean isSupported(TemporalField field) {
            return fieldValues.containsKey(field);
        }
        @Override
        public int get(TemporalField field) {
            if (fieldValues.containsKey(field) == false) {
                throw new DateTimeException("Unsupported field: " + field);
            }
            long value = fieldValues.get(field);
            return Jdk8Methods.safeToInt(value);
        }
        @Override
        public long getLong(TemporalField field) {
            if (fieldValues.containsKey(field) == false) {
                throw new DateTimeException("Unsupported field: " + field);
            }
            return fieldValues.get(field);
        }
        @SuppressWarnings("unchecked")
        @Override
        public <R> R query(TemporalQuery<R> query) {
            if (query == TemporalQueries.chronology()) {
                return (R) chrono;
            }
            if (query == TemporalQueries.zoneId() || query == TemporalQueries.zone()) {
                return (R) zone;
            }
            return super.query(query);
        }

        /**
         * Resolves the fields in this context.
         *
         * @return this, for method chaining
         * @throws DateTimeException if resolving one field results in a value for
         *  another field that is in conflict
         */
        Parsed resolveFields() {
            outer:
            while (true) {
                for (Map.Entry<TemporalField, Long> entry : fieldValues.entrySet()) {
                    TemporalField targetField = entry.getKey();
                    Map<TemporalField, Long> changes = targetField.resolve(this, entry.getValue());
                    if (changes != null) {
                        resolveMakeChanges(targetField, changes);
                        fieldValues.remove(targetField);  // helps avoid infinite loops
                        continue outer;  // have to restart to avoid concurrent modification
                    }
                }
                break;
            }
            return this;
        }

        private void resolveMakeChanges(TemporalField targetField, Map<TemporalField, Long> changes) {
            for (Map.Entry<TemporalField, Long> change : changes.entrySet()) {
                TemporalField changeField = change.getKey();
                Long changeValue = change.getValue();
                Objects.requireNonNull(changeField, "changeField");
                if (changeValue != null) {
                    Long old = fieldValues.put(changeField, changeValue);
                    if (old != null && old.longValue() != changeValue.longValue()) {
                        throw new DateTimeException("Conflict found: " + changeField + " " + old +
                                " differs from " + changeField + " " + changeValue +
                                " while resolving  " + targetField);
                    }
                } else {
                    fieldValues.remove(changeField);
                }
            }
        }

        /**
         * Returns a {@code DateTimeBuilder} that can be used to interpret
         * the results of the parse.
         * <p>
         * This method is typically used once parsing is complete to obtain the parsed data.
         * Parsing will typically result in separate fields, such as year, month and day.
         * The returned builder can be used to combine the parsed data into meaningful
         * objects such as {@code LocalDate}, potentially applying complex processing
         * to handle invalid parsed data.
         *
         * @return a new builder with the results of the parse, not null
         */
        DateTimeBuilder toBuilder() {
            DateTimeBuilder builder = new DateTimeBuilder();
            for (Map.Entry<TemporalField, Long> fv : fieldValues.entrySet()) {
                builder.addFieldValue(fv.getKey(), fv.getValue());
            }
            if (chrono != null) {
                builder.addObject(chrono);
            } else if (chronology != null) {
                builder.addObject(chronology);
            }
//            builder.addCalendrical(getEffectiveChronology());
            if (zone != null) {
                builder.addObject(zone);
            }
            return builder;
        }
    }

    //-------------------------------------------------------------------------
    // for testing
    /**
     * Sets the locale.
     * <p>
     * This locale is used to control localization in the print output except
     * where localization is controlled by the symbols.
     *
     * @param locale  the locale, not null
     */
    void setLocale(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        this.locale = locale;
    }

}
