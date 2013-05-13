/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.DbTool.TableCreationCallback;
import com.opengamma.util.tuple.Triple;

/**
 * Tests the creation + upgrade sequence results in the same structure as a pure create.
 */
public abstract class AbstractDbUpgradeTest implements TableCreationCallback {

  private static final Map<String, Map<String, String>> s_targetSchema = Maps.newHashMap();

  private final List<Triple<String, String, String>> _comparisons = Lists.newLinkedList();

  private final String _tableSet;
  private final String _databaseType;
  private final String _targetVersion;
  private final String _createVersion;
  private volatile DbTool _dbTool;

  /**
   * Creates an instance.
   */
  protected AbstractDbUpgradeTest(String databaseType, String tableSet, final String targetVersion, final String createVersion) {
    ArgumentChecker.notNull(databaseType, "databaseType");
    _tableSet = tableSet;
    _databaseType = databaseType;
    _targetVersion = targetVersion;
    _createVersion = createVersion;
    _dbTool = DbTest.createDbTool(databaseType, null);
  }

  //-------------------------------------------------------------------------
  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    DbTool dbTool = _dbTool;
    dbTool.setTargetVersion(_targetVersion);
    dbTool.setCreateVersion(_createVersion);
    dbTool.dropTestSchema();
    dbTool.createTestSchema();
    dbTool.createTables(_tableSet, dbTool.getTestCatalog(), dbTool.getTestSchema(), Integer.parseInt(_targetVersion), Integer.parseInt(_createVersion), this);
    dbTool.clearTestTables();
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() {
    DbTest.s_databaseTypeVersion.clear();
  }

  protected Map<String, String> getVersionSchemas() {
    Map<String, String> versionSchema = s_targetSchema.get(_databaseType);
    if (versionSchema == null) {
      versionSchema = new HashMap<>();
      s_targetSchema.put(_databaseType, versionSchema);
    }
    return versionSchema;
  }

  //-------------------------------------------------------------------------
  @Test(groups = TestGroup.UNIT_DB)
  public void testDatabaseUpgrade() {
    for (Triple<String, String, String> comparison : _comparisons) {
      /*
       * System.out.println(comparison.getFirst() + " expected:");
       * System.out.println(comparison.getSecond());
       * System.out.println(comparison.getFirst() + " found:");
       * System.out.println(comparison.getThird());
       */
      int diff = StringUtils.indexOfDifference(comparison.getSecond(), comparison.getThird());
      if (diff >= 0) {
        System.err.println("Difference at " + diff);
        System.err.println("Upgraded --->..." + StringUtils.substring(comparison.getSecond(), diff - 200, diff) +
          "<-!!!->" + StringUtils.substring(comparison.getSecond(), diff, diff + 200) + "...");
        System.err.println(" Created --->..." + StringUtils.substring(comparison.getThird(), diff - 200, diff) +
          "<-!!!->" + StringUtils.substring(comparison.getThird(), diff, diff + 200) + "...");
      }
      assertEquals(_databaseType + ": " + comparison.getFirst(), comparison.getSecond(), comparison.getThird());
    }
  }

  @Override
  public void tablesCreatedOrUpgraded(final String version, final String prefix) {
    final Map<String, String> versionSchemas = getVersionSchemas();
    if (versionSchemas.containsKey(prefix + "_" + version)) {
      // if we've already done the full schema, then we want to test that this upgrade has given us the same (but defer the comparison)
      _comparisons.add(new Triple<String, String, String>(prefix + "_" + version, versionSchemas.get(prefix + "_" + version), _dbTool.describeDatabase(prefix)));
    } else {
      // tests are run with most recent full schema first, so we can store that as a reference
      versionSchemas.put(prefix + "_" + version, _dbTool.describeDatabase(prefix));
    }
  }

  @Override
  public String toString() {
    return _databaseType + "/" + _tableSet + ":" + _createVersion + " >>> " + _targetVersion;
  }

}
