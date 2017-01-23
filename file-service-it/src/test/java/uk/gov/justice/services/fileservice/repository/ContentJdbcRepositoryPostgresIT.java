package uk.gov.justice.services.fileservice.repository;

import static java.io.File.createTempFile;
import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.fileservice.it.helpers.JdbcConnectionProvider;
import uk.gov.justice.services.fileservice.it.helpers.LiquibaseDatabaseBootstrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Remove this ignore if you want to run the File Service against a local Postgres database")
public class ContentJdbcRepositoryPostgresIT {

    private static final String LIQUIBASE_FILE_STORE_DB_CHANGELOG_XML = "liquibase/file-service-liquibase-db-changelog.xml";

    private static final String URL = "jdbc:postgresql://localhost:5432/fileservice";
    private static final String USERNAME = "fileservice";
    private static final String PASSWORD = "fileservice";
    private static final String DRIVER_CLASS = org.postgresql.Driver.class.getName();

    private final JdbcConnectionProvider connectionProvider = new JdbcConnectionProvider();
    private final ContentJdbcRepository contentJdbcRepository = new ContentJdbcRepository();
    private final LiquibaseDatabaseBootstrapper liquibaseDatabaseBootstrapper = new LiquibaseDatabaseBootstrapper();

    private Connection connection;

    @Before
    public void setupDatabase() throws Exception {

        connection = connectionProvider.getConnection(URL, USERNAME, PASSWORD, DRIVER_CLASS);

        liquibaseDatabaseBootstrapper.bootstrap(
                LIQUIBASE_FILE_STORE_DB_CHANGELOG_XML,
                connection);
    }

    @After
    public void closeConnection() throws SQLException {

        if(connection != null) {
            connection.close();
        }
    }

    @Test
    public void shouldStoreAndRetrieveFileContent() throws Exception {

        final UUID fileId = randomUUID();
        final File inputFile = getFile("/for-testing-file-store.jpg");

        final InputStream content = new FileInputStream(inputFile);
        contentJdbcRepository.insert(fileId, content, connection);
        content.close();

        final Optional<InputStream> fileContents = contentJdbcRepository.findByFileId(fileId, connection);

        assertThat(fileContents.isPresent(), is(true));

        final InputStream contentStream = fileContents.orElseThrow(() -> new AssertionError("Failed to find file contents"));

        final File outputFile = createTempFile("/created-for-testing-file-store-please-delete-me_1", "jpg");
        outputFile.deleteOnExit();

        copy(contentStream, outputFile.toPath(), REPLACE_EXISTING);

        contentStream.close();

        assertThat(outputFile.exists(), is(true));
        assertThat(outputFile.length(), is(greaterThan(0L)));
        assertThat(outputFile.length(), is(inputFile.length()));
    }

    @Test
    public void shouldDeleteFileContent() throws Exception {

        final UUID fileId = randomUUID();
        final File inputFile = getFile("/for-testing-file-store.jpg");

        final InputStream content = new FileInputStream(inputFile);
        contentJdbcRepository.insert(fileId, content, connection);

        content.close();

        final InputStream contentStream = contentJdbcRepository
                .findByFileId(fileId, connection)
                .orElseThrow(() -> new AssertionError("Failed to find file content"));

        contentStream.close();

        contentJdbcRepository.delete(fileId, connection);

        assertThat(contentJdbcRepository.findByFileId(fileId, connection).isPresent(), is(false));
    }

    public File getFile(final String fileName) throws URISyntaxException {
        final URL url = getClass().getResource(fileName);
        return new File(url.toURI());
    }
}
