#!/bin/bash

# ============================================================================
# ExpertLift Database Seed Script
# ============================================================================
# Feltölti az adatbázist teszt adatokkal.
# Használat: ./seed-database.sh
# ============================================================================

set -e

# ANSI color codes
RED=$'\e[0;31m'
GREEN=$'\e[0;32m'
YELLOW=$'\e[0;33m'
BLUE=$'\e[0;34m'
NC=$'\e[0m' # No color

echo ""
echo "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo "${BLUE}        ExpertLift Database Seed Script                        ${NC}"
echo "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Load .env.local if it exists
if [ -f .env.local ]; then
  echo "${YELLOW}[INFO]${NC} Loading environment from .env.local..."
  export $(grep -v '^#' .env.local | grep -v '^$' | xargs)
else
  echo "${RED}[ERROR]${NC} .env.local not found!"
  echo "Please create .env.local with database connection settings."
  exit 1
fi

# Check required environment variables
if [ -z "$EXPERTLIFT_DB_HOST" ] || [ -z "$EXPERTLIFT_DB_NAME" ] || [ -z "$EXPERTLIFT_DB_USER" ]; then
  echo "${RED}[ERROR]${NC} Missing required database environment variables!"
  echo "Required: EXPERTLIFT_DB_HOST, EXPERTLIFT_DB_NAME, EXPERTLIFT_DB_USER"
  exit 1
fi

# Set defaults
DB_HOST="${EXPERTLIFT_DB_HOST}"
DB_PORT="${EXPERTLIFT_DB_PORT:-5432}"
DB_NAME="${EXPERTLIFT_DB_NAME}"
DB_USER="${EXPERTLIFT_DB_USER}"
DB_PASSWORD="${EXPERTLIFT_DB_PASSWORD}"

echo "${YELLOW}[INFO]${NC} Database: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo ""

# Warning about data deletion
echo "${RED}╔═══════════════════════════════════════════════════════════════╗${NC}"
echo "${RED}║  FIGYELEM! Ez a script TÖRLI az összes meglévő adatot!       ║${NC}"
echo "${RED}╚═══════════════════════════════════════════════════════════════╝${NC}"
echo ""
read -p "Biztosan folytatod? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
  echo "${YELLOW}[INFO]${NC} Seed megszakítva."
  exit 0
fi

echo ""
echo "${YELLOW}[INFO]${NC} Seed futtatása..."

# Run the SQL seed file
PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f seed-data.sql

if [ $? -eq 0 ]; then
  echo ""
  echo "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
  echo "${GREEN}  Seed sikeresen lefutott!                                      ${NC}"
  echo "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
  echo ""
  echo "${BLUE}Létrehozott adatok:${NC}"
  echo "  • 1 workspace (ExpertLift)"
  echo "  • 5 user:"
  echo "      - paul / Kf8#mNq2\$xL9 (superadmin)"
  echo "      - valentin / Vr4&pWz7!jH3 (admin)"
  echo "      - janos / Jn6@tKs5#bY8 (employee)"
  echo "      - peter / Pt9!cRm4&dF2 (employee)"
  echo "      - anna / An3#gVx8\$qE6 (employee)"
  echo "  • 5 address (Budapest, Debrecen, Szeged, Győr, Pécs)"
  echo "      - mindegyikhez 1-2 lift (JSONB)"
  echo "  • 5 material template"
  echo "  • 5 worksheet (draft, in_progress, completed)"
  echo "      - mindegyikhez hozzárendelt lift (elevator_identifier)"
  echo ""
  echo "${GREEN}Bejelentkezés:${NC}"
  echo "  Username: paul"
  echo "  Password: Kf8#mNq2\$xL9"
  echo ""
else
  echo ""
  echo "${RED}[ERROR]${NC} Seed sikertelen!"
  exit 1
fi
