# Huki Awards 2026 - Features Documentation (EN)

## Introduction

**Huki Awards** is a custom voting platform for gaming awards, integrating with the IGDB API for
metadata search and using Discord for secure authentication. The system is designed to be intuitive
for users and robust for administrators.

## User Features

### 1. Voting Experience

- **Guided Flow:** Voting is divided into categories, presented one at a time to avoid information
  overload and maintain focus on each choice.
- **Flexible Navigation:** "Previous" and "Next" buttons allow users to review their choices and
  change their minds before proceeding.
- **Real-Time Search (IGDB):** Dynamic game search using the official IGDB database, ensuring titles
  and covers are always correct.
- **Persistent Progress:** The system automatically saves voting progress. If the user closes the
  browser, they can continue exactly where they left off when they return.
- **Vote Editing:** While the voting window defined by the administrators is open, the user has
  total freedom to return to the dashboard and change their votes.

### 2. Social Sharing

- **Choices Summary:** A dedicated screen presents a visual summary of all the games nominated by
  the user.
- **Image Generator (Share Card):** Feature that generates a custom PNG image directly on the
  server, including:
    - User name and avatar synced from Discord.
    - Organized list of categories and chosen games.
    - Huki Awards 2026 visual identity.
    - Voting timestamp.
- **Direct Download:** Integration with the browser API to download the summary instantly,
  facilitating sharing on social networks like Twitter, Instagram, or Discord.

## Admin Features

### 1. Control Panel (Admin Dashboard)

- **Overview Tab:** A visual dashboard showing total votes, unique voters, and participation
  percentage per category.
- **Category Management:** Complete interface to create, edit (name, description, weight/order), and
  remove categories from the award.
- **Vote Monitoring:** Real-time visualization of all registered votes, allowing for participation
  auditing.
- **Admin Management:** System for promoting regular users to administrators through their Discord
  username. Includes protection for "System Admins" who cannot be accidentally removed.
- **Audit Logs:** Every administrative action (category changes, settings updates, vote clearing) is
  logged with timestamp and author for transparency.
- **Vote as User:** Ability for admins to view the site as a specific user to help troubleshoot or
  vote on their behalf if necessary.

### 2. Event Settings

- **Phase Management:** Toggle between "NOMINATION" (users suggest games) and "VOTING" (users pick
  from nominees) phases.
- **Date Scheduling:** Precise definition of when voting starts and when it ends.
- **Visibility Control:** Option to show or hide the voting period for users in the site's top bar.
- **Manual Open/Close:** Master switch to open or close voting instantly, regardless of the
  scheduled time.

### 3. Results and Publicity

- **Dynamic Leaderboards:** Visualization of the Top 10 most voted games for each category with
  exact vote counts.
- **Ranking Export:** Generation of professional PNG images with the partial ranking (Top 10) for
  each category, ready for official publication.
- **Winner Card:** Generation of special art with golden borders and high-resolution covers to
  announce the winners of each category in an epic way.
- **CSV Export:** Download all raw voting data in CSV format for external analysis.

## Architecture and Technology

- **Frontend:** Developed with **Compose Multiplatform** targeting **WebAssembly (Wasm)** for high
  performance and visual fidelity in the browser.
- **Backend:** Built in **Kotlin** with **Ktor**, using **Exposed** for communication with the
  database (PostgreSQL/Supabase).
- **Security:** Authentication via **OAuth2 (Discord)** with session management through **JWT (JSON
  Web Tokens)**.
- **Infrastructure:** Database hosted on **Supabase** and integration with the **Twitch/IGDB API**
  for game metadata.
