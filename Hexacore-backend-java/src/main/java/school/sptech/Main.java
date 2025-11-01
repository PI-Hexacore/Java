package school.sptech;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) throws IOException {
        String url = "jdbc:mysql://localhost:3306/hexacore?useSSL=false&serverTimezone=UTC";
        String usuario = "root";
        String senha = "142536";

        S3Client s3Client = new S3Provider().getS3Client();

        InputStream arquivoS3Top = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket("s3-raw-lab-ismael")
                        .key("teste1.xlsx")
                        .build(),
                ResponseTransformer.toInputStream()
        );

        InputStream arquivoS3Youtube = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket("s3-raw-lab-ismael")
                        .key("teste2.xlsx")
                        .build(),
                ResponseTransformer.toInputStream()
        );

        Workbook workbookTop = new XSSFWorkbook(arquivoS3Top);
        Workbook workbookYoutube = new XSSFWorkbook(arquivoS3Youtube);

        int countSpotifyTop = 0;
        String sqlSpotifyTop = """
            INSERT INTO SpotifyTop (nm_titulo, cd_rank, dt_rank, nm_artista, nm_pais, ds_chart, ds_trend, qt_stream, ds_genero)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conexao = DriverManager.getConnection(url, usuario, senha);
             PreparedStatement stmt = conexao.prepareStatement(sqlSpotifyTop)) {

            conexao.setAutoCommit(false);
            Sheet sheet = workbookTop.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String nomeTitulo = row.getCell(0).getStringCellValue();
                    Integer cdRank = (int) row.getCell(1).getNumericCellValue();
                    LocalDateTime dtRank = row.getCell(2).getLocalDateTimeCellValue();
                    String nomeArtista = row.getCell(3).getStringCellValue();
                    String nomePais = row.getCell(5).getStringCellValue();
                    String dsChart = row.getCell(6).getStringCellValue();
                    String dsTrend = row.getCell(7).getStringCellValue();
                    Integer qtStream = (int) row.getCell(8).getNumericCellValue();
                    String genero = row.getCell(9).getStringCellValue();

                    stmt.setString(1, nomeTitulo);
                    stmt.setInt(2, cdRank);
                    stmt.setObject(3, dtRank);
                    stmt.setString(4, nomeArtista);
                    stmt.setString(5, nomePais);
                    stmt.setString(6, dsChart);
                    stmt.setString(7, dsTrend);
                    stmt.setInt(8, qtStream);
                    stmt.setString(9, genero);

                    stmt.addBatch();
                    countSpotifyTop++;
                } catch (Exception e) {
                    System.err.printf("ERRO na linha Excel %d: %s. Linha ignorada.%n", i + 1, e.getMessage());
                }
            }

            stmt.executeBatch();
            conexao.commit();
            registrarLog("SpotifyTop", "SUCESSO", countSpotifyTop, null);

        } catch (SQLException e) {
            registrarLog("SpotifyTop", "FALHA", countSpotifyTop, e.getMessage());
        }

        int countSpotifyYoutube = 0;
        String sqlSpotifyYoutube = """
            INSERT INTO SpotifyYoutube (nm_track, nm_album, tp_album, nm_artista, nm_title, qt_stream)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conexao2 = DriverManager.getConnection(url, usuario, senha);
             PreparedStatement stmt2 = conexao2.prepareStatement(sqlSpotifyYoutube)) {

            conexao2.setAutoCommit(false);
            Sheet sheet2 = workbookYoutube.getSheetAt(0);

            for (int i = 1; i <= sheet2.getLastRowNum(); i++) {
                Row row = sheet2.getRow(i);
                if (row == null) continue;

                try {
                    String nomeTitulo = row.getCell(3).getStringCellValue();
                    String nomeAlbum = row.getCell(4).getStringCellValue();
                    String tipoAlbum = row.getCell(5).getStringCellValue();
                    String nomeArtista = row.getCell(1).getStringCellValue();
                    String nomeYoutube = row.getCell(19).getStringCellValue();
                    Integer qtStream = row.getCell(27) == null ? 0 : (int) row.getCell(27).getNumericCellValue();

                    stmt2.setString(1, nomeTitulo);
                    stmt2.setString(2, nomeAlbum);
                    stmt2.setString(3, tipoAlbum);
                    stmt2.setString(4, nomeArtista);
                    stmt2.setString(5, nomeYoutube);
                    stmt2.setInt(6, qtStream);

                    stmt2.addBatch();
                    countSpotifyYoutube++;
                } catch (Exception e) {
                    System.err.printf("ERRO na linha Excel %d: %s. Linha ignorada.%n", i + 1, e.getMessage());
                }
            }

            stmt2.executeBatch();
            conexao2.commit();
            registrarLog("SpotifyYoutube", "SUCESSO", countSpotifyYoutube, null);

        } catch (SQLException e) {
            registrarLog("SpotifyYoutube", "FALHA", countSpotifyYoutube, e.getMessage());
        }

        System.out.println("Processo completo de importação finalizado!");

    DadosTratados dao = new DadosTratados(null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        try {
        dao.inserirTodos();
        registrarLog("DadosTratados", "SUCESSO", dao.buscarDadosTratados().size(), null);
    } catch (Exception e) {
        registrarLog("DadosTratados", "FALHA", 0, e.getMessage());
    }


        try {
        ArtistaMusica artistaMusica = new ArtistaMusica();
        artistaMusica.importarArtistasEMusicas();
        registrarLog("Artista/Musica", "SUCESSO", 0, "Importação de artistas e músicas concluída.");
    } catch (Exception e) {
        registrarLog("Artista/Musica", "FALHA", 0, e.getMessage());
        e.printStackTrace();
    }

        System.out.println("Processo completo de importação finalizado!");
}

    public static void registrarLog(String tabela, String status, int registros, String mensagem) {
        String url = "jdbc:mysql://localhost:3306/hexacore?useSSL=false&serverTimezone=UTC";
        String usuario = "root";
        String senha = "142536";

        String sql = "INSERT INTO LogImportacao (tabelaAlvo, statusLog, registrosInseridos, mensagem) VALUES (?, ?, ?, ?)";

        try (Connection conexao = DriverManager.getConnection(url, usuario, senha);
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, tabela);
            stmt.setString(2, status);
            stmt.setInt(3, registros);
            stmt.setString(4, mensagem);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Falha ao registrar log: " + e.getMessage());
        }
    }
}
