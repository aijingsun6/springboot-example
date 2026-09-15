package cc.alking.example.springboot.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class EmbeddedDatabaseTest {

    private EmbeddedDatabase db;

    @BeforeEach
    public void setUp() {
        // creates an HSQL in-memory database populated from default scripts
        // classpath:schema.sql and classpath:data.sql
        db = new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .addScripts("schema.sql", "data.sql")
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }

    @Test
    public void testDataAccess() {
        JdbcTemplate template = new JdbcTemplate(db);
//        template.execute("create table t_actor (first_name varchar(100), last_name varchar(100))");
        template.update(
                "insert into t_actor (first_name, last_name) values (?, ?)",
                "Leonor", "Watling");
        template.query(
                "select first_name, last_name from t_actor",
                (rs, rowNum) -> new Actor(rs.getString(1), rs.getString(2))).forEach(System.out::println);
    }

    @AfterEach
    public void tearDown() {
        if(db != null){
            db.shutdown();
        }

    }

    public static class Actor {
        String firstName;
        String lastName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public Actor(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public String toString() {
            return "Actor{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }
    }
}
