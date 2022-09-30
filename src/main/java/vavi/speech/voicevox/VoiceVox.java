package vavi.speech.voicevox;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jna.Library;
import com.sun.jna.Native;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PropsEntity
public class VoiceVox {

    @Property(name = "vavi.speech.voicevox.VoiceVox.bundlePath")
    String bundlePath;

    /** */
    private static VoiceVox instance;

    /** */
    public static VoiceVox getInstance() {
        if (instance == null) {
            try {
                instance = new VoiceVox();
                VoiceVox bean = new VoiceVox();
                PropsEntity.Util.bind(bean);
                if (bean.bundlePath == null) {
                    throw new IllegalStateException("system property vavi.speech.voicevox.VoiceVox.bundlePath must not be null");
                }
                instance.bundlePath = bean.bundlePath;
Debug.println("bundlePath: " + instance.bundlePath);
                boolean r = API.INSTANCE.initialize(instance.bundlePath, false, 0);
                if (!r) {
Debug.println("init failed");
                    throw new IllegalStateException(API.INSTANCE.last_error_message());
                }
Debug.println("init succeed");
                String metas = API.INSTANCE.metas();
//Debug.println("metas: " + metas);
                Gson gson = new GsonBuilder().create();
                Type collectionType = new TypeToken<Collection<Voice>>(){}.getType();
                List<Voice> voices = gson.fromJson(metas, collectionType);
                voices.forEach(v -> instance.voices.put(v.name, v));
                instance.voices.forEach((k, v) -> Debug.println(k + ": " + v));
Debug.println("supported_devices: " + API.INSTANCE.supported_devices());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return instance;
    }

    /** */
    Map<String, Voice> voices = new HashMap<>();

    /* */
    private VoiceVox() {}

    @Override
    protected void finalize() {
        API.INSTANCE.finalize();
    }

    static class Voice {
        String name;
        String speaker_uuid;
        static class Style {
            int id;
            String name;
            @Override
            public String toString() {
                return "Style{" +
                        "id=" + id +
                        ", name='" + name + '\'' +
                        '}';
            }
        }
        List<Style> styles;
        String version;
        @Override
        public String toString() {
            return "Voice{" +
                    "name='" + name + '\'' +
                    ", speaker_uuid='" + speaker_uuid + '\'' +
                    ", styles=" + styles +
                    ", version='" + version + '\'' +
                    '}';
        }
    }
    interface API extends Library {

        VoiceVox.API INSTANCE = Native.load("core_cpu_universal2", VoiceVox.API.class);

        /**
         * 初期化する
         * <p>
         * 音声合成するための初期化を行う。他の関数を正しく実行するには先に初期化が必要
         * 何度も実行可能。use_gpuを変更して実行しなおすことも可能。
         * 最後に実行したuse_gpuに従って他の関数が実行される。
         * </p>
         * @param root_dir_path   必要なファイルがあるディレクトリ。相対パス・絶対パスどちらも指定可能。文字コードはUTF-8
         * @param use_gpu         trueならGPU用、falseならCPU用の初期化を行う
         * @param cpu_num_threads 推論に用いるスレッド数を設定する。0の場合論理コア数の半分か、物理コア数が設定される
         * @return 成功したらtrue、失敗したらfalse
         */
        boolean initialize(final String root_dir_path, boolean use_gpu, int cpu_num_threads/* =0*/);

        /**
         * 終了処理を行う
         * <p>
         * 終了処理を行う。以降関数を利用するためには再度初期化を行う必要がある。
         * 何度も実行可能。実行せずにexitしても大抵の場合問題ないが、
         * CUDAを利用している場合これを実行しておかないと例外が起こることがある。
         * </p>
         */
        void finalize();

        /**
         * メタ情報を取得する
         * <p>
         * 話者名や話者IDのリストを取得する
         * </p>
         * @return メタ情報が格納されたjson形式の文字列
         */
        String metas();

        /**
         * 対応デバイス情報を取得する
         * <p>
         * cpu, cudaのうち、使用可能なデバイス情報を取得する
         * </p>
         * @return 各デバイスが使用可能かどうかをboolで格納したjson形式の文字列
         */
        String supported_devices();

        /**
         * 音素ごとの長さを求める
         * <p>
         * 音素列から、音素ごとの長さを求める
         * </p>
         * @param length 音素列の長さ
         * @param phoneme_list 音素列
         * @param speaker_id 話者番号
         * @return 音素ごとの長さ
         */
        boolean yukarin_s_forward(long length, long[] phoneme_list, long[] speaker_id, float[] output);

        /**
         * モーラごとの音高を求める
         * <p>
         * モーラごとの音素列とアクセント情報から、モーラごとの音高を求める
         * </p>
         * @param length モーラ列の長さ
         * @param vowel_phoneme_list 母音の音素列
         * @param consonant_phoneme_list 子音の音素列
         * @param start_accent_list アクセントの開始位置
         * @param end_accent_list アクセントの終了位置
         * @param start_accent_phrase_list アクセント句の開始位置
         * @param end_accent_phrase_list アクセント句の終了位置
         * @param speaker_id 話者番号
         * @return モーラごとの音高
         */
        boolean yukarin_sa_forward(long length, long[] vowel_phoneme_list,
                                   long[] consonant_phoneme_list, long[] start_accent_list,
                                   long[] end_accent_list, long[] start_accent_phrase_list,
                                   long[] end_accent_phrase_list, long[] speaker_id,
                                   float[] output);

        /**
         * 波形を求める
         * <p>
         * フレームごとの音素と音高から、波形を求める
         * </p>
         * @param length フレームの長さ
         * @param phoneme_size 音素の種類数
         * @param f0 フレームごとの音高
         * @param phoneme フレームごとの音素
         * @param speaker_id 話者番号
         * @return 音声波形
         */
        boolean decode_forward(long length, long phoneme_size, float[] f0, float[] phoneme,
                               long[] speaker_id, float[] output);

        /**
         * 最後に発生したエラーのメッセージを取得する
         * @return エラーメッセージ
         */
        String last_error_message();
    }
}