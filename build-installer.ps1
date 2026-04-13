$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$appName = "StockManagerCI"
$appVersion = "1.0.0"
$vendor = "INP-HB"
$mainJar = "stockmanager-ci-1.0.jar"
$mainClass = "com.inphb.icgl.stocks.MainApp"
$menuGroup = "StockManager CI"
$installDir = "StockManagerCI"
$upgradeUuid = "5f45a36f-8a70-4d32-a88f-8d7d0d4b7d61"

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

$jpackageExe = Resolve-JPackage

Write-Host "Compilation et tests..."
& .\mvnw.cmd package

$inputDir = Join-Path $projectRoot "target\installer-input"
$outputDir = Join-Path $projectRoot "target\installer"

if (Test-Path $inputDir) {
    Remove-Item -Recurse -Force $inputDir
}
if (Test-Path $outputDir) {
    Remove-Item -Recurse -Force $outputDir
}

New-Item -ItemType Directory -Path $inputDir | Out-Null
New-Item -ItemType Directory -Path $outputDir | Out-Null

Copy-Item (Join-Path $projectRoot "target\$mainJar") $inputDir
Copy-Item (Join-Path $projectRoot "target\jpackage-input\*.jar") $inputDir

Write-Host "Generation de l'installateur Windows..."
& $jpackageExe `
    --type exe `
    --name $appName `
    --app-version $appVersion `
    --vendor $vendor `
    --dest $outputDir `
    --input $inputDir `
    --main-jar $mainJar `
    --main-class $mainClass `
    --install-dir $installDir `
    --win-menu-group $menuGroup `
    --win-upgrade-uuid $upgradeUuid `
    --win-dir-chooser `
    --win-menu `
    --win-shortcut `
    --java-options "--module-path=`$APPDIR" `
    --java-options "--add-modules=javafx.controls,javafx.fxml" `
    --java-options "--enable-native-access=ALL-UNNAMED" `
    --java-options "--enable-native-access=javafx.graphics"

Write-Host "Installateur genere dans $outputDir"
