# AI Image Studio 🎨

AI Image Studio is a beautiful, modern, native Android application built using Kotlin, Jetpack Compose, Room Database, Retrofit, and the Gemini API (`gemini-2.5-flash-image` model). It allows users to write creative prompts, select artistic styles, set advanced modifiers (aspect ratios, negative prompts), and automatically render beautiful AI images in real-time.

---

## 🚀 Key Features

1. **Jetpack Compose UI**: A gorgeous, highly responsive, Material 3-compliant interface featuring elegant accent animations, dynamic light/dark theme toggles, and customized styled components.
2. **Preset Art Styles**: Style-conditioned prompt modifications! Choose Cyberpunk, Anime, Cinematic, Pixar-style 3D Render, Watercolor, Comic Book, Pixel Art, or classic Oil Painting. The app automatically appends optimized quality adjectives and style suffixes.
3. **Advanced Controls accordion**:
   - **Aspect Ratio Selector**: 1:1, 16:9 widescreen, 9:16 portrait, 4:3 camera, and 3:4 aspect ratios.
   - **Negative Prompts**: Exclude unwanted visual components (such as "blurry", "ugly", "deformed features") from final rendering.
4. **Interactive Playground**:
   - **Surprise Me 🎲**: A random prompt generator that populates artistic, high-quality prompt ideas to spark creativity.
   - **Save to Device Gallery 📥**: Automatically structures, compresses, and saves your masterpiece to the Android public pictures gallery folder via modern `MediaStore` queries.
   - **Favorite ❤️**: Bookmarks items inside your local studio.
5. **Robust Prompt Gallery / History view**: Full-featured local persistence via SQLite / Room Database allowing you to inspect previous prompts, see detailed generation metadata, reload preset details, and delete entries.
6. **CursorWindow Optimization**: Since storing massive image binaries directly inside a local SQLite database causes OS CursorWindow buffer problems, this app has a specialized filesystem utility (`ImageStorageHelper`) that writes images to local cache/app folders and only references the filepath. This guarantees 100% stability.

---

## 🧱 Local File Architecture (Clean MVVM Layers)

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

## 🔑 How to Add your API Key

The application relies on the Google AI Studio **Secrets** system. 

### 1. In Google AI Studio Build (Online Prototype)
Simply enter your Google Gemini API Key into the **Secrets panel** under the name:
```properties
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```
The platform automatically links and exposes this key at build time!

### 2. Running Locally (Android Studio)
Insert your key into a localized `.env` file at the root. The Secrets Gradle Plugin will automatically load it:
```properties
GEMINI_API_KEY=YourActualApiKeyStringHere
```
*Never commit this `.env` file to version control.*

---

## 🛠️ How to Run Locally

1. Make sure you have the latest **Android Studio (Ladybug or newer)**.
2. Clone this project repository.
3. Open Android Studio and select **File -> Open...** then point to this directory.
4. Let Gradle sync and download dependencies.
5. Connect your physical Android Device or launch the Virtual Device Emulator.
6. Press the green **Run (Play) ▶️** button inside your toolbar to compile the app and install it onto your device!

---

## 📦 How to Deploy

To generate a standalone APK or an Android App Bundle (AAB):
1. In Android Studio, go to **Build -> Build Bundle(s) / APK(s) -> Build APK(s)** in your menu bar.
2. Once compilation finishes, a popup bubble will show where the APK is located (typically under `/app/build/outputs/apk/debug/app-debug.apk`).
3. Side-load this APK onto any compatible Android device (Android API Level 24+ / Android 7.0 and above).

---

## ⚠️ Known Limitation

**API Key Security with Native Prototypes**:
Because this prototype communicates directly with the Gemini API using native Retrofit client-side requests, the `GEMINI_API_KEY` is packaged directly into the client-side BuildConfig. 
While this is optimal for fast prototyping, testing, and easy stand-alone deployment, a compiled client-side APK can be decompiled and analyzed by an attacker to extract the key. 

*Recommendation for Production*: When preparing this application for a wider public launch, the image-generation REST requests should be routed through a secure middle-tier server proxy (such as Firebase Functions or cloud-run microservices) which communicates with Google GenAI securely, completely hiding the API key from the mobile client.
