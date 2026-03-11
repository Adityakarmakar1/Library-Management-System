# 📚 Library Management System
### A Modern Java Swing + SQLite Desktop Application

---

## ✨ Features

| Module | Features |
|--------|----------|
| **Dashboard** | Live stats cards (books, members, borrowings, overdue), system info |
| **Books** | Add / Edit / Delete books, search by title/author/ISBN/genre, availability tracking |
| **Members** | Member registration, types (Standard/Premium/Student), status management |
| **Borrowings** | Issue books, return books, automatic fine calculation (₹2/day), filter by status |

---

## 🖥 Screenshots (what you'll see)

- **Deep navy** color theme with **gold accents**
- Sidebar navigation with active-state indicator
- Stat cards with color-coded metrics
- Searchable, sortable tables with alternating rows
- Modal dialogs for data entry
- Status badges (green = active/available, red = overdue/inactive, gold = borrowed)

---

## 🚀 Quick Start

### Prerequisites
- **Java JDK 17+** — Download from https://adoptium.net/
- **SQLite JDBC driver** — The build script auto-downloads it

### Linux / macOS
```bash
chmod +x build_and_run.sh
./build_and_run.sh
```

### Windows
```
Double-click build_and_run.bat
```
or in Command Prompt:
```
build_and_run.bat
```

### Manual Build
```bash
# 1. Download SQLite JDBC
mkdir lib
curl -L https://github.com/xerial/sqlite-jdbc/releases/download/3.45.1.0/sqlite-jdbc-3.45.1.0.jar -o lib/sqlite-jdbc.jar

# 2. Compile
mkdir out
find src -name "*.java" | xargs javac --release 17 -cp lib/sqlite-jdbc.jar -d out

# 3. Package
cd out && jar xf ../lib/sqlite-jdbc.jar && cd ..
jar cfe LibraryManagement.jar library.Main -C out .

# 4. Run
java -jar LibraryManagement.jar
```

---

## 📁 Project Structure

```
LibraryManagement/
├── src/
│   └── library/
│       ├── Main.java                    # Entry point
│       ├── model/
│       │   ├── Book.java
│       │   ├── Member.java
│       │   └── Borrowing.java
│       ├── dao/
│       │   ├── BookDAO.java             # Book CRUD + search
│       │   ├── MemberDAO.java           # Member CRUD + search
│       │   └── BorrowingDAO.java        # Issue / Return + fines
│       ├── ui/
│       │   ├── MainWindow.java          # App shell + sidebar
│       │   ├── DashboardPanel.java      # Stats overview
│       │   ├── BooksPanel.java          # Book management
│       │   ├── MembersPanel.java        # Member management
│       │   ├── BorrowingsPanel.java     # Issue / Return
│       │   └── UIComponents.java        # Reusable custom components
│       └── util/
│           ├── Theme.java               # Color palette & fonts
│           └── DatabaseManager.java     # SQLite connection & init
├── lib/
│   └── sqlite-jdbc.jar                  # Auto-downloaded
├── build_and_run.sh                     # Linux/macOS build script
├── build_and_run.bat                    # Windows build script
└── README.md
```

---

## 🗄 Database Schema

The app creates `library.db` (SQLite) in the working directory.

**books** — id, title, author, isbn, genre, publisher, year, total_copies, available_copies, description  
**members** — id, name, email, phone, address, membership_type, membership_date, expiry_date, status  
**borrowings** — id, book_id, member_id, borrow_date, due_date, return_date, fine_amount, status  

Sample data (10 books, 5 members) is auto-inserted on first run.

---

## ⚙ Configuration

| Setting | Location | Default |
|---------|----------|---------|
| Fine per day | `BorrowingDAO.java` | ₹2.00 |
| Default loan period | `BorrowingsPanel.java` | 14 days |
| Database file name | `DatabaseManager.java` | `library.db` |

---

## 🎨 Tech Stack

- **Java 17** (records, text blocks, switch expressions)
- **Java Swing** — custom-painted components, no external UI framework
- **SQLite** via xerial sqlite-jdbc 3.45
- **SwingWorker** for non-blocking DB operations
- Architecture: **MVC** (Model → DAO → Panel)

---

## 📝 License

MIT License — free to use, modify and distribute.
