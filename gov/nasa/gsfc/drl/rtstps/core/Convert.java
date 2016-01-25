/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

/**
 * This class contains some static conversion utility methods to convert XML
 * element attributes to useable values.
 * 
 */
public final class Convert
{
    /**
     * Convert an XML element's attribute value to an integer.
     * @param element The XML element
     * @param key The attribute name (key=value)
     * @param defaultValue The returned value if the attribute was not
     *          specified with the element.
     * @return The integer value (key=value) or the default value if the
     *          attribute was not specified.
     * @throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException If the value could not be converted
     *          to an integer.
     */
    public static int toInteger(org.w3c.dom.Element element,
            String key, int defaultValue) throws RtStpsException
    {
        int v = defaultValue;
        String value = element.getAttribute(key);
        if (value.length() > 0)
        {
            try
            {
                v = Integer.valueOf(value).intValue();
            }
            catch (NumberFormatException nfe)
            {
                throw new RtStpsException(element.getTagName() +
                    " attribute " + key + " is not an integer.");
            }
        }
        return v;
    }

    /**
     * Convert an XML element's attribute value to an integer. The value must
     * be greater than or equal to a minimum.
     * @param element The XML element
     * @param key The attribute name (key=value)
     * @param defaultValue The returned value if the attribute was not
     *          specified with the element.
     * minimum The minimum integer value the value can take.
     * @return The integer value (key=value) or the default value if the
     *          attribute was not specified.
     * @throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException If the value could not be converted
     *          to an integer, or the value is less than the minimum.
     */
    public static int toInteger(org.w3c.dom.Element element,
            String key, int defaultValue, int minimum) throws RtStpsException
    {
        int v = toInteger(element,key,defaultValue);
        if (v < minimum)
        {
            throw new RtStpsException(element.getTagName() +
                " attribute " + key + " must be >= " + minimum);
        }
        return v;
    }

    /**
     * Convert an XML element's attribute value to an integer. The value must
     * be greater than or equal to a minimum and less than or equal to a
     * maximum.
     * @param element The XML element
     * @param key The attribute name (key=value)
     * @param defaultValue The returned value if the attribute was not
     *          specified with the element.
     * minimum The minimum integer value the value can take.
     * maximum The maximum integer value the value can take.
     * @return The integer value (key=value) or the default value if the
     *          attribute was not specified.
     * @throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException If the value could not be converted
     *          to an integer, or the value is less than the minimum, or the
     *          value is greater than the maximum.
     */
    public static int toInteger(org.w3c.dom.Element element,
            String key, int defaultValue, int minimum, int maximum)
            throws RtStpsException
    {
        int v = toInteger(element,key,defaultValue);
        if (v < minimum || v > maximum)
        {
            throw new RtStpsException(element.getTagName() +
                " attribute " + key + " must be >= " + minimum +
                " and <= " + maximum);
        }
        return v;
    }

    /**
     * Convert an XML element's attribute value to an integer. It assumes the
     * string is in hexadecimal format.
     * @param element The XML element
     * @param key The attribute name (key=value)
     * @param defaultValue The returned value if the attribute was not
     *          specified with the element.
     * @return The integer value (key=value) or the default value if the
     *          attribute was not specified.
     * @throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException If the value could not be converted
     *          to an integer.
     */
    public static int toHexInteger(org.w3c.dom.Element element,
            String key, int defaultValue) throws RtStpsException
    {
        int v = defaultValue;
        String value = element.getAttribute(key);
        if (value.length() > 0)
        {
            try
            {
                v = Integer.valueOf(value,16).intValue();
            }
            catch (NumberFormatException nfe)
            {
                throw new RtStpsException(element.getTagName() +
                    " attribute " + key + " is not a hexadecimal integer.");
            }
        }
        return v;
    }

    /**
     * Convert an XML element's attribute value to a boolean. It assumes the
     * string is either "true" or "false".
     * @param element The XML element
     * @param key The attribute name (key=value)
     * @param defaultValue The returned value if the attribute was not
     *          specified with the element.
     * @return The boolean value (key=value) or the default value if the
     *          attribute was not specified.
     * @throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException If the value could not be
     *          converted to a boolean.
     */
     public static boolean toBoolean(org.w3c.dom.Element element,
            String key, boolean defaultValue) throws RtStpsException
    {
        boolean b = defaultValue;
        String value = element.getAttribute(key);
        if (value.length() > 0)
        {
            if (value.equals("true") || value.equals("false"))
            {
                b = Boolean.valueOf(value).booleanValue();
            }
            else
            {
                throw new RtStpsException(element.getTagName() +
                    " attribute " + key + " must have true or false value.");
            }
        }
        return b;
    }
}
