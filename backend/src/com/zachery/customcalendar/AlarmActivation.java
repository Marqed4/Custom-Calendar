package com.zachery.customcalendar;

import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.awt.*;

import javax.print.attribute.standard.Media;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AlarmActivation
{
    private static final int BUFFER_SIZE = 4096;
    String title;
    String desc;

    public AlarmActivation(String title, String desc) throws AWTException
    {
        this.title = title;
        this.desc = desc;
    }

    public AlarmActivation displayTray() throws AWTException 
    {
        SystemTray tray = SystemTray.getSystemTray();

        File notificationPNG = SystemDirectory.Directory("resources/assets/images/notification.png");
        Image notificationImg = Toolkit.getDefaultToolkit().createImage(notificationPNG.getAbsolutePath());

        TrayIcon trayIcon = new TrayIcon(notificationImg, "Calendar Notification");

        trayIcon.setImageAutoSize(true);

        trayIcon.setToolTip("");
        tray.add(trayIcon);

        trayIcon.displayMessage(this.title, this.desc, MessageType.INFO);

        return this;
    }

    public AlarmActivation playSound() 
    {
        try 
        {
            
            File sound = SystemDirectory.Directory("Assets/notification.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(sound);
            
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } 
        catch (Exception e) 
        {
            System.err.println("Missing Notification Audio");
        }
        
        return this;
    }
}