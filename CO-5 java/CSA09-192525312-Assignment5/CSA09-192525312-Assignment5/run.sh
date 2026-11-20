#!/bin/bash
java "$@" 2>&1 | grep -v "Picked up"
