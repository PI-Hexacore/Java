package school.sptech;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Artista {

    private final Connection conexao;

    public Artista(Connection conexao) {
        this.conexao = conexao;
    }

    // AGREGAÇÃO: lista de músicas
    private List<Musica> musicas = new ArrayList<>();

    public void adicionarMusica(Musica musica) {
        musicas.add(musica);
        musica.setArtista(this); // ligação dos dois lados
    }

    public List<Musica> getMusicas() {
        return musicas;
    }

    private static final String SELECT_ARTISTA =
            "SELECT id_artista FROM ArtistaClient WHERE nm_artista = ?";

    private static final String INSERT_ARTISTA = """
            INSERT INTO ArtistaClient (nm_artista, ds_genero_musical,
                                 fk_dados_spotify_top, fk_dados_spotify_youtube)
            VALUES (?, ?, ?, ?)
            """;

    public PreparedStatement prepararSelectArtista() throws SQLException {
        return conexao.prepareStatement(SELECT_ARTISTA);
    }

    public PreparedStatement prepararInsertArtista() throws SQLException {
        return conexao.prepareStatement(INSERT_ARTISTA, Statement.RETURN_GENERATED_KEYS);
    }

    // Resultado estruturado para saber se houve inserção
    public static class ResultadoArtista {
        private final int idArtista;
        private final boolean inserido;

        public ResultadoArtista(int idArtista, boolean inserido) {
            this.idArtista = idArtista;
            this.inserido = inserido;
        }

        public int getIdArtista() { return idArtista; }
        public boolean isInserido() { return inserido; }
    }

    public ResultadoArtista obterOuCriarArtista(String nomeArtista,
                                                String genero,
                                                Integer fkSpotifyTop,
                                                Integer fkSpotifyYoutube,
                                                PreparedStatement stmtBusca,
                                                PreparedStatement stmtInsert) throws SQLException {

        // tenta encontrar
        stmtBusca.setString(1, nomeArtista);
        try (ResultSet rs = stmtBusca.executeQuery()) {
            if (rs.next()) {
                return new ResultadoArtista(rs.getInt("id_artista"), false); // não inseriu
            }
        }

        // insere
        stmtInsert.setString(1, nomeArtista);
        stmtInsert.setString(2, genero);
        stmtInsert.setInt(3, fkSpotifyTop);
        stmtInsert.setInt(4, fkSpotifyYoutube);
        int affected = stmtInsert.executeUpdate();

        if (affected == 0) {
            throw new SQLException("Falha ao inserir artista: nenhuma linha afetada.");
        }

        try (ResultSet chaves = stmtInsert.getGeneratedKeys()) {
            if (!chaves.next()) {
                throw new SQLException("Falha ao obter chave de artista inserido.");
            }
            return new ResultadoArtista(chaves.getInt(1), true); // inseriu
        }
    }
}