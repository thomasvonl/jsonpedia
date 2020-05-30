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

/*
 * JSONpedia v1.1 jquery plugin.
 */

function JSONpedia() {
    var _params;
    var _deferred = $.Deferred();

    function perform(params) {
        var PORT = document.location.port;
        var SERVER = 'http://' + document.location.hostname + (PORT.length > 0 ? ":" + PORT : "");

        if(params.performing) return;
        params.performing = true;
        if('processors' in params) {
            request = SERVER + '/annotate/resource/' + params.out + '/' + params.entity + '?&procs=' + params.processors.join(',');
        } else if('mongo' in params) {
            call = params.mongo[0];
            method = call[0];
            if('select' == method) {
                request = SERVER + '/storage/mongo/select?q=' + encodeURIComponent(call[1]) +
                        "&filter=" + encodeURIComponent(call[2]) +
                        "&limit=" + call[3];
            } else if('mapred' == method) {
                request = SERVER + '/storage/mongo/mapred?criteria=' + encodeURIComponent(call[1]) +
                        "&map=" + encodeURIComponent(call[2]) +
                        "&reduce=" + encodeURIComponent(call[3]) +
                        "&limit=" + call[4];
            } else throw new Error();
        } else if('elastic' in params) {
            call = params.elastic[0];
            request = SERVER + '/storage/elastic/select?q=' + encodeURIComponent(call[1]) +
                        "&filter=" + encodeURIComponent(call[2]) +
                        "&limit=" + call[3];
        } else throw new Error();
        console.log('Performing request: ' + request);
        $.get(request)
                .done(
                function (data) {
                    _deferred.resolve(_params, data, null)
                }
        )
                .fail(
                function (xhr, status, error) {
                    _deferred.resolve(_params, null, error + "[" + xhr.status + "]")
                }
        );
    }

    function handleReply(params, data, err) {
        if(params.handled) return;
        params.handled = true;
        if(data) params.done(data);
        if(err)  params.fail(err);
    }

    _handlers = {
        done: function(callback) {
            _params.done = callback;
            _deferred.done(handleReply);
            perform(_params);
            return _handlers;
        },
        fail: function(callback) {
            _params.fail = callback;
            _deferred.done(handleReply);
            perform(_params);
            return _handlers;
        }
    };

    _methods = {
        extractors: function () {
            _params.processors.push('Extractors');
            return _methods;
        },
        linkers: function () {
            _params.processors.push('Linkers');
            return _methods;
        },
        splitters: function () {
            _params.processors.push('Splitters');
            return _methods;
        },
        structure: function () {
            _params.processors.push('Structure');
            return _methods;
        },
        validate: function () {
            _params.processors.push('Validate');
            return _methods;
        },
        json: function () {
            _params.out = 'json';
            return _handlers;
        },
        html: function () {
            _params.out = 'html';
            return _handlers;
        }
    };

    _annotate = function (entity) {
        _params = { processors : [] };
        _params.entity = entity;
        return _methods;
    };

    _mongo_methods = {
        select: function(selector, filter, limit) {
            _params.mongo.push(['select', selector, filter, limit]);
            return _handlers;
        },
        mapred: function(criteria, map, reduce, limit) {
            _params.mongo.push(['mapred', criteria, map, reduce, limit]);
            return _handlers;
        }
    };

    _mongo = function() {
        _params = {mongo :[]};
        return _mongo_methods;
    };

    _elastic_methods = {
        select: function(selector, filter, limit) {
            _params.elastic.push(['select', selector, filter, limit]);
            return _handlers;
        }
    };

    _elastic = function() {
        _params = {elastic :[]};
        return _elastic_methods;
    };

    return {
        annotate: _annotate,
        mongo: _mongo,
        elastic: _elastic
    };
}

