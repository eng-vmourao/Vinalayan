# Changelog

## Vinalayan 0.1.3 Preview

- Added Google sign-in to the welcome screen and account settings.
- Added Firebase Authentication configuration to signed GitHub APK builds.
- Added clearer Portuguese account actions and credential errors.

## Vinalayan 0.1.2 Preview

- Made the repository public so releases can be checked without embedding a GitHub credential.
- Replaced the placeholder update button with an in-app release check and verified APK download.
- Added package, version, size, and SHA-256 checks before opening Android's installer.
- Added the Android unknown-source permission flow required for self-updates.

## Vinalayan 0.1.1 Preview

- Added Brazilian real (`BRL`, `R$`) to the currency list and made it the default for new selections/installations.
- Added Brazilian thousands and decimal separators when displaying values in reais.
- Kept previously selected currencies unchanged after updating the app.

## 1.3.1

- Added a currency selector in Settings for expense display and exports.
- Added Android 11 / ColorOS Wi-Fi fallback for dash pairing when the system network dialog or SSID callback is unreliable.
- Prevented dash authentication from starting with an unresolved prefix-only SSID.
- Masked SSIDs in connection diagnostics.
- Kept dash handshake, authentication packet format, socket ports, RTP, H.264, and UDP behavior unchanged.

## 1.3

- Material 3 Expressive UI refresh across the app.
- Added Dynamic Wallpaper theming as the first theme option.
- Added Auto Day/Night black-and-white theme as the second theme option.
- Kept motorcycle theme palettes available after the dynamic and auto modes.
- Improved app-wide theme contrast so Garage, dialogs, cards, buttons, and spare-part rows stay readable across light, dynamic, and motorcycle themes.
- Active vehicle selection with vehicle-specific garage and expense data.
- New expenses now default to the current selected vehicle without an editable vehicle field.
- Redesigned Garage with editable odometer and average mileage from the latest five fill-ups.
- Spare-part details, interval editing, history, and service logging.
- Fuel entries no longer delete when tapped.
- Monthly and all-time expense filtering and sharing.
- Dash wallpaper video decoding capped at 8 FPS.
- Improved navigation transitions and general UI fixes.

## 1.3 Beta 2

- Moved ride totals and recent ride history onto the Home screen.
- Improved ride distance accuracy by rejecting poor GPS fixes and stationary drift.
- Added GPS weak/lost indicators and heading-aware off-route detection.
- Added the upstream MapLibre page-switch lifecycle crash fix.
- Added now-playing and caller cards for the bike dash.
- Added joystick controls for media tracks and incoming/active calls.
- Added notification-access and call-control settings.
- Reduced the arm64 release download from about 44.5 MB to about 14.4 MB with ABI-specific APKs.
- Kept dash handshake, authentication, ACK, route-card, socket, and RTP behavior unchanged.
- Documented the reviewed additive `05 0D` media and `05 22` call packet extension.
