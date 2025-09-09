#!/bin/bash
export $(grep -v '^#' .env | xargs)
atlas "$@"