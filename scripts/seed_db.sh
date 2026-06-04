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
# Date helpers — all dates are relative to today so seeds stay fresh on re-run
# ---------------------------------------------------------------------------

# date_add <N> — YYYY-MM-DD that is N days from today (negative = past)
date_add() {
  local n="$1"
  if date -v +0d >/dev/null 2>&1; then
    # macOS/BSD date
    if (( n >= 0 )); then
      date -v "+${n}d" +%Y-%m-%d
    else
      date -v "${n}d" +%Y-%m-%d
    fi
  else
    # GNU date
    date -d "$n days" +%Y-%m-%d
  fi
}

# dt <N> <HH:MM:SS> — ISO-8601 datetime N days from today, UTC
dt() { echo "$(date_add "$1")T${2}Z"; }

# ---------------------------------------------------------------------------
# Users
# ---------------------------------------------------------------------------

declare -a USERS=(
  "alice|Alice|Wright|Password1!"
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
  email="${handle}@fz.dev"

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

# Dates are relative to today. Offsets chosen so each production's phase makes
# narrative sense: DISTRIBUTION/POST have past dates, PRODUCTION straddles now,
# PRE_PRODUCTION/DEVELOPMENT reach further into the future.
declare -a PRODUCTIONS=(
  "Midnight Signal|THRILLER|DEVELOPMENT|A detective uncovers a city-wide conspiracy hidden in plain radio frequencies.|$(date_add 27)|$(date_add 391)|75000000"
  "The Last Cartographer|DRAMA|PRE_PRODUCTION|An aging mapmaker embarks on one final expedition to chart uncharted territory.|$(date_add -64)|$(date_add 241)|40000000"
  "Orbit 7|SCI_FI|PRODUCTION|Seven astronauts must improvise survival on a station with failing AI support.|$(date_add -123)|$(date_add 194)|120000000"
  "Corner Store Blues|COMEDY|POST_PRODUCTION|A family-owned convenience store becomes the unlikely hub of neighbourhood drama.|$(date_add -368)|$(date_add -65)|18000000"
  "The Quiet Epidemic|DOCUMENTARY|DISTRIBUTION|An investigative look at the rise of chronic loneliness in modern cities.|$(date_add -826)|$(date_add -339)|5000000"
  "Hollow Earth|HORROR|DEVELOPMENT|A spelunking team discovers that the caves beneath their town are very much alive.|$(date_add 89)|$(date_add 453)|30000000"
  "Velocity|ACTION|PRE_PRODUCTION|A former rally driver is pulled into a cross-continental heist with no off ramp.|$(date_add -81)|$(date_add 269)|95000000"
  "Paper Crane|ANIMATION|PRODUCTION|A paper crane folded by a grieving child becomes sentient and searches for her.|$(date_add -140)|$(date_add 179)|55000000"
  "Landslide|DRAMA|PRE_PRODUCTION|A small mining town fights for its future after a disaster exposes corporate negligence.|$(date_add -50)|$(date_add 300)|62000000"
  "Neon Requiem|THRILLER|DEVELOPMENT|A jazz musician pieces together one catastrophic night from the memories of strangers.|$(date_add 119)|$(date_add 483)|48000000"
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
# Wire reporting links: Alice (owner) is the manager; others report to her.
# Production 0: Bob & Carol report to Alice -> Alice sees them as Reports,
# Bob & Carol see each other as Peers and Alice as their Manager.
# Production 1: David & Eva similarly report to Alice.
# ---------------------------------------------------------------------------

find_member_id() {
  local prod_id="$1" user_id="$2"
  curl -sf "$API_URL/productions/$prod_id/members" \
    -H "Authorization: Bearer $PRIMARY_TOKEN" \
    | jq -r --arg uid "$user_id" '.[] | select(.userId == $uid) | .id'
}

set_reports_to() {
  local prod_id="$1" member_id="$2" manager_member_id="$3"
  curl -sf -X PATCH "$API_URL/productions/$prod_id/members/$member_id" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $PRIMARY_TOKEN" \
    -d "{\"reportsToMemberId\":\"$manager_member_id\"}" >/dev/null
}

info "Wiring reports-to links..."
for prod_idx in 0 1; do
  prod_id="${PRODUCTION_IDS[$prod_idx]}"
  alice_member_id=$(find_member_id "$prod_id" "$ALICE_ID")
  for reporter_user_id in "$BOB_ID" "$CAROL_ID" "$DAVID_ID" "$EVA_ID"; do
    reporter_member_id=$(find_member_id "$prod_id" "$reporter_user_id")
    if [[ -n "$reporter_member_id" && "$reporter_member_id" != "null" ]]; then
      set_reports_to "$prod_id" "$reporter_member_id" "$alice_member_id"
    fi
  done
done

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
# NOTE: the first task (today-8) intentionally lands on the same day as the
# two events at offset -8 (see EVENTS array) so the schedule day-view has at
# least one day populated with both tasks and events.
declare -a TASKS=(
  "${PRODUCTION_IDS[0]}|Approve shooting script revision 4|Sign off on the radio-frequency reveal before tomorrow's location scout.|$(date_add -8)|${ALICE_ID}"
  "${PRODUCTION_IDS[0]}|Lock shooting script revision 4|Final pass on the radio-frequency reveal in act two before table read.|$(date_add -10)|${ALICE_ID}"
  "${PRODUCTION_IDS[0]}|Scout downtown rooftop locations|Need three viable rooftops with clear sightlines to the broadcast tower.|$(date_add 6)|${BOB_ID}"
  "${PRODUCTION_IDS[0]}|Build day-out-of-days schedule|Initial pass for stripboard, expecting 42 shooting days.|$(date_add -3)|${CAROL_ID}"
  "${PRODUCTION_IDS[0]}|Confirm composer availability|Reach out to shortlist of three composers for development meetings.|$(date_add 41)|${ALICE_ID}"
  "${PRODUCTION_IDS[1]}|Finalize cartographer character bible|Backstory, voice references, and expedition timeline.|$(date_add -17)|${ALICE_ID}"
  "${PRODUCTION_IDS[1]}|Source period-accurate cartography props|Brass instruments, parchment maps, leather satchels.|$(date_add 16)|${DAVID_ID}"
  "${PRODUCTION_IDS[1]}|Costume mood board for expedition crew|Layered wool, weather-beaten textures, era 1880-1900.|$(date_add 1)|${EVA_ID}"
  "${PRODUCTION_IDS[2]}|Review VFX vendor bids for station exteriors|Three bids in; need cost vs quality breakdown.|$(date_add -5)|${ALICE_ID}"
  "${PRODUCTION_IDS[5]}|Greenlight pitch deck for studio|Slides 1-12 done; need budget summary and comp titles.|$(date_add 8)|${ALICE_ID}"
  "${PRODUCTION_IDS[9]}|Option soundtrack jazz standards|List of 8 tracks, request quotes from rights holders.|$(date_add 27)|${BOB_ID}"
)

for entry in "${TASKS[@]}"; do
  IFS='|' read -r prod_id title description due assignee <<< "$entry"
  create_task "$prod_id" "$title" "$description" "$due" "$assignee"
  info "  Created task '$title' (assignee: ${assignee:0:8}...)"
done

# ---------------------------------------------------------------------------
# Schedule events (created by alice.wright; spread across productions and the
# next ~3 weeks so the schedule tab has something to render today and ahead).
# ---------------------------------------------------------------------------

info "Creating schedule events as alice.wright..."

create_event() {
  local prod_id="$1" title="$2" location="$3" starts_at="$4" ends_at="$5" kind="$6"

  local location_json="null"
  if [[ -n "$location" ]]; then
    location_json="\"$location\""
  fi

  curl -sf -X POST "$API_URL/schedule" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $PRIMARY_TOKEN" \
    -d "{
      \"productionId\": \"$prod_id\",
      \"title\": \"$title\",
      \"location\": $location_json,
      \"startsAt\": \"$starts_at\",
      \"endsAt\": \"$ends_at\",
      \"kind\": \"$kind\"
    }" >/dev/null
}

# productionId | title | location | startsAt | endsAt | kind
# Day offsets from today: -8 -7 -6 -3 -2 -1 0(today) +1 +4 +6
declare -a EVENTS=(
  "${PRODUCTION_IDS[0]}|Writers' room: act two beats|Studio C, Hollywood|$(dt -8 16:00:00)|$(dt -8 18:00:00)|MEETING"
  "${PRODUCTION_IDS[2]}|VFX vendor review|Conference Room A|$(dt -8 21:00:00)|$(dt -8 22:30:00)|REVIEW"
  "${PRODUCTION_IDS[0]}|Location scout: downtown rooftops|Downtown LA|$(dt -7 15:00:00)|$(dt -7 19:00:00)|SHOOT"
  "${PRODUCTION_IDS[1]}|Production design walkthrough|Stage 4|$(dt -7 17:00:00)|$(dt -7 18:30:00)|REVIEW"
  "${PRODUCTION_IDS[2]}|Station exterior plate shoot|Backlot Soundstage 2|$(dt -6 14:00:00)|$(dt -6 23:00:00)|SHOOT"
  "${PRODUCTION_IDS[1]}|Costume fitting: expedition crew|Wardrobe Dept|$(dt -6 18:00:00)|$(dt -6 20:00:00)|MEETING"
  "${PRODUCTION_IDS[0]}|Table read with cast|Studio B|$(dt -3 17:00:00)|$(dt -3 20:00:00)|MEETING"
  "${PRODUCTION_IDS[6]}|Stunt choreography rehearsal|Lot 12 Driveway|$(dt -2 15:00:00)|$(dt -2 19:00:00)|SHOOT"
  "${PRODUCTION_IDS[7]}|Animatic review: act one|Animation Suite|$(dt -1 16:00:00)|$(dt -1 18:00:00)|REVIEW"
  "${PRODUCTION_IDS[9]}|Jazz standards listening session|Music Room|$(dt -1 20:00:00)|$(dt -1 21:30:00)|MEETING"
  "${PRODUCTION_IDS[5]}|Greenlight pitch dry-run|Boardroom|$(dt 0 16:00:00)|$(dt 0 17:30:00)|REVIEW"
  "${PRODUCTION_IDS[2]}|Crew call: airlock sequence|Stage 7|$(dt 1 13:00:00)|$(dt 1 23:00:00)|SHOOT"
  "${PRODUCTION_IDS[1]}|Expedition location recce|Sierra Nevada Foothills|$(dt 4 15:00:00)|$(dt 4 22:00:00)|OTHER"
  "${PRODUCTION_IDS[0]}|Composer shortlist call|Zoom|$(dt 6 18:00:00)|$(dt 6 19:00:00)|MEETING"
)

for entry in "${EVENTS[@]}"; do
  IFS='|' read -r prod_id title location starts_at ends_at kind <<< "$entry"
  create_event "$prod_id" "$title" "$location" "$starts_at" "$ends_at" "$kind"
  info "  Created event '$title' ($kind) on ${starts_at%T*}"
done

info "Done. Summary:"
info "  Users:       5"
info "  Productions: ${#PRODUCTION_IDS[@]}"
info "  Tasks:       ${#TASKS[@]}"
info "  Events:      ${#EVENTS[@]}"
info "  Server:      $ORIGIN"
