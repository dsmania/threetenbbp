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
package org.threeten.bp.temporal;

import org.threeten.bp.DateTimeException;
import org.threeten.bp.Duration;
import org.threeten.bp.Period;

/**
 * A unit of date-time, such as Days or Hours.
 * <p>
 * Measurement of time is built on units, such as years, months, days, hours, minutes and seconds.
 * Implementations of this interface represent those units.
 * <p>
 * An instance of this interface represents the unit itself, rather than an amount of the unit.
 * See {@link Period} for a class that represents an amount in terms of the common units.
 * <p>
 * The most commonly used units are defined in {@link ChronoUnit}.
 * Further units are supplied in {@link ISOFields}.
 * Units can also be written by application code by implementing this interface.
 * <p>
 * The unit works using double dispatch. Client code calls methods on a date-time like
 * {@code LocalDateTime} which check if the unit is a {@code ChronoUnit}.
 * If it is, then the date-time must handle it.
 * Otherwise, the method call is re-dispatched to the matching method in this interface.
 *
 * <h3>Specification for implementors</h3>
 * This interface must be implemented with care to ensure other classes operate correctly.
 * All implementations that can be instantiated must be final, immutable and thread-safe.
 * It is recommended to use an enum where possible.
 */
public interface TemporalUnit {

    /**
     * Gets a descriptive name for the unit.
     * <p>
     * This should be in the plural and upper-first camel case, such as 'Days' or 'Minutes'.
     *
     * @return the name, not null
     */
    String getName();

    /**
     * Gets the duration of this unit, which may be an estimate.
     * <p>
     * All units return a duration measured in standard nanoseconds from this method.
     * For example, an hour has a duration of {@code 60 * 60 * 1,000,000,000ns}.
     * <p>
     * Some units may return an accurate duration while others return an estimate.
     * For example, days have an estimated duration due to the possibility of
     * daylight saving time changes.
     * To determine if the duration is an estimate, use {@link #isDurationEstimated()}.
     *
     * @return the duration of this unit, which may be an estimate, not null
     */
    Duration getDuration();

    /**
     * Checks if the duration of the unit is an estimate.
     * <p>
     * All units have a duration, however the duration is not always accurate.
     * For example, days have an estimated duration due to the possibility of
     * daylight saving time changes.
     * This method returns true if the duration is an estimate and false if it is
     * accurate. Note that accurate/estimated ignores leap seconds.
     *
     * @return true if the duration is estimated, false if accurate
     */
    boolean isDurationEstimated();

    //-----------------------------------------------------------------------
    /**
     * Checks if this unit is supported by the specified temporal object.
     * <p>
     * This checks that the implementing date-time can add/subtract this unit.
     * This can be used to avoid throwing an exception.
     *
     * @param temporal  the temporal object to check, not null
     * @return true if the unit is supported
     */
    boolean isSupported(Temporal temporal);

    /**
     * Returns a copy of the specified temporal object with the specified period added.
     * <p>
     * The period added is a multiple of this unit. For example, this method
     * could be used to add "3 days" to a date by calling this method on the
     * instance representing "days", passing the date and the period "3".
     * The period to be added may be negative, which is equivalent to subtraction.
     * <p>
     * There are two equivalent ways of using this method.
     * The first is to invoke this method directly.
     * The second is to use {@link Temporal#plus(long, TemporalUnit)}:
     * <pre>
     *   // these two lines are equivalent, but the second approach is recommended
     *   temporal = thisUnit.doPlus(temporal);
     *   temporal = temporal.plus(thisUnit);
     * </pre>
     * It is recommended to use the second approach, {@code plus(TemporalUnit)},
     * as it is a lot clearer to read in code.
     * <p>
     * Implementations should perform any queries or calculations using the units
     * available in {@link ChronoUnit} or the fields available in {@link ChronoField}.
     * If the field is not supported a {@code DateTimeException} must be thrown.
     * <p>
     * Implementations must not alter the specified temporal object.
     * Instead, an adjusted copy of the original must be returned.
     * This provides equivalent, safe behavior for immutable and mutable implementations.
     *
     * @param <R>  the type of the Temporal object
     * @param dateTime  the temporal object to adjust, not null
     * @param periodToAdd  the period of this unit to add, positive or negative
     * @return the adjusted temporal object, not null
     * @throws DateTimeException if the period cannot be added
     */
    <R extends Temporal> R doPlus(R dateTime, long periodToAdd);

    //-----------------------------------------------------------------------
    /**
     * Calculates the period in terms of this unit between two temporal objects of the same type.
     * <p>
     * The period will be positive if the second date-time is after the first, and
     * negative if the second date-time is before the first.
     * Call {@link SimplePeriod#abs() abs()} on the result to ensure that the result
     * is always positive.
     * <p>
     * The result can be queried for the {@link SimplePeriod#getAmount() amount}, the
     * {@link SimplePeriod#getUnit() unit} and used directly in addition/subtraction:
     * <pre>
     *  date = date.minus(MONTHS.between(start, end));
     * </pre>
     *
     * @param <R>  the type of the Temporal object; the two date-times must be of the same type
     * @param dateTime1  the base temporal object, not null
     * @param dateTime2  the other temporal object, not null
     * @return the period between datetime1 and datetime2 in terms of this unit;
     *      positive if datetime2 is later than datetime1, not null
     */
    <R extends Temporal> SimplePeriod between(R dateTime1, R dateTime2);

    //-----------------------------------------------------------------------
    /**
     * Outputs this unit as a {@code String} using the name.
     *
     * @return the name of this unit, not null
     */
    @Override
    String toString();

}