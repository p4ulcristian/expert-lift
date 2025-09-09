#!/bin/bash

# Check for at least one argument
if [ "$#" -lt 1 ]; then
  echo "Usage: $0 file1 [file2 ... fileN]"
  exit 1
fi

# Process each file
for file in "$@"; do
  if [ -f "$file" ]; then
    md5sum "$file" | awk '{ print $1 }' > "${file}.md5"
    echo "MD5 saved to ${file}.md5"
  else
    echo "Skipping: '$file' is not a valid file"
  fi
done