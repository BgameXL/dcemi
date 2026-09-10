#!/usr/bin/env bash
# Launch the Forge client headless.
# It runs Xvfb for two reasons: -ac disables X access control so GLFW can open the
# display without an auth, without it forge dies with "glfwInit failed".

# Gradle then runs as our direct child, so its stdout streams to the
# container instead of being buffered inside the xvfb run wrapper.
# All command I/O is over the TCP socket, stdout here is just logs.
set -uo pipefail

echo "[dcemi] user=$(id -u):$(id -g)  home=$HOME  pwd=$(pwd)"
java -version 2>&1 | sed 's/^/[dcemi] java: /'

echo "[dcemi] starting Xvfb on :99 ..."
rm -f /tmp/.X99-lock /tmp/.X11-unix/X99
Xvfb :99 -screen 0 1280x720x24 -ac -nolisten tcp &
export DISPLAY=:99
export LIBGL_ALWAYS_SOFTWARE=1
export GALLIUM_DRIVER=llvmpipe

# Wait until the X server socket is actually up before launching the client.
for _ in $(seq 1 40); do
    [ -S /tmp/.X11-unix/X99 ] && { echo "[dcemi] Xvfb ready on :99"; break; }
    sleep 0.25
done

# Run guard on a fresh game dir, Minecraft 1.20 shows an interactive AccessibilityOnboardingScreen (The Accessibility thing) before the title screen.
mkdir -p run
touch run/options.txt
if ! grep -qs '^onboardAccessibility:' run/options.txt; then
    echo 'onboardAccessibility:false' >> run/options.txt
    echo "[dcemi] set onboardAccessibility:false (skip first-run onboarding)"
fi
# Caps the main window framerate.
if ! grep -qs '^maxFps:' run/options.txt; then
    echo "maxFps:${DCEMI_MAXFPS:-10}" >> run/options.txt # Change if you want lower or higher
    echo "[dcemi] set maxFps:${DCEMI_MAXFPS:-10} (idle the headless render loop)"
fi

echo "[dcemi] launching gradle runClient ..."
exec ./gradlew runClient --no-daemon --console=plain