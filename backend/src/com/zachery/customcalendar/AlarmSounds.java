package com.zachery.customcalendar;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

public class AlarmSounds
{

    private static final String SELECTED_SOUND_FILE = "AlarmSounds/SelectedSound.txt";
    private static final String VOLUME_FILE = "AlarmSounds/Volume.txt";

    private List<String> entries = new ArrayList<>();
    private Thread currentPlayback;
    private Clip currentClip;
    private SourceDataLine currentLine;
    private String selectedSound = null;
    private float volume = 1.0f;

    public AlarmSounds()
    {
        try {
            loadSounds();
            loadSelectedSound();
            loadVolume();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSounds() throws IOException
    {
        java.io.File file = SystemDirectory.Directory("AlarmSounds/AlarmSounds.txt");

        if (!file.exists() || file.length() == 0)
            return;

        entries.clear();

        try (Scanner scanner = new Scanner(file))
        {
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty())
                    entries.add(line);
            }
        }

        Collections.sort(entries);
    }

    private void loadSelectedSound() throws IOException
    {
        java.io.File file = SystemDirectory.Directory(SELECTED_SOUND_FILE);

        if (!file.exists() || file.length() == 0) return;

        try (Scanner scanner = new Scanner(file))
        {
            if (scanner.hasNextLine())
            {
                String line = scanner.nextLine().trim();
                selectedSound = line.isEmpty() ? null : line;
            }
        }
    }

    private void loadVolume() throws IOException
    {
        java.io.File file = SystemDirectory.Directory(VOLUME_FILE);

        if (!file.exists() || file.length() == 0) return;

        try (Scanner scanner = new Scanner(file))
        {
            if (scanner.hasNextLine())
            {
                try {
                    float val = Float.parseFloat(scanner.nextLine().trim());
                    volume = Math.max(0f, Math.min(1f, val));
                } catch (NumberFormatException e) {
                    volume = 1.0f;
                }
            }
        }
    }

    public void addSound(String sourcePath) throws IOException
    {
        java.io.File sourceFile = new java.io.File(sourcePath);

        if (!sourceFile.exists())
            throw new IOException("Source file does not exist: " + sourcePath);

        String fileName = sourceFile.getName();
        String ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();

        if (!ext.equals(".mp3") && !ext.equals(".wav"))
            throw new IOException("Unsupported file type: " + ext);

        String displayName = fileName.substring(0, fileName.lastIndexOf('.'));

        java.io.File destFile = SystemDirectory.ObtainFile("AlarmSounds/Uploads/" + fileName);
        destFile.getParentFile().mkdirs();

        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String entry = displayName + "|&" + destFile.getAbsolutePath();

        if (entries.contains(entry))
            return;

        entries.add(entry);
        Collections.sort(entries);

        try (FileWriter fw = new FileWriter(SystemDirectory.ObtainFile("AlarmSounds/AlarmSounds.txt"), true);
             PrintWriter pw = new PrintWriter(fw))
        {
            pw.println(entry);
        }
    }

    public void playSound(String name)
    {
        if (name == null || name.equals("chime") || name.isEmpty())
        {
            playSoundFile(null);
            return;
        }

        String filePath = null;

        for (String entry : entries)
        {
            String[] parts = entry.split("\\|&", 2);
            if (parts.length == 2 && parts[0].equals(name))
            {
                filePath = parts[1];
                break;
            }
        }

        if (filePath == null)
        {
            System.out.println("Sound not found: " + name);
            return;
        }

        playSoundFile(filePath);
    }

    private void playSoundFile(String filePath)
    {
        stopSound();

        try
        {
            java.io.File soundFile = (filePath != null && !filePath.isEmpty())
                ? new java.io.File(filePath)
                : SystemDirectory.Directory("resources/assets/sounds/Default Chime.mp3");

            if (!soundFile.exists())
            {
                System.err.println("Sound file not found: " + soundFile.getAbsolutePath());
                return;
            }

            String name = soundFile.getName().toLowerCase();

            if (name.endsWith(".mp3"))
            {
                java.io.File ref = soundFile;
                currentPlayback = new Thread(() -> playMp3(ref));
            }
            else if (name.endsWith(".wav"))
            {
                java.io.File ref = soundFile;
                currentPlayback = new Thread(() -> playWav(ref));
            }
            else
            {
                System.err.println("Unsupported format: " + name);
                return;
            }

            currentPlayback.setDaemon(true);
            currentPlayback.start();
        }
        catch (Exception e)
        {
            System.err.println("Failed to play sound: " + e.getMessage());
        }
    }

    private void playMp3(java.io.File file)
    {
        try (FileInputStream fis = new FileInputStream(file))
        {
            Bitstream bitstream = new Bitstream(fis);
            Decoder decoder = new Decoder();
            SourceDataLine line = null;

            try
            {
                while (!Thread.currentThread().isInterrupted())
                {
                    Header header = bitstream.readFrame();
                    if (header == null) break;

                    SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);

                    if (line == null)
                    {
                        AudioFormat format = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            output.getSampleFrequency(),
                            16,
                            output.getChannelCount(),
                            output.getChannelCount() * 2,
                            output.getSampleFrequency(),
                            false
                        );
                        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                        line = (SourceDataLine) AudioSystem.getLine(info);
                        line.open(format);
                        synchronized (this) {
                            currentLine = line;
                        }
                        applyVolumeToLine(line, getVolume());
                        line.start();
                    }

                    short[] samples = output.getBuffer();
                    int length = output.getBufferLength();
                    byte[] bytes = new byte[length * 2];

                    for (int i = 0; i < length; i++)
                    {
                        bytes[i * 2]     = (byte) (samples[i] & 0xFF);
                        bytes[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xFF);
                    }

                    line.write(bytes, 0, bytes.length);
                    bitstream.closeFrame();
                }

                if (line != null) line.drain();
            }
            finally
            {
                synchronized (this) {
                    if (currentLine != null) {
                        currentLine.stop();
                        currentLine.close();
                        currentLine = null;
                    }
                }
                try { bitstream.close(); } catch (Exception ignored) {}
            }
        }
        catch (Exception e)
        {
            if (!Thread.currentThread().isInterrupted())
                System.err.println("MP3 playback error: " + e.getMessage());
        }
    }

    private void playWav(java.io.File file)
    {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(file))
        {
            synchronized (this) {
                currentClip = AudioSystem.getClip();
            }
            currentClip.open(ais);
            applyVolumeToClip(currentClip, getVolume());
            currentClip.start();

            long durationMs = currentClip.getMicrosecondLength() / 1000;
            Thread.sleep(durationMs);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        catch (Exception e)
        {
            if (!Thread.currentThread().isInterrupted())
                System.err.println("WAV playback error: " + e.getMessage());
        }
        finally
        {
            synchronized (this) {
                if (currentClip != null) {
                    currentClip.stop();
                    currentClip.close();
                    currentClip = null;
                }
            }
        }
    }

    private float toDecibels(float vol)
    {
        if (vol <= 0f) return -80f;
        float curved = vol * vol;
        return 20f * (float) Math.log10(curved);
    }

    private void applyVolumeToClip(Clip clip, float vol)
    {
        try
        {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = toDecibels(vol);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
        }
        catch (Exception e)
        {
            System.err.println("Volume control not supported: " + e.getMessage());
        }
    }

    private void applyVolumeToLine(SourceDataLine line, float vol)
    {
        try
        {
            FloatControl gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = toDecibels(vol);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
        }
        catch (Exception e)
        {
            System.err.println("Volume control not supported: " + e.getMessage());
        }
    }

    public void stopSound()
    {
        synchronized (this) {
            if (currentClip != null) {
                currentClip.stop();
                currentClip.close();
                currentClip = null;
            }
            if (currentLine != null) {
                currentLine.stop();
                currentLine.close();
                currentLine = null;
            }
        }
        if (currentPlayback != null && currentPlayback.isAlive())
        {
            currentPlayback.interrupt();
            currentPlayback = null;
        }
    }

    public void removeSound(String name) throws IOException
    {
        String entryToRemove = null;

        for (String entry : entries)
        {
            String[] parts = entry.split("\\|&", 2);
            if (parts.length == 2 && parts[0].equals(name))
            {
                entryToRemove = entry;
                break;
            }
        }

        if (entryToRemove == null)
        {
            System.out.println("Sound not found: " + name);
            return;
        }

        String filePath = entryToRemove.split("\\|&", 2)[1];
        java.io.File uploadedFile = new java.io.File(filePath);

        if (uploadedFile.exists())
            uploadedFile.delete();

        entries.remove(entryToRemove);
        rewriteSoundsFile();
    }

    private void rewriteSoundsFile() throws IOException
    {
        java.io.File file = SystemDirectory.ObtainFile("AlarmSounds/AlarmSounds.txt");

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, false)))
        {
            for (String entry : entries)
                pw.println(entry);
        }
    }

    public void setVolume(float vol) throws IOException
    {
        volume = Math.max(0f, Math.min(1f, vol));

        synchronized (this) {
            if (currentClip != null && currentClip.isOpen())
                applyVolumeToClip(currentClip, volume);
            if (currentLine != null && currentLine.isOpen())
                applyVolumeToLine(currentLine, volume);
        }

        java.io.File file = SystemDirectory.ObtainFile(VOLUME_FILE);
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, false)))
        {
            pw.println(volume);
        }
    }

    public float getVolume()
    {
        return volume;
    }

    public List<String> getAllSounds()
    {
        return Collections.unmodifiableList(entries);
    }

    public static String getDisplayName(String entry)
    {
        return entry.split("\\|&", 2)[0];
    }

    public static String getFilePath(String entry)
    {
        String[] parts = entry.split("\\|&", 2);
        return parts.length == 2 ? parts[1] : "";
    }

    public void selectSound(String name) throws IOException
    {
        selectedSound = (name == null || name.isEmpty()) ? null : name;

        java.io.File file = SystemDirectory.ObtainFile(SELECTED_SOUND_FILE);
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, false)))
        {
            pw.println(selectedSound == null ? "" : selectedSound);
        }
    }

    public String getSelectedSound()
    {
        return selectedSound;
    }
}