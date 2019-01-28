package kudu;

import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.CreateTableOptions;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.PartialRow;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.spark_project.guava.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class KuduMain {


    public static void create(String KUDU_MASTER,List<String> tables) {


        KuduClient client = new KuduClient.KuduClientBuilder(KUDU_MASTER).build();

        try {

            for(String table:tables) {
                if(client.tableExists(table+"kudu")) {
                    client.deleteTable(table+"kudu");
                }
                if(!client.tableExists(table+"kudu")) {
                    List<ColumnSchema> columns = new ArrayList();

                    columns.add(new ColumnSchema.ColumnSchemaBuilder("fieldvalue", Type.STRING).key(true)
                            .build());
                    columns.add(new ColumnSchema.ColumnSchemaBuilder("tenant_id", Type.INT64).key(true)
                            .build());
                    columns.add(new ColumnSchema.ColumnSchemaBuilder("channel_id", Type.STRING).key(true)
                            .build());
                    columns.add(new ColumnSchema.ColumnSchemaBuilder("event", Type.STRING).key(true)
                            .build());
                    columns.add(new ColumnSchema.ColumnSchemaBuilder("randnum", Type.INT32).key(true)
                            .build());
                    columns.add(new ColumnSchema.ColumnSchemaBuilder("event_day", Type.INT64).key(true)
                            .build());
                    columns.add(new ColumnSchema.ColumnSchemaBuilder("rids", Type.BINARY).nullable(true).key(false)
                            .build());
                    columns.add(new ColumnSchema.ColumnSchemaBuilder("utimes", Type.STRING).nullable(true).key(false)
                            .build());

                    List<String> rangeKeys = new ArrayList<>();
                    rangeKeys.add("event_day");
                    CreateTableOptions options = new CreateTableOptions().setRangePartitionColumns(rangeKeys).addHashPartitions(ImmutableList.of("fieldvalue", "tenant_id", "channel_id", "event", "event_day"), 2)
                            .setNumReplicas(3);
                    Schema schema = new Schema(columns);
                    DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                    //时间解析
                    DateTime dateStar = DateTime.parse("2017-01-01 00:00:00", format);


                    //2023/4/1
                    for (int i = 0; i < 7; i++) {
                        DateTime dateNext = dateStar.plusMonths(6);
                        long nextMillis = dateNext.getMillis();

                        PartialRow lowerBound = schema.newPartialRow();
                        lowerBound.addLong("event_day", dateStar.getMillis() / 1000);
                        PartialRow upperBound = schema.newPartialRow();
                        upperBound.addLong("event_day", nextMillis / 1000);
                        options.addRangePartition(lowerBound, upperBound);
                        dateStar = dateNext;
                    }
                    client.createTable(table+"kudu", schema, options);
                }else {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                client.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
