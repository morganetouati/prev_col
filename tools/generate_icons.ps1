# Generate PNG icons from SVG using Inkscape or ImageMagick (magick)
# Usage: .\generate_icons.ps1 -Source ..\assets\icons\favicon.svg -OutDir ..\assets\icons\output
param(
    [string]$Source = "..\assets\icons\favicon.svg",
    [string]$OutDir = "..\assets\icons\output"
)

$sizes = @(48,72,96,144,192,512)

if (-not (Test-Path $Source)) {
    Write-Error "Source SVG not found: $Source"
    exit 1
}

New-Item -ItemType Directory -Path $OutDir -Force | Out-Null

function Convert-WithInkscape($in,$out,$size){
    & inkscape --export-type=png --export-filename="$out" --export-width=$size --export-height=$size "$in"
}

function Convert-WithMagick($in,$out,$size){
    & magick convert -background none -resize ${size}x${size} "$in" "$out"
}

foreach ($s in $sizes) {
    $out = Join-Path $OutDir "icon_${s}.png"
    if (Get-Command inkscape -ErrorAction SilentlyContinue) {
        Write-Host "Rendering $s -> $out (inkscape)"
        Convert-WithInkscape $Source $out $s
    } elseif (Get-Command magick -ErrorAction SilentlyContinue) {
        Write-Host "Rendering $s -> $out (ImageMagick)"
        Convert-WithMagick $Source $out $s
    } else {
        Write-Warning "Neither Inkscape nor ImageMagick found. Please install one to generate PNGs."
        Write-Host "Suggested command (Inkscape): inkscape --export-type=png --export-filename=icon_512.png --export-width=512 --export-height=512 $Source"
        break
    }
}

Write-Host "Done. Output folder: $OutDir"