package com.zachery.customcalendar;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

public class DateAlarm
{
    PriorityQueue<AlarmRecord> alarmDataQueue = new PriorityQueue<>();
    List<AlarmRecord> alarmDataList = new ArrayList<>();

    private Thread alarmThread = null;
    private AlarmSounds alarmSounds;

    public DateAlarm(AlarmSounds alarmSounds) throws IOException
    {
        this.alarmSounds = alarmSounds;

        try (Scanner scan = getAllNotificationData())
        {
            while (scan.hasNextLine())
            {
                String line = scan.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|&\\^");
                if (parts.length < 2) continue;

                LocalDateTime alarmTime = LocalDateTime.parse(parts[0]);

                String title = parts[1].replace("<NL>", "\n");
                String desc  = (parts.length > 2 ? parts[2] : "").replace("<aNL>", "\n");

                AlarmRecord alarm = new AlarmRecord(alarmTime, title, desc);

                alarmDataList.add(alarm);

                if (alarmTime.isAfter(LocalDateTime.now()))
                    alarmDataQueue.add(alarm);
            }
        }
        checkAlarm();
    }

    public void setAlarm(LocalDateTime time, String title, String desc) throws IOException
    {
        String safeTitle = title.replace("\n", "<NL>");
        String safeDesc  = desc.replace("\n", "<aNL>");

        AlarmRecord alarm = new AlarmRecord(time, title, desc);

        alarmDataQueue.add(alarm);
        alarmDataList.add(alarm);

        try (FileWriter fw = new FileWriter(SystemDirectory.ObtainFile("Events/Events.txt"), true);
             PrintWriter pw = new PrintWriter(fw))
        {
            pw.println(time + "|&^" + safeTitle + "|&^" + safeDesc);
        }

        checkAlarm();
    }

    public void removeAlarm(LocalDateTime time) throws IOException
    {
        alarmDataQueue.removeIf(a -> a.time().equals(time));
        alarmDataList.removeIf(a -> a.time().equals(time));

        rewriteEventsFile();
    }

    public void updateAlarm(String id, LocalDateTime newTime, String newTitle, String newDesc) throws IOException
    {
        LocalDateTime oldTime = LocalDateTime.parse(id);

        alarmDataQueue.removeIf(a -> a.time().equals(oldTime));
        alarmDataList.removeIf(a -> a.time().equals(oldTime));

        rewriteEventsFile();

        setAlarm(newTime, newTitle, newDesc);
    }

    public void checkAlarm()
    {
        if (alarmThread != null && alarmThread.isAlive())
        {
            alarmThread.interrupt();
            try {
                alarmThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        alarmThread = new Thread(() ->
        {
            System.out.println("=== THREAD STARTED, queue size: " + alarmDataQueue.size());

            while (!Thread.currentThread().isInterrupted() && !alarmDataQueue.isEmpty())
            {
                AlarmRecord nextAlarm = alarmDataQueue.peek();
                if (nextAlarm == null) break;

                long delayMs = Duration.between(LocalDateTime.now(), nextAlarm.time()).toMillis();
                System.out.println("=== Waiting " + delayMs + "ms for: " + nextAlarm.title());

                try
                {
                    if (delayMs > 0)
                        Thread.sleep(delayMs);

                    System.out.println("=== SLEEP COMPLETE for: " + nextAlarm.title());

                    if (Thread.currentThread().isInterrupted())
                        return;

                    AlarmRecord cannon = alarmDataQueue.poll();
                    if (cannon == null) continue;

                    System.out.println("=== ALARM FIRING: " + cannon.title());

                    String selected = alarmSounds.getSelectedSound();
                    System.out.println("=== Selected sound: " + selected);

                    AlarmActivation activation = new AlarmActivation(cannon.title(), cannon.desc());

                    Main.alarmFiring.set(true);
                    Main.firingTitle.set(cannon.title());
                    Main.firingDesc.set(cannon.desc());

                    System.out.println("=== Calling displayTray...");
                    activation.displayTray();

                    System.out.println("=== Calling playSound...");
                    String soundName = (selected != null && !selected.isEmpty()) ? selected : "chime";
                    alarmSounds.playSound(soundName);

                    System.out.println("=== Done.");
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    return;
                }
                catch (Exception e)
                {
                    System.err.println("Error firing alarm: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("=== THREAD EXITING, queue size: " + alarmDataQueue.size());
        });

        alarmThread.setDaemon(true);
        alarmThread.start();
    }

    private void rewriteEventsFile() throws IOException
    {
        java.io.File file = SystemDirectory.ObtainFile("Events/Events.txt");

        try (PrintWriter pw = new PrintWriter(file))
        {
            for (AlarmRecord alarm : alarmDataList)
            {
                String safeTitle = alarm.title().replace("\n", "<NL>");
                String safeDesc  = alarm.desc().replace("\n", "<aNL>");
                pw.println(alarm.time() + "|&^" + safeTitle + "|&^" + safeDesc);
            }
        }
    }

    private Scanner getAllNotificationData() throws IOException
    {
        java.io.File file = SystemDirectory.ObtainFile("Events/Events.txt");
        if (!file.exists())
        {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return new Scanner(file);
    }
}