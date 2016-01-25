/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;
import gov.nasa.gsfc.drl.rtstps.core.fs.FrameSynchronizer;

import java.util.TreeMap;

/**
 * This class does Reed Solomon error detection and correction on frames.
 * It sends frames to a target FrameReceiver. Corrected issues related to
 * corrections in August 2007.  
 * 
 * 
 */
public class ReedSolomonDecoder extends FrameSenderNode implements
        FrameReceiver, Sender, Cloneable
{
    /**
     * This is a class name for this STPS node type, which is also the element
     * name. It is not necessarily the link name, which is the name of one
     * particular object.
     */
    public static final String CLASSNAME = "reed_solomon";

    private static final int OK = 0;
    private static final int CORRECTED = -1;
    private static final int UNCORRECTABLE = -2;

    //int[256]
    private static final int[] ANTILOG = {
    0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x87, 0x89, 0x95, 0xad, 0xdd,
    0x3d, 0x7a, 0xf4, 0x6f, 0xde, 0x3b, 0x76, 0xec, 0x5f, 0xbe, 0xfb, 0x71, 0xe2,
    0x43, 0x86, 0x8b, 0x91, 0xa5, 0xcd, 0x1d, 0x3a, 0x74, 0xe8, 0x57, 0xae, 0xdb,
    0x31, 0x62, 0xc4, 0x0f, 0x1e, 0x3c, 0x78, 0xf0, 0x67, 0xce, 0x1b, 0x36, 0x6c,
    0xd8, 0x37, 0x6e, 0xdc, 0x3f, 0x7e, 0xfc, 0x7f, 0xfe, 0x7b, 0xf6, 0x6b, 0xd6,
    0x2b, 0x56, 0xac, 0xdf, 0x39, 0x72, 0xe4, 0x4f, 0x9e, 0xbb, 0xf1, 0x65, 0xca,
    0x13, 0x26, 0x4c, 0x98, 0xb7, 0xe9, 0x55, 0xaa, 0xd3, 0x21, 0x42, 0x84, 0x8f,
    0x99, 0xb5, 0xed, 0x5d, 0xba, 0xf3, 0x61, 0xc2, 0x03, 0x06, 0x0c, 0x18, 0x30,
    0x60, 0xc0, 0x07, 0x0e, 0x1c, 0x38, 0x70, 0xe0, 0x47, 0x8e, 0x9b, 0xb1, 0xe5,
    0x4d, 0x9a, 0xb3, 0xe1, 0x45, 0x8a, 0x93, 0xa1, 0xc5, 0x0d, 0x1a, 0x34, 0x68,
    0xd0, 0x27, 0x4e, 0x9c, 0xbf, 0xf9, 0x75, 0xea, 0x53, 0xa6, 0xcb, 0x11, 0x22,
    0x44, 0x88, 0x97, 0xa9, 0xd5, 0x2d, 0x5a, 0xb4, 0xef, 0x59, 0xb2, 0xe3, 0x41,
    0x82, 0x83, 0x81, 0x85, 0x8d, 0x9d, 0xbd, 0xfd, 0x7d, 0xfa, 0x73, 0xe6, 0x4b,
    0x96, 0xab, 0xd1, 0x25, 0x4a, 0x94, 0xaf, 0xd9, 0x35, 0x6a, 0xd4, 0x2f, 0x5e,
    0xbc, 0xff, 0x79, 0xf2, 0x63, 0xc6, 0x0b, 0x16, 0x2c, 0x58, 0xb0, 0xe7, 0x49,
    0x92, 0xa3, 0xc1, 0x05, 0x0a, 0x14, 0x28, 0x50, 0xa0, 0xc7, 0x09, 0x12, 0x24,
    0x48, 0x90, 0xa7, 0xc9, 0x15, 0x2a, 0x54, 0xa8, 0xd7, 0x29, 0x52, 0xa4, 0xcf,
    0x19, 0x32, 0x64, 0xc8, 0x17, 0x2e, 0x5c, 0xb8, 0xf7, 0x69, 0xd2, 0x23, 0x46,
    0x8c, 0x9f, 0xb9, 0xf5, 0x6d, 0xda, 0x33, 0x66, 0xcc, 0x1f, 0x3e, 0x7c, 0xf8,
    0x77, 0xee, 0x5b, 0xb6, 0xeb, 0x51, 0xa2, 0xc3, 0x00};

    //int[256]
    private int[] DUAL_ANTILOG = {
    0x7b, 0xaf, 0x99, 0xfa, 0x86, 0xec, 0xef, 0x8d, 0xc0, 0x0c, 0xe9, 0x79, 0xfc,
    0x72, 0xd0, 0x91, 0xb4, 0x28, 0x44, 0xb3, 0xed, 0xde, 0x2b, 0x26, 0xfe, 0x21,
    0x3b, 0xbb, 0xa3, 0x70, 0x83, 0x7a, 0x9e, 0x3f, 0x1c, 0x74, 0x24, 0xad, 0xca,
    0x11, 0xac, 0xfb, 0xb7, 0x4a, 0x09, 0x7f, 0x08, 0x4e, 0xae, 0xa8, 0x5c, 0x60,
    0x1e, 0x27, 0xcf, 0x87, 0xdd, 0x49, 0x6b, 0x32, 0xc4, 0xab, 0x3e, 0x2d, 0xd2,
    0xc2, 0x5f, 0x02, 0x53, 0xeb, 0x2a, 0x17, 0x58, 0xc7, 0xc9, 0x73, 0xe1, 0x37,
    0x52, 0xda, 0x8c, 0xf1, 0xaa, 0x0f, 0x8b, 0x34, 0x30, 0x97, 0x40, 0x14, 0x3a,
    0x8a, 0x05, 0x96, 0x71, 0xb2, 0xdc, 0x78, 0xcd, 0xd4, 0x36, 0x63, 0x7c, 0x6a,
    0x03, 0x62, 0x4d, 0xcc, 0xe5, 0x90, 0x85, 0x8e, 0xa2, 0x41, 0x25, 0x9c, 0x6c,
    0xf7, 0x5e, 0x33, 0xf5, 0x0d, 0xd8, 0xdf, 0x1a, 0x80, 0x18, 0xd3, 0xf3, 0xf9,
    0xe4, 0xa1, 0x23, 0x68, 0x50, 0x89, 0x67, 0xdb, 0xbd, 0x57, 0x4c, 0xfd, 0x43,
    0x76, 0x77, 0x46, 0xe0, 0x06, 0xf4, 0x3c, 0x7e, 0x39, 0xe8, 0x48, 0x5a, 0x94,
    0x22, 0x59, 0xf6, 0x6f, 0x95, 0x13, 0xff, 0x10, 0x9d, 0x5d, 0x51, 0xb8, 0xc1,
    0x3d, 0x4f, 0x9f, 0x0e, 0xba, 0x92, 0xd6, 0x65, 0x88, 0x56, 0x7d, 0x5b, 0xa5,
    0x84, 0xbf, 0x04, 0xa7, 0xd7, 0x54, 0x2e, 0xb0, 0x8f, 0x93, 0xe7, 0xc3, 0x6e,
    0xa4, 0xb5, 0x19, 0xe2, 0x55, 0x1f, 0x16, 0x69, 0x61, 0x2f, 0x81, 0x29, 0x75,
    0x15, 0x0b, 0x2c, 0xe3, 0x64, 0xb9, 0xf0, 0x9b, 0xa9, 0x6d, 0xc6, 0xf8, 0xd5,
    0x07, 0xc5, 0x9a, 0x98, 0xcb, 0x20, 0x0a, 0x1d, 0x45, 0x82, 0x4b, 0x38, 0xd9,
    0xee, 0xbc, 0x66, 0xea, 0x1b, 0xb1, 0xbe, 0x35, 0x01, 0x31, 0xa6, 0xe6, 0xf2,
    0xc8, 0x42, 0x47, 0xd1, 0xa0, 0x12, 0xce, 0xb6, 0x00};

    /** number of symbols in field, 2 ** bitsPerSymbol - 1 */
    private int codewordLength = 255;
    @SuppressWarnings("unused")
	private int actualLength;

    /** The syndrome polynomial */
    private int[] syndrome;

    private int frameLength = 1024;
    private int skipBytes = 4;
    private int codewordDistance = 33;
    private int parityLength = 32;

    private int[] sigma;
    private int[] sigmab;
    private int[] correctionPolynomial;
    private int[] errorMagnitudes;
    private int[] errorLocations;
    private int[] log; /* Galois log table */
    private int[] antilog;
    private int[] magnitudePolynomial;

    private Setup setup = null;



    /**
     * Create a Reed Solomon decoder node.
     */
    public ReedSolomonDecoder()
    {
        /**
         * There is only one ReedSolomonDecoder object, so the class name is
         * the same as the link/object name.
         */
        super(CLASSNAME,CLASSNAME);
    }

    /**
     * Set up this stps node with a configuration.
     */
    public void load(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
    	setup = new Setup(element);

        codewordDistance = 2 * setup.maxCorrectableErrors + 1;
        codewordLength = (1 << setup.bitsPerSymbol) - 1;
        parityLength = 2 * setup.maxCorrectableErrors;
        actualLength = codewordLength - setup.virtualFill;

        syndrome = new int[parityLength];
        sigma = new int[parityLength];
        sigmab = new int[parityLength];
        correctionPolynomial = new int[parityLength];

        errorMagnitudes = new int[setup.maxCorrectableErrors];
        errorLocations = new int[setup.maxCorrectableErrors];

        magnitudePolynomial = new int[codewordDistance];

        int[] table = setup.isDual? DUAL_ANTILOG : ANTILOG;
        int size = setup.poa * (setup.mo + parityLength) + codewordLength + 1;
        int size2 = setup.poa * codewordLength * parityLength;
        if (size2 > size) size = size2;
        antilog = new int[size];
        int k = 0;
        for (int n = 0; n < antilog.length; n++)
        {
            antilog[n] = table[k];
            if (++k == codewordLength) k = 0;
        }

        log = new int[codewordLength + 1];
        log[0] = 0;
        for (int n = 0; n < codewordLength; n++)
        {
            log[table[n]] = n;
        }
    }

    /**
     * Finish the setup. When this method is called, you may assume all nodes
     * have been created and exist by name in the map, and all standard links
     * have been resolved. This is a last chance to prepare for data flow.
     */
    public void finishSetup(Configuration configuration) throws RtStpsException
    {
        super.finishSetup(configuration);

        TreeMap<String, RtStpsNode> nodes = configuration.getStpsNodes();
        //There is only one FS, so its node name is its class name.
        String fsNodeName = FrameSynchronizer.CLASSNAME;
        FrameSynchronizer fs = (FrameSynchronizer)nodes.get(fsNodeName);
        if (fs == null)
        {
            throw new RtStpsException("The FrameSynchronizer node is missing.");
        }

        skipBytes = fs.getSyncPatternLength();
        frameLength = fs.getFrameLength();

        //int expectedFrameLength = skipBytes + (codewordLength -
        //        setup.virtualFill) * setup.interleave;

	//[C]: This is the correct frame length equation according to CCSDS docs
	int expectedFrameLength = skipBytes + (codewordLength * setup.interleave) - setup.virtualFill;

        if (frameLength != expectedFrameLength)
        {
            throw new RtStpsException("The Reed Solomon decoder demands " +
                expectedFrameLength + " byte frames.");
        }
    }

    /**
     * Get the parity length in bytes, which is the full parity length for
     * all interleaves. Do not use this method until after this object has
     * been loaded.
     */
    public final int getParityLength()
    {
        return setup.interleave * parityLength;
    }

    /**
     * Give an array of frames to this FrameReceiver.
     */
    public void putFrames(Frame[] frames) throws RtStpsException
    {
        for (int n = 0; n < frames.length; n++)
        {
            Frame frame = frames[n];
            if (!frame.isDeleted())
            {
                int state = decode(frame.getData());
                setAnnotation(state,frame);
            }
        }

        output.putFrames(frames);
    }

    /**
     * Give a frame to this FrameReceiver.
     */
    public void putFrame(Frame frame) throws RtStpsException
    {
        if (!frame.isDeleted())
        {
            int state = decode(frame.getData());
            setAnnotation(state,frame);
        }

        output.putFrame(frame);
    }

    /**
     * Set a frame's Reed Solomon annotation and mark the frame for
     * deletion if necessary.
     */
    private void setAnnotation(int state, Frame frame)
    {
        FrameAnnotation a = frame.getFrameAnnotation();

        a.isRsUncorrectable = (state == UNCORRECTABLE);

        if (a.isRsUncorrectable)
        {
            a.isRsCorrected = false;
            if (setup.discardUncorrectables) frame.setDeleted(true);
        }
        else
        {
            a.isRsCorrected = (state == CORRECTED);
        }
    }

    /**
     * Do Reed Solomon decoding on one frame.
     * <pre>
     * 1) Calculate the syndrome vector from the received RS codeword.
     * 2) Calculate the coefficients of the error locator polynomial.
     * 3) Calculate the roots of the error locator polynomial.
     * 4) Calculate the error magnitudes.
     * 5) Correct the symbols in error with the previously calculated
     *    information.
     * <\pre>
     * Polynomial coefficients are stored P[0] = a*X ... P[n] = z*X
     */
    private int decode(byte[] data)
    {
        int state = OK;
        
        for (int level = 0; level < setup.interleave; level++)
        {
            /**
             * Calculate the syndrome. If the checksum is not equal to zero,
             * then there are errors.
             */
            boolean errorDetected = computeSyndrome(data,level);
            
 
            if (errorDetected)
            {
                if (!setup.doBlockCorrection)
                {
                    state = UNCORRECTABLE;
                    break;
                }

                for (int n = 0; n < setup.maxCorrectableErrors; n++)
                {
                    errorLocations[n] = 0;
                    errorMagnitudes[n] = 0;
                }
                for (int n = 0; n < parityLength; n++)
                {
                    sigma[n] = 0;
                    sigmab[n] = 0;
                    correctionPolynomial[n] = 0;
                }

                /** Calculate the Error Locator Polynomial. */
                int degreeOfSigma = computeErrorLocatorPolynomial();

                /**
                 * Calculate the roots of the error locator polynomial,
                 * which yields the error locations.
                 */
                int errors = computeErrorLocations(degreeOfSigma);

                if (errors == UNCORRECTABLE)
                {
                    state = UNCORRECTABLE;
                    break;
                }

                state  = computeErrorMagnitudes(errors);
                if (state == UNCORRECTABLE) break;

                correctSymbols(data,level,errors);
                state = CORRECTED;
            }
        }

        return state;
    }

    /**
     * This function implements the FFT-like syndrome calculation.
     * Reed Solomon Codes define the symbol ordering in the opposite direction
     * that the data symbols are received.
     * <pre>
     * R_n-1 = r[0]
     * R_n-2 = r[1]
     * ...
     * R1   = r[n-2]
     * R0   = r[n-1]
     *
     * For CCSDS:
     * R254 = r[0]
     * R253 = r[1]
     * ...
     * R1   = r[253]
     * R0   = r[254]
     *
     * Recursive syndrome formula.
     *                        (mo + i)                  (mo + i)
     * Si = ((...(R[n-1] * gamma        + R[n-2]) * gamma          +
     *                 (mo + i)
     *     ...) * gamma         + R[0])
     *
     *                   POA
     * Where gamma = alpha    , therefore powerOfAlpha = (POA * (mo + i))
     * and
     * 0 <= i <= (d - 2) = (2t - 1)
     * and
     * n = 2**bitsPerSymbol - 1, bitsPerSymbol = number of bits per symbol
     * </pre>
     *
     * This function is the most important one, since it is called for each
     * frame. It is the first step in the decoding process, and it determines
     * if there are errors in the data.
     * @param data The frame data including the sync pattern and parity.
     * @param level The current interleave level.
     * @return true if it detects an error or false otherwise.
     */
    private boolean computeSyndrome(byte[] data, int level)
    {
        int term = setup.poa * setup.mo;
        int checksum = 0;

        for (int n = 0; n < parityLength; n++)
        {
            int si = 0;
            for (int d = level + skipBytes; d < frameLength; )
            {
                if (si == 0)
                {
                    si = (int)data[d];
                }
                else
                {
                    int v = log[si] + term;
                    si = antilog[v] ^ data[d];
                }

                si &= 0x0ff;
                d += setup.interleave;
            }

            for (int i = 0; i < setup.virtualFill; i++)
            {
                if (si > 0)
                {
                    int v = log[si] + term;
                    si = (antilog[v] ^ 0) & 0x0ff;
                }
            }

            term += setup.poa;
            checksum |= si;
            syndrome[n] = si;
        }
 
        return (checksum != 0);
    }

    /**
     * This is the Berlekamp Algorithm. This algorithm always
     * finds the shortest feedback shift register that generates
     * the first 2t (32 for CCSDS) terms of S(z), the syndrome.
     * <P>
     * This algorithm finds the solution of the key equation:
     *             [sigma(z)*syndrome(z)] = 0
     * The syndrome is given, so this algorithm finds sigma(z)
     * of minimum degree. This is the same problem as finding
     * the minimum-length shift register, which generates the first
     * 2t terms of syndrome(z), which will predict s[1] from s[0]
     * and so on.
     * For each new syndrome symbol, the algorithm tests to see
     * whether the current correction polynomial will predict this
     * symbol correctly. If so, the correction polynomial is left
     * unchanged and the correction term is modified by multiplying
     * by z. If the current correction polynomial fails, then it is
     * modified by adding the correction term. One then checks to
     * determine if the new register length has increased. If it has
     * not, then the correction polynomial that is currently being
     * maintained is at least as good a choice as any other. If the
     * length of the new register increases, then the previous correction
     * polynomial becomes the better choice since the oldest symbol
     * associated with this register must have a higher index than the
     * oldest symbol associated with the current correction term.
     * @return The degree of the error location polynomial.
     */
    private int computeErrorLocatorPolynomial()
    {
        int loc = -1;
        int feedbackShiftRegLength = 0;

        sigma[0] = antilog[0];               //sigma(z) = 1 == alpha**0
        correctionPolynomial[1] = antilog[0];   //D(z) = z

        //for each syndrome symbol...
        for (int n = 0; n < parityLength; n++)
        {
            //calculate the discrepancy using:
            // d = SUM(i=0, feedbackShiftRegLength, sigma[i] * syndrome[n-i])
            int d = 0;
            for (int i = 0; i <= feedbackShiftRegLength; i++)
            {
                if (sigma[i] != 0)
                {
                    int m = n - i;
                    if (syndrome[m] != 0)
                    {
                        int x = log[sigma[i]] + log[syndrome[m]];
                        d ^= antilog[x];
                    }
                }
            }

            //if there is a discrepancy...
            if (d != 0)
            {
                //modify sigma(z) by subtracting the correction term
                //(d * D(z)) <sigma[i] - (d * D[i])>
                for (int i = 0; i < parityLength; i++)
                {
                    if (correctionPolynomial[i] == 0)
                    {
                        sigmab[i] = sigma[i];
                    }
                    else
                    {
                        int x = log[d] + log[correctionPolynomial[i]];
                        sigmab[i] = sigma[i] ^ antilog[x];
                    }
                }

                //if new register length has increased...
                if (feedbackShiftRegLength < (n - loc))
                {
                    int fsr = n - loc;
                    loc = n - feedbackShiftRegLength;

                    //then the previous connection polynomial becomes a
                    //better choice  D[i] = sigma[i] / d
                    int powerOfAlpha = codewordLength - log[d];

                    for (int i = 0; i < parityLength; i++)
                    {
                        if ((powerOfAlpha != 0) && (sigma[i] != 0))
                        {
                            int x = log[sigma[i]] + powerOfAlpha;
                            correctionPolynomial[i] = antilog[x];
                        }
                        else
                        {
                            correctionPolynomial[i] = 0;
                        }
                    }
                    feedbackShiftRegLength = fsr;
                }

                /* now update sigma with new sigma */
                for (int i = 0; i < parityLength; i++)
                {
                    sigma[i] = sigmab[i];
                }
            }

            /* D(z) = z * D(z), this is accomplished by shifting the terms
               of D(z) to the left, i.e. a logical left shift is effectively
               a multiply by z */
            for (int i = parityLength - 1; i >= 1; i--)
            {
                correctionPolynomial[i] = correctionPolynomial[i-1];
            }
            correctionPolynomial[0] = 0;
        }

        return feedbackShiftRegLength;
    }

    /**
     * Calculate the roots of the error location polynomial by using the
     * Chien search.
     * <pre>
     * Solve:
     *                          i                 poa
     * sigma(x): where x = gamma and gamma = alpha    , and
     * vf_end <= i <= (n - 1), n = 2**bitsPerSymbol - 1,
     * bitsPerSymbol = number of bits per symbol,
     * </pre>
     * and vf_end = the first location in the received vector following the
     * virtual fill symbols. I start the Chien Search at vf_end since there
     * is no point trying to find errors in the virtual fill symbols where
     * errors will never exist.
     *                                                    i
     * An error location is found for each value of gamma  that
     * makes sigma evaluate to 0.
     * @return The number of errors detected or UNCORRECTABLE if the number
     *      of errors is greater than degreeOfSigma.
     */
    private int computeErrorLocations(int degreeOfSigma)
    {
        int errors = 0;

        /**
         * check for valid degree of sigma, I don't think that I
         * need this check, because I don't think that this condition
         * will ever exist, so I will comment it out for now. If
         * any strange problems arise(seg fault), uncomment it
         * if (degreeOfSigma >= checkSize)
         *    return(UNCORRECTABLE_FLAG);
         */

        /**
         * Solve:
         * sigma(x) = sum = sigma[0]*X**0 + sigma[1]*X**1 +
         * sigma[2]*X**2 + ... + sigma[i]*X**i
         * by substituting 1, gamma, gamma**2,..., gamma**n-1 into
         * sigma(x), so that
         * sigma(x) = sum = sigma[0] + sigma[1]*gamma**j +
         * sigma[2]*gamma**2j + ... + sigma[i]*gamma**ij
         * ONLY look for errors in the data symbols,
         * not the virtual fill symbols
         */
        //for (int j = setup.virtualFill; j <= codewordLength; j++)
        for (int j = 0; j <= codewordLength; j++)
        {
            int sum = sigma[0];
            for (int i = 1; i <= degreeOfSigma; i++)
            {
                // powerOfAlpha = log(alpha**powerOfAlpha)
                if (sigma[i] != 0)
                {
                    int powerOfAlpha = i * j * setup.poa;
                    if (powerOfAlpha != 0)
                    {
                        int x = log[sigma[i]] + powerOfAlpha;
                        sum ^= antilog[x];
                    }
                }
            }

            /**
             * if sum equals 0, then alpha**codewordLength-j is an error location
             * number(alpha**j is a root of sigma(x), alpha**codewordLength == 1,
             * alpha**-j == alpha**n-j, therefore alpha**n-j is the reciprocal
             * of the root alpha**j, and the actual error location)
             */
            if (sum == 0)
            {
                /**
                 * if the number of errors equals the max number of errors,
                 * then max number of errors + 1 have just been detected,
                 * so return the uncorrectable flag.
                 */
                if (errors == setup.maxCorrectableErrors)
                {
                    return UNCORRECTABLE;
                }

                errorLocations[errors] = codewordLength - j;
                ++errors;
            }
        }

        /**
         * the degree of the error location polynomial and the number
         * of errors MUST match, otherwise this is an uncorrectable
         * code word
         */
        if (errors != degreeOfSigma)
        {
            errors = UNCORRECTABLE;
        }

        return errors;
    }

    /**
     * This function uses the Forney Algorithm to calculate the error magnitudes.
     *
     * It first calculates magnitudePolynomial(X){magnitudePolynomial(X) is the
     * error magnitude polynomial}:
     * <pre>                                                              2
     * magnitudePolynomial(X) = 1 + (s[1]+sigma[1])*X +
     *      (s[2]+s[1]*sigma[1]+sigma[2])*X + ...
     *                                                                v
     * + (s[v] + sigma[1]*s[v-1] + ... + sigma[v-1]*s[1] + sigma[v])*X
     * Where v = number of errors detected
     *
     *         n-l
     * If gamma    is an error-location number, then the error value
     * at location n-l is given by:
     *
     *              -(1-mo)l         l
     *         gamma        * magnitudePolynomial(gamma )
     * e    = ------------------------
     *  n-l         l               l
     *         gamma  * sigma'(gamma )
     *
     *                    POA
     * Where gamma = alpha    and
     * sigma'(X) is the derivative of sigma(X) and is given by:
     *
     *                                  2                     2i
     * sigma'(X) = sigma[1] + sigma[3]*X + ... + sigma[2i+1]*X  + ...
     * 0 <= i <= floor((t-1)/2)
     * </pre>
     */
    private int computeErrorMagnitudes(int errors)
    {
        int state = OK;
        int powerOfAlpha = 0;
        int elpMax = (setup.maxCorrectableErrors - 1) / 2;

        for (int n = 0; n < magnitudePolynomial.length; n++)
        {
            magnitudePolynomial[n] = 0;
        }

        //calculate the coefficients of magnitudePolynomial(X)
        magnitudePolynomial[0] = antilog[0];
        for (int i = 1; i <= errors; i++)
        {
            magnitudePolynomial[i] = syndrome[i-1] ^ sigma[i];
            for (int j = 1; j < i; j++)
            {
                if (sigma[j] != 0)
                {
                    int m = i - j - 1;
                    if (syndrome[m] != 0)
                    {
                        int x = log[sigma[j]] + log[syndrome[m]];
                        magnitudePolynomial[i] ^= antilog[x];
                    }
                }
            }
        }

        //for each error, calculate the error magnitude
        for (int i = 0; i < errors; i++)
        {
            int location = codewordLength - errorLocations[i];

            //plug alpha**location into magnitudePolynomial(X)
            int emag = magnitudePolynomial[0];

            for (int j = 1; j <= errors; j++)
            {
                powerOfAlpha = setup.poa * location * j;
                if ((magnitudePolynomial[j] != 0) &&(powerOfAlpha != 0))
                {
                    int x = log[magnitudePolynomial[j]] + powerOfAlpha;
                    emag ^= antilog[x];
                }
            }

            //plug alpha**location into sigma'(X)
            //sigma'(X) = sigma[1] + sigma[3]*X**2 + ... + sigma[2j+1]*X**2j +...
            int elp = sigma[1];
            for (int j = 1; j <= elpMax; j++)
            {
                //calculate power of alpha for this error
                powerOfAlpha = setup.poa * location * 2 * j;
                int m = 2 * j + 1;
                if ((powerOfAlpha != 0) && (sigma[m] != 0))
                {
                    int x = log[sigma[m]] + powerOfAlpha;
                    elp ^= antilog[x];
                }
            }

            //adjust emag for symmetric code
            int y = setup.poa * (setup.mo - 1) * location + log[emag];
            emag = antilog[y%codewordLength];

            //adjust elp for symmetric code
            y = setup.poa * location + log[elp];
            elp = antilog[y];

            if (elp == 0)
            {
                state = UNCORRECTABLE;
            }
            else
            {
                //error_magnitude[i] = emag / elp  = emag * inverse(elp)
                //take inverse of the error location polynomial prime
                powerOfAlpha = codewordLength - log[elp];
                if (emag != 0 && powerOfAlpha != 0)
                {
                    int x = log[emag] + powerOfAlpha;
                    errorMagnitudes[i] = antilog[x];
                }
                else
                {
                    state = UNCORRECTABLE;
                }
            }
        }

        return state;
    }

    /**
     * This function xor's the error magnitudes with the appropriate symbols
     * in error, thus correcting all  symbols in error.
     * @param data The data vector.
     * @param level Interleave level
     * @param errorCount Number of correctable errors
     */
    private void correctSymbols(byte[] data, int level, int errorCount)
    {
        for (int n = 0; n < errorCount; n++)
        {
            int a = codewordLength - errorLocations[n] - 1;
            a *= setup.interleave;
            a += level + skipBytes;
            data[a] ^= errorMagnitudes[n];
        }
    }


    /**
     * This class defines setup fields that are commonly associated with a
     * Reed Solomon block decoder component.
     */
    public static class Setup
    {
        /**
         * If true, the system discards frames with uncorrectable errors.
         * The default is true.
         */
        public boolean discardUncorrectables = true;

        /**
         * If true, the system corrects frames with Reed Solomon errors.
         * If false, it marks a frame's annotation, but it does not correct
         * any errors. The default is true.
         */
        public boolean doBlockCorrection = true;

        /**
         * The interleave. The default is 4.
         */
        public int interleave = 4;

        /**
         * When this field is true, the RS will set standard CCSDS values for
         * bitsPerSymbol, mo, poa, maxCorrectableErrors, virtualFill, and
         * isDual depending on the interleave.
         */
        public boolean isCCSDS = true;

        /** max number of correctable errors per codeword */
        public int maxCorrectableErrors = 16;

        /**
         * There are two Reed Solomon decoder modes: dual and non-dual. The
         * default CCSDS mode is dual (true).
         */
        public boolean isDual = true;

        public int bitsPerSymbol = 8;
        public int mo = 112;
        public int poa = 11;
        public int virtualFill = 0;

        //private static final int virtualFillTable[] = {0,3,2,9,0,15};
 		private static final int virtualFillTable[] = {0,3,2,9,0,15};
        @SuppressWarnings("unused")
		private static final int validFrameLengths[] = {0,256,512,760,1024,1264};

        public void setCCSDS(int interleave) throws RtStpsException
        {
            this.interleave = interleave;
            mo = 112;
            poa = 11;
            maxCorrectableErrors = 16;
            bitsPerSymbol = 8;
            isDual = true;
            if (interleave < 1 || interleave > 5)
            {
                throw new RtStpsException("Interleave "+interleave+
                        " is not a valid CCSDS interleave. (<=5)");
            }
            virtualFill = virtualFillTable[interleave];
         }

        public Setup(org.w3c.dom.Element element) throws RtStpsException,
                NumberFormatException
        {
            discardUncorrectables = Convert.toBoolean(element,
                    "discardUncorrectables",discardUncorrectables);

            doBlockCorrection = Convert.toBoolean(element,
                    "doBlockCorrection",doBlockCorrection);

            interleave = Convert.toInteger(element,"interleave",
                    interleave,1);

            bitsPerSymbol = Convert.toInteger(element,"bitsPerSymbol",
                    bitsPerSymbol,1);

            mo = Convert.toInteger(element,"mo",mo,1);
            poa = Convert.toInteger(element,"poa",poa,1);
            virtualFill = Convert.toInteger(element,"virtualFill",virtualFill,0);

            isDual = Convert.toBoolean(element,"dual",isDual);

            boolean isCCSDS = Convert.toBoolean(element,
                    "useStandardCCSDS",true);
            if (isCCSDS) setCCSDS(interleave);
        }
    }
}
