package example.client.verifier;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.internal.cache.InternalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class WanVerificationService {

  private final GemFireCache cache;

  private final Pool site1Pool;

  private final Pool site2Pool;

  private final StringBuilder builder;

  private boolean allValuesAreEqual;

  private static final Logger logger = LoggerFactory.getLogger(WanVerificationService.class);

  public WanVerificationService(GemFireCache cache, Pool site1Pool, Pool site2Pool) {
    this.cache = cache;
    this.site1Pool = site1Pool;
    this.site2Pool = site2Pool;
    this.builder = new StringBuilder();
    this.allValuesAreEqual = true;
  }

  public void verify(String regionName) {
    verify(regionName, new ValueComparer() {});
  }

  public void verify(String regionName, ValueComparer valueComparer) {
    // Get the region's key set in site 1
    Set site1Keys = getKeySet(regionName, this.site1Pool.getName());

    // Get the region's key set in site 2
    Set site2Keys = getKeySet(regionName, this.site2Pool.getName());

    // Compare the key sets
    this.builder.append("\nVerifying entries for region=").append(regionName);
    boolean allKeysAreEqual = compareKeySets(site1Keys, site2Keys);

    // Compare values in site 1 to those in site 2
    compareAllValues(regionName, valueComparer, site1Keys, 1, 2);

    // Compare values in site 2 to those in site 1 if the keys aren't equal
    if (!allKeysAreEqual) {
      compareAllValues(regionName, valueComparer, site2Keys, 2, 1);
    }

    // Log the comparison
    logComparison();

    // Reset for reuse
    reset();
  }

  private Set getKeySet(String regionName, String siteName) {
    Region region = createRegion(regionName, siteName);
    Set keySet = region.keySetOnServer();
    closeRegion(region);
    return keySet;
  }

  private boolean compareKeySets(Set site1Keys, Set site2Keys) {
    this.builder
      .append("\n\n==============")
      .append("\nComparing keys")
      .append("\n==============");
    boolean allKeysAreEqual = site1Keys.equals(site2Keys);
    if (allKeysAreEqual) {
      this.builder.append("\nAll ").append(site1Keys.size()).append(" keys are equal");
    } else {
      this.builder.append("\nAll keys are not equal. Site 1 contains ").append(site1Keys.size()).append(" keys. Site 2 contains ").append(site2Keys.size())
        .append(" keys.");
      Set site1Differences = new HashSet(site1Keys);
      site1Differences.removeIf(site2Keys::contains);
      this.builder.append("\nSite 1 contains these ").append(site1Differences.size()).append(" keys not found in site 2: ").append(site1Differences);
      Set site2Differences = new HashSet(site2Keys);
      site2Differences.removeIf(site1Keys::contains);
      this.builder.append("\nSite 2 contains these ").append(site2Differences.size()).append(" keys not found in site 1: ").append(site2Differences);
    }
    return allKeysAreEqual;
  }

  private void compareAllValues(String regionName, ValueComparer valueComparer, Set keys, int fromSite, int toSite) {
    this.builder
      .append("\n\n=============================================")
      .append("\nComparing values in site ")
      .append(fromSite)
      .append(" to those in site ")
      .append(toSite)
      .append("\n=============================================");
    this.allValuesAreEqual = true;
    keys.forEach(key -> compareSingleValues(regionName, valueComparer, key));
    if (this.allValuesAreEqual) {
      this.builder
        .append("\nAll values in site ")
        .append(fromSite)
        .append(" are equal to those in site ")
        .append(toSite);
    }
  }

  private void compareSingleValues(String regionName, ValueComparer valueComparer, Object key) {
    // Get the value in site 1
    Region site1Region = createRegion(regionName, this.site1Pool.getName());
    Object site1Value = site1Region.get(key);
    closeRegion(site1Region);

    // Get the value in site 2
    Region site2Region = createRegion(regionName, this.site2Pool.getName());
    Object site2Value = site2Region.get(key);
    closeRegion(site2Region);

    // Compare the values
    boolean valuesAreEqual;
    if (site1Value == null && site2Value == null) {
      valuesAreEqual = true;
    } else if (site1Value == null) {
      valuesAreEqual = valueComparer.compare(site2Value, site1Value);
    } else {
      valuesAreEqual = valueComparer.compare(site1Value, site2Value);
    }

    if (!valuesAreEqual) {
      this.builder
        .append("\nValues are not equal for key=")
        .append(key)
        .append("; site1Value=")
        .append(site1Value)
        .append("; site2Value=")
        .append(site2Value);
      this.allValuesAreEqual = false;
    }
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

  private void logComparison() {
    logger.info(this.builder.toString());
  }

  private void reset() {
    this.builder.setLength(0);
    this.allValuesAreEqual = true;
  }
}
