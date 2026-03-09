"""
Captures supplémentaires Play Store — 2x tablette 7" + 2x tablette 10"
  7"-B : Écran Alerte Active (vibration orange + caméra)
  7"-C : Écran Confidentialité / Onboarding
 10"-B : Détection Enfant + Gamification badges
 10"-C : État SAFE — Aucune détection, radar vert
"""

from PIL import Image, ImageDraw, ImageFont
import math, os, random

# ── Palette ──────────────────────────────────────────────────────────
C_BG         = "#1A1B2E"; C_DARK       = "#0D0E1A"
C_SURFACE    = "#1E2038"; C_CARD       = "#232544"
C_ELEVATED   = "#2A2D52"; C_ACCENT     = "#00F5D4"
C_PURPLE     = "#8B5CF6"; C_CORAL      = "#FF6B6B"
C_AMBER      = "#FFB347"; C_WHITE      = "#FFFFFF"
C_TEXT_SEC   = "#B8B9CC"; C_TEXT_HINT  = "#6B6D8A"
C_DANGER     = "#FF4757"; C_WARNING    = "#FFA502"
C_SAFE       = "#2ED573"; C_GRAD_START = "#8B5CF6"
C_GRAD_END   = "#00F5D4"

def h2r(h): h=h.lstrip("#"); return tuple(int(h[i:i+2],16) for i in (0,2,4))

def lerp(c1, c2, t):
    r1,g1,b1=h2r(c1); r2,g2,b2=h2r(c2)
    return (int(r1+(r2-r1)*t), int(g1+(g2-g1)*t), int(b1+(b2-b1)*t))

def rr(draw, xy, r, fill, outline=None, ow=2):
    x0,y0,x1,y1=xy
    if fill is not None:
        draw.rectangle([x0+r,y0,x1-r,y1], fill=fill)
        draw.rectangle([x0,y0+r,x1,y1-r], fill=fill)
        for cx,cy in [(x0,y0),(x1-2*r,y0),(x0,y1-2*r),(x1-2*r,y1-2*r)]:
            draw.ellipse([cx,cy,cx+2*r,cy+2*r], fill=fill)
    if outline:
        for a0,a1,cx,cy in [(180,270,x0,y0),(270,360,x1-2*r,y0),(90,180,x0,y1-2*r),(0,90,x1-2*r,y1-2*r)]:
            draw.arc([cx,cy,cx+2*r,cy+2*r],a0,a1,fill=outline,width=ow)
        draw.line([x0+r,y0,x1-r,y0],fill=outline,width=ow)
        draw.line([x0+r,y1,x1-r,y1],fill=outline,width=ow)
        draw.line([x0,y0+r,x0,y1-r],fill=outline,width=ow)
        draw.line([x1,y0+r,x1,y1-r],fill=outline,width=ow)

def grad_rect(img, xy, c1, c2, vert=True):
    x0,y0,x1,y1=xy; draw=ImageDraw.Draw(img)
    n = y1-y0 if vert else x1-x0
    for i in range(n):
        t=i/max(n-1,1); c=lerp(c1,c2,t)
        if vert: draw.line([(x0,y0+i),(x1,y0+i)],fill=c)
        else:    draw.line([(x0+i,y0),(x0+i,y1)],fill=c)

def grad_rr(img, draw, xy, radius, c1, c2):
    mask=Image.new("L",img.size,0); md=ImageDraw.Draw(mask)
    rr(md,xy,radius,fill=255)
    ov=img.copy(); grad_rect(ov,xy,c1,c2,vert=True)
    img.paste(ov,mask=mask)

def font(sz, bold=False):
    for name in (("arialbd" if bold else "arial"), ("calibrib" if bold else "calibri")):
        try: return ImageFont.truetype(f"C:/Windows/Fonts/{name}.ttf", sz)
        except: pass
    return ImageFont.load_default()

def tc(draw, x, y, txt, f, fill):
    try:
        bb=draw.textbbox((0,0),txt,font=f); tw=bb[2]-bb[0]
        draw.text((x-tw//2,y),txt,font=f,fill=fill)
    except:
        draw.text((x,y),txt,font=f,fill=fill)

def bg_base(W, H, seed=42):
    img=Image.new("RGBA",(W,H),h2r(C_BG)); draw=ImageDraw.Draw(img)
    for y in range(H):
        c=lerp(C_BG,"#0A0B18",y/H*0.45); draw.line([(0,y),(W,y)],fill=c)
    random.seed(seed)
    for _ in range(70):
        sx,sy=random.randint(0,W),random.randint(0,H//2)
        a=random.randint(15,70)
        draw.ellipse([sx-1,sy-1,sx+1,sy+1],fill=(*h2r(C_WHITE),a))
    return img

def status_bar(draw, W, f_sz=24):
    draw.rectangle([0,0,W,56],fill=(*h2r(C_DARK),220))
    draw.text((22,16),"9:41",font=font(f_sz,True),fill=C_WHITE)
    draw.text((W-130,16),"📶 🔋",font=font(f_sz),fill=C_WHITE)

def radar_safe(img, cx, cy, radius):
    draw=ImageDraw.Draw(img,"RGBA")
    # glow
    for i in range(20,0,-1):
        a=int(60*(1-i/20)); r,g,b=h2r(C_SAFE)
        draw.ellipse([cx-radius-i,cy-radius-i,cx+radius+i,cy+radius+i],
                     outline=(r,g,b,a),width=1)
    draw.ellipse([cx-radius,cy-radius,cx+radius,cy+radius],fill=(*h2r("#000820"),200))
    for i in range(1,4):
        r_=int(radius*i/3); a=50+i*25
        draw.ellipse([cx-r_,cy-r_,cx+r_,cy+r_],outline=(*h2r(C_SAFE),a),width=1)
    draw.line([cx-radius,cy,cx+radius,cy],fill=(*h2r(C_SAFE),30),width=1)
    draw.line([cx,cy-radius,cx,cy+radius],fill=(*h2r(C_SAFE),30),width=1)
    # sweep (green)
    for i in range(60):
        a=int(int(120)*(i/60)); ang=100+i
        xe=cx+int(radius*math.cos(math.radians(ang)))
        ye=cy+int(radius*math.sin(math.radians(ang)))
        draw.line([cx,cy,xe,ye],fill=(*h2r(C_SAFE),a),width=2)
    draw.ellipse([cx-radius,cy-radius,cx+radius,cy+radius],
                 outline=(*h2r(C_SAFE),180),width=2)
    draw.ellipse([cx-6,cy-6,cx+6,cy+6],fill=(*h2r(C_SAFE),255))
    draw.ellipse([cx-3,cy-3,cx+3,cy+3],fill=(*h2r(C_WHITE),255))
    tf=font(max(12,radius//9),True)
    tc(draw,cx,cy+radius//2+10,"VOUS",tf,(*h2r(C_WHITE),200))
    draw.text((cx+4,cy-int(radius*1/3)-16),"2m",font=font(max(10,radius//10)),fill=(*h2r(C_SAFE),140))
    draw.text((cx+4,cy-int(radius*2/3)-16),"5m",font=font(max(10,radius//10)),fill=(*h2r(C_SAFE),140))
    draw.text((cx+4,cy-radius-2),"10m",font=font(max(10,radius//10)),fill=(*h2r(C_SAFE),140))

def radar_warning(img, cx, cy, radius, objs):
    draw=ImageDraw.Draw(img,"RGBA")
    for i in range(20,0,-1):
        a=int(80*(1-i/20)); r,g,b=h2r(C_WARNING)
        draw.ellipse([cx-radius-i,cy-radius-i,cx+radius+i,cy+radius+i],
                     outline=(r,g,b,a),width=1)
    draw.ellipse([cx-radius,cy-radius,cx+radius,cy+radius],fill=(*h2r("#000820"),200))
    for i in range(1,4):
        r_=int(radius*i/3); a=50+i*25
        draw.ellipse([cx-r_,cy-r_,cx+r_,cy+r_],outline=(*h2r(C_ACCENT),a),width=1)
    draw.line([cx-radius,cy,cx+radius,cy],fill=(*h2r(C_ACCENT),30),width=1)
    draw.line([cx,cy-radius,cx,cy+radius],fill=(*h2r(C_ACCENT),30),width=1)
    for i in range(60):
        a=int(150*(i/60)); ang=-20+i
        xe=cx+int(radius*math.cos(math.radians(ang)))
        ye=cy+int(radius*math.sin(math.radians(ang)))
        draw.line([cx,cy,xe,ye],fill=(*h2r(C_ACCENT),a),width=2)
    draw.ellipse([cx-radius,cy-radius,cx+radius,cy+radius],
                 outline=(*h2r(C_WARNING),200),width=2)
    for obj in objs:
        dn,ang,danger=obj["dist"],obj["angle"],obj.get("danger",False)
        ox=cx+int(radius*dn*math.cos(math.radians(ang)))
        oy=cy+int(radius*dn*math.sin(math.radians(ang)))
        col=C_DANGER if danger else C_WARNING
        for i in range(10,0,-1):
            draw.ellipse([ox-i,oy-i,ox+i,oy+i],fill=(*h2r(col),int(50*(1-i/10))))
        draw.ellipse([ox-6,oy-6,ox+6,oy+6],fill=(*h2r(col),230))
        draw.text((ox+9,oy-8),obj.get("type","?"),font=font(max(9,radius//13)),
                  fill=(*h2r(col),210))
    draw.ellipse([cx-6,cy-6,cx+6,cy+6],fill=(*h2r(C_ACCENT),255))
    draw.ellipse([cx-3,cy-3,cx+3,cy+3],fill=(*h2r(C_WHITE),255))
    tc(draw,cx,cy+radius//2+10,"VOUS",font(max(12,radius//9),True),(*h2r(C_WHITE),200))


# ═══════════════════════════════════════════════════════════════════════
# 7"-B  :  Alerte active — caméra + vibration orange
# ═══════════════════════════════════════════════════════════════════════
def screenshot_7b():
    W,H=1200,1920; pad=48
    img=bg_base(W,H,seed=7)
    draw=ImageDraw.Draw(img)
    status_bar(draw,W)

    # Pulsing alert header
    alert_h=130
    rr(draw,[0,0,W,alert_h+56],16,fill=h2r(C_DANGER))
    for i in range(3):
        a=int(60*(1-i/3))
        draw.rectangle([0,0,W,alert_h+56+i*6],outline=(*h2r(C_CORAL),a),width=2)
    f_alert=font(44,True)
    tc(draw,W//2,68,"🟠 ALERTE : Adulte s'approche (2.8m)",f_alert,C_WHITE)

    y=200

    # Language + title
    draw.text((W-pad-70,y),"🇫🇷",font=font(40),fill=C_WHITE)
    icon_r=56; icx=pad+icon_r+10; icy=y+icon_r+10
    for i in range(icon_r,0,-1):
        t=1-i/icon_r; c=lerp(C_GRAD_START,C_GRAD_END,t)
        draw.ellipse([icx-i,icy-i,icx+i,icy+i],fill=c)
    tc(draw,icx,icy-34,"👁️",font(44),C_WHITE)
    draw.text((icx+icon_r+20,y+8),"Regards au monde",font=font(46,True),fill=C_WHITE)
    draw.text((icx+icon_r+22,y+64),"Mode Caméra · Détection active",font=font(30),fill=C_WARNING)
    y+=148

    # Camera preview placeholder
    cam_h=480
    rr(draw,[pad,y,W-pad,y+cam_h],20,fill=h2r("#0A0B18"),
       outline=h2r(C_WARNING),ow=3)
    # Fake camera grid lines
    for xi in range(1,3):
        draw.line([pad+int((W-2*pad)*xi/3),y,pad+int((W-2*pad)*xi/3),y+cam_h],
                  fill=(*h2r(C_WHITE),20),width=1)
    for yi in range(1,3):
        draw.line([pad,y+int(cam_h*yi/3),W-pad,y+int(cam_h*yi/3)],
                  fill=(*h2r(C_WHITE),20),width=1)
    # Person silhouette suggestion
    cx_p,cy_p=W//2,y+cam_h//2
    # body oval
    draw.ellipse([cx_p-90,cy_p-160,cx_p+90,cy_p+180],fill=(*h2r("#1a1a2e"),150),
                 outline=(*h2r(C_WARNING),180),width=3)
    # head
    draw.ellipse([cx_p-50,cy_p-220,cx_p+50,cy_p-130],fill=(*h2r("#1a1a2e"),150),
                 outline=(*h2r(C_WARNING),180),width=3)
    # detection box
    rr(draw,[cx_p-110,cy_p-240,cx_p+110,cy_p+200],4,fill=None,
       outline=h2r(C_WARNING),ow=3)
    draw.text((cx_p-100,cy_p-270),"ADULTE 91%",font=font(28,True),fill=C_WARNING)
    # distance badge
    rr(draw,[cx_p-70,cy_p+210,cx_p+70,cy_p+258],12,fill=h2r(C_WARNING))
    tc(draw,cx_p,cy_p+224,"2.8m",font(28,True),C_DARK)
    # corner brackets
    for bx,by,dx,dy in [(pad+10,y+8,1,1),(W-pad-10,y+8,-1,1),
                        (pad+10,y+cam_h-8,1,-1),(W-pad-10,y+cam_h-8,-1,-1)]:
        draw.line([bx,by,bx+dx*40,by],fill=C_WARNING,width=4)
        draw.line([bx,by,bx,by+dy*40],fill=C_WARNING,width=4)
    y+=cam_h+28

    # Vibration indicator
    vib_y=y; vib_h=96
    rr(draw,[pad,vib_y,W-pad,vib_y+vib_h],18,fill=h2r(C_ELEVATED),
       outline=h2r(C_WARNING),ow=2)
    draw.text((pad+30,vib_y+20),"📳  Vibration FORTE — Approche détectée",
              font=font(30),fill=C_WARNING)
    draw.text((W-pad-220,vib_y+28),"⚡ Rapide",font=font(28,True),fill=C_CORAL)
    y+=vib_h+22

    # Stats mini-card
    rr(draw,[pad,y,W-pad,y+220],20,fill=h2r(C_CARD))
    for i in range(3):
        c=lerp(C_WARNING,C_CARD,0.5)
        draw.line([pad+20,y+i,W-pad-20,y+i],fill=c)
    draw.text((pad+30,y+24),"📊 Session en cours",font=font(36,True),fill=C_WHITE)
    draw.text((pad+30,y+78),"⚠️  Alertes évitées: 12",font=font(32),fill=C_TEXT_SEC)
    draw.text((pad+30,y+126),"⚡ Approches rapides: 4   🎯 Points: +18",
              font=font(30),fill=C_TEXT_HINT)
    y+=240

    # Notification bar
    notif_h=110
    rr(draw,[pad,y,W-pad,y+notif_h],20,fill=h2r(C_ELEVATED),
       outline=h2r(C_ACCENT),ow=2)
    draw.text((pad+28,y+18),"👁️ Regards au monde",font=font(28,True),fill=C_ACCENT)
    draw.text((pad+28,y+60),"👀 Caméra active — Détection! · Maintenant",
              font=font(26),fill=C_TEXT_SEC)
    stop_r=[W-pad-140,y+24,W-pad-12,y+notif_h-24]
    rr(draw,stop_r,10,fill=h2r(C_CORAL))
    tc(draw,W-pad-76,y+40,"Arrêter",font(24,True),C_WHITE)
    y+=notif_h+28

    # Radar mini
    rad_r=200; rad_cx=W//2; rad_cy=y+rad_r+10
    radar_warning(img,rad_cx,rad_cy,rad_r,[
        {"dist":0.28,"angle":-20,"type":"ADULTE","danger":True},
    ])
    draw=ImageDraw.Draw(img)
    y=rad_cy+rad_r+30

    # Level progress
    draw.text((pad,y),"👁️ Confirmé  →  141 pts avant Expert ⚡",
              font=font(26),fill=C_TEXT_HINT)
    y+=40
    rr(draw,[pad,y,W-pad,y+10],5,fill=h2r(C_ELEVATED))
    prog_end=pad+int((W-2*pad)*0.72)
    grad_rect(img,[pad,y,prog_end,y+10],C_GRAD_START,C_GRAD_END,vert=False)
    draw=ImageDraw.Draw(img)
    y+=46

    # Ad banner
    ad_y=H-106
    draw.rectangle([0,ad_y,W,H],fill=h2r(C_DARK))
    draw.line([0,ad_y,W,ad_y],fill=h2r(C_ELEVATED),width=1)
    tc(draw,W//2,ad_y+36,"PUBLICITÉ",font(24),C_TEXT_HINT)

    return img.convert("RGB")


# ═══════════════════════════════════════════════════════════════════════
# 7"-C  :  Écran Confidentialité / Onboarding
# ═══════════════════════════════════════════════════════════════════════
def screenshot_7c():
    W,H=1200,1920; pad=56
    img=bg_base(W,H,seed=13)
    draw=ImageDraw.Draw(img)
    status_bar(draw,W)

    y=90

    # Big hero icon
    icon_r=110; icx=W//2; icy=y+icon_r+30
    for i in range(icon_r+20,0,-1):
        t=1-i/(icon_r+20); c=lerp(C_GRAD_START,C_GRAD_END,t)
        a=max(0,int(200*(1-i/(icon_r+20))))
        fill=(*c,a) if i>icon_r else c
        draw.ellipse([icx-i,icy-i,icx+i,icy+i],fill=fill)
    tc(draw,icx,icy-66,"👁️",font(80),C_WHITE)
    y=icy+icon_r+30

    tc(draw,W//2,y,"Regards au monde",font(54,True),C_WHITE); y+=72
    tc(draw,W//2,y,"Votre garde du corps discret",font(34),C_TEXT_SEC); y+=72

    # Privacy section card
    sec_h=380
    rr(draw,[pad,y,W-pad,y+sec_h],24,fill=h2r(C_CARD),
       outline=h2r(C_ELEVATED),ow=1)
    for i in range(4):
        c=lerp(C_GRAD_START,C_GRAD_END,i/3)
        draw.line([pad+24,y+i,W-pad-24,y+i],fill=c)
    cy=y+42
    draw.text((pad+36,cy),"🔒 Votre vie privée protégée",font=font(40,True),fill=C_WHITE)
    cy+=66
    guarantees=[
        "✅  Traitement 100% sur votre téléphone",
        "✅  Aucune image stockée ou transmise",
        "✅  Aucun compte requis",
        "✅  Aucune donnée personnelle collectée",
        "✅  Fonctionne sans internet",
    ]
    for g in guarantees:
        draw.text((pad+36,cy),g,font=font(30),fill=C_TEXT_SEC); cy+=46
    y+=sec_h+28

    # Permissions card
    perm_h=300
    rr(draw,[pad,y,W-pad,y+perm_h],24,fill=h2r(C_CARD),
       outline=h2r(C_ELEVATED),ow=1)
    for i in range(4):
        c=lerp(C_PURPLE,C_ACCENT,i/3)
        draw.line([pad+24,y+i,W-pad-24,y+i],fill=c)
    cy=y+42
    draw.text((pad+36,cy),"📋 Permissions demandées",font=font(40,True),fill=C_WHITE); cy+=66
    perms=[
        "📷  Caméra : détecter les personnes (jamais sauvegardées)",
        "📡  Superposition : afficher le radar HUD  (optionnel)",
        "📳  Vibration : alertes de proximité",
    ]
    for p in perms:
        draw.text((pad+36,cy),p,font=font(28),fill=C_TEXT_SEC); cy+=58
    y+=perm_h+28

    # Accessibility card
    acc_h=260
    rr(draw,[pad,y,W-pad,y+acc_h],24,fill=h2r(C_CARD),
       outline=h2r(C_ELEVATED),ow=1)
    for i in range(4):
        c=lerp(C_ACCENT,C_SAFE,i/3)
        draw.line([pad+24,y+i,W-pad-24,y+i],fill=c)
    cy=y+40
    draw.text((pad+36,cy),"♿ Application accessible",font=font(40,True),fill=C_WHITE); cy+=62
    accs=[
        "✅  Compatible TalkBack (lecteur d'écran)",
        "✅  Alertes vibratoires pour sourds",
        "✅  Alertes sonores pour aveugles",
    ]
    for a in accs:
        draw.text((pad+36,cy),a,font=font(28),fill=C_TEXT_SEC); cy+=52
    y+=acc_h+36

    # Accept button (gradient)
    btn_h=104
    btn_rect=[pad,y,W-pad,y+btn_h]
    grad_rr(img,draw,btn_rect,22,C_GRAD_START,C_GRAD_END)
    draw=ImageDraw.Draw(img)
    tc(draw,W//2,y+26,"Comprendre et continuer",font(38,True),C_WHITE)
    y+=btn_h+22

    # Privacy link
    tc(draw,W//2,y,"Politique de confidentialité →",font(28),C_ACCENT)
    y+=60

    # Ad bottom
    ad_y=H-106
    draw.rectangle([0,ad_y,W,H],fill=h2r(C_DARK))
    draw.line([0,ad_y,W,ad_y],fill=h2r(C_ELEVATED),width=1)
    tc(draw,W//2,ad_y+36,"PUBLICITÉ",font(24),C_TEXT_HINT)

    return img.convert("RGB")


# ═══════════════════════════════════════════════════════════════════════
# 10"-B  :  Gamification + Détection enfant  (paysage-friendly tall)
# ═══════════════════════════════════════════════════════════════════════
def screenshot_10b():
    W,H=1600,2560; pad=64
    img=bg_base(W,H,seed=55)
    draw=ImageDraw.Draw(img)
    status_bar(draw,W,f_sz=30)

    y=80
    # Compact header
    draw.text((W-pad-80,y+6),"🇬🇧",font=font(50),fill=C_WHITE)
    icon_r=60; icx=pad+icon_r+12; icy=y+icon_r+12
    for i in range(icon_r,0,-1):
        t=1-i/icon_r; c=lerp(C_PURPLE,C_GRAD_END,t)
        draw.ellipse([icx-i,icy-i,icx+i,icy+i],fill=c)
    tc(draw,icx,icy-36,"👁️",font(48),C_WHITE)
    draw.text((icx+icon_r+20,y+8),"Regards au monde",font=font(54,True),fill=C_WHITE)
    draw.text((icx+icon_r+22,y+74),"Child detected nearby · Camera Mode",
              font=font(34),fill=C_AMBER)
    y+=168

    # Alert — enfant
    a_h=110
    rr(draw,[pad,y,W-pad,y+a_h],22,fill=(*h2r(C_AMBER),220))
    tc(draw,W//2,y+26,"👶  ENFANT détecté — 1.9m  (surveillance renforcée)",
       font(40,True),h2r(C_DARK))
    y+=a_h+30

    # Two-column layout: radar left, badges right
    col_w=(W-pad*2-40)//2

    # Radar (left)
    r_rad=280; r_cx=pad+col_w//2; r_cy=y+r_rad+20
    radar_warning(img,r_cx,r_cy,r_rad,[
        {"dist":0.35,"angle":10,"type":"ENFANT","danger":True},
        {"dist":0.7,"angle":150,"type":"ADULTE","danger":False},
    ])
    draw=ImageDraw.Draw(img)

    # Detection card (below radar, left col)
    dc_y=r_cy+r_rad+30
    dc_h=320
    rr(draw,[pad,dc_y,pad+col_w,dc_y+dc_h],20,fill=h2r(C_CARD),
       outline=h2r(C_AMBER),ow=2)
    for i in range(3):
        draw.line([pad+20,dc_y+i,pad+col_w-20,dc_y+i],fill=lerp(C_AMBER,C_CARD,0.5))
    draw.text((pad+28,dc_y+28),"👶 ENFANT",font=font(42,True),fill=C_AMBER)
    draw.text((pad+28,dc_y+86),"1.9m  ↙ Approche",font=font(50,True),fill=C_CORAL)
    draw.text((pad+28,dc_y+152),"Confiance: 87%",font=font(32),fill=C_TEXT_SEC)
    draw.text((pad+28,dc_y+200),"⚡ Approche rapide détectée!",font=font(30),fill=C_WARNING)
    draw.text((pad+28,dc_y+248),"📳 Vibration FORTE active",font=font(30),fill=C_TEXT_HINT)

    # Badges column (right)
    bx=pad+col_w+40; by=y
    rr(draw,[bx,by,bx+col_w,by+440],24,fill=h2r(C_CARD),
       outline=h2r(C_PURPLE),ow=2)
    for i in range(4):
        draw.line([bx+24,by+i,bx+col_w-24,by+i],fill=lerp(C_PURPLE,C_ACCENT,i/3))
    draw.text((bx+30,by+30),"🏆 Badges débloqués",font=font(40,True),fill=C_WHITE)
    badges=[
        ("🥉","Premier Regard","1ère alerte évitée"),
        ("⚡","Speedster","1ère approche rapide"),
        ("🥇","Gardien de la Rue","100 points"),
        ("👶","Protecteur","10 enfants détectés"),
        ("🌩️","Éclair","10 approches rapides"),
    ]
    by2=by+100
    for ico,name,desc in badges:
        rr(draw,[bx+20,by2,bx+col_w-20,by2+60],12,fill=h2r(C_ELEVATED))
        draw.text((bx+34,by2+10),f"{ico} {name}",font=font(28,True),fill=C_WHITE)
        draw.text((bx+34+len(name)*22,by2+14),f"  — {desc}",font=font(24),fill=C_TEXT_HINT)
        by2+=68

    # NEW badge notification
    nb_y=by+456; nb_h=90
    rr(draw,[bx,nb_y,bx+col_w,nb_y+nb_h],18,fill=h2r(C_ELEVATED),
       outline=h2r(C_AMBER),ow=2)
    tc(draw,bx+col_w//2,nb_y+16,"🌟 NOUVEAU BADGE DÉBLOQUÉ !",font(30,True),C_AMBER)
    tc(draw,bx+col_w//2,nb_y+52,"👶 Protecteur — 10 enfants détectés",font(26),C_TEXT_SEC)

    # Points + level (right col)
    pts_y=nb_y+nb_h+24; pts_h=200
    rr(draw,[bx,pts_y,bx+col_w,pts_y+pts_h],20,fill=h2r(C_CARD))
    for i in range(4):
        draw.line([bx+24,pts_y+i,bx+col_w-24,pts_y+i],fill=lerp(C_GRAD_START,C_GRAD_END,i/3))
    tc(draw,bx+col_w//2,pts_y+30,"🎯  Points",font(32),C_TEXT_HINT)
    tc(draw,bx+col_w//2,pts_y+72,"412",font(72,True),C_ACCENT)
    tc(draw,bx+col_w//2,pts_y+154,"👁️ Confirmé  →  88 pts avant Expert",font(26),C_TEXT_HINT)

    bot_y=max(dc_y+dc_h, pts_y+pts_h)+40

    # Stats row
    rr(draw,[pad,bot_y,W-pad,bot_y+180],24,fill=h2r(C_CARD))
    for i in range(4):
        draw.line([pad+24,bot_y+i,W-pad-24,bot_y+i],fill=lerp(C_GRAD_START,C_GRAD_END,i/3))
    stats=[("🎯","412","Points"),("⚠️","96","Alertes"),("⚡","27","Approches"),("👶","11","Enfants")]
    sw=(W-2*pad)//4; sx=pad
    for ico,val,lbl in stats:
        scx=sx+sw//2
        draw.text((scx-20,bot_y+24),ico,font=font(44),fill=C_WHITE)
        tc(draw,scx,bot_y+78,val,font(50,True),C_ACCENT)
        tc(draw,scx,bot_y+136,lbl,font(26),C_TEXT_HINT)
        sx+=sw
    bot_y+=210

    # Notification bar
    bot_y+=20; nh=120
    rr(draw,[pad,bot_y,W-pad,bot_y+nh],20,fill=h2r(C_ELEVATED),
       outline=h2r(C_AMBER),ow=2)
    draw.text((pad+36,bot_y+20),"👁️ Regards au monde",font=font(32,True),fill=C_AMBER)
    draw.text((pad+36,bot_y+66),"👶 ALERTE: Enfant s'approche — Détection active",
              font=font(28),fill=C_TEXT_SEC)
    sr=[W-pad-158,bot_y+28,W-pad-18,bot_y+nh-28]
    rr(draw,sr,12,fill=h2r(C_CORAL))
    tc(draw,W-pad-88,bot_y+48,"Arrêter",font(28,True),C_WHITE)
    bot_y+=nh+30

    # Progress
    draw.text((pad,bot_y),"Niveau : 👁️ Confirmé  →  88 pts avant Expert ⚡",
              font=font(30),fill=C_TEXT_HINT)
    bot_y+=44
    rr(draw,[pad,bot_y,W-pad,bot_y+14],7,fill=h2r(C_ELEVATED))
    grad_rect(img,[pad,bot_y,pad+int((W-2*pad)*0.82),bot_y+14],C_GRAD_START,C_GRAD_END,vert=False)
    draw=ImageDraw.Draw(img)

    # Ad
    ad_y=H-130
    draw.rectangle([0,ad_y,W,H],fill=h2r(C_DARK))
    draw.line([0,ad_y,W,ad_y],fill=h2r(C_ELEVATED),width=1)
    tc(draw,W//2,ad_y+44,"PUBLICITÉ",font(28),C_TEXT_HINT)

    return img.convert("RGB")


# ═══════════════════════════════════════════════════════════════════════
# 10"-C  :  État SAFE — no detection, radar vert, stats/badges
# ═══════════════════════════════════════════════════════════════════════
def screenshot_10c():
    W,H=1600,2560; pad=64
    img=bg_base(W,H,seed=88)
    draw=ImageDraw.Draw(img)
    status_bar(draw,W,f_sz=30)

    y=80
    draw.text((W-pad-80,y+6),"🇫🇷",font=font(50),fill=C_WHITE)
    icon_r=60; icx=pad+icon_r+12; icy=y+icon_r+12
    for i in range(icon_r,0,-1):
        t=1-i/icon_r; c=lerp(C_SAFE,C_ACCENT,t)
        draw.ellipse([icx-i,icy-i,icx+i,icy+i],fill=c)
    tc(draw,icx,icy-36,"👁️",font(48),C_WHITE)
    draw.text((icx+icon_r+20,y+8),"Regards au monde",font=font(54,True),fill=C_WHITE)
    draw.text((icx+icon_r+22,y+74),"🚶 En marche · Aucune menace détectée",
              font=font(34),fill=C_SAFE)
    y+=168

    # SAFE banner
    safe_h=108
    rr(draw,[pad,y,W-pad,y+safe_h],22,fill=(*h2r(C_SAFE),30),
       outline=h2r(C_SAFE),ow=2)
    tc(draw,W//2,y+24,"✅  Zone libre — Aucune menace dans un rayon de 10m",
       font(38,True),C_SAFE)
    y+=safe_h+36

    # Big radar centered
    rad_r=400; rad_cx=W//2; rad_cy=y+rad_r+10
    radar_safe(img,rad_cx,rad_cy,rad_r)
    draw=ImageDraw.Draw(img)
    y=rad_cy+rad_r+50

    # Three-col stats
    rr(draw,[pad,y,W-pad,y+200],24,fill=h2r(C_CARD))
    for i in range(4):
        draw.line([pad+24,y+i,W-pad-24,y+i],fill=lerp(C_SAFE,C_ACCENT,i/3))
    stats=[("🎯","347","Points"),("⚠️","89","Alertes évitées"),("🔥","5j","Série"),
           ("⚡","23","Approches")]
    sw=(W-2*pad)//4; sx=pad
    for ico,val,lbl in stats:
        scx=sx+sw//2
        draw.text((scx-20,y+24),ico,font=font(44),fill=C_WHITE)
        tc(draw,scx,y+80,val,font(50,True),C_SAFE)
        tc(draw,scx,y+140,lbl,font(26),C_TEXT_HINT)
        sx+=sw
    y+=222

    # Two-column below: badges + level card
    col_w=(W-pad*2-30)//2

    # Badges (left)
    bh=560
    rr(draw,[pad,y,pad+col_w,y+bh],24,fill=h2r(C_CARD),
       outline=h2r(C_PURPLE),ow=2)
    for i in range(4):
        draw.line([pad+24,y+i,pad+col_w-24,y+i],fill=lerp(C_PURPLE,C_ACCENT,i/3))
    draw.text((pad+30,y+30),"🏆 Badges débloqués",font=font(40,True),fill=C_WHITE)
    bs=[
        "🥉 Premier Regard",
        "⚡ Speedster",
        "🥇 Gardien de la Rue",
        "🔥 En feu — 5 jours",
        "👁️ Confirmé — Niv. 3",
        "⚡ Éclair — 10 approches",
    ]
    by2=y+100
    for b in bs:
        rr(draw,[pad+20,by2,pad+col_w-20,by2+58],10,fill=h2r(C_ELEVATED))
        draw.text((pad+36,by2+12),b,font=font(28),fill=C_TEXT_SEC)
        by2+=66

    # Level / progress (right)
    rx=pad+col_w+30
    # Level card
    lev_h=240
    rr(draw,[rx,y,rx+col_w,y+lev_h],24,fill=h2r(C_CARD))
    for i in range(4):
        draw.line([rx+24,y+i,rx+col_w-24,y+i],fill=lerp(C_GRAD_START,C_GRAD_END,i/3))
    tc(draw,rx+col_w//2,y+30,"📈 Niveau actuel",font(36),C_TEXT_HINT)
    tc(draw,rx+col_w//2,y+84,"👁️ Confirmé",font(54,True),C_ACCENT)
    tc(draw,rx+col_w//2,y+152,"153 points avant Expert ⚡",font(30),C_TEXT_SEC)
    # Progress bar
    bar_y=y+196; bar_h2=14
    rr(draw,[rx+30,bar_y,rx+col_w-30,bar_y+bar_h2],7,fill=h2r(C_ELEVATED))
    fill_w=rx+30+int((col_w-60)*0.69)
    grad_rect(img,[rx+30,bar_y,fill_w,bar_y+bar_h2],C_GRAD_START,C_GRAD_END,vert=False)
    draw=ImageDraw.Draw(img)

    # Streak card (right, below)
    streak_y=y+lev_h+24; streak_h=130
    rr(draw,[rx,streak_y,rx+col_w,streak_y+streak_h],20,fill=h2r(C_CARD),
       outline=(*h2r(C_AMBER),150),ow=2)
    tc(draw,rx+col_w//2,streak_y+20,"🔥 Série active",font(34),C_AMBER)
    tc(draw,rx+col_w//2,streak_y+66,"5 jours consécutifs",font(44,True),C_WHITE)

    # Accessibility card (right)
    acc_y=streak_y+streak_h+24; acc_h=200
    rr(draw,[rx,acc_y,rx+col_w,acc_y+acc_h],20,fill=h2r(C_CARD))
    for i in range(4):
        draw.line([rx+24,acc_y+i,rx+col_w-24,acc_y+i],fill=lerp(C_ACCENT,C_SAFE,i/3))
    draw.text((rx+30,acc_y+26),"♿ Accessible",font=font(36,True),fill=C_WHITE)
    for txt,iy in [("✅ TalkBack compatible",68),("✅ Vibrations pour sourds",108),("✅ Alertes sonores",148)]:
        draw.text((rx+30,acc_y+iy),txt,font=font(28),fill=C_TEXT_SEC)

    bot_y=max(y+bh,acc_y+acc_h)+40

    # Notification (safe)
    nh=120
    rr(draw,[pad,bot_y,W-pad,bot_y+nh],20,fill=h2r(C_ELEVATED),
       outline=h2r(C_SAFE),ow=2)
    draw.text((pad+36,bot_y+20),"👁️ Regards au monde",font=font(32,True),fill=C_SAFE)
    draw.text((pad+36,bot_y+66),"🚶 En marche — RAS (rien autour) · Maintenant",
              font=font(28),fill=C_TEXT_SEC)
    sr=[W-pad-160,bot_y+28,W-pad-18,bot_y+nh-28]
    rr(draw,sr,12,fill=h2r(C_CORAL))
    tc(draw,W-pad-90,bot_y+48,"Arrêter",font(28,True),C_WHITE)
    bot_y+=nh+30

    # Hint
    tc(draw,W//2,bot_y,
       "💡 Activez la surveillance depuis les Réglages rapides (👁️)",
       font(30),C_TEXT_HINT)
    bot_y+=58

    # Ad
    ad_y=H-130
    draw.rectangle([0,ad_y,W,H],fill=h2r(C_DARK))
    draw.line([0,ad_y,W,ad_y],fill=h2r(C_ELEVATED),width=1)
    tc(draw,W//2,ad_y+44,"PUBLICITÉ",font(28),C_TEXT_HINT)

    return img.convert("RGB")


def main():
    out=os.path.join(os.path.dirname(os.path.dirname(__file__)),"screenshots")
    os.makedirs(out,exist_ok=True)

    tasks=[
        ("📱 7\"-B  Alerte active (orange)…",  lambda:screenshot_7b(),  "tablet_7inch_B_alerte_1200x1920.png"),
        ("📱 7\"-C  Confidentialité…",          lambda:screenshot_7c(),  "tablet_7inch_C_confidentialite_1200x1920.png"),
        ("📱 10\"-B Gamification + enfant…",    lambda:screenshot_10b(), "tablet_10inch_B_gamification_1600x2560.png"),
        ("📱 10\"-C Zone safe + radar vert…",   lambda:screenshot_10c(), "tablet_10inch_C_safe_1600x2560.png"),
    ]
    for msg,fn,fname in tasks:
        print(msg)
        img=fn()
        path=os.path.join(out,fname)
        img.save(path,"PNG",optimize=True)
        kb=os.path.getsize(path)//1024
        print(f"   ✅ {path}  ({img.size[0]}×{img.size[1]}px, {kb} KB)")

    print("\n🎉 4 captures supplémentaires dans 'screenshots/'")

if __name__=="__main__":
    main()
