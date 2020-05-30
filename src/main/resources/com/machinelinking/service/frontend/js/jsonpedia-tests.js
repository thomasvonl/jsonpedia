/*
 * Copyright 2012-2015 Michele Mostarda (me@michelemostarda.it)
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

asyncTest("annotate", function() {
    expect(1);
    var c = new JSONpedia();
    c.annotate('en:Albert_Einstein').extractors()
            .linkers().splitters().structure().validate()
            .json()
            .done(
                function (data) {
                    ok(true, 'Loaded data: ' + data);
                    start();
                }
            )
            .fail(
                function(err) {
                    ok(false, 'Error while loading data: ' + err);
                    start();
                }
            );
});

asyncTest("mongo select", function() {
    expect(1);
    var c = new JSONpedia();
    c.mongo().select('_id = #736 -> title', '@type : link', 1)
            .done(
                function (data) {
                    ok(true, 'Loaded data: ' + data);
                    start();
                }
            )
            .fail(
                function(err) {
                    ok(false, 'Error while loading data: ' + err);
                    start();
                }
            );
});

asyncTest("mongo mapred", function() {
    expect(1);
    var c = new JSONpedia();
    c.mongo().mapred(
            '_id = #736',
            'function() { ocs = this.content.templates.occurrences; for(template in ocs) { emit(template, ocs[template]); } }',
            'function(key, values) { return Array.sum(values) }',
            10)
            .done(
                function (data) {
                    ok(true, 'Loaded data: ' + data);
                    start();
                }
            )
            .fail(
                function(err) {
                    ok(false, 'Error while loading data: ' + err);
                    start();
                }
            );
});

asyncTest("elastic select", function() {
    expect(1);
    var c = new JSONpedia();
    c.elastic().select('Albert Einstein', '@type : link', 1)
            .done(
                function (data) {
                    ok(true, 'Loaded data: ' + data);
                    start();
                }
            )
            .fail(
                function(err) {
                    ok(false, 'Error while loading data: ' + err);
                    start();
                }
            );
});