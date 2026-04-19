import { useState, useEffect } from "react";
import { getCurrentWindow } from "@tauri-apps/api/window";
import { open } from "@tauri-apps/plugin-dialog";
import { convertFileSrc } from "@tauri-apps/api/core";

import FallBackground from "../resources/assets/images/Backgrounds/Fall Forest.gif";
import WinterBackground from "../resources/assets/images/Backgrounds/Winter Forest.gif";
import SpringBackground from "../resources/assets/images/Backgrounds/Spring Forest.gif";
import SummerBackground from "../resources/assets/images/Backgrounds/Summer Forest.gif";
import SilosBackground from "../resources/assets/images/Backgrounds/Silos.gif";
import LakeSideBackground from "../resources/assets/images/Backgrounds/Lake Side.gif";
import PeaceBackground from "../resources/assets/images/Backgrounds/Peace.gif";
import BarnBackground from "../resources/assets/images/Backgrounds/Barn.gif";

import SaveInactive from "../resources/assets/images/Settings/Save Inactive.gif";
import SaveActive from "../resources/assets/images/Settings/Save Active.gif";
import CancelInactive from "../resources/assets/images/Settings/Cancel Inactive.gif";
import CancelActive from "../resources/assets/images/Settings/Cancel Active.gif";
import UploadInactive from "../resources/assets/images/Settings/Upload Inactive.gif";
import UploadActive from "../resources/assets/images/Settings/Upload Active.gif";
import Remove from "../resources/assets/images/Signs/Red Remove.gif";
import "./Settings.css";

const BG_MAP = [
  { label: "Fall",   value: "fall",   src: FallBackground },
  { label: "Winter", value: "winter", src: WinterBackground },
  { label: "Spring", value: "spring", src: SpringBackground },
  { label: "Summer", value: "summer", src: SummerBackground },
  { label: "Silos", value: "silos", src: SilosBackground },
  { label: "Lake Side", value: "lakeside", src: LakeSideBackground },
  { label: "Peace", value: "peace", src: PeaceBackground },
  { label: "Barn", value: "barn", src: BarnBackground },
];

function HoverGif({ inactive, active, onClick, title, className }) {
  const [hovered, setHovered] = useState(false);
  return (
    <img
      src={hovered ? active : inactive}
      alt={title}
      className={className}
      title={title}
      onClick={onClick}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    />
  );
}

export default function Settings() {
  const [selectedBg, setSelectedBg] = useState(
    localStorage.getItem("calisigh-bg") ?? "fall"
  );
  const [customBackgrounds, setCustomBackgrounds] = useState([]);
  const [customSounds, setCustomSounds] = useState([]);
  const [selectedSound, setSelectedSound] = useState(
    localStorage.getItem("calisigh-sound") ?? null
  );

  const CURRENT_BACKGROUND = BG_MAP.find(b => b.value === selectedBg);

  const previewBg = CURRENT_BACKGROUND
    ? CURRENT_BACKGROUND.src
    : customBackgrounds.find(e => e.split("|&")[0] === selectedBg)
        ? convertFileSrc(customBackgrounds.find(e => e.split("|&")[0] === selectedBg).split("|&")[1])
        : BG_MAP[0].src;

  useEffect(() => {
    loadBackgrounds();
    loadSounds();
  }, []);

  async function loadBackgrounds() {
    try {
      const res = await fetch("http://localhost:4567/api/backgrounds");
      const data = await res.json();
      setCustomBackgrounds(data);
    } catch (err) {
      console.error("Failed to load backgrounds:", err);
    }
  }

  async function loadSounds() {
    try {
      const [soundsRes, selectedRes] = await Promise.all([
        fetch("http://localhost:4567/api/sounds"),
        fetch("http://localhost:4567/api/sounds/selected"),
      ]);
      const sounds = await soundsRes.json();
      const selected = await selectedRes.json();
      setCustomSounds(sounds);
      if (selected && selected !== "") {
        setSelectedSound(selected);
        localStorage.setItem("calisigh-sound", selected);
      } else {
        setSelectedSound(null);
        localStorage.removeItem("calisigh-sound");
      }
    } catch (err) {
      console.error("Failed to load sounds:", err);
    }
  }

  async function uploadBackground() {
    try {
      const path = await open({
        multiple: false,
        filters: [{ name: "Images", extensions: ["png", "jpg", "jpeg", "gif", "webp"] }],
      });
      if (!path) return;
      await fetch("http://localhost:4567/api/backgrounds", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ sourcePath: path }),
      });
      await new Promise(r => setTimeout(r, 300));
      await loadBackgrounds();
    } catch (err) {
      console.error("Failed to upload background:", err);
    }
  }

  async function removeBackground(entry) {
    const name = entry.split("|&")[0];
    if (selectedBg === name) setSelectedBg("fall");
    try {
      await fetch("http://localhost:4567/api/backgrounds", {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name }),
      });
      await loadBackgrounds();
    } catch (err) {
      console.error("Failed to remove background:", err);
    }
  }

  async function uploadSound() {
    try {
      const path = await open({
        multiple: false,
        filters: [{ name: "Audio", extensions: ["mp3", "wav"] }],
      });
      if (!path) return;
      await fetch("http://localhost:4567/api/sounds", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ sourcePath: path }),
      });
      await new Promise(r => setTimeout(r, 300));
      await loadSounds();
    } catch (err) {
      console.error("Failed to upload sound:", err);
    }
  }

  async function removeSound(entry) {
    const name = entry.split("|&")[0];
    if (selectedSound === name) setSelectedSound(null);
    try {
      await fetch("http://localhost:4567/api/sounds", {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name }),
      });
      await loadSounds();
    } catch (err) {
      console.error("Failed to remove sound:", err);
    }
  }

  async function stopSound() {
    try {
      await fetch("http://localhost:4567/api/sounds/stop", { method: "POST" });
    } catch (err) {
      console.error("Failed to stop sound:", err);
    }
  }

  async function previewSound(name) {
    try {
      await fetch("http://localhost:4567/api/sounds/play", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name }),
      });
    } catch (err) {
      console.error("Failed to preview sound:", err);
    }
  }

  async function save() {
    localStorage.setItem("calisigh-bg", selectedBg);
    localStorage.setItem("calisigh-custom-bgs", JSON.stringify(customBackgrounds));
    if (selectedSound) localStorage.setItem("calisigh-sound", selectedSound);
    else localStorage.removeItem("calisigh-sound");
    try {
      await fetch("http://localhost:4567/api/sounds/select", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name: selectedSound ?? "" }),
      });
      await stopSound();
    } catch (_) {}
    await getCurrentWindow().close();
  }

  async function handleCancel() {
    await stopSound();
    await getCurrentWindow().close();
  }

  return (
    <>
      <div
        className="settings-background"
        style={{ backgroundImage: `url(${previewBg})` }}
      />
      <div className="settings-root">

        <div className="settings-main">
          <div className="settings-main-body">
            <h2 className="settings-title">Settings</h2>
            <p className="settings-subtitle">Choose your background</p>

            <div className="settings-bg-picker">
              {BG_MAP.map(bg => (
                <div
                  key={bg.value}
                  className={`settings-bg-card ${selectedBg === bg.value ? "settings-bg-card--selected" : ""}`}
                  onClick={() => setSelectedBg(bg.value)}
                >
                  <img src={bg.src} alt={bg.label} className="settings-bg-img" />
                  <span className="settings-bg-label">{bg.label}</span>
                </div>
              ))}

              {customBackgrounds.map((entry, i) => {
                const name = entry.split("|&")[0];
                const path = entry.split("|&")[1];
                return (
                  <div
                    key={i}
                    className={`settings-bg-card ${selectedBg === name ? "settings-bg-card--selected" : ""}`}
                    onClick={() => setSelectedBg(name)}
                  >
                    <img src={convertFileSrc(path)} alt={name} className="settings-bg-img" />
                    <span className="settings-bg-label">{name}</span>
                    <img
                      src={Remove}
                      alt="Remove"
                      className="settings-bg-remove"
                      onClick={(e) => { e.stopPropagation(); removeBackground(entry); }}
                    />
                  </div>
                );
              })}
            </div>
          </div>

          <div className="settings-actions">
            <div className="settings-action-btn" onClick={uploadBackground}>
              <HoverGif inactive={UploadInactive} active={UploadActive} title="Upload Background" className="settings-upload-backgrounds-gif" />
            </div>
            <div className="settings-action-btn" onClick={save}>
              <HoverGif inactive={SaveInactive} active={SaveActive} title="Save" className="settings-save-gif" />
            </div>
            <div className="settings-action-btn" onClick={handleCancel}>
              <HoverGif inactive={CancelInactive} active={CancelActive} title="Cancel" className="settings-cancel-gif" />
            </div>
          </div>
        </div>

        <div className="settings-sounds">
          <div className="settings-sounds-body">
            <h1 className="sound-subtitle">Alert Sound</h1>
            <div className="settings-sounds-list">
              <div
                className={`settings-sound-card ${selectedSound === null ? "settings-sound-card--selected" : ""}`}
                onClick={() => {
                  if (selectedSound === null) {
                    stopSound();
                  } else {
                    setSelectedSound(null);
                    previewSound("chime");
                  }
                }}
              >
                <span>Default Chime</span>
              </div>

              {customSounds.map((entry, i) => {
                const name = entry.split("|&")[0];
                return (
                  <div
                    key={i}
                    title={name}
                    className={`settings-sound-card ${selectedSound === name ? "settings-sound-card--selected" : ""}`}
                    onClick={() => {
                      if (selectedSound === name) {
                        stopSound();
                      } else {
                        setSelectedSound(name);
                        previewSound(name);
                      }
                    }}
                  >
                    <span>{name}</span>
                    <img
                      src={Remove}
                      alt="Remove"
                      className="settings-sound-remove"
                      onClick={(e) => { e.stopPropagation(); removeSound(entry); }}
                    />
                  </div>
                );
              })}
            </div>
          </div>

          <div className="settings-sounds-footer">
            <HoverGif inactive={UploadInactive} active={UploadActive} onClick={uploadSound} title="Add Sound" className="settings-upload-songs-gif" />
          </div>
        </div>

      </div>
    </>
  );
}