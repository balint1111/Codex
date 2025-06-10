#!/usr/bin/perl -0pi
s/DSL.name\("([A-Z_]+)"\)/"DSL.name(\"" . lc($1) . "\")"/ge;
s/name = "([A-Z_]+)"/"name = \"" . lc($1) . "\""/ge;
