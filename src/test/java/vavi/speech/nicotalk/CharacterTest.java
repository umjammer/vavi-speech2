package vavi.speech.nicotalk;

import javax.swing.JFrame;

import vavi.speech.nicotalk.CharacterFrame;


public class CharacterTest {

    public static void main(String[] args) throws Exception {
        CharacterFrame frame = new CharacterFrame("神威式魔理沙");
        frame.setLocation(1400, 800);
        frame.setVisible(true);
        frame.setFloating(true);
    }
}
