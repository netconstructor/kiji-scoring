/**
 * (c) Copyright 2013 WibiData, Inc.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kiji.scoring;

import java.io.IOException;

import com.google.common.base.Preconditions;

import org.kiji.annotations.ApiAudience;
import org.kiji.annotations.ApiStability;
import org.kiji.schema.InternalKijiError;
import org.kiji.schema.KijiTable;
import org.kiji.scoring.impl.InternalFreshKijiTableReader;

/**
 * Builder for configuring options for FreshKijiTableReaders.
 *
 * <p>
 *   Allows the setting of options for creation of FreshKijiTableReaders.  Options include setting
 *   the reader type as enumerated in FreshKijiTableReaderBuilder.FreshReaderType (defaults to
 *   local), setting the time (in milliseconds) to wait for freshening to occur (defaults to 100
 *   ms), setting the period (in milliseconds) between automatically reloading freshness policies
 *   from the meta table (defaults to never automatically reloading), setting whether to allow
 *   partially fresh data to be returned by calls to {@link org.kiji.scoring.FreshKijiTableReader
 *   #get(org.kiji.schema.EntityId, org.kiji.schema.KijiDataRequest)} (defaults to false), and a
 *   required setting for the table from which to read.
 * </p>
 *
 * <p>
 *   To create a new FreshKijiTableReader:
 * </p>
 * <p><pre>
 *   final FreshKijiTableReader = FreshKijiTableReaderBuilder.get()
 *       .withReaderType(FreshReaderType.LOCAL)
 *       .withTable(myTable)
 *       .withTimeout(100)
 *       .withAutomaticReload(3600000)
 *       .returnPartialFreshData(true)
 *       build();
 * </pre></p>
 */
@ApiAudience.Public
@ApiStability.Experimental
public final class FreshKijiTableReaderBuilder {
  /** Do not allow returning partially fresh data by default. */
  private static final Boolean DEFAULT_PARTIAL_FRESHENING = false;
  /** Create local FreshKijiTableReaders by default. */
  private static final FreshReaderType DEFAULT_READER_TYPE = FreshReaderType.LOCAL;
  /** Wait 100 milliseconds for freshening to occur by default. */
  private static final int DEFAULT_TIMEOUT = 100;

  /**
   * Get a new instance of FreshKijiTableReaderBuilder.
   *
   * @return a new instance of FreshKijiTableReaderBuilder.
   */
  public static FreshKijiTableReaderBuilder get() {
    return new FreshKijiTableReaderBuilder();
  }

  /** Enumeration of types of fresh readers. */
  public static enum FreshReaderType {
    LOCAL
  }

  /** The type of FreshKijiTableReader to build. */
  private FreshReaderType mReaderType;
  /** The KijiTable from which the new reader will read. */
  private KijiTable mTable;
  /** The time in milliseconds the new reader will wait for freshening to occur. */
  private long mTimeout;
  /** The time in milliseconds the new reader will wait between reloading freshness policies. */
  private long mReload;
  /** Whether or not the new reader will return partially fresh data when available. */
  private Boolean mPartialFresh;

  /**
   * Select the type of FreshKijiTableReader to instantiate.  Types are enumerated in
   * FreshKijiTableReaderBuilder.FreshReaderType.
   *
   * @param type the type of reader to instantiate.
   * @return this FreshKijiTableReaderBuilder configured to build the given type of reader.
   */
  public FreshKijiTableReaderBuilder withReaderType(FreshReaderType type) {
    Preconditions.checkArgument(mReaderType == null, "Reader type already set to: %s", mReaderType);
    mReaderType = type;
    return this;
  }

  /**
   * Configure the FreshKijiTableReader to read from the given KijiTable.
   *
   * @param table the KijiTable from which to read.
   * @return this FreshKijiTableReaderBuilder configured to read from the given table.
   */
  public FreshKijiTableReaderBuilder withTable(KijiTable table) {
    Preconditions.checkArgument(mTable == null, "KijiTable already set to: %s", mTable);
    mTable = table;
    return this;
  }

  /**
   * Configure the FreshKijiTableReader to wait a given number of milliseconds before returning
   * stale data.
   *
   * @param timeout the duration in milliseconds to wait before returning stale data.
   * @return this FreshKijiTableReaderBuilder configured to wait the given number of milliseconds
   * before returning stale data.
   */
  public FreshKijiTableReaderBuilder withTimeout(int timeout) {
    Preconditions.checkArgument(timeout > 0, "Timeout must be positive, got: %d", timeout);
    Preconditions.checkArgument(mTimeout == 0, "Timeout is already set to: %d", mTimeout);
    mTimeout = timeout;
    return this;
  }

  /**
   * Configure the FreshKijiTableReader to automatically reload freshness policies from the meta
   * table on a scheduled interval.
   *
   * @param reload the interval between automatic reloads in milliseconds.
   * @return this FreshKijiTableReaderBuilder configured to automatically reload on the given
   * interval.
   */
  public FreshKijiTableReaderBuilder withAutomaticReload(long reload) {
    Preconditions.checkArgument(reload > 0, "Reload time must be positive, got: %s", reload);
    Preconditions.checkArgument(mReload == 0, "Reload time is already set to: %d", mReload);
    mReload = reload;
    return this;
  }

  /**
   * Configure the FreshKijiTableReader to return partially fresh data when available.  This
   * options may increase the time to return for certain calls to
   * {@link FreshKijiTableReader#get(org.kiji.schema.EntityId, org.kiji.schema.KijiDataRequest)}.
   *
   * @param partial whether the FreshKijiTableReader should return partially freshened data when
   * available.
   * @return this FreshKijiTableReaderBuilder configured allow returning partially freshened data.
   */
  public FreshKijiTableReaderBuilder returnPartiallyFreshData(boolean partial) {
    Preconditions.checkArgument(
        mPartialFresh == null, "Partial freshening is already set to: %s", mPartialFresh);
    mPartialFresh = partial;
    return this;
  }

  /**
   * Builds a FreshKijiTableReader with the configured options.
   *
   * @return a FreshKijiTableReader with the configured options.
   * @throws IOException in case of an error creating the FreshKijiTableReader.
   */
  public FreshKijiTableReader build() throws IOException {
    Preconditions.checkState(mTable != null, "Target table must be set in order to build.");
    if (mReaderType == null) {
      mReaderType = DEFAULT_READER_TYPE;
    }
    if (mTimeout == 0) {
      mTimeout = DEFAULT_TIMEOUT;
    }
    if (mPartialFresh == null) {
      mPartialFresh = DEFAULT_PARTIAL_FRESHENING;
    }
    switch (mReaderType) {
      case LOCAL:
        return new InternalFreshKijiTableReader(mTable, mTimeout, mReload, mPartialFresh);
      default:
        throw new InternalKijiError(String.format("Unknown reader type: %s", mReaderType));
    }
  }
}