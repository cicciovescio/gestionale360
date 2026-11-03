package it.meridiana.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PostgresSchemaFix implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(PostgresSchemaFix.class);
    private final DataSource dataSource;
    private final Environment env;

    public PostgresSchemaFix(DataSource dataSource, Environment env) {
        this.dataSource = dataSource;
        this.env = env;
    }

    @Override
    public void run(String... args) throws Exception {
        String url = env.getProperty("spring.datasource.url", "");
        if (!url.contains("postgresql")) {
            return;
        }
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            toText(s, "party");
            toText(s, "doc");
            toText(s, "item");
            toText(s, "company");
            toText(s, "work_event");
            toText(s, "budget_entry");
            toText(s, "investment");
            toText(s, "bank_loan");
        }
    }

    private void toText(Statement s, String table) {
        try {
            s.execute("""
                    DO $fix$
                    BEGIN
                      IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema = 'public'
                          AND table_name = '%s'
                          AND column_name = 'notes'
                          AND udt_name = 'oid'
                      ) THEN
                        ALTER TABLE %s ALTER COLUMN notes DROP DEFAULT;
                        ALTER TABLE %s ALTER COLUMN notes TYPE text USING NULL;
                      END IF;
                    END
                    $fix$;
                    """.formatted(table, table, table));
        } catch (Exception ex) {
            log.warn("Conversione notes {} non riuscita: {}", table, ex.getMessage());
        }
    }
}
