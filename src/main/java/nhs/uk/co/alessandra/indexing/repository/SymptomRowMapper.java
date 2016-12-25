package nhs.uk.co.alessandra.indexing.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import nhs.uk.co.alessandra.indexing.symptom.Symptom;

@Component
public class SymptomRowMapper implements RowMapper<Symptom> {

    @Override
    public Symptom mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        String ailment = rs.getString("ailment");
        String symptom = rs.getString("symptom");

        return new Symptom(ailment, symptom);
    }

}
