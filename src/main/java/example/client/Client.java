package example.client;

import example.client.verifier.WanVerificationService;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.internal.cache.InternalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.EnablePool;
import org.springframework.data.gemfire.config.annotation.EnablePools;
import org.springframework.geode.boot.autoconfigure.ContinuousQueryAutoConfiguration;
import example.client.domain.CusipHelper;
import example.client.domain.Trade;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

@SpringBootApplication(exclude = ContinuousQueryAutoConfiguration.class)
@EnablePools(pools = {
    @EnablePool(name = "site1"),
    @EnablePool(name = "site2")
  }
)
public class Client {

  @Autowired
  GemFireCache cache;

  // Note: Autowiring causes the pools to be created
  @Autowired
  @Qualifier("site1")
  Pool site1Pool;

  @Autowired
  @Qualifier("site2")
  Pool site2Pool;

  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  public static void main(String[] args) {
    new SpringApplicationBuilder(Client.class)
      .build()
      .run(args);
  }

  @Bean
  ApplicationRunner runner() {
    return args -> {
      List<String> operations = args.getOptionValues("operation");
      String operation = operations.get(0);
      String parameter1 = (args.containsOption("parameter1")) ? args.getOptionValues("parameter1").get(0) : null;
      switch (operation) {
      case "run-scenario-1":
        runScenario1(parameter1);
        break;
      case "run-scenario-2":
        runScenario2(parameter1);
        break;
      case "run-scenario-3":
        runScenario3(parameter1);
        break;
      case "run-scenario-4":
        runScenario4(parameter1);
        break;
      case "run-scenario-5":
        runScenario5(parameter1);
        break;
      case "clear-region":
        clearRegion(parameter1);
        break;
      case "verify-region":
        runVerification(parameter1);
        break;
      }};
  }

  void runScenario1(String regionName) {
    Random random = new Random();

    // Do puts in site1 region. These are replicated to site 2.
    doPuts(regionName, this.site1Pool, 20, random);
  }

  void runScenario2(String regionName) {
    Random random = new Random();

    // Do puts in site1 region. These are replicated to site 2.
    doPuts(regionName, this.site1Pool, 20, random);

    // Do puts in site 2 region. These are not replicated to site 1.
    doPuts(regionName, this.site2Pool, 10, random);
  }

  void runScenario3(String regionName) {
    Random random = new Random();

    // Do puts in site2 region. These are not replicated to site 1.
    doPuts(regionName, this.site2Pool, 20, random);
  }

  void runScenario4(String regionName) {
    Random random = new Random();

    // Do puts in site1 region with the GatewaySender paused. These are not replicated to site 2.
    doPuts(regionName, this.site1Pool, 20, random);
  }

  void runScenario5(String regionName) {
    Random random = new Random();

    // Do puts in site1 region. These are not replicated to site 2.
    doPuts(regionName, this.site1Pool, 20, random, 0);

    // Do puts in site 2 region. These are not replicated to site 1.
    doPuts(regionName, this.site2Pool, 20, random, 1);
  }

  private void doPuts(String regionName, Pool pool, int numEntries, Random random) {
    logger.info("Putting into region={}, site={}; numEntries={}", regionName, pool.getName(), numEntries);
    Region region = createRegion(regionName, pool.getName());
    for (int i=0; i<numEntries; i++) {
      put(region, i, random);
    }
    closeRegion(region);
  }

  private void doPuts(String regionName, Pool pool, int numEntries, Random random, int remainder) {
    logger.info("Putting into region={}, site={}; numEntries={}", regionName, pool.getName(), numEntries);
    Region region = createRegion(regionName, pool.getName());
    for (int i=0; i<numEntries; i++) {
      if (i % 2  == remainder) {
        put(region, i, random);
      }
    }
    closeRegion(region);
  }

  private void put(Region region, int index, Random random) {
    String key = String.valueOf(index);
    Trade value = new Trade(key, CusipHelper.getCusip(), random.nextInt(100), new BigDecimal(BigInteger.valueOf(random.nextInt(100000)), 2));
    region.put(key, value);
    logger.info("\tkey={}, value={}", key, value);
  }

  private void clearRegion(String regionName) {
    // Do destroys in site1 region. These are replicated across the WAN
    doDestroys(regionName, this.site1Pool, 20);
  }

  private void doDestroys(String regionName, Pool pool, int numEntries) {
    logger.info("Destroying from region={}, site={}; numEntries={}", regionName, pool.getName(), numEntries);
    Region region = createRegion(regionName, pool.getName());
    for (int i=0; i<numEntries; i++) {
      String key = String.valueOf(i);
      region.destroy(key);
      logger.info("\tkey={}", key);
    }
    closeRegion(region);
  }

  private void runVerification(String regionName) {
    // Create the WanVerificationService
    WanVerificationService service = new WanVerificationService(this.cache, this.site1Pool, this.site2Pool);

    // Verify the region using the default ValueComparer
    service.verify(regionName);

    // Verify the region using a custom ValueComparer
    /*
    ValueComparer comparer = new ValueComparer() {
      @Override
      public boolean compare(Object value1, Object value2) {
        logger.info("Client.compare comparing value1=" + value1 + "; value2=" + value2);
        return value1.equals(value2);
      }
    };
    service.verify(regionName, comparer);
    */
  }

  private Region createRegion(String regionName, String siteName) {
    return ((ClientCache) this.cache)
      .createClientRegionFactory(ClientRegionShortcut.PROXY)
      .setPoolName(siteName)
      .create(regionName);
  }

  private void closeRegion(Region region) {
    region.close();
    ((InternalCache) this.cache).getClientMetadataService().close();
  }
}
