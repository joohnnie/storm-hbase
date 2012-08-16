package backtype.storm.contrib.hbase.example;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.contrib.hbase.bolts.HBaseBolt;
import backtype.storm.contrib.hbase.utils.TupleTableConfig;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

/**
 * Example HBaseBolt topology that inserts a stream of shortened URL's into the
 * 'data' column family of the 'shorturl' table
 * <p>
 * Assumes the HBase table has been created. E.g.
 * "create 'shorturl', {NAME => 'data', VERSIONS => 3}, {NAME => 'daily', VERSION => 1, TTL => 604800}"
 */
public class HBaseExampleTopology {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TopologyBuilder builder = new TopologyBuilder();

    // Add test spout
    builder.setSpout("spout", new TestSpout(), 1);

    // Build TupleTableConifg
    TupleTableConfig config = new TupleTableConfig("shorturl", "shortid");
    config.setBatch(false);
    config.addColumn("data", "url");
    config.addColumn("data", "user");
    config.addColumn("data", "date");

    // Add HBaseBolt
    builder.setBolt("hbase", new HBaseBolt(config), 1).shuffleGrouping("spout");

    Config stormConf = new Config();
    stormConf.setDebug(true);

    LocalCluster cluster = new LocalCluster();
    cluster
        .submitTopology("hbase-example", stormConf, builder.createTopology());

    Utils.sleep(10000);
    cluster.shutdown();
  }

}
