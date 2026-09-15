#!/usr/bin/env python3
"""
High-Ranking Programmatic SEO Generator for Gesture Volume
Generates 100 optimized, mobile-friendly, schema-rich landing pages.
"""

import os
import html
import json
from seo_topics import PAGES

BASE_URL = "https://khalidabdullahh.github.io/GestureVolume"

def generate_seo_html(page, all_pages):
    slug = page["slug"]
    title = page["title"]
    desc = page["desc"]
    category = page["category"]
    keywords = page["keywords"]
    brand = page.get("brand", "Android")
    canonical_url = f"{BASE_URL}/seo/{slug}.html"
    
    related_pages = [p for p in all_pages if p["slug"] != slug and p["category"] == category][:3]
    if len(related_pages) < 3:
        related_pages += [p for p in all_pages if p["slug"] != slug and p not in related_pages][:3 - len(related_pages)]
    
    schema_json = {
        "@context": "https://schema.org",
        "@graph": [
            {
                "@type": "Article",
                "headline": title,
                "description": desc,
                "mainEntityOfPage": canonical_url,
                "author": {
                    "@type": "Person",
                    "name": "Khalid Abdullah",
                    "url": "https://github.com/khalidabdullahh"
                },
                "publisher": {
                    "@type": "Organization",
                    "name": "Gesture Volume",
                    "url": BASE_URL
                },
                "datePublished": "2026-09-14",
                "dateModified": "2026-09-15"
            },
            {
                "@type": "BreadcrumbList",
                "itemListElement": [
                    {"@type": "ListItem", "position": 1, "name": "Home", "item": f"{BASE_URL}/"},
                    {"@type": "ListItem", "position": 2, "name": "SEO Directory", "item": f"{BASE_URL}/seo/"},
                    {"@type": "ListItem", "position": 3, "name": title, "item": canonical_url}
                ]
            },
            {
                "@type": "SoftwareApplication",
                "name": "Gesture Volume",
                "operatingSystem": "Android 8.0+",
                "applicationCategory": "UtilitiesApplication",
                "offers": {"@type": "Offer", "price": "0", "priceCurrency": "USD"}
            }
        ]
    }

    related_links_html = "".join([
        f'''<a href="./{r["slug"]}.html" class="block p-4 rounded-xl bg-slate-900/60 hover:bg-slate-800 border border-white/5 hover:border-brand-500/30 transition-all group">
            <span class="text-xs font-semibold text-accent-cyan uppercase tracking-wider block mb-1">{r["category"]}</span>
            <span class="text-sm font-bold text-white group-hover:text-brand-400 transition-colors line-clamp-1">{r["title"]}</span>
            <span class="text-xs text-slate-400 mt-1 line-clamp-2 block">{r["desc"]}</span>
        </a>''' for r in related_pages
    ])

    return f"""<!DOCTYPE html>
<html lang="en" class="scroll-smooth">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0">

    <title>{html.escape(title)} | Gesture Volume</title>
    <meta name="title" content="{html.escape(title)} | Gesture Volume">
    <meta name="description" content="{html.escape(desc)}">
    <meta name="keywords" content="{html.escape(keywords)}">
    <meta name="author" content="Khalid Abdullah">
    <meta name="robots" content="index, follow, max-snippet:-1, max-image-preview:large, max-video-preview:-1">
    <link rel="canonical" href="{canonical_url}">

    <meta property="og:type" content="article">
    <meta property="og:url" content="{canonical_url}">
    <meta property="og:title" content="{html.escape(title)}">
    <meta property="og:description" content="{html.escape(desc)}">
    <meta property="og:site_name" content="Gesture Volume">
    <meta property="og:image" content="https://raw.githubusercontent.com/khalidabdullahh/GestureVolume/main/docs/assets/og-preview.png">

    <meta property="twitter:card" content="summary_large_image">
    <meta property="twitter:url" content="{canonical_url}">
    <meta property="twitter:title" content="{html.escape(title)}">
    <meta property="twitter:description" content="{html.escape(desc)}">
    <meta property="twitter:image" content="https://raw.githubusercontent.com/khalidabdullahh/GestureVolume/main/docs/assets/og-preview.png">

    <script type="application/ld+json">
    {json.dumps(schema_json, indent=2)}
    </script>

    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <script>
        tailwind.config = {{
            darkMode: 'class',
            theme: {{
                extend: {{
                    fontFamily: {{ sans: ['"Plus Jakarta Sans"', 'sans-serif'] }},
                    colors: {{
                        brand: {{
                            400: '#a78bfa', 500: '#8b5cf6', 600: '#6c5ce7', 700: '#5843e0',
                            900: '#1a1d2b', dark: '#0a0c14', surface: '#131622'
                        }},
                        accent: {{ cyan: '#00cec9', green: '#10b981', amber: '#f59e0b', bmc: '#FFDD00' }}
                    }}
                }}
            }}
        }}
    </script>
    <style>
        body {{ background-color: #0a0c14; color: #f8fafc; font-family: 'Plus Jakarta Sans', sans-serif; overflow-x: hidden; }}
        .glass-card {{ background: rgba(19, 22, 34, 0.75); backdrop-filter: blur(14px); -webkit-backdrop-filter: blur(14px); border: 1px solid rgba(255, 255, 255, 0.08); }}
        .glass-nav {{ background: rgba(10, 12, 20, 0.85); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); border-bottom: 1px solid rgba(255, 255, 255, 0.08); }}
        .bmc-btn {{ background: #FFDD00; color: #000; font-weight: 700; }}
        .bmc-btn:hover {{ background: #FFE433; }}
    </style>
</head>
<body class="bg-brand-dark text-slate-100 antialiased selection:bg-brand-600 selection:text-white">

    <header class="glass-nav sticky top-0 z-50">
        <div class="max-w-6xl mx-auto px-4 sm:px-6 h-18 flex items-center justify-between">
            <a href="../" class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-gradient-to-tr from-brand-600 to-accent-cyan flex items-center justify-center shadow-md">
                    <i data-lucide="volume-2" class="w-5 h-5 text-white"></i>
                </div>
                <span class="text-lg font-bold text-white tracking-tight">Gesture<span class="text-accent-cyan">Volume</span></span>
            </a>

            <div class="flex items-center gap-3">
                <a href="../seo/" class="text-xs sm:text-sm font-medium text-slate-300 hover:text-white transition-colors">Directory</a>
                <a href="https://buymeacoffee.com/khalidabdullahh" target="_blank" class="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-lg bmc-btn text-xs font-bold shadow-sm">
                    <i data-lucide="coffee" class="w-3.5 h-3.5 text-black"></i>
                    <span>Buy Coffee</span>
                </a>
                <a href="../downloads/GestureVolume-v1.0.0.apk" download class="flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl bg-brand-600 hover:bg-brand-500 text-white font-bold text-xs sm:text-sm shadow-md transition-all">
                    <i data-lucide="download" class="w-3.5 h-3.5"></i>
                    <span>Download APK</span>
                </a>
            </div>
        </div>
    </header>

    <main class="max-w-4xl mx-auto px-4 sm:px-6 py-10">
        <nav class="flex items-center gap-2 text-xs text-slate-400 mb-6">
            <a href="../" class="hover:text-white">Home</a>
            <span>/</span>
            <a href="./" class="hover:text-white">Directory</a>
            <span>/</span>
            <span class="text-brand-400 line-clamp-1">{html.escape(page["title"])}</span>
        </nav>

        <div class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-brand-600/15 border border-brand-500/20 text-brand-400 text-xs font-bold uppercase tracking-wider mb-4">
            <i data-lucide="wrench" class="w-3.5 h-3.5 text-accent-cyan"></i>
            {category} • {brand}
        </div>

        <h1 class="text-2xl sm:text-4xl font-extrabold text-white tracking-tight leading-tight mb-5">
            {html.escape(title)}
        </h1>

        <p class="text-sm sm:text-base text-slate-300 leading-relaxed mb-8">
            {html.escape(desc)}
        </p>

        <div class="glass-card rounded-2xl p-6 sm:p-8 border border-brand-500/30 mb-10 shadow-xl bg-gradient-to-r from-brand-900/40 via-brand-dark to-slate-900">
            <div class="flex flex-col sm:flex-row items-center justify-between gap-6">
                <div>
                    <h3 class="text-lg sm:text-xl font-bold text-white mb-1.5">Fix Broken Volume in 60 Seconds</h3>
                    <p class="text-slate-300 text-xs sm:text-sm">Download the free Gesture Volume APK. No floating screen bubbles, zero ads, no root required.</p>
                </div>
                <a href="../downloads/GestureVolume-v1.0.0.apk" download class="w-full sm:w-auto shrink-0 flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-gradient-to-r from-brand-600 to-accent-cyan hover:from-brand-500 hover:to-accent-cyan text-white font-bold text-sm shadow-lg shadow-brand-600/30 transition-all hover:scale-105">
                    <i data-lucide="download" class="w-4 h-4"></i>
                    <span>Download Free APK</span>
                </a>
            </div>
        </div>

        <article class="space-y-6 text-slate-300 text-sm sm:text-base leading-relaxed">
            <section class="glass-card rounded-2xl p-5 sm:p-7 border border-white/5 space-y-3">
                <h2 class="text-xl font-bold text-white flex items-center gap-2">
                    <i data-lucide="alert-circle" class="w-5 h-5 text-accent-amber"></i>
                    The Hardware Volume Rocker Problem
                </h2>
                <p>
                    Physical volume buttons on modern smartphones are delicate mechanical parts. Over months of repeated pressing, dust accumulation, moisture exposure, or accidental drops, volume rockers frequently get jammed, become unresponsive, or completely fall off.
                </p>
                <p>
                    Traditional repairs often cost between <strong>$40 and $100</strong> and require opening your phone. Gesture Volume provides a permanent, zero-cost on-screen software fix.
                </p>
            </section>

            <section class="glass-card rounded-2xl p-5 sm:p-7 border border-white/5 space-y-4">
                <h2 class="text-xl font-bold text-white flex items-center gap-2">
                    <i data-lucide="check-circle-2" class="w-5 h-5 text-accent-green"></i>
                    How Gesture Volume Solves It Permanently
                </h2>
                <p>
                    Unlike older assistive-touch apps that place a permanent screen-blocking bubble, <strong>Gesture Volume remains 100% invisible</strong> until you need it:
                </p>
                <div class="grid sm:grid-cols-3 gap-3.5 pt-1">
                    <div class="p-4 rounded-xl bg-slate-950/60 border border-white/5 text-center">
                        <div class="w-7 h-7 rounded-full bg-brand-600/20 text-brand-400 font-bold mx-auto mb-2 flex items-center justify-center text-xs">1</div>
                        <span class="font-bold text-white text-xs block mb-1">Draw Circle (⭕)</span>
                        <span class="text-[11px] text-slate-400">Draw a circular stroke anywhere over any active application.</span>
                    </div>
                    <div class="p-4 rounded-xl bg-slate-950/60 border border-white/5 text-center">
                        <div class="w-7 h-7 rounded-full bg-accent-cyan/20 text-accent-cyan font-bold mx-auto mb-2 flex items-center justify-center text-xs">2</div>
                        <span class="font-bold text-white text-xs block mb-1">Swipe Up / Down (↕)</span>
                        <span class="text-[11px] text-slate-400">Slide finger upward to increase volume, downward to decrease.</span>
                    </div>
                    <div class="p-4 rounded-xl bg-slate-950/60 border border-white/5 text-center">
                        <div class="w-7 h-7 rounded-full bg-accent-green/20 text-accent-green font-bold mx-auto mb-2 flex items-center justify-center text-xs">3</div>
                        <span class="font-bold text-white text-xs block mb-1">Auto-Dismiss</span>
                        <span class="text-[11px] text-slate-400">Fades away after 3s of inactivity into zero memory usage.</span>
                    </div>
                </div>
            </section>

            <section class="glass-card rounded-2xl p-5 sm:p-7 border border-white/5 space-y-3">
                <h2 class="text-xl font-bold text-white flex items-center gap-2">
                    <i data-lucide="shield" class="w-5 h-5 text-accent-cyan"></i>
                    100% Privacy & Zero-Network Commitment
                </h2>
                <ul class="space-y-2 text-xs sm:text-sm text-slate-300">
                    <li class="flex items-center gap-2"><i data-lucide="check" class="w-4 h-4 text-accent-green"></i> <strong>Zero Network Access:</strong> No internet permission declared in AndroidManifest.</li>
                    <li class="flex items-center gap-2"><i data-lucide="check" class="w-4 h-4 text-accent-green"></i> <strong>No Text/Screen Scraping:</strong> Window content retrieval is disabled.</li>
                    <li class="flex items-center gap-2"><i data-lucide="check" class="w-4 h-4 text-accent-green"></i> <strong>Zero Ads & Trackers:</strong> No third-party analytics or background telemetry.</li>
                </ul>
            </section>
        </article>

        <section class="mt-12 pt-6 border-t border-white/10">
            <h3 class="text-lg font-bold text-white mb-4 flex items-center gap-2">
                <i data-lucide="book-open" class="w-4 h-4 text-brand-400"></i>
                Related Troubleshooting Guides
            </h3>
            <div class="grid sm:grid-cols-3 gap-3.5">
                {related_links_html}
            </div>
        </section>

        <section class="mt-10 glass-card rounded-2xl p-6 border border-amber-500/20 text-center">
            <span class="text-xs font-bold text-accent-amber uppercase tracking-wider block mb-1.5">Free & Open Source</span>
            <h4 class="text-base font-bold text-white mb-1.5">Did this fix your volume problem?</h4>
            <p class="text-xs text-slate-400 mb-4 max-w-md mx-auto">Support independent open-source development with a small coffee tip!</p>
            <a href="https://buymeacoffee.com/khalidabdullahh" target="_blank" class="inline-flex items-center gap-2 px-4 py-2 rounded-xl bmc-btn text-xs font-bold shadow-sm">
                <i data-lucide="coffee" class="w-3.5 h-3.5 text-black"></i> Buy Me a Coffee
            </a>
        </section>
    </main>

    <footer class="py-8 border-t border-white/5 text-center text-slate-500 text-xs">
        <div class="max-w-6xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-3">
            <div><span class="font-bold text-slate-300">Gesture Volume</span> • Free Open-Source Android Utility</div>
            <div class="flex items-center gap-4">
                <a href="../" class="hover:text-white">Home</a>
                <a href="../seo/" class="hover:text-white">All Guides (100)</a>
                <a href="https://github.com/khalidabdullahh/GestureVolume" target="_blank" class="hover:text-white">GitHub</a>
            </div>
        </div>
    </footer>

    <script>lucide.createIcons();</script>
</body>
</html>
"""

def generate_directory_index(all_pages):
    categories = {}
    for p in all_pages:
        cat = p["category"]
        if cat not in categories:
            categories[cat] = []
        categories[cat].append(p)

    cat_html = ""
    for cat, pages in categories.items():
        links = "".join([
            f'''<li class="py-2.5 border-b border-white/5 last:border-0">
                <a href="./{p["slug"]}.html" class="flex items-start justify-between gap-3 text-slate-300 hover:text-accent-cyan transition-colors group">
                    <span class="text-sm font-medium group-hover:underline">{html.escape(p["title"])}</span>
                    <i data-lucide="chevron-right" class="w-4 h-4 text-slate-500 group-hover:text-accent-cyan shrink-0 mt-0.5"></i>
                </a>
            </li>''' for p in pages
        ])
        cat_html += f'''<div class="glass-card rounded-2xl p-6 border border-white/10 shadow-lg">
            <h2 class="text-lg sm:text-xl font-bold text-white mb-3.5 flex items-center justify-between pb-3 border-b border-white/10">
                <span>{cat}</span>
                <span class="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-brand-600/20 text-brand-400">{len(pages)} Guides</span>
            </h2>
            <ul class="space-y-0.5">
                {links}
            </ul>
        </div>'''

    return f"""<!DOCTYPE html>
<html lang="en" class="scroll-smooth">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0">
    <title>Gesture Volume Guides & Solutions Directory — 100 SEO Topics</title>
    <meta name="description" content="Explore 100 comprehensive guides, device-specific fixes, and gesture volume tutorials for broken Android volume buttons.">
    <meta name="robots" content="index, follow">
    <link rel="canonical" href="{BASE_URL}/seo/">

    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <script>
        tailwind.config = {{
            darkMode: 'class',
            theme: {{
                extend: {{
                    fontFamily: {{ sans: ['"Plus Jakarta Sans"', 'sans-serif'] }},
                    colors: {{
                        brand: {{ 400: '#a78bfa', 500: '#8b5cf6', 600: '#6c5ce7', 900: '#1a1d2b', dark: '#0a0c14', surface: '#131622' }},
                        accent: {{ cyan: '#00cec9', green: '#10b981', amber: '#f59e0b' }}
                    }}
                }}
            }}
        }}
    </script>
    <style>
        body {{ background-color: #0a0c14; color: #f8fafc; font-family: 'Plus Jakarta Sans', sans-serif; overflow-x: hidden; }}
        .glass-card {{ background: rgba(19, 22, 34, 0.75); backdrop-filter: blur(14px); -webkit-backdrop-filter: blur(14px); border: 1px solid rgba(255, 255, 255, 0.08); }}
        .glass-nav {{ background: rgba(10, 12, 20, 0.85); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); border-bottom: 1px solid rgba(255, 255, 255, 0.08); }}
        .gradient-text {{ background: linear-gradient(135deg, #a78bfa 0%, #00cec9 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }}
    </style>
</head>
<body class="bg-brand-dark text-slate-100 antialiased">
    <header class="glass-nav sticky top-0 z-50">
        <div class="max-w-6xl mx-auto px-4 sm:px-6 h-18 flex items-center justify-between">
            <a href="../" class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-gradient-to-tr from-brand-600 to-accent-cyan flex items-center justify-center shadow-md">
                    <i data-lucide="volume-2" class="w-5 h-5 text-white"></i>
                </div>
                <span class="text-lg font-bold text-white tracking-tight">Gesture<span class="text-accent-cyan">Volume</span></span>
            </a>
            <div class="flex items-center gap-3">
                <a href="../" class="text-sm font-medium text-slate-300 hover:text-white">Home</a>
                <a href="../downloads/GestureVolume-v1.0.0.apk" download class="px-4 py-2 rounded-xl bg-brand-600 hover:bg-brand-500 text-white font-bold text-xs sm:text-sm">Download APK</a>
            </div>
        </div>
    </header>

    <main class="max-w-6xl mx-auto px-4 sm:px-6 py-10">
        <div class="text-center max-w-2xl mx-auto mb-10">
            <h1 class="text-3xl sm:text-5xl font-extrabold text-white tracking-tight mb-3">
                Knowledge <span class="gradient-text">Hub</span>
            </h1>
            <p class="text-slate-300 text-sm sm:text-base">
                100 comprehensive guides, brand-specific solutions, and symptom tutorials for on-screen volume control.
            </p>
        </div>

        <div class="grid md:grid-cols-2 gap-6 sm:gap-8">
            {cat_html}
        </div>
    </main>

    <footer class="py-8 border-t border-white/5 text-center text-slate-500 text-xs">
        <div class="max-w-6xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-3">
            <div><span class="font-bold text-slate-300">Gesture Volume</span> • 100 SEO Guides Directory</div>
            <a href="../" class="text-brand-400 hover:underline">Back to Main Landing Page</a>
        </div>
    </footer>
    <script>lucide.createIcons();</script>
</body>
</html>
"""

def generate_full_sitemap(all_pages):
    urls = [
        f"""  <url>
    <loc>{BASE_URL}/</loc>
    <lastmod>2026-09-15</lastmod>
    <changefreq>daily</changefreq>
    <priority>1.0</priority>
  </url>""",
        f"""  <url>
    <loc>{BASE_URL}/seo/</loc>
    <lastmod>2026-09-15</lastmod>
    <changefreq>daily</changefreq>
    <priority>0.9</priority>
  </url>"""
    ]

    for p in all_pages:
        urls.append(f"""  <url>
    <loc>{BASE_URL}/seo/{p["slug"]}.html</loc>
    <lastmod>2026-09-15</lastmod>
    <changefreq>weekly</changefreq>
    <priority>0.8</priority>
  </url>""")

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
{os.linesep.join(urls)}
</urlset>
"""

def main():
    docs_seo_dir = "docs/seo"
    root_seo_dir = "seo"
    os.makedirs(docs_seo_dir, exist_ok=True)
    os.makedirs(root_seo_dir, exist_ok=True)

    print(f"Generating {len(PAGES)} updated SEO pages...")

    for page in PAGES:
        html_content = generate_seo_html(page, PAGES)
        with open(os.path.join(docs_seo_dir, f"{page['slug']}.html"), "w", encoding="utf-8") as f:
            f.write(html_content)
        with open(os.path.join(root_seo_dir, f"{page['slug']}.html"), "w", encoding="utf-8") as f:
            f.write(html_content)

    dir_html = generate_directory_index(PAGES)
    with open(os.path.join(docs_seo_dir, "index.html"), "w", encoding="utf-8") as f:
        f.write(dir_html)
    with open(os.path.join(root_seo_dir, "index.html"), "w", encoding="utf-8") as f:
        f.write(dir_html)

    sitemap_xml = generate_full_sitemap(PAGES)
    with open("sitemap.xml", "w", encoding="utf-8") as f:
        f.write(sitemap_xml)
    with open("docs/sitemap.xml", "w", encoding="utf-8") as f:
        f.write(sitemap_xml)

    print(f"Successfully regenerated {len(PAGES)} SEO pages!")

if __name__ == "__main__":
    main()
