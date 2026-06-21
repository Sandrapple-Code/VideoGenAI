package com.example.ui.styles

data class ImageStyle(
    val name: String,
    val icon: String, // Emoji or icon identifier
    val promptSuffix: String,
    val description: String
)

object ImageStyles {
    val styles = listOf(
        ImageStyle(
            name = "None",
            icon = "🎨",
            promptSuffix = "",
            description = "Natural output without explicit styling"
        ),
        ImageStyle(
            name = "Cyberpunk",
            icon = "🌆",
            promptSuffix = "cyberpunk style, neon glow, high tech, futuristic city, highly detailed, vibrant blue and purple accents, synthwave aesthetic",
            description = "Futuristic neon-lit dystopias"
        ),
        ImageStyle(
            name = "Anime",
            icon = "🌸",
            promptSuffix = "anime style, gorgeous digital illustration, vibrant colors, detailed line art, clean shading, studio ghibli or makoto shinkai style, high-end anime look",
            description = "Beautiful animated aesthetics"
        ),
        ImageStyle(
            name = "Cinematic",
            icon = "🎬",
            promptSuffix = "cinematic photograph, dramatic lighting, shot on 35mm lens, depth of field, photorealistic, intricate detail, epic composition, unreal engine rendering style",
            description = "Movie-like dramatic lighting"
        ),
        ImageStyle(
            name = "3D Render",
            icon = "🧸",
            promptSuffix = "3D render, Pixar style, cute, smooth textures, claymation aesthetic, raytracing, vibrant pastel colors, highly detailed, octane render",
            description = "Cute, stylized 3D models"
        ),
        ImageStyle(
            name = "Watercolor",
            icon = "🖌️",
            promptSuffix = "watercolor painting, soft fluid brush strokes, bleeding paint edges, heavy textured paper, fine artistic detail, pastel tones, elegant hand-painted masterpiece",
            description = "Soft hand-painted artwork"
        ),
        ImageStyle(
            name = "Pixel Art",
            icon = "👾",
            promptSuffix = "retro pixel art, 16-bit arcade style, charming pixelated look, vibrant limited palette, cute, high-quality pixel illustration, game art asset",
            description = "Charming 16-bit game nostalgia"
        ),
        ImageStyle(
            name = "Comic Book",
            icon = "💥",
            promptSuffix = "comic book illustration, bold ink outlines, halftone patterns, dramatic action-packed shading, pop art aesthetic, vintage hand-drawn marvel style",
            description = "Bold ink and action strokes"
        ),
        ImageStyle(
            name = "Oil Painting",
            icon = "🖼️",
            promptSuffix = "gorgeous oil on canvas painting, thick impasto brushstrokes, rich classical colors, dramatic chiaroscuro lighting, museum masterpiece, realistic textures",
            description = "Classic textured canvas look"
        )
    )

    // A collection of random prompts to inspire the user
    val randomPrompts = listOf(
        "A magical library floating inside a giant glowing cosmic nebula",
        "A cute chubby orange dragon sipping boba tea on top of a fluffy cloud",
        "A cozy cabin nestled under a massive ancient glowing bioluminescent tree",
        "An adorable red panda wearing an astronaut helmet floating in deep space",
        "A majestic samurai cat standing on a high cliff overlooking a cherry blossom valley",
        "A secret hidden hot spring in a snowy neon-lit cyberpunk forest",
        "A vintage steampunk locomotive winding its way through floating crystalline islands",
        "A tiny wizard mouse reading a giant leatherbound book next to an glowing candle",
        "An ancient mystical temple hidden underwater inside a giant coral reef",
        "A futuristic vehicle speeding through a rainy cyberpunk Tokyo alley",
        "An enchanted cafe where flowers bloom inside coffee cups and servers have delicate fairy wings"
    )

    fun getRandomPrompt(): String {
        return randomPrompts.random()
    }

    fun applyStyle(prompt: String, style: ImageStyle, negativePrompt: String = ""): String {
        val base = prompt.trim()
        if (style.name == "None" || style.promptSuffix.isEmpty()) {
            return base
        }
        val styledPrompt = "$base, ${style.promptSuffix}"
        return if (negativePrompt.isNotEmpty()) {
            "$styledPrompt, [Negative Prompt: $negativePrompt]"
        } else {
            styledPrompt
        }
    }
}
