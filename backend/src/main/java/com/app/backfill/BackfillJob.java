package com.app.backfill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BackfillJob {

    private static final Logger log = LoggerFactory.getLogger(BackfillJob.class);
    private final JdbcTemplate jdbc;

    public BackfillJob(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void backfill() {
        int updated = jdbc.update("""
            UPDATE produto
            SET valor = preco
            WHERE ctid IN (
                SELECT ctid FROM produto
                WHERE valor IS NULL AND preco IS NOT NULL
                LIMIT 1000
            )
        """);

        if (updated > 0) {
            log.info("Backfill: {} registros atualizados", updated);
        }
    }
}
