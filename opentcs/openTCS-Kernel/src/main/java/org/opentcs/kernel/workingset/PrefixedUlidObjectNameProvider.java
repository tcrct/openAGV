/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import de.huxhorn.sulky.ulid.ULID;
import org.opentcs.access.to.CreationTO;
import org.opentcs.components.kernel.ObjectNameProvider;

import static java.util.Objects.requireNonNull;

/**
 * Provides names for objects based on ULIDs, prefixed with the name taken from a given
 * {@link CreationTO}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
// TODO 订单ID的生产者
public class PrefixedUlidObjectNameProvider
    implements ObjectNameProvider {

  /**
   * Generates ULIDs for us.
   */
  private final ULID ulid = new ULID();

  /**
   * Creates a new instance.
   */
  public PrefixedUlidObjectNameProvider() {
  }

  @Override
  public String apply(CreationTO to) {
    requireNonNull(to, "to");

    return to.getName() + ulid.nextULID();
  }
}
