package RocketGame.Audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private final Map<String, Clip> soundClips;
    private boolean muted;
    private float volume;
    private Clip currentBackgroundMusic;

    // Sound file directory
    private static final String SOUNDS_FOLDER = "Assets/Sounds/";

    private SoundManager() {
        soundClips = new HashMap<>();
        muted = false;
        volume = 0.5f;
        currentBackgroundMusic = null;

// Try to load sound files (optional)
        tryLoadSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // Try to load sound files (won't crash if files don't exist)
    private void tryLoadSounds() {
        try {
// Try to load sound files if they exist
            loadSoundIfExists("shoot", "shoot.wav");
            loadSoundIfExists("hit", "hit.wav");
            loadSoundIfExists("explosion", "explosion.wav");
            loadSoundIfExists("powerup", "powerup.wav");
            loadSoundIfExists("damage", "damage.wav");
            loadSoundIfExists("background", "background.wav");
            loadSoundIfExists("menu", "menu.wav");
            loadSoundIfExists("boss", "boss.wav");
        } catch (Exception e) {
// Silently fail - we'll use tones instead
            System.out.println("Sound files not found,");
        }
    }

    // Load sound only if file exists
    private void loadSoundIfExists(String name, String filename) {
        try {
            String filepath = SOUNDS_FOLDER + filename;
            File soundFile = new File(filepath);

            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                soundClips.put(name, clip);
                System.out.println("Loaded sound file: " + filename);
            }
        } catch (Exception e) {

        }
    }

    private boolean hasSoundFile(String name) {
        Clip clip = soundClips.get(name);
        return clip != null;
    }

    private void playSoundOrTone(String name, float frequency, int duration) {
        if (muted) return;

        if (hasSoundFile(name)) {
            Clip clip = soundClips.get(name);
            if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.setFramePosition(0);
                clip.start();
            }
        } else {

            playTone(frequency, duration);
        }
    }

    private void playTone(float frequency, int duration) {
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


// BACKGROUND MUSIC METHODS

    public void startBackgroundMusic(String musicName) {
        if (muted) return;

        stopBackgroundMusic();

        if (hasSoundFile(musicName)) {
            currentBackgroundMusic = soundClips.get(musicName);
            if (currentBackgroundMusic != null) {
                currentBackgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Started background music: " + musicName);
            }
        } else {
            System.out.println("Background music not found: " + musicName);
        }
    }

    public void stopBackgroundMusic() {
        if (currentBackgroundMusic != null && currentBackgroundMusic.isRunning()) {
            currentBackgroundMusic.stop();
            currentBackgroundMusic.setFramePosition(0); // Rewind
            currentBackgroundMusic = null;
        }
    }

    public void pauseBackgroundMusic() {
        if (currentBackgroundMusic != null && currentBackgroundMusic.isRunning()) {
            currentBackgroundMusic.stop();
        }
    }

    public void resumeBackgroundMusic() {
        if (currentBackgroundMusic != null && !currentBackgroundMusic.isRunning()) {
            currentBackgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void setBackgroundMusicVolume(float bgmVolume) {
        if (currentBackgroundMusic != null &&
                currentBackgroundMusic.isControlSupported(FloatControl.Type.MASTER_GAIN)) {

            FloatControl gainControl = (FloatControl) currentBackgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(bgmVolume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }

// SOUND EFFECTS METHODS

    public void playShoot() {
        playSoundOrTone("shoot", 800, 100);
    }

    public void playHit() {
        playSoundOrTone("hit", 200, 200);
    }

    public void playExplosion() {
        playSoundOrTone("explosion", 100, 500);
    }

    public void playPowerup() {
        playSoundOrTone("powerup", 600, 300);
    }

    public void playDamage() {
        playSoundOrTone("damage", 150, 300);
    }

// MUTE / VOLUME CONTROLS

    public void toggleMute() {
        muted = !muted;
        if (muted) {
            pauseBackgroundMusic();
        } else {
            resumeBackgroundMusic();
        }
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            pauseBackgroundMusic();
        } else {
            resumeBackgroundMusic();
        }
    }

    public boolean isMuted() {
        return muted;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public float getVolume() {
        return volume;
    }

// CLEANUP

    public void cleanup() {
        stopBackgroundMusic();
        for (Clip clip : soundClips.values()) {
            clip.close();
        }
        soundClips.clear();
    }

    public Clip getClip(String name) {
        return soundClips.get(name);
    }


}