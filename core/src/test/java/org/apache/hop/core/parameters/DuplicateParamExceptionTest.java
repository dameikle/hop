/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hop.core.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class DuplicateParamExceptionTest {
  DuplicateParamException exception;

  @Before
  public void setUp() throws Exception {
    // Do nothing
  }

  @Test
  public void testConstructors() {
    exception = new DuplicateParamException();
    assertNotNull(exception);
    NamedParamsExceptionTest.assertMessage("null", exception);

    exception = new DuplicateParamException("message");
    NamedParamsExceptionTest.assertMessage("message", exception);

    Throwable t = mock(Throwable.class);
    when(t.getStackTrace()).thenReturn(new StackTraceElement[0]);
    exception = new DuplicateParamException(t);
    assertEquals(t, exception.getCause());

    exception = new DuplicateParamException("message", t);
    NamedParamsExceptionTest.assertMessage("message", exception);
    assertEquals(t, exception.getCause());
  }
}
