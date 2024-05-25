import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileOutput implements AutoCloseable {
    private PrintWriter writer;

    public FileOutput(String fileName) throws IOException {
        writer = new PrintWriter(new FileWriter(fileName));
    }

    public void writeLine(String line) {
        writer.println(line);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}