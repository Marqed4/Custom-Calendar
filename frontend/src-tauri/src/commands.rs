use tauri::{AppHandle, Manager, WebviewUrl, WebviewWindowBuilder};
#[allow(dead_code)]
#[tauri::command]
pub async fn call_java(json: String) -> Result<String, String> {
    let client = reqwest::Client::new();

    let response = client
        .post("http://localhost:8080/api")
        .header("Content-Type", "application/json")
        .body(json)
        .send()
        .await
        .map_err(|e| format!("Request failed: {}", e))?;

    let body = response
        .text()
        .await
        .map_err(|e| format!("Failed to read response: {}", e))?;

    Ok(body)
}

fn open_window(
    app: &AppHandle,
    label: &str,
    title: &str,
    url: &str,
    width: f64,
    height: f64,
    fullscreen: bool,
    resizable: bool,
) {
    if let Some(w) = app.get_webview_window(label) {
        let _: tauri::Result<()> = w.set_focus();
        return;
    }

    WebviewWindowBuilder::new(app, label, WebviewUrl::App(url.into()))
        .title(title)
        .inner_size(width, height)
        .fullscreen(fullscreen)
        .max_inner_size(950.0, 650.0)
        .resizable(resizable)
        .build()
        .expect(&format!("Failed to open {}", label));
}

#[tauri::command]
pub fn open_add_alarm(app: AppHandle, date: String) {
    let url = format!("/add-alarm?date={}", date);
    open_window(
        &app,
        "add-alarm",
        "Add Alarm",
        &url,
        420.0,
        420.0,
        false,
        true,
    );
}

#[tauri::command]
pub fn open_view_edit_alarm(app: AppHandle, alarm_id: String) {
    let url = format!("/view-edit-alarm?id={}", alarm_id);
    open_window(
        &app,
        "view-edit-alarm",
        "Edit Alarm",
        &url,
        420.0,
        420.0,
        false,
        true,
    );
}

#[tauri::command]
pub fn open_settings(app: AppHandle) {
    open_window(
        &app,
        "view-settings",
        "Settings",
        "/view-settings",
        420.0,
        420.0,
        false,
        true,
    );
}

#[tauri::command]
pub fn open_chat_assistant(app: AppHandle) {
    open_window(
        &app,
        "view-chat-assistant",
        "Chat",
        "/view-chat-assistant",
        420.0,
        420.0,
        false,
        true,
    );
}
