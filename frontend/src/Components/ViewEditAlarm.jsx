import { useState } from "react";
import { emit } from "@tauri-apps/api/event";
import { getCurrentWindow } from "@tauri-apps/api/window";

import FallBackground from "../resources/assets/images/Backgrounds/Fall Forest.gif";
import WinterBackground from "../resources/assets/images/Backgrounds/Winter Forest.gif";
import SpringBackground from "../resources/assets/images/Backgrounds/Spring Forest.gif";
import SummerBackground from "../resources/assets/images/Backgrounds/Summer Forest.gif";
import SilosBackground from "../resources/assets/images/Backgrounds/Silos.gif";
import LakeSideBackground from "../resources/assets/images/Backgrounds/Lake Side.gif";
import PeaceBackground from "../resources/assets/images/Backgrounds/Peace.gif";
import BarnBackground from "../resources/assets/images/Backgrounds/Barn.gif";

const BG_MAP = {
  fall: FallBackground,
  winter: WinterBackground,
  spring: SpringBackground,
  summer: SummerBackground,
  silos: SilosBackground,
  lakeside: LakeSideBackground,
  peace: PeaceBackground,
  barn: BarnBackground,
};

import "./AddAlarm.css";
import "./ViewEditAlarm.css";

export default function ViewEditAlarm() {
  const params = new URLSearchParams(window.location.search);
  const id    = params.get("id");
  const day   = params.get("day");
  const month = params.get("month");
  const year  = params.get("year");

  const [title, setTitle] = useState(params.get("title") || "");
  const [desc,  setDesc]  = useState(params.get("desc")  || "");
  const [time,  setTime]  = useState(params.get("time")  || "");
  const [error, setError] = useState("");

  const bg = BG_MAP[localStorage.getItem("calisigh-bg") ?? "fall"];

  async function saveAlarm() {
    if (!title || !time) {
      setError("Title and time are required.");
      return;
    }

    const iso = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}T${time}:00`;

    try {
      await fetch(`http://localhost:4567/api/alarms/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ time: iso, title, desc }),
      });

      await emit("alarm-saved");
      await getCurrentWindow().close();
    } catch {
      setError("Failed to save. Is the server running?");
    }
  }

  async function deleteAlarm() {
    try {
      await fetch(`http://localhost:4567/api/alarms/${id}`, {
        method: "DELETE",
      });

      await emit("alarm-saved");
      await getCurrentWindow().close();
    } catch {
      setError("Failed to delete. Is the server running?");
    }
  }

  async function handleCancel() {
    await getCurrentWindow().close();
  }

  return (
    <>
      <div className="alarm-background" 
      style={{ backgroundImage: `url(${bg})` }} />

      <div className="alarm-window">
        <h2>Edit Alarm</h2>
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
          <button className="delete-btn" onClick={deleteAlarm}>Delete</button>
          <button onClick={handleCancel}>Cancel</button>
        </div>
      </div>
    </>
  );
}