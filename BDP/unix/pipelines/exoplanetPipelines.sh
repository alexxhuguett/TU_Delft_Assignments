#!/usr/bin/env bash
echo "Running exoplanetProcessing.sh"

# ---- APACHE ----
cd ./../data/planets || exit

# -- Q1 --
echo "-- Q1 --"
# Write a pipline that for all planets that were discovered using Pulsar Timing displays the discovery year, name  and facility separated by spaces.
# Example: 2017 PSR B0329+54 b Multiple Facilities
pulsarTiming=$(grep 'Pulsar Timing' exoplanets |  awk -F',' '{print $6 $1 $7}')
# Print pulsarTiming
echo "Pulsar Timing:"
echo "$pulsarTiming"


# -- Q2 --
echo "-- Q2 --"
# Write a pipeline that finds the year in which most exoplanets were discovered. Also provide the number of planets discovered that year.
# Example: 1505 2016
highestYear=$(awk -F',' '{print $6}' exoplanets | sort | uniq -c | sort -rn | head -n 1 | awk '{print $1, $2}')
echo "Highest year:"
echo "$highestYear"

# -- Q3 --
echo "-- Q3 --"
# Write a pipeline that counts the number of exoplanets discovered between 1997 and 2006
intervalPlanets=$(cat exoplanets | tail -n +2 | awk -F',' '$6 >= 1997 && $6 <= 2006' | wc -l)
echo "Planets in interval:"
echo "$intervalPlanets"

# -- Q4 --
echo "-- Q4 --"
# Write a pipeline that outputs the names of two exoplanets that have the highest number of stars in the planetary system.
highestStars=$(cat exoplanets | tail -n +2 | cut -d',' -f1,3 | sort -t',' -nr -k2 | head -n 2 | cut -d',' -f1)
echo "$highestStars"
cd ../../pipelines/ || exit
