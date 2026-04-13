# StockManager CI

Application desktop JavaFX de gestion de stock avec MySQL.

## Fonctions principales

- authentification avec roles `ADMIN` et `GESTIONNAIRE`
- gestion des categories, fournisseurs, produits, mouvements et utilisateurs
- tableau de bord de suivi du stock
- alertes de stock minimum
- export `XLSX` et `PDF`
- pagination sur les listes
- tests unitaires sur une partie du projet

## Technologies

- Java 21
- JavaFX 21
- Maven
- MySQL 8+
- Apache POI
- PDFBox
- JUnit 5

## Prerequis

- JDK 21
- Maven ou le wrapper Maven fourni
- MySQL demarre localement

## Base de donnees

Le script SQL est ici :

- [database/stockmanager_ci.sql](database/stockmanager_ci.sql)

Base par defaut utilisee par l'application :

- URL : `jdbc:mysql://localhost:3306/stockmanager_ci?useSSL=false&serverTimezone=UTC`
- utilisateur : `root`
- mot de passe : vide

Ces valeurs peuvent etre surchargees via :

- `STOCKMANAGER_DB_URL`
- `STOCKMANAGER_DB_USER`
- `STOCKMANAGER_DB_PASSWORD`

ou avec des proprietes Java :

- `-Dstockmanager.db.url=...`
- `-Dstockmanager.db.user=...`
- `-Dstockmanager.db.password=...`

## Comptes par defaut

Le script SQL cree notamment :

- `admin` / `admin123`
- `aminata` / `aminata2026`
- `gbede` / `gbede`
- `natey` / `natey2026`
- `armand` / `armand2026`

## Lancer le projet

Importer d'abord le script SQL dans MySQL, puis lancer :

```powershell
.\mvnw.cmd javafx:run
```

Sous Linux :

```bash
./mvnw javafx:run
```

## Compiler et tester

Compilation :

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Tests :

```powershell
.\mvnw.cmd test
```

## Packaging

Scripts disponibles :

- [build-installer.ps1](build-installer.ps1)
- [build-portable.ps1](build-portable.ps1)
- [build-linux.sh](build-linux.sh)

Exemples :

```powershell
.\build-portable.ps1
.\build-installer.ps1
```

```bash
chmod +x build-linux.sh
./build-linux.sh app-image
```

## Structure du projet

- `src/main/java` : code Java
- `src/main/resources` : FXML, CSS, images
- `src/test/java` : tests
- `database` : script SQL

## Rapport final

Le rapport final inclus dans le depot :

- [Rapport_Complet_StockManager_CI3_v2.docx](Rapport_Complet_StockManager_CI3_v2.docx)
