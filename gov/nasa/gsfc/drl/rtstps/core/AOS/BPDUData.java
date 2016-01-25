package gov.nasa.gsfc.drl.rtstps.core.AOS;

import gov.nasa.gsfc.drl.rtstps.core.Frame;
import gov.nasa.gsfc.drl.rtstps.core.Unit;

/**
 * Created by youngcle on 16-1-23.
 */
public class BPDUData extends Unit {
    /**
     * Create a frame.
     *
     *
     */
    public BPDUData() {
        super(0);
    }

    void setData(byte[] bpdudata)
    {
        data = bpdudata;
        deleted = false;
    }
}
