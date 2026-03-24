package com.zachery.windowscalendarenhanced;

import java.io.File;

public class SystemDirectory 
{
    public static File Directory(String filePath) 
    {
    String os = System.getProperty("os.name").toLowerCase();
    String base;

    if (os.contains("win")) {
        base = System.getenv("APPDATA");
    } else if (os.contains("mac")) {
        base = System.getProperty("user.home") + "/Library/Application Support";
    } else {
        // Find Linux, Unix, etc. Main Directory
        base = System.getProperty("user.home") + "/.local/share";
    }

    File workingDirectory = new File(base, "WindowsCalendar");
    workingDirectory.mkdirs();
    return new File(workingDirectory, filePath);
    }

    protected static File ObtainFile(String requestedPath)
    {
        try 
        {
            File file = SystemDirectory.Directory(requestedPath);
            file.getParentFile().mkdirs();

            if (!file.exists())
            {
                file.createNewFile();
            }

            return file;
        } 
        catch (Exception e) 
        {
            throw new RuntimeException
            (
                "Failed to create file: " + requestedPath, e
            );
        }
    }
}