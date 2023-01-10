package org.tetris;

import javax.media.*;
import java.io.IOException;

public class MusicPlayer {
    private static Player player;
    private static final MediaLocator mediaLocator = new MediaLocator("file:./resource/korobeiniki.wav");
    public static void play() {
        createPlayer();
        while (true) {
            if (player.getMediaTime().getSeconds() >= player.getDuration().getSeconds()) {
                createPlayer();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static void createPlayer() {
        try {
            player = Manager.createRealizedPlayer(mediaLocator);
        } catch (IOException | NoPlayerException | CannotRealizeException e) {
            throw new RuntimeException(e);
        }
        GainControl volumeControl = player.getGainControl();
        // Set the volume to quarter the maximum value
        float maxVolume = volumeControl.getLevel();
        volumeControl.setLevel(maxVolume / 4);
        player.start();
    }
}
