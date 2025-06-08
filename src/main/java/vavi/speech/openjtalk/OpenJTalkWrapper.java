/*
 * https://github.com/rosmarinus/jtalkdll/blob/master/ffi/java/JTalkJna.java
 */

package vavi.speech.openjtalk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;


/**
 * OpenJTalkWrapper.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/26 umjammer initial version <br>
 */
public class OpenJTalkWrapper {

    private static final int MAXPATH = 260;

    /**
     * Changes in the list of acoustic model filesListener interface
     */
    public interface VoiceListChangedListener extends EventListener {
        void onVoiceListChanged(VoiceListChangedEvent e);
    }

    /**
     * Acoustic model file list change event class
     */
    public static class VoiceListChangedEvent extends EventObject {
        /**
         * Acoustic model file change event constructor
         *
         * @param source Source Object
         */
        public VoiceListChangedEvent(Object source) {
            super(source);
        }
    }

    /**
     * A list of acoustic model change event listeners
     */
    protected List<VoiceListChangedListener> listeners = new ArrayList<>();

    /**
     * Add a change listener for the acoustic model file
     * 
     * @param listener Acoustic model file change listener
     */
    public void addVoiceListChangedListener(VoiceListChangedListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove the change listener for the acoustic model file
     * 
     * @param listener Acoustic model file change listener
     */
    public void removeVoiceListChangedListener(VoiceListChangedListener listener) {
        listeners.remove(listener);
    }

    /**
     * Handle changes to the list of acoustic model files
     */
    public void fireVoiceListChanged() {
        VoiceListChangedEvent e = new VoiceListChangedEvent(this);
        for (VoiceListChangedListener listener : listeners) {
            listener.onVoiceListChanged(e);
        }
    }

    /**
     * Acoustic Model Information Class
     */
    public static class VoiceFileInfo {

        /**
         * Path to the acoustic model file
         */
        public String path;

        /**
         * Acoustic model name
         */
        public String name;

        /**
         * Acoustic model information constructor (path and name)
         *
         * @param path Path to the acoustic model file
         * @param name Acoustic model name
         */
        VoiceFileInfo(String path, String name) {
            this.path = path;
            this.name = name;
        }

        /**
         * Acoustic model information constructor (path only)
         *
         * @param path Path to the acoustic model file
         */
        VoiceFileInfo(String path) {
            this(path, OpenJTalkWrapper.baseName(path));
        }

        /**
         * Acoustic model information constructor (empty)
         */
        VoiceFileInfo() {
            this("", "");
        }
    }

    /**
     * A JNA class that reflects the acoustic model list structure defined in the DLL.
     */
    static class HTSVoiceList extends Structure {
        public HTSVoiceList.ByReference succ;

        public String path;

        public String name;

        public HTSVoiceList() {
            super(null, Structure.ALIGN_NONE);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("succ", "path", "name");
        }

        public static class ByReference extends HTSVoiceList implements Structure.ByReference {
        }
    }

    /**
     * A JNA interface that reflects the functions defined in the DLL
     */
    interface API extends Library {

        API INSTANCE = Native.load("jtalk", API.class);

        void openjtalk_clearHTSVoiceList(Pointer handle, HTSVoiceList list);
        HTSVoiceList openjtalk_getHTSVoiceList(Pointer handle);
//        HTSVoiceList openjtalk_getHTSVoiceListSjis(Pointer handle);
        Pointer openjtalk_initialize(String voicePath, String dicPath, String voiceDirPath);
//        Pointer openjtalk_initializeSjis(String voicePath, String dicPath, String voiceDirPath);
        void openjtalk_clear(Pointer handle);
        void openjtalk_refresh(Pointer handle);
        void openjtalk_setSamplingFrequency(Pointer handle, int i);
        int openjtalk_getSamplingFrequency(Pointer handle);
        void openjtalk_setFperiod(Pointer handle, int i);
        int openjtalk_getFperiod(Pointer handle);
        void openjtalk_setAlpha(Pointer handle, double f);
        double openjtalk_getAlpha(Pointer handle);
        void openjtalk_setBeta(Pointer handle, double f);
        double openjtalk_getBeta(Pointer handle);
        void openjtalk_setSpeed(Pointer handle, double f);
        double openjtalk_getSpeed(Pointer handle);
        void openjtalk_setAdditionalHalfTone(Pointer handle, double f);
        double openjtalk_getAdditionalHalfTone(Pointer handle);
        void openjtalk_setMsdThreshold(Pointer handle, double f);
        double openjtalk_getMsdThreshold(Pointer handle);
        void openjtalk_setGvWeightForSpectrum(Pointer handle, double f);
        double openjtalk_getGvWeightForSpectrum(Pointer handle);
        void openjtalk_setGvWeightForLogF0(Pointer handle, double f);
        double openjtalk_getGvWeightForLogF0(Pointer handle);
        void openjtalk_setVolume(Pointer handle, double f);
        double openjtalk_getVolume(Pointer handle);
        Boolean openjtalk_setDic(Pointer handle, String path);
//        Boolean openjtalk_setDicSjis(Pointer handle, String path);
        String openjtalk_getDic(Pointer handle, byte[] path);
//        String openjtalk_getDicSjis(Pointer handle, byte[] path);
        Boolean openjtalk_setVoiceDir(Pointer handle, String path);
//        Boolean openjtalk_setVoiceDirSjis(Pointer handle, String path);
        String openjtalk_getVoiceDir(Pointer handle, byte[] path);
//        String openjtalk_getVoiceDirSjis(Pointer handle, byte[] path);
        Boolean openjtalk_setVoice(Pointer handle, String path);
//        Boolean openjtalk_setVoiceSjis(Pointer handle, String path);
        Boolean openjtalk_setVoicePath(Pointer handle, String path);
//        Boolean openjtalk_setVoicePathSjis(Pointer handle, String path);
        String openjtalk_getVoicePath(Pointer handle, byte[] path);
//        String openjtalk_getVoicePathSjis(Pointer handle, byte[] path);
        Boolean openjtalk_setVoiceName(Pointer handle, String path);
//        Boolean openjtalk_setVoiceNameSjis(Pointer handle, String path);
        String openjtalk_getVoiceName(Pointer handle, byte[] path);
//        String openjtalk_getVoiceNameSjis(Pointer handle, byte[] path);
        void openjtalk_speakSync(Pointer handle, String text);
//        void openjtalk_speakSyncSjis(Pointer handle, String text);
        void openjtalk_speakAsync(Pointer handle, String text);
//        void openjtalk_speakAsyncSjis(Pointer handle, String text);
        void openjtalk_pause(Pointer handle);
        void openjtalk_resume(Pointer handle);
        void openjtalk_stop(Pointer handle);
        Boolean openjtalk_isSpeaking(Pointer handle);
        Boolean openjtalk_isPaused(Pointer handle);
        Boolean openjtalk_isFinished(Pointer handle);
        void openjtalk_waitUntilDone(Pointer handle);
        void openjtalk_wait(Pointer handle, int duration);
        Boolean openjtalk_speakToFile(Pointer handle, String text, String file);
        int openjtalk_getCharCode(String text);
    }

    private Pointer handle = null;
    private List<VoiceFileInfo> voices = new ArrayList<>();

    /**
     * Constructor of the class that accesses jtalk.dll using JNA (3 arguments)
     *
     * @param voicePath Path to the acoustic model file or name
     * @param dicPath Dictionary directory path
     * @param voiceDirPath Acoustic Model Directory
     */
    public OpenJTalkWrapper(String voicePath, String dicPath, String voiceDirPath) {
        handle = API.INSTANCE.openjtalk_initialize(voicePath, dicPath, voiceDirPath);
        generateVoiceList();
    }

    /**
     * Constructor for the class that accesses jtalk.dll using JNA (2 arguments)
     *
     * @param voicePath Path to the acoustic model file or name
     * @param dicPath Dictionary directory path
     */
    public OpenJTalkWrapper(String voicePath, String dicPath) {
        this(voicePath, dicPath, null);
    }

    /**
     * Constructor of the class that accesses jtalk.dll using JNA (one argument)
     *
     * @param voicePath Path to the acoustic model file or name
     */
    public OpenJTalkWrapper(String voicePath) {
        this(voicePath, null, null);
    }

    /**
     * Constructor of the class that accesses jtalk.dll using JNA (no arguments)
     */
    public OpenJTalkWrapper() {
        this(null, null, null);
    }

    /**
     * Raises an exception if the object pointer is NULL.
     *
     * @throws IllegalStateException Null object pointer exception
     */
    private void checkOpenjtalkObject() {
        if (handle == null) {
            throw new IllegalStateException("Internal Error: OpenJTalk pointer is null");
        }
    }

    /**
     * Recursively registering acoustic model files
     *
     * @param dir Directory to be explored
     */
    private void setVoiceFile(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!file.exists()) {
                continue;
            } else if (file.isDirectory()) {
                setVoiceFile(file);
            } else if (file.isFile()) {
                String fileName = file.getName();
                int index = fileName.lastIndexOf(".htsvoice");
                if (index > 0) {
                    String path = file.getAbsolutePath();
                    String name = fileName.substring(0, index);
                    voices.add(new VoiceFileInfo(path, name));
                }
            }
        }
    }

    /**
     * Generate a list of acoustic model files
     */
    private void generateVoiceList() {
        checkOpenjtalkObject();
        String dir = getVoiceDir();
        if (voices != null) {
            voices.clear();
        } else {
            voices = new ArrayList<>();
        }
        setVoiceFile(new File(dir));
    }

    /**
     * Get the name part from a path string
     *
     * @param path Path String
     * @return Name part of the file
     */
    private static String baseName(String path) {
        File file = new File(path);
        String fileName = file.getName();
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            return fileName.substring(0, index);
        } else {
            return "";
        }
    }

    /**
     * Get a list of all acoustic model files in the Acoustic Model Directory
     *
     * @return A list of all acoustic model files
     */
    public List<VoiceFileInfo> getVoices() {
        return voices;
    }

    /**
     * Set the sampling frequency (S)
     *
     * @param i Sampling frequency (S) (integer)
     */
    public void setSamplingFrequency(int i) {
        checkOpenjtalkObject();
        if (i < 1) {
            throw new IllegalArgumentException("sampling frequency の範囲は1以上の整数です。");
        }
        API.INSTANCE.openjtalk_setSamplingFrequency(handle, i);
    }

    /**
     * Get the sampling frequency (S)
     *
     * @return Sampling frequency (S) (integer)
     */
    public int getSamplingFrequency() {
        return API.INSTANCE.openjtalk_getSamplingFrequency(handle);
    }

    /**
     * サンプリング周波数(S)を設定する
     *
     * @param i Sampling frequency (S) (integer)
     */
    public void setS(int i) {
        setSamplingFrequency(i);
    }

    /**
     * サンプリング周波数(S)を取得する
     *
     * @return Sampling frequency (S) (integer)
     */
    public int getS() {
        return getSamplingFrequency();
    }

    /**
     * Set the frame period (P)
     *
     * @param i Frame period (P) (integer)
     */
    public void setFperiod(int i) {
        checkOpenjtalkObject();
        if (i < 1) {
            throw new IllegalArgumentException("The frame period range is an integer greater than or equal to 1.");
        }
        API.INSTANCE.openjtalk_setFperiod(handle, i);
    }

    /**
     * Get the frame period (P)
     *
     * @return Frame period (P) (integer)
     */
    public int getFperiod() {
        return API.INSTANCE.openjtalk_getFperiod(handle);
    }

    /**
     * Set the frame period (P)
     *
     * @param i Frame period (P) (integer)
     */
    public void setP(int i) {
        setFperiod(i);
    }

    /**
     * Get the frame period (P)
     *
     * @return Frame period (P) (integer)
     */
    public int getP() {
        return getFperiod();
    }

    /**
     * Setting the All-pass value (Alpha)
     *
     * @param f All-pass Value (float)
     */
    public void setAlpha(double f) {
        checkOpenjtalkObject();
        if (f < 0.0 || f > 1.0) {
            throw new IllegalArgumentException("The all-pass constant is a floating point number between 0 and 1.");
        }
        API.INSTANCE.openjtalk_setAlpha(handle, f);
    }

    /**
     * Get the All-pass value (Alpha)
     *
     * @return All-pass value (Alpha) (float)
     */
    public double getAlpha() {
        return API.INSTANCE.openjtalk_getAlpha(handle);
    }

    /**
     * Setting the All-pass value (Alpha)
     *
     * @param f Allpass Value (float)
     */
    public void setA(double f) {
        setAlpha(f);
    }

    /**
     * Get the All-pass value (Alpha)
     * 
     * @return Allpass value (Alpha) (float)
     */
    public double getA() {
        return getAlpha();
    }

    /**
     * Set the post-filter coefficient (Beta)
     *
     * @param f Post-filter coefficient (Beta) (floating point number)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setBeta(double f) {
        checkOpenjtalkObject();
        if (f < 0.0 || f > 1.0) {
            throw new IllegalArgumentException("The postfiltering coefficient range is a floating point number between 0 and 1.");
        }
        API.INSTANCE.openjtalk_setBeta(handle, f);
    }

    /**
     * Get the post-filter coefficient (Beta)
     *
     * @return Post-filter coefficient (Beta) (floating point number)
     */
    public double getBeta() {
        return API.INSTANCE.openjtalk_getBeta(handle);
    }

    /**
     * Set the post-filter coefficient (Beta)
     *
     * @param f Post-filter coefficient (Beta) (floating point number)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setB(double f) {
        setBeta(f);
    }

    /**
     * Get the post-filter coefficient (Beta)
     *
     * @return Post-filter coefficient (Beta) (floating point number)
     */
    public double getB() {
        return getBeta();
    }

    /**
     * Set speech rate (R)
     *
     * @param f Speech Rate (R) (floating point)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setSpeed(double f) {
        checkOpenjtalkObject();
        if (f < 0.0) {
            throw new IllegalArgumentException("The speech speed rate is a floating point number ranging from 0 to 9999.");
        }
        API.INSTANCE.openjtalk_setSpeed(handle, f);
    }

    /**
     * Get speech rate (R)
     * 
     * @return Speech Rate (R) (floating point)
     */
    public double getSpeed() {
        return API.INSTANCE.openjtalk_getSpeed(handle);
    }

    /**
     * Set speech rate (R)
     *
     * @param f Speech Rate (R) (floating point)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setR(double f) {
        setSpeed(f);
    }

    /**
     * Get speech rate (R)
     *
     * @return Speech Rate (R) (floating point)
     */
    public double getR() {
        return getSpeed();
    }

    /**
     * Set additional halftone (Fm)
     * 
     * @param f Additional Halftone (Fm) (floating point)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setAdditionalHalfTone(double f) {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_setAdditionalHalfTone(handle, f);
    }

    /**
     * Get additional halftones (Fm)
     *
     * @return Additional Halftone (Fm) (floating point)
     */
    public double getAdditionalHalfTone() {
        return API.INSTANCE.openjtalk_getAdditionalHalfTone(handle);
    }

    /**
     * Set additional halftone (Fm)
     *
     * @param f Additional Halftone (Fm) (floating point)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setFm(double f) {
        setAdditionalHalfTone(f);
    }

    /**
     * Get additional halftones (Fm)
     *
     * @return Additional Halftone (Fm) (floating point)
     */
    public double getFm() {
        return getAdditionalHalfTone();
    }

    /**
     * Set the voiced/unvoiced boundary (U)
     *
     * @param f Voiced/unvoiced boundary (U) (floating point number)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setMsdThreshold(double f) {
        checkOpenjtalkObject();
        if (f < 0.0 || f > 1.0) {
            throw new IllegalArgumentException("The voiced/unvoiced threshold range is a float between 0 and 1.");
        }
        API.INSTANCE.openjtalk_setMsdThreshold(handle, f);
    }

    /**
     * Get the voiced/unvoiced boundary (U)
     *
     * @return Voiced/unvoiced boundary (U) (floating point number)
     */
    public double getMsdThreshold() {
        return API.INSTANCE.openjtalk_getMsdThreshold(handle);
    }

    /**
     * Set the voiced/unvoiced boundary (U)
     *
     * @param f Voiced/unvoiced boundary (U) (floating point number)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setU(double f) {
        setMsdThreshold(f);
    }

    /**
     * Get the voiced/unvoiced boundary (U)
     *
     * @return Voiced/unvoiced boundary (U) (floating point number)
     */
    public double getU() {
        return getMsdThreshold();
    }

    /**
     * Set the weighting of the fluctuation within the spectrum series (Jm)
     *
     * @param f Weight of fluctuation within spectrum series (Jm) (floating point number)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setGvWeightForSpectrum(double f) {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_setGvWeightForSpectrum(handle, f);
    }

    /**
     * Obtain the weight of fluctuation within the spectrum series (Jm)
     *
     * @return Weight of fluctuation within spectrum series (Jm) (floating point number)
     */
    public double getGvWeightForSpectrum() {
        return API.INSTANCE.openjtalk_getGvWeightForSpectrum(handle);
    }

    /**
     * Set the weighting of the fluctuation within the spectrum series (Jm)
     *
     * @param f Weight of fluctuation within spectrum series (Jm) (floating point number)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setJm(double f) {
        setGvWeightForSpectrum(f);
    }

    /**
     * Obtain the weight of fluctuation within the spectrum series (Jm)
     *
     * @return Weight of fluctuation within spectrum series (Jm) (floating point number)
     */
    public double getJm() {
        return getGvWeightForSpectrum();
    }

    /**
     * Set the F0 intra-series variation weight (Jf)
     *
     * @param f F0 intra-series variation weighting (Jf) (floating point number)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setGvWeightForLogF0(double f) {
        checkOpenjtalkObject();
        if (f < 0.0) {
            throw new IllegalArgumentException("The range of weight of GV for spectrum is a floating point number equal to or greater than 0.");
        }
        API.INSTANCE.openjtalk_setGvWeightForLogF0(handle, f);
    }

    /**
     * Obtain the F0 intrasequence variation weight (Jf)
     *
     * @return F0 intra-series variation weighting (Jf) (floating point number)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public double getGvWeightForLogF0() {
        return API.INSTANCE.openjtalk_getGvWeightForLogF0(handle);
    }

    /**
     * Set the F0 intra-series variation weight (Jf)
     *
     * @param f F0 intra-series variation weighting (Jf) (floating point number)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setJf(double f) {
        setGvWeightForLogF0(f);
    }

    /**
     * Obtain the F0 intrasequence variation weight (Jf)
     *
     * @return F0 intra-series variation weighting (Jf) (floating point number)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public double getJf() {
        return getGvWeightForLogF0();
    }

    /**
     * Set the volume (G)
     *
     * @param f Volume(G) (float)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setVolume(double f) {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_setVolume(handle, f);
    }

    /**
     * Get Volume (G)
     *
     * @return Volume(G) (float)
     */
    public double getVolume() {
        return API.INSTANCE.openjtalk_getVolume(handle);
    }

    /**
     * Set the volume (G)
     *
     * @param f Volume(G) (float)
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void setG(double f) {
        setVolume(f);
    }

    /**
     * Get Volume (G)
     *
     * @return Volume(G) (float)
     */
    public double getG() {
        return getVolume();
    }

    /**
     * Set the dictionary directory
     *
     * @param path Dictionary directory path
     * @throws FileNotFoundException Dictionaries folder not found
     */
    public void setDic(String path) throws IOException {
        checkOpenjtalkObject();
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("The string indicating the dictionary folder is empty.");
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("The dictionaries folder cannot be found.");
        }
        boolean res = API.INSTANCE.openjtalk_setDic(handle, path);
        if (!res) {
            throw new IllegalStateException("Unable to set dictionary folder. Dictionary may not be for UTF-8.");
        }
    }

    /**
     * Get the dictionary directory
     *
     * @return Dictionary directory path
     */
    public String getDic() {
        byte[] buff = new byte[MAXPATH];
        String res = API.INSTANCE.openjtalk_getDic(handle, buff);
        String path = new String(buff, StandardCharsets.UTF_8);
        return res != null ? path.trim() : null;
    }

    /**
     * Setting the Acoustic Model Directory
     *
     * @param path Acoustic Model Directory
     * @throws FileNotFoundException Acoustic model folder not found
     */
    public void setVoiceDir(String path) throws IOException {
        checkOpenjtalkObject();
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("The string indicating the acoustic model folder is empty.");
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("Acoustic model folder not found.");
        }
        boolean res = API.INSTANCE.openjtalk_setVoiceDir(handle, path);
        if (!res) {
            throw new IllegalStateException("Unable to set acoustic model folder.");
        }
        generateVoiceList();
        fireVoiceListChanged();
    }

    /**
     * Get the current Acoustic Model Directory
     *
     * @return Acoustic Model Directory Path
     */
    public String getVoiceDir() {
        byte[] buff = new byte[MAXPATH];
        String res = API.INSTANCE.openjtalk_getVoiceDir(handle, buff);
        String path = new String(buff, StandardCharsets.UTF_8);
        return res != null ? path.trim() : null;
    }

    /**
     * Set the current acoustic model by the path to the acoustic model file.
     *
     * @param path Acoustic model path
     */
    public void setVoicePath(String path) {
        checkOpenjtalkObject();
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("The acoustic model string is empty.");
        }
        boolean res = API.INSTANCE.openjtalk_setVoicePath(handle, path);
        if (!res) {
            throw new IllegalStateException("Unable to set acoustic model.");
        }
    }

    /**
     * Get the current path to the acoustic model file
     *
     * @return Acoustic model path
     */
    public String getVoicePath() {
        byte[] buff = new byte[MAXPATH];
        API.INSTANCE.openjtalk_getVoicePath(handle, buff);
        String path = new String(buff, StandardCharsets.UTF_8);
        return path.trim();
    }

    /**
     * Set the current acoustic model by acoustic model name
     *
     * @param name Acoustic model name
     */
    public void setVoiceName(String name) {
        checkOpenjtalkObject();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("The acoustic model string is empty.");
        }
        boolean res = API.INSTANCE.openjtalk_setVoiceName(handle, name);
        if (!res) {
            throw new IllegalStateException("Unable to set acoustic model.");
        }
    }

    /**
     * Get the current acoustic model name
     *
     * @return Acoustic model name
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public String getVoiceName() {
        byte[] buff = new byte[MAXPATH];
        API.INSTANCE.openjtalk_getVoiceName(handle, buff);
        String name = new String(buff, StandardCharsets.UTF_8);
        return name.trim();
    }

    /**
     * Set the current acoustic model by the path to the acoustic model file.
     *
     * @param path Path to the audio file
     */
    public void setVoice(String path) {
        checkOpenjtalkObject();
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("The acoustic model string is empty.");
        }
        boolean res = API.INSTANCE.openjtalk_setVoice(handle, path);
        if (!res) {
            throw new IllegalStateException("Unable to set acoustic model.");
        }
    }

    /**
     * Set the current acoustic model by acoustic model information
     *
     * @param arg Acoustic model information (not null)
     */
    public void setVoice(VoiceFileInfo arg) {
        checkOpenjtalkObject();
        if (arg == null) {
            throw new IllegalArgumentException("The acoustic model specification is NULL.");
        }
        String path = arg.path;
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("The acoustic model string is empty.");
        }
        boolean res = API.INSTANCE.openjtalk_setVoice(handle, path);
        if (!res) {
            throw new IllegalStateException("Unable to set acoustic model.");
        }
    }

    /**
     * Get the current acoustic model information
     *
     * @return Acoustic model information object
     */
    public VoiceFileInfo getVoice() {
        byte[] buffPath = new byte[MAXPATH];
        byte[] buffName = new byte[MAXPATH];
        API.INSTANCE.openjtalk_getVoicePath(handle, buffPath);
        String path = new String(buffPath, StandardCharsets.UTF_8);
        API.INSTANCE.openjtalk_getVoiceName(handle, buffName);
        String name = new String(buffName, StandardCharsets.UTF_8);
        VoiceFileInfo result = new VoiceFileInfo();
        result.path = path.trim();
        result.name = name.trim();
        return result;
    }

    /**
     * Synchronize speech
     *
     * @param text String
     */
    public void speakSync(String text) {
        checkOpenjtalkObject();
        if (text == null || text.isEmpty()) {
            return;
        }
        API.INSTANCE.openjtalk_speakSync(handle, text);
    }

    /**
     * Asynchronous speech
     *
     * @param text String
     */
    public void speakAsync(String text) {
        checkOpenjtalkObject();
        if (text == null || text.isEmpty()) {
            return;
        }
        API.INSTANCE.openjtalk_speakAsync(handle, text);
    }

    /**
     * Pause asynchronous utterances
     */
    public void pause() {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_pause(handle);
    }

    /**
     * Resume asynchronous utterance pause
     *
     * @throws IllegalStateException Exceptions such as null object pointer
     */
    public void resume() {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_resume(handle);
    }

    /**
     * Force stop of asynchronous speech
     */
    public void stop() {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_stop(handle);
    }

    /**
     * Whether or not the person is speaking asynchronously
     *
     * @return Boolean value of whether or not the voice is being spoken
     */
    public boolean isSpeaking() {
        checkOpenjtalkObject();
        return API.INSTANCE.openjtalk_isSpeaking(handle);
    }

    /**
     * Whether asynchronous utterances are paused
     *
     * @return A boolean value indicating whether the game is paused.
     */
    public boolean isPaused() {
        checkOpenjtalkObject();
        return API.INSTANCE.openjtalk_isPaused(handle);
    }

    /**
     * Whether the asynchronous utterance is completed
     *
     * @return Boolean value indicating whether the task is completed
     */
    public boolean isFinished() {
        checkOpenjtalkObject();
        return API.INSTANCE.openjtalk_isFinished(handle);
    }

    /**
     * Wait while speaking
     */
    public void waitUntilDone() {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_waitUntilDone(handle);
    }

    /**
     * Wait for a specified time
     *
     * @param duration Wait Time (ms)
     */
    public void wait(int duration) {
        checkOpenjtalkObject();
        if (duration == 0) {
            waitUntilDone();
        } else {
            API.INSTANCE.openjtalk_wait(handle, duration);
        }
    }

    /**
     * Save the audio of a string to a file in wav format
     *
     * @param text Speech string
     * @param file Save file name
     */
    public void speakToFile(String text, String file) {
        checkOpenjtalkObject();
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("The speech string is empty.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("The filename string is empty.");
        }
        if (!API.INSTANCE.openjtalk_speakToFile(handle, text, file)) {
            throw new IllegalStateException("An error occurred while creating the audio file.");
        }
    }
}
