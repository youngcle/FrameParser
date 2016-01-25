package gov.nasa.gsfc.drl.rtstps.core.AOS;

import java.math.BigInteger;

/**
 * Created by youngcle on 16-1-24.
 */
public class AOSDEFINE_xx103 {
    final public static int SPACECRFAT_ID= 172;
    final public static long AOS_INTERLEAVED_FRAME_SYNC = 0x1a1acfcf;//fcfc1d1dL;//1A1A CFCF fcfc1d1d;
    final public static int AOS_INTERLEAVED_FRAME_SYNC_LENGTH = 4;//1A1A CFCF fcfc1d1d;
    final public static int AOS_INTERLEAVED_FRAME_LENGTH = 2048;//1A1A CFCF fcfc1d1d;

    final public static int AOS_FRAME_SYNC = 0x1acffc1d;
    final public static int COMPRESSED_BLOCK1_FRAME_SYNC = 0xffffa1a1;//FF FF A1 A1 AA AA;
    final public static int COMPRESSED_BLOCK1_FRAME_SYNC_LENGTH = 4;
    final public static int COMPRESSED_BLOCK_LENGTH = 159036;//62个字节辅助数据， 62+158380
    final public static int COMPRESSED_FRAME_LENGTH = 477108;//62个字节辅助数据， 477108    159036     (62+158380)*3
    final public static int COMPRESSED_BLOCK2_FRAME_SYNC = 0xffffa2a2;//FF FF A1 A1 AA AA;
    final public static int COMPRESSED_BLOCK3_FRAME_SYNC = 0xffffa3a3;//FF FF A1 A1 AA AA;
    final public static int COMPRESSED_MSS_FRAME_SYNC = 0xffffBBBB;//FF FF BB BB AA AA;
    final public static int COMPRESSED_MSS_FRAME_LENGTH = 8226;//
    final public static int VCID_CACCD1=8;
    final public static int VCID_CACCD2=17;
    final public static int VCID_CACCD3=18;
    final public static int VCID_CACCD4=24;

    final public static int VCID_CBCCD1=32;
    final public static int VCID_CBCCD2=41;
    final public static int VCID_CBCCD3=42;
    final public static int VCID_CBCCD4=48;

    final public static int VCID_CAMSS1=49;
    final public static int VCID_CAMSS2=50;
    final public static int VCID_CBMSS1=51;
    final public static int VCID_CBMSS2=52;

}
