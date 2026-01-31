#!/usr/bin/env bash
# ---- MY AMAZING BOOK ----
cd ./../data/myBook/ || exit

# -- Q1 --
echo "-- Q1 --"
# Write a pipeline that for the current directory prints the 10 most common letters in all text files.
# Your pipeline should be case insensitive and ignore punctuation and space characters.
# Example output:
# 309 e
# 229 t
mostCommonLetters=$(cat * | tr -cd 'A-Za-z' | tr '[:upper:]' '[:lower:]' | fold -w1 | sort | uniq -c | sort -nr | head -n 10)
# Prints the mostCommonLetters
echo "Most common letters in my book:"
echo "$mostCommonLetters"

# -- Q2 --
# Write a pipeline that finds the number of words in the book that have been used only once.
# Your pipeline should be case insensitive
echo "-- Q2 --"
onlyOnce=$(cat *.txt | tr '[:upper:]' '[:lower:]' | tr -c '[:alnum:]' '[\n*]' | sort | uniq -c | grep -E '^\s*1\s' | wc -l)                                # Count how many lines (words) there are
echo "Words used only once:"
echo "$onlyOnce"

cd ../../pipelines/ || exit
