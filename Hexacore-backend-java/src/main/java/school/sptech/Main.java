package school.sptech;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cglib.core.Local;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Date;


public class Main {

    public static void main(String[] args) throws IOException {
        String url = "jdbc:mysql://localhost:3306/hexacore?useSSL=false&serverTimezone=UTC";
        String usuario = "root";
        String senha = "142536";

        String caminhoArquivo = "Pasta1.xlsx";

        int batchSize = 200;
        int count = 0;

        String sql = "INSERT INTO SpotifyTop (nm_titulo, cd_rank, dt_rank, nm_artista, nm_pais, ds_chart, ds_trend, qt_stream) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexao = DriverManager.getConnection(url, usuario, senha);
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            FileInputStream arquivo = new FileInputStream(new File(caminhoArquivo));
            Workbook workbook = new XSSFWorkbook(arquivo); {

                conexao.setAutoCommit(false);

                Sheet sheet = workbook.getSheetAt(0);
                for (int i = 1; i <= 200; i++) {
                    Row row = sheet.getRow(i);

                    if (row == null) {
                        continue;
                    }

                    String nomeTitulo;
                    Integer cdRank;
                    LocalDateTime dtRank;
                    String nomeArtista;
                    String nomePais;
                    String dsChart;
                    String dsTrend;
                    Integer qtStream;

                    try {
                        nomeTitulo = row.getCell(0).getStringCellValue();
                        cdRank =(int) row.getCell(1).getNumericCellValue();
                        dtRank = row.getCell(2).getLocalDateTimeCellValue();
                        nomeArtista = row.getCell(3).getStringCellValue();
                        nomePais = row.getCell(5).getStringCellValue();
                        dsChart = row.getCell(6).getStringCellValue();
                        dsTrend = row.getCell(7).getStringCellValue();
                        qtStream =(int) row.getCell(8).getNumericCellValue();



                        stmt.setString(1, nomeTitulo);
                        stmt.setInt(2, cdRank);
                        stmt.setObject(3, dtRank);
                        stmt.setString(4, nomeArtista);
                        stmt.setString(5, nomePais);
                        stmt.setString(6, dsChart);
                        stmt.setString(7, dsTrend);
                        stmt.setInt(8, qtStream);

                        stmt.addBatch();
                        count++;

                    } catch (Exception e) {
                        System.err.printf("ERRO de tipo de célula na linha Excel %d: %s. Linha ignorada.%n", i + 1, e.getMessage());
                        continue;
                    }
            }}
                stmt.executeBatch(); // Executa todas as inserções
                conexao.commit();
           //Confirma a transação
            System.out.println("Inserções concluídas!");
        } catch (SQLException e) {
            e.printStackTrace();

        }
        }
    }
