#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod commands;

use std::process::{Command, Stdio};
use tauri::Manager;

fn main() {
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .setup(|app| {
            let exe_dir = std::env::current_exe()
                .ok()
                .and_then(|p| p.parent().map(|p| p.to_path_buf()))
                .unwrap_or_else(|| std::path::PathBuf::from("."));

            println!("exe_dir: {:?}", exe_dir);

            let jar_path = exe_dir
                .ancestors()
                .find_map(|p| {
                    let candidate = p.join("CustomCalendar.jar");
                    if candidate.exists() { Some(candidate) } else { None }
                })
                .expect("Could not find CustomCalendar.jar");

            println!("Found JAR at: {:?}", jar_path);

            let result = Command::new("java")
                .arg("-jar")
                .arg(&jar_path)
                .stdin(Stdio::null())
                .stdout(Stdio::inherit())
                .stderr(Stdio::inherit())
                .spawn();

            match result {
                Ok(_) => println!("Backend started!"),
                Err(e) => println!("Failed: {}", e),
            }

            Ok(())
        })
        .invoke_handler(tauri::generate_handler![
            commands::call_java,
            commands::open_alarm_window
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}