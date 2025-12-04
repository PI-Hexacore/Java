package school.sptech;

import java.sql.*;

public class ImportadorArtistaMusica extends Importador {

    private static final String URL = "jdbc:mysql://localhost:3306/hexacore?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "142536";

    private static final String SELECT_DADOS = """
            SELECT fk_spotify_top, fk_spotify_youtube, nm_artista, nm_track,
                   tp_album, nm_album, cd_rank, nm_pais, qt_stream, ds_genero
            FROM DadosTratadosTrusted
            """;

    private int countArtistas = 0;
    private int countMusicas = 0;

    public int getCountArtistas() { return countArtistas; }
    public int getCountMusicas() { return countMusicas; }

    @Override
    public void importar() {

        try (Connection conexao = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmtSelect = conexao.prepareStatement(SELECT_DADOS)) {

            conexao.setAutoCommit(false);

            Artista artistaService = new Artista(conexao);
            Musica musicaService = new Musica(conexao);

            PreparedStatement stmtBuscaArtista = artistaService.prepararSelectArtista();
            PreparedStatement stmtInsertArtista = artistaService.prepararInsertArtista();
            PreparedStatement stmtInsertMusica = musicaService.prepararInsertMusica();

            try (ResultSet rs = stmtSelect.executeQuery()) {
                while (rs.next()) {

                    Integer fkTop = rs.getInt("fk_spotify_top");
                    Integer fkYt = rs.getInt("fk_spotify_youtube");
                    String artista = rs.getString("nm_artista");
                    String track = rs.getString("nm_track");
                    String tpAlbum = rs.getString("tp_album");
                    String nomeAlbum = rs.getString("nm_album");
                    Integer rank = rs.getInt("cd_rank");
                    String pais = rs.getString("nm_pais");
                    Integer streams = rs.getInt("qt_stream");
                    String genero = rs.getString("ds_genero");

                    Artista.ResultadoArtista resultado = artistaService.obterOuCriarArtista(
                            artista, genero, fkTop, fkYt, stmtBuscaArtista, stmtInsertArtista
                    );

                    if (resultado.isInserido()) {
                        countArtistas++;
                    }

                    musicaService.adicionarMusicaBatch(
                            stmtInsertMusica,
                            track,
                            tpAlbum,
                            nomeAlbum,
                            rank,
                            pais,
                            streams,
                            resultado.getIdArtista(),
                            fkTop,
                            fkYt
                    );

                    countMusicas++;
                }
            }

            stmtInsertMusica.executeBatch();
            conexao.commit();

            Main.registrarLog("Artista", "SUCESSO", countArtistas, null);
            Main.registrarLog("Musica", "SUCESSO", countMusicas, null);

            System.out.printf("✔ Inseridos %d artistas e %d músicas.%n", countArtistas, countMusicas);

        } catch (SQLException e) {
            Main.registrarLog("ImportadorArtistaMusica", "FALHA", 0, e.getMessage());
            e.printStackTrace();
        }
    }
}