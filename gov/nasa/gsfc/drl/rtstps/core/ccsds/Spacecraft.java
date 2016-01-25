/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds;
import gov.nasa.gsfc.drl.rtstps.core.Convert;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

/**
 * This class contains information about one spacecraft.
 * 
 */
public class Spacecraft
{
    /**
     * The spacecraft name.
     */
    private String name;

    /**
     * A spacecraft identification number. The spacecraft ID is found
     * embedded in CCSDS VCDUs.
     */
    public int spid;

    /**
     * If true, CCSDS VCDUs for this spacecraft contain a 16-bit header
     * error control field.
     */
    public boolean headerErrorControlPresent = false;

    /**
     * If true, the software decodes and corrects the VCDU header for
     * this spacecraft. This field is ignored if the header error control
     * field is not present.
     */
    public boolean doHeaderDecode = false;

    /**
     * The insert zone, if present, also exists in fill frames.
     */
    public int insertZoneLength = 0;

    /**
     * Get the spacecraft name.
     */
    public final String getName()
    {
        return name;
    }

    public Spacecraft(String sname,int sid) throws RtStpsException{
        name = sname;
        spid = sid;
    }

    public Spacecraft(org.w3c.dom.Element element) throws RtStpsException
    {
        name = element.getAttribute("label");

        spid = Convert.toInteger(element,"id",spid);

        headerErrorControlPresent = Convert.toBoolean(element,
                "headerErrorControlPresent",headerErrorControlPresent);


        doHeaderDecode = Convert.toBoolean(element,
                "doHeaderDecode",doHeaderDecode);

        insertZoneLength = Convert.toInteger(element,"insertZoneLength",
                insertZoneLength);
    }


    public String toString()
    {
        return name + " id=" + spid;
    }
}
