$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$appName = "StockManagerCI"
$appVersion = "1.0.0"
$vendor = "INP-HB"
$mainJar = "stockmanager-ci-1.0.jar"
$mainClass = "com.inphb.icgl.stocks.MainApp"

function Resolve-JPackage {
    if ($env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    $command = Get-Command jpackage.exe -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    throw "jpackage introuvable. Installez un JDK 21 complet puis definissez JAVA_HOME."
}

function Prepare-Input([string]$root, [string]$jarName) {
    $inputDir = Join-Path $root "target\portable-input"
    if (Test-Path $inputDir) {
        Remove-Item -Recurse -Force $inputDir
    }
    New-Item -ItemType Directory -Path $inputDir | Out-Null
    Copy-Item (Join-Path $root "target\$jarName") $inputDir
    Copy-Item (Join-Path $root "target\jpackage-input\*.jar") $inputDir
    return $inputDir
}

$jpackageExe = Resolve-JPackage

Write-Host "Compilation et tests..."
& .\mvnw.cmd package

$inputDir = Prepare-Input $projectRoot $mainJar
$outputDir = Join-Path $projectRoot "target\portable"

if (Test-Path $outputDir) {
    Remove-Item -Recurse -Force $outputDir
}
New-Item -ItemType Directory -Path $outputDir | Out-Null

Write-Host "Generation de la version portable Windows..."
& $jpackageExe `
    --type app-image `
    --name $appName `
    --app-version $appVersion `
    --vendor $vendor `
    --dest $outputDir `
    --input $inputDir `
    --main-jar $mainJar `
    --main-class $mainClass `
    --java-options "--module-path=`$APPDIR" `
    --java-options "--add-modules=javafx.controls,javafx.fxml" `
    --java-options "--enable-native-access=ALL-UNNAMED" `
    --java-options "--enable-native-access=javafx.graphics"

Write-Host "Version portable generee dans $outputDir"
