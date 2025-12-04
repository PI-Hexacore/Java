package school.sptech;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.*;

public class ImportadorSpotifyYoutube extends Importador {

    private static final String URL = "jdbc:mysql://localhost:3306/hexacore?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "142536";

    private final Workbook workbook;

    public ImportadorSpotifyYoutube(Workbook workbook) {
        this.workbook = workbook;
    }

    @Override
    public void importar() {

        int count = 0;

        String sql = """
                INSERT INTO SpotifyYoutubeRaw 
                (nm_track, nm_album, tp_album, nm_artista, nm_title, qt_stream)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexao = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            conexao.setAutoCommit(false);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String nomeTrack = row.getCell(3).getStringCellValue();
                    String nomeAlbum = row.getCell(4).getStringCellValue();
                    String tipoAlbum = row.getCell(5).getStringCellValue();
                    String nomeArtista = row.getCell(1).getStringCellValue();
                    String nomeYoutube = row.getCell(19).getStringCellValue();

                    Integer qtStream = 0;
                    if (row.getCell(27) != null) {
                        qtStream = (int) row.getCell(27).getNumericCellValue();
                    }

                    stmt.setString(1, nomeTrack);
                    stmt.setString(2, nomeAlbum);
                    stmt.setString(3, tipoAlbum);
                    stmt.setString(4, nomeArtista);
                    stmt.setString(5, nomeYoutube);
                    stmt.setInt(6, qtStream);

                    stmt.addBatch();
                    count++;

                } catch (Exception e) {
                    System.err.printf("Erro na linha excel %d: %s%n", i + 1, e.getMessage());
                }
            }

            stmt.executeBatch();
            conexao.commit();

            Main.registrarLog("SpotifyYoutube", "SUCESSO", count, null);

        } catch (SQLException e) {
            Main.registrarLog("SpotifyYoutube", "FALHA", count, e.getMessage());
            e.printStackTrace();
        }
    }
}
