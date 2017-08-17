package au.com.marlo.tests.routes;

import au.com.marlo.tests.processors.UnzipInChunks;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by isilva on 24/07/17.
 */
public class MyTestRouteBuilder extends RouteBuilder {
    private Logger LOG = LoggerFactory.getLogger(MyTestRouteBuilder.class);
    private Long startTime = 0L;

    public void configure() throws Exception {
//        sftp_v1();
        sftp_v4();
    }

    /**
     * Thread0 Poll metadata -> Multiple threads download zip and upload
     */
    public  void sftp_v1()
    {
        from("sftp://localhost:21100//home/isilva/zip_unzip_tests/file_polling_test?stepwise=false&username=isilva&password={{sftp.isilva.pass}}&localWorkDirectory=processing&idempotent=true")
            .streamCaching()
            .process(new Processor() {
                public void process(Exchange exchange) throws Exception {
                    Date date = new Date();
                    startTime = date.getTime();
                }
            })
            .log("Downloaded file ${file:name}...")
            .to("seda:mytest");

        from("seda:mytest?concurrentConsumers=4")
            .log("I'm concurrent")
            .log("Unzipping file ${file:name}...")
            .process(new UnzipInChunks())
            .log("Done unzipping ${file:name}")
            .recipientList(simple("file:/home/isilva/zip_unzip_tests/archive/${file:name}"));

        from("file:{{unzipped.file.path}}?move=/home/isilva/zip_unzip_tests/archive/${file:name}")
            .streamCaching()
            .log("Sending file ${file:name} to target...")
            .to("sftp://localhost:21100//home/isilva/zip_unzip_tests/my_target_test?stepwise=false&username=isilva&password={{sftp.isilva.pass}}")
            .log("------------------>Done sending ${file:name}")
            .process(new Processor() {
                public void process(Exchange exchange) throws Exception {
                    Date date = new Date();
                    Long currentTime = date.getTime();
                    Long elapsedTime = currentTime - startTime;
                    LOG.info("---------------------TOTAL PROCESSING TIME ---------------> " + elapsedTime + " ms");
                }
            });
    }

    /**
     *
     *  Thread0 Download -> Multiple threads zip -> Thread5 upload
     *
     */
    public  void sftp_v2()
    {
        from("sftp://localhost:21100//home/isilva/zip_unzip_tests/file_polling_test?stepwise=false&username=isilva&password={{sftp.isilva.pass}}&localWorkDirectory=processing&move=.camel&idempotent=true&passiveMode=true&disconnect=true")
                .streamCaching()
                .log("Donwloaded file ${file:name}...")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        Date date = new Date();
                        exchange.setProperty("startTime", date.getTime());
                    }
                })
                .to("seda:forkZip");

        from("seda:forkZip?concurrentConsumers=4")
                .log("I'm concurrent")
                .log("Unzipping file ${file:name}...")
                .unmarshal().zipFile()
                .log("Done unzipping ${file:name}")
                .to("seda:joinTarget");

        from("seda:joinTarget?concurrentConsumers=1")
                .log("Sending file ${file:name} to target...")
                .to("sftp://localhost:21100//home/isilva/zip_unzip_tests/my_target_test?stepwise=false&username=isilva&password={{sftp.isilva.pass}}&passiveMode=true&disconnect=true")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        Date date = new Date();
                        Long currentTime = date.getTime();
                        Long elapsedTime = currentTime - exchange.getProperty("startTime",Long.class);
                        LOG.info("---------------------TOTAL PROCESSING TIME ---------------> file: " + exchange.getIn().getHeader("CamelFileName") + " " + elapsedTime + " ms");
                    }
                });
    }

    /**
     *
     *  Thread0 Download -> Multiple threads zip and upload
     *
     */
    public  void sftp_v3()
    {
        from("sftp://localhost:21100//home/isilva/zip_unzip_tests/file_polling_test?stepwise=false&username=isilva&password={{sftp.isilva.pass}}&localWorkDirectory=processing&idempotent=true&passiveMode=true&disconnect=true")
                .log("Donwloaded file ${file:name}...")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        Date date = new Date();
                        exchange.setProperty("startTime", date.getTime());
                    }
                })
                .to("seda:forkZip");

        from("seda:forkZip?concurrentConsumers=4")
                .log("I'm concurrent")
                .log("Unzipping file ${file:name}...")
                .unmarshal().zipFile()
                .to("file:test")
                .log("Done unzipping ${file:name}");

        from("file:test?delete=true")
            .streamCaching()
            .log("Sending file ${file:name} to target...")
                .to("sftp://localhost:21100//home/isilva/zip_unzip_tests/my_target_test?stepwise=false&username=isilva&password={{sftp.isilva.pass}}&passiveMode=true&disconnect=true")
                .log("Done sending file ${file:name}");
//            .process(new Processor() {
//                public void process(Exchange exchange) throws Exception {
//                    Date date = new Date();
//                    Long currentTime = date.getTime();
//                    Long elapsedTime = currentTime - exchange.getProperty("startTime",Long.class);
//                    LOG.info("---------------------TOTAL PROCESSING TIME ---------------> file: " + exchange.getIn().getHeader("CamelFileName") + " " + elapsedTime + " ms");
//                }
//            });
    }

    /**
     * Thread0 Poll metadata -> Multiple threads download zip and upload
     */
    public  void sftp_v4()
    {
        from("sftp://localhost:21100//home/isilva/zip_unzip_tests/file_polling_test?stepwise=false&username=isilva&password={{sftp.isilva.pass}}&localWorkDirectory=processing&idempotent=true")
                .streamCaching()
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        Date date = new Date();
                        startTime = date.getTime();
                    }
                })
                .log("Downloaded file ${file:name}...")
                .multicast()
                    .to("seda:myFork", "seda:myBLABLA");

        from("seda:myFork?concurrentConsumers=2")
                .log("I'm concurrent on myFork")
                .log("myFork Unzipping file ${file:name}...")
                .process(new UnzipInChunks())
                .log("myFork Done unzipping ${file:name}")
                .to("direct:myJoin");

        from("seda:myBLABLA?concurrentConsumers=2")
                .log("I'm concurrent on myBLABLA")
                .log("myBLABLA Unzipping file ${file:name}...")
                .process(new UnzipInChunks())
                .log("myBLABLA Done unzipping ${file:name}")
                .to("direct:myJoin");

        from("direct:myJoin")
                .log("I'm in direct")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        log.info("I'm going to sleep....");
                        Thread.sleep(100000);
                        log.info("Woke up.");
                    }
                })
                .log("Sending file ${file:name} to target...")
                .to("file:/home/isilva/zip_unzip_tests/archive");
    }

}
