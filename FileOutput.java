import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A utility class for writing lines of text to a file.
 * Implements AutoCloseable to ensure proper resource management.
 */
public class FileOutput implements AutoCloseable {
    private final PrintWriter writer;       // An object used to write text to the file

    /**
     * Constructs a FileOutput object and initializes the PrintWriter.
     * @param fileName The name of the file to write to.
     * @throws IOException If an I/O error occurs while opening the file.
     */
    public FileOutput(String fileName) throws IOException {
        writer = new PrintWriter(new FileWriter(fileName));
    }

    /**
     * Writes a line of text to the file.
     * @param line The line of text to write.
     */
    public void writeLine(String line) {
        writer.println(line);
    }

    /**
     * Closes the PrintWriter, flushing any buffered output.
     * @throws IOException If an I/O error occurs while closing the writer.
     */
    @Override
    public void close() throws IOException {
        writer.close();
    }
}