use std::process::{Command, Stdio};
use std::io::Write;

#[tauri::command]
pub async fn call_java(json: String) -> Result<String, String> {
    let mut child = Command::new("java")
        .arg("-jar")
        .arg("CustomCalendar.jar")
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .spawn()
        .map_err(|e| e.to_string())?;

    if let Some(stdin) = &mut child.stdin {
        stdin.write_all(json.as_bytes()).unwrap();
    }

    let output = child.wait_with_output().unwrap();
    let response = String::from_utf8(output.stdout).unwrap();

    Ok(response)
}
