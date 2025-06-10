#!/usr/bin/perl -0pi
s/DSL.name\("([A-Z_]+)"\)/"DSL.name(\"" . lc($1) . "\")"/ge;
