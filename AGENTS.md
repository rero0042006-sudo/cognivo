# Memory Moments — Project Rules

## Product

Memory Moments is a gamified family-recognition memory game.

Core flow:

Family photo
→ AI-generated game-style portrait
→ family profile
→ recognition question
→ player selects family member
→ XP / stars / combo
→ results

This is a cognitive-training game prototype, NOT a medical diagnostic tool.

## Current Platform

The existing project is a web prototype.

When migrating to Android:

* Native Android
* Kotlin
* Jetpack Compose
* Material 3
* MVVM
* Retrofit
* Room/DataStore
* Coil
* Android Photo Picker

Do NOT use WebView.

Do NOT recreate the website inside a WebView.

## AI

Use Google Gemini image generation through a backend.

NEVER put the Gemini API key inside the Android application.

Android:
→ Backend
→ Gemini
→ Backend
→ Android

Portrait generation must preserve recognizable characteristics of the uploaded person.

Always provide:

* Use AI portrait
* Regenerate
* Use original photo

If Gemini is unavailable, the game must still work using the original photo.

## Game

Game modes:

Easy:

* 3 family members
* name questions

Normal:

* 4–5 family members
* name + relationship questions

Challenge:

* 5–6 family members
* name + relationship + memory questions

Default game length:
10 rounds.

Correct:

* +10 XP
* +1 star/progress
* combo increases

Incorrect:

* gentle feedback
* allow retry
* no punishment

## UI

Visual identity:

Retro arcade + modern accessible mobile game.

Use:

* pixel-inspired headings
* arcade panels
* chunky borders
* stars
* XP bars
* combo counter
* level badges
* pixel-inspired decorative elements
* subtle animations

However:

Accessibility is more important than decoration.

Use:

* large buttons
* large text
* high contrast
* simple navigation
* minimum 48dp touch targets
* readable typography

Do not make the application look like a hospital dashboard.

Do not make it childish.

## Architecture

Use:

UI
→ ViewModel
→ Repository
→ Data/API

Keep business logic outside composables.

Do not put large amounts of logic in MainActivity.

Do not duplicate functionality.

Prefer reusable components.

## Development Rules

Before changing code:

1. Inspect the relevant existing files.
2. Reuse existing components and logic where possible.
3. Do not rewrite unrelated files.
4. Make the smallest change necessary.
5. Do not introduce new dependencies unless necessary.
6. Do not refactor unrelated code while implementing a feature.

After changes:

1. Build the project.
2. Fix compilation errors.
3. Fix obvious runtime issues.
4. Report what changed briefly.

## Important

Do NOT implement future features unless explicitly requested.

Do NOT add:

* authentication
* cloud database
* analytics
* social features
* multiplayer
* VR
* 3D models

unless explicitly requested.

Prioritize:

1. Working gameplay
2. Reliable photo handling
3. Gemini portrait generation
4. Accessibility
5. Retro game UI
6. Gamification
7. Polish
