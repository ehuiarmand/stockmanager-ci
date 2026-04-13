$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

$mainJar = "stockmanager-ci-1.0.jar"
$mainClass = "com.inphb.icgl.stocks.MainApp"
$appName = "StockManagerCI"
$appVersion = "1.0"
$jpackageInput = Join-Path $projectRoot "target\jpackage-input"
$logoPngPath = Join-Path $projectRoot "src\main\resources\images\logo.png"
$iconPath = Join-Path $projectRoot "src\main\resources\images\logo.ico"

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw "jpackage est introuvable. Installe un JDK 21+ et ajoute son dossier bin au PATH."
}

Write-Host "1. Build Maven..."
& .\mvnw.cmd clean package -DskipTests

Write-Host "2. Preparation du dossier jpackage..."
if (Test-Path $jpackageInput) {
    Remove-Item -Recurse -Force $jpackageInput
}
New-Item -ItemType Directory -Force -Path $jpackageInput | Out-Null

& .\mvnw.cmd dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=target\jpackage-input"

$builtJar = Join-Path $projectRoot "target\$mainJar"
if (-not (Test-Path $builtJar)) {
    throw "JAR principal introuvable: $builtJar"
}

Copy-Item $builtJar -Destination (Join-Path $jpackageInput $mainJar)

Write-Host "3. Generation de l'executable..."
$arguments = @(
    "--input", $jpackageInput,
    "--name", $appName,
    "--main-jar", $mainJar,
    "--main-class", $mainClass,
    "--type", "exe",
    "--app-version", $appVersion,
    "--dest", (Join-Path $projectRoot "dist"),
    "--win-shortcut",
    "--win-menu",
    "--vendor", "INP-HB IC-GL",
    "--description", "StockManager CI - Application de gestion des stocks"
)

if (Test-Path $iconPath) {
    $arguments += @("--icon", $iconPath)
} elseif (Test-Path $logoPngPath) {
    Write-Host "logo.png detecte dans src/main/resources/images, mais jpackage sous Windows attend un fichier .ico pour l'icone de l'executable."
}

& jpackage @arguments

Write-Host "Executable genere dans le dossier dist."
