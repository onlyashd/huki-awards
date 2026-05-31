# Manual Testing Guide

This guide provides a step-by-step walkthrough to verify that your Huki Awards instance is correctly
configured and all features are functioning as expected.

## 1. Authentication & Profile

- [ ] **Discord Login**: Click the "Login with Discord" button. You should be redirected to Discord,
  asked to authorize the app, and then returned to the Huki Awards site.
- [ ] **Session Persistence**: Refresh the page after logging in. Your profile (name and avatar)
  should still be visible in the top bar.
- [ ] **Logout**: Click your profile and select "Logout". You should be signed out and redirected to
  the home/login page.

## 2. Admin Dashboard (Requires Admin Role)

- [ ] **Access Control**: Log in with the account you added to the `admins` table in
  `initial_population.sql`. You should see the "Admin Dashboard" option in the profile menu.
- [ ] **Phase Management**:
    - Change the phase to **NOMINATION** and save. Verify that users can search and nominate games.
    - Change the phase to **VOTING** and save. Verify that users can only vote for existing
      nominees.
- [ ] **Category Management**:
    - **Create**: Add a new category (e.g., "Best Indie"). Verify it appears in the main list.
    - **Edit**: Change the name or description of a category.
    - **Delete**: Remove a category and confirm it disappears.
- [ ] **Global Stats**: Open the "Stats" tab in the Admin Dashboard. Verify that the total number of
  users and votes is updating correctly.
- [ ] **Audit Logs**: Perform an action (like changing the event name) and check the "Audit Logs"
  tab. Your action should be recorded with your username and timestamp.

## 3. Nomination Phase

- [ ] **Game Search**: In a category, type the name of a game (e.g., "Elden Ring"). Verify that
  suggestions from IGDB appear.
- [ ] **Submitting Nomination**: Select a game and click "Nominate". The game should now appear as
  your choice for that category.
- [ ] **Changing Nomination**: Select a different game. Verify that your previous nomination is
  replaced.

## 4. Voting Phase (Finals)

- [ ] **Nominee Display**: Ensure that only games nominated during the Nomination phase (or manually
  added by admins) are available for voting.
- [ ] **Casting a Vote**: Click "Vote" on a nominee. Verify the UI reflects your selection.

## 5. UI & Localization

- [ ] **Language Toggle**: Switch between English and Portuguese (if available). Verify that all
  strings in `Strings.kt` are correctly updated.
- [ ] **Responsive Design**: Resize your browser or open on a mobile device. The layout should
  adapt (cards should stack, menus should become hamburgers).

## 6. Environment & Security

- [ ] **CORS Settings**: If hosted on a domain, try to access the API from a different unauthorized
  domain. It should be blocked by CORS unless specified in `ALLOWED_HOSTS`.
- [ ] **Audit Trail**: Verify that even if a database entry is changed manually, the admin dashboard
  logs who changed what through the UI.

## Troubleshooting

If any of these tests fail:

1. Check the **Server Logs** for exceptions.
2. Verify that `DATABASE_URL` and Discord/IGDB credentials are correct in your environment
   variables.
3. Ensure the `initial_population.sql` script was executed correctly.
4. Check the browser's **Developer Tools (F12)** for console errors or blocked network requests.
