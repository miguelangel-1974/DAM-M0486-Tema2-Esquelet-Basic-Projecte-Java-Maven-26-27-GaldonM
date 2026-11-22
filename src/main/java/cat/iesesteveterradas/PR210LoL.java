package cat.iesesteveterradas;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Aplicació que gestiona una base de dades SQLite de personatges i faccions
 * de League of Legends.
 */
public class PR210LoL {

    private static final String RUTA_BD = "data/lol.db";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Si la base de dades no existeix, es crea i s'omple
            File fitxer = new File(RUTA_BD);
            if (!fitxer.exists()) {
                crearBaseDades();
            }

            int opcio = 0;
            while (opcio != 5) {
                System.out.println();
                System.out.println("1. Mostrar una taula");
                System.out.println("2. Mostrar personatges per facció");
                System.out.println("3. Mostrar el millor atacant per facció");
                System.out.println("4. Mostrar el millor defensor per facció");
                System.out.println("5. Sortir");
                System.out.print("Opció: ");
                opcio = scanner.nextInt();

                if (opcio == 1) {
                    mostrarUnaTaula(scanner);
                } else if (opcio == 2) {
                    mostrarTaula("Faccio");
                    System.out.print("Id de la facció: ");
                    int idFaccio = scanner.nextInt();
                    mostrarPersonatgesPerFaccio(idFaccio);
                } else if (opcio == 3) {
                    mostrarTaula("Faccio");
                    System.out.print("Id de la facció: ");
                    int idFaccio = scanner.nextInt();
                    mostrarMillorAtacant(idFaccio);
                } else if (opcio == 4) {
                    mostrarTaula("Faccio");
                    System.out.print("Id de la facció: ");
                    int idFaccio = scanner.nextInt();
                    mostrarMillorDefensor(idFaccio);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Obre una connexió i activa les claus foranes
    private static Connection connectar() throws SQLException {
        String url = "jdbc:sqlite:" + RUTA_BD;
        Connection conn = DriverManager.getConnection(url);
        try (Statement stmt = conn.createStatement()) {
            // Les claus foranes NO es comproven per defecte: cal activar-les a cada connexió
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    // Crea les taules i insereix les dades inicials
    private static void crearBaseDades() throws SQLException {
        try (Connection conn = connectar(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE Faccio (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom VARCHAR(15),
                    resum VARCHAR(500)
                )""");

            stmt.executeUpdate("""
                CREATE TABLE Personatge (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom VARCHAR(15),
                    atac REAL,
                    defensa REAL,
                    idFaccio INTEGER,
                    FOREIGN KEY (idFaccio) REFERENCES Faccio(id)
                )""");

            stmt.executeUpdate("INSERT INTO Faccio (nom, resum) VALUES ('Demacia', 'Regne orgullós amb una gran "
                    + "tradició militar, que posa la justícia, l’honor i el deure per sobre de tot. Desconfia "
                    + "de la màgia i la controla amb duresa dins de les seves fronteres. És el gran rival de Noxus.')");
            stmt.executeUpdate("INSERT INTO Faccio (nom, resum) VALUES ('Noxus', 'Imperi poderós i expansionista que "
                    + "valora la força i l’ambició. Qualsevol persona hi pot arribar al poder si demostra prou "
                    + "talent, sigui quin sigui el seu origen. Les seves legions han conquerit molts territoris "
                    + "de Runeterra.')");
            stmt.executeUpdate("INSERT INTO Faccio (nom, resum) VALUES ('Ionia', 'Arxipèlag on la màgia, la natura i "
                    + "l’espiritualitat conviuen en equilibri. La seva cultura busca l’harmonia en totes les "
                    + "coses, però la invasió de Noxus va obligar molts ionians a agafar les armes per defensar "
                    + "la seva terra.')");

            stmt.executeUpdate("INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES ('Galio', 1, 3, 1)");
            stmt.executeUpdate("INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES ('Lux', 2, 2, 1)");
            stmt.executeUpdate("INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES ('Garen', 2, 3, 1)");

            stmt.executeUpdate("INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES ('Darius', 3, 3, 2)");
            stmt.executeUpdate("INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES ('Katarina', 2, 1, 2)");
            stmt.executeUpdate("INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES ('Swain', 1, 2, 2)");

            stmt.executeUpdate("INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES ('Yasuo', 3, 2, 3)");
            stmt.executeUpdate("INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES ('Ahri', 2, 1, 3)");
            stmt.executeUpdate("INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES ('Irelia', 3, 2, 3)");
        }
    }

    // Mostra les taules que hi ha, demana quina es vol veure i la mostra
    private static void mostrarUnaTaula(Scanner scanner) throws SQLException {
        try (Connection conn = connectar()) {
            DatabaseMetaData meta = conn.getMetaData();

            System.out.println("Taules de la base de dades:");
            try (ResultSet rs = meta.getTables(null, null, "%", new String[] {"TABLE"})) {
                while (rs.next()) {
                    System.out.println("    " + rs.getString("TABLE_NAME"));
                }
            }

            System.out.print("Quina taula vols veure? ");
            String nomTaula = scanner.next();

            // El nom de la taula no es pot passar com a paràmetre ?:
            // comprovem que és a la llista de getTables() abans de posar-lo a l'SQL
            boolean existeix = false;
            try (ResultSet rs = meta.getTables(null, null, "%", new String[] {"TABLE"})) {
                while (rs.next()) {
                    if (rs.getString("TABLE_NAME").equals(nomTaula)) {
                        existeix = true;
                    }
                }
            }

            if (existeix) {
                mostrarTaula(nomTaula);
            } else {
                System.out.println("Aquesta taula no existeix.");
            }
        }
    }

    // Mostra el contingut d'una taula sense conèixer-ne les columnes
    private static void mostrarTaula(String nomTaula) throws SQLException {
        try (Connection conn = connectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + nomTaula)) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumnes = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= numColumnes; i++) {
                    System.out.print(rsmd.getColumnName(i) + "=" + rs.getString(i) + " ");
                }
                System.out.println();
            }
        }
    }

    // Mostra els personatges d'una facció amb el nom de la facció
    private static void mostrarPersonatgesPerFaccio(int idFaccio) throws SQLException {
        String sql = "SELECT Personatge.nom, Faccio.nom AS faccio FROM Personatge "
                + "JOIN Faccio ON Personatge.idFaccio = Faccio.id "
                + "WHERE Faccio.id = ?";
        try (Connection conn = connectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFaccio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getString("nom") + " (" + rs.getString("faccio") + ")");
                }
            }
        }
    }

    // Mostra el personatge amb més atac d'una facció (si hi ha empat, només un)
    private static void mostrarMillorAtacant(int idFaccio) throws SQLException {
        String sql = "SELECT Personatge.nom, Faccio.nom AS faccio FROM Personatge "
                + "JOIN Faccio ON Personatge.idFaccio = Faccio.id "
                + "WHERE Faccio.id = ? ORDER BY Personatge.atac DESC LIMIT 1";
        try (Connection conn = connectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFaccio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Millor atacant de " + rs.getString("faccio") + ": " + rs.getString("nom"));
                }
            }
        }
    }

    // Mostra el personatge amb més defensa d'una facció (si hi ha empat, només un)
    private static void mostrarMillorDefensor(int idFaccio) throws SQLException {
        String sql = "SELECT Personatge.nom, Faccio.nom AS faccio FROM Personatge "
                + "JOIN Faccio ON Personatge.idFaccio = Faccio.id "
                + "WHERE Faccio.id = ? ORDER BY Personatge.defensa DESC LIMIT 1";
        try (Connection conn = connectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFaccio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Millor defensor de " + rs.getString("faccio") + ": " + rs.getString("nom"));
                }
            }
        }
    }
}
