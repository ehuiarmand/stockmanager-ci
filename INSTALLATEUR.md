# Generation des executables

## Prerequis

- JDK 21 complet installe
- `jpackage` disponible
- `JAVA_HOME` pointe vers ce JDK

## Windows installable

Depuis le dossier du projet :

```powershell
.\build-installer.ps1
```

## Resultat

L'installateur Windows `.exe` sera genere dans :

```text
target\installer
```

Desinstallation Windows :

- Parametres > Applications > Applications installees > `StockManagerCI`
- ou commande :

```powershell
MsiExec.exe /X{1D79ADBD-873E-383D-8820-EAF69FDB14B3}
```

## Windows portable

Depuis le dossier du projet :

```powershell
.\build-portable.ps1
```

Resultat :

```text
target\portable
```

## Linux

Depuis Linux, lancez :

```bash
chmod +x build-linux.sh
./build-linux.sh app-image
```

Autres formats supportes :

```bash
./build-linux.sh deb
./build-linux.sh rpm
```

Resultat :

```text
target/linux-app-image
target/linux-deb
target/linux-rpm
```

## Remarque

Si `jpackage` n'est pas reconnu, installez un JDK Oracle/OpenJDK 21 qui inclut `jpackage`, puis rouvrez le terminal.
