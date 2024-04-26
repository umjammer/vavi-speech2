[![Release](https://jitpack.io/v/umjammer/vavi-speech2.svg)](https://jitpack.io/#umjammer/vavi-speech2)
[![Java CI](https://github.com/umjammer/vavi-speech2/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-speech2/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-speech2/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-speech2/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)

# vavi-speech2

<img alt="yukkuries" src="https://github.com/umjammer/vavi-speech2/assets/493908/5ccc63da-5dc8-40ac-b6f6-d8dce89b7cf7" width="300" />

Text to Speech and Speech to Text (JSAPI2) engines for Java

| **Type**                    | **Description**                                                                                                                                                                                   | **Sythesizer** | **Recognizer** | **Quality** | **Comment** |
|:----------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------:|:--------------:|:-----------:|:------------|
| AquesTalk10                 | [AquesTalk](https://www.a-quest.com/products/aquestalk.html), JNA                                                                                                                                 | ✅ |  - | 😐 | ゆっくり        |
| Google Cloud Text To Speech | [Google Cloud Text To Speech](https://cloud.google.com/text-to-speech/docs/quickstart-client-libraries), Library                                                                  | ✅ | 🚧 | 👑 |             |
| Cocoa                       | [Rococoa](https://github.com/iterate-ch/rococoa/blob/d5fdd3b884d5f044bc0b168aff66e5f52a014da8/rococoa/rococoa-contrib/src/test/java/org/rococoa/contrib/appkit/NSSpeechSynthesizerTest.java), JNA | ✅ | 🚫 | 😃 |             |
| Open JTalk                  | [jtalkdll](https://github.com/rosmarinus/jtalkdll), JNA                                                                                          | ✅ | - | 💩 |             |
| VoiceVox                    | [VOICEVOX](https://voicevox.hiroshiba.jp/), REST                                                                                                                                                  | ✅ | - | 😃 | ずんだもん       |
| CoeiroInk                   | [CoeiroInk](https://coeiroink.com/), REST                                                                                                                                                  | ✅ | - | 😃 | つくよみちゃん       |
| Gyutan (Open JTalk in Java) | [Gyutan](https://github.com/umjammer/Gyutan), Library                                                                                          | ✅ | - | 💩 |             |

## Install

### maven

 * https://jitpack.io/#umjammer/vavi-speech2

### AquesTalk10

 * place `AquesTalk10.framework` into `~/Library/Frameworks`
 * create symbolic link `AquesTalk10.framework/AquesTalk` as `AquesTalk10.framework/AquesTalk10`
 * write `aquesTalk10DevKey` into `local.properties`

### Google Cloud Text To Speech

 * [get token as json](https://cloud.google.com/text-to-speech/docs/quickstart-client-libraries)
 * set environment variable `"GOOGLE_APPLICATION_CREDENTIALS"` `your_json_path`

### Open JTalk

 * make `libjtalk.dylib` from https://github.com/rosmarinus/jtalkdll
 * locate `libjtalk.dylib` into java classpath or `jna.library.path` system property

### VOICEVOX

 * [download](https://voicevox.hiroshiba.jp/) the application
 * run the application before using this library

### COEIROINK

* [download](https://coeiroink.com/) the application
* run the application before using this library

### DoCoMo AI Agent API (wip)

 * https://agentcraft.sebastien.ai/

## Usage

### user

  * [zundamod](https://github.com/umjammer/zundamod)
  * [w/ chatGPT](https://github.com/umjammer/vavi-speech-sandbox/)
  * [RPC](https://github.com/umjammer/vavi-speech-rpc/)

## Reference

 * [jsr113](https://github.com/JVoiceXML/jsapi)
   * [vavi patched](https://github.com/umjammer/jsapi) (volume enabled)

## TODO

 * speech.properties
 * engine
   * [watson](https://www.ibm.com/watson/jp-ja/developercloud/text-to-speech.html)
   * ~~[open jtalk](http://open-jtalk.sourceforge.net/)~~
     * ~~https://github.com/icn-lab/Gyutan~~ (done)
   * [festival](https://github.com/festvox/festival)
   * amazon polly
   * microsoft cognitive services text to speech
   * ~~https://github.com/julius-speech/julius~~ -> Gyutan
   * ~~VoiceVox~~
     * ~~search レキシカ voice and parameter~~ (wip)
       * vavi.speech.voicevox.VoiceVoxTest#test5
       * RekishikaTest
   * https://github.com/espeak-ng/espeak-ng
   * https://github.com/festvox/flite
 * text analytics + nicotalk character emotion (nicotalk branch)
   * wave lipsync
     * https://github.com/hecomi/MMD4Mecanim-LipSync-Plugin/blob/master/Assets/LipSync/Core/LipSyncCore.cs
 * VoiceVox editor compatible
   * ~~[CoeiroInk](https://coeiroink.com/)~~ ... ~~api doesn't work~~ ~~api is different from VoiceVox?~~ yes
     * https://github.com/sevenc-nanashi/coeiroink-v2-bridge 🎯
     * ~~https://github.com/sinsen9000/MultiSpeech~~ api is old
   * [LMROID](https://lmroidsoftware.wixsite.com/nhoshio)
   * [SHAREVOX](https://www.sharevox.app)
   * [http://itvoice.starfree.jp/](http://itvoice.starfree.jp/)
 * AVSpeechSynthesizer needs [obj-c block](https://github.com/umjammer/rococoa/discussions/23)
 * ~~rcp client/server (wip)~~ -> [vavi-speech-rpc](https://github.com/umjammer/vavi-speech-rpc)

---
<sub>images by <a href="https://commons.nicovideo.jp/works/nc327182">霊夢</a>, <a href="https://commons.nicovideo.jp/works/nc327184">魔理沙</a>, <a href="https://seiga.nicovideo.jp/seiga/im10865385">ずんだもん</a></sub>