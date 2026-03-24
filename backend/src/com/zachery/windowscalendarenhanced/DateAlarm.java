package com.zachery.windowscalendarenhanced;

import java.time.LocalDateTime;
import java.util.PriorityQueue;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.time.Duration;
import java.util.Scanner;
import java.util.Objects;
import java.util.List;
import java.awt.*;

/*
todo
Implement SQL Lite in app database for alarms instead of records by text file
Possibly have a List<Map<LocalDateTime, Map<String, String>>> Yea... nightmare fuel.
*/

public class DateAlarm
{
    //List of lists includes LocalDateTime(s) & notification information
    PriorityQueue<AlarmRecord> alarmDataQueue = new PriorityQueue<>();

    //Creates a DataAlarm Object
    //Notification String data format: LocalDateTime|&^Title|&^Description (Key: |&^ = Seperation)
    public DateAlarm() throws IOException 
    {
        Scanner scan = getAllNotificationData();

        while (scan.hasNextLine()) 
        {
            String line = scan.nextLine();
            String[] parts = line.split("\\|&\\^");

            AlarmRecord alarm = new AlarmRecord(
                LocalDateTime.parse(parts[0]),
                parts[1],
                parts[2]
            );

            alarmDataQueue.add(alarm);
        }

        // Remove all of the expired alarms
        while (!alarmDataQueue.isEmpty()) {
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

        //Add alarm data to current Queue
        alarmDataQueue.add(new AlarmRecord(time, title, desc));

        //Add alarm data to notififications.txt
        try (FileWriter fw = new FileWriter("notifications/notifications.txt", true);
        PrintWriter pw = new PrintWriter(fw)) 
        {
        pw.print(time + "|&^");
        pw.print(title + "|&^");
        pw.print(desc + "\n");
        }
    }

    public void removeAlarm(LocalDateTime time) 
    {
        //todo
        //remove the alarm info from the notifications.txt
        alarmDataQueue.removeIf(now -> now.time().equals(time));
;
    }

    //I learned somewhat about threads dealing with memory allocation via malloc! Can it come in handy here?
    //checkAlarm should be called when the application is started. From there it remains a background process.
    public void checkAlarm() throws InterruptedException 
    {
    Thread thread = new Thread(() -> 
    {
        while (!alarmDataQueue.isEmpty()) 
        {
            AlarmRecord nextAlarm = alarmDataQueue.peek();
            if (nextAlarm == null) break;

            long delay = Duration.between(LocalDateTime.now(), nextAlarm.time()).getSeconds();

            try {
                if (delay > 0) {
                    Thread.sleep(delay * 1000);
                }

                //Fire the CANNONS
                AlarmRecord CANNON = alarmDataQueue.poll();
                new AlarmActivation
                (
                    CANNON.title(),
                    CANNON.desc()
                )
                
                .displayTray()
                .playSound();
            } 
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } 
            catch (AWTException e) {
                throw new RuntimeException(e);
            }
        }
    });

        thread.start();
    }
    

    //Helper Functions
    private Scanner getAllNotificationData() throws IOException  
    {
        return new Scanner(SystemDirectory.ObtainFile("notifications/notifications.txt"));
    }

    //Legacy Helpers

    // private PrintWriter getPrintWriter() throws IOException  
    // {
    //     return new PrintWriter(SystemDirectory.ObtainFile("notifications/notifications.txt"));
    // }

    /* <---- ALARM DATA ----> */
    // private String getAlarmTime(String line)
    // {
    //     int div1 = line.indexOf("|&^");
    //     return line.substring(0, div1);
    // }

    // private String getTitle(String line)
    // {
    //     int div1 = line.indexOf("|&^");
    //     int div2 = line.indexOf("|&^", div1 + 3);
    //     return line.substring(div1 + 3, div2);
    // }

    // private String getDesc(String line)
    // {
    //     int div1 = line.indexOf("|&^");
    //     int div2 = line.indexOf("|&^", div1 + 3);
    //     return line.substring(div2 + 3);
    // }
}

//Notification Examples (Ignore "No. ")
//1. 2026-20-02T17:05:00|&^Homework|&^Essentials of Software Engineering Chapter 6 Reading
//2. 2026-24-03T17:05:00|&^Homework|&^Essentials of Software Engineering Chapter 12 Reading
//2. 2026-26-03T17:05:00|&^Homework|&^CISC 3810/7510: Database Systems: Database Design
