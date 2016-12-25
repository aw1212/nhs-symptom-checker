package nhs.uk.co.alessandra.indexing;

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean(name = "directory")
    public Directory directory() throws IOException {
        return new RAMDirectory();
    }

    @Bean(name = "dataSource")
    public JdbcDataSource dataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setUrl("jdbc:h2:~/test;MVCC=TRUE"); //ensure Multi-Version Concurrency Control is on
        return ds;
    }

}
