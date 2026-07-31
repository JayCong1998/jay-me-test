# Storyboard

**Format:** 1920x1080
**Duration:** 20.00s
**Audio:** Voiceover script in `narration.txt`; preview currently uses a quiet silent WAV placeholder because local TTS is not installed.
**VO direction:** Warm, confident, fan-community energy. Clear pauses after the opening question and before the final title.
**Style basis:** `DESIGN.md` with captured mobile screenshots and recreated gold note badge.

## Asset Audit

| Asset | Type | Assign to Beat | Role |
| --- | --- | --- | --- |
| `capture/assets/logo-note.svg` | SVG badge | Beat 1, Beat 5 | Brand opener and closer |
| `capture/screenshots/desktop-home.png` | Product screenshot | Beat 1 | Establish centered H5 product shell |
| `capture/screenshots/scroll-000.png` | Product screenshot | Beat 2, Beat 3 | Home UI, mode cards, stats |
| `capture/screenshots/scroll-001-leaderboard.png` | Product screenshot | Beat 4 | Ranking/login proof screen |
| `capture/screenshots/scroll-002-profile.png` | Product screenshot | Beat 4 | Personal record proof screen |

## Beat 1 - Cold Open (0.00-3.80s)

**VO:** "Can you prove you're a real Jay Chou fan in twenty questions?"

**Concept:** The video opens on a quiet off-white canvas, then the gold music badge pulses like a stamp on an exam paper. The mobile product shell glides into view behind it, making the question feel playable immediately.

**Visual:** Full 1920x1080 light canvas. Oversized pale rings drift behind a centered phone screenshot from `desktop-home.png`. The recreated logo badge floats in the foreground, the words "Real fan?" and "Twenty questions." arrive as bold editorial type, and three tiny stat chips echo the app's score tiles.

**Mood:** Refined fan challenge, clean and ceremonial.

**Assets:** `capture/assets/logo-note.svg`, `capture/screenshots/desktop-home.png`.

**Techniques:** CSS 3D screenshot tilt, SVG ring drawing, per-word kinetic typography.

**Transition:** Blur crossfade into Beat 2, 0.45s.

## Beat 2 - Three Ways To Play (3.80-8.20s)

**VO:** "Jay Me Test turns fandom into a fast mobile challenge."

**Concept:** The mode cards become the product promise. Classic, Album, and Abyss separate into floating cards around the phone, as if the app is dealing a playable menu.

**Visual:** Home screenshot sits left in a phone frame. Three large mode cards fan out on the right: Classic Mode, Album Challenge, Endless Abyss. Gold, blue, and purple icon panels pop in with different motion verbs; small `NEW` ribbons slice across the album and abyss cards.

**Mood:** Fast but elegant, product-menu clarity.

**Assets:** `capture/screenshots/scroll-000.png`, `capture/assets/logo-note.svg`.

**Techniques:** Card cascade, counter-style labels, SVG connector lines.

**Transition:** Velocity-matched upward push into Beat 3, 0.35s.

## Beat 3 - The Abyss Hook (8.20-12.90s)

**VO:** "Play random classic questions, unlock album stages, or step into Endless Abyss where one wrong answer ends the streak."

**Concept:** The promo zooms into stakes. The Endless Abyss card expands into a dramatic challenge panel while the other two modes orbit as supporting choices.

**Visual:** A cropped home screenshot anchors the center. The purple Abyss card grows forward with a triangular glyph, a red danger slash, and the line "One wrong answer ends the streak." Classic and Album labels slide behind it as alternate paths. A streak counter animates from 0 to 4.

**Mood:** Playful tension, not horror; still clean and mobile-first.

**Assets:** `capture/screenshots/scroll-000.png`.

**Techniques:** CSS 3D card push, numeric counter, procedural dust/ring canvas.

**Transition:** Quick zoom-through into Beat 4, 0.35s.

## Beat 4 - Progress And Community (12.90-16.70s)

**VO:** "Save scores, check rankings, and keep building your own Jay fan record."

**Concept:** The app becomes a habit loop: scores, ranking, personal record. Two captured screens slide into a split frame and lock into a polished dashboard moment.

**Visual:** Leaderboard screenshot enters from the left, Profile screenshot from the right. Gold CTA bars align across both screens. Three proof chips animate underneath: Save scores, Join rankings, Track records. A faint trophy outline draws behind the split screens.

**Mood:** Trustworthy and communal.

**Assets:** `capture/screenshots/scroll-001-leaderboard.png`, `capture/screenshots/scroll-002-profile.png`, `capture/assets/logo-note.svg`.

**Techniques:** Split-screen product compositing, SVG trophy path drawing, chip cascade.

**Transition:** Gentle light wash into Beat 5, 0.55s.

## Beat 5 - CTA (16.70-20.00s)

**VO:** "Jay Me Test. Start the trial."

**Concept:** Everything resolves back into the mark and product name. The phone UI recedes, leaving a clean badge, gold title, and one direct call to action.

**Visual:** Logo badge grows to center, gold title "杰迷试炼" appears beneath, English product name and CTA sit below. Tiny mode icons orbit once and settle into a neat row: Classic, Albums, Abyss. End on a warm off-white frame, no clutter.

**Mood:** Confident closing, app-store promo ending.

**Assets:** `capture/assets/logo-note.svg`, `capture/screenshots/scroll-000.png`.

**Techniques:** Logo pulse, per-word CTA reveal, finite orbit motion.

**Transition:** Final fade to the still end frame.

## Production Architecture

```text
project/
├── index.html
├── DESIGN.md
├── SCRIPT.md
├── STORYBOARD.md
├── narration.txt
├── narration.wav
├── capture/
│   ├── screenshots/
│   ├── assets/
│   └── extracted/
└── snapshots/
```
