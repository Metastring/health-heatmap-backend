/*
 *    Copyright 2020 Metastring Foundation
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.metastringfoundation.healthheatmap.entities;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Embeddable
public class Gender {
    public enum GenderType {
        ANY,
        MALE,
        FEMALE,
        OTHER
    }

    @Enumerated(EnumType.STRING)
    private GenderType gender;

    public static GenderType getGenderTypeFromString(String genderType) throws IllegalArgumentException {
        List<String> anyList = new ArrayList<>(Arrays.asList("any", "total", "all", ""));
        if (anyList.contains(genderType.toLowerCase())) return GenderType.ANY;

        List<String> maleList = new ArrayList<>(Arrays.asList("male", "m"));
        if (maleList.contains(genderType.toLowerCase())) return GenderType.MALE;

        List<String> femaleList = new ArrayList<>(Arrays.asList("female", "f"));
        if (femaleList.contains(genderType.toLowerCase())) return GenderType.FEMALE;

        List<String> otherList = new ArrayList<>(Arrays.asList("other", "transgender", "tg"));
        if (otherList.contains(genderType.toLowerCase())) return GenderType.OTHER;

        throw new IllegalArgumentException("Unknown gender type: " + genderType);
    }

    public static Gender getDefault() {
        Gender gender = new Gender();
        gender.setGender(GenderType.ANY);
        return gender;
    }

    public GenderType getGender() {
        return gender;
    }

    public void setGender(GenderType gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Gender{" +
                "gender=" + gender +
                '}';
    }
}
