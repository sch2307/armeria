/*
 * Copyright 2019 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linecorp.armeria.client.endpoint.healthcheck;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.linecorp.armeria.client.Endpoint;

class PartialHealthCheckStrategyTest {

    private static List<Endpoint> createCandidates(int size) {
        final Random random = new Random();

        return IntStream.range(0, size)
                        .mapToObj(i -> Endpoint.of("dummy" + random.nextInt()))
                        .collect(toImmutableList());
    }

    private static void assertUniqueCandidates(List<Endpoint> candidates) {
        if (candidates.isEmpty()) {
            return;
        }

        assertThat(candidates).hasSameSizeAs(ImmutableSet.copyOf(candidates));
    }

    @Test
    void getCandidatesWithRatio() {
        final List<Endpoint> candidates = createCandidates(10);
        final List<Endpoint> selectedCandidates = HealthCheckStrategy.ofRatio(0.9)
                                                                     .select(candidates);
        assertThat(selectedCandidates).hasSize(9);
        assertThat(candidates).containsAll(selectedCandidates);
    }

    @Test
    void getCandidatesWithCount() {
        final List<Endpoint> candidates = createCandidates(6);
        final List<Endpoint> selectedCandidates = HealthCheckStrategy.ofCount(5)
                                                                     .select(candidates);
        assertThat(selectedCandidates).hasSize(5);
        assertThat(candidates).containsAll(selectedCandidates);
    }

    @Test
    void shouldReturnUniqueCandidates() {
        final List<Endpoint> endpoints =
                ImmutableList.of(Endpoint.of("foo"), Endpoint.of("foo"), Endpoint.of("bar"));
        List<Endpoint> selectedCandidates = HealthCheckStrategy.ofCount(2).select(endpoints);
        assertThat(selectedCandidates).hasSize(2);
        assertUniqueCandidates(selectedCandidates);

        selectedCandidates = HealthCheckStrategy.ofRatio(1).select(endpoints);
        assertThat(selectedCandidates).hasSize(2);
        assertUniqueCandidates(selectedCandidates);
    }
}
