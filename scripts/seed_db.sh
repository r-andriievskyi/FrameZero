#!/usr/bin/env bash

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
  "frank.lozano|Frank|Lozano|Password1!"
  "grace.tan|Grace|Tan|Password1!"
  "henry.okafor|Henry|Okafor|Password1!"
  "iris.novak|Iris|Novak|Password1!"
  "jack.romano|Jack|Romano|Password1!"
  "kara.diaz|Kara|Diaz|Password1!"
  "liam.brennan|Liam|Brennan|Password1!"
  "maya.patel|Maya|Patel|Password1!"
  "noah.kim|Noah|Kim|Password1!"
  "omar.haddad|Omar|Haddad|Password1!"
  "priya.sharma|Priya|Sharma|Password1!"
  "quinn.reyes|Quinn|Reyes|Password1!"
  "ruby.chen|Ruby|Chen|Password1!"
  "sam.walsh|Sam|Walsh|Password1!"
  "tara.willis|Tara|Willis|Password1!"
)

declare -a ACCESS_TOKENS=()
declare -a USER_IDS=()

# The /auth endpoints are rate limited (10 req/min per client). With 20 users
# doing register+login we exceed that, so route every auth call through this
# helper, which retries with backoff on HTTP 429. Echoes the response body and
# sets AUTH_HTTP_CODE to the final status.
# Split a `curl -w '\n%{http_code}'` response into body / status. Uses tail+sed
# rather than ${var##*$'\n'} parameter expansion, which is unreliable on the
# bash 3.2 that ships with macOS.
http_code() { printf '%s' "$1" | tail -n1; }
http_body() { printf '%s' "$1" | sed '$d'; }

# POST to an auth endpoint, retrying with backoff on HTTP 429 (the server allows
# 10 auth req/min). Echoes the raw "<body>\n<status>" — the caller parses it in
# the parent shell, because this runs in a $() subshell and any variable set
# here would not survive. Progress notes go to stderr so they don't pollute it.
auth_post() {
  local url="$1" payload="$2"
  local attempt raw
  for attempt in $(seq 1 10); do
    raw=$(curl -s -w '\n%{http_code}' -X POST "$url" \
      -H "Content-Type: application/json" -d "$payload")
    if [[ "$(http_code "$raw")" == "429" ]]; then
      info "  rate limited — waiting 20s (attempt $attempt)…" >&2
      sleep 20
      continue
    fi
    break
  done
  printf '%s' "$raw"
}

info "Registering ${#USERS[@]} users..."
for entry in "${USERS[@]}"; do
  IFS='|' read -r handle first last pass <<< "$entry"
  email="${handle}@fz.dev"

  raw=$(auth_post "$AUTH_URL/register" \
    "{\"email\":\"$email\",\"password\":\"$pass\",\"firstName\":\"$first\",\"lastName\":\"$last\"}")
  code=$(http_code "$raw")

  if [[ "$code" != "200" && "$code" != "201" ]]; then
    info "  $email exists (register $code) — logging in"
    raw=$(auth_post "$AUTH_URL/login" \
      "{\"email\":\"$email\",\"password\":\"$pass\"}")
    code=$(http_code "$raw")
    if [[ "$code" != "200" ]]; then
      error "Login failed for $email [$code]: $(http_body "$raw")"
    fi
  fi

  response=$(http_body "$raw")
  token=$(echo "$response" | jq -r '.accessToken')
  user_id=$(echo "$response" | jq -r '.user.id')
  if [[ -z "$user_id" || "$user_id" == "null" ]]; then
    error "Could not obtain user id for $email — response: $response"
  fi
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
FRANK_ID="${USER_IDS[5]}"
GRACE_ID="${USER_IDS[6]}"
HENRY_ID="${USER_IDS[7]}"
IRIS_ID="${USER_IDS[8]}"
JACK_ID="${USER_IDS[9]}"
KARA_ID="${USER_IDS[10]}"
LIAM_ID="${USER_IDS[11]}"
MAYA_ID="${USER_IDS[12]}"
NOAH_ID="${USER_IDS[13]}"
OMAR_ID="${USER_IDS[14]}"
PRIYA_ID="${USER_IDS[15]}"
QUINN_ID="${USER_IDS[16]}"
RUBY_ID="${USER_IDS[17]}"
SAM_ID="${USER_IDS[18]}"
TARA_ID="${USER_IDS[19]}"
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
  "Cold Harbor|THRILLER|DEVELOPMENT|A small-town cop reopens a hit-and-run the rest of her department wants buried.|$(date_add 27)|$(date_add 391)|32000000"
  "Copper Country|DRAMA|PRE_PRODUCTION|A union organizer goes back to the mining town that ran him out twenty years ago.|$(date_add -64)|$(date_add 241)|22000000"
  "Halsey Station|SCI_FI|PRODUCTION|The skeleton crew of an orbital fuel depot loses contact with Earth for six days.|$(date_add -123)|$(date_add 194)|110000000"
  "Bob's Big Retirement|COMEDY|POST_PRODUCTION|A retirement party falls apart when the guest of honor's old band shows up uninvited.|$(date_add -200)|$(date_add -20)|15000000"
  "Last Orders|DOCUMENTARY|DISTRIBUTION|A year inside a sixty-year-old neighborhood pub in its final months before demolition.|$(date_add -300)|$(date_add -35)|1200000"
  "The Tenant Below|HORROR|DEVELOPMENT|New parents start hearing the neighbor downstairs mimic their newborn.|$(date_add 89)|$(date_add 453)|9000000"
  "Hard Left|ACTION|PRE_PRODUCTION|A rideshare driver is blackmailed into running a fugitive across three states overnight.|$(date_add -81)|$(date_add 269)|70000000"
  "Marbles|ANIMATION|PRODUCTION|A boy and his late grandfather's marble collection set off on a backyard odyssey.|$(date_add -140)|$(date_add 179)|60000000"
  "The Pelham Diner|DRAMA|PRE_PRODUCTION|Three siblings clash over whether to sell the diner that raised them.|$(date_add -50)|$(date_add 300)|8000000"
  "The Night Shift|THRILLER|DEVELOPMENT|A hospital janitor reconstructs a patient's disappearance from cameras he isn't meant to watch.|$(date_add 119)|$(date_add 483)|24000000"
)

for entry in "${PRODUCTIONS[@]}"; do
  IFS='|' read -r title genre phase logline start wrap budget <<< "$entry"
  raw=$(create_production "$title" "$genre" "$logline" "$start" "$wrap" "$budget")
  status="$(http_code "$raw")"
  body="$(http_body "$raw")"
  if [[ "$status" != "200" && "$status" != "201" ]]; then
    error "Production create failed [$status] for '$title': $body"
  fi
  id=$(echo "$body" | jq -r '.id')
  PRODUCTION_IDS+=("$id")
  advance_phase "$id" "$phase"
  info "  Created '$title' ($phase) — id: $id"
done

# ---------------------------------------------------------------------------
# Crew members. Alice (owner) is auto-added to every production she creates;
# here we attach the other registered users to a few productions with real
# department roles. Emails match the registered accounts so each member links
# to a real user, which drives the reports-to org chart below.
# ---------------------------------------------------------------------------

info "Adding crew members across productions..."

add_member() {
  local prod_id="$1" name="$2" role="$3" email="$4"
  curl -sf -X POST "$API_URL/productions/$prod_id/members" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $PRIMARY_TOKEN" \
    -d "{\"name\":\"$name\",\"role\":\"$role\",\"email\":\"$email\"}" >/dev/null
}

# Cold Harbor (idx0) — camera, cutting room & cast
add_member "${PRODUCTION_IDS[0]}" "Bob Hayes"    "Director of Photography" "bob.hayes@fz.dev"
add_member "${PRODUCTION_IDS[0]}" "Carol Kim"    "1st AD"                  "carol.kim@fz.dev"
add_member "${PRODUCTION_IDS[0]}" "Frank Lozano" "Gaffer"                  "frank.lozano@fz.dev"
add_member "${PRODUCTION_IDS[0]}" "Grace Tan"    "Editor"                  "grace.tan@fz.dev"
add_member "${PRODUCTION_IDS[0]}" "Tara Willis"  "Casting Director"        "tara.willis@fz.dev"
add_member "${PRODUCTION_IDS[0]}" "Kara Diaz"    "Lead Actor"              "kara.diaz@fz.dev"

# Copper Country (idx1) — art, wardrobe, sound & cast
add_member "${PRODUCTION_IDS[1]}" "David Osei"    "Production Designer"    "david.osei@fz.dev"
add_member "${PRODUCTION_IDS[1]}" "Eva Muller"    "Costume Designer"       "eva.muller@fz.dev"
add_member "${PRODUCTION_IDS[1]}" "Henry Okafor"  "Production Sound Mixer" "henry.okafor@fz.dev"
add_member "${PRODUCTION_IDS[1]}" "Ruby Chen"     "Makeup Department Head" "ruby.chen@fz.dev"
add_member "${PRODUCTION_IDS[1]}" "Liam Brennan"  "Lead Actor"             "liam.brennan@fz.dev"

# Halsey Station (idx2) — vfx-heavy stage shoot
add_member "${PRODUCTION_IDS[2]}" "Iris Novak"    "VFX Supervisor"         "iris.novak@fz.dev"
add_member "${PRODUCTION_IDS[2]}" "Jack Romano"   "1st AD"                 "jack.romano@fz.dev"
add_member "${PRODUCTION_IDS[2]}" "Carol Kim"     "Line Producer"          "carol.kim@fz.dev"
add_member "${PRODUCTION_IDS[2]}" "Omar Haddad"   "VFX Compositor"         "omar.haddad@fz.dev"
add_member "${PRODUCTION_IDS[2]}" "Sam Walsh"     "Boom Operator"          "sam.walsh@fz.dev"
add_member "${PRODUCTION_IDS[2]}" "Priya Sharma"  "Lead Actor"             "priya.sharma@fz.dev"

# Hard Left (idx6) — stunt-heavy action prep
add_member "${PRODUCTION_IDS[6]}" "Maya Patel"    "Stunt Coordinator"      "maya.patel@fz.dev"
add_member "${PRODUCTION_IDS[6]}" "Noah Kim"      "Stunt Performer"        "noah.kim@fz.dev"
add_member "${PRODUCTION_IDS[6]}" "Quinn Reyes"   "Stunt Performer"        "quinn.reyes@fz.dev"
add_member "${PRODUCTION_IDS[6]}" "Kara Diaz"     "Lead Actor"             "kara.diaz@fz.dev"

# ---------------------------------------------------------------------------
# Wire reporting links. Alice (owner) manages each department head; on Cold
# Harbor the chain goes a level deeper (Alice -> Bob -> Frank) so the org chart
# shows a Manager, Peers and Reports for a mid-level crew member.
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

# prod_idx | reporter user id | manager user id
declare -a REPORTS=(
  "0|$BOB_ID|$ALICE_ID"
  "0|$CAROL_ID|$ALICE_ID"
  "0|$GRACE_ID|$ALICE_ID"
  "0|$FRANK_ID|$BOB_ID"
  "0|$TARA_ID|$ALICE_ID"
  "0|$KARA_ID|$ALICE_ID"
  "1|$DAVID_ID|$ALICE_ID"
  "1|$EVA_ID|$ALICE_ID"
  "1|$HENRY_ID|$ALICE_ID"
  "1|$RUBY_ID|$ALICE_ID"
  "1|$LIAM_ID|$ALICE_ID"
  "2|$IRIS_ID|$ALICE_ID"
  "2|$JACK_ID|$ALICE_ID"
  "2|$CAROL_ID|$ALICE_ID"
  "2|$OMAR_ID|$IRIS_ID"
  "2|$SAM_ID|$ALICE_ID"
  "2|$PRIYA_ID|$ALICE_ID"
  "6|$MAYA_ID|$ALICE_ID"
  "6|$NOAH_ID|$MAYA_ID"
  "6|$QUINN_ID|$MAYA_ID"
  "6|$KARA_ID|$ALICE_ID"
)

info "Wiring reports-to links..."
for entry in "${REPORTS[@]}"; do
  IFS='|' read -r prod_idx reporter_uid manager_uid <<< "$entry"
  prod_id="${PRODUCTION_IDS[$prod_idx]}"
  reporter_member_id=$(find_member_id "$prod_id" "$reporter_uid")
  manager_member_id=$(find_member_id "$prod_id" "$manager_uid")
  if [[ -n "$reporter_member_id" && "$reporter_member_id" != "null" \
     && -n "$manager_member_id" && "$manager_member_id" != "null" ]]; then
    set_reports_to "$prod_id" "$reporter_member_id" "$manager_member_id"
  fi
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
# NOTE: two tasks land on today (offset 0) and two more on offset -8, each
# alongside events on the same days (see EVENTS), so the schedule day-view has
# days populated with both tasks and events — including the day the script runs.
declare -a TASKS=(
  "${PRODUCTION_IDS[0]}|Approve script rev 4|Sign off before Thursday's scout — mainly the act-two interrogation.|$(date_add -8)|${ALICE_ID}"
  "${PRODUCTION_IDS[0]}|Lock script rev 4|Final pass on act two before the table read.|$(date_add -10)|${ALICE_ID}"
  "${PRODUCTION_IDS[0]}|Scout rooftop locations|Three rooftops downtown with a clear line to the courthouse.|$(date_add 6)|${BOB_ID}"
  "${PRODUCTION_IDS[0]}|Build day-out-of-days|First stripboard pass — figure on 38 shooting days.|$(date_add -3)|${CAROL_ID}"
  "${PRODUCTION_IDS[0]}|Confirm composer availability|Email the three names from the shortlist for a development call.|$(date_add 41)|${ALICE_ID}"
  "${PRODUCTION_IDS[1]}|Finalize lead character bible|Backstory, voice notes, and the twenty-year timeline.|$(date_add -17)|${ALICE_ID}"
  "${PRODUCTION_IDS[1]}|Source period props|Lunch pails, hard hats, union banners — early '80s.|$(date_add 16)|${DAVID_ID}"
  "${PRODUCTION_IDS[1]}|Costume mood board|Worn denim, flannel, steel-toe boots — mining town, 1983.|$(date_add 1)|${EVA_ID}"
  "${PRODUCTION_IDS[2]}|Review VFX bids for station exteriors|Three vendors in — cost-vs-quality breakdown by Friday.|$(date_add -5)|${ALICE_ID}"
  "${PRODUCTION_IDS[2]}|Award VFX exterior package|Confirm the vendor and send the award letter today.|$(date_add 0)|${ALICE_ID}"
  "${PRODUCTION_IDS[2]}|First comp pass on airlock plates|Rough the exterior plates against the previs so editorial can cut.|$(date_add 4)|${OMAR_ID}"
  "${PRODUCTION_IDS[0]}|Send callback list for the lead|Three names for the detective; book the room for next week.|$(date_add 3)|${TARA_ID}"
  "${PRODUCTION_IDS[6]}|Block the highway flip|Walk the sequence with the team and confirm the rig load.|$(date_add 0)|${MAYA_ID}"
  "${PRODUCTION_IDS[6]}|Wire rig fitting|Harness and pads check for the rooftop drop — bring spares.|$(date_add 7)|${NOAH_ID}"
  "${PRODUCTION_IDS[5]}|Finish studio pitch deck|Slides 1-12 done; add the budget summary and comps before the dry-run.|$(date_add 0)|${ALICE_ID}"
  "${PRODUCTION_IDS[9]}|Clear archival footage rights|Eight news clips from '94 — get quotes from the rights holders.|$(date_add 27)|${BOB_ID}"
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
# Day offsets from today: -8 -8 -7 -7 -6 -6 -3 -2 -1 -1 0 0(today) +1 +4 +6
declare -a EVENTS=(
  "${PRODUCTION_IDS[0]}|Writers' room — act two|Studio C, Hollywood|$(dt -8 16:00:00)|$(dt -8 18:00:00)|MEETING"
  "${PRODUCTION_IDS[2]}|VFX vendor review|Conference Room A|$(dt -8 21:00:00)|$(dt -8 22:30:00)|REVIEW"
  "${PRODUCTION_IDS[0]}|Rooftop location scout|Downtown LA|$(dt -7 15:00:00)|$(dt -7 19:00:00)|SHOOT"
  "${PRODUCTION_IDS[1]}|Production design walkthrough|Stage 4|$(dt -7 17:00:00)|$(dt -7 18:30:00)|REVIEW"
  "${PRODUCTION_IDS[2]}|Station exterior plate shoot|Backlot Soundstage 2|$(dt -6 14:00:00)|$(dt -6 23:00:00)|SHOOT"
  "${PRODUCTION_IDS[1]}|Costume fitting — leads|Wardrobe Dept|$(dt -6 18:00:00)|$(dt -6 20:00:00)|MEETING"
  "${PRODUCTION_IDS[0]}|Table read with cast|Studio B|$(dt -3 17:00:00)|$(dt -3 20:00:00)|MEETING"
  "${PRODUCTION_IDS[6]}|Stunt choreography rehearsal|Lot 12 Driveway|$(dt -2 15:00:00)|$(dt -2 19:00:00)|SHOOT"
  "${PRODUCTION_IDS[7]}|Animatic review — act one|Animation Suite|$(dt -1 16:00:00)|$(dt -1 18:00:00)|REVIEW"
  "${PRODUCTION_IDS[9]}|Sound design spotting session|Music Room|$(dt -1 20:00:00)|$(dt -1 21:30:00)|MEETING"
  "${PRODUCTION_IDS[5]}|Greenlight pitch dry-run|Boardroom|$(dt 0 16:00:00)|$(dt 0 17:30:00)|REVIEW"
  "${PRODUCTION_IDS[2]}|Station dailies review|Screening Room 1|$(dt 0 19:00:00)|$(dt 0 20:00:00)|REVIEW"
  "${PRODUCTION_IDS[2]}|Crew call — airlock sequence|Stage 7|$(dt 1 13:00:00)|$(dt 1 23:00:00)|SHOOT"
  "${PRODUCTION_IDS[1]}|Mining town location recce|Bisbee, AZ|$(dt 4 15:00:00)|$(dt 4 22:00:00)|OTHER"
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
