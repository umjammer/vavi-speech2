[![](https://jitpack.io/v/umjammer/vavi-speech2.svg)](https://jitpack.io/#umjammer/vavi-speech2)

# vavi-speech2

Text to Speech and Speech to Text (JSAPI2) for Java

| **Type** | **Description** | **Sythesizer** | **Recognizer** | **Quality** | **Comment** |
|:---------|:----------------|:---------:|:--------------:|:-----------:|:------------|
| AquesTalk10 | JNA | ✅ |  - | 🙂 | |
| Google Cloud Text To Speech | [Google Cloud Text To Speech](https://cloud.google.com/text-to-speech/docs/quickstart-client-libraries) | ✅ | 🚧 | 👑 | |
| Rococoa | [Rococoa](https://github.com/iterate-ch/rococoa/blob/d5fdd3b884d5f044bc0b168aff66e5f52a014da8/rococoa/rococoa-contrib/src/test/java/org/rococoa/contrib/appkit/NSSpeechSynthesizerTest.java), JNA | ✅ | 🚫 | 😃 | |
| Open JTalk | [jtalkdll](https://github.com/rosmarinus/jtalkdll), JNA | ✅ | - | 💩 | |

## Install

### AquesTalk

 * copy `AquesTalk10.framework/Versions/Current/AquesTalk` as `libaquestalk10.dylib` into `lib` directory
    * TODO it's better to locate the framework in `~/Library/Frameworks` ?
 * write `aquesTalk10DevKey` into `local.properties`
 * set jvmarg `"java.libraly.path"` `lib`

### Google Cloud Text To Speech

 * [get token as json](https://cloud.google.com/text-to-speech/docs/quickstart-client-libraries)
 * set environment variable `"GOOGLE_APPLICATION_CREDENTIALS"` `your_json_path`

### Rococoa

 * locate `librococoa.dylib` into `lib` directory
 * set jvm argument `"java.libraly.path"` `lib`

### Open JTalk

 * make `libjtalk.dylib` from `https://github.com/rosmarinus/jtalkdll`
 * locate `libjtalk.dylib` into `lib` directory
 * set jvm argument `"java.libraly.path"` `lib` (no need ???)


## Reference

 * [jsr113](https://github.com/JVoiceXML/jsapi)
   * [vavi patched](https://github.com/umjammer/jsapi)

## TODO

 * [watson](https://www.ibm.com/watson/jp-ja/developercloud/text-to-speech.html)
 * [open jtalk](http://open-jtalk.sourceforge.net/)
   * https://github.com/icn-lab/Gyutan
 * [festival](https://github.com/festvox/festival)
 * amazon polly
 * microsoft cognitive services text to speech
