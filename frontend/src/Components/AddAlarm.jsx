import { useState } from "react";
import { emit } from "@tauri-apps/api/event";
import { getCurrentWindow } from "@tauri-apps/api/window";
import WinterBackground from "../resources/assets/images/Winter Forest.gif";
import "./AddAlarm.css";

export default function AddAlarm() {
  const params = new URLSearchParams(window.location.search);
  const day = params.get("day");
  const month = params.get("month");
  const year = params.get("year");

  const [title, setTitle] = useState("");
  const [desc, setDesc] = useState("");
  const [time, setTime] = useState("");
  const [error, setError] = useState("");

  async function saveAlarm() {
    if (!title || !time) {
      setError("Title and time are required.");
      return;
    }

    const iso = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}T${time}:00`;

    try {
      await fetch("http://localhost:4567/api/alarms", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ time: iso, title, desc }),
      });

      await emit("alarm-saved");
      await getCurrentWindow().close();
    } catch (err) {
      setError("Failed to save alarm. Is the server running?");
    }
  }

  async function handleCancel() {
    await getCurrentWindow().close();
  }

  return (
    <>
      <div
        className="alarm-background"
        style={{ backgroundImage: `url(${WinterBackground})` }}
      />
      <div className="alarm-window">
        <h2>Add Alarm</h2>
        <p>{`${month}/${day}/${year}`}</p>

        {error && <p className="alarm-error">{error}</p>}

        <input
          type="text"
          placeholder="Title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />

        <textarea
          placeholder="Description"
          value={desc}
          onChange={(e) => setDesc(e.target.value)}
        />

        <input
          type="time"
          value={time}
          onChange={(e) => setTime(e.target.value)}
        />

        <div className="alarm-buttons">
          <button onClick={saveAlarm}>Save</button>
          <button onClick={handleCancel}>Cancel</button>
        </div>
      </div>
    </>
  );
}