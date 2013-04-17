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
package org.kiji.scoring.impl;

import org.apache.hadoop.conf.Configuration;

import org.kiji.schema.KijiColumnName;
import org.kiji.schema.KijiDataRequest;
import org.kiji.scoring.PolicyContext;

/**
 * Internal implementation of PolicyContext for providing KijiFreshnessPolicy instances access to
 * outside data.
 */
public final class InternalPolicyContext implements PolicyContext {
  private final Configuration mConf;
  private final KijiColumnName mAttachedColumn;
  private final KijiDataRequest mUserRequest;

  /**
   * Creates a new InternalPolicyContext to give freshness policies access to outside data.
   *
   * @param userRequest The client data request which necessitates a freshness check.
   * @param attachedColumn The column to which the freshness policy is attached.
   * @param conf The Configuration from the Kiji instance housing the KijiTable from which this
   *   FreshKijiTableReader reads.
   */
  public InternalPolicyContext(
      KijiDataRequest userRequest, KijiColumnName attachedColumn, Configuration conf) {
    mUserRequest = userRequest;
    mAttachedColumn = attachedColumn;
    mConf = conf;
  }

  /** {@inheritDoc} */
  @Override
  public KijiDataRequest getUserRequest() {
    return mUserRequest;
  }

  /** {@inheritDoc} */
  @Override
  public KijiColumnName getAttachedColumn() {
    return mAttachedColumn;
  }

  /** {@inheritDoc} */
  @Override
  public Configuration getConfiguration() {
    return mConf;
  }
}
