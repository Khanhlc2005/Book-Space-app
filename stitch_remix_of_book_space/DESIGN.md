# Design System Specification: The Digital Atelier

## 1. Overview & Creative North Star
**Creative North Star: The Modern Librarian**
This design system rejects the "app-like" rigidity of standard SaaS platforms in favor of an editorial, high-end literary experience. We are not building a utility; we are building a sanctuary for thought. The aesthetic is "Modern Intellectual"—combining the precision of Swiss design with the warmth of a physical reading room.

To move beyond a "template" look, we utilize **Intentional Asymmetry**. Hero sections should feature off-center typography and overlapping elements (e.g., a book cover bleeding over a container edge) to create a sense of movement. We prioritize "Breathing Room" over density; every element must earn its place on the canvas.

---

## 2. Colors & Tonal Architecture
The palette is rooted in three distinct atmospheric modes. Each mode is designed to reduce eye strain while maintaining premium contrast.

### The "No-Line" Rule
**Explicit Instruction:** 1px solid borders for sectioning are strictly prohibited. 
Structural boundaries must be defined solely through background color shifts or subtle tonal transitions. For example, a `surface-container-low` section sitting on a `surface` background provides all the definition required.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers—like stacked sheets of fine vellum.
- **Base:** `surface` (The foundation).
- **Secondary Tier:** `surface-container-low` (For subtle grouping).
- **High Tier:** `surface-container-high` (For interactive elements like cards).
- **The Glass & Gradient Rule:** For floating elements (Modals, Navigation Bars), use Glassmorphism. Implement `surface` colors at 80% opacity with a `20px` backdrop-blur. 

### Signature Textures
Main CTAs and Hero backgrounds should utilize a subtle linear gradient (135°) transitioning from `primary` to `primary-container`. This adds a "soul" to the interface that flat hex codes cannot achieve.

---

## 3. Typography: The Editorial Voice
Typography is the primary driver of our brand identity. We pair a high-contrast serif for immersive reading with a clean, functional sans-serif for UI utility.

| Role | Token | Font Family | Size | Character |
| :--- | :--- | :--- | :--- | :--- |
| **Display** | `display-lg` | Newsreader | 3.5rem | Authoritative, elegant, airy. |
| **Headline** | `headline-md` | Newsreader | 1.75rem | Book titles and section headers. |
| **Title** | `title-lg` | Inter | 1.375rem | Navigation and functional labels. |
| **Body** | `body-lg` | Inter | 1.0rem | Standard UI text (Medium weight). |
| **Reading** | Custom | Newsreader | 1.125rem | 1.6x Line height for long-form. |
| **Label** | `label-md` | Inter | 0.75rem | Metadata (All caps, +5% tracking). |

---

## 4. Elevation & Depth: Tonal Layering
We do not use shadows to simulate "height"; we use color to simulate "presence."

- **The Layering Principle:** Place a `surface-container-lowest` card on a `surface-container-low` background. This creates a soft, natural "lift" without the "dirty" look of grey dropshadows.
- **Ambient Shadows:** When a float is required (e.g., a Popover), use an ultra-diffused shadow: `0px 12px 32px rgba(on-surface, 0.06)`. The shadow must be tinted with the `on-surface` color to feel like natural ambient occlusion.
- **The Ghost Border Fallback:** If accessibility requires a border, use the `outline-variant` token at **15% opacity**. Never use a 100% opaque border.

---

## 5. Components: Modern Primitives

### Buttons & Interaction
- **Primary:** `primary` background with `on-primary` text. Use `xl` (3rem) rounding. No shadow.
- **Secondary:** `secondary-container` background. These should feel "embedded" rather than "floating."
- **Tertiary:** Text-only with an underline that appears on hover, using the `accent` color.

### The "Floating" Search Bar
Avoid the standard box. The search bar should be a `full` (9999px) rounded element using `surface-container-highest` with a `20px` blur glass effect when scrolled over content.

### Chips & Metadata
- **Shape:** `full` rounding.
- **Style:** Use `surface-variant` backgrounds with `on-surface-variant` text.
- **Interactions:** On hover, shift background to `primary-container` to signify choice.

### Cards & Content Lists
**Forbid the use of divider lines.**
Separate list items using `spacing-6` (1.5rem) of vertical white space. For cards, use the "Tonal Nesting" method: a `surface-container-low` card sitting on a `surface` background. The `DEFAULT` (1rem) corner radius applies to all standard cards.

### Contextual Reading Progress (Custom Component)
A slim, `2px` horizontal bar at the top of the viewport using the `tertiary` token. It should feel like a bookmark ribbon, not a loading bar.

---

## 6. Do’s and Don'ts

### Do:
*   **Do** use asymmetrical margins. If the left margin is `spacing-8`, consider a right margin of `spacing-12` for editorial layouts.
*   **Do** use `Newsreader` for any text that is meant to be "savored" (Quotes, Titles, Book Excerpts).
*   **Do** leverage the Sepia theme as a "Focus Mode" to reduce blue light during evening use.

### Don't:
*   **Don't** use pure black `#000000`. Use the `on-background` or `on-surface` tokens to maintain a "printed ink" feel.
*   **Don't** use standard Material Design "Floating Action Buttons" (FABs). They clutter the reading experience. Use integrated, grounded actions instead.
*   **Don't** use 1px dividers to separate content. Use the spacing scale (`spacing-8` to `spacing-16`) to create "visual silences."