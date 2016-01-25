package gov.nasa.gsfc.drl.rtstps;

import gov.nasa.gsfc.drl.rtstps.core.fs.FrameSynchronizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * 定
 义	同
 步
 字	VCDU主导头	VCDU插入区	VCDU
 数据单元	VCDU差
 错
 控
 制
 域	RS校验符号域
 版本号	VCDU
 标识符	VCDU计数器	信号域		BPDU导头	BPDU位
 流数
 据区
 航天器标识符	虚拟信道标识符		回放
 标识	I/Q标识	加密明传标识	工作模式
 bit	32	2	8	6	24	1	2	2	3	256	16	7312	16	512
 Byte	4	6	32	2	914	2	64
 *
 *
 *
 * Created by yanghl on 16-1-21.
 */




public class PlainAOSParser {
    static final int AOS_SYNC_MARK = 0x1ACFFC1D;

    static final int BIT_POS_AOS_SYNC_MARK = 0;
    static final int BIT_LENGTH_AOS_SYNC_MARK = 32;
    static final int BIT_MASK_AOS_SYNC_MARK = 0xffffffff;

    static final int BIT_POS_AOS_VERSION = 32;
    static final int BIT_LENGTH_AOS_VERSION = 2;
    static final int BIT_MASK_AOS_VERSION = 0x3;


    static final int BIT_POS_AOS_SPACECRAFTID = 34;
    static final int BIT_LENGTH_AOS_SPACECRAFTID = 8;

    static final int BIT_POS_AOS_VCDUID = 42;
    static final int BIT_LENGTH_AOS_VCDUID = 6;

    static final int BIT_POS_AOS_VCDUCOUNTER = 48;
    static final int BIT_LENGTH_AOS_VCDUCOUNTER = 24;


    static final int BIT_POS_AOS_FLAG_REPLAY = 72;
    static final int BIT_LENGTH_AOS_FLAG_REPLAY = 1;

    static final int BIT_POS_AOS_FLAG_IQ = 73;
    static final int BIT_LENGTH_AOS_FLAG_IQ = 2;

    static final int BIT_POS_AOS_FLAG_ENCRYPTED = 75;
    static final int BIT_LENGTH_AOS_FLAG_ENCRYPTED = 2;

    static final int BIT_POS_AOS_WORKINGMODE = 77;
    static final int BIT_LENGTH_AOS_WORKINGMODE = 3;

    static final int BIT_POS_AOS_VCDU_INSERTZONE = 80;
    static final int BIT_LENGTH_AOS_VCDU_INSERTZONE = 256;

    static final int BIT_POS_AOS_BPDU_HEADER = 336;
    static final int BIT_LENGTH_AOS_BPDU_HEADER = 16;

    static final int BIT_POS_AOS_BPDU_DATA_BODY = 352;
    static final int BIT_LENGTH_AOS_BPDU_DATA_BODY = 7312;

    static final int BIT_POS_AOS_CRC_CODE = 7664;
    static final int BIT_LENGTH_AOS_CRC_CODE = 16;

    static final int BIT_POS_AOS_RS_CODE = 7680;
    static final int BIT_LENGTH_AOS_RS_CODE = 512;


    int getAosSyncMark(byte[] AOSFrame){
        int pos = BIT_POS_AOS_SYNC_MARK/8;

        int count = BIT_LENGTH_AOS_SYNC_MARK/8;
        int mask = BIT_MASK_AOS_SYNC_MARK;
        int AOS_SYNC_MARK = 0;

        for(int i = 0;i<count;i++) {
            AOS_SYNC_MARK = AOS_SYNC_MARK<<8 |  (int)AOSFrame[pos + i];
        }
        return AOS_SYNC_MARK ;
    }

    int getAOS_SPACECRAFT_ID(byte[] AOSFrame){
        int pos = BIT_POS_AOS_SPACECRAFTID/8;

        int mod = BIT_POS_AOS_SPACECRAFTID%8;

        int count = BIT_LENGTH_AOS_SPACECRAFTID/8;

        int AOS_SPACECRAFT_ID = 0;

        AOS_SPACECRAFT_ID = ((int)AOSFrame[pos] & 0x3f)<<2 | ((int)AOSFrame[pos+1] & 0xc0)>>6;

        return AOS_SPACECRAFT_ID ;
    }


    int getAOS_VCDUID(byte[] AOSFrame){
        int pos = BIT_POS_AOS_VCDUID/8;

        int mod = BIT_POS_AOS_VCDUID%8;

        int count = BIT_LENGTH_AOS_SPACECRAFTID/8;

        int AOS_VCDUID = 0;

        AOS_VCDUID = ((int)AOSFrame[pos] & 0x3f);

        return AOS_VCDUID ;
    }

    byte[] getAOS_BPDU_DATA_BODY(byte[] AOSFrame){
        int pos = BIT_POS_AOS_BPDU_DATA_BODY/8;

        int mod = BIT_POS_AOS_VCDUID%8;

        int count = BIT_LENGTH_AOS_SPACECRAFTID/8;

        int AOS_VCDUID = 0;
        byte[] body = new byte[914];

        ByteBuffer source = ByteBuffer.wrap(AOSFrame);
        source.position(pos);
        source.get(body);

        return body;
    }

    public PlainAOSParser(){
        //创建PlainAOSParser
    }


    ByteBuffer BufferI = ByteBuffer.allocate(1024);
    ByteBuffer BufferQ = ByteBuffer.allocate(1024);
    public void DeMultiplex(ByteBuffer interleaved_AOS){
        BufferI.clear();
        BufferQ.clear();
        interleaved_AOS.rewind();
        while (interleaved_AOS.hasRemaining()) {

            BufferI.put(interleaved_AOS.get());
            BufferQ.put(interleaved_AOS.get());
        }
        BufferI.rewind();
        BufferQ.rewind();

    }




    public static void main(String[] args) throws Exception {
//        String filename = "/data/DATA_PLAIN_AOS/TRGS-BJ_JB10-3_006340_000185524_a_3_1_DECRYTION_20160113092134.dat";
        String filename ="/run/media/youngcle/3178-435E/nasa/rt-stps/testdata/input/PLAIN_AOS_006424small.dat";
        if(args.length >0)
            filename = args[1];
        File aos_file = new File(filename);
        ByteBuffer bb = ByteBuffer.allocate(2048);
        PlainAOSParser plainAOSParser = new PlainAOSParser();
        try {
            FileChannel fileChannel = new FileInputStream(aos_file).getChannel();
            int counter =0;
            while (fileChannel.read(bb)>0){
                plainAOSParser.DeMultiplex(bb);
                int SyncMarkI  = plainAOSParser.getAosSyncMark(plainAOSParser.BufferI.array());
                int scidI  = plainAOSParser.getAOS_SPACECRAFT_ID(plainAOSParser.BufferI.array());
                int SyncMarkQ  = plainAOSParser.getAosSyncMark(plainAOSParser.BufferQ.array());
                int scidQ  = plainAOSParser.getAOS_SPACECRAFT_ID(plainAOSParser.BufferQ.array());

                int vcduidinI =plainAOSParser.getAOS_VCDUID(plainAOSParser.BufferI.array());
                int vcduidinQ =plainAOSParser.getAOS_VCDUID(plainAOSParser.BufferQ.array());
                byte[] bodyI = plainAOSParser.getAOS_BPDU_DATA_BODY(plainAOSParser.BufferI.array());
                byte[] bodyQ = plainAOSParser.getAOS_BPDU_DATA_BODY(plainAOSParser.BufferQ.array());
                File vcduidfileI = new File("/home/youngcle/"+vcduidinI);
                File vcduidfileQ = new File("/home/youngcle/"+vcduidinQ);

                FileChannel vcduidfcI = new FileOutputStream(vcduidfileI,true).getChannel();
                FileChannel vcduidfcQ = new FileOutputStream(vcduidfileQ,true).getChannel();
                vcduidfcI.position(vcduidfileI.length());
                vcduidfcQ.position(vcduidfileQ.length());
                vcduidfcI.write(ByteBuffer.wrap(bodyI));
                vcduidfcQ.write(ByteBuffer.wrap(bodyQ));
                vcduidfcI.close();
                vcduidfcQ.close();
                System.out.println("Channel I Line:"+counter+" SYNCMARK:"+SyncMarkI+" scid:"+scidI+" vcduid:"+vcduidinI);
                System.out.println("Channel Q Line:"+counter+" SYNCMARK:"+SyncMarkQ+" scid:"+scidQ+" vcduid:"+vcduidinQ);
                counter++;
                bb.clear();
            }
            fileChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
