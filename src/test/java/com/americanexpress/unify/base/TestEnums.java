/*
 * Copyright 2020 American Express Travel Related Services Company, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.americanexpress.unify.base;

/*
 * @author Benjamin Kats
 */
public class TestEnums {

  public enum journey_name {

    SOME_JOURNEY_1("some_journey_1"),
    SOME_JOURNEY_2("some_journey_2"),
    SOME_JOURNEY_3("some_journey_3"),
    SOME_JOURNEY_4("some_journey_4"),
    SOME_JOURNEY_5("some_journey_5");

    private String journeyName;

    journey_name(String journeyName) {
      this.journeyName = journeyName;
    }

    public String getJourneyName() {
      return journeyName;
    }
  }

}
