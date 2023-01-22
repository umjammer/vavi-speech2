[![GitHub Packages](https://github.com/umjammer/vavi-speech2/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/umjammer?tab=packages&repo_name=vavi-speech)
[![Java CI with Maven](https://github.com/umjammer/vavi-speech2/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-speech2/actions)
[![CodeQL](https://github.com/umjammer/vavi-speech2/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-speech2/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-8-b07219)

# vavi-speech2

Text to Speech and Speech to Text (JSAPI2) for Java

| **Type**                    | **Description**                                                                                                                                                                                   | **Sythesizer** | **Recognizer** | **Quality** | **Comment** |
|:----------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------:|:--------------:|:-----------:|:------------|
| AquesTalk10                 | [AquesTalk](https://www.a-quest.com/products/aquestalk.html), JNA                                                                                                                                 | ✅ |  - | 😐 | ゆっくり        |
| Google Cloud Text To Speech | [Google Cloud Text To Speech](https://cloud.google.com/text-to-speech/docs/quickstart-client-libraries), Library                                                                  | ✅ | 🚧 | 👑 |             |
| Cocoa                       | [Rococoa](https://github.com/iterate-ch/rococoa/blob/d5fdd3b884d5f044bc0b168aff66e5f52a014da8/rococoa/rococoa-contrib/src/test/java/org/rococoa/contrib/appkit/NSSpeechSynthesizerTest.java), JNA | ✅ | 🚫 | 😃 |             |
| Open JTalk                  | [jtalkdll](https://github.com/rosmarinus/jtalkdll), JNA                                                                                          | ✅ | - | 💩 |             |
| VoiceVox                    | [VOICEVOX](https://voicevox.hiroshiba.jp/), REST                                                                                                                                                  | ✅ | - | 😃 | ずんだもん       |
| Gyutan (Open JTalk in Java) | [Gyutan](https://github.com/umjammer/Gyutan), Library                                                                                          | ✅ | - | 💩 |             |

## Install

### maven

 * https://github.com/umjammer/vavi-speech2/packages/1691523
 * this project uses gitlab package registry. add a personal access token to `~/.m2/settings.xml`
 * see https://docs.gitlab.com/ee/user/packages/maven_repository/index.html#authenticate-to-the-package-registry-with-maven

### AquesTalk10

 * place `AquesTalk10.framework` into `~/Library/Frameworks`
 * create symbolic link `AquesTalk10.framework/AquesTalk` as `AquesTalk10.framework/AquesTalk10`
 * write `aquesTalk10DevKey` into `local.properties`

### Google Cloud Text To Speech

 * [get token as json](https://cloud.google.com/text-to-speech/docs/quickstart-client-libraries)
 * set environment variable `"GOOGLE_APPLICATION_CREDENTIALS"` `your_json_path`

### Rococoa

 * locate `librococoa.dylib` into one of class paths
   * if you use maven it's already done, you can find it at `target/test-classes`.

### Open JTalk

 * make `libjtalk.dylib` from `https://github.com/rosmarinus/jtalkdll`
 * locate `libjtalk.dylib` into `DYLD_LIBRARY_PATH`

### VOICEVOX

 * [download](https://voicevox.hiroshiba.jp/) the application
 * run the application before using this library

## Reference

 * [jsr113](https://github.com/JVoiceXML/jsapi)
   * [vavi patched](https://github.com/umjammer/jsapi) (volume enabled)

## TODO

 * [watson](https://www.ibm.com/watson/jp-ja/developercloud/text-to-speech.html)
 * ~~[open jtalk](http://open-jtalk.sourceforge.net/)~~
   * ~~https://github.com/icn-lab/Gyutan~~ (wip)
 * [festival](https://github.com/festvox/festival)
 * amazon polly
 * microsoft cognitive services text to speech
 * ~~https://github.com/julius-speech/julius~~ -> Gyutan
 * ~~VoiceVox~~
 * NicoTalk character
