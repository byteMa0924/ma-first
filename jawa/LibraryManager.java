import java.io.*;
import java.util.*;

class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    int id;
    String title;
    String author;
    String isbn;
    int total;
    int available;

    Book(int id, String title, String author, String isbn, int total) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.total = Math.max(total, 0);
        this.available = this.total;
    }
}

public class LibraryManager {
    private static final String DATA_FILE = "books.ser";
    private static final List<Book> books = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadBooks();
        while (true) {
            printMenu();
            int choice = readInt("请选择：");
            switch (choice) {
                case 1 -> addBook();
                case 2 -> listBooks();
                case 3 -> searchBooks();
                case 4 -> updateBook();
                case 5 -> deleteBook();
                case 6 -> borrowBook();
                case 7 -> returnBook();
                case 0 -> {
                    saveBooks();
                    System.out.println("已保存，程序退出。");
                    return;
                }
                default -> System.out.println("无效选项，请重新输入。");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n==== 图书管理系统 ====");
        System.out.println("1. 添加图书");
        System.out.println("2. 浏览图书");
        System.out.println("3. 搜索图书");
        System.out.println("4. 修改图书");
        System.out.println("5. 删除图书");
        System.out.println("6. 借阅图书");
        System.out.println("7. 归还图书");
        System.out.println("0. 保存并退出");
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            String input = readLine(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("请输入整数。");
            }
        }
    }

    private static int nextId() {
        int maxId = 0;
        for (Book book : books) {
            maxId = Math.max(maxId, book.id);
        }
        return maxId + 1;
    }

    private static Book findById(int id) {
        for (Book book : books) {
            if (book.id == id) {
                return book;
            }
        }
        return null;
    }

    private static void addBook() {
        String title = readLine("书名：");
        String author = readLine("作者：");
        String isbn = readLine("ISBN：");
        int total = readInt("总数量：");
        Book book = new Book(nextId(), title, author, isbn, total);
        books.add(book);
        saveBooks();
        System.out.println("添加成功，图书 ID 为 " + book.id + "。");
    }

    private static void listBooks() {
        if (books.isEmpty()) {
            System.out.println("当前没有图书记录。");
            return;
        }
        printHeader();
        for (Book book : books) {
            printBook(book);
        }
    }

    private static void searchBooks() {
        String keyword = readLine("请输入书名或作者关键词：");
        boolean found = false;
        printHeader();
        for (Book book : books) {
            if (book.title.contains(keyword) || book.author.contains(keyword)) {
                printBook(book);
                found = true;
            }
        }
        if (!found) {
            System.out.println("未找到匹配记录。");
        }
    }

    private static void updateBook() {
        int id = readInt("请输入要修改的图书 ID：");
        Book book = findById(id);
        if (book == null) {
            System.out.println("未找到该图书。");
            return;
        }
        int borrowed = book.total - book.available;
        book.title = readLine("新书名：");
        book.author = readLine("新作者：");
        book.isbn = readLine("新 ISBN：");
        int newTotal = readInt("新总数量：");
        if (newTotal < borrowed) {
            System.out.println("总数量不能小于已借出数量 " + borrowed + "，本次修改取消。");
            return;
        }
        book.total = newTotal;
        book.available = newTotal - borrowed;
        saveBooks();
        System.out.println("修改成功。");
    }

    private static void deleteBook() {
        int id = readInt("请输入要删除的图书 ID：");
        Book book = findById(id);
        if (book == null) {
            System.out.println("未找到该图书。");
            return;
        }
        if (book.available != book.total) {
            System.out.println("该书仍有借出记录，不能删除。");
            return;
        }
        books.remove(book);
        saveBooks();
        System.out.println("删除成功。");
    }

    private static void borrowBook() {
        int id = readInt("请输入要借阅的图书 ID：");
        Book book = findById(id);
        if (book == null) {
            System.out.println("未找到该图书。");
            return;
        }
        if (book.available <= 0) {
            System.out.println("该书暂无可借库存。");
            return;
        }
        book.available--;
        saveBooks();
        System.out.println("借阅成功，当前可借数量：" + book.available);
    }

    private static void returnBook() {
        int id = readInt("请输入要归还的图书 ID：");
        Book book = findById(id);
        if (book == null) {
            System.out.println("未找到该图书。");
            return;
        }
        if (book.available >= book.total) {
            System.out.println("该书没有待归还记录。");
            return;
        }
        book.available++;
        saveBooks();
        System.out.println("归还成功，当前可借数量：" + book.available);
    }

    private static void printHeader() {
        System.out.printf("%-4s %-24s %-16s %-16s %-6s %-6s%n",
                "ID", "书名", "作者", "ISBN", "总量", "可借");
    }

    private static void printBook(Book book) {
        System.out.printf("%-4d %-24s %-16s %-16s %-6d %-6d%n",
                book.id, book.title, book.author, book.isbn, book.total, book.available);
    }

    @SuppressWarnings("unchecked")
    private static void loadBooks() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            Object data = input.readObject();
            if (data instanceof List<?>) {
                books.clear();
                books.addAll((List<Book>) data);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("读取数据失败，将使用空数据启动。");
        }
    }

    private static void saveBooks() {
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            output.writeObject(books);
        } catch (IOException e) {
            System.out.println("保存失败：" + e.getMessage());
        }
    }
}
