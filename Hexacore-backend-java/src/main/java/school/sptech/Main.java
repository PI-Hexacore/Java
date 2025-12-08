package school.sptech;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.sql.*;

public class Main {

    private static final String URL = "jdbc:mysql://44.208.146.218:3306/hexacore?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "142536";

    public static void main(String[] args) {

        int idUsuario = buscarUsuarioAtivo();
        if (idUsuario == -1) {
            System.err.println("Nenhum usuário ativo encontrado para Slack.");
            return;
        }

        S3Client s3Client = new S3Provider().getS3Client();

        String bucket = "s3-raw-lab-ismael";
        String keyTop = "teste3.xlsx";
        String keyYoutube = "teste4.xlsx";

        try (
                InputStream inTop = s3Client.getObject(
                        GetObjectRequest.builder().bucket(bucket).key(keyTop).build(),
                        ResponseTransformer.toInputStream()
                );

                InputStream inYoutube = s3Client.getObject(
                        GetObjectRequest.builder().bucket(bucket).key(keyYoutube).build(),
                        ResponseTransformer.toInputStream()
                );

                Workbook workbookTop = new XSSFWorkbook(inTop);
                Workbook workbookYoutube = new XSSFWorkbook(inYoutube)
        ) {
            Importador[] importadores = {
                    new ImportadorSpotifyTop(workbookTop),
                    new ImportadorSpotifyYoutube(workbookYoutube)
            };

            for (Importador importador : importadores) {
                try {
                    importador.importar();
                } catch (Exception e) {
                    String nome = importador.getClass().getSimpleName();
                    registrarLog(nome, "FALHA", 0, e.getMessage());
                }
            }

        } catch (Exception e) {
            registrarLog("S3/Workbook", "FALHA", 0, e.getMessage());
        }

        try {
            DadosTratados dao = new DadosTratados();
            dao.inserirTodos();
            int qtd = dao.buscarDadosTratados().size();
            registrarLog("DadosTratados", "SUCESSO", qtd, null);
        } catch (Exception e) {
            registrarLog("DadosTratados", "FALHA", 0, e.getMessage());
        }

        // IMPORTA ARTISTAS + MÚSICAS → só aqui notificamos o cliente
        try {
            ImportadorArtistaMusica importadorArtistaMusica = new ImportadorArtistaMusica();
            importadorArtistaMusica.importar();

            registrarLog("Artista/Musica", "SUCESSO", 0, "Importação concluída.");

            if (importadorArtistaMusica.getCountMusicas() > 0 || importadorArtistaMusica.getCountArtistas() > 0) {
                Slack slack = new Slack(idUsuario);
                slack.enviarNotificacaoFinal(importadorArtistaMusica.getCountArtistas(),
                        importadorArtistaMusica.getCountMusicas());
            }

        } catch (Exception e) {
            registrarLog("Artista/Musica", "FALHA", 0, e.getMessage());
        }

        System.out.println("Processo completo de importação finalizado!");
    }

    // 🔹 Busca o primeiro usuário ativo no SlackAtivo
    public static int buscarUsuarioAtivo() {
        String sql = "SELECT fk_usuario FROM SlackAtivo WHERE notificacoes_desativadas = 0 LIMIT 1";

        try (Connection conexao = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("fk_usuario");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário ativo: " + e.getMessage());
        }
        return -1;
    }

    public static void registrarLog(String tabela, String status, int registros, String mensagem) {
        String sql = """
            INSERT INTO LogImportacao (tabela_alvo, id_status, registros_inseridos, mensagem)
            VALUES (?, ?, ?, ?)
        """;

        int statusId = switch (status.toUpperCase()) {
            case "SUCESSO" -> 1;
            case "PARCIAL" -> 2;
            case "FALHA" -> 3;
            default -> 3;
        };

        try (Connection conexao = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, tabela);
            stmt.setInt(2, statusId);
            stmt.setInt(3, registros);
            stmt.setString(4, mensagem);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Falha ao registrar log: " + e.getMessage());
        }
    }
}