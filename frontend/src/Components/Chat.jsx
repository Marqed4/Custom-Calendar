import { useState, useEffect, useRef } from "react";
import { getCurrentWebviewWindow } from "@tauri-apps/api/webviewWindow";
import { convertFileSrc } from "@tauri-apps/api/core";

import FallBackground from "../resources/assets/images/Backgrounds/Fall Forest.gif";
import WinterBackground from "../resources/assets/images/Backgrounds/Winter Forest.gif";
import SpringBackground from "../resources/assets/images/Backgrounds/Spring Forest.gif";
import SummerBackground from "../resources/assets/images/Backgrounds/Summer Forest.gif";
import SilosBackground from "../resources/assets/images/Backgrounds/Silos.gif";
import LakeSideBackground from "../resources/assets/images/Backgrounds/Lake Side.gif";
import PeaceBackground from "../resources/assets/images/Backgrounds/Peace.gif";
import BarnBackground from "../resources/assets/images/Backgrounds/Barn.gif";

import Send from "../resources/assets/images/Signs/Semi Reflective Guitar.gif";

import "./Chat.css";

const appWindow = getCurrentWebviewWindow();

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

export default function Chat() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [alarms, setAlarms] = useState([]);
  const messagesEndRef = useRef(null);

  const [bg, setBg] = useState(null);

  useEffect(() => {
    async function init() {
        let loadedAlarms = [];
        try {
            const res = await fetch("http://localhost:4567/api/alarms");
            loadedAlarms = await res.json();
            setAlarms(loadedAlarms);
        } catch (_) {}

        const alarmContext = loadedAlarms.length > 0
            ? `The user's calendar events are: ${loadedAlarms.map(a =>
                `"${a.title}" at ${new Date(a.time).toLocaleString()}${a.desc ? ` (${a.desc})` : ""}`
              ).join("; ")}.`
            : "The user has no calendar events.";

        setLoading(true);
        try {
            const res = await fetch("http://localhost:4567/api/chat/greeting", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ alarmContext }),
            });
            const data = await res.json();
            setMessages([{ role: "assistant", content: data.reply ?? "Hey... like, what do you want?" }]);
        } catch (_) {
            setMessages([{ role: "assistant", content: "Hey... like, what do you want?" }]);
        }
        setLoading(false);
      }
      init();
  }, []);

  useEffect(() => {
    async function loadBg() {
      const savedBg = localStorage.getItem("calisigh-bg") ?? "fall";

      if (BG_MAP[savedBg]) {
        setBg(BG_MAP[savedBg]);
        return;
      }

      // supporting custom background, but only updates upon forcereload
      try {
        const res = await fetch("http://localhost:4567/api/backgrounds");
        const data = await res.json();
        const entry = data.find(e => e.split("|&")[0] === savedBg);
        if (entry) {
          setBg(convertFileSrc(entry.split("|&")[1]));
        } else {
          setBg(BG_MAP["fall"]);
        }
      } catch {
        setBg(BG_MAP["fall"]);
      }
    }
    loadBg();
  }, []);

  const sendMessage = async () => {
    if (!input.trim() || loading) return;
    const userMsg = { role: "user", content: input.trim() };
    const newMessages = [...messages, userMsg];
    setMessages(newMessages);
    setInput("");
    setLoading(true);
    try {
      const alarmContext = alarms.length > 0
        ? `The user's calendar events are: ${alarms.map(a =>
            `"${a.title}" at ${new Date(a.time).toLocaleString()}${a.desc ? ` (${a.desc})` : ""}`
          ).join("; ")}.`
        : "The user has no calendar events.";
      const response = await fetch("http://localhost:4567/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ alarmContext, messages: newMessages }),
      });
      const data = await response.json();
      const reply = data.reply ?? "...whatever, I got nothing. Try again I guess.";
      setMessages(prev => [...prev, { role: "assistant", content: reply }]);
    } catch (err) {
      setMessages(prev => [...prev, { role: "assistant", content: "Great, something broke. not surprised honestly. Try again." }]);
    }
    setLoading(false); 
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <div style={{ position: "relative", height: "100vh" }}>
      <div className="chat-background" style={{ backgroundImage: bg ? `url(${bg})` : undefined }} />
      <div className="chat-wrapper">
        <div className="chat-header">
          <span>Calisigh Helper</span>
        </div>
        <div className="chat-messages">
          {messages.map((msg, i) => (
            <div key={i} className={`chat-bubble ${msg.role}`}>
              {msg.content}
            </div>
          ))}
          {loading && (
            <div className="chat-bubble assistant loading">
              <span className="dot" /><span className="dot" /><span className="dot" />
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>
        <div className="chat-input-row">
          <textarea
            className="chat-input"
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Pour your heart out... or whatever."
            rows={2}
          />
          <button className="chat-send" onClick={sendMessage} disabled={loading}>
            <img src={Send} 
            alt="Send"
            className="chat-send" 
            />
          </button>
        </div>
      </div>
    </div>
  );
}