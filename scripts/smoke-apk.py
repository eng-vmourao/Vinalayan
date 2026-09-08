"""Install the release APK on a disposable emulator and open the main tabs."""

from pathlib import Path
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

PACKAGE = "com.vinalayan.app"
ACTIVITY = f"{PACKAGE}/com.example.opendash.MainActivity"
OUTPUT = Path("dist/smoke")


def adb(*args, check=True):
    return subprocess.run(
        ["adb", *args], check=check, capture_output=True, timeout=60
    ).stdout.decode("utf-8", errors="replace")


def hierarchy():
    adb("shell", "uiautomator", "dump", "/sdcard/vinalayan-ui.xml")
    return adb("shell", "cat", "/sdcard/vinalayan-ui.xml")


def wait_for(label):
    deadline = time.monotonic() + 60
    while time.monotonic() < deadline:
        if not adb("shell", "pidof", PACKAGE, check=False).strip():
            raise AssertionError("Vinalayan process stopped")
        xml = hierarchy()
        (OUTPUT / "last-screen.xml").write_text(xml, encoding="utf-8")
        root = ET.fromstring(xml)
        matches = [
            node for node in root.iter("node")
            if label.casefold() in [
                line.strip().casefold() for line in node.get("text", "").splitlines()
            ]
        ]
        if matches:
            return matches, xml
        time.sleep(1)
    raise AssertionError(f"Screen text not found: {label}; visible text: {[n.get('text') for n in root.iter('node') if n.get('text')]}")


def tap(label):
    matches, _ = wait_for(label)
    # Repeated titles also appear at the top; the navigation tab is lowest.
    node = max(matches, key=lambda n: int(re.findall(r"\d+", n.get("bounds"))[1]))
    left, top, right, bottom = map(int, re.findall(r"\d+", node.get("bounds")))
    adb("shell", "input", "tap", str((left + right) // 2), str((top + bottom) // 2))


def capture(name, label):
    _, xml = wait_for(label)
    (OUTPUT / f"{name}.xml").write_text(xml, encoding="utf-8")
    screenshot = subprocess.run(
        ["adb", "exec-out", "screencap", "-p"],
        check=True, capture_output=True, timeout=30,
    ).stdout
    (OUTPUT / f"{name}.png").write_bytes(screenshot)
    print(f"PASS: {name}", flush=True)


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    apk = Path(sys.argv[1])
    if not apk.is_file():
        raise FileNotFoundError(apk)
    try:
        print(adb("install", "-r", str(apk)), flush=True)
        adb("logcat", "-c")
        result = adb("shell", "am", "start", "-W", "-n", ACTIVITY)
        if "Status: ok" not in result:
            raise AssertionError(result)
        capture("01-login", "Vinalayan")
        tap("Continue")
        capture("02-vehicles", "My Vehicles")
        for tab, marker in [
            ("Expenses", "My Expenses"),
            ("Garage", "Active vehicle"),
            ("More", "Update from GitHub"),
        ]:
            tap(tab)
            capture(tab.lower(), marker)
        crash_log = adb("logcat", "-b", "crash", "-d")
        if PACKAGE in crash_log:
            raise AssertionError("Android recorded a Vinalayan crash")
        print("PASS: signed APK installed and all four main tabs opened", flush=True)
    finally:
        screenshot = subprocess.run(
            ["adb", "exec-out", "screencap", "-p"], capture_output=True, timeout=30,
        ).stdout
        (OUTPUT / "last-screen.png").write_bytes(screenshot)
        (OUTPUT / "logcat.txt").write_text(adb("logcat", "-d", check=False), encoding="utf-8")


if __name__ == "__main__":
    main()
