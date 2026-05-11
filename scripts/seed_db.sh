#!/usr/bin/env bash
# Seed the FrameZero backend with 5 users and 10 productions.
# Requires: curl, jq
# Usage: ./scripts/seed_db.sh [SERVER_ORIGIN]
# Defaults to http://localhost:8080

set -euo pipefail

ORIGIN="${1:-http://localhost:8080}"
AUTH_URL="$ORIGIN/auth"
API_URL="$ORIGIN/api/v1"

info()  { echo "[seed] $*"; }
error() { echo "[seed] ERROR: $*" >&2; exit 1; }

command -v curl >/dev/null || error "curl not found"
command -v jq   >/dev/null || error "jq not found"

# ---------------------------------------------------------------------------
# Users
# ---------------------------------------------------------------------------

declare -a USERS=(
  "alice.wright|Alice|Wright|Password1!"
  "bob.hayes|Bob|Hayes|Password1!"
  "carol.kim|Carol|Kim|Password1!"
  "david.osei|David|Osei|Password1!"
  "eva.muller|Eva|Muller|Password1!"
)

declare -a ACCESS_TOKENS=()
declare -a USER_IDS=()

info "Registering 5 users..."
for entry in "${USERS[@]}"; do
  IFS='|' read -r handle first last pass <<< "$entry"
  email="${handle}@framezero.dev"

  response=$(curl -sf -X POST "$AUTH_URL/register" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$pass\",\"firstName\":\"$first\",\"lastName\":\"$last\"}" \
    || true)

  if [[ -z "$response" ]]; then
    info "  $email already exists — logging in"
    response=$(curl -sf -X POST "$AUTH_URL/login" \
      -H "Content-Type: application/json" \
      -d "{\"email\":\"$email\",\"password\":\"$pass\"}")
  fi

  token=$(echo "$response" | jq -r '.accessToken')
  user_id=$(echo "$response" | jq -r '.user.id')
  ACCESS_TOKENS+=("$token")
  USER_IDS+=("$user_id")
  info "  $email — id: $user_id"
done

PRIMARY_TOKEN="${ACCESS_TOKENS[0]}"
ALICE_ID="${USER_IDS[0]}"
BOB_ID="${USER_IDS[1]}"
CAROL_ID="${USER_IDS[2]}"
DAVID_ID="${USER_IDS[3]}"
EVA_ID="${USER_IDS[4]}"
info "Primary user token acquired."

# ---------------------------------------------------------------------------
# Productions (all created by user 0 / alice.wright)
# ---------------------------------------------------------------------------

info "Creating 10 productions..."

create_production() {
  local title="$1" genre="$2" logline="$3" start="$4" wrap="$5" budget="$6"

  curl -s -X POST "$API_URL/productions" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $PRIMARY_TOKEN" \
    -w "\n%{http_code}" \
    -d "{
      \"title\": \"$title\",
      \"genre\": \"$genre\",
      \"logline\": \"$logline\",
      \"startDate\": \"$start\",
      \"wrapDate\": \"$wrap\",
      \"budgetCents\": $budget
    }"
}

# The create endpoint always sets phase=IDEA. Advance to the target phase
# afterwards via POST /productions/{id}/phase (forward-only, skipping allowed).
advance_phase() {
  local prod_id="$1" target="$2"
  if [[ "$target" == "IDEA" ]]; then
    return 0
  fi
  curl -sf -X POST "$API_URL/productions/$prod_id/phase" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $PRIMARY_TOKEN" \
    -d "{\"phase\":\"$target\"}" >/dev/null
}

PRODUCTION_IDS=()

declare -a PRODUCTIONS=(
  "Midnight Signal|THRILLER|DEVELOPMENT|A detective uncovers a city-wide conspiracy hidden in plain radio frequencies.|2026-07-01|2027-06-30|75000000"
  "The Last Cartographer|DRAMA|PRE_PRODUCTION|An aging mapmaker embarks on one final expedition to chart uncharted territory.|2026-04-01|2027-01-31|40000000"
  "Orbit 7|SCI_FI|PRODUCTION|Seven astronauts must improvise survival on a station with failing AI support.|2026-02-01|2026-12-15|120000000"
  "Corner Store Blues|COMEDY|POST_PRODUCTION|A family-owned convenience store becomes the unlikely hub of neighbourhood drama.|2025-06-01|2026-03-31|18000000"
  "The Quiet Epidemic|DOCUMENTARY|DISTRIBUTION|An investigative look at the rise of chronic loneliness in modern cities.|2024-03-01|2025-06-30|5000000"
  "Hollow Earth|HORROR|DEVELOPMENT|A spelunking team discovers that the caves beneath their town are very much alive.|2026-09-01|2027-08-31|30000000"
  "Velocity|ACTION|PRE_PRODUCTION|A former rally driver is pulled into a cross-continental heist with no off ramp.|2026-03-15|2027-02-28|95000000"
  "Paper Crane|ANIMATION|PRODUCTION|A paper crane folded by a grieving child becomes sentient and searches for her.|2026-01-15|2026-11-30|55000000"
  "Landslide|DRAMA|PRE_PRODUCTION|A small mining town fights for its future after a disaster exposes corporate negligence.|2026-04-15|2027-03-31|62000000"
  "Neon Requiem|THRILLER|DEVELOPMENT|A jazz musician pieces together one catastrophic night from the memories of strangers.|2026-10-01|2027-09-30|48000000"
)

for entry in "${PRODUCTIONS[@]}"; do
  IFS='|' read -r title genre phase logline start wrap budget <<< "$entry"
  raw=$(create_production "$title" "$genre" "$logline" "$start" "$wrap" "$budget")
  status="${raw##*$'\n'}"
  body="${raw%$'\n'*}"
  if [[ "$status" != "200" && "$status" != "201" ]]; then
    error "Production create failed [$status] for '$title': $body"
  fi
  id=$(echo "$body" | jq -r '.id')
  PRODUCTION_IDS+=("$id")
  advance_phase "$id" "$phase"
  info "  Created '$title' ($phase) — id: $id"
done

# ---------------------------------------------------------------------------
# Add crew members from other users to a couple of productions
# ---------------------------------------------------------------------------

info "Adding crew members to first two productions..."

add_member() {
  local prod_id="$1" name="$2" role="$3" email="$4"
  curl -sf -X POST "$API_URL/productions/$prod_id/members" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $PRIMARY_TOKEN" \
    -d "{\"name\":\"$name\",\"role\":\"$role\",\"email\":\"$email\"}" >/dev/null
}

add_member "${PRODUCTION_IDS[0]}" "Bob Hayes"   "Director of Photography" "bob.hayes@framezero.dev"
add_member "${PRODUCTION_IDS[0]}" "Carol Kim"   "1st AD"                  "carol.kim@framezero.dev"
add_member "${PRODUCTION_IDS[1]}" "David Osei"  "Production Designer"     "david.osei@framezero.dev"
add_member "${PRODUCTION_IDS[1]}" "Eva Muller"  "Costume Designer"        "eva.muller@framezero.dev"

# ---------------------------------------------------------------------------
# Tasks (all created by alice.wright; mix of self-assigned and assigned to others)
# ---------------------------------------------------------------------------

info "Creating tasks as alice.wright..."

create_task() {
  local prod_id="$1" title="$2" description="$3" due="$4" assignee="$5"

  local assignee_json="null"
  if [[ -n "$assignee" ]]; then
    assignee_json="\"$assignee\""
  fi
  local due_json="null"
  if [[ -n "$due" ]]; then
    due_json="\"$due\""
  fi

  curl -sf -X POST "$API_URL/tasks" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $PRIMARY_TOKEN" \
    -d "{
      \"productionId\": \"$prod_id\",
      \"title\": \"$title\",
      \"description\": \"$description\",
      \"dueDate\": $due_json,
      \"assigneeUserId\": $assignee_json
    }" >/dev/null
}

# productionId | title | description | dueDate | assigneeUserId
declare -a TASKS=(
  "${PRODUCTION_IDS[0]}|Lock shooting script revision 4|Final pass on the radio-frequency reveal in act two before table read.|2026-05-25|${ALICE_ID}"
  "${PRODUCTION_IDS[0]}|Scout downtown rooftop locations|Need three viable rooftops with clear sightlines to the broadcast tower.|2026-06-10|${BOB_ID}"
  "${PRODUCTION_IDS[0]}|Build day-out-of-days schedule|Initial pass for stripboard, expecting 42 shooting days.|2026-06-01|${CAROL_ID}"
  "${PRODUCTION_IDS[0]}|Confirm composer availability|Reach out to shortlist of three composers for development meetings.|2026-07-15|${ALICE_ID}"
  "${PRODUCTION_IDS[1]}|Finalize cartographer character bible|Backstory, voice references, and expedition timeline.|2026-05-18|${ALICE_ID}"
  "${PRODUCTION_IDS[1]}|Source period-accurate cartography props|Brass instruments, parchment maps, leather satchels.|2026-06-20|${DAVID_ID}"
  "${PRODUCTION_IDS[1]}|Costume mood board for expedition crew|Layered wool, weather-beaten textures, era 1880-1900.|2026-06-05|${EVA_ID}"
  "${PRODUCTION_IDS[2]}|Review VFX vendor bids for station exteriors|Three bids in; need cost vs quality breakdown.|2026-05-30|${ALICE_ID}"
  "${PRODUCTION_IDS[5]}|Greenlight pitch deck for studio|Slides 1-12 done; need budget summary and comp titles.|2026-06-12|${ALICE_ID}"
  "${PRODUCTION_IDS[9]}|Option soundtrack jazz standards|List of 8 tracks, request quotes from rights holders.|2026-07-01|${BOB_ID}"
)

for entry in "${TASKS[@]}"; do
  IFS='|' read -r prod_id title description due assignee <<< "$entry"
  create_task "$prod_id" "$title" "$description" "$due" "$assignee"
  info "  Created task '$title' (assignee: ${assignee:0:8}...)"
done

info "Done. Summary:"
info "  Users:       5"
info "  Productions: ${#PRODUCTION_IDS[@]}"
info "  Tasks:       ${#TASKS[@]}"
info "  Server:      $ORIGIN"
