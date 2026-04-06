import { useState, useEffect, useRef } from "react";
import { listen } from "@tauri-apps/api/event";
import { invoke } from "@tauri-apps/api/core";
import WinterBackground from "../resources/assets/images/Winter Forest.gif";
import MonthYearDisplay from "./MonthYearDisplay.jsx";
import CalendarGrid from "./CalendarGrid.jsx";
import Sidebar from "./Sidebar.jsx";
import "./MonthYearDisplay.css";
import "./Home.css";

export default function Home() {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [alarms, setAlarms] = useState([]);
  const [gridSize, setGridSize] = useState(0);
  const mainRef = useRef(null);

  function updateSize() {
    if (mainRef.current) {
      const { width, height } = mainRef.current.getBoundingClientRect();
      const navHeight = document.querySelector(".top-nav")?.getBoundingClientRect().height ?? 55;
      setGridSize(Math.min(width, height - navHeight));
    }
  }

  async function loadAlarms() {
    try {
      const res = await fetch("http://localhost:4567/api/alarms");
      const data = await res.json();
      setAlarms(data);
    } catch (err) {
      console.error("Failed to load alarms:", err);
    }
  }

  useEffect(() => {
    loadAlarms();
    const unlisten = listen("alarm-saved", () => loadAlarms());
    updateSize();
    window.addEventListener("resize", updateSize);
    return () => {
      unlisten.then(f => f());
      window.removeEventListener("resize", updateSize);
    };
  }, []);

  function changeMonth(offset) {
    const newDate = new Date(currentDate);
    newDate.setMonth(newDate.getMonth() + offset);
    setCurrentDate(newDate);
  }

  function getCalendarDays(date) {
    const year = date.getFullYear();
    const month = date.getMonth();
    const startDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const days = [];
    for (let i = 0; i < startDay; i++) days.push(null);
    for (let d = 1; d <= daysInMonth; d++) days.push(new Date(year, month, d));
    return days;
  }

  async function openAlarmWindow(date) {
    if (!date) return;
    try {
      await invoke("open_alarm_window", {
        day: date.getDate(),
        month: currentDate.getMonth() + 1,
        year: currentDate.getFullYear(),
      });
    } catch (err) {
      console.error("Failed to open alarm window:", err);
    }
  }

  const calendarDays = getCalendarDays(currentDate);

  return (
    <div
      className="background-wrapper"
      style={{ backgroundImage: `url(${WinterBackground})` }}
    >
      <div className="app-container">
        <Sidebar currentDate={currentDate} calendarDays={calendarDays} />
        <main className="main" ref={mainRef}>
          <MonthYearDisplay
            currentDate={currentDate}
            onPrev={() => changeMonth(-1)}
            onNext={() => changeMonth(1)}
          />
          <CalendarGrid
            calendarDays={calendarDays}
            currentDate={currentDate}
            alarms={alarms}
            onDayClick={openAlarmWindow}
            gridSize={gridSize}
          />
        </main>
      </div>
    </div>
  );
}