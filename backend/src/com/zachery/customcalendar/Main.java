package com.zachery.customcalendar;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDateTime;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting...");
        System.out.flush();

        // Spark Server's Port
        port(4567);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                (com.google.gson.JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                    new com.google.gson.JsonPrimitive(src.toString()))
            .create();

        DateAlarm dateAlarm;
        try {
            dateAlarm = new DateAlarm();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, DELETE");
            res.header("Access-Control-Allow-Headers", "Content-Type");
        });

        options("/*", (req, res) -> "OK");

        get("/api/alarms", (req, res) -> {
            res.type("application/json");
            try {
                return gson.toJson(dateAlarm.alarmDataList);
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        });

        post("/api/alarms", (req, res) -> {
            res.type("application/json");
            try {
                AlarmRequest body = gson.fromJson(req.body(), AlarmRequest.class);
                dateAlarm.setAlarm(
                    LocalDateTime.parse(body.time),
                    body.title,
                    body.desc
                );
                return gson.toJson(new MessageResponse("Alarm set!"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(new MessageResponse("Failed: " + e.getMessage()));
            }
        });

        delete("/api/alarms", (req, res) -> {
            res.type("application/json");
            try {
                AlarmRequest body = gson.fromJson(req.body(), AlarmRequest.class);
                dateAlarm.removeAlarm(java.time.LocalDateTime.parse(body.time));
                return gson.toJson(new MessageResponse("Alarm removed!"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(new MessageResponse("Failed: " + e.getMessage()));
            }
        });

        dateAlarm.checkAlarm();

        System.out.println("Java backend running on http://localhost:4567");
        System.out.flush();
    }

    static class AlarmRequest
    {
        String time, title, desc;
    }

    static class MessageResponse
    {
        String message;
        MessageResponse(String msg) { this.message = msg; }
    }
}