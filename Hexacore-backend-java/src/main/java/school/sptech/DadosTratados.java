package school.sptech;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DadosTratados {

    private static final String URL = "jdbc:mysql://172.31.23.38:3306/hexacore?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "142536";

    private Integer fk_spotify_top;
    private Integer fk_spotify_youtube;
    private String nm_artista;
    private String nm_track;
    private String tp_album;
    private String nm_titulo;
    private Integer qt_stream;
    private String nm_album;
    private Integer cd_rank;
    private LocalDateTime dt_rank;
    private String nm_pais;
    private String ds_chart;
    private String ds_trend;
    private String genero;

    public DadosTratados(Integer fk_spotify_top, Integer fk_spotify_youtube, String nm_artista, String nm_track, String tp_album, String nm_titulo, Integer qt_stream, String nm_album, Integer cd_rank, LocalDateTime dt_rank, String nm_pais, String ds_chart, String ds_trend, String generos) {
        this.fk_spotify_top = fk_spotify_top;
        this.fk_spotify_youtube = fk_spotify_youtube;
        this.nm_artista = nm_artista;
        this.nm_track = nm_track;
        this.tp_album = tp_album;
        this.nm_titulo = nm_titulo;
        this.qt_stream = qt_stream;
        this.nm_album = nm_album;
        this.cd_rank = cd_rank;
        this.dt_rank = dt_rank;
        this.nm_pais = nm_pais;
        this.ds_chart = ds_chart;
        this.ds_trend = ds_trend;
        this.genero = generos;
    }

    public List<DadosTratados> buscarDadosTratados() {
        List<DadosTratados> lista = new ArrayList<>();

        String sql = """
    SELECT
        st.id_spotify_top,
        sy.id_spotify_youtube,
        st.nm_titulo AS titulo_spotify,
        sy.nm_track AS track_youtube,
        st.nm_artista AS artista_spotify,
        sy.nm_artista AS artista_youtube,
        st.qt_stream AS streams_spotify,
        sy.qt_stream AS streams_youtube,
        st.nm_pais AS pais,
        sy.nm_album AS album,
        sy.tp_album AS tipo_album,
        st.cd_rank AS `rank`,
        st.ds_genero AS genero,
        st.dt_rank AS data_rank,
        st.ds_chart AS chart,
        st.ds_trend AS trend
    FROM SpotifyTop st
    JOIN SpotifyYoutube sy
        ON (
            st.nm_artista = sy.nm_artista
            OR st.nm_titulo = sy.nm_track
            OR st.nm_titulo = sy.nm_title
        );
""";


        try (Connection conexao = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DadosTratados dado = new DadosTratados(
                        rs.getInt("id_spotify_top"),
                        rs.getInt("id_spotify_youtube"),
                        rs.getString("artista_spotify"),
                        rs.getString("track_youtube"),
                        rs.getString("tipo_album"),
                        rs.getString("titulo_spotify"),
                        somarStreams(rs.getInt("streams_spotify"), rs.getInt("streams_spotify")),
                        rs.getString("album"),
                        rs.getInt("rank"),
                        rs.getTimestamp("data_rank") != null ? rs.getTimestamp("data_rank").toLocalDateTime() : null,
                        rs.getString("pais"),
                        rs.getString("chart"),
                        rs.getString("trend"),
                        rs.getString("genero")
                );

                lista.add(dado);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    private Integer somarStreams(Integer a, Integer b) {
        if (a == null) a = 0;
        if (b == null) b = 0;
        return a + b;
    }

    public void inserirTodos() {
        List<DadosTratados> lista = buscarDadosTratados();

        String insertSql = """
            INSERT INTO DadosTratados 
            (fk_spotify_top, fk_spotify_youtube, nm_artista, nm_track, tp_album, nm_titulo, 
             qt_stream, nm_album, cd_rank, dt_rank, nm_pais, ds_chart, ds_trend, ds_genero)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conexao = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conexao.prepareStatement(insertSql)) {

            int count = 0;

            for (DadosTratados d : lista) {
                stmt.setInt(1, d.getFk_spotify_top());
                stmt.setInt(2, d.getFk_spotify_youtube());
                stmt.setString(3, d.getNm_artista());
                stmt.setString(4, d.getNm_track());
                stmt.setString(5, d.getTp_album());
                stmt.setString(6, d.getNm_titulo());
                stmt.setInt(7, d.getQt_stream());
                stmt.setString(8, d.getNm_album());
                stmt.setInt(9, d.getCd_rank());
                stmt.setTimestamp(10, Timestamp.valueOf(d.getDt_rank()));
                stmt.setString(11, d.getNm_pais());
                stmt.setString(12, d.getDs_chart());
                stmt.setString(13, d.getDs_trend());
                stmt.setString(14, d.getGenero());
                stmt.addBatch();
                count++;
                if (count % 100 == 0) {
                    stmt.executeBatch();
                }
            }

            stmt.executeBatch();
            System.out.println(" Inserido com sucesso! " + count + " registros adicionados.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Integer getFk_spotify_top() {
        return fk_spotify_top;
    }

    public void setFk_spotify_top(Integer fk_spotify_top) {
        this.fk_spotify_top = fk_spotify_top;
    }

    public Integer getFk_spotify_youtube() {
        return fk_spotify_youtube;
    }

    public void setFk_spotify_youtube(Integer fk_spotify_youtube) {
        this.fk_spotify_youtube = fk_spotify_youtube;
    }

    public String getNm_artista() {
        return nm_artista;
    }

    public void setNm_artista(String nm_artista) {
        this.nm_artista = nm_artista;
    }

    public String getNm_track() {
        return nm_track;
    }

    public void setNm_track(String nm_track) {
        this.nm_track = nm_track;
    }

    public String getTp_album() {
        return tp_album;
    }

    public void setTp_album(String tp_album) {
        this.tp_album = tp_album;
    }

    public String getNm_titulo() {
        return nm_titulo;
    }

    public void setNm_titulo(String nm_titulo) {
        this.nm_titulo = nm_titulo;
    }

    public Integer getQt_stream() {
        return qt_stream;
    }

    public void setQt_stream(Integer qt_stream) {
        this.qt_stream = qt_stream;
    }

    public String getNm_album() {
        return nm_album;
    }

    public void setNm_album(String nm_album) {
        this.nm_album = nm_album;
    }

    public Integer getCd_rank() {
        return cd_rank;
    }

    public void setCd_rank(Integer cd_rank) {
        this.cd_rank = cd_rank;
    }

    public LocalDateTime getDt_rank() {
        return dt_rank;
    }

    public void setDt_rank(LocalDateTime dt_rank) {
        this.dt_rank = dt_rank;
    }

    public String getNm_pais() {
        return nm_pais;
    }

    public void setNm_pais(String nm_pais) {
        this.nm_pais = nm_pais;
    }

    public String getDs_chart() {
        return ds_chart;
    }

    public void setDs_chart(String ds_chart) {
        this.ds_chart = ds_chart;
    }

    public String getDs_trend() {
        return ds_trend;
    }

    public void setDs_trend(String ds_trend) {
        this.ds_trend = ds_trend;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
}
