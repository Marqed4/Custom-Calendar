package com.zachery.customcalendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class Holidays
{
    public static final String CATEGORY_FEDERAL    = "federal";
    public static final String CATEGORY_OBSERVANCE = "observance";
    public static final String CATEGORY_RELIGIOUS  = "religious";

    public static class Holiday
    {
        public final String date; // "YYYY-MM-DD"
        public final String name;
        public final String shortName;
        public final String category;

        Holiday(LocalDate date, String name, String shortName, String category)
        {
            this.date = date.toString();
            this.name = name;
            this.shortName = shortName;
            this.category = category;
        }
    }

    // Returns all holidays for the given year across all categories.
    public static List<Holiday> forYear(int year)
    {
        List<Holiday> list = new ArrayList<>();
        addFederalHolidays(list, year);
        addObservances(list, year);
        addReligiousObservances(list, year);
        return list;
    }

    // Federal Holidays
    private static void addFederalHolidays(List<Holiday> list, int year)
    {
        // Fixed-date
        addFixed(list, year, Month.JANUARY,  1,  "New Year's Day",   "New Year's", CATEGORY_FEDERAL);
        addFixed(list, year, Month.JUNE,     19, "Juneteenth",       "Juneteenth", CATEGORY_FEDERAL);
        addFixed(list, year, Month.JULY,     4,  "Independence Day", "Indep. Day", CATEGORY_FEDERAL);
        addFixed(list, year, Month.NOVEMBER, 11, "Veterans Day",     "Veterans",   CATEGORY_FEDERAL);
        addFixed(list, year, Month.DECEMBER, 25, "Christmas Day",    "Christmas",  CATEGORY_FEDERAL);

        // Floating
        list.add(new Holiday(nthWeekday(year, Month.JANUARY,   DayOfWeek.MONDAY,    3),
            "Martin Luther King Jr. Day", "MLK Day",      CATEGORY_FEDERAL));
        list.add(new Holiday(nthWeekday(year, Month.FEBRUARY,  DayOfWeek.MONDAY,    3),
            "Presidents' Day",            "Pres. Day",    CATEGORY_FEDERAL));
        list.add(new Holiday(lastWeekday(year, Month.MAY,      DayOfWeek.MONDAY),
            "Memorial Day",               "Memorial",     CATEGORY_FEDERAL));
        list.add(new Holiday(nthWeekday(year, Month.SEPTEMBER, DayOfWeek.MONDAY,    1),
            "Labor Day",                  "Labor Day",    CATEGORY_FEDERAL));
        list.add(new Holiday(nthWeekday(year, Month.OCTOBER,   DayOfWeek.MONDAY,    2),
            "Columbus Day",               "Columbus",     CATEGORY_FEDERAL));
        list.add(new Holiday(nthWeekday(year, Month.NOVEMBER,  DayOfWeek.THURSDAY,  4),
            "Thanksgiving",               "Thanksgiving", CATEGORY_FEDERAL));
    }

    // Static Cultural Observances
    private static void addObservances(List<Holiday> list, int year)
    {
        // Fixed-date observances
        addSimple(list, year, Month.FEBRUARY,  2,  "Groundhog Day",      "Groundhog", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.FEBRUARY,  14, "Valentine's Day",    "Valentine's", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.MARCH,     17, "St. Patrick's Day",  "St. Pat's", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.APRIL,     22, "Earth Day",          "Earth Day", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.MAY,       5,  "Cinco de Mayo",      "Cinco Mayo", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.JUNE,      19, "Juneteenth (Obs.)",  "Juneteenth", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.JUNE,      26, "Trans Pride Day",    "Trans Pride", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.OCTOBER,   16, "Spirit Day",         "Spirit Day", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.OCTOBER,   31, "Halloween",          "Halloween", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.NOVEMBER,  1,  "Día de los Muertos", "Día Muertos", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.NOVEMBER,  2,  "Día de los Muertos", "Día Muertos", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.DECEMBER,  24, "Christmas Eve",      "Xmas Eve", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.DECEMBER,  31, "New Year's Eve",     "New Year's Eve", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.MARCH,     14, "Pi Day",             "Pi Day", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.APRIL,     1,  "April Fools' Day",   "April Fools", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.APRIL,     15, "Tax Day",            "Tax Day", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.APRIL,     20, "420",                "420", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.MAY,       4,  "Star Wars Day",      "May the 4th", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.MAY,       19, "Malcolm X Day",       "Malcolm X", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.MAY,       22, "Harvey Milk Day",    "Harvey Milk", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.JUNE,      14, "Flag Day",           "Flag Day", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.SEPTEMBER, 11, "Patriot Day",        "Patriot Day", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.SEPTEMBER, 23, "Bisexual Visibility Day", "Bi Visibility", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.OCTOBER,   11, "National Coming Out Day", "Coming Out", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.OCTOBER,   26, "Intersex Awareness Day", "Intersex Day", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.NOVEMBER,  20, "Transgender Day of Remembrance", "Trans Rememb.", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.DECEMBER,  1,  "Rosa Parks Day", "Rosa Parks", CATEGORY_OBSERVANCE);
        addSimple(list, year, Month.DECEMBER,  7,  "Pearl Harbor Remembrance Day", "Pearl Harbor",  CATEGORY_OBSERVANCE);

        // Dynamic Cultural Observances
        // Mother's Day – 2nd Sunday of May
        list.add(new Holiday(nthWeekday(year, Month.MAY,  DayOfWeek.SUNDAY, 2),
            "Mother's Day", "Mother's", CATEGORY_OBSERVANCE));

        // Father's Day – 3rd Sunday of June
        list.add(new Holiday(nthWeekday(year, Month.JUNE, DayOfWeek.SUNDAY, 3),
            "Father's Day", "Father's", CATEGORY_OBSERVANCE));

        // Mardi Gras – 47 days before Easter (Fat Tuesday)
        LocalDate easter = computeEaster(year);
        list.add(new Holiday(easter.minusDays(47),
            "Mardi Gras", "Mardi Gras", CATEGORY_OBSERVANCE));
    }

    // Religious Observances

    private static void addReligiousObservances(List<Holiday> list, int year)
    {
        LocalDate easter = computeEaster(year);

        // Easter-relative
        list.add(new Holiday(easter.minusDays(46), "Ash Wednesday",  "Ash Wed",    CATEGORY_RELIGIOUS));
        list.add(new Holiday(easter.minusDays(7),  "Palm Sunday",    "Palm Sun",   CATEGORY_RELIGIOUS));
        list.add(new Holiday(easter.minusDays(3),  "Holy Thursday",  "Holy Thu",   CATEGORY_RELIGIOUS));
        list.add(new Holiday(easter.minusDays(2),  "Good Friday",    "Good Fri",   CATEGORY_RELIGIOUS));
        list.add(new Holiday(easter.minusDays(1),  "Holy Saturday",  "Holy Sat",   CATEGORY_RELIGIOUS));
        list.add(new Holiday(easter,               "Easter Sunday",  "Easter",     CATEGORY_RELIGIOUS));
        list.add(new Holiday(easter.plusDays(39),  "Ascension Day",  "Ascension",  CATEGORY_RELIGIOUS));
        list.add(new Holiday(easter.plusDays(49),  "Pentecost",      "Pentecost",  CATEGORY_RELIGIOUS));

        // Fixed-date religious observances
        addSimple(list, year, Month.DECEMBER, 24, "Christmas Eve",   "Xmas Eve",  CATEGORY_RELIGIOUS);
        addSimple(list, year, Month.DECEMBER, 25, "Christmas Day",   "Christmas", CATEGORY_RELIGIOUS);
        addSimple(list, year, Month.DECEMBER, 26, "Kwanzaa Begins",  "Kwanzaa",   CATEGORY_RELIGIOUS);

        // Hanukkah – 25 Kislev (approximate via algorithm; shifts yearly)
        LocalDate hanukkah = computeHanukkah(year);
        for (int i = 0; i < 8; i++)
        {
            LocalDate day = hanukkah.plusDays(i);
            String label = i == 0 ? "Hanukkah Begins" : (i == 7 ? "Hanukkah Ends" : "Hanukkah");
            list.add(new Holiday(day, label, "Hanukkah", CATEGORY_RELIGIOUS));
        }
    }

    // Easter Algorithm

    /**
     * Computes Easter Sunday for the given year using the Anonymous Gregorian algorithm.
     */
    static LocalDate computeEaster(int year)
    {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day   = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }

    // Hanukkah Algorithm

    /**
     * Approximates the Gregorian date of the first night of Hanukkah (25 Kislev)
     * using the Hebrew calendar epoch offset.
     */
    static LocalDate computeHanukkah(int year)
    {
        // Hebrew year starts in autumn; Kislev 25 falls in Nov/Dec of the civil year
        // Use a well-known approximation based on the Metonic cycle

        int hebrewYear = year + 3761; // approx Hebrew year for the Kislev of civil year
        // Molad of Tishri (simplified): epoch Jan 1, 1 CE = 1 Tishri 3761 HY
        // Average Hebrew year ≈ 365.2468 days
        double HEBREW_YEAR_DAYS = 365.24682220903; // mean Hebrew year
        double EPOCH_JD = 347996.5; // Julian Day of 1 Tishri 3761 (approx 7 Oct 3761 BCE)

        // Days from epoch to 1 Tishri of the relevant Hebrew year
        long daysSinceEpoch = Math.round((hebrewYear - 3761) * HEBREW_YEAR_DAYS);

        // 1 Tishri 1 CE ≈ October 7, 3761 BCE → Julian Day 347998
        // We'll anchor off a known date instead: Hanukkah 2000 = Dec 22
        // Known anchor: Hanukkah 5761 (year 2000) started Dec 22, 2000
        int ANCHOR_YEAR    = 2000;
        LocalDate ANCHOR   = LocalDate.of(2000, 12, 22);
        // Average days per Hebrew year
        long diff = Math.round((year - ANCHOR_YEAR) * HEBREW_YEAR_DAYS);
        LocalDate approx = ANCHOR.plusDays(diff);

        // Snap to the nearest December (Hanukkah is always late Nov – late Dec)
        // If we overshot into January, back up a Hebrew year
        if (approx.getMonthValue() == 1) approx = approx.minusDays(Math.round(HEBREW_YEAR_DAYS));
        // If we're in November too early, nudge forward
        if (approx.getMonthValue() == 11 && approx.getDayOfMonth() < 20)
            approx = approx.plusDays(Math.round(HEBREW_YEAR_DAYS));

        return approx;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Fixed-date holiday with observed date for federal holidays. */
    private static void addFixed(List<Holiday> list, int year, Month month, int day, String name, String shortName, String category)
    {
        LocalDate actual = LocalDate.of(year, month, day);
        list.add(new Holiday(actual, name, shortName, category));

        DayOfWeek dow = actual.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY)
            list.add(new Holiday(actual.minusDays(1), name + " (Observed)", shortName + "*", category));
        else if (dow == DayOfWeek.SUNDAY)
            list.add(new Holiday(actual.plusDays(1),  name + " (Observed)", shortName + "*", category));
    }

    /** Simple fixed-date entry with no observed-date shift. */
    private static void addSimple(List<Holiday> list, int year, Month month, int day, String name, String shortName, String category)
    {
        list.add(new Holiday(LocalDate.of(year, month, day), name, shortName, category));
    }

    private static LocalDate nthWeekday(int year, Month month, DayOfWeek dow, int n)
    {
        return LocalDate.of(year, month, 1)
            .with(TemporalAdjusters.nextOrSame(dow))
            .plusWeeks(n - 1);
    }

    private static LocalDate lastWeekday(int year, Month month, DayOfWeek dow)
    {
        return LocalDate.of(year, month, 1)
            .with(TemporalAdjusters.lastDayOfMonth())
            .with(TemporalAdjusters.previousOrSame(dow));
    }
}