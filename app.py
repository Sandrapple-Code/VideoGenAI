import os

import streamlit as st
from dotenv import load_dotenv
from google import genai

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

load_dotenv()

st.set_page_config(
    page_title="VideoGenAI",
    page_icon="🎥",
    layout="centered",
)

# ---------------------------------------------------------------------------
# Page header
# ---------------------------------------------------------------------------

st.title("🎥 VideoGenAI")
st.write(
    "Generate images and videos powered by Google Gemini (Imagen & Veo). "
    "Enter your Gemini API key below to get started."
)

st.divider()

# ---------------------------------------------------------------------------
# API key input
# ---------------------------------------------------------------------------

api_key = st.text_input(
    "Gemini API Key",
    value=os.getenv("GEMINI_API_KEY", ""),
    type="password",
    placeholder="Paste your Gemini API key here…",
    help="Get a key at https://aistudio.google.com/app/apikey",
)

# ---------------------------------------------------------------------------
# Generation mode selector
# ---------------------------------------------------------------------------

mode = st.radio(
    "Generation mode",
    options=["📸 Image Canvas", "🎥 Video Motion"],
    horizontal=True,
)

# ---------------------------------------------------------------------------
# Prompt input
# ---------------------------------------------------------------------------

prompt = st.text_area(
    "Your prompt",
    placeholder="e.g. Futuristic cyberpunk lotus garden, glowing neon mist…",
    max_chars=500,
    height=120,
)

generate = st.button("✨ Generate", use_container_width=True, type="primary")

# ---------------------------------------------------------------------------
# Generation logic
# ---------------------------------------------------------------------------

if generate:
    if not api_key:
        st.error("Please enter a valid Gemini API key before generating.")
    elif not prompt.strip():
        st.warning("Please enter a prompt before generating.")
    else:
        client = genai.Client(api_key=api_key)

        if mode == "📸 Image Canvas":
            with st.spinner("Generating image…"):
                try:
                    response = client.models.generate_images(
                        model="imagen-3.0-generate-002",
                        prompt=prompt,
                        config=genai.types.GenerateImagesConfig(number_of_images=1),
                    )
                    for image in response.generated_images:
                        st.image(
                            image.image.image_bytes,
                            caption=prompt,
                            use_container_width=True,
                        )
                except Exception as exc:
                    st.error(f"Image generation failed: {exc}")

        else:  # Video Motion
            st.info(
                "Video generation via Veo is a long-running operation. "
                "Add your polling logic here using the `google-genai` SDK."
            )
            # TODO: call client.models.generate_videos(...) and poll the
            # returned operation until done, then display the video bytes.

# ---------------------------------------------------------------------------
# Footer
# ---------------------------------------------------------------------------

st.divider()
st.caption(
    "Built with [Streamlit](https://streamlit.io) · "
    "Powered by [Google Gemini](https://ai.google.dev)"
)
