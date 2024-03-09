package de.kobich.audiosolutions.frontend.service;

import org.hibernate.dialect.HSQLDialect;

/**
 * Workaround to avoid "alter table track drop constraint FKi28jadqiuqk1dlxtl0me7hqh2" before creating tables.
 * @see http://stackoverflow.com/questions/12054422/unsuccessful-alter-table-xxx-drop-constraint-yyy-in-hibernate-jpa-hsqldb-standa/20698339
 * @see https://hibernate.onjira.com/browse/HHH-7002 
 */
public class HSQLInMemoryDialect extends HSQLDialect {

    @Override
    public boolean dropConstraints() {
        // We don't need to drop constraints before dropping tables, that just
        // leads to error messages about missing tables when we don't have a
        // schema in the database
        return false;
    }
}
