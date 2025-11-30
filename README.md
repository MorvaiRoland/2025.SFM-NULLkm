# Szoftverfejlesztés Mérnököknek 2025 – DriveSync Projekt

**Autószerviz és nyilvántartó alkalmazás**

## Csapattagok

* **Morvai Roland**
* **Dóczi Bence**
* **Lőrincz Levente**
* **Kovács Dávid**

---

# 🔧 Projekt Áttekintés

A **DriveSync** egy JavaFX alapú, MySQL adatbázissal működő autószerviz- és járműnyilvántartó rendszer, amely célja a papír alapú adminisztráció digitalizálása, a szervizidőpontok és költségek nyomon követése, valamint egy modern irányítópult biztosítása felhasználók számára.
A rendszer tartalmaz AI alapú diagnosztikát, költségkezelést, PDF generálást, személyre szabható beállításokat és figyelmeztető értesítéseket is.

---

# 🛠️ Technológiák

* **Java / JavaFX**
* **MySQL** – relációs adatbázis
* **JDBC** – adatbázis kapcsolat
* **SHA-256 titkosítás** jelszókezeléshez
* **DAO architektúra** adat-hozzáféréshez
* **PDFBox / ReportLab** – PDF generálás
* **Preferences API** – lokális beállítások tárolása
* **AI modul** – tünet alapú diagnosztika

---

# 🔑 Bejelentkezési Modul

**Csomag:** `drivesync.Bejelentkezes`

### Login.java

Feladata a felhasználók hitelesítése MySQL adatbázisban.

* SHA-256 hash összehasonlítás (MySQL: `SHA2(?, 256)`)
* SQL injekció elleni védelem Prepared Statement segítségével

### LoginController.java

JavaFX alapú felhasználói felület kezelése.

**Funkciók:**

* Bejelentkezés kezelése (input validáció, hibaüzenetek)
* „Emlékezz rám” funkció Preferences segítségével
* Jelszó megjelenítési váltás
* Oldalsó intro videó betöltése
* Sikeres bejelentkezés után HomeController megnyitása

---

# 📝 Regisztrációs Modul

**Csomag:** `drivesync.Regisztracio`

### Register.java

Új felhasználók létrehozása.

**Validációk:**

* Teljes mezőellenőrzés
* Jelszó egyezés ellenőrzése
* Felhasználónév és email egyediség ellenőrzése

**Adatbázis műveletek:**

* SHA-256 hash-elt jelszót tárol
* Siker vagy hiba esetén JavaFX Alert

---

# 🏠 Irányítópult (Dashboard)

**Csomag:** `drivesync.Home`

A fő kezelőfelület, widget alapú moduláris megoldással.

### ⚙️ Funkciók

* Widgetek dinamikus hozzáadása/eltávolítása
* Sötét/világos téma váltás
* Egér-effektusok, tooltip-ek, ikonok

### 🌤 Időjárás Widget

* WeatherService segítségével adatlekérés
* Városkeresés
* Hőmérséklet, szél, páratartalom, leírás

### ⛽ Üzemanyag Widget

* 95, 100 benzin + dízel árak
* Min/átlag/max értékek
* Óránkénti automatikus frissítés (Timeline)

### 🚗 Autók Widget

* Felhasználó járművei az adatbázisból
* Aszinkron lekérdezés
* Márka, típus, rendszám, évjárat stb.

### 💰 Költségvetés Widget

* Havi/éves összkiadás
* BarChart vizualizáció havi bontásban
* Kategóriák: Üzemanyag / Szervíz / Egyéb

### 🔔 Szerviz Értesítések Widget

* Közelgő szervizek listázása
* Emlékeztetők jelzése
* Archív kezelés

---

# 🤖 AI Diagnosztikai Modul

**Új funkció**

### AIDiagnostics Widget

* Felhasználó megad egy tünetet
* Rendszer lekéri a felhasználó elsődleges autóját
* AI szolgáltatás elemzi a tünetet (Async Task)
* Javasolt diagnózis jelenik meg

Alkalmazott technikák:

* Thread + Task → UI nem fagy le
* Biztonságos adatkezelés

---

# 🚗 Saját Autók Kezelése

**Csomag:** `drivesync.SajatAutok`

### 🔍 Autók kezelése

* Autók listázása FlowPane-ben
* Dinamikus kártyák hover-effektussal
* Válogatás, részletek megjelenítése

### 🛠️ Autó hozzáadása/szerkesztése

* Márkák, típusok, motorok dinamikus betöltése
* Kombinált kereshető ComboBox mezők
* Szín kiválasztása ColorPicker-rel
* Validációk (km, évjárat, ár stb.)

### 🔧 Szerviztörténet és Közelgő Szerviz

* Megtörtént szerviz rögzítése
* Új szerviztípus automatikus felvétele, ha nem létezik
* Közelgő szervizek létrehozása, szerkesztése, törlése
* Emlékeztetők email küldéssel

### 📧 Emlékeztetők

* Háttérszálon futó figyelés
* 3 napon belüli szervizek email értesítést küldenek
* Egyszeri küldés (last_email_sent mező)

### 📄 PDF generálás

* Több autó kiválasztható
* Külső PdfGenerator modul
* Szerviztörténeti jelentés

---

# 💰 Költségvetés Kezelő

**Csomag:** `drivesync.Budget`

### Adatbevitel

* Kiadások rögzítése (mit, mennyi, mikor)
* Owner ID automatikus feloldása
* Érvényesség ellenőrzés

### Táblázat funkciók

* Szerkesztés, törlés
* Automatikusan frissülő TableView

### 📊 Diagramok

* BarChart: havi bontás kategóriánként
* LineChart: éves trend
* Tooltipek minden adatponthoz

### Figyelmeztetések

* 200 000 Ft havi kategória limit
* Vizualizált figyelmeztetés

### 📄 PDF Export

* PDFBox használata
* Unicode betűkészlet támogatás
* Havi/kategóriás bontás exportálása

---

# ⚙️ Beállítások Modul

**Csomag:** `drivesync.Settings`

### Felhasználói adatok betöltése

* Név, email, regisztráció dátuma, 2FA állapot
* Google-felhasználók felismerése (nincs jelszó hash)

### Jelszó módosítás

* Ha Google felhasználó → tiltva
* Ha nem → SHA-256 hash frissítés

### Lokális beállítások

* Téma (világos/sötét)
* Betűméret
* Értesítések
* Preferences API mentés

### Visszajelzés

* Egyedi Toast értesítés kis animációval

---

# 🧠 Üzleti Logika (Business Logic)

A DriveSync üzleti logikája arra épül, hogy a felhasználók digitálisan, gyorsan és biztonságosan kezelhessék járműveik és szervizeléseik teljes életciklusát.
A rendszer az alábbi fő üzleti folyamatokat valósítja meg:

---

## 1. Felhasználókezelés

* Regisztráció egyedi email + felhasználónév alapján
* Jelszó biztonságos SHA-256 hash-elése
* Bejelentkezés preferenciák alapján
* Külső azonosítás támogatása (Google Login)

**Üzleti szabályok:**

* Felhasználónév és email egyedi
* Google felhasználó nem módosíthat jelszót
* Sikertelen bejelentkezés nem fedi fel, hogy melyik adat hibás

---

## 2. Járműkezelés

* Több jármű rögzítése egy felhasználóhoz
* Adatok részletes nyilvántartása (motor, gumi, olaj stb.)
* Dinamikus adatkapcsolatok (márka → típus → motor)

**Üzleti szabályok:**

* Jármű csak teljes és érvényes adatokkal rögzíthető
* KM és évjárat számszerű validáció
* Szín HEX formátumban kerül eltárolásra

---

## 3. Szervizelés és karbantartás

* Szerviztörténet rögzítése
* Közelgő szervizek határidővel és emlékeztetővel
* Emlékeztetők automatikus kiküldése emailben

**Üzleti szabályok:**

* Új szerviztípus automatikusan felvehető
* 3 napon belüli szervizekről értesítés küldése
* Lejárt szervizek archiválása automatikus

---

## 4. Költségkezelés

* Külön költségkategóriák rögzítése
* Éves és havi kiadás összesítés
* Limit figyelmeztető rendszer

**Üzleti szabályok:**

* Kiadás csak pozitív összegű lehet
* 200 000 Ft feletti havi kategóriakiadás figyelmeztet
* Export PDF tartalmazza az összesített értékeket

---

## 5. AI Diagnosztika

* Tünet alapján automatikus előzetes hibaelemzés
* Autó márka + típus figyelembevétele
* Felhasználónak javaslat és hiba lehetséges oka

**Üzleti szabályok:**

* Diagnózis csak akkor indítható, ha van aktív autó
* Hálózati hibák kezelése
* UI soha nem fagy le (Task háttérszál)

---

## 6. Jelentések és Export

* Szerviztörténeti PDF minden autóról
* Költségvetési PDF éves bontásban
* Unicode támogatás a magyar karakterek miatt

**Üzleti szabályok:**

* PDF csak kiválasztott autókról generálható
* Exportált dokumentum dátummal és felhasználóval ellátott

---

# 📦 Összegzés

A DriveSync egy modern, biztonságos, moduláris és erősen adatvezérelt autónyilvántartó rendszer, amely lefedi egy jármű tulajdonos teljes digitális igényeit:

* Adminisztráció → digitalizált
* Szervizelések → dokumentált
* Költségvetés → vizualizált
* Emlékeztetők → automatizált
* Diagnosztika → AI támogatott

Teljes mértékben alkalmas egy járműkarbantartó rendszer valós vállalati bevezetésére is.

---


