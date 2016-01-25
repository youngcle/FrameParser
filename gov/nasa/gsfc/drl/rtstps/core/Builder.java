/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;
import gov.nasa.gsfc.drl.rtstps.core.AOS.AOSDEFINE_xx103;
import gov.nasa.gsfc.drl.rtstps.core.AOS.AOSService;
import gov.nasa.gsfc.drl.rtstps.core.AOS.AOSVcduService;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.BitstreamService;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.CaduService;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Spacecraft;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.VcduService;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.path.PacketPipeline;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.path.PathService;
import gov.nasa.gsfc.drl.rtstps.core.fs.FrameSynchronizer;
import gov.nasa.gsfc.drl.rtstps.core.output.NullChannel;
import gov.nasa.gsfc.drl.rtstps.core.output.PacketChannel;
import gov.nasa.gsfc.drl.rtstps.core.output.PacketChannelA;
import gov.nasa.gsfc.drl.rtstps.core.output.PacketChannelB;
import gov.nasa.gsfc.drl.rtstps.core.output.UnitChannel;
import gov.nasa.gsfc.drl.rtstps.core.output.UnitChannelA;
import gov.nasa.gsfc.drl.rtstps.core.output.UnitChannelB;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDROutput;

import java.util.Iterator;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class creates an RT-STPS pipeline.
 * 
 * 
 */
public class Builder
{
    private RtStpsNodeFactory nodeFactory = new RtStpsNodeFactory();
    private DocumentBuilder documentBuilder = null;
    private Configuration config = null;

    public Builder() throws RtStpsException
    {
        //I create my node factory.
        populateNodeFactory();

//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        dbf.setValidating(true);
//        dbf.setIgnoringComments(true);
//        dbf.setIgnoringElementContentWhitespace(true);
//
//        try
//        {
//            documentBuilder = dbf.newDocumentBuilder();
//            documentBuilder.setErrorHandler(new MyErrorHandler());
//        }
//        catch (ParserConfigurationException pce)
//        {
//            throw new RtStpsException(pce);
//        }
    }

    /**
     * Get the STPS node factory.
     */
    public final RtStpsNodeFactory getStpsNodeFactory()
    {
        return nodeFactory;
    }

    /**
     * Get the last-used configuration. The builder does not change the
     * configuration until after it creates an STPS pipeline. It will be
     * null if no configuration has ever been loaded.
     */
    public final Configuration getConfiguration()
    {
        return config;
    }


    public FrameSynchronizer createChain103() throws RtStpsException{
        //The cid is a description of the configuration, which I made as a
        //root element attribute.
        String cid = "xx103";

        //I use the configuration object to store shared information.
        config = new Configuration(cid,nodeFactory);

        //I do the spacecraft first so that RT-STPS nodes that need them may
        //map them immediately.
        Spacecraft s = new Spacecraft(cid,172);

        //Create all STPS nodes. I save the links element to process later.
        java.util.TreeMap<String,RtStpsNode> stpsNodes = config.getStpsNodes();

        RtStpsNode frameSynchronizer = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)frameSynchronizer).setupFS(AOSDEFINE_xx103.AOS_INTERLEAVED_FRAME_SYNC,
                AOSDEFINE_xx103.AOS_INTERLEAVED_FRAME_SYNC_LENGTH,
                AOSDEFINE_xx103.AOS_INTERLEAVED_FRAME_LENGTH,
                config);
        frameSynchronizer.setLinkName("frame_sync_interleaved");
        stpsNodes.put(frameSynchronizer.getLinkName(),frameSynchronizer);

        RtStpsNode frameStatus = nodeFactory.create("frame_status");
        frameStatus.load(null,config);
        frameStatus.setLinkName("frame_status");
        stpsNodes.put(frameStatus.getLinkName(),frameStatus);

        RtStpsNode deinterleaver = nodeFactory.create("deinterleaver");
        ((Deinterleaver)deinterleaver).dlsetup(0,1);
        deinterleaver.setLinkName("deinterleaver");
        stpsNodes.put(deinterleaver.getLinkName(),deinterleaver);

        RtStpsNode aosService = nodeFactory.create("aos_service");
        aosService.setLinkName("aosService");
        ((AOSService)aosService).AOSServiceSetup();
        stpsNodes.put(aosService.getLinkName(),aosService);

        RtStpsNode VCDU_CACCD1 = nodeFactory.create("VCDUservice");
        ((AOSVcduService)VCDU_CACCD1).vcduSetup();
        VCDU_CACCD1.setLinkName("VCDU_CACCD1");
        stpsNodes.put(VCDU_CACCD1.getLinkName(),VCDU_CACCD1);

        RtStpsNode VCDU_CACCD2= nodeFactory.create("VCDUservice");
        ((AOSVcduService)VCDU_CACCD2).vcduSetup();
        VCDU_CACCD2.setLinkName("VCDU_CACCD2");
        stpsNodes.put(VCDU_CACCD2.getLinkName(),VCDU_CACCD2);

        RtStpsNode VCDU_CACCD3 = nodeFactory.create("VCDUservice");
        ((AOSVcduService)VCDU_CACCD3).vcduSetup();
        VCDU_CACCD3.setLinkName("VCDU_CACCD3");
        stpsNodes.put(VCDU_CACCD3.getLinkName(),VCDU_CACCD3);

        RtStpsNode VCDU_CACCD4 = nodeFactory.create("VCDUservice");
        ((AOSVcduService)VCDU_CACCD4).vcduSetup();
        VCDU_CACCD4.setLinkName("VCDU_CACCD4");
        stpsNodes.put(VCDU_CACCD4.getLinkName(),VCDU_CACCD4);


        RtStpsNode VCDU_CBCCD1 = nodeFactory.create("VCDUservice");
        ((AOSVcduService)VCDU_CBCCD1).vcduSetup();
        VCDU_CBCCD1.setLinkName("VCDU_CBCCD1");
        stpsNodes.put(VCDU_CBCCD1.getLinkName(),VCDU_CBCCD1);

        RtStpsNode VCDU_CBCCD2= nodeFactory.create("VCDUservice");
        ((AOSVcduService)VCDU_CBCCD2).vcduSetup();
        VCDU_CBCCD2.setLinkName("VCDU_CBCCD2");
        stpsNodes.put(VCDU_CBCCD2.getLinkName(),VCDU_CBCCD2);

        RtStpsNode VCDU_CBCCD3 = nodeFactory.create("VCDUservice");
        ((AOSVcduService)VCDU_CBCCD3).vcduSetup();
        VCDU_CBCCD3.setLinkName("VCDU_CBCCD3");
        stpsNodes.put(VCDU_CBCCD3.getLinkName(),VCDU_CBCCD3);

        RtStpsNode VCDU_CBCCD4 = nodeFactory.create("VCDUservice");
        ((AOSVcduService)VCDU_CBCCD4).vcduSetup();
        VCDU_CBCCD4.setLinkName("VCDU_CBCCD4");
        stpsNodes.put(VCDU_CBCCD4.getLinkName(),VCDU_CBCCD4);


        RtStpsNode VCDU_CAMSS1 = nodeFactory.create("VCDUservice");
        ((AOSVcduService)VCDU_CAMSS1).vcduSetup();
        VCDU_CAMSS1.setLinkName("VCDU_CAMSS1");
        stpsNodes.put(VCDU_CAMSS1.getLinkName(),VCDU_CAMSS1);

        RtStpsNode VCDU_CAMSS2 = nodeFactory.create("VCDUservice");
        ((AOSVcduService)VCDU_CAMSS2).vcduSetup();
        VCDU_CAMSS2.setLinkName("VCDU_CAMSS1");
        stpsNodes.put(VCDU_CAMSS2.getLinkName(),VCDU_CAMSS2);

        RtStpsNode VCDU_CBMSS1 = nodeFactory.create("VCDUservice");
        ((AOSVcduService)VCDU_CBMSS1).vcduSetup();
        VCDU_CBMSS1.setLinkName("VCDU_CBMSS1");
        stpsNodes.put(VCDU_CBMSS1.getLinkName(),VCDU_CBMSS1);

        RtStpsNode VCDU_CBMSS2 = nodeFactory.create("VCDUservice");
        ((AOSVcduService)VCDU_CBMSS2).vcduSetup();
        VCDU_CBMSS2.setLinkName("VCDU_CBMSS1");
        stpsNodes.put(VCDU_CBMSS2.getLinkName(),VCDU_CBMSS2);





        RtStpsNode caccd1PacketFS = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)caccd1PacketFS).setupFS(AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC,
                AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC_LENGTH,
                AOSDEFINE_xx103.COMPRESSED_FRAME_LENGTH,
                config);
        caccd1PacketFS.setLinkName("frame_sync_caccd1");
        stpsNodes.put(caccd1PacketFS.getLinkName(),caccd1PacketFS);

        RtStpsNode caccd2PacketFS = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)caccd2PacketFS).setupFS(AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC,
                 AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC_LENGTH,
                 AOSDEFINE_xx103.COMPRESSED_FRAME_LENGTH,
                 config);
        caccd2PacketFS.setLinkName("frame_sync_caccd2");
        stpsNodes.put(caccd2PacketFS.getLinkName(),caccd2PacketFS);

        RtStpsNode caccd3PacketFS = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)caccd3PacketFS).setupFS(AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC,
                AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC_LENGTH,
                AOSDEFINE_xx103.COMPRESSED_FRAME_LENGTH,
                config);
        caccd3PacketFS.setLinkName("frame_sync_caccd3");
        stpsNodes.put(caccd3PacketFS.getLinkName(),caccd3PacketFS);

        RtStpsNode caccd4PacketFS = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)caccd4PacketFS).setupFS(AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC,
                AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC_LENGTH,
                AOSDEFINE_xx103.COMPRESSED_FRAME_LENGTH,
                config);
        caccd4PacketFS.setLinkName("frame_sync_caccd4");
        stpsNodes.put(caccd4PacketFS.getLinkName(),caccd4PacketFS);

        RtStpsNode cbccd1PacketFS = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)cbccd1PacketFS).setupFS(AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC,
                AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC_LENGTH,
                AOSDEFINE_xx103.COMPRESSED_FRAME_LENGTH,
                config);
        cbccd1PacketFS.setLinkName("frame_sync_cbccd1");
        stpsNodes.put(cbccd1PacketFS.getLinkName(),cbccd1PacketFS);


        RtStpsNode cbccd2PacketFS = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)cbccd2PacketFS).setupFS(AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC,
                AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC_LENGTH,
                AOSDEFINE_xx103.COMPRESSED_FRAME_LENGTH,
                config);
        cbccd2PacketFS.setLinkName("frame_sync_cbccd2");
        stpsNodes.put(cbccd2PacketFS.getLinkName(),cbccd2PacketFS);

        RtStpsNode cbccd3PacketFS = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)cbccd3PacketFS).setupFS(AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC,
                AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC_LENGTH,
                AOSDEFINE_xx103.COMPRESSED_FRAME_LENGTH,
                config);
        cbccd3PacketFS.setLinkName("frame_sync_cbccd3");
        stpsNodes.put(cbccd3PacketFS.getLinkName(),cbccd3PacketFS);

        RtStpsNode cbccd4PacketFS = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)cbccd4PacketFS).setupFS(AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC,
                AOSDEFINE_xx103.COMPRESSED_BLOCK1_FRAME_SYNC_LENGTH,
                AOSDEFINE_xx103.COMPRESSED_FRAME_LENGTH,
                config);
        cbccd4PacketFS.setLinkName("frame_sync_cbccd4");
        stpsNodes.put(cbccd4PacketFS.getLinkName(),cbccd4PacketFS);

        RtStpsNode camss1PacketFS = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)camss1PacketFS).setupFS(AOSDEFINE_xx103.COMPRESSED_MSS_FRAME_SYNC,
                4,
                AOSDEFINE_xx103.COMPRESSED_MSS_FRAME_LENGTH,
                config);
        camss1PacketFS.setLinkName("frame_sync_camss1");
        stpsNodes.put(camss1PacketFS.getLinkName(),camss1PacketFS);


        RtStpsNode camss2PacketFS = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)camss2PacketFS).setupFS(AOSDEFINE_xx103.COMPRESSED_MSS_FRAME_SYNC,
                4,
                AOSDEFINE_xx103.COMPRESSED_MSS_FRAME_LENGTH,
                config);
        camss2PacketFS.setLinkName("frame_sync_camss2");
        stpsNodes.put(camss2PacketFS.getLinkName(),camss2PacketFS);

        RtStpsNode cbmss1PacketFS = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)cbmss1PacketFS).setupFS(AOSDEFINE_xx103.COMPRESSED_MSS_FRAME_SYNC,
                4,
                AOSDEFINE_xx103.COMPRESSED_MSS_FRAME_LENGTH,
                config);
        cbmss1PacketFS.setLinkName("frame_sync_cbmss1");
        stpsNodes.put(cbmss1PacketFS.getLinkName(),cbmss1PacketFS);

        RtStpsNode cbmss2PacketFS = nodeFactory.create("frame_sync");
        ((FrameSynchronizer)cbmss2PacketFS).setupFS(AOSDEFINE_xx103.COMPRESSED_MSS_FRAME_SYNC,
                4,
                AOSDEFINE_xx103.COMPRESSED_MSS_FRAME_LENGTH,
                config);
        cbmss2PacketFS.setLinkName("frame_sync_cbmss2");
        stpsNodes.put(cbmss2PacketFS.getLinkName(),cbmss2PacketFS);


        RtStpsNode OUT_CACCD1 = nodeFactory.create("FRAME_NONE");
        OUT_CACCD1.setLinkName("OUT_CACCD1");
        ((UnitChannel)OUT_CACCD1).setOutput("file",OUT_CACCD1.getLinkName());
        stpsNodes.put(OUT_CACCD1.getLinkName(),OUT_CACCD1);

        RtStpsNode OUT_CACCD2 = nodeFactory.create("FRAME_NONE");
        OUT_CACCD2.setLinkName("OUT_CACCD2");
        ((UnitChannel)OUT_CACCD2).setOutput("file",OUT_CACCD2.getLinkName());
        stpsNodes.put(OUT_CACCD2.getLinkName(),OUT_CACCD2);

        RtStpsNode OUT_CACCD3 = nodeFactory.create("FRAME_NONE");
        OUT_CACCD3.setLinkName("OUT_CACCD3");
        ((UnitChannel)OUT_CACCD3).setOutput("file",OUT_CACCD3.getLinkName());
        stpsNodes.put(OUT_CACCD3.getLinkName(),OUT_CACCD3);

        RtStpsNode OUT_CACCD4 = nodeFactory.create("FRAME_NONE");
        OUT_CACCD4.setLinkName("OUT_CACCD4");
        ((UnitChannel)OUT_CACCD4).setOutput("file",OUT_CACCD4.getLinkName());
        stpsNodes.put(OUT_CACCD4.getLinkName(),OUT_CACCD4);

        RtStpsNode OUT_CBCCD1 = nodeFactory.create("FRAME_NONE");
        OUT_CBCCD1.setLinkName("OUT_CBCCD1");
        ((UnitChannel)OUT_CBCCD1).setOutput("file",OUT_CBCCD1.getLinkName());
        stpsNodes.put(OUT_CBCCD1.getLinkName(),OUT_CBCCD1);

        RtStpsNode OUT_CBCCD2 = nodeFactory.create("FRAME_NONE");
        OUT_CBCCD2.setLinkName("OUT_CBCCD2");
        ((UnitChannel)OUT_CBCCD2).setOutput("file",OUT_CBCCD2.getLinkName());
        stpsNodes.put(OUT_CBCCD2.getLinkName(),OUT_CBCCD2);

        RtStpsNode OUT_CBCCD3 = nodeFactory.create("FRAME_NONE");
        OUT_CBCCD3.setLinkName("OUT_CBCCD3");
        ((UnitChannel)OUT_CBCCD3).setOutput("file",OUT_CBCCD3.getLinkName());
        stpsNodes.put(OUT_CBCCD3.getLinkName(),OUT_CBCCD3);

        RtStpsNode OUT_CBCCD4 = nodeFactory.create("FRAME_NONE");
        OUT_CBCCD4.setLinkName("OUT_CBCCD4");
        ((UnitChannel)OUT_CBCCD4).setOutput("file",OUT_CBCCD4.getLinkName());
        stpsNodes.put(OUT_CBCCD4.getLinkName(),OUT_CBCCD4);


        RtStpsNode OUT_CAMSS1 = nodeFactory.create("FRAME_NONE");
        OUT_CAMSS1.setLinkName("OUT_CAMSS1");
        ((UnitChannel)OUT_CAMSS1).setOutput("file",OUT_CAMSS1.getLinkName());
        stpsNodes.put(OUT_CAMSS1.getLinkName(),OUT_CAMSS1);

        RtStpsNode OUT_CAMSS2 = nodeFactory.create("FRAME_NONE");
        OUT_CAMSS2.setLinkName("OUT_CAMSS2");
        ((UnitChannel)OUT_CAMSS2).setOutput("file",OUT_CAMSS2.getLinkName());
        stpsNodes.put(OUT_CAMSS2.getLinkName(),OUT_CAMSS2);

        RtStpsNode OUT_CBMSS1 = nodeFactory.create("FRAME_NONE");
        OUT_CBMSS1.setLinkName("OUT_CBMSS1");
        ((UnitChannel)OUT_CBMSS1).setOutput("file",OUT_CBMSS1.getLinkName());
        stpsNodes.put(OUT_CBMSS1.getLinkName(),OUT_CBMSS1);

        RtStpsNode OUT_CBMSS2 = nodeFactory.create("FRAME_NONE");
        OUT_CBMSS2.setLinkName("OUT_CBMSS2");
        ((UnitChannel)OUT_CBMSS2).setOutput("file",OUT_CBMSS2.getLinkName());
        stpsNodes.put(OUT_CBMSS2.getLinkName(),OUT_CBMSS2);





        stpsNodes.get("frame_sync_interleaved");
        linkTwoNodes("frame_sync_interleaved","deinterleaver",config);
        linkTwoNodes("deinterleaver","aosService",config);

//        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CACCD1,(FrameReceiver)stpsNodes.get("OUT_CACCD1"));
//        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CACCD2,(FrameReceiver)stpsNodes.get("OUT_CACCD2"));
//        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CACCD3,(FrameReceiver)stpsNodes.get("OUT_CACCD3"));
//        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CACCD4,(FrameReceiver)stpsNodes.get("OUT_CACCD4"));
//        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CBCCD1,(FrameReceiver)stpsNodes.get("OUT_CBCCD1"));
//        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CBCCD2,(FrameReceiver)stpsNodes.get("OUT_CBCCD2"));
//        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CBCCD3,(FrameReceiver)stpsNodes.get("OUT_CBCCD3"));
//        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CBCCD4,(FrameReceiver)stpsNodes.get("OUT_CBCCD4"));
//        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CAMSS1,(FrameReceiver)stpsNodes.get("OUT_CAMSS1"));
//        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CAMSS2,(FrameReceiver)stpsNodes.get("OUT_CAMSS2"));
//        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CBMSS1,(FrameReceiver)stpsNodes.get("OUT_CBMSS1"));
//        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CBMSS2,(FrameReceiver)stpsNodes.get("OUT_CBMSS2"));

        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CACCD1,(FrameReceiver)stpsNodes.get("frame_sync_caccd1"));
        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CACCD2,(FrameReceiver)stpsNodes.get("frame_sync_caccd2"));
        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CACCD3,(FrameReceiver)stpsNodes.get("frame_sync_caccd3"));
        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CACCD4,(FrameReceiver)stpsNodes.get("frame_sync_caccd4"));
        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CBCCD1,(FrameReceiver)stpsNodes.get("frame_sync_cbccd1"));
        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CBCCD2,(FrameReceiver)stpsNodes.get("frame_sync_cbccd2"));
        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CBCCD3,(FrameReceiver)stpsNodes.get("frame_sync_cbccd3"));
        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CBCCD4,(FrameReceiver)stpsNodes.get("frame_sync_cbccd4"));
        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CAMSS1,(FrameReceiver)stpsNodes.get("frame_sync_camss1"));
        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CAMSS2,(FrameReceiver)stpsNodes.get("frame_sync_camss2"));
        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CBMSS1,(FrameReceiver)stpsNodes.get("frame_sync_cbmss1"));
        ((AOSService)aosService).addFrameReceiver(AOSDEFINE_xx103.SPACECRFAT_ID,AOSDEFINE_xx103.VCID_CBMSS2,(FrameReceiver)stpsNodes.get("frame_sync_cbmss2"));

        linkTwoNodes("frame_sync_caccd1","OUT_CACCD1",config);
        linkTwoNodes("frame_sync_caccd2","OUT_CACCD2",config);
        linkTwoNodes("frame_sync_caccd3","OUT_CACCD3",config);
        linkTwoNodes("frame_sync_caccd4","OUT_CACCD4",config);

        linkTwoNodes("frame_sync_cbccd1","OUT_CBCCD1",config);
        linkTwoNodes("frame_sync_cbccd2","OUT_CBCCD2",config);
        linkTwoNodes("frame_sync_cbccd3","OUT_CBCCD3",config);
        linkTwoNodes("frame_sync_cbccd4","OUT_CBCCD4",config);

        linkTwoNodes("frame_sync_camss1","OUT_CAMSS1",config);
        linkTwoNodes("frame_sync_camss2","OUT_CAMSS2",config);
        linkTwoNodes("frame_sync_cbmss1","OUT_CBMSS1",config);
        linkTwoNodes("frame_sync_cbmss2","OUT_CBMSS2",config);


        //Link all nodes. These are all links defined by link statements.
        //It does not include special links as defined in the CADU and
        //Path nodes.

        //Allow each node to finish setup. Nodes can now assume that all
        //other nodes have been created. Some nodes will resolve their
        //special links, while others will get shared information from
        //other nodes.
//        Iterator<RtStpsNode> i = config.getStpsNodes().values().iterator();
//        while (i.hasNext())
//        {
//            RtStpsNode node = (RtStpsNode)i.next();
//            node.finishSetup(config);
//        }

        //There is one FS, so the node name is the same as the class name.
        String fsNodeName = "frame_sync_interleaved";
        return (FrameSynchronizer)config.getStpsNodes().get(fsNodeName);


    }


    /**
     * Create an STPS pipeline.
     * @return The FrameSynchronizer object.
     */
    public FrameSynchronizer create(org.xml.sax.InputSource input)
            throws RtStpsException
    {
	try{
		//I build an XML DOM document from the loaded configuration file.
		Document document = null;
		try
		{
		    document = documentBuilder.parse(input);
		}
		catch (SAXException se)
		{
		    throw new RtStpsException(se);
		}
		catch (java.io.IOException ioe)
		{
		    throw new RtStpsException(ioe);
		}

		//The root element is the top of the document.
		Element root = document.getDocumentElement();
		if (!root.getTagName().equals("rt_stps"))
		{
		    throw new RtStpsException("The setup is not an RT-STPS configuration.");
		}

		//The cid is a description of the configuration, which I made as a
		//root element attribute.
		String cid = root.getAttribute("id");

		//I use the configuration object to store shared information.
		config = new Configuration(cid,nodeFactory);

		 //I do the spacecraft first so that RT-STPS nodes that need them may
		 //map them immediately.
		createSpacecraftMap(document,config);

		 //Create all STPS nodes. I save the links element to process later.
		Element links = createNodes(root,config);

		 //Link all nodes. These are all links defined by link statements.
		 //It does not include special links as defined in the CADU and
		 //Path nodes.
		linkNodes(links,config);

		 //Allow each node to finish setup. Nodes can now assume that all
		 //other nodes have been created. Some nodes will resolve their
		 //special links, while others will get shared information from
		 //other nodes.
		Iterator<RtStpsNode> i = config.getStpsNodes().values().iterator();
		while (i.hasNext())
		{
		    RtStpsNode node = (RtStpsNode)i.next();
		    node.finishSetup(config);
		}

		//There is one FS, so the node name is the same as the class name.
		String fsNodeName = FrameSynchronizer.CLASSNAME;
		return (FrameSynchronizer)config.getStpsNodes().get(fsNodeName);
	}
	catch(Exception oute){
		// [A]: When building goes horribly wrong, we want to flush whatever's	
		// been created already
		try{
			if(config != null){
				String fsNodeName = FrameSynchronizer.CLASSNAME;
				FrameSynchronizer toFlush = (FrameSynchronizer)config.getStpsNodes().get(fsNodeName);
				if(toFlush != null)
					toFlush.flush();
			}
		} catch(Exception inre){

		}
		throw new RtStpsException(oute);
	}
    }

    private void populateNodeFactory() throws RtStpsException
    {
        nodeFactory.addNode(FrameSynchronizer.CLASSNAME,new FrameSynchronizer());
        nodeFactory.addNode(CrcDecoder.CLASSNAME,new CrcDecoder());
        nodeFactory.addNode(PnDecoder.CLASSNAME,new PnDecoder());
        nodeFactory.addNode(ReedSolomonDecoder.CLASSNAME,new ReedSolomonDecoder());
        nodeFactory.addNode(FrameStatus.CLASSNAME,new FrameStatus());

        nodeFactory.addNode(CaduService.CLASSNAME,new CaduService());
        nodeFactory.addNode(VcduService.CLASSNAME,new VcduService());
        nodeFactory.addNode(BitstreamService.CLASSNAME,new BitstreamService());
        nodeFactory.addNode(PathService.CLASSNAME,new PathService());
        nodeFactory.addNode(PacketPipeline.CLASSNAME,new PacketPipeline());

        nodeFactory.addNode(PacketChannel.CLASSNAME,new PacketChannel());
        nodeFactory.addNode(PacketChannelA.CLASSNAME,new PacketChannelA());
        nodeFactory.addNode(PacketChannelB.CLASSNAME,new PacketChannelB());

        nodeFactory.addNode(NullChannel.CLASSNAME,new NullChannel());
        nodeFactory.addNode(UnitChannel.CLASSNAME,new UnitChannel());
        nodeFactory.addNode(UnitChannelA.CLASSNAME,new UnitChannelA());
        nodeFactory.addNode(UnitChannelB.CLASSNAME,new UnitChannelB());

        nodeFactory.addNode("FRAME_NONE",new UnitChannel());
        nodeFactory.addNode("FRAME_BEFORE",new UnitChannelA());
        nodeFactory.addNode("FRAME_AFTER",new UnitChannelB());

        nodeFactory.addNode(gov.nasa.gsfc.drl.rtstps.core.xstps.TerraDecoder.CLASSNAME,
                new gov.nasa.gsfc.drl.rtstps.core.xstps.TerraDecoder());
        nodeFactory.addNode(gov.nasa.gsfc.drl.rtstps.core.xstps.pds.PdsOutput.CLASSNAME,
                new gov.nasa.gsfc.drl.rtstps.core.xstps.pds.PdsOutput());
        
        nodeFactory.addNode(RDROutput.CLASSNAME, new RDROutput());

        nodeFactory.addNode(Deinterleaver.CLASSNAME,new Deinterleaver());
        nodeFactory.addNode(AOSService.CLASSNAME,new AOSService());
        nodeFactory.addNode(AOSVcduService.CLASSNAME,new AOSVcduService());

    }

    /**
     * Ingest all spacecrafts from the xml configuration file.
     * 
     * NOTE: this seems to ingest info and do nothing with it with
     * no side effects. 
     */
    private void createSpacecraftMap(Document document,
            Configuration config) throws RtStpsException
    {
        NodeList list = document.getElementsByTagName("spacecraft");
        int length = list.getLength();
        if (length > 0)
        {
            java.util.Map<String,Spacecraft> spacecrafts = config.getSpacecrafts();
            for (int n = 0; n < length; n++)
            {
                Spacecraft s = new Spacecraft((Element)list.item(n));
                spacecrafts.put(s.getName(),s);
            }
        }
    }

    private Element createNodes(Element root, Configuration config)
            throws RtStpsException
    {
        Element links = null;
        java.util.TreeMap<String,RtStpsNode> stpsNodes = config.getStpsNodes();	

        /**
         * I create the FrameStatus node here because there is no element
         * in the setup file related to it.
         */
        // FIXME - empty XML frame_status is not supported or properly defined.
        /**
         * This refers to the original comment above, there's an empty but seemingly unused element in the DTD.
         * If this element should be supported, this should be removed from here... and the proper
         * processing put in. 
         */
        RtStpsNode frameStatus = nodeFactory.create("frame_status");
        frameStatus.load(null,config);
        stpsNodes.put(frameStatus.getLinkName(),frameStatus);

         //The PN decoder does not have its own element, so it is not
         //created here. Since the pn flag is in FsSetup, the
         //FrameSynchronizer node creates PnDecoder when FrameSynchronizer
         //is loaded.
        NodeList list = root.getChildNodes();

        int length = list.getLength();
        for (int n = 0; n < length; n++)
        {
            Element element = (Element)list.item(n);
            String id = element.getTagName();

            if (id.equals("links"))
            {
                //Save the links element for later to ensure that I've done
                //all nodes first.
                links = element;
            }
            else if (id.equals("spacecrafts"))
            {
                continue;  //spacecraft were already completed
            }
            else if (id.equals("ccsds_services"))
            {
                createServiceNodes(element,config);
            }
            else if (id.equals("packets"))
            {
                createPacketNodes(element,config);
            }
            else if (id.equals("output_channels"))
            {
                createOutputChannels(element,config);
            }
            else
            {
                RtStpsNode snode = nodeFactory.create(element,config);
                if (snode == null)
                {
                    throw new RtStpsException("Unknown node type " +
                        element.getTagName());
                }
                if (snode.getLinkName() == null)
                {
                    throw new RtStpsException(element.getTagName() +
                        " does not have a link name.");
                }
                stpsNodes.put(snode.getLinkName(),snode);
            }
        }

        return links;
    }

    private void createServiceNodes(Element element, Configuration config)
            throws RtStpsException
    {
        NodeList list = element.getChildNodes();
        int length = list.getLength();
        java.util.TreeMap<String,RtStpsNode> stpsNodes = config.getStpsNodes();	
        RtStpsNodeFactory nodeFactory = config.getNodeFactory();
        for (int s = 0; s < length; s++)
        {
            element = (Element)list.item(s);
            RtStpsNode service = nodeFactory.create(element,config);
            stpsNodes.put(service.getLinkName(),service);
        }
    }

    private void createPacketNodes(Element element, Configuration config)
            throws RtStpsException
    {
        NodeList list = element.getElementsByTagName("packet");
        int length = list.getLength();
        RtStpsNodeFactory nodeFactory = config.getNodeFactory();
        java.util.TreeMap<String,RtStpsNode> stpsNodes = config.getStpsNodes();
        for (int p = 0; p < length; p++)
        {
            element = (Element)list.item(p);
            RtStpsNode packet = nodeFactory.create(element,config);
            stpsNodes.put(packet.getLinkName(),packet);
        }
    }

    private void createOutputChannels(Element element, Configuration config)
            throws RtStpsException
    {
        NodeList list = element.getChildNodes();
        int length = list.getLength();
        RtStpsNodeFactory nodeFactory = config.getNodeFactory();
        java.util.TreeMap<String,RtStpsNode> stpsNodes = config.getStpsNodes();	
        for (int p = 0; p < length; p++)
        {
            Node xnode = list.item(p);
            if (xnode instanceof Element)
            {
                element = (Element)list.item(p);
                String id = element.getTagName();
                if (id.equals("file") || id.equals("socket"))
                {
                    String utype = element.getAttribute("unitType");
                    String annotation = element.getAttribute("annotation");
                    if (annotation.length() == 0) annotation = "BEFORE";
                    id = utype + '_' + annotation;
                    RtStpsNode oo = nodeFactory.create(id);
                    
                    // try {
                      oo.load(element,config);
                      stpsNodes.put(oo.getLinkName(),oo);
                    // } catch (Exception e) {
                      // System.out.println("failed to created output node: " + e.toString());
                    // }
                }
                else
                {
                	// create calls load which typically makes a unique linkname from the
                	// contents of the XML file...
                    RtStpsNode oo = nodeFactory.create(element,config);  
                    
                    System.out.println("Link Name: " + oo.getLinkName());
                    stpsNodes.put(oo.getLinkName(),oo);
                }
            }
            else
            {
                throw new RtStpsException("Invalid output element in configuration.");
            }
        }
    }

    private void linkTwoNodes(String src, String dest,Configuration config)  throws RtStpsException{
        String from = src;
        String to = dest;
        TreeMap<String, RtStpsNode> stpsNodes = config.getStpsNodes();
        RtStpsNode source = (RtStpsNode)stpsNodes.get(from);
        if (source == null)
        {
            throw new RtStpsException("Source node " + from +
                    " does not exist.");
        }
        RtStpsNode destination = (RtStpsNode)stpsNodes.get(to);
        if (destination == null)
        {
            throw new RtStpsException("Destination node " + to +
                    " does not exist.");
        }

        if (source instanceof Sender)
        {
            if (destination instanceof Receiver)
            {
                Sender sender = (Sender)source;
                sender.addReceiver((Receiver)destination);
            }
            else
            {
                throw new RtStpsException("Destination " + to +
                        " is not a receiver.");
            }
        }
        else
        {
            throw new RtStpsException("Source " + from +
                    " is not a sender.");
        }

    }

    private void linkNodes(Element element, Configuration config)
            throws RtStpsException
    {
        TreeMap<String, RtStpsNode> stpsNodes = config.getStpsNodes();
        NodeList links = element.getChildNodes();
        int length = links.getLength();
        for (int n = 0; n < length; n++)
        {
            Element link = (Element)links.item(n);

            String from = link.getAttribute("from");
            RtStpsNode source = (RtStpsNode)stpsNodes.get(from);
            if (source == null)
            {
                throw new RtStpsException("Source node " + from +
                        " does not exist.");
            }

            String to = link.getAttribute("to");
            RtStpsNode destination = (RtStpsNode)stpsNodes.get(to);
            if (destination == null)
            {
                throw new RtStpsException("Destination node " + to +
                        " does not exist.");
            }

            if (source instanceof Sender)
            {
                if (destination instanceof Receiver)
                {
                    Sender sender = (Sender)source;
                    sender.addReceiver((Receiver)destination);
                }
                else
                {
                    throw new RtStpsException("Destination " + to +
                            " is not a receiver.");
                }
            }
            else
            {
                throw new RtStpsException("Source " + from +
                        " is not a sender.");
            }
        }
    }

    class MyErrorHandler implements org.xml.sax.ErrorHandler
    {
        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe)
        {
            String systemId = spe.getSystemId();
            if (systemId == null) systemId = "null";
            String info = "URI=" + systemId + " Line=" + spe.getLineNumber() +
                ": " + spe.getMessage();
            return info;
        }

        public void warning(SAXParseException spe) throws SAXException
        {
            System.err.println("Warning: " + getParseExceptionInfo(spe));
        }

        public void error(SAXParseException spe) throws SAXException
        {
            String message = "Error: " + getParseExceptionInfo(spe) + "at line #: " + spe.getLineNumber();
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException
        {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
}
