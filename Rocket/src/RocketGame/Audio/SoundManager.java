package RocketGame.Audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private final Map<String, Clip> soundClips;
    private boolean muted;
    private float volume;

    private SoundManager() {
        soundClips = new HashMap<>();
        muted = false;
        volume = 0.5f;
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // Load sound file
    public void loadSound(String name, String filepath) {
        try {
            File soundFile = new File(filepath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            soundClips.put(name, clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound: " + name);
            e.printStackTrace();
        }
    }

    // Play sound
    public void playSound(String name) {
        if (muted) return;

        Clip clip = soundClips.get(name);
        if (clip != null) {
            clip.setFramePosition(0); // Rewind to beginning
            clip.start();
        }
    }

    // Play procedural sound (simple beep)
    public void playTone(float frequency, int duration) {
        if (muted) return;

        new Thread(() -> {
            try {
                byte[] buffer = new byte[duration * 8000 / 1000];
                for (int i = 0; i < buffer.length; i++) {
                    double angle = 2.0 * Math.PI * i / (8000.0 / frequency);
                    buffer[i] = (byte) (Math.sin(angle) * 127.0 * volume);
                }

                AudioFormat format = new AudioFormat(8000, 8, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Sound effects shortcuts
    public void playShoot() {
        playTone(800, 100);
    }

    public void playHit() {
        playTone(200, 200);
    }

    public void playExplosion() {
        playTone(100, 500);
    }

    public void playPowerup() {
        playTone(600, 300);
    }

    public void playDamage() {
        playTone(150, 300);
    }

    // Controls
    public void toggleMute() {
        muted = !muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    // Cleanup
    public void cleanup() {
        for (Clip clip : soundClips.values()) {
            clip.close();
        }
        soundClips.clear();
    }
}