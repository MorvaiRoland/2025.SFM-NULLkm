# Szoftverfejlesztés Mérnököknek 2025.

Ez a repository tartalmazza a tárgyhoz készült projektmunkát

Csapattagok
-----------

* Morvai Roland  
* Dóczi Bence  
* Lőrincz Levente  
* Kovács Dávid  
  
---

Téma
---

Autó szervíz és nyilvántartó alkalmazás ami segíti a papír alapú adminsztráció digitalizációját.   


Funkciók (Későbbre a végén a bele írni hogy mik lettek implementálva.)
---


További fejlesztési ötletek és roadmap
---

Az alábbi lista a projekt lehetséges bővítési irányait gyűjti össze. A javaslatok prioritás szerint vannak csoportosítva (Quick Win / Középtáv / Hosszútáv), és tématerületekre bontva.

1) Funkcionális bővítések
- Több jármű kezelése: járművek hozzáadása, címkézés, szűrés, aktív jármű kiválasztása (Quick Win)
- Karbantartási napló: szerviz bejegyzések, alkatrészcserék, garanciák, mellékletek/fotók kezelése (Középtáv)
- Üzemanyag-napló és statisztikák: tankolások rögzítése, átlagfogyasztás, havi költség grafikonok (Középtáv)
- Szerviz időpontfoglalás és emlékeztetők: naptár integráció, értesítések (e-mail/desktop) (Középtáv)
- Dokumentumkezelés: számlák, biztosítási kötvények, forgalmi engedély digitális tárolása; OCR alapadat-kinyeréssel (Hosszútáv)
- Külső API integrációk: üzemanyagár API, biztosítási ajánlatkérés API, VIN dekóder, térképes útvonal-költség kalkuláció (Hosszútáv)
- Export/Import: CSV/Excel/PDF export, teljes adatmentés importálása (Középtáv)
- Jogosultságkezelés: több felhasználó, szerepkörök (admin/felhasználó), audit log (Hosszútáv)

2) UX/UI és hozzáférhetőség
- Egységes stílus: inline FXML stílusok átszervezése közös CSS-be; világos/sötét téma támogatás (Quick Win)
- Bevitel-ellenőrzés és hibajelzés: kötelező mezők, formátumellenőrzés, hibaüzenetek és vizuális jelölések (Quick Win)
- Nemzetköziesítés (i18n): magyar/angol nyelv, resource bundle alapú szövegkezelés (Középtáv)
- Hozzáférhetőség: kontraszt, fókuszjelölések, billentyűzet-navigáció, screen reader támogatás (Középtáv)

3) Architektúra és perzisztencia
- Rétegzett felépítés tisztítása: Controller – Service – Repository rétegek szétválasztása (Középtáv)
- Adatbázis réteg: JPA/Hibernate vagy MyBatis bevezetése; Flyway/liquibase migrációk (Középtáv)
- Tároló választás: lokális SQLite → opcionálisan Postgres támogatás (Hosszútáv)
- Konfigurációkezelés: környezetenkénti beállítások, .properties/.yaml (Quick Win)

4) Minőségbiztosítás és eszközök
- Unit tesztek (JUnit5), egyszerű szolgáltatás tesztek hozzáadása (Quick Win)
- UI tesztelés (TestFX) a kritikus képernyőkre (Középtáv)
- Statikus analízis: Checkstyle, SpotBugs, PMD; kódformázási szabályok (Quick Win)
- Kódfedettség: JaCoCo riportok CI-ben (Középtáv)

5) CI/CD és kiadás
- GitHub Actions/JetBrains Space CI: build, tesztek, kódfedettség (Quick Win)
- Csomagolás jpackage-szel; telepítő készítése Windowsra (Középtáv)
- Release workflow: verziózás, changelog generálás, artefaktok publikálása (Középtáv)

6) Biztonság és adatvédelem
- Hitelesítés/jelszavak biztonságos tárolása (hash+salt); érzékeny adatok titkosítása (Középtáv)
- GDPR szempontok: adatmegőrzési idők, exportálhatóság, törlési kérések kezelése (Hosszútáv)

Javasolt roadmap
- Quick Win (1–2 hét):
  - Közös CSS bevezetése és FXML-ek style kiszervezése
  - Alap bevitel-ellenőrzés és hibaüzenetek a kalkulátorokban
  - Checkstyle + egyszerű JUnit tesztek és Action workflow
- Középtáv (1–2 hónap):
  - Üzemanyag-napló grafikonokkal, karbantartási napló
  - i18n támogatás és hozzáférhetőségi fejlesztések
  - JPA + Flyway bevezetése, export/import
- Hosszútáv (3+ hónap):
  - Külső API integrációk, felhős szinkronizáció, többfelhasználós üzem

Megjegyzés
- A Calculator.fxml dizájnja már átdolgozásra került a jobb olvashatóságért. Következő lépésként érdemes a stílusokat a /src/main/resources/drivesync/CSS/style.css fájlba áthelyezni és a képernyőket ehhez igazítani.

---

Közös CSS és Beállítások
---

- Bevezetésre került a közös stíluslap: `src/main/resources/drivesync/CSS/style.css`. Ebbe kerültek az általános stílusok (oldalsó menü, gombok, kártyák, címek stb.).
- A főképernyő (Menü/Home.fxml), a Kalkulátor (Kalkulátor/Calculator.fxml) és a Beállítások (Beállítások/Settings.fxml) képernyők frissítve lettek, hogy a stílusokat a közös CSS-ből vegyék (inline stílusok kivezetve).
- A beállítások menüpont működik: a Home oldalsó menü „⚙ Beállítások” gombja betölti a Beállítások képernyőt.
- A JavaFX Scene-ekhez a stíluslap automatikusan csatolódik (App.java).

Indítás után: bejelentkezés → a bal oldali menüben válaszd a „⚙ Beállítások” menüpontot, a dizájn egységes megjelenését a style.css biztosítja.

---

Modern 2025 UI és animációk
---

- Oldalsáv (sidebar) finom, gyors animációval csukható/nyitható (180 ms, EASE_BOTH). Az állapot Preferences-ben tárolódik és induláskor visszaáll.
- Navigáció tartalomváltása áttűnéses animációval történik (ki-fade 140 ms, be-fade 180 ms), így kellemesebb a képernyőváltás.
- Bejelentkező képernyő (login) finom belépő animációt kapott (220 ms fade-in), jobb első benyomást ad.
- Tooltip-ek a menügombokhoz: összecsukott nézetben is egyértelmű, hova vezet egy menüpont.
- Modern interakciók: 
  - Menü- és akciógombok hoverkor enyhe skálázás és árnyék, nyomáskor visszajelző „összenyomódás”.
  - Kártyák hoverkor nagyobb elevációt kapnak.
  - Beviteli mezők jól látható fókuszgyűrűt kapnak (világos/sötét témában is megfelelő kontraszt).
- Sötét téma frissített kontrasztokkal (sidebar, menügombok, címkék, logout gomb), egységes modern megjelenéssel.

Érintett fő fájlok:
- src/main/java/drivesync/HomeController.java – Sidebar animációk, tartalom fade, tooltip-ek.
- src/main/java/drivesync/App.java – Indító jelenet (login) belépő animáció, globális CSS csatolása mindkét Scene-hez.
- src/main/resources/drivesync/CSS/style.css – Sidebar, gombok, kártyák, beviteli mezők modern stílusai és sötét téma kiegészítések.

Tipp: A Beállítások → Megjelenés alatt a Téma és Betűméret azonnal érvényesül. A modern animációk rövidek és nem tolakodóak, így nem lassítják a használatot, mégis „élettel” töltik meg a felületet.

---

Modern menürendszer és működő Beállítások
---

- Új, modern, összecsukható oldalsávos menü készült:
  - Hamburger gombbal (☰) nyitható/csukható.
  - Az összecsukott állapot a felhasználói beállításokban (Preferences) megőrződik.
  - Aktív menüpont kiemelés CSS-sel (menu-button-active), egységes hover állapotok.
- Beállítások képernyő funkciói:
  - Téma váltás: Világos / Sötét / Rendszer alapértelmezett. A sötét témát a `theme-dark` osztály aktiválja, és azonnal érvényesül az alkalmazásban.
  - Betűméret skálázás: 10–24 px-ig csúszkával, azonnali élő frissítéssel az egész UI-ra.
  - Értesítések és általános kapcsolók (email/SMS/push, automatikus frissítés, naplózás, statisztikák) mentése és visszatöltése Preferences-ben.
  - Felhasználói adatok (felhasználónév, email, jelszó) mentése adatbázisba, ha a kapcsolat és userId átadásra kerül a SettingsController számára (opcionális).

Megjegyzés: A téma és betűméret beállításai már alkalmazásindításkor is érvényesülnek (App.java mindkét jelenetre alkalmazza a Preferences-ből betöltött értékeket).

---

Design system (stílus-útmutató)
---

Alap komponensek és osztályok a közös stíluslapban (src/main/resources/drivesync/CSS/style.css):

- Háttér és tartalom
  - app-bg: világos/sötét téma kompatibilis oldalháttér
  - content-scroll: modern görgetősáv és háttér a ScrollPane-hez
  - content-container: kényelmes belső margók
- Tipográfia
  - page-title, page-subtitle: oldal- és szakaszcímek
  - card-title, card-subtitle, label-sm: kártyacímek és mezőcímkék
  - text-muted, text-danger, text-success: segédszínek
- Kártyák és layout
  - card: kártya háttér, lekerekítés, árnyék, hover eleváció
  - flow-wrap: rugalmas rács elrendezéshez (FlowPane)
- Gombok
  - btn-primary, btn-danger, btn-secondary, btn-ghost: akciógombok és alternatívák
- Menü/Sidebar
  - sidebar, sidebar-appname, sidebar-label, sidebar-username
  - menu-button, menu-button-active, menu-toggle, logout-button
- Form elemek
  - Látható fókuszgyűrű és kontrasztos keret fókuszban (TextField, PasswordField, ChoiceBox, stb.)

Téma
- Sötét téma a theme-dark osztállyal aktiválható a Scene gyökérre.
- A Beállításokban választott téma/betűméret a Preferences-ben tárolódik és az App indításkor automatikusan alkalmazza.
