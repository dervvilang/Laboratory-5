package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class FilmDatabaseApp {

    private static final String DB_URL_FILMS = "jdbc:postgresql://localhost:5432/films_db";
    private static final String DB_URL_DEFAULT = "jdbc:postgresql://localhost:5432/postgres";

    private static String currentUser;
    private static String currentPassword;
    private static String currentDBUrl;
    private static String currentRole;
    private static String currentTable = "film";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }

    private static Connection getConnection(String dbUrl) throws SQLException {
        return DriverManager.getConnection(dbUrl, currentUser, currentPassword);
    }

    // окно авторизации
    static class LoginFrame extends JFrame {
        public LoginFrame() {
            setTitle("Вход в систему");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(300, 220);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel(new GridLayout(6, 1, 5, 5));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel roleLabel = new JLabel("Выберите режим доступа:");
            JRadioButton adminButton = new JRadioButton("Администратор");
            JRadioButton guestButton = new JRadioButton("Гость");
            ButtonGroup group = new ButtonGroup();
            group.add(adminButton);
            group.add(guestButton);
            adminButton.setSelected(true);

            JTextField userField = new JTextField("postgres");
            JPasswordField passField = new JPasswordField("postgres");

            JButton loginButton = new JButton("Войти");

            panel.add(roleLabel);
            panel.add(adminButton);
            panel.add(guestButton);
            panel.add(userField);
            panel.add(passField);
            panel.add(loginButton);
            add(panel, BorderLayout.CENTER);

            loginButton.addActionListener(e -> {
                currentRole = adminButton.isSelected() ? "Admin" : "Guest";
                currentUser = userField.getText().trim();
                currentPassword = new String(passField.getPassword());
                currentDBUrl = currentRole.equals("Admin") ? DB_URL_DEFAULT : DB_URL_FILMS;
                try (Connection conn = getConnection(currentDBUrl)) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Успешное подключение!",
                            "Информация",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    dispose();
                    new MainFrame();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Ошибка подключения: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            });

            setVisible(true);
        }
    }

    // главное окно приложения
    static class MainFrame extends JFrame {
        public MainFrame() {
            setTitle("Фильм - База данных");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(900, 500);
            setLocationRelativeTo(null);

            // панель кнопок
            JPanel panel = new JPanel();
            panel.setBackground(new Color(255, 182, 193));
            panel.setLayout(new GridLayout(0, 3, 10, 10));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JButton btnReturnToLogin = new JButton("Вернуться в меню входа");
            JButton btnSwitchDatabase = new JButton("Сменить базу данных");
            JButton btnSwitchTable = new JButton("Сменить таблицу");
            JButton btnCreateTable = new JButton("Создать новую таблицу");
            JButton btnClearTable = new JButton("Очистить таблицу");
            JButton btnAddFilm = new JButton("Добавить запись");
            JButton btnUpdateFilm = new JButton("Обновить запись");
            JButton btnDeleteFilm = new JButton("Удалить запись по названию");
            JButton btnViewAll = new JButton("Просмотр всех записей");
            JButton btnSearchFilm = new JButton("Поиск записи по названию");
            JButton btnCreateUser = new JButton("Создать нового пользователя");
            JButton btnCreateDB = new JButton("Создать БД");
            JButton btnDropDB = new JButton("Удалить БД");

            if (currentRole.equals("Admin")) {
                panel.add(btnReturnToLogin);
                panel.add(btnSwitchDatabase);
                panel.add(btnSwitchTable);
                panel.add(btnCreateTable);
                panel.add(btnCreateDB);
                panel.add(btnDropDB);
                panel.add(btnClearTable);
                panel.add(btnAddFilm);
                panel.add(btnUpdateFilm);
                panel.add(btnDeleteFilm);
                panel.add(btnCreateUser);
                panel.add(btnViewAll);
                panel.add(btnSearchFilm);
            } else {
                panel.add(btnReturnToLogin);
                panel.add(btnViewAll);
                panel.add(btnSearchFilm);
            }
            add(panel, BorderLayout.CENTER);

            // кнопка "Вернуться в меню входа"
            btnReturnToLogin.addActionListener(e -> {
                dispose();
                SwingUtilities.invokeLater(LoginFrame::new);
            });

            // кнопка "Сменить базу данных" (только для Admin)
            btnSwitchDatabase.addActionListener(e -> {
                String[] options = {"postgres", "films_db"};
                String selectedDB = (String) JOptionPane.showInputDialog(
                        null,
                        "Выберите базу данных:",
                        "Переключение базы данных",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        currentDBUrl.equals(DB_URL_DEFAULT) ? "postgres" : "films_db"
                );
                if (selectedDB != null) {
                    currentDBUrl = selectedDB.equals("postgres") ? DB_URL_DEFAULT : DB_URL_FILMS;
                    try (Connection conn = getConnection(currentDBUrl)) {
                        JOptionPane.showMessageDialog(null,
                                "Переключение базы данных успешно. Сейчас используется: " + selectedDB,
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Ошибка при переключении базы данных: " + ex.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // кнопка "Сменить таблицу" (только для Admin)
            btnSwitchTable.addActionListener(e -> {
                String newTable = JOptionPane.showInputDialog("Введите имя таблицы для работы:", currentTable);
                if (newTable != null && !newTable.trim().isEmpty()) {
                    currentTable = newTable.trim();
                    JOptionPane.showMessageDialog(null,
                            "Текущая таблица изменена на: " + currentTable,
                            "Информация",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            });

            // кнопка "Создать новую таблицу" (только для Admin)
            btnCreateTable.addActionListener(e -> {
                String newTable = JOptionPane.showInputDialog("Введите имя новой таблицы:");
                if (newTable != null && !newTable.trim().isEmpty()) {
                    try (Connection conn = getConnection(currentDBUrl)) {
                        conn.setAutoCommit(false);
                        try (CallableStatement cs = conn.prepareCall("CALL sp_create_table_gen(?)")) {
                            cs.setString(1, newTable.trim());
                            cs.execute();
                        }
                        conn.commit();
                        currentTable = newTable.trim(); // обновляем текущую таблицу
                        JOptionPane.showMessageDialog(null,
                                "Таблица " + currentTable + " успешно создана.",
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Ошибка при создании таблицы: " + ex.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // кнопка "Создать БД"
            btnCreateDB.addActionListener(e -> {
                String dbName = JOptionPane.showInputDialog("Введите имя новой базы данных:");
                if (dbName != null && !dbName.trim().isEmpty()) {
                    try (Connection conn = getConnection(DB_URL_DEFAULT)) {
                        conn.setAutoCommit(false);
                        try (CallableStatement cs = conn.prepareCall("CALL sp_create_database(?, ?)")) {
                            cs.setString(1, dbName);
                            cs.setString(2, currentPassword);
                            cs.execute();
                        }
                        conn.commit();
                        JOptionPane.showMessageDialog(null, "База данных '" + dbName + "' создана успешно.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Ошибка при создании БД: " + ex.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // кнопка "Удалить БД"
            btnDropDB.addActionListener(e -> {
                String dbName = JOptionPane.showInputDialog("Введите имя базы данных для удаления:");
                if (dbName != null && !dbName.trim().isEmpty()) {
                    try (Connection conn = getConnection(DB_URL_DEFAULT)) {
                        conn.setAutoCommit(false);
                        try (CallableStatement cs = conn.prepareCall("CALL sp_drop_database(?, ?)")) {
                            cs.setString(1, dbName);
                            cs.setString(2, currentPassword);
                            cs.execute();
                        }
                        conn.commit();
                        JOptionPane.showMessageDialog(null, "База данных '" + dbName + "' удалена.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Ошибка при удалении БД: " + ex.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // кнопка "Очистить таблицу"
            btnClearTable.addActionListener(e -> {
                try (Connection conn = getConnection(currentDBUrl)) {
                    conn.setAutoCommit(false);
                    try (CallableStatement cs = conn.prepareCall("CALL sp_clear_table_gen(?)")) {
                        cs.setString(1, currentTable);
                        cs.execute();
                    }
                    conn.commit();
                    JOptionPane.showMessageDialog(null,
                            "Таблица " + currentTable + " очищена.",
                            "Информация",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Ошибка при очистке таблицы: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            // кнопка "Добавить запись"
            btnAddFilm.addActionListener(e -> {
                JTextField titleField = new JTextField();
                JTextField durationField = new JTextField();
                JTextField genreField = new JTextField();
                JTextField ratingField = new JTextField();
                Object[] msg = {
                        "Название:", titleField,
                        "Длительность (мин):", durationField,
                        "Жанр:", genreField,
                        "Рейтинг:", ratingField
                };
                int option = JOptionPane.showConfirmDialog(null, msg, "Добавление записи", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    try (Connection conn = getConnection(currentDBUrl)) {
                        conn.setAutoCommit(false);
                        String title = titleField.getText().trim();
                        int duration = Integer.parseInt(durationField.getText().trim());
                        String genre = genreField.getText().trim();
                        double rating = Double.parseDouble(ratingField.getText().trim());
                        try (CallableStatement cs = conn.prepareCall("CALL sp_insert_film_gen(?, ?, ?, ?, ?)")) {
                            cs.setString(1, currentTable);
                            cs.setString(2, title);
                            cs.setInt(3, duration);
                            cs.setString(4, genre);
                            cs.setDouble(5, rating);
                            cs.execute();
                        }
                        conn.commit();
                        JOptionPane.showMessageDialog(null,
                                "Запись добавлена в таблицу " + currentTable,
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException | NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Ошибка при добавлении записи: " + ex.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // кнопка "Обновить запись"
            btnUpdateFilm.addActionListener(e -> {
                JTextField idField = new JTextField();
                JTextField titleField = new JTextField();
                JTextField durationField = new JTextField();
                JTextField genreField = new JTextField();
                JTextField ratingField = new JTextField();
                Object[] msg = {
                        "ID записи для обновления:", idField,
                        "Новое название:", titleField,
                        "Новая длительность (мин):", durationField,
                        "Новый жанр:", genreField,
                        "Новый рейтинг:", ratingField
                };
                int option = JOptionPane.showConfirmDialog(null, msg, "Обновление записи", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    try (Connection conn = getConnection(currentDBUrl)) {
                        conn.setAutoCommit(false);
                        int id = Integer.parseInt(idField.getText().trim());
                        String title = titleField.getText().trim();
                        int duration = Integer.parseInt(durationField.getText().trim());
                        String genre = genreField.getText().trim();
                        double rating = Double.parseDouble(ratingField.getText().trim());
                        try (CallableStatement cs = conn.prepareCall("CALL sp_update_film_gen(?, ?, ?, ?, ?, ?)")) {
                            cs.setString(1, currentTable);
                            cs.setInt(2, id);
                            cs.setString(3, title);
                            cs.setInt(4, duration);
                            cs.setString(5, genre);
                            cs.setDouble(6, rating);
                            cs.execute();
                        }
                        conn.commit();
                        JOptionPane.showMessageDialog(null,
                                "Запись обновлена в таблице " + currentTable,
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException | NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Ошибка при обновлении записи: " + ex.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // кнопка "Удалить запись по названию"
            btnDeleteFilm.addActionListener(e -> {
                String title = JOptionPane.showInputDialog("Введите название для удаления из таблицы " + currentTable + ":");
                if (title != null && !title.trim().isEmpty()) {
                    try (Connection conn = getConnection(currentDBUrl)) {
                        conn.setAutoCommit(false);
                        try (CallableStatement cs = conn.prepareCall("CALL sp_delete_film_gen(?, ?)")) {
                            cs.setString(1, currentTable);
                            cs.setString(2, title);
                            cs.execute();
                        }
                        conn.commit();
                        JOptionPane.showMessageDialog(null,
                                "Запись удалена из таблицы " + currentTable,
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Ошибка при удалении записи: " + ex.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // кнопка "Просмотр всех записей"
            btnViewAll.addActionListener(e -> {
                try (Connection conn = getConnection(currentDBUrl)) {
                    conn.setAutoCommit(false);
                    try (CallableStatement cs = conn.prepareCall("CALL sp_get_all_gen(?, ?)")) {
                        cs.setString(1, currentTable);
                        cs.registerOutParameter(2, Types.OTHER);
                        cs.setNull(2, Types.OTHER);
                        cs.execute();
                        try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                            StringBuilder result = new StringBuilder();
                            while (rs.next()) {
                                result.append("ID: ").append(rs.getInt("id"))
                                        .append(", Название: ").append(rs.getString("title"))
                                        .append(", Длительность: ").append(rs.getInt("duration"))
                                        .append(", Жанр: ").append(rs.getString("genre"))
                                        .append(", Рейтинг: ").append(rs.getDouble("rating"))
                                        .append("\n");
                            }
                            JOptionPane.showMessageDialog(null,
                                    new JScrollPane(new JTextArea(result.toString(), 10, 40)),
                                    "Записи в таблице " + currentTable,
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    conn.commit();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Ошибка при получении записей: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            // кнопка "Поиск записи по названию"
            btnSearchFilm.addActionListener(e -> {
                String title = JOptionPane.showInputDialog("Введите название для поиска в таблице " + currentTable + ":");
                if (title != null && !title.trim().isEmpty()) {
                    try (Connection conn = getConnection(currentDBUrl)) {
                        conn.setAutoCommit(false);
                        try (CallableStatement cs = conn.prepareCall("CALL sp_search_film_gen(?, ?, ?)")) {
                            cs.setString(1, currentTable);
                            cs.setString(2, title);
                            cs.registerOutParameter(3, Types.OTHER);
                            cs.setNull(3, Types.OTHER);
                            cs.execute();
                            try (ResultSet rs = (ResultSet) cs.getObject(3)) {
                                StringBuilder result = new StringBuilder();
                                while (rs.next()) {
                                    result.append("ID: ").append(rs.getInt("id"))
                                            .append(", Название: ").append(rs.getString("title"))
                                            .append(", Длительность: ").append(rs.getInt("duration"))
                                            .append(", Жанр: ").append(rs.getString("genre"))
                                            .append(", Рейтинг: ").append(rs.getDouble("rating"))
                                            .append("\n");
                                }
                                JOptionPane.showMessageDialog(null,
                                        new JScrollPane(new JTextArea(result.toString(), 10, 40)),
                                        "Результаты поиска в таблице " + currentTable,
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                        conn.commit();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Ошибка при поиске записи: " + ex.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // кнопка "Создать нового пользователя"
            btnCreateUser.addActionListener(e -> {
                JTextField usernameField = new JTextField();
                JTextField passwordField = new JTextField();
                String[] roles = {"Admin", "Guest"};
                JComboBox<String> roleBox = new JComboBox<>(roles);
                Object[] msg = {
                        "Имя пользователя:", usernameField,
                        "Пароль:", passwordField,
                        "Роль:", roleBox
                };
                int option = JOptionPane.showConfirmDialog(null, msg, "Создание пользователя", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String newUsername = usernameField.getText().trim();
                    String newPassword = passwordField.getText().trim();
                    String newRole = (String) roleBox.getSelectedItem();
                    if (!newUsername.isEmpty() && !newPassword.isEmpty()) {
                        try (Connection conn = getConnection(currentDBUrl)) {
                            conn.setAutoCommit(false);
                            try (CallableStatement cs = conn.prepareCall("CALL sp_create_db_user(?, ?, ?)")) {
                                cs.setString(1, newUsername);
                                cs.setString(2, newPassword);
                                cs.setString(3, newRole);
                                cs.execute();
                            }
                            conn.commit();
                            JOptionPane.showMessageDialog(null,
                                    "Пользователь '" + newUsername + "' с ролью " + newRole + " создан.",
                                    "Информация",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null,
                                    "Ошибка при создании пользователя: " + ex.getMessage(),
                                    "Ошибка",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Имя пользователя и пароль не могут быть пустыми.",
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            setVisible(true);
        }
    }
}
