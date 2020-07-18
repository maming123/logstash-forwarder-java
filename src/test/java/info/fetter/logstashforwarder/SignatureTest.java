package info.fetter.logstashforwarder;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Using signature for file id in local cache is about 10x more efficient than using inode.
 */

public class SignatureTest {
    @Test
    public void testSignature() throws IOException {
        FileState state = new FileState(new File("SignatureTest.log"));
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            long signature = FileSigner.computeSignature(state.getRandomAccessFile(), 4096);
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Signature calculation used: " + timeElapsed);
    }

    @Test
    public void testInode() throws IOException {
        File file = new File("SignatureTest.log");
        FileState state = new FileState(file);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            long ino = (long) Files.getAttribute(file.toPath(), "unix:ino");
            long dev = (long) Files.getAttribute(file.toPath(), "unix:dev");
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Inode calculation used: " + timeElapsed);
    }
}
