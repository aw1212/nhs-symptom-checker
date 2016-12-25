package nhs.uk.co.alessandra.indexing.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.google.common.collect.ImmutableMap;

import nhs.uk.co.alessandra.indexing.symptom.Symptom;

@Repository
public class NHSSymptomIndexerRepository {

    private static final String CREATE_SYMPTOMS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS symptoms " +
            "(ailment VARCHAR(255), symptom VARCHAR(255), PRIMARY KEY (ailment, symptom))";
    private static final String CREATE_START_PATHS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS start_paths " +
            "(url VARCHAR(255), visited BOOLEAN NOT NULL DEFAULT FALSE)";
    private static final String ADD_SYMPTOM_SQL = "MERGE INTO symptoms (ailment, symptom) KEY (ailment, symptom) " +
            "VALUES (:ailment, :symptom)";
    private static final String GET_SYMPTOMS_SQL = "SELECT ailment, symptom FROM symptoms ORDER BY ailment";
    private static final String GET_SYMPTOMS_BY_AILMENT = "SELECT symptom FROM symptoms WHERE ailment = :ailment";
    private static final String UPDATE_START_PATHS_SQL = "UPDATE start_paths SET visited = TRUE WHERE url = :url";
    private static final String GET_START_PATHS_SQL = "SELECT url FROM start_paths WHERE visited = FALSE";
    private static final String POPULATE_START_PATHS_SQL = "INSERT INTO start_paths (url) VALUES " +
            "('http://www.nhs.uk/conditions/Bronchitis/Pages/Symptoms.aspx'), " +
            "('http://www.nhs.uk/conditions/Heart-block/Pages/Symptoms.aspx'), " +
            "('http://www.nhs.uk/conditions/frozen-shoulder/Pages/Symptoms.aspx'), " +
            "('http://www.nhs.uk/conditions/coronary-heart-disease/Pages/Symptoms.aspx'), " +
            "('http://www.nhs.uk/conditions/warts/Pages/Symptoms.aspx'), " +
            "('http://www.nhs.uk/conditions/Sleep-paralysis/Pages/Symptoms.aspx'), " +
            "('http://www.nhs.uk/conditions/Glue-ear/Pages/Symptoms.aspx'), " +
            "('http://www.nhs.uk/conditions/Depression/Pages/Symptoms.aspx'), " +
            "('http://www.nhs.uk/conditions/Turners-syndrome/Pages/Symptoms.aspx'), " +
            "('http://www.nhs.uk/conditions/Obsessive-compulsive-disorder/Pages/Symptoms.aspx')";

    @Autowired
    private JdbcDataSource dataSource;
    @Autowired
    private SymptomRowMapper symptomRowMapper;

    @PostConstruct
    public void init() {
        createSymptomsTable();
        createStartPathsTable();
        populateStartPathsTable();
    }

    private void createSymptomsTable() {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        jt.execute(CREATE_SYMPTOMS_TABLE_SQL);
    }

    private void createStartPathsTable() {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        jt.execute(CREATE_START_PATHS_TABLE_SQL);
    }

    private void populateStartPathsTable() {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        jt.execute(POPULATE_START_PATHS_SQL);
    }

    public void addSymptom(String ailment, String symptom) {
        NamedParameterJdbcTemplate njt = new NamedParameterJdbcTemplate(new JdbcTemplate(dataSource));
        njt.update(ADD_SYMPTOM_SQL, ImmutableMap.of("ailment", ailment, "symptom", symptom));
    }

    public List<Symptom> getSymptoms() {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        return jt.query(GET_SYMPTOMS_SQL, symptomRowMapper);
    }

    public List<String> getSymptomsByAilment(String ailment) {
        NamedParameterJdbcTemplate njt = new NamedParameterJdbcTemplate(new JdbcTemplate(dataSource));
        return njt.queryForList(GET_SYMPTOMS_BY_AILMENT, ImmutableMap.of("ailment", ailment), String.class);
    }

    public void markStartPathsAsVisited(String url) {
        NamedParameterJdbcTemplate njt = new NamedParameterJdbcTemplate(new JdbcTemplate(dataSource));
        njt.update(UPDATE_START_PATHS_SQL, ImmutableMap.of("url", url));
    }

    public List<String> getStartUrls() {
        List<String> startUrls = new ArrayList<>();
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        List<Map<String, Object>> urls = jt.queryForList(GET_START_PATHS_SQL);
        startUrls.addAll(urls.stream()
                .map(url -> (String) url.get("url"))
                .collect(Collectors.toList()));
        return startUrls;
    }

}
