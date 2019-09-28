[![](https://jitpack.io/v/umjammer/vavi-speech2.svg)](https://jitpack.io/#umjammer/vavi-speech2)

# vavi-speech2

Text to Speech and Speech to Text (JSAPI2) for Java

| **Type** | **Description** | **Sythesizer** | **Recognizer** | **Quality** | **Comment** |
|:---------|:----------------|:---------:|:--------------:|:-----------:|:------------|
| AquesTalk10 | JNA | ✅ |  - | 😃 | |
| Google Cloud Text To Speech | [Google Cloud Text To Speech](https://cloud.google.com/text-to-speech/docs/quickstart-client-libraries) | ✅ | 🚧 | 👑 | |
| Rococoa | [Rococoa](https://github.com/iterate-ch/rococoa/blob/d5fdd3b884d5f044bc0b168aff66e5f52a014da8/rococoa/rococoa-contrib/src/test/java/org/rococoa/contrib/appkit/NSSpeechSynthesizerTest.java), JNA | ✅ | 🚫 | 😃 | |
| Open JTalk | [jtalkdll](https://github.com/rosmarinus/jtalkdll), JNA | ✅ | - | 💩 | |

## Reference

 * [jsr113](https://github.com/JVoiceXML/jsapi)
   * [vavi patched](https://github.com/umjammer/jsapi)

## TODO

 * [watson](https://www.ibm.com/watson/jp-ja/developercloud/text-to-speech.html)
 * [open jtalk](http://open-jtalk.sourceforge.net/)
   * https://github.com/icn-lab/Gyutan
 * [festival](https://github.com/festvox/festival)