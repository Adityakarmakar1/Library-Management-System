# 📚 Library Management System (Java Swing)

A simple and functional **Library Management System** built using **Java Swing**. This application allows users to manage books, members, issue/return operations, and export records. It uses file-based data storage through Java serialization, meaning all data persists automatically.

---

## 🚀 Features

### 📘 Book Management

* Add new books (ID, Title, Author, Year, Quantity)
* View all books in a table
* Track available and issued copies
* Search books by ID, title, or author
* Export book list to **CSV**

### 👥 Member Management

* Add new members
* View all registered members

### 🔄 Issue & Return Books

* Issue books to members
* Validates availability
* Prevents issuing if book/member doesn't exist
* Return books to restore availability

### 💾 Data Persistence

The app stores data in the following files automatically:

* `books.dat`
* `members.dat`
* `issues.dat`

---

## 🛠️ Technologies Used

* **Java (JDK 17+)**
* **Java Swing** (GUI Framework)
* **Serialization** for file-based persistence
* **JTable**, **JDialogs**, **JToolBar**

---

## 📂 Project Structure

```
library-management-system/
│
├─ src/
│  └─ LibraryManagementSystem.java
│
├─ README.md
├─ .gitignore
├─ LICENSE (optional)
│
├─ data/ (optional: sample .dat files)
│
└─ screenshots/
   ├─ main-window.png
   ├─ add-book-dialog.png
   └─ issue-dialog.png
```

---

## ▶️ How to Run

### **1. Install JDK**

Download JDK from Oracle:
[https://www.oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/)

Verify installation:

```bash
java -version
javac -version
```

### **2. Compile the Program**

```bash
javac LibraryManagementSystem.java
```

### **3. Run the Program**

```bash
java LibraryManagementSystem
```

The UI window will open.

---

## 📤 Export Books to CSV

Use the **Export Books CSV** button inside the application to generate:

```
books_export.csv
```

You can open it with Excel, Google Sheets, etc.

---

## 📁 Data Files

The program automatically generates:

| File          | Description               |
| ------------- | ------------------------- |
| `books.dat`   | Stores book information   |
| `members.dat` | Stores member information |
| `issues.dat`  | Tracks issued books       |

---

## 🧩 Future Enhancements

We may add:

* SQL Database (SQLite/MySQL)
* Login system (Admin/Librarian)
* Issue history with timestamps
* Fine calculation for overdue books
* Improved theme / dark mode
* Multi-file MVC architecture
