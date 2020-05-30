#!/usr/bin/env python
# -*- coding: utf-8 -*-

# JSONpedia - Convert any MediaWiki document to JSON.
#
# Written in 2014 by Michele Mostarda <mostarda@fbk.eu>.
#
# To the extent possible under law, the author has dedicated all copyright and related and
# neighboring rights to this software to the public domain worldwide.
# This software is distributed without any warranty.
#
# You should have received a copy of the CC BY Creative Commons Attribution 4.0 Internationa Public License.
# If not, see <https://creativecommons.org/licenses/by/4.0/legalcode>.


# == facet_loader.py -s <source-URI> -d <destination-URI> -l <limit-num> -c <config-file> ==
#
# Example usage:
#   $ bin/facet_loader.py -s localhost:9300:jsonpedia_test_load:en -d localhost:9300:jsonpedia_test_facet:en -l 100 -c conf/faceting.properties

import subprocess

MVN_BIN = 'mvn'
MVN_HEAP_SIZE = '8g'
LOADER = 'com.machinelinking.cli.facetloader'

if __name__ == '__main__':
    import sys
    if len(sys.argv) != 9:
        print 'Usage: $0 -s <source-URI> -d <destination-URI> -l <limit-num> -c <config-file>'
        sys.exit(1)

    cmd = "MAVEN_OPTS='-Xms%s -Xmx%s -Dlog4j.configuration=file:conf/log4j.properties' %s exec:java -Dexec.mainClass=%s -Dexec.args='%s'" \
            % (MVN_HEAP_SIZE, MVN_HEAP_SIZE, MVN_BIN, LOADER, ' '.join(sys.argv[1:]))
    print 'Executing command:', cmd
    subprocess.check_call(cmd, shell=True)
