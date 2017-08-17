package au.com.marlo.tests.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by isilva on 26/07/17.
 */
public class UnzipInChunks implements Processor{

    public void process(Exchange exchange) throws Exception {
        String zipFile = exchange.getIn().getHeader("CamelFileLocalWorkPath", String.class);

        FileInputStream fis = new FileInputStream(zipFile);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;

        while ((entry = zis.getNextEntry()) != null) {
            System.out.println("Unzipping: " + entry.getName());

            int size;
            byte[] buffer = new byte[2048];

            FileOutputStream fos = new FileOutputStream(exchange.getContext().resolvePropertyPlaceholders("{{unzipped.file.path}}") + "/" + entry.getName());
            BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);

            while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, size);
            }
            bos.flush();
            bos.close();
        }
        zis.close();
        fis.close();
    }

}
