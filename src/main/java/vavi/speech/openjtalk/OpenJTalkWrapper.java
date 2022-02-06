/*
 * https://github.com/rosmarinus/jtalkdll/blob/master/ffi/java/JTalkJna.java
 */

package vavi.speech.openjtalk;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import javax.swing.event.EventListenerList;


/**
 * OpenJTalkWrapper.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/26 umjammer initial version <br>
 */
public class OpenJTalkWrapper {

    private static final int MAXPATH = 260;

    /**
     * 音響モデルファイルのリストの変化リスナーインターフェイス
     */
    public interface VoiceListChangedListener extends EventListener {
        public void onVoiceListChanged(VoiceListChangedEvent e);
    }

    /**
     * 音響モデルファイルのリストの変化イベントクラス
     */
    public class VoiceListChangedEvent extends EventObject {
        /**
         * 音響モデルファイルの変化イベントコンストラクタ
         *
         * @param source ソースオブジェクト
         */
        public VoiceListChangedEvent(Object source) {
            super(source);
        }
    }

    /**
     * 音響モデル変化イベントリスナーのリスト
     */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * 音響モデルファイルの変化リスナーを追加する
     * 
     * @param listener 音響モデルファイルの変化リスナー
     */
    public void addVoiceListChangedListener(VoiceListChangedListener listener) {
        listenerList.add(VoiceListChangedListener.class, listener);
    }

    /**
     * 音響モデルファイルの変化リスナーを削除する
     * 
     * @param listener 音響モデルファイルの変化リスナー
     */
    public void removeVoiceListChangedListener(VoiceListChangedListener listener) {
        listenerList.remove(VoiceListChangedListener.class, listener);
    }

    /**
     * 音響モデルファイルのリストが変化したときの処理を行う
     */
    public void fireVoiceListChanged() {
        VoiceListChangedEvent e = new VoiceListChangedEvent(this);
        for (VoiceListChangedListener listener : listenerList.getListeners(VoiceListChangedListener.class)) {
            listener.onVoiceListChanged(e);
        }
    }

    /**
     * 音響モデル情報クラス
     */
    public static class VoiceFileInfo {

        /**
         * 音響モデルファイルのパス
         */
        public String path;

        /**
         * 音響モデルの名前
         */
        public String name;

        /**
         * 音響モデル情報コンストラクタ（パスと名前）
         *
         * @param path 音響モデルファイルのパス
         * @param name 音響モデルの名前
         */
        VoiceFileInfo(String path, String name) {
            this.path = path;
            this.name = name;
        }

        /**
         * 音響モデル情報コンストラクタ（パスのみ）
         *
         * @param path 音響モデルファイルのパス
         */
        VoiceFileInfo(String path) {
            this(path, OpenJTalkWrapper.baseName(path));
        }

        /**
         * 音響モデル情報コンストラクタ（空）
         */
        VoiceFileInfo() {
            this("", "");
        }
    }

    /**
     * DLLで定義されている音響モデルリスト構造体を反映したJNAクラス
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
     * DLLで定義されている関数を反映したJNAインターフェイス
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
     * jtalk.dllをJNAを使ってアクセスするクラスのコンストラクタ（3引数）
     *
     * @param voicePath 音響モデルファイルのパスもしくは名前
     * @param dicPath 辞書ディレクトリのパス
     * @param voiceDirPath 音響モデルディレクトリ
     */
    public OpenJTalkWrapper(String voicePath, String dicPath, String voiceDirPath) {
        handle = API.INSTANCE.openjtalk_initialize(voicePath, dicPath, voiceDirPath);
        generateVoiceList();
    }

    /**
     * jtalk.dllをJNAを使ってアクセスするクラスのコンストラクタ（2引数）
     *
     * @param voicePath 音響モデルファイルのパスもしくは名前
     * @param dicPath 辞書ディレクトリのパス
     */
    public OpenJTalkWrapper(String voicePath, String dicPath) {
        this(voicePath, dicPath, null);
    }

    /**
     * jtalk.dllをJNAを使ってアクセスするクラスのコンストラクタ（1引数）
     *
     * @param voicePath 音響モデルファイルのパスもしくは名前
     */
    public OpenJTalkWrapper(String voicePath) {
        this(voicePath, null, null);
    }

    /**
     * jtalk.dllをJNAを使ってアクセスするクラスのコンストラクタ（引数なし）
     */
    public OpenJTalkWrapper() {
        this(null, null, null);
    }

    /**
     * オブジェクトポインタがNULLであれば例外を発生させる
     *
     * @throws IllegalStateException オブジェクトポインタがnullの例外
     */
    private void checkOpenjtalkObject() {
        if (handle == null) {
            throw new IllegalStateException("Internal Error: OpenJTalk pointer is null");
        }
    }

    /**
     * 再帰的に音響モデルファイルを登録する
     *
     * @param dir 探査対象ディレクトリ
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
     * 音響モデルファイルのリストを生成する
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
     * パス文字列から名前部分を取得する
     *
     * @param path パス文字列
     * @return ファイルの名前部分
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
     * 音響モデルディレクトリにある全ての音響モデルファイルのリストを取得する
     *
     * @return 全ての音響モデルファイルのリスト
     */
    public List<VoiceFileInfo> getVoices() {
        return voices;
    }

    /**
     * サンプリング周波数(S)を設定する
     *
     * @param i サンプリング周波数(S)（整数）
     */
    public void setSamplingFrequency(int i) {
        checkOpenjtalkObject();
        if (i < 1) {
            throw new IllegalArgumentException("sampling frequency の範囲は1以上の整数です。");
        }
        API.INSTANCE.openjtalk_setSamplingFrequency(handle, i);
    }

    /**
     * サンプリング周波数(S)を取得する
     *
     * @return サンプリング周波数(S)（整数）
     */
    public int getSamplingFrequency() {
        return API.INSTANCE.openjtalk_getSamplingFrequency(handle);
    }

    /**
     * サンプリング周波数(S)を設定する
     *
     * @param i サンプリング周波数(S)（整数）
     */
    public void setS(int i) {
        setSamplingFrequency(i);
    }

    /**
     * サンプリング周波数(S)を取得する
     *
     * @return サンプリング周波数(S)（整数）
     */
    public int getS() {
        return getSamplingFrequency();
    }

    /**
     * フレームピリオド(P)を設定する
     *
     * @param i フレームピリオド(P)（整数）
     */
    public void setFperiod(int i) {
        checkOpenjtalkObject();
        if (i < 1) {
            throw new IllegalArgumentException("frame period の範囲は1以上の整数です。");
        }
        API.INSTANCE.openjtalk_setFperiod(handle, i);
    }

    /**
     * フレームピリオド(P)を取得する
     *
     * @return フレームピリオド(P)（整数）
     */
    public int getFperiod() {
        return API.INSTANCE.openjtalk_getFperiod(handle);
    }

    /**
     * フレームピリオド(P)を設定する
     *
     * @param i フレームピリオド(P)（整数）
     */
    public void setP(int i) {
        setFperiod(i);
    }

    /**
     * フレームピリオド(P)を取得する
     *
     * @return フレームピリオド(P)（整数）
     */
    public int getP() {
        return getFperiod();
    }

    /**
     * オールパス値(Alpha)を設定する
     *
     * @param f オールパス値（浮動小数点数）
     */
    public void setAlpha(double f) {
        checkOpenjtalkObject();
        if (f < 0.0 || f > 1.0) {
            throw new IllegalArgumentException("all-pass constant の範囲は0と1の間の浮動小数点数です。");
        }
        API.INSTANCE.openjtalk_setAlpha(handle, f);
    }

    /**
     * オールパス値(Alpha)を取得する
     *
     * @return オールパス値(Alpha)（浮動小数点数）
     */
    public double getAlpha() {
        return API.INSTANCE.openjtalk_getAlpha(handle);
    }

    /**
     * オールパス値(Alpha)を設定する
     *
     * @param f オールパス値（浮動小数点数）
     */
    public void setA(double f) {
        setAlpha(f);
    }

    /**
     * オールパス値(Alpha)を取得する
     * 
     * @return オールパス値(Alpha)（浮動小数点数）
     */
    public double getA() {
        return getAlpha();
    }

    /**
     * ポストフィルター係数(Beta)を設定する
     *
     * @param f ポストフィルター係数(Beta)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public void setBeta(double f) {
        checkOpenjtalkObject();
        if (f < 0.0 || f > 1.0) {
            throw new IllegalArgumentException("postfiltering coefficient の範囲は0と1の間の浮動小数点数です。");
        }
        API.INSTANCE.openjtalk_setBeta(handle, f);
    }

    /**
     * ポストフィルター係数(Beta)を取得する
     *
     * @return ポストフィルター係数(Beta)（浮動小数点数）
     */
    public double getBeta() {
        return API.INSTANCE.openjtalk_getBeta(handle);
    }

    /**
     * ポストフィルター係数(Beta)を設定する
     *
     * @param f ポストフィルター係数(Beta)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがNULLなどの例外
     */
    public void setB(double f) {
        setBeta(f);
    }

    /**
     * ポストフィルター係数(Beta)を取得する
     *
     * @return ポストフィルター係数(Beta)（浮動小数点数）
     */
    public double getB() {
        return getBeta();
    }

    /**
     * スピーチ速度(R)を設定する
     *
     * @param f スピーチ速度(R)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがNULLなどの例外
     */
    public void setSpeed(double f) {
        checkOpenjtalkObject();
        if (f < 0.0) {
            throw new IllegalArgumentException("speech speed rate の範囲は0以上の浮動小数点数です。");
        }
        API.INSTANCE.openjtalk_setSpeed(handle, f);
    }

    /**
     * スピーチ速度(R)を取得する
     * 
     * @return スピーチ速度(R)（浮動小数点数）
     */
    public double getSpeed() {
        return API.INSTANCE.openjtalk_getSpeed(handle);
    }

    /**
     * スピーチ速度(R)を設定する
     *
     * @param f スピーチ速度(R)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがNULLなどの例外
     */
    public void setR(double f) {
        setSpeed(f);
    }

    /**
     * スピーチ速度(R)を取得する
     *
     * @return スピーチ速度(R)（浮動小数点数）
     */
    public double getR() {
        return getSpeed();
    }

    /**
     * 追加ハーフトーン(Fm)を設定する
     * 
     * @param f 追加ハーフトーン(Fm)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public void setAdditionalHalfTone(double f) {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_setAdditionalHalfTone(handle, f);
    }

    /**
     * 追加ハーフトーン(Fm)を取得する
     *
     * @return 追加ハーフトーン(Fm)（浮動小数点数）
     */
    public double getAdditionalHalfTone() {
        return API.INSTANCE.openjtalk_getAdditionalHalfTone(handle);
    }

    /**
     * 追加ハーフトーン(Fm)を設定する
     *
     * @param f 追加ハーフトーン(Fm)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public void setFm(double f) {
        setAdditionalHalfTone(f);
    }

    /**
     * 追加ハーフトーン(Fm)を取得する
     *
     * @return 追加ハーフトーン(Fm)（浮動小数点数）
     */
    public double getFm() {
        return getAdditionalHalfTone();
    }

    /**
     * 有声 / 無声境界値(U)を設定する
     *
     * @param f 有声 / 無声境界値(U)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public void setMsdThreshold(double f) {
        checkOpenjtalkObject();
        if (f < 0.0 || f > 1.0) {
            throw new IllegalArgumentException("voiced/unvoiced threshold の範囲は0と1の間の浮動小数点数です。");
        }
        API.INSTANCE.openjtalk_setMsdThreshold(handle, f);
    }

    /**
     * 有声 / 無声境界値(U)を取得する
     *
     * @return 有声 / 無声境界値(U)（浮動小数点数）
     */
    public double getMsdThreshold() {
        return API.INSTANCE.openjtalk_getMsdThreshold(handle);
    }

    /**
     * 有声 / 無声境界値(U)を設定する
     *
     * @param f 有声 / 無声境界値(U)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public void setU(double f) {
        setMsdThreshold(f);
    }

    /**
     * 有声 / 無声境界値(U)を取得する
     *
     * @return 有声 / 無声境界値(U)（浮動小数点数）
     */
    public double getU() {
        return getMsdThreshold();
    }

    /**
     * スペクトラム系列内変動の重み(Jm)を設定する
     *
     * @param f スペクトラム系列内変動の重み(Jm)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public void setGvWeightForSpectrum(double f) {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_setGvWeightForSpectrum(handle, f);
    }

    /**
     * スペクトラム系列内変動の重み(Jm)を取得する
     *
     * @return スペクトラム系列内変動の重み(Jm)（浮動小数点数）
     */
    public double getGvWeightForSpectrum() {
        return API.INSTANCE.openjtalk_getGvWeightForSpectrum(handle);
    }

    /**
     * スペクトラム系列内変動の重み(Jm)を設定する
     *
     * @param f スペクトラム系列内変動の重み(Jm)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public void setJm(double f) {
        setGvWeightForSpectrum(f);
    }

    /**
     * スペクトラム系列内変動の重み(Jm)を取得する
     *
     * @return スペクトラム系列内変動の重み(Jm)（浮動小数点数）
     */
    public double getJm() {
        return getGvWeightForSpectrum();
    }

    /**
     * F0系列内変動重み(Jf)を設定する
     *
     * @param f F0系列内変動重み(Jf)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public void setGvWeightForLogF0(double f) {
        checkOpenjtalkObject();
        if (f < 0.0) {
            throw new IllegalArgumentException("weight of GV for spectrum の範囲は0以上の浮動小数点数です。");
        }
        API.INSTANCE.openjtalk_setGvWeightForLogF0(handle, f);
    }

    /**
     * F0系列内変動重み(Jf)を取得する
     *
     * @return F0系列内変動重み(Jf)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public double getGvWeightForLogF0() {
        return API.INSTANCE.openjtalk_getGvWeightForLogF0(handle);
    }

    /**
     * F0系列内変動重み(Jf)を設定する
     *
     * @param f F0系列内変動重み(Jf)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public void setJf(double f) {
        setGvWeightForLogF0(f);
    }

    /**
     * F0系列内変動重み(Jf)を取得する
     *
     * @return F0系列内変動重み(Jf)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public double getJf() {
        return getGvWeightForLogF0();
    }

    /**
     * ボリューム(G)を設定する
     *
     * @param f ボリューム(G)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public void setVolume(double f) {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_setVolume(handle, f);
    }

    /**
     * ボリューム(G)を取得する
     *
     * @return ボリューム(G)（浮動小数点数）
     */
    public double getVolume() {
        return API.INSTANCE.openjtalk_getVolume(handle);
    }

    /**
     * ボリューム(G)を設定する
     *
     * @param f ボリューム(G)（浮動小数点数）
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public void setG(double f) {
        setVolume(f);
    }

    /**
     * ボリューム(G)を取得する
     *
     * @return ボリューム(G)（浮動小数点数）
     */
    public double getG() {
        return getVolume();
    }

    /**
     * 辞書ディレクトリを設定する
     *
     * @param path 辞書ディレクトリのパス
     * @throws FileNotFoundException 辞書フォルダが見つかりません
     */
    public void setDic(String path) throws IOException {
        checkOpenjtalkObject();
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("辞書フォルダを示す文字列が空です。");
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("辞書フォルダが見つかりません。");
        }
        boolean res = API.INSTANCE.openjtalk_setDic(handle, path);
        if (!res) {
            throw new IllegalStateException("辞書フォルダを設定できません。UTF-8向けの辞書ではないかもしれません。");
        }
    }

    /**
     * 辞書ディレクトリを取得する
     *
     * @return 辞書ディレクトリのパス
     * @throws UnsupportedEncodingException 
     */
    public String getDic() {
        byte[] buff = new byte[MAXPATH];
        String res = API.INSTANCE.openjtalk_getDic(handle, buff);
        String path = new String(buff, Charset.forName("UTF-8"));
        return res != null ? path.trim() : null;
    }

    /**
     * 音響モデルディレクトリを設定する
     *
     * @param path 音響モデルディレクトリ
     * @throws FileNotFoundException 音響モデルフォルダが見つかりません
     */
    public void setVoiceDir(String path) throws IOException {
        checkOpenjtalkObject();
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("音響モデルフォルダを示す文字列が空です。");
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("音響モデルフォルダが見つかりません。");
        }
        boolean res = API.INSTANCE.openjtalk_setVoiceDir(handle, path);
        if (!res) {
            throw new IllegalStateException("音響モデルフォルダを設定できません。");
        }
        generateVoiceList();
        fireVoiceListChanged();
    }

    /**
     * 現在の音響モデルディレクトリを取得する
     *
     * @return 音響モデルディレクトリのパス
     */
    public String getVoiceDir() {
        byte[] buff = new byte[MAXPATH];
        String res = API.INSTANCE.openjtalk_getVoiceDir(handle, buff);
        String path = new String(buff, Charset.forName("UTF-8"));
        return res != null ? path.trim() : null;
    }

    /**
     * 音響モデルファイルのパスによって現在の音響モデルを設定する
     *
     * @param path 音響モデルのパス
     */
    public void setVoicePath(String path) {
        checkOpenjtalkObject();
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("音響モデルを示す文字列が空です。");
        }
        boolean res = API.INSTANCE.openjtalk_setVoicePath(handle, path);
        if (!res) {
            throw new IllegalStateException("音響モデルを設定できません。");
        }
    }

    /**
     * 現在の音響モデルファイルのパスを取得する
     *
     * @return 音響モデルのパス
     */
    public String getVoicePath() {
        byte[] buff = new byte[MAXPATH];
        API.INSTANCE.openjtalk_getVoicePath(handle, buff);
        String path = new String(buff, Charset.forName("UTF-8"));
        return path.trim();
    }

    /**
     * 音響モデル名で現在の音響モデルを設定する
     *
     * @param name 音響モデル名
     */
    public void setVoiceName(String name) {
        checkOpenjtalkObject();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("音響モデルを示す文字列が空です。");
        }
        boolean res = API.INSTANCE.openjtalk_setVoiceName(handle, name);
        if (!res) {
            throw new IllegalStateException("音響モデルを設定できません。");
        }
    }

    /**
     * 現在の音響モデル名を取得する
     *
     * @return 音響モデル名
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public String getVoiceName() {
        byte[] buff = new byte[MAXPATH];
        API.INSTANCE.openjtalk_getVoiceName(handle, buff);
        String name = new String(buff, Charset.forName("UTF-8"));
        return name.trim();
    }

    /**
     * 音響モデルファイルのパスによって現在の音響モデルを設定する
     *
     * @param path 音響ファイルのパス
     */
    public void setVoice(String path) {
        checkOpenjtalkObject();
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("音響モデルを示す文字列が空です。");
        }
        boolean res = API.INSTANCE.openjtalk_setVoice(handle, path);
        if (!res) {
            throw new IllegalStateException("音響モデルを設定できません。");
        }
    }

    /**
     * 音響モデル情報によって現在の音響モデルを設定する
     *
     * @param arg 音響モデル情報 (not null)
     */
    public void setVoice(VoiceFileInfo arg) {
        checkOpenjtalkObject();
        if (arg == null) {
            throw new IllegalArgumentException("音響モデルの指定がNULLです。");
        }
        String path = arg.path;
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("音響モデルを示す文字列が空です。");
        }
        boolean res = API.INSTANCE.openjtalk_setVoice(handle, path);
        if (!res) {
            throw new IllegalStateException("音響モデルを設定できません。");
        }
    }

    /**
     * 現在の音響モデル情報を取得する
     *
     * @return 音響モデル情報オブジェクト
     */
    public VoiceFileInfo getVoice() {
        byte[] buffPath = new byte[MAXPATH];
        byte[] buffName = new byte[MAXPATH];
        API.INSTANCE.openjtalk_getVoicePath(handle, buffPath);
        String path = new String(buffPath, Charset.forName("UTF-8"));
        API.INSTANCE.openjtalk_getVoiceName(handle, buffName);
        String name = new String(buffName, Charset.forName("UTF-8"));
        VoiceFileInfo result = new VoiceFileInfo();
        result.path = path.trim();
        result.name = name.trim();
        return result;
    }

    /**
     * 同期発声する
     *
     * @param text 文字列
     */
    public void speakSync(String text) {
        checkOpenjtalkObject();
        if (text == null || text.isEmpty()) {
            return;
        }
        API.INSTANCE.openjtalk_speakSync(handle, text);
    }

    /**
     * 非同期発声をする
     *
     * @param text 文字列
     */
    public void speakAsync(String text) {
        checkOpenjtalkObject();
        if (text == null || text.isEmpty()) {
            return;
        }
        API.INSTANCE.openjtalk_speakAsync(handle, text);
    }

    /**
     * 非同期発声を一時停止する
     */
    public void pause() {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_pause(handle);
    }

    /**
     * 非同期発声の一時停止を再開する
     *
     * @throws IllegalStateException オブジェクトポインタがnullなどの例外
     */
    public void resume() {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_resume(handle);
    }

    /**
     * 非同期発声を強制停止する
     */
    public void stop() {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_stop(handle);
    }

    /**
     * 非同期発声しているかどうか
     *
     * @return 発声しているかどうかの真偽値
     */
    public boolean isSpeaking() {
        checkOpenjtalkObject();
        return API.INSTANCE.openjtalk_isSpeaking(handle);
    }

    /**
     * 非同期発声が一時停止しているかどうか
     *
     * @return 一時停止しているかどうかの真偽値
     */
    public boolean isPaused() {
        checkOpenjtalkObject();
        return API.INSTANCE.openjtalk_isPaused(handle);
    }

    /**
     * 非同期発声が完了したかどうか
     *
     * @return 完了したかどうかの真偽値
     */
    public boolean isFinished() {
        checkOpenjtalkObject();
        return API.INSTANCE.openjtalk_isFinished(handle);
    }

    /**
     * 発声している間待機する
     */
    public void waitUntilDone() {
        checkOpenjtalkObject();
        API.INSTANCE.openjtalk_waitUntilDone(handle);
    }

    /**
     * 指定時間待機する
     *
     * @param duration 待機時間(ms)
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
     * 文字列の音声をwav形式でファイルに保存する
     *
     * @param text 読み上げ文字列
     * @param file 保存ファイル名
     */
    public void speakToFile(String text, String file) {
        checkOpenjtalkObject();
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("読み上げ文字列が空です。");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("ファイル名文字列が空です。");
        }
        if (!API.INSTANCE.openjtalk_speakToFile(handle, text, file)) {
            throw new IllegalStateException("音声ファイルの作成中にエラーが発生しました。");
        }
    }
}
