import { useEffect, useState } from "react";
import { getCurrentWindow } from "@tauri-apps/api/window";
import FallBackground from "../resources/assets/images/Backgrounds/Fall Forest.gif";
import WinterBackground from "../resources/assets/images/Backgrounds/Winter Forest.gif";
import SpringBackground from "../resources/assets/images/Backgrounds/Spring Forest.gif";
import SummerBackground from "../resources/assets/images/Backgrounds/Summer Forest.gif";
import SilosBackground from "../resources/assets/images/Backgrounds/Silos.gif";
import LakeSideBackground from "../resources/assets/images/Backgrounds/Lake Side.gif";
import PeaceBackground from "../resources/assets/images/Backgrounds/Peace.gif";
import BarnBackground from "../resources/assets/images/Backgrounds/Barn.gif";

import Bell from "../resources/assets/images/Signs/Bell.gif?url";

import "./AlarmFiring.css";

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

export default function AlarmFiring() {
  const params = new URLSearchParams(window.location.search);
  const title = params.get("title") || "Alarm";
  const desc  = params.get("desc")  || "";

  const [stopping, setStopping] = useState(false);
  const bg = BG_MAP[localStorage.getItem("calisigh-bg") ?? "fall"];

    async function stopAlarm() {
    setStopping(true);
    try {
        await fetch("http://localhost:4567/api/sounds/stop", { method: "POST" });
        await fetch("http://localhost:4567/api/alarms/firing", { method: "DELETE" });
    } catch (e) {
        console.error("Failed to stop alarm:", e);
    }
    await getCurrentWindow().close();
    }

    useEffect(() => {
    const win = getCurrentWindow();
    let unlisten;
    win.onCloseRequested(async (event) => {
        event.preventDefault();
        try {
        await fetch("http://localhost:4567/api/sounds/stop", { method: "POST" });
        await fetch("http://localhost:4567/api/alarms/firing", { method: "DELETE" });
        } catch {}
        if (unlisten) (await unlisten)();
        await win.destroy();
    });
    return () => { if (unlisten) unlisten.then(f => f()); };
    }, []);

  return (
    <>
      <div className="alarm-background" style={{ backgroundImage: `url(${bg})` }} />
      <div className="alarm-firing-window">
        <img src={Bell} className="alarm-firing-icon" alt="Alarm" />
        <h2 className="alarm-firing-title">{title}</h2>
        {desc && <p className="alarm-firing-desc">{desc}</p>}
        <button
          className={`alarm-firing-stop ${stopping ? "alarm-firing-stop--stopping" : ""}`}
          onClick={stopAlarm}
          disabled={stopping}
        >
          {stopping ? "Stopping..." : "Stop Alarm"}
        </button>
      </div>
    </>
  );
}