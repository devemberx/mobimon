# In-Car Dog Companion — Design Overview

This document describes the overall experience and visual direction of the current concept. Use the [interactive prototype](ui/home.html) to explore the screens and interactions.

## Product Direction

A small dog accompanies the user in a vehicle interface. It walks around, pauses when approached, and responds through a speech bubble. The experience should feel calm, personal, and easy to understand during parked use.

For the hackathon, prioritize a coherent UI and a clear demonstration of the companion experience. A dedicated backend is outside the current scope. Real AI and vehicle integration remain separate implementation work beyond the HTML prototype.

## Design Principles

- **Keep the dog central.** Supporting information should stay secondary to the companion.
- **Keep navigation predictable.** Related screens share one location and a clear path back.
- **Separate the conversation roles.** The user's input belongs at the bottom; only the dog's response appears beside the dog.
- **Start simple.** Add information and functionality as the core experience is reviewed.

## Screens and Navigation

| Screen | Purpose |
|---|---|
| Companion home | A quiet walking scene and the main entry point for conversation. |
| Vehicle home | A simplified vehicle OS interface where the same dog remains present. |
| Character information | The dog's appearance, growth, mood, and shared experiences. |
| Quest information | Small care activities, progress, and completed experiences. |
| Character selection | Appearance choices applied across both home screens. |
| Settings | Companion visibility on the vehicle home and reduced motion. |

The hamburger menu and all four detail views use a single **left-side drawer**. Selecting an item replaces the menu content without moving or resizing the drawer.

The back arrow returns to the menu. Escape moves back one level, then closes the menu. The close button or a tap outside dismisses the entire drawer. Returning to a previous view preserves selections, settings, and quest progress.

Both home screens use the same navigation model. The prototype's shortcuts above the screen are review controls, separate from the intended app interface.

## Companion and Conversation

The dog walks with small, grounded steps. Tapping it pauses movement and opens a composer at the bottom of the screen. The user's latest message appears above the composer, while the dog's reply appears in a bubble anchored near its head.

Closing the conversation resumes walking. The last completed question and answer remain together when the conversation is reopened or the user switches home screens. Interrupting a pending response preserves the user's input for retrying.

Opening the menu or a detail view also pauses walking. Touch, keyboard navigation, and reduced motion should support the same understandable interaction flow.

## Visual Direction

Use a warm cream background, muted green hills, and a soft beige path for the companion home. The dog has a simple silhouette, rounded legs, and restrained movement. The interface currently has no product logo or assigned character name.

The vehicle home uses a quieter dark background with simple navigation, media, and climate elements. These elements establish the feeling of a vehicle interface while keeping the dog easy to notice. Exact reproduction of an Android Automotive OS launcher is unnecessary at this stage.

## Prototype Boundaries

The prototype is a self-contained HTML experience with local UI state, sample character and quest data, and predefined replies. Settings and conversation state reset on reload.

The vehicle home is a visual simulation. It does not run an Android emulator, display a real system overlay, play audio, or control a vehicle. Starting a quest changes its displayed state without awarding growth rewards.

Validate the navigation, conversation layout, and character motion before expanding the feature set. Persistent storage, real AI responses, vehicle signals, and platform support for displaying the companion outside the app should build on these established flows.
