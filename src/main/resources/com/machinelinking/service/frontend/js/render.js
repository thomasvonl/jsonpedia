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

$(function(){
    var jsonPathVisible      = false;
    var defaultRenderVisible = false;

    function setDefaultRenderVisible(visible) {
        $('#toggle-defaultrender').attr('value', (visible ? 'Hide' : 'Show') + ' Default Render');
        if (visible) {
            $('.defaultrender').show();
        } else {
            $('.defaultrender').hide();
        }

    }

    setDefaultRenderVisible(defaultRenderVisible);
    $('#toggle-defaultrender').click(function(){
        defaultRenderVisible = !defaultRenderVisible;
        setDefaultRenderVisible(defaultRenderVisible);
    });

    modified = [];
    $('#type-filter,#name-filter').change(function(){
        typeFilter = $('#type-filter').val();
        nameFilter = $('#name-filter').val();
        for(var i in modified) {
            modified[i].attr('style', 'background-color: white')
        }
        modified = [];
        filter = "";
        if(typeFilter.length > 0) {
           filter = "[itemtype^='" + typeFilter + "']";
        }
        if(nameFilter.length > 0) {
            filter += "[name^='" + nameFilter + "']";
        }
        $(filter).each(function(i,v){
            $v = $(v);
            $v.attr('style', 'background-color: red');
            modified.push($v);
        });
        $('#search-report').text( modified.length + ' elements found.')
    });

    // jsonpath tooltip.
    $('div[title]').tooltip();
    $('span[title]').tooltip();

    // mappings accordion
    $(function () {
        $(".mapping-accordion").accordion({
            collapsible: true,
            active: false
        });
    });
});