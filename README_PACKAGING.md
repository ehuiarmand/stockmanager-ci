# Packaging Windows avec jpackage

Ce document explique comment generer l'executable Windows de `StockManager CI`.

## Prerequis

- Windows
- JDK 21 ou plus recent
- `jpackage` accessible dans le `PATH`
- Maven Wrapper disponible dans le projet (`mvnw.cmd`)

Verification rapide :

```powershell
java --version
jpackage --version
```

## Fichiers utilises

- Script de packaging :
  [scripts/package-jpackage.ps1](C:\COURS INPHB IC 2026\TP\stockmanager-ci\stockmanager-ci\scripts\package-jpackage.ps1)
- JAR principal :
  `target/stockmanager-ci-1.0.jar`
- Logo utilise par l'application :
  `src/main/resources/images/logo.png`
- Icone Windows facultative pour `jpackage` :
  `src/main/resources/images/logo.ico`

## Commande a lancer

Depuis la racine du projet :

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\package-jpackage.ps1
```

## Ce que fait le script

1. Compile et package le projet Maven
2. Copie les dependances runtime dans `target/jpackage-input`
3. Copie le JAR principal dans `target/jpackage-input`
4. Lance `jpackage` pour generer un `.exe`

## Options de packaging configurees

- Nom application : `StockManagerCI`
- Version : `1.0`
- Type : `exe`
- Raccourci Windows : oui
- Entree menu Demarrer : oui
- Vendor : `INP-HB IC-GL`
- Description : `StockManager CI - Application de gestion des stocks`

## Resultat attendu

L'executable est genere dans :

```text
dist\
```

## Si `jpackage` n'est pas reconnu

Cela signifie generalement que :

- un JRE est installe au lieu d'un JDK
- ou le dossier `bin` du JDK n'est pas dans le `PATH`

Exemple de chemin attendu :

```text
C:\Program Files\Java\jdk-21\bin
```

## Conseils pour le rendu

- Tester l'executable sur une machine propre si possible
- Le splash screen charge `src/main/resources/images/logo.png`
- Ajouter un fichier `logo.ico` si tu veux aussi une icone Windows personnalisee dans l'executable
- Verifier que MySQL est accessible sur la machine de demonstration
