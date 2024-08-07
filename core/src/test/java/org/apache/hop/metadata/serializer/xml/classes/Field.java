/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.metadata.serializer.xml.classes;

import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IIntCodeConverter;

public class Field {

  @HopMetadataProperty String name;

  @HopMetadataProperty String type;

  @HopMetadataProperty int length;

  @HopMetadataProperty int precision;

  @HopMetadataProperty String format;

  @HopMetadataProperty(key = "ott")
  private TestEnum oneTwoThree;

  @HopMetadataProperty(key = "test_code", intCodeConverter = TestCodeConverter.class)
  private int testCode;

  public Field() {}

  public Field(
      String name, String type, int length, int precision, String format, TestEnum oneTwoThree) {
    this.name = name;
    this.type = type;
    this.length = length;
    this.precision = precision;
    this.format = format;
    this.oneTwoThree = oneTwoThree;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Field field = (Field) o;
    return length == field.length
        && precision == field.precision
        && StringUtils.equals(name, field.name)
        && StringUtils.equals(type, field.type)
        && StringUtils.equals(format, field.format)
        && oneTwoThree == field.oneTwoThree;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, length, precision, format, oneTwoThree);
  }

  public static final class TestCodeConverter implements IIntCodeConverter {
    public TestCodeConverter() {
      // Do nothing
    }

    @Override
    public String getCode(int type) {
      switch (type) {
        case 0:
          return "NONE";
        case 1:
          return "ONE";
        case 2:
          return "TWO";
        case 3:
          return "THREE";
        default:
          return "NONE";
      }
    }

    @Override
    public int getType(String code) {
      switch (code) {
        case "ONE":
          return 1;
        case "TWO":
          return 2;
        case "THREE":
          return 3;
        default:
          return 0;
      }
    }
  }

  /**
   * Gets name
   *
   * @return value of name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name The name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets type
   *
   * @return value of type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type The type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets length
   *
   * @return value of length
   */
  public int getLength() {
    return length;
  }

  /**
   * @param length The length to set
   */
  public void setLength(int length) {
    this.length = length;
  }

  /**
   * Gets precision
   *
   * @return value of precision
   */
  public int getPrecision() {
    return precision;
  }

  /**
   * @param precision The precision to set
   */
  public void setPrecision(int precision) {
    this.precision = precision;
  }

  /**
   * Gets format
   *
   * @return value of format
   */
  public String getFormat() {
    return format;
  }

  /**
   * @param format The format to set
   */
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   * Gets oneTwoThree
   *
   * @return value of oneTwoThree
   */
  public TestEnum getOneTwoThree() {
    return oneTwoThree;
  }

  /**
   * @param oneTwoThree The oneTwoThree to set
   */
  public void setOneTwoThree(TestEnum oneTwoThree) {
    this.oneTwoThree = oneTwoThree;
  }

  /**
   * Gets testCode
   *
   * @return value of testCode
   */
  public int getTestCode() {
    return testCode;
  }

  /**
   * Sets testCode
   *
   * @param testCode value of testCode
   */
  public void setTestCode(int testCode) {
    this.testCode = testCode;
  }
}
