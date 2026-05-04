# FrameZero API

Base URL: `/api/v1`  
All endpoints require `Authorization: Bearer <JWT>` unless noted.  
All timestamps are ISO-8601 UTC. All dates are `YYYY-MM-DD`. All money values are integer cents.

---

## Error envelope

All non-2xx responses use:
```json
{ "error": "CODE_IN_SCREAMING_SNAKE", "message": "Human readable", "fields": { "fieldName": "reason" } }
```

Codes: `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `VALIDATION_ERROR`, `INVALID_PHASE_TRANSITION`, `CONFLICT`, `INTERNAL`.

---

## Dashboard

### GET /dashboard
Returns aggregated dashboard payload (top 5 each).  
Optional header: `X-Timezone` (e.g. `America/New_York`). Defaults to UTC.

**Response 200**
```json
{
  "greeting": { "displayName": "Jane Doe", "activeProductionsCount": 3, "openTasksCount": 7 },
  "stats": { "activeProjects": 3, "openTasks": 7 },
  "myTasks": [ { "id": "uuid", "title": "...", "productionTitle": "...", "dueDate": "2026-05-10", "dueLabel": "Today", "status": "OPEN" } ],
  "productionStatus": [ { "id": "uuid", "title": "...", "phase": "PRODUCTION", "progressPercent": 42, "daysLeft": 120, "accentColorHint": "ORANGE", "updatedAt": "..." } ]
}
```

---

## Productions

### GET /productions
List accessible productions with cursor pagination.

**Query params:** `phase` (repeatable), `q` (search), `limit` (1–100, default 20), `cursor`  
**Response 200** `{ "items": [...], "nextCursor": "..." }`

Each item: `id, title, phase, progressPercent, daysLeft, accentColorHint, updatedAt`

---

### POST /productions
Create a new production.

**Body**
```json
{
  "title": "My Film",
  "genre": "DRAMA",
  "logline": "Optional, max 280 chars",
  "phase": "DEVELOPMENT",
  "startDate": "2026-01-01",
  "wrapDate": "2026-12-31",
  "budgetCents": 500000,
  "crew": [{ "name": "Jane Doe", "role": "Director", "email": "jane@example.com" }]
}
```

Genres: `DRAMA THRILLER SCI_FI COMEDY HORROR DOCUMENTARY ACTION ANIMATION OTHER`  
Phases: `DEVELOPMENT PRE_PRODUCTION PRODUCTION POST_PRODUCTION DISTRIBUTION`

**Response 201** `ProductionDetailDto` + `Location` header

---

### GET /productions/{id}
Get full production detail.

**Response 200** `ProductionDetailDto` (includes pipeline, key crew, progress)  
**404** if not found. **403** if not a member.

---

### PATCH /productions/{id}
Partial update (title, logline, startDate, wrapDate, budgetCents). Omit fields to skip.

**Response 200** `ProductionDetailDto`

---

### POST /productions/{id}/phase
Advance production to a new phase (forward only).

**Body** `{ "phase": "PRODUCTION" }`  
**Response 200** `ProductionDetailDto`  
**409** with code `INVALID_PHASE_TRANSITION` if attempting backwards transition.

---

### DELETE /productions/{id}
Soft-delete (owner only).

**Response 204**  
**403** if not owner.

---

## Crew (Production Members)

### GET /productions/{id}/members
**Response 200** `List<ProductionMemberDto>` — `id, userId, name, role, initials, avatarColorHex, addedAt`

### POST /productions/{id}/members
Add a crew member. If `email` matches an existing user, link by userId.

**Body** `{ "name": "...", "role": "...", "email": "optional" }`  
**Response 201** `ProductionMemberDto`

### PATCH /productions/{id}/members/{memberId}
Change role.

**Body** `{ "role": "1st AD" }`  
**Response 200** `ProductionMemberDto`

### DELETE /productions/{id}/members/{memberId}
Remove member. Cannot remove the owner.

**Response 204**  
**409** if attempting to remove the production owner.

---

## Tasks

### GET /tasks
List tasks with cursor pagination.

**Query params:** `assignee=me`, `status` (OPEN/DONE), `productionId`, `limit`, `cursor`  
Optional header: `X-Timezone`  
**Response 200** `{ "items": [...], "nextCursor": "..." }`

Each item: `id, title, productionTitle, dueDate, dueLabel, status`

---

### GET /tasks/{id}
**Response 200** `TaskDetailDto` — full detail including description, assigneeUserId, createdAt

### POST /tasks
**Body** `{ "productionId": "uuid", "title": "...", "description": "optional", "dueDate": "2026-05-20", "assigneeUserId": "uuid|null" }`  
**Response 201** `TaskDetailDto`

### PATCH /tasks/{id}
Partial update (title, description, dueDate, status, assigneeUserId).

**Response 200** `TaskDetailDto`

### DELETE /tasks/{id}
**Response 204**

---

## Schedule

### GET /schedule
Get events for a date range. Exactly one of the following `view` values is required.

**Query params:**
- `view=day&date=2026-05-04`
- `view=week&date=2026-05-04` (returns Mon–Sun containing the date)
- `view=month&date=2026-05` (returns full month)
- `productionId=uuid` (optional filter)

Optional header: `X-Timezone`

**Response 200**
```json
{
  "rangeStart": "2026-05-04",
  "rangeEnd": "2026-05-04",
  "days": [
    {
      "date": "2026-05-04",
      "events": [{ "id": "uuid", "title": "Table read", "location": null, "startsAt": "...", "endsAt": "...", "kind": "MEETING", "productionId": "uuid", "productionTitle": "My Film" }]
    }
  ]
}
```

### POST /schedule
**Body** `{ "productionId": "uuid", "title": "...", "location": "optional", "startsAt": "...", "endsAt": "...", "kind": "SHOOT|MEETING|REVIEW|OTHER" }`  
**Response 201** `ScheduleEventDto`

### PATCH /schedule/{id}
Partial update (title, location, startsAt, endsAt, kind).

**Response 200** `ScheduleEventDto`

### DELETE /schedule/{id}
**Response 204**

---

## Notifications

### GET /notifications
Paginated, newest first.

**Query params:** `limit`, `cursor`  
**Response 200** `{ "items": [...], "unreadCount": 3, "nextCursor": "..." }`

Each item: `id, title, body, readAt, createdAt`

### POST /notifications/read
Mark notifications as read.

**Body (specific ids):** `{ "ids": ["uuid1", "uuid2"] }`  
**Body (all):** `{ "all": true }`  
**Response 204**
