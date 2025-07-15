package com.rabbittownsoftware.convertly

/**
 * Singleton object that handles all unit conversion logic and definitions.
 */
object UnitConverter
{
    /**
     * Represents a unit of measurement.
     *
     * @param name Full name of the unit (e.g., "Meters").
     * @param abbreviation Short form (e.g., "m").
     * @param toBase Multiplier to convert the unit to its base form.
     */
    data class UnitDefinition(val name: String, val abbreviation: String, val toBase: Double)

    /**
     * Map of supported unit categories and their corresponding units.
     * Temperature is handled separately due to its non-linear conversions.
     */
    val unitCategories = mapOf(
        "Length" to listOf(
            UnitDefinition("Meters", "m", 1.0),
            UnitDefinition("Feet", "ft", 0.3048),
            UnitDefinition("Inches", "in", 0.0254),
            UnitDefinition("Kilometers", "km", 1000.0),
            UnitDefinition("Miles", "mi", 1609.34)
        ),
        "Weight" to listOf(
            UnitDefinition("Kilograms", "kg", 1.0),
            UnitDefinition("Pounds", "lb", 0.453592),
            UnitDefinition("Grams", "g", 0.001),
            UnitDefinition("Ounces", "oz", 0.0283495)
        ),
        "Area" to listOf(
            UnitDefinition("Square Meters", "m²", 1.0),
            UnitDefinition("Square Kilometers", "km²", 1_000_000.0),
            UnitDefinition("Square Feet", "ft²", 0.092903),
            UnitDefinition("Acres", "ac", 4046.86),
            UnitDefinition("Hectares", "ha", 10_000.0)
        ),
        "Volume" to listOf(
            UnitDefinition("Liters", "L", 1.0),
            UnitDefinition("Milliliters", "mL", 0.001),
            UnitDefinition("Cubic Meters", "m³", 1000.0),
            UnitDefinition("Cubic Inches", "in³", 0.0163871),
            UnitDefinition("Gallons", "gal", 3.78541)
        ),
        "Speed" to listOf(
            UnitDefinition("Meters/Second", "m/s", 1.0),
            UnitDefinition("Kilometers/Hour", "km/h", 0.277778),
            UnitDefinition("Miles/Hour", "mph", 0.44704),
            UnitDefinition("Feet/Second", "ft/s", 0.3048),
            UnitDefinition("Knots", "kn, kt", 0.514444)
        ),
        "Time" to listOf(
            UnitDefinition("Seconds", "s", 1.0),
            UnitDefinition("Minutes", "min", 60.0),
            UnitDefinition("Hours", "h", 3600.0),
            UnitDefinition("Days", "d", 86400.0)
        ),
        "Pressure" to listOf(
            UnitDefinition("Pascals", "Pa", 1.0),
            UnitDefinition("Bar", "bar", 100000.0),
            UnitDefinition("PSI", "psi", 6894.76),
            UnitDefinition("Atmospheres", "atm", 101325.0)
        ),
        "Energy" to listOf(
            UnitDefinition("Joules", "J", 1.0),
            UnitDefinition("Kilojoules", "kJ", 1000.0),
            UnitDefinition("Calories", "cal", 4.184),
            UnitDefinition("Kilocalories", "kcal", 4184.0)
        ),
        "Power" to listOf(
            UnitDefinition("Watts", "W", 1.0),
            UnitDefinition("Kilowatts", "kW", 1000.0),
            UnitDefinition("Horsepower", "hp", 745.7)
        ),
        "Data" to listOf(
            UnitDefinition("Bytes", "B", 1.0),
            UnitDefinition("Kilobytes", "KB", 1024.0),
            UnitDefinition("Megabytes", "MB", 1048576.0),
            UnitDefinition("Gigabytes", "GB", 1073741824.0),
            UnitDefinition("Terabytes", "TB", 1.0995e+12)
        ),
        "Force" to listOf(
            UnitDefinition("Newtons", "N", 1.0),
            UnitDefinition("Kilonewtons", "kN", 1000.0),
            UnitDefinition("Pound-force", "lbf", 4.44822)
        ),
        "Density" to listOf(
            UnitDefinition("kg/m³", "kg/m³", 1.0),
            UnitDefinition("g/cm³", "g/cm³", 1000.0),
            UnitDefinition("lb/ft³", "lb/ft³", 16.0185)
        ),
        "Frequency" to listOf(
            UnitDefinition("Hertz", "Hz", 1.0),
            UnitDefinition("Kilohertz", "kHz", 1000.0),
            UnitDefinition("Megahertz", "MHz", 1_000_000.0),
            UnitDefinition("Gigahertz", "GHz", 1_000_000_000.0)
        ),
        "Temperature" to listOf() // Special handling for non-linear conversions
    )

    /**
     * Returns a list of display strings for the given category.
     * For non-temperature units, combines name and abbreviation.
     */
    fun getDisplayList(category: String): List<String>
    {
        return if (category == "Temperature")
        {
            listOf("Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)")
        }
        else
        {
            unitCategories[category]?.map { "${it.name} (${it.abbreviation})" }
                ?: listOf("Unit")
        }
    }

    /**
     * Extracts the abbreviation portion from a display string (e.g., "Meters (m)" → "m").
     */
    private fun extractAbbreviation(display: String): String
    {
        val match = Regex("\\((.*?)\\)").find(display)
        return match?.groupValues?.get(1) ?: display
    }

    /**
     * Performs unit conversion between the given display strings.
     *
     * @param category The unit category (e.g., "Length").
     * @param value The input value to convert.
     * @param fromDisplay The full display string of the source unit.
     * @param toDisplay The full display string of the target unit.
     * @return Converted value as a Double.
     */
    fun convert(category: String, value: Double, fromDisplay: String, toDisplay: String): Double
    {
        val from = extractAbbreviation(fromDisplay)
        val to = extractAbbreviation(toDisplay)

        if (category == "Temperature")
        {
            return convertTemperature(value, from, to)
        }

        val units = unitCategories[category] ?: return value
        val fromDef = units.find { it.abbreviation == from } ?: return value
        val toDef = units.find { it.abbreviation == to } ?: return value

        val baseValue = value * fromDef.toBase
        return baseValue / toDef.toBase
    }

    /**
     * Converts between temperature scales, using Celsius as the intermediary.
     *
     * @param value The input temperature value.
     * @param from Source abbreviation ("°C", "°F", or "K").
     * @param to Target abbreviation.
     * @return Converted temperature as a Double.
     */
    private fun convertTemperature(value: Double, from: String, to: String): Double
    {
        val celsius = when (from)
        {
            "°C" -> value
            "°F" -> (value - 32) * 5 / 9
            "K" -> value - 273.15
            else -> value
        }

        return when (to)
        {
            "°C" -> celsius
            "°F" -> (celsius * 9 / 5) + 32
            "K" -> celsius + 273.15
            else -> celsius
        }
    }

    /**
     * Extracts the abbreviation from a display string. Same logic as [extractAbbreviation],
     * but publicly accessible.
     */
    fun getAbbreviation(display: String): String
    {
        val match = Regex("\\((.*?)\\)").find(display)
        return match?.groupValues?.get(1) ?: display
    }
}
