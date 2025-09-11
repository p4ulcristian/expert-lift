#!/bin/bash

if [ $# -eq 0 ]; then
  echo "Error: No FRONTEND_CONFIG(s) provided."
  echo "Usage: $0 <FRONTEND_CONFIG1> [FRONTEND_CONFIG2] ..."
  exit 1
fi

# Function to trim log file to last 1000 lines
trim_log() {
  if [ -f log.txt ]; then
    tail -n 1000 log.txt > log.txt.tmp && mv log.txt.tmp log.txt
  fi
}

# Function to clean up background processes
cleanup() {
  echo "Shutting down processes..."
  [ ! -z "$CLJ_PID" ] && kill $CLJ_PID 2>/dev/null
  [ ! -z "$CLJS_PID" ] && kill $CLJS_PID 2>/dev/null
  [ ! -z "$WEBPACK_PID" ] && kill $WEBPACK_PID 2>/dev/null
  [ ! -z "$MONITOR_PID" ] && kill $MONITOR_PID 2>/dev/null
  [ ! -z "$CHROME_SETUP_PID" ] && kill $CHROME_SETUP_PID 2>/dev/null
  trim_log
}

trap cleanup EXIT

# ANSI color codes
RED=$'\e[0;31m'
GREEN=$'\e[0;32m'
BLUE=$'\e[0;34m'
YELLOW=$'\e[0;33m'
NC=$'\e[0m' # No color

# Clear log file and add startup message
echo "=== Development server started at $(date) ===" > log.txt


echo "Running clj -X:zero..."
clj -X:zero 2>&1 | while IFS= read -r line; do 
  echo "${RED}[BACKEND]${NC} $line"
  echo "[BACKEND] $line" >> log.txt
done &
CLJ_PID=$!

SHADOW_BUILDS=$(printf "%s " "$@")
echo "Running shadow-cljs watch $SHADOW_BUILDS..."
npx shadow-cljs watch $SHADOW_BUILDS 2>&1 | while IFS= read -r line; do 
  echo "${GREEN}[SHADOW-CLJS]${NC} $line"
  echo "[SHADOW-CLJS] $line" >> log.txt
done &
CLJS_PID=$!

echo "Running webpack..."
npx webpack --config-name $SHADOW_BUILDS --watch 2>&1 | while IFS= read -r line; do 
  echo "${BLUE}[WEBPACK]${NC} $line"
  echo "[WEBPACK] $line" >> log.txt
done &
WEBPACK_PID=$!


# Wait for server readiness then open Chrome
(
  # Wait for server to be ready (status 200)
  echo "Waiting for server to be ready..."
  while true; do
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3002/health/ready 2>/dev/null)
    if [ "$HTTP_STATUS" = "200" ]; then
      break
    fi
    sleep 1
  done
  echo "Server is ready! Opening Chrome with paul tab..."
  
  # Start Chrome Dev with debugging using persistent isolated development profile
  /Applications/Google\ Chrome\ Dev.app/Contents/MacOS/Google\ Chrome\ Dev \
    --remote-debugging-port=9222 \
    --user-data-dir="$HOME/.expert-lift-dev/chrome-userdata" \
    --no-first-run \
    --no-default-browser-check \
    --disable-features=VizDisplayCompositor \
    "http://localhost:3002/" \
    >/dev/null 2>&1 &
  
  # Wait for Chrome's debugging port to be ready
  echo "Waiting for Chrome debugging port..."
  while ! curl -s http://localhost:9222/json >/dev/null 2>&1; do
    sleep 1
  done
  
  echo "Starting Chrome console streaming..."
  node console-stream.js 2>&1 | while IFS= read -r line; do 
    echo "${YELLOW}[CHROME]${NC} $line"
    echo "[CHROME] $line" >> log.txt
  done
) &
CHROME_SETUP_PID=$!


# Monitor backend process and exit if it fails
monitor_backend() {
  while kill -0 $CLJ_PID 2>/dev/null; do
    sleep 1
  done
  
  # Check if backend exited with error
  wait $CLJ_PID 2>/dev/null
  BACKEND_EXIT_CODE=$?
  
  if [ $BACKEND_EXIT_CODE -ne 0 ]; then
    echo -e "${RED}[BACKEND]${NC} Backend process exited with code $BACKEND_EXIT_CODE. Shutting down dev environment."
    cleanup
    exit $BACKEND_EXIT_CODE
  fi
}

monitor_backend &
MONITOR_PID=$!

# Wait for all processes to complete (or monitor to trigger exit)
wait