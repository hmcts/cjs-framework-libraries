package uk.gov.justice.services.fileservice.repository;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.fileservice.api.DataIntegrityException;
import uk.gov.justice.services.fileservice.api.FileServiceException;
import uk.gov.justice.services.fileservice.api.StorageException;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

/**
 * Class for handling inserts/updates/selects on the 'content' database table. This class is not
 * transactional. Each method takes a valid database connection and it is assumed that the
 * transaction would have already been started on that connection.
 * <p>
 * There no update method on this class. This is because we should never update content. Delete and
 * recreate if you need to change the file content.
 * <p>
 * NB. This class does not have a unit test, but is tested instead by
 * FilePersistenceIntegrationTest
 */
public class ContentJdbcRepository {

    public static final String SQL_FIND_BY_FILE_ID = "SELECT deleted, content FROM content WHERE file_id = ?";
    public static final String SQL_INSERT_CONTENT = "INSERT INTO content(file_id, content, deleted) VALUES(?, ?, ?)";
    public static final String MARK_AS_DELETED_SQL = "UPDATE content SET deleted = TRUE, deleted_at = ? WHERE file_id = ?";

    @Inject
    private UtcClock clock;

    /**
     * Inserts the content into the content table as an array of bytes[]
     *
     * @param fileId     the file id of the content
     * @param content    an InputStream to the file content
     * @param connection the database connection. It is assumed that a transaction has previously
     *                   been started on this connection.
     */
    public void insert(
            final UUID fileId,
            final InputStream content,
            final Connection connection) throws FileServiceException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_CONTENT)) {

            preparedStatement.setObject(1, fileId);
            preparedStatement.setBinaryStream(2, content);
            preparedStatement.setBoolean(3, false);
            final int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected != 1) {
                throw new DataIntegrityException("Insert into content table affected " + rowsAffected + " rows!");
            }

        } catch (final SQLException e) {
            throw new StorageException("Failed to insert file into database", e);
        }
    }

    /**
     * Finds the file content for the specified file id, returned as a java {@link Optional}. If no
     * content found for that id then {@code empty()} is returned instead.
     *
     * @param fileId     the file id of the content
     * @param connection a live database connection
     * @return the file content as an array of bytes wrapped in a java {@link Optional}
     * @throws FileServiceException if the read failed and so the current transaction should be
     *                              rolled back.
     */
    public Optional<FileContent> findByFileId(
            final UUID fileId,
            final Connection connection) throws FileServiceException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_BY_FILE_ID)) {
            preparedStatement.setObject(1, fileId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final boolean deleted = resultSet.getBoolean(1);
                    if (deleted) {
                        return empty();
                    }

                    final InputStream contentStream = resultSet.getBinaryStream(2);
                    return of(new FileContent(contentStream));
                }
            }
        } catch (final SQLException e) {
            throw new StorageException(format("Failed to read content of file with file id %s", fileId), e);
        }

        return empty();
    }

    public void markAsDeleted(final UUID fileId, final ZonedDateTime deletedAt, final Connection connection) throws FileServiceException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(MARK_AS_DELETED_SQL)) {

            preparedStatement.setTimestamp(1, toSqlTimestamp(deletedAt));
            preparedStatement.setObject(2, fileId);

            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new StorageException(format("Failed to soft delete content of file with fileId '%s' and deletedAt '%s'", fileId, deletedAt), e);
        }
    }
}
