package school.sptech;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;

public class Slack {

    private static final String URL_DB = "jdbc:mysql://localhost:3306/hexacore?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "142536";

    private final int idUsuario;

    public Slack(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    // 🔹 Busca o webhook do banco
    private String buscarWebhook() {
        String sql = "SELECT webhook FROM webhook WHERE id_webhook = 1";
        try (Connection conexao = DriverManager.getConnection(URL_DB, USER, PASSWORD);
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("webhook");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar webhook: " + e.getMessage());
        }
        return null;
    }

    public SlackConfig buscarConfig() {
        String sql = """
            SELECT notificacoes_desativadas, receber_pais, receber_musica, receber_artista
            FROM SlackAtivo
            WHERE fk_usuario = ?
        """;

        try (Connection conexao = DriverManager.getConnection(URL_DB, USER, PASSWORD);
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new SlackConfig(
                        rs.getInt("notificacoes_desativadas") == 0,
                        rs.getInt("receber_pais") == 1,
                        rs.getInt("receber_musica") == 1,
                        rs.getInt("receber_artista") == 1
                );
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar config Slack: " + e.getMessage());
        }
        return null;
    }

    public void enviarNotificacaoFinal(int countArtistas, int countMusicas) {
        SlackConfig config = buscarConfig();

        if (config == null) {
            postarSlack("⚠ Nenhuma configuração Slack encontrada para usuário " + idUsuario);
            return;
        }

        if (!config.isAtivo()) {
            postarSlack("🔕 Slack desativado para usuário " + idUsuario);
            return;
        }

        if (countArtistas == 0 && countMusicas == 0) {
            return; // nada inserido → não notifica
        }

        StringBuilder mensagem = new StringBuilder();
        mensagem.append("✅ Novos dados disponiveis em nosso site! \n");

        if (!config.isReceberPais() && !config.isReceberMusica() && !config.isReceberArtista()) {
            mensagem.append("📢 Novos dados em nosso site.");
        } else {
            if (config.isReceberPais()) {
                mensagem.append("🌍  Novos países disponíveis em nossa dashboard!\n");
            }
            if (config.isReceberMusica()) {
                mensagem.append("🎵 Temos novas músicas em nosso site, oque acha de dar uma olhada na sua dashboard!\n");
            }
            if (config.isReceberArtista()) {
                mensagem.append("🧑‍🎤 Novos artistas dísponiveis, descubra mais sobre o gosto popular.\n");
            }
        }

        postarSlack(mensagem.toString().trim());
    }

    private void postarSlack(String mensagem) {
        String webhookUrl = buscarWebhook();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            System.err.println("Webhook não encontrado no banco.");
            return;
        }

        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String payload = "{\"text\":\"" + mensagem.replace("\"", "\\\"") + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                registrarSlackNotificacao(mensagem);
            } else {
                System.err.println("Erro ao enviar para Slack: HTTP " + responseCode);
            }

        } catch (Exception e) {
            System.err.println("Falha ao postar no Slack: " + e.getMessage());
        }
    }

    private void registrarSlackNotificacao(String mensagem) {
        String sql = """
            INSERT INTO SlackNotificacao (
                canal_slack, mensagem, dt_envio,
                log_importacao_id, log_importacao_id_status,
                id_status_ativo, fk_usuario
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conexao = DriverManager.getConnection(URL_DB, USER, PASSWORD);
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, "Importação");
            stmt.setString(2, mensagem);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setNull(4, Types.INTEGER);
            stmt.setNull(5, Types.INTEGER);
            stmt.setInt(6, 1);
            stmt.setInt(7, idUsuario);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao registrar SlackNotificacao: " + e.getMessage());
        }
    }
}