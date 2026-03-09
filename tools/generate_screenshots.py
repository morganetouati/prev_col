"""
Génère des captures d'écran Play Store pour tablette 7" et 10"
App: Regards au monde - palette indigo nocturne + accents néon
"""

from PIL import Image, ImageDraw, ImageFont
import math
import os

# ── Palette couleurs ──────────────────────────────────────────────────
C_BG          = "#1A1B2E"
C_DARK        = "#0D0E1A"
C_SURFACE     = "#1E2038"
C_CARD        = "#232544"
C_ELEVATED    = "#2A2D52"
C_ACCENT      = "#00F5D4"
C_PURPLE      = "#8B5CF6"
C_CORAL       = "#FF6B6B"
C_AMBER       = "#FFB347"
C_WHITE       = "#FFFFFF"
C_TEXT_SEC    = "#B8B9CC"
C_TEXT_HINT   = "#6B6D8A"
C_DANGER      = "#FF4757"
C_WARNING     = "#FFA502"
C_SAFE        = "#2ED573"
C_GRAD_START  = "#8B5CF6"
C_GRAD_END    = "#00F5D4"

def hex_to_rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))

def lerp_color(c1, c2, t):
    r1, g1, b1 = hex_to_rgb(c1)
    r2, g2, b2 = hex_to_rgb(c2)
    return (int(r1 + (r2 - r1) * t), int(g1 + (g2 - g1) * t), int(b1 + (b2 - b1) * t))

def draw_rounded_rect(draw, xy, radius, fill, outline=None, outline_width=2):
    x0, y0, x1, y1 = xy
    r = radius
    draw.rectangle([x0 + r, y0, x1 - r, y1], fill=fill)
    draw.rectangle([x0, y0 + r, x1, y1 - r], fill=fill)
    draw.ellipse([x0, y0, x0 + 2*r, y0 + 2*r], fill=fill)
    draw.ellipse([x1 - 2*r, y0, x1, y0 + 2*r], fill=fill)
    draw.ellipse([x0, y1 - 2*r, x0 + 2*r, y1], fill=fill)
    draw.ellipse([x1 - 2*r, y1 - 2*r, x1, y1], fill=fill)
    if outline:
        draw.arc([x0, y0, x0 + 2*r, y0 + 2*r], 180, 270, fill=outline, width=outline_width)
        draw.arc([x1 - 2*r, y0, x1, y0 + 2*r], 270, 0, fill=outline, width=outline_width)
        draw.arc([x0, y1 - 2*r, x0 + 2*r, y1], 90, 180, fill=outline, width=outline_width)
        draw.arc([x1 - 2*r, y1 - 2*r, x1, y1], 0, 90, fill=outline, width=outline_width)
        draw.line([x0 + r, y0, x1 - r, y0], fill=outline, width=outline_width)
        draw.line([x0 + r, y1, x1 - r, y1], fill=outline, width=outline_width)
        draw.line([x0, y0 + r, x0, y1 - r], fill=outline, width=outline_width)
        draw.line([x1, y0 + r, x1, y1 - r], fill=outline, width=outline_width)

def draw_gradient_rect(img, xy, color1, color2, vertical=True):
    x0, y0, x1, y1 = xy
    draw = ImageDraw.Draw(img)
    steps = y1 - y0 if vertical else x1 - x0
    for i in range(steps):
        t = i / max(steps - 1, 1)
        c = lerp_color(color1, color2, t)
        if vertical:
            draw.line([(x0, y0 + i), (x1, y0 + i)], fill=c)
        else:
            draw.line([(x0 + i, y0), (x0 + i, y1)], fill=c)

def draw_gradient_roundrect(img, draw, xy, radius, color1, color2):
    x0, y0, x1, y1 = xy
    # Create a mask
    mask = Image.new("L", img.size, 0)
    mask_draw = ImageDraw.Draw(mask)
    draw_rounded_rect(mask_draw, xy, radius, fill=255)
    # Create gradient overlay
    overlay = img.copy()
    draw_gradient_rect(overlay, xy, color1, color2, vertical=True)
    img.paste(overlay, mask=mask)

def get_font(size, bold=False):
    """Try to get a font, fallback to default."""
    try:
        if bold:
            return ImageFont.truetype("C:/Windows/Fonts/arialbd.ttf", size)
        return ImageFont.truetype("C:/Windows/Fonts/arial.ttf", size)
    except:
        try:
            if bold:
                return ImageFont.truetype("C:/Windows/Fonts/calibrib.ttf", size)
            return ImageFont.truetype("C:/Windows/Fonts/calibri.ttf", size)
        except:
            return ImageFont.load_default()

def text_center(draw, x, y, text, font, fill, max_width=None):
    """Draw centered text at (x, y)."""
    try:
        bbox = draw.textbbox((0, 0), text, font=font)
        tw = bbox[2] - bbox[0]
        draw.text((x - tw // 2, y), text, font=font, fill=fill)
    except:
        draw.text((x, y), text, font=font, fill=fill, anchor="mm")

def draw_radar_circle(img, cx, cy, radius, objects=None):
    """Draw a realistic radar HUD."""
    draw = ImageDraw.Draw(img, "RGBA")
    rings = 3
    ring_colors = ["#00F5D415", "#00F5D425", "#00F5D435"]
    
    # Background glow
    for i in range(20, 0, -1):
        alpha = int(80 * (1 - i / 20))
        r_val, g_val, b_val = hex_to_rgb(C_ACCENT)
        draw.ellipse([cx - radius - i, cy - radius - i, cx + radius + i, cy + radius + i],
                     outline=(r_val, g_val, b_val, alpha), width=1)
    
    # Radar background
    draw.ellipse([cx - radius, cy - radius, cx + radius, cy + radius],
                 fill=(*hex_to_rgb("#000820"), 200))
    
    # Grid rings
    for i in range(1, rings + 1):
        r = int(radius * i / rings)
        alpha = 60 + i * 20
        draw.ellipse([cx - r, cy - r, cx + r, cy + r],
                     outline=(*hex_to_rgb(C_ACCENT), alpha), width=1)
    
    # Cross lines
    draw.line([cx - radius, cy, cx + radius, cy],
              fill=(*hex_to_rgb(C_ACCENT), 40), width=1)
    draw.line([cx, cy - radius, cx, cy + radius],
              fill=(*hex_to_rgb(C_ACCENT), 40), width=1)
    
    # Scan sweep (static representation)
    scan_angle = 45  # degrees
    scan_img = Image.new("RGBA", img.size, (0, 0, 0, 0))
    scan_draw = ImageDraw.Draw(scan_img)
    start_angle = scan_angle - 60
    end_angle = scan_angle
    # Gradient sweep
    for i in range(60):
        a = start_angle + i
        alpha = int(150 * (i / 60))
        x_end = cx + int(radius * math.cos(math.radians(a)))
        y_end = cy + int(radius * math.sin(math.radians(a)))
        scan_draw.line([cx, cy, x_end, y_end],
                       fill=(*hex_to_rgb(C_ACCENT), alpha), width=2)
    img.alpha_composite(scan_img)
    draw = ImageDraw.Draw(img, "RGBA")
    
    # Outer ring
    draw.ellipse([cx - radius, cy - radius, cx + radius, cy + radius],
                 outline=(*hex_to_rgb(C_ACCENT), 200), width=2)
    
    # Distance labels
    font_tiny = get_font(max(10, radius // 10))
    label_positions = [(0, 1), (1, 2), (2, 3)]
    labels = ["2m", "5m", "10m"]
    for (i, label) in zip(range(1, rings + 1), labels):
        r = int(radius * i / rings)
        draw.text((cx + 4, cy - r - 14), label,
                  font=font_tiny, fill=(*hex_to_rgb(C_ACCENT), 160))
    
    # Draw detected objects
    if objects:
        for obj in objects:
            dist_norm = obj.get("dist", 0.5)  # 0=centre, 1=bord
            angle_deg = obj.get("angle", 30)
            obj_type = obj.get("type", "ADULTE")
            danger = obj.get("danger", False)
            
            ox = cx + int(radius * dist_norm * math.cos(math.radians(angle_deg)))
            oy = cy + int(radius * dist_norm * math.sin(math.radians(angle_deg)))
            
            color = (*hex_to_rgb(C_DANGER if danger else C_WARNING), 230)
            # Halo
            for i in range(8, 0, -1):
                a = int(60 * (1 - i/8))
                draw.ellipse([ox - i, oy - i, ox + i, oy + i],
                             fill=(*hex_to_rgb(C_DANGER if danger else C_WARNING), a))
            draw.ellipse([ox - 5, oy - 5, ox + 5, oy + 5], fill=color)
            # Label
            font_obj = get_font(max(9, radius // 12))
            draw.text((ox + 8, oy - 6), obj_type, font=font_obj,
                      fill=(*hex_to_rgb(C_DANGER if danger else C_WARNING), 220))
    
    # "VOUS" label at center
    font_you = get_font(max(11, radius // 9), bold=True)
    text_center(draw, cx, cy + radius // 2 + 10, "VOUS", font_you,
                (*hex_to_rgb(C_WHITE), 200))
    
    # Center dot (you)
    draw.ellipse([cx - 6, cy - 6, cx + 6, cy + 6],
                 fill=(*hex_to_rgb(C_ACCENT), 255))
    draw.ellipse([cx - 3, cy - 3, cx + 3, cy + 3],
                 fill=(*hex_to_rgb(C_WHITE), 255))


def generate_screenshot_7inch():
    """Tablette 7 pouces — Écran principal avec stats et badges."""
    W, H = 1200, 1920
    scale = 2.2  # Facteur d'échelle pour rendre lisible

    img = Image.new("RGBA", (W, H), hex_to_rgb(C_BG))
    draw = ImageDraw.Draw(img)

    # Fond gradient subtil
    for y in range(H):
        t = y / H
        c = lerp_color(C_BG, C_DARK, t * 0.4)
        draw.line([(0, y), (W, y)], fill=c)

    # Subtle background stars/particles
    import random
    random.seed(42)
    for _ in range(60):
        sx, sy = random.randint(0, W), random.randint(0, H // 2)
        alpha = random.randint(20, 80)
        draw.ellipse([sx-1, sy-1, sx+1, sy+1],
                     fill=(*hex_to_rgb(C_WHITE), alpha))

    pad = 48  # padding horizontal
    y = 60

    # ── Language button (top right) ──────────────────────────────────
    font_flag = get_font(42)
    draw.text((W - pad - 60, y), "🇫🇷", font=font_flag, fill=C_WHITE)
    y += 20

    # ── Header ──────────────────────────────────────────────────────
    icon_size = 110
    icon_cx = W // 2
    icon_cy = y + 80
    # Gradient circle background
    for i in range(icon_size // 2, 0, -1):
        t = 1 - i / (icon_size // 2)
        c = lerp_color(C_GRAD_START, C_GRAD_END, t)
        draw.ellipse([icon_cx - i, icon_cy - i, icon_cx + i, icon_cy + i], fill=c)
    # Eye icon
    font_icon = get_font(52)
    text_center(draw, icon_cx, icon_cy - 32, "👁️", font_icon, C_WHITE)

    y = icon_cy + icon_size // 2 + 20
    font_title = get_font(54, bold=True)
    font_subtitle = get_font(30)
    text_center(draw, W // 2, y, "Regards au monde", font_title, C_WHITE)
    y += 70
    text_center(draw, W // 2, y, "Vos statistiques et badges", font_subtitle, C_TEXT_SEC)
    y += 55

    # ── Hint ─────────────────────────────────────────────────────────
    font_hint = get_font(26)
    text_center(draw, W // 2, y,
                "💡 Activez la surveillance depuis les Réglages rapides (👁️)",
                font_hint, C_TEXT_HINT)
    y += 60

    # ── Stats Card ──────────────────────────────────────────────────
    card_pad = 40
    card_x0, card_x1 = pad, W - pad
    card_y0 = y
    card_y1 = card_y0 + 280
    draw_rounded_rect(draw, [card_x0, card_y0, card_x1, card_y1],
                      radius=24, fill=hex_to_rgb(C_CARD),
                      outline=hex_to_rgb(C_ELEVATED), outline_width=1)
    # Accent line at top of card
    for i in range(4):
        t = i / 3
        c = lerp_color(C_GRAD_START, C_GRAD_END, t)
        draw.line([card_x0 + 24, card_y0 + i, card_x1 - 24, card_y0 + i], fill=c)

    cy = card_y0 + card_pad
    font_card_title = get_font(40, bold=True)
    font_stat = get_font(32)
    draw.text((card_x0 + card_pad, cy), "📊 Statistiques",
              font=font_card_title, fill=C_WHITE)
    cy += 64
    draw.text((card_x0 + card_pad, cy),
              "🎯 Points: 347 | 👁️ Confirmé", font=font_stat, fill=C_ACCENT)
    cy += 48
    draw.text((card_x0 + card_pad, cy),
              "⚠️ Alertes évitées: 89", font=font_stat, fill=C_TEXT_SEC)
    cy += 48
    draw.text((card_x0 + card_pad, cy),
              "⚡ Approches rapides: 23", font=font_stat, fill=C_TEXT_SEC)
    y = card_y1 + 32

    # ── Badges Card ─────────────────────────────────────────────────
    card_y0 = y
    card_y1 = card_y0 + 340
    draw_rounded_rect(draw, [card_x0, card_y0, card_x1, card_y1],
                      radius=24, fill=hex_to_rgb(C_CARD),
                      outline=hex_to_rgb(C_ELEVATED), outline_width=1)
    for i in range(4):
        t = i / 3
        c = lerp_color(C_PURPLE, C_GRAD_END, t)
        draw.line([card_x0 + 24, card_y0 + i, card_x1 - 24, card_y0 + i], fill=c)

    cy = card_y0 + card_pad
    draw.text((card_x0 + card_pad, cy), "🏆 Badges débloqués",
              font=font_card_title, fill=C_WHITE)
    cy += 64
    badges = [
        "🥉 Premier Regard — 1ère alerte évitée",
        "⚡ Speedster — 1ère approche rapide",
        "🥇 Gardien de la Rue — 100 points",
        "👁️ Confirmé — Niveau 3",
    ]
    font_badge = get_font(28)
    for badge in badges:
        draw.text((card_x0 + card_pad, cy), badge, font=font_badge, fill=C_TEXT_SEC)
        cy += 46
    y = card_y1 + 40

    # ── Progress bar ─────────────────────────────────────────────────
    font_prog = get_font(26)
    draw.text((card_x0, y), "➡️  153 points avant niveau Expert ⚡",
              font=font_prog, fill=C_TEXT_HINT)
    y += 42
    bar_h = 10
    bar_bg = [card_x0, y, card_x1, y + bar_h]
    draw_rounded_rect(draw, bar_bg, radius=5, fill=hex_to_rgb(C_ELEVATED))
    progress = 0.69
    bar_fill = [card_x0, y, card_x0 + int((card_x1 - card_x0) * progress), y + bar_h]
    # Gradient progress
    draw_gradient_rect(img, bar_fill, C_GRAD_START, C_GRAD_END, vertical=False)
    y += 48

    # ── Primary button (Actualiser) ──────────────────────────────────
    y += 16
    btn_h = 100
    btn_rect = [pad, y, W - pad, y + btn_h]
    draw_gradient_roundrect(img, draw, btn_rect, 20, C_GRAD_START, C_GRAD_END)
    font_btn = get_font(36, bold=True)
    text_center(draw, W // 2, y + 24, "🔄 Actualiser", font_btn, C_WHITE)
    y += btn_h + 20

    # ── Secondary buttons ────────────────────────────────────────────
    font_btn2 = get_font(28)
    btn2_h = 74
    btn2_rect = [pad, y, W - pad, y + btn2_h]
    draw_rounded_rect(draw, btn2_rect, radius=16, fill=hex_to_rgb(C_CARD))
    text_center(draw, W // 2, y + 18, "🔐 Paramètres de confidentialité pub",
                font_btn2, C_TEXT_SEC)
    y += btn2_h + 16
    btn3_rect = [pad, y, W - pad, y + btn2_h]
    draw_rounded_rect(draw, btn3_rect, radius=16, fill=hex_to_rgb(C_CARD))
    text_center(draw, W // 2, y + 18, "ℹ️  Fiche de l'application",
                font_btn2, C_TEXT_SEC)
    y += btn2_h + 32

    # ── Notification status bar (active) ────────────────────────────
    notif_h = 110
    notif_rect = [pad, y, W - pad, y + notif_h]
    draw_rounded_rect(draw, notif_rect, radius=20,
                      fill=hex_to_rgb(C_ELEVATED),
                      outline=hex_to_rgb(C_ACCENT), outline_width=2)
    font_notif = get_font(28)
    draw.text((pad + 30, y + 18), "👁️ Regards au monde", font=get_font(28, bold=True), fill=C_ACCENT)
    draw.text((pad + 30, y + 58), "🚶 En marche — Détection active · Maintenant", font=font_notif, fill=C_TEXT_SEC)
    # Stop button
    stop_rect = [W - pad - 130, y + 24, W - pad - 10, y + notif_h - 24]
    draw_rounded_rect(draw, stop_rect, radius=10, fill=hex_to_rgb(C_CORAL))
    text_center(draw, W - pad - 70, y + 40, "Arrêter", get_font(24, bold=True), C_WHITE)
    y += notif_h + 30

    # ── Ad banner at bottom ──────────────────────────────────────────
    ad_h = 110
    ad_y = H - ad_h - 10
    draw.rectangle([0, ad_y, W, H], fill=hex_to_rgb(C_DARK))
    draw.line([0, ad_y, W, ad_y], fill=hex_to_rgb(C_ELEVATED), width=1)
    text_center(draw, W // 2, ad_y + 36,
                "PUBLICITÉ", get_font(24), C_TEXT_HINT)

    # ── Status bar (top) ─────────────────────────────────────────────
    draw.rectangle([0, 0, W, 50], fill=(*hex_to_rgb(C_DARK), 220))
    draw.text((20, 14), "9:41", font=get_font(24, bold=True), fill=C_WHITE)
    draw.text((W - 120, 14), "📶 🔋", font=get_font(24), fill=C_WHITE)

    # Finalize
    img_final = img.convert("RGB")
    return img_final


def generate_screenshot_10inch():
    """Tablette 10 pouces — Détection active avec Radar HUD."""
    W, H = 1600, 2560
    img = Image.new("RGBA", (W, H), hex_to_rgb(C_BG))
    draw = ImageDraw.Draw(img)

    # Fond
    for y in range(H):
        t = y / H
        c = lerp_color(C_BG, "#0A0B18", t * 0.5)
        draw.line([(0, y), (W, y)], fill=c)

    # Stars
    import random
    random.seed(99)
    for _ in range(100):
        sx, sy = random.randint(0, W), random.randint(0, H * 2 // 3)
        alpha = random.randint(15, 70)
        draw.ellipse([sx - 1, sy - 1, sx + 1, sy + 1],
                     fill=(*hex_to_rgb(C_WHITE), alpha))

    pad = 60
    y = 40

    # ── Status bar ───────────────────────────────────────────────────
    draw.rectangle([0, 0, W, 60], fill=(*hex_to_rgb(C_DARK), 220))
    draw.text((24, 16), "9:41", font=get_font(30, bold=True), fill=C_WHITE)
    draw.text((W - 140, 16), "📶 🔋", font=get_font(30), fill=C_WHITE)
    y = 80

    # ── Alert banner ─────────────────────────────────────────────────
    alert_h = 100
    alert_rect = [pad, y, W - pad, y + alert_h]
    draw_rounded_rect(draw, alert_rect, radius=20,
                      fill=hex_to_rgb(C_DANGER),
                      outline=(*hex_to_rgb(C_CORAL), 200), outline_width=3)
    font_alert = get_font(40, bold=True)
    text_center(draw, W // 2, y + 22, "🔴 DANGER : Adulte très proche ! (1.2m)", font_alert, C_WHITE)
    y += alert_h + 30

    # ── Header compact ───────────────────────────────────────────────
    font_head = get_font(52, bold=True)
    font_lang = get_font(50)
    draw.text((W - pad - 70, y + 5), "🇫🇷", font=font_lang, fill=C_WHITE)

    # Eye icon (smaller)
    icon_r = 60
    icon_cx = pad + icon_r + 10
    icon_cy = y + icon_r + 10
    for i in range(icon_r, 0, -1):
        t = 1 - i / icon_r
        c = lerp_color(C_GRAD_START, C_GRAD_END, t)
        draw.ellipse([icon_cx - i, icon_cy - i, icon_cx + i, icon_cy + i], fill=c)
    text_center(draw, icon_cx, icon_cy - 36, "👁️", get_font(48), C_WHITE)
    draw.text((icon_cx + icon_r + 20, y + 10), "Regards au monde", font=font_head, fill=C_WHITE)
    draw.text((icon_cx + icon_r + 22, y + 72), "Détection active · Mode Caméra",
              font=get_font(34), fill=C_ACCENT)
    y += 160

    # ── RADAR (main feature) ─────────────────────────────────────────
    radar_radius = 380
    radar_cx = W // 2
    radar_cy = y + radar_radius + 20

    radar_objects = [
        {"dist": 0.3, "angle": 30, "type": "ADULTE", "danger": True},
        {"dist": 0.65, "angle": 130, "type": "ADULTE", "danger": False},
        {"dist": 0.78, "angle": 220, "type": "ENFANT", "danger": False},
    ]
    draw_radar_circle(img, radar_cx, radar_cy, radar_radius, radar_objects)
    draw = ImageDraw.Draw(img, "RGBA")

    # Distance label arrow
    font_dist = get_font(38, bold=True)
    dist_x = radar_cx + int(radar_radius * 0.3 * math.cos(math.radians(30)))
    dist_y = radar_cy + int(radar_radius * 0.3 * math.sin(math.radians(30)))
    # Connecting line to label
    draw.line([dist_x, dist_y, dist_x + 80, dist_y - 60],
              fill=(*hex_to_rgb(C_DANGER), 200), width=2)
    draw.text((dist_x + 85, dist_y - 90), "1.2m ⚡", font=font_dist, fill=C_DANGER)

    y = radar_cy + radar_radius + 50

    # ── Detection details cards ──────────────────────────────────────
    card_w = (W - pad * 2 - 30) // 3
    cards = [
        {"icon": "🔴", "label": "ADULTE", "dist": "1.2m", "dir": "↙ Approche rapide", "color": C_DANGER},
        {"icon": "🟠", "label": "ADULTE", "dist": "4.1m", "dir": "→ Latéral", "color": C_WARNING},
        {"icon": "🟡", "label": "ENFANT", "dist": "6.3m", "dir": "↗ S'éloigne", "color": C_AMBER},
    ]
    cx_card = pad
    for card in cards:
        c_rect = [cx_card, y, cx_card + card_w, y + 220]
        draw_rounded_rect(draw, c_rect, radius=20, fill=hex_to_rgb(C_CARD),
                          outline=hex_to_rgb(card["color"]), outline_width=2)
        for i in range(3):
            t = i / 2
            c = lerp_color(card["color"], C_CARD, 0.7)
            draw.line([cx_card + 20, y + i, cx_card + card_w - 20, y + i], fill=c)

        ci = cx_card + card_w // 2
        draw.text((ci - 20, y + 20), card["icon"], font=get_font(52), fill=card["color"])
        draw.text((cx_card + 16, y + 84), card["label"],
                  font=get_font(36, bold=True), fill=C_WHITE)
        draw.text((cx_card + 16, y + 130), card["dist"],
                  font=get_font(44, bold=True), fill=card["color"])
        draw.text((cx_card + 16, y + 178), card["dir"],
                  font=get_font(26), fill=C_TEXT_SEC)
        cx_card += card_w + 15
    y += 240

    # ── Stats summary ────────────────────────────────────────────────
    y += 20
    stat_rect = [pad, y, W - pad, y + 180]
    draw_rounded_rect(draw, stat_rect, radius=24, fill=hex_to_rgb(C_CARD))
    for i in range(4):
        t = i / 3
        c = lerp_color(C_GRAD_START, C_GRAD_END, t)
        draw.line([pad + 24, y + i, W - pad - 24, y + i], fill=c)

    col_w = (W - pad * 2) // 4
    stats = [
        ("🎯", "347", "Points"),
        ("⚠️", "89", "Alertes évitées"),
        ("⚡", "23", "Approches"),
        ("🔥", "5j", "Série"),
    ]
    sx = pad
    for icon, val, lbl in stats:
        cx_s = sx + col_w // 2
        draw.text((cx_s - 20, y + 24), icon, font=get_font(44), fill=C_WHITE)
        text_center(draw, cx_s, y + 78, val, get_font(50, bold=True), C_ACCENT)
        text_center(draw, cx_s, y + 136, lbl, get_font(26), C_TEXT_HINT)
        sx += col_w
    y += 200

    # ── Notification bar ─────────────────────────────────────────────
    y += 20
    notif_h = 120
    notif_rect = [pad, y, W - pad, y + notif_h]
    draw_rounded_rect(draw, notif_rect, radius=20,
                      fill=hex_to_rgb(C_ELEVATED),
                      outline=hex_to_rgb(C_ACCENT), outline_width=2)
    draw.text((pad + 36, y + 20), "👁️ Regards au monde", font=get_font(32, bold=True), fill=C_ACCENT)
    draw.text((pad + 36, y + 66), "👀 Caméra active — Détection! · Maintenant",
              font=get_font(28), fill=C_TEXT_SEC)
    stop_rect = [W - pad - 160, y + 26, W - pad - 16, y + notif_h - 26]
    draw_rounded_rect(draw, stop_rect, radius=12, fill=hex_to_rgb(C_CORAL))
    text_center(draw, W - pad - 88, y + 46, "Arrêter", get_font(28, bold=True), C_WHITE)
    y += notif_h + 30

    # ── Badges row ───────────────────────────────────────────────────
    y += 10
    draw.text((pad, y), "🏆 Badges : 🥉🥇⚡👁️",
              font=get_font(34), fill=C_TEXT_SEC)
    y += 55

    # ── Level progress ───────────────────────────────────────────────
    draw.text((pad, y), "👁️ Confirmé  →  153 pts avant Expert ⚡",
              font=get_font(30), fill=C_TEXT_HINT)
    y += 44
    bar_rect = [pad, y, W - pad, y + 14]
    draw_rounded_rect(draw, bar_rect, radius=7, fill=hex_to_rgb(C_ELEVATED))
    fill_end = pad + int((W - 2 * pad) * 0.69)
    draw_gradient_rect(img, [pad, y, fill_end, y + 14], C_GRAD_START, C_GRAD_END, vertical=False)
    y += 50

    # ── Ad banner ────────────────────────────────────────────────────
    ad_h = 130
    ad_y = H - ad_h - 10
    draw.rectangle([0, ad_y, W, H], fill=hex_to_rgb(C_DARK))
    draw.line([0, ad_y, W, ad_y], fill=hex_to_rgb(C_ELEVATED), width=1)
    text_center(draw, W // 2, ad_y + 44,
                "PUBLICITÉ", get_font(28), C_TEXT_HINT)

    return img.convert("RGB")


def main():
    out_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), "screenshots")
    os.makedirs(out_dir, exist_ok=True)

    print("📱 Génération capture tablette 7\"...")
    img7 = generate_screenshot_7inch()
    path7 = os.path.join(out_dir, "tablet_7inch_1200x1920.png")
    img7.save(path7, "PNG", optimize=True)
    size7 = os.path.getsize(path7) // 1024
    print(f"   ✅ {path7}  ({img7.size[0]}×{img7.size[1]}px, {size7} KB)")

    print("📱 Génération capture tablette 10\"...")
    img10 = generate_screenshot_10inch()
    path10 = os.path.join(out_dir, "tablet_10inch_1600x2560.png")
    img10.save(path10, "PNG", optimize=True)
    size10 = os.path.getsize(path10) // 1024
    print(f"   ✅ {path10}  ({img10.size[0]}×{img10.size[1]}px, {size10} KB)")

    print("\n🎉 Captures générées dans le dossier 'screenshots/'")


if __name__ == "__main__":
    main()
