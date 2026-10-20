#!/bin/sh
# Renders every diagram of this folder to SVG next to its source. Needs plantuml and graphviz.
cd "$(dirname "$0")" && exec plantuml -tsvg -failfast2 [0-9]*.puml
