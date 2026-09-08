@echo off
setlocal EnableExtensions EnableDelayedExpansion

cd /d "%~dp0\.."

echo.
echo == Open Dash local smoke checks ==
echo Repo: %CD%
echo.

if not exist "gradlew.bat" (
    echo ERROR: gradlew.bat was not found. Run this script from the repository checkout.
    exit /b 1
)

echo [1/4] Building local debug APK...
call gradlew.bat :app:assembleLocalDebug --no-daemon
if errorlevel 1 (
    echo ERROR: local debug build failed.
    exit /b 1
)

echo.
echo [2/4] Running local debug unit tests...
call gradlew.bat :app:testLocalDebugUnitTest --no-daemon
if errorlevel 1 (
    echo ERROR: local debug unit tests failed.
    exit /b 1
)

echo.
echo [3/4] Checking Tripper connection permissions...
where rg >nul 2>nul
if errorlevel 1 (
    echo ERROR: ripgrep ^(rg^) is required for the static permission smoke checks.
    echo Install rg or manually verify HomeScreen.kt and RouteScreen.kt before release work.
    exit /b 1
)

for %%P in (ACCESS_WIFI_STATE CHANGE_NETWORK_STATE NEARBY_WIFI_DEVICES FOREGROUND_SERVICE_CONNECTED_DEVICE FOREGROUND_SERVICE_LOCATION) do (
    rg -n "%%P" app\src\main\AndroidManifest.xml >nul
    if errorlevel 1 (
        echo ERROR: required Tripper permission %%P is missing from AndroidManifest.xml.
        exit /b 1
    )
)

echo OK: Tripper connection permissions are declared.

echo.
echo [4/4] Checking connection navigation...
rg -n "NavTab\(Screen.Home" app\src\main\java\com\example\opendash\ui\navigation\AppNavigation.kt >nul
if errorlevel 1 (
    echo ERROR: Home tab is missing.
    exit /b 1
)
rg -n "composable\(Screen.Dash.route\)" app\src\main\java\com\example\opendash\ui\navigation\AppNavigation.kt >nul
if errorlevel 1 (
    echo ERROR: Dash connection route is missing.
    exit /b 1
)
rg -n "Send to Tripper Dash" app\src\main\java\com\example\opendash\ui\screens\RouteScreen.kt >nul
if errorlevel 1 (
    echo ERROR: Route-to-dash action is missing.
    exit /b 1
)

echo OK: Home, dash connection, and route projection flows are reachable.
echo.
echo == Open Dash local smoke checks passed ==
exit /b 0
