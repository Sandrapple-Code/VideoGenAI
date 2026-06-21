# AI Image Studio 

AI Image Studio is a beautiful, modern, native Android application built using Kotlin, Jetpack Compose, Room Database, Retrofit, and the Gemini API (`gemini-2.5-flash-image` model). It allows users to write creative prompts, select artistic styles, set advanced modifiers (aspect ratios, negative prompts), and automatically render beautiful AI images in real-time.



##  Key Features

1. **Jetpack Compose UI**: A gorgeous, highly responsive, Material 3-compliant interface featuring elegant accent animations, dynamic light/dark theme toggles, and customized styled components.
2. **Preset Art Styles**: Style-conditioned prompt modifications! Choose Cyberpunk, Anime, Cinematic, Pixar-style 3D Render, Watercolor, Comic Book, Pixel Art, or classic Oil Painting. The app automatically appends optimized quality adjectives and style suffixes.
3. **Advanced Controls accordion**:
   - **Aspect Ratio Selector**: 1:1, 16:9 widescreen, 9:16 portrait, 4:3 camera, and 3:4 aspect ratios.
   - **Negative Prompts**: Exclude unwanted visual components (such as "blurry", "ugly", "deformed features") from final rendering.
4. **Interactive Playground**:
   - **Surprise Me **: A random prompt generator that populates artistic, high-quality prompt ideas to spark creativity.
   - **Save to Device Gallery **: Automatically structures, compresses, and saves your masterpiece to the Android public pictures gallery folder via modern `MediaStore` queries.
   - **Favorite **: Bookmarks items inside your local studio.
5. **Robust Prompt Gallery / History view**: Full-featured local persistence via SQLite / Room Database allowing you to inspect previous prompts, see detailed generation metadata, reload preset details, and delete entries.
6. **CursorWindow Optimization**: Since storing massive image binaries directly inside a local SQLite database causes OS CursorWindow buffer problems, this app has a specialized filesystem utility (`ImageStorageHelper`) that writes images to local cache/app folders and only references the filepath. This guarantees 100% stability.



##  Local File Architecture (Clean MVVM Layers)

- **`/app/src/main/java/com/example/data/`**:
  - `model/ImageGenerationItem.kt` — Schema definition for history entries.
  - `local/ImageDao.kt` — Database Queries.
  - `local/ImageDatabase.kt` — SQLite standard setup.
  - `repository/ImageRepository.kt` — Decoupled Repository pattern.
  - `api/GeminiImageApi.kt` — Retrofit configuration for the Gemini image generation endpoints.
- **`/app/src/main/java/com/example/ui/`**:
  - `styles/ImageStyles.kt` — Configured art presets, styles, and "Surprise Me 🎲" prompts.
  - `viewmodel/ImageGenerationViewModel.kt` — Architecture controller communicating state flows with Jetpack Compose.
  - `screens/ImageGeneratorScreen.kt` — The main, visually rich UI layout with dialogs, grids, loading shimmers, and interactive tags.
- **`/app/src/main/java/com/example/util/`**:
  - `ImageStorageHelper.kt` — Filesystem read/writes and MediaStore export pipeline.

---

## How It Works

Enter an image prompt.

Example

A futuristic Indian city at night
Select a style.

Examples

Realistic
Anime
Cyberpunk
Watercolor
Sketch
Fantasy

## How it works?
The application modifies the prompt.

Example

Original prompt

A futuristic Indian city at night

Selected style

Cyberpunk

Final prompt sent to the API

A futuristic Indian city at night, cyberpunk style, neon lights, futuristic architecture, highly detailed, cinematic lighting.

The API generates the image.
The generated image is displayed inside the application.
The prompt is saved in the history panel.

Author
Sanskriti Shakya



