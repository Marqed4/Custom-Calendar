package com.zachery.customcalendar;

import java.time.LocalDateTime;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.List;

public class DateAlarm
{
    PriorityQueue<AlarmRecord> alarmDataQueue = new PriorityQueue<>();
    List<AlarmRecord> alarmDataList = new ArrayList<>();

    private Thread alarmThread = null;

    public DateAlarm() throws IOException 
    {
        Scanner scan = getAllNotificationData();

        while (scan.hasNextLine()) 
        {
            String line = scan.nextLine();
            String[] parts = line.split("\\|&\\^");

            String title = parts[1].replace("<NL>", "\n");
            String desc  = (parts.length > 2 ? parts[2] : "").replace("<aNL>", "\n");

            AlarmRecord alarm = new AlarmRecord(
                LocalDateTime.parse(parts[0]),
                title,
                desc
            );

            alarmDataQueue.add(alarm);
        }

        alarmDataList = new ArrayList<>(alarmDataQueue);

        while (!alarmDataQueue.isEmpty()) 
        {
            AlarmRecord next = alarmDataQueue.peek();
            long delay = Duration.between(LocalDateTime.now(), next.time()).getSeconds();

            if (delay <= 0) {
                alarmDataQueue.poll();
            } else {
                break;
            }
        }
    }

    public void setAlarm(LocalDateTime time, String title, String desc) throws IOException 
    {
        String safeTitle = title.replace("\n", "<NL>");
        String safeDesc = desc.replace("\n", "<aNL>");

        AlarmRecord alarm = new AlarmRecord(time, title, desc);
        alarmDataQueue.add(alarm);
        alarmDataList.add(alarm);

        try (FileWriter fw = new FileWriter(SystemDirectory.ObtainFile("notifications/notifications.txt"), true);
        PrintWriter pw = new PrintWriter(fw)) 
        {
            pw.print(time + "|&^");
            pw.print(safeTitle + "|&^");
            pw.print(safeDesc + "\n");
        }

        checkAlarm();
    }

    public void removeAlarm(LocalDateTime time) throws IOException
    {
        alarmDataQueue.removeIf(a -> a.time().equals(time));
        alarmDataList.removeIf(a -> a.time().equals(time));

        java.io.File file = SystemDirectory.ObtainFile("notifications/notifications.txt");
        List<String> lines = new ArrayList<>();

        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                String[] parts = line.split("\\|&\\^");
                if (!LocalDateTime.parse(parts[0]).equals(time)) {
                    lines.add(line);
                }
            }
        }

        try (PrintWriter pw = new PrintWriter(file)) {
            for (String line : lines) {
                pw.println(line);
            }
        }
    }

    public void checkAlarm()
    {
        if (alarmThread != null && alarmThread.isAlive())
        {
            alarmThread.interrupt();
        }

        alarmThread = new Thread(() -> 
        {
            while (!alarmDataQueue.isEmpty()) 
            {
                AlarmRecord nextAlarm = alarmDataQueue.peek();
                if (nextAlarm == null) break;

                long delay = Duration.between(LocalDateTime.now(), nextAlarm.time()).getSeconds();

                try {
                    if (delay > 0) 
                    {
                        Thread.sleep(delay * 1000);
                    }

                    AlarmRecord CANNON = alarmDataQueue.poll();
                    new AlarmActivation
                    (
                        CANNON.title(),
                        CANNON.desc()
                    )
                    .displayTray()
                    .playSound();
                } 
                catch (InterruptedException e) 
                {
                    Thread.interrupted();
                    continue;
                } 
                catch (Exception e) 
                {
                    System.err.println("Error firing alarm: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        alarmThread.setDaemon(true);
        alarmThread.start();
    }

    private Scanner getAllNotificationData() throws IOException 
    {
        java.io.File file = SystemDirectory.ObtainFile("notifications/notifications.txt");
        if (!file.exists()) 
        {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return new Scanner(file);
    }
}