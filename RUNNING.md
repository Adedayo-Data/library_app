# How to Run the Library Management System UI in IntelliJ

## Problem
IntelliJ is trying to run the old `HelloApplication` class which no longer exists.

## Solution: Update Run Configuration

### Method 1: Create New Run Configuration (Recommended)

1. Open `LibraryApplication.java` in IntelliJ
2. Right-click anywhere in the editor
3. Select **"Run 'LibraryApplication.main()'"**
4. Done! The app will launch

### Method 2: Edit Existing Configuration

1. Click the run configuration dropdown (top-right toolbar)
2. Select **"Edit Configurations..."**
3. Find the configuration pointing to `HelloApplication`
4. Change **Main class** to: `com.lawpavillion.lmsui.LibraryApplication`
5. Click **OK**
6. Click the green Run button

### Method 3: Run via Launcher

1. Open `Launcher.java`
2. Right-click in the editor
3. Select **"Run 'Launcher.main()'"**

---

## What You'll See

### Without Backend Running:
- ✅ Application window opens with the beautiful UI
- ✅ Navigation bar with icons appears
- ✅ Empty table with proper columns
- ❌ Error dialog: "Failed to load books: Connection refused" (or similar)
- ✅ Status shows: "Error loading books"
- ✅ You can still click the **+** button to see the Add Book dialog

### With Backend Running:
- ✅ Everything above PLUS
- ✅ Books load automatically into the table
- ✅ Pagination works
- ✅ All CRUD operations functional

---

## Expected Behavior

The application is designed to **handle backend errors gracefully**:

1. **On Startup**: Tries to load books from `http://localhost:8080/api/books`
2. **If Backend Offline**: Shows error dialog with message
3. **UI Still Works**: You can still interact with buttons, open dialogs, etc.
4. **Click Refresh**: Try connecting again

This is the correct behavior - the UI should launch even if the backend isn't ready!

---

## Troubleshooting

**If you still see "HelloApplication not found":**
- Make sure you've pulled the latest changes (we deleted those files)
- Run `mvn clean compile` in terminal
- Restart IntelliJ (File → Invalidate Caches / Restart)

**If the window doesn't appear:**
- Check if there's a JavaFX/display error in the console
- Make sure Java 21 is selected as the project SDK
