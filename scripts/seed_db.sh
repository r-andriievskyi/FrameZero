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
  ACCESS_TOKENS+=("$token")
  info "  $email — token: ${token:0:20}..."
done

PRIMARY_TOKEN="${ACCESS_TOKENS[0]}"
info "Primary user token acquired."

# ---------------------------------------------------------------------------
# Productions (all created by user 0 / alice.wright)
# ---------------------------------------------------------------------------

info "Creating 10 productions..."

create_production() {
  local title="$1" genre="$2" phase="$3" logline="$4" start="$5" wrap="$6" budget="$7"

  curl -sf -X POST "$API_URL/productions" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $PRIMARY_TOKEN" \
    -d "{
      \"title\": \"$title\",
      \"genre\": \"$genre\",
      \"logline\": \"$logline\",
      \"phase\": \"$phase\",
      \"startDate\": \"$start\",
      \"wrapDate\": \"$wrap\",
      \"budgetCents\": $budget
    }"
}

PRODUCTION_IDS=()

declare -a PRODUCTIONS=(
  "Midnight Signal|THRILLER|DEVELOPMENT|A detective uncovers a city-wide conspiracy hidden in plain radio frequencies.|2026-06-01|2027-03-31|75000000"
  "The Last Cartographer|DRAMA|PRE_PRODUCTION|An aging mapmaker embarks on one final expedition to chart uncharted territory.|2026-05-15|2026-12-15|40000000"
  "Orbit 7|SCI_FI|PRODUCTION|Seven astronauts must improvise survival on a station with failing AI support.|2026-03-01|2026-11-30|120000000"
  "Corner Store Blues|COMEDY|POST_PRODUCTION|A family-owned convenience store becomes the unlikely hub of neighbourhood drama.|2025-09-01|2026-04-30|18000000"
  "The Quiet Epidemic|DOCUMENTARY|DISTRIBUTION|An investigative look at the rise of chronic loneliness in modern cities.|2025-01-01|2025-12-31|5000000"
  "Hollow Earth|HORROR|DEVELOPMENT|A spelunking team discovers that the caves beneath their town are very much alive.|2026-07-01|2027-06-30|30000000"
  "Velocity|ACTION|PRE_PRODUCTION|A former rally driver is pulled into a cross-continental heist with no off ramp.|2026-04-01|2027-01-31|95000000"
  "Paper Crane|ANIMATION|PRODUCTION|A paper crane folded by a grieving child becomes sentient and searches for her.|2026-02-01|2026-10-31|55000000"
  "Landslide|DRAMA|PRE_PRODUCTION|A small mining town fights for its future after a disaster exposes corporate negligence.|2026-05-01|2027-02-28|62000000"
  "Neon Requiem|THRILLER|DEVELOPMENT|A jazz musician pieces together one catastrophic night from the memories of strangers.|2026-08-01|2027-07-31|48000000"
)

for entry in "${PRODUCTIONS[@]}"; do
  IFS='|' read -r title genre phase logline start wrap budget <<< "$entry"
  response=$(create_production "$title" "$genre" "$phase" "$logline" "$start" "$wrap" "$budget")
  id=$(echo "$response" | jq -r '.id')
  PRODUCTION_IDS+=("$id")
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

info "Done. Summary:"
info "  Users:       5"
info "  Productions: ${#PRODUCTION_IDS[@]}"
info "  Server:      $ORIGIN"
