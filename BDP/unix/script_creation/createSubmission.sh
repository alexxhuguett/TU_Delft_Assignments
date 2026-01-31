#!/usr/bin/env bash
# The Unix assignment is almost over, time to create a submission.
# You could create a zip folder by hand. Just place the '.sh' files in there, but where's the fun in that.
# Let's create a script that does this for us.
# This script should take an output name as first parameter
# If called in a directory it should recursively find all the .sh files and add them to a zip
# Zip should only contain .sh files and no folders.

# Write code below

output="$1"
find . -type f -name '*.sh' | xargs zip -j "$output"

# To submit on CPM, run from the parent directory (unix).
