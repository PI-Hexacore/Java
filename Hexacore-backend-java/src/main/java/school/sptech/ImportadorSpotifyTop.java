package school.sptech;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.*;
import java.time.LocalDateTime;

public class ImportadorSpotifyTop extends Importador {

    private static final String URL = "jdbc:mysql://44.208.146.218:3306/hexacore?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "142536";

    private final Workbook workbook;

    public ImportadorSpotifyTop(Workbook workbook) {
        this.workbook = workbook;
    }

    @Override
    public void importar() {

        int count = 0;

        String sql = """
                INSERT INTO SpotifyTopRaw 
                (nm_titulo, cd_rank, dt_rank, nm_artista, nm_pais, ds_chart, ds_trend, qt_stream, ds_genero)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexao = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            conexao.setAutoCommit(false);

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    stmt.setString(1, row.getCell(0).getStringCellValue());
                    stmt.setInt(2, (int) row.getCell(1).getNumericCellValue());
                    stmt.setObject(3, row.getCell(2).getLocalDateTimeCellValue());
                    stmt.setString(4, row.getCell(3).getStringCellValue());
                    stmt.setString(5, row.getCell(5).getStringCellValue());
                    stmt.setString(6, row.getCell(6).getStringCellValue());
                    stmt.setString(7, row.getCell(7).getStringCellValue());
                    stmt.setInt(8, (int) row.getCell(8).getNumericCellValue());
                    stmt.setString(9, row.getCell(9).getStringCellValue());

                    stmt.addBatch();
                    count++;

                } catch (Exception e) {
                    System.err.printf("Erro na linha excel %d: %s%n", i + 1, e.getMessage());
                }
            }

            stmt.executeBatch();
            conexao.commit();

            Main.registrarLog("SpotifyTop", "SUCESSO", count, null);

        } catch (SQLException e) {
            Main.registrarLog("SpotifyTop", "FALHA", count, e.getMessage());
        }
    }
}
