#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]
mod commands;
use std::os::windows::process::CommandExt;
use std::process::{Command, Stdio};
use tauri::{
    Manager,
    menu::{Menu, MenuItem, PredefinedMenuItem},
    tray::{MouseButton, MouseButtonState, TrayIconBuilder, TrayIconEvent},
};
use tauri_plugin_dialog::DialogExt;
use tauri_plugin_autostart::ManagerExt;

const CREATE_NO_WINDOW: u32 = 0x08000000;

fn install_ollama() -> Result<(), String> {
    let status = Command::new("powershell")
        .args([
            "-NoProfile",
            "-NonInteractive",
            "-Command",
            "Invoke-WebRequest -Uri 'https://ollama.com/download/OllamaSetup.exe' -OutFile \"$env:TEMP\\OllamaSetup.exe\"; Start-Process \"$env:TEMP\\OllamaSetup.exe\" -Wait"
        ])
        .creation_flags(CREATE_NO_WINDOW)
        .status()
        .map_err(|e| e.to_string())?;

    if status.success() {
        Ok(())
    } else {
        Err("Ollama installer failed".to_string())
    }
}

fn is_ollama_installed() -> bool {
    Command::new("ollama")
        .arg("--version")
        .creation_flags(CREATE_NO_WINDOW)
        .output()
        .is_ok()
}

fn is_model_available() -> bool {
    Command::new("ollama")
        .args(["list"])
        .creation_flags(CREATE_NO_WINDOW)
        .output()
        .map(|o| String::from_utf8_lossy(&o.stdout).contains("llama3.2"))
        .unwrap_or(false)
}

fn is_java_installed() -> bool {
    Command::new("java")
        .arg("-version")
        .creation_flags(CREATE_NO_WINDOW)
        .output()
        .is_ok()
}

fn install_jre(exe_dir: &std::path::Path) -> Result<(), String> {
    let msi_path = exe_dir
        .ancestors()
        .find_map(|p| {
            let candidate = p.join("temurin-21-jre.msi");
            if candidate.exists() { Some(candidate) } else { None }
        })
        .ok_or_else(|| "Could not find temurin-21-jre.msi in bundle".to_string())?;

    #[cfg(debug_assertions)]
    println!("Installing JRE from: {:?}", msi_path);

    let status = Command::new("msiexec")
        .args([
            "/i",
            msi_path.to_str().unwrap(),
            "/quiet",
            "/norestart",
        ])
        .creation_flags(CREATE_NO_WINDOW)
        .status()
        .map_err(|e| format!("Failed to launch msiexec: {}", e))?;

    if status.success() {
        Ok(())
    } else {
        Err(format!("msiexec exited with status: {}", status))
    }
}

fn kill_java_process() {
    let _ = Command::new("cmd")
        .args(["/C", "taskkill /F /IM java.exe"])
        .creation_flags(CREATE_NO_WINDOW)
        .spawn();
}

/// Attempt to show the main window, retrying up to 10 times with 200ms gaps
/// in case the webview hasn't finished registering yet (common on cold boot).
fn show_main_window(app: &tauri::AppHandle) {
    let app = app.clone();
    std::thread::spawn(move || {
        for _ in 0..10 {
            if let Some(win) = app.get_webview_window("main") {
                let _ = win.set_skip_taskbar(false);
                let _ = win.show();
                let _ = win.set_focus();
                return;
            }
            std::thread::sleep(std::time::Duration::from_millis(200));
        }
        #[cfg(debug_assertions)]
        println!("show_main_window: could not get 'main' window after 10 attempts");
    });
}

fn main() {
    tauri::Builder::default()
        .plugin(tauri_plugin_autostart::Builder::new().args(vec!["--autostart"]).build())
        .plugin(tauri_plugin_single_instance::init(|app, _args, _cwd| {
            // Second launch attempt: show and focus the existing window
            show_main_window(app);
        }))
        .plugin(tauri_plugin_opener::init())
        .plugin(tauri_plugin_dialog::init())
        .setup(|app| {
            let show_item = MenuItem::with_id(app, "show", "Show", true, None::<&str>)?;
            let toggle_autostart_item = MenuItem::with_id(app, "toggle_autostart", "Toggle Autostart", true, None::<&str>)?;
            let quit_item = MenuItem::with_id(app, "quit", "Quit", true, None::<&str>)?;
            let sep1 = PredefinedMenuItem::separator(app)?;
            let sep2 = PredefinedMenuItem::separator(app)?;

            let tray_menu = Menu::with_items(app, &[
                &show_item,
                &sep1,
                &toggle_autostart_item,
                &sep2,
                &quit_item,
            ])?;

            let _tray = TrayIconBuilder::new()
                .icon({
                    let img = image::open(
                        std::path::Path::new(env!("CARGO_MANIFEST_DIR")).join("icons/128x128@2x.png")
                    ).unwrap().into_rgba8();
                    let (w, h) = img.dimensions();
                    tauri::image::Image::new_owned(img.into_raw(), w, h)
                })
                .menu(&tray_menu)
                .on_menu_event(|app, event| match event.id.as_ref() {
                    "show" => {
                        show_main_window(app);
                    }
                    "toggle_autostart" => {
                        let autolaunch = app.autolaunch();
                        match autolaunch.is_enabled() {
                            Ok(true) => { let _ = autolaunch.disable(); }
                            Ok(false) => { let _ = autolaunch.enable(); }
                            Err(e) => {
                                let _ = app
                                    .dialog()
                                    .message(&format!("Autostart toggle failed: {}", e))
                                    .title("Autostart")
                                    .blocking_show();
                            }
                        }
                    }
                    "quit" => {
                        kill_java_process();
                        app.exit(0);
                    }
                    _ => {}
                })
                .on_tray_icon_event(|tray, event| {
                    if let TrayIconEvent::Click {
                        button: MouseButton::Left,
                        button_state: MouseButtonState::Up,
                        ..
                    } = event {
                        show_main_window(tray.app_handle());
                    }
                })
                .build(app)?;

            // --- Ollama check ---
            if !is_ollama_installed() {
                app.dialog()
                    .message("Calisigh 3.2 requires Ollama for its chat assistant.\n\nInstall it from https://ollama.com/download, then run:\n\n  ollama pull llama3.2")
                    .title("Ollama 3.2 Required")
                    .blocking_show();
            } else if !is_model_available() {
                std::thread::spawn(|| {
                    let _ = Command::new("ollama")
                        .args(["pull", "llama3.2"])
                        .creation_flags(CREATE_NO_WINDOW)
                        .status();
                });
            }

            // --- Java / JAR setup ---
            let exe_dir = std::env::current_exe()
                .ok()
                .and_then(|p| p.parent().map(|p| p.to_path_buf()))
                .unwrap_or_else(|| std::path::PathBuf::from("."));

            #[cfg(debug_assertions)]
            println!("exe_dir: {:?}", exe_dir);

            if !is_java_installed() {
                app.dialog()
                    .message("Java is not installed. Calisigh will now install the bundled Java 21 runtime.\n\nThis may take a minute…")
                    .title("Installing Java Runtime")
                    .blocking_show();

                match install_jre(&exe_dir) {
                    Ok(_) => {
                        #[cfg(debug_assertions)]
                        println!("JRE installed successfully.");
                        app.dialog()
                            .message("Java 21 was installed successfully.")
                            .title("Java Installed")
                            .blocking_show();
                    }
                    Err(e) => {
                        app.dialog()
                            .message(&format!(
                                "Failed to install Java automatically:\n\n{}\n\nPlease install Java 21 manually from https://adoptium.net",
                                e
                            ))
                            .title("Java Install Failed")
                            .blocking_show();
                        std::process::exit(1);
                    }
                }
            }

            let jar_path = exe_dir
                .ancestors()
                .find_map(|p| {
                    let candidate = p.join("CustomCalendar.jar");
                    if candidate.exists() { Some(candidate) } else { None }
                })
                .or_else(|| {
                    let candidate = exe_dir.join("CustomCalendar.jar");
                    if candidate.exists() { Some(candidate) } else { None }
                })
                .expect("Could not find CustomCalendar.jar");

            #[cfg(debug_assertions)]
            println!("Found JAR at: {:?}", jar_path);

            let already_running = Command::new("cmd")
                .args(["/C", "wmic process where \"commandline like '%CustomCalendar.jar%'\" get processid 2>nul | findstr /r \"[0-9]\""])
                .creation_flags(CREATE_NO_WINDOW)
                .output()
                .map(|o| !o.stdout.is_empty())
                .unwrap_or(false);

            if !already_running {
                #[cfg(debug_assertions)]
                let result = {
                    let mut child = Command::new("java")
                        .arg("-jar")
                        .arg(&jar_path)
                        .stdin(Stdio::null())
                        .stdout(Stdio::piped())
                        .stderr(Stdio::piped())
                        .creation_flags(CREATE_NO_WINDOW)
                        .spawn();

                    if let Ok(ref mut child) = child {
                        if let Some(stdout) = child.stdout.take() {
                            std::thread::spawn(move || {
                                use std::io::{BufRead, BufReader};
                                for line in BufReader::new(stdout).lines() {
                                    if let Ok(line) = line {
                                        println!("[java] {}", line);
                                    }
                                }
                            });
                        }
                        if let Some(stderr) = child.stderr.take() {
                            std::thread::spawn(move || {
                                use std::io::{BufRead, BufReader};
                                for line in BufReader::new(stderr).lines() {
                                    if let Ok(line) = line {
                                        eprintln!("[java:err] {}", line);
                                    }
                                }
                            });
                        }
                    }
                    child
                };

                #[cfg(not(debug_assertions))]
                let result = Command::new("java")
                    .arg("-jar")
                    .arg(&jar_path)
                    .stdin(Stdio::null())
                    .stdout(Stdio::null())
                    .stderr(Stdio::null())
                    .creation_flags(CREATE_NO_WINDOW)
                    .spawn();

                #[cfg(debug_assertions)]
                match result {
                    Ok(_) => println!("Backend started!"),
                    Err(e) => println!("Failed to start backend: {}", e),
                }
                #[cfg(not(debug_assertions))]
                let _ = result;
            } else {
                #[cfg(debug_assertions)]
                println!("Backend already running, skipping.");
            }

            let autolaunch = app.autolaunch();
            let _ = autolaunch.enable();

            let is_autostart = std::env::args().any(|a| a == "--autostart");
            if let Some(win) = app.get_webview_window("main") {
                if is_autostart {
                    let _ = win.set_skip_taskbar(true);
                    let _ = win.hide();
                    // Longer cooldown on cold boot to let the webview fully register
                    // before any tray click events can arrive
                    std::thread::sleep(std::time::Duration::from_millis(1500));
                } else {
                    let _ = win.set_skip_taskbar(false);
                    let _ = win.show();
                    let _ = win.set_focus();
                }
            }

            Ok(())
        })
        .on_window_event(|window, event| {
            match event {
                tauri::WindowEvent::CloseRequested { api, .. } => {
                    if window.label() == "main" {
                        api.prevent_close();
                        let _ = window.set_skip_taskbar(true);
                        let _ = window.hide();
                    }
                }
                tauri::WindowEvent::Destroyed => {
                    if window.label() == "main" {
                        kill_java_process();
                    }
                }
                _ => {}
            }
        })
        .invoke_handler(tauri::generate_handler![commands::call_java])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}