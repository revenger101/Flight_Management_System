# AirPort — React Frontend

## Overview

A modern single-page application for managing airports, airlines, flights, passengers, and bookings. Built with **React 19**, **Vite 7**, and a fully custom dark-themed UI. Consumes the Spring Boot REST API backend.

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| React | ^19.2.0 | Core UI framework |
| React DOM | ^19.2.0 | DOM rendering |
| React Router DOM | ^7.13.1 | Client-side SPA routing |
| Axios | ^1.13.6 | HTTP client for API calls |
| react-hot-toast | ^2.6.0 | Toast notification system |
| lucide-react | ^0.576.0 | SVG icon library |
| Vite | ^7.3.1 | Build tool & dev server |
| ESLint | ^9.39.1 | Code linting |

---

## Getting Started

### Prerequisites

- Node.js 18+
- npm or yarn

### Installation

```bash
cd aireport_front
npm install
```

### Development

```bash
npm run dev
```

App runs on **http://localhost:5173** (Vite default).

### Build for Production

```bash
npm run build
npm run preview
```

### Lint

```bash
npm run lint
```

---

## Project Structure

```
src/
├── main.jsx                 # Entry point (StrictMode, createRoot)
├── App.jsx                  # BrowserRouter + route definitions
├── App.css                  # App-level styles
├── index.css                # Global styles & CSS variables
├── api/
│   └── apiClient.js         # Axios instance (baseURL, interceptors)
├── components/
│   ├── Layout.jsx           # Page wrapper (Sidebar + Navbar + content)
│   ├── Navbar.jsx           # Top bar (title, date/time)
│   ├── Sidebar.jsx          # Left navigation (6 routes, active state)
│   └── StatCard.jsx         # Statistics display card
├── pages/
│   ├── Dashboard.jsx        # Overview with stats and recent bookings
│   ├── Airlines.jsx         # Airlines CRUD management
│   ├── Airports.jsx         # Airports CRUD management
│   ├── Flights.jsx          # Flights CRUD management
│   ├── Passengers.jsx       # Passengers CRUD management
│   └── Bookings.jsx         # Bookings Create/Read/Delete
└── services/
    ├── airlineService.js    # Airlines API calls
    ├── airportService.js    # Airports API calls
    ├── bookingService.js    # Bookings API calls
    ├── flightService.js     # Flights API calls
    └── passengerService.js  # Passengers API calls
```

---

## Routing

Router: **React Router DOM v7** with `BrowserRouter`

| Path | Page Component | Description |
|------|---------------|-------------|
| `/` | Dashboard | Statistics overview + recent bookings |
| `/airlines` | Airlines | Airline management (full CRUD) |
| `/airports` | Airports | Airport management (full CRUD) |
| `/flights` | Flights | Flight management (full CRUD) |
| `/passengers` | Passengers | Passenger management (full CRUD) |
| `/bookings` | Bookings | Booking management (Create, Read, Delete) |

All pages are wrapped in the `<Layout>` component which provides the sidebar, navbar, and toast container.

---

## API Client Configuration

**File:** `src/api/apiClient.js`

```javascript
axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' }
})
```

- **Base URL:** `http://localhost:8080/api`
- **Response Interceptor:** Logs errors to console (`API Error: ...`), rejects promise for component-level handling
- **No authentication** configured
- **No retry logic**

---

## Services Layer — API Consumption

Each service module exports an object with methods that call the backend REST API using the shared Axios client.

### airlineService.js

| Method | HTTP | Endpoint | Description |
|--------|------|----------|-------------|
| `getAll()` | GET | `/airlines` | Fetch all airlines |
| `getById(id)` | GET | `/airlines/{id}` | Fetch airline by ID |
| `create(data)` | POST | `/airlines` | Create new airline |
| `update(id, data)` | PUT | `/airlines/{id}` | Update existing airline |
| `delete(id)` | DELETE | `/airlines/{id}` | Delete airline |

### airportService.js

| Method | HTTP | Endpoint | Description |
|--------|------|----------|-------------|
| `getAll()` | GET | `/airports` | Fetch all airports |
| `getById(id)` | GET | `/airports/{id}` | Fetch airport by ID |
| `create(data)` | POST | `/airports` | Create new airport |
| `update(id, data)` | PUT | `/airports/{id}` | Update existing airport |
| `delete(id)` | DELETE | `/airports/{id}` | Delete airport |

### flightService.js

| Method | HTTP | Endpoint | Description |
|--------|------|----------|-------------|
| `getAll()` | GET | `/flights` | Fetch all flights |
| `getById(id)` | GET | `/flights/{id}` | Fetch flight by ID |
| `create(data)` | POST | `/flights` | Create new flight |
| `update(id, data)` | PUT | `/flights/{id}` | Update existing flight |
| `delete(id)` | DELETE | `/flights/{id}` | Delete flight |
| `addConnecting(id, connectedId)` | POST | `/flights/{id}/connecting/{connectedId}` | Link connecting flights |

### passengerService.js

| Method | HTTP | Endpoint | Description |
|--------|------|----------|-------------|
| `getAll()` | GET | `/passengers` | Fetch all passengers |
| `getById(id)` | GET | `/passengers/{id}` | Fetch passenger by ID |
| `create(data)` | POST | `/passengers` | Create new passenger |
| `update(id, data)` | PUT | `/passengers/{id}` | Update existing passenger |
| `delete(id)` | DELETE | `/passengers/{id}` | Delete passenger |

### bookingService.js

| Method | HTTP | Endpoint | Description |
|--------|------|----------|-------------|
| `getAll()` | GET | `/bookings` | Fetch all bookings |
| `getById(id)` | GET | `/bookings/{id}` | Fetch booking by ID |
| `getByPassenger(id)` | GET | `/bookings/passenger/{id}` | Fetch bookings by passenger *(not used in UI)* |
| `getByFlight(id)` | GET | `/bookings/flight/{id}` | Fetch bookings by flight *(not used in UI)* |
| `create(data)` | POST | `/bookings` | Create new booking |
| `update(id, data)` | PUT | `/bookings/{id}` | Update booking *(not exposed in UI)* |
| `delete(id)` | DELETE | `/bookings/{id}` | Delete booking |

---

## Components

### Layout (`Layout.jsx`)

**Props:** `{ children, title, subtitle }`

Page wrapper that assembles:
- `<Sidebar />` — fixed left navigation
- `<Navbar />` — top bar with title and clock
- `{children}` — page content
- `<Toaster />` — toast notification container (dark theme, top-right)

No hooks used — static structural component.

---

### Sidebar (`Sidebar.jsx`)

Fixed 260px-wide left navigation panel.

**Features:**
- Logo section with "AirPort" branding and plane emoji
- 6 navigation links using React Router's `<NavLink>`:
  - Dashboard (`/`) — LayoutDashboard icon
  - Flights (`/flights`) — Plane icon
  - Airports (`/airports`) — Building2 icon
  - Airlines (`/airlines`) — Briefcase icon
  - Passengers (`/passengers`) — Users icon
  - Bookings (`/bookings`) — BookOpen icon
- Active route highlighting with gold background and left accent bar
- System status indicator ("API Connected" with animated pulsing dot)
- Uses `end` prop on root route NavLink to prevent always-active state

No hooks used — static component.

---

### Navbar (`Navbar.jsx`)

Top header bar.

**Displays:**
- Page title (left side)
- Optional subtitle (left side, below title)
- Current date & time (right side) — formatted as `"Mon, Mar 10 · 10:30 AM"`

Uses `toLocaleTimeString()` and `toLocaleDateString()` for formatting.

---

### StatCard (`StatCard.jsx`)

**Props:** `{ icon, value, label, color, bgColor }`

A presentational card displaying:
- Icon (top, with customizable color/background)
- Numeric value (large text)
- Label (smaller muted text)

No hooks — pure presentational component.

---

## Pages — Detailed Breakdown

### Dashboard (`/`)

**React Hooks:**

| Hook | State Variable | Purpose |
|------|---------------|---------|
| `useState` | `stats` | Object holding counts: `{airlines, airports, flights, passengers, bookings}` |
| `useState` | `recentBookings` | Last 5 bookings (reversed from API response) |
| `useEffect` | — | Fetches all data on mount using `Promise.all` across 5 services |

**API Calls (on mount):**
```javascript
Promise.all([
  airlineService.getAll(),
  airportService.getAll(),
  flightService.getAll(),
  passengerService.getAll(),
  bookingService.getAll()
])
```

**UI Elements:**
- Welcome banner with gradient background
- 5 StatCards in auto-fit CSS grid:
  | Card | Icon | Color |
  |------|------|-------|
  | Airlines | Briefcase | Gold `rgb(201,168,76)` |
  | Airports | Building2 | Blue `rgb(59,130,246)` |
  | Flights | Plane | Purple |
  | Passengers | Users | Green `rgb(34,197,94)` |
  | Bookings | BookOpen | Orange `rgb(245,158,11)` |
- Recent Bookings table:
  | Column | Content |
  |--------|---------|
  | ID | Booking ID |
  | Kind | Booking kind text |
  | Type | Badge (BUSINESS = gold, other = blue) |
  | Passenger | Passenger ID |
  | Flight | Flight ID |
  | Date | Booking date |
- Empty state message when no bookings exist

---

### Airlines (`/airlines`)

**React Hooks:**

| Hook | State Variable | Initial Value | Purpose |
|------|---------------|---------------|---------|
| `useState` | `airlines` | `[]` | List of all airlines |
| `useState` | `loading` | `true` | Loading spinner toggle |
| `useState` | `showModal` | `false` | Create/Edit modal visibility |
| `useState` | `form` | `{name:'', shortName:'', logo:''}` | Form field values |
| `useState` | `editId` | `null` | ID of airline being edited, `null` for create mode |
| `useEffect` | — | — | Calls `load()` on component mount |

**API Calls:**
| Action | Service Method | Trigger |
|--------|---------------|---------|
| List all | `airlineService.getAll()` | On mount + after any mutation |
| Create | `airlineService.create(form)` | Form submit (when `editId === null`) |
| Update | `airlineService.update(editId, form)` | Form submit (when `editId !== null`) |
| Delete | `airlineService.delete(id)` | Delete button click (after confirm) |

**Table Columns:**
| Column | Content |
|--------|---------|
| ID | Airline ID |
| Logo | Image with error fallback to placeholder |
| Name | Airline name |
| Short Name | Airline code |
| Actions | Edit (Pencil icon) + Delete (Trash2 icon) buttons |

**Modal Form Fields:**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Name | text input | Yes | Airline full name |
| Short Name | text input | Yes | Airline code/abbreviation |
| Logo | URL input | No | Image URL with inline preview |

**Features:**
- Logo preview in form (shows image from URL input)
- Image error fallback to placeholder icon
- Confirmation dialog before delete
- Toast notifications for success/error/validation

---

### Airports (`/airports`)

**React Hooks:**

| Hook | State Variable | Initial Value | Purpose |
|------|---------------|---------------|---------|
| `useState` | `airports` | `[]` | List of all airports |
| `useState` | `airlines` | `[]` | List of airlines for dropdown |
| `useState` | `loading` | `true` | Loading spinner toggle |
| `useState` | `showModal` | `false` | Modal visibility |
| `useState` | `form` | `{name:'', shortName:'', country:'', fee:'', airlineId:''}` | Form values |
| `useState` | `editId` | `null` | Edit mode tracking |
| `useEffect` | — | — | Fetches airports + airlines on mount via `Promise.all` |

**API Calls:**
| Action | Service Method | Trigger |
|--------|---------------|---------|
| List all | `airportService.getAll()` + `airlineService.getAll()` | On mount (Promise.all) |
| Create | `airportService.create({...form, fee: parseFloat(form.fee)})` | Form submit |
| Update | `airportService.update(editId, {...form, fee: parseFloat(form.fee)})` | Form submit |
| Delete | `airportService.delete(id)` | Delete button (after confirm) |

**Table Columns:**
| Column | Content |
|--------|---------|
| ID | Airport ID |
| Name | Airport full name |
| Code | IATA code in blue badge |
| Country | Country name |
| Fee | Fee amount in gold with `$` prefix |
| Airline | Airline name (lookup by ID) or `—` |
| Actions | Edit + Delete buttons |

**Modal Form Fields:**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Name | text input | Yes | Airport name |
| Short Name | text input | Yes | IATA code (e.g., CDG, JFK) |
| Country | text input | No | Country name |
| Fee | number input | No | Parsed as `parseFloat` |
| Airline | select dropdown | No | Populated from airlines list, nullable |

**Features:**
- Two-column form layout (name + code, country + fee)
- Airline dropdown with dynamic options
- Fee formatting with dollar sign
- Code displayed as blue badge

---

### Flights (`/flights`)

**React Hooks:**

| Hook | State Variable | Initial Value | Purpose |
|------|---------------|---------------|---------|
| `useState` | `flights` | `[]` | List of all flights |
| `useState` | `airports` | `[]` | List of airports for dropdowns |
| `useState` | `loading` | `true` | Loading state |
| `useState` | `showModal` | `false` | Modal visibility |
| `useState` | `form` | `{time:'', miles:'', departureAirportId:'', arrivalAirportId:''}` | Form values |
| `useState` | `editId` | `null` | Edit mode tracking |
| `useEffect` | — | — | Fetches flights + airports on mount via `Promise.all` |

**API Calls:**
| Action | Service Method | Trigger |
|--------|---------------|---------|
| List all | `flightService.getAll()` + `airportService.getAll()` | On mount (Promise.all) |
| Create | `flightService.create({...form, miles: parseInt(form.miles)})` | Form submit |
| Update | `flightService.update(editId, {...form, miles: parseInt(form.miles)})` | Form submit |
| Delete | `flightService.delete(id)` | Delete button (after confirm) |

**Table Columns:**
| Column | Content |
|--------|---------|
| ID | Flight ID |
| Time | Departure time in monospace font |
| From | Departure airport IATA code (blue badge) |
| To | Arrival airport IATA code (gold badge) |
| Miles | Distance formatted with commas |
| Handlings | Count of flight handlings in green badge |
| Actions | Edit + Delete buttons |

**Modal Form Fields:**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Time | time input | Yes | 24-hour format (e.g., 14:30) |
| Miles | number input | Yes | Parsed as `parseInt` |
| Departure Airport | select dropdown | Yes | Populated from airports list |
| Arrival Airport | select dropdown | Yes | Populated from airports list |

**Features:**
- Airport name lookup with fallback to `#ID`
- Miles formatted with `toLocaleString()` (comma-separated)
- Flight handlings count badge
- Two-column form layout

---

### Passengers (`/passengers`)

**React Hooks:**

| Hook | State Variable | Initial Value | Purpose |
|------|---------------|---------------|---------|
| `useState` | `passengers` | `[]` | List of all passengers |
| `useState` | `loading` | `true` | Loading state |
| `useState` | `showModal` | `false` | Modal visibility |
| `useState` | `form` | `{name:'', cc:'', mileCard:'', status:'Bronze', milesAccount:{number:'', flightMiles:'', statusMiles:''}}` | Form values (nested) |
| `useState` | `editId` | `null` | Edit mode tracking |
| `useEffect` | — | — | Fetches passengers on mount |

**API Calls:**
| Action | Service Method | Trigger |
|--------|---------------|---------|
| List all | `passengerService.getAll()` | On mount |
| Create | `passengerService.create(form)` | Form submit (includes milesAccount) |
| Update | `passengerService.update(editId, form)` | Form submit (basic fields only) |
| Delete | `passengerService.delete(id)` | Delete button (after confirm) |

**Table Columns:**
| Column | Content |
|--------|---------|
| ID | Passenger ID |
| Name | Full name |
| CC | Credit card / ID number |
| Mile Card | Mile card number |
| Status | Loyalty tier badge (Gold = gold, Silver = blue, other = green) |
| Flight Miles | Miles count in gold text |
| Actions | Edit + Delete buttons |

**Modal Form Fields:**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Name | text input | Yes | Full name |
| CC | text input | No | Credit card / ID |
| Mile Card | text input | No | Mile card number |
| Status | select dropdown | Yes | Options: Bronze, Silver, Gold, Platinum |
| *Miles Account (create only):* | | | |
| Account Number | text input | No | Hidden during edit |
| Flight Miles | number input | No | Hidden during edit |
| Status Miles | number input | No | Hidden during edit |

**Features:**
- Nested `milesAccount` object in form state (create only)
- Miles account fields hidden when editing
- Status-based badge coloring
- Loyalty tier system (Bronze → Silver → Gold → Platinum)

---

### Bookings (`/bookings`)

**React Hooks:**

| Hook | State Variable | Initial Value | Purpose |
|------|---------------|---------------|---------|
| `useState` | `bookings` | `[]` | List of all bookings |
| `useState` | `passengers` | `[]` | List of passengers for dropdown |
| `useState` | `flights` | `[]` | List of flights for dropdown |
| `useState` | `loading` | `true` | Loading state |
| `useState` | `showModal` | `false` | Modal visibility |
| `useState` | `form` | `{kind:'One-way', date:'', type:'ECONOMIC', passengerId:'', flightId:''}` | Form values |
| `useEffect` | — | — | Fetches bookings + passengers + flights on mount via `Promise.all` |

**API Calls:**
| Action | Service Method | Trigger |
|--------|---------------|---------|
| List all | `bookingService.getAll()` + `passengerService.getAll()` + `flightService.getAll()` | On mount (Promise.all) |
| Create | `bookingService.create({...form, passengerId: parseInt(...), flightId: parseInt(...)})` | Form submit |
| Delete | `bookingService.delete(id)` | Delete button (after confirm) |

> **Note:** Bookings have **no edit functionality** in the UI. The update endpoint exists in the service but is not exposed.

**Table Columns:**
| Column | Content |
|--------|---------|
| ID | Booking ID |
| Passenger | Passenger name (lookup by ID, fallback to `Passenger #ID`) |
| Flight | Flight info showing miles (lookup by ID, fallback to `Flight #ID`) |
| Type | Badge (BUSINESS = gold, ECONOMIC = blue) |
| Kind | Booking kind text (One-way / Round-trip) |
| Date | Booking date |
| Actions | Delete button only (no edit) |

**Modal Form Fields:**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Kind | select dropdown | Yes | Options: One-way, Round-trip |
| Date | date picker | Yes | HTML date input |
| Type | select dropdown | Yes | Options: ECONOMIC, BUSINESS |
| Passenger | select dropdown | Yes | Populated from passengers list |
| Flight | select dropdown | Yes | Populated from flights list (shows miles) |

**Features:**
- Read + Create + Delete only (no update)
- Passenger and flight name lookups
- ID conversion to integers before POST
- Type badge coloring (BUSINESS = gold, ECONOMIC = blue)

---

## CRUD Operations Matrix

| Entity | Create | Read | Update | Delete |
|--------|--------|------|--------|--------|
| Airlines | ✅ Modal form | ✅ Table | ✅ Modal form | ✅ With confirmation |
| Airports | ✅ Modal form | ✅ Table | ✅ Modal form | ✅ With confirmation |
| Flights | ✅ Modal form | ✅ Table | ✅ Modal form | ✅ With confirmation |
| Passengers | ✅ Modal form (with miles) | ✅ Table | ✅ Modal form (no miles) | ✅ With confirmation |
| Bookings | ✅ Modal form | ✅ Table | ❌ Not available | ✅ With confirmation |

---

## React Hooks Usage Summary

### `useState` Patterns

| Pattern | Variables | Purpose |
|---------|----------|---------|
| Data arrays | `airlines`, `airports`, `flights`, `passengers`, `bookings` | Store entities fetched from API |
| Related data | `airlines` (in Airports page), `airports` (in Flights page) | Data for dropdown selects / name lookups |
| Loading state | `loading` | Toggle loading spinner |
| Modal control | `showModal` | Toggle modal visibility |
| Form state | `form` | Object holding all form field values, updated via spread operator |
| Edit tracking | `editId` | `null` = create mode, number = edit mode |
| Computed stats | `stats` | Dashboard aggregate counts |

### `useEffect` Patterns

| Page | Dependencies | Side Effect |
|------|-------------|-------------|
| Dashboard | `[]` (mount only) | `Promise.all` across all 5 services |
| Airlines | `[]` (mount only) | `airlineService.getAll()` |
| Airports | `[]` (mount only) | `Promise.all([airportService.getAll(), airlineService.getAll()])` |
| Flights | `[]` (mount only) | `Promise.all([flightService.getAll(), airportService.getAll()])` |
| Passengers | `[]` (mount only) | `passengerService.getAll()` |
| Bookings | `[]` (mount only) | `Promise.all([bookingService.getAll(), passengerService.getAll(), flightService.getAll()])` |

All effects run once on mount. Data is refreshed by calling the `load()` function manually after each mutation (create, update, delete).

---

## State Management

- **Approach:** Local component state only (`useState` hooks)
- **No global state management** (no Redux, Zustand, Context API)
- **Data flow:** Each page independently fetches and manages its own data
- **Re-fetching:** Manual `load()` call after every mutation
- **No caching** — fresh API call on every page mount

---

## UI Design System

### No Third-Party UI Framework

The entire UI is built with **100% custom CSS** — no Material UI, Tailwind, Shadcn, Chakra, or Bootstrap.

### Color Scheme (Dark Theme)

| Variable | Value | Usage |
|----------|-------|-------|
| `--bg-primary` | `#080c14` | Main background |
| `--bg-secondary` | `#0d1420` | Sidebar/navbar |
| `--bg-card` | `#111827` | Cards and modals |
| `--bg-card-hover` | `#1a2438` | Hover states |
| `--border` | `#1e2d45` | Primary borders |
| `--gold` | `#c9a84c` | Primary accent |
| `--gold-light` | `#e8c96a` | Light gold accent |
| `--blue-accent` | `#3b82f6` | Secondary accent |
| `--text-primary` | `#f0f4ff` | Main text |
| `--text-secondary` | `#8899bb` | Secondary text |
| `--success` | `#22c55e` | Success green |
| `--danger` | `#ef4444` | Danger red |
| `--warning` | `#f59e0b` | Warning orange |

### Typography

| Font | Weight | Usage |
|------|--------|-------|
| Syne | 400, 600, 700, 800 | Headings |
| DM Sans | 300, 400, 500 | Body text |
| System monospace | — | Codes and times |

### CSS Animations

| Animation | Duration | Usage |
|-----------|----------|-------|
| `fadeIn` | 0.15s | Modal overlay appearance |
| `slideUp` | 0.2s | Modal content slide-up |
| `pulse-dot` | 2s | API status indicator |
| `spin` | 0.8s | Loading spinner |

### Icons (lucide-react)

```
LayoutDashboard  Plane         Building2      Users
BookOpen         Briefcase     Wifi           Plus
Pencil           Trash2        X              ImageIcon
TrendingUp
```

---

## Error Handling

- **API level:** Axios interceptor logs errors to console
- **Component level:** `try-catch` blocks around API calls
- **User feedback:** `toast.error()` for user-facing error messages
- **Validation:** Form fields checked before submission (required fields)
- **Delete confirmation:** `window.confirm()` dialog before destructive actions

---

## Connection to Backend

The frontend expects the Spring Boot backend running at `http://localhost:8080`. The backend CORS configuration allows requests from `http://localhost:5173`.

**To run the full stack:**

1. Start the backend: `cd Flight_Management_System && ./mvnw spring-boot:run`
2. Start the frontend: `cd aireport_front && npm run dev`
3. Open **http://localhost:5173** in the browser
