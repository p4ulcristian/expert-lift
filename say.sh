#!/bin/bash

if [ -p /dev/stdin ]; then
    # Read JSON from stdin and extract text
    JSON=$(cat)
    TEXT=$(echo "$JSON" | jq -r '.content // .message // .text // .response // "done"' 2>/dev/null || echo "done")
else
    # Use command line argument if no stdin
    TEXT="$*"
fi

# Default to "done" if empty
TEXT=${TEXT:-"done"}

# Use macOS say command with Ava Premium voice at 180 words per minute
say -v "Ava (Premium)" -r 180 "$TEXT"